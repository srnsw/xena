<?xml version="1.0" encoding="ISO-8859-1"?>

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
							  xmlns:email="http://preservation.naa.gov.au/email/1.0"
							  xmlns:plaintext="http://preservation.naa.gov.au/plaintext/1.0">

	<!-- Main template -->
	<xsl:template match="/">
	  <html>
		  <header>
			<title></title>
		  </header>
		  
		  <body>
			<xsl:apply-templates select="email:email/email:headers" />
			<p>
				<xsl:apply-templates select="//plaintext:plaintext" />
			</p>
			<p>
				<xsl:apply-templates select="email:email/email:parts" />
			</p>
		  </body>
	  </html>
	</xsl:template>
	
	<!-- Headers -->
	<xsl:template match="email:email/email:headers">
		<h2>
			<xsl:value-of select="email:header[@name='Subject']/text()" />
		</h2>
		
		<p>
			<strong>From:</strong><xsl:value-of select="email:header[@name='From']/text()" /><br/>
			<strong>To:</strong><xsl:value-of select="email:header[@name='To']/text()" /><br/>
			<strong>Date:</strong><xsl:value-of select="email:header[@name='Date']/text()" /><br/>
			<strong>MIME-Version:</strong><xsl:value-of select="email:header[@name='MIME-Version']/text()" /><br/>	
		</p>
	</xsl:template>
	
	
	<!-- Message body -->
	<xsl:template match="//plaintext:plaintext">
		<xsl:for-each select="plaintext:line">
			<xsl:value-of select="text()"/><br/>
		</xsl:for-each>
	</xsl:template>
	
	
	<!-- Attachments -->
	<xsl:template match="email:email/email:parts">
		<xsl:if test="email:part/email:attachment">
			<hr/>
			<strong>Attachments:</strong> <br/>
		</xsl:if>
		
		<xsl:for-each select="email:part/email:attachment">
			<a>
				<xsl:attribute name="href">
					<xsl:value-of select="@filename" />
				</xsl:attribute>
				<xsl:value-of select="@filename" />
			</a>
			<br/>
		</xsl:for-each>				
	</xsl:template>


</xsl:stylesheet>