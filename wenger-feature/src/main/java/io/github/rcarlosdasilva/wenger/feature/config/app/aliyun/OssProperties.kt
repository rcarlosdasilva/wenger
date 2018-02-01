package io.github.rcarlosdasilva.wenger.feature.config.app.aliyun

import io.github.rcarlosdasilva.wenger.feature.config.AbleProperties

/**
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
class OssProperties : AbleProperties() {

  /**
   * OSS地址，默认外网地址
   */
  var address = "https://oss-cn-shanghai.aliyuncs.com"
  /**
   * OSS Bucket
   */
  var bucket: String? = null
  /**
   * 默认OSS文件缓存时间（Cache-Control: max-age），单位：秒，默认2,592,000秒（30天）
   */
  var maxAge = 2592000L

}
