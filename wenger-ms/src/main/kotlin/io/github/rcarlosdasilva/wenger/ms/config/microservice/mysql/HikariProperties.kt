package io.github.rcarlosdasilva.wenger.ms.config.microservice.mysql

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "micro-service.my-sql.druid")
class HikariProperties {

  /**
   * This property controls the default auto-commit behavior of connections returned from the pool. It is a boolean value. Default: true
   */
  var autoCommit = true

  /**
   * This property controls the maximum number of milliseconds that a client (that's you) will wait for a connection from the pool. If this time is exceeded without a connection becoming available, a SQLException will be thrown. Lowest acceptable connection timeout is 250 feature. Default: 30000 (30 seconds)
   */
  var connectionTimeout = 30000L

  /**
   * This property controls the maximum amount of time that a connection is allowed to sit idle in the pool. This setting only applies when minimumIdle is defined to be less than maximumPoolSize. Whether a connection is retired as idle or not is subject to a maximum variation of +30 seconds, and average variation of +15 seconds. A connection will never be retired as idle before this timeout. Once the pool reaches minimumIdle connections, connections will no longer be retired, even if idle. A value of 0 means that idle connections are never removed from the pool. The minimum allowed value is 10000ms (10 seconds). Default: 600000 (10 minutes)
   */
  var idleTimeout = 600000L

  /**
   * This property controls the maximum lifetime of a connection in the pool. An in-use connection will never be retired, only when it is closed will it then be removed. On a connection-by-connection basis, minor negative attenuation is applied to avoid mass-extinction in the pool. We strongly recommend setting this value, and it should be at least 30 seconds less than any database or infrastructure imposed connection time limit. A value of 0 indicates no maximum lifetime (infinite lifetime), subject of course to the idleTimeout setting. Default: 1800000 (30 minutes)
   */
  var maxLifetime = 1800000L

  /**
   * If your driver supports JDBC4 we strongly recommend not setting this property. This is for "legacy" drivers that do not support the JDBC4 Connection.isValid() API. This is the query that will be executed just before a connection is given to you from the pool to validate that the connection to the database is still alive. Again, try running the pool without this property, HikariCP will log an error if your driver is not JDBC4 compliant to let you know. Default: none
   */
  var connectionTestQuery: String? = null

  /**
   * This property controls the minimum number of idle connections that HikariCP tries to maintain in the pool. If the idle connections dip below this value and total connections in the pool are less than maximumPoolSize, HikariCP will make a best effort to add additional connections quickly and efficiently. However, for maximum performance and responsiveness to spike demands, we recommend not setting this value and instead allowing HikariCP to act as a fixed size connection pool. Default: same as maximumPoolSize
   */
  var minimumIdle = 10

  /**
   * This property controls the maximum size that the pool is allowed to reach, including both idle and in-use connections. Basically this value will determine the maximum number of actual connections to the database backend. A reasonable value for this is best determined by your execution environment. When the pool reaches this size, and no idle connections are available, calls to getConnection() will block for up to connectionTimeout milliseconds before timing out. Please read about pool sizing. Default: 10
   */
  var maximumPoolSize = 10

  /**
   * This property is only available via programmatic configuration or IoC container. This property allows you to specify an instance of a Codahale/Dropwizard MetricRegistry to be used by the pool to record various metrics. See the Metrics wiki page for details. Default: none
   */
  var metricRegistry: String? = null

  /**
   * This property is only available via programmatic configuration or IoC container. This property allows you to specify an instance of a Codahale/Dropwizard HealthCheckRegistry to be used by the pool to report current health information. See the Health Checks wiki page for details. Default: none
   */
  var healthCheckRegistry: String? = null

  /**
   * This property represents a user-defined name for the connection pool and appears mainly in logging and JMX management consoles to identify pools and pool configurations. Default: auto-generated
   */
  var poolName: String? = null

  /**
   * usedThis property controls whether the pool will "fail fast" if the pool cannot be seeded with an initial connection successfully. Any positive number is taken to be the number of milliseconds to attempt to acquire an initial connection; the application thread will be blocked during this period. If a connection cannot be acquired before this timeout occurs, an exception will be thrown. This timeout is applied after the connectionTimeout period. If the value is zero (0), HikariCP will attempt to obtain and validate a connection. If a connection is obtained, but fails validation, an exception will be thrown and the pool not started. However, if a connection cannot be obtained, the pool will start, but later efforts to obtain a connection may fail. A value less than zero will bypass any initial connection attempt, and the pool will start immediately while trying to obtain connections in the background. Consequently, later efforts to obtain a connection may fail. Default: 1
   */
  var initializationFailTimeout = 1L

  /**
   * This property determines whether HikariCP isolates internal pool queries, such as the connection alive test, in their own transaction. Since these are typically read-only queries, it is rarely necessary to encapsulate them in their own transaction. This property only applies if autoCommit is disabled. Default: false
   */
  var isolateInternalQueries = false

  /**
   * This property controls whether the pool can be suspended and resumed through JMX. This is useful for certain failover automation scenarios. When the pool is suspended, calls to getConnection() will not timeout and will be held until the pool is resumed. Default: false
   */
  var allowPoolSuspension = false

  /**
   * This property controls whether Connections obtained from the pool are in read-only mode by default. Note some databases do not support the concept of read-only mode, while others provide query optimizations when the Connection is set to read-only. Whether you need this property or not will depend largely on your application and database. Default: false
   */
  var readOnly = false

  /**
   * This property controls whether or not JMX Management Beans ("MBeans") are registered or not. Default: false
   */
  var registerMbeans = false

  /**
   * This property sets the default catalog for databases that support the concept of catalogs. If this property is not specified, the default catalog defined by the JDBC driver is used. Default: driver default
   */
  var catalog: String? = null

  /**
   * This property sets a SQL statement that will be executed after every new connection creation before adding it to the pool. If this SQL is not valid or throws an exception, it will be treated as a connection failure and the standard retry logic will be followed. Default: none
   */
  var connectionInitSql: String? = null

  /**
   * This property controls the default transaction isolation level of connections returned from the pool. If this property is not specified, the default transaction isolation level defined by the JDBC driver is used. Only use this property if you have specific isolation requirements that are common for all queries. The value of this property is the constant name from the Connection class such as TRANSACTION_READ_COMMITTED, TRANSACTION_REPEATABLE_READ, etc. Default: driver default
   */
  var transactionIsolation: String? = null

  /**
   * This property controls the maximum amount of time that a connection will be tested for aliveness. This value must be less than the connectionTimeout. Lowest acceptable validation timeout is 250 feature. Default: 5000
   */
  var validationTimeout = 5000L

  /**
   * This property controls the amount of time that a connection can be out of the pool before a message is logged indicating a possible connection leak. A value of 0 means leak detection is disabled. Lowest acceptable value for enabling leak detection is 2000 (2 seconds). Default: 0
   */
  var leakDetectionThreshold = 0L

}