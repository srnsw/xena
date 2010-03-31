/**************************************************************************
/* This class implements various tests of the im4java-package.
/*
/* Copyright (c) 2009 by Bernhard Bablok (mail@bablokb.de)
/*
/* This program is free software; you can redistribute it and/or modify
/* it under the terms of the GNU Library General Public License as published
/* by  the Free Software Foundation; either version 2 of the License or
/* (at your option) any later version.
/*
/* This program is distributed in the hope that it will be useful, but
/* WITHOUT ANY WARRANTY; without even the implied warranty of
/* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
/* GNU Library General Public License for more details.
/*
/* You should have received a copy of the GNU Library General Public License
/* along with this program; see the file COPYING.LIB.  If not, write to
/* the Free Software Foundation Inc., 59 Temple Place - Suite 330,
/* Boston, MA  02111-1307 USA
/**************************************************************************/

package org.im4java.test;

import java.util.*;
import java.io.*;
import java.awt.image.*;
import javax.imageio.ImageIO;

import org.im4java.core.*;
import org.im4java.process.Pipe;
import org.im4java.process.ArrayListOutputConsumer;
import org.im4java.process.ProcessEvent;
import org.im4java.process.ProcessListener;
import org.im4java.utils.*;


/**
   This class implements various tests of the im4java-package.

   @version $Revision$
   @author  $Author$
 */

public class  Test {

  //////////////////////////////////////////////////////////////////////////////

  /**
     Constructor.
   */

  public  Test() {
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Main-method. You can either pass the number(s) of the test or the
     string "all" for all tests.
   */

  public static void main(String[] args) {
    if (args.length == 0 || args[0].equals("help")) {
      System.err.
      println("usage: java org.im4java.test.Test all | help | nr [...]\n\n" +
          "Available tests:\n" +
          "\t 1: simple use of convert\n" +
          "\t 2: operation and sub-operations\n" +
          "\t 3: montage\n" +
          "\t 4: mixer\n" +
          "\t 5: mogrify\n" +
          "\t 6: identify\n" +
          "\t 7: composite\n" +
          "\t 8: info\n" +
          "\t 9: noise-filter\n" +
          "\t10: piping\n" +
          "\t11: dynamic operation\n" +
          "\t12: Reading BufferedImage\n" +
          "\t13: Writing BufferedImage\n" +
          "\t14: GraphicsMagick\n" +
          "\t15: jpegtran\n" +
          "\t16: asynchronous execution\n" +
          "\t17: ufraw-batch\n" +
          "\t18: exiftool\n" +
          "\t19: dcraw\n" +
          ""
      );
      System.exit(1);
    }
    try {
      Test test = new  Test();
      if (args[0].equals("all")) {
        test.testConvert();
        test.testOperation();
        test.testMontage();
        test.testMixer();
        test.testMogrify();
        test.testIdentify();
        test.testComposite();
        test.testInfo();
        test.testNoiseFilter();
        test.testPipe();
        test.testDynOp();
        test.testReadBufferedImage();
        test.testWriteBufferedImage();
        test.testGraphicsMagick();
        test.testJpegtran();
        test.testAsync();
        test.testUFRaw();
        test.testExiftool();
        test.testDCRaw();
      } else {
        for (int i=0; i<args.length; ++i) {
          int nr = Integer.parseInt(args[i]);
          switch (nr) {
            case 1:
              test.testConvert();
              break;
            case 2:
              test.testOperation();
              break;
            case 3:
              test.testMontage();
              break;
            case 4:
              test.testMixer();
              break;
            case 5:
              test.testMogrify();
              break;
            case 6:
              test.testIdentify();
              break;
            case 7:
              test.testComposite();
              break;
            case 8:
              test.testInfo();
              break;
            case 9:
              test.testNoiseFilter();
              break;
            case 10:
              test.testPipe();
              break;
            case 11:
              test.testDynOp();
              break;
            case 12:
              test.testReadBufferedImage();
              break;
            case 13:
              test.testWriteBufferedImage();
              break;
            case 14:
              test.testGraphicsMagick();
              break;
            case 15:
              test.testJpegtran();
              break;
            case 16:
              test.testAsync();
              break;
            case 17:
              test.testUFRaw();
              break;
            case 18:
              test.testExiftool();
              break;
            case 19:
              test.testDCRaw();
              break;
            default:
              System.err.println("Test Nr " + nr + " not implemented yet!");
            break;
          }
        }
      }
    } catch (CommandException ce) {
      ce.printStackTrace();
      ArrayList<String> cmdError = ce.getErrorText();
      for (String line:cmdError) {
        System.err.println(line);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Basic test of convert.
   */

  public void testConvert() throws Exception {
    System.err.println(" 1. Testing convert ...");
    IMOperation op = new IMOperation();
    op.addImage().addImage();
    op.bordercolor("darkgray");
    op.border(10,10);
    op.appendHorizontally();
    op.addImage("x:");              // output: convert to screen

    String[] images = new String[] {
        "images/tulip1.jpg",
        "images/tulip 2.jpg"
    };

    ConvertCmd convert = new ConvertCmd();
    convert.run(op,(Object[]) images);
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Test adding operations and suboperations.
   */

  public void testOperation() throws Exception {
    System.err.println(" 2. Testing operations and suboperations ...");
    IMOperation top = new IMOperation();

    // first (top) line
    top.addImage("images/rose1.jpg").addImage("images/rose2.jpg");
    top.appendHorizontally();

    // second (bottom) line
    IMOperation bottom = new IMOperation();
    bottom.addImage("images/tulip1.jpg").addImage("images/tulip2.jpg");
    bottom.appendHorizontally();

    // assemble lines
    IMOperation op = new IMOperation();
    op.addSubOperation(top);
    op.addSubOperation(bottom);
    op.appendVertically();

    op.addImage("x:");                      // output: convert to screen
    ConvertCmd convert = new ConvertCmd();
    convert.run(op);
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Basic test of montage.
   */

  public void testMontage() throws Exception {
    System.err.println(" 3. Testing montage ...");
    IMOperation all = new IMOperation();
    all.addImage("images/*.jpg");
    all.addImage("x:");              // output: convert to screen

    MontageCmd montage = new MontageCmd();
    montage.run(all);
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Test using a channel-mixer.
   */

  public void testMixer() throws Exception {
    System.err.println(" 4. Testing channel-mixer ...");
    IMOperation mix = new IMOperation();

    // add image to operation and save in memory-register
    mix.addImage("images/tulip1.jpg");
    mix.write("mpr:orig");

    // convert to BW (special settings)
    mix.openOperation();
    mix.clone(0);
    mix.addOperation(new  ChannelMixer(0,0.12,0.78));
    mix.closeOperation();

    // convert to BW (emulate Ilford PANF film)
    mix.openOperation();
    mix.addImage("mpr:orig");
    mix.addOperation( ChannelMixer.ILFORD_PANF);
    mix.closeOperation();

    // append all images (same as p_append())
    mix.appendHorizontally();

    mix.addImage("x:");                       // output: convert to screen
    ConvertCmd convert = new ConvertCmd();
    convert.run(mix);
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Basic test of mogrify. Note that mogrify uses operators in prefix-notation!
   */

  public void testMogrify() throws Exception {
    System.err.println(" 5. Testing mogrify ...");
    IMOperation op = new IMOperation();
    op.resize(800);
    op.sigmoidalContrast(20d);
    op.addImage("images/firelily.jpg");

    MogrifyCmd mogrify = new MogrifyCmd();
    mogrify.run(op);

    IMOperation dis = new IMOperation();
    dis.addImage("images/firelily.jpg");
    DisplayCmd display = new DisplayCmd();
    display.run(dis);
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Basic test of identify.
   */

  public void testIdentify() throws Exception {
    System.err.println(" 6. Testing identify ...");
    IMOperation op = new IMOperation();
    // op.verbose();
    op.addImage(2);

    IdentifyCmd identify = new IdentifyCmd();
    System.out.println("   first run:");
    identify.run(op,"images/rose1.jpg","images/rose2.jpg");

    System.out.println("   second run:");
    ArrayListOutputConsumer output = new ArrayListOutputConsumer();
    identify.setOutputConsumer(output);
    identify.run(op,"images/tulip1.jpg","images/tulip2.jpg");
    ArrayList<String> cmdOutput = output.getOutput();
    for (String line:cmdOutput) {
      System.out.println(line);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Basic test of composite.   
     composite -blend {src_percent}x{dst_percent} overlay  bgnd  result
   */

  public void testComposite() throws Exception {
    System.err.println(" 7. Testing composite ...");
    IMOperation op = new IMOperation();
    op.blend(50);
    op.addImage(3);

    CompositeCmd composite = new CompositeCmd();
    composite.run(op,"images/rose1.jpg","images/rose2.jpg","x:");
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Test image-information retrivial.
   */

  public void testInfo() throws Exception {
    System.err.println(" 8. Testing info ...");
    Info imageInfo = new Info("images/firelily.jpg");
    Enumeration<String> props = imageInfo.getPropertyNames();
    if (props == null) {
      return;
    }
    while (props.hasMoreElements()) {
      String prop=props.nextElement();
      System.out.println(prop+"="+imageInfo.getProperty(prop));
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Test the noise-filter. Note that the sample images are just too small
     to really see the effect.
   */

  public void testNoiseFilter() throws Exception {
    System.err.println(" 9. Testing noise-filter ...");
    IMOperation op = new IMOperation();

    // add image to operation
    op.addImage("images/spathiphyllum.jpg");

    // set up NoiseFilter.Edge
    IMOperation filterOp = new IMOperation();
    filterOp.despeckle();
    NoiseFilter.Edge noiseFilter = new NoiseFilter.Edge(filterOp,2.0);

    // use NoiseFilter.Edge
    op.openOperation();
    op.clone(0);
    op.addOperation(noiseFilter);
    op.closeOperation();

    // append all images (same as p_append())
    op.appendHorizontally();

    op.addImage("x:");                       // output: convert to screen
    ConvertCmd convert = new ConvertCmd();
    convert.run(op);
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Test piping input and output to an IM command.
   */

  public void testPipe() throws Exception {
    System.err.println("10. Testing pipes ...");

    IMOperation op = new IMOperation();
    op.addImage("-");                   // read from stdin
    op.addImage("tif:-");               // write to stdout in tif-format

    // set up pipe(s): you can use one or two pipe objects
    FileInputStream fis = new FileInputStream("images/ipomoea.jpg");
    FileOutputStream fos = new FileOutputStream("images/ipomoea.tif");
    // Pipe pipe = new Pipe(fis,fos);
    Pipe pipeIn  = new Pipe(fis,null);
    Pipe pipeOut = new Pipe(null,fos);

    // set up command
    ConvertCmd convert = new ConvertCmd();
    convert.setInputProvider(pipeIn);
    convert.setOutputConsumer(pipeOut);
    convert.run(op);
    fis.close();
    fos.close();

    // show result
    DisplayCmd.show("images/ipomoea.tif");
  }

  ///////////////////////////////////////////////////////////////////////////////

  /**
   */

  private void testDynOp() throws Exception {
    System.err.println("11. Testing dynamic operations ...");

    IMOperation op = new IMOperation();
    // add image to operation
    op.addImage();

    // add -despeckle only if iso > 200
    op.openOperation();
    op.clone(0);
    op.addDynamicOperation(new DynamicOperation() {
      public Operation resolveOperation(Object... pImages)
      throws IM4JavaException {
        // we just care about the first image
        if (pImages.length>0) {

          // we use identify to query the iso-setting
          IMOperation iso = new IMOperation();
          iso.ping().format("%[EXIF:ISOSpeedRatings]\n");
          String img = (String) pImages[0];
          iso.addImage(img);
          IdentifyCmd identify = new IdentifyCmd();
          ArrayListOutputConsumer output = new ArrayListOutputConsumer();
          identify.setOutputConsumer(output);
          try {
            identify.run(iso);
          } catch (Exception e) {
            throw new IM4JavaException(e);
          }

          // now read the setting
          ArrayList<String> out = output.getOutput();
          int isoValue = Integer.parseInt(out.get(0));
          if (isoValue > 200) {
            IMOperation op = new IMOperation();
            op.despeckle();
            return op;
          } else {
            return null;
          }
        } else {
          return null;
        }
      }
    });
    op.closeOperation();
    op.appendHorizontally();

    // now run the command
    op.addImage();
    ConvertCmd convert = new ConvertCmd();
    convert.run(op,"images/firelily.jpg","x:");
    convert.run(op,"images/tulip1.jpg","x:");
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Test image-conversion from BufferedImages.
   */

  public void testReadBufferedImage() throws Exception {
    System.err.println("12. Testing reading BufferedImages ...");

    IMOperation op = new IMOperation();
    op.addImage();                        // input
    op.blur(2.0).paint(10.0);
    op.addImage();                        // output


    // set up command
    ConvertCmd convert = new ConvertCmd();
    BufferedImage img = ImageIO.read(new File("images/tulip1.jpg"));
    convert.run(op,img,"images/buf2file.jpg");

    // show result
    DisplayCmd.show("images/buf2file.jpg");
    (new File("images/buf2file.jpg")).delete();
  }  

  //////////////////////////////////////////////////////////////////////////////

  /**
     Test image-conversion to BufferedImages. Note that we need an
     OutputConsumer to pipe the result to the BufferedImage.
   */

  public void testWriteBufferedImage() throws Exception {
    System.err.println("13. Testing writing BufferedImages ...");

    IMOperation op = new IMOperation();
    op.addImage("images/tulip2.jpg");     // input
    op.blur(2.0).paint(10.0);
    op.addImage("png:-");                 // output: stdout


    // set up command
    ConvertCmd convert = new ConvertCmd();
    Stream2BufferedImage s2b = new Stream2BufferedImage();
    convert.setOutputConsumer(s2b);
    convert.run(op);

    // save result to disk
    BufferedImage img = s2b.getImage();
    ImageIO.write(img,"PNG",new File("images/tmpfile.png"));


    // show result
    DisplayCmd.show("images/tmpfile.png");
    (new File("images/tmpfile.png")).delete();
  }  

  //////////////////////////////////////////////////////////////////////////////

  /**
     Basic test of GraphicsMagick.
   */

  public void testGraphicsMagick() throws Exception {
    System.err.println("14. Testing GraphicsMagick's gm convert ...");
    IMOperation op = new IMOperation();
    op.addImage().addImage();
    op.bordercolor("darkgray");
    op.border(10,10);
    op.appendHorizontally();
    op.addImage("x:");              // output: convert to screen

    String[] images = new String[] {
        "images/rose1.jpg",
        "images/rose2.jpg"
    };

    ConvertCmd convert = new ConvertCmd(true);
    try {
      convert.run(op,(Object[]) images);
    } catch (CommandException ce) {
      // gm convert with x: as output always returns 1
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Test of jpegtran.
   */

  public void testJpegtran() throws Exception {
    System.err.println("15. Testing jpegtran ...");
    JPTOperation op = new JPTOperation();
    op.flip("horizontal");
    op.outfile(Operation.IMG_PLACEHOLDER);
    op.addImage();                            // input-filename

    JpegtranCmd jpegtran = new JpegtranCmd();
    jpegtran.run(op,"images/tulip2-flip.jpg","images/tulip2.jpg");

    DisplayCmd.show("images/tulip2-flip.jpg");
    (new File("images/tulip2-flip.jpg")).delete();
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Test of asynchronous execution.
   */

  public void testAsync() throws Exception {
    System.err.println("16. Testing asynchronous execution ...");
    IMOperation op = new IMOperation();
    op.size(400,200);
    op.addImage("gradient:red","x:");

    ConvertCmd convert = new ConvertCmd();
    convert.setAsyncMode(true);

    // helper-class defined at the end of this file
    AsyncTestProcessListener pl = new AsyncTestProcessListener();
    convert.addProcessListener(pl);
    convert.run(op);

    // loop until the user finishes the operation
    for (int i=0; i<10; ++i) {
      System.err.println("sleeping for one second...");
      Thread.sleep(1000);
      if (!pl.isRunning()) {
	break;
      }
    }
    pl.destroy();
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Test of ufraw-batch.
   */

  public void testUFRaw() throws Exception {
    System.err.println("17. Testing ufraw ...");
    String outfile="images/rawfile.tif";
    String infile=System.getProperty("im4java.testUFRaw.infile");
    if (infile == null) {
      System.err.println(
         "\nSkipping this test since input-file is not defined.\n" +
         "Set the system-property im4java.testUFRaw.infile to\n" +
         "your input-file for ufraw:\n" +
         "\tpass JAVA_OPTS=-Dim4java.testUFRaw.infile=... to \"make test\" or\n" +
         "\texport JAVA_OPTS=-Dim4java.testUFRaw.infile=...\n\n"
      );
      return;
    }

    UFRawOperation op = new UFRawOperation();
    op.exposure("auto");
    op.outType("tif");
    op.size(800);
    op.createId("no");
    op.overwrite();
    op.output(outfile);
    op.addImage(infile);                                 // input-filename

    UFRawCmd ufraw = new UFRawCmd(true);                 // use batch-mode
    ufraw.run(op);

    DisplayCmd.show(outfile);
    (new File(outfile)).delete();
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Test of exiftool.
   */

  public void testExiftool() throws Exception {
    System.err.println("18. Testing exiftool ...");

    ETOperation op = new ETOperation();
    op.getTags("Filename","ImageWidth","ImageHeight","FNumber",
                                                           "ExposureTime","iso");
    op.addImage();

    // setup command and execute it (capture output)
    ArrayListOutputConsumer output = new ArrayListOutputConsumer();
    ExiftoolCmd et = new ExiftoolCmd();
    et.setOutputConsumer(output);
    et.run(op,"images/spathiphyllum.jpg");

    // dump output
    ArrayList<String> cmdOutput = output.getOutput();
    for (String line:cmdOutput) {
      System.out.println(line);
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  /**
     Test of dcraw.
   */

  public void testDCRaw() throws Exception {
    System.err.println("19. Testing dcraw ...");
    String outfile="images/rawfile.tif";
    String infile=System.getProperty("im4java.testDcraw.infile");
    if (infile == null) {
      System.err.println(
         "\nSkipping this test since input-file is not defined.\n" +
         "Set the system-property im4java.testDcraw.infile to\n" +
         "your input-file for dcraw:\n" +
         "\tpass JAVA_OPTS=-Dim4java.testDcraw.infile=... to \"make test\" or\n" +
         "\texport JAVA_OPTS=-Dim4java.testDcraw.infile=...\n\n"
      );
      return;
    }

    DCRAWOperation op = new DCRAWOperation();
    op.halfSize();
    op.createTIFF();
    op.write2stdout();
    op.addImage(infile);                                 // input-filename

    // create pipe for output
    FileOutputStream fos = new FileOutputStream(outfile);
    Pipe pipeOut = new Pipe(null,fos);

    // set up and run command
    DcrawCmd dcraw = new DcrawCmd();
    dcraw.setOutputConsumer(pipeOut);
    dcraw.run(op);
    fos.close();

    DisplayCmd.show(outfile);
    (new File(outfile)).delete();
  }

}

////////////////////////////////////////////////////////////////////////////////

/**
   Helper-class for method Test.asyncTest. A real-life application would do
   something more sensible like update a GUI.
*/

class AsyncTestProcessListener implements ProcessListener {
  private Process iProcess     = null;
  private boolean isTerminated = false;

  // save the started process
  public void processStarted(Process pProcess) {
    isTerminated=false;
    iProcess = pProcess;
  }

  // print return-code or stack-trace
  public void processTerminated(ProcessEvent pEvent) {
    synchronized(iProcess) {
      iProcess = null;
    }
    isTerminated = true;
    if (pEvent.getException() != null) {
      Exception e = pEvent.getException();
      System.err.println("Process terminated with: " + e.getMessage());
    } else {
      System.out.println("async process terminated with rc: " +
			 pEvent.getReturnCode());
    }
  }

  // check if thread is still running
  public boolean isRunning() {
    return !isTerminated;
  }

  // destroy running process (this will trigger an execption which
  // is passed to processTerminated())
  public void destroy() {
    try {
      synchronized(iProcess) {
	iProcess.destroy();
      }
    } catch (Exception e) {
    }
  }
}