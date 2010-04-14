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

package com.jgoodies.looks;

import javax.swing.UIDefaults;


/**
 * Provides predefined MicroLayoutPolicy implementations.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 *
 * @see     MicroLayout
 * @see     MicroLayouts
 * @see     MicroLayoutPolicy
 *
 * @since 2.1
 */
public final class MicroLayoutPolicies {


    private MicroLayoutPolicies() {
        // Override default constructor; prevents instantation.
    }


    // Getting a MicroLayoutPolicy ********************************************

    /**
     * Returns the default MicroLayoutPolicy for the Plastic L&amp;fs.
     * Uses component insets that are similar to the Windows L&amp;f
     * micro layout, making it easier to
     *
     * @return a Windows-like micro layout policy for the Plastic L&fs
     */
    public static MicroLayoutPolicy getDefaultPlasticPolicy() {
        return new DefaultPlasticPolicy();
    }


    /**
     * Returns the default MicroLayoutPolicy for the Windows L&amp;f.
     * It aims to describe component insets that follow the native guidelines.
     *
     * @return the default micro layout policy for the Windows platform.
     */
    public static MicroLayoutPolicy getDefaultWindowsPolicy() {
        return new DefaultWindowsPolicy();
    }


    // MicroLayoutPolicy Implementations **************************************

    /**
     * Implements the default font lookup for the Plastic L&f family
     * when running in a Windows environment.
     */
    private static final class DefaultPlasticPolicy implements MicroLayoutPolicy {

        public MicroLayout getMicroLayout(String lafName, UIDefaults table) {
            boolean isClassic = !LookUtils.IS_LAF_WINDOWS_XP_ENABLED;
            boolean isVista = LookUtils.IS_OS_WINDOWS_VISTA;
            boolean isLowRes = LookUtils.IS_LOW_RESOLUTION;
            boolean isPlasticXP = lafName.equals("JGoodies Plastic XP");
            if (isPlasticXP) {
                if (isVista) {
                    return isClassic
                        ? MicroLayouts.createPlasticXPVistaClassicMicroLayout()
                        : MicroLayouts.createPlasticXPVistaMicroLayout();
                } else {
                    return isLowRes
                        ? MicroLayouts.createPlasticXPLowResMicroLayout()
                        : MicroLayouts.createPlasticXPHiResMicroLayout();
                }
            } else {
                if (isVista) {
                    return isClassic
                        ? MicroLayouts.createPlasticVistaClassicMicroLayout()
                        : MicroLayouts.createPlasticVistaMicroLayout();
                } else {
                    return isLowRes
                        ? MicroLayouts.createPlasticLowResMicroLayout()
                        : MicroLayouts.createPlasticHiResMicroLayout();
                }
            }
        }

    }


    /**
     * Implements the default font lookup on the Windows platform.
     */
    private static final class DefaultWindowsPolicy implements MicroLayoutPolicy {

        public MicroLayout getMicroLayout(String lafName, UIDefaults table) {
            boolean isClassic = !LookUtils.IS_LAF_WINDOWS_XP_ENABLED;
            boolean isVista = LookUtils.IS_OS_WINDOWS_VISTA;
            boolean isLowRes = LookUtils.IS_LOW_RESOLUTION;
            if (isClassic) {
                return isLowRes
                    ? MicroLayouts.createWindowsClassicLowResMicroLayout()
                    : MicroLayouts.createWindowsClassicHiResMicroLayout();
            } else if (isVista) {
                return isLowRes
                    ? MicroLayouts.createWindowsVistaLowResMicroLayout()
                    : MicroLayouts.createWindowsVistaHiResMicroLayout();
            } else {
                return isLowRes
                ? MicroLayouts.createWindowsXPLowResMicroLayout()
                : MicroLayouts.createWindowsXPHiResMicroLayout();
            }
        }

    }


}
