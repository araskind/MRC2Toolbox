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

package edu.umich.med.mrc2.datoolbox.gui.idworks.export.cpdfilter;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.StringUtils;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdFilter;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdFilterType;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.ChemInfoUtils;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import io.github.dan2097.jnainchi.InchiStatus;

public class CompoundIdFilterDefinitionPanel extends JPanel 
		implements ActionListener, BackedByPreferences, ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Preferences preferences;
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private File baseDirectory;
	private CpdFilterComponentsTable filterComponentsTable;
	private JTextField inputFileTextField;
	private static final String BROWSE_COMMAND = "BROWSE";
	private JComboBox inputTypeComboBox;
	private JComboBox filterTypeComboBox;
	private JTextField filterNameTextField;
	private CompoundIdFilter compoundIdFilter;
	private JButton browseButton;
	private JCheckBox matchAllSmartsCheckBox;
	
	public CompoundIdFilterDefinitionPanel() {
		
		super(new BorderLayout(0,0));
		
		JPanel controlsPanel = new JPanel();
		controlsPanel.setBorder(new EmptyBorder(10, 5, 10, 5));
		add(controlsPanel, BorderLayout.NORTH);
		GridBagLayout gbl_controlsPanel = new GridBagLayout();
		gbl_controlsPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_controlsPanel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_controlsPanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_controlsPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		controlsPanel.setLayout(gbl_controlsPanel);
		
		JLabel lblNewLabel_2 = new JLabel("Name: ");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 0;
		controlsPanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		String newFilterName = "New compound filter " + 
		MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());;
		filterNameTextField = new JTextField(newFilterName);
		GridBagConstraints gbc_filterNameTextField = new GridBagConstraints();
		gbc_filterNameTextField.gridwidth = 2;
		gbc_filterNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_filterNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_filterNameTextField.gridx = 1;
		gbc_filterNameTextField.gridy = 0;
		controlsPanel.add(filterNameTextField, gbc_filterNameTextField);
		filterNameTextField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Input type: ");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		controlsPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		inputTypeComboBox = new JComboBox<CompoundIdFilterType>(
				new DefaultComboBoxModel<CompoundIdFilterType>(
						CompoundIdFilterType.values()));
		inputTypeComboBox.setSelectedItem(CompoundIdFilterType.INCHI_KEY);
		inputTypeComboBox.addItemListener(this);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.gridwidth = 2;
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 1;
		controlsPanel.add(inputTypeComboBox, gbc_comboBox);
		
		JLabel lblNewLabel = new JLabel("Filter type: ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 2;
		controlsPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		filterTypeComboBox = new JComboBox<CompoundIdFilterType>(
				new DefaultComboBoxModel<CompoundIdFilterType>(
						CompoundIdFilterType.values()));
		filterTypeComboBox.setSelectedItem(CompoundIdFilterType.INCHI_KEY);
		filterTypeComboBox.addItemListener(this);
		GridBagConstraints gbc_filterTypeComboBox = new GridBagConstraints();
		gbc_filterTypeComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_filterTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_filterTypeComboBox.gridx = 1;
		gbc_filterTypeComboBox.gridy = 2;
		controlsPanel.add(filterTypeComboBox, gbc_filterTypeComboBox);
		
		matchAllSmartsCheckBox = new JCheckBox("Match all SMARTS");
		matchAllSmartsCheckBox.setEnabled(false);
		GridBagConstraints gbc_matchAllSmartsCheckBox = new GridBagConstraints();
		gbc_matchAllSmartsCheckBox.gridx = 2;
		gbc_matchAllSmartsCheckBox.gridy = 2;
		controlsPanel.add(matchAllSmartsCheckBox, gbc_matchAllSmartsCheckBox);
		
		filterComponentsTable = new CpdFilterComponentsTable();
		add(new JScrollPane(filterComponentsTable) , BorderLayout.CENTER);	

		JPanel inputSelectorPanel = new JPanel();
		inputSelectorPanel.setBorder(new EmptyBorder(10, 5, 10, 5));
		add(inputSelectorPanel, BorderLayout.SOUTH);
		GridBagLayout gbl_inputSelectorPanel = new GridBagLayout();
		gbl_inputSelectorPanel.columnWidths = new int[]{0, 0, 0};
		gbl_inputSelectorPanel.rowHeights = new int[]{0, 0};
		gbl_inputSelectorPanel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_inputSelectorPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		inputSelectorPanel.setLayout(gbl_inputSelectorPanel);
		
		inputFileTextField = new JTextField();
		inputFileTextField.setEditable(false);
		GridBagConstraints gbc_inputFileTextField = new GridBagConstraints();
		gbc_inputFileTextField.insets = new Insets(0, 0, 0, 5);
		gbc_inputFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_inputFileTextField.gridx = 0;
		gbc_inputFileTextField.gridy = 0;
		inputSelectorPanel.add(inputFileTextField, gbc_inputFileTextField);
		
		browseButton = new JButton("Browse...");
		browseButton.setActionCommand(BROWSE_COMMAND);
		browseButton.addActionListener(this);
		GridBagConstraints gbc_browseButton = new GridBagConstraints();
		gbc_browseButton.gridx = 1;
		gbc_browseButton.gridy = 0;
		inputSelectorPanel.add(browseButton, gbc_browseButton);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(BROWSE_COMMAND))
			selectInputFile();
	}
	
	public void clearFilterData() {
		
		compoundIdFilter = null;
		filterNameTextField.setText("");
		filterNameTextField.setEditable(true);
		inputFileTextField.setText("");
		browseButton.setEnabled(true);
		filterComponentsTable.clearTable();		
		inputTypeComboBox.setEnabled(true);
		filterTypeComboBox.setEnabled(true);
		inputTypeComboBox.addItemListener(this);
		filterTypeComboBox.addItemListener(this);
	}
	
	public void loadFilter(CompoundIdFilter compoundIdFilter) {
		
		this.compoundIdFilter = compoundIdFilter;
		filterNameTextField.setText(compoundIdFilter.getName());
		filterNameTextField.setEditable(false);
		browseButton.setEnabled(false);
		inputFileTextField.setText("");
		inputTypeComboBox.removeItemListener(this);
		inputTypeComboBox.setSelectedItem(compoundIdFilter.getFilterType());
		inputTypeComboBox.setEnabled(false);
		filterTypeComboBox.removeItemListener(this);
		filterTypeComboBox.setSelectedItem(compoundIdFilter.getFilterType());
		filterTypeComboBox.setEnabled(false);
		matchAllSmartsCheckBox.setSelected(compoundIdFilter.isMatchAllSmarts());
		
		Map<String,String>filterComponentMap = new TreeMap<String,String>();
		for(String fc : compoundIdFilter.getFilterComponents()) 
			filterComponentMap.put(fc, fc);
		
		filterComponentsTable.setTableModelFromValuePairs(filterComponentMap);
	}
	
	public CompoundIdFilter getCompoundIdFilter() {
		
		if(compoundIdFilter == null) {
			
			Set<String>filterComponents = 
					filterComponentsTable.getFilterComponents();
			if(filterComponents == null || filterComponents.isEmpty())
				return null;
			
			compoundIdFilter = new CompoundIdFilter(
					getFilterName(), getCompoundIdFilterType());
			compoundIdFilter.getFilterComponents().addAll(filterComponents);
			compoundIdFilter.setMatchAllSmarts(matchAllSmartsCheckBox.isSelected());
			return compoundIdFilter;
		}
		else
			return compoundIdFilter;
	}
	
	public String getFilterName() {
		return filterNameTextField.getText().trim();
	}
	
	private void selectInputFile() {
		// TODO Auto-generated method stub
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT");
		fc.setTitle("Import filter components from text file:");
		fc.setMultiSelectionEnabled(false);
		fc.setSaveButtonText("Set output file");

		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this))) {
			
			File inputFile  = fc.getSelectedFile();
			inputFileTextField.setText(inputFile.getAbsolutePath());
			baseDirectory = inputFile.getParentFile();
			processInputFile(inputFile);
		}
	}

	private void processInputFile(File inputFile) {
		
		FilterComponentVerificationTask task = 
				new FilterComponentVerificationTask(
						inputFile, getInputType(), getCompoundIdFilterType());
		IndeterminateProgressDialog idp = 
				new IndeterminateProgressDialog(
						"Importing and verifying the data from file ...", this, task);
		idp.setLocationRelativeTo(this);
		idp.setVisible(true);
	}
	
	class FilterComponentVerificationTask extends LongUpdateTask {

		private File inputFile;
		private CompoundIdFilterType inputType;
		private CompoundIdFilterType filterType;
		public FilterComponentVerificationTask(
				File inputFile,
				CompoundIdFilterType inputType,
				CompoundIdFilterType filterType) {			
			this.inputFile = inputFile;
			this.inputType = inputType;
			this.filterType = filterType;
		}

		@Override
		public Void doInBackground() {

			String[][] inputData = null;
			try {
				inputData = DelimitedTextParser.parseTextFileWithEncoding(
						inputFile, MRC2ToolBoxConfiguration.getTabDelimiter());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (inputData == null || inputData.length == 0)
				return null;
				
			InChIGeneratorFactory igfactory = null;
			InChIGenerator inChIGenerator;
			try {
				igfactory = InChIGeneratorFactory.getInstance();
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//	Only use first column
			Set<String>inputStrings = new TreeSet<String>();
			Set<String>invalidEntries = new TreeSet<String>();
			Map<String,String>filterComponentMap = new TreeMap<String,String>();
			for(int i=0; i<inputData.length; i++)	
				inputStrings.add(inputData[i][0]);
			
			if(inputType.equals(CompoundIdFilterType.COMPOUND_NAME) 
					|| inputType.equals(CompoundIdFilterType.COMPOUND_DATABASE_ID)) {
				for(String inputString : inputStrings)
					filterComponentMap.put(inputString, inputString);
			}
			if(inputType.equals(CompoundIdFilterType.SMILES)) {
				
				for(String smilesString : inputStrings) {
					
					IAtomContainer mol = null;
					try {
						mol = ChemInfoUtils.generateMoleculeFromSMILES(smilesString);
					} catch (Exception e) {
						//	e.printStackTrace();
					}
					if(mol != null) {
						
						if(filterType.equals(CompoundIdFilterType.INCHI_KEY)
								|| filterType.equals(CompoundIdFilterType.INCHI_KEY2D)) {
							
							String inchiKey = null;
							try {
								inChIGenerator = igfactory.getInChIGenerator(mol);
								InchiStatus inchiStatus = inChIGenerator.getStatus();
								if (inchiStatus.equals(InchiStatus.WARNING)) {
									//	errorLog.add(accession + "\tInChI warning: " + inChIGenerator.getMessage());
								} else if (!inchiStatus.equals(InchiStatus.SUCCESS)) {
									invalidEntries.add(smilesString + "\tInChI failed: [" + inChIGenerator.getMessage() + "]");
								}
								inchiKey = inChIGenerator.getInchiKey();
							} catch (CDKException e1) {
								invalidEntries.add(smilesString);
								e1.printStackTrace();
							}
							if(inchiKey != null) {
								
								if(filterType.equals(CompoundIdFilterType.INCHI_KEY))
									filterComponentMap.put(smilesString, inchiKey);
								
								if(filterType.equals(CompoundIdFilterType.INCHI_KEY2D))
									filterComponentMap.put(smilesString, inchiKey.substring(0, 14));
							}
						}
						else
							filterComponentMap.put(smilesString, smilesString);
					}
					else
						invalidEntries.add(smilesString);
				}
			}
			if(inputType.equals(CompoundIdFilterType.SMARTS)) {
				
				for(String smartsString : inputStrings) {
					
					if(ChemInfoUtils.isSMARTSstringValid(smartsString))
						filterComponentMap.put(smartsString, smartsString);
					else
						invalidEntries.add(smartsString);
				}
			}
			if(inputType.equals(CompoundIdFilterType.INCHI_KEY)) {
				
				for(String inchiKey : inputStrings) {
					
					if(ChemInfoUtils.isInchiKeyValid(inchiKey)) {
						
						if(filterType.equals(CompoundIdFilterType.INCHI_KEY))
							filterComponentMap.put(inchiKey, inchiKey);
						
						if(filterType.equals(CompoundIdFilterType.INCHI_KEY2D))
							filterComponentMap.put(inchiKey, inchiKey.substring(0, 14));
					}						
					else
						invalidEntries.add(inchiKey);
				}
			}
			if(inputType.equals(CompoundIdFilterType.INCHI_KEY2D)) {
				
				for(String inchiKey2D : inputStrings) {
					
					if(ChemInfoUtils.isInchiKey2DValid(inchiKey2D))
						filterComponentMap.put(inchiKey2D, inchiKey2D);
					else
						invalidEntries.add(inchiKey2D);
				}
			}
			String invalidEntriesMessage = null; 
			if(!invalidEntries.isEmpty()) {
				
				invalidEntriesMessage = "Invalid entries found:\n\n";
				if(invalidEntries.size() < 20)
					invalidEntriesMessage += StringUtils.join(invalidEntries, "\n");
				else {
					List<String> inv2print = invalidEntries.stream().limit(20).collect(Collectors.toList());
					invalidEntriesMessage += StringUtils.join(inv2print, "\n");
					invalidEntriesMessage += "\n\n" + (invalidEntries.size() - 20) + " more invalid entries ...";
				}
			}
			if(filterComponentMap.isEmpty()) {
				
				String message = "No valid input entries found!";
				if(invalidEntriesMessage != null)
					message += "\n\n" + invalidEntriesMessage;
				
				MessageDialog.showErrorMsg(message, filterComponentsTable);
				return null;
			}
			else{
				filterComponentsTable.setTableModelFromValuePairs(filterComponentMap);
				if(invalidEntriesMessage != null)
					MessageDialog.showWarningMsg(invalidEntriesMessage, filterComponentsTable);
			}
			return null;
		}
	}
	
	public CompoundIdFilterType getCompoundIdFilterType() {
		return (CompoundIdFilterType)filterTypeComboBox.getSelectedItem();
	}
	
	public CompoundIdFilterType getInputType() {
		return (CompoundIdFilterType)inputTypeComboBox.getSelectedItem();
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		
		preferences = prefs;
		baseDirectory =  new File(preferences.get(BASE_DIRECTORY, 
				MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void loadPreferences() {
		
		loadPreferences(
				Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if(e.getStateChange() == ItemEvent.SELECTED) {
			
			if(e.getSource().equals(inputTypeComboBox))
				setOutputOptionsBasedOnInput();
			
			revalidateFilterComponents();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void setOutputOptionsBasedOnInput() {

		CompoundIdFilterType inputType = getInputType();
		filterTypeComboBox.removeItemListener(this);
		if(inputType.equals(CompoundIdFilterType.COMPOUND_DATABASE_ID)
				|| inputType.equals(CompoundIdFilterType.COMPOUND_NAME)
				|| inputType.equals(CompoundIdFilterType.SMARTS)
				|| inputType.equals(CompoundIdFilterType.INCHI_KEY2D)) {
			filterTypeComboBox.setModel(new DefaultComboBoxModel<CompoundIdFilterType>(
					new CompoundIdFilterType[] {inputType}));
			filterTypeComboBox.setSelectedItem(inputType);
			filterTypeComboBox.setEnabled(false);
		}
		if(inputType.equals(CompoundIdFilterType.INCHI_KEY)) {
			
			filterTypeComboBox.setEnabled(true);
			filterTypeComboBox.setModel(new DefaultComboBoxModel<CompoundIdFilterType>(
					new CompoundIdFilterType[] {
							CompoundIdFilterType.INCHI_KEY,
							CompoundIdFilterType.INCHI_KEY2D,
							}));
			filterTypeComboBox.setSelectedItem(CompoundIdFilterType.INCHI_KEY);			
		}
		if(inputType.equals(CompoundIdFilterType.SMILES)) {
			
			filterTypeComboBox.setEnabled(true);
			filterTypeComboBox.setModel(new DefaultComboBoxModel<CompoundIdFilterType>(
					new CompoundIdFilterType[] {
							CompoundIdFilterType.SMILES,
							CompoundIdFilterType.INCHI_KEY,
							CompoundIdFilterType.INCHI_KEY2D,
							}));
			filterTypeComboBox.setSelectedItem(CompoundIdFilterType.SMILES);			
		}		
		filterTypeComboBox.addItemListener(this);
		matchAllSmartsCheckBox.setEnabled(
				getCompoundIdFilterType().equals(CompoundIdFilterType.SMARTS));
	}

	private void revalidateFilterComponents() {
		
		filterComponentsTable.clearTable();
		matchAllSmartsCheckBox.setEnabled(
				getCompoundIdFilterType().equals(CompoundIdFilterType.SMARTS));
		
		String inputPath = inputFileTextField.getText().trim();
		if(inputPath.isEmpty())
			return;
		
		File inputFile = new File(inputPath);
		if(!inputFile.exists())
			return;
		
		processInputFile(inputFile);
	}
}











