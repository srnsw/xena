//

package com.jclark.xsl.tr;
import com.jclark.xsl.om.Node;

public interface ActionDebugTarget extends Result
{

    public void startAction(Node stylesheetActionNode,
                            Node contextNode,
                            Object action);
    
    
    public void endAction(Node stylesheetActionNode,
                          Node contextNode,
                          Object action);


}
