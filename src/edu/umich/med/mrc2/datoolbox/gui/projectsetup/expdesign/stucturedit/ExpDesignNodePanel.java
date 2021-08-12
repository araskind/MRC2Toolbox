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

package edu.umich.med.mrc2.datoolbox.gui.projectsetup.expdesign.stucturedit;

import java.awt.BorderLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ExpDesignNodePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 743880760771512654L;
	private JLabel label = new JLabel();
	private JCheckBox check = new JCheckBox();

	private Object userObject;

	public ExpDesignNodePanel() {

		check.setMargin(new Insets(0, 0, 0, 0));
		check.setOpaque(false);
		setLayout(new BorderLayout());
		add(check, BorderLayout.WEST);
		add(label, BorderLayout.CENTER);

		setFocusable(false);
		setRequestFocusEnabled(false);
	}

	public JCheckBox getCheck() {
		return check;
	}

	public JLabel getLabel() {
		return label;
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

}
