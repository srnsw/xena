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
* ImageHandler.java
* ---------------
*/
package org.jpedal.external;

import org.jpedal.objects.GraphicsState;
import org.jpedal.objects.raw.PdfArrayIterator;
import org.jpedal.objects.raw.PdfObject;
import org.jpedal.objects.raw.XObject;
import org.jpedal.io.ObjectStore;

import java.awt.image.BufferedImage;
import java.awt.geom.AffineTransform;
import java.awt.*;

public interface ImageHandler {

    //tell JPedal if it ignores its own Image code or not
    public boolean alwaysIgnoreGenericHandler();

    //pass in raw data for image handling - if valid image returned it will be used.
    //if alwaysIgnoreGenericHandler() is true JPedal code always ignored. If false, JPedal code used if null
    public BufferedImage processImageData(GraphicsState gs, PdfObject XObject, PdfObject ColorSpace );

    /**Indicate that image already scaled so should not be scaled/clipped by JPedal*/
    public boolean imageHasBeenScaled();

    /**
     * Allow user to paint directly onto g2 for screen display
     * @param image - actual BufferedImage
     * @param optionsApplied - any options already done (ie 90 rotate) - values from org.jpedal.constants.PDFImageProcessing
     * @param upside_down - AffineTransform applied to page
     * @param currentImageFile - name of file stored on disk
     * @param g2 - Graphics2D render object
     * @param renderDirect -if being rendered straight to g2 (ie bufferedImage)
     * @param objectStore - JPedals class to access cached images
     * @param isPrinting
     * @return true to ignore standard JPedal routines and false to use.
     */
    boolean drawImageOnscreen(BufferedImage image, int optionsApplied, AffineTransform upside_down, String currentImageFile, Graphics2D g2, boolean renderDirect, ObjectStore objectStore, boolean isPrinting);
}
