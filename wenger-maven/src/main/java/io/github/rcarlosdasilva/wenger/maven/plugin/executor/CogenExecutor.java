package io.github.rcarlosdasilva.wenger.maven.plugin.executor;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.rcarlosdasilva.cogen.config.*;
import io.github.rcarlosdasilva.cogen.core.Generator;
import io.github.rcarlosdasilva.cogen.db.MysqlTypeConverter;
import io.github.rcarlosdasilva.kits.string.TextHelper;
import io.github.rcarlosdasilva.wenger.maven.plugin.utils.PropertiesUtil;
import io.github.rcarlosdasilva.wenger.maven.plugin.utils.Storage;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CogenExecutor {

  private static final String PROPERTIES_FILE_EXTENSION = ".properties";
  private static final String LIST_STRING_SEPARATOR = ",";

  private static Properties globalConfig;
  private static List<File> tableConfigs;

  public static void execute(String location) throws MojoFailureException {
    parse(location);

    Configuration configuration = globalConfig();
    for (File tc : tableConfigs) {
      configuration.getDatabase().getTables().clear();
      configuration.getDatabase().getTables().putAll(dbConfig(tc));
      Generator.INSTANCE.run(configuration);
      done(tc);
    }
  }

  private static void parse(String location) throws MojoFailureException {
    File configFile = new File(location);
    String configDirectory = configFile.getParent();

    Properties prop = loadProperties(configFile.getAbsolutePath());

    MavenProject project = Storage.getInstance().getProject();
    String modulePrefix = prop.getProperty("global.moudle.ms.prefix");
    String artifactId = project.getArtifactId();

    // 模板目录默认与配置目录相同
    String templateDirectory = prop.getProperty("global.template.dir", configDirectory);
    // 数据库表配置目录默认与配置目录相同
    String dbConfigDirectory = prop.getProperty("global.config.db.dir", configDirectory);
    // 数据库表配置文件前缀
    String dbConfigPrefix = prop.getProperty("global.config.db.prefix", "");

    if (Strings.isNullOrEmpty(dbConfigDirectory)) {
      dbConfigDirectory = configDirectory;
    }
    if (Strings.isNullOrEmpty(templateDirectory)) {
      templateDirectory = configDirectory;
    }

    prop.setProperty("runtime.template.dir", templateDirectory);
    prop.setProperty("runtime.config.db.dir", dbConfigDirectory);
    prop.setProperty("runtime.config.db.prefix", dbConfigPrefix);
    prop.setProperty("runtime.root.path", project.getParent().getBasedir().getAbsolutePath());
    prop.setProperty("runtime.module.name", artifactId);
    prop.setProperty("runtime.module.simplename", TextHelper.trim(artifactId, modulePrefix, -1));
    prop.setProperty("runtime.db.driver", Storage.getInstance().getConfig("spring.datasource.driver-class-name"));
    prop.setProperty("runtime.db.url", Storage.getInstance().getConfig("spring.datasource.url"));
    prop.setProperty("runtime.db.username", Storage.getInstance().getConfig("spring.datasource.username"));
    prop.setProperty("runtime.db.password", Storage.getInstance().getConfig("spring.datasource.password"));
    globalConfig = prop;

    // 加载未执行过的数据库表配置
    File dbConfigSpoor = new File(dbConfigDirectory + "/.spoor");
    if (!dbConfigSpoor.exists()) {
      dbConfigSpoor.mkdirs();
    }
    List<String> spoors = Stream.of(dbConfigSpoor.listFiles()).map(File::getName).collect(Collectors.toList());

    File dbConfigDirectoryFile = new File(dbConfigDirectory);
    File[] dbConfigs = dbConfigDirectoryFile.listFiles(pathname -> {
      String name = pathname.getName();
      boolean right = name.startsWith(dbConfigPrefix) && name.endsWith(PROPERTIES_FILE_EXTENSION);
      String spoorName = TextHelper.trim(name, dbConfigPrefix, -1);
      spoorName = "." + TextHelper.trim(spoorName, PROPERTIES_FILE_EXTENSION, 1);
      return right && !spoors.contains(spoorName);
    });
    tableConfigs = Lists.newArrayList(dbConfigs);
  }

  private static Map<String, Table> dbConfig(File dbConfig) throws MojoFailureException {
    Properties dbProp = loadProperties(dbConfig.getAbsolutePath());

    Map<String, String> dbConfigs = PropertiesUtil.getByKeyStartsWith(dbProp, "db.table.");
    Set<String> dbNames = dbConfigs.keySet().stream().map(p -> {
      int ind = p.indexOf('.', 9);
      return ind > 0 ? p.substring(9, ind) : p.substring(9);
    }).distinct().collect(Collectors.toSet());

    Map<String, Table> tables = Maps.newHashMap();
    for (String dn : dbNames) {
      String propPrefix = "db.table." + dn;
      Table table = new Table(dn);
      String includes = dbProp.getProperty(propPrefix + ".includes");
      if (!Strings.isNullOrEmpty(includes)) {
        table.setIncludes(Lists.newArrayList(includes.split(LIST_STRING_SEPARATOR)));
      }
      String excludes = dbProp.getProperty(propPrefix + ".excludes");
      if (!Strings.isNullOrEmpty(excludes)) {
        table.setExcludes(Lists.newArrayList(excludes.split(LIST_STRING_SEPARATOR)));
      }
      table.setHoldTablePrefix(
          Boolean.parseBoolean(dbProp.getProperty(propPrefix + ".hold.prefix.table", "false")));
      String prefixs = dbProp.getProperty(propPrefix + ".field.cut.prefixs");
      if (!Strings.isNullOrEmpty(prefixs)) {
        table.setCutFieldPrefixes(Lists.newArrayList(prefixs.split(LIST_STRING_SEPARATOR)));
      }
      String ignores = dbProp.getProperty(propPrefix + ".field.ignores");
      if (!Strings.isNullOrEmpty(ignores)) {
        table.setIgnoreFields(Lists.newArrayList(ignores.split(LIST_STRING_SEPARATOR)));
      }
      tables.put(dn, table);
    }

    return tables;
  }

  private static Configuration globalConfig() throws MojoFailureException {
    ClassType classType = ClassType.valueOf(globalConfig.getProperty("code.lang").toUpperCase());
    Boolean usePrimitive = Boolean.parseBoolean(globalConfig.getProperty("code.primitive", "true"));

    Configuration configuration = new Configuration();
    configuration.setOut(globalConfig.getProperty("runtime.root.path"));
    configuration.setTemplateDir(globalConfig.getProperty("runtime.template.dir"));
    configuration.setAuthorName(globalConfig.getProperty("global.author.name"));
    configuration.setAuthorEmail(globalConfig.getProperty("global.author.mail"));
    configuration.setShowTime(Boolean.parseBoolean(globalConfig.getProperty("global.javadoc.since", "false")));
    configuration.setVersion(globalConfig.getProperty("global.javadoc.version"));
    configuration.setLang(classType);
    configuration.setCoverage(Boolean.parseBoolean(globalConfig.getProperty("global.code.cover", "false")));
    configuration.setOpenExplorer(false);

    Database database = new Database(
        globalConfig.getProperty("runtime.db.driver"),
        globalConfig.getProperty("runtime.db.url"),
        globalConfig.getProperty("runtime.db.username"),
        globalConfig.getProperty("runtime.db.password")
    );
    boolean up = classType == ClassType.JAVA ? usePrimitive : false;
    database.setDbTypeConverter(new MysqlTypeConverter(up));
    database.setIdName(globalConfig.getProperty("db.id.name"));
    database.setIgnoreId(Boolean.parseBoolean(globalConfig.getProperty("db.id.ignore", "true")));
    String ignoreFields = globalConfig.getProperty("db.field.ignore");
    if (!Strings.isNullOrEmpty(ignoreFields)) {
      database.setIgnoreFields(Lists.newArrayList(ignoreFields.split(LIST_STRING_SEPARATOR)));
    }
    String ignoreFieldsByrefixes = globalConfig.getProperty("db.field.ignore.prefixes");
    if (!Strings.isNullOrEmpty(ignoreFieldsByrefixes)) {
      database.setIgnoreFieldsByPrefixes(Lists.newArrayList(ignoreFieldsByrefixes.split(LIST_STRING_SEPARATOR)));
    }
    String cutFieldPrefixes = globalConfig.getProperty("db.field.cut.prefixes");
    if (!Strings.isNullOrEmpty(cutFieldPrefixes)) {
      database.setCutFieldPrefixes(Lists.newArrayList(cutFieldPrefixes.split(LIST_STRING_SEPARATOR)));
    }
    configuration.setDatabase(database);

    final String entityPackage = globalConfig.getProperty("code.package.entity")
        .replace("$module", globalConfig.getProperty("runtime.module.simplename"));
    if ("entity".equalsIgnoreCase(globalConfig.getProperty("code.base", "db"))) {
      // 暂时不实现基于entity生成
      boolean allClasses = Boolean.parseBoolean(globalConfig.getProperty("code.package.entity.classes.all", "false"));
      List<Class<?>> classess;
      if (allClasses) {
        classess = loadClassesAll(entityPackage);
      } else {
        List<String> classNames = Lists.newArrayList(globalConfig.getProperty("code.package.entity.classes").split
            (LIST_STRING_SEPARATOR +
                ""));
        classess = loadClasses(entityPackage, classNames);
      }
      configuration.getEntities().addAll(classess);
    } else {
      String templateName = classType.name().toLowerCase() + "_" + globalConfig.getProperty("code.package.entity" +
          ".template");
      BasicPackage pck = new BasicPackage(entityPackage, templateName);
      pck.setModule(globalConfig.getProperty("runtime.module.name"));
      pck.getBasicClass().setPrefix(globalConfig.getProperty("code.package.entity.class.prefix", ""));
      pck.getBasicClass().setSuffix(globalConfig.getProperty("code.package.entity.class.suffix", ""));
      pck.getBasicClass().setExtension(classType.getExtension());

      List<String> keys = PropertiesUtil.getMultiKeysByStartsWith(globalConfig, "code.package.entity.class.super");
      for (String key : keys) {
        String k = globalConfig.getProperty(key + ".field");
        String v = globalConfig.getProperty(key + ".class");
        pck.getBasicClass().getSupers().put(k, v);
      }

      configuration.setEntityPackage(pck);
    }

    Map<String, String> pckConfigs = PropertiesUtil.getByKeyStartsWith(globalConfig, "code.package.");
    Set<String> pckNames = pckConfigs.keySet().stream().map(p -> {
      int ind = p.indexOf('.', 13);
      return ind > 0 ? p.substring(13, ind) : p.substring(13);
    }).distinct()
        .filter(p -> !p.equalsIgnoreCase("entity"))
        .collect(Collectors.toSet());
    for (String pn : pckNames) {
      String propPrefix = "code.package." + pn;
      String pckPath = globalConfig.getProperty(propPrefix)
          .replace("$module", globalConfig.getProperty("runtime.module.simplename"));
      String templateName = classType.name().toLowerCase() + "_" + globalConfig.getProperty(propPrefix + ".template");
      BasicPackage pck = new BasicPackage(pckPath, templateName);
      pck.setModule(globalConfig.getProperty(propPrefix + ".moudle", globalConfig.getProperty("runtime.module.name")));
      pck.getBasicClass().setPrefix(globalConfig.getProperty(propPrefix + ".class.prefix", ""));
      pck.getBasicClass().setSuffix(globalConfig.getProperty(propPrefix + ".class.suffix", ""));
      pck.getBasicClass().setExtension(
          globalConfig.getProperty(propPrefix + ".class.extension", classType.getExtension()));
      configuration.getPackages().add(pck);
    }

    Map<String, String> extras = PropertiesUtil.getByKeyStartsWith(globalConfig, "extras.");
    extras.forEach((k, v) -> {
      if (v.startsWith("@")) {
        String ref = v.substring(1);
        v = globalConfig.getProperty(ref, "")
            .replace("$module", globalConfig.getProperty("runtime.module.simplename"));
      }
      configuration.getExtras().put(k.substring(7), v);
    });
    return configuration;
  }

  private static void done(File dbConfig) throws MojoFailureException {
    String spoorName = TextHelper.trim(dbConfig.getName(), globalConfig.getProperty("runtime.config.db.prefix"), -1);
    spoorName = "." + TextHelper.trim(spoorName, PROPERTIES_FILE_EXTENSION, 1);

    File dbConfigSpoor = new File(globalConfig.getProperty("runtime.config.db.dir") + "/.spoor");
    if (!dbConfigSpoor.exists()) {
      dbConfigSpoor.mkdirs();
    }
    File spoorFile = new File(dbConfigSpoor.getAbsolutePath() + "/" + spoorName);
    try {
      spoorFile.createNewFile();
    } catch (IOException ex) {
      throw new MojoFailureException("创建表配置历史文件失败", ex);
    }
  }

  private static List<Class<?>> loadClassesAll(String packageName) throws MojoFailureException {
    String lang = globalConfig.getProperty("code.lang").toLowerCase();
    String packagePath = packageName.replace('.', '/');
    String fullPath = TextHelper.join("/",
        Storage.getInstance().getProject().getBasedir().getAbsolutePath(),
        "src/main",
        lang,
        packagePath);

    File packageDirectory = new File(fullPath);
    File[] classFiles = packageDirectory.listFiles();
    List<Class<?>> classes = Lists.newArrayList();
    for (File cf : classFiles) {
      String cn = TextHelper.trim(cf.getName(), "." + lang, 1);
      classes.add(loadClass(packageName + "." + cn));
    }
    return classes;
  }

  private static List<Class<?>> loadClasses(String packageName, List<String> classSimpleNames) throws
      MojoFailureException {
    List<Class<?>> classes = Lists.newArrayList();
    for (String cn : classSimpleNames) {
      classes.add(loadClass(packageName + "." + cn));
    }
    return classes;
  }

  private static Class<?> loadClass(String cls) throws MojoFailureException {
    try {
      return Class.forName(cls);
    } catch (ClassNotFoundException ex) {
      throw new MojoFailureException("找不到类：" + cls, ex);
    }
  }

  private static Properties loadProperties(String path) throws MojoFailureException {
    Properties prop = new Properties();
    try (InputStream is = new FileInputStream(new File(path))) {
      prop.load(is);
    } catch (IOException ex) {
      throw new MojoFailureException("找不到Cogen配置文件：" + path, ex);
    }
    return prop;
  }

}

// TODO 配置文件extra  的 . 层级处理