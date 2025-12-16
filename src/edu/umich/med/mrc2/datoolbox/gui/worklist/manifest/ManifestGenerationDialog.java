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

package edu.umich.med.mrc2.datoolbox.gui.worklist.manifest;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.WorklistItem;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.compare.WorklistItemComparator;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentSampleInfoFields;
import edu.umich.med.mrc2.datoolbox.data.enums.DataExportFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MoTrPACRawDataManifestFields;
import edu.umich.med.mrc2.datoolbox.data.enums.WorklistImportType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.WorklistImportTask;
import edu.umich.med.mrc2.datoolbox.utils.DataImportUtils;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class ManifestGenerationDialog extends JDialog 
		implements ActionListener, BackedByPreferences, TaskListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8716421349210019279L;
	protected static final Icon dialogIcon = GuiUtils.getIcon("editCollection", 32);
	
	private File baseDirectory;
	private Preferences preferences;
	public static final String PREFS_NODE = ManifestGenerationDialog.class.getName();
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	public static final String BROWSE = "BROWSE";
	
	private ManifestGenerationToolbar toolbar;
	private ManifestTable manifestTable;
	private JComboBox msModeComboBox;
	private boolean appendWorklist;
	private ExperimentDesign experimentDesign;
	private static final IOFileFilter dotDfilter = 
			FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[dD]$"));
	private Worklist activeWorklist;

	public ManifestGenerationDialog(ExperimentDesign experimentDesign) {
		super();
		
		this.experimentDesign = experimentDesign;
		
		setPreferredSize(new Dimension(800, 640));	
		setTitle("Generate manifest from experiment design and worklist");
		setIconImage(((ImageIcon)dialogIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setSize(new Dimension(640, 150));
		setResizable(true);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		toolbar = new ManifestGenerationToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		JPanel panel1 = new JPanel(new BorderLayout(0, 0));
		panel1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel1, BorderLayout.CENTER);
		manifestTable = new ManifestTable();
		panel1.add(new JScrollPane(manifestTable), BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);
		
		JLabel lblNewLabel = new JLabel("MS mode");
		panel.add(lblNewLabel);
		
		msModeComboBox = new JComboBox<String>(
				new DefaultComboBoxModel<String>(new String[] {
						"RPPOS", "RPNEG", "IONPNEG"}));
		panel.add(msModeComboBox);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setPreferredSize(new Dimension(100, 0));
		horizontalStrut.setMinimumSize(new Dimension(100, 0));
		panel.add(horizontalStrut);

		JButton btnCancel = new JButton("Close dialog");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(btnCancel);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		loadPreferences();
		pack();
	}
	
	@Override
	public void dispose() {		
		savePreferences();
		super.dispose();
	}

	private String getMsMode() {
		return (String)msModeComboBox.getSelectedItem();
	}
	@Override
	public void actionPerformed(ActionEvent e) {
	
		String command = e.getActionCommand();
			
		if(command.equals(MainActionCommands.SCAN_DIR_SAMPLE_INFO_COMMAND.getName()))
			loadWorklistFromDirectoryScan(false);
		
		if(command.equals(MainActionCommands.SCAN_DIR_ADD_SAMPLE_INFO_COMMAND.getName()))
			loadWorklistFromDirectoryScan(true);
		
		if(command.equals(MainActionCommands.COPY_ASSAY_MANIFEST_COMMAND.getName()))
			copyManifestToClipboard();
		
		if(command.equals(MainActionCommands.SAVE_ASSAY_MANIFEST_COMMAND.getName()))
			saveManifestToFile();
		
		if(command.equals(MainActionCommands.CLEAR_MANIFEST_COMMAND.getName()))
			clearManifestData();		
	}
	
	private void copyManifestToClipboard() {
	
		String manifestString = manifestTable.getTableDataAsString();
		if(manifestString == null || manifestString.isEmpty())
			return;

		StringSelection stringSelection = new StringSelection(manifestString);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);		
	}

	private void saveManifestToFile() {
		
		String manifestString = manifestTable.getTableDataAsString();
		if(manifestString == null || manifestString.isEmpty())
			return;

		DataAnalysisProject currentExperiment = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		File saveDirectory = 
				new File(MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory()).getAbsoluteFile();
		if (currentExperiment != null)
			saveDirectory = MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExportsDirectory();

		JnaFileChooser fc = new JnaFileChooser(saveDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT");
		fc.setTitle("Save namifest to file:");
		fc.setMultiSelectionEnabled(false);
		String defaultFileName = currentExperiment.getName() + "_" + getMsMode() + "_MANIFEST_"
				+ MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".txt";
		fc.setDefaultFileName(defaultFileName);
		
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File outputFile = fc.getSelectedFile();
			outputFile = FIOUtils.changeExtension(outputFile, "txt") ;
			Path outputPath = Paths.get(outputFile.getAbsolutePath());
		    try {
				Files.writeString(outputPath, 
						manifestString, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void clearManifestData() {

		if(manifestTable.getModel().getRowCount() == 0)
			return;
		
		int selectedValue = MessageDialog.showChoiceWithWarningMsg(
				"Do you want to clear the curent manifest data?", 
				this.getContentPane());

		if (selectedValue == JOptionPane.YES_OPTION)
			manifestTable.clearTable();
	}

	private void loadWorklistFromDirectoryScan(boolean append) {

		if(append && activeWorklist == null)
			append = false;
		
		appendWorklist = append;
		try {
			scanDirectoryForSampleInfo(append);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void scanDirectoryForSampleInfo(boolean append) throws Exception {

		
		File dirToScan = selectRawFilesDirectory();

		if (dirToScan != null && dirToScan.exists()) {

			Collection<File> dataDiles = FileUtils.listFilesAndDirs(
					dirToScan,
					DirectoryFileFilter.DIRECTORY,
					dotDfilter);

			if (!dataDiles.isEmpty()) {

				WorklistImportTask wlit = new WorklistImportTask(
						dirToScan,
						null,
						appendWorklist,
						WorklistImportType.RAW_DATA_DIRECTORY_SCAN);
				wlit.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(wlit);
			}
		}
	}

	private File selectRawFilesDirectory() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select directory containing raw data files:");
		fc.setOpenButtonText("Select folder");
		fc.setMultiSelectionEnabled(false);		
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane())))	{	
			baseDirectory = fc.getSelectedFile();
			savePreferences();
			return baseDirectory;
		}
		else
			return null;
	}
	
	private List<String> createManifestColumns(Worklist worklist) {

		Set<String> allColumnNames = new TreeSet<String>();
		worklist.getTimeSortedWorklistItems().stream().
			forEach(i -> allColumnNames.addAll(i.getProperties().keySet()));

		HashMap<String, Integer> valueCount = new HashMap<String, Integer>();

		for (String field : allColumnNames) {

			valueCount.put(field, 0);
			for (WorklistItem item : worklist.getTimeSortedWorklistItems()) {

				if (item.getProperty(field) == null)
					continue;

				if (!item.getProperty(field).isEmpty()) {
					Integer current = valueCount.get(field) + 1;
					valueCount.replace(field, current);
				}
			}
		}
		ArrayList<String>columnNames = new ArrayList<String>();
		
		//	Add MoTrPAC obligatory column names
		for(MoTrPACRawDataManifestFields field : MoTrPACRawDataManifestFields.values())
			columnNames.add(field.getName());
			
		columnNames.add(DataExportFields.MRC2_SAMPLE_ID.getName());
		
		//	This will go to MoTrPAC sample id
		//	columnNames.add(DataExportFields.CLIENT_SAMPLE_ID.getName());
		
		//	Data file goes to MotrPAC column
//		if(valueCount.containsKey(AgilentSampleInfoFields.DATA_FILE.getName())) {
//			columnNames.add(AgilentSampleInfoFields.DATA_FILE.getName());
//		}
//		else {
//			MessageDialogue.showErrorMsg("Data file name field missing.", this.getContentPane());
//			return null;
//		}
		if(!valueCount.containsKey(AgilentSampleInfoFields.DATA_FILE.getName())) {			
			MessageDialog.showErrorMsg("Data file name field missing.", MRC2ToolBoxCore.getMainWindow());
			return null;
		}
		if(valueCount.containsKey(AgilentSampleInfoFields.ACQUISITION_TIME.getName())
				|| valueCount.containsKey(AgilentSampleInfoFields.ACQTIME.getName())) {
			columnNames.add(DataExportFields.INJECTION_TIME.getName());
		}
		else {
			MessageDialog.showErrorMsg("Injection time field missing.", MRC2ToolBoxCore.getMainWindow());
			return null;
		}
		for (Entry<String, Integer> entry : valueCount.entrySet()) {

			if (entry.getValue() > 0
					&& !entry.getKey().equals(AgilentSampleInfoFields.DATA_FILE.getName())
					&& !entry.getKey().equals(AgilentSampleInfoFields.ACQTIME.getName())
					&& !entry.getKey().equals(AgilentSampleInfoFields.ACQUISITION_TIME.getName()))
				columnNames.add(entry.getKey());
		}
		return columnNames;
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =  
				new File(preferences.get(BASE_DIRECTORY, 
						MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory()));
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

	@Override
	public void statusChanged(TaskEvent e) {
		
		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(WorklistImportTask.class))
				finalizeWorklistExtraction((WorklistImportTask) e.getSource());			
		}
		if (e.getStatus() == TaskStatus.ERROR || e.getStatus() == TaskStatus.CANCELED)
			MainWindow.hideProgressDialog();
	}

	private synchronized void finalizeWorklistExtraction(WorklistImportTask task) {
		
		//	Add obligatory manifest fields based on design
		DataAnalysisProject project = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		Worklist worklist = task.getWorklist();
		String msMode = getMsMode();
		
		Map<DataFile,String>fileMotrPacSampleTypeMap = 
				createDataFileMotrPacSampleTypeMap(worklist);
		Map<DataFile,String>fileMotrPacSampleIdMap = 
				createDataFileMotrPacSampleIdMap(worklist);
		
		for(WorklistItem item : worklist.getWorklistItems()) {
			
			DataFile df = item.getDataFile();
			ExperimentalSample sample = 
					DataImportUtils.getSampleFromFileName(df.getName(), project);
			if(sample == null)
				continue;
			
			item.setProperty(
					MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_ID.getName(), 
					fileMotrPacSampleIdMap.get(df));
			item.setProperty(
					MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_TYPE.getName(), 
					fileMotrPacSampleTypeMap.get(df));
			item.setProperty(
					MoTrPACRawDataManifestFields.MOTRPAC_RAW_FILE.getName(), 
					df.getName());
			item.setProperty(
					MoTrPACRawDataManifestFields.MOTRPAC_MS_MODE.getName(), 
					msMode);
			item.setProperty(
					DataExportFields.MRC2_SAMPLE_ID.getName(), 
					sample.getId());
		}
		if(appendWorklist && activeWorklist != null) {
			
			for(WorklistItem item : worklist.getWorklistItems())
				activeWorklist.addItem(item);			
		}
		else {
			activeWorklist = worklist;
		}
		List<WorklistItem> itemsToRemove = activeWorklist.getWorklistItems().stream().
			filter(i -> i.getProperty(MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_ID.getName()) == null).
			collect(Collectors.toList());
		if(!itemsToRemove.isEmpty()) {
			
			for(WorklistItem item : itemsToRemove)
				activeWorklist.getWorklistItems().remove(item);
		}		
		activeWorklist.setRunOrder();
		manifestTable.setTableModelFromWorklist(activeWorklist);
	}
	
	private Map<DataFile, String> createDataFileMotrPacSampleTypeMap(Worklist worklist) {
		
		DataAnalysisProject project = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		Map<DataFile,String>fileMotrPacSampleTypeMap = new TreeMap<DataFile,String>();
		for(WorklistItem item : worklist.getTimeSortedWorklistItems()) {
			
			DataFile df = item.getDataFile();		
			ExperimentalSample sample = 
					DataImportUtils.getSampleFromFileName(df.getName(), project);
			
			if(sample == null)
				continue;
			
			if(sample.getSampleType().equals(ReferenceSamplesManager.sampleLevel))
				fileMotrPacSampleTypeMap.put(df,"Sample");
			else {				
				if(sample.getMoTrPACQCSampleType() == null) {
					
					ExperimentalSample refSample = ReferenceSamplesManager.getReferenceSampleById(sample.getId());
					if(refSample != null)
						sample.setMoTrPACQCSampleType(refSample.getMoTrPACQCSampleType());
				}
				if(sample.getMoTrPACQCSampleType() != null)
					fileMotrPacSampleTypeMap.put(df, sample.getMoTrPACQCSampleType().getName());
				else
					fileMotrPacSampleTypeMap.put(df, "Sample");
			}			
		}
		return fileMotrPacSampleTypeMap;
	}
	
	private Map<DataFile, String> createDataFileMotrPacSampleIdMap(Worklist worklist) {
		
		DataAnalysisProject project = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		Map<DataFile,String>fileMotrPacSampleIdMap = new TreeMap<DataFile,String>();
		List<? extends WorklistItem> items = worklist.getTimeSortedWorklistItems().stream().
				sorted(new WorklistItemComparator(SortProperty.injectionTime)).
				collect(Collectors.toList());
		
		Map<ExperimentalSample,List<DataFile>>sampleFileMap = new TreeMap<ExperimentalSample,List<DataFile>>();
		
		for(WorklistItem item : items) {
			
			DataFile df = item.getDataFile();		
			ExperimentalSample sample = 
					DataImportUtils.getSampleFromFileName(df.getName(), project);
			if(sample == null)
				continue;
			
			if(!sampleFileMap.containsKey(sample))
				sampleFileMap.put(sample, new ArrayList<DataFile>());
			
			sampleFileMap.get(sample).add(df);
		}
		for (Entry<ExperimentalSample, List<DataFile>> entry : sampleFileMap.entrySet()) {
			
			ExperimentalSample sample = entry.getKey();
			if(entry.getValue().size() == 1) {
				
				if(sample.getSampleType().equals(ReferenceSamplesManager.sampleLevel))
					fileMotrPacSampleIdMap.put(entry.getValue().get(0), sample.getName());
				else
					fileMotrPacSampleIdMap.put(entry.getValue().get(0), sample.getId());
			}
			else {
				int counter = 1;
				for(DataFile df : entry.getValue()) {
					
					String increment = StringUtils.leftPad(Integer.toString(counter), 2, '0');
					
					if(sample.getSampleType().equals(ReferenceSamplesManager.sampleLevel))
						fileMotrPacSampleIdMap.put(df, sample.getName() + "-" + increment);
					else
						fileMotrPacSampleIdMap.put(df, sample.getId() + "-" + increment);
					counter++;
				}
			}
		}
		return fileMotrPacSampleIdMap;
	}
}
