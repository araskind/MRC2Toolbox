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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.IonizationType;
import edu.umich.med.mrc2.datoolbox.data.MassAnalyzerType;
import edu.umich.med.mrc2.datoolbox.data.MsType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SoftwareType;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicSeparationType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataProcessingSoftware;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dextr.SoftwareSelectorDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DockableAcquisitionMethodDataPanel extends DefaultSingleCDockable 
	implements ItemListener, ActionListener, BackedByPreferences {

	private static final Icon componentIcon = GuiUtils.getIcon("editDataAcquisitionMethod", 16);
	
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.cefanalyzer.gui.DockableAcquisitionMethodDataPanel";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	
	private static final String BROWSE_COMMAND = "BROWSE_COMMAND";
	
	private JTextField methodNameTextField;
	private JTextField methodFileTextField;
	private JTextField softwareNameTextField;
	private JLabel idValueLabel;
	private JLabel dateCreatedLabel;
	private JLabel methodAuthorLabel;
	private JTextArea descriptionTextArea;
	private JComboBox polarityComboBox;
	private JComboBox msTypeComboBox;
	private JComboBox columnComboBox;
	private JComboBox ionizationTypeComboBox;
	private JComboBox massAnalyzerComboBox;
	private JComboBox separationTypeComboBox;
	private JButton btnBrowse;
//	private ImprovedFileChooser chooser;
	private File baseDirectory;
	private JDialog parentDialog;
	private DataAcquisitionMethod method;
	private SoftwareSelectorDialog softwareSelectorDialog;
	private DataProcessingSoftware software;
	
	public DockableAcquisitionMethodDataPanel(JDialog parentDialog) {

		super("DockableAcquisitionMethodDataPanel", componentIcon, 
				"Method data", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0,0));
		this.parentDialog = parentDialog;
		JPanel dataPanel = createDataPanel();
		add(dataPanel, BorderLayout.CENTER);
		loadPreferences();
		separationTypeComboBox.addItemListener(this);
	}
	
	@SuppressWarnings("unchecked")
	public void loadMethodData(DataAcquisitionMethod method) {
		
		this.method = method;
		separationTypeComboBox.removeItemListener(this);
		if(method == null) {

			polarityComboBox.setSelectedIndex(-1);
			msTypeComboBox.setSelectedIndex(-1);
			columnComboBox.setSelectedIndex(-1);
			ionizationTypeComboBox.setSelectedIndex(-1);
			massAnalyzerComboBox.setSelectedIndex(-1);
			separationTypeComboBox.setSelectedIndex(-1);
		}
		else {
			idValueLabel.setText(method.getId());

			if (method.getCreatedOn() != null)
				dateCreatedLabel.setText(MRC2ToolBoxConfiguration.getDateTimeFormat().format(method.getCreatedOn()));

			methodNameTextField.setText(method.getName());
			descriptionTextArea.setText(method.getDescription());

			if(method.getCreatedBy() != null)
				methodAuthorLabel.setText(method.getCreatedBy().getInfo());

			polarityComboBox.setSelectedItem(method.getPolarity());
			msTypeComboBox.setSelectedItem(method.getMsType());
			ionizationTypeComboBox.setSelectedItem(method.getIonizationType());
			massAnalyzerComboBox.setSelectedItem(method.getMassAnalyzerType());
			separationTypeComboBox.setSelectedItem(method.getSeparationType());

			Collection<LIMSChromatographicColumn> columnSet =
					IDTDataCache.getChromatographicColumns().stream().
					filter(c -> c.getSeparationType().equals(method.getSeparationType())).
					collect(Collectors.toList());

			columnComboBox.setModel(
					new SortedComboBoxModel<LIMSChromatographicColumn>(columnSet));

			columnComboBox.setSelectedItem(method.getColumn());
			
			software  = method.getSoftware();
			if(method.getSoftware() != null)
				softwareNameTextField.setText(method.getSoftware().getName());
		}
		loadPreferences();
		//initChooser();
		separationTypeComboBox.addItemListener(this);
	}
	
	@SuppressWarnings("unchecked")
	public void clearPanel() {
		
		method = null;	
		polarityComboBox.setSelectedIndex(-1);
		msTypeComboBox.setSelectedIndex(-1);
		
		columnComboBox.setModel(
				new SortedComboBoxModel<LIMSChromatographicColumn>(
						IDTDataCache.getChromatographicColumns()));
		columnComboBox.setSelectedIndex(-1);
		
		ionizationTypeComboBox.setSelectedIndex(-1);
		massAnalyzerComboBox.setSelectedIndex(-1);
		separationTypeComboBox.setSelectedIndex(-1);
		separationTypeComboBox.removeItemListener(this);
		separationTypeComboBox.addItemListener(this);		
		idValueLabel.setText("");
		dateCreatedLabel.setText("");
		methodNameTextField.setText("");
		descriptionTextArea.setText("");
		methodAuthorLabel.setText("");
		softwareNameTextField.setText("");
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private JPanel createDataPanel() {

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 137, 82, 144, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{1.0, 1.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);

		JLabel lblId = new JLabel("ID");
		lblId.setForeground(Color.BLUE);
		lblId.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.anchor = GridBagConstraints.EAST;
		gbc_lblId.insets = new Insets(0, 0, 5, 5);
		gbc_lblId.gridx = 0;
		gbc_lblId.gridy = 0;
		dataPanel.add(lblId, gbc_lblId);

		idValueLabel = new JLabel("");
		GridBagConstraints gbc_idValueLabel = new GridBagConstraints();
		gbc_idValueLabel.anchor = GridBagConstraints.WEST;
		gbc_idValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_idValueLabel.gridx = 1;
		gbc_idValueLabel.gridy = 0;
		dataPanel.add(idValueLabel, gbc_idValueLabel);

		JLabel lblCreated = new JLabel("Created on");
		GridBagConstraints gbc_lblCreated = new GridBagConstraints();
		gbc_lblCreated.anchor = GridBagConstraints.EAST;
		gbc_lblCreated.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreated.gridx = 2;
		gbc_lblCreated.gridy = 0;
		dataPanel.add(lblCreated, gbc_lblCreated);

		dateCreatedLabel = new JLabel("");
		GridBagConstraints gbc_dateCreatedLabel = new GridBagConstraints();
		gbc_dateCreatedLabel.gridwidth = 2;
		gbc_dateCreatedLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_dateCreatedLabel.insets = new Insets(0, 0, 5, 0);
		gbc_dateCreatedLabel.gridx = 3;
		gbc_dateCreatedLabel.gridy = 0;
		dataPanel.add(dateCreatedLabel, gbc_dateCreatedLabel);

		JLabel lblCreatedBy = new JLabel("Created by");
		GridBagConstraints gbc_lblCreatedBy = new GridBagConstraints();
		gbc_lblCreatedBy.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblCreatedBy.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreatedBy.gridx = 0;
		gbc_lblCreatedBy.gridy = 1;
		dataPanel.add(lblCreatedBy, gbc_lblCreatedBy);

		methodAuthorLabel = new JLabel("");
		GridBagConstraints gbc_methodAuthorLabel = new GridBagConstraints();
		gbc_methodAuthorLabel.anchor = GridBagConstraints.NORTH;
		gbc_methodAuthorLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_methodAuthorLabel.gridwidth = 4;
		gbc_methodAuthorLabel.insets = new Insets(0, 0, 5, 0);
		gbc_methodAuthorLabel.gridx = 1;
		gbc_methodAuthorLabel.gridy = 1;
		dataPanel.add(methodAuthorLabel, gbc_methodAuthorLabel);

		btnBrowse = new JButton("Select method file");
		btnBrowse.setActionCommand(BROWSE_COMMAND); // TODO
		btnBrowse.addActionListener(this);

		methodFileTextField = new JTextField();
		methodFileTextField.setEditable(false);
		GridBagConstraints gbc_methodFileTextField = new GridBagConstraints();
		gbc_methodFileTextField.gridwidth = 4;
		gbc_methodFileTextField.insets = new Insets(0, 0, 5, 5);
		gbc_methodFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_methodFileTextField.gridx = 0;
		gbc_methodFileTextField.gridy = 2;
		dataPanel.add(methodFileTextField, gbc_methodFileTextField);
		methodFileTextField.setColumns(10);
		GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
		gbc_btnBrowse.insets = new Insets(0, 0, 5, 0);
		gbc_btnBrowse.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnBrowse.gridx = 4;
		gbc_btnBrowse.gridy = 2;
		dataPanel.add(btnBrowse, gbc_btnBrowse);

		JLabel lblName = new JLabel("Name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 3;
		dataPanel.add(lblName, gbc_lblName);

		methodNameTextField = new JTextField();
		methodNameTextField.setEditable(false);
		GridBagConstraints gbc_methodNameTextField = new GridBagConstraints();
		gbc_methodNameTextField.gridwidth = 4;
		gbc_methodNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_methodNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_methodNameTextField.gridx = 1;
		gbc_methodNameTextField.gridy = 3;
		dataPanel.add(methodNameTextField, gbc_methodNameTextField);
		methodNameTextField.setColumns(10);

		JLabel lblDescription = new JLabel("Description");
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.gridwidth = 2;
		gbc_lblDescription.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 4;
		dataPanel.add(lblDescription, gbc_lblDescription);

		descriptionTextArea = new JTextArea();
		descriptionTextArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		descriptionTextArea.setRows(3);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridwidth = 5;
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 5;
		dataPanel.add(descriptionTextArea, gbc_textArea);

		JLabel lblPolarity = new JLabel("Polarity");
		GridBagConstraints gbc_lblPolarity = new GridBagConstraints();
		gbc_lblPolarity.anchor = GridBagConstraints.EAST;
		gbc_lblPolarity.insets = new Insets(0, 0, 5, 5);
		gbc_lblPolarity.gridx = 0;
		gbc_lblPolarity.gridy = 6;
		dataPanel.add(lblPolarity, gbc_lblPolarity);

		polarityComboBox =
			new JComboBox<Polarity>(new DefaultComboBoxModel<Polarity>(
					new Polarity[] {Polarity.Negative, Polarity.Positive}));
		GridBagConstraints gbc_polarityComboBox = new GridBagConstraints();
		gbc_polarityComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_polarityComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_polarityComboBox.gridx = 1;
		gbc_polarityComboBox.gridy = 6;
		dataPanel.add(polarityComboBox, gbc_polarityComboBox);

		JLabel lblMsType = new JLabel("MS type");
		GridBagConstraints gbc_lblMsType = new GridBagConstraints();
		gbc_lblMsType.anchor = GridBagConstraints.EAST;
		gbc_lblMsType.insets = new Insets(0, 0, 5, 5);
		gbc_lblMsType.gridx = 2;
		gbc_lblMsType.gridy = 6;
		dataPanel.add(lblMsType, gbc_lblMsType);

		msTypeComboBox = new JComboBox(
				new SortedComboBoxModel<MsType>(IDTDataCache.getMsTypes()));
		GridBagConstraints gbc_msTypeComboBox = new GridBagConstraints();
		gbc_msTypeComboBox.gridwidth = 2;
		gbc_msTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_msTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_msTypeComboBox.gridx = 3;
		gbc_msTypeComboBox.gridy = 6;
		dataPanel.add(msTypeComboBox, gbc_msTypeComboBox);

		JLabel lblSeparation = new JLabel("Separation");
		GridBagConstraints gbc_lblSeparation = new GridBagConstraints();
		gbc_lblSeparation.anchor = GridBagConstraints.EAST;
		gbc_lblSeparation.insets = new Insets(0, 0, 5, 5);
		gbc_lblSeparation.gridx = 0;
		gbc_lblSeparation.gridy = 7;
		dataPanel.add(lblSeparation, gbc_lblSeparation);

		separationTypeComboBox = new JComboBox(
				new SortedComboBoxModel<ChromatographicSeparationType>(
						IDTDataCache.getChromatographicSeparationTypes()));
		separationTypeComboBox.addItemListener(this);
		GridBagConstraints gbc_separationTypeomboBox = new GridBagConstraints();
		gbc_separationTypeomboBox.insets = new Insets(0, 0, 5, 5);
		gbc_separationTypeomboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_separationTypeomboBox.gridx = 1;
		gbc_separationTypeomboBox.gridy = 7;
		dataPanel.add(separationTypeComboBox, gbc_separationTypeomboBox);

		JLabel lblColumn = new JLabel("Column");
		GridBagConstraints gbc_lblColumn = new GridBagConstraints();
		gbc_lblColumn.anchor = GridBagConstraints.EAST;
		gbc_lblColumn.insets = new Insets(0, 0, 5, 5);
		gbc_lblColumn.gridx = 2;
		gbc_lblColumn.gridy = 7;
		dataPanel.add(lblColumn, gbc_lblColumn);

		columnComboBox = new JComboBox(
				new SortedComboBoxModel<LIMSChromatographicColumn>(
						IDTDataCache.getChromatographicColumns()));
		GridBagConstraints gbc_columnComboBox = new GridBagConstraints();
		gbc_columnComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_columnComboBox.gridwidth = 2;
		gbc_columnComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_columnComboBox.gridx = 3;
		gbc_columnComboBox.gridy = 7;
		dataPanel.add(columnComboBox, gbc_columnComboBox);

		JLabel lblIonization = new JLabel("Ionization");
		GridBagConstraints gbc_lblIonization = new GridBagConstraints();
		gbc_lblIonization.anchor = GridBagConstraints.EAST;
		gbc_lblIonization.insets = new Insets(0, 0, 5, 5);
		gbc_lblIonization.gridx = 0;
		gbc_lblIonization.gridy = 8;
		dataPanel.add(lblIonization, gbc_lblIonization);

		ionizationTypeComboBox =
				new JComboBox(new SortedComboBoxModel<IonizationType>(IDTDataCache.getIonizationTypes()));
		GridBagConstraints gbc_ionizationTypeComboBox = new GridBagConstraints();
		gbc_ionizationTypeComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_ionizationTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_ionizationTypeComboBox.gridx = 1;
		gbc_ionizationTypeComboBox.gridy = 8;
		dataPanel.add(ionizationTypeComboBox, gbc_ionizationTypeComboBox);

		JLabel lblMassAnalyzer = new JLabel("Mass analyzer");
		GridBagConstraints gbc_lblMassAnalyzer = new GridBagConstraints();
		gbc_lblMassAnalyzer.anchor = GridBagConstraints.EAST;
		gbc_lblMassAnalyzer.insets = new Insets(0, 0, 5, 5);
		gbc_lblMassAnalyzer.gridx = 2;
		gbc_lblMassAnalyzer.gridy = 8;
		dataPanel.add(lblMassAnalyzer, gbc_lblMassAnalyzer);

		massAnalyzerComboBox =
				new JComboBox(new SortedComboBoxModel<MassAnalyzerType>(IDTDataCache.getMassAnalyzerTypes()));
		GridBagConstraints gbc_massAnalyzerComboBox = new GridBagConstraints();
		gbc_massAnalyzerComboBox.gridwidth = 2;
		gbc_massAnalyzerComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_massAnalyzerComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_massAnalyzerComboBox.gridx = 3;
		gbc_massAnalyzerComboBox.gridy = 8;
		dataPanel.add(massAnalyzerComboBox, gbc_massAnalyzerComboBox);
		
		softwareNameTextField = new JTextField();
		softwareNameTextField.setEditable(false);
		GridBagConstraints gbc_softwareNameTextField = new GridBagConstraints();
		gbc_softwareNameTextField.gridwidth = 4;
		gbc_softwareNameTextField.insets = new Insets(0, 0, 5, 5);
		gbc_softwareNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_softwareNameTextField.gridx = 0;
		gbc_softwareNameTextField.gridy = 9;
		dataPanel.add(softwareNameTextField, gbc_softwareNameTextField);
		softwareNameTextField.setColumns(10);
		
		JButton selectSoftButton = new JButton("Select software");
		selectSoftButton.setActionCommand(
				MainActionCommands.SHOW_SOFTWARE_SELECTOR_COMMAND.getName());		
		selectSoftButton.addActionListener(this);
		GridBagConstraints gbc_selectSoftButton = new GridBagConstraints();
		gbc_selectSoftButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_selectSoftButton.insets = new Insets(0, 0, 5, 0);
		gbc_selectSoftButton.gridx = 4;
		gbc_selectSoftButton.gridy = 9;
		dataPanel.add(selectSoftButton, gbc_selectSoftButton);
		
		return dataPanel;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.SHOW_SOFTWARE_SELECTOR_COMMAND.getName()))
			showSoftwareSelector();
		
		if(e.getActionCommand().equals(MainActionCommands.SELECT_SOFTWARE_COMMAND.getName()))
			selectSoftware();
		
		if(e.getActionCommand().equals(BROWSE_COMMAND))
			selectMethodFile();
	}
	
	private void selectMethodFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.FilesAndDirectories);
		fc.setTitle("Select acquisition method file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File methodFile = fc.getSelectedFile();
			baseDirectory = methodFile.getParentFile();
			methodFileTextField.setText(methodFile.getAbsolutePath());
			if(method == null) {
				methodNameTextField.setText(methodFile.getName());
				descriptionTextArea.setText(methodFile.getName());
			}
			savePreferences();
		}
	}
	
	public void setMethodFile(File methodFile) {

		baseDirectory = methodFile.getParentFile();
		methodFileTextField.setText(methodFile.getAbsolutePath());
		if(method == null) {
			methodNameTextField.setText(methodFile.getName());
			descriptionTextArea.setText(methodFile.getName());
		}
		savePreferences();
	}
	
	private void showSoftwareSelector() {

		softwareSelectorDialog = new SoftwareSelectorDialog(this, SoftwareType.INSTRUMENT_CONTROL);
		softwareSelectorDialog.setLocationRelativeTo(this.getContentPane());
		softwareSelectorDialog.setVisible(true);
	}
	
	private void selectSoftware() {
		
		if(softwareSelectorDialog.getSelectedSoftware() != null) {
			
			software = softwareSelectorDialog.getSelectedSoftware();
			softwareNameTextField.setText(software.getName());
			softwareSelectorDialog.dispose();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED) {

			if (e.getSource().equals(separationTypeComboBox)) {

				ChromatographicSeparationType sType = getChromatographicSeparationType();
				Collection<LIMSChromatographicColumn> columnSet =
						IDTDataCache.getChromatographicColumns().stream().
						filter(c -> c.getSeparationType().equals(sType)).
						collect(Collectors.toList());

				columnComboBox.setModel(
						new SortedComboBoxModel<LIMSChromatographicColumn>(columnSet));
			}
		}
	}
	
	public String getMethodName() {
		return methodNameTextField.getText().trim();
	}

	public String getMethodDescription() {
		return descriptionTextArea.getText().trim();
	}

	public Polarity getMethodPolarity() {
		return (Polarity) polarityComboBox.getSelectedItem();
	}

	public MsType getMethodMsType() {
		return (MsType) msTypeComboBox.getSelectedItem();
	}

	public LIMSChromatographicColumn getColumn() {
		return (LIMSChromatographicColumn)columnComboBox.getSelectedItem();
	}

	public IonizationType getIonizationType() {
		return (IonizationType)ionizationTypeComboBox.getSelectedItem();
	}

	public MassAnalyzerType getMassAnalyzerType() {
		return (MassAnalyzerType)massAnalyzerComboBox.getSelectedItem();
	}

	public ChromatographicSeparationType getChromatographicSeparationType() {
		return (ChromatographicSeparationType)separationTypeComboBox.getSelectedItem();
	}

	public File getMethodFile() {

		if(methodFileTextField.getText().trim().isEmpty())
			return null;

		return new File(methodFileTextField.getText().trim());
	}
	
	public Collection<String>validateMethodData(){
		
		Collection<String>errors = new ArrayList<String>();
		if(getMethodName().isEmpty()) 
			errors.add("Method name can not be empty.");
		
		if(method == null && getMethodFile() == null) 
			errors.add("Method file should be specified for new method definition.");
		
		DataAcquisitionMethod existingFileMethod = null;
		
		//	Check if method file was already used
		if(getMethodFile() != null) {
			existingFileMethod = IDTDataCache.getAcquisitionMethodByName(getMethodFile().getName());
			if(existingFileMethod != null)
				errors.add("Method file \"" + getMethodFile().getName() + "\" is already in the database.");
		}
		//	Check if method name was already used
		if(!getMethodName().isEmpty()) {
			
			if(method == null) {	//	New method 
				existingFileMethod = IDTDataCache.getAcquisitionMethodByName(getMethodName());
				if(existingFileMethod != null)
					errors.add("Method \"" + getMethodName() + "\" is already in the database.");
			}
			else {
				String newName = getMethodName();
				existingFileMethod = IDTDataCache.getAcquisitionMethods().stream().
					filter(m -> !m.equals(method)).
					filter(m -> m.getName().equals(newName)).
					findFirst().orElse(null);
				if(existingFileMethod != null)
					errors.add("Another method named \"" + newName + "\" is already in the database.");
			}
		}
		if(getMethodPolarity() == null)
			errors.add("Method polarity should be specified.");

		if(getMethodMsType() == null)
			errors.add("Method MS type should be specified.");

		if(getIonizationType() == null)
			errors.add("Ionization type should be specified.");

		if(getMassAnalyzerType() == null)
			errors.add("Mass analyzer type should be specified.");

		if(getChromatographicSeparationType() == null)
			errors.add("Chromatographic separation type should be specified.");
		
		if(software == null)
			errors.add("Software should be specified.");
		
		return errors;
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =
			new File(preferences.get(BASE_DIRECTORY,
					MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

	public DataProcessingSoftware getSoftware() {
		return software;
	}
}
