package com.daren.chen.iot.mqtt.general.container;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.daren.chen.iot.mqtt.general.api.RsocketMessageHandler;
import com.daren.chen.iot.mqtt.general.api.RsocketTopicManager;
import com.daren.chen.iot.mqtt.general.api.client.RsocketClientSession;
import com.daren.chen.iot.mqtt.general.api.server.RsocketServerSession;
import com.daren.chen.iot.mqtt.general.transport.client.TransportClient;
import com.daren.chen.iot.mqtt.general.transport.server.TransportServer;

/**
 *
 */
public class IotConfiguration {
    /**
     *
     * @return
     */
    public RsocketServerSession initServer(IotConfig iotConfig) {
        IotConfig.Server server = iotConfig.getServer();
        if (!server.isEnable()) {
            throw new RuntimeException("Server服务没有开启!");
        }
        //
        AuthencationSession authencationSession = server.getAuthencationSessionClass();
        //
        ExceptorAcceptor exceptorAcceptor = server.getExceptorAcceptorClass();
        //
        RsocketMessageHandler messageHandler = server.getMessageHandlerClass();
        RsocketTopicManager rsocketTopicManager = server.getRsocketTopicManagerClass();
        // 身份验证
        BiFunction<String, String,
            Boolean> auth = Optional.ofNullable(authencationSession)
                .map(au -> (BiFunction<String, String, Boolean>)authencationSession::auth)
                .orElse((userName, password) -> true);
        // 异常处理
        Consumer<Throwable> throwableConsumer =
            Optional.ofNullable(exceptorAcceptor).map(ea -> (Consumer<Throwable>)ea::accept).orElse(ts -> {
            });
        return TransportServer.create(server.getHost(), server.getPort()).heart(server.getHeart()).log(server.isLog())
            .protocol(server.getProtocol()).revBufSize(server.getRevBufSize()).sendBufSize(server.getSendBufSize())
            .noDelay(server.isNoDelay()).keepAlive(server.isKeepAlive()).auth(auth).ssl(server.isSsl())
            .messageHandler(messageHandler).topicManager(rsocketTopicManager).exception(throwableConsumer).start()
            .block();
    }

    /**
     *
     * @param iotConfig
     * @return
     */
    public RsocketClientSession initClient(IotConfig iotConfig) {
        IotConfig.Client client = iotConfig.getClient();
        if (!client.isEnable()) {
            throw new RuntimeException("client服务没有开启!");
        }
        MessageAcceptor messageAcceptor = client.getMessageAcceptorClass();
        //
        ExceptorAcceptor exceptorAcceptor = client.getExceptorAcceptorClass();
        //
        OnCloseListener onCloseListener = client.getOnCloseListenerClass();
        return TransportClient.create(client.getIp(), client.getPort()).heart(client.getHeart())
            .protocol(client.getProtocol()).ssl(client.isSsl()).log(client.isLog())
            .clientId(client.getOption().getClientIdentifier()).password(client.getOption().getPassword())
            .username(client.getOption().getUserName()).willMessage(client.getOption().getWillMessage())
            .willTopic(client.getOption().getWillTopic()).willQos(client.getOption().getWillQos())
            .onClose(() -> Optional.ofNullable(onCloseListener).ifPresent(OnCloseListener::start))
            .exception(throwable -> Optional.ofNullable(exceptorAcceptor).ifPresent(ec -> ec.accept(throwable)))
            .messageAcceptor(
                (t, m) -> Optional.ofNullable(messageAcceptor).ifPresent(ma -> messageAcceptor.accept(t, m)))
            .connect().block();

    }

}
