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

package com.jgoodies.looks.plastic;

import java.io.File;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalFileChooserUI;


/**
 * The JGoodies Plastic L&amp;F implementation of <code>FileChooserUI</code>.
 * Uses {@link FileSystemView#getSystemIcon(File)} to look up icons for files.
 *
 * @author Andrej Golovnin
 * @author Karsten Lentzsch
 * @version $Revision$
 *
 * @see FileSystemView#getSystemIcon(File)
 */
public final class PlasticFileChooserUI extends MetalFileChooserUI {

    private final BasicFileView fileView = new SystemIconFileView();


    public static ComponentUI createUI(JComponent c) {
        return new PlasticFileChooserUI((JFileChooser) c);
    }


    public PlasticFileChooserUI(JFileChooser fileChooser) {
        super(fileChooser);
    }


    public void clearIconCache() {
        fileView.clearIconCache();
    }


    public FileView getFileView(JFileChooser fc) {
        return fileView;
    }


    /**
     * Unlike its superclass, this class can look up and cache
     * the system icon from the file chooser's file system view.
     */
    private final class SystemIconFileView extends BasicFileView {

        public Icon getIcon(File f) {
            Icon icon = getCachedIcon(f);
            if (icon != null) {
                return icon;
            }
            if ((f != null) && UIManager.getBoolean("FileChooser.useSystemIcons")) {
                icon = getFileChooser().getFileSystemView().getSystemIcon(f);
            }
            if (icon == null) {
                return super.getIcon(f);
            }
            cacheIcon(f, icon);
            return icon;
        }

    }


}
