package com.daren.chen.iot.mqtt.general.container;

/**
 * 异常处理
 */
public interface ExceptorAcceptor {

    /**
     *
     * @param throwable
     */
    void accept(Throwable throwable);

}
