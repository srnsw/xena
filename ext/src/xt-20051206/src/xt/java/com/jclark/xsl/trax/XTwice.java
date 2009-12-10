// $Id$

package com.jclark.xsl.trax;

import org.xml.sax.*;
import org.xml.sax.helpers.ParserAdapter;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Result;

import javax.xml.transform.sax.SAXTransformerFactory;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

import java.util.Vector;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import java.io.File;
import java.io.FileDescriptor;
import java.net.URL;

/**
 * A Command line driver program for XT's TrAX API
 */
public class XTwice 
{

    public static void main(String[] args) 
    {
        Transformer transformer = null;

        // use our TrAX factory
        System.setProperty("javax.xml.transform.TransformerFactory", 
                           "com.jclark.xsl.trax.TransformerFactoryImpl");

        SAXTransformerFactory factory = 
            (SAXTransformerFactory) TransformerFactory.newInstance();

        Vector params = new Vector();
        
        // move parameter assignments from the argument list

        int nArgs = 0;
        for (int i = 0; i < args.length; i++) {
            int k = args[i].indexOf('=');
            if (k > 0) {
                params.add(args[i]);
            } else {
                args[nArgs++] = args[i];
            }
        }
        
        if (nArgs != 2 && nArgs != 3) {
            System.err.println("usage: java com.jclark.xsl.trax.XTwice source stylesheet [result] [param=value]...");
            System.exit(1);
        }

        boolean succeeded = true;
        File in = new File(args[0]);

        File stylesheet = new File(args[1]);
        Source styleSource = new StreamSource(stylesheet);

        try {

            transformer = factory.newTransformer(styleSource);

        } catch (TransformerConfigurationException ex) {
            System.err.println("unable to build transformer");
            ex.printStackTrace();
            System.exit(1);
        }

        // set the parameters we grabbed from the command args
        for (int i = params.size() - 1; i >= 0; --i) {
            String paramArg = (String) params.elementAt(i);
            int k = paramArg.indexOf('=');
            transformer.setParameter(paramArg.substring(0, k),
                                     paramArg.substring(k + 1));
        }

        // if our input is a file, do one transform
        if (!in.isDirectory()) {
            Source inputStreamSource = new StreamSource(in);
            Result outputStreamResult = null;;
            if (nArgs == 3) {
                try {
                    FileOutputStream fo = new FileOutputStream(new File(args[2]));
                    outputStreamResult = new StreamResult(fo);
                } catch (FileNotFoundException ex) {
                    System.err.println("Output file " + args[2] + " not found");
                    System.exit(1);
                }
            } else {
                outputStreamResult = new StreamResult(System.out);
            }
            try {
                transformer.transform(inputStreamSource, outputStreamResult);
            } catch (TransformerException ex) {
                System.err.println("unrecoverable TransformerException");
                ex.printStackTrace();
            }
        } else {
            System.out.println("I don't transform directories");

//              // do a buncgh of transforms
//              String[] inFiles = in.list();
//              for (int i = 0; i < inFiles.length; i++) {
//                  File inFile = new File(in, inFiles[i]);
//                  if (!inFile.isDirectory()) {
//                      if (!stylesheet.isDirectory()) {
//                          // FIXME optimize this case by loading the stylesheet only once
//                          if (!transformFile(xsl, outputMethodHandler,
//                                             inFile, stylesheet,
//                                             new File(out, inFiles[i]))) {
//                              succeeded = false;
//                          }
//                      } else {
//                          int ext = inFiles[i].lastIndexOf('.');
//                          File stylesheetFile = 
//                              new File(stylesheet,
//                                       ext < 0
//                                       ? inFiles[i]
//                                       : inFiles[i].substring(0, ext) + ".xsl");
//                          if (stylesheetFile.exists()
//                              && !transformFile(xsl,
//                                                outputMethodHandler,
//                                                inFile,
//                                                stylesheetFile,
//                                                new File(out, inFiles[i]))) {
//                              succeeded = false;
//                          }
//                      }
//                  }
//              }
          }
          if (!succeeded) {
              System.exit(1);
          }

    }

//      static boolean transformFile(XSLProcessor xsl,
//                                   OutputMethodHandlerImpl outputMethodHandler,
//                                   File inputFile,
//                                   File stylesheetFile,
//                                   File outputFile) 
//      {
//          Destination dest;
//          if (outputFile == null) {
//              dest = new FileDescriptorDestination(FileDescriptor.out);
//          } else {
//              dest = new FileDestination(outputFile);
//          }
//          // the processor already knows the outputMethodHandler
//          // now we tell the outputMethodHandler where to write
//          outputMethodHandler.setDestination(dest);
//          return transform(xsl,
//                           fileInputSource(stylesheetFile),
//                           fileInputSource(inputFile));
//      }

//      static boolean transform(XSLProcessor xsl,
//                               InputSource stylesheetSource,
//                               InputSource inputSource) 
//      {
//          try {
//              xsl.loadStylesheet(stylesheetSource);
//              xsl.parse(inputSource);
//              return true;
//          }
//          catch (SAXParseException e) {
//              printSAXParseException(e);
//          }
//          catch (SAXException e) {
//              System.err.println(e.getMessage());
//          }
//          catch (IOException e) {
//              System.err.println(e.toString());
//          }
//          return false;
//      }

    static void printSAXParseException(SAXParseException e) 
    {
        String systemId = e.getSystemId();
        int lineNumber = e.getLineNumber();
        if (systemId != null) {
            System.err.print(systemId + ":");
        }
        if (lineNumber >= 0) {
            System.err.print(lineNumber + ":");
        }
        if (systemId != null || lineNumber >= 0) {
            System.err.print(" ");
        }
        System.err.println(e.getMessage());
    }
    
//      static void setParser(XSLProcessorImpl xsl) 
//      {
//          String parserClass =
//              System.getProperty("com.blnz.xx.sax.reader");
//          if (parserClass == null) {
//              parserClass = System.getProperty("com.blnz.xx.sax.parser");
//          }
//          if (parserClass == null) {
//              parserClass = System.getProperty("org.xml.sax.parser");
//          }
//          if (parserClass == null) {
//              parserClass = "com.jclark.xml.sax.CommentDriver";
//          }
//          try {
//              Object parserObj = Class.forName(parserClass).newInstance();
//              XMLReader reader;
//              if (parserObj instanceof XMLReader) {
//                  reader = (XMLReader) parserObj;
//              } else {
//                  reader = new ParserAdapter((Parser) parserObj);
//              }
//              xsl.setReaders(reader, reader);

//              return;
//          }
//          catch (ClassNotFoundException e) {
//              System.err.println(e.toString());
//          }
//          catch (InstantiationException e) {
//              System.err.println(e.toString());
//          }
//          catch (IllegalAccessException e) {
//              System.err.println(e.toString());
//          }
//          catch (ClassCastException e) {
//              System.err.println(parserClass + " is not a SAX driver");
//          }
//          System.exit(1);
//      }



    static class ErrorHandlerImpl implements ErrorHandler
    {
        public void warning(SAXParseException e) 
        {
            printSAXParseException(e);
        }

        public void error(SAXParseException e) 
        {
            printSAXParseException(e);
        }
        
        public void fatalError(SAXParseException e) throws SAXException 
        {
            throw e;
        }
    }

}
