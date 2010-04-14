/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 * Created by: test 
 *
 * (C) Copyright 2008, IDRsolutions and Contributors.
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
 * JAITiffHelper.java
 * ---------------
 * (C) Copyright 2008, by IDRsolutions and Contributors.
 *
 * 
 * --------------------------
*/
package org.jpedal.io;

import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;

import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

public class JAITiffHelper {

    private File imgFile;
	private SeekableStream s;
	private ImageDecoder dec;

    int pageCount=0;

    /**
     * setup access to Tif file and also read page count
     */
    public JAITiffHelper(String file){

        try{
            //Get file info
            File imgFile = new File(file);
            FileSeekableStream s = new FileSeekableStream(imgFile);
            dec = ImageCodec.createImageDecoder("tiff", s, null);
            pageCount=dec.getNumPages();

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public int getTiffPageCount() {

        return pageCount;
    }

    public BufferedImage getImage(int tiffImageToLoad) {

        BufferedImage img=null;

        try {

            RenderedImage op = new javax.media.jai.NullOpImage(dec.decodeAsRenderedImage(tiffImageToLoad),
					null,
					javax.media.jai.OpImage.OP_IO_BOUND,
					null);

			img = (javax.media.jai.JAI.create("affine", op, null, new javax.media.jai.InterpolationBicubic(1))).getAsBufferedImage();

            /**change to grey as default*/
            img= ColorSpaceConvertor.convertColorspace(img, BufferedImage.TYPE_BYTE_GRAY);

        } catch (IOException e) {
			e.printStackTrace();
		}

        return img;
    }
}

