package com.daren.chen.iot.mqtt.general.common.message;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransportMessage {

    /**
     *
     */
    private String topic;

    /**
     *
     */
    private byte[] message;

    /**
     *
     */
    private int qos;

    /**
     *
     */
    private boolean isRetain;

    /**
     *
     */
    private boolean isDup;

}
