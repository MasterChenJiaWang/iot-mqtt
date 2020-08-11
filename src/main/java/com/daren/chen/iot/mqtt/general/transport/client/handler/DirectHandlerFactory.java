package com.daren.chen.iot.mqtt.general.transport.client.handler;

import java.util.concurrent.ConcurrentHashMap;

import com.daren.chen.iot.mqtt.general.common.exception.NotSuppportHandlerException;
import com.daren.chen.iot.mqtt.general.transport.DirectHandler;
import com.daren.chen.iot.mqtt.general.transport.client.handler.connect.ConnectHandler;
import com.daren.chen.iot.mqtt.general.transport.client.handler.heart.HeartHandler;
import com.daren.chen.iot.mqtt.general.transport.client.handler.pub.PubHandler;
import com.daren.chen.iot.mqtt.general.transport.client.handler.sub.SubHandler;

import io.netty.handler.codec.mqtt.MqttMessageType;

public class DirectHandlerFactory {

    private final MqttMessageType messageType;

    private final ConcurrentHashMap<MqttMessageType, DirectHandler> messageTypeCollection = new ConcurrentHashMap<>();

    public DirectHandlerFactory(MqttMessageType messageType) {
        this.messageType = messageType;
    }

    public DirectHandler loadHandler() {
        return messageTypeCollection.computeIfAbsent(messageType, type -> {
            switch (type) {
                // 下面5种相同
                case PUBACK:
                case PUBREC:
                case PUBREL:
                case PUBLISH:
                case PUBCOMP:
                    return new PubHandler();
                case CONNACK:
                    return new ConnectHandler();
                case PINGRESP:
                    return new HeartHandler();
                // 下面两种相同
                case UNSUBACK:
                case SUBACK:
                    return new SubHandler();
                default:
                    break;
            }
            throw new NotSuppportHandlerException(messageType + " not support ");
        });
    }

}
