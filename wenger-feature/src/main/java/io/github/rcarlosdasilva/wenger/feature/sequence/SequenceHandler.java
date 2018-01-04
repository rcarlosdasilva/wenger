package io.github.rcarlosdasilva.wenger.feature.sequence;

import io.github.rcarlosdasilva.wenger.feature.config.AppProperties;
import io.github.rcarlosdasilva.wenger.feature.config.app.misc.SequenceProperties;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 获取Snowflake算法唯一ID
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@ConditionalOnProperty(name = "app.misc.sequence.enable", havingValue = "true")
@Component
@EnableConfigurationProperties({AppProperties.class})
public class SequenceHandler implements SmartInitializingSingleton {

  @Autowired
  private AppProperties appProperties;
  private SnowFlakeSequence sequence;

  @Override
  public void afterSingletonsInstantiated() {
    SequenceProperties properties = appProperties.getMisc().getSequence();

    sequence = new SnowFlakeSequence(properties.getWorkerId(), properties.getDataCenterId());
  }

  /**
   * 获取id
   *
   * @return id
   */
  public long id() {
    return sequence.nextId();
  }

}
