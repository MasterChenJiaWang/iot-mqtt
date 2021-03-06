package com.daren.chen.iot.mqtt.general.container;

/**
 *
 */
public interface MessageAcceptor {

    /**
     * 消息接受
     *
     * @param topic
     * @param message
     */
    void accept(String topic, byte[] message);

}
