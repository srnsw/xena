<?xml version="1.0" encoding="ISO-8859-1" ?>

<!-- DOCTYPE xsl:stylesheet -->

<!-- XSL Style sheet, DTD omitted -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:lxslt="http://xml.apache.org/xslt"
    xmlns:redirect="org.apache.xalan.lib.Redirect"
    extension-element-prefixes="redirect">

  <xsl:output method="xml"/>
  
  <xsl:param name="xsltcdone" select="'.\XSLTCDONE'"/>
  
  <xsl:template match="Commits">
  <xsl:comment>This XML fragment contains a list of source code updates to place in an &lt;s3&gt; section of readme.xml</xsl:comment>
    <xsl:if test="count(Commit[@category='core'])>0">
      <p> Core (Non-XSLTC) source code updates:</p>
      <ul>
      <xsl:for-each select="Commit[@category='core']">
        <li><xsl:apply-templates select="Who|DateCommitted|Modified|Added|Removed|Log"/></li>
      </xsl:for-each>
      </ul>
    </xsl:if>
    <xsl:if test="count(Commit[@category='core'])=0">
      <note>This release includes no updates of the non-XSLTC core source code.</note>
    </xsl:if>
    
    <xsl:if test="count(Commit[@category='xsltc'])>0">
      <redirect:write file="{$xsltcdone}">
        <p>XSLTC source code updates:</p>
        <ul>
        <xsl:for-each select="Commit[@category='xsltc']">
          <li><xsl:apply-templates select="Who|DateCommitted|Modified|Added|Removed|Log"/></li>
        </xsl:for-each>
        </ul>
      </redirect:write>
    </xsl:if>    
    <xsl:if test="count(Commit[@category='xsltc'])=0">
      <redirect:write file="{$xsltcdone}">
        <note>This release includes no updates of the XSLTC source code.</note>
      </redirect:write>
    </xsl:if>
    
  </xsl:template>
  
  <xsl:template match="Who">
    <ref>Committed by </ref><xsl:value-of select="."/>
  </xsl:template>
  <xsl:template match="DateCommitted">
    <ref> on </ref><xsl:value-of select="."/><br/>
  </xsl:template>    
  <xsl:template match="Modified">    
    <ref>Modified: </ref><xsl:value-of select="."/><br/>
  </xsl:template>    
  <xsl:template match="Added">    
    <ref>Added: </ref><xsl:value-of select="."/><br/>
  </xsl:template>    
  <xsl:template match="Removed">    
    <ref>Removed: </ref><xsl:value-of select="."/><br/>
  </xsl:template>    
    <xsl:template match="Log">    
    <ref>Committer's log entry: </ref><xsl:value-of select="."/><br/><br/>
  </xsl:template>
  
</xsl:stylesheet>