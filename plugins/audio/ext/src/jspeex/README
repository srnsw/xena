Building
--------
If you have apache ant installed, there is a build.xml file in the root
directory. For those of you who are interested in getting the smallest
footprint possible out of this code, there are several build targets which
remove the wideband code to reduce the footprint, and obfuscate the code using
another open source tool, Proguard, which also further reduces the footprint.
With this I have manager to build a Jar file with just the narrowband decoder
that is only 47.7k

Using with JavaSound
----------------------
place the dist/jspeex.jar in your classpath and JavaSound will automatically
have access to the codecs, to read, write and convert the Speex (.spx) files.
It has been tested and works with the jlGui Java Music Player.

Running on the Command line
---------------------------
encoder help: java -cp dist/jspeex.jar JSpeexEnc -h
or: java -jar dist/jspeex.jar -h
decoder help: java -cp dist/jspeex.jar JSpeexDec -h
ex:
encoding a wav file: java -cp dist/jspeex.jar JSpeexEnc input.wav output.spx
decoding any speex file: java -cp dist/jspeex.jar JSpeexDec input.spx output.wav

Credits
-------
First of all I'd like to thank Jean-Marc Valin for giving us all this fantastic
codec. This code is really just a porting of Jean-Marc's code from C to Java.
I'd also like to thank James Lawrence, who wrote the original Java Speex
Decoder, from which this encoder was build.
Finally I'd like to thank all the people who have submitted patches since the
project has started:
Dan Rollo <drollo at ets.org>
William Shubert <wms at igoweb.org>
George Arnhold <armhold at cs.rutgers.edu>
Mark Brown <markbrown at iseinc.biz>

Marc Gimpel <mgimpel at horizonwimba.com>
Director of Research
Horizon Wimba
