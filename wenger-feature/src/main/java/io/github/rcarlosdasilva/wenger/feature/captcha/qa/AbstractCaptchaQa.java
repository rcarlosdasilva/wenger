package io.github.rcarlosdasilva.wenger.feature.captcha.qa;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractCaptchaQa implements CaptchaQa {

  protected static final String INVALID_VALUE = "";

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private String question;
  @Getter(AccessLevel.PROTECTED)
  @Setter(AccessLevel.PROTECTED)
  private String answer;

  protected AbstractCaptchaQa() {
  }

  /**
   * 答案是否正确（默认用户输入要与答案完全相同），可根据验证码不同形式，重写判断
   * <p>判断时，可借助{@link #isValid}和{@link #ignoreInvalidQa()}方法（非必须）</p>
   *
   * @param answer 用户输入答案
   * @return boolean
   */
  @Override
  public boolean isRight(String answer) {
    return !Strings.isNullOrEmpty(answer) && answer.equals(this.answer);
  }

  /**
   * 问题答案对信息是否靠谱，主要为了验证问题/答案对是否有效
   * <p>
   * 在问题/答案长度过小、或其他因素导致无法生成有效验证码时不靠谱，通常不靠谱的原因可能是用户或三方等外部因素导致。
   * 在不靠谱的情况下，验证码是否正确，取决于 {@linkplain #ignoreInvalidQa()}方法
   *
   * @return boolean
   */
  public boolean isValid() {
    return !Strings.isNullOrEmpty(question) || !Strings.isNullOrEmpty(answer);
  }

  /**
   * 如果验证码不靠谱，应该反馈用户的输入是否为正确？（默认永远不正确）
   * <p>
   * 应该认为在正确的配置或外部数据，验证码永远靠谱，如果不靠谱则无法信任用户的输入，所以返回false。如有其它考虑，可重写。
   *
   * @return boolean
   */
  public boolean ignoreInvalidQa() {
    return false;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("question", question).add("answer", answer)
        .toString();
  }

}
