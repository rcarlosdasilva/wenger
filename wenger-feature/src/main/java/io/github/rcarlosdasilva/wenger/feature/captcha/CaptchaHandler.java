package io.github.rcarlosdasilva.wenger.feature.captcha;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import io.github.rcarlosdasilva.wenger.feature.captcha.qa.CaptchaQa;
import io.github.rcarlosdasilva.wenger.feature.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.util.Properties;

/**
 * 验证码处理器
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Slf4j
@ConditionalOnProperty(name = "app.misc.captcha.enable", havingValue = "true")
@ConditionalOnBean({CaptchaCacheManager.class})
@Component
@EnableConfigurationProperties({AppProperties.class})
public class CaptchaHandler implements SmartInitializingSingleton {

  @Autowired
  private AppProperties appProperties;
  @Autowired
  private CaptchaCacheManager captchaCacheManager;
  private DefaultKaptcha kaptcha;
  private Class<? extends CaptchaQa> qaClass;

  /**
   * ("kaptcha.border", "yes"); <br>
   * ("kaptcha.border.color", "BLACK"); <br>
   * ("kaptcha.border.thickness", "1"); 边框粗细度 <br>
   * ("kaptcha.noise.color", "BLACK"); 验证码噪点颜色<br>
   * ("kaptcha.obscurificator.impl", "WaterRipple"); 验证码样式引擎<br>
   * ("kaptcha.textproducer.font.names", "Arial,Courier"); 文本字体<br>
   * ("kaptcha.textproducer.font.color", "40");<br>
   * ("kaptcha.textproducer.font.size", "BLACK");<br>
   * ("kaptcha.textproducer.char.space", "2"); 文本字符间距<br>
   * ("kaptcha.background.clear.from", "LIGHT_GRAY"); 验证码背景颜色渐进<br>
   * ("kaptcha.background.clear.to", "WHITE"); 验证码背景颜色渐进<br>
   * ("kaptcha.image.width", "200");<br>
   * ("kaptcha.image.height", "50");
   */
  @Override
  public void afterSingletonsInstantiated() {
    qaClass = appProperties.getMisc().getCaptcha().getQa();
    try {
      Class.forName(qaClass.getName());
    } catch (ClassNotFoundException ex) {
      throw new CaptchaException("找不到指定的QA实现类", ex);
    }

    kaptcha = new DefaultKaptcha();
    Properties properties = new Properties();
    properties.setProperty("kaptcha.border", "yes");
    properties.setProperty("kaptcha.textproducer.font.names", "微软雅黑");
    kaptcha.setConfig(new Config(properties));
  }

  /**
   * 获取验证码问题文本.
   *
   * @param key key(可使用SessionID，在没有session的情况下，尽量使用一个能唯一标识当前访问者的key)
   * @return 验证码问题
   */
  public String justQuestion(String key) {
    return justQuestion(key, null);
  }

  /**
   * 获取验证码问题文本.
   *
   * @param key  key(可使用SessionID，在没有session的情况下，尽量使用一个能唯一标识当前访问者的key)
   * @param seed 验证码生成参数（使用不同的QA实现类，传入相应的参数）
   * @return 验证码问题
   */
  public String justQuestion(String key, Object seed) {
    CaptchaQa qa = getInstance();
    qa.make(seed);
    captchaCacheManager.put(key, qa);
    String question = qa.getQuestion();
    if (log.isDebugEnabled()) {
      log.debug("[验证码] - 新的 {}： {}", key, question);
    }
    return question;
  }

  /**
   * 获取验证码图片，内容为问题文本.
   *
   * @param key key(在没有session的情况下，充当sessionid的作用)
   * @return 图片
   */
  public BufferedImage image(String key) {
    String question = justQuestion(key);
    return kaptcha.createImage(question);
  }

  /**
   * 验证码检查答案是否正确.
   *
   * @param key    key(在没有session的情况下，充当sessionid的作用)
   * @param answer 答案
   * @return boolean
   */
  public boolean validate(String key, String answer) {
    CaptchaQa qa = captchaCacheManager.get(key);
    if (qa == null) {
      return false;
    }

    if (qa.isRight(answer)) {
      captchaCacheManager.remove(key);
      return true;
    } else {
      return false;
    }
  }

  /**
   * 获取QA实现类实例
   *
   * @return {@link CaptchaQa}
   */
  private CaptchaQa getInstance() {
    try {
      return qaClass.newInstance();
    } catch (InstantiationException | IllegalAccessException ex) {
      throw new CaptchaException("无法实例化QA实现类，请使用mark方法决定如何验证码内容，而不是构造函数", ex);
    }
  }

}
