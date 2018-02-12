package io.github.rcarlosdasilva.wenger.feature.aliyun.oss

import com.aliyun.oss.common.utils.BinaryUtil
import com.aliyun.oss.model.ObjectMetadata
import com.google.common.io.ByteStreams
import io.github.rcarlosdasilva.kits.io.FileHelper
import io.github.rcarlosdasilva.kits.io.FileSignatures
import io.github.rcarlosdasilva.kits.string.Characters
import io.github.rcarlosdasilva.kits.string.TextHelper
import io.github.rcarlosdasilva.wenger.common.constant.GeneralConstant
import io.github.rcarlosdasilva.wenger.feature.aliyun.oss.AliyunOssHandler.Companion.applicationName
import io.github.rcarlosdasilva.wenger.feature.aliyun.oss.AliyunOssHandler.Companion.defaultMaxAge
import io.github.rcarlosdasilva.wenger.feature.aliyun.oss.AliyunOssHandler.Companion.profile
import io.github.rcarlosdasilva.wenger.feature.aliyun.oss.AliyunOssHandler.Companion.tempDir
import io.github.rcarlosdasilva.wenger.feature.context.RuntimeProfile
import io.github.rcarlosdasilva.wenger.feature.extension.runIf
import mu.KotlinLogging
import org.joda.time.DateTime
import org.springframework.util.MimeTypeUtils
import org.springframework.util.StreamUtils
import java.io.*
import java.net.URL
import java.nio.charset.Charset
import java.nio.file.Files

/**
 * 阿里云OSS内容装饰类
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
class ContentWrapper private constructor(val path: Path) : AutoCloseable {

  private val logger = KotlinLogging.logger {}

  private var `object`: Any? = null
  private var type: ObjectType? = null
  /**
   * OSS文件的Cache-Control: max-age，单位：秒
   */
  var maxAge: Long = -1
  /**
   * 使用断点续传，适用于大文件，或网络环境差的情况
   *
   * 否则简单上传，为false时
   */
  var isBreakpointUpload = false

  /**
   * 断点续传（分片）执行的线程数（默认5）.
   */
  val threads = 5
  /**
   * 断点续传（分片）的分片大小（默认200k）.
   *
   *
   * 阿里封装SDK中默认100k，如果传值小于100k，则取默认值
   */
  val sizeOfPart: Long = 204800

  private var stream: InputStream? = null
  private var streamSize: Int = 0
  private var file: File? = null


  /**
   * 生成meta元信息
   *
   * @return [ObjectMetadata]
   */
  fun meta(): ObjectMetadata {
    toInputStream()
    var realExtension = identifyRealType()
    if (realExtension == null) {
      realExtension = path.extension
    }

    val content: ByteArray?
    try {
      stream!!.mark(streamSize + 1)
      content = ByteStreams.toByteArray(stream!!)
      stream!!.reset()
    } catch (ex: IOException) {
      throw WengerAliyunOssException("[Aliyun:OSS] - 读取内容失败", ex)
    }

    return ObjectMetadata().apply {
      this.contentEncoding = GeneralConstant.DEFAULT_ENCODING
      this.cacheControl = "max-age: " + maxAge
      this.lastModified = DateTime.now().toDate()
      this.contentType = mimetype(realExtension)
      val md5 = BinaryUtil.toBase64String(BinaryUtil.calculateMd5(content))
      this.contentMD5 = md5
      this.contentLength = content!!.size.toLong()
    }
  }

  private fun mimetype(extension: Extension): String {
    when (extension) {
      Extension.HTML -> return MimeTypeUtils.TEXT_HTML_VALUE
      Extension.JPG -> return MimeTypeUtils.IMAGE_JPEG_VALUE
      Extension.PNG -> return MimeTypeUtils.IMAGE_PNG_VALUE
      Extension.GIF -> return MimeTypeUtils.IMAGE_GIF_VALUE
      Extension.BMP -> return Extension.IMAGE_BMP_VALUE
      Extension.MP3 -> return Extension.AUDIO_MP3_VALUE
      Extension.WAV -> return Extension.AUDIO_WAV_VALUE
      Extension.MP4 -> return Extension.VIDEO_MP4_VALUE
      Extension.AVI -> return Extension.VIDEO_AVI_VALUE
      Extension.FLV -> return Extension.VIDEO_FLV_VALUE
      Extension.WMV -> return Extension.VIDEO_WMV_VALUE
      Extension.JSON -> return MimeTypeUtils.APPLICATION_JSON_VALUE
      Extension.XML -> return MimeTypeUtils.APPLICATION_XML_VALUE
      else -> return MimeTypeUtils.TEXT_PLAIN_VALUE
    }
  }

  /**
   * 从流中识别文件真实类型.
   *
   * @return [Extension]
   */
  private fun identifyRealType(): Extension? {
    val signature = FileHelper.type(stream!!)
    if (signature == null) {
      logger.warn { "[Aliyun:OSS] - 无法识别正确的文件类型" }
      return null
    }
    val ext = Extension[signature]
    if (ext == null) {
      logger.warn { "[Aliyun:OSS] - 文件头中未匹配到已知的类型，将使用指定的文件类型" }
    }
    return ext
  }

  /**
   * 上传文本信息.
   *
   * @param text 文本
   */
  fun setObject(text: String) {
    this.`object` = text
    this.type = ObjectType.TEXT
  }

  /**
   * 上传文件（本地）.
   *
   * @param file 文件
   */
  fun setObject(file: File) {
    this.`object` = file
    this.type = ObjectType.FILE
  }

  /**
   * 上传网络资源
   *
   * @param link 网络资源
   */
  fun setObject(link: URL) {
    this.`object` = link
    this.type = ObjectType.URL
  }

  /**
   * 上传输入流信息
   *
   * @param is 输入流
   */
  fun setObject(`is`: InputStream) {
    this.`object` = `is`
    this.type = ObjectType.STREAM
  }

  /**
   * 将上传内容转换成输入流形式（markSupported input stream）.
   *
   * @return [InputStream]
   * @throws WengerAliyunOssException [WengerAliyunOssException]流处理异常
   */
  internal fun toInputStream(): InputStream {
    (`object` == null).runIf { throw WengerAliyunOssException("[Aliyun:OSS] - 未设置上传内容") }
    if (stream != null) {
      return stream!!
    }

    stream = try {
      when (type!!) {
        ObjectType.TEXT -> {
          val text = `object` as String
          ByteArrayInputStream(text.toByteArray(Charset.forName("UTF-8")))
        }
        ObjectType.URL -> {
          val link = `object` as URL
          val connection = link.openConnection().apply {
            this.connectTimeout = 2000
            this.readTimeout = 2000
          }
          val `is` = connection.getInputStream()
          val bytes = StreamUtils.copyToByteArray(`is`)
          `is`.close()
          ByteArrayInputStream(bytes)
        }
        ObjectType.FILE -> {
          val temp = `object` as File
          if (!temp.exists() || !temp.isFile) {
            throw WengerAliyunOssException("[Aliyun:OSS] - 文件不存在或不是文件：" + temp.absoluteFile)
          }

          BufferedInputStream(FileInputStream(temp))
        }
        ObjectType.STREAM -> {
          val `is` = `object` as InputStream
          if (`is`.markSupported()) `is` else BufferedInputStream(`is`)
        }
      }
    } catch (ex: IOException) {
      throw WengerAliyunOssException("[Aliyun:OSS] - 上传内容读取输入流异常", ex)
    }
    streamSize = stream!!.available()

    return stream!!
  }

  /**
   * 将上传内容转换成本地文件形式.
   *
   * @return [File]
   * @throws WengerAliyunOssException [WengerAliyunOssException]流处理异常
   */
  internal fun toLocalFile(): File {
    (`object` == null).runIf { throw WengerAliyunOssException("[Aliyun:OSS] - 为设置上传内容") }
    if (file != null) {
      return file!!
    }

    file = if (type == ObjectType.FILE) {
      `object` as File?
    } else {
      val filename = TextHelper.random(16, Characters.NUMBERS_AND_LETTERS)
      val filePath = TextHelper.concat(tempDir, filename, ".", path.extension.toString().toLowerCase())
      File(filePath)
    }

    when (type) {
      ObjectType.TEXT -> {
        val text = `object` as String
        try {
          FileWriter(file).use { fileWriter -> fileWriter.write(text) }
        } catch (ex: Exception) {
          throw WengerAliyunOssException("[Aliyun:OSS] - 文本字符串写文件失败", ex)
        }
      }
      ObjectType.URL -> {
        val link = `object` as URL
        if (!FileHelper.copy(link, file)) {
          throw WengerAliyunOssException("[Aliyun:OSS] - 读取网络资源失败：" + link.toString())
        }
      }
      ObjectType.FILE -> if (!file!!.exists() || !file!!.isFile) {
        throw WengerAliyunOssException("[Aliyun:OSS] - 读取文件失败" + file!!.absolutePath)
      }
      ObjectType.STREAM -> try {
        ByteStreams.copy((`object` as InputStream), FileOutputStream(file))
      } catch (ex: IOException) {
        throw WengerAliyunOssException("[Aliyun:OSS] - 输入流写文件失败", ex)
      }
    }

    return file!!
  }

  /**
   * 关闭流.
   *
   *
   * 如果上传内容，本身从外部传入的就是输入流，则不关闭
   *
   * @throws WengerAliyunOssException [WengerAliyunOssException]流处理异常
   */
  private fun closeStream() {
    if (this.stream == null) {
      return
    }

    val `is` = this.stream
    this.stream = null
    if (this.type != ObjectType.STREAM) {
      try {
        `is`!!.close()
      } catch (ex: IOException) {
        throw WengerAliyunOssException("[Aliyun:OSS] - 关闭流失败", ex)
      }

    }
  }

  /**
   * 移除文件.
   *
   * @throws WengerAliyunOssException [WengerAliyunOssException]流处理异常
   */
  private fun removeFile() {
    if (this.file == null) {
      return
    }

    val f = this.file
    this.file = null
    try {
      Files.delete(f!!.toPath())
    } catch (ex: IOException) {
      throw WengerAliyunOssException("[Aliyun:OSS] - 临时文件删除异常", ex)
    } catch (ex: SecurityException) {
      throw WengerAliyunOssException("[Aliyun:OSS] - 临时文件删除异常", ex)
    }

  }

  override fun close() {
    closeStream()
    removeFile()
  }

  companion object Builder {
    /**
     * OSS 存放路径
     *
     * @param module    文件分模块存储，只能包含大小写英文字母、数字和斜线(/)，不能以斜线开头和结尾
     * @param category  存放子目录，只能包含大小写英文字母、数字和斜线(/)，不能以斜线开头和结尾
     * @param fileName  文件名，只能包含大小写英文字母、数字
     * @param extension 文件扩展名 [Extension]
     * @return [Path]
     */
    fun with(module: String, category: String, fileName: String, extension: Extension): ContentWrapper =
      ContentWrapper(Path(applicationName, profile, module, category, fileName, extension)).apply {
        this.maxAge = defaultMaxAge
      }
  }

  /**
   * OSS 存储路径描述及构建
   *
   * 路径由当前运行环境、目录、文件名、扩展名四个部分组成
   *
   * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
   */
  data class Path internal constructor(
    val project: String,
    val profile: RuntimeProfile,
    val module: String,
    val category: String,
    val fileName: String,
    val extension: Extension
  ) {

    /**
     * 生成OSS路径.
     *
     * @return 路径
     */
    fun get(): String = TextHelper.join(
      GeneralConstant.URL_SEPARATOR,
      project,
      profile.toString().toLowerCase(),
      module,
      category,
      fileName
    ) + "." + extension.toString().toLowerCase()


    fun validate() {
      module.isEmpty().runIf { throw WengerAliyunOssException("[Aliyun:OSS] - 未设置文件存放模块") }
      category.isEmpty().runIf { throw WengerAliyunOssException("[Aliyun:OSS] - 未设置文件存放子目录") }
      fileName.isEmpty().runIf { throw WengerAliyunOssException("[Aliyun:OSS] - 未设置文件名") }
//      if (path.getExtension() == null) {
//        throw WengerAliyunOssException("[Aliyun:OSS] - 未设置文件扩展名")
//      }

      module.matches(DEFAULT_PATH_PATTERN).runIf { throw WengerAliyunOssException("[Aliyun:OSS] - 文件存放模块包含非法字符") }
      category.matches(DEFAULT_PATH_PATTERN).runIf { throw WengerAliyunOssException("[Aliyun:OSS] - 文件存放子目录包含非法字符") }
      fileName.matches(DEFAULT_FILENAME_PATTERN).runIf { throw WengerAliyunOssException("[Aliyun:OSS] - 文件名包含非法字符") }
    }

    companion object {
      private val DEFAULT_PATH_PATTERN = "^[a-zA-Z0-9-]+(/[a-zA-Z0-9-]+)+$".toRegex()
      private val DEFAULT_FILENAME_PATTERN = "^[a-zA-Z0-9-]+$".toRegex()
    }
  }

  internal enum class ObjectType {
    /**
     * 文本内容
     */
    TEXT,
    /**
     * 本地文件
     */
    FILE,
    /**
     * URL
     */
    URL,
    /**
     * 输入流
     */
    STREAM
  }

}

/**
 * 支持的扩展名
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
enum class Extension {
  /**
   * HTML
   */
  HTML,
  /**
   * JPG
   */
  JPG,
  /**
   * PNG
   */
  PNG,
  /**
   * GIF
   */
  GIF,
  /**
   * BMP
   */
  BMP,
  /**
   * MP3
   */
  MP3,
  /**
   * WAV
   */
  WAV,
  /**
   * MP4
   */
  MP4,
  /**
   * AVI
   */
  AVI,
  /**
   * FLV
   */
  FLV,
  /**
   * WMV
   */
  WMV,
  /**
   * PROP
   */
  PROP,
  /**
   * JSON
   */
  JSON,
  /**
   * XML
   */
  XML,
  /**
   * TXT
   */
  TXT;

  companion object {
    internal val IMAGE_BMP_VALUE = MimeTypeUtils.parseMimeType("image/bmp").toString()
    internal val AUDIO_MP3_VALUE = MimeTypeUtils.parseMimeType("audio/mpeg3").toString()
    internal val AUDIO_WAV_VALUE = MimeTypeUtils.parseMimeType("audio/wav").toString()
    internal val VIDEO_MP4_VALUE = MimeTypeUtils.parseMimeType("video/mp4").toString()
    internal val VIDEO_AVI_VALUE = MimeTypeUtils.parseMimeType("video/avi").toString()
    internal val VIDEO_FLV_VALUE = MimeTypeUtils.parseMimeType("video/x-flv").toString()
    internal val VIDEO_WMV_VALUE = MimeTypeUtils.parseMimeType("video/x-ms-wmv").toString()

    operator fun get(sig: FileSignatures): Extension? = values().firstOrNull { sig.`is`(it.toString()) }
  }
}

// TODO 自定义或动态计算断点续传的线程数