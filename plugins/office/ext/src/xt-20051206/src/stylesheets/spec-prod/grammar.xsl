<!-- $Id$ -->
<!DOCTYPE xsl:stylesheet [
<!ENTITY nbsp "&#160;">
]>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rng="http://relaxng.org/ns/structure/1.0"
                exclude-result-prefixes="rng">

<xsl:variable name="rng:conn-none" select="0"/>
<xsl:variable name="rng:conn-or" select="1"/>
<xsl:variable name="rng:conn-seq" select="2"/>
<xsl:variable name="rng:conn-top" select="3"/>

<xsl:template match="rng:grammar">
  <table>
    <xsl:apply-templates select="rng:define"/>
  </table>
</xsl:template>

<xsl:template match="rng:define">
  <tr valign="baseline">
    <td><xsl:value-of select="@name"/></td>
    <td>&nbsp;&nbsp;::=&nbsp;&nbsp;</td>
    <td>
      <xsl:call-template name="rng:pattern">
	<xsl:with-param name="conn-cur" select="$rng:conn-top"/>
        <xsl:with-param name="sub" select="*"/>
      </xsl:call-template>
    </td>
  </tr>
</xsl:template>

<xsl:template match="rng:choice">
  <xsl:param name="conn-cur" select="$rng:conn-none"/>
  <xsl:param name="conn-new" select="$rng:conn-seq"/>
  <xsl:call-template name="rng:pattern">
    <xsl:with-param name="conn-new" select="$rng:conn-or"/>
    <xsl:with-param name="conn-cur" select="$conn-cur"/>
    <xsl:with-param name="sub" select="*"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="rng:zeroOrMore">
  <xsl:call-template name="rng:pattern">
    <xsl:with-param name="sub" select="*"/>
  </xsl:call-template>
  <xsl:text>*</xsl:text>
</xsl:template>

<xsl:template match="rng:oneOrMore">
  <xsl:call-template name="rng:pattern">
    <xsl:with-param name="sub" select="*"/>
  </xsl:call-template>
  <xsl:text>+</xsl:text>
</xsl:template>

<xsl:template match="rng:optional">
  <xsl:text>[</xsl:text>
  <xsl:call-template name="rng:pattern">
    <xsl:with-param name="sub" select="*"/>
    <xsl:with-param name="conn-cur" select="$rng:conn-top"/>
  </xsl:call-template>
  <xsl:text>]</xsl:text>
</xsl:template>

<xsl:template match="rng:group">
  <xsl:param name="conn-cur" select="$rng:conn-none"/>
  <xsl:param name="conn-new" select="$rng:conn-seq"/>
  <xsl:call-template name="rng:pattern">
    <xsl:with-param name="conn-new" select="$rng:conn-seq"/>
    <xsl:with-param name="conn-cur" select="$conn-cur"/>
    <xsl:with-param name="sub" select="*"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="rng:ref">
  <i><xsl:value-of select="@name"/></i>
</xsl:template>

<xsl:template match="rng:element">
  <xsl:param name="conn-cur" select="$rng:conn-none"/>
  <xsl:param name="conn-new" select="$rng:conn-seq"/>
  <code>&lt;<xsl:value-of select="@name"/></code>
  <xsl:for-each select="rng:attribute|rng:optional[rng:attribute]">
    <xsl:text> </xsl:text>
    <xsl:apply-templates select="."/>
  </xsl:for-each>
  <xsl:variable name="children"
    select="*[not(self::rng:attribute|self::rng:optional[rng:attribute])]"/>
  <xsl:choose>
    <xsl:when test="
 not($children) or ($children[self::rng:empty] and count($children) = 1)">
     <code>/&gt;</code>
    </xsl:when>
    <xsl:otherwise>
      <code>&gt; </code>
      <xsl:call-template name="rng:pattern">
        <xsl:with-param name="conn-new" select="$rng:conn-seq"/>
        <xsl:with-param name="conn-cur" select="$rng:conn-seq"/>
	<xsl:with-param name="sub" select="$children"/>
      </xsl:call-template>
      <code> &lt;/<xsl:value-of select="@name"/>&gt;</code>
    </xsl:otherwise>
  </xsl:choose>  
</xsl:template>

<xsl:template match="rng:attribute">
  <code><xsl:value-of select="@name"/>="</code>
  <xsl:call-template name="rng:pattern">
    <xsl:with-param name="sub" select="*"/>
    <xsl:with-param name="conn-cur" select="$rng:conn-top"/>
  </xsl:call-template>
  <code>"</code>
</xsl:template>

<xsl:template match="rng:value">
  <code><xsl:value-of select="."/></code>
</xsl:template>

<xsl:template name="rng:pattern">
  <xsl:param name="conn-cur" select="$rng:conn-none"/>
  <xsl:param name="conn-new" select="$rng:conn-seq"/>
  <xsl:param name="sub" select="/.."/>
  <xsl:choose>
    <xsl:when test="count($sub)>1
                    and $conn-new != $conn-cur
                    and $conn-cur != $rng:conn-top">
      <xsl:text>(</xsl:text>
      <xsl:call-template name="rng:pattern">
        <xsl:with-param name="conn-new" select="$conn-new"/>
        <xsl:with-param name="conn-cur" select="$conn-new"/>
        <xsl:with-param name="sub" select="$sub"/>
      </xsl:call-template>
      <xsl:text>)</xsl:text>
    </xsl:when>
    <xsl:when test="count($sub)>1">
      <xsl:for-each select="$sub">
        <xsl:if test="position() != 1">
          <xsl:choose>
            <xsl:when test="$conn-new=$rng:conn-seq"><xsl:text> </xsl:text></xsl:when>
            <xsl:when test="parent::*/parent::rng:define"><br/>| </xsl:when>
            <xsl:otherwise> | </xsl:otherwise>
          </xsl:choose> 
        </xsl:if>
        <xsl:apply-templates select=".">
          <xsl:with-param name="conn-new" select="$conn-new"/>
          <xsl:with-param name="conn-cur" select="$conn-new"/>
        </xsl:apply-templates>
      </xsl:for-each>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates select="$sub">
        <xsl:with-param name="conn-new" select="$conn-new"/>
        <xsl:with-param name="conn-cur" select="$conn-cur"/>
      </xsl:apply-templates>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="rng:data">
  <i><xsl:value-of select="@type"/></i>
</xsl:template>

</xsl:stylesheet>
