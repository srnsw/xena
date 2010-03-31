// $Id$

package com.jclark.xsl.sax;

import org.xml.sax.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileDescriptor;
import java.net.URL;

/**
 * A Command line driver program for XSLProcessor
 */
public class Driver 
{
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

    public static void main(String[] args) 
    {
        XSLProcessorImpl xsl = new XSLProcessorImpl();
        setParser(xsl);
        xsl.setErrorHandler(new ErrorHandlerImpl());
        
        int nArgs = 0;
        for (int i = 0; i < args.length; i++) {
            int k = args[i].indexOf('=');
            if (k > 0) {
                xsl.setParameter(args[i].substring(0, k),
                                 args[i].substring(k + 1));
            } else {
                args[nArgs++] = args[i];
            }
        }
        
        if (nArgs != 2 && nArgs != 3) {
            System.err.println("usage: java com.jclark.xsl.sax.Driver source stylesheet [result] [param=value]...");
            System.exit(1);
        }

        // create something to receive SAX events from the processor
        OutputMethodHandlerImpl outputMethodHandler = 
            new OutputMethodHandlerImpl(xsl);
        xsl.setOutputMethodHandler(outputMethodHandler);

        boolean succeeded = true;
        File in = new File(args[0]);
        File stylesheet = new File(args[1]);
        File out = nArgs == 3 ? new File(args[2]) : null;

        // if it's a file, do one transform
        if (!in.isDirectory()) {
            succeeded = transformFile(xsl, outputMethodHandler, in, stylesheet, out);
        } else {
            // do a buncgh of transforms
            String[] inFiles = in.list();
            for (int i = 0; i < inFiles.length; i++) {
                File inFile = new File(in, inFiles[i]);
                if (!inFile.isDirectory()) {
                    if (!stylesheet.isDirectory()) {
                        // FIXME optimize this case by loading the stylesheet only once
                        if (!transformFile(xsl, outputMethodHandler,
                                           inFile, stylesheet,
                                           new File(out, inFiles[i]))) {
                            succeeded = false;
                        }
                    } else {
                        int ext = inFiles[i].lastIndexOf('.');
                        File stylesheetFile = 
                            new File(stylesheet,
                                     ext < 0
                                     ? inFiles[i]
                                     : inFiles[i].substring(0, ext) + ".xsl");
                        if (stylesheetFile.exists()
                            && !transformFile(xsl,
                                              outputMethodHandler,
                                              inFile,
                                              stylesheetFile,
                                              new File(out, inFiles[i]))) {
                            succeeded = false;
                        }
                    }
                }
            }
        }
        if (!succeeded) {
            System.exit(1);
        }
    }

    static boolean transformFile(XSLProcessor xsl,
                                 OutputMethodHandlerImpl outputMethodHandler,
                                 File inputFile,
                                 File stylesheetFile,
                                 File outputFile) 
    {
        Destination dest;
        if (outputFile == null) {
            dest = new FileDescriptorDestination(FileDescriptor.out);
        } else {
            dest = new FileDestination(outputFile);
        }
        // the processor already knows the outputMethodHandler
        // now we tell the outputMethodHandler where to write
        outputMethodHandler.setDestination(dest);
        return transform(xsl,
                         fileInputSource(stylesheetFile),
                         fileInputSource(inputFile));
    }

    static boolean transform(XSLProcessor xsl,
                             InputSource stylesheetSource,
                             InputSource inputSource) 
    {
        try {
            xsl.loadStylesheet(stylesheetSource);
            xsl.parse(inputSource);
            return true;
        }
        catch (SAXParseException e) {
            printSAXParseException(e);
        }
        catch (SAXException e) {
            System.err.println(e.getMessage());
        }
        catch (IOException e) {
            System.err.println(e.toString());
        }
        return false;
    }

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

    static void setParser(XSLProcessorImpl xsl) 
    {
        String parserClass =
            System.getProperty("com.jclark.xsl.sax.parser");
        if (parserClass == null) {
            parserClass = System.getProperty("org.xml.sax.parser");
        }
        if (parserClass == null) {
            parserClass = "com.jclark.xml.sax.CommentDriver";
        }
        try {
            Object parserObj = Class.forName(parserClass).newInstance();
            if (parserObj instanceof XMLProcessorEx) {
                xsl.setParser((XMLProcessorEx)parserObj);
            } else {
                xsl.setParser((Parser)parserObj);
            }
            return;
        }
        catch (ClassNotFoundException e) {
            System.err.println(e.toString());
        }
        catch (InstantiationException e) {
            System.err.println(e.toString());
        }
        catch (IllegalAccessException e) {
            System.err.println(e.toString());
        }
        catch (ClassCastException e) {
            System.err.println(parserClass + " is not a SAX driver");
        }
        System.exit(1);
    }

    /**
     * Generates an <code>InputSource</code> from a file name.
     */
    static public InputSource fileInputSource(String str)
    {
        return fileInputSource(new File(str));
    }

    static public InputSource fileInputSource(File file) 
    {
        String path = file.getAbsolutePath();
        String fSep = System.getProperty("file.separator");
        if (fSep != null && fSep.length() == 1) {
            path = path.replace(fSep.charAt(0), '/');
        }
        if (path.length() > 0 && path.charAt(0) != '/') {
            path = '/' + path;
        }
        try {
            return new InputSource(new URL("file", "", path).toString());
        }
        catch (java.net.MalformedURLException e) {
            /* According to the spec this could only happen if the file
               protocol were not recognized. */
            throw new Error("unexpected MalformedURLException");
        }
    }

}
