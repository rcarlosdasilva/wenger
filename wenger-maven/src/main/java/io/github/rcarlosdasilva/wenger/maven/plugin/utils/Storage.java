package io.github.rcarlosdasilva.wenger.maven.plugin.utils;

import lombok.Getter;
import lombok.Setter;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.util.Map;

public final class Storage {

  @Getter
  private static final Storage instance = new Storage();

  @Getter
  @Setter
  private Log log;
  @Getter
  @Setter
  private MavenProject project;
  @Setter
  private Map<String, String> config;

  private Storage() {
  }

  public String getConfig(String key) {
    return config == null ? null : config.get(key);
  }

  public boolean containsConfig(String key) {
    return config != null && config.containsKey(key);
  }

  public boolean containsConfigStartsWith(String key) {
    return config != null && PropertiesUtil.keyStartsWith(config, key);
  }

}
