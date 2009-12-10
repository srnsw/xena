<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

<!-- This stylesheet is a customization of the DocBook XSL Stylesheets -->
<!-- See http://sourceforge.net/projects/docbook/ -->
<xsl:import href="../../../thirdparty/dbxslt/docbook-xsl-1.62.4/html/docbook.xsl"/>

<!-- <xsl:include href="titlepage.xsl"/> -->

<!-- ============================================================ -->
<!-- Parameters -->

<xsl:param name="html.stylesheet">snapbridge-spec.css</xsl:param>
<xsl:param name="section.autolabel" select="'1'"/>
<xsl:param name="generate.component.toc" select="'1'"/>
<!-- <xsl:param name="toc.list.type" select="ol" /> -->

<!-- ============================================================ -->
<!-- Titlepage -->

<xsl:template match="pubdate" mode="titlepage.mode">
  <h2>
    <xsl:choose>
      <xsl:when test="/*/@status">
        <xsl:value-of select="/*/@status"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>???Unknown Status???</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:text>&#160;</xsl:text>
    <xsl:apply-templates mode="titlepage.mode"/>
  </h2>
</xsl:template>

<xsl:template match="revhistory" mode="titlepage.mode">
  <dl>
    <dt>This version:</dt>
    <dd>
      <xsl:apply-templates select="revision[1]"
                           mode="titlepage.mode"/>
    </dd>
  </dl>
  <xsl:if test="count(revision)&gt;1">
    <dl>
      <dt>Previous versions:</dt>
      <dd>
	<xsl:apply-templates select="revision[position()&gt;1]"
			     mode="titlepage.mode"/>
      </dd>
    </dl>
  </xsl:if>
</xsl:template>

<xsl:template match="revision" mode="titlepage.mode">
  <xsl:choose>
    <xsl:when test="@role">
      <a href="{@role}">
        <xsl:apply-templates select="revnumber" mode="titlepage.mode"/>
        <xsl:text>: </xsl:text>
        <xsl:apply-templates select="date" mode="titlepage.mode"/>
      </a>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates select="revnumber" mode="titlepage.mode"/>
      <xsl:text>: </xsl:text>
      <xsl:apply-templates select="date" mode="titlepage.mode"/>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:if test="position()&lt;last()">
    <br/>
  </xsl:if>
</xsl:template>

<xsl:template match="revnumber|revremark|date" mode="titlepage.mode">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="authorgroup" mode="titlepage.mode">
  <xsl:variable name="label">
    <xsl:choose>
      <xsl:when test="count(*) = 1">
        <xsl:text>Editor:</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>Editors:</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <dl>
    <dt><xsl:value-of select="$label"/></dt>
    <dd>
      <xsl:apply-templates mode="titlepage.mode"/>
    </dd>
  </dl>
</xsl:template>

<xsl:template match="editor" mode="titlepage.mode">
  <xsl:call-template name="person.name"/>
  <xsl:apply-templates select="affiliation/address/email"
                       mode="titlepage.mode"/>
  <xsl:if test="position()&lt;last()">, </xsl:if>
</xsl:template>

<xsl:template match="email" mode="titlepage.mode">
  <xsl:text>&#160;</xsl:text>
  <xsl:apply-templates select="."/>
</xsl:template>

<xsl:template match="abstract" mode="titlepage.mode">
  <hr/>
  <div class="{name(.)}">
    <h2>
      <a>
        <xsl:attribute name="name">
          <xsl:call-template name="object.id"/>
        </xsl:attribute>
      </a>
      <xsl:apply-templates select="." mode="object.title.markup"/>
    </h2>

    <xsl:apply-templates mode="titlepage.mode"/>
  </div>
</xsl:template>

<xsl:template match="legalnotice[@role='status']" mode="titlepage.mode">
  <div class="{name(.)}">
    <xsl:apply-templates mode="titlepage.mode"/>
  </div>
</xsl:template>

<xsl:template match="legalnotice/title" mode="titlepage.mode">
  <h2><xsl:apply-templates/></h2>
</xsl:template>

<xsl:template match="author" mode="titlepage.mode">
  <p class="{name(.)}">
    <xsl:call-template name="person.name"/>
    <xsl:text>, </xsl:text>
    <xsl:apply-templates select="affiliation/shortaffil"
                         mode="titlepage.mode"/>
    <xsl:text>, </xsl:text>
    <xsl:apply-templates select="affiliation/jobtitle"
                         mode="titlepage.mode"/>
    <xsl:text>, </xsl:text>
    <xsl:apply-templates select="affiliation/orgname"
                         mode="titlepage.mode"/>
  </p>
</xsl:template>

<xsl:template match="releaseinfo" mode="titlepage.mode">
  <xsl:comment>
    <xsl:text> </xsl:text>
    <xsl:apply-templates/>
    <xsl:text> </xsl:text>
  </xsl:comment>
</xsl:template>

<xsl:template match="jobtitle|shortaffil|orgname|contrib"
              mode="titlepage.mode">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="othercredit" mode="titlepage.mode">
  <xsl:comment>
    <xsl:text>Other credit: </xsl:text>
    <xsl:call-template name="person.name"/>
    <xsl:text>, </xsl:text>
    <xsl:apply-templates select="affiliation/orgname" mode="titlepage.mode"/>
    <xsl:text>&#xA;</xsl:text>
    <xsl:apply-templates select="contrib" mode="titlepage.mode"/>
  </xsl:comment>
</xsl:template>

<!-- ============================================================ -->
<!-- Component TOC -->

<xsl:template name="component.toc">
  <xsl:if test="$generate.component.toc != 0">
    <xsl:variable name="nodes" select="section|sect1"/>
    <xsl:variable name="apps" select="bibliography|glossary|appendix"/>

    <xsl:if test="$nodes">
      <div class="toc">
        <h2>
          <xsl:call-template name="gentext">
            <xsl:with-param name="key">TableofContents</xsl:with-param>
          </xsl:call-template>
        </h2>

        <xsl:if test="$nodes">
          <xsl:element name="{$toc.list.type}">
            <xsl:apply-templates select="$nodes" mode="toc"/>
          </xsl:element>
        </xsl:if>


        <xsl:if test="$apps">
          <h3>Appendixes</h3>
          <xsl:element name="{$toc.list.type}">
            <xsl:apply-templates select="$apps" mode="toc"/>
          </xsl:element>
        </xsl:if>
      </div>
      <hr/>
    </xsl:if>
  </xsl:if>
</xsl:template>

<!-- ================================================================= -->

<!-- support role='non-normative' -->
<xsl:template match="preface|chapter|appendix" mode="title.markup">
  <xsl:param name="allow-anchors" select="'0'"/>
  <xsl:variable name="title" select="(docinfo/title
                                      |prefaceinfo/title
                                      |chapterinfo/title
                                      |appendixinfo/title
                                      |title)[1]"/>
  <xsl:apply-templates select="$title" mode="title.markup">
    <xsl:with-param name="allow-anchors" select="$allow-anchors"/>
  </xsl:apply-templates>
  <xsl:if test="@role='non-normative'">
    <xsl:text> (Non-Normative)</xsl:text>
  </xsl:if>
</xsl:template>

<!-- support role='non-normative' -->
<xsl:template match="section
                     |sect1|sect2|sect3|sect4|sect5
                     |refsect1|refsect2|refsect3
                     |simplesect"
              mode="title.markup">
  <xsl:param name="allow-anchors" select="'0'"/>
  <xsl:variable name="title" select="(sectioninfo/title
                                      |sect1info/title
                                      |sect2info/title
                                      |sect3info/title
                                      |sect4info/title
                                      |sect5info/title
                                      |refsect1info/title
                                      |refsect2info/title
                                      |refsect3info/title
                                      |title)[1]"/>

  <xsl:apply-templates select="$title" mode="title.markup">
    <xsl:with-param name="allow-anchors" select="$allow-anchors"/>
  </xsl:apply-templates>
  <xsl:if test="@role='non-normative'">
    <xsl:text> (Non-Normative)</xsl:text>
  </xsl:if>
</xsl:template>

<!-- ============================================================ -->
<!-- Formatting changes for OASIS look&amp;feel -->

<xsl:template match="screen">
  <table border="0" width="100&#37;" cellpadding="0" cellspacing="0"
         bgcolor="#e7deef"
         summary="just a container for the background color">
    <tr>
      <td><xsl:apply-imports/></td>
    </tr>
  </table>
</xsl:template>

<xsl:template match="quote">
  <xsl:variable name="depth">
    <xsl:call-template name="dot.count">
      <xsl:with-param name="string">
        <xsl:number level="multiple"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:variable>
  <xsl:choose>
    <xsl:when test="$depth mod 2 = 0">
      <xsl:text>"</xsl:text>
      <xsl:call-template name="inline.charseq"/>
      <xsl:text>"</xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <xsl:text>'</xsl:text>
      <xsl:call-template name="inline.charseq"/>
      <xsl:text>'</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="filename">
  <b>
    <xsl:apply-templates/>
  </b>
</xsl:template>

<xsl:template match="articleinfo/copyright" mode="titlepage.mode"/>

<!-- ============================================================ -->

</xsl:stylesheet>
