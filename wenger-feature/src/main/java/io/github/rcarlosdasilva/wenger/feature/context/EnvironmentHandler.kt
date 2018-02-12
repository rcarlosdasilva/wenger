package io.github.rcarlosdasilva.wenger.feature.context

import com.google.common.base.Enums
import io.github.rcarlosdasilva.kits.sys.SystemHelper
import io.github.rcarlosdasilva.wenger.common.constant.GeneralConstant
import io.github.rcarlosdasilva.wenger.feature.config.AppProperties
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.context.ContextIdApplicationContextInitializer
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

/**
 * 环境
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
@Component
@EnableConfigurationProperties(value = [AppProperties::class])
class EnvironmentHandler @Autowired constructor(
  private val env: Environment,
  private val appProperties: AppProperties
) : ContextIdApplicationContextInitializer(), BeanPostProcessor {

  private val logger = KotlinLogging.logger {}

  lateinit var runtimeProfile: RuntimeProfile
  lateinit var applicationName: String
  lateinit var tempDir: String

  override fun initialize(applicationContext: ConfigurableApplicationContext) {
    applicationName = applicationContext.applicationName ?: GeneralConstant.DEFAULT_APPLICATION_NAME
    super.initialize(applicationContext)
  }

  override fun postProcessBeforeInitialization(bean: Any, beanName: String?): Any? {
    val profiles = env.activeProfiles
    if (profiles.size > 1) {
      logger.warn { "[Environment] - 激活了多个Profile，执行环境将会是第一个激活的预置环境，建议使用单个Profile" }
    }

    val rps = profiles.mapNotNull { Enums.getIfPresent(RuntimeProfile::class.java, it.toUpperCase()).orNull() }

    runtimeProfile = if (rps.isEmpty()) {
      logger.warn { "[Environment] - 未找到预置环境（devel, ci, test, prepro, production）" }
      RuntimeProfile.CUSTOM_DEFINED
    } else {
      rps[0]
    }

    this.tempDir = appProperties.tempDir ?: SystemHelper.tempDir()
    logger.info { "[Environment] - 使用临时目录：$tempDir" }

    EnvironmentHandlerHolder.Companion.self = this
    return bean
  }

  class EnvironmentHandlerHolder private constructor() {
    companion object {
      internal lateinit var self: EnvironmentHandler

      fun get(): EnvironmentHandler {
        return self
      }
    }
  }

}

/**
 * 系统运行时环境
 *
 * @author [Dean Zhao](mailto:rcarlosdasilva@qq.com)
 */
enum class RuntimeProfile {
  /**
   * 开发环境
   */
  DEVEL,
  /**
   * 持续集成环境
   */
  CI,
  /**
   * 测试环境
   */
  TEST,
  /**
   * 预生产环境
   */
  PREPRO,
  /**
   * 生产环境
   */
  PRODUCTION,
  /**
   * 自定义
   */
  CUSTOM_DEFINED
}