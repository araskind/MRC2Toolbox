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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.ms;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang.StringUtils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.data.DAMethodAssignmentDialog;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.data.IDtrackerDataFileSampleMatchTable;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.IDTrackerDataLoadWizard;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.IDTrackerDataLoadWizardPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.InformationDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.MSMSSearchResultsBatchPrescanTask;

public class WizardMsDataVerifierPanel extends IDTrackerDataLoadWizardPanel 
		implements ActionListener, BackedByPreferences, TaskListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2460123645924507011L;

	private Preferences preferences;
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private File baseDirectory;
	private IDtrackerDataFileSampleMatchTable dataFileSampleMatchTable;
	private WizardMsDataFileImportToolbar toolbar;
	private ImprovedFileChooser chooser;
	private FileNameExtensionFilter txtFilter, xmlFilter, mgfFilter;
	private LIMSExperiment experiment;
	private Collection<CompoundIdentity>missingIdentities;
	private DAMethodAssignmentDialog daMethodAssignmentDialog;
	private boolean dataVerified;

	public WizardMsDataVerifierPanel(IDTrackerDataLoadWizard wizard) {
		
		super(wizard);
		
		JPanel panel  = initPanel();
		add(panel, gbc_panel);
		
		completeStageButton.setText(
				MainActionCommands.COMPLETE_DATA_VERIFICATION_COMMAND.getName());		
		completeStageButton.setActionCommand(
				MainActionCommands.COMPLETE_DATA_VERIFICATION_COMMAND.getName());
		
		missingIdentities = null;
		dataVerified = false;
		loadPreferences();
		initChooser();
	}
	
	private JPanel initPanel() {
		
		JPanel panel = new JPanel(new BorderLayout(0,0));
		dataFileSampleMatchTable = new IDtrackerDataFileSampleMatchTable(); 
		dataFileSampleMatchTable.addTablePopupMenu(new DaAssignmentPopupMenu(this));
		JScrollPane scrollPane = new JScrollPane(dataFileSampleMatchTable);
		panel.add(scrollPane, BorderLayout.CENTER);
		
		toolbar = new WizardMsDataFileImportToolbar(this);
		panel.add(toolbar, BorderLayout.NORTH);
		return panel;
	}
	
	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setCurrentDirectory(baseDirectory);

		txtFilter = new FileNameExtensionFilter("Text files", "txt", "tsv");
		xmlFilter = new FileNameExtensionFilter("XML files", "xml", "cef", "CEF");
		mgfFilter = new FileNameExtensionFilter("MGF files", "mgf");
	}
	
	public void setExperiment(LIMSExperiment experiment) {
		this.experiment = experiment;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(experiment == null || wizard.getWorklist() == null )
			return;
		
		String command = e.getActionCommand();
		if (e.getSource().equals(chooser) && command.equals(JFileChooser.APPROVE_SELECTION)) {

			int originalFileCount = dataFileSampleMatchTable.getDataFiles().size();
			File[] dataFiles = chooser.getSelectedFiles();
			//	dataFileSampleMatchTable.setTableModelFromFiles(dataFiles, experiment);
			dataFileSampleMatchTable.addDataFilesUsingWorklist(dataFiles, wizard.getWorklist());
			baseDirectory = dataFiles[0].getParentFile();
			savePreferences();
			
			if(dataFileSampleMatchTable.getDataFiles().size() > originalFileCount)
				dataVerified = false;			
		}	
		if(command.equals(MainActionCommands.ADD_DATA_FILES_COMMAND.getName()))
			addDataFiles();
		
		if(command.equals(MainActionCommands.REMOVE_DATA_FILES_COMMAND.getName()))
			removeDataFiles();
		
		if(command.equals(MainActionCommands.CLEAR_DATA_COMMAND.getName()))
			clearDataFiles();
		
		if (command.equals(MainActionCommands.ASSIGN_DA_METHOD_TO_DATA_FILES_DIALOG_COMMAND.getName()))
			showDAMethodAssignmentDialog();

		if (command.equals(MainActionCommands.ASSIGN_DA_METHOD_TO_DATA_FILES_COMMAND.getName()))
			assignDaMethodToFiles();
		
		if (command.equals(MainActionCommands.CEF_MSMS_SCAN_RUN_COMMAND.getName()))
			runCefMsMsPrescan();				
	}
	
	private void runCefMsMsPrescan() {
		
		if(dataFileSampleMatchTable.getDataFiles().isEmpty())
			return;

		List<File> fileList = dataFileSampleMatchTable.getDataFiles().stream().
				map(f -> new File(f.getFullPath())).collect(Collectors.toList());
		MSMSSearchResultsBatchPrescanTask task = new MSMSSearchResultsBatchPrescanTask(fileList, null);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}
	
	private void addDataFiles() {

		chooser.resetChoosableFileFilters();
		chooser.setFileFilter(xmlFilter);
		chooser.setMultiSelectionEnabled(true);
		chooser.showOpenDialog(this);
	}
	
	private void removeDataFiles() {

		if(dataFileSampleMatchTable.getSelectedDataFiles().isEmpty())
			return;

		String yesNoQuestion = "Do you want to remove selected data files from import queue?";
		if(MessageDialog.showChoiceMsg(yesNoQuestion , this) == JOptionPane.NO_OPTION)
			return;

		dataFileSampleMatchTable.removeSelectedDataFiles();
	}
	
	private void clearDataFiles() {

		String yesNoQuestion = "Do you want to remove all data files from import queue?";
		if(MessageDialog.showChoiceMsg(yesNoQuestion , this) == JOptionPane.NO_OPTION)
			return;

		dataFileSampleMatchTable.removeSelectedDataFiles();
	}

	private void showDAMethodAssignmentDialog() {

		if(dataFileSampleMatchTable.getSelectedDataFiles().isEmpty())
			return;

		daMethodAssignmentDialog =
				new DAMethodAssignmentDialog(
						dataFileSampleMatchTable.getSelectedDataFiles(), this);
		daMethodAssignmentDialog.setAvailableDataExtractionMethods(wizard.getDataExtractionMethods());
		daMethodAssignmentDialog.setLocationRelativeTo(this);
		daMethodAssignmentDialog.setVisible(true);
	}
		
	private void assignDaMethodToFiles() {

		Collection<DataFile> files = daMethodAssignmentDialog.getDataFiles();
		DataExtractionMethod method = daMethodAssignmentDialog.getSelectedMethod();
		if(files.isEmpty() || method == null)
			return;

		dataFileSampleMatchTable.setDaMethodFoFiles(files, method);
		daMethodAssignmentDialog.dispose();
	}

	public Collection<String> verifyDataForUpload() {

		Collection<String>errors = new ArrayList<String>();	
		if(dataFileSampleMatchTable.getDataFiles().isEmpty()) {
			errors.add("No MSMS data selected.");
			return errors;
		}
		if(dataFileSampleMatchTable.hasMissingInjectionData()) {
			errors.add("Some files can not be matched to worklist\n"
					+ "(no data acquisition method and injection time).");
			return errors;
		}
		if(dataFileSampleMatchTable.hasMissingDataAnalysisMethod()) {
			errors.add("Some files do not have data analysis method assigned.");
			return errors;
		}
		if(!dataVerified) {
			errors.add("Data in some MSMS files not checked against compound/MSMS database");
			return errors;
		}
		if(missingIdentities != null && !missingIdentities.isEmpty()) {
			writeMissingCompoundData(errors);
			return errors;
		}		
		return errors;
	}
	
	private void writeMissingCompoundData(Collection<String>errors) {
		
		List<CompoundDatabaseEnum> dbList = missingIdentities.stream().
				flatMap(id -> id.getDbIdMap().keySet().stream()).
				distinct().sorted().collect(Collectors.toList());

		ArrayList<String>headerChunks = new ArrayList<String>();
		headerChunks.add("Name");
		headerChunks.add("Formula");
		headerChunks.add("Mass");
		dbList.stream().forEach(d -> headerChunks.add(d.name()));
		errors.add(StringUtils.join(headerChunks, "\t"));		
		IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
		
		for(CompoundIdentity id : missingIdentities) {

			headerChunks.clear();
			headerChunks.add(id.getName());
			headerChunks.add(id.getFormula());
			IMolecularFormula mf = null;
			try {
				mf = MolecularFormulaManipulator.getMolecularFormula(id.getFormula(), builder);
			} catch (Exception e) {
				System.out.println(id.getFormula());
				e.printStackTrace();
			}
			try {
				mf = MolecularFormulaManipulator.getMolecularFormula(id.getFormula(), builder);
			} catch (Exception e) {
				System.out.println(id.getFormula());
				e.printStackTrace();
			}
			double mass = 0.0d;
			if(mf != null) 
				mass = MolecularFormulaManipulator.getMajorIsotopeMass(mf);
			
			headerChunks.add(MRC2ToolBoxConfiguration.getMzFormat().format(mass));
			for(CompoundDatabaseEnum db : dbList) {
				String cid = id.getDbId(db);
				if(cid == null)
					cid = "";

				headerChunks.add(cid);
			}
			errors.add(StringUtils.join(headerChunks, "\t"));
		}
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
				Preferences.userRoot().node(WizardMsDataVerifierPanel.class.getName()));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(WizardMsDataVerifierPanel.class.getName());
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

	@Override
	public void statusChanged(TaskEvent e) {
		// 
		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(MSMSSearchResultsBatchPrescanTask.class)) {
				
				MSMSSearchResultsBatchPrescanTask task = (MSMSSearchResultsBatchPrescanTask)e.getSource();
				missingIdentities = task.getMissingIdentities();
				if(!task.getPrescanLog().isEmpty()) {
					
					InformationDialog id = new InformationDialog(
							"Data prescan details", 
							"Added / missing compounds information", 
							StringUtils.join(task.getPrescanLog(), "\n"), 
							this);
				}
				dataVerified = true;
				MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
				MainWindow.hideProgressDialog();
			}
		}
		if (e.getStatus() == TaskStatus.ERROR || e.getStatus() == TaskStatus.CANCELED)
			MainWindow.hideProgressDialog();
	}
	
	public Map<DataFile,DataExtractionMethod>getFileDaMethodMap(){
		return dataFileSampleMatchTable.getFileDaMethodMap();
	}
}
