@echo off
IF EXIST plugins\audio.jar (
	java -cp plugins\audio.jar;xena.jar au.gov.naa.digipres.xena.viewer.ViewerMainFrame %1
) ELSE (
	java -cp xena.jar au.gov.naa.digipres.xena.viewer.ViewerMainFrame %1
)

