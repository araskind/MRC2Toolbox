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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.swing.JButton;
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

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupDataSet;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureLookupDataSetUtils;
import edu.umich.med.mrc2.datoolbox.gui.communication.FormChangeEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FormChangeListener;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.lookup.FeatureLookupListSelectorDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.byexp.MinimalMSOneFeatureTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
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
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.ImportMinimalMSOneFeaturesFromCefTask;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public class FeatureListImportPanel extends JPanel implements ActionListener, TaskListener {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3411264885987118578L;

	private MinimalMSOneFeatureTable featureTable;
	private File baseDirectory;
	private JTextField dataSetNameTextField;
	private JTextArea descriptionTextArea;
	private FeatureLookupDataSet dataSet;
	private JButton btnNewButton, dbOpenButton;
	private FeatureLookupListSelectorDialog featureLookupListSelectorDialog;
	private Set<FormChangeListener> changeListeners;
	
	public FeatureListImportPanel() {
		
		super(new BorderLayout(0,0));
		changeListeners = ConcurrentHashMap.newKeySet();
		
		featureTable = new MinimalMSOneFeatureTable();
		add(new JScrollPane(featureTable), BorderLayout.CENTER);
		
		JPanel fileImportPanel = new JPanel();
		fileImportPanel.setBorder(null);
		
		btnNewButton = new JButton(
				MainActionCommands.IMPORT_LOOKUP_FEATURE_LIST_FROM_FILE_COMMAND.getName());
		btnNewButton.setActionCommand(MainActionCommands.IMPORT_LOOKUP_FEATURE_LIST_FROM_FILE_COMMAND.getName());
		btnNewButton.addActionListener(this);
		fileImportPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		
		dbOpenButton = 
				new JButton(MainActionCommands.SELECT_LOOKUP_FEATURE_LIST_FROM_DATABASE_COMMAND.getName());
		dbOpenButton.setActionCommand(MainActionCommands.SELECT_LOOKUP_FEATURE_LIST_FROM_DATABASE_COMMAND.getName());
		dbOpenButton.addActionListener(this);
		fileImportPanel.add(dbOpenButton);
		fileImportPanel.add(btnNewButton);
		add(fileImportPanel, BorderLayout.SOUTH);
		
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10), 
				new CompoundBorder(new TitledBorder(
						new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
						"Feature set properties", TitledBorder.LEADING, 
						TitledBorder.TOP, null, new Color(0, 0, 0)), 
						new EmptyBorder(10, 10, 10, 10))));
		add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
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
		gbc_textField.insets = new Insets(0, 0, 5, 0);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		panel.add(dataSetNameTextField, gbc_textField);
		dataSetNameTextField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Description");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.gridwidth = 2;
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
		gbc_textArea.gridwidth = 2;
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 2;
		panel.add(descriptionTextArea, gbc_textArea);
	}
	
	public void disableLoadingFeaturesFromDatabase() {
		dbOpenButton.setEnabled(false);
	}
	
	public void disableLoadingFeatures() {
		btnNewButton.setEnabled(false);
		dbOpenButton.setEnabled(false);
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
	
	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(ImportMinimalMSOneFeaturesFromCefTask.class))
				finalizeCefImportTask((ImportMinimalMSOneFeaturesFromCefTask)e.getSource());		
		}		
	}

	private void finalizeCefImportTask(ImportMinimalMSOneFeaturesFromCefTask task) {

		Collection<MinimalMSOneFeature>features = task.getMinFeatures(); 
		if(features != null)
			featureTable.setTableModelFromFeatureCollection(features);
		
		fireFormChangeEvent(ParameterSetStatus.CHANGED);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.IMPORT_LOOKUP_FEATURE_LIST_FROM_FILE_COMMAND.getName()))
			importLookupFeatureListFromFile();

		if(e.getActionCommand().equals(MainActionCommands.SELECT_LOOKUP_FEATURE_LIST_FROM_DATABASE_COMMAND.getName()))
			selectLookupFeatureListFromDatabase();
		
		if(e.getActionCommand().equals(MainActionCommands.LOAD_LOOKUP_FEATURE_LIST_FROM_DATABASE_COMMAND.getName()))
			loadLookupFeatureListFromDatabase();
	}
	
	private void importLookupFeatureListFromFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files (TAB-separated)", "txt", "TXT", "tsv", "TSV");
		fc.addFilter("Comma-separated text files", "csv", "CSV");
		fc.addFilter("CEF files", "cef", "CEF");
		fc.setTitle("Read MZ/RT feature list from file");
		fc.setOpenButtonText("Import feature list from file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this))) {
			
			File inputFile = fc.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			importFromFile(inputFile);
		}
	}

	private void loadLookupFeatureListFromDatabase() {
		
		FeatureLookupDataSet dataSet = 
				featureLookupListSelectorDialog.getSelectedDataSet();
		if(dataSet == null)
			return;		
		
		featureLookupListSelectorDialog.dispose();		
		loadDataSet(dataSet);
		fireFormChangeEvent(ParameterSetStatus.CHANGED);
	}

	private void selectLookupFeatureListFromDatabase() {

		featureLookupListSelectorDialog = new FeatureLookupListSelectorDialog(this);
		featureLookupListSelectorDialog.setLocationRelativeTo(this);
		featureLookupListSelectorDialog.setVisible(true);
	}
	
	public void loadDataSet(FeatureLookupDataSet dataSet) {
		
		if(dataSet == null)
			return;
		
		this.dataSet = dataSet;
		setDataSetName(dataSet.getName());
		setDataSetDescription(dataSet.getDescription());
		
		if(dataSet.getFeatures().isEmpty()) 
			getFeaturesForFeatureLookupDataSet(dataSet);		
		else
			featureTable.setTableModelFromFeatureCollection(dataSet.getFeatures());
	}
	
	private void getFeaturesForFeatureLookupDataSet(FeatureLookupDataSet dataSet) {

		GetFeaturesForFeatureLookupDataSetTask task = 
				new GetFeaturesForFeatureLookupDataSetTask(dataSet);
		IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
				"Getting features for lookup data set ...", this, task);
		idp.setLocationRelativeTo(this);
		idp.setVisible(true);
	}
	
	class GetFeaturesForFeatureLookupDataSetTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		private FeatureLookupDataSet dataSet;

		public GetFeaturesForFeatureLookupDataSetTask(FeatureLookupDataSet dataSet) {
			this.dataSet = dataSet;
		}

		@Override
		public Void doInBackground() {

			try {
				FeatureLookupDataSetUtils.getFeaturesForFeatureLookupDataSet(dataSet);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			featureTable.setTableModelFromFeatureCollection(dataSet.getFeatures());;
			return null;
		}
	}
		
	private void importFromFile(File inputFile) {		
		
		ReadFeaturesFromInputFileTask task = 
				new ReadFeaturesFromInputFileTask(inputFile);
		IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
				"Getting features for lookup data set ...", this, task);
		idp.setLocationRelativeTo(this);
		idp.setVisible(true);
	}
	
	class ReadFeaturesFromInputFileTask extends LongUpdateTask {
		
		private File inputFile;

		public ReadFeaturesFromInputFileTask(File inputFile) {
			super();
			this.inputFile = inputFile;
		}
		
		@Override
		public Void doInBackground() {
			
			try {
				readFeaturesFromInputFile(inputFile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}		
	}

	private void readFeaturesFromInputFile(File inputFile) {
		
		dataSet = null;
		String extension  = 
				FilenameUtils.getExtension(inputFile.getName()).toLowerCase();
		
		if(extension.equals("txt") || extension.equals("tsv"))
			readFeaturesFromTextFile(inputFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		
		if(extension.equals("csv"))
			readFeaturesFromTextFile(inputFile, ',');
		
		if(extension.equals("cef"))
			readFeaturesFromCefFile(inputFile);
	}

	private void readFeaturesFromCefFile(File inputFile) {

		DataFile df = new DataFile(inputFile);
		ImportMinimalMSOneFeaturesFromCefTask task = 
				new ImportMinimalMSOneFeaturesFromCefTask(df);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	private void readFeaturesFromTextFile(File inputFile, char delimiter) {
		
		Collection<MinimalMSOneFeature>features = new TreeSet<MinimalMSOneFeature>();
		String[][]featureData = DelimitedTextParser.parseTextFile(inputFile,delimiter);
		int mzIndex = -1;
		int rtIndex = -1;
		int nameIndex = -1;
		int rankIndex = -1;
		int foldChangeIndex = -1;
		int pValueIndex = -1;
		int smilesIndex = -1;
		int inchiKeyIndex = -1;
		
		for(int i=0; i<featureData[0].length; i++) {
			
			if(featureData[0][i].equalsIgnoreCase(FeatureListImportFields.mz.name()))
				mzIndex = i;
			
			if(featureData[0][i].equalsIgnoreCase(FeatureListImportFields.rt.name()))
				rtIndex = i;
			
			if(featureData[0][i].equalsIgnoreCase(FeatureListImportFields.name.name()))
				nameIndex = i;
			
			if(featureData[0][i].equalsIgnoreCase(FeatureListImportFields.rank.name()))
				rankIndex = i;
			
			if(featureData[0][i].equalsIgnoreCase(FeatureListImportFields.foldChange.name()))
				foldChangeIndex = i;
			
			if(featureData[0][i].equalsIgnoreCase(FeatureListImportFields.pValue.name()))
				pValueIndex = i;
			
			if(featureData[0][i].equalsIgnoreCase(FeatureListImportFields.smiles.name()))
				smilesIndex = i;
			
			if(featureData[0][i].equalsIgnoreCase(FeatureListImportFields.inchiKey.name()))
				inchiKeyIndex = i;
		}
		if(mzIndex == -1 || rtIndex == -1 || nameIndex == -1) {
			MessageDialog.showErrorMsg("Invalid file format.\n"
					+ "First line must include \"MZ\", \"RT\", and \"Name\" columns\n"
					+ "\"Rank\", \"SMILES\" and \"InChiKey\" columns are optional", 
					this);
			return;
		}
		if(mzIndex >= 0 && rtIndex >= 0) {
			
			for(int i=1; i<featureData.length; i++) {
				
				try {
					double mz = Double.parseDouble(featureData[i][mzIndex]);
					double rt = Double.parseDouble(featureData[i][rtIndex]);
					MinimalMSOneFeature f = new MinimalMSOneFeature(mz, rt);
					
					if(nameIndex >= 0)
						f.setName(featureData[i][nameIndex]);
					
					if(rankIndex >= 0 && !featureData[i][rankIndex].isEmpty())
						f.setRank(Double.parseDouble(featureData[i][rankIndex]));
					
					if(foldChangeIndex >= 0 && !featureData[i][foldChangeIndex].isEmpty())
						f.setFoldChange(Double.parseDouble(featureData[i][foldChangeIndex]));
					
					if(pValueIndex >= 0 && !featureData[i][pValueIndex].isEmpty())
						f.setpValue(Double.parseDouble(featureData[i][pValueIndex]));
					
					if(smilesIndex >= 0 && !featureData[i][smilesIndex].isEmpty())
						f.setSmiles(featureData[i][smilesIndex]);
					
					if(inchiKeyIndex >= 0 && !featureData[i][inchiKeyIndex].isEmpty())
						f.setInchiKey(featureData[i][inchiKeyIndex]);
										
					features.add(f);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else {
			if(nameIndex >= 0) {
				
				for(int i=1; i<featureData.length; i++) {
					String name = featureData[i][nameIndex];
					String[]mzrt = name.replace("UNK_", "").split("_");
					if(mzrt.length == 2) {
						
						try {
							double mz = Double.parseDouble(mzrt[0]);
							double rt = Double.parseDouble(mzrt[1]);
							MinimalMSOneFeature f = new MinimalMSOneFeature(name, mz, rt);
							
							if(rankIndex >= 0)
								f.setRank(Double.parseDouble(featureData[i][rankIndex]));
							
							if(smilesIndex >= 0)
								f.setSmiles(featureData[i][smilesIndex]);
							
							if(inchiKeyIndex >= 0)
								f.setInchiKey(featureData[i][inchiKeyIndex]);
							
							features.add(f);
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		if(features != null) {
			
			try {
				getMissingSmiles(features);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			featureTable.setTableModelFromFeatureCollection(features);
			fireFormChangeEvent(ParameterSetStatus.CHANGED);
		}
	}
	
	private void getMissingSmiles(Collection<MinimalMSOneFeature> features) throws Exception{
		
		List<MinimalMSOneFeature> featuresToUpdate = features.stream().
				filter(f -> Objects.nonNull(f.getInchiKey())).
				filter(f -> Objects.isNull(f.getSmiles())).
				collect(Collectors.toList());
		if(featuresToUpdate.isEmpty())
			return;
		
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT SMILES FROM COMPOUND_DATA WHERE INCHI_KEY = ?";
		String query2d = "SELECT SMILES FROM COMPOUND_DATA WHERE INCHI_KEY_CONNECT = ?";
		
		PreparedStatement ps = conn.prepareStatement(query);
		PreparedStatement ps2d = conn.prepareStatement(query2d);
		ResultSet rs = null;
		for(MinimalMSOneFeature f : featuresToUpdate) {
			
			ps.setString(1, f.getInchiKey());
			rs = ps.executeQuery();
			while(rs.next()) {
				f.setSmiles(rs.getString("SMILES"));
				break;
			}
			rs.close();
			if(f.getSmiles() == null) {
				
				ps2d.setString(1, f.getInchiKey().substring(0, 14));
				rs = ps2d.executeQuery();
				while(rs.next()) {
					f.setSmiles(rs.getString("SMILES"));
					break;
				}
				rs.close();
			}
		}
		ps.close();
		ps2d.close();
		ConnectionManager.releaseConnection(conn);
	}

	public Collection<MinimalMSOneFeature>getSelectedFeatures(){
		return featureTable.getSelectedFeatures();
	}
	
	public Collection<MinimalMSOneFeature>getAllFeatures(){
		return featureTable.getAllFeatures();
	}

	public FeatureLookupDataSet getDataSet() {
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
		featureTable.clearTable();
		dataSetNameTextField.setText("");
		descriptionTextArea.setText("");
		dataSet = null;
	}
}
