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

package edu.umich.med.mrc2.datoolbox.gui.rawdata;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.AverageMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExtractedChromatogram;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.library.feditor.DockableMsMsTable;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.plot.chromatogram.DockableChromatogramPlot;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.MsDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.spectrum.DockableSpectumPlot;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.msc.RawDataConversionSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.tree.RawDataTree;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.tree.TreeGrouping;
import edu.umich.med.mrc2.datoolbox.gui.tables.ms.DockableMsTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.ChromatogramExtractionTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.MassSpectraAveragingTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.RawDataBatchCoversionTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.RawDataFileOpenTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.RawDataRepositoryIndexingTask;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.scan.IScan;
import umich.ms.datatypes.scancollection.IScanCollection;

public class RawDataExaminerPanel extends DockableMRC2ToolboxPanel implements TreeSelectionListener{
	
	private RawDataExaminerToolbar toolbar;
	private DockableChromatogramPlot chromatogramPanel;
	private DockableSpectumPlot msPlotPanel;
	private DockableMsTable msTable;
	private DockableSpectumPlot msmsPlotPane;
	private DockableMsMsTable msmsTable;
	private DockableDataTreePanel dataFileTreePanel;
	private DockableXICSetupPanel xicSetupPanel;
	private DockableMsExtractorPanel msExtractorPanel;
	private DockableRawDataFilePropertiesTable rawDataFilePropertiesTable;
	private ImprovedFileChooser chooser;
	private IndeterminateProgressDialog idp;
	private CloseRawDataFilesDialog closeRawDataFilesDialog;
	private RawDataConversionSetupDialog rawDataConversionSetupDialog;

	private static final Icon componentIcon = GuiUtils.getIcon("chromatogram", 16);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "RawDataPanel.layout");
	
	public RawDataExaminerPanel(){
		
		super("RawDataExaminerPanel", PanelList.RAW_DATA_EXAMINER.getName(), componentIcon);;

		toolbar = new RawDataExaminerToolbar(this);
		add(toolbar, BorderLayout.NORTH);
		
		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);
		
		dataFileTreePanel = new DockableDataTreePanel(this);
		dataFileTreePanel.addTreeSelectionListener(this);
		chromatogramPanel =  new DockableChromatogramPlot(
				"RawDataExaminerPanelDockableChromatogramPlot", "Chromatograms");
		msPlotPanel = new DockableSpectumPlot(
				"RawDataExaminerPanelDockableSpectumPlotMS1", "MS1 spectra");	
		msPlotPanel.setRawDataExaminerPanel(this);
		msTable = new DockableMsTable("RawDataExaminerPanelDockableMsTableMS1", "MS1 table");
		msmsPlotPane = new DockableSpectumPlot(
				"RawDataExaminerPanelDockableSpectumPlotMS2", "MS2 spectra");
		msPlotPanel.setRawDataExaminerPanel(this);
		msmsTable = new DockableMsMsTable(
				"RawDataExaminerPanelDockableMsMsTable", "MS2 table");
		rawDataFilePropertiesTable = new DockableRawDataFilePropertiesTable();
		
		xicSetupPanel = new DockableXICSetupPanel(this);
		msExtractorPanel = new DockableMsExtractorPanel(this);
		
		grid.add( 0, 0, 25, 100, dataFileTreePanel, rawDataFilePropertiesTable );
		grid.add( 25, 0, 75, 50, chromatogramPanel );
		grid.add( 25, 50, 50, 50, msPlotPanel, msTable, msmsPlotPane, msmsTable);
		grid.add( 75, 50, 25, 50, xicSetupPanel, msExtractorPanel);
		control.getContentArea().deploy( grid );

		add(control.getContentArea(), BorderLayout.CENTER);		
		loadLayout(layoutConfigFile);
		
		initChooser();
	}
	
	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(true);
		File rawDataRepository = new File(MRC2ToolBoxConfiguration.getRawDataRepository());
		if(rawDataRepository.exists() && rawDataRepository.isDirectory())
			chooser.setCurrentDirectory(rawDataRepository);

		chooser.setFileFilter(
				new FileNameExtensionFilter("Raw MS files", "mzml", "mzML", "mzXML", "mzxml"));
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		
//		if(MRC2ToolBoxCore.getIdTrackerUser() == null) {
//			MessageDialog.showErrorMsg("You are not logged in ID tracker!", this.getContentPane());
//			return;
//		}
		String command = event.getActionCommand();
		if (command.equals(MainActionCommands.GROUP_TREE_BY_FILE.getName())){
						
			dataFileTreePanel.groupTree(TreeGrouping.BY_DATA_FILE);
			toolbar.groupTree(TreeGrouping.BY_DATA_FILE);
		}
		if (command.equals(MainActionCommands.GROUP_TREE_BY_TYPE.getName())){
						
			dataFileTreePanel.groupTree(TreeGrouping.BY_OBJECT_TYPE);
			toolbar.groupTree(TreeGrouping.BY_OBJECT_TYPE);
		}
		if (command.equals(MainActionCommands.EXPAND_TREE.getName())){

			dataFileTreePanel.toggleTreeExpanded(true);
			toolbar.treeExpanded(true);
		}
		if (command.equals(MainActionCommands.COLLAPSE_TREE.getName())) {

			dataFileTreePanel.toggleTreeExpanded(false);
			toolbar.treeExpanded(false);
		}	
		if (command.equals(MainActionCommands.OPEN_RAW_DATA_FILE_COMMAND.getName())) 
			openRawDataFiles();
		
		if (command.equals(MainActionCommands.CLOSE_RAW_DATA_FILE_COMMAND.getName())) 
			showCloseRawDataFilesDialog();
		
		if (command.equals(MainActionCommands.FINALIZE_CLOSE_RAW_DATA_FILE_COMMAND.getName())) 
			closeRawDataFiles();		

		if (command.equals(MainActionCommands.SETUP_RAW_DATA_CONVERSION_COMMAND.getName()))	
			setupRawDataConversion();	
		
		if (command.equals(MainActionCommands.CONVERT_RAW_DATA_COMMAND.getName()))	
			convertRawData();
		
		if (command.equals(MainActionCommands.INDEX_RAW_DATA_REPOSITORY_COMMAND.getName()))	
			indexRawData();	
		
		if (command.equals(MainActionCommands.EXTRACT_CHROMATOGRAM.getName()))	
			extractChromatogramms();		
	}

	private void extractChromatogramms() {
		
		Collection<String>errors = xicSetupPanel.veryfyParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this.getContentPane());
			return;
		}
		ChromatogramExtractionTask task = xicSetupPanel.createChromatogramExtractionTask();
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	private void indexRawData() {

		RawDataRepositoryIndexingTask task = new RawDataRepositoryIndexingTask();
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	private void setupRawDataConversion() {
		
		String msConvertPath = MRC2ToolBoxConfiguration.getMsConvertExecutableFile();
		if(msConvertPath.isEmpty()) {
			MessageDialog.showErrorMsg("msconvert executable file location not specified.", this.getContentPane());
			return;
		}
		File msConvertExe = new File(msConvertPath);
		if(!msConvertExe.exists()) {
			MessageDialog.showErrorMsg("msconvert executable file can not be found at \n\"" + 
					msConvertPath + "\"" , this.getContentPane());
			return;
		}		
		rawDataConversionSetupDialog = new RawDataConversionSetupDialog(this);
		rawDataConversionSetupDialog.setLocationRelativeTo(this.getContentPane());
		rawDataConversionSetupDialog.setVisible(true);
	}

	private void convertRawData() {

		File outputDir = rawDataConversionSetupDialog.getOutputFolder();
		if(outputDir == null) {
			MessageDialog.showErrorMsg("Output folder not specified.", rawDataConversionSetupDialog);
			return;
		}
		Collection<File>filesToConvert = rawDataConversionSetupDialog.getFiles();
		if(filesToConvert.isEmpty()) {
			MessageDialog.showErrorMsg("No files selected for conversion.", rawDataConversionSetupDialog);
			return;
		}
		RawDataBatchCoversionTask task = 
				new RawDataBatchCoversionTask(outputDir, filesToConvert);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);		
		rawDataConversionSetupDialog.dispose();
	}

	private void openRawDataFiles() {
		
		if(chooser.showOpenDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {
			
			File[] selectedFiles = chooser.getSelectedFiles();
			if(selectedFiles.length == 0)
				return;
			
			ArrayList<File>filesToOpen = new ArrayList<File>();
			for(File rf : selectedFiles) {
				
				if(RawDataManager.getRawData(rf) == null)
					filesToOpen.add(rf);
			}
			if(filesToOpen.isEmpty()) {
				MessageDialog.showWarningMsg("All selected files already opened.", this.getContentPane());
				return;
			}			
			chooser.setCurrentDirectory(chooser.getSelectedFile().getParentFile());
			RawDataFileOpenTask task = new RawDataFileOpenTask(filesToOpen);
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
	}
	
	class OpenRawDataFilesTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		private Collection<DataFile>newRawFiles;

		public OpenRawDataFilesTask(Collection<DataFile>newRawFiles) {
			this.newRawFiles = newRawFiles;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Void doInBackground() {

			try {
				Collection<DataFile> existingFiles = dataFileTreePanel.getDataFiles();
				existingFiles.addAll(newRawFiles);
				dataFileTreePanel.loadData(existingFiles, true);	
				dataFileTreePanel.toggleTreeExpanded(dataFileTreePanel.isTreeExpanded());				
				xicSetupPanel.loadData(newRawFiles, true);
				msExtractorPanel.loadData(newRawFiles, true);				
			} 
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}

	private void showCloseRawDataFilesDialog() {
		
		closeRawDataFilesDialog = new CloseRawDataFilesDialog(this);
		closeRawDataFilesDialog.setLocationRelativeTo(this.getContentPane());
		closeRawDataFilesDialog.setVisible(true);
	}
	
	private void closeRawDataFiles() {
		
		Collection<DataFile> files = closeRawDataFilesDialog.getSelectedFiles();
		if(files.isEmpty()) {
			MessageDialog.showErrorMsg("No files selected.", closeRawDataFilesDialog);
			return;
		}
		dataFileTreePanel.removeDataFiles(files);
		chromatogramPanel.removeChromatogramsForFiles(files);
		files.stream().forEach(f -> RawDataManager.removeDataSource(f));
		clearMsData(files);
		
		xicSetupPanel.removeDataFiles(files);
		msExtractorPanel.removeDataFiles(files);
		
		// TODO clear only if data related to selected files
		rawDataFilePropertiesTable.clearTable();
		
		closeRawDataFilesDialog.dispose();
	}

	private void clearMsData(Collection<DataFile> files) {
		
		// TODO clear only if data related to selected files
		msPlotPanel.clearPanel();
		msTable.clearTable();
		msmsPlotPane.clearPanel();
		msmsTable.clearTable();
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {
			
			((AbstractTask)e.getSource()).removeTaskListener(this);
			
			if (e.getSource().getClass().equals(ChromatogramExtractionTask.class)) {			
				ChromatogramExtractionTask task = (ChromatogramExtractionTask)e.getSource();				
				finalizeChromatogramExtraction(task);						
			}
			if (e.getSource().getClass().equals(MassSpectraAveragingTask.class)) {				
				MassSpectraAveragingTask task = (MassSpectraAveragingTask)e.getSource();			
				finalizeMassSpectraAveraging(task);		
			}
			if(e.getSource().getClass().equals(RawDataFileOpenTask.class)) {
				
				RawDataFileOpenTask rdoTask = (RawDataFileOpenTask)e.getSource();		
				OpenRawDataFilesTask task = new OpenRawDataFilesTask(rdoTask.getOpenedFiles());
				MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
				MainWindow.hideProgressDialog();
				idp = new IndeterminateProgressDialog("Loading raw data tree ...", this.getContentPane(), task);
				idp.setLocationRelativeTo(this.getContentPane());
				idp.setVisible(true);
			}
			if(e.getSource().getClass().equals(RawDataBatchCoversionTask.class)) {
				MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
				MainWindow.hideProgressDialog();
				MessageDialog.showInfoMsg("Data conversion completed", this.getContentPane());
			}
			if(e.getSource().getClass().equals(RawDataRepositoryIndexingTask.class)) 
				MessageDialog.showInfoMsg("Raw data indexing completed", this.getContentPane());
		}
	}
	
	public void finalizeMassSpectraAveraging(MassSpectraAveragingTask task) {
		
		Collection<AverageMassSpectrum> spectra = task.getExtractedSpectra();
		for (AverageMassSpectrum ms : spectra)
			dataFileTreePanel.addObject(ms);
		
		if(!spectra.isEmpty()) { 
			AverageMassSpectrum avgMs = spectra.iterator().next();
			showAverageMassSpectrum(avgMs);
			//	dataFileTreePanel.selectNodeForObject(avgMs);
		}
	}
	
	public void finalizeChromatogramExtraction(ChromatogramExtractionTask task) {
		
		Collection<ExtractedChromatogram> chroms = task.getExtractedChromatograms();
		for (ExtractedChromatogram ec : chroms)
			dataFileTreePanel.addObject(ec);

		chromatogramPanel.showExtractedChromatogramCollection(chroms);			
		Set<DataFile> chromFiles = chroms.stream().map(c -> c.getDataFile()).distinct().collect(Collectors.toSet());
		xicSetupPanel.selectFiles(chromFiles);
		msExtractorPanel.selectFiles(chromFiles);
	}
	
	public void loadRawData(Collection<DataFile>dataFiles) {
		
		OpenRawDataFilesTask task = new OpenRawDataFilesTask(dataFiles);
		idp = new IndeterminateProgressDialog("Loading raw data tree ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	@Override
	public void clearPanel() {

		dataFileTreePanel.clearPanel();
		chromatogramPanel.clearPanel();
		msPlotPanel.clearPanel();
		msTable.clearTable();
		msmsPlotPane.clearPanel();
		msmsTable.clearTable();
		xicSetupPanel.clearPanel();
		msExtractorPanel.clearPanel();
		rawDataFilePropertiesTable.clearTable();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void loadLayout(File layoutFile) {

		if(layoutConfigFile.exists()) {
			try {
				control.readXML(layoutFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				control.writeXML(layoutFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void saveLayout(File layoutFile) {

		try {
			control.writeXML(layoutFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {

		RawDataTree pt = (RawDataTree) e.getSource();
		Collection<Object> selected = pt.getSelectedObjects();
		if(selected.isEmpty())
			return;
		
		//	Show first selected scan
		if(selected.stream().findFirst().get() instanceof IScan)
			showScan((IScan)selected.stream().findFirst().get());
				
		//	Show first selected user spectrum
		if(selected.stream().findFirst().get() instanceof AverageMassSpectrum)
			showAverageMassSpectrum((AverageMassSpectrum)selected.stream().findFirst().get());
		
		//	Show selected chromatograms
		List<ExtractedChromatogram> chroms = 
				selected.stream().filter(o -> (o instanceof ExtractedChromatogram)).
				map(ExtractedChromatogram.class::cast).collect(Collectors.toList());
		
		if(!chroms.isEmpty())
			showChromatograms(chroms);
		
		List<DataFile> files = 
				selected.stream().filter(o -> (o instanceof DataFile)).
				map(DataFile.class::cast).collect(Collectors.toList());
		
		if(!files.isEmpty())
			showFiles(files);
	}

	private void showFiles(List<DataFile> files) {

		xicSetupPanel.selectFiles(files);
		msExtractorPanel.selectFiles(files);
		
		LCMSData data = RawDataManager.getRawData(files.get(0));
		rawDataFilePropertiesTable.showDataFileProperties(data);
	}

	private void showAverageMassSpectrum(AverageMassSpectrum averageMassSpectrum) {
		msPlotPanel.showMsDataSet(new MsDataSet(averageMassSpectrum));
		DataFile df = averageMassSpectrum.getDataFile();
		if(df != null) {
			xicSetupPanel.selectFiles(Collections.singleton(df));
			msExtractorPanel.selectFiles(Collections.singleton(df));
		}
	}

	private void showScan(IScan s) {
		
		if(s.getMsLevel() == 1) {
			msPlotPanel.showScan(s);
			msTable.setTableModelFromScan(s);
		}
		else {
			msmsPlotPane.showScan(s);
			msmsTable.setTableModelFromScan(s);
			IScan parent = getParentScan(s);
			if(parent != null) {
				msPlotPanel.showScan(parent);
				msTable.setTableModelFromScan(parent);
			}
		}
		DataFile df = dataFileTreePanel.getDataFileForScan(s);
		if(df != null) {
			xicSetupPanel.selectFiles(Collections.singleton(df));
			msExtractorPanel.selectFiles(Collections.singleton(df));
		}
	}
	
	public IScan getParentScan(IScan s) {
		
		if(s.getPrecursor() != null) {
			int parentScanNumber = s.getPrecursor().getParentScanNum();
			DataFile df = dataFileTreePanel.getDataFileForScan(s);
			if(df != null) {
				LCMSData data = RawDataManager.getRawData(df);
				if(data != null) {
					IScanCollection scans = data.getScans();
					return scans.getScanByNum(parentScanNumber);
				}
			}
		}
		return null;
	}
	
	public DataFile getDataFileForScan(IScan s) {
		return dataFileTreePanel.getDataFileForScan(s);
	}		

	private void showChromatograms(Collection<ExtractedChromatogram> chroms) {

		chromatogramPanel.clearPanel();
		if(!chroms.isEmpty()) {			
			chromatogramPanel.showExtractedChromatogramCollection(chroms);	
			TreeSet<DataFile>files = chroms.stream().map(c -> c.getDataFile()).
					collect(Collectors.toCollection(TreeSet::new));
			xicSetupPanel.selectFiles(files);
			msExtractorPanel.selectFiles(files);			
		}
	}
	
	public void removeDataFiles(Collection<DataFile> selectedFiles) {
		dataFileTreePanel.removeDataFiles(selectedFiles);		
	}

	@Override
	public void designStatusChanged(ExperimentDesignEvent e) {

	}

	@Override
	public void designSetStatusChanged(ExperimentDesignSubsetEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public Range getSelectedRTRange() {
		return chromatogramPanel.getSelectedRTRange();
	}
	
	public void clearChromatogramPanel() {
		chromatogramPanel.clearPanel();
	}
	
	public void clearSpectraPanel() {
		msPlotPanel.clearPanel();
	}

	@Override
	public void msFeatureStatusChanged(MsFeatureEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void featureSetStatusChanged(FeatureSetEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reloadDesign() {
		// TODO Auto-generated method stub
		
	}
}








































