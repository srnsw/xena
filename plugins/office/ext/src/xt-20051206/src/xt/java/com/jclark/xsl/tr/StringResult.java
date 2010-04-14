// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

class StringResult implements Result
{
    private Result result;
    private StringBuffer buf = new StringBuffer();

    StringResult()
    {
        this.result = null;
    }

    StringResult(Result result)
    {
        this.result = result;
    }

    void clear() {
        buf.setLength(0);
    }

    public String toString()
    {
        return buf.toString();
    }

    public void characters(String str) throws XSLException
    {
        buf.append(str);
    }

    public void rawCharacters(String str) throws XSLException
    {
        throw new XSLException("illegal use of disable-output-escaping='yes'");
    }

    public void comment(String str) { }

    public void processingInstruction(String target, String data) { }

    public void attribute(Name name, String value) { }

    public void startElement(Name elementType, NamespacePrefixMap nsMap) { }

    public void endElement(Name elementType) { }

    public void start(OutputMethod om) 
    {
        throw new Error("start on StringResult");
    }
    public void end()
    {
        throw new Error("end on StringResult");
    }

    public Result createResult(String uri)
    {
        return null;
    }

    public void message(Node node, String str) throws XSLException
    {
        if (result != null)
            result.message(node, str);
    }
}
