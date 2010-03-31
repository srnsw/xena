// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

/**
 * <p>As transformation proceeds, "Actions" are performed,
 * and an output tree is constructed.  Rather than building
 * the output tree as an object model in memory, it is represented
 * as the sequence of events that could be used to construct
 * the tree.  These events may indeed be used to build such
 * a model, as is the case with result tree fragments, or
 * they may be directly serialized to the output.</p>
 *
 * <p> a Result is the object which recieves these events,
 * then serializes them or builds an object model, as appropriate.</p>
 *
 */
public interface Result
{
    /**
     * build a TEXT node
     */
    void characters(String str) throws XSLException;

    /**
     * Some (possibly) non XML characters
     */
    void rawCharacters(String str) throws XSLException;


    /**
     * Start constructing an Element
     * (NB) The nsMap must declare the prefix on elementType correctly.
     */
    void startElement(Name elementType,
                      NamespacePrefixMap nsMap) throws XSLException;

    /**
     *  Finish constructing an Element
     */
    void endElement(Name elementType) throws XSLException;

    /**
     * Construct a comment
     */
    void comment(String str) throws XSLException;

    /**
     * Construct a Processing Instruction
     */
    void processingInstruction(String target, String data) throws XSLException;

    /**
     * Construct an Attribute ... Unlike SAX, we don't have the
     * luxury of having all the Attributes present when the 
     * Element is started. Some may be constructed later as
     * a consequence of an <code>xsl:attribute</code> for example.
     */
    void attribute(Name name, String value) throws XSLException;

    /**
     * Prepare to start constructing stuff. ... take care of
     * any initialization tasks.
     *
     * @param outputMethod whatever the stylesheeet has told us
     *   about how it wants the trasnformed results output
     */
    void start(OutputMethod outputMethod) throws XSLException;

    /**
     * Finish constructing stuff. 
     */
    void end() throws XSLException;

    /**
     * Create a new Result object for serializing to
     * the destination uri.  Provides support for the
     * "xt:document" extension element
     */
    Result createResult(String uri) throws XSLException;

    /**
     * Support the <code>xsl:message</code> element.
     *
     * @param node The source context node under consideration
     *   when the message action is performed. May be used for
     *   locator information
     */
    void message(Node node, String str) throws XSLException;
}
