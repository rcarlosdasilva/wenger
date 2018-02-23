package io.github.rcarlosdasilva.wenger.feature.ip

import com.google.common.io.ByteStreams
import io.github.rcarlosdasilva.kits.net.AddressHelper
import io.github.rcarlosdasilva.kits.string.Characters
import io.github.rcarlosdasilva.kits.string.TextHelper
import io.github.rcarlosdasilva.kits.sys.SystemHelper
import io.github.rcarlosdasilva.wenger.common.exception.WengerRuntimeException
import io.github.rcarlosdasilva.wenger.feature.config.app.misc.IpProperties
import mu.KotlinLogging
import org.apache.commons.lang.StringUtils
import org.lionsoul.ip2region.DataBlock
import org.lionsoul.ip2region.DbConfig
import org.lionsoul.ip2region.DbMakerConfigException
import org.lionsoul.ip2region.DbSearcher
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Component
import java.io.*
import java.net.MalformedURLException

/**
 * IP解析
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 * @date Create at 2018/1/5 11:49
 */
@ConditionalOnProperty(name = ["app.misc.ip.enable"], havingValue = "true")
@Component
@EnableConfigurationProperties(value = [IpProperties::class])
class IpHandler @Autowired constructor(
  private val ipProperties: IpProperties
) : SmartInitializingSingleton {

  private val logger = KotlinLogging.logger {}

  private lateinit var searcher: DbSearcher
  private lateinit var arithmetic: IpSearchArithmetic

  override fun afterSingletonsInstantiated() {
    val resource: Resource = with(ipProperties) {
      this@IpHandler.arithmetic = this.arithmetic
      load(this.location)
    }

    val path = dump(resource)
    try {
      searcher = DbSearcher(DbConfig(), path)
    } catch (ex: DbMakerConfigException) {
      throw WengerIpException("[IP] - 初始化ip2region DbSearcher失败", ex)
    } catch (ex: FileNotFoundException) {
      throw WengerIpException("[IP] - 初始化ip2region DbSearcher失败", ex)
    }
  }

  private fun load(location: String): Resource {
    // 默认jar包内数据文件
    var resource: Resource = ClassPathResource(location)

    // 尝试URL读取
    if (!resource.exists()) {
      resource = try {
        UrlResource(location)
      } catch (ex: MalformedURLException) {
        FileSystemResource(location) // 尝试本地文件读取
      }
    }

    // 无法读取
    if (!resource.exists()) {
      throw WengerIpException("[IP] - 无法找到IP解析数据文件，请确保app.misc.ip.location配置的文件地址存在")
    }
    return resource
  }

  private fun dump(resource: Resource): String {
    val tempFilePath = SystemHelper.tempDir() + TextHelper.random(5, Characters.NUMBERS_AND_LETTERS) + ".db-migration"
    val tempFile = File(tempFilePath)
    // 转文件
    try {
      resource.inputStream.use { `is` ->
        BufferedOutputStream(FileOutputStream(tempFile)).use { os -> ByteStreams.copy(`is`, os) }
      }
    } catch (ex: IOException) {
      throw WengerIpException("[IP] - 数据文件无法读取", ex)
    }
    return tempFile.absolutePath
  }

  // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ functions ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

  /**
   * 解析IP详细信息
   */
  fun detail(ip: String): IpDetail {
    if (!AddressHelper.isLegalIpv4(ip)) {
      logger.warn { "[IP] - 不规范的IP地址：$ip" }
      return IpDetail(ip)
    }

    val data = try {
      when (arithmetic) {
        IpSearchArithmetic.BINARY -> searcher.binarySearch(ip)
        IpSearchArithmetic.B_TREE -> searcher.btreeSearch(ip)
        IpSearchArithmetic.MEMORY -> searcher.memorySearch(ip)
      }
    } catch (ex: IOException) {
      throw WengerIpException("[IP] - 搜索IP详情异常")
    }

    return convert(ip, data)
  }

  private fun convert(ip: String, dataBlock: DataBlock): IpDetail {
    if (StringUtils.isBlank(dataBlock.region)) {
      logger.warn { "[IP] - 搜索不到IP的详情：$ip" }
    }

    val detail = IpDetail(ip)
    val parts = dataBlock.region
      .split(DATA_BLOCK_REGION_SEPARATOR.toRegex())
      .dropLastWhile { it.isEmpty() }.toTypedArray()
    detail.country = parts[0]
    detail.area = parts[1]
    detail.province = parts[2]
    detail.city = parts[3]
    detail.isp = parts[4]
    return detail
  }

  companion object {
    private const val DATA_BLOCK_REGION_SEPARATOR = "\\|"
  }

}

data class IpDetail(val ip: String) {
  lateinit var country: String
  lateinit var area: String
  lateinit var province: String
  lateinit var city: String
  lateinit var isp: String
}


enum class IpSearchArithmetic {
  /**
   * Binary算法
   */
  BINARY,
  /**
   * B树算法
   */
  B_TREE,
  /**
   * 内存算法
   */
  MEMORY
}

class WengerIpException : WengerRuntimeException {
  constructor() : super()
  constructor(message: String?) : super(message)
  constructor(message: String?, cause: Throwable?) : super(message, cause)
  constructor(cause: Throwable?) : super(cause)
  constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(
    message,
    cause,
    enableSuppression,
    writableStackTrace
  )
}