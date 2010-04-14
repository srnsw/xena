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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns:sql="org.apache.xalan.lib.sql.XConnection"
                extension-element-prefixes="sql">

<xsl:output method="html" indent="yes"/>

<xsl:param name="driver" select="'org.apache.derby.jdbc.EmbeddedDriver'"/>
<xsl:param name="datasource" select="'jdbc:derby:sampleDB'"/>
<xsl:param name="query" select="'SELECT * FROM import1'"/>

<xsl:template match="/">
    <xsl:variable name="db" select="sql:new($driver, $datasource)"/>
    <xsl:variable name="table" select='sql:query($db, $query)'/>
    <xsl:copy-of select="$table" />
    <xsl:value-of select="sql:close($db)"/>
</xsl:template>

</xsl:stylesheet>