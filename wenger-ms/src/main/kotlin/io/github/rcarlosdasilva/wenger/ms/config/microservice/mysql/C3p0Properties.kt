package io.github.rcarlosdasilva.wenger.ms.config.microservice.mysql

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "micro-service.my-sql.c3p0")
class C3p0Properties {

  var testConnectionOnCheckout = true
  var testConnectionOnCheckin = true
  var dataSourceName = ""
  var minPoolSize = 10
  var maxPoolSize = 40
  var checkoutTimeout = 2000
  var idleConnectionTestPeriod = 30
  var maxConnectionAge = 10
  var maxIdleTime = 2
  var maxIdleTimeExcessConnections = 1
  var propertyCycle = 1
  var numHelperThreads = 10
  var unreturnedConnectionTimeout = 15
  var debugUnreturnedConnectionStackTraces = true
  var forceSynchronousCheckins = true
  var maxStatements = 20
  var maxStatementsPerConnection = 5
  var maxAdministrativeTaskTime = 3
  var preferredTestQuery = "SELECT 1"
  var statementCacheNumDeferredCloseThreads = 1
  var usesTraditionalReflectiveProxies = true
  var automaticTestTable: String? = null
  var acquireIncrement = 3
  var acquireRetryDelay = 1000
  var acquireRetryAttempts = 60
  var connectionTesterClassName = "com.mchange.v2.c3p0.test.AlwaysFailConnectionTester"
  var initialPoolSize = 15
  var contextClassLoaderSource: String? = null
  var privilegeSpawnedThreads = true
  var forceUseNamedDriverClass = true

}