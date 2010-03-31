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

import java.io.IOException;


/**
 * BuildNumberHandler interface.
 * BuildNumbers can be get from a file, a web service, etc
 *
 * @author Thomas Cotting, Tangarena Engineering AG, Luzern
 * @version $Revision$ ($Date$ / $Author$)
 */
public interface BuildNumberHandlerIF {
   /** Property name for Build Number. */
   public static final String PROPNAME_BUILDNUM = "buildNumber";

   /**
    * Get the JReleaseInfoProperty with the updated buildNumber.
    * @param buildNumIncrement increment to apply, may also be 0.
    * @return JReleaseInfoProperty
    */
   public abstract JReleaseInfoProperty getUpdatedBuildNumberProperty(int buildNumIncrement)
      throws IllegalArgumentException, IOException;

}
