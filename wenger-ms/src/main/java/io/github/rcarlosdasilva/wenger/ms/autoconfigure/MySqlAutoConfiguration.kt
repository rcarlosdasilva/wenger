package io.github.rcarlosdasilva.wenger.ms.autoconfigure

import com.alibaba.druid.pool.DruidDataSource
import com.baomidou.mybatisplus.MybatisConfiguration
import com.baomidou.mybatisplus.MybatisXMLLanguageDriver
import com.baomidou.mybatisplus.entity.GlobalConfiguration
import com.baomidou.mybatisplus.enums.DBType
import com.baomidou.mybatisplus.enums.IdType
import com.baomidou.mybatisplus.mapper.LogicSqlInjector
import com.baomidou.mybatisplus.plugins.PaginationInterceptor
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean
import com.mchange.v2.c3p0.ComboPooledDataSource
import com.zaxxer.hikari.HikariDataSource
import io.github.rcarlosdasilva.wenger.feature.context.EnvironmentHandler
import io.github.rcarlosdasilva.wenger.feature.context.RuntimeProfile
import io.github.rcarlosdasilva.wenger.ms.arc.BasicEntity
import io.github.rcarlosdasilva.wenger.ms.arc.BasicMapper
import io.github.rcarlosdasilva.wenger.ms.config.microservice.MySqlProperties
import io.github.rcarlosdasilva.wenger.ms.handler.mybatis.AuditingHandler
import org.apache.commons.dbcp2.BasicDataSource
import org.apache.ibatis.io.VFS
import org.apache.ibatis.logging.slf4j.Slf4jImpl
import org.apache.ibatis.plugin.Interceptor
import org.apache.ibatis.session.ExecutorType
import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.SqlSessionTemplate
import org.mybatis.spring.annotation.MapperScan
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.io.IOException
import java.net.URI
import java.net.URL
import javax.sql.DataSource

/**
 * MySQL自动配置，统一所有微服务配置方式
 *
 * 使用前提，1. 依赖本模块(wenger-feature)的所有微服务模块。2. 正确配置了spring.datasource属性。
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@Configuration
@ConditionalOnBean(value = [DataSourceProperties::class])
@MapperScan(basePackages = ["io.github.rcarlosdasilva.wenger.feature.**.mapper"], markerInterface = BasicMapper::class)
@EnableTransactionManagement
@EnableConfigurationProperties(value = [DataSourceProperties::class, MySqlProperties::class])
open class MySqlAutoConfiguration @Autowired constructor(
  private val dataSourceProperties: DataSourceProperties,
  private val defaultResourceLoader: DefaultResourceLoader,
  private val environmentHandler: EnvironmentHandler,
  private val auditingHandler: AuditingHandler?,
  private val mySqlProperties: MySqlProperties
) {

  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  @Bean
  open fun dataSource(): DataSource =
    with(mySqlProperties) {
      val url = dataSourceProperties.url.let {
        val markIndex = it.lastIndexOf('?') + 1
        val query = it.substring(markIndex)
        val fragments = query.split("&").associate { q ->
          q.split("=").let { Pair(it[0], it[1]) }
        }.toMutableMap()

        // Mysql连接字符串参数 tinyInt1isBit设置为false(默认 true)，可以将bit和tinyint类型映射为boolean
        fragments["tinyInt1isBit"] = (!this.tinyintToBoolean).toString()

        it.substring(0, markIndex) + fragments.map { it.key + "=" + it.value }.joinToString("&")
      }

      val builder = DataSourceBuilder.create().apply {
        this.url(url)
        this.driverClassName(dataSourceProperties.driverClassName)
        this.username(dataSourceProperties.username)
        this.password(dataSourceProperties.password)
      }

      logger.info("[MySQL] - DataSource装配使用{}连接池", this.poolType)
      when (this.poolType) {
        MySqlPoolType.NONE -> builder.type(DriverManagerDataSource::class.java).build()
        MySqlPoolType.DRUID -> builder.type(DruidDataSource::class.java).build().apply { configDruid(this) }
        MySqlPoolType.C3P0 -> builder.type(ComboPooledDataSource::class.java).build().apply { configC3p0(this) }
        MySqlPoolType.DBCP -> builder.type(BasicDataSource::class.java).build().apply { configDbcp(this) }
        MySqlPoolType.HIKARI -> builder.type(HikariDataSource::class.java).build().apply { configHikari(this) }
      }
    }

  @Bean
  open fun sqlSessionFactory(dataSource: DataSource): SqlSessionFactory =
    MybatisSqlSessionFactoryBean().run fb@ {
      this.setDataSource(dataSource)
      this.vfs = SpringBootVFS::class.java

      with(mySqlProperties.mybatis) {
        this.configLocation?.let { this@fb.setConfigLocation(defaultResourceLoader.getResource(it)) }
        this.typeHandlersPackage?.let { this@fb.setTypeHandlersPackage(it) }
        this.typeEnumsPackage?.let { this@fb.setTypeEnumsPackage(it) }
        this.typeAliasesPackage?.let { this@fb.setTypeAliasesPackage(it) } ?: run {
          logger.info("[MySQL] - 自动扫描继承自BasicEntity的所有Entity")
          this@fb.setTypeAliasesSuperType(BasicEntity::class.java)
        }
        this.mapperLocations?.let {
          val resourceResolver = PathMatchingResourcePatternResolver()
          this@fb.setMapperLocations(it.flatMap { s ->
            resourceResolver.getResources(s).toList()
          }.toTypedArray())
        } ?: run {
          logger.info("[MySQL] - 自动扫描路径\"resources/storage/mapper\"下的所有Mapper XML文件")
          this@fb.setMapperLocations(PathMatchingResourcePatternResolver().getResources("classpath:/storage/mapper/*Mapper.xml"))
        }
        this.configurationProperties?.let { this@fb.setConfigurationProperties(it) }
        this@fb.setPlugins(arrayOf<Interceptor>(PaginationInterceptor()))

        val mc = this.configuration ?: MybatisConfiguration()
        mc.setDefaultScriptingLanguage(MybatisXMLLanguageDriver::class.java)
        mc.logImpl = Slf4jImpl::class.java
        mc.defaultExecutorType = ExecutorType.REUSE

        val gc = GlobalConfiguration().apply {
          this.setDbType(DBType.MYSQL.name)
          // ID 策略 AUTO->`0`("数据库ID自增") INPUT->`1`(用户输入ID") ID_WORKER->`2`("全局唯一ID")
          // 在auditingHandler.fillInsert中注入
          this.setIdType(IdType.INPUT.key)
          this.sqlInjector = LogicSqlInjector()
          this.logicDeleteValue = "1"
          this.logicNotDeleteValue = "0"
          this.isRefresh = environmentHandler.runtimeProfile === RuntimeProfile.DEVEL

          auditingHandler?.let { this.metaObjectHandler = auditingHandler }
              ?: logger.warn("[MySQL] - AuditingHandler未实例化，审计字段将无法自动填充")
        }

        this@fb.setConfiguration(mc)
        this@fb.setGlobalConfig(gc)
      }
      logger.info("[MySQL] - MyBatis 自动配置完毕")
      return this.`object`!!
    }

  @Bean
  open fun sqlSessionTemplate(sqlSessionFactory: SqlSessionFactory): SqlSessionTemplate =
    mySqlProperties.mybatis.executorType?.let { SqlSessionTemplate(sqlSessionFactory, it) }
        ?: run { SqlSessionTemplate(sqlSessionFactory) }

  private fun configDruid(druidDataSource: DruidDataSource) {
    mySqlProperties.druid ?: return

    with(mySqlProperties.druid!!) {
      druidDataSource.initialSize = this.initialSize
      druidDataSource.minIdle = this.minIdle
      druidDataSource.maxActive = this.maxActive
      druidDataSource.maxWait = this.maxWait
      druidDataSource.timeBetweenEvictionRunsMillis = this.timeBetweenEvictionRunsMillis
      druidDataSource.minEvictableIdleTimeMillis = this.minEvictableIdleTimeMillis
      druidDataSource.validationQuery = this.validationQuery
      druidDataSource.isTestWhileIdle = this.testWhileIdle
      druidDataSource.isTestOnBorrow = this.testOnBorrow
      druidDataSource.isTestOnReturn = this.testOnReturn
      druidDataSource.isPoolPreparedStatements = this.poolPreparedStatements
      druidDataSource.maxPoolPreparedStatementPerConnectionSize = this.maxPoolPreparedStatementPerConnectionSize
      druidDataSource.setFilters(this.filters)
    }
  }

  private fun configC3p0(comboPooledDataSource: ComboPooledDataSource) {
    mySqlProperties.c3p0 ?: return

    with(mySqlProperties.c3p0!!) {
      comboPooledDataSource.isTestConnectionOnCheckout = this.testConnectionOnCheckout
      comboPooledDataSource.isTestConnectionOnCheckin = this.testConnectionOnCheckin
      comboPooledDataSource.dataSourceName = this.dataSourceName
      comboPooledDataSource.minPoolSize = this.minPoolSize
      comboPooledDataSource.maxPoolSize = this.maxPoolSize
      comboPooledDataSource.checkoutTimeout = this.checkoutTimeout
      comboPooledDataSource.idleConnectionTestPeriod = this.idleConnectionTestPeriod
      comboPooledDataSource.maxConnectionAge = this.maxConnectionAge
      comboPooledDataSource.maxIdleTime = this.maxIdleTime
      comboPooledDataSource.maxIdleTimeExcessConnections = this.maxIdleTimeExcessConnections
      comboPooledDataSource.propertyCycle = this.propertyCycle
      comboPooledDataSource.numHelperThreads = this.numHelperThreads
      comboPooledDataSource.unreturnedConnectionTimeout = this.unreturnedConnectionTimeout
      comboPooledDataSource.isDebugUnreturnedConnectionStackTraces = this.debugUnreturnedConnectionStackTraces
      comboPooledDataSource.isForceSynchronousCheckins = this.forceSynchronousCheckins
      comboPooledDataSource.maxStatements = this.maxStatements
      comboPooledDataSource.maxStatementsPerConnection = this.maxStatementsPerConnection
      comboPooledDataSource.maxAdministrativeTaskTime = this.maxAdministrativeTaskTime
      comboPooledDataSource.preferredTestQuery = this.preferredTestQuery
      comboPooledDataSource.statementCacheNumDeferredCloseThreads = this.statementCacheNumDeferredCloseThreads
      comboPooledDataSource.isUsesTraditionalReflectiveProxies = this.usesTraditionalReflectiveProxies
      comboPooledDataSource.automaticTestTable = this.automaticTestTable
      comboPooledDataSource.acquireIncrement = this.acquireIncrement
      comboPooledDataSource.acquireRetryDelay = this.acquireRetryDelay
      comboPooledDataSource.acquireRetryAttempts = this.acquireRetryAttempts
      comboPooledDataSource.connectionTesterClassName = this.connectionTesterClassName
      comboPooledDataSource.initialPoolSize = this.initialPoolSize
      comboPooledDataSource.contextClassLoaderSource = this.contextClassLoaderSource
      comboPooledDataSource.isPrivilegeSpawnedThreads = this.privilegeSpawnedThreads
      comboPooledDataSource.isForceUseNamedDriverClass = this.forceUseNamedDriverClass
    }
  }

  private fun configDbcp(basicDataSource: BasicDataSource) {
    mySqlProperties.dbcp ?: return

    with(mySqlProperties.dbcp!!) {
      basicDataSource.defaultAutoCommit = this.defaultAutoCommit
      basicDataSource.defaultReadOnly = defaultReadOnly
      basicDataSource.defaultTransactionIsolation = defaultTransactionIsolation
      basicDataSource.defaultQueryTimeout = defaultQueryTimeout
      basicDataSource.defaultCatalog = defaultCatalog
      basicDataSource.cacheState = cacheState
      basicDataSource.lifo = lifo
      basicDataSource.maxTotal = maxTotal
      basicDataSource.maxIdle = maxIdle
      basicDataSource.minIdle = minIdle
      basicDataSource.initialSize = initialSize
      basicDataSource.maxWaitMillis = maxWaitMillis
      basicDataSource.isPoolPreparedStatements = poolPreparedStatements
      basicDataSource.maxOpenPreparedStatements = maxOpenPreparedStatements
      basicDataSource.testOnCreate = testOnCreate
      basicDataSource.testOnBorrow = testOnBorrow
      basicDataSource.testOnReturn = testOnReturn
      basicDataSource.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis
      basicDataSource.numTestsPerEvictionRun = numTestsPerEvictionRun
      basicDataSource.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis
      basicDataSource.testWhileIdle = testWhileIdle
      basicDataSource.validationQuery = validationQuery
      basicDataSource.validationQueryTimeout = validationQueryTimeout
      basicDataSource.setConnectionInitSqls(connectionInitSqls)
      basicDataSource.isAccessToUnderlyingConnectionAllowed = accessToUnderlyingConnectionAllowed
      basicDataSource.maxConnLifetimeMillis = maxConnLifetimeMillis
      basicDataSource.logExpiredConnections = logExpiredConnections
      basicDataSource.jmxName = jmxName
      basicDataSource.enableAutoCommitOnReturn = enableAutoCommitOnReturn
      basicDataSource.rollbackOnReturn = rollbackOnReturn
      basicDataSource.setDisconnectionSqlCodes(disconnectionSqlCodes)
      basicDataSource.fastFailValidation = fastFailValidation
    }
  }

  private fun configHikari(hikariDataSource: HikariDataSource) {
    mySqlProperties.hikari ?: return

    with(mySqlProperties.hikari!!) {
      hikariDataSource.isAutoCommit = autoCommit
      hikariDataSource.connectionTimeout = connectionTimeout
      hikariDataSource.idleTimeout = idleTimeout
      hikariDataSource.maxLifetime = maxLifetime
      hikariDataSource.connectionTestQuery = connectionTestQuery
      hikariDataSource.minimumIdle = minimumIdle
      hikariDataSource.maximumPoolSize = maximumPoolSize
      hikariDataSource.metricRegistry = metricRegistry
      hikariDataSource.healthCheckRegistry = healthCheckRegistry
      hikariDataSource.poolName = poolName
      hikariDataSource.initializationFailTimeout = initializationFailTimeout
      hikariDataSource.isIsolateInternalQueries = isolateInternalQueries
      hikariDataSource.isAllowPoolSuspension = allowPoolSuspension
      hikariDataSource.isReadOnly = readOnly
      hikariDataSource.isRegisterMbeans = registerMbeans
      hikariDataSource.catalog = catalog
      hikariDataSource.connectionInitSql = connectionInitSql
      hikariDataSource.transactionIsolation = transactionIsolation
      hikariDataSource.validationTimeout = validationTimeout
      hikariDataSource.leakDetectionThreshold = leakDetectionThreshold
    }
  }

}

class SpringBootVFS : VFS() {

  private val resourceResolver: ResourcePatternResolver

  init {
    resourceResolver = PathMatchingResourcePatternResolver(javaClass.classLoader)
  }

  private fun preserveSubpackageName(uri: URI, rootPath: String): String {
    val uriStr = uri.toString()
    return uriStr.substring(uriStr.indexOf(rootPath))
  }

  override fun isValid() = true

  @Throws(IOException::class)
  override fun list(url: URL, path: String): List<String> {
    val resources = resourceResolver.getResources("classpath*:$path/**/*.class")
    return resources.map { preserveSubpackageName(it.uri, path) }
  }

}

enum class MySqlPoolType {
  /**
   * 不使用连接池
   */
  NONE,
  /**
   * 阿里的Druid，推荐
   */
  DRUID,
  /**
   * C3P0
   */
  C3P0,
  /**
   * Apache Commons DBCP
   */
  DBCP,
  /**
   * HikariCP
   */
  HIKARI;
}