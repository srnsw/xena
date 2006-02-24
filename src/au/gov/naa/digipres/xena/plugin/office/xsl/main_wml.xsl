<!--

   The Contents of this file are made available subject to the terms of
   either of the following licenses

          - GNU Lesser General Public License Version 2.1
          - Sun Industry Standards Source License Version 1.1

   Sun Microsystems Inc., October, 2000

   GNU Lesser General Public License Version 2.1
   =============================================
   Copyright 2000 by Sun Microsystems, Inc.
   901 San Antonio Road, Palo Alto, CA 94303, USA

   This library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License version 2.1, as published by the Free Software Foundation.

   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.

   You should have received a copy of the GNU Lesser General Public
   License along with this library; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place, Suite 330, Boston,
   MA  02111-1307  USA


   Sun Industry Standards Source License Version 1.1
   =================================================
   The contents of this file are subject to the Sun Industry Standards
   Source License Version 1.1 (the "License"); You may not use this file
   except in compliance with the License. You may obtain a copy of the
   License at http://www.openoffice.org/license.html.

   Software provided under this License is provided on an "AS IS" basis,
   WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING,
   WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
   MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
   See the License for the specific provisions governing your rights and
   obligations concerning the Software.

   The Initial Developer of the Original Code is: Sun Microsystems, Inc.

   Copyright © 2002 by Sun Microsystems, Inc.

   All Rights Reserved.

   Contributor(s): _______________________________________

-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:office="http://openoffice.org/2000/office"
                xmlns:style="http://openoffice.org/2000/style"
                xmlns:text="http://openoffice.org/2000/text"
                xmlns:table="http://openoffice.org/2000/table"
                xmlns:draw="http://openoffice.org/2000/drawing"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:number="http://openoffice.org/2000/datastyle"
                xmlns:svg="http://www.w3.org/2000/svg"
                xmlns:chart="http://openoffice.org/2000/chart"
                xmlns:dr3d="http://openoffice.org/2000/dr3d"
                xmlns:math="http://www.w3.org/1998/Math/MathML"
                xmlns:form="http://openoffice.org/2000/form"
                xmlns:script="http://openoffice.org/2000/script"
                office:class="text"
                office:version="1.0"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:meta="http://openoffice.org/2000/meta"
                xmlns:config="http://openoffice.org/2001/config"
                xmlns:help="http://openoffice.org/2000/help"
                xmlns:xt="http://www.jclark.com/xt"
                xmlns:system="http://www.jclark.com/xt/java/java.lang.System"
                xmlns:urlencoder="http://www.jclark.com/xt/java/java.net.URLEncoder"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:java="http://xml.apache.org/xslt/java"
                exclude-result-prefixes="java">


    <xsl:output method               = "xml"
                omit-xml-declaration = "no"
                media-type           = "text/vnd.wap.wml"
                encoding             = "UTF-8"
                indent               = "yes"
                doctype-public       = "-//WAPFORUM//DTD WML 1.1//EN"
                doctype-system       = "http://www.wapforum.org/DTD/wml_1.1.xml"/>


    <!--+++++ INCLUDED XSL MODULES +++++-->

      <!-- inherited style properties will be collected and written in a CSS header (CSS) -->
    <xsl:include href="style_header.xsl"/>
      <!-- inherited style properties will be collected and written as html properties in a temporary variable (HTML4, PALM) -->
    <xsl:include href="style_inlined.xsl"/>
      <!-- our xml style properties will be mapped to CSS and HTML4.x properties -->
    <xsl:include href="style_mapping.xsl"/>
      <!-- common element handling -->
    <xsl:include href="common.xsl"/>
      <!-- table handling -->
    <xsl:include href="table.xsl"/>
    <xsl:include href="table_wml.xsl"/>
      <!-- palm handling -->
    <xsl:include href="palm.xsl"/>



    <!--+++++ PARAMETER FROM THE APPLICATION AND GLOBAL VARIABLES +++++-->

   <!--+++++ PARAMETER FROM THE APPLICATION AND GLOBAL VARIABLES +++++-->

    <!-- MANDATORY: URL of meta stream -->
    <xsl:param name="metaFileURL"/>

    <!-- MANDATORY: URL of styles stream -->
    <xsl:param name="stylesFileURL"/>

    <!-- MANDATORY: for resolving relative links
        For resolving realtive links to the packed SO document, i.e. the path/URL of the jared sxw file (e.g. meta.xml, styles.xml, links to graphics in a relative directory) -->
    <xsl:param name="absoluteSourceDirRef"/>

    <!-- OPTIONAL (mandatory, when when source is compressed): Necessary for the in the packed OO document embedded files (mostly graphics from the compressed /Picture dir).
         When the OpenOffice (OO) file has been unpacked the absoluteSoureDirRef can be taken,
         Otherwise, a JAR URL could be choosen or when working with OpenOffice a so called Package-URL encoded over HTTP can be used to
         access the jared contents of the the jared document. . -->
    <xsl:param name="jaredRootURL" select="$absoluteSourceDirRef"/>

    <!-- OPTIONAL (mandatory, when used in session based environment)
         Useful for WebApplications: if a HTTP session is not cookie based, URL rewriting is beeing used (the session is appended to the URL).
         This URL session is used when creating links to graphics by XSLT. Otherwise the user havt to log again in for every graphic he would like to see. -->
    <xsl:param name="optionalURLSuffix"/>

    <!-- OPTIONAL: DPI (dots per inch) the standard solution of given pictures (necessary for the conversion of 'cm' into 'pixel')-->
    <xsl:param name="dpi" select="96"/>

    <!-- OPTIONAL: in case of using a different processor than a JAVA XSLT, you can unable the Java functionality
         (i.e. debugging time and encoding chapter names for the content-table as href and anchors ) -->
    <xsl:param name="isJavaDisabled" select="false()"/>

    <!-- OPTIONAL: user-agent will be differntiated by this parameter given by application (e.g. java servlet)-->
    <xsl:param name="outputType" select="'WML'"/>

    <!-- OPTIONAL: for activating the debug mode set the variable here to 'true()' or give any value from outside -->
    <xsl:param name="isDebugMode"   select="false()"/>

    <!-- not tested for WML -->
    <xsl:param name="disableLinkedTableOfContent" select="true()"/>

    <!-- following variables are not used for WML transformation, but have to be declared -->
    <xsl:param name="precedingChapterLevel1"  select="0"/>
    <xsl:param name="precedingChapterLevel2"  select="0"/>
    <xsl:param name="precedingChapterLevel3"  select="0"/>
    <xsl:param name="precedingChapterLevel4"  select="0"/>
    <xsl:param name="precedingChapterLevel5"  select="0"/>
    <xsl:param name="precedingChapterLevel6"  select="0"/>
    <xsl:param name="precedingChapterLevel7"  select="0"/>
    <xsl:param name="precedingChapterLevel8"  select="0"/>
    <xsl:param name="precedingChapterLevel9"  select="0"/>
    <xsl:param name="precedingChapterLevel10" select="0"/>
    <xsl:param name="contentTableURL"/>



    <!-- works for normal separated zipped xml files as for flat filter single xml file format as well -->
    <xsl:variable name="office:meta-file"           select="document($metaFileURL)"/>
    <xsl:variable name="office:styles-file"         select="document($stylesFileURL)"/>
    <xsl:variable name="office:font-decls"          select="$office:styles-file/*/office:font-decls"/>
    <xsl:variable name="office:styles"              select="$office:styles-file/*/office:styles"/>
    <!-- office:automatic-styles may occure in two different files (i.d. content.xml and styles.xml). Furthermore the top level tag is different in a flat xml file -->
    <xsl:variable name="office:automatic-styles"    select="/*/office:automatic-styles"/>


    <!--    AVOID NESTING PARAGRAPHS IN WML:
            The list of all office tags, which map to a paragraph. This is necessary as no nested
            paragraph tags are allowed in WML1.1 so a parent check before open has to be done.
            Checked in a table cell, the table row is ALWAYS an ancestor, so it have to be excluded -->
    <xsl:variable name="wap-paragraph-elements-without-table-row" select="'text:p text:h text:span text:a text:section text:list-item draw:text-box draw:page table:sub-table'"/>
    <xsl:variable name="wap-paragraph-elements"                   select="concat($wap-paragraph-elements-without-table-row, ' table:table-row ')"/>




    <!-- ************************************* -->
    <!-- *** build the propriate HTML file *** -->
    <!-- ************************************* -->

    <xsl:template match="/">
        <!--    to access the variable like a node-set it is necessary to convert it
                from a result-tree-fragment (RTF) to a node set using the James Clark extension-->
        <xsl:variable name="collectedGlobalData-RTF">
                <xsl:call-template name='create-all-inline-styles'/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="function-available('xt:node-set')">
                <xsl:call-template name="start">
                    <xsl:with-param name="collectedGlobalData" select="xt:node-set($collectedGlobalData-RTF)"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="function-available('xalan:nodeset')">
                <xsl:call-template name="start">
                    <xsl:with-param name="collectedGlobalData" select="xalan:nodeset($collectedGlobalData-RTF)"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="NodeSetFunctionNotAvailable"/>
                <xsl:call-template name="start"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="start">
        <xsl:param name="collectedGlobalData"/>

        <xsl:element name="wml">
            <xsl:element name="card">
                <!-- processing the content of the xml file -->
                <xsl:apply-templates select="/*/office:body">
                    <xsl:with-param name="collectedGlobalData" select="$collectedGlobalData"/>
                </xsl:apply-templates>
            </xsl:element>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>