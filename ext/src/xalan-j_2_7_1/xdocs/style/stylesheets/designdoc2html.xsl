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

  <xsl:param name="stylebook.project"/>
  <xsl:param name="copyright"/>
  <xsl:param name="id"/>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="s1">
    <html>
      <head>
        <title><xsl:value-of select="@title"/></title>
      </head>
      <body text="#000000" link="#0000ff" vlink="#0000aa" alink="#ff0000"
            topmargin="4" leftmargin="4" marginwidth="4" marginheight="4"
            bgcolor="#ffffff">
         <xsl:variable name="topimage" select="./p/img/@src"/>
         <h1><a href="http://xml.apache.org"><img src="images/{$topimage}" alt="{@alt}"/></a>&#160;&#160;
         <xsl:value-of select="@title"/></h1><hr/>
             <xsl:apply-templates/>
         <hr/>
            <font size="-1" color="#0086b2"><i>
              Copyright &#169; <xsl:value-of select="$copyright"/>
            </i></font>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="s2">
      
      <h2><xsl:value-of select="@title"/></h2>
      <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="s3">
      <h3><xsl:value-of select="@title"/></h3>
      <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="s4">
      <h4><xsl:value-of select="@title"/></h4>
      <xsl:apply-templates/>
  </xsl:template>

<!-- ###################################################################### -->
<!-- blocks -->

  <xsl:template match="p">
    <p><xsl:apply-templates/></p>
  </xsl:template>

  <xsl:template match="note">
    <table width="100%" cellspacing="3" cellpadding="0" border="0">
      <tr>
        <td width="20" valign="top">
          <img src="resources/note.gif" width="20" height="24" vspace="0" hspace="0" border="0" alt="Note"/>
        </td>
        <td valign="top">
          <font size="-1" face="arial,helvetica,sanserif" color="#000000">
            <i>
              <xsl:apply-templates/>
            </i>
          </font>
        </td>
      </tr>  
    </table>
  </xsl:template>

  <xsl:template match="u">
    <u><xsl:apply-templates/></u>
  </xsl:template>

  <xsl:template match="i">
    <i><xsl:apply-templates/></i>
  </xsl:template>

  <xsl:template match="b">
    <b><xsl:apply-templates/></b>
  </xsl:template>

  <xsl:template match="ul">
    <ul><xsl:apply-templates/></ul>
  </xsl:template>

  <xsl:template match="ol">
    <ol><xsl:apply-templates/></ol>
  </xsl:template>

  <xsl:template match="li">
    <li><xsl:apply-templates/></li>
  </xsl:template>
  
    <!--Definition lists: gloss, term, label, item -->
  <xsl:template match="gloss">
    <dl><xsl:apply-templates/></dl>
  </xsl:template>
   <!-- <term> contains a single-word, multi-word or symbolic 
       designation which is regarded as a technical term. --> 
  <xsl:template match="term">
    <dfn><xsl:apply-templates/></dfn>
  </xsl:template>
  <xsl:template match="label" priority="1">
    <dt><xsl:apply-templates/></dt>
  </xsl:template>
  <xsl:template match="item" priority="2">
    <dd>
      <xsl:apply-templates/>
    </dd>
  </xsl:template>

  <xsl:template match="source">
    <p><font size="-1"><pre><xsl:apply-templates/></pre></font></p>
  </xsl:template>

  <xsl:template match="small-table">
    <center>
      <xsl:choose>
        <xsl:when test="@leave-me-alone = 'yes'">
          <table>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="*"/>
          </table>
        </xsl:when>
        <xsl:otherwise>
          <table width="90%" border="0" cellspacing="2">
            <xsl:apply-templates mode="small-table"/>
          </table>
        </xsl:otherwise>
      </xsl:choose>
    </center>
  </xsl:template>

  <xsl:template match="tr" mode="small-table">
    <tr><xsl:apply-templates mode="small-table"/></tr>
  </xsl:template>

  <xsl:template match="td" mode="small-table">
    <td valign="top"><font size="-1"><xsl:apply-templates/></font></td>
  </xsl:template>


  <xsl:template match="table">
    <table width="100%" border="0" cellspacing="2" cellpadding="2">
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="tr">
    <tr><xsl:apply-templates/></tr>
  </xsl:template>

  <xsl:template match="th">
    <td bgcolor="#039acc" colspan="{@colspan}" rowspan="{@rowspan}" valign="center" align="center">
      <font color="#ffffff" size="-1" face="arial,helvetica,sanserif">
        <b><xsl:apply-templates/></b>&#160;
      </font>
    </td>
  </xsl:template>

  <xsl:template match="td">
    <td bgcolor="#a0ddf0" colspan="{@colspan}" rowspan="{@rowspan}" valign="top" align="left">
      <font color="#000000" size="-1" face="arial,helvetica,sanserif">
        <xsl:apply-templates/>&#160;
      </font>
    </td>
  </xsl:template>

  <xsl:template match="tn">
    <td bgcolor="#ffffff" colspan="{@colspan}" rowspan="{@rowspan}">
      &#160;
    </td>
  </xsl:template>

<!-- ###################################################################### -->
<!-- markup -->

  <xsl:template match="em">
    <b><xsl:apply-templates/></b>
  </xsl:template>

  <xsl:template match="ref">
    <center><i><xsl:apply-templates/></i></center>
  </xsl:template>
  
  <xsl:template match="code">
    <code><font face="courier, monospaced"><xsl:apply-templates/></font></code>
  </xsl:template>
  
  <xsl:template match="br">
    <br/>
  </xsl:template>
  
<!-- ###################################################################### -->
<!-- links -->

  <xsl:template match="link">
    <xsl:if test="string-length(@anchor)=0">
      <xsl:if test="string-length(@idref)=0">
        <!--xsl:apply-templates/-->
      </xsl:if>
      <xsl:if test="string-length(@idref)>0">
        <a href="{@idref}.html"><xsl:apply-templates/></a>
      </xsl:if>
    </xsl:if>

    <xsl:if test="string-length(@anchor)>0">
      <xsl:if test="string-length(@idref)=0">
        <a href="#{@anchor}"><xsl:apply-templates/></a>
      </xsl:if>
      <xsl:if test="string-length(@idref)>0">
        <a href="{@idref}.html#{@anchor}"><xsl:apply-templates/></a>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template match="anchor">
    <a name="{@name}"><xsl:comment>anchor</xsl:comment></a>
  </xsl:template>

  <xsl:template match="jump">
    <a href="{@href}"><xsl:apply-templates/></a>
  </xsl:template>

  <xsl:template match="/s1/s2//img">
    <center><img src="images/{@src}" alt="{@alt}"/></center>
  </xsl:template>

  <xsl:template match="resource-ref">
    <xsl:variable name="resourceFile" 
          select="document($stylebook.project)/book/resources/@source"/>
    <xsl:variable name="xref" select="@idref"/>
    <xsl:variable name="href"
          select="document($resourceFile)/resources/resource[@id=$xref]/@location"/>
    <xsl:variable name="label"
          select="document($resourceFile)/resources/resource[@id=$xref]/@title"/>
    <A href="{$href}" target="_top"><xsl:value-of select="$label"/></A>
  </xsl:template>

  <xsl:template match="human-resource-ref">
    <xsl:variable name="resourceFile" 
          select="document($stylebook.project)/book/resources/@source"/>  
    <xsl:variable name="ref"  select="@idref"/>
    <xsl:variable name="mailto"
          select="document($resourceFile)/resources/human-resource[@id=$ref]/@mailto"/>
   <xsl:variable name="name"
          select="document($resourceFile)/resources/human-resource[@id=$ref]/@name"/>                          
    <A href="mailto:{$mailto}"><xsl:value-of select="$name"/></A>
  </xsl:template>

<!-- ###################################################################### -->

</xsl:stylesheet>