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

package edu.umich.med.mrc2.datoolbox.gui.adducts.bindif;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.umich.med.mrc2.datoolbox.data.BinnerNeutralMassDifference;
import edu.umich.med.mrc2.datoolbox.gui.adducts.adduct.CompositeAdductComponentsTable;

public class BinnerNeutralMassDifferenceSelectorPanel extends JPanel 
		implements ListSelectionListener {	

	/**
	 * 
	 */
	private static final long serialVersionUID = 4917041924341389822L;
	private BinnerNeutralMassDifferenceTable massDifferenceTable;
	private CompositeAdductComponentsTable compositAdductComponentsTable;

	public BinnerNeutralMassDifferenceSelectorPanel() {
		
		super(new BorderLayout(0,0));		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		massDifferenceTable = new BinnerNeutralMassDifferenceTable();
		JScrollPane scrollPane = new JScrollPane(massDifferenceTable);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panel.add(scrollPane, gbc_scrollPane);
				
		compositAdductComponentsTable = new CompositeAdductComponentsTable();
		JScrollPane scrollPane_1 = new JScrollPane(compositAdductComponentsTable);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 1;
		panel.add(scrollPane_1, gbc_scrollPane_1);
	}
	
	public void populateBinnerNeutralMassDifferenceTable(Collection<BinnerNeutralMassDifference> diffList) {
		
		massDifferenceTable.getSelectionModel().removeListSelectionListener(this);
		compositAdductComponentsTable.clearTable();
		massDifferenceTable.setTableModelFromBinnerNeutralMassDifferenceList(diffList);
		massDifferenceTable.getSelectionModel().addListSelectionListener(this);
	}
	
	public BinnerNeutralMassDifference getSelectedMassDifference() {		
		return massDifferenceTable.getSelectedMassDifference();
	}

	public void selectMassDifference(BinnerNeutralMassDifference binnerNeutralMassDifference) {
		massDifferenceTable.selectMassDifference(binnerNeutralMassDifference);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()) {
			
			BinnerNeutralMassDifference massDiff = massDifferenceTable.getSelectedMassDifference();
			compositAdductComponentsTable.setTableModelFromBinnerNeutralMassDifference(massDiff);
		}
	}
}








