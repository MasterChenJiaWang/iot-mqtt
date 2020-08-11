package com.daren.chen.iot.mqtt.general.transport;

import com.daren.chen.iot.mqtt.general.api.RsocketConfiguration;
import com.daren.chen.iot.mqtt.general.api.TransportConnection;

import io.netty.handler.codec.mqtt.MqttMessage;

public interface DirectHandler {

    void handler(MqttMessage message, TransportConnection connection, RsocketConfiguration config);

}
