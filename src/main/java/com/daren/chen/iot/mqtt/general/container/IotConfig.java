package com.daren.chen.iot.mqtt.general.container;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.daren.chen.iot.mqtt.general.api.RsocketMessageHandler;
import com.daren.chen.iot.mqtt.general.api.RsocketTopicManager;
import com.daren.chen.iot.mqtt.general.common.annocation.ProtocolType;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.Data;

/**
 * @author 曾帅
 */
@Data
public class IotConfig {

    private static final String BASE_KEY = "iot.";
    /**
     *
     */
    private static final String BASE_KEY_SERVER = BASE_KEY + "server.";
    /**
     *
     */
    private static final String BASE_KEY_CLIENT = BASE_KEY + "client.";

    /**
     *
     */
    private static final Properties prop = new Properties();
    /**
     *
     */
    private static Boolean IS_LOAD = false;

    public IotConfig() {
        this.server = initServer();
        this.client = initClient();
        this.uid = IdUtil.simpleUUID();
        this.serverName = prop.getProperty(BASE_KEY_SERVER + "serverName");
    }

    /**
     * 服务端
     */
    private Server server;

    /**
     * 客户端
     */
    private Client client;

    /**
     *
     */
    private String uid;
    /**
     *
     */
    private String serverName;

    static {
        if (!IS_LOAD) {
            InputStream in = null;
            try {

                String filePath = "/config.properties";
                // 读取属性文件config.props
                in = IotConfig.class.getResourceAsStream(filePath);
                if (in == null) {
                    throw new RuntimeException(filePath + " 配置文件为空!");
                }
                /// 加载属性列表
                prop.load(in);
                IS_LOAD = true;
            } catch (IOException e) {
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ignored) {
                    }
                }
                IS_LOAD = true;
            }
        }

    }

    /**
     *
     */
    public Server initServer() {

        Server server = new Server();
        server.setEnable(Boolean.parseBoolean(prop.getProperty(BASE_KEY_SERVER + "enable", "false")));
        server.setHost(prop.getProperty(BASE_KEY_SERVER + "host", server.getHost()));
        String serverPortProperty = prop.getProperty(BASE_KEY_SERVER + "port");
        server
            .setPort(StrUtil.isNotBlank(serverPortProperty) ? Integer.parseInt(serverPortProperty) : server.getPort());
        server.setLog(Boolean.parseBoolean(prop.getProperty(BASE_KEY_SERVER + "log", "false")));
        server.setProtocol(ProtocolType.valueOf(prop.getProperty(BASE_KEY_SERVER + "protocol", "MQTT")));
        server.setHeart(Integer.parseInt(prop.getProperty(BASE_KEY_SERVER + "heart", "10000")));
        server.setSsl(Boolean.parseBoolean(prop.getProperty(BASE_KEY_SERVER + "ssl", "false")));
        server.setSendBufSize(Integer.parseInt(prop.getProperty(BASE_KEY_SERVER + "sendBufSize", "32768")));
        server.setRevBufSize(Integer.parseInt(prop.getProperty(BASE_KEY_SERVER + "revBufSize", "32768")));
        server.setKeepAlive(Boolean.parseBoolean(prop.getProperty(BASE_KEY_SERVER + "keepAlive", "false")));
        server.setNoDelay(Boolean.parseBoolean(prop.getProperty(BASE_KEY_SERVER + "noDelay", "false")));
        String authencationSessionClass = prop.getProperty(BASE_KEY_SERVER + "authencationSessionClass");
        String serverExceptorAcceptorClass = prop.getProperty(BASE_KEY_SERVER + "exceptorAcceptorClass");
        String rsocketMessageHandlerClass = prop.getProperty(BASE_KEY_SERVER + "rsocketMessageHandlerClass");
        String rsocketTopicManagerClass = prop.getProperty(BASE_KEY_SERVER + "rsocketTopicManagerClass");
        try {
            server.setAuthencationSessionClass(StrUtil.isBlank(authencationSessionClass) ? null
                : (AuthencationSession)ClassUtil.loadClass(authencationSessionClass).newInstance());
            server.setExceptorAcceptorClass(StrUtil.isBlank(serverExceptorAcceptorClass) ? null
                : (ExceptorAcceptor)ClassUtil.loadClass(serverExceptorAcceptorClass).newInstance());
            server.setMessageHandlerClass(StrUtil.isBlank(rsocketMessageHandlerClass) ? null
                : (RsocketMessageHandler)ClassUtil.loadClass(rsocketMessageHandlerClass).newInstance());
            server.setRsocketTopicManagerClass(StrUtil.isBlank(rsocketTopicManagerClass) ? null
                : (RsocketTopicManager)ClassUtil.loadClass(rsocketTopicManagerClass).newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return server;
    }

    private Client initClient() {
        //
        Client client = new Client();
        client.setEnable(Boolean.parseBoolean(prop.getProperty(BASE_KEY_CLIENT + "enable", "false")));
        client.setIp(prop.getProperty(BASE_KEY_CLIENT + "ip", client.getIp()));
        String clientPortProperty = prop.getProperty(BASE_KEY_CLIENT + "port");
        client
            .setPort(StrUtil.isNotBlank(clientPortProperty) ? Integer.parseInt(clientPortProperty) : client.getPort());
        client.setLog(Boolean.parseBoolean(prop.getProperty(BASE_KEY_CLIENT + "log", "false")));
        client.setProtocol(ProtocolType.valueOf(prop.getProperty(BASE_KEY_CLIENT + "protocol", "MQTT")));
        client.setHeart(Integer.parseInt(prop.getProperty(BASE_KEY_CLIENT + "heart", "10000")));
        client.setSsl(Boolean.parseBoolean(prop.getProperty(BASE_KEY_CLIENT + "ssl", "false")));
        client.setSendBufSize(Integer.parseInt(prop.getProperty(BASE_KEY_CLIENT + "sendBufSize", "32768")));
        client.setRevBufSize(Integer.parseInt(prop.getProperty(BASE_KEY_CLIENT + "revBufSize", "32768")));
        client.setKeepAlive(Boolean.parseBoolean(prop.getProperty(BASE_KEY_CLIENT + "keepAlive", "false")));
        client.setNoDelay(Boolean.parseBoolean(prop.getProperty(BASE_KEY_CLIENT + "noDelay", "false")));
        String onCloseListenerClass = prop.getProperty(BASE_KEY_CLIENT + "onCloseListenerClass");
        String messageAcceptorClass = prop.getProperty(BASE_KEY_CLIENT + "messageAcceptorClass");
        String exceptorAcceptorClass = prop.getProperty(BASE_KEY_CLIENT + "exceptorAcceptorClass");
        try {
            client.setOnCloseListenerClass(StrUtil.isBlank(onCloseListenerClass) ? null
                : (OnCloseListener)ClassUtil.loadClass(onCloseListenerClass).newInstance());
            client.setMessageAcceptorClass(StrUtil.isBlank(messageAcceptorClass) ? null
                : (MessageAcceptor)ClassUtil.loadClass(messageAcceptorClass).newInstance());
            client.setExceptorAcceptorClass(StrUtil.isBlank(onCloseListenerClass) ? null
                : (ExceptorAcceptor)ClassUtil.loadClass(exceptorAcceptorClass).newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        //
        Client.Options options = new Client.Options();
        options.setClientIdentifier(prop.getProperty(BASE_KEY_CLIENT + "option.clientIdentifier"));
        options.setWillTopic(prop.getProperty(BASE_KEY_CLIENT + "option.willTopic"));
        options.setWillMessage(prop.getProperty(BASE_KEY_CLIENT + "option.willMessage"));
        options.setUserName(prop.getProperty(BASE_KEY_CLIENT + "option.userName"));
        options.setPassword(prop.getProperty(BASE_KEY_CLIENT + "option.password"));
        options.setHasUserName(Boolean.parseBoolean(prop.getProperty(BASE_KEY_CLIENT + "option.hasUserName", "false")));
        options.setHasPassword(Boolean.parseBoolean(prop.getProperty(BASE_KEY_CLIENT + "option.hasPassword", "false")));
        options.setHasWillRetain(
            Boolean.parseBoolean(prop.getProperty(BASE_KEY_CLIENT + "option.hasWillRetain", "false")));
        options.setHasWillFlag(Boolean.parseBoolean(prop.getProperty(BASE_KEY_CLIENT + "option.hasWillFlag", "false")));
        options.setHasCleanSession(
            Boolean.parseBoolean(prop.getProperty(BASE_KEY_CLIENT + "option.hasCleanSession", "false")));
        String property = prop.getProperty(BASE_KEY_CLIENT + "option.willQos");
        options.setWillQos(StrUtil.isNotBlank(property) ? MqttQoS.valueOf(property) : null);
        //
        client.setOption(options);
        return client;
    }

    @Data
    public static class Server {
        /**
         *
         */
        public boolean enable = false;

        /**
         *
         */
        public String host;
        /**
         *
         */
        public int port;
        /**
         *
         */
        private boolean log = false;
        /**
         * 消息类型
         */
        private ProtocolType protocol = ProtocolType.MQTT;
        /**
         *
         */
        private int heart = 10000;
        /**
         *
         */
        private boolean ssl = false;

        /**
         * 发送缓冲区大小 默认 32k
         */
        private int sendBufSize = 32 * 1024;
        /**
         * 接收缓冲区大小 默认 32k
         */
        private int revBufSize = 32 * 1024;

        /**
         * Socket参数，连接保活，默认值为False。启用该功能时，TCP会主动探测空闲连接的有效性。可以将此功能视为TCP的心跳机制， 需要注意的是：默认的心跳间隔是7200s即2小时。Netty默认关闭该功能。
         */
        private boolean keepAlive = false;
        /**
         * Socket参数，立即发送数据，默认值为True（Netty默认为True而操作系统默认为False）。该值设置Nagle算法的启用， 该算法将小的碎片数据连接成更大的报文来最小化所发送的报文的数量，
         * 如果需要发送一些较小的报文，则需要禁用该算法。 Netty默认禁用该算法，从而最小化报文传输延时
         */
        private boolean noDelay = true;

        /**
         *
         */
        private AuthencationSession authencationSessionClass;
        /**
         *
         */
        private ExceptorAcceptor exceptorAcceptorClass;
        /**
         *
         */
        private RsocketMessageHandler messageHandlerClass;
        /**
         *
         */
        private RsocketTopicManager rsocketTopicManagerClass;
    }

    @Data
    public static class Client {
        private boolean enable = false;

        private String ip;

        private int port;

        private ProtocolType protocol = ProtocolType.MQTT;

        private int heart = 10000;

        private boolean log = false;

        private boolean ssl = false;

        /**
         * 发送缓冲区大小 默认 32k
         */
        private int sendBufSize = 32 * 1024;
        /**
         * 接收缓冲区大小 默认 32k
         */
        private int revBufSize = 32 * 1024;

        /**
         * Socket参数，连接保活，默认值为False。启用该功能时，TCP会主动探测空闲连接的有效性。可以将此功能视为TCP的心跳机制， 需要注意的是：默认的心跳间隔是7200s即2小时。Netty默认关闭该功能。
         */
        private boolean keepAlive = false;
        /**
         * Socket参数，立即发送数据，默认值为True（Netty默认为True而操作系统默认为False）。该值设置Nagle算法的启用， 该算法将小的碎片数据连接成更大的报文来最小化所发送的报文的数量，
         * 如果需要发送一些较小的报文，则需要禁用该算法。 Netty默认禁用该算法，从而最小化报文传输延时
         */
        private boolean noDelay = true;

        private Consumer<Throwable> throwableConsumer;

        private BiConsumer<String, byte[]> messageAcceptor;

        private Runnable onClose = () -> {
        };

        private Options option;

        /**
         *
         */
        private OnCloseListener onCloseListenerClass;
        /**
         *
         */
        private ExceptorAcceptor exceptorAcceptorClass;
        /**
         *
         */
        private MessageAcceptor messageAcceptorClass;

        @Data
        public static class Options {

            private String clientIdentifier = UUID.randomUUID().toString();

            private String willTopic;

            private String willMessage;

            private String userName;

            private String password;

            private boolean hasUserName;

            private boolean hasPassword;

            private boolean hasWillRetain;

            private MqttQoS willQos;

            private boolean hasWillFlag;

            private boolean hasCleanSession;

        }

    }

}
