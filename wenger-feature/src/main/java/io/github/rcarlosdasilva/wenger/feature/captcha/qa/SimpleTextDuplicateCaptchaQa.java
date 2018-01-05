package io.github.rcarlosdasilva.wenger.feature.captcha.qa;

import com.google.common.base.Strings;
import io.github.rcarlosdasilva.kits.string.Characters;
import io.github.rcarlosdasilva.kits.string.TextHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * 普通文本（副本）验证码
 * <p>
 * 问题与答案相同，可不区分大小写，最常见最普通的验证码形式
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Slf4j
public class SimpleTextDuplicateCaptchaQa extends AbstractCaptchaQa {

  private static final int DEFAULT_SIZE = 4;

  /**
   * @param seed 验证码长度，最好4位及以上
   */
  @Override
  public void make(Object seed) {
    int size = seed == null ? DEFAULT_SIZE : Integer.valueOf(seed.toString());

    if (size <= 0) {
      log.error("[验证码] - 长度为负数，验证码无效");
      setQuestion(INVALID_VALUE);
      setAnswer(INVALID_VALUE);
      return;
    } else if (size < DEFAULT_SIZE) {
      log.warn("[验证码] - 简单文本（数字字母）验证码的长度过小：{}", seed);
    }

    String question = TextHelper.random(size, Characters.NUMBERS_AND_LETTERS_UPPER_CASE);
    setQuestion(question);
    setAnswer(question);
  }

  @Override
  public boolean isRight(String answer) {
    return !Strings.isNullOrEmpty(answer) && answer.equalsIgnoreCase(getAnswer());
  }

}
