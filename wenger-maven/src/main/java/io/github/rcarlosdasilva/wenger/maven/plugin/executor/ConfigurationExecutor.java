package io.github.rcarlosdasilva.wenger.maven.plugin.executor;

import com.google.common.collect.Sets;
import io.github.rcarlosdasilva.wenger.maven.plugin.utils.PropertiesUtil;
import io.github.rcarlosdasilva.wenger.maven.plugin.utils.Storage;
import org.apache.maven.plugin.MojoExecutionException;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Set;

public class ConfigurationExecutor {

  private static final String DEFAULT_APPLICATION_FILE_PATH = "/src/main/resources/application";

  @SuppressWarnings("unchecked")
  public static void fetch(String configPath) throws MojoExecutionException {
    String rootPath = Storage.getInstance().getProject().getBasedir().getAbsolutePath();
    if (configPath == null) {
      configPath = DEFAULT_APPLICATION_FILE_PATH + ".yml";
    }
    String defaultConfigPath = rootPath + configPath;

    Map<String, String> defaultConfig = readYamlAsProperties(defaultConfigPath);

    if (PropertiesUtil.keyStartsWith(defaultConfig, "spring.profiles.active")) {
      Set<String> profiles = Sets.newHashSet();
      int multiKeyCount = PropertiesUtil.multiKeyCount(defaultConfig, "spring.profiles.active");
      if (multiKeyCount > 0) {
        for (int i = 0; i < multiKeyCount; i++) {
          profiles.add(defaultConfig.get("spring.profiles.active[" + i + "]"));
        }
      } else {
        profiles.add(defaultConfig.get("spring.profiles.active"));
      }

      for (String p : profiles) {
        String profilePath = rootPath + DEFAULT_APPLICATION_FILE_PATH + "-" + p + ".yml";
        Map<String, String> conf = readYamlAsProperties(profilePath);
        defaultConfig.putAll(conf);
      }
    }

    Storage.getInstance().setConfig(defaultConfig);
  }

  private static Map<String, String> readYamlAsProperties(String path) throws MojoExecutionException {
    Storage.getInstance().getLog().info("加载配置文件: " + path);
    File defaultConfigFile = new File(path);
    if (!defaultConfigFile.exists()) {
      throw new MojoExecutionException("找不到配置文件: " + path);
    }


    try {
      Map<String, Object> yml = new Yaml().load(new FileInputStream(defaultConfigFile));
      return PropertiesUtil.fromMap(yml);
    } catch (FileNotFoundException ex) {
      throw new MojoExecutionException("无法读取配置文件", ex);
    }
  }

}
