package com.daren.chen.iot.mqtt.general.transport.client;

import com.daren.chen.iot.mqtt.general.api.TransportConnection;
import com.daren.chen.iot.mqtt.general.api.client.RsocketClientSession;
import com.daren.chen.iot.mqtt.general.common.annocation.ProtocolType;
import com.daren.chen.iot.mqtt.general.config.RsocketClientConfig;
import com.daren.chen.iot.mqtt.general.protocol.ProtocolFactory;
import com.daren.chen.iot.mqtt.general.protocol.mqtt.MqttProtocol;
import com.daren.chen.iot.mqtt.general.transport.client.connection.RsocketClientConnection;

import reactor.core.publisher.Mono;

/**
 *
 */
public class TransportClientFactory {

    /**
     *
     */
    private ProtocolFactory protocolFactory;

    /**
     *
     */
    private RsocketClientConfig clientConfig;

    public TransportClientFactory() {
        protocolFactory = new ProtocolFactory();
    }

    public Mono<RsocketClientSession> connect(RsocketClientConfig config) {
        this.clientConfig = config;
        // 默认 mqtt
        return Mono.from(protocolFactory.getProtocol(ProtocolType.valueOf(config.getProtocol()))
            .orElse(new MqttProtocol()).getTransport().connect(config)).map(this::wrapper)
            .doOnError(config.getThrowableConsumer());
    }

    private RsocketClientSession wrapper(TransportConnection connection) {
        return new RsocketClientConnection(connection, clientConfig);
    }

}
