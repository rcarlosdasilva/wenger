package io.github.rcarlosdasilva.wenger.feature.sequence

import io.github.rcarlosdasilva.wenger.common.exception.WengerRuntimeException
import io.github.rcarlosdasilva.wenger.feature.config.AppProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * 时间起始标记点（2015年1月1日0时0分0秒），作为基准，一般取系统的最近时间（一旦确定不能变动）
 */
internal const val TWEPOCH = 1420041600000L
/**
 * 机器id所占的位数
 */
internal const val WORKER_ID_BITS = 5
/**
 * 数据标识id所占的位数
 */
internal const val DATACENTER_ID_BITS = 5
/**
 * 支持的最大机器id，结果是31 (这个移位算法可以很快的计算出几位二进制数所能表示的最大十进制数)
 */
internal const val MAX_WORKER_ID = (-1L).shl(WORKER_ID_BITS).inv()
/**
 * 支持的最大数据标识id，结果是31
 */
internal const val MAX_DATACENTER_ID = (-1L).shl(DATACENTER_ID_BITS).inv()
/**
 * 序列在id中占的位数
 */
internal const val SEQUENCE_BITS = 12
/**
 * 机器ID向左移12位
 */
internal const val WORKER_ID_SHIFT = SEQUENCE_BITS
/**
 * 数据标识id向左移17位(12+5)
 */
internal const val DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS
/**
 * 时间截向左移22位(5+5+12)
 */
internal const val TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS
/**
 * 生成序列的掩码，这里为4095 (0b111111111111=0xfff=4095)
 */
internal const val SEQUENCE_MASK = (-1L).shl(SEQUENCE_BITS).inv()

/**
 * 获取基于Snowflake算法的ID
 *
 * 借用 http://git.oschina.net/yu120/sequence
 *
 * 基于Twitter的Snowflake算法实现分布式高效有序ID生产黑科技(sequence)
 *
 * SnowFlake的结构如下(每部分用-分开):
 *
 * 0-0000000000 0000000000 0000000000 0000000000 0-00000-00000-000000000000
 *
 * 1位标识，由于long基本类型在Java中是带符号的，最高位是符号位，正数是0，负数是1，所以id一般是正数，最高位是0
 *
 * 41位时间截(毫秒级)，注意，41位时间截不是存储当前时间的时间截，而是存储时间截的差值（当前时间截 - 开始时间截)
 * 得到的值），这里的的开始时间截，一般是我们的id生成器开始使用的时间，由我们程序来指定的（如下下面程序twepoch属性）。
 * 41位的时间截，可以使用69年，年T = (1L << 41) / (1000L * 60 * 60 * 24 * 365) = 69
 *
 * 10位的数据机器位，可以部署在1024个节点，包括5位datacenterId和5位workerId
 *
 * 12位序列，毫秒内的计数，12位的计数顺序号支持每个节点每毫秒(同一机器，同一时间截)产生4096个ID序号
 *
 * 加起来刚好64位，为一个Long型。
 * SnowFlake的优点是，整体上按照时间自增排序，并且整个分布式系统内不会产生ID碰撞(由数据中心ID和机器ID作区分)，并且效率较高，经测试，SnowFlake每秒能够产生26万ID左右。
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@ConditionalOnProperty(name = ["app.misc.sequence.enable"], havingValue = "true")
@Component
@EnableConfigurationProperties(value = [AppProperties::class])
class SequenceHandler @Autowired constructor(
    private val appProperties: AppProperties
) : SmartInitializingSingleton {

  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  private lateinit var host: Host
  /**
   * 毫秒内序列(0~4095)
   */
  private var sequence = 0L
  /**
   * 上次生成ID的时间截
   */
  private var lastTimestamp = -1L

  override fun afterSingletonsInstantiated() {
    with(appProperties.misc.sequence) {
      if (workerId !in 0..MAX_WORKER_ID) {
        throw WengerSequenceException("[ID] - WorkerId的取值超出了范围，默认0 - 31")
      }
      if (dataCenterId !in 0..MAX_DATACENTER_ID) {
        throw WengerSequenceException("[ID] - DataCenterId的取值超出了范围，默认0 - 31")
      }

      host = Host(this.workerId, this.dataCenterId)
      logger.info("[ID] - ID序列化参数：WorkerId: {}, DataCenterId: {}", this.workerId, this.dataCenterId)
    }
  }

  /**
   * @property wi workerId - 工作机器ID(0~31)
   * @property dci dataCenterId - 数据中心ID(0~31)
   */
  internal data class Host(private val wi: Long, private val dci: Long) {
    val workerId: Long = wi.shl(WORKER_ID_SHIFT)
    val dataCenterId: Long = dci.shl(DATACENTER_ID_SHIFT)
  }

  /**
   * 阻塞到下一个毫秒，直到获得新的时间戳
   *
   * @param lastTimestamp 上次生成ID的时间截
   * @return 当前时间戳
   */
  private fun until(lastTimestamp: Long): Long {
    var ts: Long
    do {
      ts = SystemClock.instance().now()
    } while (ts < lastTimestamp)
    return ts
  }

  /**
   * 获取id
   *
   * @return id
   */
  @Synchronized
  fun id(): Long {
    var timestamp = SystemClock.instance().now()

    // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过这个时候应当抛出异常
    // 闰秒
    if (timestamp < lastTimestamp) {
      val offset = lastTimestamp - timestamp
      if (offset <= 5) {
        try {
          Thread.sleep(offset.shl(1))
          timestamp = SystemClock.instance().now()
          if (timestamp < lastTimestamp) {
            throw WengerSequenceException("[ID] - 时钟回退，距当前时间：$offset 毫秒")
          }
        } catch (ex: InterruptedException) {
          throw WengerSequenceException("[ID] - 时钟回退，调整等待时异常", ex)
        }
      } else {
        throw WengerSequenceException("[ID] - 时钟回退，距当前时间：$offset 毫秒")
      }
    }

    // $NON-NLS-解决跨毫秒生成ID序列号始终为偶数的缺陷$
    // 如果是同一时间生成的，则进行毫秒内序列
    sequence = if (lastTimestamp == timestamp) {
      sequence = (sequence + 1) and SEQUENCE_MASK
      // 毫秒内序列溢出
      if (sequence == 0L)
        until(lastTimestamp) // 阻塞到下一个毫秒,获得新的时间戳
      else sequence
    } else 0L // 时间戳改变，毫秒内序列重置

    // 上次生成ID的时间截
    lastTimestamp = timestamp

    // 移位并通过或运算拼到一起组成64位的ID
    return (timestamp - TWEPOCH).shl(TIMESTAMP_LEFT_SHIFT) or host.dataCenterId or host.workerId or sequence
  }

}

/**
 * 借用 http://git.oschina.net/yu120/sequence，Kotlin重写
 * <p>
 * 高并发场景下System.currentTimeMillis()的性能问题的优化
 * <p>
 * System.currentTimeMillis()的调用比new一个普通对象要耗时的多（具体耗时高出多少我还没测试过，有人说是100倍左右）
 * <p>
 * System.currentTimeMillis()之所以慢是因为去跟系统打了一次交道
 * <p>
 * 后台定时更新时钟，JVM退出时，线程自动回收
 * <p>
 * 10亿：43410,206,210.72815533980582%
 * <p>
 * 1亿：4699,29,162.0344827586207%
 * <p>
 * 1000万：480,12,40.0%
 * <p>
 * 100万：50,10,5.0%
 * <p>
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
class SystemClock private constructor() {

  private val period: Long = 1
  private val now: AtomicLong = AtomicLong(System.currentTimeMillis())

  init {
    scheduleClockUpdating()
  }

  private fun scheduleClockUpdating() {
    val executor = ScheduledThreadPoolExecutor(1) { r ->
      Thread(r, "System Clock").apply { this.isDaemon = true }
    }

    executor.scheduleAtFixedRate({ now.set(System.currentTimeMillis()) }, period, period, TimeUnit.MILLISECONDS)
  }

  fun now(): Long = now.get()

  companion object Holder {
    private val INSTANCE = SystemClock()

    fun instance(): SystemClock {
      return INSTANCE
    }
  }

}

class WengerSequenceException : WengerRuntimeException {
  constructor() : super()
  constructor(message: String?) : super(message)
  constructor(message: String?, cause: Throwable?) : super(message, cause)
  constructor(cause: Throwable?) : super(cause)
  constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(message, cause, enableSuppression, writableStackTrace)
}