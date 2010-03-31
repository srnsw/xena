<!-- $Id$ -->
<xsl:transform xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
  

  <xsl:template match="/">
    <html>
      <head>
        <title>XML Echo</title>
      </head>
      <body>
        <img src="xmlecho.gif" />
        <xsl:apply-templates select="*"/> 
      </body>

    </html>
  </xsl:template>


  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="@*" />
      <xsl:apply-templates select="*|text()" />
    </xsl:copy>
  </xsl:template>
  
</xsl:transform>