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
package ch.oscg.jreleaseinfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Properties;


/**
 * BuildNumberHandler getting the number from a file.
 *
 * @author Thomas Cotting, Tangarena Engineering AG, Luzern
 * @version $Revision$ ($Date$ / $Author$)
 */
public class BuildNumberHandler implements BuildNumberHandlerIF {
   
   /** Initial build number */
   private static final String INITIAL_BUILDNUM = "1";
   
   /** Property Name in buildnum file. */
   public static final String PROPERTY_BUILDNUM_LAST = "build.num.last";

   /**
    * FileName of the file containing the build number, 'null' means no build
    * number property.
    */
   private String buildNumFileName = null;


   /**
    * Main method to keep track of the build number.
    * @param inc increment
    * @return update success true/false
    *
    * @throws IOException on file errors
    * @throws IllegalArgumentException on wrong parameters
    */
   protected String updateBuildNumber(int buildNumIncrement) throws IllegalArgumentException, IOException {
      // buildNumFileName == null is legal und means, 
      // no property must be returned
      if (buildNumFileName == null) {
         return null;
      }

      // check parameter
      if (!JReleaseInfoUtil.isValidNameString(buildNumFileName)) {
         throw new IllegalArgumentException("Invalid name for buildfile (" + buildNumFileName + ")");
      }

      File file = new File(buildNumFileName);
      Properties props = new Properties();

      // Only store properties when they changed
      boolean doStoreProps = false;
      try {
         FileInputStream fis = new FileInputStream(file);
         props.load(fis);
         fis.close();

         String strBuildNumber = props.getProperty(PROPERTY_BUILDNUM_LAST);
         int iLast = Integer.parseInt(strBuildNumber);
         String strNewBuildNumber = "" + (iLast + buildNumIncrement);
         props.setProperty(PROPERTY_BUILDNUM_LAST, strNewBuildNumber);
         if (buildNumIncrement != 0) {
            doStoreProps = true;
         }
      } catch (IOException ex) {
         props.setProperty(PROPERTY_BUILDNUM_LAST, INITIAL_BUILDNUM);
         doStoreProps = true;
      }

      // Store the changed properties 
      if (doStoreProps) {
         storeProperties(file, props);
      }

      return props.getProperty(PROPERTY_BUILDNUM_LAST);
   }

   /**
    * Store the property file.
    * @param file File to store the properties
    * @param props Properties to store
    * @throws FileNotFoundException
    * @throws IOException
    */
   private void storeProperties(File file, Properties props) throws FileNotFoundException, IOException {
      File parent = file.getParentFile();

      if (parent != null) {
         parent.mkdirs();
      }

      FileOutputStream fos = new FileOutputStream(file);
      props.store(fos, "ANT Task: " + this.getClass().getName());
      fos.close();
   }


   /**
    * Get the JReleaseInfoProperty with the updated buildNumber.
    * @param buildNumIncrement  int
    * @return JReleaseInfoProperty
    */
   public JReleaseInfoProperty getUpdatedBuildNumberProperty(int buildNumIncrement) throws IllegalArgumentException, IOException {
      String updatedBuildNum = updateBuildNumber(buildNumIncrement);
      if (updatedBuildNum == null) {
         return null;
      }

      JReleaseInfoProperty biProp = new JReleaseInfoProperty();
      biProp.setName(PROPNAME_BUILDNUM);
      biProp.setType(JReleaseInfoProperty.TYPE_PRI_INT);
      biProp.setValue(updatedBuildNum);

      return biProp;
   }

   /**
    * Set method for the fileName containing the buildnumber.
    *
    * @param fileName for build number
    */
   public void setBuildNumFile(String fileName) {
      this.buildNumFileName = fileName;
   }

}
