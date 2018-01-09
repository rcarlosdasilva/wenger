package io.github.rcarlosdasilva.wenger.feature.context;

/**
 * 系统运行时环境
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
public enum RuntimeProfile {

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
