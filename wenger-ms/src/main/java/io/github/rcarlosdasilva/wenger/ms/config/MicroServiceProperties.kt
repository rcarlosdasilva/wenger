package io.github.rcarlosdasilva.wenger.ms.config

import io.github.rcarlosdasilva.wenger.ms.config.microservice.MySqlProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * 微服务配置统一入口
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@ConfigurationProperties(prefix = "micro-service")
class MicroServiceProperties {

  @NestedConfigurationProperty
  var mySql = MySqlProperties()

}