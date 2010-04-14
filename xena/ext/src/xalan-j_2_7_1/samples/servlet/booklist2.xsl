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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/TR/REC-html40">
<xsl:output method="html" indent="no"/>

<xsl:template match="/">
 <html>
  <head>
    <meta name="GENERATOR" content="Mozilla/4.73 [en] (Windows NT 5.0; U) [Netscape]"></meta>
  </head>
  <body>
   <table BORDER="0" CELLSPACING="0" CELLPADDING="0" COLS="4" WIDTH="100%" >
    <caption><b><font face="Arial,Helvetica"><font size="-1">My Books</font></font></b></caption>
    <tr>
     <td ALIGN="CENTER" WIDTH="75" BGCOLOR="#666666"><b><font face="Arial,Helvetica"><font color="#FFFFFF"><font size="-2">ISBN</font></font></font></b></td>
     <td ALIGN="CENTER" WIDTH="250" BGCOLOR="#666666"><b><font face="Arial,Helvetica"><font color="#FFFFFF"><font size="-2">Title</font></font></font></b></td>
     <td ALIGN="CENTER" BGCOLOR="#666666"><b><font face="Arial,Helvetica"><font color="#FFFFFF"><font size="-2">Author</font></font></font></b></td>
     <td ALIGN="CENTER" BGCOLOR="#666666"><b><font face="Arial,Helvetica"><font color="#FFFFFF"><font size="-2">Published</font></font></font></b></td>
    </tr>
    <xsl:apply-templates/>
   </table>
 </body>
 </html>
</xsl:template>

<xsl:template match="BOOK">
 <tr>
  <td BGCOLOR="#CCCCCC"><font face="Arial,Helvetica"><font size="-1"> <xsl:value-of select="ISBN"/> </font></font></td>
  <td BGCOLOR="#EBEBEB"><font face="Arial,Helvetica"><font size="-1"> <xsl:value-of select="TITLE"/> </font></font></td>
  <td BGCOLOR="#CCCCCC"><font face="Arial,Helvetica"><font size="-1"> <xsl:value-of select="AUTHOR"/> </font></font></td>
  <td ALIGN="left" BGCOLOR="#EBEBEB"><font face="Arial,Helvetica"><font size="-1"> <xsl:value-of select="YEAR-PUBLISHED"/> </font></font></td>
 </tr>
</xsl:template>

</xsl:stylesheet>
