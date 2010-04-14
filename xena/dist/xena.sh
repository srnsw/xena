#!/bin/bash

# Starts the Xena application from the Xena jar file

# If the plugins/audio.jar plugin exists then load xena via the 'java -cp' as it is required for audio playback.
if [ -e plugins/audio.jar ]
then
	java -cp plugins/audio.jar:xena.jar au.gov.naa.digipres.xena.litegui.LiteMainFrame $@
else
	java -jar xena.jar $@
fi
