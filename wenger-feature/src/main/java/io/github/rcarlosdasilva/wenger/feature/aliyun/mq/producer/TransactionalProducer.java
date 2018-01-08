package io.github.rcarlosdasilva.wenger.feature.aliyun.mq.producer;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionExecuter;
import com.aliyun.openservices.ons.api.transaction.TransactionProducer;
import io.github.rcarlosdasilva.kits.bean.SerializeHelper;
import io.github.rcarlosdasilva.wenger.feature.aliyun.mq.MqException;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Slf4j
public class TransactionalProducer extends AbstractProducer {

  private TransactionProducer producer;

  public TransactionalProducer(Properties config, LocalTransactionChecker checker) {
    if (checker == null) {
      throw new MqException("[Aliyun:MQ] - 未找到LocalTransactionChecker的Bean");
    }
    this.producer = ONSFactory.createTransactionProducer(config, checker);
    this.producer.start();

    String producerId = config.getProperty(PropertyKeyConst.ProducerId);
    log.info("[Aliyun:MQ] - 注册事务消息生产者(TRANSACTION PRODUCER)：PID: {}", producerId);
  }

  @Override
  public void shutdown() {
    producer.shutdown();
  }

  /**
   * 发送事务消息.
   *
   * @param topic    消息主题
   * @param tag      标签
   * @param key      设置代表消息的业务关键属性，请尽可能全局唯一。 以方便您在无法正常收到消息情况下，可通过阿里云服务器管理控制台查询消息并补发
   *                 注意：不设置也不会影响消息正常收发
   * @param body     消息体
   * @param executer 本地事务执行器
   * @param param    附加参数
   * @return 消息ID
   */
  public String send(String topic, String tag, String key, Object body, LocalTransactionExecuter executer,
                     Object param) {
    return retry(() -> {
      String mark = mark();
      Message message = new Message(topic, tag, key, SerializeHelper.serialize(body));
      log.info("[Aliyun:MQ] - 发送事务消息(TRANSACTION MESSAGE)：Mark: {}, Topic: {}, Tag: {}, Key: {}", mark, topic, tag,
          key);
      SendResult result = producer.send(message, executer, param);
      log.info("[Aliyun:MQ] - 事务消息：Mark: {}, MessageId: {}", mark, message.getMsgID());
      return result.getMessageId();
    });
  }

}
