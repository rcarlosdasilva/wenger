package io.github.rcarlosdasilva.wenger.feature.config.app.misc

import io.github.rcarlosdasilva.wenger.feature.config.AbleProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.misc.sequence")
class SequenceProperties : AbleProperties() {

  /**
   * 工作ID (取值范围0~31)，默认0
   */
  var workerId = 0L
  /**
   * 数据中心ID (取值范围0~31)，默认0
   */
  var dataCenterId = 0L

}
