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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Parameter;

import ch.oscg.jreleaseinfo.BuildNumberHandler;
import ch.oscg.jreleaseinfo.BuildNumberHandlerIF;
import ch.oscg.jreleaseinfo.JReleaseInfoBean;
import ch.oscg.jreleaseinfo.JReleaseInfoProperty;
import ch.oscg.jreleaseinfo.SourceGenerator;
import ch.oscg.jreleaseinfo.SourceGeneratorApp;


/**
 * This Task creates a new java source file with embedded informationen about
 * the current build.
 *
 * <p>
 * There will be some standard arguments like Version, BuildDate, as well as
 * user defined properties.
 * </p>
 *
 * <p>
 * The java class will provide final get methods to access these informations.
 * </p>
 *
 * @author Thomas Cotting, Tangarena Engineering AG, Luzern
 * @version $Revision$ ($Date$ / $Author$)
 */
public class JReleaseInfoAntTask extends Task {
   /** Membervariables for created class. */
   protected Map props = new HashMap();

   /** Generator for build info source file. */
   protected JReleaseInfoBean creator = new JReleaseInfoBean();

   /** Build number handler */
   protected BuildNumberHandlerIF buildNumberHandler = new BuildNumberHandler();

   /** Flag for additional viewer code */
   protected boolean isWithViewer = false;

   /** Build number property name */
   protected String buildNumPropertyName = null;

   /** Increment for build number. Default is 1, 0 could be used to prevent upcounting */
   private int buildNumIncrement = 1;

   /**
    * This is the standard execute() method from Task which must be
    * overwritten.
    * @throws BuildException on wrong argument or IO error
    */
   public void execute() throws BuildException {
      try {
         JReleaseInfoProperty prop = buildNumberHandler.getUpdatedBuildNumberProperty(buildNumIncrement);

         if (prop != null) {
            props.put(prop.getName(), prop);
            if (buildNumPropertyName != null) {
               getProject().setNewProperty(buildNumPropertyName, prop.getValue());
            }
         }

         // now create version file
         if (isWithViewer) {
            creator.execute(props, new SourceGeneratorApp());
         }
         else {
            creator.execute(props, new SourceGenerator());
         }
      } catch (Exception ex) {
         throw new BuildException(ex);
      }
   }

   /**
    * Set method for any property using the ant type Parameter.
    *
    * <p>
    * Note: if you define the type, then only use one of these (if you do not
    * define the type, String will be used):
    * </p>
    *
    * <ul>
    * <li>
    * boolean
    * </li>
    * <li>
    * int
    * </li>
    * <li>
    * String
    * </li>
    * <li>
    * Boolean
    * </li>
    * <li>
    * Integer
    * </li>
    * </ul>
    *
    *
    * @param prop Ant type Parameter
    */
   public void addConfiguredParameter(Parameter prop) {
      JReleaseInfoProperty p = new JReleaseInfoProperty();
      p.setName(prop.getName());

      String type = prop.getType();

      if (type == null) {
         p.setType(JReleaseInfoProperty.TYPE_OBJ_STRING);
      } else {
         p.setType(type);
      }

      p.setValue(prop.getValue());
      props.put(p.getName(), p);
   }

	/**
	 * Set the build number property name.
	 *
	 * @param buildNumPropertyName Name of property.
	 */
    public void setBuildNumProperty(String buildNumPropertyName) {
       this.buildNumPropertyName = buildNumPropertyName;
    }

   /**
    * Set method for the fileName containing the buildnumber.
    * Note: The fileName should be set with an absolute path.
    *
    * @param fileName Name of buildnumber file
    */
    public void setBuildNumFile(String fileName) {
       if (buildNumberHandler instanceof BuildNumberHandler) {
          ((BuildNumberHandler)buildNumberHandler).setBuildNumFile(fileName);
          if (isRelativePath(fileName)) {
             this.log("BuildNum File should have an absolute path!", Project.MSG_WARN);
          }
       }
    }

   /**
    * Set method for the buildNumber increment.
    *
    * @param increment int
    */
   public void setBuildNumIncrement(int inc) {
      this.buildNumIncrement = inc;
   }

   /**
    * Set method for the classname of the JReleaseInfo file to be created.
    *
    * @param className of the JReleaseInfo
    */
   public void setClassName(String className) {
      creator.setClassName(className);
   }

   /**
    * Set method for the version-number.
    *
    * @param version info
    */
   public void setVersion(String version) {
      String name = "Version";
      this.props.put(name,
         new JReleaseInfoProperty(name, JReleaseInfoProperty.TYPE_OBJ_STRING, version));
   }

   /**
    * Set method for the name of the project
    *
    * @param version info
    */
   public void setProject(String project) {
      String name = "Project";
      this.props.put(name,
         new JReleaseInfoProperty(name, JReleaseInfoProperty.TYPE_OBJ_STRING, project));
   }

   /**
    * Set method for the package of the JReleaseInfo class to be created.
    *
    * @param packageName of JReleaseInfo class
    */
   public void setPackageName(String packageName) {
      creator.setPackageName(packageName);
   }

   /**
    * Set method for target directory where the JReleaseInfo file should be created.
    * Note: The targetDir should be set as an absolute path.
    * @param targetDir of JReleaseInfo file
    */
   public void setTargetDir(String targetDir) {
      if (isRelativePath(targetDir)) {
         this.log("Target directory should be defined absolute!", Project.MSG_WARN);
      }
      creator.setTargetDir(targetDir);
   }


   /**
    * Utility method to delete the JReleaseInfo file (for test purposes).
    */
   public void deleteJReleaseInfoFile() {
      creator.deleteJReleaseInfoFile();
   }

   /**
    * Utility method to check the JReleaseInfo file (for test purposes).
    * @return file with JReleaseInfo
    */
   public File getJReleaseInfoFile() {
      return creator.createJReleaseInfoFile();
   }


   /**
    * Set method for isWithViewer flag
    *
    * @param isWithViewer flag
    */
   public void setWithViewer(boolean isWithViewer) {
      this.isWithViewer = isWithViewer;
   }


   /**
    * Utility method to check if a path is relative.
    * 
    * @param path
    * @return true/false
    */
   protected boolean isRelativePath(String path) {
      String pathN = path.replaceAll("\\\\", "/");
      File file = new File(path);
      String absPath = file.getAbsolutePath().replaceAll("\\\\", "/");
      return !pathN.toLowerCase().equals(absPath.toLowerCase());
   }
}
