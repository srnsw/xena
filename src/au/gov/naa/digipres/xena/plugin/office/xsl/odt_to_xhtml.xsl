<?xml version="1.0"?>
<!--
This file is dual-licensed.
 - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
LGPL:
    This stylesheet converts OpenDocument text files to XHTML.
    Copyright (C) 2005-2006 J. David Eisenberg

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
	
	Author: J. David Eisenberg
	Contact: catcode@catcode.com
 - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
 Apache 2.0
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->


<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
    xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0"
    xmlns:config="urn:oasis:names:tc:opendocument:xmlns:config:1.0"
    xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
    xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
    xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
    xmlns:presentation="urn:oasis:names:tc:opendocument:xmlns:presentation:1.0"
    xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0"
    xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0"
    xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0"
    xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0"
    xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
    xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
    xmlns:anim="urn:oasis:names:tc:opendocument:xmlns:animation:1.0"

    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:math="http://www.w3.org/1998/Math/MathML"
    xmlns:xforms="http://www.w3.org/2002/xforms"

    xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
    xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
    xmlns:smil="urn:oasis:names:tc:opendocument:xmlns:smil-compatible:1.0"
	
	xmlns:ooo="http://openoffice.org/2004/office"
	xmlns:ooow="http://openoffice.org/2004/writer"
	xmlns:oooc="http://openoffice.org/2004/calc"
	xmlns:int="http://catcode.com/odf_to_xhtml/internal"
    xmlns="http://www.w3.org/1999/xhtml"
	exclude-result-prefixes="office meta config text table draw presentation
		dr3d chart form script style number anim dc xlink math xforms fo
		svg smil ooo ooow oooc int #default"
>

<xsl:output
	method="xml"
	indent="yes"
	omit-xml-declaration="yes"
	doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
	doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
	encoding="UTF-8"
/>
<xsl:variable name="lineBreak"><xsl:text>
</xsl:text></xsl:variable>

<xsl:key name="listTypes" match="text:list-style" use="@style:name"/>

<xsl:template match="/office:document-content">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Converted by odt_to_xhtml.xsl</title>
<meta http-equiv="Content-Type" content="text/html" />
<xsl:apply-templates select="office:automatic-styles"/>
</head>
<body>
<xsl:apply-templates select="office:body/office:text"/>
</body>
</html>
</xsl:template>

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!--
	This section of the transformation handles styles in the
	content.xml file
-->
<xsl:template match="office:automatic-styles">
	<style type="text/css">
	<xsl:apply-templates/>
	</style>
</xsl:template>

<xsl:template match="style:style">
	<xsl:choose>
		<xsl:when test="@style:family='table'">
			<xsl:call-template name="process-table-style"/>
		</xsl:when>
		<xsl:when test="@style:family='table-column'">
			<xsl:call-template name="process-table-column-style"/>
		</xsl:when>
		<xsl:when test="@style:family='table-cell'">
			<xsl:call-template name="process-table-cell-style"/>
		</xsl:when>
		<xsl:when test="@style:family='paragraph'">
			<xsl:call-template name="process-paragraph-style"/>
		</xsl:when>
		<xsl:when test="@style:family='text'">
			<xsl:call-template name="process-text-style"/>
		</xsl:when>
	</xsl:choose>
</xsl:template>

<xsl:template name="process-table-style">
	<xsl:if test="style:table-properties">
		<xsl:value-of select="$lineBreak"/>
		<xsl:text>.</xsl:text>
		<xsl:value-of select="translate(@style:name,'.','_')"/>
		<xsl:text>{width: </xsl:text>
		<xsl:value-of select="style:table-properties/
			@style:width"/>
		<xsl:text>}</xsl:text>
		<xsl:value-of select="$lineBreak"/>
	</xsl:if>
</xsl:template>

<xsl:template name="process-table-column-style">
	<xsl:if test="style:table-column-properties">
		<xsl:value-of select="$lineBreak"/>
		<xsl:text>.</xsl:text>
		<xsl:value-of select="translate(@style:name,'.','_')"/>
		<xsl:text>{width: </xsl:text>
		<xsl:value-of select="style:table-column-properties/
			@style:column-width"/><xsl:text>;</xsl:text>
		<xsl:value-of select="$lineBreak"/>
				<xsl:call-template name="handle-style-properties">
			<xsl:with-param name="nodeSet"
				select="style:table-properties"/>
		</xsl:call-template>
		<xsl:text>}</xsl:text>
		<xsl:value-of select="$lineBreak"/>
	</xsl:if>
</xsl:template>

<xsl:template name="process-table-cell-style">
	<xsl:if test="style:table-cell-properties">
		<xsl:value-of select="$lineBreak"/>
		<xsl:text>.</xsl:text>
		<xsl:value-of select="translate(@style:name,'.','_')"/>
		<xsl:text>{</xsl:text><xsl:value-of select="$lineBreak"/>
		<xsl:call-template name="handle-style-properties">
			<xsl:with-param name="nodeSet"
				select="style:table-cell-properties"/>
		</xsl:call-template>
		<xsl:text>}</xsl:text><xsl:value-of select="$lineBreak"/>
	</xsl:if>
</xsl:template>

<xsl:template name="process-paragraph-style">
	<xsl:if test="style:paragraph-properties">
		<xsl:value-of select="$lineBreak"/>
		<xsl:text>.</xsl:text>
		<xsl:value-of select="translate(@style:name,'.','_')"/>
		<xsl:text>{</xsl:text><xsl:value-of select="$lineBreak"/>
		<xsl:call-template name="handle-style-properties">
			<xsl:with-param name="nodeSet"
				select="style:paragraph-properties"/>
		</xsl:call-template>
		<xsl:text>}</xsl:text><xsl:value-of select="$lineBreak"/>
	</xsl:if>
</xsl:template>

<xsl:template name="process-text-style">
	<xsl:if test="style:text-properties">
		<xsl:value-of select="$lineBreak"/>
		<xsl:text>.</xsl:text>
		<xsl:value-of select="translate(@style:name,'.','_')"/>
		<xsl:text>{</xsl:text><xsl:value-of select="$lineBreak"/>
		<xsl:call-template name="handle-style-properties">
			<xsl:with-param name="nodeSet"
				select="style:text-properties"/>
		</xsl:call-template>
		<xsl:text>}</xsl:text><xsl:value-of select="$lineBreak"/>
	</xsl:if>
</xsl:template>

<xsl:template name="handle-style-properties">
	<xsl:param name="nodeSet"/>
	<xsl:for-each select="$nodeSet/@*">
		<xsl:variable name="this" select="."/>
		<xsl:variable name="find" select="document('')/xsl:stylesheet/
			int:attr-map/int:attr[@name=name($this)]"/>
		<xsl:if test="$find">
			<xsl:variable name="action" select="$find/@action"/>
			<xsl:choose>
				<xsl:when test="$action='pass-through'">
					<xsl:call-template name="pass-through"/>
				</xsl:when>
				<xsl:when test="$action='check-align'">
					<xsl:call-template name="check-align"/>
				</xsl:when>
			</xsl:choose>
		</xsl:if>
	</xsl:for-each>
</xsl:template>

<xsl:template name="pass-through">
	<xsl:value-of select="local-name()"/><xsl:text>: </xsl:text>
	<xsl:value-of select="."/><xsl:text>;</xsl:text>
	<xsl:value-of select="$lineBreak"/>
</xsl:template>

<xsl:template name="check-align">
	<xsl:value-of select="local-name()"/><xsl:text>: </xsl:text>
	<xsl:choose>
		<xsl:when test=".='start'"><xsl:text>left</xsl:text></xsl:when>
		<xsl:when test=".='end'"><xsl:text>right</xsl:text></xsl:when>
		<xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
	</xsl:choose>
	<xsl:text>;</xsl:text>
	<xsl:value-of select="$lineBreak"/>
</xsl:template>

<xsl:template match="text:list-level-style-bullet">
	<xsl:text>.</xsl:text>
	<xsl:value-of select="../@style:name"/>
	<xsl:text>_</xsl:text>
	<xsl:value-of select="@text:level"/>
	<xsl:text>{ list-style-type: </xsl:text>
	<xsl:choose>
		<xsl:when test="@text:level mod 3 = 1">disc</xsl:when>
		<xsl:when test="@text:level mod 3 = 2">circle</xsl:when>
		<xsl:when test="@text:level mod 3 = 0">square</xsl:when>
		<xsl:otherwise>decimal</xsl:otherwise>
	</xsl:choose>
	<xsl:text>;}</xsl:text>
	<xsl:value-of select="$lineBreak"/>
</xsl:template>


<xsl:template match="text:list-level-style-number">
	<xsl:text>.</xsl:text>
	<xsl:value-of select="../@style:name"/>
	<xsl:text>_</xsl:text>
	<xsl:value-of select="@text:level"/>
	<xsl:text>{ list-style-type: </xsl:text>
	<xsl:choose>
		<xsl:when test="@style:num-format='1'">decimal</xsl:when>
		<xsl:when test="@style:num-format='I'">upper-roman</xsl:when>
		<xsl:when test="@style:num-format='i'">lower-roman</xsl:when>
		<xsl:when test="@style:num-format='A'">upper-alpha</xsl:when>
		<xsl:when test="@style:num-format='a'">lower-alpha</xsl:when>
		<xsl:otherwise>decimal</xsl:otherwise>
	</xsl:choose>
	<xsl:text>;}</xsl:text>
	<xsl:value-of select="$lineBreak"/>
</xsl:template>
<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!--
	This section of the transformation handles the true content
	elements in the content.xml file
-->

<!--
	Yes, paragraphs in ODT really produce a <div> in XHTML,
	because an ODT paragraph has no extra line spacing.
-->
<xsl:template match="text:p">
	<div class="{translate(@text:style-name,'.','_')}">
		<xsl:apply-templates/>
		<xsl:if test="count(node())=0"><br /></xsl:if>
	</div>
</xsl:template>

<xsl:template match="text:span">
	<span class="{translate(@text:style-name,'.','_')}">
		<xsl:apply-templates/>
	</span>
</xsl:template>

<xsl:template match="text:h">
	<!-- Heading levels go only to 6 in XHTML -->
	<xsl:variable name="level">
		<xsl:choose>
			<xsl:when test="@text:outline-level &gt; 6">6</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="@text:outline-level"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:element name="{concat('h', $level)}">
		<xsl:attribute name="class">
			<xsl:value-of
			select="translate(@text:style-name,'.','_')"/>
		</xsl:attribute>
		<xsl:apply-templates/>
	</xsl:element>
</xsl:template>

<!--
	When processing a list, you have to look at the parent style
	*and* level of nesting
-->
<xsl:template match="text:list">
	<xsl:variable name="level" select="count(ancestor::text:list)+1"/>
	
	<!-- the list class is the @text:style-name of the outermost
		<text:list> element -->
	<xsl:variable name="listClass">
		<xsl:choose>
			<xsl:when test="$level=1">
				<xsl:value-of select="@text:style-name"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="
					ancestor::text:list[last()]/@text:style-name"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	
	<!-- Now select the <text:list-level-style-foo> element at this
		level of nesting for this list -->
	<xsl:variable name="node" select="key('listTypes',
		$listClass)/*[@text:level='$level']"/>

	<!-- emit appropriate list type -->
	<xsl:choose>
		<xsl:when test="local-name($node)='list-level-style-number'">
			<ol class="{concat($listClass,'_',$level)}">
				<xsl:apply-templates/>
			</ol>
		</xsl:when>
		<xsl:otherwise>
			<ul class="{concat($listClass,'_',$level)}">
				<xsl:apply-templates/>
			</ul>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template match="text:list-item">
	<li><xsl:apply-templates/></li>
</xsl:template>

<xsl:template match="table:table">
	<table class="{@table:style-name}">
		<colgroup>
			<xsl:apply-templates select="table:table-column"/>
		</colgroup>
		<xsl:if test="table:table-header-rows/table:table-row">
			<thead>
			<xsl:apply-templates
				select="table:table-header-rows/table:table-row"/>
				</thead>
		</xsl:if>
		<tbody>
		<xsl:apply-templates select="table:table-row"/>
		</tbody>
	</table>
</xsl:template>

<xsl:template match="table:table-column">
<col>
	<xsl:if test="@table:number-columns-repeated">
		<xsl:attribute name="span">
			<xsl:value-of select="@table:number-columns-repeated"/>
		</xsl:attribute>
	</xsl:if>
	<xsl:if test="@table:style-name">
		<xsl:attribute name="class">
			<xsl:value-of select="translate(@table:style-name,'.','_')"/>
		</xsl:attribute>
	</xsl:if>
</col>
</xsl:template>

<xsl:template match="table:table-row">
<tr>
	<xsl:apply-templates select="table:table-cell"/>
</tr>
</xsl:template>

<xsl:template match="table:table-cell">
	<xsl:variable name="n">
		<xsl:choose>
			<xsl:when test="@table:number-columns-repeated != 0">
				<xsl:value-of select="@table:number-columns-repeated"/>
			</xsl:when>
			<xsl:otherwise>1</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
	<xsl:call-template name="process-table-cell">
		<xsl:with-param name="n" select="$n"/>
	</xsl:call-template>
</xsl:template>

<xsl:template name="process-table-cell">
	<xsl:param name="n"/>
	<xsl:if test="$n != 0">
		<td>
		<xsl:if test="@table:style-name">
			<xsl:attribute name="class">
				<xsl:value-of select="translate(@table:style-name,
					'.','_')"/>
			</xsl:attribute>
		</xsl:if>
		<xsl:if test="@table:number-columns-spanned">
			<xsl:attribute name="colspan">
				<xsl:value-of select="@table:number-columns-spanned"/>
			</xsl:attribute>
		</xsl:if>
		<xsl:if test="@table:number-rows-spanned">
			<xsl:attribute name="rowspan">
				<xsl:value-of select="@table:number-rows-spanned"/>
			</xsl:attribute>
		</xsl:if>
		<xsl:apply-templates/>
		</td>
		<xsl:call-template name="process-table-cell">
			<xsl:with-param name="n" select="$n - 1"/>
		</xsl:call-template>
	</xsl:if>
</xsl:template>

<xsl:template match="text:tab">
	<xsl:text xml:space="preserve">	</xsl:text>
</xsl:template>

<xsl:template match="text:line-break">
	<br />
</xsl:template>

<xsl:variable name="spaces"
    xml:space="preserve">                              </xsl:variable>

<xsl:template match="text:s">
<xsl:choose>
    <xsl:when test="@text:c">
        <xsl:call-template name="insert-spaces">
            <xsl:with-param name="n" select="@text:c"/>
        </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
        <xsl:text> </xsl:text>
    </xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="insert-spaces">
<xsl:param name="n"/>
<xsl:choose>
    <xsl:when test="$n &lt;= 30">
        <xsl:value-of select="substring($spaces, 1, $n)"/>
    </xsl:when>
    
    <xsl:otherwise>
        <xsl:value-of select="$spaces"/>
        <xsl:call-template name="insert-spaces">
            <xsl:with-param name="n">
                <xsl:value-of select="$n - 30"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="text:a">
<a href="{@xlink:href}"><xsl:apply-templates/></a>
</xsl:template>

<!--
	<text:bookmark-start /> and <text:bookmark-end /> can
	be on non-wellformed boundaries. The quickest solution is
	to create an <a name=""></a> element.
	
	If you don't put in any content, it becomes an empty element,
	which will confuse browsers. While we would love to insert
	a zero-width non-breaking space (Unicode 0x200b), Internet
	Explorer doesn't recognize it.  Hence this ugly hack:
-->
<xsl:template match="text:bookmark-start|text:bookmark">
<a name="{@text:name}"><span style="font-size: 0px"><xsl:text> </xsl:text></span></a>
</xsl:template>

<!--
	This template is too dangerous to leave active...
<xsl:template match="text()">
	<xsl:if test="normalize-space(.) !=''">
		<xsl:value-of select="normalize-space(.)"/>
	</xsl:if>
</xsl:template>
-->

<!--
	This is a list of fo: attributes to be transferred to the
	output document. The action tells which template is to be
	called to process the attribute.
-->
<int:attr-map>
	<int:attr name="fo:border-left" action="pass-through"/>
	<int:attr name="fo:border-right" action="pass-through"/>
	<int:attr name="fo:border-top" action="pass-through"/>
	<int:attr name="fo:border-bottom" action="pass-through"/>
	<int:attr name="fo:border" action="pass-through"/>
	<int:attr name="fo:margin-left" action="pass-through"/>
	<int:attr name="fo:margin-right" action="pass-through"/>
	<int:attr name="fo:margin-top" action="pass-through"/>
	<int:attr name="fo:margin-bottom" action="pass-through"/>
	<int:attr name="fo:margin" action="pass-through"/>
	<int:attr name="fo:padding-left" action="pass-through"/>
	<int:attr name="fo:padding-right" action="pass-through"/>
	<int:attr name="fo:padding-top" action="pass-through"/>
	<int:attr name="fo:padding-bottom" action="pass-through"/>
	<int:attr name="fo:padding" action="pass-through"/>
	<int:attr name="fo:text-indent" action="pass-through"/>
	<int:attr name="fo:font-variant" action="pass-through"/>
	<int:attr name="fo:font-family" action="pass-through"/>
	<int:attr name="fo:color" action="pass-through"/>
	<int:attr name="fo:background-color" action="pass-through"/>
	<int:attr name="fo:font-style" action="pass-through"/>
	<int:attr name="fo:font-weight" action="pass-through"/>
	<int:attr name="fo:line-height" action="pass-through"/>
	<int:attr name="fo:text-align" action="check-align"/>
</int:attr-map>
</xsl:stylesheet>
