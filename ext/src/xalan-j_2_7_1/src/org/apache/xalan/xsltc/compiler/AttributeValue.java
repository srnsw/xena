/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the  "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * $Id$
 */

package org.apache.xalan.xsltc.compiler;


/**
 * @author Jacek Ambroziak
 * @author Santiago Pericas-Geertsen
 */
abstract class AttributeValue extends Expression {

    public static final AttributeValue create(SyntaxTreeNode parent,
					      String text, Parser parser) {

	AttributeValue result;
	if (text.indexOf('{') != -1) {
	    result = new AttributeValueTemplate(text, parser, parent);
	}
	else if (text.indexOf('}') != -1) {
	    result = new AttributeValueTemplate(text, parser, parent);
	}
	else {
	    result = new SimpleAttributeValue(text);
	    result.setParser(parser);
	    result.setParent(parent);
	}
	return result;
    }
}
