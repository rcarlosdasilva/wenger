package io.github.rcarlosdasilva.wenger.maven.plugin.executor;

import com.google.common.base.Strings;
import io.github.rcarlosdasilva.wenger.maven.plugin.utils.Storage;
import org.apache.maven.plugin.MojoExecutionException;
import org.flywaydb.core.Flyway;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import javax.sql.DataSource;

public class FlywayExecutor {

  private static final String DEFAULT_MIGRATION_SCRIPT_DIRECTORY = "/src/main/resources/db/migration";

  public static void migration() throws MojoExecutionException {
    flyway().migrate();
  }

  public static void clean() throws MojoExecutionException {
    flyway().clean();
  }

  public static void repair() throws MojoExecutionException {
    flyway().repair();
  }

  @SuppressWarnings("unchecked")
  private static DataSource datasource() throws MojoExecutionException {
    Storage storage = Storage.getInstance();
    if (!storage.containsConfigStartsWith("spring.datasource")) {
      return null;
    }

    SimpleDriverDataSource sdds = new SimpleDriverDataSource();
    String driverClassName = storage.getConfig("spring.datasource.driver-class-name");
    if (!Strings.isNullOrEmpty(driverClassName)) {
      try {
        Class driver = Class.forName(driverClassName);
        sdds.setDriverClass(driver);
      } catch (ClassNotFoundException e) {
        throw new MojoExecutionException("", e);
      }
    }
    sdds.setUrl(storage.getConfig("spring.datasource.url"));
    sdds.setUsername(storage.getConfig("spring.datasource.username"));
    sdds.setPassword(storage.getConfig("spring.datasource.password"));

    return sdds;
  }

  private static Flyway flyway() throws MojoExecutionException {
    String rootPath = Storage.getInstance().getProject().getBasedir().getAbsolutePath();
    String migrationPath = "filesystem:" + rootPath + DEFAULT_MIGRATION_SCRIPT_DIRECTORY;

    Flyway flyway = new Flyway();
    flyway.setDataSource(datasource());
    flyway.setLocations(migrationPath);
    return flyway;
  }

}
