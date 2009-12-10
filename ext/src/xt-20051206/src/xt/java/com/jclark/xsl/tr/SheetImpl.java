// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;

import com.jclark.xsl.expr.Pattern;
import com.jclark.xsl.expr.ExprParser;
import com.jclark.xsl.expr.StringExpr;
import com.jclark.xsl.expr.TopLevelPattern;
import com.jclark.xsl.expr.NodeSetExpr;
import com.jclark.xsl.expr.BooleanExpr;
import com.jclark.xsl.expr.VariantExpr;
import com.jclark.xsl.expr.Variant;
import com.jclark.xsl.expr.StringVariant;
import com.jclark.xsl.expr.NumberVariant;
import com.jclark.xsl.expr.VariableSet;
import com.jclark.xsl.expr.EmptyVariableSet;
import com.jclark.xsl.expr.ExtensionContext;

import com.jclark.xsl.util.Comparator;
import com.jclark.xsl.util.BilevelComparator;
import com.jclark.xsl.util.TextComparator;
import com.jclark.xsl.util.NumberComparator;
import com.jclark.xsl.util.ReverseComparator;

import com.jclark.xsl.sax.SaxFilterMaker;

import java.io.IOException;

import java.net.URL;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Locale;

import java.text.Collator;

/**
 * Actually does the work of compiling the stylesheet's object model
 *  into the template rules, actions, variable definitions, etc.
 *
 *  The constructor compiles the stylesheet, the method "process()"
 *  runs the transformation
 */
class SheetImpl 
    implements SheetDetails, LoadContext, NamespaceConstants
{
    private SaxFilterMaker _xrap = null;
    
    private ActionDebugTarget _debugger = null;

    private ImportantBoolean stripSource = null;

    private Hashtable stripSourceElementTable = null;

    private Hashtable stripSourceNamespaceTable = null;

    private TemplateRuleSet templateRules = 
        new TemplateRuleSet(new BuiltinAction());

    private Hashtable modeTable = new Hashtable();

    private Hashtable namedTemplateTable = new Hashtable();

    private Hashtable variableInfoTable = new Hashtable();

    private Hashtable userFunctionTable = new Hashtable();

    private Hashtable attributeSetTable = new Hashtable();

    private Hashtable namespaceAliasTable = new Hashtable();

    private Hashtable keysDefinitionsTable = new Hashtable();

    private Vector idAttributes = new Vector();

    private static final Action emptyAction = new EmptyAction();

    private Hashtable topLevelTable = new Hashtable();

    private Hashtable actionTable = new Hashtable();

    Importance currentImportance = Importance.create();

    int importCount = 0;

    static NodeSetExpr childrenExpr = ExprParser.getChildrenExpr();

    VariableSet currentLocalVariables = new EmptyVariableSet();

    int nCurrentLocalVariables = 0;

    Vector excludedNamespaces;

    XMLProcessor _omBuilder;

    static StringVariant emptyStringVariant = new StringVariant("");

    Name XSL_WHEN;
    Name XSL_OTHERWISE;
    Name XSL_STYLESHEET;
    Name XSL_TRANSFORM;
    Name XSL_WITH_PARAM;
    Name XSL_SORT;
    Name XSL_FOR_EACH;
    Name XSL_FALLBACK;
    Name XSL_VERSION;
    Name XSL_VENDOR;
    Name XSL_VENDOR_URL;
    Name XSL_USE_ATTRIBUTE_SETS;
    Name XSL_ATTRIBUTE;

    // stylesheet attribute names
    Name ATTRIBUTE;
    Name CASE_ORDER;
    Name CDATA_SECTION_ELEMENTS;
    Name COUNT;
    Name DATA_TYPE;
    Name DEFAULT;
    Name DISABLE_OUTPUT_ESCAPING;
    Name DOCTYPE_PUBLIC;
    Name DOCTYPE_SYSTEM;
    Name ELEMENTS;
    Name ENCODING;
    Name EXCLUDE_RESULT_PREFIXES;
    Name FORMAT;
    Name FROM;
    Name GROUPING_SEPARATOR;
    Name GROUPING_SIZE;
    Name HREF;
    Name INDENT;
    Name LANG;
    Name LETTER_VALUE;
    Name LEVEL;
    Name MATCH;
    Name MEDIA_TYPE;
    Name METHOD;
    Name MODE;
    Name NAME;
    Name NAMESPACE;
    Name OMIT_XML_DECLARATION;
    Name ORDER;
    Name PRIORITY;
    Name RESULT_PREFIX;
    Name SELECT;
    Name STANDALONE;
    Name STYLESHEET_PREFIX;
    Name TERMINATE;
    Name TEST;
    Name USE;
    Name USE_ATTRIBUTE_SETS;
    Name VALUE;
    Name VERSION;

    // output method properties from the stylesheet
    OutputMethodImpl _outputMethod;

    // output method properties with overrides from client code
    OutputMethodImpl _runtimeOutputMethod = null;

    LoadContext sheetLoadContext;
    NameTable nameTable;
    ExtensionHandler extensionHandler;
  
    /**
     * The constructor builds (compiles) a stylesheet
     * @param node the (xslt D) OM root Node of the loaded stylesheet
     * @param omBuilder a (xslt D) OM builder we can use for 
     *               included/imported sheets
     * @param extensionHandler for extensions?
     * @param sheetLoadContext ??
     * @param nameTable ??
     */
    SheetImpl(Node node, XMLProcessor omBuilder, 
              ExtensionHandler extensionHandler,
	      LoadContext sheetLoadContext, 
              NameTable nameTable)
	throws IOException, XSLException
    {
	this.sheetLoadContext = sheetLoadContext;
	this.nameTable = nameTable;
	this._omBuilder = omBuilder;
	this.extensionHandler = extensionHandler;
        _debugger =  sheetLoadContext.getDebugger();

	XSL_WHEN = xsl("when");
	XSL_OTHERWISE = xsl("otherwise");
	XSL_STYLESHEET = xsl("stylesheet");
	XSL_TRANSFORM = xsl("transform");
	XSL_WITH_PARAM = xsl("with-param");
	XSL_SORT = xsl("sort");
	XSL_FOR_EACH = xsl("for-each");
	XSL_FALLBACK = xsl("fallback");
	XSL_VERSION = xsl("version");
	XSL_VENDOR = xsl("vendor");
	XSL_VENDOR_URL = xsl("vendor-url");
	XSL_USE_ATTRIBUTE_SETS = xsl("use-attribute-sets");
	XSL_ATTRIBUTE = xsl("attribute");

        // no namespace for these attribute names
	ATTRIBUTE = nameTable.createName("attribute");
	CASE_ORDER = nameTable.createName("case-order");
	CDATA_SECTION_ELEMENTS = nameTable.createName("cdata-section-elements");
	COUNT = nameTable.createName("count");
	DATA_TYPE = nameTable.createName("data-type");
	DEFAULT = nameTable.createName("default");
	DISABLE_OUTPUT_ESCAPING = 
            nameTable.createName("disable-output-escaping");
	DOCTYPE_PUBLIC = nameTable.createName("doctype-public");
	DOCTYPE_SYSTEM = nameTable.createName("doctype-system");
	ELEMENTS = nameTable.createName("elements");
	ENCODING = nameTable.createName("encoding");
	EXCLUDE_RESULT_PREFIXES = 
            nameTable.createName("exclude-result-prefixes");
	FORMAT = nameTable.createName("format");
	FROM = nameTable.createName("from");
	GROUPING_SEPARATOR = nameTable.createName("grouping-separator");
	GROUPING_SIZE = nameTable.createName("grouping-size");
	HREF = nameTable.createName("href");
	INDENT = nameTable.createName("indent");
	LANG = nameTable.createName("lang");
	LETTER_VALUE = nameTable.createName("letter-value");
	LEVEL = nameTable.createName("level");
	MATCH = nameTable.createName("match");
	MEDIA_TYPE = nameTable.createName("media-type");
	METHOD = nameTable.createName("method");
	MODE = nameTable.createName("mode");
	NAME = nameTable.createName("name");
	NAMESPACE = nameTable.createName("namespace");
	OMIT_XML_DECLARATION = nameTable.createName("omit-xml-declaration");
	ORDER = nameTable.createName("order");
	PRIORITY = nameTable.createName("priority");
	RESULT_PREFIX = nameTable.createName("result-prefix");
	SELECT = nameTable.createName("select");
	STANDALONE = nameTable.createName("standalone");
	STYLESHEET_PREFIX = nameTable.createName("stylesheet-prefix");
	TERMINATE = nameTable.createName("terminate");
	TEST = nameTable.createName("test");
	USE = nameTable.createName("use");
	USE_ATTRIBUTE_SETS = nameTable.createName("use-attribute-sets");
	VALUE = nameTable.createName("value");
	VERSION = nameTable.createName("version");
    
	topLevelTable.put(xsl("include"), new IncludeParser());
	topLevelTable.put(xsl("import"), new ImportParser());
	topLevelTable.put(xsl("template"), new TemplateParser());
	topLevelTable.put(xsl("attribute-set"), new AttributeSetParser());
	topLevelTable.put(xsl("decimal-format"), new DecimalFormatParser());
	topLevelTable.put(xsl("key"), new KeyParser());
	topLevelTable.put(xsl("variable"), new VariableTopLevelParser());
	topLevelTable.put(xsl("param"), new ParamTopLevelParser());
	topLevelTable.put(xsl("strip-space"), new StripSpaceParser());
	topLevelTable.put(xsl("preserve-space"), new PreserveSpaceParser());
	topLevelTable.put(xsl("output"), new OutputParser());
	topLevelTable.put(xsl("namespace-alias"), new NamespaceAliasParser());
	topLevelTable.put(func("function"), new EXSLFunctionTopLevelParser());

	actionTable.put(xsl("text"), new TextParser());
	actionTable.put(xsl("apply-templates"), new ApplyTemplatesParser());
	actionTable.put(xsl("call-template"), new CallTemplateParser());
	actionTable.put(xsl("for-each"), new ForEachParser());
	actionTable.put(xsl("value-of"), new ValueOfParser());
	actionTable.put(xsl("number"), new NumberParser());
	actionTable.put(xsl("if"), new IfParser());
	actionTable.put(xsl("choose"), new ChooseParser());
	actionTable.put(xsl("copy"), new CopyParser());
	actionTable.put(xsl("copy-of"), new CopyOfParser());
	actionTable.put(xsl("comment"), new CommentParser());
	actionTable.put(xsl("processing-instruction"), 
                        new ProcessingInstructionParser());
	actionTable.put(xsl("element"), new ElementParser());
	actionTable.put(xsl("attribute"), new AttributeParser());
	actionTable.put(xsl("apply-imports"), new ApplyImportsParser());
	actionTable.put(xsl("variable"), new VariableActionParser());
	actionTable.put(xsl("param"), new ParamActionParser());
	actionTable.put(xsl("message"), new MessageParser());

	actionTable.put(xt("document"), new DocumentParser());

        actionTable.put(exsl("document"), new DocumentParser());

        actionTable.put(func("result"), new EXSLFunctionResultParser());

	actionTable.put(xfyxt("xrap"), new XRAPParser());

        _outputMethod = new OutputMethodImpl();

        // ... and now, do something big, 
        // compile from the OM into something we can execute

	parseSheet(node);

    }


    /**
     * sets the xrap processor for use during processing
     */
    public void setSaxExtensionFilter(SaxFilterMaker xrap)
    { _xrap = xrap; }


    /**
     * gets the xrap processor for use during processing
     */
    public SaxFilterMaker getSaxExtensionFilter()
    { return _xrap; }


    /**
     * sets the xrap processor for use during processing
     */
    public void setDebugger(ActionDebugTarget debugger)
    { _debugger = debugger; }


    /**
     * gets the xrap processor for use during processing
     */
    public ActionDebugTarget getDebugger()
    { return _debugger; }


    //////////////////////////////////////////////////////////
    //
    //
    //


    /**
     * returns the current values set either by client code,
     * the stylesheet, or defaulted for the output method properties
     */
    public OutputMethod getOutputMethod() 
    {
        return _runtimeOutputMethod == null ? _outputMethod : _runtimeOutputMethod;
    }

    /**
     * returns the current values set either by client code,
     * the stylesheet, or defaulted for the output method properties
     * see java.xml.transform.Transform.getOutputMethodProperties()
     */
    public Properties getOutputMethodProperties()
    {
        // FIXME
        return null;
    }

    /**
     * resets the output method to just those properties which were
     * set in the stylesheet and/or defaults
     */
    public void clearOutputMethodProperties()
    {
        _runtimeOutputMethod = null;
    }

    /**
     * override the stylesheet's or default value for an
     * output method property
     */
    public void setOutputMethodProperty(String encodedPropertyNamespace, 
                                        String encodedPropertyName, String value)
        throws XSLException
    {
        if (_runtimeOutputMethod == null) {
            _runtimeOutputMethod = (OutputMethodImpl) _outputMethod.clone();
        }
        Name name = (encodedPropertyNamespace == null) ?
            nameTable.createName(encodedPropertyName) :
            nameTable.createName("omp:" + encodedPropertyName,
                                 encodedPropertyNamespace);
        
        // null node for namespace prefix mapping
        _runtimeOutputMethod.setSpecifiedValue(name, value, null);     
    }

    /**
     * returns the current value of the named property
     */ 
    public String getOutputMethodProperty(String encodedPropertyNamespace,
                                          String encodedPropertyName)
    {
        OutputMethod om = ( _runtimeOutputMethod == null) ?
            _outputMethod :
            _runtimeOutputMethod;
        
        Name name = (encodedPropertyNamespace == null) ?
            nameTable.createName(encodedPropertyName) :
            nameTable.createName("omp:" + encodedPropertyName,
                                 encodedPropertyNamespace);

        return om.getSpecifiedValue(name);
    }
    //
    //
    //
    /////////////////////////////////////////////////////////

    /**
     * process an XML input document against this stylesheet
     * @param node the source document
     * @param omBuilder an object model builder we can (re-)use for e.g. "document()"
     * @param params the XSLT run-time parameters
     * @param root the destination for thetransformation results
     */
    public Result process(Node node, XMLProcessor omBuilder,
                          ParameterSet params, Result root) 
        throws XSLException
    {
	root.start(_runtimeOutputMethod == null ? 
                   _outputMethod :
                   _runtimeOutputMethod);

	ProcessContextImpl pci = 
            new ProcessContextImpl(this, node, omBuilder, params);

        pci.processSafe(node, null, root);

	root.end();

	return root;
    }
    
    // parse the om representation of the stylesheet
    // in order to build a compiled sheet
    private void parseSheet(Node rootNode) 
        throws XSLException, IOException
    {

        if (rootNode == null) {
            throw new XSLException("null document rootNode");
        }
        // get the root Element
	Node sheetNode = rootNode.getChildren().next();  

        if (sheetNode == null) {
            throw new XSLException("no root element");
        }
        
        // make sure it's an XSLT stylesheet if it's in the XSL namespace
	if (XSL_NAMESPACE.equals(sheetNode.getName().getNamespace())) {
	    if (!XSL_STYLESHEET.equals(sheetNode.getName())
		&& !XSL_TRANSFORM.equals(sheetNode.getName()))
		throw new XSLException("bad document element for stylesheet",
				       sheetNode);
            
            // now start our recursive descent
	    parseTopLevel(sheetNode);

	} else {
            // the root element wasn't in the XSL namespace, so ...
	    parseRootTemplate(rootNode);
        }
 	currentImportance.set(importCount);

	templateRules.compile();

	for (Enumeration iter = modeTable.elements() ; 
             iter.hasMoreElements() ; ) {
	    ((TemplateRuleSet)iter.nextElement()).compile();
        }
    }

    //
    //
    //
    private Vector getExcludedNamespaces(Node node) throws XSLException
    {
	String prefixList = node.getAttributeValue(EXCLUDE_RESULT_PREFIXES);
	if (prefixList == null)
	    return null;
	Vector v = new Vector();
	StringTokenizer iter = new StringTokenizer(prefixList);
	while (iter.hasMoreElements()) {
	    String prefix = (String)iter.nextElement();
	    if (prefix == null) {
		break;
            }
	    v.addElement(getPrefixNamespace(node, prefix));
	}
	if (v.size() == 0) {
	    return null;
        }
	return v;
    }

    /**
     * @return  the namespace URI reference for the given prefix
     *   in scope at the given Node
     */
    String getPrefixNamespace(Node node, String prefix) throws XSLException
    {
	NamespacePrefixMap map = node.getNamespacePrefixMap();
	String ns = map.getNamespace(prefix);
	if (ns == null) {
	    if (prefix.equals("#default")) {
		ns = map.getDefaultNamespace();
            }
	    if (ns == null) {
		throw new XSLException("undefined prefix", node);
            }
	}
	return ns;
    }

    /**
     * come here if we have a "xsl:stylesheet" root element
     * expect XSLT elements permitted at the top level ... params, 
     * templates, keys, etc.
     */
    void parseTopLevel(Node sheetNode) throws XSLException, IOException
    {

        //        System.out.println("parseTopLevel:: {" + sheetNode.getName().getNamespace() + "}" +  sheetNode.getName().getLocalPart());


	Vector saveExcludedNamespaces = excludedNamespaces;
	excludedNamespaces = getExcludedNamespaces(sheetNode);

	// hmmm ... should we really silently ignore some elements?
	for (NodeIterator iter = sheetNode.getChildren();;) {
	    Node node = iter.next();
	    if (node == null) {
		break;
            }
	    TopLevelParser parser = null;
	    try {
		Name name = node.getName();

		if (name == null) {
		    throw new XSLException("illegal data characters inside xsl:stylesheet", node);
                }
		parser = (TopLevelParser)topLevelTable.get(name);
		if (parser == null && name.getNamespace() == null) {
		    throw new XSLException("illegal top-level element", node);
                }
	    }
	    catch (ClassCastException e) { }
	    if (parser != null) {
		parser.parse(node);
            }
	}
	excludedNamespaces = saveExcludedNamespaces;
    }

    //
    // handles a stylesheet with no "xsl:stylesheet" or "xsl:transform" 
    //
    private void parseRootTemplate(Node defNode) throws XSLException
    {

        //        System.out.println("parseRootTemplate:: {" + defNode.getName().getNamespace() + "}" +  defNode.getName().getLocalPart());

	templateRules.add(ExprParser.parsePattern(defNode, "/"),
			  currentImportance,
			  null,
			  parseActions(defNode, emptyAction));
    }

    /**
     * Parse the attributes on node as literal attributes and then
     * parse the actions. 
     */
    Action parseAttributesAndActions(Node node) throws XSLException
    {

        //        System.out.println("parseAttributesAndActions:: {" + node.getName().getNamespace() + "}" +  node.getName().getLocalPart());

	AppendAction sequence = parseUseAttributeSets(node, true, null);

	for (NodeIterator iter = node.getAttributes();;) {
	    Node att = iter.next();
	    if (att == null) {
		break;
            }
	    if (sequence == null) {
		if (_debugger != null) {
                    sequence = new AppendActionDebug(_debugger, node, "");
                } else {
                    sequence = new AppendAction();
                }
            }
	    String value = att.getData();
	    Name name = att.getName();

	    if (XSL_NAMESPACE.equals(name.getNamespace())) {
		continue; // FIXME error checking
            }

	    if (value.indexOf('{') >= 0 || value.indexOf('}') >= 0) {
		sequence.add(new TemplateAttributeAction(name,
							 ExprParser.parseValueExpr(node, value, currentLocalVariables)));
            } else {
		sequence.add(new LiteralAttributeAction(name, value));
            }
        }
	return parseActions(node, null, sequence);
    }

    /**
     *
     */
    Action parseUseAttributeSetsAndActions(Node node) throws XSLException
    {
	return parseActions(node,
			    emptyAction,
			    parseUseAttributeSets(node, false, null));
    }

    /**
     *
     */
    AppendAction parseUseAttributeSets(Node node, boolean literal, 
				       AppendAction sequence) throws XSLException
    {
	String value = node.getAttributeValue(literal
					      ? XSL_USE_ATTRIBUTE_SETS
					      : USE_ATTRIBUTE_SETS);
	if (value != null) {
	    for (StringTokenizer iter = new StringTokenizer(value);
		 iter.hasMoreElements() ;  ) {

		if (sequence == null) {
		    sequence = new AppendAction();
                }
		sequence.add(new UseAttributeSetAction(expandSourceElementTypeName((String)iter.nextElement(),node)));
	    }
	}
	return sequence;
    }

    /**
     *
     */
    Action parseActions(Node node, Action ifEmpty) throws XSLException
    {
        //        System.out.println("parseActions1:: {" + node.getName().getNamespace() + "}" +  node.getName().getLocalPart());
        return parseActions(node, ifEmpty, null);
    }
  
    /**
     *
     */
    Action parseActions(Node node, Action ifEmpty, 
                        AppendAction sequence) throws XSLException
    {
        //        System.out.println("parseActions2:: {" + node.getName().getNamespace() + "}" +  node.getName().getLocalPart());

	final VariableSet startLocalVariables = currentLocalVariables;
	final int nStartLocalVariables = nCurrentLocalVariables;
	NodeIterator iter = node.getChildren();
	node = iter.next();
	if (node == null) {
	    if (sequence == null) {
		return ifEmpty;
	    } else {
		return sequence;
            }
	}
	if (sequence == null) {
	    if (_debugger != null) {
                sequence = new AppendActionDebug(_debugger, node, "");
            } else {
                sequence = new AppendAction();
            }
        }
	do {
	    switch (node.getType()) {
	    case Node.TEXT:
		if (_debugger != null) {
                    sequence.add(new CharsActionDebug(_debugger, node, "",
                                                      node.getData()));
                } else {
                    sequence.add(new CharsAction(node.getData()));
                }
		break;
	    case Node.ELEMENT:
		{
		    Name name = node.getName();
		    String ns = name.getNamespace();
		    if (!XSL_NAMESPACE.equals(ns) && 
                        !XT_NAMESPACE.equals(ns) &&
                        !XFYXT_NAMESPACE.equals(ns)) {
			if (_debugger != null) {
                            sequence.add(new LiteralElementActionDebug(_debugger,
                                                                       node, "",
                                                                       node.getName(),
                                                                       literalNamespacePrefixMap(node),
                                                                       parseAttributesAndActions(node)));
                        } else {
                            sequence.add(new LiteralElementAction(node.getName(),
                                                                  literalNamespacePrefixMap(node),
                                                                  parseAttributesAndActions(node)));
                        }
		    } else {
			ActionParser actionParser = null;
			try {
			    actionParser = (ActionParser)actionTable.get(name);
			} catch (ClassCastException e) { 

                        }
			if (actionParser == null) {
			    if (name.equals(XSL_SORT)
				&& XSL_FOR_EACH.equals(node.getParent().getName()))
				;
			    else if (name.equals(XSL_FALLBACK))
				; // FIXME error checking
			    else
				throw new XSLException("expected action not " + name, node);
			} else {
			    sequence.add(actionParser.parse(node));
                        }
		    }
		}
	    }
	    node = iter.next();
	} while (node != null);

	// FIXME: should use finally here
	if (nStartLocalVariables != nCurrentLocalVariables) {
	    sequence.add(new UnbindLocalVariablesAction(nCurrentLocalVariables -
                                                        nStartLocalVariables));
	    nCurrentLocalVariables = nStartLocalVariables;
	    currentLocalVariables = startLocalVariables;
	}
	return sequence;
    }

    /**
     *
     */
    String getRequiredAttribute(Node node, Name name) 
        throws XSLException 
    {
	String value = node.getAttributeValue(name);
	if (value == null) {
	    throw new XSLException("missing attribute \"" + 
                                   name + "\"", node);
        }
	return value;
    }

    /**
     *
     */
    String getOptionalAttribute(Node node, Name name, String dflt)
    {
	String value = node.getAttributeValue(name);
	return value == null ? dflt : value;
    }

    /**
     *
     */
    String getData(Node node) throws XSLException
    {
	node = node.getChildren().next();
	if (node == null) {
	    return "";
        }
	String data = node.getData();
	if (data == null) {
	    throw new XSLException("only character data allowed", node);
        }
	return data;
    }

    /**
     *
     */
    NumberListFormatTemplate getNumberListFormatTemplate(Node node) 
        throws XSLException
    {
	NumberListFormatTemplate t = new NumberListFormatTemplate();

	String value = node.getAttributeValue(FORMAT);
	if (value != null) {
	    StringExpr expr = 
                ExprParser.parseValueExpr(node, value,
                                          currentLocalVariables);
	    String format = expr.constantValue();
	    if (format != null) {
		t.setFormat(format);
	    } else {
		t.setFormat(expr);
            }
        }

	// FIXME should be able to use attribute value templates for these
	value = node.getAttributeValue(LANG);
	if (value != null)
	    t.setLang(value);
	value = node.getAttributeValue(LETTER_VALUE);
	if (value != null)
	    t.setLetterValue(value);
	value = node.getAttributeValue(GROUPING_SIZE);
	if (value != null) {
	    try {
		t.setGroupingSize(Integer.parseInt(value));
	    }
	    catch (NumberFormatException e) { }
	}
	value = node.getAttributeValue(GROUPING_SEPARATOR);
	if (value != null)
	    t.setGroupingSeparator(value);
	return t;
    }

    /**
     *
     */
    Action addParams(ParamAction action, Node node) 
        throws XSLException
    {
	for (NodeIterator iter = node.getChildren() ; ; ) {
	    node = iter.next();
	    if (node == null) {
		break;
            }
	    if (XSL_WITH_PARAM.equals(node.getName()))
		action.addParam(expandSourceElementTypeName(getRequiredAttribute(node, NAME), 
                                                            node),
				getVariantExpr(node));
	}
	return action;
    }

    NodeSetExpr getSortNodeSetExpr(Node node, 
                                   NodeSetExpr expr) throws XSLException
    {
	ComparatorTemplate comparatorTemplate = null;
	for (NodeIterator iter = node.getChildren();;) {
	    node = iter.next();
	    if (node == null) {
		break;
            }
	    if (XSL_SORT.equals(node.getName())) {
		Locale locale = Lang.getLocale(node.getAttributeValue(LANG));
		Comparator cmp;
		String dataType = node.getAttributeValue(DATA_TYPE);
		if ("number".equals(dataType)) {
		    cmp = new NumberComparator();
		} else {
		    int caseOrder = 0;
		    String caseOrderString =
                        node.getAttributeValue(CASE_ORDER);

		    if ("upper-first".equals(caseOrderString)) {
			caseOrder = TextComparator.UPPER_FIRST;
		    } else if ("lower-first".equals(caseOrderString)) {
			caseOrder = TextComparator.LOWER_FIRST;
                    }
		    cmp = TextComparator.create(locale, caseOrder);
		}
		if ("descending".equals(node.getAttributeValue(ORDER)))
		    cmp = new ReverseComparator(cmp);
		ComparatorTemplate templ 
		    = new NodeComparatorTemplate(cmp,
						 ExprParser.parseStringExpr(node,
									    getOptionalAttribute(node, SELECT, "."), currentLocalVariables));
		if (comparatorTemplate == null)
		    comparatorTemplate = templ;
		else
		    comparatorTemplate = new BilevelComparatorTemplate(comparatorTemplate, templ);
	    }
	}
	if (comparatorTemplate == null)
	    return expr;
	return new SortNodeSetExpr(expr, comparatorTemplate);
    }

    /**
     * gets the value (an expression to be evaluated later) to be bound for a variable or parameter
     */
    VariantExpr getVariantExpr(Node defNode) throws XSLException
    {
	String expr = defNode.getAttributeValue(SELECT);

	if (defNode.getChildren().next() == null) {
	    if (expr == null) {
		expr = "\"\"";
            }
	} else {
	    if (expr != null) {
		throw new XSLException("non-empty content with select attribute", defNode);
            }

	    // OPT optimize case when there's a single text node child;
	    // optimize case when the children consist of text nodes
	    // and xsl:value-of elements
	    return new ResultFragmentExpr(parseActions(defNode, emptyAction),
					  defNode,
					  extensionHandler);
	}
	return ExprParser.parseVariantExpr(defNode, expr, 
                                           currentLocalVariables);
    }

    /**
     * obtain the collection of templates which may be
     * applied in a named Mode
     */
    public TemplateRuleSet getModeTemplateRuleSet(Name modeName)
    {
	if (modeName == null) {
            // default, unnamed mode
	    return templateRules;
        }
	TemplateRuleSet ruleSet = (TemplateRuleSet)modeTable.get(modeName);
	if (ruleSet == null) {
	    ruleSet = new TemplateRuleSet(new BuiltinAction(modeName));
	    modeTable.put(modeName, ruleSet);
	}
	return ruleSet;
    }

    /**
     * obtain the definition of the named key
     */
    public KeyDefinition getKeyDefinition(Name keyName)
    {
	return (KeyDefinition) keysDefinitionsTable.get(keyName);
    }

    // shortcut for creating names in the xsl namespace
    private Name xsl(String name)
    {
	return nameTable.createName("xsl:" + name, XSL_NAMESPACE);
    }
  
    // shortcut for creating names in the exslt namespace
    private Name exsl(String name)
    {
	return nameTable.createName("exsl:" + name, EXSL_COMMON_NAMESPACE);
    }

    // shortcut for creating names in the exslt function (http://exslt.org/functions) namespace
    private Name func(String name)
    {
	return nameTable.createName("func:" + name, EXSL_FUNCTIONS_NAMESPACE);
    }

    private Name regexp(String name)
    {
	return nameTable.createName("regexp:" + name, EXSL_REGEXP_NAMESPACE);
    }

    // shortcut for creating names in the xt namespace
    private Name xt(String name)
    {
	return nameTable.createName("xt:" + name, XT_NAMESPACE);
    }

    // shortcut for creating names in the xfyxt namespace
    private Name xfyxt(String name)
    {
	return nameTable.createName("xfyxt:" + name, XFYXT_NAMESPACE);
    }


    private boolean namespaceExcluded(String ns)
    {
	if (ns == null) {
	    return false;
        }
	if (ns.equals(XSL_NAMESPACE) || ns.equals(XT_NAMESPACE) ||
            ns.equals(XFYXT_NAMESPACE)) {
	    return true;
        }
	if (excludedNamespaces == null) {
	    return false;
        }
	int len = excludedNamespaces.size();
	for (int i = 0; i < len; i++) {
	    if (excludedNamespaces.elementAt(i).equals(ns)) {
		return true;
            }
        }
        return false;
    }

    //
    private NamespacePrefixMap literalNamespacePrefixMap(Node node)
    {
	NamespacePrefixMap map = node.getNamespacePrefixMap();
	NamespacePrefixMap newMap = map;
	String ns = map.getDefaultNamespace();
	if (namespaceExcluded(ns)) {
	    newMap = newMap.unbindDefault();
        }
	int size = map.getSize();

	for (int i = 0; i < size; i++) {
	    ns = map.getNamespace(i);
	    if (namespaceExcluded(ns)) {
		newMap = newMap.unbind(map.getPrefix(i));
            }
	}
	return newMap;
    }

    //
    static Name expandSourceElementTypeName(String nameString, 
                                            Node node) 
        throws XSLException
    {
        // WDL Debug
        if (node == null) {
            throw new XSLException("no node for prefix map");
        }
        NamespacePrefixMap pm = node.getNamespacePrefixMap();
        if (pm == null) {
            throw new XSLException("no prefix map");
        }
        Name n = pm.expandAttributeName(nameString, node);
        if (n == null) {
            throw new XSLException("no name for {" + nameString + "}");
        }
    
	return node.getNamespacePrefixMap().expandAttributeName(nameString,
                                                                node);
    }

    /**
     *
     */
    public LoadContext getSourceLoadContext()
    {
	return this;
    }

    /**
     *
     */
    public boolean getStripSource(Name elementTypeName)
    {
	ImportantBoolean match = stripSource;
	if (stripSourceNamespaceTable != null) {
	    String ns = elementTypeName.getNamespace();
	    if (ns != null) {
		ImportantBoolean ib = 
                    (ImportantBoolean)stripSourceNamespaceTable.get(ns);
		if (ib != null && 
                    (match == null || 
                     ib.getImportance().compareTo(match.getImportance()) >= 0)){
		    match = ib;
                }
	    }
	}
	if (stripSourceElementTable != null) {
	    ImportantBoolean ib = 
                (ImportantBoolean)stripSourceElementTable.get(elementTypeName);
	    if (ib != null && 
                (match == null || 
                 ib.getImportance().compareTo(match.getImportance()) >= 0)) {
		match = ib;
            }
	}
	return match != null ? match.getBoolean() : false;
    }

    /**
     * @return true
     */
    public boolean getIncludeComments()
    {
	return true;
    }

    /**
     * @return true
     */
    public boolean getIncludeProcessingInstructions()
    {
	return true;
    }

    /**
     *
     */
    public VariableInfo getGlobalVariableInfo(Name name)
    {
	return (VariableInfo)variableInfoTable.get(name);
    }

    /**
     *
     */
    public Variant getSystemProperty(Name name)
    {
        // FIXME: maybe I ought to change this 
	if (name.equals(XSL_VERSION)) {
	    return new NumberVariant(1.0);
        }
	if (name.equals(XSL_VENDOR)) {
	    return new StringVariant("James Clark");
        }
	if (name.equals(XSL_VENDOR_URL)) {
	    return new StringVariant("http://www.jclark.com/");
        }
	return emptyStringVariant;
    }

    /**
     *
     */
    public ExtensionContext createExtensionContext(String namespace) 
        throws XSLException
    {
	return extensionHandler.createContext(namespace);
    }

    /**
     *
     */
    public Action getAttributeSet(Name name)
    {
	return (Action)attributeSetTable.get(name);
    }

    /**
     *
     */
    public boolean haveNamespaceAliases()
    {
	return namespaceAliasTable.size() != 0;
    }
  
    /**
     *
     */
    public String getNamespaceAlias(String ns)
    {
	if (ns == null) {
	    return null;
	}
	return (String)namespaceAliasTable.get(ns);
    }

    /////////////////////////////////////////////////////////////
    //
    // internal class definitions
    //
    //
    //////////////////////////////////////////////////////////

    //
    // for parsing XSLT stylesheet top-level elements
    //
    private interface TopLevelParser
    {
        void parse(Node node) throws XSLException, IOException;
    }

    //
    // hanldles an xslt "include" (and "import")
    //
    private class IncludeParser implements TopLevelParser
    {
        public void parse(Node ruleNode) throws XSLException, IOException
        {

            Node sheetNode = 
                _omBuilder.load(new URL(ruleNode.getURL(),
                                        getRequiredAttribute(ruleNode,
                                                             HREF)),
                                0, 
                                sheetLoadContext,
                                nameTable).getChildren().next();
            
            if (XSL_NAMESPACE.equals(sheetNode.getName().getNamespace())) {
                if (!XSL_STYLESHEET.equals(sheetNode.getName())
                    && !XSL_TRANSFORM.equals(sheetNode.getName()))
                    throw new XSLException("bad document element for stylesheet",
                                           sheetNode);
                parseTopLevel(sheetNode);
            }
            else {
                parseRootTemplate(sheetNode);
            }
        }
    }

    // parses "xsl:import"
    private class ImportParser extends IncludeParser
    {
        public void parse(Node ruleNode) throws XSLException, IOException
        {
            //    System.out.println("importParser:parse(): {" + ruleNode.getName().getNamespace() + "}" +  ruleNode.getName().getLocalPart());

            // HST: simplified and fixed the way import precedence is recorded
            //Importance zero = Importance.create();
            Importance oldCurrentImportance = currentImportance;
 	    currentImportance = Importance.create();

 	    //System.err.println("< xsl:import: "+currentImportance.compareTo(zero));
            super.parse(ruleNode);
 	    currentImportance.set(importCount);

 	    //System.err.println("> xsl:import: "+currentImportance.compareTo(zero));
 	    importCount++;
            currentImportance = oldCurrentImportance;
        }
    }
    
    // parses "xsl:template"
    private class TemplateParser implements TopLevelParser
    {
        public void parse(Node defNode) throws XSLException
        {

            //            System.out.println("TemplateParser:parse(): {" + defNode.getName().getNamespace() + "}" +  defNode.getName().getLocalPart());
            String name = defNode.getAttributeValue(NAME);
            Action contents = parseActions(defNode, emptyAction);

            if (name != null) {
                namedTemplateTable.put(expandSourceElementTypeName(name, 
                                                                   defNode),
                                       contents);
            }

            String pattern = defNode.getAttributeValue(MATCH);
            if (pattern == null) {

                // no match pattern, presumably only reached
                // through xsl:call-template
                return;
            }

            String modeString = defNode.getAttributeValue(MODE);

            TemplateRuleSet ruleSet;  // based upon mode

            if (modeString != null) {
                ruleSet = 
                    getModeTemplateRuleSet(expandSourceElementTypeName(modeString,
                                                                       defNode));
            } else {
                ruleSet = templateRules;
            }
            try {
                ruleSet.add(ExprParser.parsePattern(defNode, pattern),
                            currentImportance,
                            Priority.create(defNode.getAttributeValue(PRIORITY)),
                            contents);
            }
            catch (NumberFormatException e) {
                throw new XSLException("invalid priority", defNode);
            }
        }
    }
    
    // parses "xsl:attribute-set"
    private class AttributeSetParser implements TopLevelParser
    {
        public void parse(Node defNode) throws XSLException
        {
            Name name = 
                expandSourceElementTypeName(getRequiredAttribute(defNode,
                                                                 NAME),
                                            defNode);

            AppendAction action = (AppendAction)attributeSetTable.get(name);
            if (action == null) {
                action = new AppendAction();
                attributeSetTable.put(name, action);
            }
            parseUseAttributeSets(defNode, false, action);
            for (SafeNodeIterator iter = defNode.getChildren();;) {
                Node node = iter.next();
                if (node == null) {
                    break;
                }
                if (!XSL_ATTRIBUTE.equals(node.getName()))
                    throw new XSLException("only xsl:attribute allowed inside xsl:attribute-set", node);
            }
            parseActions(defNode, null, action);
        }
    }
    
    // parses "xsl:key"
    private class KeyParser implements TopLevelParser
    {
        public void parse(Node defNode) throws XSLException
        {

            String pattern = getRequiredAttribute(defNode, MATCH);
	    TopLevelPattern matchPattern = ExprParser.parsePattern(defNode, pattern);

            String name = getRequiredAttribute(defNode, NAME);
            Name expname = expandSourceElementTypeName(name, defNode);

	    String use = getRequiredAttribute(defNode, USE);

	    // FIXME: -- need to check enforcment of no variable refs constraint?
	    StringExpr useExpr = 
		ExprParser.parseStringExpr(defNode, use, currentLocalVariables);

	    KeyDefinition kd = new KeyDefinition(expname, matchPattern, useExpr);
	    keysDefinitionsTable.put(expname, kd);
        }
    }
    
    // parses "xsl:decimal-format"
    private class DecimalFormatParser implements TopLevelParser
    {
        public void parse(Node defNode) throws XSLException
        {
            // FIXME
        }
    }
    
    // parses "xsl:variable" top level elements
    private class VariableTopLevelParser implements TopLevelParser
    {
        public void parse(Node defNode) throws XSLException
        {
            //            System.out.println("VariableTopLevelParser:parse1(): {" + defNode.getName().getNamespace() + "}" +  defNode.getName().getLocalPart());
            parse(defNode, false);
        }
        
        void parse(Node defNode, boolean isParam) throws XSLException
        {

            //            System.out.println("VariableTopLevelParser:parse2(): {" + defNode.getName().getNamespace() + "}" +  defNode.getName().getLocalPart());
            variableInfoTable
                .put(expandSourceElementTypeName(getRequiredAttribute(defNode,
                                                                      NAME), 
                                                 defNode),
                     new VariableInfo(getVariantExpr(defNode),
                                      isParam));
        }
    }
    
    // parses "xsl:param" top level element
    private class ParamTopLevelParser extends VariableTopLevelParser
    {
        public void parse(Node defNode) throws XSLException 
        {
            parse(defNode, true);
        }
    }
    
    // parses EXSLT "func:function" top level elements
    private class EXSLFunctionTopLevelParser implements TopLevelParser
    {
        public void parse(Node defNode) throws XSLException
        {
            parse(defNode, false);
        }
        
        void parse(Node defNode, boolean isParam) throws XSLException
        {
            userFunctionTable
                .put(expandSourceElementTypeName(getRequiredAttribute(defNode,
                                                                      NAME), 
                                                 defNode),
                     // FIXME what kinda object:
                     new VariableInfo(getVariantExpr(defNode),
                                      isParam));
        }
    }
    

    // parses "xsl:strip-space" top level element
    private class StripSpaceParser implements TopLevelParser
    {
        public void parse(Node node) throws XSLException
        {
            parse(node, true);
        }
        
        void parse(Node node, boolean strip) throws XSLException
        {
            StringTokenizer iter
                = new StringTokenizer(getRequiredAttribute(node, ELEMENTS));
            ImportantBoolean ib = new ImportantBoolean(strip, currentImportance);
            while (iter.hasMoreElements()) {
                String str = (String)iter.nextElement();
                Name name = expandSourceElementTypeName(str, node);

                if (name.getLocalPart().equals("*")) {
                    String ns = name.getNamespace();
                    if (ns == null)
                        stripSource = ib;
                    else {
                        if (stripSourceNamespaceTable == null)
                            stripSourceNamespaceTable = new Hashtable();
                        stripSourceNamespaceTable.put(ns, ib);
                    }
                }
                else {
                    if (stripSourceElementTable == null)
                        stripSourceElementTable = new Hashtable();
                    stripSourceElementTable.put(name, ib);
                }
            }
        }
    }
    
    // parses "xsl:preserve-space" top level element
    private class PreserveSpaceParser extends StripSpaceParser
    {
        public void parse(Node node) throws XSLException 
        {
            parse(node, false);
        }
    }
    
    // parses "xsl:output" top level element
    private class OutputParser implements TopLevelParser
    {
        public void parse(Node node) throws XSLException
        {
            _outputMethod.merge(node);
        }
    }
    
    // parses xsl:namespace-alias top level element
    private class NamespaceAliasParser implements TopLevelParser
    {
        public void parse(Node node) throws XSLException
        {
            namespaceAliasTable.
                put(getPrefixNamespace(node,
                                       getRequiredAttribute(node,
                                                            STYLESHEET_PREFIX)),
                    getPrefixNamespace(node,
                                       getRequiredAttribute(node,
                                                            RESULT_PREFIX)));
        }
    }
    
    ////////////////////////////////////////////////////////////

    //
    // interface for parsing template actions
    //
    private interface ActionParser
    {
        Action parse(Node node) throws XSLException;
    }

    // parses "xsl:apply-templates"    
    private class ApplyTemplatesParser implements ActionParser
    {
        public Action parse(Node node) throws XSLException
        {
            //            System.out.println("ApplyTemplatesParser:parse1(): {" + node.getName().getNamespace() + "}" +  node.getName().getLocalPart());

            NodeSetExpr expr = getSortNodeSetExpr(node, getNodeSetExpr(node));
            String modeString = node.getAttributeValue(MODE);
            Name modeName = null;
            if (modeString != null)
                modeName = expandSourceElementTypeName(modeString, node);
            if (_debugger != null) {
                return addParams(new ProcessActionDebug(_debugger, node, 
                                                        "", expr, modeName), 
                                 node);
            } else {
                return addParams(new ProcessAction(expr, modeName), node);
            }
        }
        
        //
        // return the parsed XPath expression from the "select" attribute
        //
        NodeSetExpr getNodeSetExpr(Node node) throws XSLException
        {
            String select = node.getAttributeValue(SELECT);
            if (select == null) {
                return childrenExpr;
            }
            return ExprParser.parseNodeSetExpr(node, select,
                                               currentLocalVariables);
        }
        
    }
    
    // parses "xsl:for-each" template action element
    private class ForEachParser implements ActionParser
    {
        public Action parse(Node node) throws XSLException
        {
            if (_debugger != null) {
                return new ForEachActionDebug(_debugger, node, "",
                                              getSortNodeSetExpr(node, ExprParser
                                                                 .parseNodeSetExpr(node, 
                                                                                   getRequiredAttribute(node, SELECT), 
                                                                                   currentLocalVariables)),
                                              parseActions(node, emptyAction));
            } else {
                return new ForEachAction(getSortNodeSetExpr(node, ExprParser
                                                            .parseNodeSetExpr(node, 
                                                                              getRequiredAttribute(node, SELECT), 
                                                                              currentLocalVariables)),
                                         parseActions(node, emptyAction));
            }
        }
    }

    // parses "xsl:if" template action element
    //
    //  <!-- Category: instruction -->
    //     <xsl:if 
    //       test = boolean-expression>
    //       <!-- Content: template -->
    //     </xsl:if> 
    //
    private class IfParser implements ActionParser
    {
        // construct an IfAction
        public Action parse(Node node) throws XSLException
        {
            if (_debugger != null) {
                return new IfActionDebug(_debugger, node, "",
                                         makeCondition(node),
                                         parseActions(node, emptyAction),
                                         emptyAction);
            } else {
                return new IfAction(makeCondition(node),
                                    parseActions(node, emptyAction),
                                    emptyAction);
            }
        }
        
        // parse the XPath expression in the "test" attribute
        BooleanExpr makeCondition(Node node) throws XSLException
        {
            return ExprParser.parseBooleanExpr(node,
                                               getRequiredAttribute(node, TEST),
                                               currentLocalVariables);
        }
    }
  
    // <!-- Category: instruction -->
    //     <xsl:copy 
    //       use-attribute-sets = qnames>
    //       <!-- Content: template -->
    //     </xsl:copy> 
    //
    private class CopyParser implements ActionParser
    {
        public Action parse(Node node) throws XSLException
        {
            if (_debugger != null) {
                return new CopyActionDebug(_debugger, node, "",
                                           parseUseAttributeSetsAndActions(node));
            } else {
                return new CopyAction(parseUseAttributeSetsAndActions(node));
            }
        }
    }
    
    //  <!-- Category: instruction -->
    //     <xsl:copy-of 
    //        select = expression /> 
    //
    private class CopyOfParser implements ActionParser
    {
        public Action parse(Node node) throws XSLException
        {
            if (_debugger != null) {

                return new CopyOfActionDebug(_debugger, node, "",
                                             ExprParser.
                                             parseVariantExpr(node,
                                                              getRequiredAttribute(node,
                                                                                   SELECT),
                                                              currentLocalVariables));
                
            } else {
                return new CopyOfAction(ExprParser.
                                        parseVariantExpr(node,
                                                         getRequiredAttribute(node,
                                                                              SELECT),
                                                         currentLocalVariables));
            }
        }
    }

    // <!-- Category: instruction -->
    //    <xsl:comment>
    //       <!-- Content: template -->
    //    </xsl:comment> 
    //
    private class CommentParser implements ActionParser
    {
        public Action parse(Node node) throws XSLException
        {
            if (_debugger != null) {
                return new CommentActionDebug(_debugger, node, "",
                                              parseActions(node, emptyAction));
            } else {
                return new CommentAction(parseActions(node, emptyAction));
            }
        }
    }
    
    // <!-- Category: instruction -->
    //     <xsl:processing-instruction 
    //       name = { ncname }>
    //       <!-- Content: template -->
    //     </xsl:processing-instruction> 
    //
    private class ProcessingInstructionParser implements ActionParser
    {
        public Action parse(Node node) throws XSLException
        {
            if (_debugger != null) {
                return new
                    ProcessingInstructionActionDebug(_debugger, node, "",
                                                     ExprParser.
                                                     parseValueExpr(node,
                                                                    getRequiredAttribute(node, NAME),
                                                                    currentLocalVariables),
                                                     parseActions(node, emptyAction));
                
            } else {
                return new
                    ProcessingInstructionAction(ExprParser.
                                                parseValueExpr(node,
                                                               getRequiredAttribute(node, NAME),
                                                               currentLocalVariables),
                                                parseActions(node, emptyAction));
            }
        }
    }
    
    // <!-- Category: instruction -->
    //     <xsl:element 
    //       name = { qname }
    //       namespace = { uri-reference }
    //       use-attribute-sets = qnames>
    //       <!-- Content: template -->
    //     </xsl:element> 
    //
    private class ElementParser implements ActionParser
    {
        /**
         *
         */
        public Action parse(Node node) throws XSLException
        {
            if (_debugger != null) {
                return new ElementActionDebug(_debugger, node, "",
                                              ExprParser.
                                              parseValueExpr(node,
                                                             getRequiredAttribute(node, 
                                                                                  NAME),
                                                             currentLocalVariables),
                                              getNamespaceExpr(node),
                                              node.getNamespacePrefixMap(),
                                              parseUseAttributeSetsAndActions(node));
            } else {
                return new ElementAction(ExprParser.
                                         parseValueExpr(node,
                                                        getRequiredAttribute(node, 
                                                                             NAME),
                                                        currentLocalVariables),
                                         getNamespaceExpr(node),
                                         node.getNamespacePrefixMap(),
                                         parseUseAttributeSetsAndActions(node));
            }
        }
           
        /**
         *
         */ 
        StringExpr getNamespaceExpr(Node node) throws XSLException
        {
            String namespace = node.getAttributeValue(NAMESPACE);
            if (namespace == null) {
                return null;
            }
            return ExprParser.parseValueExpr(node, 
                                             namespace, 
                                             currentLocalVariables);
        }
    }

    // <!-- Category: instruction -->
    //     <xsl:attribute 
    //       name = { qname }
    //       namespace = { uri-reference }>
    //       <!-- Content: template -->
    //     </xsl:attribute> 
    //
    private class AttributeParser extends ElementParser
    {
        /**
         *
         */
        public Action parse(Node node) throws XSLException
        {
            if (_debugger != null) {
                return new AttributeActionDebug(_debugger, node, "",
                                                ExprParser.
                                                parseValueExpr(node,
                                                               getRequiredAttribute(node,
                                                                                    NAME),
                                                               currentLocalVariables),
                                                getNamespaceExpr(node),
                                                node.getNamespacePrefixMap(),
                                                parseActions(node, emptyAction));
            } else {
                return new AttributeAction(ExprParser.
                                           parseValueExpr(node,
                                                          getRequiredAttribute(node,
                                                                               NAME),
                                                          currentLocalVariables),
                                           getNamespaceExpr(node),
                                           node.getNamespacePrefixMap(),
                                           parseActions(node, emptyAction));
            }
        }
    }

    // xt:document  extension element 
    private class DocumentParser implements ActionParser
    {
        /**
         *
         */
        public Action parse(Node node) throws XSLException
        {
            return new DocumentAction(ExprParser.
                                      parseValueExpr(node,
                                                     getRequiredAttribute(node,
                                                                          HREF),
                                                     currentLocalVariables),
                                      _outputMethod.mergeCopy(node),
                                      parseActions(node, emptyAction));
        }
    }


    // xt xrap extension element
    // <xfy:xrap>
    //  <!-- Content -->
    // </xfy:xrap />    
    private class XRAPParser implements ActionParser
    {
        /**
         *
         */
        public Action parse(Node node) throws XSLException
        {
            if (_debugger != null) {
                return new XRAPActionDebug(_debugger, node, "",
                                           node.getNamespacePrefixMap(),
                                           parseActions(node, emptyAction));
            } else {
                return new XRAPAction( node.getNamespacePrefixMap(),
                                       parseActions(node, emptyAction));
            }
        }
    }
    
    //
    //  <!-- Category: instruction -->
    //     <xsl:call-template 
    //       name = qname>
    //       <!-- Content: xsl:with-param* -->
    //     </xsl:call-template> 
    //
    private class CallTemplateParser implements ActionParser
    {
        public Action parse(Node node) throws XSLException
        {

            InvokeAction callAction;
            if (_debugger != null) {
                callAction = 
                    new InvokeActionDebug(_debugger, node, "",
                                          expandSourceElementTypeName(getRequiredAttribute(node, NAME), 
                                                                      node),
                                          namedTemplateTable);
                
            } else {
                callAction = 
                    new InvokeAction(expandSourceElementTypeName(getRequiredAttribute(node, NAME), 
                                                                 node),
                                     namedTemplateTable);
            }
            
//             return addParams(new InvokeAction(expandSourceElementTypeName(getRequiredAttribute(node, NAME), node),
//                                               namedTemplateTable),
//                              node);
            
            return addParams(callAction,
                             node);
            
            
        }
    }

    
    // <!-- Category: instruction -->
    //     <xsl:choose>
    //       <!-- Content: (xsl:when+, xsl:otherwise?) -->
    //     </xsl:choose> 
    //
    private class ChooseParser extends IfParser
    {
        public Action parse(Node node) 
            throws XSLException
        {
            return parseChoices(node.getChildren());
        }

        public Action parseChoices(NodeIterator iter) 
            throws XSLException
        {
            Node node = iter.next();
            if (node == null) {
                return emptyAction;
            }
            Name name = node.getName();
            if (XSL_WHEN.equals(name)) {
                if (_debugger != null) {
                    return new IfActionDebug(_debugger, node, "",
                                             makeCondition(node),
                                             parseActions(node, emptyAction),
                                             parseChoices(iter));
                    
                } else {
                    
                    return new IfAction(makeCondition(node),
                                        parseActions(node, emptyAction),
                                        parseChoices(iter));
                }
            }
            if (XSL_OTHERWISE.equals(name)) {
                Node next = iter.next();
                if (next != null)
                    throw new XSLException("unexpected element after otherwise",
                                           next);
                return parseActions(node, emptyAction);
            }
            throw new XSLException("expected when or otherwise", node);
        }
    }
    
    // <!-- Category: instruction -->
    //     <xsl:text 
    //       disable-output-escaping = "yes" | "no">
    //       <!-- Content: #PCDATA -->
    //     </xsl:text> 
    //
    private class TextParser implements ActionParser
    {
        public Action parse(Node node) throws XSLException
        {
            Node child = node.getChildren().next();
            if (child != null) {
                String data = child.getData();
                if (data == null || 
                    child.getFollowingSiblings().next() != null)
                    throw new XSLException("xsl:text must not contain elements",
                                           node);
                if ("yes".equals(node.getAttributeValue(DISABLE_OUTPUT_ESCAPING))) {
                    if (_debugger != null) {
                        return new RawCharsActionDebug(_debugger, node, "",
                                                       data);
                    } else {
                        return new RawCharsAction(data);

                    }
                }
                if (_debugger != null) {
                    return new CharsActionDebug(_debugger, node, "", 
                                                data);
                } else {
                    return new CharsAction(data);
                }
            } else {
                return emptyAction;
            }
        }
    }
    
    // <!-- Category: instruction -->
    //     <xsl:value-of 
    //       select = string-expression 
    //       disable-output-escaping = "yes" | "no" /> 
    //
    private class ValueOfParser implements ActionParser
    {
        public Action parse(Node node) throws XSLException
        {
            StringExpr expr = 
                ExprParser.parseStringExpr(node,
                                           getRequiredAttribute(node, SELECT),
                                           currentLocalVariables);
            if ("yes".equals(node.getAttributeValue(DISABLE_OUTPUT_ESCAPING))){
                if (_debugger != null) {
                    return new RawValueOfActionDebug(_debugger, node, "",
                                                expr);
                } else {
                    return new RawValueOfAction(expr);
                }
            }
            if (_debugger != null) {
                return new ValueOfActionDebug(_debugger, node, "",
                                              expr);
            } else {
                return new ValueOfAction(expr);
            }
        }
    }
    
    // <!-- Category: instruction -->
    //     <xsl:number 
    //       level = "single" | "multiple" | "any"
    //       count = pattern 
    //       from = pattern 
    //       value = number-expression 
    //       format = { string }
    //       lang = { nmtoken }
    //       letter-value = { "alphabetic" | "traditional" }
    //       grouping-separator = { char }
    //       grouping-size = { number } /> 
    //
    private class NumberParser implements ActionParser
    {
        public Action parse(Node node) throws XSLException
        {
            NumberListFormatTemplate format =
                getNumberListFormatTemplate(node);
            String value = node.getAttributeValue(VALUE);
            if (value != null)
                return new 
                    ExprNumberAction(ExprParser.
                                     parseNumberExpr(node, value, 
                                                     currentLocalVariables), 
                                     format);
            String level = getOptionalAttribute(node, LEVEL, "single");
            String countString = node.getAttributeValue(COUNT);
            Pattern count;
            if (countString == null) {
                count = null;
            } else {
                count = ExprParser.parsePattern(node, countString, 
                                                currentLocalVariables);
	    }
            String fromString = node.getAttributeValue(FROM);
            Pattern from;
            if (fromString == null) {
                from = null;
            } else {
                from = ExprParser.parsePattern(node, fromString, currentLocalVariables);
            }
            if (level.equals("any")) {
                if (_debugger != null) {
                    return new AnyLevelNumberActionDebug(_debugger, node, "",
                                                         count, from, format);
                } else {
                    return new AnyLevelNumberAction(count, from, format);
                }
            }
            if (level.equals("multiple")) {
                if (_debugger != null) {
                    return new MultiLevelNumberActionDebug(_debugger, node, "",
                                                           count, from, format);
                } else {
                    return new MultiLevelNumberAction(count, from, format);
                }
            }
            if (level.equals("single")) {
                if (_debugger != null) {
                    return new SingleLevelNumberActionDebug(_debugger, node, "",
                                                            count, from, format);
                } else {
                    return new SingleLevelNumberAction(count, from, format);
                }
            }
            throw new XSLException("bad level", node);
        }
    }
    
    // parses EXSLT Functions: result action
    // <func:result select='...' />
    private class EXSLFunctionResultParser implements ActionParser
    {

        public Action parse(Node node) throws XSLException
        {
            VariantExpr expr = getVariantExpr(node);
            return null;

//             if (_debugger != null) {
//                 return new ValueOfActionDebug(_debugger, node, "",
//                                               expr);
//             } else {
//                 return new ValueOfAction(expr);
//             }
        }
        
    }


    //
    //     <!-- Category: instruction -->
    //     <xsl:apply-imports /> 
    //
    private class ApplyImportsParser implements ActionParser
    {
        public Action parse(Node node) throws XSLException
        {
            if (_debugger != null) {
                return new ApplyImportsActionDebug(_debugger, node, "");
            } else {
                return new ApplyImportsAction();
            }
        }
    }
    
    //
    // parses non-top level xsl:variable or xsl:param
    // <!-- Category: instruction -->
    //     <xsl:variable 
    //       name = qname 
    //       select = expression>
    //       <!-- Content: template -->
    //     </xsl:variable> 
    //
    private class VariableActionParser implements ActionParser
    {
        public Action parse(Node node) throws XSLException
        {
            Name name = 
                expandSourceElementTypeName(getRequiredAttribute(node, NAME), 
                                            node);
            VariantExpr expr = getVariantExpr(node);

            // Must create the VariantExpr before adding to local variables.
            currentLocalVariables = new AddVariableSet(name, 
                                                       currentLocalVariables);
            nCurrentLocalVariables++;
            return makeAction(node, name, expr);
        }
        
        Action makeAction(Node node, Name name, VariantExpr expr)
        {
            if (_debugger != null) {
                return new BindLocalVariableActionDebug(_debugger, node, "",
                                                        name, expr);
            } else {
                return new BindLocalVariableAction(name, expr);
            }
        }
    }
    
    //
    //<!-- Category: top-level-element -->
    //     <xsl:param 
    //       name = qname 
    //       select = expression>
    //       <!-- Content: template -->
    //     </xsl:param> 
    //
    private class ParamActionParser extends VariableActionParser
    {
        Action makeAction(Node node, Name name, VariantExpr expr)
        {
            if (_debugger != null) {
                return new BindLocalParamActionDebug(_debugger, node, "",
                                                     name, expr);
            } else {
                return new BindLocalParamAction(name, expr);
            }
        }
    }
    
    //
    // <!-- Category: instruction -->
    //     <xsl:message 
    //       terminate = "yes" | "no">
    //       <!-- Content: template -->
    //     </xsl:message> 
    //
    private class MessageParser implements ActionParser
    {
        public Action parse(Node node) throws XSLException
        {
            boolean terminate = "yes".equals(node.getAttributeValue(TERMINATE));
            Action content = parseActions(node, emptyAction);
            if (terminate) {
                if (_debugger != null) {
                    return new TerminateMessageActionDebug(_debugger, node, "",
                                                           content);
                } else {
                    return new TerminateMessageAction(content);
                }
            } else {
                if (_debugger != null) {
                    return new MessageActionDebug(_debugger, node, "",
                                                  content);
                } else {
                    return new MessageAction(content);
                }
            }
        }
    }
    
    // Use this with Result. Tracks the output method options
    // specified in the xsl:stylesheet element
    private class OutputMethodImpl implements OutputMethod, Cloneable
    {
        // "text", "html", "xml" or colonized qName
        private Name _outputMethodName = null;

        // don't include "method" and "cdata-section-elements"
        private Name[] _outputMethodAttNames = new Name[] {
            DOCTYPE_PUBLIC,
            DOCTYPE_SYSTEM,
            ENCODING,
            INDENT,
            MEDIA_TYPE,
            OMIT_XML_DECLARATION,
            STANDALONE,
            VERSION
        };

        private String[] _outputMethodAttValues = new String[] {
            null, null, null, null, null, null, null, null};

        private Vector _outputCdataSectionElements = new Vector();

        //        private Vector outputMethodAttributes = new Vector();

        
        /**
         * create a new OutputMethod for an xt:document extension element
         */
        OutputMethod mergeCopy(Node node) throws XSLException
        {
            SafeNodeIterator iter = node.getAttributes();
            if (iter.next() != null && iter.next() == null) {
                // we don't need to bother if they're no new attributes
                return this;
            }
            OutputMethodImpl om = (OutputMethodImpl)clone();
            om.merge(node);
            return om;
        }
        
        public Object clone()
        {
            try {
                OutputMethodImpl om = (OutputMethodImpl)super.clone();
                om._outputCdataSectionElements = (Vector)_outputCdataSectionElements.clone();
                om._outputMethodAttValues = (String[]) _outputMethodAttValues.clone();

                return om;
            }
            catch (CloneNotSupportedException e) {
                throw new Error("unexpected CloneNotSupportedException");
            }
        }
        
        /**
         * merge in the attributes found in an "xsl:output" top level element
         */
        void merge(Node node) throws XSLException
        {
            // FIXME error checking

            for (SafeNodeIterator iter = node.getAttributes() ; ; ) {
                Node att = iter.next();
                if (att == null) {
                    break;
                }

                Name name = att.getName();
                String value = att.getData();
                setSpecifiedValue(name, value, node);
            }
        }
        
        public Name getName()
        {
            return _outputMethodName;
        }
        
        /**
         * return the value for the named output method attribute,
         * if that attribute was specified in the stylesheet
         *
         * @return null if the value was not declared in the stylesheet
         */
        public String getSpecifiedValue(Name name)
        {
            for (int i = 0; i < _outputMethodAttNames.length; ++i) {
                if (_outputMethodAttNames[i].equals(name)) {
                    return _outputMethodAttValues[i];
                }
            }
            return null;

        }


        public void setSpecifiedValue(Name name, String value, Node node)
            throws XSLException
        {
            if (HREF.equals(name)) {
                // ignore
            } else if (METHOD.equals(name)) {
                if (node != null ) {
                    _outputMethodName = 
                        node.getNamespacePrefixMap().expandAttributeName(value, node);
                } else {
                    // FIXME: value may have another mechanism for namespace ref
                }
            } else if (CDATA_SECTION_ELEMENTS.equals(name)) {
                StringTokenizer t = new StringTokenizer(value);
                if (node != null) {
                    NamespacePrefixMap nsm = node.getNamespacePrefixMap();
                    
                    while (t.hasMoreElements()) {
                        String elQnm = (String)t.nextElement();
                        Name cdataElNm = nsm.expandAttributeName(elQnm, node);
                        _outputCdataSectionElements.addElement(cdataElNm);
                    }
                } else {
                    // FIXME: value may have another mechanism for namespace ref
                }
            } else {
                for (int i = 0; i < _outputMethodAttNames.length; ++i) {
                    if (_outputMethodAttNames[i].equals(name)) {
                        _outputMethodAttValues[i] = value;
                        return;
                    }
                }
                // we grow arrays for non-standard att names (is this prudent?)
                int len = _outputMethodAttNames.length;
                Name[] newNames = new Name[len + 1];
                String[] newVals = new String[len + 1];
                System.arraycopy(_outputMethodAttNames, 0, newNames, 0, len);
                System.arraycopy(_outputMethodAttValues, 0, newVals, 0, len);
                newNames[len] = name;
                newVals[len] = value;
                _outputMethodAttNames = newNames;
                _outputMethodAttValues = newVals;
            }
        }       

 
        /**
         * all the known output method attribute names, excluding
         * "method" and "cdata-section-elements"
         */
        public Name[] getAttributeNames()
        {
            return _outputMethodAttNames;
        }

        /**
         * returns the defaulted value for an output method property
         * which hasn't been set by an xsl:output stylesheet element
         */
        private String getDefaultValue(Name name)
        {
            if (METHOD.equals(name)) {
                return "xml";
            } else {
                if ("xml".equals(getPropertyValue(METHOD))) {
                    if (DOCTYPE_PUBLIC.equals(name)) {
                    } else if (DOCTYPE_SYSTEM.equals(name)) {
                    } else if (ENCODING.equals(name)) {
                    } else if (INDENT.equals(name)) {
                    } else if (MEDIA_TYPE.equals(name)) {
                    } else if (OMIT_XML_DECLARATION.equals(name)) {
                        return "no";
                    } else if (STANDALONE.equals(name)) {
                        return "no";
                    } else if (VERSION.equals(name)) {
                        return "1.0";
                    } else {
                        return null;
                    }
                } else if ("html".equals(getPropertyValue(METHOD))) {
                    if (DOCTYPE_PUBLIC.equals(name)) {
                    } else if (DOCTYPE_SYSTEM.equals(name)) {
                    } else if (ENCODING.equals(name)) {
                    } else if (INDENT.equals(name)) {
                    } else if (MEDIA_TYPE.equals(name)) {
                    } else if (OMIT_XML_DECLARATION.equals(name)) {
                        return "no";
                    } else if (STANDALONE.equals(name)) {
                        return "no";
                    } else if (VERSION.equals(name)) {
                        return "1.0";
                    } else {
                        return null;
                    }
                } else if ("text".equals(getPropertyValue(METHOD))) {
                    if (DOCTYPE_PUBLIC.equals(name)) {
                    } else if (DOCTYPE_SYSTEM.equals(name)) {
                    } else if (ENCODING.equals(name)) {
                    } else if (INDENT.equals(name)) {
                    } else if (MEDIA_TYPE.equals(name)) {
                    } else if (OMIT_XML_DECLARATION.equals(name)) {
                        return "no";
                    } else if (STANDALONE.equals(name)) {
                        return "no";
                    } else if (VERSION.equals(name)) {
                        return "1.0";
                    } else {
                        return null;
                    }
                } else if ("java".equals(getPropertyValue(METHOD))) {
                    if (DOCTYPE_PUBLIC.equals(name)) {
                    } else if (DOCTYPE_SYSTEM.equals(name)) {
                    } else if (ENCODING.equals(name)) {
                    } else if (INDENT.equals(name)) {
                    } else if (MEDIA_TYPE.equals(name)) {
                    } else if (OMIT_XML_DECLARATION.equals(name)) {
                        return "no";
                    } else if (STANDALONE.equals(name)) {
                        return "no";
                    } else if (VERSION.equals(name)) {
                        return "1.0";
                    } else {
                        return null;
                    }
                } else {
                    if (DOCTYPE_PUBLIC.equals(name)) {
                    } else if (DOCTYPE_SYSTEM.equals(name)) {
                    } else if (ENCODING.equals(name)) {
                    } else if (INDENT.equals(name)) {
                    } else if (MEDIA_TYPE.equals(name)) {
                    } else if (OMIT_XML_DECLARATION.equals(name)) {
                        return "no";
                    } else if (STANDALONE.equals(name)) {
                        return "no";
                    } else if (VERSION.equals(name)) {
                        return "1.0";
                    } else {
                        return null;
                    }
                }

            }
            return null;
        }

        /**
         * gets the value specified in the stylesheet, if available,
         * else gets the defaulted value
         */
        public String getPropertyValue(Name name)
        {
            String val = getSpecifiedValue(name);
            return (val == null) ? getDefaultValue(name) : val;
        }

        /**
         * return a list of all element names which should
         * have their content escaped in cdata sections
         */
        public Name[] getCdataSectionElements()
        {
            Name names[] = new Name[_outputCdataSectionElements.size()];
            for (int i = 0; i < names.length; i++) {
                names[i] = (Name)_outputCdataSectionElements.elementAt(i);
            }
            return names;
        }
        
        public NameTable getNameTable()
        {
            return nameTable;
        }
        
    }

}
