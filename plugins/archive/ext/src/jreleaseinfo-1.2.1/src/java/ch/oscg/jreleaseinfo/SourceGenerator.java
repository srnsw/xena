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

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * The SourceGenerator is a simple implementation of the
 * SourceGeneratorIF interface. In this class the code to be
 * generated is hard coded.<br>
 * Other implementation may provide extensions (like SourceGeneratorApp)
 * or other mechanism (like using a template engine, etc).
 *
 * @author Thomas Cotting, Tangarena Engineering AG, Luzern
 * @version $Revision$ ($Date$ / $Author$)
 */
public class SourceGenerator implements SourceGeneratorIF {
   /** Linebreak to apply to sourcefile. */
   final static String LINE_BREAK = "\n";

   /** Indentation. */
   final static String INDENT = "   ";

   /** ClassName of version file to be created. */
   protected String className = "";

   /** PackageName of version file to be created, 'null' means default package. */
   protected String packageName = "";

   /** Membervariables for created class. */
   protected Map props = new HashMap();

   /** Date for test purposes. */
   protected Date newDate = null;

   /**
    * Default constructor.
    */
   public SourceGenerator() {
   }

   /**
    * Helper routine  for junit test of date.
    * @return new Date
    */
   private Date createDate() {
      if (newDate == null) {
         return new Date();
      }

      return newDate;
   }

   /**
    * Set the packagename.
    * @param packageName
    */
   public void setPackageName(String packageName) {
      this.packageName = packageName;
   }

   /**
    * Set the classname.
    * @param className
    */
   public void setClassName(String className) {
      this.className = className;
   }

   /**
    * Set the property map.
    * @param property map
    */
   public void setProperties(Map props) {
      this.props = props;
   }

   /**
    * Utility method to write the class java code to a String.
    *
    * @return the created java class in a string
    */
   public String createCode() {
      StringBuffer buf = new StringBuffer();

      createClassInfoHeader(buf);

      createClassHeader(buf);

      createMethods(buf);

      createClassFooter(buf);

      writeln(buf);

      return buf.toString();
   }

   /**
    * Utility method to write the class header code to a StringBuffer.
    *
    * @param buf StringBuffer to write into
    */
   protected void createClassInfoHeader(StringBuffer buf) {
      writeln(buf, "/* Created by JReleaseInfo AntTask from Open Source Competence Group */");
      writeln(buf, "/* Creation date " + createDate().toString() + " */");
      if (this.packageName != null) {
         writeln(buf, "package " + this.packageName + ";");
         writeln(buf, "");
      }

      writeln(buf, "import java.util.Date;");
   }

   /**
    * Utility method to write the class header code to a StringBuffer.
    *
    * @param buf StringBuffer to write into
    */
   protected void createClassHeader(StringBuffer buf) {
      writeln(buf);
      writeln(buf, "/**");
      writeln(buf, " * This class provides information gathered from the build environment.");
      writeln(buf, " * ");
      writeln(buf, " * @author JReleaseInfo AntTask");
      writeln(buf, " */");
      writeln(buf, "public class " + this.className + " {");
      writeln(buf);
   }

   /**
    * Utility method to write the class footer code to a StringBuffer.
    *
    * @param buf StringBuffer to write into
    */
   protected void createClassFooter(StringBuffer buf) {
      buf.append("}");
   }

   /**
    * Utility method to write the class method code to a StringBuffer.
    *
    * @param buf StringBuffer to write into
    */
   protected void createMethods(StringBuffer buf) {
      // Write actual build date
      writeDateMethod(buf, JReleaseInfoProperty.TYPE_OBJ_DATE, JReleaseInfoBean.PROPNAME_BUILDDATE,
         createDate());

      Set keys = this.props.keySet();
      Iterator it = keys.iterator();

      while (it.hasNext()) {
         String key = (String)it.next();

         // Key empty continue to next
         if (key.length() == 0) {
            continue;
         }

         // Uppercase first character of property
         JReleaseInfoProperty biProp = (JReleaseInfoProperty)this.props.get(key);

         writeObjectMethod(buf, biProp.getType(), biProp.getName(), biProp.getValue().toString());
      }
   }

   /**
    * Utility method to write java code for an object method to a
    * StringBuffer.
    *
    * @param buf StringBuffer to write into
    * @param type must be one of the TYPE_OBJ_xxx Strings
    * @param name of Property
    * @param value ReturnValue of Property
    */
   protected void writeObjectMethod(final StringBuffer buf, String type, String name, String value) {
      String uName = JReleaseInfoUtil.upperCaseFirstLetter(name);
      String lName = JReleaseInfoUtil.lowerCaseFirstLetter(name);
      writeln(buf);

      if (type.equals(JReleaseInfoProperty.TYPE_PRI_BOOLEAN)) {
         String strVal = value.toString();
         writeMethodDeclaration(buf, type, "is" + uName, lName, strVal, strVal);
      } else if (type.equals(JReleaseInfoProperty.TYPE_PRI_INT)) {
         String strVal = value.toString();
         writeMethodDeclaration(buf, type, "get" + uName, lName, strVal, strVal);
      } else if (type.equals(JReleaseInfoProperty.TYPE_OBJ_BOOLEAN)) {
         String val = value.equalsIgnoreCase("true") ? "Boolean.TRUE" : "Boolean.FALSE";
         writeMethodDeclaration(buf, type, "is" + uName, lName, val, val);
      } else if (type.equals(JReleaseInfoProperty.TYPE_OBJ_STRING)) {
         String val = "\"" + value + "\"";
         writeObjectDeclaration(buf, type, lName, val);
         writeMethodDeclaration(buf, type, "get" + uName, lName, lName, val);
      } else {
         writeObjectDeclaration(buf, type, lName, value);
         writeMethodDeclaration(buf, type, "get" + uName, lName, lName, value);
      }

      writeln(buf);
   }

   /**
    * Utility method to write java code for an date method to a
    * StringBuffer.
    *
    * @param buf StringBuffer to write into
    * @param type must be TYPE_OBJ_DATE
    * @param name of Property
    * @param value ReturnValue of Property
    */
   protected void writeDateMethod(final StringBuffer buf, String type, String name, Date date) {
      String uName = JReleaseInfoUtil.upperCaseFirstLetter(name);
      String lName = JReleaseInfoUtil.lowerCaseFirstLetter(name);
      writeln(buf);
      String val = date.getTime() + "L";
      writeObjectDeclaration(buf, type, lName, val);
      writeMethodDeclaration(buf, type, "get" + uName, lName, lName, date.toString());
      writeln(buf);
   }

   /**
    * Utility method to write a method declaration
    * @param buf StringBuffer to write into
    * @param type String return type
    * @param type String property name used for generation of methodName
    */
   protected void writeObjectDeclaration(final StringBuffer buf, String type, String lName,
      String value) {
      buf.append(INDENT);
      writeln(buf, "/** " + lName + " (set during build process to " + value + "). */");
      buf.append(INDENT);
      writeln(buf, "private static " + type + " " + lName + " = new " + type + "(" + value + ");");
      writeln(buf);
   }

   /**
    * Utility method to write a method declaration
    * @param buf StringBuffer to write into
    * @param type String return type
    * @param type String property name used for generation of methodName
    */
   protected void writeMethodDeclaration(final StringBuffer buf, String type, String methodName,
      String name, String value, String preset) {
      buf.append(INDENT);
      writeln(buf, "/**");
      buf.append(INDENT);
      writeln(buf, " * Get " + name + " (set during build process to " + preset + ").");
      buf.append(INDENT);
      writeln(buf, " * @return " + type + " " + name);
      buf.append(INDENT);
      writeln(buf, " */");
      buf.append(INDENT);
      buf.append("public static final " + type + " " + methodName + "() ");
      buf.append("{ return " + value + "; }");
      writeln(buf);
   }

   /**
    * Utility method to write a line to the StringBuffer and append a line
    * break.
    *
    * @param buf StringBuffer to write into
    * @param line String to append to buf
    */
   protected void writeln(final StringBuffer buf, String line) {
      buf.append(line);
      writeln(buf);
   }

   /**
    * Utility method to write an empty line to the StringBuffer.
    *
    * @param buf StringBuffer to write into
    */
   protected void writeln(final StringBuffer buf) {
      buf.append(LINE_BREAK);
   }
}
