package io.github.rcarlosdasilva.wenger.feature.aliyun.mq;

import com.aliyun.openservices.ons.api.*;
import com.aliyun.openservices.ons.api.order.MessageOrderListener;
import com.aliyun.openservices.ons.api.order.OrderConsumer;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.rcarlosdasilva.kits.string.TextHelper;
import io.github.rcarlosdasilva.wenger.feature.aliyun.mq.consumer.AbstractConsumer;
import io.github.rcarlosdasilva.wenger.feature.aliyun.mq.producer.NormalProducer;
import io.github.rcarlosdasilva.wenger.feature.aliyun.mq.producer.OrderedProducer;
import io.github.rcarlosdasilva.wenger.feature.aliyun.mq.producer.TransactionalProducer;
import io.github.rcarlosdasilva.wenger.feature.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Slf4j
@ConditionalOnProperty(name = "app.aliyun.mq.enable", havingValue = "true")
@Component
@EnableConfigurationProperties({AppProperties.class})
public class MqHandler implements SmartInitializingSingleton, DisposableBean {

  private static final int DEFAULT_ORDERED_MESSAGE_SUSPEND_TIME_MILLIS = 500;
  private static final int DEFAULT_MAX_RECONSUMER_TIMES = 5;

  @Autowired
  private AppProperties appProperties;
  @Autowired(required = false)
  private List<AbstractConsumer> consumers;
  @Autowired(required = false)
  private LocalTransactionChecker localTransactionChecker;
  private Map<String, NormalProducer> normalProducers = Maps.newHashMap();
  private Map<String, OrderedProducer> orderedProducers = Maps.newHashMap();
  private Map<String, TransactionalProducer> transactionalProducers = Maps.newHashMap();
  private List<Consumer> normalConsumers = Lists.newArrayList();
  private List<OrderConsumer> orderedConsumers = Lists.newArrayList();

  @Override
  public void afterSingletonsInstantiated() {
    Map<String, MqProducerType> producers = appProperties.getAliyun().getMq().getProducers();
    if (producers == null || producers.isEmpty()) {
      log.warn("[Aliyun:MQ] - 未配置消息队列生产者");
    } else {
      for (Map.Entry<String, MqProducerType> entry : producers.entrySet()) {
        String producerId = entry.getKey();
        Properties config = producerConfig(producerId);
        switch (entry.getValue()) {
          case NORMAL:
            normalProducers.put(producerId, new NormalProducer(config));
            break;
          case ORDERED:
            orderedProducers.put(producerId, new OrderedProducer(config));
            break;
          case TRANSACTIONAL:
            transactionalProducers.put(producerId, new TransactionalProducer(config, localTransactionChecker));
            break;
          default:
        }
      }
    }

    if (consumers == null || consumers.isEmpty()) {
      log.warn("[Aliyun:MQ] - 未配置消息队列消费者");
    } else {
      for (AbstractConsumer consumer : consumers) {
        if (consumer.isOrdered()) {
          subscribeOrdered(consumer.topic(), consumer.consumerId(), TextHelper.join("||", consumer.tags()),
              consumer.orderedListener(), consumer.threads(), consumer.timeout());
        } else {
          subscribeNormal(consumer.topic(), consumer.consumerId(), TextHelper.join("||", consumer.tags()),
              consumer.normalListener(), consumer.threads(), consumer.timeout());
        }
      }
    }
  }

  @Override
  public void destroy() {
    normalProducers.forEach((key, producer) -> producer.shutdown());
    orderedProducers.forEach((key, producer) -> producer.shutdown());
    transactionalProducers.forEach((key, producer) -> producer.shutdown());

    normalConsumers.forEach(Consumer::shutdown);
    orderedConsumers.forEach(OrderConsumer::shutdown);
  }

  public NormalProducer normal(String producerId) {
    return normalProducers.get(producerId);
  }

  public OrderedProducer ordered(String producerId) {
    return orderedProducers.get(producerId);
  }

  public TransactionalProducer transactional(String producerId) {
    return transactionalProducers.get(producerId);
  }

  private void subscribeNormal(String topic, String consumerId, String tags, MessageListener listener, int threads,
                               int timeout) {
    Properties config = consumerConfig(consumerId, threads, timeout);
    Consumer consumer = ONSFactory.createConsumer(config);
    consumer.subscribe(topic, tags, listener);
    consumer.start();
    normalConsumers.add(consumer);
    log.info("[Aliyun:MQ] - 注册消息消费者(NORMAL CONSUMER)：Topic: {}, CID: {}, Tags: {}", topic, consumerId, tags);
  }

  private void subscribeOrdered(String topic, String consumerId, String tags, MessageOrderListener listener,
                                int threads, int timeout) {
    Properties config = consumerConfig(consumerId, threads, timeout);
    OrderConsumer consumer = ONSFactory.createOrderedConsumer(config);
    consumer.subscribe(topic, tags, listener);
    consumer.start();
    orderedConsumers.add(consumer);
    log.info("[Aliyun:MQ] - 注册顺序消息消费者(ORDERED CONSUMER)：Topic: {}, CID: {}, Tags: {}", topic,
        consumerId, tags);
  }

  private Properties producerConfig(String producerId) {
    Properties properties = new Properties();

    properties.put(PropertyKeyConst.AccessKey, appProperties.getAliyun().getAccessId());
    properties.put(PropertyKeyConst.SecretKey, appProperties.getAliyun().getAccessSecret());
    properties.put(PropertyKeyConst.ONSAddr,
        appProperties.getAliyun().getMq().getAddress());
    properties.put(PropertyKeyConst.ProducerId, producerId);
    // 设置发送超时时间，单位毫秒
    properties.put(PropertyKeyConst.SendMsgTimeoutMillis, appProperties.getAliyun().getMq().getSendTimeout());
    return properties;
  }

  private Properties consumerConfig(String consumerId, int threads, int timeout) {
    Properties properties = new Properties();

    properties.put(PropertyKeyConst.AccessKey, appProperties.getAliyun().getAccessId());
    properties.put(PropertyKeyConst.SecretKey, appProperties.getAliyun().getAccessSecret());
    properties.put(PropertyKeyConst.ONSAddr,
        appProperties.getAliyun().getMq().getAddress());
    properties.put(PropertyKeyConst.ConsumerId, consumerId);
    properties.put(PropertyKeyConst.ConsumeThreadNums, threads);
    properties.put(PropertyKeyConst.ConsumeTimeout, timeout);
    // 顺序消息消费失败进行重试前的等待时间，单位(毫秒)
    properties.put(PropertyKeyConst.SuspendTimeMillis, DEFAULT_ORDERED_MESSAGE_SUSPEND_TIME_MILLIS);
    // 消息消费失败时的最大重试次数
    properties.put(PropertyKeyConst.MaxReconsumeTimes, DEFAULT_MAX_RECONSUMER_TIMES);
    return properties;
  }

}
