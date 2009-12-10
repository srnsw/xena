#!/bin/bash

# Starts the Viewer application from the Xena jar file

# If the plugins/audio.jar plugin exists then load xena via the 'java -cp' as it is required for audio playback.
if [ -e plugins/audio.jar ]
then
	java -cp plugins/audio.jar:xena.jar au.gov.naa.digipres.xena.viewer.ViewerMainFrame $1
else
	java -cp xena.jar au.gov.naa.digipres.xena.viewer.ViewerMainFrame $1
fi

