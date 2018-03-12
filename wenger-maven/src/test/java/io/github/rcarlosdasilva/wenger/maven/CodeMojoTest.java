package io.github.rcarlosdasilva.wenger.maven;

import io.github.rcarlosdasilva.wenger.maven.plugin.CodeMojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;

import java.io.File;

public class CodeMojoTest extends AbstractMojoTestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testMojoGoal() throws Exception {
    File testPom = new File(getBasedir(), "target/test-classes/unit/code-normal/plugin-config.xml");
    CodeMojo mojo = (CodeMojo) lookupMojo("code", testPom);
    assertNotNull(mojo);
    setVariableValueToObject(mojo, "project", getMockMavenProject());
    mojo.execute();
  }

  private MavenProject getMockMavenProject() {
    MavenProject mp = new MavenProject();
    mp.setFile(new File("C:\\working\\java\\wenger\\wenger-ms-upms\\pom.xml"));
    return mp;
  }
}
