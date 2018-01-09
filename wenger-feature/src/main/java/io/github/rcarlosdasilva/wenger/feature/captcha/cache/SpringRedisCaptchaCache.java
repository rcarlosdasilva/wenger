package io.github.rcarlosdasilva.wenger.feature.captcha.cache;

import com.google.common.base.Preconditions;
import io.github.rcarlosdasilva.kits.string.TextHelper;
import io.github.rcarlosdasilva.wenger.common.constant.RedisConstant;
import io.github.rcarlosdasilva.wenger.feature.captcha.qa.CaptchaQa;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * 使用Spring中配置好的Redis缓存，可支持集群
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public class SpringRedisCaptchaCache implements CaptchaCache {

  @SuppressWarnings("rawtypes")
  private final RedisTemplate redisTemplate;
  private final String redisKeyPrefix;
  private final int livetime;

  @SuppressWarnings("rawtypes")
  public SpringRedisCaptchaCache(RedisTemplate redisTemplate, String redisKeyPrefix, int livetime) {
    this.redisTemplate = redisTemplate;
    this.redisKeyPrefix = redisKeyPrefix;
    this.livetime = livetime;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void put(String key, CaptchaQa qa) {
    redisTemplate.opsForValue().set(key(key), qa, livetime, TimeUnit.MINUTES);
  }

  @Override
  public CaptchaQa get(String key) {
    return (CaptchaQa) redisTemplate.opsForValue().get(key(key));
  }

  @SuppressWarnings("unchecked")
  @Override
  public void remove(String key) {
    redisTemplate.delete(key(key));
  }

  private String key(String mark) {
    Preconditions.checkNotNull(mark);

    mark = TextHelper.trim(mark, RedisConstant.DEFAULT_KEY_SEPARATOR);
    return TextHelper.concat(RedisConstant.DEFAULT_KEY_SEPARATOR, redisKeyPrefix, mark);
  }

}
