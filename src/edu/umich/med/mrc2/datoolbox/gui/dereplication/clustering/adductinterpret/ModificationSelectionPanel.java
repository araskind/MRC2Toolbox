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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering.adductinterpret;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class ModificationSelectionPanel extends JPanel {

	/**
	 *
	 */
	private static final long serialVersionUID = 6513092910427101819L;

	private static final Icon deleteIcon = GuiUtils.getIcon("delete", 16);

	private ModificationType modificationType;

	@SuppressWarnings("rawtypes")
	private JComboBox comboBox;
	private JSpinner spinner;

	private JButton deleteButton;

	public ModificationSelectionPanel(ModificationType modificationType, ActionListener listener) {

		super();
		this.modificationType = modificationType;

		setSize(new Dimension(400, 40));
		setMinimumSize(new Dimension(400, 40));
		setMaximumSize(new Dimension(800, 40));
		setBorder(new TitledBorder(
				UIManager.getBorder("TitledBorder.border"), "New " + modificationType.getName(),
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		this.modificationType = modificationType;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		comboBox = new JComboBox<Adduct>();
		comboBox.setPreferredSize(new Dimension(280, 25));
		comboBox.setMinimumSize(new Dimension(120, 25));
		add(comboBox);

		spinner = new JSpinner();
		spinner.setPreferredSize(new Dimension(50, 26));
		spinner.setMinimumSize(new Dimension(50, 26));
		spinner.setMaximumSize(new Dimension(50, 26));
		spinner.setSize(new Dimension(50, 26));
		spinner.setModel(new SpinnerNumberModel(1, 1, 5, 1));
		add(spinner);

		deleteButton = new JButton("");
		deleteButton.setIcon(deleteIcon);
		deleteButton.setActionCommand(AdductInterpreterDialog.DELETE_MOD_COMMAND);
		deleteButton.addActionListener(listener);
		add(deleteButton);

		populateModificationSelector();
	}

	public int getCount() {

		return (int) spinner.getValue();
	}

	public Adduct getModification() {

		Adduct mod = null;

		if (comboBox.getSelectedIndex() > -1)
			mod = (Adduct) comboBox.getSelectedItem();

		return mod;
	}

	@SuppressWarnings("unchecked")
	private void populateModificationSelector() {

		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment() == null || 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getActiveDataPipeline() == null)
			return;

		ArrayList<Adduct> modeModifications = new ArrayList<Adduct>();
		Polarity pol = MRC2ToolBoxCore.getActiveMetabolomicsExperiment().
				getActiveDataPipeline().getAcquisitionMethod().getPolarity();
		for (Adduct cm : AdductManager.getAdductsForType(modificationType)) {

			if (cm.getPolarity().equals(pol) || cm.getPolarity().equals(Polarity.Neutral))
				modeModifications.add(cm);
		}
		SortedComboBoxModel<Adduct> adductSelectorModel = 
				new SortedComboBoxModel<Adduct>(
				modeModifications.toArray(new Adduct[modeModifications.size()]));
		comboBox.setModel(adductSelectorModel);
		comboBox.setSelectedIndex(-1);	
	}
}
