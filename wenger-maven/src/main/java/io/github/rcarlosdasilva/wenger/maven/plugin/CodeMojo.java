package io.github.rcarlosdasilva.wenger.maven.plugin;

import io.github.rcarlosdasilva.wenger.maven.plugin.executor.CogenExecutor;
import io.github.rcarlosdasilva.wenger.maven.plugin.executor.ConfigurationExecutor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * 提供快捷的代码生成功能，降低开发成本
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Mojo(name = "code", threadSafe = true)
public class CodeMojo extends BasicMojo {

  /**
   * Cogen代码生成配置文件路径
   */
  @Parameter(required = true)
  private String cogenConfigLocation;

  /**
   * <p>具体配置文件路径（默认：通过模块根目录下/src/main/resources/application.yml文件自动获取），一般不用设置</p>
   */
  @Parameter
  private String configPath;

  @Override
  public void doit() throws MojoExecutionException, MojoFailureException {
    ConfigurationExecutor.fetch(configPath);
    CogenExecutor.execute(cogenConfigLocation);
  }
}
