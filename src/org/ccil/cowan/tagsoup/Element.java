// This file is part of TagSoup.
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.  You may also distribute
// and/or modify it under version 2.1 of the Academic Free License.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  

package org.ccil.cowan.tagsoup;

/**
The internal representation of an actual element (not an element type).
An Element has an element type, attributes, and a successor Element
for use in constructing stacks and queues of Elements.
@see ElementType
@see AttributesImpl
*/
public class Element {


	private ElementType theType;		// type of element
	private AttributesImpl theAtts;		// attributes of element
	private Element theNext;		// successor of element
	private boolean preclosed;		// this element has been preclosed

	/**
	Return an Element from a specified ElementType.
	@param type The element type of the newly constructed element
	@param defaultAttributes True if default attributes are wanted
	*/

	public Element(ElementType type, boolean defaultAttributes) {
		theType = type;
		if (defaultAttributes) theAtts = new AttributesImpl(type.atts());
		else theAtts = new AttributesImpl();
		theNext = null;
		preclosed = false;
		}

	/**
	Return the element type.
	@return The element type.
	*/

	public ElementType type() { return theType; }

	/**
	Return the attributes as an AttributesImpl object.
	Returning an AttributesImpl makes the attributes mutable.
	@return The attributes
	@see AttributesImpl
	*/
	public AttributesImpl atts() { return theAtts; }

	/**
	Return the next element in an element stack or queue.
	@return The next element
	*/

	public Element next() { return theNext; }

	/**
	Change the next element in an element stack or queue.
	@param next The new next element
	*/

	public void setNext(Element next) { theNext = next; }

	/**
	Return the name of the element's type.
	Convenience method.
	@return The element type name
	*/

	public String name() { return theType.name(); }

	/**
	Return the namespace name of the element's type.
	Convenience method.
	@return The element type namespace name
	*/

	public String namespace() { return theType.namespace(); }

	/**
	Return the local name of the element's type.
	Convenience method.
	@return The element type local name
	*/

	public String localName() { return theType.localName(); }

	/**
	Return the content model vector of the element's type.
	Convenience method.
	@return The content model vector
	*/

	public int model() { return theType.model(); }

	/**
	Return the member-of vector of the element's type.
	Convenience method.
	@return The member-of vector
	*/

	public int memberOf() { return theType.memberOf(); }

	/**
	Return the flags vector of the element's type.
	Convenience method.
	@return The flags vector
	*/

	public int flags() { return theType.flags(); }

	/**
	Return the parent element type of the element's type.
	Convenience method.
	@return The parent element type
	*/

	public ElementType parent() { return theType.parent(); }

	/**
	Return true if the type of this element can contain the type of
	another element.
	Convenience method.
	@param other The other element
	*/

	public boolean canContain(Element other) 
	{
		// NAA CHANGE - jwaddell
		// We will be receiving HTML that will have elements containing disallowed elements, but will still be
		// browser-renderable. It causes problems if we try and fix this (especially in combination with out-of-order
		// start and end tage) so we won't try and enforce this rule.
		return true;
//		return theType.canContain(other.theType);
	}


	/**
	Set an attribute and its value into this element.
	@param name The attribute name (Qname)
	@param type The attribute type
	@param value The attribute value
	*/

	public void setAttribute(String name, String type, String value) {
		theType.setAttribute(theAtts, name, type, value);
		}

	/**
	Make this element anonymous.
	Remove any <tt>id</tt> or <tt>name</tt> attribute present
	in the element's attributes.
	*/

	public void anonymize() {
		for (int i = theAtts.getLength() - 1; i >= 0; i--) {
			if (theAtts.getType(i).equals("ID") ||
			    theAtts.getQName(i).equals("name")) {
				theAtts.removeAttribute(i);
				}
			}
		}

	/**
	Clean the attributes of this element.
	Attributes with null name (the name was ill-formed)
	or null value (the attribute was present in the element type but
	not in this actual element) are removed.  Type BOOLEAN is
	changed to type NMTOKEN at this time.  
	*/

	public void clean() {
		for (int i = theAtts.getLength() - 1; i >= 0; i--) {
			String name = theAtts.getLocalName(i);
			if (theAtts.getValue(i) == null || name == null ||
					name.length() == 0) {
				theAtts.removeAttribute(i);
				continue;
				}
			if (theAtts.getType(i).equals("BOOLEAN")) {
				theAtts.setType(i, "NMTOKEN");
				}
			}
		}

	/**
	Force this element to preclosed status, meaning that an end-tag has
	been seen but the element cannot yet be closed for structural reasons.
	*/

	public void preclose() {
		preclosed = true;
		}

	/**
	Return true if this element has been preclosed.
	*/

	public boolean isPreclosed() {
		return preclosed;
		}

	}
