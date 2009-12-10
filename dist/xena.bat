@echo off
IF EXIST plugins\audio.jar (
	java -cp plugins\audio.jar;xena.jar au.gov.naa.digipres.xena.litegui.LiteMainFrame %1 %2 %3 %4 %5 %6 %7 %8 %9
) ELSE (
	java -jar xena.jar %1 %2 %3 %4 %5 %6 %7 %8 %9
)
