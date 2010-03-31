/**
 * ===========================================
 * Java Pdf Extraction Decoding Access Library
 * ===========================================
 *
 * Project Info:  http://www.jpedal.org
 * (C) Copyright 1997-2009, IDRsolutions and Contributors.
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
  * RandomAccessMemoryMapBuffer.java
  * ---------------
 */

package org.jpedal.io;

import org.jpedal.utils.LogWriter;

import java.io.*;

public class RandomAccessMemoryMapBuffer implements RandomAccessBuffer {

    //private byte[] data;
    private long pointer;

    int length=0;

    File file;
    RandomAccessFile buf;

    public RandomAccessMemoryMapBuffer(InputStream in)
    {

        this.pointer = -1;

        length=0;

        FileOutputStream to=null;
        BufferedInputStream from=null;

        try {

            file=File.createTempFile("page",".bin", new File(ObjectStore.temp_dir));
            //file.deleteOnExit();

            to =new java.io.FileOutputStream(file);

            from=new BufferedInputStream(in);

			//write
			byte[] buffer = new byte[65535];
			int bytes_read;
			while ((bytes_read = from.read(buffer)) != -1){
				to.write(buffer, 0, bytes_read);
                length=length+bytes_read;
            }

        } catch (Exception e) {
            e.printStackTrace();
            //LogWriter.writeLog("Unable to save jpeg " + name);

        }
		//close streams
		try {
            if(to!=null)
			to.close();
            if(from!=null)
			from.close();
		} catch (Exception e) {
			LogWriter.writeLog("Exception " + e + " closing files");
		}

        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public RandomAccessMemoryMapBuffer(byte[] data)
    {

        this.pointer = -1;

        length=data.length;

        try {

            file=File.createTempFile("page",".bin", new File(ObjectStore.temp_dir));
            //file.deleteOnExit();

            java.io.FileOutputStream a =new java.io.FileOutputStream(file);

            a.write(data);
            a.flush();
            a.close();
            a=null;

            init();

        } catch (Exception e) {
            e.printStackTrace();
            //LogWriter.writeLog("Unable to save jpeg " + name);

        }
    }
     /**
    public RandomAccessMemoryMapBuffer(File file,int a)
    {

        this.pointer = -1;

        length= (int) file.length();

        try {

            init(file);

        } catch (Exception e) {
            e.printStackTrace();
            //LogWriter.writeLog("Unable to save jpeg " + name);

        }
    }   /**/

    private void init() throws Exception{

        // Create a read-only memory-mapped file
        buf=new RandomAccessFile(file, "r");

    }

    public long getFilePointer() throws IOException {
        return pointer;
    }

    public void seek(long pos) throws IOException {
        if ( checkPos(pos) ) {
            this.pointer = pos;
        } else {
            throw new IOException("Position out of bounds");
        }
    }

    public void close() throws IOException {

        if(buf!=null){

            buf.close();
            buf=null;
        }


        this.pointer = -1;

        if(file!=null && file.exists()){
           file.delete();
        }
    }

    /**/public void finalize(){

        try {
            super.finalize();
        } catch (Throwable throwable) {
            throwable.printStackTrace();  
        }

        //ensure removal actual file
        try {
			close();
		} catch (IOException e) {
			e.printStackTrace();
		}

    } /**/

    public long length() throws IOException {

        if (buf!=null) {
            return length;
        } else {
            throw new IOException("Data buffer not initialized.");
        }
    }

    public int read() throws IOException {
        if (checkPos(this.pointer)) {
            buf.seek(pointer++);
            return b2i(buf.readByte());
        } else {
            return -1;
        }
    }

    private int peek() throws IOException {
        if (checkPos(this.pointer)) {
            buf.seek(pointer++);
            return b2i(buf.readByte());
        } else {
            return -1;
        }
    }

    /**
     * return next line (returns null if no line)
     */
    public String readLine() throws IOException {

        if (this.pointer >= this.length - 1) {
            return null;
        } else {

            StringBuffer buf = new StringBuffer();
            int c;
            while ((c = read()) >= 0) {
                if ((c == 10) || (c == 13)) {
                    if (((peek() == 10) || (peek() == 13)) && (peek() != c))
                        read();
                    break;
                }
                buf.append((char) c);
            }
            return buf.toString();
        }
    }

    public int read(byte[] b) throws IOException {

        if (buf==null) throw new IOException("Data buffer not initialized.");

        if (pointer<0 || pointer>=length)
            return -1;

        int length=this.length-(int)pointer;
        if(length>b.length)
                length=b.length;

        for (int i=0; i<length; i++) {
            buf.seek(pointer++);
            b[i] = buf.readByte();
        }
        return length;
    }

    private static final int b2i(byte b) {
        if (b>=0) return b;
        return 256+b;
    }

    private boolean checkPos(long pos) throws IOException {
        return ( (pos>=0) && (pos<length()) );
    }

    /* returns the byte data*/
    public byte[] getPdfBuffer(){

        byte[] bytes=new byte[length];
        try {
            buf.seek(0);
            buf.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }
}

