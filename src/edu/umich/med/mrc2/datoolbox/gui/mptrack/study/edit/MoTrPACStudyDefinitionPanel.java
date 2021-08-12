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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.study.edit;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACStudy;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MotracSubjectType;
import edu.umich.med.mrc2.datoolbox.database.mp.MoTrPACDatabaseCash;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;

public class MoTrPACStudyDefinitionPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5354749969536211708L;
	private JTextField studyCodeTextField;
	private JTextField descriptionTextField;
	private JComboBox subjectTypeComboBox;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MoTrPACStudyDefinitionPanel() {
		super();
		setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		studyCodeTextField = new JTextField();
		GridBagConstraints gbc_studyCodeTextField = new GridBagConstraints();
		gbc_studyCodeTextField.insets = new Insets(0, 0, 5, 5);
		gbc_studyCodeTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_studyCodeTextField.gridx = 0;
		gbc_studyCodeTextField.gridy = 0;
		add(studyCodeTextField, gbc_studyCodeTextField);
		studyCodeTextField.setColumns(10);
		
		SortedComboBoxModel<MotracSubjectType> model = 
				new SortedComboBoxModel<MotracSubjectType>(MoTrPACDatabaseCash.getMotrpacSubjectTypeList());
		subjectTypeComboBox = new JComboBox(model);
		subjectTypeComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_subjectTypeComboBox = new GridBagConstraints();
		gbc_subjectTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_subjectTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_subjectTypeComboBox.gridx = 1;
		gbc_subjectTypeComboBox.gridy = 0;
		add(subjectTypeComboBox, gbc_subjectTypeComboBox);
		
		descriptionTextField = new JTextField();
		GridBagConstraints gbc_descriptionTextField = new GridBagConstraints();
		gbc_descriptionTextField.gridwidth = 2;
		gbc_descriptionTextField.insets = new Insets(0, 0, 0, 5);
		gbc_descriptionTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_descriptionTextField.gridx = 0;
		gbc_descriptionTextField.gridy = 1;
		add(descriptionTextField, gbc_descriptionTextField);
		descriptionTextField.setColumns(10);
	}
	
	public void loadStudy(MoTrPACStudy study) {
		
		if(study == null)
			return;
		
		studyCodeTextField.setText(study.getCode());
		descriptionTextField.setText(study.getDescription());
		subjectTypeComboBox.setSelectedItem(study.getSubjectType());			
	}
	
	public String getStudyCode() {
		return studyCodeTextField.getText().trim();
	}

	public String getStudyDescription() {
		return descriptionTextField.getText().trim();
	}
	
	public MotracSubjectType getMotracSubjectType() {
		return (MotracSubjectType)subjectTypeComboBox.getSelectedItem();
	}
}




