// $Id$

package com.jclark.xsl.expr;

import com.jclark.xsl.om.*;
import java.util.Hashtable;

/**
 * XPAth expression parser / compiler
 * extends the lexer ExprTokenizer
 */
public class ExprParser extends ExprTokenizer 
    implements NamespaceConstants 
{
    // instance variables
    private NamespacePrefixMap prefixMap;

    private Node node;   // the node containing the XPath expression
                         // being parsed

    private VariableSet locals;

    private boolean usesCurrentFunction = false;
    

    // all the rest are static class variables
    static final private Hashtable axisTable = new Hashtable();

    static final private AxisExpr childAxis = new ChildAxisExpr();
    static final private AxisExpr parentAxis = new ParentAxisExpr();
    static final private AxisExpr selfAxis = new SelfAxisExpr();
    static final private AxisExpr attributeAxis = new AttributeAxisExpr();

    static final private AxisExpr descendantOrSelfAxis = 
        new DescendantOrSelfAxisExpr();
  
    // a table of the XPath and XSLT builtin functions, as 
    // implemented by objects implementing the Function interface
    static final private Hashtable functionTable = new Hashtable();
    static final private Hashtable extensionFunctionTable = new Hashtable();

    static final private Hashtable exsltCommonFunctionTable = new Hashtable();
    static final private Hashtable exsltMathFunctionTable = new Hashtable();
    static final private Hashtable exsltDateTimeFunctionTable = new Hashtable();
    static final private Hashtable exsltDynamicFunctionTable = new Hashtable();
    static final private Hashtable exsltFunctionsFunctionTable = new Hashtable();
    static final private Hashtable exsltSetsFunctionTable = new Hashtable();
    static final private Hashtable exsltStringsFunctionTable = new Hashtable();
    static final private Hashtable exsltRegexFunctionTable = new Hashtable();


    static final private Relation equalsRelation = new EqualsRelation();
    static final private Relation notEqualsRelation = new NotEqualsRelation();
    static final private Relation greaterThanEqualsRelation = 
        new GreaterThanEqualsRelation();
    static final private Relation greaterThanRelation = new GreaterThanRelation();
    static final private Function currentFunction = new CurrentFunction();

    // static initialization of the axis table and function table
    static {
        axisTable.put("child", childAxis);
        axisTable.put("parent", parentAxis);
        axisTable.put("self", selfAxis);
        axisTable.put("attribute", attributeAxis);
        axisTable.put("descendant-or-self", descendantOrSelfAxis);

	// hmmm .. why did we switch from the pre constructed ones?
        axisTable.put("descendant", new DescendantAxisExpr());

        axisTable.put("ancestor-or-self", new AncestorOrSelfAxisExpr());
        axisTable.put("ancestor", new AncestorAxisExpr());
        axisTable.put("following-sibling", new FollowingSiblingAxisExpr());
        axisTable.put("preceding-sibling", new PrecedingSiblingAxisExpr());
        axisTable.put("following", new FollowingAxisExpr());
        axisTable.put("preceding", new PrecedingAxisExpr());
        axisTable.put("namespace", new NamespaceAxisExpr());

        // namespace axis?

        functionTable.put("boolean", new BooleanFunction());
        functionTable.put("ceiling", new CeilingFunction());
        functionTable.put("concat", new ConcatFunction());
        functionTable.put("contains", new ContainsFunction());
        functionTable.put("count", new CountFunction());
        functionTable.put("document", new DocumentFunction());
        functionTable.put("false", new FalseFunction());
        functionTable.put("floor", new FloorFunction());
        functionTable.put("format-number", new FormatNumberFunction());
        functionTable.put("function-available",
                          new FunctionAvailableFunction());

        functionTable.put("element-available",
			  new ElementAvailableFunction());

        functionTable.put("generate-id", new GenerateIdFunction());
        functionTable.put("id", new IdFunction());

        functionTable.put("key", new KeyFunction());

        functionTable.put("lang", new LangFunction());
        functionTable.put("last", new LastFunction());
        functionTable.put("local-name", new LocalNameFunction());
        functionTable.put("namespace-uri", new NamespaceUriFunction());
        functionTable.put("normalize-space", new NormalizeSpaceFunction());
        functionTable.put("not", new NotFunction());
        functionTable.put("number", new NumberFunction());
        functionTable.put("position", new PositionFunction());
        functionTable.put("name", new NameFunction());
        functionTable.put("round", new RoundFunction());
        functionTable.put("starts-with", new StartsWithFunction());
        functionTable.put("string", new StringFunction());
        functionTable.put("string-length", new StringLengthFunction());
        functionTable.put("substring", new SubstringFunction());
        functionTable.put("substring-after", new SubstringAfterFunction());
        functionTable.put("substring-before", new SubstringBeforeFunction());
        functionTable.put("sum", new SumFunction());
        functionTable.put("system-property", new SystemPropertyFunction());
        functionTable.put("translate", new TranslateFunction());
        functionTable.put("true", new TrueFunction());
        functionTable.put("unparsed-entity-uri", new UnparsedEntityURIFunction());

        // James Clark's xt extension functions
        extensionFunctionTable.put("node-set", new NodeSetFunction());
        extensionFunctionTable.put("intersection", new IntersectionFunction());
        extensionFunctionTable.put("difference", new DifferenceFunction());

        // exslt common extension functions
        exsltCommonFunctionTable.put("node-set", new NodeSetFunction());
        exsltCommonFunctionTable.put("object-type", new ObjectTypeFunction());

        // exslt regular expressions
        exsltRegexFunctionTable.put("test", new RegexpTestFunction());
	exsltRegexFunctionTable.put("replace", new RegexpReplaceFunction());
	exsltRegexFunctionTable.put("match", new RegexpMatchFunction());
	
	//exslt string
	exsltStringsFunctionTable.put("encode-uri", new EncodeURIFunction());
	exsltStringsFunctionTable.put("decode-uri", new DecodeURIFunction());
	exsltStringsFunctionTable.put("split", new SplitFunction());
    }


    /**
     * parse an XSLT attribute value template (which may include
     *  XPath expression(s) enclosed in curlybraces "{" "}")
     */
    public static StringExpr parseValueExpr(Node node, 
                                            String value, 
                                            VariableSet locals)
        throws XSLException 
    {
        try {

            ConvertibleStringExpr prev = null;

            StringBuffer buf = new StringBuffer();
            int valueLen = value.length();
            for (int i = 0; i < valueLen; i++) {
                char c = value.charAt(i);
                switch (c) {
                case '{':
                    if (i + 1 < valueLen && value.charAt(i + 1) == '{') {
			// curly brace was escaped, put it in buf, and continue
                        i++;
                        buf.append('{');
                    } else {
			// okay, we have us an expression, here
                        int n = findExprEnd(value, i + 1);
                        if (n < 0) {
                            throw new XSLException("missing }", node);
                        }
                        ConvertibleStringExpr expr
                            = parseConvertibleExpr(node,
                                                   value.substring(i + 1, n),
                                                   locals).makeStringExpr();
                        if (buf.length() > 0) {
			    // we've collected some literal characters we need to take care of
			    
                            if (prev == null) {
                                prev = new LiteralExpr(buf.toString());
                            } else {
                                prev = 
                                    new AppendExpr(prev,
                                                   new LiteralExpr(buf.toString()));
                            }
                            buf.setLength(0);
                        }
                        if (prev == null) {
			    // this is the very first thing we've seen in this string
                            prev = expr;
                        } else {
                            prev = new AppendExpr(prev, expr); 
			}
                        i = n;
                    }
                    break;
                case '}':
                    buf.append('}');
                    if (i + 1 < valueLen && value.charAt(i + 1) == '}') {
                        i++;
		    }
                    break;
                default:
                    buf.append(c);
                    break;
                }
            }
            if (buf.length() > 0) {
                if (prev == null) {
                    return new LiteralExpr(buf.toString());
                } else {
                    return new AppendExpr(prev, new LiteralExpr(buf.toString()));
                }
            }
            if (prev != null) {
                return prev;
            }
            return new LiteralExpr("");
        }
        catch (ParseException e) {
            throw makeXSLException(e, node);
        }
    }

    /**
     *
     */
    public static NodeSetExpr getChildrenExpr() 
    {
        return new ChildAxisExpr();
    }

    /**
     * parse an XPath match Pattern
     */
    public static TopLevelPattern parsePattern(Node node, 
                                               String pattern) 
        throws XSLException 
    {
        return new ExprParser(pattern, node, new EmptyVariableSet())
            .parseTopLevelPattern(node);
    }

    /**
     * parse an XPath match pattern (with some variables!?!?)
     * @param node represents the node in the stylesheet
     *  with the expression we're parsing.  Used for 
     *  plugging the location in any Exception we might throw
     *
     * xsl:number allows variables in the "count" and "from" attributes
     */
    public static TopLevelPattern parsePattern(Node node, 
                                               String pattern, 
                                               VariableSet locals) 
        throws XSLException 
    {
        return new ExprParser(pattern, node, locals)
            .parseTopLevelPattern(node);
    }


    /**
     *  parse an XPath expression which is to be
     *  used in a context in which a NodeSet is expected
     * i.e. for the "select" attribute of an "xsl:apply-templates"
     */
    public static NodeSetExpr parseNodeSetExpr(Node node, 
                                               String expr, 
                                               VariableSet locals) 
        throws XSLException 
    {
        try {
            return parseConvertibleExpr(node, expr, locals).makeNodeSetExpr();
        }
        catch (ParseException e) {
            throw makeXSLException(e, node);
        }
    }

    /**
     *  parse an XPath expression which is to be
     *  used in a context in which a String is expected
     */
    public static StringExpr parseStringExpr(Node node, String expr,
                                             VariableSet locals) 
        throws XSLException 
    {
        try {
            return parseConvertibleExpr(node, expr, locals).makeStringExpr();
        }
        catch (ParseException e) {
            throw makeXSLException(e, node);
        }
    }

    /**
     *  parse an XPath expression which is to be
     *  used in a context in which a Number is expected
     */
    public static NumberExpr parseNumberExpr(Node node, 
                                             String expr, 
                                             VariableSet locals) 
        throws XSLException 
    {
        try {
            return parseConvertibleExpr(node, expr, locals).makeNumberExpr();
        }
        catch (ParseException e) {
            throw makeXSLException(e, node);
        }
    }

    /**
     *  parse an XPath expression which is to be
     *  used in a context in which a Boolean is expected
     */
    public static BooleanExpr parseBooleanExpr(Node node, String expr,
                                               VariableSet locals) 
        throws XSLException 
    {
        try {
            return parseConvertibleExpr(node, expr, locals).makeBooleanExpr();
        }
        catch (ParseException e) {
            throw makeXSLException(e, node);
        }
    }

    /**
     *  parse an XPath expression which is to be
     *  used in a context in which a Variant (xsl:variable, xsl:param)
     *  is expected
     */
    public static VariantExpr parseVariantExpr(Node node, String expr, 
                                               VariableSet locals)
        throws XSLException 
    {
        try {
            return parseConvertibleExpr(node, expr, locals).makeVariantExpr();
        }
        catch (ParseException e) {
            throw makeXSLException(e, node);
        }
    }

    //  construct an instance of ourself, and parse the string
    //
    private static ConvertibleExpr parseConvertibleExpr(Node node,
                                                        String expr, 
                                                        VariableSet locals)
        throws ParseException 
    {
        return new ExprParser(expr, node, locals).parseExpr();
    }

    ///////////////////////////////////////////
    //
    // The constructor ...
    //
    //
    private ExprParser(String expr, Node node, VariableSet locals) 
    {
        super(expr);
        this.node = node;
        if (node != null) {
            prefixMap = node.getNamespacePrefixMap();
        }
        this.locals = locals;
    }

    //
    //
    //
    private ConvertibleExpr parseExpr() throws ParseException 
    {
        next();
        ConvertibleExpr expr = parseOrExpr();
        if (currentToken != TOK_EOF) {
            throw new ParseException("unexpected token");
        }
	if (usesCurrentFunction) {
            return new WithCurrentExpr(expr);
	}
        return expr;
    }

    //
    //
    //
    private static XSLException makeXSLException(ParseException e, Node node)
    {
        return new XSLException(e.getMessage(), node);
    }

    //
    // XPath Production #21
    //
    private ConvertibleExpr parseOrExpr() throws ParseException 
    {
        ConvertibleExpr expr = parseAndExpr();
        while (currentToken == TOK_OR) {
            next();
            expr = new OrExpr(expr.makeBooleanExpr(),
                              parseAndExpr().makeBooleanExpr());
        }
        return expr;
    }

    //
    // XPath production #22
    //
    private ConvertibleExpr parseAndExpr() throws ParseException 
    {
        ConvertibleExpr expr = parseEqualityExpr();
        while (currentToken == TOK_AND) {
            next();
            expr = new AndExpr(expr.makeBooleanExpr(),
                               parseEqualityExpr().makeBooleanExpr());
        }
        return expr;
    }

    //
    // XPath Production #23
    //
    private ConvertibleExpr parseEqualityExpr() throws ParseException 
    {
        ConvertibleExpr expr = parseRelationalExpr();
        loop:
        for (;;) {
            switch (currentToken) {
            case TOK_EQUALS:
                next();
                expr = makeRelationalExpr(equalsRelation,
                                          expr,
                                          parseRelationalExpr());
                break;
            case TOK_NOT_EQUALS:
                next();
                expr = makeRelationalExpr(notEqualsRelation,
                                          expr,
                                          parseRelationalExpr());
                break;
            default:
                break loop;
            }
        }
        return expr;
    }

    //
    // XPath Production #24
    //
    // A RelationalExpr is an AdditiveExpr, possibly
    // followed by a comparison operator and another
    // RelationalExpr
    //
    private ConvertibleExpr parseRelationalExpr() throws ParseException 
    {
        ConvertibleExpr expr = parseAdditiveExpr();
        loop:
        for (;;) {
            switch (currentToken) {
            case TOK_GT:
                next();
                expr = makeRelationalExpr(greaterThanRelation,
                                          expr,
                                          parseAdditiveExpr());
                break;
                
            case TOK_GTE:
                next();
                expr = makeRelationalExpr(greaterThanEqualsRelation,
                                          expr,
                                          parseAdditiveExpr());
                break;
                
            case TOK_LT:
                next();
                expr = makeRelationalExpr(greaterThanRelation,
                                          parseAdditiveExpr(),
                                          expr);
                break;

            case TOK_LTE:
                next();
                expr = makeRelationalExpr(greaterThanEqualsRelation,
                                          parseAdditiveExpr(),
                                          expr);
                break;

            default:
                break loop;
            }
        }
        return expr;
    }
  
    //
    // XPath production #25  AdditiveExpr
    //
    private ConvertibleExpr parseAdditiveExpr() throws ParseException {
        ConvertibleExpr expr = parseMultiplicativeExpr();
        loop:
        for (;;) {
            switch (currentToken) {
            case TOK_PLUS:
                next();
                expr = new AddExpr(expr.makeNumberExpr(), 
                                   parseMultiplicativeExpr().makeNumberExpr());
                break;
            case TOK_MINUS:
                next();
                expr = new SubtractExpr(expr.makeNumberExpr(),
                                        parseMultiplicativeExpr().makeNumberExpr());
                break;
            default:
                break loop;
            }
        }
        return expr;
    }

    //
    // XPAth production #26
    //
    private ConvertibleExpr parseMultiplicativeExpr() 
        throws ParseException 
    {
        
        // get the first part
        ConvertibleExpr expr = parseUnaryExpr();
        loop:
        for (;;) {
            switch (currentToken) {
            case TOK_DIV:
                next();
                expr = new DivideExpr(expr.makeNumberExpr(),
                                      parseUnaryExpr().makeNumberExpr());
                break;
            case TOK_MOD:
                next();
                expr = new ModuloExpr(expr.makeNumberExpr(),
                                      parseUnaryExpr().makeNumberExpr());
                break;
            case TOK_MULTIPLY:
                next();
                expr = new MultiplyExpr(expr.makeNumberExpr(),
                                        parseUnaryExpr().makeNumberExpr());
                break;
            default:
                break loop;
            }
        }
        return expr;
    }

    //
    // XPath production #27
    //
    // we've recognized something which may be a 
    //    unary operator (-) followed by an expression
    // or a union expression (or group)
    // or a path expression
    //
    private ConvertibleExpr parseUnaryExpr() 
        throws ParseException 
    {
        if (currentToken == TOK_MINUS) {
            next();
            return new NegateExpr(parseUnaryExpr().makeNumberExpr());
        }
        return parseUnionExpr();
    }

    //
    // XPath production #18
    //
    // any expression which may contain alternative
    // path expressions (separated by the or operator "|")
    //
    private ConvertibleExpr parseUnionExpr() throws ParseException 
    {
        ConvertibleExpr expr = parsePathExpr();
        while (currentToken == TOK_VBAR) {
            next();
            expr = new UnionExpr(expr.makeNodeSetExpr(),
                                 parsePathExpr().makeNodeSetExpr());
        }
        return expr;
    }

    //
    // XPath production #19
    //
    private ConvertibleExpr parsePathExpr() throws ParseException 
    {
        if (tokenStartsStep()) {
            return parseRelativeLocationPath(); // XPath production #3
        }
        
        if (currentToken == TOK_SLASH) {
            next();
            if (tokenStartsStep()) {
                // XPath production #2
                return new RootExpr(parseRelativeLocationPath());
            }
            // the root, by itself
            return new RootExpr(selfAxis);
        }
        if (currentToken == TOK_SLASH_SLASH) {

            // abbreviated absolute location XPath production #10
            next();
            return new RootExpr(descendantOrSelfAxis.compose(parseRelativeLocationPath()));
        }

        //
        // if none of the above alternatives, we should be looking
        // at a FilterExpression (production #20) followed by
        // either a "/" or "//", and then a RelativeLocationPath
        // (production #3)
        //

        ConvertibleExpr expr = parsePrimaryExpr();

        // Production 20 requires at least one primary expression
        // and any number of predicates
        // the TOK_LSQB ("[") starts a predicate

        while (currentToken == TOK_LSQB) {
            next();
            expr = new FilterExpr(expr.makeNodeSetExpr(),
                                  parseOrExpr().makePredicateExpr());
            expectRsqb();
        }

        if (currentToken == TOK_SLASH) {
            next();
            return expr.makeNodeSetExpr().compose(parseRelativeLocationPath());
        } else if (currentToken == TOK_SLASH_SLASH) {
            next();
            return expr.makeNodeSetExpr().compose(descendantOrSelfAxis.compose(parseRelativeLocationPath()));
        }
        else
            return expr;
    }

    //
    // RelativeLocationPath -- XPath production #3
    //
    private ConvertibleNodeSetExpr parseRelativeLocationPath()
        throws ParseException 
    {
        ConvertibleNodeSetExpr step = parseStep();
        if (currentToken == TOK_SLASH) {
            next();
            return step.compose(parseRelativeLocationPath());
        }
        if (currentToken == TOK_SLASH_SLASH) {
            next();
            return step.compose(descendantOrSelfAxis.compose(parseRelativeLocationPath()));
        }
        return step;
    }

    //
    // XPath production #4
    //
    private ConvertibleNodeSetExpr parseStep() throws ParseException 
    {
        switch (currentToken) {
        case TOK_AXIS:
            {
                AxisExpr axis = (AxisExpr)axisTable.get(currentTokenValue);
                if (axis == null) {
                    throw new ParseException("no such axis");
		}
                boolean isAttribute = currentTokenValue.equals("attribute");
                next();
                return parsePredicates(axis, parseNodeTest(isAttribute));
            }
        case TOK_DOT:
            next();
            return selfAxis;
        case TOK_DOT_DOT:
            next();
            return parentAxis;
        case TOK_AT:
            next();
            return parsePredicates(attributeAxis, parseNodeTest(true));
        default:
            return parsePredicates(childAxis, parseNodeTest(false));
        }
    }

    //
    // Productions #4 and #8
    //
    private ConvertibleNodeSetExpr parsePredicates(AxisExpr axis, 
                                                   Pattern nodeTest) 
        throws ParseException
    {
        ConvertibleNodeSetExpr expr = axis;
        if (nodeTest != null) {
            expr = new NodeTestExpr(expr, nodeTest);
        }
        while (currentToken == TOK_LSQB) {
            next();
            expr = new FilterExpr(expr, parseOrExpr().makePredicateExpr());
            expectRsqb();
        }
        return axis.makeDocumentOrderExpr(expr);
    }

    //
    // XPath Production #7
    //
    // Compile a node test for an XPath pattern step,
    // up to, but not including any predicates
    //
    // WDL do not return null, even if the test is vacuous (e.g. "node()"
    //
    private PathPatternBase parseNodeTest(boolean isAttributeAxis) 
        throws ParseException 
    {

        PathPatternBase nodeTest;
        switch (currentToken) {

        case TOK_QNAME:
            if (isAttributeAxis) {
                nodeTest = new AttributeTest(expandName());
            } else {
                nodeTest = new ElementTest(expandName());
            }
            break;

        case TOK_STAR:
            nodeTest = isAttributeAxis ? null : new NodeTypeTest(Node.ELEMENT);
            break;

        case TOK_NAME_COLON_STAR:
            if (isAttributeAxis) {
                nodeTest = new NamespaceAttributeTest(expandPrefix());
            } else {
                nodeTest = new NamespaceElementTest(expandPrefix());
            }
            break;

        case TOK_PROCESSING_INSTRUCTION_LPAR:
            next();
            if (currentToken == TOK_LITERAL) {
                nodeTest = new ProcessingInstructionTest(expandName());
                next();
            }
            else {
                nodeTest = new NodeTypeTest(Node.PROCESSING_INSTRUCTION);
            }
            expectRpar();
            return nodeTest;

        case TOK_COMMENT_LPAR:
            next();
            expectRpar();
            return new NodeTypeTest(Node.COMMENT);

	    // text()
        case TOK_TEXT_LPAR:
            next();
            expectRpar();
            return new NodeTypeTest(Node.TEXT);

	    // node()
        case TOK_NODE_LPAR:
            next();
            expectRpar();
	    if (isAttributeAxis) {
		return new NodeTypeTest(Node.ATTRIBUTE);
	    }
            return new NodeTypeTest(Node.ALLTYPES);

        default:
            throw new ParseException("expected node test");
        }
        next();
        return nodeTest;
    }

    //
    // Checks to ensure that the CurrentToken is ')', then
    //   lexes the next
    //
    private final void expectRpar() throws ParseException 
    {
        if (currentToken != TOK_RPAR) {
            throw new ParseException("expected )");
        }
        next();
    }

    //
    // Checks to ensure that the currentToken is ']', then
    //  lexes the next
    //
    private final void expectRsqb() throws ParseException 
    {
        if (currentToken != TOK_RSQB) {
            throw new ParseException("expected ]");
        }
        next();
    }

    //
    // XPath Production #15  PrimaryExpr
    //    a VariableReference  (production #36) OR
    //      "(" Expr ")"  (prod #14) OR
    //      Literal  (prod #29)  OR
    //      Number   (prod #30)  OR
    //      FunctionCall (prod #16)
    //
    private ConvertibleExpr parsePrimaryExpr() throws ParseException 
    {
        ConvertibleExpr expr;
        switch (currentToken) {
        case TOK_VARIABLE_REF:
            // prod #36
            {
                Name name = expandName();
                if (locals.contains(name))
                    expr = new LocalVariableRefExpr(name);
                else
                    expr = new GlobalVariableRefExpr(name, node);
                break;
            }

        case TOK_LPAR:
            // prod #14
            next();
            expr = parseOrExpr();
            expectRpar();
            return expr;

        case TOK_LITERAL:
            // prod #16 (handled by lexer)
            expr = new LiteralExpr(currentTokenValue);
            break;

        case TOK_NUMBER:
            // prod #30 (handled by lexer)
            expr = new NumberConstantExpr(Converter.toNumber(currentTokenValue));
            break;

        case TOK_FUNCTION_LPAR:
            // prod #16 FunctionCall
            {
                // try a lookup to find if we have somebody who can make
		// a CallExpression  
                Function function = 
                    (Function) functionTable.get(currentTokenValue);

                if (function == null) {
                    // "current()" is special, 'cuz we'll want
                    // to take note of the fact that this expr
                    // uses it
                    if (!currentTokenValue.equals("current")) {
                        throw new ParseException("no such function: " 
                                                 + currentTokenValue);
                    }
                    usesCurrentFunction = true;
                    function = currentFunction;
                }
                next();

                return function.makeCallExpr(parseArgs(), node);

            }

            // an extension function
        case TOK_CNAME_LPAR:
            // also prod #16 FunctionCall
           {
                Name name = expandName();
                next();
                if (XT_NAMESPACE.equals(name.getNamespace())) {

                    // xt: extension functions are constructed and
                    // called just like builtin functions, rather
                    // than the more loosely coupled extension mechanism

                    Function function = 
                        (Function)extensionFunctionTable.get(name.getLocalPart());
                    if (function != null) {
                        return function.makeCallExpr(parseArgs(), node);
                    }
                } else if (EXSL_COMMON_NAMESPACE.equals(name.getNamespace())) {
                    Function function = 
                        (Function)exsltCommonFunctionTable.get(name.getLocalPart());
                    if (function != null) {
                        return function.makeCallExpr(parseArgs(), node);
                    }
                } else if (EXSL_REGEXP_NAMESPACE.equals(name.getNamespace())) {
                    Function function = 
                        (Function)exsltRegexFunctionTable.get(name.getLocalPart());
                    if (function != null) {
                        return function.makeCallExpr(parseArgs(), node);
                    }
                } else if (EXSL_STRINGS_NAMESPACE.equals(name.getNamespace())) {
                    Function function = 
                        (Function)exsltStringsFunctionTable.get(name.getLocalPart());
                    if (function != null) {
                        return function.makeCallExpr(parseArgs(), node);
                    }
		}
		
                ConvertibleExpr[] args = parseArgs();
                VariantExpr[] variantArgs = new VariantExpr[args.length];
                for (int i = 0; i < args.length; i++) {
                    variantArgs[i] = args[i].makeVariantExpr();
                }

                return new ExtensionFunctionCallExpr(name, variantArgs);
            }
        default:
            throw new ParseException("syntax error");
        }
        next();
        return expr;
    }

    //
    //parse the zero or more arguments to a function call
    // XPath Productions #16 FunctionCall, and 17 Argument
    //  Production #17 (Argument) is an Expr (Production #14) 
    // which is in turn an OrExpr (Production #21)
    //
    // We return the Arguments as an array of ConvertibleExprs
    //
    private ConvertibleExpr[] parseArgs() throws ParseException 
    {
        if (currentToken == TOK_RPAR) {
            next();
            return new ConvertibleExpr[0];
        }
        ConvertibleExpr[] args = new ConvertibleExpr[1];
        for (;;) {
            args[args.length - 1] = parseOrExpr();
            if (currentToken != TOK_COMMA) {
                break;
            }
            next();
            ConvertibleExpr[] oldArgs = args;
            args = new ConvertibleExpr[oldArgs.length + 1];
            System.arraycopy(oldArgs, 0, args, 0, oldArgs.length);
        } 
        expectRpar(); // check currentToken to ensure it's ")"
        return args;
    }

    //
    //
    private boolean tokenStartsNodeTest() 
    {
        switch (currentToken) {
        case TOK_QNAME:
        case TOK_STAR:
        case TOK_NAME_COLON_STAR:
        case TOK_PROCESSING_INSTRUCTION_LPAR:
        case TOK_COMMENT_LPAR:
        case TOK_TEXT_LPAR:
        case TOK_NODE_LPAR:
            return true;
        }
        return false;
    }

    //
    //
    private boolean tokenStartsStep() 
    {
        switch (currentToken) {
        case TOK_AXIS:
        case TOK_DOT:
        case TOK_DOT_DOT:
        case TOK_AT:
            return true;
        }
        return tokenStartsNodeTest();
    }

    //
    // returns an expanded Name from the qName in 
    // currentTokenValue
    //
    private Name expandName() throws ParseException 
    {
        try {
            if (prefixMap != null) {
                return prefixMap.expandAttributeName(currentTokenValue, null);
            } else {
                return null;
            }
        } catch (XSLException e) {
            throw new ParseException("undefined prefix");
        }
    }

    //
    // gets the Namespace URI associated with the prefix in
    // currentTokenValue
    //
    private String expandPrefix() throws ParseException 
    {
        if (prefixMap == null) {
            return null;
        }
        String ns = prefixMap.getNamespace(currentTokenValue);
        if (ns == null)
            throw new ParseException("undefined prefix");
        return ns;
    }

    /**
     *  A ConvertibleExpr allows for the casting of
     *  one type to another for the purpose of making a comparison
     */
    ConvertibleExpr makeRelationalExpr(Relation rel,
                                       ConvertibleExpr e1,
                                       ConvertibleExpr e2)
        throws ParseException 
    {

        // OPT: have some more expressions for non-variant cases
        if (e1 instanceof NodeSetExpr
            || e2 instanceof NodeSetExpr
            || e1 instanceof VariantExpr
            || e2 instanceof VariantExpr) {
            return new VariantRelationalExpr(rel,
                                             e1.makeVariantExpr(),
                                             e2.makeVariantExpr());
        }

        if (rel instanceof NumericRelation)
            return new NumberRelationalExpr(rel,
                                            e1.makeNumberExpr(),
                                            e2.makeNumberExpr());

        if (e1 instanceof BooleanExpr || e2 instanceof BooleanExpr)
            return new BooleanRelationalExpr(rel,
                                             e1.makeBooleanExpr(),
                                             e2.makeBooleanExpr());

        if (e1 instanceof NumberExpr || e2 instanceof NumberExpr)
            return new NumberRelationalExpr(rel,
                                            e1.makeNumberExpr(),
                                            e2.makeNumberExpr());

        return new StringRelationalExpr(rel,
                                        e1.makeStringExpr(),
                                        e2.makeStringExpr());
    }

    //  
    // return the index of the end of an XPath expression,
    // which is found in an XSLT value attribute template
    // and identified by a pair of curlybraces "{" "}"
    //
    private static int findExprEnd(String value, int i)
    {
        int valueLen = value.length();
        char quote = '\0';
        for (; i < valueLen; i++) {
            char c = value.charAt(i);
            switch (c) {
            case '}':
                if (quote == '\0') {
                    return i;
                }
                break;
            case '\"':
            case '\'':
                if (quote == c) {
                    // we matched quotes
                    quote = '\0';
                } else if (quote == '\0') {
                    quote = c;
                }
                break;
            }
        }
        return -1;
    }

    //
    //  Here's where we start parsing an XPath expression
    // XSLT Production #1
    //
    private TopLevelPattern parseTopLevelPattern(Node node) 
        throws XSLException 
    {
        try {
            next(); // lex "currentToken" from the pattern String

            TopLevelPattern pattern = parsePathPattern();

           
            while (currentToken == TOK_VBAR) {
                // we have an "OR" 
                next();
                pattern = new AlternativesPattern(pattern, parsePathPattern());
            }
            if (currentToken != TOK_EOF) {
                throw new ParseException("unexpected token");
            }
            if (usesCurrentFunction) {
                throw new ParseException("current() in match pattern");
            }
            return pattern;
        }
        catch (ParseException e) {
            throw makeXSLException(e, node);
        }
    }

    //
    // XSLT Production #2 LocationPathPattern
    //
    private PathPatternBase parsePathPattern() 
        throws ParseException 
    {
        Pattern parent = null;

        // first, we look for "//", "/", "/{node test}", {node-test},
        // "id(...)", "key( ... ) "
        switch (currentToken) {

        case TOK_SLASH_SLASH:
            next();
            break;

        case TOK_SLASH:
            next();
            if (!tokenStartsStep()) {
                return new NodeTypeTest(Node.ROOT);
            }
            parent = new NodeTypeTest(Node.ROOT);
            break;

        case TOK_FUNCTION_LPAR:
            if (currentTokenValue.equals("id")) {
                next();
                if (currentToken != TOK_LITERAL) {
                    throw new ParseException("expected literal");
		}
                PathPatternBase tem = new IdPattern(currentTokenValue);
                next();
                expectRpar();
                if (currentToken == TOK_SLASH)
                    parent = tem;
                else if (currentToken == TOK_SLASH_SLASH)
                    parent = new InheritPattern(tem);
                else
                    return tem;
                next();
                break;
            } else if (currentTokenValue.equals("key")) {

                next();
                if (currentToken != TOK_LITERAL) {
                    throw new ParseException("key pattern: expected literal arg 1");
		}
		String keyName = currentTokenValue;
		next();

                if (currentToken != TOK_COMMA) {
                    throw new ParseException("key pattern: expected comma between two literals");
		}
		next();
                if (currentToken != TOK_LITERAL) {
                    throw new ParseException("key pattern: expected literal arg 2");
		}

                PathPatternBase tem = new KeyPattern(keyName, currentTokenValue);
                next();
                expectRpar();
                if (currentToken == TOK_SLASH) {
                    parent = tem;
		}
                else if (currentToken == TOK_SLASH_SLASH) {
                    parent = new InheritPattern(tem);
                } else {
                    return tem;
		}
                next();
                break;
            }

            throw new ParseException("function illegal in pattern");
        default:
            break;
        }
        for (;;) {
            PathPatternBase tem = parseStepPattern();
            if (parent != null) {
                tem = new ParentPattern(tem, parent);
	    }

            if (currentToken == TOK_SLASH) {
                parent = tem;
            } else if (currentToken == TOK_SLASH_SLASH) {
                parent = new InheritPattern(tem);
            } else {
		// this way out ...
                return tem;
	    }
            next();
        }
    }

    //
    // parse one location step in an XPath pattern XSLT Production #5
    //
    private PathPatternBase parseStepPattern() throws ParseException 
    {
        PathPatternBase pattern;
        if (currentToken == TOK_AT
            || (currentToken == TOK_AXIS
                && currentTokenValue.equals("attribute"))) {
            next();
            pattern = parseNodeTest(true);

            if (pattern == null) {
                pattern = new NodeTypeTest(Node.ATTRIBUTE);
            }

        } else {
            if (currentToken == TOK_AXIS
                && currentTokenValue.equals("child")) {
                next();
            }
            pattern = parseNodeTest(false);


            // FIXME implement this -- I think I did
            if (pattern == null) {
                throw new ParseException("node() in step pattern not implemented");
            }

        }
        while (currentToken == TOK_LSQB) {
            next();
            pattern = new FilterPattern(pattern,
                                        parseOrExpr().makePredicateExpr());
            expectRsqb();
        }
        return pattern;
    }

    //
    //
    //
    static boolean functionAvailable(Name name, 
                                     ExprContext context) 
        throws XSLException {
        String ns = name.getNamespace();
        if (ns == null) {
            return functionTable.get(name.getLocalPart()) != null;
        } else if (ns.equals(XT_NAMESPACE)
            && extensionFunctionTable.get(name.getLocalPart()) != null) {
            return true;
        } else if (ns.equals(EXSL_COMMON_NAMESPACE)
            && exsltCommonFunctionTable.get(name.getLocalPart()) != null) {
            return true;
        } else if (ns.equals(EXSL_REGEXP_NAMESPACE)
            && exsltRegexFunctionTable.get(name.getLocalPart()) != null) {
            return true;
        } else if (ns.equals(EXSL_STRINGS_NAMESPACE)
            && exsltStringsFunctionTable.get(name.getLocalPart()) != null) {
            return true;
        } else {
            return context.getExtensionContext(ns).available(name.getLocalPart());
        }


        //         static final String EXSL_DATE_NAMESPACE = "http://exslt.org/dates-and-times";
        //         static final String EXSL_MATH_NAMESPACE = "http://exslt.org/math";
        //         static final String EXSL_REGEXP_NAMESPACE = "http://exslt.org/regular-expressions";
        //         static final String EXSL_SETS_NAMESPACE = "http://exslt.org/sets";
        //         static final String EXSL_STRINGS_NAMESPACE = "http://exslt.org/strings";
        //         static final String EXSL_FUNCTIONS_NAMESPACE = "http://exslt.org/functions";
        //         static final String EXSL_DYNAMIC_NAMESPACE = "http://exslt.org/dynamic";
        



    }

}

