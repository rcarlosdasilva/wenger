package io.github.rcarlosdasilva.wenger.feature.aliyun.mq.producer;

import com.aliyun.openservices.ons.api.*;
import io.github.rcarlosdasilva.kits.bean.SerializeHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Slf4j
public class NormalProducer extends AbstractProducer {

  private final Producer producer;

  public NormalProducer(Properties config) {
    this.producer = ONSFactory.createProducer(config);
    this.producer.start();

    String producerId = config.getProperty(PropertyKeyConst.ProducerId);
    log.info("[Aliyun:MQ] - 注册无序消息生产者(NORMAL PRODUCER)：PID: {}", producerId);
  }

  @Override
  public void shutdown() {
    producer.shutdown();
  }

  /**
   * 发送普通同步消息.
   *
   * @param topic 消息主题
   * @param tag   标签
   * @param key   设置代表消息的业务关键属性，请尽可能全局唯一。 以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发
   *              注意：不设置也不会影响消息正常收发
   * @param body  消息体
   * @return 消息ID
   */
  public String sendSync(String topic, String tag, String key, Object body) {
    return retry(() -> {
      String mark = mark();
      Message message = new Message(topic, tag, key, SerializeHelper.serialize(body));
      log.info("[Aliyun:MQ] - 发送同步消息(SYNC MESSAGE)：Mark: {}, Topic: {}, Tag: {}, Key: {}", mark, topic, tag, key);
      SendResult result = producer.send(message);
      log.info("[Aliyun:MQ] - 同步消息：Mark: {}, MessageId: {}", mark, message.getMsgID());
      return result.getMessageId();
    });
  }

  /**
   * 发送普通异步消息.
   *
   * @param topic    消息主题
   * @param tag      标签
   * @param key      设置代表消息的业务关键属性，请尽可能全局唯一。 以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发
   *                 注意：不设置也不会影响消息正常收发
   * @param body     消息体
   * @param callback {@link SendCallback}
   * @return 消息ID
   */
  public String sendAsync(String topic, String tag, String key, Object body, SendCallback callback) {
    return retry(() -> {
      Message message = new Message(topic, tag, key, SerializeHelper.serialize(body));
      log.info("[Aliyun:MQ] - 发送异步消息(ASYNC MESSAGE)：Topic: {}, Tag: {}, Key: {}", topic, tag, key);
      producer.sendAsync(message, callback);
      return message.getMsgID();
    });
  }

  /**
   * 发送普通异步消息.
   *
   * @param topic 消息主题
   * @param tag   标签
   * @param key   设置代表消息的业务关键属性，请尽可能全局唯一。 以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发
   *              注意：不设置也不会影响消息正常收发
   * @param body  消息体
   * @return 消息ID
   */
  public String sendAsync(String topic, String tag, String key, Object body) {
    return sendAsync(topic, tag, key, body, null);
  }

  /**
   * 发送普通定时消息.
   *
   * @param topic 消息主题
   * @param tag   标签
   * @param key   设置代表消息的业务关键属性，请尽可能全局唯一。 以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发
   *              注意：不设置也不会影响消息正常收发
   * @param body  消息体
   * @param time  发送时间，时间戳
   * @return 消息ID
   */
  public String sendTiming(String topic, String tag, String key, Object body, long time) {
    return retry(() -> {
      String mark = mark();
      Message message = new Message(topic, tag, key, SerializeHelper.serialize(body));
      message.setStartDeliverTime(time);
      log.info("[Aliyun:MQ] - 发送定时消息(TIMING MESSAGE)：Mark: {}, Topic: {}, Tag: {}, Key: {}, Timing: {}", mark, topic,
          tag, key, time);
      SendResult result = producer.send(message);
      log.info("[Aliyun:MQ] - 定时消息：Mark: {}, MessageId: {}", mark, message.getMsgID());
      return result.getMessageId();
    });
  }

  /**
   * 发送普通延时消息.
   *
   * @param topic 消息主题
   * @param tag   标签
   * @param key   设置代表消息的业务关键属性，请尽可能全局唯一。 以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发
   *              注意：不设置也不会影响消息正常收发
   * @param body  消息体
   * @param time  延时，单位毫秒
   * @return 消息ID
   */
  public String sendDelay(String topic, String tag, String key, Object body, long time) {
    return retry(() -> {
      String mark = mark();
      Message message = new Message(topic, tag, key, SerializeHelper.serialize(body));
      message.setStartDeliverTime(System.currentTimeMillis() + time);
      log.info("[Aliyun:MQ] - 发送延时消息(TIMING MESSAGE)：Mark: {}, Topic: {}, Tag: {}, Key: {}, Timing: {}", mark, topic,
          tag,
          key, time);
      SendResult result = producer.send(message);
      log.info("[Aliyun:MQ] - 延时消息：Mark: {}, MessageId: {}", mark, message.getMsgID());
      return result.getMessageId();
    });
  }

  /**
   * 发送普通单向消息.
   *
   * @param topic 消息主题
   * @param tag   标签
   * @param key   设置代表消息的业务关键属性，请尽可能全局唯一。 以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发
   *              注意：不设置也不会影响消息正常收发
   * @param body  消息体
   * @return 消息ID
   */
  public String sendOneway(String topic, String tag, String key, Object body) {
    return retry(() -> {
      String mark = mark();
      Message message = new Message(topic, tag, key, SerializeHelper.serialize(body));
      log.info("[Aliyun:MQ] - 发送单向消息(ONEWAY MESSAGE)：Mark: {}, Topic: {}, Tag: {}, Key: {}", mark, topic, tag, key);
      producer.sendOneway(message);
      log.info("[Aliyun:MQ] - 单向消息：Mark: {}, MessageId: {}", mark, message.getMsgID());
      return message.getMsgID();
    });
  }

}
