package io.github.rcarlosdasilva.wenger.maven;

import io.github.rcarlosdasilva.wenger.maven.plugin.DbMojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;

import java.io.File;

public class DbMojoTest extends AbstractMojoTestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testMojoGoal() throws Exception {
    File testPom = new File(getBasedir(), "target/test-classes/unit/db-migration/plugin-config.xml");
    DbMojo mojo = (DbMojo) lookupMojo("db", testPom);
    assertNotNull(mojo);
    setVariableValueToObject(mojo, "project", getMockMavenProject());
    setVariableValueToObject(mojo, "action", "migration");
    mojo.execute();
  }

  private MavenProject getMockMavenProject() {
    MavenProject mp = new MavenProject();
    mp.setFile(new File("C:\\working\\java\\wenger\\wenger-ms-upms\\pom.xml"));
    return mp;
  }

}