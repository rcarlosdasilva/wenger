package io.github.rcarlosdasilva.wenger.ms.config.microservice.mysql

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "micro-service.my-sql.druid")
class DruidProperties {

  /**
   * 配置初始化大小、最小、最大
   */
  var initialSize = 1
  var minIdle = 1
  var maxActive = 20

  /**
   * 配置获取连接等待超时的时间
   */
  var maxWait = 60000L

  /**
   * 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
   */
  var timeBetweenEvictionRunsMillis = 60000L

  /**
   * 配置一个连接在池中最小生存的时间，单位是毫秒
   */
  var minEvictableIdleTimeMillis = 300000L

  var validationQuery = "SELECT 1"
  var testWhileIdle = true
  var testOnBorrow = false
  var testOnReturn = false

  /**
   * 打开PSCache，并且指定每个连接上PSCache的大小
   */
  var poolPreparedStatements = true
  var maxPoolPreparedStatementPerConnectionSize = 20

  /**
   * 配置监控统计拦截的filters
   */
  var filters: String? = null

}
