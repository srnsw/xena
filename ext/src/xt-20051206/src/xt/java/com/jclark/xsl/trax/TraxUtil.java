// $Id$

package com.blnz.xx.trax;

import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.w3c.dom.Node;
import org.w3c.dom.Document;


/**
 * A collection of TrAX related utility methods.
 */
public abstract class TraxUtil
{
//      /**
//       * Create a XmlSource from a javax.xml.transform.Source.
//       */
//      public static XmlSource getXmlSource(Source source) throws TransformerConfigurationException
//      {
//          try
//          {
//              String systemId = source.getSystemId();
//              if (systemId == null)
//                  systemId = "";

//              if (source instanceof StreamSource)
//              {
//                  StreamSource streamSource = (StreamSource)source;
//                  InputSource inputSource = new InputSource(systemId);
//                  inputSource.setByteStream(streamSource.getInputStream());
//                  inputSource.setCharacterStream(streamSource.getReader());
//                  inputSource.setPublicId(streamSource.getPublicId());
//                  return new XmlSource(inputSource);
//              }

//              if (source instanceof SAXSource)
//              {
//                  SAXSource saxSource = (SAXSource)source;
//                  InputSource inputSource = saxSource.getInputSource();
//                  if (inputSource == null)
//                      throw new TransformerConfigurationException("SAXSource does not provide an InputSource");
//                  if (inputSource.getSystemId() == null)
//                      inputSource.setSystemId("");
//                  XmlSource xmlSource = new XmlSource(inputSource);
//                  xmlSource.setXmlReader(saxSource.getXMLReader());
//                  return xmlSource;
//              }

//              if (source instanceof DOMSource)
//              {
//                  Node node = ((DOMSource)source).getNode();
//                  return new XmlSource(systemId, node);
//              }

//              if (source instanceof TraxModelXmlSource)
//              {
//                  return new ModelXmlSource(systemId, ((TraxModelXmlSource)source).getRoot());
//              }
//          }
//          catch(TransformerConfigurationException e)
//          {
//              throw (TransformerConfigurationException)e;
//          }
//          catch(Exception e)
//          {
//              throw new TransformerConfigurationException(e);
//          }

//          throw new TransformerConfigurationException("cannot handle a transformation source of " + source.getClass());
//      }


//      /**
//       * Create a XsltResult from a javax.xml.transform.Result.
//       */
//      public static XsltResult getXsltResult(Result result) throws TransformerConfigurationException
//      {
//          if (result instanceof StreamResult)
//              return getXsltResult((StreamResult)result);

//          if (result instanceof DOMResult)
//              return new XsltResult(getDomResultBuilder((DOMResult)result));

//          if (result instanceof SAXResult)
//              return new XsltResult(getSaxResultBuilder((SAXResult)result));

//          throw new TransformerConfigurationException("cannot handle transformation result " + result.getClass());
//      }


//      private static XsltResult getXsltResult(StreamResult result)
//           throws TransformerConfigurationException
//      {
//          String systemId = result.getSystemId();
//          String uri = (systemId != null) ? systemId : "";

//          // try to use the OutputStream of the StreamResult
//          OutputStream out = result.getOutputStream();
//          if (out != null)
//              return new XsltResult(uri, out, false);

//          // try to use the Writer of the StreamResult
//          Writer writer = result.getWriter();
//          if (writer != null)
//              return new XsltResult(uri, writer, false);

//          if (systemId != null)
//          {
//              // try to derive a file name from the systemId
//              String fileName = systemId;
//              if (fileName.startsWith("file:///"))
//                  fileName = fileName.substring(8);
//              try
//              {
//                  out = new BufferedOutputStream(new FileOutputStream(fileName));
//              }
//              catch(IOException e)
//              {
//                  throw new TransformerConfigurationException(e);
//              }
//              return new XsltResult(uri, out, true);
//          }

//          throw new TransformerConfigurationException("StreamResult has no OutputStream, Writer or SystemId");
//      }


//      /**
//       * Create a ResultBuilder for a DOMResult.
//       */
//      private static ResultBuilder getDomResultBuilder(DOMResult result)
//          throws TransformerConfigurationException
//      {
//          // extract a Node from the result
//          Node node = result.getNode();
//          if (node == null)
//          {
//              node = createDomDocument();
//              result.setNode(node);
//          }

//          Document doc = node.getNodeType() == Node.DOCUMENT_NODE ?
//              (Document)node : node.getOwnerDocument();
//          if (doc == null)
//              throw new TransformerConfigurationException("Node of DOMResult does not have a owner-document");

//          return new DomResultBuilder(doc, node);
//      }


//      private static ResultBuilder getSaxResultBuilder(SAXResult result)
//          throws TransformerConfigurationException
//      {
//          // extract the systemId, a ContentHandler and a LexicalHandler
//          // from the result
//          SAXResult saxResult = (SAXResult)result;
//          String systemId = result.getSystemId();
//          if (systemId == null)
//              systemId = "";
//          ContentHandler contentHandler = saxResult.getHandler();
//          if (contentHandler == null)
//              throw new TransformerConfigurationException("SAXResult does not provide a ContentHandler");
//          return new SaxResultBuilder(systemId, contentHandler, saxResult.getLexicalHandler());
//      }


//      /**
//       * Create a new DOM document.
//       */
//      public static Document createDomDocument() throws TransformerConfigurationException
//      {
//          try
//          {
//              DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
//              return dbfactory.newDocumentBuilder().newDocument();
//          }
//          catch(ParserConfigurationException e)
//          {
//              throw new TransformerConfigurationException(e);
//          }
//      }


//      /**
//       * Create a UriResolver from a Trax URIResolver.
//       */
//      public static UriResolver getUriResolver(URIResolver traxUriResolver)
//      {
//          return (traxUriResolver == null) ? null : new UriResolverAdapter(traxUriResolver);
//      }


//      /**
//       * Convert a qualified name from the Trax representation ([{<uri>}]name)
//       * to jd representation ([<uri>:]name).
//       */
//      public static String convertQualifiedName(String name)
//      {
//          if (name.charAt(0) == '{')
//          {
//              int p = name.charAt('}');
//              if (p == -1)
//                  throw new IllegalArgumentException("cannot extract namespace uri from name '" + name + "'");
//              name = name.substring(1, p) + ':' + name.substring(p + 1);
//          }
//          return name;
//      }

}
