package io.github.rcarlosdasilva.wenger.feature.region

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.google.common.collect.*
import io.github.rcarlosdasilva.kits.string.TextHelper
import io.github.rcarlosdasilva.wenger.common.exception.WengerRuntimeException
import io.github.rcarlosdasilva.wenger.feature.config.AppProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Component
import java.io.IOException
import java.io.InputStreamReader
import java.net.MalformedURLException
import java.util.*

internal const val DEFAULT_PROVINCE_PATTERN = "^[1-9]\\d0000$"
internal const val DEFAULT_CITY_PATTERN = "^[1-9]\\d{3}00$"
internal const val DEFAULT_DISTRICT_PATTERN = "^\\d{4}(\\d[1-9]|[1-9]\\d)$"

/**
 * 地区数据解析器
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@ConditionalOnProperty(name = ["app.misc.region.enable"], havingValue = "true")
@Component
@EnableConfigurationProperties(value = [AppProperties::class])
class RegionHandler @Autowired constructor(
    private val appProperties: AppProperties
) : SmartInitializingSingleton {

  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  private val china = Region("+86", "中国")
  private lateinit var indexes: BiMap<String, String> // 区域可逆键值索引，用于速查
  private lateinit var json: String

  override fun afterSingletonsInstantiated() {
    val resource = load()

    val properties = try {
      InputStreamReader(resource.inputStream).use {
        Properties().apply { this.load(it) }
      }
    } catch (ex: IOException) {
      throw WengerRegionException("[地域] - 文件读取失败", ex)
    }

    // 解析
    val tempProvinces = linkedMapOf<String, Region>()
    val tempCities = LinkedListMultimap.create<String, Region>()
    val tempDistricts = LinkedListMultimap.create<String, Region>()

    val tempIndexes = HashBiMap.create<String, String>()
    val keys = properties.stringPropertyNames().sorted()

    // 归类
    keys.forEach {
      val name = properties.getProperty(it)
      tempIndexes.forcePut(it, name)

      when {
        isProvince(it) -> tempProvinces[it] = Region(it, name)
        isCity(it) -> tempCities.put(fragment(it, 2), Region(it, name))
        isDistrict(it) -> tempDistricts.put(fragment(it, 4), Region(it, name))
        else -> logger.warn("[区域] - 有不合法数据：{}", it)
      }
    }

    // 连接省市区
    tempProvinces.forEach {
      val cities = tempCities.get(fragment(it.key, 2)) // it = 省code

      cities.forEach {
        val districts = tempDistricts.get(fragment(it.code, 4)) // it = 市Region
        it.subs = districts.associate { it.code to it }
      }
      it.value.subs = cities.associate { it.code to it }
    }

    china.subs = ImmutableSortedMap.copyOf(tempProvinces)
    indexes = ImmutableBiMap.copyOf(tempIndexes)

    toJson()
  }

  private fun load(): Resource {
    val dataLocation = appProperties.misc.region.location
    // 默认jar包内数据文件
    var resource: Resource = ClassPathResource(dataLocation)

    // 尝试URL读取
    if (!resource.exists()) {
      resource = try {
        UrlResource(dataLocation)
      } catch (ex: MalformedURLException) {
        FileSystemResource(dataLocation) // 尝试本地文件读取
      }
    }

    // 无法读取
    if (!resource.exists()) {
      throw WengerRegionException("[地域] - 无法找到地域数据文件，请确保app.misc.region.location配置的文件地址存在")
    }
    return resource
  }

  private fun toJson() {
    val root = JSONObject()
    val jps = JSONArray()
    root["provinces"] = jps

    china.subs!!.values.forEach { province ->
      val jp = JSONObject()
      val jcs = JSONArray()
      jp["code"] = province.code
      jp["name"] = province.name
      jp["cities"] = jcs

      province.subs!!.values.forEach { city ->
        val jc = JSONObject()
        val jds = JSONArray()
        jc["code"] = city.code
        jc["name"] = city.name
        jc["districts"] = jds

        city.subs!!.values.forEach { district ->
          val jd = JSONObject()
          jd["code"] = district.code
          jd["name"] = district.name
          jds.add(jd)
        }
        jcs.add(jc)
      }
      jps.add(jp)
    }

    json = root.toJSONString()
  }

  private fun fragment(code: String, length: Int): String = TextHelper.fill(code.substring(0, length), "0", -6)

  // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ functions ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

  fun isProvince(code: String): Boolean = code.matches(DEFAULT_PROVINCE_PATTERN.toRegex())
  fun isCity(code: String): Boolean = code.matches(DEFAULT_CITY_PATTERN.toRegex())
  fun isDistrict(code: String): Boolean = code.matches(DEFAULT_DISTRICT_PATTERN.toRegex())

  /**
   * 将地区信息转成字符串并返回
   */
  fun json(): String = json

  /**
   * 根据code获取地区名
   *
   * @param code code
   * @return name
   */
  fun name(code: String): Optional<String> = Optional.ofNullable(indexes[code])

  /**
   * 根据地区名获取code，可能为null
   *
   * 因为每个市下都有一个市辖区，所以不保证获取市辖区的code正确
   *
   * @param name name
   * @return code
   */
  fun code(name: String): Optional<String> = Optional.ofNullable(indexes.inverse()[name])

  /**
   * 获取所有省
   *
   * @return 省集合
   */
  fun getProvinces(): Collection<Region> = china.subs!!.values

  /**
   * 根据code（不一定非要是省code，也可以是市或区code），获取所有市。例如：370202为山东青岛市南区，返回山东下所有城市
   *
   * @param code code
   * @return 市
   */
  fun getCities(code: String): Collection<Region> =
      china.subs!![fragment(code, 2)]?.subs?.values ?: emptyList()

  /**
   * 根据code（市或区code），获取所有行政区
   *
   * @param code code
   * @return 行政区
   */
  fun getDistricts(code: String): Collection<Region> =
      china.subs!![fragment(code, 2)]?.subs!![fragment(code, 4)]?.subs?.values ?: emptyList()

  /**
   * 根据code，取出code下所有区域
   *
   * 即code=省时，取省下所有市，code=市时，取市下所有区，code=区时，返回空集合，code=null或空字符串时返回所有省，code不存在返回空集合
   *
   * @param code code
   * @return regions
   */
  fun getLower(code: String?): Collection<Region> {
    return when {
      code == null || code.isEmpty() -> getProvinces()
      isProvince(code) -> getCities(code)
      isCity(code) -> getDistricts(code)
      else -> emptyList()
    }
  }

  /**
   * 根据code，返回当前及上级所有地区名，例如：传入市的code，返回省与市
   *
   * 与getLower, getProvinces, getCities, getDistricts方法不同，返回的Region中无subs属性
   *
   * @param code code
   * @return regions
   */
  fun getFullRegion(code: String): Collection<Region> =
      listOf(fragment(code, 2), fragment(code, 4), code).mapNotNull {
        val name = indexes[it]
        if (name != null) Region(it, name) else null
      }

  /**
   * 根据code，返回当前及上级所有的地区名称
   *
   * 与getFullRegion类似，返回为全部地区的名称字符串
   *
   * @param code code
   * @param separator 区域之间间隔符
   * @return name
   */
  fun getFullRegionName(code: String, separator: String = ""): String =
      listOf(fragment(code, 2), fragment(code, 4), code).mapNotNull { indexes[it] }.joinToString(separator)

}

/**
 * 区域基本抽象类
 */
data class Region(val code: String, val name: String) {
  internal var subs: Map<String, Region>? = null
}

/**
 * 中国（区域根类）
 */
//class China

class WengerRegionException : WengerRuntimeException {
  constructor() : super()
  constructor(message: String?) : super(message)
  constructor(message: String?, cause: Throwable?) : super(message, cause)
  constructor(cause: Throwable?) : super(cause)
  constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(message, cause, enableSuppression, writableStackTrace)
}

// TODO 数据Reload
// TODO 编辑数据，并回写文件