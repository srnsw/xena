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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * The SourceGenerator is a simple implementation of the
 * SourceGeneratorIF interface. In this class the code to be
 * generated is hard coded.<br>
 * This implementation adds code which shows the
 * build information in a frame as default main-class of
 * the jar file.<br>
 * This is usefull for libraries, which do not have a main-class
 * definition in the manifest.
 *
 * @author Thomas Cotting, Tangarena Engineering AG, Luzern
 * @version $Revision$ ($Date$ / $Author$)
 */
public class SourceGeneratorApp extends SourceGenerator {
   /** Include filename. */
   private String filename = "ch/oscg/jreleaseinfo/anttask/JReleaseInfoViewer.txt";

   /**
    * Default constructor.
    */
   public SourceGeneratorApp() {
   }

   /**
    * Utility method to write the class java code to a String.
    * This methods overwrites the method from SourceGenerator,
    * adding code through createViewerCall().
    *
    * @return the created java class in a string
    */
   public String createCode() {
      StringBuffer buf = new StringBuffer();

      createClassInfoHeader(buf);

      createViewerClass(buf);

      createClassHeader(buf);

      createMethods(buf);

      createViewerCall(buf);

      createClassFooter(buf);

      writeln(buf);

      return buf.toString();
   }

   /**
    * Utility method to write a the code for the viewer class.
    *
    * @param buf StringBuffer to write into
    */
   protected void createViewerClass(StringBuffer buf) {
      ClassLoader cl = SourceGeneratorApp.class.getClassLoader();
      InputStream fis = cl.getResourceAsStream(filename);
      InputStreamReader isr = new InputStreamReader(fis);
      try {
         BufferedReader br = new BufferedReader(isr);

         String line = null;
         while ((line = br.readLine()) != null) {
            buf.append(line + "\n");
         }

         isr.close();
      } catch (Exception e) {
         buf.append("// File (" + filename + ") could not be read");
      }
   }

   /**
    * Utility method to write a call to the JReleaseInfoViewer.
    *
    * @param buf StringBuffer to write into
    */
   protected void createViewerCall(StringBuffer buf) {
      buf.append(INDENT);
      writeln(buf, "public static void main(String[] args) throws Exception {");
      buf.append(INDENT);
      writeln(buf,
         INDENT + "JReleaseInfoViewer frame = new JReleaseInfoViewer(" + className + ".class);");
      buf.append(INDENT);
      writeln(buf, INDENT + "frame.setVisible(true);");
      buf.append(INDENT);
      writeln(buf, "}");
   }
}
