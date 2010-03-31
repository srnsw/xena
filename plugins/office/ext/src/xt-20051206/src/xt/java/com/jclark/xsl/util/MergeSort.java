// $Id$

package com.jclark.xsl.util;

public class MergeSort 
{

    private MergeSort() { }

    public static void sort(Comparator cmp, Object[] src)
    {
        sort(cmp, src, 0, src.length);
    }

    public static void sort(Comparator cmp, Object[] src, int off, int len)
    {
        sort(cmp, src, off, len, new Object[len], 0);
    }

    public static void sort(Comparator cmp,
                            Object[] src, int off, int len,
                            Object[] temp, int tempOff) {
        if (len <= 1) {
            return;
	}
        int halfLen = len/2;
        sortCopy(cmp, src, off, halfLen, temp, tempOff);
        sortCopy(cmp, src, off + halfLen, len - halfLen, temp, tempOff + halfLen);
        merge(cmp, temp, tempOff, halfLen, len - halfLen, src, off);
    }

    private static void sortCopy(Comparator cmp,
                                 Object[] src, int off, int len,
                                 Object[] dest, int destOff)
    {
        if (len <= 1) {
            if (len != 0)
                dest[destOff] = src[off];
            return;
        }
        int halfLen = len/2;
        sort(cmp, src, off, halfLen, dest, destOff);
        sort(cmp, src, off + halfLen, len - halfLen, dest, destOff + halfLen);
        merge(cmp, src, off, halfLen, len - halfLen, dest, destOff);
    }
  

    private static void merge(Comparator cmp,
                              Object[] src, int off1, int len1, int len2,
                              Object[] dest, int destOff)
    {
        int off2 = off1 + len1;
        if (len1 != 0 && len2 != 0) {
            for (;;) {
                if (cmp.compare(src[off1], src[off2]) <= 0) {
                    dest[destOff++] = src[off1++];
                    if (--len1 == 0)
                        break;
                }
                else {
                    dest[destOff++] = src[off2++];
                    if (--len2 == 0)
                        break;
                }
            }     
        }
        for (; len1 > 0; --len1)
            dest[destOff++] = src[off1++];
        for (; len2 > 0; --len2)
            dest[destOff++] = src[off2++];
    }
}
