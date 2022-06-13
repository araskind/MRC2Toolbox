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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.byexp.MinimalMSOneFeatureTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
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
	private ImprovedFileChooser chooser;
	private File baseDirectory;
	
	public FeatureListImportPanel() {
		
		super(new BorderLayout(0,0));

		featureTable = new MinimalMSOneFeatureTable();
		add(new JScrollPane(featureTable), BorderLayout.CENTER);
		
		JPanel fileImportPanel = new JPanel();
		fileImportPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		fileImportPanel.setLayout(gridBagLayout);
		
		JButton btnNewButton = new JButton(
				MainActionCommands.IMPORT_MS1_FEATURES_FROM_FILE_COMMAND.getName());
		btnNewButton.setActionCommand(MainActionCommands.IMPORT_MS1_FEATURES_FROM_FILE_COMMAND.getName());
		btnNewButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 0;
		fileImportPanel.add(btnNewButton, gbc_btnNewButton);
		add(fileImportPanel, BorderLayout.SOUTH);
		
		initChooser();
	}
	
	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		//	chooser.setApproveButtonText("Attach document");
		chooser.addChoosableFileFilter(
				new FileNameExtensionFilter("Text files (TAB-separated)", "txt", "TXT", "tsv", "TSV"));
		chooser.addChoosableFileFilter(
				new FileNameExtensionFilter("Comma-separated text files", "csv", "CSV"));
		chooser.addChoosableFileFilter(
				new FileNameExtensionFilter("CEF files", "cef", "CEF"));
	}
	
	public void setBaseDirectory(File baseDirectory) {
		this.baseDirectory = baseDirectory;
		chooser.setCurrentDirectory(baseDirectory);
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
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.IMPORT_MS1_FEATURES_FROM_FILE_COMMAND.getName())) 			
			chooser.showOpenDialog(this);

		if(e.getSource().equals(chooser) && e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION)) {

			File inputFile = chooser.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			readFeaturesFromInputFile(inputFile);
		}
	}

	private void readFeaturesFromInputFile(File inputFile) {
		
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
		for(int i=0; i<featureData[0].length; i++) {
			
			if(featureData[0][i].toLowerCase().equals("mz") 
					|| featureData[0][i].toLowerCase().equals("m/z"))
				mzIndex = i;
			
			if(featureData[0][i].toLowerCase().equals("rt"))
				rtIndex = i;
			
			if(featureData[0][i].toLowerCase().equals("name"))
				nameIndex = i;
		}
		if(mzIndex == -1 && rtIndex == -1 && nameIndex == -1) {
			MessageDialog.showErrorMsg("Invalid file format.\n"
					+ "First line must include \"MZ\", \"RT\" and/or \"Name\" columns", 
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
							features.add(f);
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		if(features != null)
			featureTable.setTableModelFromFeatureCollection(features);		
	}
	
	public Collection<MinimalMSOneFeature>getSelectedFeatures(){
		return featureTable.getSelectedFeatures();
	}
	
	public Collection<MinimalMSOneFeature>getAllFeatures(){
		return featureTable.getAllFeatures();
	}
}
