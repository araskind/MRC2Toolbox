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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.binner;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotationCluster;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerAnnotationLookupDataSet;
import edu.umich.med.mrc2.datoolbox.database.idt.BinnerUtils;
import edu.umich.med.mrc2.datoolbox.gui.communication.FormChangeEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FormChangeListener;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.binner.BinnerAnnotationLookupDataSetSelectorDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.ExtractBinnerAnnotationsForMSMSFeatureClusteringTask;

public class BinnerAnnotationsImportPanel extends JPanel 
		implements ActionListener, TaskListener, BackedByPreferences {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3411264885987118578L;
	
	public static final String PREFS_NODE = 
			"edu.umich.med.mrc2.datoolbox.gui.idworks.BinnerAnnotationsImportPanel";
	private Preferences preferences;
	public static final String ANNOTATION_MERGE_MASS_ERROR = "ANNOTATION_MERGE_MASS_ERROR";
	public static final String ANNOTATION_MERGE_MASS_ERROR_TYPE = "ANNOTATION_MERGE_MASS_ERROR_TYPE";
	public static final String ANNOTATION_MERGE_RT_ERROR = "ANNOTATION_MERGE_RT_ERROR";

	private BinnerAnnotationClusterTable clustersTable;
	private File baseDirectory;
	private JTextField dataSetNameTextField;
	private JTextArea descriptionTextArea;
	private BinnerAnnotationLookupDataSet dataSet;
	private JButton fileOpenButton, dbOpenButton;
	private BinnerAnnotationLookupDataSetSelectorDialog 
				binnerAnnotationLookupDataSetSelectorDialog;
	private Set<FormChangeListener> changeListeners;

	private JFormattedTextField massErrorTextField;
	private JComboBox massErrorTypeComboBox;
	private JFormattedTextField rtErrorTextField;
	
	public BinnerAnnotationsImportPanel() {
		
		super(new BorderLayout(0,0));
		changeListeners = ConcurrentHashMap.newKeySet();
		
		clustersTable = new BinnerAnnotationClusterTable();
		add(new JScrollPane(clustersTable), BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10), 
				new CompoundBorder(new TitledBorder(
						new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
						"Binner annotations set properties", TitledBorder.LEADING, 
						TitledBorder.TOP, null, new Color(0, 0, 0)), 
						new EmptyBorder(10, 10, 10, 10))));
		add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{62, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel = new JLabel("Name ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		dataSetNameTextField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 6;
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		panel.add(dataSetNameTextField, gbc_textField);
		dataSetNameTextField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Description");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.gridwidth = 7;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		descriptionTextArea = new JTextArea();
		descriptionTextArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.setRows(2);
		descriptionTextArea.setWrapStyleWord(true);
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.gridwidth = 7;
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 2;
		panel.add(descriptionTextArea, gbc_textArea);
		
		JLabel lblNewLabel_2 = new JLabel("Annotation merge parameters");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.gridwidth = 7;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 0);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 3;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		JLabel lblNewLabel_3 = new JLabel('\u0394' + "M/Z");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 4;
		panel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		massErrorTextField = new JFormattedTextField(
				MRC2ToolBoxConfiguration.getPpmFormat());
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 0, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 4;
		panel.add(massErrorTextField, gbc_formattedTextField);
		
		massErrorTypeComboBox = new JComboBox<MassErrorType>(MassErrorType.values());
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 0, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 2;
		gbc_comboBox.gridy = 4;
		panel.add(massErrorTypeComboBox, gbc_comboBox);
		
		JLabel lblNewLabel_4 = new JLabel('\u0394' + "RT");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_4.gridx = 4;
		gbc_lblNewLabel_4.gridy = 4;
		panel.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		rtErrorTextField = new JFormattedTextField(
				MRC2ToolBoxConfiguration.getRtFormat());
		GridBagConstraints gbc_formattedTextField_1 = new GridBagConstraints();
		gbc_formattedTextField_1.insets = new Insets(0, 0, 0, 5);
		gbc_formattedTextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField_1.gridx = 5;
		gbc_formattedTextField_1.gridy = 4;
		panel.add(rtErrorTextField, gbc_formattedTextField_1);
		
		JLabel lblNewLabel_5 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_5.gridx = 6;
		gbc_lblNewLabel_5.gridy = 4;
		panel.add(lblNewLabel_5, gbc_lblNewLabel_5);
				
		JPanel fileImportPanel = new JPanel();
		fileImportPanel.setBorder(null);
		fileImportPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		
		fileOpenButton = new JButton(
				MainActionCommands.IMPORT_BINNER_ANNOTATIONS_FROM_FILE_COMMAND.getName());
		fileOpenButton.setActionCommand(
				MainActionCommands.IMPORT_BINNER_ANNOTATIONS_FROM_FILE_COMMAND.getName());
		fileOpenButton.addActionListener(this);
		fileImportPanel.add(fileOpenButton);
		
		dbOpenButton = new JButton(
				MainActionCommands.SELECT_BINNER_ANNOTATIONS_FROM_DATABASE_COMMAND.getName());
		dbOpenButton.setActionCommand(
				MainActionCommands.SELECT_BINNER_ANNOTATIONS_FROM_DATABASE_COMMAND.getName());
		dbOpenButton.addActionListener(this);
		fileImportPanel.add(dbOpenButton);
		
		add(fileImportPanel, BorderLayout.SOUTH);
		
		loadPreferences();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.IMPORT_BINNER_ANNOTATIONS_FROM_FILE_COMMAND.getName()))
			importBinnerAnnotationsFromFile();

		if(e.getActionCommand().equals(MainActionCommands.SELECT_BINNER_ANNOTATIONS_FROM_DATABASE_COMMAND.getName()))
			selectBinnerAnnotationsSetFromDatabase();
		
		if(e.getActionCommand().equals(MainActionCommands.LOAD_BINNER_ANNOTATIONS_FROM_DATABASE_COMMAND.getName()))
			loadBinnerAnnotationsSetFromDatabase();
	}
	
	public void disableLoadingFeaturesFromDatabase() {
		dbOpenButton.setEnabled(false);
	}
	
	public void disableLoadingFeatures() {
		
		fileOpenButton.setEnabled(false);
		dbOpenButton.setEnabled(false);		
		massErrorTextField.setEnabled(false);
		massErrorTextField.setEditable(false);
		massErrorTypeComboBox.setEnabled(false);
		rtErrorTextField.setEnabled(false);
		rtErrorTextField.setEditable(false);
	}	
	
	public String getDataSetName() {
		return dataSetNameTextField.getText().trim();
	}
	
	public String getDataSetDescription() {
		return descriptionTextArea.getText().trim();
	}
	
	public void setDataSetName(String name) {
		dataSetNameTextField.setText(name);
	}
	
	public void setDataSetDescription(String description) {
		descriptionTextArea.setText(description);
	}
	
	public void setBaseDirectory(File baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public File getBaseDirectory() {
		return baseDirectory;
	}
	
	public double getAnnotationMergeMzWindow() {	
		return Double.parseDouble(massErrorTextField.getText().trim());
	}
	
	public MassErrorType getMassErrorType() {
		return (MassErrorType)massErrorTypeComboBox.getSelectedItem();
	}
	
	public double getAnnotationMergeRtWindow() {	
		return Double.parseDouble(rtErrorTextField.getText().trim());
	}
	
	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(ExtractBinnerAnnotationsForMSMSFeatureClusteringTask.class))
				finalizeBinnerImportTask((ExtractBinnerAnnotationsForMSMSFeatureClusteringTask)e.getSource());			
		}		
	}

	private void finalizeBinnerImportTask(
			ExtractBinnerAnnotationsForMSMSFeatureClusteringTask task) {

		clustersTable.setTableModelFromBinnerAnnotationClusterCollection(
				task.getBinnerAnnotationClusters());
		fireFormChangeEvent(ParameterSetStatus.CHANGED);
	}
	
	private void importBinnerAnnotationsFromFile() {
		
		Collection<String>errors = validateParameters();
		if(!errors.isEmpty()){
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Binner files", "xlsx", "XLSX");
		fc.setTitle("Read annotations from Binner output file");
		fc.setOpenButtonText("Select Binner file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this))) {
			
			File inputFile = fc.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			parseBinnerFile(inputFile);
		}
	}

	private void loadBinnerAnnotationsSetFromDatabase() {
		
		BinnerAnnotationLookupDataSet dataSet = 
				binnerAnnotationLookupDataSetSelectorDialog.getSelectedDataSet();
		if(dataSet == null)
			return;		
		
		binnerAnnotationLookupDataSetSelectorDialog.dispose();		
		loadDataSet(dataSet);
		fireFormChangeEvent(ParameterSetStatus.CHANGED);
	}

	private void selectBinnerAnnotationsSetFromDatabase() {

		binnerAnnotationLookupDataSetSelectorDialog = 
				new BinnerAnnotationLookupDataSetSelectorDialog(this);
		binnerAnnotationLookupDataSetSelectorDialog.setLocationRelativeTo(this);
		binnerAnnotationLookupDataSetSelectorDialog.setVisible(true);
	}
	
	public void loadDataSet(BinnerAnnotationLookupDataSet dataSet) {
		
		if(dataSet == null)
			return;
		
		this.dataSet = dataSet;
		setDataSetName(dataSet.getName());
		setDataSetDescription(dataSet.getDescription());
		
		if(dataSet.getBinnerAnnotationClusters().isEmpty()) 
			getClustersForBinnerAnnotationLookupDataSet(dataSet);		
		else
			clustersTable.setTableModelFromBinnerAnnotationClusterCollection(
					dataSet.getBinnerAnnotationClusters());
	}
	
	private void getClustersForBinnerAnnotationLookupDataSet(BinnerAnnotationLookupDataSet dataSet) {

		GetClustersForBinnerAnnotationLookupDataSetTask task = 
				new GetClustersForBinnerAnnotationLookupDataSetTask(dataSet);
		IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
				"Getting Binner annotation clusters for lookup data set ...", this, task);
		idp.setLocationRelativeTo(this);
		idp.setVisible(true);
	}
	
	class GetClustersForBinnerAnnotationLookupDataSetTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		private BinnerAnnotationLookupDataSet dataSet;

		public GetClustersForBinnerAnnotationLookupDataSetTask(
				BinnerAnnotationLookupDataSet dataSet) {
			this.dataSet = dataSet;
		}

		@Override
		public Void doInBackground() {

			try {
				BinnerUtils.getClustersForBinnerAnnotationLookupDataSet(dataSet);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		public void done() {
			
			clustersTable.setTableModelFromBinnerAnnotationClusterCollection(
					dataSet.getBinnerAnnotationClusters());
			super.done();
		}
	}
		
	private void parseBinnerFile(File inputFile) {		

		ExtractBinnerAnnotationsForMSMSFeatureClusteringTask task = 
				new ExtractBinnerAnnotationsForMSMSFeatureClusteringTask(
						inputFile,
						getAnnotationMergeMzWindow(),
						getMassErrorType(),
						getAnnotationMergeRtWindow());
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);		
	}

	public Collection<BinnerAnnotationCluster>getSelectedClusters(){
		return clustersTable.getSelectedClusters();
	}
	
	public Collection<BinnerAnnotationCluster>getAllClusters(){
		return clustersTable.getAllClusters();
	}

	public BinnerAnnotationLookupDataSet getDataSet() {
		return dataSet;
	}
	
	public void addFormChangeListener(FormChangeListener listener) {
		changeListeners.add(listener);
	}
	
	public void removeFormChangeListener(FormChangeListener listener) {
		changeListeners.remove(listener);
	}

	public void fireFormChangeEvent(ParameterSetStatus newStatus) {

		FormChangeEvent event = new FormChangeEvent(this, newStatus);
		changeListeners.stream().forEach(l -> ((FormChangeListener) l).
				formDataChanged(event));
	}
	
	public void clearPanel() {
		clustersTable.clearTable();
		dataSetNameTextField.setText("");
		descriptionTextArea.setText("");
		dataSet = null;
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		
		preferences = prefs;
		
		massErrorTextField.setText(
				Double.toString(preferences.getDouble(ANNOTATION_MERGE_MASS_ERROR, 20.0d)));
		massErrorTypeComboBox.setSelectedItem(
				MassErrorType.getTypeByName(preferences.get(ANNOTATION_MERGE_MASS_ERROR_TYPE, MassErrorType.ppm.name())));
		rtErrorTextField.setText(
				Double.toString(preferences.getDouble(ANNOTATION_MERGE_RT_ERROR, 0.02d)));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		
		if(!validateParameters().isEmpty())
			return;
		
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.putDouble(ANNOTATION_MERGE_MASS_ERROR, getAnnotationMergeMzWindow());
		if(getMassErrorType() != null)
			preferences.put(ANNOTATION_MERGE_MASS_ERROR_TYPE, getMassErrorType().name());
		
		preferences.putDouble(ANNOTATION_MERGE_RT_ERROR, getAnnotationMergeRtWindow());
	}
	
	public Collection<String>validateParameters(){
		
		Collection<String>errors = new ArrayList<String>();
		if(getAnnotationMergeMzWindow() == 0.0d)
			errors.add("M/Z window for annotation merging must be > 0");
		
		if(getMassErrorType() == null)
			errors.add("M/Z error type must be specified");
			
		if(getAnnotationMergeRtWindow() == 0.0d)
			errors.add("RT window for annotation merging must be > 0");
				
		return errors;
	}
}
