// $Id$

package com.jclark.xsl.expr;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;

import com.jclark.xsl.om.*;

/**
 * This is where we come to find which template match pattern
 * applies to any given node when we do an apply-templates. 
 *
 * maintains lists of match patterns (PathPatterns?), and for each pattern, an
 * associated object. 
 * This is usually (always?) associated with
 * a mode for selecting the appropriate template action
 */
public class PatternList 
{

    /*
     * we do some optimizing by pre-organizing the patterns
     * by what they're able to tell us about the Nodes they match.
     * nameRules is a hastable, keyed by the Name which all
     * the patterns in a Vector will match.
     */
    private Hashtable nameRules = new Hashtable();

    /*
     * a vector of patterns for each node Type
     */ 
    private Vector typeRules[] = new Vector[Node.N_TYPES];

    // patterns who cannot tell us which name or type they match
    private Vector anyNameRules = new Vector();
    
    public PatternList() 
    {
        for (int i = 0; i < Node.N_TYPES; i++) {
            typeRules[i] = new Vector();
        }
    }

    /**
     *  finds the first pattern in the list that matches the
     * given Node in the given ExprContext.  if it is found,
     * returns the associated object. returns null if none match
     */
    public Object get(Node node, ExprContext context)
        throws XSLException 
    {
        Vector v = getVector(node);
        int len = v.size();
        for (int i = 0; i < len; i += 2) {
            // All top level patterns are PathPatterns
            if (((PathPattern)v.elementAt(i)).matches(node, context)) {
                return v.elementAt(i + 1);
            }
        }
        return null;
    }

    /**
     * get an enumeration of all Rules that might match
     * the given Node in the given ExprContext
     */
    public Enumeration getAll(Node node, ExprContext context) 
    {
        return new MatchEnumeration(getVector(node).elements(), node, context);
    }

    //
    //
    //
    private Vector getVector(Node node) 
    {
        Name nodeName = node.getName();
        if (nodeName != null) {
            Vector rules = (Vector)nameRules.get(nodeName);
            if (rules != null)
                return rules;
        }
        return typeRules[node.getType()];
    }

    // add a new pattern and associated Rule to the given Vector
    private static void append(Vector v, PathPattern pp, Object obj) 
    {
        v.addElement(pp);
        v.addElement(obj);
    }

    /**
     * add the pattern and object (a template rule) to the collection(s) of
     * patterns this manages
     */
    public void add(PathPattern pp, Object obj)
    {

	// we maintain a vector of PathPatterns for each type of node
	// and a vector of PathPatterns for each name which has
	// ever been called for in the final step of a PathPattern
	// we'll attempt to avoid putting this guy in any vector
	// he's guaranteed to not match
 
        PathPatternBase ppb = (PathPatternBase)pp;
        Name matchName = ppb.getMatchName();
        if (matchName == null) {
            byte matchNodeType = ppb.getMatchNodeType();

	    // node test "node()" goes everywhere
	    if (matchNodeType == Node.ALLTYPES) {
		for (int i = 0; i < Node.N_TYPES; ++i) {
		    append(typeRules[i], ppb, obj);
		}
	    } else {
		append(typeRules[matchNodeType], ppb, obj);
	    }

	    // it doesn't have any name, so every name qualifies
	    // for those node types which may carry a name
            switch (matchNodeType) {
            case Node.ELEMENT:
            case Node.ATTRIBUTE:
            case Node.PROCESSING_INSTRUCTION:
            case Node.ALLTYPES:            // for "node()" node test
                for (Enumeration enum =
                         nameRules.elements();  enum.hasMoreElements(); ) {
                    append((Vector)enum.nextElement(), ppb, obj);
                }
                append(anyNameRules, ppb, obj);
                break;
            }
        } else {
	    // we know what name it matches, so put it there
            Vector v = (Vector)nameRules.get(matchName);
            if (v == null) {
                v = (Vector)anyNameRules.clone();
                nameRules.put(matchName, v);
            }
            append(v, ppb, obj);
        }
    }


    // a list of PathPatterns who match in a given context
    // and their associated Rules (Actions)
    // nextElement() returns the matching Rules 
    private static class MatchEnumeration 
        implements Enumeration 
    {
        private Enumeration possibleMatches;
        private Node node;
        private Object nextMatch;
        private ExprContext context;

        MatchEnumeration(Enumeration possibleMatches, Node node, 
                         ExprContext context) 
        {
            this.node = node;
            this.possibleMatches = possibleMatches;
            this.context = context;
            setNextMatch();
        }

        public boolean hasMoreElements() 
        {
            return nextMatch != null;
        }
    
        /**
         * return the next Rule (Action) which matches 
         */
        public Object nextElement() 
        {
            Object tem = nextMatch;
            setNextMatch();
            return tem;
        }

        void setNextMatch() 
        {
            while (possibleMatches.hasMoreElements()) {
                PathPattern pp = (PathPattern)possibleMatches.nextElement();
                try {
                    if (pp.matches(node, context)) {
                        nextMatch = possibleMatches.nextElement();
                        return;
                    }
                }
                catch (XSLException e) { } // FIXME
                possibleMatches.nextElement();
            }
            nextMatch = null;
        }
    }

}

