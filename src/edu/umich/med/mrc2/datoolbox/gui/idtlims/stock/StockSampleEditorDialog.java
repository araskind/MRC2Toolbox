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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.stock;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSBioSpecies;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSampleType;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.stock.lookup.SampleTypeLookupDialog;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.stock.lookup.TaxonomyLookupDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class StockSampleEditorDialog extends JDialog  implements ActionListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = 7362033770546665803L;

	private static final Icon addSampleIcon = GuiUtils.getIcon("addSample", 32);
	private static final Icon editSampleIcon = GuiUtils.getIcon("editSample", 32);

	private Preferences preferences;
	public static final String PREFS_NODE = 
			"edu.umich.med.mrc2.cefanalyzer.gui.idtracker.StockSampleEditorDialog";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";

	private File baseDirectory;
	private JTextField sampleNameTextField;
	private JTextField externalSourceIDTextField;
	private JTextField externalSourceNametextField;
	private JTextArea descriptionTextArea;
	private JTextArea limsExperimentValueTextArea;
	private JLabel sampleTypeValueLabel;
	private JLabel speciesValueLabel;
	private JLabel sampleIdValueLabel;
	private JButton sampleTypeSelectButton;
	private JButton speciesSelectButton;
	private JButton btnSave;
	private LIMSSampleType sampleType;
	private LIMSBioSpecies species;
	private StockSample stockSample;
	private LIMSExperiment limsExperiment;

	private LIMSExperimentSelectorDialog limsExperimentSelectorDialog;
	private TaxonomyLookupDialog taxonomyLookupDialog;
	private SampleTypeLookupDialog sampleTypeLookupDialog;

	public StockSampleEditorDialog(StockSample sample, ActionListener actionListener) {
		super();
		this.stockSample = sample;
		if(sample != null) {
			sampleType = sample.getLimsSampleType();
			species = sample.getSpecies();
		}
		setPreferredSize(new Dimension(640, 400));
		setSize(new Dimension(640, 400));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);

		JLabel lblSampleId = new JLabel("Stock sample ID");
		lblSampleId.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblSampleId = new GridBagConstraints();
		gbc_lblSampleId.anchor = GridBagConstraints.EAST;
		gbc_lblSampleId.insets = new Insets(0, 0, 5, 5);
		gbc_lblSampleId.gridx = 0;
		gbc_lblSampleId.gridy = 0;
		dataPanel.add(lblSampleId, gbc_lblSampleId);

		sampleIdValueLabel = new JLabel("");
		sampleIdValueLabel.setForeground(Color.BLUE);
		sampleIdValueLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_sampleIdValueLabel = new GridBagConstraints();
		gbc_sampleIdValueLabel.anchor = GridBagConstraints.WEST;
		gbc_sampleIdValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_sampleIdValueLabel.gridx = 1;
		gbc_sampleIdValueLabel.gridy = 0;
		dataPanel.add(sampleIdValueLabel, gbc_sampleIdValueLabel);

		JLabel lblName = new JLabel("Name");
		lblName.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		dataPanel.add(lblName, gbc_lblName);

		sampleNameTextField = new JTextField();
		GridBagConstraints gbc_sampleNameTextField = new GridBagConstraints();
		gbc_sampleNameTextField.gridwidth = 2;
		gbc_sampleNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_sampleNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_sampleNameTextField.gridx = 1;
		gbc_sampleNameTextField.gridy = 1;
		dataPanel.add(sampleNameTextField, gbc_sampleNameTextField);
		sampleNameTextField.setColumns(10);
		
				JLabel lblDescription = new JLabel("Description");
				lblDescription.setFont(new Font("Tahoma", Font.BOLD, 11));
				GridBagConstraints gbc_lblDescription = new GridBagConstraints();
				gbc_lblDescription.anchor = GridBagConstraints.NORTHWEST;
				gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
				gbc_lblDescription.gridx = 0;
				gbc_lblDescription.gridy = 2;
				dataPanel.add(lblDescription, gbc_lblDescription);

		descriptionTextArea = new JTextArea();
		descriptionTextArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		GridBagConstraints gbc_descriptionTextArea = new GridBagConstraints();
		gbc_descriptionTextArea.gridwidth = 3;
		gbc_descriptionTextArea.insets = new Insets(0, 0, 5, 0);
		gbc_descriptionTextArea.fill = GridBagConstraints.BOTH;
		gbc_descriptionTextArea.gridx = 0;
		gbc_descriptionTextArea.gridy = 3;
		dataPanel.add(descriptionTextArea, gbc_descriptionTextArea);

		JLabel lblSampleType = new JLabel("Sample type");
		lblSampleType.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblSampleType = new GridBagConstraints();
		gbc_lblSampleType.anchor = GridBagConstraints.EAST;
		gbc_lblSampleType.insets = new Insets(0, 0, 5, 5);
		gbc_lblSampleType.gridx = 0;
		gbc_lblSampleType.gridy = 4;
		dataPanel.add(lblSampleType, gbc_lblSampleType);

		sampleTypeValueLabel = new JLabel("");
		sampleTypeValueLabel.setForeground(Color.BLUE);
		sampleTypeValueLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_sampleTypeValueLabel = new GridBagConstraints();
		gbc_sampleTypeValueLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_sampleTypeValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_sampleTypeValueLabel.gridx = 1;
		gbc_sampleTypeValueLabel.gridy = 4;
		dataPanel.add(sampleTypeValueLabel, gbc_sampleTypeValueLabel);

		sampleTypeSelectButton = new JButton("Select sample type");
		sampleTypeSelectButton.setActionCommand(MainActionCommands.SHOW_SELECT_SAMPLE_TYPE_DIALOG_COMMAND.getName());
		sampleTypeSelectButton.addActionListener(this);
		GridBagConstraints gbc_sampleTypeSelectButton = new GridBagConstraints();
		gbc_sampleTypeSelectButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_sampleTypeSelectButton.insets = new Insets(0, 0, 5, 0);
		gbc_sampleTypeSelectButton.gridx = 2;
		gbc_sampleTypeSelectButton.gridy = 4;
		dataPanel.add(sampleTypeSelectButton, gbc_sampleTypeSelectButton);

		JLabel lblSpecies = new JLabel("Species");
		lblSpecies.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblSpecies = new GridBagConstraints();
		gbc_lblSpecies.anchor = GridBagConstraints.EAST;
		gbc_lblSpecies.insets = new Insets(0, 0, 5, 5);
		gbc_lblSpecies.gridx = 0;
		gbc_lblSpecies.gridy = 5;
		dataPanel.add(lblSpecies, gbc_lblSpecies);

		speciesValueLabel = new JLabel("");
		speciesValueLabel.setForeground(Color.BLUE);
		speciesValueLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_speciesValueLabel = new GridBagConstraints();
		gbc_speciesValueLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_speciesValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_speciesValueLabel.gridx = 1;
		gbc_speciesValueLabel.gridy = 5;
		dataPanel.add(speciesValueLabel, gbc_speciesValueLabel);

		speciesSelectButton = new JButton("Select species");
		speciesSelectButton.setActionCommand(MainActionCommands.SHOW_SELECT_SPECIES_DIALOG_COMMAND.getName());
		speciesSelectButton.addActionListener(this);
		GridBagConstraints gbc_speciesSelectButton = new GridBagConstraints();
		gbc_speciesSelectButton.insets = new Insets(0, 0, 5, 0);
		gbc_speciesSelectButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_speciesSelectButton.anchor = GridBagConstraints.NORTH;
		gbc_speciesSelectButton.gridx = 2;
		gbc_speciesSelectButton.gridy = 5;
		dataPanel.add(speciesSelectButton, gbc_speciesSelectButton);
		
		JLabel lblLimsExperiment = new JLabel("LIMS experiment");
		lblLimsExperiment.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblLimsExperiment = new GridBagConstraints();
		gbc_lblLimsExperiment.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblLimsExperiment.insets = new Insets(0, 0, 5, 5);
		gbc_lblLimsExperiment.gridx = 0;
		gbc_lblLimsExperiment.gridy = 6;
		dataPanel.add(lblLimsExperiment, gbc_lblLimsExperiment);
		
		limsExperimentValueTextArea = new JTextArea("");
		limsExperimentValueTextArea.setWrapStyleWord(true);
		limsExperimentValueTextArea.setLineWrap(true);
		limsExperimentValueTextArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		limsExperimentValueTextArea.setEditable(false);
		limsExperimentValueTextArea.setForeground(Color.BLUE);
		limsExperimentValueTextArea.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_limsExperimentValueLabel = new GridBagConstraints();
		gbc_limsExperimentValueLabel.gridwidth = 2;
		gbc_limsExperimentValueLabel.fill = GridBagConstraints.BOTH;
		gbc_limsExperimentValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_limsExperimentValueLabel.gridx = 1;
		gbc_limsExperimentValueLabel.gridy = 6;
		dataPanel.add(limsExperimentValueTextArea, gbc_limsExperimentValueLabel);
		
		JButton selectExperimentButton = new JButton("Select LIMS experiment");
		selectExperimentButton.setActionCommand(
				MainActionCommands.SHOW_LIMS_EXPERIMENT_SELECTOR_COMMAND.getName());
		selectExperimentButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 7;
		dataPanel.add(selectExperimentButton, gbc_btnNewButton);

		JLabel lblExternalSource = new JLabel("External source");
		lblExternalSource.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblExternalSource = new GridBagConstraints();
		gbc_lblExternalSource.insets = new Insets(0, 0, 5, 5);
		gbc_lblExternalSource.gridx = 0;
		gbc_lblExternalSource.gridy = 8;
		dataPanel.add(lblExternalSource, gbc_lblExternalSource);

		externalSourceNametextField = new JTextField("");
		GridBagConstraints gbc_externalSourceNameLabel = new GridBagConstraints();
		gbc_externalSourceNameLabel.insets = new Insets(0, 0, 5, 0);
		gbc_externalSourceNameLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_externalSourceNameLabel.gridwidth = 2;
		gbc_externalSourceNameLabel.gridx = 1;
		gbc_externalSourceNameLabel.gridy = 8;
		dataPanel.add(externalSourceNametextField, gbc_externalSourceNameLabel);

		JLabel lblExternalId = new JLabel("External ID");
		lblExternalId.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblExternalId = new GridBagConstraints();
		gbc_lblExternalId.anchor = GridBagConstraints.EAST;
		gbc_lblExternalId.insets = new Insets(0, 0, 0, 5);
		gbc_lblExternalId.gridx = 0;
		gbc_lblExternalId.gridy = 9;
		dataPanel.add(lblExternalId, gbc_lblExternalId);

		externalSourceIDTextField = new JTextField("");
		GridBagConstraints gbc_externalSourceIDLabel = new GridBagConstraints();
		gbc_externalSourceIDLabel.insets = new Insets(0, 0, 0, 5);
		gbc_externalSourceIDLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_externalSourceIDLabel.gridx = 1;
		gbc_externalSourceIDLabel.gridy = 9;
		dataPanel.add(externalSourceIDTextField, gbc_externalSourceIDLabel);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		btnSave = new JButton("Save");
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		loadSampleData();
		loadPreferences();
	}

	private void loadSampleData() {

		if(stockSample == null) {

			setTitle("Add new stock sample");
			setIconImage(((ImageIcon) addSampleIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.ADD_REFERENCE_SAMPLE_COMMAND.getName());
		}
		else {
			setTitle("Edit stock sample \"" + stockSample.getSampleName() +
					" (" + stockSample.getSampleId() + ")\"");
			setIconImage(((ImageIcon) editSampleIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.EDIT_REFERENCE_SAMPLE_COMMAND.getName());

			sampleIdValueLabel.setText(stockSample.getSampleId());
			sampleNameTextField.setText(stockSample.getSampleName());
			descriptionTextArea.setText(stockSample.getSampleDescription());
			externalSourceIDTextField.setText(stockSample.getExternalId());
			externalSourceNametextField.setText(stockSample.getExternalSource());
			sampleTypeValueLabel.setText(stockSample.getLimsSampleType().getName());
			speciesValueLabel.setText(stockSample.getSpecies().getSpeciesPrimaryName());
			if(stockSample.getLimsExperiment() != null)
				limsExperimentValueTextArea.setText(stockSample.getLimsExperiment().toString());
		}
		pack();
	}

	public StockSample getStockSample() {
		return stockSample;
	}

	public LIMSSampleType getSampleType() {
		return sampleType;
	}

	public LIMSBioSpecies getSpecies() {
		return species;
	}
	
	public LIMSExperiment getLIMSExperiment() {
		return limsExperiment;
	}

	public String getSampleName() {
		return sampleNameTextField.getText().trim();
	}

	public String getSampleDescription() {
		return descriptionTextArea.getText().trim();
	}

	public String getExternalSourceId() {
		return externalSourceIDTextField.getText().trim();
	}

	public String getExternalSourceName() {
		return externalSourceNametextField.getText().trim();
	}

	public Collection<String>validateStockSample(){

		Collection<String>errors = new ArrayList<String>();
		if(sampleType == null)
			errors.add("Sample type not defined.");

		if(species == null)
			errors.add("Species not defined.");

		if(getSampleName().isEmpty())
			errors.add("Sample name is empty.");

		return errors;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();

		if(command.equals(MainActionCommands.SHOW_SELECT_SPECIES_DIALOG_COMMAND.getName()))
			showTaxonomySelector();
		
		if(command.equals(MainActionCommands.SELECT_SPECIES_COMMAND.getName()))
			selectTaxonomyGroup();

		if(command.equals(MainActionCommands.SHOW_SELECT_SAMPLE_TYPE_DIALOG_COMMAND.getName()))
			showSampleTypeSelector();
		
		if(command.equals(MainActionCommands.SELECT_SAMPLE_TYPE_COMMAND.getName()))
			selectSampleType();
		
		if(command.equals(MainActionCommands.SHOW_LIMS_EXPERIMENT_SELECTOR_COMMAND.getName()))
			showLIMSExperimentSelector();
			
		if(command.equals(MainActionCommands.SELECT_LIMS_EXPERIMENT_COMMAND.getName()))
			selectLIMSExperiment();
	}
	
	private void showTaxonomySelector() {
		
		taxonomyLookupDialog = new TaxonomyLookupDialog(this);
		taxonomyLookupDialog.setLocationRelativeTo(this);
		taxonomyLookupDialog.setVisible(true);
	}
	
	private void selectTaxonomyGroup() {
		
		if(taxonomyLookupDialog.getSelectedSpecies() == null)
			return;

		species = taxonomyLookupDialog.getSelectedSpecies();
		speciesValueLabel.setText(species.getSpeciesPrimaryName());
		taxonomyLookupDialog.dispose();
	}
	
	private void showSampleTypeSelector() {
		
		sampleTypeLookupDialog = new SampleTypeLookupDialog(this);
		sampleTypeLookupDialog.setLocationRelativeTo(this);
		sampleTypeLookupDialog.setVisible(true);
	}
	
	private void selectSampleType() {
		
		if(sampleTypeLookupDialog.getSelectedSampleType() == null)
			return;

		sampleType = sampleTypeLookupDialog.getSelectedSampleType();
		sampleTypeValueLabel.setText(sampleType.getName());
		sampleTypeLookupDialog.dispose();
	}
	
	private void showLIMSExperimentSelector() {
		
		limsExperimentSelectorDialog = new LIMSExperimentSelectorDialog(this);
		limsExperimentSelectorDialog.setLocationRelativeTo(this);
		limsExperimentSelectorDialog.setVisible(true);
	}
	
	private void selectLIMSExperiment() {
		
		if(limsExperimentSelectorDialog.getSelectedLIMSExperiment() == null)
			return;

		limsExperiment = limsExperimentSelectorDialog.getSelectedLIMSExperiment();
		limsExperimentValueTextArea.setText(limsExperiment.toString());			
		limsExperimentSelectorDialog.dispose();
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		loadPreferences();
	}

	@Override
	public void loadPreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		baseDirectory =  new File(preferences.get(BASE_DIRECTORY, 
				MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}
}
