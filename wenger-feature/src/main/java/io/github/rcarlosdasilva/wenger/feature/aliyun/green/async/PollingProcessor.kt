package io.github.rcarlosdasilva.wenger.feature.aliyun.green.async

import io.github.rcarlosdasilva.wenger.feature.aliyun.green.AliyunGreenHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 异步检测结果轮询处理器
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
class PollingProcessor(private val handler: AliyunGreenHandler) {

  private val logger: Logger = LoggerFactory.getLogger(javaClass)

  private var sleepTime = DEFAULT_SLEEP_TIME

  fun start() {
    while (!Thread.currentThread().isInterrupted) {
      request()

      try {
        Thread.sleep(sleepTime)
      } catch (ex: InterruptedException) {
        logger.error("[Aliyun:GREEN] - ", ex)
        Thread.currentThread().interrupt()
      }
    }
  }

  private fun request() {
    val images: MutableList<String> = mutableListOf()
    val videos: MutableList<String> = mutableListOf()

    // 一次最多请求20个，为了缩短请求时间，尽快获取到结果
    for (i in 1..DEFAULT_TASK_NUMBER_ONCE) {
      val t = AsyncContentHolder.task() ?: break

      if (t.type === AsyncContentHolder.TaskType.IMAGE) {
        images.add(t.taskId)
      } else {
        videos.add(t.taskId)
      }
    }

    if (images.isEmpty() && videos.isEmpty()) {
      if (sleepTime < MAX_SLEEP_TIME) {
        sleepTime += SLEEP_ASCENDING_STEP
      }
    } else {
      sleepTime = DEFAULT_SLEEP_TIME

      AsyncContentHolder.putImageResults(handler.imageAsyncResults(images))
      AsyncContentHolder.putVideoResults(handler.videoAsyncResults(videos))
    }
  }

  companion object {
    private const val DEFAULT_SLEEP_TIME = 5L
    private const val MAX_SLEEP_TIME = 1005L
    private const val SLEEP_ASCENDING_STEP = 100L
    private const val DEFAULT_TASK_NUMBER_ONCE = 20
  }

}
