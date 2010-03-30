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


/**
 * Utility class for JReleaseInfoAntTask task. All methods are defined static so
 * they can be called without creating an instance.
 *
 * @author Thomas Cotting, Tangarena Engineering AG, Luzern
 * @version $Revision$ ($Date$ / $Author$)
 */
public class JReleaseInfoUtil {
   /**
    * Utility method to append slash to pathelement.
    *
    * @param pathElement which should be checked
    *
    * @return the processed pathelement
    */
   public static String getPathElement(String pathElement) {
      if (pathElement == null) {
         return "";
      } else if (pathElement == "") {
         return "";
      } else if (pathElement.endsWith("/")) {
         return pathElement;
      } else if (pathElement.endsWith("\\")) {
         return pathElement;
      } else {
         return pathElement + '/';
      }
   }

   /**
    * Utility method to change the first letter of a string to uppercase.
    *
    * @param str String to process
    *
    * @return str where first letter is uppercase
    */
   public static String upperCaseFirstLetter(String str) {
      if ((str == null) || (str.length() == 0)) {
         return str;
      } else if (str.length() == 1) {
         return str.toUpperCase();
      } else {
         //if (str.length() > 1)
         return str.substring(0, 1).toUpperCase() + str.substring(1);
      }
   }

   /**
    * Utility method to change the first letter of a string to lowercase.
    *
    * @param str String to process
    *
    * @return str where first letter is lowercase
    */
   public static String lowerCaseFirstLetter(String str) {
      if ((str == null) || (str.length() == 0)) {
         return str;
      } else if (str.length() == 1) {
         return str.toLowerCase();
      } else {
         // if (str.length() > 1)
         return str.substring(0, 1).toLowerCase() + str.substring(1);
      }
   }

   /**
    * Utility method to check if a String value is a valid boolean value.
    *
    * @param val String value expressing a boolean
    *
    * @return true if val==true|TRUE|True...
    */
   public static boolean checkIsBoolean(String val) {
      return (val.equalsIgnoreCase("TRUE") || val.equalsIgnoreCase("FALSE"));
   }

   /**
    * Utility method to check the validity of string for name. Note: Only some
    * common mistakes are checked.
    *
    * @param name String to check
    *
    * @return true when not null and not empty
    */
   public static boolean isValidNameString(String name) {
      return ((name != null) && (name != ""));
   }

   /**
    * Utility method to check the validity of string for a class name. Note:
    * Only some common mistakes are checked.
    *
    * @param name String to check
    *
    * @return true when valid name string and no dot
    */
   public static boolean isValidClassNameString(String name) {
      return (isValidNameString(name) && (name.indexOf(".") == -1));
   }

   /**
    * Utility method to check the validity of string for a package name. Note:
    * Only some common mistakes are checked.
    *
    * @param name String to check (empty string is allowed)
    *
    * @return true/false
    */
   public static boolean isValidPackageNameString(String name) {
      return (name == null) ||
      ((name != "") && (name.indexOf("/") == -1) && (name.indexOf("\\") == -1) &&
      ((name.indexOf(".") == -1) ||
      ((name.indexOf(".") != 0) && (name.indexOf(".") != (name.length() - 1)))));
   }
}
