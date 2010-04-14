<?xml version="1.0" encoding="ISO-8859-1" ?>

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

<!DOCTYPE xsl:stylesheet [
<!ENTITY copy   "&#169;">
<!ENTITY nbsp   "&#160;">
]>

<!-- XSL Style sheet, DTD omitted -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:redirect="org.apache.xalan.lib.Redirect"
                extension-element-prefixes="redirect">
  <xsl:output method="html" doctype-public="-//W3C//DTD HTML 4.0 Transitional//EN"/>
  
  <xsl:param name="package-root" select="'../../../src/'"/>  <!-- root of destination for package.html files -->

  <xsl:template match="spec">
    <html>
      <head>
        <title>
          <xsl:value-of select="header/title"/>
        </title>
      </head>
      <body>
        <xsl:apply-templates/>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="spec/title[1]">
    <h1><xsl:apply-templates/></h1>
  </xsl:template>

  <xsl:template match="frontmatter/pubdate">
    <p><b>Edit Date: </b><xsl:apply-templates/></p>
  </xsl:template>

  <xsl:template match="frontmatter/copyright">
    <!-- p>&copy;<xsl:apply-templates/></p -->
  </xsl:template>

  <xsl:template match="frontmatter/author">
  </xsl:template>
  
  <xsl:template match="spec/title">
    <h2>
      <xsl:choose>
        <xsl:when test="@id">
          <a name="@id">
            <xsl:apply-templates/>
          </a>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates/>
        </xsl:otherwise>
      </xsl:choose>
    </h2>
  </xsl:template>
  
  <xsl:template name="apply-id-templates">
    <xsl:choose>
      <xsl:when test="@id">
        <a name="{@id}">
          <xsl:apply-templates/>
        </a>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="sect2/title | spec/*/title">
    <h3>
      <xsl:call-template name="apply-id-templates"/>
    </h3>
  </xsl:template>
  
  <xsl:template match="sect3/title">
    <h4>
      <xsl:call-template name="apply-id-templates"/>
    </h4>
  </xsl:template>

  <xsl:template match="sect4/title">
    <h5>
      <xsl:call-template name="apply-id-templates"/>
    </h5>
  </xsl:template>
  
  <xsl:template match="para">
    <p><xsl:apply-templates/></p>
  </xsl:template>

  <xsl:template match="variablelist">
    <ul>
    <xsl:for-each select="varlistentry">
      <li>
        <p><b><xsl:apply-templates select="term"/></b><br/>
        <xsl:apply-templates select="listitem"/></p>
      </li>
    </xsl:for-each>
    </ul>
  </xsl:template>

  <xsl:template match="orderedlist">
    <ol>
    <xsl:for-each select="listitem">
      <li><xsl:apply-templates/></li>
    </xsl:for-each>
    </ol>
  </xsl:template>

  <xsl:template match="patterns">
    <H3><xsl:value-of select="@module"/><xsl:text> </xsl:text>Patterns</H3>
    <ul>
      <xsl:for-each select="pattern">
        <p>
          <b>
            <xsl:for-each select="pattern-name">
              <xsl:call-template name="apply-id-templates"/>
            </xsl:for-each>
          </b>
          <br/>
        <xsl:apply-templates select="*[name() != 'pattern-name']"/></p>
      </xsl:for-each>
    </ul>
  </xsl:template>
  
  <xsl:template match="pattern/intent">
    <br/><i>Intent: </i><xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="pattern/responsibilities">
    <br/><i>Responsibilities: </i><xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="pattern/potential-alternate-name">
    <br/><i>Potential alternate name: </i><xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="pattern/thread-safety">
    <br/><i>Thread safety: </i><xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="pattern/notes">
    <br/><i>Notes: </i><xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="programlisting">
    <code>
    <pre>
      <xsl:apply-templates/>
    </pre>
    </code>
  </xsl:template>
  
  <xsl:template match="link">
    <A href="#{@linkend}">
      <xsl:apply-templates/>
    </A>
  </xsl:template>
  <xsl:template match="ulink">
    <A href="{@url}">
      <xsl:apply-templates/>
    </A>
  </xsl:template>

  <xsl:template match="termref">
    <xsl:choose>
      <xsl:when test="@link-url">
        <A href="#{@link-url}">
          <xsl:value-of select="."/>
        </A>
      </xsl:when>
      <xsl:otherwise>
        <U><xsl:value-of select="."/></U>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="plink">
    <xsl:text>{@link </xsl:text>
      <xsl:value-of select="."/>
    <xsl:text>}</xsl:text>
  </xsl:template>
  
  <xsl:template match="sect1[@id='package']">
    <xsl:variable name="filename" select="concat($package-root,translate(title,'.', '/'),'/package.html')"/>
    <redirect:write file="{$filename}">
      <xsl:call-template name="sub-package"/>
    </redirect:write>
  </xsl:template>
  
  <xsl:template match="sect2[@id='specialized-packages']">
    <xsl:message>Found specialized-packages</xsl:message>
    <xsl:for-each select="sect3">
	  <xsl:variable name="filename" select="concat($package-root,translate(title,'.', '/'),'/package.html')"/>
      <redirect:write file="{$filename}">
        <xsl:call-template name="sub-package"/>
      </redirect:write>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="sub-package">
    <html>
       <head>
         <title>
           <xsl:value-of select="title"/>
         </title>
       </head>
       <body>
         <xsl:apply-templates select="*[not (name()='title')]"/>
       </body>
     </html>
  </xsl:template>


</xsl:stylesheet>

