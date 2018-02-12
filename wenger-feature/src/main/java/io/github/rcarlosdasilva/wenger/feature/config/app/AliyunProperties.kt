package io.github.rcarlosdasilva.wenger.feature.config.app

import io.github.rcarlosdasilva.wenger.feature.config.app.aliyun.GreenProperties
import io.github.rcarlosdasilva.wenger.feature.config.app.aliyun.MnsProperties
import io.github.rcarlosdasilva.wenger.feature.config.app.aliyun.MqProperties
import io.github.rcarlosdasilva.wenger.feature.config.app.aliyun.OssProperties
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 阿里云功能配置
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@ConfigurationProperties(prefix = "app.aliyun")
class AliyunProperties {

  /**
   * 阿里access key id
   */
  var accessId: String? = null
  /**
   * 阿里access key secret
   */
  var accessSecret: String? = null
  /**
   * 消息队列配置
   */
  var mq = MqProperties()
  /**
   * 消息服务配置
   */
  var mns = MnsProperties()
  /**
   * 文件存储配置
   */
  var oss = OssProperties()
  /**
   * 内容安全配置
   */
  var green = GreenProperties()

}
