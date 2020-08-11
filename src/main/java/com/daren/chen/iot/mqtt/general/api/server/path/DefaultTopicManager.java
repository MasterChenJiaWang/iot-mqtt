package com.daren.chen.iot.mqtt.general.api.server.path;

import java.util.List;
import java.util.Optional;

import com.daren.chen.iot.mqtt.general.api.TransportConnection;

/**
 * 主题管理器
 */
public class DefaultTopicManager {

    private final TopicFilter<TransportConnection> pathMap = new TopicFilter<>();

    /**
     *
     * @param topic
     * @return
     */
    public Optional<List<TransportConnection>> getTopicConnection(String topic) {
        try {
            return Optional.ofNullable(pathMap.getData(topic));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public synchronized void addTopicConnection(String topic, TransportConnection connection) {
        pathMap.addTopic(topic, connection);
    }

    public synchronized void deleteTopicConnection(String topic, TransportConnection connection) {
        pathMap.delete(topic, connection);
    }

    // private final TopicMap<String, TransportConnection> pathMap = new TopicMap<>();
    // /**
    // *
    // */
    // private final LoadingCache<String,
    // // 设置并发级别为8，并发级别是指可以同时写缓存的线程数
    // // 设置缓存容器的初始容量为10
    // // 设置缓存最大容量为100，超过100之后就会按照LRU最近虽少使用算法来移除缓存项
    // // 是否需要统计缓存情况,该操作消耗一定的性能,生产环境应该去除
    // // 设置写缓存后n秒钟过期
    // // 设置读写缓存后n秒钟过期,实际很少用到,类似于expireAfterWrite
    // Optional<List<TransportConnection>>> cache = CacheBuilder.newBuilder().concurrencyLevel(8).initialCapacity(10)
    // .maximumSize(100).expireAfterWrite(20, TimeUnit.MINUTES)
    // .build(new CacheLoader<String, Optional<List<TransportConnection>>>() {
    // // 数据加载
    // @Override
    // public Optional<List<TransportConnection>> load(String key) throws Exception {
    // String[] methodArray = key.split("/");
    // return Optional.ofNullable(pathMap.getData(methodArray));
    // }
    // });
    //
    // public Optional<List<TransportConnection>> getTopicConnection(String topic) {
    // try {
    // return cache.getUnchecked(topic);
    // } catch (Exception e) {
    // return Optional.empty();
    // }
    // }
    //
    // public void addTopicConnection(String topic, TransportConnection connection) {
    // String[] methodArray = topic.split("/");
    // pathMap.putData(methodArray, connection);
    // cache.invalidate(topic);
    // }
    //
    // public void deleteTopicConnection(String topic, TransportConnection connection) {
    // String[] methodArray = topic.split("/");
    // pathMap.delete(methodArray, connection);
    // cache.invalidate(topic);
    // }

}
