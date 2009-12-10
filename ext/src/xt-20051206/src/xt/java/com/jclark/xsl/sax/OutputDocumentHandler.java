// $Id$

package com.jclark.xsl.sax;

import org.xml.sax.*;
import java.io.IOException;

/**
 * A SAX 1 DocumentHandler that serializes the results of a transform
 * to a Destination, in a manner influenced by the "xsl:output" element 
 */
public interface OutputDocumentHandler extends DocumentHandler
{
    /**
     * initialize with the given target destination and
     * xsl:output attributes.
     *
     * @return a new DocumentHandler appropriate to the task, or possibly this
     */
    DocumentHandler init(Destination dest, AttributeList atts) 
        throws SAXException, IOException;
}
