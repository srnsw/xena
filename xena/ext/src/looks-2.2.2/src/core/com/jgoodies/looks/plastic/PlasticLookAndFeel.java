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

import java.awt.Color;
import java.awt.Insets;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;

import com.jgoodies.looks.*;
import com.jgoodies.looks.common.MinimumSizedIcon;
import com.jgoodies.looks.common.RGBGrayFilter;
import com.jgoodies.looks.common.ShadowPopupFactory;
import com.jgoodies.looks.plastic.theme.SkyBluer;

/**
 * The base class for the JGoodies Plastic look&amp;feel family.
 * Initializes class and component defaults for the Plastic L&amp;f
 * and provides keys and optional features for the Plastic family.
 *
 * @author Karsten Lentzsch
 * @version $Revision$
 */
public class PlasticLookAndFeel extends MetalLookAndFeel {

    // System and Client Property Keys ****************************************

	/**
     * Client property key to set a border style - shadows the header style.
     */
	public static final String BORDER_STYLE_KEY = "Plastic.borderStyle";

	/**
     * Client property key to disable the pseudo 3D effect.
     */
	public static final String IS_3D_KEY = "Plastic.is3D";

    /**
     * A System property key to set the default theme.
     */
    public static final String DEFAULT_THEME_KEY =
        "Plastic.defaultTheme";

    /**
     * A System property key that indicates that the high contrast
     * focus colors shall be choosen - if applicable.
     * If not set, some focus colors look good but have low contrast.
     * Basically, the low contrast scheme uses the Plastic colors
     * before 1.0.7, and the high contrast scheme is 1.0.7 - 1.0.9.
     */
    public static final String HIGH_CONTRAST_FOCUS_ENABLED_KEY =
        "Plastic.highContrastFocus";

    /**
     * A System property key for the rendering style of the Plastic
     * TabbedPane. Valid values are: <tt>default</tt> for the
     * Plastic 1.0 tabs, and <tt>metal</tt> for the Metal L&amp;F tabs.
     */
    protected static final String TAB_STYLE_KEY =
        "Plastic.tabStyle";

    /**
     * A System property value that indicates that Plastic shall render
     * tabs in the Plastic 1.0 style. This is the default.
     */
    public static final String TAB_STYLE_DEFAULT_VALUE =
        "default";

    /**
     * A System property value that indicates that Plastic shall
     * render tabs in the Metal L&amp;F style.
     */
    public static final String TAB_STYLE_METAL_VALUE =
        "metal";


    /**
     * A UIManager key used to store and retrieve the PlasticTheme in Java 1.4
     *
     * @see #getPlasticTheme()
     * @see #setPlasticTheme(PlasticTheme)
     */
    private static final Object THEME_KEY = new StringBuffer("Plastic.theme");


    // State *****************************************************************

    /**
     * Holds whether Plastic uses Metal or Plastic tabbed panes.
     */
    private static boolean useMetalTabs =
        LookUtils.getSystemProperty(TAB_STYLE_KEY, "").
            equalsIgnoreCase(TAB_STYLE_METAL_VALUE);

    /**
     * Holds whether we are using the high contrast focus colors.
     */
    private static boolean useHighContrastFocusColors =
        LookUtils.getSystemProperty(HIGH_CONTRAST_FOCUS_ENABLED_KEY) != null;

	/**
     * The List of installed Plastic themes.
     */
	private static List	installedThemes;

	/** The look-global state for the 3D enablement. */
	private static boolean is3DEnabled = false;


    private static boolean selectTextOnKeyboardFocusGained =
        LookUtils.IS_OS_WINDOWS;

    /**
     * In Java 5 or later, this field holds the public static method
     * <code>MetalLookAndFeel#getCurrentTheme</code>.
     */
    private static Method getCurrentThemeMethod = null;

    static {
        if (LookUtils.IS_JAVA_5_OR_LATER) {
            getCurrentThemeMethod = getMethodGetCurrentTheme();
        }
    }

    // Instance Creation ******************************************************

    /**
     * Constructs the PlasticLookAndFeel, creates the default theme
     * and sets it as current Plastic theme.
     */
    public PlasticLookAndFeel() {
        getPlasticTheme();
    }


    // L&f Description ********************************************************

    public String getID() {
        return "JGoodies Plastic";
    }

    public String getName() {
        return "JGoodies Plastic";
    }

    public String getDescription() {
        return "The JGoodies Plastic Look and Feel"
            + " - \u00a9 2001-2009 JGoodies Karsten Lentzsch";
    }


    // Optional Settings ******************************************************

    /**
     * Looks up and retrieves the FontPolicy used
     * by the JGoodies Plastic Look&amp;Feel family.
     * If a FontPolicy has been set, it'll be returned.
     * Otherwise, this method checks if a FontPolicy or FontSet is defined
     * in the system properties or UIDefaults. If so, it is returned.
     * If no FontPolicy has been set for this look, in the system
     * properties or UIDefaults, the default Plastic font policy
     * will be returned.
     *
     * @return the FontPolicy set for this Look&amp;feel - if any,
     *     the FontPolicy specified in the system properties or UIDefaults
     *     - if any, or the default Plastic font policy.
     *
     * @see #setFontPolicy
     * @see Options#PLASTIC_FONT_POLICY_KEY
     * @see FontPolicies
     * @see FontPolicies#customSettingsPolicy(FontPolicy)
     * @see FontPolicies#getDefaultPlasticPolicy()
     */
    public static FontPolicy getFontPolicy() {
        FontPolicy policy =
            (FontPolicy) UIManager.get(Options.PLASTIC_FONT_POLICY_KEY);
        if (policy != null)
            return policy;

        FontPolicy defaultPolicy = FontPolicies.getDefaultPlasticPolicy();
        return FontPolicies.customSettingsPolicy(defaultPolicy);
    }


    /**
     * Sets the FontPolicy to be used with the JGoodies Plastic L&amp;F
     * family. If the specified policy is <code>null</code>, the default will
     * be reset.
     *
     * @param fontPolicy   the FontPolicy to be used with
     *     the JGoodies Plastic L&amp;F family, or <code>null</code> to reset
     *     to the default
     *
     * @see #getFontPolicy()
     * @see Options#PLASTIC_FONT_POLICY_KEY
     */
    public static void setFontPolicy(FontPolicy fontPolicy) {
        UIManager.put(Options.PLASTIC_FONT_POLICY_KEY, fontPolicy);
    }


    /**
     * Looks up and retrieves the MicroLayoutPolicy used by
     * the JGoodies Plastic Look&amp;Fs.
     * If a MicroLayoutPolicy has been set for this look, it'll be returned.
     * Otherwise, the default Plastic micro layout policy will be returned.
     *
     * @return the MicroLayoutPolicy set for this Look&amp;feel - if any,
     *     or the default Plastic MicroLayoutPolicy.
     *
     * @see #setMicroLayoutPolicy
     * @see Options#PLASTIC_MICRO_LAYOUT_POLICY_KEY
     * @see MicroLayoutPolicies
     */
    public static MicroLayoutPolicy getMicroLayoutPolicy() {
        MicroLayoutPolicy policy =
            (MicroLayoutPolicy) UIManager.get(Options.PLASTIC_MICRO_LAYOUT_POLICY_KEY);
        return policy != null
            ? policy
            : MicroLayoutPolicies.getDefaultPlasticPolicy();
    }


    /**
     * Sets the MicroLayoutPolicy to be used with the JGoodies Plastic L&amp;Fs.
     * If the specified policy is <code>null</code>, the default will be reset.
     *
     * @param microLayoutPolicy   the MicroLayoutPolicy to be used with
     *     the JGoodies Plastic L&amp;Fs, or <code>null</code> to reset
     *     to the default
     *
     * @see #getMicroLayoutPolicy()
     * @see Options#PLASTIC_MICRO_LAYOUT_POLICY_KEY
     */
    public static void setMicroLayoutPolicy(MicroLayout microLayoutPolicy) {
        UIManager.put(Options.PLASTIC_MICRO_LAYOUT_POLICY_KEY, microLayoutPolicy);
    }


    protected boolean is3DEnabled() {
        return is3DEnabled;
    }

    public static void set3DEnabled(boolean b) {
        is3DEnabled = b;
    }

    public static String getTabStyle() {
        return useMetalTabs ? TAB_STYLE_METAL_VALUE : TAB_STYLE_DEFAULT_VALUE;
    }

    public static void setTabStyle(String tabStyle) {
        useMetalTabs = tabStyle.equalsIgnoreCase(TAB_STYLE_METAL_VALUE);
    }

    public static boolean getHighContrastFocusColorsEnabled() {
        return useHighContrastFocusColors;
    }

    public static void setHighContrastFocusColorsEnabled(boolean b) {
        useHighContrastFocusColors = b;
    }

    public static boolean isSelectTextOnKeyboardFocusGained() {
        return selectTextOnKeyboardFocusGained;
    }


    /**
     * Sets whether text field text shall be selected when it gains focus
     * via the keyboard. This is enabled on Windows by default and
     * disabled on all other platforms.
     *
     * @param b
     */
    public static void setSelectTextOnKeyboardFocusGained(boolean b) {
        selectTextOnKeyboardFocusGained = b;
    }


	// Overriding Superclass Behavior ***************************************

    /**
     * Invoked during <code>UIManager#setLookAndFeel</code>. In addition
     * to the superclass behavior, we install the ShadowPopupFactory.
     *
     * @see #uninitialize
     */
    public void initialize() {
        super.initialize();
        ShadowPopupFactory.install();
    }


    /**
     * Invoked during <code>UIManager#setLookAndFeel</code>. In addition
     * to the superclass behavior, we uninstall the ShadowPopupFactory.
     *
     * @see #initialize
     */
    public void uninitialize() {
        super.uninitialize();
        ShadowPopupFactory.uninstall();
    }


    /**
     * Returns an icon with a disabled appearance. This method is used
     * to generate a disabled icon when one has not been specified.<p>
     *
     * This method will be used only on JDK 5.0 and later.
     *
     * @param component the component that will display the icon, may be null.
     * @param icon the icon to generate disabled icon from.
     * @return disabled icon, or null if a suitable icon can not be generated.
     */
    public Icon getDisabledIcon(JComponent component, Icon icon) {
        Icon disabledIcon = RGBGrayFilter.getDisabledIcon(component, icon);
        return disabledIcon != null
             ? new IconUIResource(disabledIcon)
             : null;
    }


	/**
	 * Initializes the class defaults, that is, overrides some UI delegates
	 * with JGoodies Plastic implementations.
	 *
     * @param table   the UIDefaults table to work with
	 * @see javax.swing.plaf.basic.BasicLookAndFeel#getDefaults()
	 */
	protected void initClassDefaults(UIDefaults table) {
		super.initClassDefaults(table);

		final String plasticPrefix = "com.jgoodies.looks.plastic.Plastic";
        final String commonPrefix  = "com.jgoodies.looks.common.ExtBasic";

		// Overwrite some of the uiDefaults.
		Object[] uiDefaults = {
				// 3D effect; optional narrow margins
				"ButtonUI",					plasticPrefix + "ButtonUI",
				"ToggleButtonUI",			plasticPrefix + "ToggleButtonUI",

				// 3D effect
				"ComboBoxUI", 	 			plasticPrefix + "ComboBoxUI",
				"ScrollBarUI", 				plasticPrefix + "ScrollBarUI",
				"SpinnerUI",				plasticPrefix + "SpinnerUI",

				// Special borders defined by border style or header style, see LookUtils
				"MenuBarUI",				plasticPrefix + "MenuBarUI",
				"ToolBarUI",				plasticPrefix + "ToolBarUI",

				// Aligns menu icons
                "MenuUI",                   plasticPrefix + "MenuUI",
				"MenuItemUI",				commonPrefix + "MenuItemUI",
				"CheckBoxMenuItemUI",		commonPrefix + "CheckBoxMenuItemUI",
				"RadioButtonMenuItemUI",	commonPrefix + "RadioButtonMenuItemUI",

                // Provides an option for a no margin border
                "PopupMenuUI",              plasticPrefix + "PopupMenuUI",

				// Has padding above and below the separator lines
		        "PopupMenuSeparatorUI",		commonPrefix + "PopupMenuSeparatorUI",

                // Honors the screen resolution and uses a minimum button width
                "OptionPaneUI",             plasticPrefix + "OptionPaneUI",

                // Can installs an optional etched border
				"ScrollPaneUI",				plasticPrefix + "ScrollPaneUI",

                // Uses a modified split divider
				"SplitPaneUI", 				plasticPrefix + "SplitPaneUI",

                // Renders a circle, not a star '*' in Java 1.4 and Java 5
                // Selects all text after focus gain via keyboard.
                "PasswordFieldUI",          plasticPrefix + "PasswordFieldUI",

                // Updates the disabled and inactive background
                "TextAreaUI",               plasticPrefix + "TextAreaUI",

				// Modified icons and lines
				"TreeUI", 					plasticPrefix + "TreeUI",

				// Just to use Plastic colors
				"InternalFrameUI",			plasticPrefix + "InternalFrameUI",

                // Share the UI delegate instances
                "SeparatorUI",              plasticPrefix + "SeparatorUI",
                "ToolBarSeparatorUI",       plasticPrefix + "ToolBarSeparatorUI",

                // Optionally looks up the system icons
                "FileChooserUI",            plasticPrefix + "FileChooserUI"

			};
        if (!useMetalTabs) {
            // Modified tabs and ability use a version with reduced borders.
            uiDefaults = append(uiDefaults,
                    "TabbedPaneUI", plasticPrefix + "TabbedPaneUI");
        }
        if (isSelectTextOnKeyboardFocusGained()) {
            // Selects all text after focus gain via keyboard.
            uiDefaults = append(uiDefaults,
                    "TextFieldUI", plasticPrefix + "TextFieldUI");
            uiDefaults = append(uiDefaults,
                    "FormattedTextFieldUI", plasticPrefix + "FormattedTextFieldUI");
        }
		table.putDefaults(uiDefaults);
	}


	protected void initComponentDefaults(UIDefaults table) {
		super.initComponentDefaults(table);

        MicroLayout microLayout = getMicroLayoutPolicy().getMicroLayout(getName(), table);
        Insets buttonBorderInsets = microLayout.getButtonBorderInsets();

        Object marginBorder				= new BasicBorders.MarginBorder();

        Object buttonBorder				= PlasticBorders.getButtonBorder(buttonBorderInsets);
        Object comboBoxButtonBorder     = PlasticBorders.getComboBoxArrowButtonBorder();
        Border comboBoxEditorBorder     = PlasticBorders.getComboBoxEditorBorder();
		Object menuItemBorder			= PlasticBorders.getMenuItemBorder();
        Object textFieldBorder			= PlasticBorders.getTextFieldBorder();
        Object toggleButtonBorder		= PlasticBorders.getToggleButtonBorder(buttonBorderInsets);

		Object scrollPaneBorder			= PlasticBorders.getScrollPaneBorder();
		Object tableHeaderBorder		= new BorderUIResource(
										   (Border) table.get("TableHeader.cellBorder"));

		Object menuBarEmptyBorder		= marginBorder;
		Object menuBarSeparatorBorder	= PlasticBorders.getSeparatorBorder();
		Object menuBarEtchedBorder		= PlasticBorders.getEtchedBorder();
		Object menuBarHeaderBorder		= PlasticBorders.getMenuBarHeaderBorder();

		Object toolBarEmptyBorder		= marginBorder;
		Object toolBarSeparatorBorder	= PlasticBorders.getSeparatorBorder();
		Object toolBarEtchedBorder		= PlasticBorders.getEtchedBorder();
		Object toolBarHeaderBorder		= PlasticBorders.getToolBarHeaderBorder();

		Object internalFrameBorder		= getInternalFrameBorder();
		Object paletteBorder			= getPaletteBorder();

		Color controlColor 				= table.getColor("control");

		Object checkBoxIcon				= PlasticIconFactory.getCheckBoxIcon();
		Object checkBoxMargin = microLayout.getCheckBoxMargin();

        Object buttonMargin = microLayout.getButtonMargin();
		Object textInsets = microLayout.getTextInsets();
        Object wrappedTextInsets = microLayout.getWrappedTextInsets();
        Insets comboEditorInsets = microLayout.getComboBoxEditorInsets();

        Insets comboEditorBorderInsets = comboBoxEditorBorder.getBorderInsets(null);
        int comboBorderSize  = comboEditorBorderInsets.left;
        int comboPopupBorderSize = microLayout.getComboPopupBorderSize();
        int comboRendererGap = comboEditorInsets.left + comboBorderSize - comboPopupBorderSize;
        Object comboRendererBorder = new EmptyBorder(1, comboRendererGap, 1, comboRendererGap);
        Object comboTableEditorInsets = new Insets(0, 0, 0, 0);

	    Object menuItemMargin			= microLayout.getMenuItemMargin();
		Object menuMargin				= microLayout.getMenuMargin();

		Icon   menuItemCheckIcon		= new MinimumSizedIcon();
		Icon   checkBoxMenuItemIcon		= PlasticIconFactory.getCheckBoxMenuItemIcon();
		Icon   radioButtonMenuItemIcon	= PlasticIconFactory.getRadioButtonMenuItemIcon();

		Color  menuItemForeground		= table.getColor("MenuItem.foreground");

        Color inactiveTextBackground    = table.getColor("TextField.inactiveBackground");

		// 	Should be active.
		int     treeFontSize			= table.getFont("Tree.font").getSize();
		Integer rowHeight				= new Integer(treeFontSize + 6);
        Object  treeExpandedIcon		= PlasticIconFactory.getExpandedTreeIcon();
        Object  treeCollapsedIcon		= PlasticIconFactory.getCollapsedTreeIcon();
        ColorUIResource gray 			= new ColorUIResource(Color.GRAY);

		Boolean is3D					= Boolean.valueOf(is3DEnabled());

        Character  passwordEchoChar     = new Character(LookUtils.IS_OS_WINDOWS ? '\u25CF' : '\u2022');

        String iconPrefix = "icons/" + (LookUtils.IS_LOW_RESOLUTION ? "32x32/" : "48x48/");
        Object errorIcon       = makeIcon(getClass(), iconPrefix + "dialog-error.png");
        Object informationIcon = makeIcon(getClass(), iconPrefix + "dialog-information.png");
        Object questionIcon    = makeIcon(getClass(), iconPrefix + "dialog-question.png");
        Object warningIcon     = makeIcon(getClass(), iconPrefix + "dialog-warning.png");


		Object[] defaults = {
		"Button.border",								buttonBorder,
		"Button.margin",								buttonMargin,

		"CheckBox.margin", 								checkBoxMargin,

		// Use a modified check
		"CheckBox.icon", 								checkBoxIcon,

		"CheckBoxMenuItem.border",						menuItemBorder,
		"CheckBoxMenuItem.margin",						menuItemMargin,			// 1.4.1 Bug
		"CheckBoxMenuItem.checkIcon",					checkBoxMenuItemIcon,
        "CheckBoxMenuItem.background", 					getMenuItemBackground(),// Added by JGoodies
		"CheckBoxMenuItem.selectionForeground",			getMenuItemSelectedForeground(),
		"CheckBoxMenuItem.selectionBackground",			getMenuItemSelectedBackground(),
		"CheckBoxMenuItem.acceleratorForeground",		menuItemForeground,
		"CheckBoxMenuItem.acceleratorSelectionForeground",getMenuItemSelectedForeground(),
		"CheckBoxMenuItem.acceleratorSelectionBackground",getMenuItemSelectedBackground(),

		// ComboBox uses menu item selection colors
		"ComboBox.selectionForeground",					getMenuSelectedForeground(),
		"ComboBox.selectionBackground",					getMenuSelectedBackground(),
        "ComboBox.arrowButtonBorder",                   comboBoxButtonBorder,
        "ComboBox.editorBorder",                        comboBoxEditorBorder,
        "ComboBox.editorColumns",                       new Integer(5),
        "ComboBox.editorBorderInsets",                  comboEditorBorderInsets,          // Added by JGoodies
        "ComboBox.editorInsets",                        textInsets,          // Added by JGoodies
        "ComboBox.tableEditorInsets",                   comboTableEditorInsets,
        "ComboBox.rendererBorder",                      comboRendererBorder, // Added by JGoodies

        "EditorPane.margin",                            wrappedTextInsets,

        "InternalFrame.border", 						internalFrameBorder,
        "InternalFrame.paletteBorder", 					paletteBorder,

		"List.font",									getControlTextFont(),
		"Menu.border",									PlasticBorders.getMenuBorder(),
		"Menu.margin",									menuMargin,
		"Menu.arrowIcon",								PlasticIconFactory.getMenuArrowIcon(),

		"MenuBar.emptyBorder",							menuBarEmptyBorder,		// Added by JGoodies
		"MenuBar.separatorBorder",						menuBarSeparatorBorder,	// Added by JGoodies
		"MenuBar.etchedBorder",							menuBarEtchedBorder,	// Added by JGoodies
		"MenuBar.headerBorder",							menuBarHeaderBorder,	// Added by JGoodies

		"MenuItem.border",								menuItemBorder,
		"MenuItem.checkIcon",	 						menuItemCheckIcon,		// Aligns menu items
		"MenuItem.margin",								menuItemMargin,			// 1.4.1 Bug
        "MenuItem.background", 							getMenuItemBackground(),// Added by JGoodies
		"MenuItem.selectionForeground",					getMenuItemSelectedForeground(),// Added by JGoodies
		"MenuItem.selectionBackground",					getMenuItemSelectedBackground(),// Added by JGoodies
		"MenuItem.acceleratorForeground",				menuItemForeground,
		"MenuItem.acceleratorSelectionForeground",		getMenuItemSelectedForeground(),
		"MenuItem.acceleratorSelectionBackground",		getMenuItemSelectedBackground(),

		"OptionPane.errorIcon",							errorIcon,
        "OptionPane.informationIcon",                   informationIcon,
        "OptionPane.questionIcon",                      questionIcon,
        "OptionPane.warningIcon",                       warningIcon,

        //"DesktopIcon.icon", 							makeIcon(superclass, "icons/DesktopIcon.gif"),
		"FileView.computerIcon",						makeIcon(getClass(), "icons/Computer.gif"),
		"FileView.directoryIcon",						makeIcon(getClass(), "icons/TreeClosed.gif"),
		"FileView.fileIcon", 							makeIcon(getClass(), "icons/File.gif"),
		"FileView.floppyDriveIcon", 					makeIcon(getClass(), "icons/FloppyDrive.gif"),
		"FileView.hardDriveIcon", 						makeIcon(getClass(), "icons/HardDrive.gif"),
		"FileChooser.homeFolderIcon", 					makeIcon(getClass(), "icons/HomeFolder.gif"),
        "FileChooser.newFolderIcon", 					makeIcon(getClass(), "icons/NewFolder.gif"),
        "FileChooser.upFolderIcon",						makeIcon(getClass(), "icons/UpFolder.gif"),
		"Tree.closedIcon", 								makeIcon(getClass(), "icons/TreeClosed.gif"),
	  	"Tree.openIcon", 								makeIcon(getClass(), "icons/TreeOpen.gif"),
	  	"Tree.leafIcon", 								makeIcon(getClass(), "icons/TreeLeaf.gif"),

        "FormattedTextField.border",                    textFieldBorder,
        "FormattedTextField.margin",                    textInsets,

		"PasswordField.border",							textFieldBorder,
        "PasswordField.margin",                         textInsets,
        "PasswordField.echoChar",                       passwordEchoChar,


		"PopupMenu.border",								PlasticBorders.getPopupMenuBorder(),
        "PopupMenu.noMarginBorder",                     PlasticBorders.getNoMarginPopupMenuBorder(),
		"PopupMenuSeparator.margin",					new InsetsUIResource(3, 4, 3, 4),

		"RadioButton.margin",							checkBoxMargin,
		"RadioButtonMenuItem.border",					menuItemBorder,
		"RadioButtonMenuItem.checkIcon",				radioButtonMenuItemIcon,
		"RadioButtonMenuItem.margin",					menuItemMargin,			// 1.4.1 Bug
        "RadioButtonMenuItem.background", 				getMenuItemBackground(),// Added by JGoodies
		"RadioButtonMenuItem.selectionForeground",		getMenuItemSelectedForeground(),
		"RadioButtonMenuItem.selectionBackground",		getMenuItemSelectedBackground(),
		"RadioButtonMenuItem.acceleratorForeground",	menuItemForeground,
		"RadioButtonMenuItem.acceleratorSelectionForeground",	getMenuItemSelectedForeground(),
		"RadioButtonMenuItem.acceleratorSelectionBackground",	getMenuItemSelectedBackground(),
		"Separator.foreground",							getControlDarkShadow(),
		"ScrollPane.border",							scrollPaneBorder,
		"ScrollPane.etchedBorder",   					scrollPaneBorder,
//			"ScrollPane.background",					table.get("window"),

		"SimpleInternalFrame.activeTitleForeground",	getSimpleInternalFrameForeground(),
		"SimpleInternalFrame.activeTitleBackground",	getSimpleInternalFrameBackground(),

	    "Spinner.border", 								PlasticBorders.getFlush3DBorder(),
	    "Spinner.defaultEditorInsets",				    textInsets,

		"SplitPane.dividerSize",						new Integer(7),
		"TabbedPane.focus",								getFocusColor(),
		"TabbedPane.tabInsets",							new InsetsUIResource(1, 9, 1, 8),
		"Table.foreground",								table.get("textText"),
		"Table.gridColor",								controlColor, //new ColorUIResource(new Color(216, 216, 216)),
        "Table.scrollPaneBorder", 						scrollPaneBorder,
		"TableHeader.cellBorder",						tableHeaderBorder,
        "TextArea.inactiveBackground",                  inactiveTextBackground,
		"TextArea.margin",								wrappedTextInsets,
		"TextField.border",								textFieldBorder,
		"TextField.margin", 							textInsets,
		"TitledBorder.font",							getTitleTextFont(),
		"TitledBorder.titleColor",						getTitleTextColor(),
		"ToggleButton.border",							toggleButtonBorder,
		"ToggleButton.margin",							buttonMargin,

		"ToolBar.emptyBorder", 							toolBarEmptyBorder,		// Added by JGoodies
		"ToolBar.separatorBorder", 						toolBarSeparatorBorder,	// Added by JGoodies
		"ToolBar.etchedBorder", 						toolBarEtchedBorder,	// Added by JGoodies
		"ToolBar.headerBorder", 						toolBarHeaderBorder,	// Added by JGoodies

		"ToolTip.hideAccelerator",						Boolean.TRUE,

        "Tree.expandedIcon", 							treeExpandedIcon,
        "Tree.collapsedIcon", 							treeCollapsedIcon,
        "Tree.line",									gray,
        "Tree.hash",									gray,
		"Tree.rowHeight",								rowHeight,

		"Button.is3DEnabled",							is3D,
		"ComboBox.is3DEnabled",							is3D,
		"MenuBar.is3DEnabled",							is3D,
		"ToolBar.is3DEnabled",							is3D,
		"ScrollBar.is3DEnabled",						is3D,
		"ToggleButton.is3DEnabled",						is3D,

        // 1.4.1 uses a 2 pixel non-standard border, that leads to bad
        // alignment in the typical case that the border is not painted
        "CheckBox.border",                      marginBorder,
        "RadioButton.border",                   marginBorder,

        // Fix of the issue #21
        "ProgressBar.selectionForeground",      getSystemTextColor(),
        "ProgressBar.selectionBackground",      getSystemTextColor()
		};
		table.putDefaults(defaults);

        // Set paths to sounds for auditory feedback
        String soundPathPrefix = "/javax/swing/plaf/metal/";
        Object[] auditoryCues = (Object[]) table.get("AuditoryCues.allAuditoryCues");
        if (auditoryCues != null) {
            Object[] audioDefaults = new String[auditoryCues.length * 2];
            for (int i = 0; i < auditoryCues.length; i++) {
                Object auditoryCue = auditoryCues[i];
                audioDefaults[2*i]     = auditoryCue;
                audioDefaults[2*i + 1] = soundPathPrefix + table.getString(auditoryCue);
            }
            table.putDefaults(audioDefaults);
        }
	}


	/**
	 * Unlike my superclass I register a unified shadow color.
	 * This color is used by my ThinBevelBorder class.
     *
     * @param table   the UIDefaults table to work with
	 */
	protected void initSystemColorDefaults(UIDefaults table) {
		super.initSystemColorDefaults(table);
		table.put("unifiedControlShadow", table.getColor("controlDkShadow"));
		table.put("primaryControlHighlight", getPrimaryControlHighlight());
	}


	// Color Theme Behavior *************************************************************

	private static final String THEME_CLASSNAME_PREFIX = "com.jgoodies.looks.plastic.theme.";

	/**
	 * Creates and returns the default color theme. Honors the current platform
     * and platform flavor - if available.
     *
     * @return the default color theme for the current environment
	 */
	public static PlasticTheme createMyDefaultTheme() {
		String defaultName;
        if (LookUtils.IS_LAF_WINDOWS_XP_ENABLED) {
            defaultName = getDefaultXPTheme();
        } else if (LookUtils.IS_OS_WINDOWS_MODERN) {
            defaultName = "DesertBluer";
        } else {
            defaultName = "SkyBlue";
        }
		// Don't use the default now, so we can detect that the users tried to set one.
		String userName  = LookUtils.getSystemProperty(DEFAULT_THEME_KEY, "");
		boolean overridden = userName.length() > 0;
		String themeName = overridden ? userName : defaultName;
		PlasticTheme theme = createTheme(themeName);
		PlasticTheme result = theme != null ? theme : new SkyBluer();

		// In case the user tried to set a theme, log a message.
		if (overridden) {
			String className = result.getClass().getName().substring(
													THEME_CLASSNAME_PREFIX.length());
			if (className.equals(userName)) {
				LookUtils.log("I have successfully installed the '" + result.getName() + "' theme.");
			} else {
				LookUtils.log("I could not install the Plastic theme '" + userName + "'.");
				LookUtils.log("I have installed the '" + result.getName() + "' theme, instead.");
			}
		}
		return result;
	}


    private static String getDefaultXPTheme() {
        String fallbackName = "ExperienceBlue";
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        String xpstyleDll = (String) toolkit.getDesktopProperty("win.xpstyle.dllName");
        if (xpstyleDll == null) {
            return fallbackName;
        }
        boolean isStyleLuna = xpstyleDll.endsWith("luna.msstyles");
        boolean isStyleRoyale = xpstyleDll.endsWith("Royale.msstyles");
        boolean isStyleAero = xpstyleDll.endsWith("Aero.msstyles");
        if (isStyleRoyale) {
            return "ExperienceRoyale";
        }
        if (isStyleLuna) {
            String xpstyleColorName = (String) toolkit.getDesktopProperty("win.xpstyle.colorName");
            if (xpstyleColorName == null) {
                return fallbackName;
            }
            if (xpstyleColorName.equalsIgnoreCase("HomeStead")) {
                return "ExperienceGreen";
            } else if (xpstyleColorName.equalsIgnoreCase("Metallic")) {
                return "Silver";
            } else {
                return fallbackName;
            }
        }
        if (isStyleAero) {
            return "LightGray";
        }
        return fallbackName;
    }


	/**
	 * Lazily initializes and returns the <code>List</code> of installed
     * color themes.
     *
     * @return a list of installed color/font themes
	 */
	public static List getInstalledThemes() {
		if (null == installedThemes)
			installDefaultThemes();

		Collections.sort(installedThemes, new Comparator() {
			public int compare(Object o1, Object o2) {
				MetalTheme theme1 = (MetalTheme) o1;
				MetalTheme theme2 = (MetalTheme) o2;
				return theme1.getName().compareTo(theme2.getName());
			}
		});

		return installedThemes;
	}


	/**
	 * Install the default color themes.
	 */
	protected static void installDefaultThemes() {
		installedThemes = new ArrayList();
		String[] themeNames = {
		    "BrownSugar",
		    "DarkStar",
			"DesertBlue",
		    "DesertBluer",
		    "DesertGreen",
		    "DesertRed",
		    "DesertYellow",
			"ExperienceBlue",
			"ExperienceGreen",
            "ExperienceRoyale",
            "LightGray",
			"Silver",
		    "SkyBlue",
		    "SkyBluer",
		    "SkyGreen",
		    "SkyKrupp",
		    "SkyPink",
		    "SkyRed",
		    "SkyYellow"};
		for (int i = themeNames.length - 1; i >= 0; i--)
			installTheme(createTheme(themeNames[i]));
	}


	/**
	 * Creates and returns a color theme from the specified theme name.
     *
     * @param themeName   the unqualified name of the theme to create
     * @return the associated color theme or <code>null</code> in case of
     *     a problem
	 */
	protected static PlasticTheme createTheme(String themeName) {
	    String className = THEME_CLASSNAME_PREFIX + themeName;
	    try {
		    Class cl = Class.forName(className);
            return (PlasticTheme) (cl.newInstance());
        } catch (ClassNotFoundException e) {
            // Ignore the exception here and log below.
        } catch (IllegalAccessException e) {
            // Ignore the exception here and log below.
	    } catch (InstantiationException e) {
            // Ignore the exception here and log below.
	    }
	    LookUtils.log("Can't create theme " + className);
	    return null;
	}


	/**
	 * Installs a color theme.
     *
     * @param theme    the theme to install
	 */
	public static void installTheme(PlasticTheme theme) {
		if (null == installedThemes)
			installDefaultThemes();
		installedThemes.add(theme);
	}


    /**
     * Lazily initializes and returns the PlasticTheme.
     * In Java 5 or later, this method looks up the theme
     * using <code>MetalLookAndFeel#getCurrentTheme</code>.
     * In Java 1.4 it is requested from the UIManager.
     * Both access methods use an AppContext to store the theme,
     * so that applets in different contexts don't share the theme.
     *
     * @return the current PlasticTheme
     */
    public static PlasticTheme getPlasticTheme() {
        if (LookUtils.IS_JAVA_5_OR_LATER) {
            MetalTheme theme = getCurrentTheme0();
            if (theme instanceof PlasticTheme)
                return (PlasticTheme) theme;
        }
        PlasticTheme uimanagerTheme = (PlasticTheme) UIManager.get(THEME_KEY);
        if (uimanagerTheme != null)
            return uimanagerTheme;

        PlasticTheme initialTheme = createMyDefaultTheme();
        setPlasticTheme(initialTheme);
        return initialTheme;
    }


    /**
     * Sets the theme for colors and fonts used by the Plastic L&amp;F.<p>
     *
     * After setting the theme, you need to re-install the Look&amp;Feel,
     * as well as update the UI's of any previously created components
     * - just as if you'd change the Look&amp;Feel.
     *
     * @param theme    the PlasticTheme to be set
     *
     * @throws NullPointerException   if the theme is null.
     *
     * @see #getPlasticTheme()
     */
    public static void setPlasticTheme(PlasticTheme theme) {
        if (theme == null)
            throw new NullPointerException("The theme must not be null.");

        UIManager.put(THEME_KEY, theme);
        // Also set the theme in the superclass.
        setCurrentTheme(theme);
    }


	// Accessed by ProxyLazyValues ******************************************

	public static BorderUIResource getInternalFrameBorder() {
		return new BorderUIResource(PlasticBorders.getInternalFrameBorder());
	}

	public static BorderUIResource getPaletteBorder() {
		return new BorderUIResource(PlasticBorders.getPaletteBorder());
	}



	// Accessing Theme Colors and Fonts *************************************


	public static ColorUIResource getPrimaryControlDarkShadow() {
		return getPlasticTheme().getPrimaryControlDarkShadow();
	}

	public static ColorUIResource getPrimaryControlHighlight() {
		return getPlasticTheme().getPrimaryControlHighlight();
	}

	public static ColorUIResource getPrimaryControlInfo() {
		return getPlasticTheme().getPrimaryControlInfo();
	}

	public static ColorUIResource getPrimaryControlShadow() {
		return getPlasticTheme().getPrimaryControlShadow();
	}

	public static ColorUIResource getPrimaryControl() {
		return getPlasticTheme().getPrimaryControl();
	}

	public static ColorUIResource getControlHighlight() {
		return getPlasticTheme().getControlHighlight();
	}

	public static ColorUIResource getControlDarkShadow() {
		return getPlasticTheme().getControlDarkShadow();
	}

	public static ColorUIResource getControl() {
		return getPlasticTheme().getControl();
	}

	public static ColorUIResource getFocusColor() {
		return getPlasticTheme().getFocusColor();
	}

	public static ColorUIResource getMenuItemBackground() {
		return getPlasticTheme().getMenuItemBackground();
	}

	public static ColorUIResource getMenuItemSelectedBackground() {
		return getPlasticTheme().getMenuItemSelectedBackground();
	}

	public static ColorUIResource getMenuItemSelectedForeground() {
		return getPlasticTheme().getMenuItemSelectedForeground();
	}

	public static ColorUIResource getWindowTitleBackground() {
		return getPlasticTheme().getWindowTitleBackground();
	}

	public static ColorUIResource getWindowTitleForeground() {
		return getPlasticTheme().getWindowTitleForeground();
	}

	public static ColorUIResource getWindowTitleInactiveBackground() {
		return getPlasticTheme().getWindowTitleInactiveBackground();
	}

	public static ColorUIResource getWindowTitleInactiveForeground() {
		return getPlasticTheme().getWindowTitleInactiveForeground();
	}

	public static ColorUIResource getSimpleInternalFrameForeground() {
		return getPlasticTheme().getSimpleInternalFrameForeground();
	}

	public static ColorUIResource getSimpleInternalFrameBackground() {
		return getPlasticTheme().getSimpleInternalFrameBackground();
	}

	public static ColorUIResource getTitleTextColor() {
		return getPlasticTheme().getTitleTextColor();
	}

	public static FontUIResource getTitleTextFont() {
		return getPlasticTheme().getTitleTextFont();
	}


    // Helper Code ************************************************************

    private static MetalTheme getCurrentTheme0() {
        if (getCurrentThemeMethod != null) {
            try {
                return (MetalTheme) getCurrentThemeMethod.invoke(null, null);
            } catch (IllegalArgumentException e) {
                // Return null
            } catch (IllegalAccessException e) {
                // Return null
            } catch (InvocationTargetException e) {
                // Return null
            }
        }
        return null;
    }


    private static Method getMethodGetCurrentTheme() {
        try {
            Class clazz = MetalLookAndFeel.class;
            return clazz.getMethod("getCurrentTheme", new Class[] {});
        } catch (SecurityException e) {
            // returns null
        } catch (NoSuchMethodException e) {
            // returns null
        }
        return null;
    }


    // Helper Code ************************************************************

    /**
     * Appends the key and value to the given source array and returns
     * a copy that has the two new elements at its end.
     *
     * @return an array with the key and value appended
     */
    private static Object[] append(Object[] source, String key, Object value) {
        int length = source.length;
        Object[] destination = new Object[length + 2];
        System.arraycopy(source, 0, destination, 0, length);
        destination[length] = key;
        destination[length + 1] = value;
        return destination;
    }


}