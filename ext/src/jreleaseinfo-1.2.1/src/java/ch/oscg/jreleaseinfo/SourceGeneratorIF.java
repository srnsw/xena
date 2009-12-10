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

import java.util.Map;


/**
 * Source Generator Interface defines the
 * method an implementation must provide in
 * order to be called as Plug-In from the BuilInfoBean
 * class.
 * @author Thomas Cotting, Tangarena Engineering AG, Luzern
 * @version $Revision$ ($Date$ / $Author$)
 */
public interface SourceGeneratorIF {
   /**
    * Set the packagename.
    * @param packageName
    */
   public abstract void setPackageName(String packageName);

   /**
    * Set the classname.
    * @param className
    */
   public abstract void setClassName(String className);

   /**
    * Set the property map.
    * @param property map
    */
   public abstract void setProperties(Map props);

   /**
    * Utility method to write the class java code to a String.
    * @return the created java class in a string
    */
   public abstract String createCode();
}
