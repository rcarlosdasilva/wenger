package io.github.rcarlosdasilva.wenger.ms.config.microservice.mysql

import org.apache.commons.pool2.impl.BaseObjectPoolConfig
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "micro-service.my-sql.dbcp")
class DbcpProperties {

  /**
   * The default auto-commit state of connections created by this pool.
   */
  var defaultAutoCommit: Boolean? = null

  /**
   * The default read-only state of connections created by this pool.
   */
  var defaultReadOnly: Boolean? = null

  /**
   * The default TransactionIsolation state of connections created by this pool.
   */
  var defaultTransactionIsolation = -1

  var defaultQueryTimeout: Int? = null

  /**
   * The default "catalog" of connections created by this pool.
   */
  var defaultCatalog: String? = null

  /**
   * The property that controls if the pooled connections cache some state
   * rather than query the database for current state to improve performance.
   */
  var cacheState = true

  /**
   * True means that borrowObject returns the most recently used ("last in")
   * connection in the pool (if there are idle connections available).  False
   * means that the pool behaves as a FIFO queue - connections are taken from
   * the idle instance pool in the order that they are returned to the pool.
   */
  var lifo = BaseObjectPoolConfig.DEFAULT_LIFO

  /**
   * The maximum number of active connections that can be allocated from
   * this pool at the same time, or negative for no limit.
   */
  var maxTotal = GenericObjectPoolConfig.DEFAULT_MAX_TOTAL

  /**
   * The maximum number of connections that can remain idle in the
   * pool, without extra ones being destroyed, or negative for no limit.
   * If maxIdle is set too low on heavily loaded systems it is possible you
   * will see connections being closed and almost immediately new connections
   * being opened. This is a result of the active threads momentarily closing
   * connections faster than they are opening them, causing the number of idle
   * connections to rise above maxIdle. The best value for maxIdle for heavily
   * loaded system will vary but the default is a good starting point.
   */
  var maxIdle = GenericObjectPoolConfig.DEFAULT_MAX_IDLE

  /**
   * The minimum number of active connections that can remain idle in the
   * pool, without extra ones being created when the evictor runs, or 0 to create none.
   * The pool attempts to ensure that minIdle connections are available when the idle object evictor
   * runs. The value of this property has no effect unless [.timeBetweenEvictionRunsMillis]
   * has a positive value.
   */
  var minIdle = GenericObjectPoolConfig.DEFAULT_MIN_IDLE

  /**
   * The initial number of connections that are created when the pool
   * is started.
   */
  var initialSize = 0

  /**
   * The maximum number of milliseconds that the pool will wait (when there
   * are no available connections) for a connection to be returned before
   * throwing an exception, or <= 0 to wait indefinitely.
   */
  var maxWaitMillis = BaseObjectPoolConfig.DEFAULT_MAX_WAIT_MILLIS

  /**
   * Prepared statement pooling for this pool. When this property is set to `true`
   * both PreparedStatements and CallableStatements are pooled.
   */
  var poolPreparedStatements = false

  /**
   *
   * The maximum number of open statements that can be allocated from
   * the statement pool at the same time, or negative for no limit.  Since
   * a connection usually only uses one or two statements at a time, this is
   * mostly used to help detect resource leaks.
   *
   *
   * Note: As of version 1.3, CallableStatements (those produced by [java.sql.Connection.prepareCall])
   * are pooled along with PreparedStatements (produced by [java.sql.Connection.prepareStatement])
   * and `maxOpenPreparedStatements` limits the total number of prepared or callable statements
   * that may be in use at a given time.
   */
  var maxOpenPreparedStatements = GenericKeyedObjectPoolConfig.DEFAULT_MAX_TOTAL

  /**
   * The indication of whether objects will be validated as soon as they have
   * been created by the pool. If the object fails to validate, the borrow
   * operation that triggered the creation will fail.
   */
  var testOnCreate = false

  /**
   * The indication of whether objects will be validated before being
   * borrowed from the pool.  If the object fails to validate, it will be
   * dropped from the pool, and we will attempt to borrow another.
   */
  var testOnBorrow = true

  /**
   * The indication of whether objects will be validated before being
   * returned to the pool.
   */
  var testOnReturn = false

  /**
   * The number of milliseconds to sleep between runs of the idle object
   * evictor thread.  When non-positive, no idle object evictor thread will
   * be run.
   */
  var timeBetweenEvictionRunsMillis = BaseObjectPoolConfig.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS

  /**
   * The number of objects to examine during each run of the idle object
   * evictor thread (if any).
   */
  var numTestsPerEvictionRun = BaseObjectPoolConfig.DEFAULT_NUM_TESTS_PER_EVICTION_RUN

  /**
   * The minimum amount of time an object may sit idle in the pool before it
   * is eligible for eviction by the idle object evictor (if any).
   */
  var minEvictableIdleTimeMillis = BaseObjectPoolConfig.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS

  /**
   * The indication of whether objects will be validated by the idle object
   * evictor (if any).  If an object fails to validate, it will be dropped
   * from the pool.
   */
  var testWhileIdle = false

  /**
   * The SQL query that will be used to validate connections from this pool
   * before returning them to the caller.  If specified, this query
   * **MUST** be an SQL SELECT statement that returns at least
   * one row. If not specified, [java.sql.Connection.isValid] will be used
   * to validate connections.
   */
  var validationQuery: String? = null

  /**
   * Timeout in seconds before connection validation queries fail.
   */
  var validationQueryTimeout = -1

  /**
   * These SQL statements run once after a Connection is created.
   *
   *
   * This property can be used for example to run ALTER SESSION SET
   * NLS_SORT=XCYECH in an Oracle Database only once after connection
   * creation.
   *
   */
  var connectionInitSqls: List<String>? = null

  /**
   * Controls access to the underlying connection.
   */
  var accessToUnderlyingConnectionAllowed = false

  var maxConnLifetimeMillis: Long = -1

  var logExpiredConnections = true

  var jmxName: String? = null

  var enableAutoCommitOnReturn = true

  var rollbackOnReturn = true

  var disconnectionSqlCodes: Set<String>? = null

  var fastFailValidation: Boolean = false

}