package io.github.rcarlosdasilva.wenger.feature.config.app.aliyun

import io.github.rcarlosdasilva.wenger.feature.config.AbleProperties
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@ConfigurationProperties(prefix = "app.aliyun.green")
class GreenProperties : AbleProperties() {

  /**
   * 内容安全Region，暂时只支持上海
   */
  var region = "cn-shanghai"
  /**
   * 内容安全地址
   */
  var address = "green.cn-shanghai.aliyuncs.com"
  /**
   * 使用异步检测（适用于图片），不设置但使用异步检测的话，会获取不到结果，默认false
   */
  var useAsync = false
  /**
   * 异步检测间隔时间（异步检测请求发送后间隔多久发送结果查询请求），单位：秒
   */
  var asyncInterval = 30L

}
