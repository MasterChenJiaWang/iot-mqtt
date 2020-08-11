package com.daren.chen.iot.mqtt.general.transport.client.connection;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import com.daren.chen.iot.mqtt.general.api.AttributeKeys;
import com.daren.chen.iot.mqtt.general.api.MqttMessageApi;
import com.daren.chen.iot.mqtt.general.api.TransportConnection;
import com.daren.chen.iot.mqtt.general.api.client.RsocketClientSession;
import com.daren.chen.iot.mqtt.general.config.RsocketClientConfig;
import com.daren.chen.iot.mqtt.general.transport.client.handler.ClientMessageRouter;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import lombok.extern.slf4j.Slf4j;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.netty.NettyInbound;

@Slf4j
public class RsocketClientConnection implements RsocketClientSession {

    private final TransportConnection connection;

    private final RsocketClientConfig clientConfig;

    private final ClientMessageRouter clientMessageRouter;

    private final Set<String> topics = new HashSet<>();

    /**
     *
     * @param connection
     * @param clientConfig
     */
    public RsocketClientConnection(TransportConnection connection, RsocketClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.connection = connection;
        this.clientMessageRouter = new ClientMessageRouter(clientConfig);
        initHandler();
    }

    @Override
    public void initHandler() {
        RsocketClientConfig.Options options = clientConfig.getOptions();
        NettyInbound inbound = connection.getInbound();
        // 登录重试
        Disposable disposable = Mono.fromRunnable(() -> connection
            .write(MqttMessageApi.buildConnect(options.getClientIdentifier(), options.getWillTopic(),
                options.getWillMessage(), options.getUserName(), options.getPassword(), options.isHasUserName(),
                options.isHasPassword(), options.isHasWillFlag(), options.getWillQos(), clientConfig.getHeart()))
            .subscribe()).delaySubscription(Duration.ofSeconds(2)).repeat().subscribe();
        // 登录
        connection
            .write(MqttMessageApi.buildConnect(options.getClientIdentifier(), options.getWillTopic(),
                options.getWillMessage(), options.getUserName(), options.getPassword(), options.isHasUserName(),
                options.isHasPassword(), options.isHasWillFlag(), options.getWillQos(), clientConfig.getHeart()))
            .doOnError(throwable -> log.error(throwable.getMessage())).subscribe();
        // 保存 重试线程,用于在其他地方 关闭线程
        connection.getConnection().channel().attr(AttributeKeys.closeConnection).set(disposable);
        // 发送心跳
        connection.getConnection().onWriteIdle(clientConfig.getHeart(), () -> connection.sendPingReq().subscribe());
        // 发送心跳
        connection.getConnection().onReadIdle(clientConfig.getHeart() * 2, () -> connection.sendPingReq().subscribe());
        // 关闭 异常关闭
        connection.getConnection().onDispose(() -> clientConfig.getOnClose().run());
        // 收到消息 处理逻辑
        inbound.receiveObject().cast(MqttMessage.class)
            .subscribe(message -> clientMessageRouter.handler(message, connection));
        connection.getConnection().channel().attr(AttributeKeys.clientConnectionAttributeKey).set(this);
        // 重连后 需要把以前订阅的主题重新订阅
        // connection.getTopics()
        List<MqttTopicSubscription> mqttTopicSubscriptions =
            topics.stream().map(s -> new MqttTopicSubscription(s, MqttQoS.AT_MOST_ONCE)).collect(Collectors.toList());
        if (mqttTopicSubscriptions.size() > 0) {
            int messageId = connection.messageId();
            // 重试线程
            connection.addDisposable(messageId,
                Mono.fromRunnable(
                    () -> connection.write(MqttMessageApi.buildSub(messageId, mqttTopicSubscriptions)).subscribe())
                    .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe()); // retryPooledConnectionProvider
            // 订阅主题
            connection.write(MqttMessageApi.buildSub(messageId, mqttTopicSubscriptions)).subscribe();
        }

    }

    @Override
    public Set<String> getAllTopicList() {
        return this.topics;
    }

    @Override
    public List<String> getTopicLikeList(String topicLike) {
        return this.topics.stream().filter(s -> s.startsWith(topicLike)).collect(Collectors.toList());
    }

    @Override
    public String getTopic(String topic) {
        return this.topics.stream().filter(s -> s.equals(topic)).findFirst().orElse("");
    }

    @Override
    public Mono<Void> pub(String topic, byte[] message, boolean retained, int qos) {
        int messageId = qos == 0 ? 1 : connection.messageId();
        MqttQoS mqttQoS = MqttQoS.valueOf(qos);
        switch (mqttQoS) {
            case AT_MOST_ONCE:
                return connection.write(MqttMessageApi.buildPub(false, MqttQoS.AT_MOST_ONCE, retained, messageId, topic,
                    Unpooled.wrappedBuffer(message)));
            case EXACTLY_ONCE:
            case AT_LEAST_ONCE:
                return Mono.fromRunnable(() -> {
                    connection.write(MqttMessageApi.buildPub(false, mqttQoS, retained, messageId, topic,
                        Unpooled.wrappedBuffer(message))).subscribe();
                    // 失败 重试线程
                    connection.addDisposable(messageId,
                        Mono.fromRunnable(() -> connection.write(MqttMessageApi.buildPub(true, mqttQoS, retained,
                            messageId, topic, Unpooled.wrappedBuffer(message))).subscribe())
                            .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe()); // retry
                });
            default:
                return Mono.empty();
        }
    }

    @Override
    public Mono<Void> pub(String topic, byte[] message) {
        return pub(topic, message, false, 0);
    }

    @Override
    public Mono<Void> pub(String topic, byte[] message, int qos) {
        return pub(topic, message, false, qos);
    }

    @Override
    public Mono<Void> pub(String topic, byte[] message, boolean retained) {
        return pub(topic, message, retained, 0);
    }

    @Override
    public Mono<Void> sub(String... subMessages) {
        topics.addAll(Arrays.asList(subMessages));
        List<MqttTopicSubscription> topicSubscriptions = Arrays.stream(subMessages)
            .map(s -> new MqttTopicSubscription(s, MqttQoS.AT_MOST_ONCE)).collect(Collectors.toList());
        int messageId = connection.messageId();
        connection.addDisposable(messageId, Mono
            .fromRunnable(() -> connection.write(MqttMessageApi.buildSub(messageId, topicSubscriptions)).subscribe())
            .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe());
        return connection.write(MqttMessageApi.buildSub(messageId, topicSubscriptions));
    }

    /**
     *
     * @param subMessages
     * @param qos
     * @return
     */
    @Override
    public Mono<Void> sub(String subMessages, int qos) {
        topics.add(subMessages);
        MqttQoS mqttQoS = MqttQoS.valueOf(qos);
        List<MqttTopicSubscription> topicSubscriptions = new ArrayList<>();
        topicSubscriptions.add(new MqttTopicSubscription(subMessages, mqttQoS));
        int messageId = connection.messageId();
        connection.addDisposable(messageId, Mono
            .fromRunnable(() -> connection.write(MqttMessageApi.buildSub(messageId, topicSubscriptions)).subscribe())
            .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe());
        return connection.write(MqttMessageApi.buildSub(messageId, topicSubscriptions));
    }

    @Override
    public Mono<Void> unsub(List<String> topics) {
        this.topics.removeAll(topics);
        int messageId = connection.messageId();
        connection.addDisposable(messageId,
            Mono.fromRunnable(() -> connection.write(MqttMessageApi.buildUnSub(messageId, topics)).subscribe())
                .delaySubscription(Duration.ofSeconds(10)).repeat().subscribe());
        return connection.write(MqttMessageApi.buildUnSub(messageId, topics));
    }

    @Override
    public Mono<Void> unsubLike(String topicsLike) {
        return unsub(this.topics.stream().filter(s -> s.startsWith(topicsLike)).collect(Collectors.toList()));
    }

    @Override
    public Mono<Void> unsub() {
        return unsub(new ArrayList<>(this.topics));
    }

    @Override
    public Mono<Void> messageAcceptor(BiConsumer<String, byte[]> messageAcceptor) {
        return Mono.fromRunnable(() -> clientConfig.setMessageAcceptor(messageAcceptor));
    }

    @Override
    public void dispose() {
        connection.dispose();
    }
}
