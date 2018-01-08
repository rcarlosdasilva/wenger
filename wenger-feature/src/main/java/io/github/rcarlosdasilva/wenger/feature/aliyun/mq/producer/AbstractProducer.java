package io.github.rcarlosdasilva.wenger.feature.aliyun.mq.producer;

import io.github.rcarlosdasilva.kits.string.Characters;
import io.github.rcarlosdasilva.kits.string.TextHelper;
import io.github.rcarlosdasilva.wenger.feature.aliyun.mq.MqException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Slf4j
public abstract class AbstractProducer {

  private static final int DEFAULT_RETRIES = 3;

  /**
   * 关闭MQ
   */
  abstract void shutdown();

  /**
   * 重试
   *
   * @param executive 执行代码
   * @return Message id
   */
  protected String retry(Executive executive) {
    int i = 0;
    Exception temEx = null;
    while (i < DEFAULT_RETRIES) {
      try {
        return executive.exe();
      } catch (Exception ex) {
        log.error("[Aliyun:MQ] - 发送消息失败", ex);
        temEx = ex;

        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          log.error("[Aliyun:MQ] - ", e);
        }
        i++;
      }
    }
    throw new MqException(temEx);
  }

  protected String mark() {
    return TextHelper.random(5, Characters.NUMBERS_AND_LETTERS);
  }

  interface Executive {

    /**
     * 可重试执行代码
     *
     * @return Message id
     */
    String exe();

  }

}
