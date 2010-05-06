@echo off
IF EXIST plugins\audio.jar (
	java -cp plugins\audio.jar;xena.jar au.gov.naa.digipres.xena.litegui.LiteMainFrame %*
) ELSE (
	java -jar xena.jar %*
)
