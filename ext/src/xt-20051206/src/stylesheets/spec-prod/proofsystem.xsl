<xsl:stylesheet version="1.0" exclude-result-prefixes="p"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:p="http://relaxng.org/ns/proofsystem">

<xsl:output encoding="iso-8859-1"/>

<xsl:template match="p:proofSystem">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="p:formula">
 <div class="formula" style="margin-left: 0.5in; margin-right: 0.5in;">
    <xsl:apply-templates/>
  </div>
</xsl:template>

<xsl:template match="p:notation">
 <h2>Notation</h2>
 <table border="1">
  <tr>
   <th>Notation</th>
   <th>Meaning</th>
  </tr>
<xsl:apply-templates/></table>
  <h2>Inference Rules</h2>
</xsl:template>

<xsl:template match="p:item">
  <tr>
   <td align="center"><xsl:apply-templates select="*[1]"/></td>
   <td><xsl:apply-templates select="*[position()!=1]|text()"/></td>
  </tr>
</xsl:template>

<xsl:template match="p:rule">
  <table cellspacing="20">
    <tr valign="center">
      <td>(<xsl:value-of select="@name"/>)</td>
      <td>
	<xsl:variable name="ncols" select="count(*) - 1"/>
	<table cellpadding="0" cellspacing="0">
          <xsl:if test="$ncols">
	    <tr align="center" width="100%" valign="baseline">
	      <xsl:for-each select="*[position() != last()]">
		<td>
                   <xsl:if test="position() != 1">&#160;&#160;&#160;&#160;</xsl:if>
 		   <xsl:apply-templates select="."/>
		</td>
	      </xsl:for-each>
	    </tr>
	    <tr><td colspan="{$ncols}"><hr noshade="noshade"/></td></tr>
          </xsl:if>
	  <tr align="center">
	    <td colspan="{$ncols}">
	      <xsl:apply-templates select="*[last()]"/>
	    </td>
	  </tr>
	</table>
      </td>
    </tr>
  </table>
</xsl:template>

<xsl:template match="p:rule" mode="ignore">
<table align="center">
<tr>
<td colspan="2" align="center">
Rule <xsl:value-of select="@name"/>
</td>
</tr>
<xsl:if test="count(*)=1">
<tr>
<td>If</td>
<td></td>
</tr>
</xsl:if>
<xsl:for-each select="*">
<tr  valign="baseline">
<td>
<xsl:choose>
<xsl:when test="position()=last()">then</xsl:when>
<xsl:when test="position()=1">If</xsl:when>
<xsl:otherwise>and</xsl:otherwise>
</xsl:choose>
</td>
<td>
<xsl:apply-templates select="."/>
</td>
</tr>
</xsl:for-each>
</table>
</xsl:template>


<xsl:template match="p:judgement[@name='match']">
  <xsl:apply-templates select="*[1]"/>
  <xsl:text> |- </xsl:text>
  <xsl:apply-templates select="*[2]"/>
  <xsl:text>; </xsl:text>
  <xsl:apply-templates select="*[3]"/>
  <xsl:text> =~ </xsl:text>
  <xsl:apply-templates select="*[4]"/>
</xsl:template>

<xsl:template match="p:judgement[@name='weakMatch']">
  <xsl:apply-templates select="*[1]"/>
  <xsl:text> |- </xsl:text>
  <xsl:apply-templates select="*[2]"/>
  <xsl:text>; </xsl:text>
  <xsl:apply-templates select="*[3]"/>
  <xsl:text> =~</xsl:text>
  <sub>weak</sub>
  <xsl:text> </xsl:text>
  <xsl:apply-templates select="*[4]"/>
</xsl:template>

<xsl:template match="p:judgement[@name='belongs']">
  <xsl:apply-templates select="*[1]"/>
  <xsl:text> in </xsl:text>
  <xsl:apply-templates select="*[2]"/>
</xsl:template>

<xsl:template match="p:judgement[@name='contentType']">
  <xsl:apply-templates select="*[1]"/>
  <xsl:text> </xsl:text><b>:</b><sub>c</sub><xsl:text> </xsl:text>
  <xsl:apply-templates select="*[2]"/>
</xsl:template>


<xsl:template match="p:judgement[@name='subset']">
  <!-- This is a really hacky way to get the parentheses in.  -->
  <xsl:text>(</xsl:text>
  <xsl:apply-templates select="*[1]"/>
  <xsl:text>)</xsl:text>
  <xsl:text> subset </xsl:text>
  <xsl:apply-templates select="*[2]"/>
</xsl:template>

<xsl:template match="p:judgement[@name='interleave']">
  <xsl:apply-templates select="*[1]"/>
  <xsl:text> interleaves </xsl:text>
  <xsl:apply-templates select="*[2]"/>
  <xsl:text>; </xsl:text>
  <xsl:apply-templates select="*[3]"/>
</xsl:template>

<xsl:template match="p:judgement[@name='whiteSpace']">
  <xsl:text>WS( </xsl:text>
  <xsl:apply-templates select="*"/>
  <xsl:text> )</xsl:text>
</xsl:template>

<xsl:template match="p:judgement">
  <xsl:value-of select="@name"/>
  <xsl:text>(</xsl:text>
  <xsl:for-each select="*">
    <xsl:if test="position()!=1">
      <xsl:text>, </xsl:text>
    </xsl:if>
    <xsl:apply-templates select="."/>
  </xsl:for-each>
  <xsl:text>)</xsl:text>
</xsl:template>

<xsl:template match="p:judgement[@name='equal']">
  <xsl:apply-templates select="*[1]"/>
  <xsl:text> = </xsl:text>
  <xsl:apply-templates select="*[2]"/>
</xsl:template>

<xsl:template match="p:not">
   <xsl:text>not(</xsl:text>
     <xsl:apply-templates select="*"/>
   <xsl:text>)</xsl:text>
</xsl:template>

<xsl:template match="p:function">
  <xsl:value-of select="@name"/>
  <xsl:text>( </xsl:text>
  <xsl:for-each select="*">
    <xsl:if test="position() != 1">
      <xsl:text>, </xsl:text>
    </xsl:if>
    <xsl:apply-templates select="."/>
  </xsl:for-each>
  <xsl:text> )</xsl:text>
</xsl:template>

<xsl:template match="p:var">
  <xsl:apply-templates select="@range"/>
  <xsl:if test="@sub">
    <sub><xsl:value-of select="@sub"/></sub>
  </xsl:if>
</xsl:template>

<xsl:template match="p:element">
  <xsl:choose>
    <xsl:when test="not(*[not(self::p:attribute|self::p:context)])">
      <code>&lt;<xsl:value-of select="@name"/></code>
        <xsl:apply-templates select="p:attribute" mode="start-tag"/>
      <code>/&gt;</code>
    </xsl:when>
    <xsl:otherwise>
      <code>&lt;<xsl:value-of select="@name"/></code>
        <xsl:apply-templates select="p:attribute|p:context" mode="start-tag"/>
      <code>&gt;</code>
	<xsl:for-each select="*[not(self::p:attribute|self::p:context)]">
	  <xsl:text> </xsl:text>
	  <xsl:apply-templates select="."/>
	</xsl:for-each>
	<xsl:text> </xsl:text>
      <code>&lt;/<xsl:value-of select="@name"/>&gt;</code>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="p:group">
  <xsl:for-each select="*">
    <xsl:if test="position() != 1">
      <xsl:text> </xsl:text>
    </xsl:if>
    <xsl:apply-templates select="."/>
  </xsl:for-each>
</xsl:template>

<xsl:template match="p:attribute" mode="start-tag">
  <code>
    <xsl:text> </xsl:text>
    <xsl:value-of select="@name"/>
    <xsl:text>="</xsl:text>
  </code>
    <xsl:apply-templates select="*"/>
  <code>"</code>
</xsl:template>

<xsl:template match="p:context" mode="start-tag">
  <code><xsl:text> </xsl:text></code>
  <xsl:apply-templates select="."/>
</xsl:template>

<xsl:template match="p:context">
  <xsl:text>[</xsl:text>
  <xsl:apply-templates select="*"/>
  <xsl:text>]</xsl:text>
</xsl:template>

<xsl:template match="p:attribute//p:string">
  <code><xsl:value-of select="."/></code>
</xsl:template>

<xsl:template match="p:string">
  <code>"<xsl:value-of select="."/>"</code>
</xsl:template>

<xsl:template match="p:attribute//p:function[@name='emptyString']" priority="2">
 <xsl:text></xsl:text>
</xsl:template>

<xsl:template match="p:function[@name='emptyString']">
 <code>""</code>
</xsl:template>

<xsl:template match="@range">
 <i><xsl:value-of select="."/></i>
</xsl:template>

<xsl:template match="@range[.='pattern']">
 <i>p</i>
</xsl:template>

<xsl:template match="@range[.='grammar']">
 <i>g</i>
</xsl:template>

<xsl:template match="@range[.='att']">
 <i>a</i>
</xsl:template>

<xsl:template match="@range[.='element']">
 <i>e</i>
</xsl:template>

<xsl:template match="@range[.='mixed']">
 <i>m</i>
</xsl:template>

<xsl:template match="@range[.='string']">
 <i>s</i>
</xsl:template>

<xsl:template match="@range[.='whiteSpace']">
 <i>ws</i>
</xsl:template>

<xsl:template match="@range[.='nameClass']">
 <i>nc</i>
</xsl:template>

<xsl:template match="@range[.='name']">
 <i>n</i>
</xsl:template>

<xsl:template match="@range[.='ncname']">
 <i>ln</i>
</xsl:template>

<xsl:template match="@range[.='context']">
 <i>cx</i>
</xsl:template>

<xsl:template match="@range[.='contentType']">
 <i>ct</i>
</xsl:template>

<xsl:template match="@range[.='uri']">
 <i>u</i>
</xsl:template>

<xsl:template match="p:function[@name='emptySet']">
 <xsl:text>{ }</xsl:text>
</xsl:template>

<xsl:template match="p:function[@name='emptySequence']">
 <xsl:text>( )</xsl:text>
</xsl:template>

<xsl:template match="p:function[@name='union']">
  <xsl:for-each select="*">
    <xsl:if test="position() != 1">
      <xsl:text> + </xsl:text>
    </xsl:if>
    <xsl:apply-templates select="."/>
  </xsl:for-each>
</xsl:template>

<xsl:template match="p:function[@name='append']">
  <xsl:for-each select="*">
    <xsl:if test="position() != 1">
      <xsl:text>, </xsl:text>
    </xsl:if>
    <xsl:apply-templates select="."/>
  </xsl:for-each>
</xsl:template>

<xsl:template match="p:judgement[@name='start']">
  <xsl:text>start() = </xsl:text>
  <xsl:apply-templates select="*[1]"/>
</xsl:template>

<xsl:template match="p:judgement[@name='bind']">
  <xsl:text>deref(</xsl:text>
  <xsl:apply-templates select="*[1]"/>
  <xsl:text>)</xsl:text>
  <xsl:text> = </xsl:text>
  <code>&lt;element> </code>
  <xsl:apply-templates select="*[2]"/>
  <xsl:text> </xsl:text>
  <xsl:apply-templates select="*[3]"/>
  <code> &lt;/element></code>
</xsl:template>

</xsl:stylesheet>
