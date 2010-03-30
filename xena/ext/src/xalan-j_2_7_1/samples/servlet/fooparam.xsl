<?xml version="1.0"?>

  <!--
   * Licensed to the Apache Software Foundation (ASF) under one
   * or more contributor license agreements. See the NOTICE file
   * distributed with this work for additional information
   * regarding copyright ownership. The ASF licenses this file
   * to you under the Apache License, Version 2.0 (the  "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *     http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:param name="param1" select="'default value'"/>
  <xsl:template match="doc">
    <html>
      <head><title>Stylesheet parameter</title></head>
      <body>
        <h2>XML source</h2>
          <p><xsl:value-of select="."/></p>
        <h2>Stylesheet parameter</h2>
          <p>The param1 stylesheet parameter has been set to <xsl:value-of select="$param1"/>.</p>
      </body>
     </html>          
  </xsl:template>
</xsl:stylesheet>
