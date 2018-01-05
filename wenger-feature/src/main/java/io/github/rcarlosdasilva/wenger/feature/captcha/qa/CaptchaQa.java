package io.github.rcarlosdasilva.wenger.feature.captcha.qa;

/**
 * 验证码问题答案对
 * <p>
 * 验证码分为两个部分，问题和答案。问题就是显示给终端用户看到的（提示），答案是用户根据问题，需要输入的正确信息。
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public interface CaptchaQa {

  /**
   * 获取问题
   *
   * @return question
   */
  String getQuestion();

  /**
   * 根据给定的数据创建问题与答案
   *
   * @param seed 数据，可兼容null
   */
  void make(Object seed);

  /**
   * 答案是否正确（默认用户输入要与答案完全相同），可根据验证码不同形式，重写判断
   *
   * @param answer 用户输入答案
   * @return boolean
   */
  boolean isRight(String answer);

}
