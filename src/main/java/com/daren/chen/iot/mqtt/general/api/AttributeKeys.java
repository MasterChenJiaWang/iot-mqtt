package com.daren.chen.iot.mqtt.general.api;

import com.daren.chen.iot.mqtt.general.api.client.RsocketClientSession;
import com.daren.chen.iot.mqtt.general.api.server.RsocketServerSession;
import com.daren.chen.iot.mqtt.general.common.connection.WillMessage;

import io.netty.util.AttributeKey;
import lombok.experimental.UtilityClass;
import reactor.core.Disposable;

/**
 *
 */
@UtilityClass
public class AttributeKeys {

    /**
     *
     */
    public AttributeKey<RsocketClientSession> clientConnectionAttributeKey = AttributeKey.valueOf("client_operation");

    /**
     *
     */
    public AttributeKey<RsocketServerSession> serverConnectionAttributeKey = AttributeKey.valueOf("server_operation");

    /**
     *
     */
    public AttributeKey<Disposable> closeConnection = AttributeKey.valueOf("close_connection");

    /**
     *
     */
    public AttributeKey<TransportConnection> connectionAttributeKey = AttributeKey.valueOf("transport_connection");

    /**
     *
     */
    public AttributeKey<String> deviceId = AttributeKey.valueOf("device_id");

    /**
     *
     */
    public AttributeKey<WillMessage> wILLMESSAGE = AttributeKey.valueOf("WILL_MESSAGE");

}
