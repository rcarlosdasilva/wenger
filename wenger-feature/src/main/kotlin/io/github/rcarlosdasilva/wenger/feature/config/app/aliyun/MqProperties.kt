package io.github.rcarlosdasilva.wenger.feature.config.app.aliyun

import io.github.rcarlosdasilva.wenger.feature.aliyun.mq.MqProducerType
import io.github.rcarlosdasilva.wenger.feature.config.AbleProperties
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@ConfigurationProperties(prefix = "app.aliyun.mq")
class MqProperties : AbleProperties() {

  /**
   * 消息队列服务器地址，无特殊需要不需要指定，默认使用公网地址
   */
  var address = "http://onsaddr-internet.aliyun.com/rocketmq/nsaddr4client-internet"
  /**
   * 是否打印消息日志
   *
   * 考虑到消息队列的触发频率较高，日志产生量非常大，并且通过日志排查问题比较直接，这里不依赖Log Level（Debug或Info）。
   *
   * 这样可以结合中央配置服务器，随时修改打印日志参数，按需要打印，默认日志Level为INFO
   */
  var printLog = true
  /**
   * 注册消息生产者，key值为Producer Id，不配置无法使用消息生产
   */
  var producers: Map<String, MqProducerType>? = null
  /**
   * 消息生产者发送消息超时时间（单位：毫秒），默认2000
   */
  var sendTimeout = 2000L
  /**
   * 消息生产失败时，生产者重试次数
   */
  var producerRetries = 3

}
