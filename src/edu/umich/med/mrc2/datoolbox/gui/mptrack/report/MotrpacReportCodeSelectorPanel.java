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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.report;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReportCode;
import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReportCodeBlock;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;

public class MotrpacReportCodeSelectorPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3495448640934872451L;
	private MoTrPACReportCodeBlock codeBlock;
	private JComboBox reportCodeComboBox;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MotrpacReportCodeSelectorPanel(MoTrPACReportCodeBlock codeBlock) {
		
		super();
		setBorder(new EmptyBorder(10, 10, 10, 10));
		this.codeBlock = codeBlock;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel(codeBlock.getBlockId());
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		add(lblNewLabel, gbc_lblNewLabel);
		
		reportCodeComboBox = new JComboBox(
				new SortedComboBoxModel<MoTrPACReportCode>(codeBlock.getBlockCodes()));
		reportCodeComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_reportCodeComboBox = new GridBagConstraints();
		gbc_reportCodeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_reportCodeComboBox.gridx = 1;
		gbc_reportCodeComboBox.gridy = 0;
		add(reportCodeComboBox, gbc_reportCodeComboBox);
	}
	
	public MoTrPACReportCodeBlock getCodeBlock() {
		return codeBlock;
	}
	
	public MoTrPACReportCode getSelectedMotracReportCode() {
		return (MoTrPACReportCode)reportCodeComboBox.getSelectedItem();
	}
	
	public void setMoTrPACReportCode(MoTrPACReportCode code) {
		reportCodeComboBox.setSelectedItem(code);
	}
}
