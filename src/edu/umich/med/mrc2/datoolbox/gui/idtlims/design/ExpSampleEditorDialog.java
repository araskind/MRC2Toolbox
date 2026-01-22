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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.design;

import java.awt.BorderLayout;
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

import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.stock.StockSampleSelectorDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class ExpSampleEditorDialog extends JDialog  implements ActionListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = 7362033770546665803L;

	private static final Icon addSampleIcon = GuiUtils.getIcon("addSample", 32);
	private static final Icon editSampleIcon = GuiUtils.getIcon("editSample", 32);

	private Preferences preferences;
	public static final String PREFS_NODE = ExpSampleEditorDialog.class.getName();
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";




	private File baseDirectory;
	private ActionListener actionListener;
	private JTextField sampleNameTextField;
	private JTextField externalSourceIDTextField;
	private JTextField externalSourceNametextField;
	private JTextArea descriptionTextArea;
	private JLabel sampleTypeValueLabel;
	private JLabel speciesValueLabel;
	private JLabel sampleIdValueLabel;
	private JLabel stockSampleDescriptionLabel;
	private JButton btnSelectStockSample;
	private JButton btnSave;

	private IDTExperimentalSample sample;
	private StockSample stockSample;
	private StockSampleSelectorDialog stockSampleSelectorDialog;

	public ExpSampleEditorDialog(IDTExperimentalSample sample, ActionListener actionListener) {
		super();
		this.sample = sample;
		this.actionListener = actionListener;

		setPreferredSize(new Dimension(600, 350));
		setSize(new Dimension(600, 350));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);

		JLabel lblStockSample = new JLabel("Stock sample");
		lblStockSample.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblStockSample = new GridBagConstraints();
		gbc_lblStockSample.insets = new Insets(0, 0, 5, 5);
		gbc_lblStockSample.gridx = 0;
		gbc_lblStockSample.gridy = 0;
		dataPanel.add(lblStockSample, gbc_lblStockSample);

		stockSampleDescriptionLabel = new JLabel("");
		GridBagConstraints gbc_stockSampleDescriptionLabel = new GridBagConstraints();
		gbc_stockSampleDescriptionLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_stockSampleDescriptionLabel.insets = new Insets(0, 0, 5, 5);
		gbc_stockSampleDescriptionLabel.gridx = 1;
		gbc_stockSampleDescriptionLabel.gridy = 0;
		dataPanel.add(stockSampleDescriptionLabel, gbc_stockSampleDescriptionLabel);

		btnSelectStockSample = new JButton("Select stock sample");
		btnSelectStockSample.setActionCommand(MainActionCommands.SHOW_STOCK_SAMPLE_SELECTOR_COMMAND.getName());
		btnSelectStockSample.addActionListener(this);
		GridBagConstraints gbc_btnSelectStockSample = new GridBagConstraints();
		gbc_btnSelectStockSample.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSelectStockSample.insets = new Insets(0, 0, 5, 0);
		gbc_btnSelectStockSample.gridx = 2;
		gbc_btnSelectStockSample.gridy = 0;
		dataPanel.add(btnSelectStockSample, gbc_btnSelectStockSample);

		JLabel lblSampleId = new JLabel("Sample ID");
		lblSampleId.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblSampleId = new GridBagConstraints();
		gbc_lblSampleId.anchor = GridBagConstraints.EAST;
		gbc_lblSampleId.insets = new Insets(0, 0, 5, 5);
		gbc_lblSampleId.gridx = 0;
		gbc_lblSampleId.gridy = 1;
		dataPanel.add(lblSampleId, gbc_lblSampleId);

		sampleIdValueLabel = new JLabel("");
		GridBagConstraints gbc_sampleIdValueLabel = new GridBagConstraints();
		gbc_sampleIdValueLabel.anchor = GridBagConstraints.WEST;
		gbc_sampleIdValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_sampleIdValueLabel.gridx = 1;
		gbc_sampleIdValueLabel.gridy = 1;
		dataPanel.add(sampleIdValueLabel, gbc_sampleIdValueLabel);

		JLabel lblName = new JLabel("Name");
		lblName.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 2;
		dataPanel.add(lblName, gbc_lblName);

		sampleNameTextField = new JTextField();
		GridBagConstraints gbc_sampleNameTextField = new GridBagConstraints();
		gbc_sampleNameTextField.gridwidth = 2;
		gbc_sampleNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_sampleNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_sampleNameTextField.gridx = 1;
		gbc_sampleNameTextField.gridy = 2;
		dataPanel.add(sampleNameTextField, gbc_sampleNameTextField);
		sampleNameTextField.setColumns(10);

		JLabel lblDescription = new JLabel("Description");
		lblDescription.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 3;
		dataPanel.add(lblDescription, gbc_lblDescription);

		descriptionTextArea = new JTextArea();
		GridBagConstraints gbc_descriptionTextArea = new GridBagConstraints();
		gbc_descriptionTextArea.gridwidth = 2;
		gbc_descriptionTextArea.insets = new Insets(0, 0, 5, 0);
		gbc_descriptionTextArea.fill = GridBagConstraints.BOTH;
		gbc_descriptionTextArea.gridx = 1;
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
		GridBagConstraints gbc_sampleTypeValueLabel = new GridBagConstraints();
		gbc_sampleTypeValueLabel.gridwidth = 2;
		gbc_sampleTypeValueLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_sampleTypeValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_sampleTypeValueLabel.gridx = 1;
		gbc_sampleTypeValueLabel.gridy = 4;
		dataPanel.add(sampleTypeValueLabel, gbc_sampleTypeValueLabel);

		JLabel lblSpecies = new JLabel("Species");
		lblSpecies.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblSpecies = new GridBagConstraints();
		gbc_lblSpecies.anchor = GridBagConstraints.EAST;
		gbc_lblSpecies.insets = new Insets(0, 0, 5, 5);
		gbc_lblSpecies.gridx = 0;
		gbc_lblSpecies.gridy = 5;
		dataPanel.add(lblSpecies, gbc_lblSpecies);

		speciesValueLabel = new JLabel("");
		GridBagConstraints gbc_speciesValueLabel = new GridBagConstraints();
		gbc_speciesValueLabel.gridwidth = 2;
		gbc_speciesValueLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_speciesValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_speciesValueLabel.gridx = 1;
		gbc_speciesValueLabel.gridy = 5;
		dataPanel.add(speciesValueLabel, gbc_speciesValueLabel);

		JLabel lblExternalSource = new JLabel("External source");
		lblExternalSource.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblExternalSource = new GridBagConstraints();
		gbc_lblExternalSource.insets = new Insets(0, 0, 5, 5);
		gbc_lblExternalSource.gridx = 0;
		gbc_lblExternalSource.gridy = 6;
		dataPanel.add(lblExternalSource, gbc_lblExternalSource);

		externalSourceNametextField = new JTextField("");
		externalSourceNametextField.setEditable(false);
		GridBagConstraints gbc_externalSourceNameLabel = new GridBagConstraints();
		gbc_externalSourceNameLabel.insets = new Insets(0, 0, 5, 0);
		gbc_externalSourceNameLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_externalSourceNameLabel.gridwidth = 2;
		gbc_externalSourceNameLabel.gridx = 1;
		gbc_externalSourceNameLabel.gridy = 6;
		dataPanel.add(externalSourceNametextField, gbc_externalSourceNameLabel);

		JLabel lblExternalId = new JLabel("External ID");
		lblExternalId.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblExternalId = new GridBagConstraints();
		gbc_lblExternalId.anchor = GridBagConstraints.EAST;
		gbc_lblExternalId.insets = new Insets(0, 0, 0, 5);
		gbc_lblExternalId.gridx = 0;
		gbc_lblExternalId.gridy = 7;
		dataPanel.add(lblExternalId, gbc_lblExternalId);

		externalSourceIDTextField = new JTextField("");
		externalSourceIDTextField.setEditable(false);
		GridBagConstraints gbc_externalSourceIDLabel = new GridBagConstraints();
		gbc_externalSourceIDLabel.insets = new Insets(0, 0, 0, 5);
		gbc_externalSourceIDLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_externalSourceIDLabel.gridx = 1;
		gbc_externalSourceIDLabel.gridy = 7;
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
				disposeWithoutSavingPreferences();
			}
		};
		btnCancel.addActionListener(al);

		btnSave = new JButton("Save");
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);


		loadSampleData();
		loadPreferences();
	}
	
	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}

	private void loadSampleData() {

		if(sample == null) {

			setTitle("Add new sample");
			setIconImage(((ImageIcon) addSampleIcon).getImage());
			btnSave.setText(MainActionCommands.ADD_SAMPLE_COMMAND.getName());
			btnSave.setActionCommand(MainActionCommands.ADD_SAMPLE_COMMAND.getName());
		}
		else {
			setTitle("Edit sample \"" + sample.getName() + "\"");
			setIconImage(((ImageIcon) editSampleIcon).getImage());
			btnSave.setText(MainActionCommands.EDIT_SAMPLE_COMMAND.getName());
			btnSave.setActionCommand(MainActionCommands.EDIT_SAMPLE_COMMAND.getName());
			btnSelectStockSample.setEnabled(false);
			stockSample = sample.getParentStockSample();
			if(stockSample != null) {
				stockSampleDescriptionLabel.setText(stockSample.getSampleName() + " (" + stockSample.getSampleId() +")");
				externalSourceIDTextField.setText(stockSample.getExternalId());
				externalSourceNametextField.setText(stockSample.getExternalSource());
				sampleTypeValueLabel.setText(stockSample.getLimsSampleType().getName());
				speciesValueLabel.setText(stockSample.getSpecies().getSpeciesPrimaryName());
			}
			sampleIdValueLabel.setText(sample.getId());
			sampleNameTextField.setText(sample.getName());
			descriptionTextArea.setText(sample.getDescription());
		}
		pack();
	}

	public IDTExperimentalSample getSample() {
		return sample;
	}

	public StockSample getStockSample() {
		return stockSample;
	}

	public String getSampleName() {
		return sampleNameTextField.getText().trim();
	}

	public String getSampleDescription() {
		return descriptionTextArea.getText().trim();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.SHOW_STOCK_SAMPLE_SELECTOR_COMMAND.getName())) {
			stockSampleSelectorDialog = new StockSampleSelectorDialog(this);
			stockSampleSelectorDialog.setLocationRelativeTo(this);
			stockSampleSelectorDialog.setVisible(true);
		}
		if(e.getActionCommand().equals(MainActionCommands.SELECT_STOCK_SAMPLE_COMMAND.getName())) {

			if(stockSampleSelectorDialog.getSelectedSample() == null)
				return;

			stockSample = stockSampleSelectorDialog.getSelectedSample();
			stockSampleDescriptionLabel.setText(stockSample.getSampleName() + " (" + stockSample.getSampleId() +")");
			externalSourceIDTextField.setText(stockSample.getExternalId());
			externalSourceNametextField.setText(stockSample.getExternalSource());
			
			if(stockSample.getLimsSampleType() != null)
				sampleTypeValueLabel.setText(stockSample.getLimsSampleType().getName());
			else
				sampleTypeValueLabel.setText(stockSample.getSampleName());
				
			speciesValueLabel.setText(stockSample.getSpecies().getSpeciesPrimaryName());
			sampleNameTextField.setText("Aliquot of " + stockSample.getSampleName());
			descriptionTextArea.setText(stockSample.getSampleDescription());
			stockSampleSelectorDialog.dispose();
		}
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		loadPreferences();
	}

	@Override
	public void loadPreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		baseDirectory =  new File(preferences.get(BASE_DIRECTORY, MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}
}
