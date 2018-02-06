package io.github.rcarlosdasilva.wenger.ms.config.microservice.mysql

import com.baomidou.mybatisplus.MybatisConfiguration
import org.apache.ibatis.session.ExecutorType
import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.*

@ConfigurationProperties(prefix = "micro-service.my-sql.mybatis")
class MybatisProperties {

  /**
   * XML文件位置
   */
  var configLocation: String? = null
  /**
   * Mapper位置
   */
  var mapperLocations: Array<String>? = null
  /**
   * Packages to search type aliases. (Package delimiters are ",; \t\n")
   */
  var typeAliasesPackage: String? = null

  var typeEnumsPackage: String? = null
  /**
   * Packages to search for type handlers. (Package delimiters are ",; \t\n")
   */
  var typeHandlersPackage: String? = null
  /**
   * Execution mode for [org.mybatis.spring.SqlSessionTemplate].
   */
  var executorType: ExecutorType? = null
  /**
   * Externalized properties for MyBatis configuration.
   */
  var configurationProperties: Properties? = null
  /**
   * A Configuration object for customize default settings. If [.configLocation]
   * is specified, this property is not used.
   */
  var configuration: MybatisConfiguration? = null

}