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

import junit.framework.TestCase;

/**
 * Testing the methods in the Util class.
 *
 * @author Thomas Cotting, Tangarena Engineering AG, Luzern
 * @version $Revision$ ($Date$ / $Author$)
 */
public class JReleaseInfoUtilTest extends TestCase {

   /**
    * Constructor for JReleaseInfoUtilTest.
    * @param arg0
    */
   public JReleaseInfoUtilTest(String arg0) {
      super(arg0);
   }

   public void testGetPathElement() {
      assertEquals("", JReleaseInfoUtil.getPathElement(null));
      assertEquals("", JReleaseInfoUtil.getPathElement(""));
      assertEquals("path/", JReleaseInfoUtil.getPathElement("path"));
      assertEquals("path/", JReleaseInfoUtil.getPathElement("path/"));
      assertEquals("path\\", JReleaseInfoUtil.getPathElement("path\\"));
   }

   public void testUpperCaseFirstLetter() {
      String str = JReleaseInfoUtil.upperCaseFirstLetter("");
      assertEquals("Empty string", "", str);

      str = JReleaseInfoUtil.upperCaseFirstLetter("a");
      assertEquals("One character string", "A", str);

      str = JReleaseInfoUtil.upperCaseFirstLetter("A");
      assertEquals("One character string", "A", str);

      str = JReleaseInfoUtil.upperCaseFirstLetter("opensource");
      assertEquals("More character string", "Opensource", str);

      str = JReleaseInfoUtil.upperCaseFirstLetter("OPENSOURCE");
      assertEquals("More character string", "OPENSOURCE", str);

      str = JReleaseInfoUtil.upperCaseFirstLetter("1234");
      assertEquals("numeric string", "1234", str);

      str = JReleaseInfoUtil.upperCaseFirstLetter(null);
      assertNull("null string", str);
   }

   public void testLowerCaseFirstLetter() {
      String str = JReleaseInfoUtil.lowerCaseFirstLetter("");
      assertEquals("Empty string", "", str);

      str = JReleaseInfoUtil.lowerCaseFirstLetter("a");
      assertEquals("One character string", "a", str);

      str = JReleaseInfoUtil.lowerCaseFirstLetter("A");
      assertEquals("One character string", "a", str);

      str = JReleaseInfoUtil.lowerCaseFirstLetter("opensource");
      assertEquals("More character string", "opensource", str);

      str = JReleaseInfoUtil.lowerCaseFirstLetter("OPENSOURCE");
      assertEquals("More character string", "oPENSOURCE", str);

      str = JReleaseInfoUtil.lowerCaseFirstLetter("1234");
      assertEquals("numeric string", "1234", str);

      str = JReleaseInfoUtil.lowerCaseFirstLetter(null);
      assertNull("null string", str);
   }

   public void testCheckIsBoolean() {
      assertTrue("Is Boolean (true) should return true", JReleaseInfoUtil.checkIsBoolean("true"));
      assertTrue("Is Boolean (True) should return true", JReleaseInfoUtil.checkIsBoolean("True"));
      assertTrue("Is Boolean (tRue) should return true", JReleaseInfoUtil.checkIsBoolean("tRue"));
      assertFalse("Is Boolean (t) should return false",   JReleaseInfoUtil.checkIsBoolean("t"));

      assertTrue("Is Boolean (false) should return true", JReleaseInfoUtil.checkIsBoolean("false"));
      assertTrue("Is Boolean (False) should return true", JReleaseInfoUtil.checkIsBoolean("False"));
      assertTrue("Is Boolean (fAlse) should return true", JReleaseInfoUtil.checkIsBoolean("fAlse"));
      assertFalse("Is Boolean (f) should return false",    JReleaseInfoUtil.checkIsBoolean("f"));
   }

   /**
    * Note: Utility-Method does only reject empty or null string
    */
   public void testIsValidNameString() {
      assertFalse("Is NameOk () should return false",     JReleaseInfoUtil.isValidNameString(""));
      assertFalse("Is NameOK (null) should return false", JReleaseInfoUtil.isValidNameString(null));
      assertTrue("Is Name (123) should return true", JReleaseInfoUtil.isValidNameString("123"));
   }

   /**
    * Note: Method does reject when with dot
    */
   public void testIsValidClassNameString() {
      assertFalse("Is ClassNameOk () should return false",      JReleaseInfoUtil.isValidClassNameString(""));
      assertFalse("Is ClassNameOk (null) should return false",  JReleaseInfoUtil.isValidClassNameString(null));
      assertFalse("Is ClassNameOk (abc.d) should return false", JReleaseInfoUtil.isValidClassNameString("abc.d"));
      assertTrue("Is ClassNameOk (123) should return true",    JReleaseInfoUtil.isValidClassNameString("123"));
   }

   /**
    * Note: Method does reject when with dot
    */
   public void testIsValidPackageNameString() {
      // not yet done
   }

}
