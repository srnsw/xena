// $Id$

package com.jclark.xsl.sax;

import java.io.IOException;
import java.io.File;
import java.io.Writer;
import java.net.URL;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.http.*;
import org.xml.sax.*;

/**
 * an example servlet which compiles and caches a stylesheet,
 * then with each "GET" transforms the requested file
 */
public class XSLServlet extends HttpServlet
{
    private XSLProcessor cached;

    public void init() throws ServletException
    {
        String stylesheet = getInitParameter("stylesheet");
        if (stylesheet == null)
            throw new ServletException("missing stylesheet parameter");
        cached = new XSLProcessorImpl();
        cached.setParser(createParser());
        try {
            cached.loadStylesheet(new InputSource(getServletContext().getResource(stylesheet).toString()));
        }
        catch (SAXException e) {
            throw new ServletException(e);
        }
        catch (IOException e) {
            throw new ServletException(e);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        File inputFile = new File(request.getPathTranslated());
        if (!inputFile.isFile()) {
            inputFile = new File(request.getPathTranslated() + ".xml");
            if (!inputFile.isFile()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                                   "File not found: " + request.getPathTranslated());
                return;
            }
        }
        XSLProcessor xsl = (XSLProcessor)cached.clone();
        xsl.setParser(createParser());
        for (Enumeration e = request.getParameterNames(); e.hasMoreElements();) {
            String name = (String)e.nextElement();
            // What to do about multiple values?
            xsl.setParameter(name, request.getParameter(name));
        }
        OutputMethodHandlerImpl outputMethodHandler =
            new OutputMethodHandlerImpl(xsl);
        xsl.setOutputMethodHandler(outputMethodHandler);
        outputMethodHandler.setDestination(new ServletDestination(response));
        try {
            xsl.parse(fileInputSource(inputFile));
        }
        catch (SAXException e) {
            throw new ServletException(e);
        }
    }

    static Parser createParser() throws ServletException
    {
        String parserClass = System.getProperty("com.jclark.xsl.sax.parser");
        if (parserClass == null) {
            parserClass = System.getProperty("org.xml.sax.parser");
        }
        if (parserClass == null) {
            parserClass = "com.jclark.xml.sax.CommentDriver";
        }
        try {
            return (Parser)Class.forName(parserClass).newInstance();
        }
        catch (ClassNotFoundException e) {
            throw new ServletException(e);
        }
        catch (InstantiationException e) {
            throw new ServletException(e);
        }
        catch (IllegalAccessException e) {
            throw new ServletException(e);
        }
        catch (ClassCastException e) {
            throw new ServletException(parserClass + " is not a SAX driver");
        }
    }

    /**
     * Generates an <code>InputSource</code> from a file name.
     */
    static public InputSource fileInputSource(File file)
    {
        String path = file.getAbsolutePath();
        String fSep = System.getProperty("file.separator");
        if (fSep != null && fSep.length() == 1)
            path = path.replace(fSep.charAt(0), '/');
        if (path.length() > 0 && path.charAt(0) != '/')
            path = '/' + path;
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
