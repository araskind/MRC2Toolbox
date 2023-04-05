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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.search;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataField;

public class PropertySearchPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8875276197364376526L;

	private CpdMetadataField field;
	private JTextField valueField;

	public PropertySearchPanel(CpdMetadataField field) {
		super();
		setBorder(new EmptyBorder(10, 10, 10, 10));
		this.field = field;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel fieldNameLabel = new JLabel(field.getName());
		GridBagConstraints gbc_fieldNameLabel = new GridBagConstraints();
		gbc_fieldNameLabel.insets = new Insets(0, 0, 0, 5);
		gbc_fieldNameLabel.anchor = GridBagConstraints.NORTHEAST;
		gbc_fieldNameLabel.gridx = 0;
		gbc_fieldNameLabel.gridy = 0;
		add(fieldNameLabel, gbc_fieldNameLabel);
		
		valueField = new JTextField();
		GridBagConstraints gbc_valueField = new GridBagConstraints();
		gbc_valueField.anchor = GridBagConstraints.NORTH;
		gbc_valueField.fill = GridBagConstraints.HORIZONTAL;
		gbc_valueField.gridx = 1;
		gbc_valueField.gridy = 0;
		add(valueField, gbc_valueField);
		valueField.setColumns(10);
	}

	public CpdMetadataField getProperty() {
		return field;
	}
	
	public String getPropertyValue() {
		
		String value = valueField.getText().trim();
		if(value.isEmpty())
			return null;
		else
			return value;
	}
	
	public void setPropertyValue(String value) {
		valueField.setText(value);
	}
}
