package io.github.rcarlosdasilva.wenger.feature.aliyun.green

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.annotation.JSONField
import com.aliyuncs.AcsRequest
import com.aliyuncs.DefaultAcsClient
import com.aliyuncs.IAcsClient
import com.aliyuncs.exceptions.ClientException
import com.aliyuncs.green.model.v20170112.*
import com.aliyuncs.http.FormatType
import com.aliyuncs.http.MethodType
import com.aliyuncs.profile.DefaultProfile
import com.google.common.collect.Lists
import io.github.rcarlosdasilva.kits.string.Characters
import io.github.rcarlosdasilva.kits.string.TextHelper
import io.github.rcarlosdasilva.wenger.common.constant.GeneralConstant
import io.github.rcarlosdasilva.wenger.common.exception.WengerRuntimeException
import io.github.rcarlosdasilva.wenger.feature.aliyun.green.async.AsyncContentHolder
import io.github.rcarlosdasilva.wenger.feature.aliyun.green.async.PollingProcessor
import io.github.rcarlosdasilva.wenger.feature.config.AppProperties
import io.github.rcarlosdasilva.wenger.feature.extension.runIf
import io.github.rcarlosdasilva.wenger.feature.extension.runUnless
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.io.Serializable
import java.io.UnsupportedEncodingException
import kotlin.concurrent.thread

/**
 * 阿里云内容安全功能封装
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@ConditionalOnProperty(name = ["app.aliyun.green.enable"], havingValue = "true")
@Component
@EnableConfigurationProperties(value = [AppProperties::class])
class AliyunGreenHandler @Autowired constructor(
    private val appProperties: AppProperties
) : SmartInitializingSingleton {

  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  private lateinit var client: IAcsClient
  private lateinit var pollingProcessor: PollingProcessor

  override fun afterSingletonsInstantiated() {
    with(appProperties.aliyun) {
      AliyunGreenHandler.region = this.green.region
      AliyunGreenHandler.isUseAsync = this.green.useAsync

      val profile = DefaultProfile.getProfile(this.green.region, this.accessId, this.accessSecret)
      try {
        DefaultProfile.addEndpoint(region, region, DEFAULT_GREEN_PRODUCT_NAME, this.green.address)
        client = DefaultAcsClient(profile)
      } catch (ex: ClientException) {
        throw WengerAliyunGreenException("[Aliyun:GREEN] - 初始化异常", ex)
      }

      if (this.green.useAsync) {
        AsyncContentHolder.init(this.green.asyncInterval)

        pollingProcessor = PollingProcessor(this@AliyunGreenHandler)
        thread(start = true, isDaemon = true, name = ASYNC_THREAD_NAME) { pollingProcessor.start() }
      }
    }
  }

  private fun send(request: AcsRequest<*>, data: String): List<GreenResult> {
    try {
      request.setContent(data.toByteArray(GeneralConstant.DEFAULT_CHARSET), GeneralConstant.DEFAULT_ENCODING, FormatType.JSON)
      val response = client.doAction(request)
      if (!response.isSuccess) {
        logger.error("[Aliyun:GREEN] - 文本安全请求失败，response status: {}", response.status)
        return emptyList()
      }

      return parseResponse(String(response.content, GeneralConstant.DEFAULT_CHARSET))
    } catch (ex: UnsupportedEncodingException) {
      throw WengerAliyunGreenException("[Aliyun:GREEN] - 文本安全请求异常", ex)
    } catch (ex: ClientException) {
      throw WengerAliyunGreenException("[Aliyun:GREEN] - 文本安全请求异常", ex)
    }
  }

  private fun configRequest(request: AcsRequest<*>): AcsRequest<*> = request.apply {
    this.acceptFormat = FormatType.JSON
    this.contentType = FormatType.JSON
    this.method = MethodType.POST
    this.encoding = GeneralConstant.DEFAULT_ENCODING
    this.regionId = region
    this.connectTimeout = DEFAULT_CONNECT_TIMEOUT
    this.readTimeout = DEFAULT_READ_TIMEOUT
  }

  private fun parseResponse(responseText: String): List<GreenResult> {
    val response = try {
      JSON.parseObject(responseText)
    } catch (ex: Exception) {
      throw WengerAliyunGreenException("[Aliyun:GREEN] - 响应内容解析异常", ex)
    }

    val code = response["code"]?.toString()?.toInt()
        ?: throw WengerAliyunGreenException("[Aliyun:GREEN] - 获取不到响应参数code")
    val status = HttpStatus.valueOf(code)

    status.is2xxSuccessful.runUnless {
      logger.warn("文本检测结果不成功，code: {}", status)
      return emptyList()
    }

    val datas = response.getJSONArray("data")
    return datas.map { (it as JSONObject).toJavaObject(GreenResult::class.java) }
  }

  // ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ functions ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓

  /**
   * 检测文本
   *
   * @param contents [GreenContent]
   * @param scene    检测场景
   * @return [GreenResult]
   */
  fun text(contents: List<GreenContent>, scene: TextScene): List<GreenResult> {
    contents.isEmpty().runIf { return emptyList() }

    val data = JSONObject().apply {
      this["scenes"] = Lists.newArrayList(scene.toString())
      this["tasks"] = contents
    }

    return send(configRequest(TextScanRequest()), data.toJSONString())
  }

  /**
   * 检测图片
   *
   * 是否同步执行，取决于配置useAsync（同步调用时图片大小限制为5M，异步调用时图片大小限制为20M，图片下载时间限制为3s内，如果下载时间超过3s返回下载超时），建议使用oss存储或者cdn做缓存
   *
   * @param contents [GreenContent]
   * @param scenes   如果一次需要检测多种场景
   * @return [GreenResult]
   */
  fun image(contents: List<GreenContent>, vararg scenes: ImageScene): List<GreenResult> {
    contents.isEmpty().runIf { return emptyList() }

    val data = JSONObject().apply {
      this["scenes"] = scenes.map(ImageScene::toString)
      this["tasks"] = contents
    }

    return if (isUseAsync) {
      send(configRequest(ImageAsyncScanRequest()), data.toJSONString()).apply {
        forEach { AsyncContentHolder.addImageTask(it.taskId!!) }
      }
    } else {
      send(configRequest(ImageSyncScanRequest()), data.toJSONString())
    }
  }

  /**
   * 检测视频（只有异步方式）
   *
   * @param contents [GreenContent]
   * @param scenes   如果一次需要检测多种场景
   * @return [GreenResult]
   */
  fun video(contents: List<GreenContent>, vararg scenes: VideoScene): List<GreenResult> {
    contents.isEmpty().runIf { return emptyList() }

    val data = JSONObject().apply {
      this["scenes"] = scenes.map(VideoScene::toString)
      this["tasks"] = contents
    }

    return send(configRequest(VideoAsyncScanRequest()), data.toJSONString()).apply {
      forEach { AsyncContentHolder.addVideoTask(it.taskId!!) }
    }
  }

  /**
   * 异步查询图片检测结果，开发者无需调用，可使用[AsyncContentHolder.imageResults]获取
   *
   * @param tasks 任务ID列表
   * @return [GreenResult]
   */
  internal fun imageAsyncResults(tasks: List<String>): List<GreenResult> {
    tasks.isEmpty().runIf { return emptyList() }

    return send(configRequest(ImageAsyncScanResultsRequest()), JSONArray().let {
      it.addAll(tasks)
      it.toJSONString()
    })
  }

  /**
   * 异步查询视频检测结果，开发者无需调用，可使用[AsyncContentHolder.videoResults]获取
   *
   * @param tasks 任务ID列表
   * @return [GreenResult]
   */
  internal fun videoAsyncResults(tasks: List<String>): List<GreenResult> {
    tasks.isEmpty().runIf { return emptyList() }

    return send(configRequest(VideoAsyncScanResultsRequest()), JSONArray().let {
      it.addAll(tasks)
      it.toJSONString()
    })
  }

  companion object {
    var region = ""
    var isUseAsync = false

    private const val ASYNC_THREAD_NAME = "aliyun-green-async-polling"
    private const val DEFAULT_GREEN_PRODUCT_NAME = "Green"
    private const val DEFAULT_CONNECT_TIMEOUT = 3000
    private const val DEFAULT_READ_TIMEOUT = 6000
    private const val MAX_ASYN_RESULT_REQUEST_TASK = 1000
  }

}

/**
 * 待检测内容，使用GreenContent.ofXXX()创建
 */
class GreenContent private constructor(@field:JSONField(name = "dataId")
                                       private val mark: String) : Serializable {
  internal var content: String? = null
  internal var url: String? = null

  companion object Builder {
    private const val MAX_TEXT_CONTENT_LENGTH = 3999
    private const val MAX_TASK_SIZE = 100

    private val logger: Logger = LoggerFactory.getLogger(GreenContent::class.java)

    /**
     * 创建文本内容
     *
     * @param mark 文本内容唯一标识，通常为文本所属的资源id，对应dataId，可为null，会自动生成随机字符串作为标识
     * @param text 文本字符串
     * @return [GreenContent]
     * @throws WengerAliyunGreenException 内容为空时
     */
    fun ofText(mark: String?, text: String): List<GreenContent> {
      val m = mark ?: TextHelper.random(10, Characters.NUMBERS_AND_LETTERS)
      val contents = text.chunked(MAX_TEXT_CONTENT_LENGTH).map {
        GreenContent(m).apply { this.content = it }
      }
      (contents.size > MAX_TASK_SIZE).runIf {
        val brief = TextHelper.brief(contents[0].content, 100, "...")
        logger.warn("[Aliyun:GREEN] - 文本过长，单个任务最多4000个字符，一次请求最多100个任务，可能导致请求失败，文本开头：{}", brief)
      }
      return contents
    }

    /**
     * 创建图片内容
     *
     * @param mark 图片内容唯一标识，通常为文本所属的资源id，对应dataId，可为null，会自动生成随机字符串作为标识
     * @param url  图片URL
     * @return [GreenContent]
     */
    fun ofImage(mark: String?, url: String): GreenContent =
        GreenContent(mark ?: TextHelper.random(10, Characters.NUMBERS_AND_LETTERS)).apply { this.url = url }

    /**
     * 创建图片内容
     *
     * @param mark 图片内容唯一标识，通常为文本所属的资源id，对应dataId，可为null，会自动生成随机字符串作为标识
     * @param url  图片URL
     * @return [GreenContent]
     */
    fun ofImage(mark: String?, url: List<String>): List<GreenContent> =
        url.map { ofImage(mark ?: TextHelper.random(10, Characters.NUMBERS_AND_LETTERS), it) }
  }

}

/**
 * 检测结果封装
 */
class GreenResult {
  var code: String? = null
  /**
   * 错误描述信息
   */
  @JSONField(name = "msg")
  var message: String? = null
  /**
   * 对应请求中的dataId，也是[GreenContent]中的mark属性
   */
  var dataId: String? = null
  /**
   * 阿里端执行的任务id.
   *
   *
   * 云盾内容安全服务器返回的唯一标识该检测任务的ID
   */
  var taskId: String? = null
  var content: String? = null
  var url: String? = null
  /**
   * 检测详细内容
   */
  @JSONField(name = "results")
  var details: List<Detail>? = null

  fun isSuccess(): Boolean = code?.toInt() == HttpStatus.OK.value()

  /**
   * 检测结果详细内容
   *
   * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
   */
  class Detail {
    /**
     * 风险场景，一般与请求时设置的scenes相同
     */
    var scene: String? = null
    /**
     * 建议用户处理，取值范围：[“pass”, “review”, “block”],
     * pass:文本正常，review：需要人工审核，block：文本违规，可以直接删除或者做限制处理
     */
    private var suggestion: Suggestion? = null
    /**
     * 该文本的分类，取值范围参考1.1小节
     */
    var label: String? = null
    /**
     * 结果为该分类的概率；值越高，越趋于该分类；取值为[0.00-100.00], 分值仅供参考，您只需要关注label和suggestion的取值即可
     */
    var rate: Float = 0.toFloat()

    fun setSuggestion(suggestion: String) {
      this.suggestion = Suggestion.valueOf(suggestion.toUpperCase())
    }
  }
}


class WengerAliyunGreenException : WengerRuntimeException {
  constructor() : super()
  constructor(message: String?) : super(message)
  constructor(message: String?, cause: Throwable?) : super(message, cause)
  constructor(cause: Throwable?) : super(cause)
  constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(message, cause, enableSuppression, writableStackTrace)
}

// TODO 支持回调函数