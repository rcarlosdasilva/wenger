package io.github.rcarlosdasilva.wenger.feature.aliyun.green.term;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public enum ImageScene {

  /**
   * 鉴黄
   */
  PORN,
  /**
   * 暴恐涉政识别
   */
  TERRORISM,
  /**
   * OCR识别
   */
  OCR,
  /**
   * 人脸识别
   */
  SFACE,
  /**
   * 广告识别
   */
  AD,
  /**
   * 二维码识别
   */
  QRCODE,
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
