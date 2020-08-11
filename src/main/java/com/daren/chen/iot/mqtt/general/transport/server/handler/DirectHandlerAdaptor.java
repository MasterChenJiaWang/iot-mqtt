package com.daren.chen.iot.mqtt.general.transport.server.handler;

import io.netty.handler.codec.mqtt.MqttMessageType;

public interface DirectHandlerAdaptor {

    DirectHandlerFactory handler(MqttMessageType messageType);

}
