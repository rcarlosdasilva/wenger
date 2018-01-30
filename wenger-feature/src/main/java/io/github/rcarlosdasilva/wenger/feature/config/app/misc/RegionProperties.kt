package io.github.rcarlosdasilva.wenger.feature.config.app.misc

import io.github.rcarlosdasilva.wenger.feature.config.AbleProperties
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.misc.region")
class RegionProperties : AbleProperties() {

  /**
   * 地域数据文件位置
   *
   * 默认为从resources下读取。如果地域文件经常需要变更，建议将文件位置指定为URL或本地文件，无需重启服务
   */
  var location = "region.properties"

}
