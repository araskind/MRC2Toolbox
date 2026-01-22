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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready.std;

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
import javax.swing.JCheckBox;
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

import ambit2.tautomers.processor.StructureStandardizer;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class CompoundStandardizerSettingsDialog extends JDialog implements BackedByPreferences {

	/**
	 *
	 */
	private static final long serialVersionUID = 5041872153346492046L;
	private static final Icon dialogIcon = GuiUtils.getIcon("standardCompound", 32);

	private Preferences preferences;
	
	public static final String SMILES_FLAVOR = "SMILES_FLAVOR";
	public static final String USE_AROMATIC_SYMBOLS = "USE_AROMATIC_SYMBOLS";
	public static final String GENERATE_INCHI = "GENERATE_INCHI";
	public static final String SPLITF_RAGMENTS = "SPLITF_RAGMENTS";
	public static final String GENERATE_TAUTOMERS = "GENERATE_TAUTOMERS";
	public static final String NEUTRALISE = "NEUTRALISE";
	public static final String CLEAR_ISOTOPES = "CLEAR_ISOTOPES";
	public static final String IMPLICIT_HYDROGENS = "IMPLICIT_HYDROGENS";
	public static final String GENERATE_2D_COORDINATES = "GENERATE_2D_COORDINATES";
	public static final String STEREO_FROM_2D = "STEREO_FROM_2D";
	
	private JComboBox<CompoundStandardizerSmilesFlavors> smiFlavorComboBox;
	private JCheckBox useAromaticSymbolsCheckBox;
	private JCheckBox generateInChICheckBox;
	private JCheckBox splitFragmentsCheckBox;
	private JCheckBox generateTautomersCheckBox;
	private JCheckBox neutraliseCheckBox;
	private JCheckBox clearIsotopesCheckBox;
	private JCheckBox implicitHydrogensCheckBox;
	private JCheckBox generate2DCoordinatesCheckBox;
	private JCheckBox generateStereofrom2DCheckBox;	

	public CompoundStandardizerSettingsDialog(ActionListener listener) {
		super();
		setTitle("Compound Standardizer Settings");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(350, 400));
		setSize(new Dimension(640, 480));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel = new JLabel("Smiles flavor");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		smiFlavorComboBox = 
				new JComboBox<CompoundStandardizerSmilesFlavors>(
						new DefaultComboBoxModel<CompoundStandardizerSmilesFlavors>(
						new CompoundStandardizerSmilesFlavors[] {
								CompoundStandardizerSmilesFlavors.Canonical, 
								CompoundStandardizerSmilesFlavors.Isomeric
							}));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		panel_1.add(smiFlavorComboBox, gbc_comboBox);
		
		useAromaticSymbolsCheckBox = new JCheckBox("Use aromatic symbols in SMILES");
		GridBagConstraints gbc_useAromaticSymbolsCheckBox = new GridBagConstraints();
		gbc_useAromaticSymbolsCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_useAromaticSymbolsCheckBox.gridwidth = 2;
		gbc_useAromaticSymbolsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_useAromaticSymbolsCheckBox.gridx = 0;
		gbc_useAromaticSymbolsCheckBox.gridy = 1;
		panel_1.add(useAromaticSymbolsCheckBox, gbc_useAromaticSymbolsCheckBox);
		
		generateInChICheckBox = new JCheckBox("Generate InChI");
		GridBagConstraints gbc_generateInChICheckBox = new GridBagConstraints();
		gbc_generateInChICheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_generateInChICheckBox.gridwidth = 2;
		gbc_generateInChICheckBox.anchor = GridBagConstraints.WEST;
		gbc_generateInChICheckBox.gridx = 0;
		gbc_generateInChICheckBox.gridy = 2;
		panel_1.add(generateInChICheckBox, gbc_generateInChICheckBox);
		
		splitFragmentsCheckBox = new JCheckBox("Split fragments");
		GridBagConstraints gbc_splitFragmentsCheckBox = new GridBagConstraints();
		gbc_splitFragmentsCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_splitFragmentsCheckBox.gridwidth = 2;
		gbc_splitFragmentsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_splitFragmentsCheckBox.gridx = 0;
		gbc_splitFragmentsCheckBox.gridy = 3;
		panel_1.add(splitFragmentsCheckBox, gbc_splitFragmentsCheckBox);
		
		generateTautomersCheckBox = new JCheckBox("Generate tautomers");
		GridBagConstraints gbc_generateTautomersCheckBox = new GridBagConstraints();
		gbc_generateTautomersCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_generateTautomersCheckBox.gridwidth = 2;
		gbc_generateTautomersCheckBox.anchor = GridBagConstraints.WEST;
		gbc_generateTautomersCheckBox.gridx = 0;
		gbc_generateTautomersCheckBox.gridy = 4;
		panel_1.add(generateTautomersCheckBox, gbc_generateTautomersCheckBox);
		
		neutraliseCheckBox = new JCheckBox("Neutralise");
		GridBagConstraints gbc_neutraliseCheckBox = new GridBagConstraints();
		gbc_neutraliseCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_neutraliseCheckBox.gridwidth = 2;
		gbc_neutraliseCheckBox.anchor = GridBagConstraints.WEST;
		gbc_neutraliseCheckBox.gridx = 0;
		gbc_neutraliseCheckBox.gridy = 5;
		panel_1.add(neutraliseCheckBox, gbc_neutraliseCheckBox);
		
		clearIsotopesCheckBox = new JCheckBox("Clear isotopes");
		GridBagConstraints gbc_clearIsotopesCheckBox = new GridBagConstraints();
		gbc_clearIsotopesCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_clearIsotopesCheckBox.anchor = GridBagConstraints.WEST;
		gbc_clearIsotopesCheckBox.gridwidth = 2;
		gbc_clearIsotopesCheckBox.gridx = 0;
		gbc_clearIsotopesCheckBox.gridy = 6;
		panel_1.add(clearIsotopesCheckBox, gbc_clearIsotopesCheckBox);
		
		implicitHydrogensCheckBox = new JCheckBox("Suppress explicit hydrogens");
		GridBagConstraints gbc_implicitHydrogensCheckBox = new GridBagConstraints();
		gbc_implicitHydrogensCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_implicitHydrogensCheckBox.anchor = GridBagConstraints.WEST;
		gbc_implicitHydrogensCheckBox.gridwidth = 2;
		gbc_implicitHydrogensCheckBox.gridx = 0;
		gbc_implicitHydrogensCheckBox.gridy = 7;
		panel_1.add(implicitHydrogensCheckBox, gbc_implicitHydrogensCheckBox);
		
		generate2DCoordinatesCheckBox = new JCheckBox("Generate 2D coordinates");
		GridBagConstraints gbc_generate2DCoordinatesCheckBox = new GridBagConstraints();
		gbc_generate2DCoordinatesCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_generate2DCoordinatesCheckBox.anchor = GridBagConstraints.WEST;
		gbc_generate2DCoordinatesCheckBox.gridwidth = 2;
		gbc_generate2DCoordinatesCheckBox.gridx = 0;
		gbc_generate2DCoordinatesCheckBox.gridy = 8;
		panel_1.add(generate2DCoordinatesCheckBox, gbc_generate2DCoordinatesCheckBox);
		
		generateStereofrom2DCheckBox = new JCheckBox("Create stereo elements using 2Dcoordinates ");
		GridBagConstraints gbc_generateStereofrom2DCheckBox = new GridBagConstraints();
		gbc_generateStereofrom2DCheckBox.anchor = GridBagConstraints.WEST;
		gbc_generateStereofrom2DCheckBox.gridwidth = 2;
		gbc_generateStereofrom2DCheckBox.gridx = 0;
		gbc_generateStereofrom2DCheckBox.gridy = 9;
		panel_1.add(generateStereofrom2DCheckBox, gbc_generateStereofrom2DCheckBox);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(
				MainActionCommands.SAVE_COMPOUND_STANDARDIZER_SETTINGS_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.SAVE_COMPOUND_STANDARDIZER_SETTINGS_COMMAND.getName());
		btnSave.addActionListener(listener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		loadPreferences();
		pack();
	}	
	
	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		
		preferences = prefs;
		String flavorName = preferences.get(
				SMILES_FLAVOR, CompoundStandardizerSmilesFlavors.Isomeric.name());
		CompoundStandardizerSmilesFlavors smiFlavor = 
				CompoundStandardizerSmilesFlavors.valueOf(flavorName);
		smiFlavorComboBox.setSelectedItem(smiFlavor);
		
		useAromaticSymbolsCheckBox.setSelected(preferences.getBoolean(USE_AROMATIC_SYMBOLS,false));
		generateInChICheckBox.setSelected(preferences.getBoolean(GENERATE_INCHI,true));
		splitFragmentsCheckBox.setSelected(preferences.getBoolean(SPLITF_RAGMENTS,false));
		generateTautomersCheckBox.setSelected(preferences.getBoolean(GENERATE_TAUTOMERS,false));
		neutraliseCheckBox.setSelected(preferences.getBoolean(NEUTRALISE,false));
		clearIsotopesCheckBox.setSelected(preferences.getBoolean(CLEAR_ISOTOPES,false));
		implicitHydrogensCheckBox.setSelected(preferences.getBoolean(IMPLICIT_HYDROGENS,false));
		generate2DCoordinatesCheckBox.setSelected(preferences.getBoolean(GENERATE_2D_COORDINATES,false));
		generateStereofrom2DCheckBox.setSelected(preferences.getBoolean(STEREO_FROM_2D,false));
	}

	@Override
	public void loadPreferences() {
				    
		loadPreferences(Preferences.userRoot().node(
				MRC2ToolBoxConfiguration.COMPOUND_STANDARDIZER_PREFERENCES_NODE));
	}

	@Override
	public void savePreferences() {
		
		preferences = Preferences.userRoot().node(
				MRC2ToolBoxConfiguration.COMPOUND_STANDARDIZER_PREFERENCES_NODE);
		preferences.put(SMILES_FLAVOR, 
				((CompoundStandardizerSmilesFlavors)smiFlavorComboBox.getSelectedItem()).name());
		
		preferences.putBoolean(USE_AROMATIC_SYMBOLS,useAromaticSymbolsCheckBox.isSelected());
		preferences.putBoolean(GENERATE_INCHI,generateInChICheckBox.isSelected());
		preferences.putBoolean(SPLITF_RAGMENTS,splitFragmentsCheckBox.isSelected());
		preferences.putBoolean(GENERATE_TAUTOMERS,generateTautomersCheckBox.isSelected());
		preferences.putBoolean(NEUTRALISE,neutraliseCheckBox.isSelected());
		preferences.putBoolean(CLEAR_ISOTOPES,clearIsotopesCheckBox.isSelected());
		preferences.putBoolean(IMPLICIT_HYDROGENS,implicitHydrogensCheckBox.isSelected());
		preferences.putBoolean(GENERATE_2D_COORDINATES,generate2DCoordinatesCheckBox.isSelected());
		preferences.putBoolean(STEREO_FROM_2D,generateStereofrom2DCheckBox.isSelected());
	}
	
	public StructureStandardizer getConfiguredStructureStandardizer() {
		
		StructureStandardizer structStd = new StructureStandardizer();
		CompoundStandardizerSmilesFlavors smiFlavor = 
				(CompoundStandardizerSmilesFlavors)smiFlavorComboBox.getSelectedItem();
		structStd.setGenerateSMILES_Canonical(
				smiFlavor.equals(CompoundStandardizerSmilesFlavors.Canonical));
		structStd.setGenerateSMILES_Aromatic(
				useAromaticSymbolsCheckBox.isSelected());
		structStd.setGenerateInChI(generateInChICheckBox.isSelected());
		structStd.setSplitFragments(splitFragmentsCheckBox.isSelected());
		structStd.setGenerateTautomers(generateTautomersCheckBox.isSelected());
		structStd.setNeutralise(neutraliseCheckBox.isSelected());
		structStd.setClearIsotopes(clearIsotopesCheckBox.isSelected());
		structStd.setImplicitHydrogens(implicitHydrogensCheckBox.isSelected());
		structStd.setGenerate2D(generate2DCoordinatesCheckBox.isSelected());
		structStd.setGenerateStereofrom2D(generateStereofrom2DCheckBox.isSelected());
		
		return structStd;
	}
}


















