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
 * Testing the methods in the JReleaseInfoProperty class.
 *
 * @author Thomas Cotting, Tangarena Engineering AG, Luzern
 * @version $Revision$ ($Date$ / $Author$)
 */
public class JReleaseInfoPropertyTest extends TestCase {

   /**
    * Constructor for JReleaseInfoPropertyTest.
    * @param arg0
    */
   public JReleaseInfoPropertyTest(String arg0) {
      super(arg0);
   }

   /*
    * Test for void JReleaseInfoProperty()
    */
   public void testJReleaseInfoProperty() {
      JReleaseInfoProperty prop = new JReleaseInfoProperty();
      assertNull("Default name must be null", prop.getName());
      assertNull("Default type must be null", prop.getType());
      assertNull("Default value must be null", prop.getValue());
   }

   /*
    * Test for void JReleaseInfoProperty(String, String, String)
    */
   public void testJReleaseInfoPropertyStringStringString() {
      String name = "Name";
      String type = "Type";
      String value = "Value";

      JReleaseInfoProperty prop = new JReleaseInfoProperty(name, type, value);
      assertEquals(name,  prop.getName());
      assertEquals(type,  prop.getType());
      assertEquals(value, prop.getValue());
   }

   /**
    * Test set/get of field name
    */
   public void testSetGetName() {
      String name = "Name";
      JReleaseInfoProperty prop = new JReleaseInfoProperty();
      prop.setName(name);

      assertEquals(name,  prop.getName());
      assertNull("Default type must be null", prop.getType());
      assertNull("Default value must be null", prop.getValue());
   }

   /**
    * Test set/get of field type
    */
   public void testSetGetType() {
      String type = "Type";
      JReleaseInfoProperty prop = new JReleaseInfoProperty();
      prop.setType(type);

      assertNull("Default name must be null", prop.getName());
      assertEquals(type,  prop.getType());
      assertNull("Default value must be null", prop.getValue());
   }

   /**
    * Test set/get of field value
    */
   public void testSetGetValue() {
      String value = "Value";
      JReleaseInfoProperty prop = new JReleaseInfoProperty();
      prop.setValue(value);

      assertNull("Default name must be null", prop.getName());
      assertNull("Default type must be null", prop.getType());
      assertEquals(value,  prop.getValue());
   }

}
