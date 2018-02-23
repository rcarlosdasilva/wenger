package io.github.rcarlosdasilva.wenger.maven.plugin;

import io.github.rcarlosdasilva.wenger.maven.plugin.utils.Storage;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

public abstract class BasicMojo extends AbstractMojo {

  private static final String SPRING_GROUP_ID = "org.springframework.boot";

  @Parameter(readonly = true, defaultValue = "${project}")
  protected MavenProject project;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    analyzeModule();

    Storage.getInstance().setProject(project);
    Storage.getInstance().setLog(getLog());

    doit();
  }

  protected void analyzeModule() throws MojoFailureException {
    if (project.hasParent()) {
      MavenProject parent = project.getParent();
      if (SPRING_GROUP_ID.equals(parent.getGroupId())) {
        throw new MojoFailureException("[Wenger] - 请在具体的模块目录下执行");
      }
    }
  }

  abstract void doit() throws MojoExecutionException, MojoFailureException;
}