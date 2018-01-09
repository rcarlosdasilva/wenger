package io.github.rcarlosdasilva.wenger.feature.context;

import com.google.common.base.Enums;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import io.github.rcarlosdasilva.kits.sys.SystemHelper;
import io.github.rcarlosdasilva.wenger.common.constant.GeneralConstant;
import io.github.rcarlosdasilva.wenger.feature.config.AppProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.ContextIdApplicationContextInitializer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 环境
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Slf4j
@Component
@EnableConfigurationProperties({AppProperties.class})
public class EnvironmentHandler extends ContextIdApplicationContextInitializer implements BeanPostProcessor {

  @Autowired
  private Environment env;
  @Autowired
  private AppProperties appProperties;

  @Getter
  private RuntimeProfile runtimeProfile;
  @Getter
  private String applicationName;
  @Getter
  private String tempDir;

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    applicationName = applicationContext.getApplicationName();
    if (Strings.isNullOrEmpty(applicationName)) {
      applicationName = GeneralConstant.DEFAULT_APPLICATION_NAME;
    }
    super.initialize(applicationContext);
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) {
    String[] profiles = env.getActiveProfiles();
    if (profiles.length > 1) {
      log.warn("[Environment] - 激活了多个Profile，执行环境将会是第一个激活的预置环境，建议使用单个Profile");
    }
    Stream.of(profiles).forEach(profile -> {
      if (runtimeProfile != null) {
        return;
      }

      Optional<RuntimeProfile> orf = Enums.getIfPresent(RuntimeProfile.class, profile.toUpperCase());
      if (orf.isPresent()) {
        runtimeProfile = orf.get();
      }
    });

    if (runtimeProfile == null) {
      log.warn("[Environment] - 未找到预置环境（devel, ci, test, prepro, production）");
      runtimeProfile = RuntimeProfile.CUSTOM_DEFINED;
    }

    this.tempDir = Objects.toString(appProperties.getTempDir(), SystemHelper.tempDir());
    log.info("[Environment] - 使用临时目录：{}", tempDir);

    EnvironmentHandlerHolder.setEnvironmentHandler(this);
    return bean;
  }

  public static class EnvironmentHandlerHolder {

    private static EnvironmentHandler environmentHandler;

    private static void setEnvironmentHandler(EnvironmentHandler environmentHandler) {
      EnvironmentHandlerHolder.environmentHandler = environmentHandler;
    }

    public static EnvironmentHandler get() {
      return environmentHandler;
    }

    private EnvironmentHandlerHolder() {
      throw new IllegalStateException();
    }

  }

}
