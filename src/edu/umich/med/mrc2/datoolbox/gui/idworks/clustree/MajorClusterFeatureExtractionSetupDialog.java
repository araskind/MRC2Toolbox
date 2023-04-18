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

package edu.umich.med.mrc2.datoolbox.gui.idworks.clustree;

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

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MajorClusterFeatureExtractionSetupDialog extends JDialog implements BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4663123308816746383L;

	private static final Icon filterIcon = GuiUtils.getIcon("filterCluster", 32);
	
	private Preferences preferences;

	private JComboBox mfpComboBox;
	public static final String PREFS_NODE = 
			"edu.umich.med.mrc2.datoolbox.gui.MajorClusterFeatureExtractionSetupDialog";
	
	public MajorClusterFeatureExtractionSetupDialog(ActionListener listener) {
		super();

		setTitle("Extract major features from MSMS clusters");
		setIconImage(((ImageIcon)filterIcon).getImage());
		setPreferredSize(new Dimension(400, 200));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel = new JLabel("Major Cluster Feature Defining Property");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		mfpComboBox = new JComboBox<MajorClusterFeatureDefiningProperty>(
				new DefaultComboBoxModel<MajorClusterFeatureDefiningProperty>(
						MajorClusterFeatureDefiningProperty.values()));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 1;
		panel_1.add(mfpComboBox, gbc_comboBox);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton cancelButton = new JButton("Cancel");
		panel.add(cancelButton);

		JButton filterButton = 
				new JButton(MainActionCommands.EXTRACT_MAJOR_CLUSTER_FEATURES_COMMAND.getName());
		filterButton.setActionCommand(
				MainActionCommands.EXTRACT_MAJOR_CLUSTER_FEATURES_COMMAND.getName());
		filterButton.addActionListener(listener);
		panel.add(filterButton);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);

		JRootPane rootPane = SwingUtilities.getRootPane(filterButton);
		rootPane.registerKeyboardAction(al, stroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(filterButton);
		loadPreferences();
		pack();
	}
	
	@Override
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}
	
	public MajorClusterFeatureDefiningProperty getMajorClusterFeatureDefiningProperty() {
		return (MajorClusterFeatureDefiningProperty)mfpComboBox.getSelectedItem();
	}

	private static final String FEATURE_DEFINING_PROPERTY = "FEATURE_DEFINING_PROPERTY";
	
	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;
		
		MajorClusterFeatureDefiningProperty met = 
				MajorClusterFeatureDefiningProperty.getPropertyByName(
				preferences.get(FEATURE_DEFINING_PROPERTY, MajorClusterFeatureDefiningProperty.LARGEST_AREA.name()));
		mfpComboBox.setSelectedItem(met);
	}

	@Override
	public void savePreferences() {
		
		preferences = Preferences.userRoot().node(PREFS_NODE);		
		MajorClusterFeatureDefiningProperty met = 
				getMajorClusterFeatureDefiningProperty();
		if(met == null)
			met = MajorClusterFeatureDefiningProperty.LARGEST_AREA;
		preferences.put(FEATURE_DEFINING_PROPERTY, met.name());
	}
}
