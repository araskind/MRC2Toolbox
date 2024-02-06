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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.jcs3.access.exception.InvalidArgumentException;
import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.DataExportFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MissingExportType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.DataExportTask;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

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
	public static final String REPLACE_SPEC_CHARS = "REPLACE_SPEC_CHARS";
	
	
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
	private JComboBox namingComboBox;
	private FileNameExtensionFilter txtFilter;
	private JCheckBox exportManifestCheckBox;	
	private JCheckBox replaceSpecCharsCheckBox;
	
	private MsFeatureSet activeFeatureSet;
	private File baseDirectory;
	private File exportFile;
	
//	private JTextField resultsFileTextField;
//	private JCheckBox enableFiltersCheckBox;
//	private JSpinner minFrequencySpinner;
//	private JFormattedTextField maxRsdTextField;
	
	public DataExportDialog() {		
		this(MainActionCommands.EXPORT_RESULTS_4BINNER_COMMAND);
	}
	
	public DataExportDialog(MainActionCommands exportType) {

		super();
		
		if(!Arrays.asList(exportTypes).contains(exportType)) {
			throw new InvalidArgumentException(
					"Invalid export type \"" + exportType.getName() +"\"");
		}
		setPreferredSize(new Dimension(500, 250));
		setIconImage(((ImageIcon) exportIcon).getImage());
		DataAnalysisProject currentProject = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		DataPipeline pipeline = currentProject.getActiveDataPipeline();
		activeFeatureSet = currentProject.getActiveFeatureSetForDataPipeline(pipeline);
		setTitle("Export results for data pipeline " + 
				pipeline.getName() + " (" + activeFeatureSet.getName() + ")");

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(500, 250));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		getContentPane().add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 96, 114, 70, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
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
		
		replaceSpecCharsCheckBox = 
				new JCheckBox("Replace special characters in names with dash symbol");
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.anchor = GridBagConstraints.WEST;
		gbc_chckbxNewCheckBox.gridwidth = 4;
		gbc_chckbxNewCheckBox.gridx = 0;
		gbc_chckbxNewCheckBox.gridy = 4;
		panel.add(replaceSpecCharsCheckBox, gbc_chckbxNewCheckBox);

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
		fc.setSaveButtonText("Write export file");
		String defaultFileName = FIOUtils.createFileNameForDataExportType(getExportType());
		fc.setDefaultFileName(defaultFileName);	
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
						
			File exportFile  = fc.getSelectedFile();
//			resultsFileTextField.setText(exportFile.getAbsolutePath());
			baseDirectory = exportFile.getParentFile();
		}
	}
	
	public boolean replaceSpecChars() {
		return replaceSpecCharsCheckBox.isSelected();
	}

	public void setBaseDirectory(File newBase) {		
		baseDirectory = newBase;
//		String fileName = createExportFile(getExportType());	
//		String newFilePath = Paths.get(baseDirectory.getAbsolutePath(),fileName).
//				toAbsolutePath().toString();
//		resultsFileTextField.setText(newFilePath);
	}

	public void setExportType(MainActionCommands exportType) {
		exportTypeComboBox.setSelectedItem(exportType);
	}

	private void exportData() {
		
		exportFile = null;
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT");
		fc.setTitle("Export metabolomics experiment data to text file:");
		fc.setMultiSelectionEnabled(false);
		fc.setSaveButtonText("Export data");
		String defaultFileName = FIOUtils.createFileNameForDataExportType(getExportType());
		fc.setDefaultFileName(defaultFileName);	
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
						
			exportFile = fc.getSelectedFile();
			baseDirectory = exportFile.getParentFile();
		}	
		if(exportFile == null)
			return;
		
		Collection<String>errors = validateInput();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}			
		DataExportTask det = new DataExportTask(
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment(),
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getActiveDataPipeline(),
				exportFile,
				getExportType(),
				getMissingExportType(),
				false,
				1000.0d,
				0.0d,
				getDataExportNamingField(),
				exportManifest(),
				replaceSpecChars());
		det.setMsFeatureSet4export(activeFeatureSet.getFeatures());
		det.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(det);		
	}
	
	private Collection<String>validateInput(){
		
		Collection<String>errors = new ArrayList<String>();
//		if(getResultsFile() == null)
//			errors.add("Output file not specified");
		
		
		return errors;		
	}

	public MainActionCommands getExportType() {
		return (MainActionCommands) exportTypeComboBox.getSelectedItem();
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
	
	public boolean exportManifest() {
		return exportManifestCheckBox.isSelected();
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
				DataExportFields.getOptionByName(
						preferences.get(SAMPLE_NAMING_FIELD, DataExportFields.DATA_FILE_EXPORT.name())));
		
		missingTypeComboBox.setSelectedItem(
				MissingExportType.getOptionByName(
						preferences.get(EXPORT_MISSING_TYPE, MissingExportType.AS_MISSING.name())));
		
		replaceSpecCharsCheckBox.setSelected(preferences.getBoolean(REPLACE_SPEC_CHARS, false));
	}

	@Override
	public void savePreferences() {

		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.put(EXPORT_TYPE, getExportType().name());
		prefs.putBoolean(EXPORT_MANIFEST, exportManifestCheckBox.isSelected());
		prefs.put(SAMPLE_NAMING_FIELD, getNamingField().name());
		prefs.put(EXPORT_MISSING_TYPE, getMissingExportType().name());
		prefs.putBoolean(REPLACE_SPEC_CHARS, replaceSpecCharsCheckBox.isSelected());
		
//		prefs.putBoolean(ENABLE_FILTERS, enableFiltersCheckBox.isSelected());
//		prefs.putInt(MAX_RSD, Integer.parseInt(maxRsdTextField.getText()));
//		prefs.putInt(MIN_FREQUENCY, (Integer)minFrequencySpinner.getValue());
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








