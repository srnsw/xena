/*
 * Copyright (c) 2001-2009 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.jgoodies.looks.common;

import java.awt.*;

import javax.swing.JPasswordField;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.PasswordView;
import javax.swing.text.Position;


/**
 * Differs from its superclass in that it uses the UIManager's echo char,
 * not a star (&quot;*&quot;).
 * Used in Java 1.4 and Java 5 only.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
public final class ExtPasswordView extends PasswordView {

    public ExtPasswordView(Element element) {
        super(element);
    }

    public float getPreferredSpan(int axis) {
        overrideEchoChar();
        return super.getPreferredSpan(axis);
    }

    public Shape modelToView(int pos, Shape a, Position.Bias b) throws BadLocationException {
        overrideEchoChar();
        return super.modelToView(pos, a, b);
    }


    public int viewToModel(float fx, float fy, Shape a, Position.Bias[] bias) {
        overrideEchoChar();
        return super.viewToModel(fx, fy, a, bias);
    }


    /**
     * Overrides the superclass behavior to draw the Windows dot,
     * not the star (&quot;*&quot;) character.
     *
     * @param g the graphics context
     * @param x the starting X coordinate >= 0
     * @param y the starting Y coordinate >= 0
     * @param c the echo character
     * @return the updated X position >= 0
     */
    protected int drawEchoCharacter(Graphics g, int x, int y, char c) {
        Container container = getContainer();
        if (!(container instanceof JPasswordField)) {
            return super.drawEchoCharacter(g, x, y, c);
        }
        JPasswordField field = (JPasswordField) container;
        if (canOverrideEchoChar(field)) {
            c = getEchoChar();
        }
        // Painting the dot with anti-alias enabled.
        Graphics2D g2 = (Graphics2D) g;
        Object newAAHint = RenderingHints.VALUE_ANTIALIAS_ON;
        Object oldAAHint = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        if (newAAHint != oldAAHint) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, newAAHint);
        } else {
            oldAAHint = null;
        }

        int newX = super.drawEchoCharacter(g, x, y, c);

        if (oldAAHint != null) {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAAHint);
        }
        return newX;
    }


    private void overrideEchoChar() {
        Container container = getContainer();
        if (!(container instanceof JPasswordField)) {
            return;
        }
        JPasswordField field = (JPasswordField) container;
        if (canOverrideEchoChar(field)) {
            setFieldEchoChar(field, getEchoChar());
        }
    }


    private boolean canOverrideEchoChar(JPasswordField field) {
        return field.echoCharIsSet() && field.getEchoChar() == '*';
    }

    /**
     * Sets a new echo char in the given password field,
     * if and only if the new echo char differs from the old one.
     *
     * @param field        the JPasswordField to change
     * @param newEchoChar  the echo char that shall be set
     */
    private void setFieldEchoChar(JPasswordField field, char newEchoChar) {
        char oldEchoChar = field.getEchoChar();
        if (oldEchoChar == newEchoChar)
            return;
        field.setEchoChar(newEchoChar);
    }


    private static char getEchoChar() {
        return ((Character) UIManager.get("PasswordField.echoChar")).charValue();
    }


}