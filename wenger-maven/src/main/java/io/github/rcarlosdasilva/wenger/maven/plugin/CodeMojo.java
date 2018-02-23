package io.github.rcarlosdasilva.wenger.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * 提供快捷的代码生成功能，降低开发成本
 *
 * @author <a href="mailto:rcarlosdasilva@qq.com">Dean Zhao</a>
 */
@Mojo(name = "code")
public class CodeMojo extends BasicMojo {

  @Override
  public void doit() throws MojoExecutionException, MojoFailureException {

  }
}
