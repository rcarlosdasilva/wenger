package io.github.rcarlosdasilva.wenger.feature.aliyun.green.term;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public enum TextScene {

  /**
   * 反垃圾检测
   */
  ANTISPAM,
  /**
   * 关键词检测
   */
  KEYWORD;

  @Override
  public String toString() {
    return super.toString().toLowerCase();
  }
}
