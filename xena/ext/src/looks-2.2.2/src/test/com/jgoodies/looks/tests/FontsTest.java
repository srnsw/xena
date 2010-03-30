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

package com.jgoodies.looks.tests;

import java.awt.Font;
import java.util.Locale;

import junit.framework.TestCase;

import com.jgoodies.looks.Fonts;

/**
 * A test case for class {@link Fonts}.
 *
 * @author	Karsten Lentzsch
 * @version $Revision$
 */
public final class FontsTest extends TestCase {

    /**
     * Checks the localization display test for a bunch of combinations
     * of font and locale.
     */
    public void testCanDisplayLocalizedText() {
        Font tahoma = new Font("Tahoma", Font.PLAIN, 12);
        if (tahoma.getName().equals(tahoma.getFontName())) {
            canDisplayLocalizedText(tahoma, Locale.ENGLISH, true);
            canDisplayLocalizedText(tahoma, Locale.GERMAN,  true);
            canDisplayLocalizedText(tahoma, Locale.FRENCH,  true);
            canDisplayLocalizedText(tahoma, Locale.ITALIAN, true);
            canDisplayLocalizedText(tahoma, Locale.CHINESE, false);
            canDisplayLocalizedText(tahoma, Locale.SIMPLIFIED_CHINESE, false);
            canDisplayLocalizedText(tahoma, Locale.TRADITIONAL_CHINESE, false);
            canDisplayLocalizedText(tahoma, Locale.JAPANESE, false);
            canDisplayLocalizedText(tahoma, Locale.KOREAN,   false);
        }
        Font msSansSerif = new Font("Microsoft Sans Serif", Font.PLAIN, 12);
        if (msSansSerif.getName().equals(msSansSerif.getFontName())) {
            canDisplayLocalizedText(msSansSerif, Locale.ENGLISH, true);
            canDisplayLocalizedText(msSansSerif, Locale.GERMAN,  true);
            canDisplayLocalizedText(msSansSerif, Locale.FRENCH,  true);
            canDisplayLocalizedText(msSansSerif, Locale.ITALIAN, true);
            canDisplayLocalizedText(msSansSerif, Locale.CHINESE, false);
            canDisplayLocalizedText(msSansSerif, Locale.SIMPLIFIED_CHINESE, false);
            canDisplayLocalizedText(msSansSerif, Locale.TRADITIONAL_CHINESE, false);
            canDisplayLocalizedText(msSansSerif, Locale.JAPANESE, false);
            canDisplayLocalizedText(msSansSerif, Locale.KOREAN,   false);
        }
    }


    private void canDisplayLocalizedText(Font font, Locale locale, boolean expectedResult) {
        boolean result = Boolean.TRUE.equals(Fonts.canDisplayLocalizedText(font, locale));
        assertEquals("Failed display test. Font=" + font + "; Locale=" + locale,
                expectedResult,
                result);
    }


}
