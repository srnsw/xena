// $Id$

package com.jclark.xsl.dom;

import com.jclark.xsl.tr.*;
import com.jclark.xsl.om.*;
import com.jclark.xsl.sax.ExtensionHandlerImpl;
import com.jclark.xsl.sax.MultiNamespaceResult;
import java.net.URL;
import java.io.IOException;

public class XSLTransformEngine 
    implements TransformEngine, XMLProcessor
{
    private Engine engine;
    private DOMExtensions extend;

    public Node load(URL url,
                     int documentIndex,
                     LoadContext context,
                     NameTable nameTable) throws XSLException
    {
        throw new XSLException("external documents not supported");
    }

    public Result createResult(Node baseNode,
                               int documentIndex,
                               LoadContext loadContext,
                               Node[] rootNodeRef) throws XSLException
    {
        if (baseNode == null)
            throw new XSLException("cannot convert result tree fragment returned by extension function to a node-set with the DOM");
        RootNode root = ((NodeBase)baseNode).root;
        org.w3c.dom.DocumentFragment docFrag
            = root.ownerDocument.createDocumentFragment();
        String base = null;
        URL baseURL = baseNode.getURL();
        if (baseURL != null)
            base = baseURL.toString();
        rootNodeRef[0] =
            new RootNode(docFrag,
                         extend,
                         loadContext,
                         engine.getNameTable(),
                         base,
                         documentIndex);
        return new MultiNamespaceResult(new DOMBuilder(docFrag), null);
    }

    public XSLTransformEngine()
    {
        engine = new EngineImpl(this, new ExtensionHandlerImpl());
    }

    public XSLTransformEngine(DOMExtensions extend)
    {
        this();
        this.extend = extend;
    }

    private class TransformImpl implements Transform, ParameterSet
    {
        private Sheet sheet;

        TransformImpl(Sheet sheet) {
            this.sheet = sheet;
        }

        public void transform(org.w3c.dom.Node sourceRoot,
                              org.w3c.dom.Node resultRoot)
            throws TransformException {
            try {
                sheet.process(new RootNode(sourceRoot,
                                           extend,
                                           sheet.getSourceLoadContext(),
                                           engine.getNameTable(),
                                           null,
                                           0),
                              XSLTransformEngine.this,
                              this, // ParameterSet
                              new MultiNamespaceResult(new DOMBuilder(resultRoot),
                                                       null));
            }
            catch (XSLException e) {
                throw toTransformException(e);
            }
        }
        public Object getParameter(Name name) {
            return null;
        }
    }
    
    public Transform createTransform(org.w3c.dom.Node domNode)
        throws TransformException
    {
        try {
            return new TransformImpl(engine.createSheet(new RootNode(domNode,
                                                                     extend,
                                                                     engine.getSheetLoadContext(),
                                                                     engine.getNameTable(),
                                                                     null,
                                                                     0)));
        }
        catch (XSLException e) {
            throw toTransformException(e);
        }
        catch (IOException e) {
            throw new Error("unexpected exception: " + e);
        }
    }
  
    private TransformException toTransformException(XSLException e)
    {
        org.w3c.dom.Node domNode = null;
        Node node = e.getNode();
        if (node != null) {
            domNode = ((NodeBase)node).domNode;
        }
        String message = e.getMessage();
        if (e == null) {
            message = e.getException().toString();
        }
        return new TransformException(message, domNode);
    }
}
