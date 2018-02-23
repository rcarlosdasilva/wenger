package io.github.rcarlosdasilva.wenger.feature.aliyun.green

enum class Suggestion {
  /**
   * 通过
   */
  PASS,
  /**
   * 需要人工审核
   */
  REVIEW,
  /**
   * 违规，可以直接删除或者做限制处理
   */
  BLOCK
}

enum class TextScene {
  /**
   * 反垃圾检测
   */
  ANTISPAM,
  /**
   * 关键词检测
   */
  KEYWORD;

  override fun toString(): String = super.toString().toLowerCase()
}

enum class TextLable {
  /**
   * 正常文本
   */
  NORMAL,
  /**
   * 含垃圾信息
   */
  SPAM,
  /**
   * 广告
   */
  AD,
  /**
   * 渉政
   */
  POLITICS,
  /**
   * 暴恐
   */
  TERRORISM,
  /**
   * 辱骂
   */
  ABUSE,
  /**
   * 色情
   */
  PORN,
  /**
   * 灌水
   */
  FLOOD,
  /**
   * 违禁
   */
  CONTRABAND,
  /**
   * 自定义(比如命中自定义关键词)
   */
  CUSTOMIZED
}


enum class ImageScene {
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

  override fun toString(): String = super.toString().toLowerCase()
}

enum class ImageLabel {
  /**
   * 通用，正常
   */
  NORMAL,
  /**
   * 鉴黄，性感图片
   */
  SEXY,
  /**
   * 鉴黄，色情图片
   */
  PORN,
  /**
   * 暴恐涉政，含暴恐图片
   */
  TERRORISM,
  /**
   * 暴恐涉政，特殊装束
   */
  OUTFIT,
  /**
   * 暴恐涉政，特殊标识
   */
  LOGO,
  /**
   * 暴恐涉政，武器
   */
  WEAPON,
  /**
   * 暴恐涉政，渉政
   */
  POLITICS,
  /**
   * 暴恐涉政，其它暴恐渉政
   */
  OTHERS,
  /**
   * ORC，含文字图片
   */
  ORC,
  /**
   * 人脸识别，	含人脸图片
   */
  SFACE,
  /**
   * 广告，含广告
   */
  AD,
  /**
   * 二维码，含二维码的图片
   */
  QRCODE,
  /**
   * 带有管控logo的图片
   */
  TV
}

enum class VideoScene {
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

  override fun toString(): String = super.toString().toLowerCase()
}

enum class VideoLable {
  /**
   * 通用，正常
   */
  NORMAL,
  /**
   * 色情视频
   */
  PORN,
  /**
   * 暴恐涉政视频
   */
  TERRORISM,
  /**
   * 不良场景视频
   */
  LIVE,
  /**
   * 带有logo视频
   */
  LOGO
}