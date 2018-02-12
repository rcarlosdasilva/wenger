package io.github.rcarlosdasilva.wenger.feature.captcha

import com.google.code.kaptcha.impl.DefaultKaptcha
import com.google.code.kaptcha.util.Config
import io.github.rcarlosdasilva.wenger.common.exception.WengerRuntimeException
import io.github.rcarlosdasilva.wenger.feature.captcha.qa.AbstractCaptchaQa
import io.github.rcarlosdasilva.wenger.feature.captcha.qa.CaptchaQa
import io.github.rcarlosdasilva.wenger.feature.config.app.misc.CaptchaProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.SmartInitializingSingleton
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import java.util.*

/**
 * 验证码处理器
 *
 * Kaptcha参数：
 * ("kaptcha.border", "yes");
 * ("kaptcha.border.color", "BLACK");
 * ("kaptcha.border.thickness", "1"); 边框粗细度
 * ("kaptcha.noise.color", "BLACK"); 验证码噪点颜色
 * ("kaptcha.obscurificator.impl", "WaterRipple"); 验证码样式引擎
 * ("kaptcha.textproducer.font.names", "Arial,Courier"); 文本字体
 * ("kaptcha.textproducer.font.color", "40");
 * ("kaptcha.textproducer.font.size", "BLACK");
 * ("kaptcha.textproducer.char.space", "2"); 文本字符间距
 * ("kaptcha.background.clear.from", "LIGHT_GRAY"); 验证码背景颜色渐进
 * ("kaptcha.background.clear.to", "WHITE"); 验证码背景颜色渐进
 * ("kaptcha.image.width", "200");
 * ("kaptcha.image.height", "50");
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@ConditionalOnProperty(name = ["app.misc.captcha.enable"], havingValue = "true")
@ConditionalOnBean(value = [CaptchaCacheAdaptor::class])
@Component
@EnableConfigurationProperties(value = [CaptchaProperties::class])
class CaptchaHandler @Autowired constructor(
  private val captchaProperties: CaptchaProperties,
  private val captchaCacheManager: CaptchaCacheAdaptor
) : SmartInitializingSingleton {

  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  private lateinit var kaptcha: DefaultKaptcha
  private lateinit var qaClass: Class<out AbstractCaptchaQa>

  override fun afterSingletonsInstantiated() {
    with(captchaProperties) {
      try {
        Class.forName(this.qa.name)
      } catch (ex: ClassNotFoundException) {
        throw WengerCaptchaException("[验证码] - 找不到指定的QA实现类", ex)
      }
      qaClass = this.qa
    }

    kaptcha = DefaultKaptcha()
    val properties = Properties()
    properties.setProperty("kaptcha.border", "yes")
    properties.setProperty("kaptcha.textproducer.font.names", "微软雅黑")
    kaptcha.config = Config(properties)
  }

  /**
   * 获取QA实现类实例
   *
   * @return [CaptchaQa]
   */
  private fun qa(): AbstractCaptchaQa = try {
    qaClass.newInstance()
  } catch (ex: InstantiationException) {
    throw WengerCaptchaException("[验证码] - 无法实例化QA实现类，请使用mark方法决定如何验证码内容，而不是构造函数", ex)
  } catch (ex: IllegalAccessException) {
    throw WengerCaptchaException("[验证码] - 无法实例化QA实现类，请使用mark方法决定如何验证码内容，而不是构造函数", ex)
  }

  /**
   * 获取验证码问题文本.
   *
   * @param key  key(可使用SessionID，在没有session的情况下，尽量使用一个能唯一标识当前访问者的key)
   * @param seed 验证码生成参数（使用不同的QA实现类，传入相应的参数）
   * @return 验证码问题
   */
  @JvmOverloads
  fun justQuestion(key: String, seed: Any? = null): String {
    val qa = qa().apply { this.make(seed) }

    if (!qa.inspect()) {
      throw WengerCaptchaException("[验证码] - 验证码信息异常 $key：$qa")
    }

    captchaCacheManager.put(key, qa)
    if (logger.isDebugEnabled) {
      logger.debug("[验证码] - 生成 {}：{}", key, qa.question)
    }
    return qa.question!!
  }

  /**
   * 获取验证码图片，内容为问题文本.
   *
   * @param key key,标识验证码作用主体（用户）
   * @return 图片
   */
  fun image(key: String): BufferedImage = kaptcha.createImage(justQuestion(key))

  /**
   * 验证码检查答案是否正确.
   *
   * @param key    key,标识验证码作用主体（用户）
   * @param answer 答案
   * @return boolean
   */
  fun validate(key: String, answer: String): Boolean =
    captchaCacheManager.get(key)?.run {
      val v = this.isRight(answer)
      if (v) {
        captchaCacheManager.remove(key)
      }
      return v
    } ?: false

}

class WengerCaptchaException : WengerRuntimeException {
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