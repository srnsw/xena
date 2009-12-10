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

import java.awt.Image;
import java.awt.image.*;

import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;

import com.jgoodies.looks.Options;

/**
 * An image filter that turns an icon into a grayscale icon. Used by
 * the JGoodies Windows and Plastic L&amp;Fs to create a disabled icon.<p>
 *
 * The high-resolution gray filter can be disabled globally using
 * {@link Options#setHiResGrayFilterEnabled(boolean)}; it is enabled by default.
 * The global setting can be overridden per component by setting
 * the client property key {@link Options#HI_RES_DISABLED_ICON_CLIENT_KEY}
 * to <code>Boolean.FALSE</code>.<p>
 *
 * Thanks to Andrej Golovnin for suggesting a simpler filter formula.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
public final class RGBGrayFilter extends RGBImageFilter {

    /**
     * Overrides default constructor; prevents instantiation.
     */
    private RGBGrayFilter() {
        canFilterIndexColorModel = true;
    }


    /**
     * Returns an icon with a disabled appearance. This method is used
     * to generate a disabled icon when one has not been specified.
     *
     * @param component the component that will display the icon, may be null.
     * @param icon the icon to generate disabled icon from.
     * @return disabled icon, or null if a suitable icon can not be generated.
     */
    public static Icon getDisabledIcon(JComponent component, Icon icon) {
        if (   (icon == null)
            || (component == null)
            || (icon.getIconWidth() == 0)
            || (icon.getIconHeight() == 0)) {
            return null;
        }
        Image img;
        if (icon instanceof ImageIcon) {
            img = ((ImageIcon) icon).getImage();
        } else {
            img = new BufferedImage(
                    icon.getIconWidth(),
                    icon.getIconHeight(),
                    BufferedImage.TYPE_INT_ARGB);
            icon.paintIcon(component, img.getGraphics(), 0, 0);
        }
        if (   !Options.isHiResGrayFilterEnabled()
            || (Boolean.FALSE.equals(component.getClientProperty(Options.HI_RES_DISABLED_ICON_CLIENT_KEY)))) {
            return new ImageIcon(GrayFilter.createDisabledImage(img));
        }

        ImageProducer producer =
            new FilteredImageSource(img.getSource(), new RGBGrayFilter());

        return new ImageIcon(component.createImage(producer));
    }


    /**
     * Converts a single input pixel in the default RGB ColorModel to a single
     * gray pixel.
     *
     * @param x    the horizontal pixel coordinate
     * @param y    the vertical pixel coordinate
     * @param rgb  the integer pixel representation in the default RGB color model
     * @return a gray pixel in the default RGB color model.
     *
     * @see ColorModel#getRGBdefault
     * @see #filterRGBPixels
     */
    public int filterRGB(int x, int y, int rgb) {
        // Find the average of red, green, and blue.
        float avg = (((rgb >> 16) & 0xff) / 255f +
                     ((rgb >>  8) & 0xff) / 255f +
                      (rgb        & 0xff) / 255f) / 3;
        // Pull out the alpha channel.
        float alpha = (((rgb >> 24) & 0xff) / 255f);

        // Calculate the average.
        // Sun's formula: Math.min(1.0f, (1f - avg) / (100.0f / 35.0f) + avg);
        // The following formula uses less operations and hence is faster.
        avg = Math.min(1.0f, 0.35f + 0.65f * avg);
        // Convert back into RGB.
       return (int) (alpha * 255f) << 24 |
              (int) (avg   * 255f) << 16 |
              (int) (avg   * 255f) << 8  |
              (int) (avg   * 255f);
    }

}
