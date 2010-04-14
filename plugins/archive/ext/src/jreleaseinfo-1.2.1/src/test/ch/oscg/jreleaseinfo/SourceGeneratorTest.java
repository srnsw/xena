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

import java.util.Date;


/**
 * Some JUnit testcases for the SourceGenerator.
 *
 * @author Thomas Cotting, Tangarena Engineering AG, Luzern
 * @version $Revision$ ($Date$ / $Author$)
 */
public class SourceGeneratorTest extends TestCase {
   /**
    * Constructor for SourceGeneratorTest.
    * @param arg0
    */
   public SourceGeneratorTest(String arg0) {
      super(arg0);
   }

   public void testWriteObjectMethod() {
      SourceGenerator gen = new SourceGenerator();
      StringBuffer buf = new StringBuffer();

      gen.writeObjectMethod(buf, JReleaseInfoProperty.TYPE_PRI_INT, "number", "33");
      gen.writeObjectMethod(buf, JReleaseInfoProperty.TYPE_PRI_BOOLEAN, "ready", "true");
      gen.writeObjectMethod(buf, JReleaseInfoProperty.TYPE_OBJ_STRING, "name", "Name");
      gen.writeObjectMethod(buf, JReleaseInfoProperty.TYPE_OBJ_BOOLEAN, "done", "false");
      gen.writeObjectMethod(buf, JReleaseInfoProperty.TYPE_OBJ_INTEGER, "count", "33");
      gen.writeDateMethod(buf, JReleaseInfoProperty.TYPE_OBJ_DATE, "build", new Date());
      System.out.println(buf.toString());
   }

   public void testWriteObjectDeclaration() {
   }

   public void testWriteMethodDeclaration() {
   }
}
