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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.cpdid;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdFilter;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.idworks.export.cpdfilter.CompoundIdFilterDefinitionPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MultipleCompoundIdSearchSetupDialog extends JDialog 
implements ActionListener, BackedByPreferences {

	private static final long serialVersionUID = 1L;
	protected static final Icon dialogIcon = GuiUtils.getIcon("searchCompounds", 32);
	
	private Preferences preferences;
	private CompoundIdFilterDefinitionPanel compoundIdFilterDefinitionPanel;
	private MultipleCompoundIdSearchToolbar toolbar;
	private JComboBox polarityComboBox;
		
	public MultipleCompoundIdSearchSetupDialog(ActionListener listener) {
		super();
		setTitle("Define compound filter for feature export");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(700, 640));
		setPreferredSize(new Dimension(700, 640));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		toolbar = new MultipleCompoundIdSearchToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
	
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		compoundIdFilterDefinitionPanel = new CompoundIdFilterDefinitionPanel();
		compoundIdFilterDefinitionPanel.loadPreferences();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 0;
		panel.add(compoundIdFilterDefinitionPanel, gbc_panel_2);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		panel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel = new JLabel("Limit polarity to ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		polarityComboBox = new JComboBox<Polarity>(
				new DefaultComboBoxModel<Polarity>(
						new Polarity[] {Polarity.Positive, Polarity.Negative}));
		polarityComboBox.setPreferredSize(new Dimension(80, 22));
		polarityComboBox.setMinimumSize(new Dimension(80, 22));
		polarityComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		panel_1.add(polarityComboBox, gbc_comboBox);
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		FlowLayout fl_buttonPanel = (FlowLayout) buttonPanel.getLayout();
		fl_buttonPanel.setAlignment(FlowLayout.RIGHT);
		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		btnCancel.addActionListener(al);

		JButton addFilterButton = new JButton(
				MainActionCommands.SEARCH_IDTRACKER_BY_MULTIPLE_COMPOUND_IDS_COMMAND.getName());
		addFilterButton.setActionCommand(
				MainActionCommands.SEARCH_IDTRACKER_BY_MULTIPLE_COMPOUND_IDS_COMMAND.getName());
		addFilterButton.addActionListener(listener);
		buttonPanel.add(addFilterButton);
		
		JRootPane rootPane = SwingUtilities.getRootPane(addFilterButton);
		rootPane.setDefaultButton(addFilterButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		pack();
	}
	
	public CompoundIdFilter getCompoundIdFilter() {
		return compoundIdFilterDefinitionPanel.getCompoundIdFilter();
	}
	
	public Polarity getPolarity() {
		return (Polarity)polarityComboBox.getSelectedItem();
	}
	
	@Override
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}
	
	public void loadFilter(CompoundIdFilter compoundIdFilter) {
		compoundIdFilterDefinitionPanel.loadFilter(compoundIdFilter);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.LOAD_COMPOUND_IDENTIFIERS_FILTER_FROM_DATABASE_COMMAND.getName()))
			loadFilterFromDatabase();
		
		if(command.equals(MainActionCommands.SAVE_COMPOUND_IDENTIFIERS_FILTER_TO_DATABASE_COMMAND.getName()))
			saveFilterToDatabase();
		
		if(command.equals(MainActionCommands.CLEAR_COMPOUND_IDENTIFIERS_FILTER_COMMAND.getName()))
			clearFilter();					
	}
	
	private void loadFilterFromDatabase() {
		// TODO Auto-generated method stub
		
	}

	private void saveFilterToDatabase() {
		// TODO Auto-generated method stub
		
	}

	public void clearFilter() {
		//	TODO
		
		compoundIdFilterDefinitionPanel.clearFilterData();
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		
		preferences = prefs;
		compoundIdFilterDefinitionPanel.loadPreferences();
	}

	@Override
	public void loadPreferences() {
		
		loadPreferences(
				Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userNodeForPackage(this.getClass());
		compoundIdFilterDefinitionPanel.savePreferences();
		
	}
}


