package io.github.rcarlosdasilva.wenger.maven.plugin;

import com.google.common.collect.Sets;
import io.github.rcarlosdasilva.wenger.maven.plugin.executor.ConfigurationExecutor;
import io.github.rcarlosdasilva.wenger.maven.plugin.executor.FlywayExecutor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.Set;

/**
 * 提供开发中所有与数据库相关的操作（基于Flyway实现）
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Mojo(name = "db")
public class DbMojo extends BasicMojo {

  private static final Set<String> actions = Sets.newHashSet("migration", "clean", "repair");

  /**
   * <p>执行动作</p>
   * 取值：
   * <ul>
   * <li>migration: 执行Flyway迁移，不启动项目</li>
   * <li>clean: 清除所有表</li>
   * <li>repair: 执行Flyway修复，用于在迁移过程中产生的冲突或执行失败时，恢复到最后一个成功版本，详情参考Flyway官网</li>
   * </ul>
   */
  @Parameter(property = "action", required = true)
  private String action;

  /**
   * <p>具体配置文件路径（默认：通过模块根目录下/src/main/resources/application.yml文件自动获取），一般不用设置</p>
   */
  @Parameter(property = "configPath")
  private String configPath;

  @Override
  public void doit() throws MojoExecutionException, MojoFailureException {
    if (!actions.contains(action)) {
      throw new MojoFailureException("错误的action");
    }

    ConfigurationExecutor.fetch(configPath);
    switch (action) {
      case "migration":
        FlywayExecutor.migration();
        break;
      case "clean":
        FlywayExecutor.clean();
        break;
      case "repair":
        FlywayExecutor.repair();
        break;
      default:
    }
  }

}
