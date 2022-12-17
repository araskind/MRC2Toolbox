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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.byexp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.communication.DockableParametersPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.se.IDTrackerExperimentListingTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableExperimentsTable extends DockableParametersPanel {

	private static final Icon componentIcon = GuiUtils.getIcon("idExperiment", 16);
	
	private IDTrackerExperimentListingTable experimentsTable;
	private JComboBox polarityComboBox;

	public DockableExperimentsTable(ListSelectionListener parent)  {

		super("DockableExperimentsTable", componentIcon, "IDTracker Experiments", Permissions.MIN_MAX_STACK);
		setCloseable(false);
		
		experimentsTable = new IDTrackerExperimentListingTable();
		experimentsTable.getSelectionModel().addListSelectionListener(parent);
		experimentsTable.getSelectionModel().addListSelectionListener(this);
		add(new JScrollPane(experimentsTable), BorderLayout.CENTER);
		JPanel polaritySelector = createPolaritySelector();
		add(polaritySelector, BorderLayout.SOUTH);
	}
	
	private JPanel createPolaritySelector() {
		
		JPanel polaritySelector = new JPanel();
		polaritySelector.setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		polaritySelector.setLayout(gridBagLayout);
		
		JLabel lblNewLabel = new JLabel("Polarity");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		polaritySelector.add(lblNewLabel, gbc_lblNewLabel);
		
		polarityComboBox = new JComboBox();
		polarityComboBox.setPreferredSize(new Dimension(120, 22));
		polarityComboBox.setMinimumSize(new Dimension(80, 22));
		polarityComboBox.addItemListener(this);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		polaritySelector.add(polarityComboBox, gbc_comboBox);
		
		return polaritySelector;
	}
		
	public void setTableModelFromExperimentList(Collection<LIMSExperiment>experimentList) {		
		experimentsTable.setTableModelFromExperimentList(experimentList);
	}
	
	public Collection<LIMSExperiment>getSelectedExperiments(){
		 return experimentsTable.getSelectedExperiments();
	}
	
	public Collection<LIMSExperiment>getAllExperiments(){
		 return experimentsTable.getAllExperiments();
	}
	
	public void clearSelection() {
		experimentsTable.clearSelection();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		if(!e.getValueIsAdjusting()) {
			
			Collection<Polarity>polarities = new TreeSet<Polarity>();
			for(LIMSExperiment experiment : getSelectedExperiments()) {
				
				Collection<Polarity>ep = IDTDataCash.getPolaritiesForExperiment(experiment);
				if(ep != null && !ep.isEmpty())
					polarities.addAll(ep);
			}
			polarityComboBox.removeItemListener(this);
			polarityComboBox.setModel(
					new DefaultComboBoxModel<Polarity>(
							polarities.toArray(new Polarity[polarities.size()])));
			polarityComboBox.addItemListener(this);
		}			
		super.valueChanged(e);
	}
	
	public Polarity getSelectedPolarity() {
		return (Polarity)polarityComboBox.getSelectedItem();
	}	
	
	public void setSelectedExperiments(Collection<LIMSExperiment>experimentList) {
		experimentsTable.setSelectedExperiments(experimentList);
	}
	
	public IDTrackerExperimentListingTable getTable() {
		return experimentsTable;
	}
	
	public void allowMultipSeSelection(boolean allow) {
		
		if(allow) {
			experimentsTable.getSelectionModel().setSelectionMode(
					ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);			
		}
		else {
			experimentsTable.getSelectionModel().setSelectionMode(
					ListSelectionModel.SINGLE_SELECTION);	
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<String> validateInput() {

		Collection<String>errors = new ArrayList<String>();
		
		return errors;
	}

	@Override
	public void resetPanel(Preferences preferences) {
		// TODO Auto-generated method stub
		experimentsTable.clearSelection();
	}

	@Override
	public boolean hasSpecifiedConstraints() {
		return !experimentsTable.getSelectedExperiments().isEmpty();
	}
}
