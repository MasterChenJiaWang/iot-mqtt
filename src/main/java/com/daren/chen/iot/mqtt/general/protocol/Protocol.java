package com.daren.chen.iot.mqtt.general.protocol;

import java.util.List;

import com.daren.chen.iot.mqtt.general.common.annocation.ProtocolType;

import io.netty.channel.ChannelHandler;

public interface Protocol {

    boolean support(ProtocolType protocolType);

    BaseProtocolTransport getTransport();

    List<ChannelHandler> getHandlers();

}
