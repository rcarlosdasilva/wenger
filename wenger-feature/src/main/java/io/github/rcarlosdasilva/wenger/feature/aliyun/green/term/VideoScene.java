package io.github.rcarlosdasilva.wenger.feature.aliyun.green.term;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public enum VideoScene {

  /**
   * 鉴黄
   */
  PORN,
  /**
   * 暴恐涉政
   */
  TERRORISM,
  /**
   * 不良场景
   */
  LIVE,
  /**
   * logo
   */
  LOGO;

  @Override
  public String toString() {
    return super.toString().toLowerCase();
  }

}
