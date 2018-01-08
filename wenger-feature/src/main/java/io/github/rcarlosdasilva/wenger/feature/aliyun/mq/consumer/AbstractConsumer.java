package io.github.rcarlosdasilva.wenger.feature.aliyun.mq.consumer;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderAction;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息监听器包装类，可实现自动订阅配置
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Slf4j
public abstract class AbstractConsumer {

  /**
   * 是否为集群订阅模式（默认true）.
   *
   * @return true
   */
  public boolean isClusteringSuscribe() {
    return true;
  }

  /**
   * 是否订阅顺序消息（默认false）.
   *
   * @return false
   */
  public boolean isOrdered() {
    return false;
  }

  /**
   * 设置每条消息消费的最大超时时间，超过设置时间则被视为消费失败，等下次重新投递再次消费。每个业务需要设置一个合理的值，单位（分钟）。默认：15.
   *
   * @return timeout
   */
  public int timeout() {
    return 15;
  }

  /**
   * 消费端线程数，默认32.
   *
   * @return threads
   */
  public int threads() {
    return 32;
  }

  /**
   * 消息主题
   *
   * @return topic
   */
  public abstract String topic();

  /**
   * 监听的CID - ConsumerID
   * <p>
   * <b>为规范CID的命名，只需填写ConsumerID所属的业务标识。无需关心大小写，驼峰式命名会自动转换为下划线分隔形式。例如：paperStaticize，会自动生成
   * MQ_CID_PREFIX_PAPER_STATICIZE</b>
   *
   * @return CID
   */
  public abstract String consumerId();

  /**
   * 需要监听的tag.
   * <p>
   * 无需关心前缀，tag的命名应尽量简短，不区分大小写！！可返回多个需要监听的tag，不要包含特殊字符，例如||
   *
   * @return tags
   */
  public abstract String[] tags();

  /**
   * 消费消息.
   *
   * @param message 消息
   * @param ctxn    普通消息上下文（顺序消息消费监听下为null）
   * @param ctxo    顺序消息上下文（普通消息消费监听下为null）
   * @return {@link ConsumeResult}
   */
  abstract ConsumeResult consume(final Message message, final ConsumeContext ctxn,
                                 final ConsumeOrderContext ctxo);

  public MessageListener normalListener() {
    return (Message message, ConsumeContext context) -> {
      log.info("[Aliyun:MQ] - 新的消息：MSGID: {}, KEY: {}", message.getMsgID(), message.getKey());
      long startAt = System.nanoTime();
      ConsumeResult result = consume(message, context, null);
      log.info("[Aliyun:MQ] - 消息消费结束：MSGID: {}, 用时：{}ns", message.getMsgID(), System.nanoTime() - startAt);
      return result == ConsumeResult.SUCCESS ? Action.CommitMessage : Action.ReconsumeLater;
    };
  }

  public MessageOrderListener orderedListener() {
    return (Message message, ConsumeOrderContext context) -> {
      log.info("[Aliyun:MQ] - 新的顺序消息： MSGID: {}, KEY: {}", message.getMsgID(), message.getKey());
      long startAt = System.nanoTime();
      ConsumeResult result = consume(message, null, context);
      log.info("[Aliyun:MQ] - 顺序消息消费结束： MSGID: {}, 用时：{}ns", message.getMsgID(), System.nanoTime() - startAt);
      return result == ConsumeResult.SUCCESS ? OrderAction.Success : OrderAction.Suspend;
    };
  }

}
