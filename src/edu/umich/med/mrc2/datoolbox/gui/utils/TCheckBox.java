/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.UIManager;

/**
 * From https://stackoverflow.com/questions/1263323/tristate-checkboxes-in-java
 *
 */
public class TCheckBox extends JCheckBox implements Icon, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2724639036361859811L;

	final static boolean MIDasSELECTED = true; // consider mid-state as selected ?
	final static Icon icon = UIManager.getIcon("CheckBox.icon");

	public TCheckBox() {
		this("");
	}

	public TCheckBox(String text) {
		super(text);
		putClientProperty("SelectionState", 0);
		setIcon(this);
		addActionListener(this);
	}

	public TCheckBox(String text, int sel) {
		/*
		 * tri-state checkbox has 3 selection states: 0 unselected 1 mid-state selection
		 * 2 fully selected
		 */
		super(text, sel > 1 ? true : false);

		switch (sel) {
		case 2:
			setSelected(true);
		case 1:
		case 0:
			putClientProperty("SelectionState", sel);
			break;
		default:
			throw new IllegalArgumentException();
		}
		addActionListener(this);
		setIcon(this);
	}

	@Override
	public boolean isSelected() {
		if (MIDasSELECTED && (getSelectionState() > 0))
			return true;
		else
			return super.isSelected();
	}

	public int getSelectionState() {
		return (getClientProperty("SelectionState") != null ? (int) getClientProperty("SelectionState")
				: super.isSelected() ? 2 : 0);
	}

	public void setSelectionState(int sel) {
		switch (sel) {
		case 2:
			setSelected(true);
			break;
		case 1:
		case 0:
			setSelected(false);
			break;
		default:
			throw new IllegalArgumentException();
		}
		putClientProperty("SelectionState", sel);
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		icon.paintIcon(c, g, x, y);
		if (getSelectionState() != 1)
			return;

		int w = getIconWidth();
		int h = getIconHeight();
		g.setColor(c.isEnabled() ? new Color(51, 51, 51) : new Color(122, 138, 153));
		g.fillRect(x + 4, y + 4, w - 8, h - 8);

		if (!c.isEnabled())
			return;
		g.setColor(new Color(81, 81, 81));
		g.drawRect(x + 4, y + 4, w - 9, h - 9);
	}

	@Override
	public int getIconWidth() {
		return icon.getIconWidth();
	}

	@Override
	public int getIconHeight() {
		return icon.getIconHeight();
	}

	public void actionPerformed(ActionEvent e) {
		TCheckBox tcb = (TCheckBox) e.getSource();
		if (tcb.getSelectionState() == 0)
			tcb.setSelected(false);

		tcb.putClientProperty("SelectionState", tcb.getSelectionState() == 2 ? 0 : tcb.getSelectionState() + 1);
	}
}
