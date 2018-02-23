package io.github.rcarlosdasilva.wenger.feature.config

import io.github.rcarlosdasilva.wenger.feature.config.app.AliyunProperties
import io.github.rcarlosdasilva.wenger.feature.config.app.MiscProperties
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * 系统统一配置入口
 */
@ConfigurationProperties(prefix = "app")
class AppProperties {

  /**
   * 临时文件目录，默认取系统临时目录
   */
  var tempDir: String? = null
  /**
   * 更多配置
   */
  var misc = MiscProperties()
  /**
   * 阿里功能配置
   */
  var aliyun = AliyunProperties()

}
