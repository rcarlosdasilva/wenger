package io.github.rcarlosdasilva.wenger.feature.config.app.aliyun;

import io.github.rcarlosdasilva.wenger.feature.aliyun.mq.MqProducerType;
import io.github.rcarlosdasilva.wenger.feature.config.AbleProperties;
import lombok.Data;

import java.util.Map;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Data
public class MqProperties extends AbleProperties {

  /**
   * 消息队列服务器地址，无特殊需要不需要指定，默认使用公网地址
   */
  private String address = "http://onsaddr-internet.aliyun.com/rocketmq/nsaddr4client-internet";
  /**
   * 注册消息生产者，key值为Producer Id
   */
  private Map<String, MqProducerType> producers;
  /**
   * 消息生产者发送消息超时时间（单位：毫秒），默认2000
   */
  private long sendTimeout = 2000;

}
