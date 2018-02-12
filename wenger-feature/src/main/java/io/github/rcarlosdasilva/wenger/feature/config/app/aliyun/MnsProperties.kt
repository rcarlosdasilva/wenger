package io.github.rcarlosdasilva.wenger.feature.config.app.aliyun

import io.github.rcarlosdasilva.wenger.feature.aliyun.mns.AliyunMnsQueueReceiver
import io.github.rcarlosdasilva.wenger.feature.config.AbleProperties
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@ConfigurationProperties(prefix = "app.aliyun.mns")
class MnsProperties : AbleProperties() {

  /**
   * MNS访问域名，暂时并建议使用单一地域的服务
   *
   * 进入阿里云MNS管理控制台，选取需要访问的地域，点击右上角的“获取Endpoint”按钮即可得到该地域的内外网访问域名。
   */
  var endpoint: String = ""
  /**
   * 已创建的队列配置
   */
  var queues: List<QueueProperties>? = null
  /**
   * 已创建的主题名称
   *
   * 配置的主题，将用来发布消息。不提供订阅相关功能，订阅的配置请在阿里云控制台操作
   */
  var topics: List<String>? = null

  /**
   * @param name 队列名称
   * @param receive 在当前服务中是否需要接收该队列消息。为true时，项目启动会开启一个线程单独对该队列长轮询获取消息，实现[AliyunMnsQueueReceiver]接口，消费消息
   * @param queueReceiveSize 队列消息消费时，一次接收条数（批量），默认10。默认每个队列开启一个线程，一次批量获取多条消息，暂不支持多线程对同一队列进行长轮询拉取消息
   * @param pollingTimeout receive message请求最长等待时间，单位为秒，默认30秒。
   */
  data class QueueProperties(
      val name: String,
      val receive: Boolean = false,
      val queueReceiveSize: Int = 10,
      val pollingTimeout: Int = 30
  )

}