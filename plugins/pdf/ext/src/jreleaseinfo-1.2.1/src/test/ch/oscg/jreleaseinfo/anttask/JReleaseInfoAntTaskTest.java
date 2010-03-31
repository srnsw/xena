/*
 * Copyright 2004 Thomas Cotting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id$
 */
package ch.oscg.jreleaseinfo.anttask;

import junit.framework.TestCase;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Parameter;

import ch.oscg.jreleaseinfo.JReleaseInfoProperty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Some JUnit testcases for the JReleaseInfoAntTask Ant Task.
 * 
 * @author Thomas Cotting, Tangarena Engineering AG, Luzern
 * @version $Revision$ ($Date$ / $Author$)
 */
public class JReleaseInfoAntTaskTest extends TestCase {

   private String sep = System.getProperty("file.separator");

   private String tmpDir = System.getProperty("java.io.tmpdir");

   /**
    * Constructor for JReleaseInfoAntTaskTest.
    * 
    * @param arg0
    *           String
    */
   public JReleaseInfoAntTaskTest(String arg0) {
      super(arg0);
   }

   /**
    * EntryPoint to Test
    * 
    * @param args
    *           String Array
    */
   public static void main(String[] args) {
      junit.textui.TestRunner.run(JReleaseInfoAntTaskTest.class);
   }

   /**
    * @see TestCase#setUp()
    */
   protected void setUp() throws Exception {
      super.setUp();
   }

   /**
    * @see TestCase#tearDown()
    */
   protected void tearDown() throws Exception {
      super.tearDown();
   }

   /**
    * Utiltiy method to generate path to tempfile directory.
    * 
    * @param filename
    * @return path
    */
   private String getAbsolutePath(String filename) {
      return tmpDir + sep + filename;
   }

   /**
    * Utility method to read the content of a file in a String
    * 
    * @param file
    *           to read
    * 
    * @return content of file
    * @throws IOException
    *            on file operations failures
    */
   protected String readFile(File file) throws IOException {
      FileReader fr = new FileReader(file);
      BufferedReader br = new BufferedReader(fr);
      StringBuffer buf = new StringBuffer();

      String line = null;

      while ((line = br.readLine()) != null) {
         buf.append(line);
      }

      br.close();

      return buf.toString();
   }

   /**
    * Utility method to test the content of a file
    * 
    * @param content
    *           file content
    * @param fieldname
    *           parameter name
    * @param methodname
    *           of accessor
    * @param value
    *           parameter value
    * 
    * @return true when parameteraccessor method is contained
    */
   private boolean checkStringParameter(String content, String fieldName, String methodName, String value) {
      String patDef = "String " + fieldName + " = new String(\"" + value + "\")";
      boolean hasDef = (content.indexOf(patDef) != -1);
      String patAcc = "public static final String " + methodName + "() { return " + fieldName + "; }";
      boolean hasAcc = (content.indexOf(patAcc) != -1);
      return (hasDef && hasAcc);
   }

   /**
    * Utility method to test the content of a file
    * 
    * @param content
    *           file content
    * @param fieldname
    *           parameter name
    * @param methodname
    *           of accessor
    * @param value
    *           parameter value
    * 
    * @return true when parameteraccessor method is contained
    */
   private boolean checkIntParameter(String content, String fieldName, String methodName, int value) {
      String patAcc = "public static final int " + methodName + "() { return " + value + "; }";
      boolean hasAcc = (content.indexOf(patAcc) != -1);
      return (hasAcc);
   }

   /**
    * Utility method to test the content of a file
    * 
    * @param content
    *           file content
    * @param fieldname
    *           parameter name
    * @param methodname
    *           of accessor
    * @param value
    *           parameter value
    * 
    * @return true when parameteraccessor method is contained
    */
   private boolean checkIntegerParameter(String content, String fieldName, String methodName, Integer value) {
      String patDef = "Integer " + fieldName + " = new Integer(" + value + ")";
      boolean hasDef = (content.indexOf(patDef) != -1);
      String patAcc = "public static final Integer " + methodName + "() { return " + fieldName + "; }";
      boolean hasAcc = (content.indexOf(patAcc) != -1);
      return (hasDef && hasAcc);
   }

   /**
    * Utility method to test the content of a file
    * 
    * @param content
    *           file content
    * @param fieldname
    *           parameter name
    * @param methodname
    *           of accessor
    * @param value
    *           parameter value
    * 
    * @return true when parameteraccessor method is contained
    */
   private boolean checkBooleanParameter(String content, String fieldName, String methodName, String value) {
      String patAcc = "public static final Boolean " + methodName + "() { return " + value + "; }";
      boolean hasAcc = (content.indexOf(patAcc) != -1);
      return (hasAcc);
   }

   /**
    * Utility method to test the content of a file
    * 
    * @param content
    *           file content
    * @param fieldname
    *           parameter name
    * @param methodname
    *           of accessor
    * @param value
    *           parameter value
    * 
    * @return true when parameteraccessor method is contained
    */
   private boolean check_booleanParameter(String content, String fieldName, String methodName, boolean value) {
      String pattern = "public static final boolean " + methodName + "() { return " + value + "; }";
      boolean hasAcc = (content.indexOf(pattern) != -1);
      return (hasAcc);
   }

   /**
    * Test execute with an invalid classname.
    */
   public void testInvalidClassName() {
      JReleaseInfoAntTask version = new JReleaseInfoAntTask();
      String className = "a.d.d";
      version.setClassName(className);

      try {
         version.execute();

         assertTrue("Invalid Java file " + className + " should have been detected", false);
      } catch (BuildException ex) {
         // Should occur
         System.out.println("\nException OK: Msg = " + ex.getMessage());
         assertTrue(true);
      } catch (Exception ex) {
         // Should not occur
         assertTrue(ex.getMessage(), false);
      }

      version.deleteJReleaseInfoFile();
   }

   /**
    * Test execute with an invalid classname.
    */
   public void testEmptyClassName() {
      JReleaseInfoAntTask version = new JReleaseInfoAntTask();
      String className = "";
      version.setClassName(className);

      try {
         version.execute();

         assertTrue("Invalid Java file " + className + "not detected", false);
      } catch (BuildException ex) {
         // Should occur
         System.out.println("\nException OK: Msg = " + ex.getMessage());
         assertTrue(true);
      } catch (Exception ex) {
         // Should not occur
         assertTrue(ex.getMessage(), false);
      }

      version.deleteJReleaseInfoFile();
   }

   /**
    * Test execute with an invalid className.
    */
   public void testNullClassName() {
      JReleaseInfoAntTask version = new JReleaseInfoAntTask();
      String className = null;
      version.setClassName(className);

      try {
         version.execute();

         assertTrue("Invalid Java file " + className + "not detected", false);
      } catch (BuildException ex) {
         // Should occur
         System.out.println("\nException OK: Msg = " + ex.getMessage());
         assertTrue(true);
      } catch (Exception ex) {
         // Should not occur
         assertTrue(ex.getMessage(), false);
      }

      version.deleteJReleaseInfoFile();
   }

   /**
    * Test execute with an unset filename.
    */
   public void testNotsetFilename() {
      JReleaseInfoAntTask version = new JReleaseInfoAntTask();

      try {
         version.execute();

         // Should not succeed
         assertTrue(false);
      } catch (BuildException ex) {
         // Should occur
         System.out.println("\nException OK: Msg = " + ex.getMessage());
         assertTrue(true);
      } catch (Exception ex) {
         // Should not occur
         assertTrue(ex.getMessage(), false);
      }

      version.deleteJReleaseInfoFile();
   }

   /**
    * Test execute with a valid filename.
    */
   public void testValidFilename() {
      File f = null;
      JReleaseInfoAntTask version = null;

      try {
         version = new JReleaseInfoAntTask();
         version.setProject(new Project());
         version.setTargetDir(tmpDir);
         version.setClassName("test");
         version.execute();
         f = new File(this.getAbsolutePath("Test.java"));
         assertTrue("Java file " + f.getPath() + " not detected", f.exists());
         f.delete();

         version = new JReleaseInfoAntTask();
         version.setProject(new Project());
         version.setTargetDir(tmpDir);
         version.setPackageName("");
         version.setClassName("test");
         version.execute();
         f = new File(this.getAbsolutePath("Test.java"));
         assertTrue("Java file " + f.getPath() + " not detected", f.exists());
         f.delete();

         version = new JReleaseInfoAntTask();
         version.setProject(new Project());
         version.setTargetDir(tmpDir);
         version.setPackageName(null);
         version.setClassName("test");
         version.execute();
         f = new File(this.getAbsolutePath("Test.java"));
         assertTrue("Java file " + f.getPath() + " not detected", f.exists());
         f.delete();

         version = new JReleaseInfoAntTask();
         version.setProject(new Project());
         version.setTargetDir(tmpDir);
         version.setPackageName("target.testp");
         version.setClassName("test");
         version.execute();
         f = new File(this.getAbsolutePath("target/testp/Test.java"));
         assertTrue("Java file " + f.getPath() + " not detected", f.exists());
         f.delete();

         version = new JReleaseInfoAntTask();
         version.setProject(new Project());
         version.setTargetDir(tmpDir);
         version.setClassName("t");
         version.execute();
         f = new File(this.getAbsolutePath("T.java"));
         assertTrue("Java file " + f.getPath() + " not detected", f.exists());
         f.delete();

         version = new JReleaseInfoAntTask();
         version.setProject(new Project());
         version.setTargetDir(tmpDir);
         version.setClassName("tata");
         version.execute();
         f = new File(this.getAbsolutePath("TaTa.java"));
         assertTrue("Java file " + f.getPath() + " not detected", f.exists());
         f.delete();

         version = new JReleaseInfoAntTask();
         version.setProject(new Project());
         version.setTargetDir(tmpDir);
         version.setClassName("VersionX");
         version.execute();
         f = new File(this.getAbsolutePath("VersionX.java"));
         assertTrue("Java file " + f.getPath() + " not detected", f.exists());
         f.delete();

      } catch (BuildException ex) {
         // Should occur
         System.out.println("\nException OK: Msg = " + ex.getMessage());
         assertTrue(true);
      } catch (Exception ex) {
         // Should not occur
         assertTrue(ex.getMessage(), false);
      }

      version.deleteJReleaseInfoFile();
   }

   /**
    * Test execute with a invalid filename.
    */
   public void testInvalidFilenameHandling() {
      JReleaseInfoAntTask version = null;

      try {
         version = new JReleaseInfoAntTask();
         version.setProject(new Project());
         version.setTargetDir(tmpDir);
         version.setPackageName(".");
         version.setClassName("test");
         version.execute();
         assertTrue("Invalid path for " + version.getJReleaseInfoFile().getPath() + " not detected", false);
      } catch (BuildException ex) {
         // Should occur
         System.out.println("\nException OK: Msg = " + ex.getMessage());
         assertTrue(true);
      } catch (Exception ex) {
         // Should not occur
         assertTrue(ex.getMessage(), false);
      }

      version.deleteJReleaseInfoFile();

      try {
         version = new JReleaseInfoAntTask();
         version.setProject(new Project());
         version.setTargetDir(tmpDir);
         version.setPackageName("aaa.");
         version.setClassName("test");
         version.execute();
         assertTrue("Invalid path for " + version.getJReleaseInfoFile().getPath() + " not detected", false);
      } catch (BuildException ex) {
         // Should occur
         System.out.println("\nException OK: Msg = " + ex.getMessage());
         assertTrue(true);
      } catch (Exception ex) {
         // Should not occur
         assertTrue(ex.getMessage(), false);
      }

      version.deleteJReleaseInfoFile();

      try {
         version = new JReleaseInfoAntTask();
         version.setProject(new Project());
         version.setTargetDir(tmpDir);
         version.setPackageName(".aaa");
         version.setClassName("test");
         version.execute();
         assertTrue("Invalid path for " + version.getJReleaseInfoFile().getPath() + " not detected", false);
      } catch (BuildException ex) {
         // Should occur
         System.out.println("\nException OK: Msg = " + ex.getMessage());
         assertTrue(true);
      } catch (Exception ex) {
         // Should not occur
         assertTrue(ex.getMessage(), false);
      }

      version.deleteJReleaseInfoFile();

      try {
         version = new JReleaseInfoAntTask();
         version.setProject(new Project());
         version.setTargetDir(tmpDir);
         version.setPackageName("/");
         version.setClassName("test");
         version.execute();
         assertTrue("Invalid path for " + version.getJReleaseInfoFile().getPath() + " not detected", false);
      } catch (BuildException ex) {
         // Should occur
         System.out.println("\nException OK: Msg = " + ex.getMessage());
         assertTrue(true);
      } catch (Exception ex) {
         // Should not occur
         assertTrue(ex.getMessage(), false);
      }

      version.deleteJReleaseInfoFile();
   }

   /**
    * Test accessor method generation
    */
   public void testSetGetParameters() {
      JReleaseInfoAntTask version = new JReleaseInfoAntTask();
      version.setProject(new Project());
      version.setClassName("test");
      version.setTargetDir(tmpDir);

      try {
         // Additional parameter
         Parameter par1 = new Parameter();
         par1.setName("par1");
         par1.setValue("val1");
         version.addConfiguredParameter(par1);

         // Additional parameter
         Parameter par2 = new Parameter();
         par2.setName("Par2");
         par2.setValue("val2");
         version.addConfiguredParameter(par2);

         version.execute();

         String content = readFile(new File(this.getAbsolutePath("Test.java")));

         // Test additional parameter
         assertTrue(checkStringParameter(content, "par1", "getPar1", "val1"));
         assertTrue(checkStringParameter(content, "par2", "getPar2", "val2"));
      } catch (Exception ex) {
         // Should not occur
         assertTrue(ex.getMessage(), false);
      }

      version.deleteJReleaseInfoFile();
   }

   /**
    * Test accessor method generation
    */
   public void testSetGetParameterType() {
      JReleaseInfoAntTask version = new JReleaseInfoAntTask();
      version.setProject(new Project());
      version.setClassName("test");
      version.setTargetDir(tmpDir);

      try {
         {
            Parameter par1 = new Parameter();
            par1.setName("par1");
            par1.setValue("true");
            par1.setType("boolean");
            version.addConfiguredParameter(par1);
         }
         {
            Parameter par2 = new Parameter();
            par2.setName("par2");
            par2.setValue("true");
            par2.setType("Boolean");
            version.addConfiguredParameter(par2);
         }
         {
            Parameter par3 = new Parameter();
            par3.setName("par3");
            par3.setValue("3");
            par3.setType("int");
            version.addConfiguredParameter(par3);
         }
         {
            Parameter par4 = new Parameter();
            par4.setName("par4");
            par4.setValue("4");
            par4.setType("Integer");
            version.addConfiguredParameter(par4);
         }
         {
            Parameter par5 = new Parameter();
            par5.setName("par5");
            par5.setValue("val5");
            par5.setType("String");
            version.addConfiguredParameter(par5);
         }

         version.execute();

         String content = readFile(new File(this.getAbsolutePath("Test.java")));

         // Test additional parameter
         assertTrue(check_booleanParameter(content, "par1", "isPar1", true));
         assertTrue(checkBooleanParameter(content, "par2", "isPar2", "Boolean.TRUE"));
         assertTrue(checkIntParameter(content, "par3", "getPar3", 3));
         assertTrue(checkIntegerParameter(content, "par4", "getPar4", new Integer(4)));
         assertTrue(checkStringParameter(content, "par5", "getPar5", "val5"));
      } catch (Exception ex) {
         // Should not occur
         assertTrue(ex.getMessage(), false);
      }

      version.deleteJReleaseInfoFile();
   }

   /**
    * Test accessor method for buildnum
    */
   public void testBuildNum() {
      JReleaseInfoAntTask version = new JReleaseInfoAntTask();
      File file = new File(this.getAbsolutePath("testbuild.num"));

      if (file.exists()) {
         file.delete();
      }

      version.setProject(new Project());
      version.setClassName("test");
      version.setTargetDir(tmpDir);
      version.setBuildNumFile(file.getAbsolutePath());

      try {
         version.execute();

         String content = readFile(new File(this.getAbsolutePath("Test.java")));
         assertTrue(checkIntParameter(content, "buildNumber", "getBuildNumber", 1));

         version.execute();
         content = readFile(new File(this.getAbsolutePath("Test.java")));
         assertTrue(checkIntParameter(content, "buildNumber", "getBuildNumber", 2));

         version.execute();
         content = readFile(new File(this.getAbsolutePath("Test.java")));
         assertTrue(checkIntParameter(content, "buildNumber", "getBuildNumber", 3));

         version.execute();
         content = readFile(new File(this.getAbsolutePath("Test.java")));
         assertTrue(checkIntParameter(content, "buildNumber", "getBuildNumber", 4));
      } catch (Exception ex) {
         // Should not occur
         assertTrue(ex.getMessage(), false);
      }

      if (file.exists()) {
         file.delete();
      }

      version.deleteJReleaseInfoFile();
   }

   /**
    * Test Main method
    */
   public void testMainMethod() {
      JReleaseInfoAntTask version = new JReleaseInfoAntTask();
      version.setProject(new Project());
      version.setTargetDir(tmpDir);
      version.setClassName("Test");
      version.setWithViewer(true);
      try {
         version.execute();

         String content = readFile(new File(this.getAbsolutePath("Test.java")));
         assertTrue("JReleaseInfoViewer class not found", content.indexOf("class JReleaseInfoViewer") != -1);

         assertTrue("main() not found", content.indexOf("public static void main") != -1);

      } catch (Exception ex) {
         // Should not occur
         assertTrue(ex.getMessage(), false);
      }

      version.deleteJReleaseInfoFile();
   }

   /**
    * Test attribute set.
    */
   public void testSetVersion() {
      JReleaseInfoAntTask task = new JReleaseInfoAntTask();

      String version = "V1";
      task.setVersion(version);
      JReleaseInfoProperty propV = (JReleaseInfoProperty) task.props.get("Version");
      assertEquals(version, propV.getValue());

      String project = "Proj";
      task.setProject(project);
      JReleaseInfoProperty propP = (JReleaseInfoProperty) task.props.get("Project");
      assertEquals(project, propP.getValue());

   }

   /**
    * Test attribute set.
    */
   public void testSetBuildNumPropertyName() {

      String name = "MyBuildNum";

      JReleaseInfoAntTask task = new JReleaseInfoAntTask();
      task.setProject(new Project());

      task.setClassName("test");
      task.setTargetDir(tmpDir);
      task.setBuildNumProperty(name);

      File file = new File(this.getAbsolutePath("testbuild.num"));
      if (file.exists()) {
         file.delete();
      }

      try {

         // BuildNumFile not set
         {
            task.execute();

            // Field set ?
            assertEquals(name, task.buildNumPropertyName);

            // Property set ?
            assertNull(task.getProject().getProperty(name));
         }

         // BuildNumFile set
         {
            task.setBuildNumFile(file.getAbsolutePath());
            task.execute();

            // Field set ?
            assertEquals(name, task.buildNumPropertyName);

            // Property set ?
            assertEquals("1", task.getProject().getProperty(name));
         }

      } catch (Exception ex) {
         // Should not occur
         assertTrue(ex.getMessage(), false);
      }

      task.deleteJReleaseInfoFile();

      if (file.exists()) {
         file.delete();
      }

   }

   /**
    * Test attribute set (increment).
    */
   public void testSetBuildNumIncrement() {

      JReleaseInfoAntTask task = new JReleaseInfoAntTask();
      task.setProject(new Project());
      task.setClassName("test");
      task.setTargetDir(tmpDir);

      File file = new File(this.getAbsolutePath("testbuild.num"));
      if (file.exists()) {
         file.delete();
      }

      try {
         task.setBuildNumFile(file.getAbsolutePath());

         // Initial value
         task.execute();
         assertEquals("1", getPropertyFromFile(file, "build.num.last"));

         // Default increment
         task.execute();
         assertEquals("2", getPropertyFromFile(file, "build.num.last"));

         // Increment set to 2
         task.setBuildNumIncrement(2);
         task.execute();
         assertEquals("4", getPropertyFromFile(file, "build.num.last"));

         // Increment set to 0
         task.setBuildNumIncrement(0);
         task.execute();
         assertEquals("4", getPropertyFromFile(file, "build.num.last"));

      } catch (Exception ex) {
         // Should not occur
         assertTrue(ex.getMessage(), false);
      }

      task.deleteJReleaseInfoFile();

      if (file.exists()) {
         file.delete();
      }

   }

   /**
    * Test BaseDir set.
    */
   public void testSetBaseDir() {

      JReleaseInfoAntTask task = new JReleaseInfoAntTask();
      task.setProject(new Project());
      {
         String classname = "test1";
         task.setClassName(classname);
         task.setTargetDir(tmpDir);
         String ret = tmpDir + classname + ".java";
         File file = task.getJReleaseInfoFile();
         assertEquals(ret, file.getAbsolutePath());
      }

   }

   /**
    * Test IsRelativePath.
    */
   public void testIsRelativePath() {

      
      JReleaseInfoAntTask task = new JReleaseInfoAntTask();
      task.setProject(new Project());
      assertEquals(true,  task.isRelativePath("."));
      assertEquals(true,  task.isRelativePath("./x"));
      assertEquals(true,  task.isRelativePath("../x"));
      assertEquals(true,  task.isRelativePath("aaa/bbb.c"));
//      assertEquals(false,  task.isRelativePath("/aaa/bbb.c"));
//      assertEquals(false,  task.isRelativePath("\\aaa/bbb.c"));
      assertEquals(false, task.isRelativePath("C:\\xx"));
      assertEquals(false, task.isRelativePath("//localhost/abc"));

   }

   private String getPropertyFromFile(File file, String propertyName) {

      Properties props = new Properties();

      try {
         FileInputStream fis = new FileInputStream(file);
         props.load(fis);
         fis.close();
         return props.getProperty(propertyName);
      } catch (IOException ex) {
         return null;
      }
   }
}