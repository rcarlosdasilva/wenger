package io.github.rcarlosdasilva.wenger.feature.aliyun.mq.consumer;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public enum ConsumeResult {

  /**
   * 消费成功，继续消费下一条消息
   */
  SUCCESS,
  /**
   * 普通消息时：消费失败，告知服务器稍后再投递这条消息，继续消费其他消息<br>
   * 顺序消息时：消费失败，挂起当前队列
   */
  FAILED

}
