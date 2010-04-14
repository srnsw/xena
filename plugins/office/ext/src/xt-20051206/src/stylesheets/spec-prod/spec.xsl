<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

<xsl:import href="tr.xsl"/>
<xsl:import href="proofsystem.xsl"/>
<xsl:import href="grammar.xsl"/>
<xsl:import href="rngprint.xsl"/>

<xsl:param name="toc.section.depth" select="3"/>

<xsl:template match="grammarref">
  <xsl:apply-templates select="document(@src)"/>
</xsl:template>

<xsl:template match="rngref">
  <xsl:apply-templates select="document(@src)" mode="print"/>
</xsl:template>

<xsl:template match="para">
  <xsl:choose>
    <xsl:when test="position() = 1 and parent::listitem">
      <a>
        <xsl:attribute name="name">
          <xsl:call-template name="object.id">
            <xsl:with-param name="object" select="parent::listitem"/>
          </xsl:call-template>
        </xsl:attribute>
      </a>
      <xsl:if test="@id">
        <a name="{@id}"/>
      </xsl:if>
      <xsl:apply-templates/>
    </xsl:when>
    <xsl:otherwise>
      <p>
        <xsl:if test="@id">
          <a name="{@id}"/>
        </xsl:if>
        <xsl:apply-templates/>
      </p>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="note[@role='ednote']">
  <div class="ednote">
    <xsl:if test="$admon.style">
      <xsl:attribute name="style">
        <xsl:value-of select="$admon.style"/>
      </xsl:attribute>
    </xsl:if>
    <h3 class="title">Editorial Note</h3>
    <xsl:apply-templates/>
  </div>
</xsl:template>

<!-- Hack to get Murata-san's name right. -->
<xsl:template match="editor" mode="titlepage.mode">
  <xsl:for-each select="firstname/text()|surname/text()">
    <xsl:if test="position() != 1">
      <xsl:text> </xsl:text>
    </xsl:if>
    <xsl:value-of select="."/>
  </xsl:for-each>
  <xsl:apply-templates select="affiliation/address/email" mode="titlepage.mode"/>
  <xsl:if test="position()&lt;last()">, </xsl:if>
</xsl:template>

<!-- Put labels in the references section, and sort entries by labels. -->

<xsl:template match="bibliodiv">
  <div class="{name(.)}">
    <xsl:apply-templates select="title"/>
    <dl>
      <xsl:apply-templates select="bibliomixed[abbrev]">
        <xsl:sort select="abbrev"/>
      </xsl:apply-templates>
    </dl>
  </div>
</xsl:template>

<xsl:template match="bibliography[bibliomixed]">
  <xsl:variable name="id"><xsl:call-template name="object.id"/></xsl:variable>

  <div id="{$id}" class="{name(.)}">
<!--    <xsl:call-template name="bibliography.titlepage"/> -->
    <xsl:call-template name="preface.titlepage"/>
    <dl>
      <xsl:apply-templates select="bibliomixed[abbrev]">
        <xsl:sort select="abbrev"/>
      </xsl:apply-templates>
    </dl>
    <xsl:call-template name="process.footnotes"/>
  </div>
</xsl:template>


<xsl:template match="bibliomixed[abbrev]">
  <xsl:variable name="id"><xsl:call-template name="object.id"/></xsl:variable>
  <dt><xsl:apply-templates select="abbrev"/></dt>
  <dd id="{$id}" class="{name(.)}">
    <a name="{$id}"/>
    <xsl:apply-templates select="*[not(self::abbrev)]|text()"
                         mode="bibliomixed.mode"/>
  </dd>
</xsl:template>

</xsl:stylesheet>
