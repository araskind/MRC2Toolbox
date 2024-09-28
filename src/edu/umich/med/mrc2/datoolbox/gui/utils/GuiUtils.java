/*******************************************************************************
 *
 * (C) Copyright 2018-2020 MRC2 (http://mrc2.umich.edu).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 * Alexander Raskind (araskind@med.umich.edu)
 *
 ******************************************************************************/

package edu.umich.med.mrc2.datoolbox.gui.utils;

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import edu.umich.med.mrc2.datoolbox.data.enums.DocumentFormat;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class GuiUtils {

	/**
	 * Add a new button to a given component
	 *
	 * @param component Component to add the button to
	 * @param text Button's text or null
	 * @param icon Button's icon or null
	 * @param listener Button's ActionListener or null
	 * @return Created button
	 */
	public static JButton addButton(Container component, String text, Icon icon, ActionListener listener) {

		return addButton(component, text, icon, listener, null, 0, null);
	}

	/**
	 * Add a new button to a given component
	 *
	 * @param component Component to add the button to
	 * @param text Button's text or null
	 * @param icon Button's icon or null
	 * @param listener Button's ActionListener or null
	 * @param actionCommand Button's action command or null
	 * @return Created button
	 */
	public static JButton addButton(Container component, String text, Icon icon, ActionListener listener,
			String actionCommand) {

		return addButton(component, text, icon, listener, actionCommand, 0, null);
	}

	/**
	 * Add a new button to a given component
	 *
	 * @param component Component to add the button to
	 * @param text Button's text or null
	 * @param icon Button's icon or null
	 * @param listener Button's ActionListener or null
	 * @param actionCommand Button's action command or null
	 * @param mnemonic Button's mnemonic (virtual key code) or 0
	 * @param toolTip Button's tooltip text or null
	 * @return Created button
	 */
	public static JButton addButton(Container component, String text, Icon icon, ActionListener listener,
			String actionCommand, int mnemonic, String toolTip) {

		JButton button = new JButton(text, icon);
		if (listener != null)
			button.addActionListener(listener);
		if (actionCommand != null)
			button.setActionCommand(actionCommand);
		if (mnemonic > 0)
			button.setMnemonic(mnemonic);
		if (toolTip != null)
			button.setToolTipText(toolTip);
		if (component != null)
			component.add(button);
		return button;
	}

	/**
	 * Add a new button to a given component
	 *
	 * @param component Component to add the button to
	 * @param text Button's text or null
	 * @param icon Button's icon or null
	 * @param listener Button's ActionListener or null
	 * @param actionCommand Button's action command or null
	 * @param toolTip Button's tooltip text or null
	 * @return Created button
	 */
	public static JButton addButton(Container component, String text, Icon icon, ActionListener listener,
			String actionCommand, String toolTip) {

		return addButton(component, text, icon, listener, actionCommand, 0, toolTip);
	}

	/**
	 * Add a new button to a given component
	 *
	 * @param component Component to add the button to
	 * @param text Button's text or null
	 * @param icon Button's icon or null
	 * @param listener Button's ActionListener or null
	 * @param actionCommand Button's action command or null
	 * @param toolTip Button's tooltip text or null
	 * @param buttonDimension Button's dimension
	 * @return Created button
	 */
	public static JButton addButton(Container component, String text, Icon icon, ActionListener listener,
			String actionCommand, String toolTip, Dimension buttonDimension) {

		JButton button = new JButton(text, icon);

		if (listener != null)
			button.addActionListener(listener);
		if (actionCommand != null)
			button.setActionCommand(actionCommand);
		if (toolTip != null)
			button.setToolTipText(toolTip);
		if (component != null)
			component.add(button);

		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		button.setPreferredSize(buttonDimension);
		button.setSize(buttonDimension);

		return button;
	}

	public static JToggleButton addToggleButton(
			Container component,
			Icon icon,
			ActionListener listener,
			String toolTip,
			Dimension buttonDimension) {

		JToggleButton button = new JToggleButton(icon);

		if (listener != null)
			button.addActionListener(listener);
		if (toolTip != null)
			button.setToolTipText(toolTip);

		if (component != null)
			component.add(button);

		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		button.setPreferredSize(buttonDimension);
		button.setSize(buttonDimension);

		return button;
	}

	public static JButton addButton(
			Container component,
			Icon icon,
			ActionListener listener,
			String toolTip,
			Dimension buttonDimension) {

		JButton button = new JButton(icon);

		if (listener != null)
			button.addActionListener(listener);
		if (toolTip != null)
			button.setToolTipText(toolTip);

		if (component != null)
			component.add(button);

		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		button.setPreferredSize(buttonDimension);
		button.setSize(buttonDimension);

		return button;
	}

	public static JButton addButton(
			Container component,
			Icon icon,
			Action action,
			String toolTip,
			Dimension buttonDimension) {

		JButton button = new JButton(icon);

		if (action != null) {
			button.setAction(action);
			button.setText(null);
		}
		if (toolTip != null)
			button.setToolTipText(toolTip);
		if (component != null)
			component.add(button);

		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		button.setPreferredSize(buttonDimension);
		button.setSize(buttonDimension);

		return button;
	}

	/**
	 * Add a new button to a JPanel and then add the panel to a given component
	 *
	 * @param component Component to add the button to
	 * @param text Button's text or null
	 * @param icon Button's icon or null
	 * @param listener Button's ActionListener or null
	 * @return Created button
	 */
	public static JButton addButtonInPanel(Container component, String text, ActionListener listener) {

		return addButtonInPanel(component, text, listener, null);
	}

	/**
	 * Add a new button to a JPanel and then add the panel to a given component
	 *
	 * @param component Component to add the button to
	 * @param text Button's text or null
	 * @param icon Button's icon or null
	 * @param listener Button's ActionListener or null
	 * @param actionCommand Button's action command or null
	 * @return Created button
	 */
	public static JButton addButtonInPanel(Container component, String text, ActionListener listener,
			String actionCommand) {

		JPanel panel = new JPanel();
		JButton button = new JButton(text);
		if (listener != null)
			button.addActionListener(listener);
		if (actionCommand != null)
			button.setActionCommand(actionCommand);
		panel.add(button);
		if (component != null)
			component.add(panel);
		return button;
	}

	/**
	 * Add a new label to a given component
	 *
	 * @param component Component to add the label to
	 * @param text Label's text
	 * @return Created label
	 */
	public static JLabel addLabel(Container component, String text) {

		return addLabel(component, text, null, JLabel.LEFT, null);
	}

	/**
	 * Add a new label to a given component
	 *
	 * @param component Component to add the label to
	 * @param text Label's text
	 * @param icon Label's icon
	 * @param horizontalAlignment Label's horizontal alignment (e.g. JLabel.LEFT)
	 * @param font Label's font
	 * @return Created label
	 */
	public static JLabel addLabel(Container component, String text, Icon icon, int horizontalAlignment, Font font) {

		JLabel label = new JLabel(text, icon, horizontalAlignment);
		if (component != null)
			component.add(label);
		if (font != null)
			label.setFont(font);
		return label;
	}

	/**
	 * Add a new label to a given component
	 *
	 * @param component Component to add the label to
	 * @param text Label's text
	 * @param horizontalAlignment Label's horizontal alignment (e.g. JLabel.LEFT)
	 * @return Created label
	 */
	public static JLabel addLabel(Container component, String text, int horizontalAlignment) {

		return addLabel(component, text, null, horizontalAlignment, null);
	}

	/**
	 * Add a new label to a given component
	 *
	 * @param component Component to add the label to
	 * @param text Label's text
	 * @param horizontalAlignment Label's horizontal alignment (e.g. JLabel.LEFT)
	 * @param font Label's font
	 * @return Created label
	 */
	public static JLabel addLabel(Container component, String text, int horizontalAlignment, Font font) {

		return addLabel(component, text, null, horizontalAlignment, font);
	}

	/**
	 * Add a new label to a JPanel and then add the panel to a given component
	 *
	 * @param component Component to add the label to
	 * @param text Label's text
	 * @return Created label
	 */
	public static JLabel addLabelInPanel(Container component, String text) {

		JPanel panel = new JPanel();
		component.add(panel);
		return addLabel(panel, text);
	}

	/**
	 * Add a margin to a given component
	 *
	 * @param component Component to add the margin to
	 * @param margin Margin size
	 * @return Created border
	 */
	public static Border addMargin(JComponent component, int margin) {

		Border marginBorder = BorderFactory.createEmptyBorder(margin, margin, margin, margin);
		component.setBorder(marginBorder);
		return marginBorder;
	}

	/**
	 * Add a margin and border to a given component
	 *
	 * @param component Component to add the margin to
	 * @param margin Margin size
	 * @return Created border
	 */
	public static Border addMarginAndBorder(JComponent component, int margin) {

		Border marginBorder = BorderFactory.createEmptyBorder(margin, margin, margin, margin);
		Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
		Border compoundBorder = BorderFactory.createCompoundBorder(etchedBorder, marginBorder);
		component.setBorder(compoundBorder);
		return compoundBorder;
	}

	/**
	 * Add a new menu item to a given menu
	 *
	 * @param menu Menu to add the item to
	 * @param text Menu item text
	 * @param listener Menu item's ActionListener or null
	 * @return Created menu item
	 */
	public static JMenuItem addMenuItem(Container menu, String text, ActionListener listener) {

		return addMenuItem(menu, text, listener, null, 0, false, null);
	}

	/**
	 * Add a new menu item to a given menu
	 *
	 * @param menu Menu to add the item to
	 * @param text Menu item text
	 * @param listener Menu item's ActionListener or null
	 * @param mnemonic Menu item's mnemonic (virtual key code) or 0
	 * @return Created menu item
	 */
	public static JMenuItem addMenuItem(Container menu, String text, ActionListener listener, int mnemonic) {

		return addMenuItem(menu, text, listener, null, mnemonic, false, null);
	}

	/**
	 * Add a new menu item to a given menu
	 *
	 * @param menu Menu to add the item to
	 * @param text Menu item text
	 * @param listener Menu item's ActionListener or null
	 * @param mnemonic Menu item's mnemonic (virtual key code) or 0
	 * @param setAccelerator Indicates whether to set key accelerator to CTRL + the key specified by mnemonic parameter
	 * @return Created menu item
	 */
	public static JMenuItem addMenuItem(Container menu, String text, ActionListener listener, int mnemonic,
			boolean setAccelerator) {

		return addMenuItem(menu, text, listener, null, mnemonic, setAccelerator, null);
	}

	/**
	 * Add a new menu item to a given menu
	 *
	 * @param menu Menu to add the item to
	 * @param text Menu item text
	 * @param listener Menu item's ActionListener or null
	 * @param actionCommand Menu item's action command or null
	 * @return Created menu item
	 */
	public static JMenuItem addMenuItem(Container menu, String text, ActionListener listener, String actionCommand) {
		return addMenuItem(menu, text, listener, actionCommand, 0, false, null);
	}

	/**
	 * Add a new menu item to a given menu
	 *
	 * @param menu Menu to add the item to
	 * @param text Menu item text
	 * @param listener Menu item's ActionListener or null
	 * @param actionCommand Menu item's action command or null
	 * @param icon Menu icon
	 * @return Created menu item
	 */
	public static JMenuItem addMenuItem(Container menu, String text, ActionListener listener, String actionCommand,
			Icon icon) {

		return addMenuItem(menu, text, listener, actionCommand, 0, false, icon);
	}

	/**
	 * Add a new menu item to a given menu
	 *
	 * @param menu Menu to add the item to
	 * @param text Menu item text
	 * @param listener Menu item's ActionListener or null
	 * @param actionCommand Menu item's action command or null
	 * @param mnemonic Menu item's mnemonic (virtual key code) or 0
	 * @param setAccelerator Indicates whether to set key accelerator to CTRL + the key specified by mnemonic parameter
	 * @param icon Menu icon
	 * @return Created menu item
	 */
	public static JMenuItem addMenuItem(Container menu, String text, ActionListener listener, String actionCommand,
			int mnemonic, boolean setAccelerator, Icon icon) {

		JMenuItem item = new JMenuItem(text);
		if (listener != null)
			item.addActionListener(listener);
		if (actionCommand != null)
			item.setActionCommand(actionCommand);
		if (mnemonic > 0)
			item.setMnemonic(mnemonic);
		if (setAccelerator)
			item.setAccelerator(KeyStroke.getKeyStroke(mnemonic, ActionEvent.CTRL_MASK));
		if (icon != null)
			item.setIcon(icon);

		if (menu != null)
			menu.add(item);
		return item;
	}

	/**
	 * Add a separator to a given component
	 *
	 * @param component Component to add the separator to
	 * @return Created separator
	 */
	public static JSeparator addSeparator(Container component) {

		return addSeparator(component, 0);
	}

	/**
	 * Add a separator to a given component
	 *
	 * @param component Component to add the separator to
	 * @param margin Margin around the separator
	 * @return Created separator
	 */
	public static JSeparator addSeparator(Container component, int margin) {

		JSeparator separator = new JSeparator();
		if (margin > 0)
			addMargin(separator, margin);
		if (component != null)
			component.add(separator);
		return separator;
	}

	public static Icon getIconOld(String iconName, int size) {

		return new ImageIcon(MRC2ToolBoxCore.iconDir +
				Integer.toString(size) +
				File.separator + iconName + ".png");
	}

	public static Icon getIcon(String iconName, int size) {

		ImageIcon original = new ImageIcon(MRC2ToolBoxCore.iconDir + iconName + ".png");
		Image newImage = original.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
		return new ImageIcon(newImage);
	}
	
	public static Icon getLoaderIcon(String iconName, int size) {
		
		if(size != 64 && size != 128 && size != 256)
			return null;

		File loaderFile = Paths.get(MRC2ToolBoxCore.iconDir, "loader", iconName + "_" + Integer.toString(size)+ ".gif").toFile();
		if(!loaderFile.exists())
			return null;
		
		URL img = null;
		try {
			img = loaderFile.toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(img == null)
			return null;
		
		ImageIcon original = new ImageIcon(img);
		Image newImage = original.getImage().getScaledInstance(size, size, Image.SCALE_REPLICATE);
		return new ImageIcon(newImage);
	}


	public static Icon getDocumentFormatIcon(DocumentFormat docFormat, int size) {
		
		String iconName = "document";
		if(docFormat != null) {
			
			if(docFormat.equals(DocumentFormat.UNK))
				iconName = "document";
			else
				iconName = docFormat.name().toLowerCase();
		}		
		ImageIcon original = new ImageIcon(MRC2ToolBoxCore.fleTypeIconDir + iconName + ".png");
		Image newImage = original.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
		return new ImageIcon(newImage);
	}

	public static Cursor getBusyCursor() {

		try {
			BufferedImage cursorImage = ImageIO
					.read(new File(MRC2ToolBoxCore.iconDir + "24" + File.separator + "clock.png"));

			for (int i = 0; i < cursorImage.getHeight(); i++) {

				int[] rgb = cursorImage.getRGB(0, i, cursorImage.getWidth(), 1, null, 0, cursorImage.getWidth() * 4);

				for (int j = 0; j < rgb.length; j++) {

					int alpha = (rgb[j] >> 24) & 255;
					if (alpha < 128)
						alpha = 0;
					else
						alpha = 255;

					rgb[j] &= 0x00ffffff;
					rgb[j] = (alpha << 24) | rgb[j];
				}
				cursorImage.setRGB(0, i, cursorImage.getWidth(), 1, rgb, 0, cursorImage.getWidth() * 4);
			}
			return Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(0, 0), "Busy cursor");
		}
		catch (Exception exp) {
			exp.printStackTrace();
		}
        return null;
	}
	
	public static SimpleButtonAction setupButtonAction(
			final String text, 
			final String command, 
			final Icon icon, 
			ActionListener listener) {

		SimpleButtonAction button = new SimpleButtonAction();
		button.setText(text);
		button.setTooltip(text);
		button.setIcon(icon);
		button.setCommand(command);
		button.addActionListener(listener);
		return button;
	}
}
