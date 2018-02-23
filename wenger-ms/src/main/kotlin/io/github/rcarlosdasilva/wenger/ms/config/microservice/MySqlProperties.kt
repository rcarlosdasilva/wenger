package io.github.rcarlosdasilva.wenger.ms.config.microservice

import io.github.rcarlosdasilva.wenger.ms.autoconfigure.MySqlPoolType
import io.github.rcarlosdasilva.wenger.ms.config.microservice.mysql.*
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@ConfigurationProperties(prefix = "micro-service.my-sql")
class MySqlProperties {

  /**
   * 使用的连接池类型
   */
  var poolType: MySqlPoolType = MySqlPoolType.DRUID
  /**
   * 数据库字段为tiny_int时是否映射为Java的Boolean
   */
  var tinyintToBoolean = true
  /**
   * MyBatis (MyBatis Plus)配置
   */
  var mybatis = MybatisProperties()

  var druid: DruidProperties? = null
  var c3p0: C3p0Properties? = null
  var dbcp: DbcpProperties? = null
  var hikari: HikariProperties? = null

}