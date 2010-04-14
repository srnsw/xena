<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
							  xmlns:mailbox="http://preservation.naa.gov.au/mailbox/1.0">

<!-- Main template -->
<xsl:template match="/">
  <html>
	  <header>
		<title>Xena Mailbox Export</title>
	  </header>
	  
	  <body>
	  
		<h2>
			Xena Mailbox Export
		</h2>
		
		This is the index file of an export of an email mailbox file normalised by Xena. Each entry below represents a message contained
		in the original mailbox.
		
		<p>
			<xsl:apply-templates select="mailbox:mailbox" />
		</p>
		
	  </body>
  </html>	
</xsl:template>

<!-- Messages -->
<xsl:template match="mailbox:mailbox">
		
		<xsl:for-each select="mailbox:item">
			<a>
				<xsl:attribute name="href">
					<xsl:value-of select="text()" />
				</xsl:attribute>
				<xsl:value-of select="text()" />
			</a>
			<br/>
		</xsl:for-each>
				
</xsl:template>

</xsl:stylesheet>