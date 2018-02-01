package io.github.rcarlosdasilva.wenger.feature.aliyun.oss

import com.aliyun.oss.ClientConfiguration
import com.aliyun.oss.ClientException
import com.aliyun.oss.OSSClient
import com.aliyun.oss.OSSException
import com.aliyun.oss.model.GetObjectRequest
import com.aliyun.oss.model.OSSObject
import com.aliyun.oss.model.ObjectMetadata
import com.aliyun.oss.model.UploadFileRequest
import com.google.common.base.Strings
import io.github.rcarlosdasilva.wenger.common.exception.WengerRuntimeException
import io.github.rcarlosdasilva.wenger.feature.aliyun.oss.ContentWrapper.Path
import io.github.rcarlosdasilva.wenger.feature.config.AppProperties
import io.github.rcarlosdasilva.wenger.feature.context.EnvironmentHandler
import io.github.rcarlosdasilva.wenger.feature.context.RuntimeProfile
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.io.File
import java.io.InputStream

/**
 * 阿里云OSS封装
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@ConditionalOnProperty(name = ["app.aliyun.oss.enable"], havingValue = "true")
@Component
@EnableConfigurationProperties(value = [AppProperties::class])
class AliyunOssHandler @Autowired constructor(
    private val environmentHandler: EnvironmentHandler,
    private val appProperties: AppProperties
) : SmartInitializingSingleton, DisposableBean {

  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  private lateinit var client: OSSClient
  private lateinit var bucket: String

  override fun afterSingletonsInstantiated() {
    with(appProperties.aliyun) {
      if (Strings.isNullOrEmpty(this.oss.bucket)) {
        throw WengerAliyunOssException("[Aliyun:OSS] - 未配置OSS Bucket Name")
      }
      this@AliyunOssHandler.bucket = this.oss.bucket!!

      val conf = ClientConfiguration().apply {
        this.connectionTimeout = 30000
        this.isRequestTimeoutEnabled = true
        this.maxErrorRetry = 5
      }
      client = OSSClient(this.oss.address, this.accessId, this.accessSecret, conf)

      defaultMaxAge = this.oss.maxAge
    }

    applicationName = environmentHandler.applicationName
    profile = environmentHandler.runtimeProfile
    tempDir = environmentHandler.tempDir
    logger.info("[Aliyun:OSS] - 初始化完毕")
  }

  override fun destroy() = client.shutdown()

  /**
   * 下载文件.
   *
   * @param path     [Path]
   * @param filePath 保存本地文件路径
   */
  fun download(path: Path, filePath: String) {
    download(path.get(), filePath)
  }

  /**
   * 下载文件.
   *
   * @param path     OSS端文件路径，可使用 [Path.get]方法生成，也可提前将路径保存到数据库
   * @param filePath 保存本地文件路径
   */
  fun download(path: String, filePath: String) {
    try {
      client.getObject(GetObjectRequest(bucket, path), File(filePath))
    } catch (ex: WengerAliyunOssException) {
      throw WengerAliyunOssException("[Aliyun:OSS] - 下载文件失败", ex)
    } catch (ex: ClientException) {
      throw WengerAliyunOssException("[Aliyun:OSS] - 下载文件失败", ex)
    }
  }

  /**
   * 导出[OSSObject]
   *
   * @param path [Path]
   * @return 请求结果{@link ContentWrapper}实例。使用完之后需要手动关闭其中的ObjectContent释放请求连接
   */
  fun get(path: Path): OSSObject =
      try {
        client.getObject(bucket, path.get())
      } catch (ex: WengerAliyunOssException) {
        throw WengerAliyunOssException("[Aliyun:OSS] - 下载文件失败", ex)
      } catch (ex: ClientException) {
        throw WengerAliyunOssException("[Aliyun:OSS] - 下载文件失败", ex)
      }

  /**
   * 上传文件.
   *
   * @param cw [ContentWrapper]
   * @return [OssResult]
   */
  fun upload(cw: ContentWrapper): OssResult {
    val meta = cw.meta()
    val key = cw.path.get()

    return if (cw.isBreakpointUpload) {
      uploadBreakpoint(key, cw.toLocalFile(), meta, cw.threads, cw.sizeOfPart)
    } else {
      uploadSimple(key, cw.toInputStream(), meta)
    }
  }

  private fun uploadSimple(key: String, `is`: InputStream, meta: ObjectMetadata): OssResult {
    try {
      val result = client.putObject(bucket, key, `is`, meta)
      return OssResult(result.requestId, result.eTag, key)
    } catch (ex: OSSException) {
      logger.error("[Aliyun:OSS] - 服务器端异常，Error Message: {}, Error Code: {}, Request ID: {}, Host ID: {}",
          ex.errorMessage, ex.errorCode, ex.requestId, ex.hostId)
    } catch (ex: ClientException) {
      logger.error("[Aliyun:OSS] - 客户端异常，Error Message: {}, Error Code: {}, Request ID: {}",
          ex.errorMessage, ex.errorCode, ex.requestId)
    }

    return OssResult.FAILED
  }

  private fun uploadBreakpoint(key: String, file: File, meta: ObjectMetadata, threadNumber: Int, sizeOfPart: Long): OssResult {
    val request = UploadFileRequest(bucket, key).apply {
      this.uploadFile = file.absolutePath
      this.objectMetadata = meta
      this.taskNum = threadNumber
      this.partSize = sizeOfPart
      this.isEnableCheckpoint = true
    }

    try {
      val result = client.uploadFile(request).multipartUploadResult
      return OssResult(result.requestId, result.eTag, key)
    } catch (ex: Throwable) {
      logger.error("[Aliyun:OSS] - 断点续传异常", ex)
    }

    return OssResult.FAILED
  }


  companion object {
    var defaultMaxAge = -1L
    var applicationName = ""
    var profile: RuntimeProfile = RuntimeProfile.DEVEL
    var tempDir = ""
  }

}

data class OssResult(
    val requestId: String?,
    val etag: String?,
    val path: String?,
    val success: Boolean = true
) {
  private constructor() : this(null, null, null, false)

  companion object {
    val FAILED = OssResult()
  }
}

class WengerAliyunOssException : WengerRuntimeException {
  constructor() : super()
  constructor(message: String?) : super(message)
  constructor(message: String?, cause: Throwable?) : super(message, cause)
  constructor(cause: Throwable?) : super(cause)
  constructor(message: String?, cause: Throwable?, enableSuppression: Boolean, writableStackTrace: Boolean) : super(message, cause, enableSuppression, writableStackTrace)
}

//todo 删除文件方法
//todo 视频截帧方法