package io.github.rcarlosdasilva.wenger.feature.aliyun.mq;

public enum MqProducerType {

  /**
   * 无序消息
   */
  NORMAL,
  /**
   * 顺序消息
   */
  ORDERED,
  /**
   * 事务消息
   */
  TRANSACTIONAL

}
