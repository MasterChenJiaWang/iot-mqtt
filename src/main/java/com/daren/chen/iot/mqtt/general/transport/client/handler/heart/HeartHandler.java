package com.daren.chen.iot.mqtt.general.transport.client.handler.heart;

import com.daren.chen.iot.mqtt.general.api.RsocketConfiguration;
import com.daren.chen.iot.mqtt.general.api.TransportConnection;

import io.netty.handler.codec.mqtt.MqttMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeartHandler implements com.daren.chen.iot.mqtt.general.transport.DirectHandler {

    @Override
    public void handler(MqttMessage message, TransportConnection connection, RsocketConfiguration config) {
        switch (message.fixedHeader().messageType()) {
            case PINGRESP:
                break;
            default:
                break;
        }

    }
}
