package com.daren.chen.iot.mqtt.general.container;

/**
 * 身份验证处理
 */
public interface AuthencationSession {
    /**
     *
     * @param username
     * @param password
     * @return
     */
    boolean auth(String username, String password);
}
