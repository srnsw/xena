@echo off
IF EXIST plugins\audio.jar (
	java -cp plugins\audio.jar;xena.jar au.gov.naa.digipres.xena.viewer.ViewerMainFrame %*
) ELSE (
	java -cp xena.jar au.gov.naa.digipres.xena.viewer.ViewerMainFrame %*
)

