// $Id$

package com.jclark.xsl.tr;

import com.jclark.xsl.om.*;
import com.jclark.xsl.expr.PathPattern;
import com.jclark.xsl.expr.TopLevelPattern;
import com.jclark.xsl.expr.PatternList;
import com.jclark.xsl.expr.ExprContext;

import java.util.Vector;
import java.util.Enumeration;

/**
 * holds a collection of Templates (Actions) for a mode .. 
 * their match patterns ranked by priority and importance 
 */
class TemplateRuleSet
{
  
    private Vector rules = new Vector();

    // patternList finds the best matching PathPattern and
    // associated Rule for a given Node in a given Context  
    private PatternList patternList = new PatternList();

    private Action builtinAction;

    /**
     * construct/ initialize with the default, builtin Action
     */ 
    TemplateRuleSet(Action builtinAction)
    {
        this.builtinAction = builtinAction;
    }

    /**
     * After all the patterns and actions have been added,
     * this must be called to
     * organize them to facilitate quick finding of the
     * best template action when "apply-templates" is 
     * performed on a Node
     */
    void compile() 
    {
        reverse(rules);   //  we  reverse the vector of rules
 			  //  so default tie-break for equi-important rules, i.e.
 	                  //      last one wins, takes effect
        
        sortRulesVector(rules);
        
        for (Enumeration iter = rules.elements(); iter.hasMoreElements();) {
            Rule r = (Rule)iter.nextElement();
            patternList.add(r.pattern, r);
        }
    }
  
    //
    // presumably, this makes the subsequent sort go quicker
    //
    private static void reverse(Vector v)
    {
        int i = 0;
        int j = v.size() - 1;
        for (; i < j; i++, j--) {
            Object tem = v.elementAt(i);
            v.setElementAt(v.elementAt(j), i);
            v.setElementAt(tem, j);
        }
    }


    // order by  rule importance
    // and priority,
    // so the better rules are towards the front of the vector (lower indices)
    private static void sortRulesVector(Vector v)
    {
        // Insertion sort
        int sz = v.size();
        for (int i = 1; i < sz; i++) {

	    // we do a lot of copying of pointers
	    // each time we increment i, we yank out the
	    // rule at that index, then we start backing down the
	    // list, moving each item up one, away from the front
	    // of the list, till we find where we want to put
	    // the rule we yanked from the slot indexed by i.
	    //
	    // The result is that we sort items 1 and 2, then find
	    // where to insert item 3 so that items 1, 2 and 3
	    // are sorted. Then insert item 4 so that items 1,
	    // 2, 3 and 4 are sorted. ... and so on

            Rule rule = (Rule)v.elementAt(i);
            int j;
            for (j = i; j > 0; j--) {
                Rule tem = (Rule)v.elementAt(j - 1);
                // Order best first.
                // So stop when rule is worse or equal to tem
                // => stop when not rule is better than tem.
                if (!Rule.isBetter(rule, tem)) {
                    break;
                }
                v.setElementAt(tem, j);
            }
            v.setElementAt(rule, j);
        }
    }

    /**
     * add a new (match template) rule to the set
     */
    void add(TopLevelPattern pattern,
             //         Importance ruleImportance,
             Importance importImportance,
             Priority priority,
             Action action)
    {
	// a top level match pattern can be an OR of alternatives, so
	// we'll add each alternative separately
        PathPattern[] alternatives = pattern.getAlternatives();
        for (int i = 0; i < alternatives.length; i++) {
            Rule r = new Rule(alternatives[i],
                              importImportance,
                              priority,
                              action);
            //System.err.println("nr: "+r);
            rules.addElement(r);
        }
    }

    /**
     * finds and returns the TemplateAction that is the best match
     * (or highest priority) for the given Node in the given context
     */
    Action getAction(Node node, ExprContext context) throws XSLException
    {
        Rule r = (Rule)patternList.get(node, context);
        if (r == null) {
            return builtinAction;
        }
        return r.action;
    }

    /**
     *
     */
    Action getImportAction(Node node, ExprContext context, int importLevel)
        throws XSLException
    {
	// we need to override PatternList's default algorithm for
	// finding best match 
        
        Enumeration rules = patternList.getAll(node, context);

 	// HST: This is all the matching rules, in order of importance
 	//      So the first one is the first actual match
 	// HST: The rule for apply-imports is
        //       "only template rules that were imported into the stylesheet element
        //        containing the current template rule"
        //      So hit must be less important than winner [make it be imported
        //                                                 from the importing sheet]
        //      But in case of nested apply-imports, we reset and keep going as many
 	//      times as we are deep
        Rule r = (Rule)rules.nextElement();
        Importance minImportance = r.importImportance;
 	// System.err.println("r1: "+r);
        int currentLevel = 0;
        while (rules.hasMoreElements()) {
            r = (Rule)rules.nextElement();
            //System.err.println("r: "+r+" "+" "+currentLevel+" "+importLevel);
            if (r.importImportance.compareTo(minImportance) >= 0) {
                continue;
            }
            
            if (currentLevel == importLevel) {
                return r.action;
            }
            currentLevel++;
            minImportance = r.importImportance;
            
        }
        
        return builtinAction;
    }
    
    ///////////////////////////////////////////////////////////
    //
    // Represents a match pattern (PathPattern), its
    // importance and priority and its associated 
    // template Action
    //
    private static class Rule
    {
        static Importance zero = Importance.create();

        final PathPattern pattern;
        final Importance importImportance;
        final Priority priority;
        final Action action;

        Rule(PathPattern pattern,
             Importance importImportance,
             Priority priority,
             Action action)
        {
            this.importImportance = importImportance;
            if (priority == null) {
                this.priority =
                    Priority.createDefault(pattern.getDefaultPriority());
            } else {
                this.priority = priority;
            }
            this.pattern = pattern;
            this.action = action;
        }

        /**
         * compare priorities and importance
         */
        static boolean isBetter(Rule rule1, Rule rule2)
        {
            int n = rule1.importImportance.compareTo(rule2.importImportance);
            if (n == 0) {
                return rule1.priority.compareTo(rule2.priority) > 0;
            }
            // if n < 0, rule1.importance - rule2.importance < 0
            // => rule1.importance < rule2.importance
            // => false
            return n > 0;
        }
 
        public String toString() 
        {
            return "r:" + hashCode() + ":" + pattern.toString() +
                "|" + importImportance.compareTo(zero);
        }

    }
}
