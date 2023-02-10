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

package edu.umich.med.mrc2.datoolbox.gui.io;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.jcs3.access.exception.InvalidArgumentException;
import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.DataExportFields;
import edu.umich.med.mrc2.datoolbox.data.enums.GlobalDefaults;
import edu.umich.med.mrc2.datoolbox.data.enums.MissingExportType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.DataExportTask;

public class DataExportDialog extends JDialog 
	implements ActionListener, TaskListener, ItemListener, BackedByPreferences {

	/**
	 *
	 */
	private static final long serialVersionUID = 984393319681080424L;
	private static final Icon exportIcon = GuiUtils.getIcon("export", 32);
	private static final Icon stopIcon = GuiUtils.getIcon("stopSign", 64);
	
	public static final String EXPORT_TYPE = "EXPORT_TYPE";	
	public static final String EXPORT_MISSING_TYPE = "EXPORT_MISSING_TYPE";	
	public static final String SAMPLE_NAMING_FIELD = "SAMPLE_NAMING_FIELD";	
	public static final String ENABLE_FILTERS = "ENABLE_FILTERS";
	public static final String MAX_RSD = "MAX_RSD";
	public static final String MIN_FREQUENCY = "MIN_FREQUENCY";	
	public static final String EXPORT_MANIFEST = "EXPORT_MANIFEST";
	
	private static final MainActionCommands[] exportTypes = new MainActionCommands[] {
			MainActionCommands.EXPORT_RESULTS_4R_COMMAND,
			MainActionCommands.EXPORT_RESULTS_4MPP_COMMAND,
			MainActionCommands.EXPORT_RESULTS_4BINNER_COMMAND,
			MainActionCommands.EXPORT_RESULTS_FOR_METABOLOMICS_WORKBENCH_COMMAND,
			MainActionCommands.EXPORT_RESULTS_4METSCAPE_COMMAND,
			MainActionCommands.EXPORT_DUPLICATES_COMMAND,
			MainActionCommands.EXPORT_MZRT_STATISTICS_COMMAND,
		};
	private JComboBox exportTypeComboBox;
	private JComboBox<MissingExportType> missingTypeComboBox;
	private JSpinner minFrequencySpinner;
	private JFormattedTextField maxRsdTextField;
	private File baseDirectory;
	private JCheckBox enableFiltersCheckBox;
	private JComboBox namingComboBox;
	private FileNameExtensionFilter txtFilter;
	private JCheckBox exportManifestCheckBox;
	private JTextField resultsFileTextField;
	private JCheckBox replaceSpecCharsCheckBox;

	public DataExportDialog() {		
		this(MainActionCommands.EXPORT_RESULTS_4BINNER_COMMAND);
	}
	
	public DataExportDialog(MainActionCommands exportType) {

		super();
		
		if(!Arrays.asList(exportTypes).contains(exportType)) {
			throw new InvalidArgumentException("Invalid export type \"" + exportType.getName() +"\"");
		}
		setPreferredSize(new Dimension(800, 300));
		setIconImage(((ImageIcon) exportIcon).getImage());
		DataAnalysisProject currentProject = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		DataPipeline pipeline = currentProject.getActiveDataPipeline();
		String dsName = currentProject.getActiveFeatureSetForDataPipeline(pipeline).getName();
		setTitle("Export results for data pipeline " + pipeline.getName() + " (" + dsName + ")");

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(800, 300));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		getContentPane().add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 96, 114, 70, 199, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		exportTypeComboBox = new JComboBox<MainActionCommands>(
				new DefaultComboBoxModel<MainActionCommands>(exportTypes));
		exportTypeComboBox.addItemListener(this);

		JLabel exportTypeLabel = new JLabel("Export type");
		GridBagConstraints gbc_exportTypeLabel = new GridBagConstraints();
		gbc_exportTypeLabel.insets = new Insets(0, 0, 5, 5);
		gbc_exportTypeLabel.anchor = GridBagConstraints.EAST;
		gbc_exportTypeLabel.gridx = 0;
		gbc_exportTypeLabel.gridy = 0;
		panel.add(exportTypeLabel, gbc_exportTypeLabel);

		GridBagConstraints gbc_exportTypeComboBox = new GridBagConstraints();
		gbc_exportTypeComboBox.gridwidth = 2;
		gbc_exportTypeComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_exportTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_exportTypeComboBox.gridx = 1;
		gbc_exportTypeComboBox.gridy = 0;
		panel.add(exportTypeComboBox, gbc_exportTypeComboBox);
		
		exportManifestCheckBox = new JCheckBox("Export run manifest file");
		GridBagConstraints gbc_exportManifestCheckBox = new GridBagConstraints();
		gbc_exportManifestCheckBox.anchor = GridBagConstraints.WEST;
		gbc_exportManifestCheckBox.gridwidth = 3;
		gbc_exportManifestCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_exportManifestCheckBox.gridx = 3;
		gbc_exportManifestCheckBox.gridy = 0;
		panel.add(exportManifestCheckBox, gbc_exportManifestCheckBox);

		JLabel lblNaming = new JLabel("Naming by");
		GridBagConstraints gbc_lblNaming = new GridBagConstraints();
		gbc_lblNaming.anchor = GridBagConstraints.EAST;
		gbc_lblNaming.insets = new Insets(0, 0, 5, 5);
		gbc_lblNaming.gridx = 0;
		gbc_lblNaming.gridy = 1;
		panel.add(lblNaming, gbc_lblNaming);

		DefaultComboBoxModel<DataExportFields> namingModel = 
				new DefaultComboBoxModel<DataExportFields>(
						new DataExportFields[] {
							DataExportFields.SAMPLE_EXPORT_NAME,
							DataExportFields.SAMPLE_EXPORT_ID,
							DataExportFields.DATA_FILE_EXPORT 
						});

		namingComboBox = new JComboBox<DataExportFields>(namingModel);
		GridBagConstraints gbc_namingComboBox = new GridBagConstraints();
		gbc_namingComboBox.gridwidth = 2;
		gbc_namingComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_namingComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_namingComboBox.gridx = 1;
		gbc_namingComboBox.gridy = 1;
		panel.add(namingComboBox, gbc_namingComboBox);

		JLabel lblExportMissingValues = new JLabel("Export missing values");
		GridBagConstraints gbc_lblExportMissingValues = new GridBagConstraints();
		gbc_lblExportMissingValues.anchor = GridBagConstraints.EAST;
		gbc_lblExportMissingValues.insets = new Insets(0, 0, 5, 5);
		gbc_lblExportMissingValues.gridx = 0;
		gbc_lblExportMissingValues.gridy = 2;
		panel.add(lblExportMissingValues, gbc_lblExportMissingValues);

		missingTypeComboBox = new JComboBox<MissingExportType>();
		missingTypeComboBox.setModel(
				new DefaultComboBoxModel<MissingExportType>(MissingExportType.values()));
		missingTypeComboBox.setSelectedItem(MissingExportType.AS_MISSING);

		GridBagConstraints gbc_missingTypeComboBox = new GridBagConstraints();
		gbc_missingTypeComboBox.gridwidth = 2;
		gbc_missingTypeComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_missingTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_missingTypeComboBox.gridx = 1;
		gbc_missingTypeComboBox.gridy = 2;
		panel.add(missingTypeComboBox, gbc_missingTypeComboBox);

		enableFiltersCheckBox = new JCheckBox("Enable filters");
		GridBagConstraints gbc_enableFiltersCheckBox = new GridBagConstraints();
		gbc_enableFiltersCheckBox.anchor = GridBagConstraints.WEST;
		gbc_enableFiltersCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_enableFiltersCheckBox.gridx = 0;
		gbc_enableFiltersCheckBox.gridy = 3;
		panel.add(enableFiltersCheckBox, gbc_enableFiltersCheckBox);

		JLabel lblMinFrequency = new JLabel("Min frequency, %");
		GridBagConstraints gbc_lblMinFrequency = new GridBagConstraints();
		gbc_lblMinFrequency.anchor = GridBagConstraints.EAST;
		gbc_lblMinFrequency.insets = new Insets(0, 0, 5, 5);
		gbc_lblMinFrequency.gridx = 1;
		gbc_lblMinFrequency.gridy = 3;
		panel.add(lblMinFrequency, gbc_lblMinFrequency);

		minFrequencySpinner = new JSpinner();
		minFrequencySpinner.setSize(new Dimension(60, 20));
		minFrequencySpinner.setPreferredSize(new Dimension(60, 20));
		minFrequencySpinner.setModel(new SpinnerNumberModel(100, 0, 100, 1));
		GridBagConstraints gbc_minFrequencySpinner = new GridBagConstraints();
		gbc_minFrequencySpinner.anchor = GridBagConstraints.WEST;
		gbc_minFrequencySpinner.insets = new Insets(0, 0, 5, 5);
		gbc_minFrequencySpinner.gridx = 2;
		gbc_minFrequencySpinner.gridy = 3;
		panel.add(minFrequencySpinner, gbc_minFrequencySpinner);

		JLabel lblMaxRsd = new JLabel("Max RSD, %");
		GridBagConstraints gbc_lblMaxRsd = new GridBagConstraints();
		gbc_lblMaxRsd.anchor = GridBagConstraints.EAST;
		gbc_lblMaxRsd.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaxRsd.gridx = 3;
		gbc_lblMaxRsd.gridy = 3;
		panel.add(lblMaxRsd, gbc_lblMaxRsd);

		NumberFormat integerFieldFormatter = NumberFormat.getIntegerInstance();
		integerFieldFormatter.setGroupingUsed(false);
		
		maxRsdTextField = new JFormattedTextField(integerFieldFormatter);
		maxRsdTextField.setHorizontalAlignment(SwingConstants.RIGHT);
		maxRsdTextField.setPreferredSize(new Dimension(60, 20));
		maxRsdTextField.setSize(new Dimension(60, 20));
		maxRsdTextField.setText("25");
		GridBagConstraints gbc_maxRsdTextField = new GridBagConstraints();
		gbc_maxRsdTextField.anchor = GridBagConstraints.WEST;
		gbc_maxRsdTextField.insets = new Insets(0, 0, 5, 5);
		gbc_maxRsdTextField.gridx = 4;
		gbc_maxRsdTextField.gridy = 3;
		panel.add(maxRsdTextField, gbc_maxRsdTextField);
		
		replaceSpecCharsCheckBox = 
				new JCheckBox("Replace special characters with dash symbol");
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.anchor = GridBagConstraints.WEST;
		gbc_chckbxNewCheckBox.gridwidth = 2;
		gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxNewCheckBox.gridx = 0;
		gbc_chckbxNewCheckBox.gridy = 4;
		panel.add(replaceSpecCharsCheckBox, gbc_chckbxNewCheckBox);
		
		JLabel lblNewLabel = new JLabel("Export file:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 5;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		resultsFileTextField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 5;
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 6;
		panel.add(resultsFileTextField, gbc_textField);
		resultsFileTextField.setColumns(10);
		
		JButton browseButton = new JButton("Browse");
		browseButton.setActionCommand(
				MainActionCommands.SELECT_DATA_EXPORT_FILE_COMMAND.getName());
		browseButton.addActionListener(this);
		GridBagConstraints gbc_browseButton = new GridBagConstraints();
		gbc_browseButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_browseButton.gridx = 5;
		gbc_browseButton.gridy = 6;
		panel.add(browseButton, gbc_browseButton);

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(MainActionCommands.EXPORT_RESULTS_COMMAND.getName());
		btnSave.setActionCommand(MainActionCommands.EXPORT_RESULTS_COMMAND.getName());
		btnSave.addActionListener(this);
		buttonPanel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		loadPreferences();
		setBaseDirectory(currentProject.getExportsDirectory());
		exportTypeComboBox.setSelectedItem(exportType);
		pack();
	}
	
	@Override
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		if(command.equals(MainActionCommands.SELECT_DATA_EXPORT_FILE_COMMAND.getName())) 
			selectExportFile();
		
		if(command.equals(MainActionCommands.EXPORT_RESULTS_COMMAND.getName())) 
			exportData();
	}
	
	private void selectExportFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT");
		fc.setTitle("Export IDTracker data to text file:");
		fc.setMultiSelectionEnabled(false);
		fc.setSaveButtonText("Set output file");
		String defaultFileName = createExportFile(getExportType());
		fc.setDefaultFileName(defaultFileName);	
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
						
			File exportFile  = fc.getSelectedFile();
//			TODO check if necessary			
//			if(exportFile.exists()) {
//               int result = JOptionPane.showConfirmDialog(this,
//                		"File " + exportFile.getName() + " already exists, overwrite?",
//                		"Overwrite warning", JOptionPane.YES_NO_OPTION,
//                		JOptionPane.QUESTION_MESSAGE, stopIcon);
//               if(result != JOptionPane.YES_OPTION)
//            	   return;
//			}
			resultsFileTextField.setText(exportFile.getAbsolutePath());
			baseDirectory = exportFile.getParentFile();
		}
	}
	
	public boolean replaceSpecChars() {
		return replaceSpecCharsCheckBox.isSelected();
	}

	public void setBaseDirectory(File newBase) {
		
		baseDirectory = newBase;
		String fileName = createExportFile(getExportType());	
		String newFilePath = Paths.get(baseDirectory.getAbsolutePath(),fileName).
				toAbsolutePath().toString();
		resultsFileTextField.setText(newFilePath);
	}

	public void setExportType(MainActionCommands exportType) {
		exportTypeComboBox.setSelectedItem(exportType);
	}

	private void exportData() {
		
		Collection<String>errors = validateInput();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}	
		DataExportTask det = new DataExportTask(
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment(),
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getActiveDataPipeline(),
				getResultsFile(),
				getExportType(),
				getMissingExportType(),
				areFilteresEnabled(),
				getMaxRsd(),
				getMinFrequency(),
				getDataExportNamingField(),
				exportManifest(),
				replaceSpecChars());
		det.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(det);		
	}
	
	private Collection<String>validateInput(){
		
		Collection<String>errors = new ArrayList<String>();
		if(getResultsFile() == null)
			errors.add("Output file not specified");
		
		
		return errors;		
	}
	
	private String createExportFile(MainActionCommands type) {

		DataAnalysisProject currentProject = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();

		String typeString = "_DATA_";

		if (type.equals(MainActionCommands.EXPORT_RESULTS_4R_COMMAND))
			typeString = "_4R_";

		if (type.equals(MainActionCommands.EXPORT_RESULTS_4MPP_COMMAND))
			typeString = "_4MPP_";

		if (type.equals(MainActionCommands.EXPORT_RESULTS_4BINNER_COMMAND))
			typeString = "_4BINNER_";

		if (type.equals(MainActionCommands.EXPORT_RESULTS_4METSCAPE_COMMAND))
			typeString = "_4MetScape_";

		if (type.equals(MainActionCommands.EXPORT_DUPLICATES_COMMAND))
			typeString = "_DUPLICATES_";

		if (type.equals(MainActionCommands.EXPORT_RESULTS_FOR_METABOLOMICS_WORKBENCH_COMMAND))
			typeString = "_4MWB_";
		
		if (type.equals(MainActionCommands.EXPORT_MZRT_STATISTICS_COMMAND))
			typeString = "_FEATURE_STATS_";
		
		
		String timestamp = MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
		DataPipeline dataPipeline = currentProject.getActiveDataPipeline();
		String fileName = currentProject.getName();
		if(currentProject.getLimsExperiment() != null)
			fileName = currentProject.getLimsExperiment().getId();
		
		fileName +=  "_" + dataPipeline.getName();		
		MsFeatureSet fSet = currentProject.getActiveFeatureSetForDataPipeline(dataPipeline);
		if(!fSet.getName().equals(GlobalDefaults.ALL_FEATURES.getName()))
			fileName +=  "_" + currentProject.getActiveFeatureSetForDataPipeline(dataPipeline).getName();
		
		fileName +=  typeString + timestamp + ".txt";
		return fileName;
	}

	public MainActionCommands getExportType() {
		return (MainActionCommands) exportTypeComboBox.getSelectedItem();
	}

	public int getMaxPooledRsd() {
		return Integer.parseInt(maxRsdTextField.getText());
	}

	public int getMinPooledFrequency() {
		return (int) minFrequencySpinner.getValue();
	}
	
	private File getResultsFile() {
		
		String filePath = resultsFileTextField.getText().trim();
		if(filePath == null || filePath.isEmpty())
			return null;
		else
			return new File(filePath);
	}

	@Override
	public void itemStateChanged(ItemEvent event) {

		if (event.getStateChange() == ItemEvent.SELECTED) {			
			
			MainActionCommands exportType = (MainActionCommands) event.getItem();			
			if(exportType.equals(MainActionCommands.EXPORT_RESULTS_FOR_METABOLOMICS_WORKBENCH_COMMAND)) {
				namingComboBox.setSelectedItem(DataExportFields.SAMPLE_EXPORT_NAME);
				namingComboBox.setEnabled(false);
			}
			else {
				namingComboBox.setSelectedItem(DataExportFields.DATA_FILE_EXPORT);
				namingComboBox.setEnabled(true);
			}
			File rf = getResultsFile();
			if(rf == null)
				return;
			
			String fileName = createExportFile(exportType);	
			String newFilePath = Paths.get(rf.getParentFile().getAbsolutePath(),fileName).
					toAbsolutePath().toString();
			resultsFileTextField.setText(newFilePath);
		}
	}

	public DataExportFields getNamingField() {
		return (DataExportFields) namingComboBox.getSelectedItem();
	}

	public void setNamingField(DataExportFields field) {
		namingComboBox.setSelectedItem(field);
	}
	
	public MissingExportType getMissingExportType() {
		return (MissingExportType) missingTypeComboBox.getSelectedItem();
	}
	
	public DataExportFields getDataExportNamingField() {
		 return (DataExportFields) namingComboBox.getSelectedItem();
	}
	
	public boolean areFilteresEnabled() {
		return enableFiltersCheckBox.isSelected();
	}
	
	public boolean exportManifest() {
		return exportManifestCheckBox.isSelected();
	}
	
	public double getMaxRsd() {
		return Double.parseDouble(maxRsdTextField.getText()) / 100.0d;
	}
	
	public double getMinFrequency() {
		return ((Integer) minFrequencySpinner.getValue()).doubleValue() / 100.0d;
	}
	
	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getSource().getClass().equals(DataExportTask.class)) {

			if (e.getStatus() == TaskStatus.FINISHED) {

				((AbstractTask)e.getSource()).removeTaskListener(this);
				this.dispose();
			}
		}
		if (e.getStatus() == TaskStatus.ERROR || e.getStatus() == TaskStatus.CANCELED) {

			this.dispose();
			MainWindow.hideProgressDialog();
		}
	}
	
	@Override
	public void loadPreferences() {
		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		loadPreferences(prefs);
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		
		exportTypeComboBox.setSelectedItem(
				MainActionCommands.getCommandByName(
						preferences.get(EXPORT_TYPE, MainActionCommands.EXPORT_RESULTS_4BINNER_COMMAND.name())));
		exportManifestCheckBox.setSelected(preferences.getBoolean(EXPORT_MANIFEST, true));
		namingComboBox.setSelectedItem(
				DataExportFields.getDataExportFieldByName(
						preferences.get(SAMPLE_NAMING_FIELD, DataExportFields.DATA_FILE_EXPORT.name())));
		
		missingTypeComboBox.setSelectedItem(
				MissingExportType.getMissingExportTypeByName(
						preferences.get(EXPORT_MISSING_TYPE, MissingExportType.AS_MISSING.name())));
			
		enableFiltersCheckBox.setSelected(preferences.getBoolean(ENABLE_FILTERS, false));
		maxRsdTextField.setText(Integer.toString(preferences.getInt(MAX_RSD, 25)));
		minFrequencySpinner.setValue(preferences.getInt(MIN_FREQUENCY, 70));
	}

	@Override
	public void savePreferences() {

		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.put(EXPORT_TYPE, getExportType().name());
		prefs.putBoolean(EXPORT_MANIFEST, exportManifestCheckBox.isSelected());
		prefs.put(SAMPLE_NAMING_FIELD, getNamingField().name());
		prefs.put(EXPORT_MISSING_TYPE, getMissingExportType().name());
		prefs.putBoolean(ENABLE_FILTERS, enableFiltersCheckBox.isSelected());
		prefs.putInt(MAX_RSD, Integer.parseInt(maxRsdTextField.getText()));
		prefs.putInt(MIN_FREQUENCY, (Integer)minFrequencySpinner.getValue());
	}
	
	public static Collection<String>getExportTypes(){
		return Arrays.asList(exportTypes).stream().
				map(e -> e.getName()).collect(Collectors.toList());
	}
	
	public static MainActionCommands getExportTypeByName(String name) {
		
		for(MainActionCommands c : exportTypes) {
			
			if(c.getName().equals(name))
				return c;
		}
		return null;
	}
}








