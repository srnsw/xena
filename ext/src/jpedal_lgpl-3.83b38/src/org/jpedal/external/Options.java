/**
* ===========================================
* Java Pdf Extraction Decoding Access Library
* ===========================================
*
* Project Info:  http://www.jpedal.org
* (C) Copyright 1997-2008, IDRsolutions and Contributors.
*
* 	This file is part of JPedal
*
    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 2.1 of the License, or (at your option) any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


*
* ---------------
* Options.java
* ---------------
*/
package org.jpedal.external;

/**
 * Holds set of values to indicate types of external handler
 */
public class Options {

    /**allow user to process image
     * - implements {@link org.jpedal.external.ImageHandler}
     * examples in org.jpedal.examples.handlers
     * and sample code to use commented out in SimpleViewer
     * */
    final public static int ImageHandler=1;

    /**not used - for future expansion*/
    final public static int Renderer=2;

    /**allows user to over-ride form creation code with own
     * - needs to implement {@link org.jpedal.objects.acroforms.creation.FormFactory}
     **/
    final public static int FormFactory=3;

    /**used by SimpleViewer - use not recommended*/
    final public static int MultiPageUpdate=4;

    /**allows user to replace whole forms action Handling code
     * - needs to implement {@link org.jpedal.objects.acroforms.actions.ActionHandler}
     * It is recommended you look at Options.ExpressionEngine and Options.LinkHandler for
     * most purposes 
     */
    final public static int FormsActionHandler=5;

    /**allows user to link in their own code for Javascript validation
     * - needs to implement {@link org.jpedal.objects.javascript.ExpressionEngine}
     * Default implementation at {@link org.jpedal.objects.acroforms.creation.SwingFormFactory}
     */
    final public static int ExpressionEngine=6;

    /**allows user to link in their own code for Javascript validation
     * - needs to implement {@link org.jpedal.external.LinkHandler}
     */
    final public static int LinkHandler=7; //allow user to over-ride JPedals link handling

    /**used by SimpleViewer - use not recommended*/
    final public static int ThumbnailHandler=8;

	public static final int JPedalActionHandler = 9;
	
	public static final int SwingMouseHandler = 10;

    /**pass in SwingGUI in Viewers*/
    public static final int SwingContainer=11;

    /**allow user to track glyfs generated*/
    public static final int GlyphTracker=12;

    /**allow user to track shapes*/
    public static final int ShapeTracker=13;

    /**allow user to print own forms*/
    public static final int CustomFormPrint=14;
}
