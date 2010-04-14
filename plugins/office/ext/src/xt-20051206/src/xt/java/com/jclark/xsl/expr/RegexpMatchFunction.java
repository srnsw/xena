// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.sax.*;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import java.util.regex.Matcher;


import java.net.URL;

/**
 * Represents the EXSL regexp:match function
 * For more information consult exsl specification at: 
 * <A HREF="http://www.exslt.org/regexp/functions/match/regexp.match.html">Specification</A>. 
 */
class RegexpMatchFunction implements Function 
{
    /**
     *
     */
    public ConvertibleExpr makeCallExpr(ConvertibleExpr[] args,
                                        Node exprNode) throws ParseException 
    {
        if (args.length < 2 || args.length > 3) {
            throw new ParseException("expected 2 or 3 arguments");
        }
        final StringExpr se = args[0].makeStringExpr();
        final StringExpr se2 = args[1].makeStringExpr();
        final StringExpr se3 = (args.length == 2
                                ? new LiteralExpr("") : args[2].makeStringExpr());

        return new ConvertibleNodeSetExpr() {
                public NodeIterator eval(Node node, 
                                         ExprContext context) 
                    throws XSLException 
                {
                    return match(node, 
                                 context,
                                 se.eval(node, context),
                                 se2.eval(node, context),
                                 se3.eval(node, context));
                }
            };
    }
    
    /**
     *
     */
    static final private NodeIterator match(Node node, 
                                            ExprContext context,
                                            String src,
					    String pattern, 
					    String flags) 
	throws XSLException 
    {

	try {
	    
	    boolean globalReplace = false;
	    boolean ignoreCase = false;
	    
	    if ( flags.length() > 0 ) {
		globalReplace = flags.indexOf("g") < 0 ? false : true;
		ignoreCase = flags.indexOf("i") < 0 ? false : true;
	    }
	    
	    
	    Node[] groups = new Node[24];

	    Pattern pat = ignoreCase ? Pattern.compile(pattern, Pattern.CASE_INSENSITIVE) : Pattern.compile(pattern);
	    
            Matcher matcher = pat.matcher(src);
            
	    if (matcher.find()) {
                
		int gc = matcher.groupCount();
                
		//System.out.println("matched {" + gc + "} + 1 groups");
		
		for (int i = 0; i < gc + 1; ++i) {
		    
		    Node regexGroupTextNode 
			= new RegexTextNode( matcher.group(i), i, node );
		    
		    groups[i] = regexGroupTextNode;
		}
		
		return new ArrayNodeIterator(groups, 1, gc + 1);
		
	    } else {
                return null;
            }
	    
	} catch (PatternSyntaxException ex) {
	    return null;
	} catch (Exception e) {
	    return null;
	}
	
    }


    static private class RegexTextNode implements Node
    {
	Node _parent;
	
	Node _root;
	
	int _index;  // an identifier based upon node count in document order?
    
	Node _nextSibling;
    
	private String _data;
    
    
	RegexTextNode ( String regexGroupText,
			int index,
			Node parent )
	{
	    _parent = parent;
	    
	    _index = index;
	
	    _data = regexGroupText;
	
	    _root = _parent.getRoot();
	
	    _nextSibling = null;
	
	    // 	if (parent.getLastChild() == null)
	    // 	    parent.getFirstChild() = parent.getLastChild() = this;
	    // 	else {
	    // 	    parent.getLastChild().getNextSibling() = this;
	    // 	    parent.getLastChild() = this;
	    // 	}
	}
    
	public Node getParent() 
	{
	    return _parent;
	}
    
	public SafeNodeIterator getFollowingSiblings() 
	{
	    //FIXME: ?? implement this ?
	    return new RegexNodeIterator(null);
	}
    
	/**
	 * @return the base URI for this document (obtain from root?)
	 */
	public URL getURL() 
	{
	    return _parent.getURL();
	}
    
	boolean canStrip()
	{
	    return false;
	}
    
	public Node getAttribute(Name name) 
	{
	    return null;
	}
    
	public String getAttributeValue(Name name) 
	{
	    return null;
	}
    
	public SafeNodeIterator getAttributes()
	{
	    return new RegexNodeIterator(null);
	}
    
	public SafeNodeIterator getNamespaces()
	{
	    return new RegexNodeIterator(null);
	}
    
	public Name getName() 
	{
	    return null;
	}
    
	public NamespacePrefixMap getNamespacePrefixMap() 
	{
	    return getParent().getNamespacePrefixMap();
	}
    
	public int compareTo(Node node)
	{

	    //FIXME: implement this ?
	
	    // NodeImpl ni = (NodeImpl)node;
	    // 	if (root == ni.root) {
	    // 	    return index - ((Node)node).index;
	    // 	}
	    // 	return root.compareRootTo(ni.root);
	    return -1;
	}

	public Node getElementWithId(String name)
	{
	    return _root.getElementWithId(name);
	}
    
	public String getUnparsedEntityURI(String name)
	{
	    return _root.getUnparsedEntityURI(name);
	}
    
	public boolean isId(String name) 
	{
	    return false;
	}
    
	public String getGeneratedId()
	{
	    int d = _index;//getRoot().getDocumentIndex();
	    if (d == 0) {
		return "N" + String.valueOf(_index);
	    } else {
		return "N" + String.valueOf(d) + "_" + String.valueOf(_index);
	    }
	}
    
	public Node getRoot() {
	    return _root;
	}
    
	// javax.xml.trax.SourceLocator methods
	public int getLineNumber() 
	{
	    return _parent.getLineNumber();
	}
    
	public int getColumnNumber()
	{ return -1; }
    
	public String getSystemId()
	{ 
	    return getRoot().getSystemId();
	}
    
	public String getPublicId()
	{ return null; }

	public byte getType() {
	    return Node.TEXT;
	}
    
	public String getData() {
	    return _data;
	}
    
	public SafeNodeIterator getChildren() {
	    return new RegexNodeIterator(null);
	}
    }
    
    static private class RegexNodeIterator implements SafeNodeIterator 
    {
        private Node nextNode;
        
        RegexNodeIterator(Node nextNode) 
        {
            this.nextNode = nextNode;
        }
        
	public Node next() 
        {
	    return null;
	}
    }
}

