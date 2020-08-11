package com.daren.chen.iot.mqtt.general.test;

/**
 * @Description:
 * @author: chendaren
 * @CreateDate: 2020/8/11 10:59
 */
public class AuthencationSessionImpl implements com.daren.chen.iot.mqtt.general.container.AuthencationSession {
    @Override
    public boolean auth(String username, String password) {
        return false;
    }
}
