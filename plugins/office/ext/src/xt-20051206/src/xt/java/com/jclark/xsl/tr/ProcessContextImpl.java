// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.VariantExpr;
import com.jclark.xsl.expr.Variant;
import com.jclark.xsl.expr.VariantBase;
import com.jclark.xsl.expr.StringVariant;
import com.jclark.xsl.expr.NumberVariant;
import com.jclark.xsl.expr.ExtensionContext;
import com.jclark.xsl.expr.CloneableNodeIterator;
import com.jclark.xsl.expr.CloneableNodeIteratorImpl;
import com.jclark.xsl.expr.SingleNodeIterator;
import com.jclark.xsl.expr.KeyValuesTable;

import com.jclark.xsl.sax.SaxFilterMaker;

import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Vector;

/**
 * manages the state of a transformation
 *   (and performs the transformation
 * of a source document against an XSLT stylesheet)
 */
class ProcessContextImpl implements ProcessContext
{
    /**
     * variable name/value  bindings are maintained internally as a linked list
     */
    static final class VariableBindings
    {
        VariableBindings(Name name, Variant value, VariableBindings next)
        {
            this.name = name;
            this.value = value;
            this.next = next;
        }
        final VariableBindings next;
        final Variant value;
        final Name name;
    }

    static final int OPEN_ACTION_INIT_SIZE = 2;

    static StringVariant emptyStringVariant = new StringVariant("");

    private final SheetDetails sheet;

    private final ParameterSet params; // run time params, 
                                       // passed in with constructor

    private Node root;
    private Hashtable variableValueTable = new Hashtable();
    private Name evalGlobalVariableName = null;

    private Name[] actionNames = new Name[OPEN_ACTION_INIT_SIZE];
    private Node[] actionNodes = new Node[OPEN_ACTION_INIT_SIZE];

    private int[] actionImportLevels = null;
    private int[] actionForEachLevels = null;

    private int nOpenActions = 0;

    private VariableBindings localVariables;
    private Name[] currentParamNames = null;
    private Variant[] currentParamValues = null;
    private int position = 1;
    private int lastPosition = 1;
    private NodeIterator currentIter = null;
    private Hashtable extensionTable = new Hashtable();
    private Hashtable documentTable = new Hashtable();

    private XMLProcessor processor;

    private Hashtable attributeSetInUseTable = new Hashtable();
    private Hashtable nameAliasTable;
    private Hashtable namespacePrefixMapAliasTable;
    private NameTable nameTable;
    private Hashtable objectTable = new Hashtable();

    // indexed by doc root ids, a hashtable of hashtables of "xsl:keys" 
    private Hashtable docsKeyTables = new Hashtable();
    private int nResultFragmentNodes = 0;


    /**
     *
     */
    ProcessContextImpl(SheetDetails sheet,
                       Node root,
                       XMLProcessor processor,
                       ParameterSet params)
    {
        this.sheet = sheet;
        this.root = root;
        this.processor = processor;
        this.params = params;

        if (sheet.haveNamespaceAliases()) {
            nameAliasTable = new Hashtable();
            namespacePrefixMapAliasTable = new Hashtable();
        }
        else {
            nameAliasTable = null;
            namespacePrefixMapAliasTable = null;
        }
        nameTable = root.getNamespacePrefixMap().getNameTable();
    }

    /**
     *
     */
    public void invoke(NodeIterator iter, Action action,
                       Result result)
        throws XSLException
    {
        // save some state
        int savePosition = position;
        int saveLastPosition = lastPosition;
        NodeIterator saveCurrentIter = currentIter;

        currentIter = iter;
        position = 0;
        lastPosition = 0;

        if (actionForEachLevels == null) {
            actionForEachLevels = new int[nOpenActions];
        } else if (nOpenActions > actionForEachLevels.length) {
            int[] oldActionForEachLevels = actionForEachLevels;
            actionForEachLevels = new int[nOpenActions];
            System.arraycopy(oldActionForEachLevels, 0,
                             actionForEachLevels, 0,
                             oldActionForEachLevels.length);
        }
        actionForEachLevels[nOpenActions - 1]++;
        try {
            for (;;) {
                // getLastPosition() may change the iterator,
                //    so use currentIter not iter
                Node node = currentIter.next();
                if (node == null) {
                    break;
                }
                ++position;
                action.invoke(this, node, result);
            }
        }
        finally {
            actionForEachLevels[nOpenActions - 1]--;
            position = savePosition;
            lastPosition = saveLastPosition;
            currentIter = saveCurrentIter;
        }
    }

    /**
     * run the transformation
     */
    public void process(NodeIterator iter, Name modeName,
                        Name[] paramNames, Variant[] paramValues,
                        Result result)
        throws XSLException
    {
        int savePosition = position;
        int saveLastPosition = lastPosition;
        NodeIterator saveCurrentIter = currentIter;
        currentIter = iter;
        position = 0;
        lastPosition = 0;
        Name[] saveParamNames = currentParamNames;
        currentParamNames = paramNames;
        Variant[] saveParamValues = currentParamValues;
        currentParamValues = paramValues;

        try {
            for (;;) {
                // getLastPosition() may change the iterator, so use currentIter not iter
                Node node = currentIter.next();
                if (node == null) {
                    break;
                }
                ++position;
                if (paramValues == null) {
                    processSafe(node, modeName, result);
                }
                else {
                    processUnsafe(node, modeName, result);
                }
            }
        }
        finally {
            position = savePosition;
            lastPosition = saveLastPosition;
            currentIter = saveCurrentIter;
            currentParamNames = saveParamNames;
            currentParamValues = saveParamValues;
        }
    }

    /**
     * Process the given sourceNode using the given mode name,
     *  writing any results to <code>result</code>
     */
    private void processUnsafe(Node node, Name name, Result result) 
        throws XSLException
    {
        getAction(name, node).invoke(this, node, result);
    }


    /**
     */
    void processSafe(Node node, Name name, Result result) 
        throws XSLException
    {
        if (name == null) {
            for (int i = 0; i < nOpenActions; i++) {
                if (actionNames[i] == null && actionNodes[i].equals(node)) {
                    return; // loop detected
                }
            }
        } else {
            for (int i = 0; i < nOpenActions; i++) {
                if (name.equals(actionNames[i]) && actionNodes[i].equals(node)) {
                    return; // loop detected
                }
            }
        }
        if (nOpenActions == actionNames.length) {
            Name[] oldActionNames = actionNames;
            actionNames = new Name[nOpenActions * 2];
            System.arraycopy(oldActionNames, 0, actionNames, 
                             0, nOpenActions);
            Node[] oldActionNodes = actionNodes;
            actionNodes = new Node[nOpenActions * 2];
            System.arraycopy(oldActionNodes, 0, 
                             actionNodes, 0, nOpenActions);
        }
        actionNames[nOpenActions] = name;
        actionNodes[nOpenActions] = node;

        ++nOpenActions;
        try {
            getAction(name, node).invoke(this, node, result);
        }
        finally {
            --nOpenActions;
        }

    }
    
    //
    // From the sheet, get the TemplateRuleSet for the named mode,
    // and from that, get the appropriate template for the given Node
    //
    private final Action getAction(Name name, Node node) throws XSLException
    {
        return sheet.getModeTemplateRuleSet(name).getAction(node, this);
    }

    /**
     *
     */
    public void applyImports(Node node, Result result) throws XSLException
    {
        if (actionForEachLevels != null && 
            actionForEachLevels.length >= nOpenActions &&
            actionForEachLevels[nOpenActions - 1] > 0) {
            throw new XSLException("xsl:apply-templates inside xsl:for-each", 
                                   node);
        }
        if (actionImportLevels == null) {
            actionImportLevels = new int[nOpenActions];
        }
        else if (nOpenActions > actionImportLevels.length) {
            int[] oldActionImportLevels = actionImportLevels;
            actionImportLevels = new int[nOpenActions];
            System.arraycopy(oldActionImportLevels, 0,
                             actionImportLevels, 0,
                             oldActionImportLevels.length);
        }
 	// HST: apply-imports should _not_ pass params through :-(
 	// I suppose we _could_ make this controlled by a switch . . .
        Name[] saveParamNames = currentParamNames;
        currentParamNames = null;
        Variant[] saveParamValues = currentParamValues;
        currentParamValues = null;
 	try {
            sheet.getModeTemplateRuleSet(actionNames[nOpenActions - 1])
                .getImportAction(node,
                                 this,
                                 actionImportLevels[nOpenActions - 1]++)
                .invoke(this, node, result);
            actionImportLevels[nOpenActions - 1]--;
        }
        finally {
            currentParamNames = saveParamNames;
            currentParamValues = saveParamValues;
        }
    }
    
    //      /**
    //       *
    //       */
    //      public final boolean hasAttribute(Vector nameList, Node node, String value)
    //      {
    //          int len = nameList.size();
    //          for (int i = 0; i < len; i++) {
    //             if (value.equals(node.getAttributeValue((Name)nameList.elementAt(i)))) {
    //                  return true;
    //              }
    //          }
    //          return false;
    //      }

    /**
     *
     */
    public int getPosition()
    {
        return position;
    }

    /**
     *
     */
    public SaxFilterMaker getSaxExtensionFilter()
    {
        return sheet.getSaxExtensionFilter();
    }

    /**
     *
     */
    public int getLastPosition() throws XSLException
    {
        if (lastPosition == 0) {
            lastPosition = position;
            for (NodeIterator iter = cloneCurrentIter(); iter.next() != null;)
                lastPosition++;
        }
        return lastPosition;
    }

    /**
     *
     */
    private NodeIterator cloneCurrentIter()
    {
        if (!(currentIter instanceof CloneableNodeIterator)) {
            currentIter = new CloneableNodeIteratorImpl(currentIter);
        }
        return (NodeIterator)((CloneableNodeIterator)currentIter).clone();
    }

    /**
     *
     */
    public Variant getGlobalVariableValue(Name name) 
        throws XSLException
    {
        Variant value = (Variant)variableValueTable.get(name);
        if (value != null) {
            return value;
        }
        VariableInfo info = sheet.getGlobalVariableInfo(name);
        if (info == null) {
            return null;
        }

        Object obj = params.getParameter(name);

        if (obj != null) {
            value = VariantBase.create(obj).makePermanent();
            variableValueTable.put(obj, value);
            return value;
        }

        // Avoid possibility of infinite loop
        variableValueTable.put(name, emptyStringVariant);
        Name temp = evalGlobalVariableName;

        // set this so we can save it in a memento
        evalGlobalVariableName = name;
        try {
            value = info.getExpr().eval(root, this).makePermanent();
        }
        finally {
            evalGlobalVariableName = temp;
        }
        variableValueTable.put(name, value);
        return value;
    }

    /**
     *
     */
    public Variant getLocalVariableValue(Name name)
    {
        for (VariableBindings p = localVariables; p != null; p = p.next) {
            if (p.name.equals(name)) {
                return p.value;
            }
        }
        throw new Error("no such local variable");
    }

    /**
     *
     */
    public void bindLocalVariable(Name name, Variant value)
        throws XSLException
    {
        localVariables = new VariableBindings(name, value.makePermanent(), 
                                              localVariables);
    }

    /**
     *
     */
    public void unbindLocalVariables(int n)
    {
        for (; n > 0; --n) {
            localVariables = localVariables.next;
        }
    }

    /**
     *
     */
    public void invokeWithParams(Action action, Name[] paramNames, 
                                 Variant[] paramValues,
                                 Node node, Result result) 
        throws XSLException // really?
    {
        Name[] saveParamNames = currentParamNames;
        currentParamNames = paramNames;
        Variant[] saveParamValues = currentParamValues;
        currentParamValues = paramValues;
        try {
            action.invoke(this, node, result);
        }
        finally {
            currentParamNames = saveParamNames;
            currentParamValues = saveParamValues;
        }
    }

    /**
     *
     */
    public Variant getParam(Name name)
    {
        if (currentParamNames != null) {
            for (int i = 0; i < currentParamNames.length; i++) {
                if (name.equals(currentParamNames[i])) {
                    return currentParamValues[i];
                }
            }
        }
        return null;
    }

    /**
     * save some state 
     */
    public ProcessContext.Memento createMemento()
    {
        final VariableBindings rememberLocalVariables = localVariables;
        final int rememberPosition = position;
        final int rememberLastPosition = lastPosition;
        final NodeIterator rememberCurrentIter
            = lastPosition == 0 ? cloneCurrentIter() : null;
        final Name rememberEvalGlobalVariableName = evalGlobalVariableName;

        return new ProcessContext.Memento() 
            {
                
                public void invoke(Action action, Node node, 
                                   Result result) 
                    throws XSLException 
                {
                    // save some state
                    Name[] saveParamNames = currentParamNames;
                    currentParamNames = null;
                    Variant[] saveParamValues = currentParamValues;
                    currentParamValues = null;
                    int savePosition = position;
                    position = rememberPosition;
                    int saveLastPosition = lastPosition;
                    lastPosition = rememberLastPosition;
                    NodeIterator saveCurrentIter = currentIter;
                    currentIter = rememberCurrentIter;
                    VariableBindings saveLocalVariables = localVariables;
                    localVariables = rememberLocalVariables;
                    Object saveGlobalVariableValue = null;
                    if (rememberEvalGlobalVariableName != null) {
                        saveGlobalVariableValue
                            = variableValueTable.get(rememberEvalGlobalVariableName);
                        variableValueTable.put(rememberEvalGlobalVariableName,
                                               emptyStringVariant);
                    }
                    try {
                        action.invoke(ProcessContextImpl.this, node, result);
                    }
                    finally {
                        // restore the saved state
                        currentParamNames = saveParamNames;
                        currentParamValues = saveParamValues;
                        localVariables = saveLocalVariables;
                        position = savePosition;
                        lastPosition = saveLastPosition;
                        currentIter = saveCurrentIter;
                        if (rememberEvalGlobalVariableName != null) {
                            variableValueTable.put(rememberEvalGlobalVariableName,
                                                   saveGlobalVariableValue);
                        }
                    }
                }
            };
    }

    /**
     *
     */
    public ExtensionContext getExtensionContext(String namespace) 
        throws XSLException
    {
        ExtensionContext extension = 
            (ExtensionContext) extensionTable.get(namespace);
        if (extension == null) {
            extension = sheet.createExtensionContext(namespace);
            if (extension == null) {
                extension = new ExtensionContext()
                    {
                        public boolean available(String name) {
                            return false;
                        }

                        public Object call(String name, Node currentNode,
                                           Object[] args) 
                            throws XSLException 
                        {
                            throw new XSLException("implementation of extension namespace not available");
                        }
                    };
            }
            extensionTable.put(namespace, extension);
        }
        return extension;
    }

    /**
     *
     */
    public Variant getSystemProperty(Name name)
    {
        return sheet.getSystemProperty(name);
    }

    /**
     *
     */
    public Node getCurrent(Node node)
    {
        return node;
    }

    /**
     *
     */
    public void useAttributeSet(Name name, Node node, 
                                Result result) 
        throws XSLException
    {
        try {
            Action action = sheet.getAttributeSet(name);
            if (action == null)
                return;
            boolean[] inUse = (boolean[])attributeSetInUseTable.get(name);
            if (inUse == null) {
                inUse = new boolean[1];
                attributeSetInUseTable.put(name, inUse);
            }
            if (inUse[0])
                throw new XSLException("circular attribute set usage", node);
            inUse[0] = true;
            try {
                action.invoke(this, node, result);
            }
            finally {
                inUse[0] = false;
            }
        }
        catch (ClassCastException e) {
        }
    }

    /**
     * load an object model representation of the XML document at 
     *  a url constructed from the two arguments
     */
    public NodeIterator getDocument(URL baseURL, String uriRef)
        throws XSLException
    {
        int fragmentIndex = uriRef.indexOf('#');
        String fragment = null;
        if (fragmentIndex >= 0) {
            fragment = uriRef.substring(fragmentIndex + 1);
            uriRef = uriRef.substring(0, fragmentIndex);
        }
        try {
            // Handling of empty relative specs is broken on JDK 1.2 for
            // the file protocol, so workaround this.
            URL url = uriRef.length() == 0 ? baseURL : new URL(baseURL, uriRef);
            Node node = (Node)documentTable.get(url);
            if (node == null) {
                node = processor.load(url,
                                      documentTable.size() + 1 +
                                      nResultFragmentNodes,
                                      sheet.getSourceLoadContext(),
                                      root.getNamespacePrefixMap().getNameTable());
                documentTable.put(url, node);
            }

            // return an iterator starting at the document's root
            return new SingleNodeIterator(node);
        }
        catch (MalformedURLException e) {
            throw new XSLException(e);
        }
        catch (IOException e) {
            throw new XSLException(e);
        }
    }

    /**
     *  @return the table of indexed nodes for the named key in the node's document
     */
    public KeyValuesTable getKeyValuesTable(Name keyName, Node contextNode)
    {
        
        // FIXME: hashtables are probably a bit too heavyweight
        //          for docsKeysTable and docKeys
        
        // get root nodes ID
        String docsTablesKey = contextNode.getRoot().getGeneratedId();

        // find out if we've built any keys for this doc
        Hashtable docKeys = (Hashtable) docsKeyTables.get(docsTablesKey);
        if (docKeys == null) {
            docKeys = new Hashtable();
            docsKeyTables.put(docsTablesKey, docKeys);
        } 

        // find if we've already indexed the nodes in this doc for this key
        KeyValuesTable kvt = (KeyValuesTable) docKeys.get(keyName);

        if (kvt == null) {
            KeyDefinition kd = sheet.getKeyDefinition(keyName);
            if (kd == null) {
                // FIXME: throw an exception?
                System.err.println("No key definition element for: " + 
                                   keyName.toString());
                return null;
            }
            
            kvt = new KeyValuesTable(kd.getMatchPattern(),
                                     kd.getUseExpression(),
                                     contextNode,
                                     this);
            docKeys.put(keyName, kvt);
            
        } 

        return kvt;
    }

    /**
     *
     */
    public Name unaliasName(Name name)
    {
        if (nameAliasTable == null) {
            return name;
        }
        Name unaliasedName = (Name)nameAliasTable.get(name);
        if (unaliasedName == null) {
            String ns = sheet.getNamespaceAlias(name.getNamespace());
            if (ns == null) {
                unaliasedName = name;
            } else {
                unaliasedName = nameTable.createName(name.toString(), ns);
            }
        }
        nameAliasTable.put(name, unaliasedName);
        return unaliasedName;
    }
  
    /**
     *
     */
    public NamespacePrefixMap unaliasNamespacePrefixMap(NamespacePrefixMap map)
    {
        if (namespacePrefixMapAliasTable == null) {
            return map;
        }
        NamespacePrefixMap unaliasedMap
            = (NamespacePrefixMap)namespacePrefixMapAliasTable.get(map);
        if (unaliasedMap == null) {
            unaliasedMap = map;
            String ns = sheet.getNamespaceAlias(map.getDefaultNamespace());
            if (ns != null) {
                unaliasedMap = unaliasedMap.bindDefault(ns);
	    }
            int size = map.getSize();
            for (int i = 0; i < size; i++) {
                ns = sheet.getNamespaceAlias(map.getNamespace(i));
                if (ns != null) {
                    unaliasedMap = unaliasedMap.bind(map.getPrefix(i), ns);
		}
            }
        }
        namespacePrefixMapAliasTable.put(map, unaliasedMap);
        return unaliasedMap;
    }

    /**
     *
     */
    public void put(Object key, Object value)
    {
        objectTable.put(key, value);
    }

    /**
     *
     */
    public Object get(Object key)
    {
        return objectTable.get(key);
    }
    
    /**
     *
     */
    public Node getTree(Variant variant) throws XSLException
    {
        // only know how to do this with ResultFragmentVariants
        if (!(variant instanceof ResultFragmentVariant)) {
            return null;
        }
        return ((ResultFragmentVariant)variant).getTree(this);
    }
    
    /**
     *
     */
    public Result createNodeResult(Node baseNode, 
                                   Node[] rootNodeRef) 
        throws XSLException
    {
        return processor.createResult(baseNode,
                                      documentTable.size() + 
                                      ++nResultFragmentNodes,
                                      sheet.getSourceLoadContext(),
                                      rootNodeRef);
    }
}
