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

import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

import com.jgoodies.looks.common.ShadowPopup;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.windows.WindowsLookAndFeel;
import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;

/**
 * Provides access to optional features of the JGoodies L&amp;Fs
 * via a key to the system properties, via a key for the <code>UIDefaults</code>
 * table, via a method, or all of them.<p>
 *
 * API users can use this class' constants or their values to configure
 * the JGoodies L&amP;f. Using the constants requires the Looks library
 * classes in the class path of the using application/applet, where using
 * the String values doesn't require having this class in the class path.
 *
 * @author  Karsten Lentzsch
 * @version $Revision$
 */
public final class Options {

    // Look & Feel Names ******************************************************

    /**
     * The class name of the JGoodies Plastic L&amp;f.
     */
    public static final String PLASTIC_NAME =
        "com.jgoodies.looks.plastic.PlasticLookAndFeel";

    /**
     * The class name of the JGoodies Plastic3D L&amp;f.
     */
    public static final String PLASTIC3D_NAME =
        "com.jgoodies.looks.plastic.Plastic3DLookAndFeel";

    /**
     * The class name of the JGoodies PlasticXP L&amp;f.
     */
    public static final String PLASTICXP_NAME =
        "com.jgoodies.looks.plastic.PlasticXPLookAndFeel";

    /**
     * The class name of the JGoodies Windows L&amp;f.
     */
    public static final String JGOODIES_WINDOWS_NAME =
        "com.jgoodies.looks.windows.WindowsLookAndFeel";

    /**
     * The class name of the default JGoodies L&amp;f, PlasticXP.
     */
    public static final String DEFAULT_LOOK_NAME =
        PLASTICXP_NAME;

    /**
     * Holds a Map that enables the look&amp;feel replacement
     * mechanism to replace one look by another.
     * Maps the original class names to their replacement class names.
     *
     * @see #getReplacementClassNameFor(String)
     * @see #putLookAndFeelReplacement(String, String)
     * @see #removeLookAndFeelReplacement(String)
     */
    private static final Map LAF_REPLACEMENTS;
    static {
        LAF_REPLACEMENTS = new HashMap();
        initializeDefaultReplacements();
    }


    // Keys for Overriding Font Settings **************************************

    /**
     * A key for setting a custom FontPolicy for the Plastic L&amp;fs.
     * Used for both the system properties and the UIDefaults table.
     *
     * @see PlasticLookAndFeel#getFontPolicy()
     * @see PlasticLookAndFeel#setFontPolicy(FontPolicy)
     */
    public static final String PLASTIC_FONT_POLICY_KEY =
        "Plastic.fontPolicy";

    /**
     * A key for setting the default control font in Plastic L&amp;fs.
     * Used for both the system properties and the UIDefaults table.
     *
     * @see FontPolicies#customSettingsPolicy(FontPolicy)
     */
    public static final String PLASTIC_CONTROL_FONT_KEY =
        "Plastic.controlFont";

    /**
     * A key for setting the default menu font in Plastic L&amp;fs.
     * Used for both the system properties and the UIDefaults table.
     *
     * @see FontPolicies#customSettingsPolicy(FontPolicy)
     */
    public static final String PLASTIC_MENU_FONT_KEY =
        "Plastic.menuFont";

    /**
     * A key for setting a custom FontPolicy for the Windows L&amp;fs.
     * Used for both the system properties and the UIDefaults table.
     *
     * @see WindowsLookAndFeel#getFontPolicy()
     * @see WindowsLookAndFeel#setFontPolicy(FontPolicy)
     */
    public static final String WINDOWS_FONT_POLICY_KEY =
        "Windows.fontPolicy";

    /**
     * A key for setting the default control font in the Windows L&amp;f.
     * Used for both the system properties and the UIDefaults table.
     *
     * @see FontPolicies#customSettingsPolicy(FontPolicy)
     */
    public static final String WINDOWS_CONTROL_FONT_KEY =
        "Windows.controlFont";

    /**
     * A key for setting the default menu font in the Windows L&amp;f.
     * Used for both the system properties and the UIDefaults table.
     *
     * @see FontPolicies#customSettingsPolicy(FontPolicy)
     */
    public static final String WINDOWS_MENU_FONT_KEY =
        "Windows.menuFont";

    /**
     * A convenience constant for the standard Swing system property key
     * that configures the use of system fonts.
     *
     * @see #getUseSystemFonts()
     * @see #setUseSystemFonts(boolean)
     */
    public static final String USE_SYSTEM_FONTS_KEY =
        "swing.useSystemFontSettings";

    /**
     * A convenience constant for the standard Swing UIDefaults key
     * that configures the use of system fonts.
     *
     * @see #getUseSystemFonts()
     * @see #setUseSystemFonts(boolean)
     */
    public static final String USE_SYSTEM_FONTS_APP_KEY =
        "Application.useSystemFontSettings";


    // Keys for Micro Layout Policies *****************************************

    /**
     * A UIManager key for setting a custom MicroLayoutPolicy
     * for the Plastic L&amp;fs.
     *
     * @see PlasticLookAndFeel#getMicroLayoutPolicy()
     * @see PlasticLookAndFeel#setMicroLayoutPolicy(MicroLayout)
     */
    public static final String PLASTIC_MICRO_LAYOUT_POLICY_KEY =
        "Plastic.MicroLayoutPolicy";


    /**
     * A UIManager key for setting a custom MicroLayoutPolicy
     * for the Windows L&amp;f.
     *
     * @see WindowsLookAndFeel#getMicroLayoutPolicy()
     * @see WindowsLookAndFeel#setMicroLayoutPolicy(MicroLayout)
     */
    public static final String WINDOWS_MICRO_LAYOUT_POLICY_KEY =
        "Windows.MicroLayoutPolicy";


    // Optional Global User Properties ****************************************

    public static final String DEFAULT_ICON_SIZE_KEY =
        "jgoodies.defaultIconSize";

    public static final String USE_NARROW_BUTTONS_KEY =
        "jgoodies.useNarrowButtons";

    public static final String TAB_ICONS_ENABLED_KEY =
        "jgoodies.tabIconsEnabled";

    /**
     * A UIDefaults key for the popup drop shadow enablement.
     *
     * @see #isPopupDropShadowEnabled()
     * @see #setPopupDropShadowEnabled(boolean)
     */
    public static final String POPUP_DROP_SHADOW_ENABLED_KEY =
        "jgoodies.popupDropShadowEnabled";


    /**
     * A UIDefaults key for enabling/disabling the new high-resolution
     * gray filter globally. This setting can be overridden per component.
     *
     * @see #setHiResGrayFilterEnabled(boolean)
     * @see #HI_RES_DISABLED_ICON_CLIENT_KEY
     *
     * @since 2.1
     */
    public static final String HI_RES_GRAY_FILTER_ENABLED_KEY =
        "HiResGrayFilterEnabled";


    /**
     * A UIDefaults key for the global default whether text fields
     * shall select all text on focus gain. This can be overridden per field.
     *
     * @see #setSelectOnFocusGainEnabled(boolean)
     *
     * @since 2.2
     */
    public static final String SELECT_ON_FOCUS_GAIN_KEY =
        "JGoodies.selectAllOnFocusGain";


    // Optional Client Properties *********************************************

    /**
     * A JScrollPane client property key for a hint
     * that the scroll pane border should be etched.
     */
    public static final String IS_ETCHED_KEY = "jgoodies.isEtched";

    /**
     * A client property key for JMenuBar and JToolBar style hints.
     * Available styles are: <code>HeaderStyle.Single</code> and
     * <code>HeaderStyle.Both</code>.
     *
     * @see HeaderStyle
     * @see BorderStyle
     */
    public static final String HEADER_STYLE_KEY = "jgoodies.headerStyle";

    /**
     * A JMenu client property key for a hint
     * that the menu items in the menu have no icons.
     */
    public static final String NO_ICONS_KEY = "jgoodies.noIcons";

    /**
     * A JPopupMenu client property key for a hint that the border
     * shall have no extra margin. This is useful if the popup menu
     * contains only a single component, for example a scrollpane.
     */
    public static final String NO_MARGIN_KEY = "JPopupMenu.noMargin";

    /**
     * A JTree client property key for a tree line style hint.
     *
     * @see #TREE_LINE_STYLE_ANGLED_VALUE
     * @see #TREE_LINE_STYLE_NONE_VALUE
     */
    public static final String TREE_LINE_STYLE_KEY =
        "JTree.lineStyle";

    /**
     * A JTree client property value that indicates that lines shall be drawn.
     *
     * @see #TREE_LINE_STYLE_KEY
     */
    public static final String TREE_LINE_STYLE_ANGLED_VALUE =
        "Angled";

    /**
     * A JTree client property value that indicates that lines shall be hidden.
     *
     * @see #TREE_LINE_STYLE_KEY
     */
    public static final String TREE_LINE_STYLE_NONE_VALUE   =
        "None";

    /**
     * A JTabbedPane client property key that indicates
     * that no content border shall be painted.
     * Supported by the JGoodies Windows L&amp;f and the
     * JGoodies Plastic look&amp;feel family.
     * This effect will be achieved also if the EMBEDDED property is true.
     */
    public static final String NO_CONTENT_BORDER_KEY =
        "jgoodies.noContentBorder";

    /**
     * A JTabbedPane client property key that indicates
     * that tabs are painted with a special embedded appearance.
     * Supported by the JGoodies Windows L&amp;f and the
     * JGoodies Plastic look&amp;feel family.
     * As a side effect of the embedded appearance,
     * supporting L&amp;fs won't paint the content border.
     */
    public static final String EMBEDDED_TABS_KEY =
        "jgoodies.embeddedTabs";

    /**
     * A JComboBox client property key for the combo's
     * popup menu prototype display value. If this key is set,
     * the value will be used to compute the combo popup width.
     * This optional feature is supported by the JGoodies Windows L&amp;f
     * as well as the JGoodies Plastic L&amp;fs.
     */
    public static final String COMBO_POPUP_PROTOTYPE_DISPLAY_VALUE_KEY =
        "ComboBox.popupPrototypeDisplayValue";


    /**
     * A client property key for combo box renderer components.
     * The Boolean value indicates whether the component's border
     * can be temporarily removed when painting the current value or not.
     * This is useful for custom renderers used with Windows combo boxes.
     *
     * @see WindowsComboBoxUI#paintCurrentValue(java.awt.Graphics, java.awt.Rectangle, boolean)
     * @since 2.1
     */
    public static final String COMBO_RENDERER_IS_BORDER_REMOVABLE =
        "isBorderRemovable";


    /**
     * A client property key for components with a disabled icon
     * such as buttons, labels, and tabbed panes. Specifies whether
     * the new high resolution gray filter shall be used to compute
     * a disabled icon - if none is available.
     *
     * @see #isHiResGrayFilterEnabled()
     * @see #setHiResGrayFilterEnabled(boolean)
     * @see #HI_RES_GRAY_FILTER_ENABLED_KEY
     *
     * @since 2.1
     */
    public static final String HI_RES_DISABLED_ICON_CLIENT_KEY =
        "generateHiResDisabledIcon";


    /**
     * A JTextField client property key that specifies whether all text shall
     * be selected on focus gain. Overrides the global default.<p>
     *
     * This feature can be set using
     * {@link #setSelectOnFocusGainEnabled(JTextField, Boolean)}.
     * If you want to avoid to link application code to the JGoodies Looks
     * library, you can set the client property directly using:
     * <pre>
     * aTextField.putClientProperty("JGoodies.selectAllOnFocusGain", Boolean.TRUE);
     * </pre>
     *
     * @see #setSelectOnFocusGainEnabled(boolean)
     *
     * @since 2.2
     */
    public static final String SELECT_ON_FOCUS_GAIN_CLIENT_KEY =
        "JGoodies.selectAllOnFocusGain";


    /**
     * A JTextField client property key that specifies the direction
     * how text shall be selected on focus gain - if at all:
     * from start to end vs. end to start.
     *
     * If the value is set to Boolean.TRUE, the text is selected
     * from end to start, otherwise it is selected from start to end.<p>
     *
     * You can set the client property using:
     * <pre>
     * aTextField.putClientProperty("JGoodies.invertSelection", Boolean.TRUE);
     * </pre>
     *
     * @since 2.2
     */
    public static final String INVERT_SELECTION_CLIENT_KEY =
        "JGoodies.invertSelection";


    /**
     * A JTextField client property key that specifies whether the caret
     * shall be positioned at the start on focus lost.
     * If the value is set to Boolean.TRUE, the caret is set to position 0
     * on focus lost, otherwise the caret remains unchanged.<p>
     *
     * You can set the client property using:
     * <pre>
     * aTextField.putClientProperty("JGoodies.setCaretToStartOnFocusLost", Boolean.TRUE);
     * </pre>
     *
     * @since 2.2
     */
    public static final String SET_CARET_TO_START_ON_FOCUS_LOST_CLIENT_KEY =
        "JGoodies.setCaretToStartOnFocusLost";


    // System Settings ********************************************************

    /**
     * Holds the Boolean system property value for the use of system fonts,
     * or null, if it has not been set. If this property has been
     * set, we log a message about the choosen value.
     *
     * @see #getUseSystemFonts()
     */
    private static final Boolean USE_SYSTEM_FONTS_SYSTEM_VALUE =
        LookUtils.getBooleanSystemProperty(
                USE_SYSTEM_FONTS_KEY, "Use system fonts");


    /**
     * Holds the Boolean system property value for the use of narrow buttons
     * or null, if it has not been set. If this property has been
     * set, we log a message about the choosen value.
     *
     * @see #getUseNarrowButtons()
     */
    private static final Boolean USE_NARROW_BUTTONS_SYSTEM_VALUE =
        LookUtils.getBooleanSystemProperty(
                USE_NARROW_BUTTONS_KEY, "Use narrow buttons");


    /**
     * Holds the Boolean system property value for the tab icon enablement,
     * or null, if it has not been set. If this property has been
     * set, we log a message about the choosen value.
     *
     * @see #isTabIconsEnabled()
     */
    private static final Boolean TAB_ICONS_ENABLED_SYSTEM_VALUE =
        LookUtils.getBooleanSystemProperty(
                TAB_ICONS_ENABLED_KEY, "Icons in tabbed panes");


    /**
     * Holds the Boolean system property value for the popup drop shadow
     * enablement, or null, if it has not been set. If this property has been
     * set, we log a message about the choosen value.<p>
     *
     * This property just set the feature's enablement, not its actual
     * activation. For example, drop shadows are always inactive on
     * the Mac OS X, because this platform already provides shadows.
     * The activation is requested in <code>#isPopupDropShadowActive</code>.
     *
     * @see #isPopupDropShadowEnabled()
     * @see #isPopupDropShadowActive()
     */
    private static final Boolean POPUP_DROP_SHADOW_ENABLED_SYSTEM_VALUE =
        LookUtils.getBooleanSystemProperty(
                POPUP_DROP_SHADOW_ENABLED_KEY, "Popup drop shadows");


    // Private ****************************************************************

    private static final Dimension DEFAULT_ICON_SIZE =
        new Dimension(20, 20);

    private Options() {
        // Override default constructor; prevents instantiation.
    }


    // Accessing Options ******************************************************

    /**
     * Returns whether native system fonts shall be used, <code>true</code>
     * by default unless disabled in the system properties or UIManager.
     *
     * @return true unless disabled in the system properties or UIManager
     *
     * @see #setUseSystemFonts(boolean)
     * @see #USE_SYSTEM_FONTS_KEY
     * @see #USE_SYSTEM_FONTS_APP_KEY
     */
    public static boolean getUseSystemFonts() {
        return USE_SYSTEM_FONTS_SYSTEM_VALUE != null
            ? USE_SYSTEM_FONTS_SYSTEM_VALUE.booleanValue()
            : !Boolean.FALSE.equals(UIManager.get(USE_SYSTEM_FONTS_APP_KEY));
    }

    /**
     * Sets a value in the UIManager to indicate,
     * that a look&amp;feel may use the native system fonts.
     *
     * @param useSystemFonts   true to enable system fonts in the UIManager
     *
     * @see #getUseSystemFonts()
     * @see #USE_SYSTEM_FONTS_KEY
     * @see #USE_SYSTEM_FONTS_APP_KEY
     */
    public static void setUseSystemFonts(boolean useSystemFonts) {
        UIManager.put(USE_SYSTEM_FONTS_APP_KEY, Boolean.valueOf(useSystemFonts));
    }

    /**
     * Returns the default icon size that is used in menus, menu items and
     * toolbars. Menu items that have no icon set are aligned using the default
     * icon dimensions.
     *
     * @return the dimension of the default icon
     *
     * @see #setDefaultIconSize(Dimension)
     */
    public static Dimension getDefaultIconSize() {
        Dimension size = UIManager.getDimension(DEFAULT_ICON_SIZE_KEY);
        return size == null ? DEFAULT_ICON_SIZE : size;
    }

    /**
     * Sets the default icon size.
     *
     * @param defaultIconSize   the default icon size to set
     *
     * @see #getDefaultIconSize()
     */
    public static void setDefaultIconSize(Dimension defaultIconSize) {
        UIManager.put(DEFAULT_ICON_SIZE_KEY, defaultIconSize);
    }

    /**
     * Checks and answers if we shall use narrow button margins of 4 pixels.
     * As of the Looks version 1.4 the default value is <code>true</code>
     * (narrow) for the JGoodies Windows L&amp;F and the JGoodies Plastic
     * L&amp;F family. The native Windows L&amp;F uses narrow margins too.
     * The default can be disabled in the system properties or UIManager.<p>
     *
     * <strong>Note:</strong> Using narrow button margins can potentially cause
     * compatibility issues, if you don't take care that command buttons with
     * short labels (OK) get a reasonable minimum width. Therefore you can
     * get back to wide button margins using <code>#setUseNarrowButtons</code>.
     * Sun's L&amp;F implementations use a wider button margin of 14 pixels.<p>
     *
     * Narrow button margins make it easier to give buttons in a button bar
     * the same width, even if some button labels are long. And narrow margins
     * are useful for embedded command buttons that just have an icon,
     * or an ellipsis (&hellip;). Many style guides recommend to use a minimum
     * button width in command button bars, for example 50 dialog units on
     * Windows. Such a minimum width makes it easier to click a button,
     * just because the button area has a reasonable minimum size.
     * To ensure a reasonable button minimum width, you may configure a
     * LayoutManager, use a special panel for command button bars, or
     * a factory that vends command button bars.<p>
     *
     * The JGoodies FormLayout can layout button bars that comply with both the
     * MS Windows Layout Guidelines and the Mac Aqua Human Interface Guidelines.
     * The JGoodies Forms contains a ButtonBarBuilder to build command button
     * bars, and a ButtonBarFactory that vends frequently used button bars.
     *
     * @return <code>true</code> (default) if all buttons shall use narrow
     *     margins, <code>false</code> for wider margins
     *
     * @see #setUseNarrowButtons(boolean)
     * @see #USE_NARROW_BUTTONS_KEY
     */
    public static boolean getUseNarrowButtons() {
        return USE_NARROW_BUTTONS_SYSTEM_VALUE != null
            ? USE_NARROW_BUTTONS_SYSTEM_VALUE.booleanValue()
            : !Boolean.FALSE.equals(UIManager.get(USE_NARROW_BUTTONS_KEY));
    }

    /**
     * Globally sets the use narrow or standard button margins.<p>
     *
     * In previous versions of the JGoodies Looks this setting was supported
     * also for individual buttons - primarily to offer visual backward
     * compatibility with Sun L&amp;Fs.
     *
     * @param b   true to use narrow button margins globally
     *
     * @see #getUseNarrowButtons()
     * @see #USE_NARROW_BUTTONS_KEY
     */
    public static void setUseNarrowButtons(boolean b) {
        UIManager.put(USE_NARROW_BUTTONS_KEY, Boolean.valueOf(b));
    }

    /**
     * Checks and answers if we shall use icons in JTabbedPanes.
     * By default, tab icons are enabled. If the user has set a system property,
     * we log a message about the choosen style.
     *
     * @return true if icons in tabbed panes are enabled, false if disabled
     * @see #setTabIconsEnabled(boolean)
     */
    public static boolean isTabIconsEnabled() {
        return TAB_ICONS_ENABLED_SYSTEM_VALUE != null
            ? TAB_ICONS_ENABLED_SYSTEM_VALUE.booleanValue()
            : !Boolean.FALSE.equals(UIManager.get(TAB_ICONS_ENABLED_KEY));
    }

    /**
     * Enables or disables the use of icons in JTabbedPanes.
     *
     * @param b   true to enable icons in tabbed panes, false to disable them
     *
     * @see #isTabIconsEnabled()
     */
    public static void setTabIconsEnabled(boolean b) {
        UIManager.put(TAB_ICONS_ENABLED_KEY, Boolean.valueOf(b));
    }


    /**
     * Checks and answers whether popup drop shadows are active.
     * This feature shall be inactive with toolkits that use
     * native drop shadows, such as Aqua on the Mac OS X.
     * It is also inactive if the ShadowPopup cannot snapshot
     * the desktop background (due to security and AWT exceptions).
     * Otherwise the feature's enablement state is returned.<p>
     *
     * Currently only the Mac OS X is detected as platform where
     * the toolkit uses native drop shadows.
     *
     * @return true if drop shadows are active, false if inactive
     *
     * @see #isPopupDropShadowEnabled()
     * @see #setPopupDropShadowEnabled(boolean)
     */
    public static boolean isPopupDropShadowActive() {
        return !LookUtils.getToolkitUsesNativeDropShadows()
             && ShadowPopup.canSnapshot()
             && isPopupDropShadowEnabled();
    }

    /**
     * Checks and answers whether the optional drop shadows for
     * PopupMenus are enabled or disabled.
     *
     * @return true if drop shadows are enabled, false if disabled
     *
     * @see #isPopupDropShadowActive()
     * @see #setPopupDropShadowEnabled(boolean)
     * @see #POPUP_DROP_SHADOW_ENABLED_KEY
     */
    public static boolean isPopupDropShadowEnabled() {
        if (POPUP_DROP_SHADOW_ENABLED_SYSTEM_VALUE != null)
            return POPUP_DROP_SHADOW_ENABLED_SYSTEM_VALUE.booleanValue();

        Object value = UIManager.get(POPUP_DROP_SHADOW_ENABLED_KEY);
        return value == null
            ? isPopupDropShadowEnabledDefault()
            : Boolean.TRUE.equals(value);
    }

    /**
     * Enables or disables drop shadows in PopupMenus.
     * Note that drop shadows are always inactive on platforms
     * that provide native drop shadows such as the Mac OS X.<p>
     *
     * It is recommended to enable this feature only on platforms that
     * accelerate translucency and snapshots with the hardware.
     *
     * @param b  true to enable drop shadows, false to disable them
     *
     * @see #isPopupDropShadowActive()
     * @see #isPopupDropShadowEnabled()
     */
    public static void setPopupDropShadowEnabled(boolean b) {
        UIManager.put(POPUP_DROP_SHADOW_ENABLED_KEY, Boolean.valueOf(b));
    }

    /**
     * Checks and answers whether popup drop shadows are enabled
     * or disabled by default. True for modern Windows platforms:
     * Windows 98/ME/2000/XP.<p>
     *
     * TODO: Consider enabling popup drop shadows on Linux by default.<p>
     *
     * TODO: Consider moving the default to the individual L&amp;F's
     * component defaults initialization. For example Plastic and Plastic3D
     * may disable this feature by default, while PlasticXP enables it
     * by default.
     *
     * @return false
     */
    private static boolean isPopupDropShadowEnabledDefault() {
        return LookUtils.IS_OS_WINDOWS_MODERN;
    }


    /**
     * Checks and answers whether the new high-resolution gray filter
     * is enabled or disabled. It is enabled by default.
     *
     * @return true if the high-resolution gray filter is enabled, false if disabled
     *
     * @see #setHiResGrayFilterEnabled(boolean)
     * @see #HI_RES_GRAY_FILTER_ENABLED_KEY
     * @see #HI_RES_DISABLED_ICON_CLIENT_KEY
     *
     * @since 2.1
     */
    public static boolean isHiResGrayFilterEnabled() {
        return !Boolean.FALSE.equals(UIManager.get(HI_RES_GRAY_FILTER_ENABLED_KEY));
    }

    /**
     * Enables or disables the new high-resolution gray filter globally.
     * This gray filter can be disabled for individual components using the
     * client property key <code>HI_RES_DISABLED_ICON_CLIENT_KEY</code>.
     *
     * @param b  true to enable the high-resolution gray filter,
     *     false to disable them
     *
     * @see #isHiResGrayFilterEnabled()
     *
     * @since 2.1
     */
    public static void setHiResGrayFilterEnabled(boolean b) {
        UIManager.put(HI_RES_GRAY_FILTER_ENABLED_KEY, Boolean.valueOf(b));
    }


    /**
     * Checks and answers whether the global default for text fields
     * is to select all text on focus gain. The global default can be
     * overridden per component. This feature is turned on by default.<p>
     *
     * For all full description and background
     * see {@link #setSelectOnFocusGainEnabled(boolean)}.
     *
     * @return true if the select all feature is enabled globally,
     *      false if disabled
     *
     * @see #SELECT_ON_FOCUS_GAIN_KEY
     * @see #SELECT_ON_FOCUS_GAIN_CLIENT_KEY
     *
     * @since 2.2
     */
    public static boolean isSelectOnFocusGainEnabled() {
        return !Boolean.FALSE.equals(UIManager.get(SELECT_ON_FOCUS_GAIN_KEY));
    }

    /**
     * Enables or disables the select on focus gain feature globally.
     * This feature can be disabled for individual components using the
     * client property key <code>SELECT_ALL_ON_FOCUS_GAIN_CLIENT_KEY</code>.
     * It is enabled by default.<p>
     *
     * <b>Background:</b> If users are more likely going to reenter the entire
     * value in a text field, all text shall be selected on focus gain.
     * If users are more likely to edit a text, the caret shall be placed
     * at the end of the text.
     *
     * @param b  true to enable the select all on focus gain,
     *     false to disable this feature globally
     *
     * @see #isSelectOnFocusGainEnabled()
     *
     * @since 2.2
     */
    public static void setSelectOnFocusGainEnabled(boolean b) {
        UIManager.put(SELECT_ON_FOCUS_GAIN_KEY, Boolean.valueOf(b));
    }


    /**
     * Checks and answers whether the select all on focus gain feature
     * is active for the given field. Involves both the indiviual setting
     * and the global default.
     *
     * @param c  the component to check
     * @return true if the feature is enabled for <code>c</code> or globally,
     *     false if the feature is disabled for <code>c</code> or globally
     *     disabled.
     *
     * @since 2.2
     */
    public static boolean isSelectOnFocusGainActive(JTextComponent c) {
        Boolean enabled = getSelectOnFocusGainEnabled(c);
        if (enabled != null) {
            return enabled.booleanValue();
        }
        return isSelectOnFocusGainEnabled();
    }


    /**
     * Returns component's individual setting for the select on focus gain
     * feature.
     *
     * @param c   the component to check
     * @return Boolean.TRUE if enabled, Boolean.FALSE if disabled,
     *     null if the global default is used
     *
     * @see #isSelectOnFocusGainActive(JTextComponent)
     *
     * @since 2.2
     */
    public static Boolean getSelectOnFocusGainEnabled(JTextComponent c) {
        return (Boolean) c.getClientProperty(SELECT_ON_FOCUS_GAIN_CLIENT_KEY);
    }


    /**
     * Overrides the global default for the select all feature for the given
     * text field.
     *
     * @param field  the field to set this feature
     * @param b      Boolean.TRUE to enable, Boolean.FALSE to disable,
     *     null to use the global default
     *
     * @since 2.2
     */
    public static void setSelectOnFocusGainEnabled(JTextField field, Boolean b) {
        field.putClientProperty(SELECT_ON_FOCUS_GAIN_CLIENT_KEY, b);
    }


    // Look And Feel Replacements *********************************************

    /**
     * Puts a replacement name for a given <code>LookAndFeel</code>
     * class name in the list of all look and feel replacements.
     *
     * @param original   the name of the look-and-feel to replace
     * @param replacement   the name of the replacement look-and-feel
     * @see #removeLookAndFeelReplacement(String)
     * @see #getReplacementClassNameFor(String)
     */
    public static void putLookAndFeelReplacement(
        String original,
        String replacement) {
        LAF_REPLACEMENTS.put(original, replacement);
    }

    /**
     * Removes a replacement name for a given <code>LookAndFeel</code>
     * class name from the list of all look and feel replacements.
     *
     * @param original   the name of the look-and-feel that has been replaced
     * @see #putLookAndFeelReplacement(String, String)
     * @see #getReplacementClassNameFor(String)
     */
    public static void removeLookAndFeelReplacement(String original) {
        LAF_REPLACEMENTS.remove(original);
    }


    /**
     * A replacement used to replace a given L&f with nothing,
     * that indicates that it should not be used. For example,
     * by default we mark the WindowsClass L&f this way,
     * to remove it from L&f lists that replace the Sun Windows L&f
     * with the JGoodies Windows L&f.
     *
     * @since 2.1.3
     */
    public static final String NO_REPLACEMENT = "none";

    /**
     * Initializes some default class name replacements, that replace
     * Sun's Java look and feel, and Sun's Windows look and feel by
     * the appropriate JGoodies replacements.
     *
     * @see #putLookAndFeelReplacement(String, String)
     * @see #removeLookAndFeelReplacement(String)
     * @see #getReplacementClassNameFor(String)
     */
    private static void initializeDefaultReplacements() {
        putLookAndFeelReplacement(
            "javax.swing.plaf.metal.MetalLookAndFeel",
            PLASTIC3D_NAME);
        putLookAndFeelReplacement(
            "com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
            JGOODIES_WINDOWS_NAME);
        putLookAndFeelReplacement(
                "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel",
                NO_REPLACEMENT);
    }

    /**
     * Returns the class name that can be used to replace the specified
     * <code>LookAndFeel</code> class name.
     *
     * @param className   the name of the look-and-feel class
     * @return the name of the suggested replacement class
     *
     * @see #putLookAndFeelReplacement(String, String)
     * @see #removeLookAndFeelReplacement(String)
     */
    public static String getReplacementClassNameFor(String className) {
        String replacement = (String) LAF_REPLACEMENTS.get(className);
        if (replacement == null) {
            return className;
        } else if (replacement.equals(NO_REPLACEMENT)) {
            return null;
        } else {
            return replacement;
        }
    }

    /**
     * Returns the class name for a cross-platform <code>LookAndFeel</code>.
     *
     * @return the name of a cross platform look-and-feel class
     * @see #getSystemLookAndFeelClassName()
     */
    public static String getCrossPlatformLookAndFeelClassName() {
        return PLASTICXP_NAME;
    }

    /**
     * Returns the class name for a system specific <code>LookAndFeel</code>.
     *
     * @return the name of the system look-and-feel class
     * @see #getCrossPlatformLookAndFeelClassName()
     */
    public static String getSystemLookAndFeelClassName() {
        if (LookUtils.IS_OS_WINDOWS)
            return Options.JGOODIES_WINDOWS_NAME;
        else if (LookUtils.IS_OS_MAC)
            return UIManager.getSystemLookAndFeelClassName();
        else
            return getCrossPlatformLookAndFeelClassName();
    }

}
