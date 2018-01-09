package io.github.rcarlosdasilva.wenger.feature.aliyun.mq.producer;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.order.OrderProducer;
import io.github.rcarlosdasilva.kits.bean.SerializeHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Slf4j
public class OrderedProducer extends AbstractProducer {

  private static final String DEFAULT_GLOBAL_REGION = "GLOBAL";

  private final OrderProducer producer;

  public OrderedProducer(Properties config) {
    this.producer = ONSFactory.createOrderProducer(config);
    this.producer.start();

    String producerId = config.getProperty(PropertyKeyConst.ProducerId);
    log.info("[Aliyun:MQ] - 注册有序消息生产者(ORDERED PRODUCER)：PID: {}", producerId);
  }

  @Override
  public void shutdown() {
    producer.shutdown();
  }

  /**
   * 发送顺序消息，使用分区顺序消息.
   *
   * @param topic  消息主题
   * @param region 分区顺序消息中区分不同分区的关键字段
   * @param tag    标签
   * @param key    设置代表消息的业务关键属性，请尽可能全局唯一。 以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发
   *               注意：不设置也不会影响消息正常收发
   * @param body   消息体
   * @return 消息ID
   */
  public String send(String topic, String region, String tag, String key, Object body) {
    return retry(() -> {
      String mark = mark();
      Message message = new Message(topic, tag, key, SerializeHelper.serialize(body));
      log.info("[Aliyun:MQ] - 发送顺序消息(ORDERED MESSAGE)：Mark: {}, Topic: {}, Region: {}, Tag: {}, Key: {}", mark, topic,
          region, tag, key);
      SendResult result = producer.send(message, region);
      log.info("[Aliyun:MQ] - 同步消息：Mark: {}, MessageId: {}", mark, message.getMsgID());
      return result.getMessageId();
    });
  }

  /**
   * 发送顺序消息，使用全局顺序消息.
   *
   * @param topic 消息主题
   * @param tag   标签
   * @param key   设置代表消息的业务关键属性，请尽可能全局唯一。 以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发
   *              注意：不设置也不会影响消息正常收发
   * @param body  消息体
   * @return 消息ID
   */
  public String send(String topic, String tag, String key, Object body) {
    return send(topic, DEFAULT_GLOBAL_REGION, tag, key, body);
  }

}
