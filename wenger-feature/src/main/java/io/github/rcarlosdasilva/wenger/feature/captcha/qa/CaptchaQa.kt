package io.github.rcarlosdasilva.wenger.feature.captcha.qa

import com.google.common.base.MoreObjects
import com.google.common.base.Strings
import mu.KotlinLogging

/**
 * 验证码问题答案对
 *
 *
 * 验证码分为两个部分，问题和答案。问题就是显示给终端用户看到的（提示），答案是用户根据问题，需要输入的正确信息。
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
interface CaptchaQa {

  /**
   * 根据给定的数据创建问题与答案
   *
   * @param seed 数据，可兼容null
   */
  fun make(seed: Any?)

  /**
   * 答案是否正确（默认用户输入要与答案完全相同），可根据验证码不同形式，重写判断
   *
   * @param answer 用户输入答案
   * @return boolean
   */
  fun isRight(answer: String): Boolean

}

/**
 * 验证码通用实现
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
abstract class AbstractCaptchaQa protected constructor() : CaptchaQa {

  private val logger = KotlinLogging.logger {}

  var question: String? = null
  protected var answer: String? = null

  override fun make(seed: Any?) {
    make(seed)

    logger.debug { "[验证码] - 普通文本验证码：Q - $question, A - $answer" }
  }

  /**
   * 答案是否正确（默认用户输入要与答案完全相同），可根据验证码不同形式，重写判断
   *
   * @param answer 用户输入答案
   * @return boolean
   */
  override fun isRight(answer: String): Boolean = answer == this.answer

  /**
   * 检查验证码信息是否合法（比较复杂的验证码生成方式可用）
   *
   * 特殊情况下，假如使用逻辑比较复杂的方式，生成的验证码问题与答案，在开发者无法100%保证信息有效可用，可以重写该方法进行检查，失败可重新生成，默认实现仅检查不为空
   *
   * @return boolean
   */
  open fun inspect(): Boolean = !Strings.isNullOrEmpty(this.question) && !Strings.isNullOrEmpty(this.answer)

  override fun toString(): String =
    MoreObjects.toStringHelper(this).add("question", this.question).add("answer", this.answer).toString()

}