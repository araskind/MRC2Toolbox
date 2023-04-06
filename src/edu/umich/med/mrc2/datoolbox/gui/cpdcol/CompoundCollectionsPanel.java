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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Date;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundCollection;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundCollectionComponent;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixture;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixtureComponent;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataField;
import edu.umich.med.mrc2.datoolbox.database.cpdcol.CompoundMultiplexUtils;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.cpdcol.export.MultiplexExportSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.cpdcol.mplex.DockableCompoundMultiplexComponentsListingTable;
import edu.umich.med.mrc2.datoolbox.gui.cpdcol.mplex.DockableCompoundMultiplexListingTable;
import edu.umich.med.mrc2.datoolbox.gui.cpdcol.prop.DockableCompoundCollectionComponentPropertiesTable;
import edu.umich.med.mrc2.datoolbox.gui.cpdcol.search.PropertySearchDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.structure.DockableMolStructurePanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cpdcoll.LoadCompoundCollectionTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cpdcoll.LoadCompoundMultiplexesTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cpdcoll.MultiplexDataExportTask;

public class CompoundCollectionsPanel extends DockableMRC2ToolboxPanel {

	private static final Icon componentIcon = GuiUtils.getIcon("compoundCollection", 16);
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "CompoundCollectionsPanel.layout");
	
//	private DockableCompoundCollectionListingTable compoundTable;
	private DockableMolStructurePanel 
		sourceMolStructurePanel, primaryMolStructurePanel, msReadyMolStructurePanel;
	private DockableCompoundCollectionComponentPropertiesTable propertiesTable;
	private DockableCompoundMultiplexListingTable compoundMultiplexListingTable;
	private CompoundCollectionSelectorDialog compoundCollectionSelectorDialog;
	private DockableCompoundMultiplexComponentsListingTable multiplexComponentsListingTable;
	private MultiplexExportSetupDialog multiplexExportSetupDialog;
	private PropertySearchDialog propertySearchDialog;
	private File baseExportDirectory;
		
	public CompoundCollectionsPanel() {
		
		super("CompoundCollectionsPanel", PanelList.COMPOUND_COLLECTIONS.getName(), componentIcon);
		setLayout(new BorderLayout(0, 0));

		menuBar = new CompoundCollectionsPanelMenuBar(this);
		add(menuBar, BorderLayout.NORTH);
		
//		compoundTable = new DockableCompoundCollectionListingTable();
//		compoundTable.getTable().addCompoundPopupListener(this);
//		compoundTable.getTable().getSelectionModel().addListSelectionListener(this);
		
		sourceMolStructurePanel = new DockableMolStructurePanel(
				"CpdCollectionsPanelSourceMolStructurePanel");
		sourceMolStructurePanel.setTitleText("Source compound structure");
		primaryMolStructurePanel = new DockableMolStructurePanel(
				"CpdCollectionsPanelPrimaryMolStructurePanel");
		primaryMolStructurePanel.setTitleText("Primary compound structure");
		msReadyMolStructurePanel = new DockableMolStructurePanel(
				"CpdCollectionsPanelMSReadyMolStructurePanel");
		msReadyMolStructurePanel.setTitleText("MSReady compound structure");
		
		compoundMultiplexListingTable =  
				new DockableCompoundMultiplexListingTable();
		compoundMultiplexListingTable.getTable().
				getSelectionModel().addListSelectionListener(this);
		multiplexComponentsListingTable = 
				new DockableCompoundMultiplexComponentsListingTable();
		multiplexComponentsListingTable.getTable().
				getSelectionModel().addListSelectionListener(this);
		propertiesTable = new DockableCompoundCollectionComponentPropertiesTable();

		grid.add(0, 0, 75, 40, 
				//	compoundTable, 
				compoundMultiplexListingTable, multiplexComponentsListingTable);
		grid.add(75, 0, 25, 40, sourceMolStructurePanel, 
				primaryMolStructurePanel, msReadyMolStructurePanel);
		grid.add(0, 50, 100, 60, propertiesTable);
		
		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
		
		initActions();
		loadLayout(layoutConfigFile);
		populatePanelsMenu();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		super.actionPerformed(e);
		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.SELECT_COMPOUND_COLLECTION_COMMAND.getName()))
			selectCompoundCollection();
		
		if(command.equals(MainActionCommands.LOAD_COMPOUND_COLLECTION_COMMAND.getName()))
			loadCompoundCollection();
		
		if(command.equals(MainActionCommands.LOAD_COMPOUND_MULTIPLEXES_COMMAND.getName()))
			loadCompoundMultiplexes();
		
		if(command.equals(MainActionCommands.SETUP_MULTIPLEXES_EXPORT_COMMAND.getName()))
			setupMultiplexDataExport();
		
		if(command.equals(MainActionCommands.EXPORT_SELECTED_MULTIPLEXES_COMMAND.getName()))
			exportSelectedMultiplexData();
		
		if(command.equals(MainActionCommands.SETUP_COMPOUND_PROPERTIES_SEARCH_COMMAND.getName()))
			showSearchDialog();
		
		if(command.equals(MainActionCommands.SEARCH_COMPOUND_PROPERTIES_COMMAND.getName()))
			searchCompoundsByProperties();
		
		if(command.equals(MainActionCommands.EXPORT_SELECTED_MULTIPLEX_FOR_FBF_COMMAND.getName()))
			exportSelectedMultiplexForFBF();	
		
		if(command.equals(MainActionCommands.EXPORT_SELECTED_MULTIPLEX_FOR_PCDL_IMPORT_COMMAND.getName()))
			exportSelectedMultiplexForPCDLImport();
	}

	private void exportSelectedMultiplexForFBF() {
		
		CompoundMultiplexMixture plex = 
				compoundMultiplexListingTable.getSelectedMultiplexMixture();
		if(plex == null)
			return;
		
		String fbfString =
				CompoundMultiplexUtils.createFindByFormulaInputForMultiplex(plex);
		
		if(baseExportDirectory == null)
			baseExportDirectory = new File(MRC2ToolBoxCore.libraryDir);
		
		JnaFileChooser fc = new JnaFileChooser(baseExportDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("CSV files", "csv", "CSV");
		fc.setTitle("Export Multiplex data formatted for Find by Formula search:");
		fc.setMultiSelectionEnabled(false);
//		fc.setSaveButtonText("Set output file");
		String defaultFileName = plex.getName() + "_4FBF_" + 
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".csv";
		fc.setDefaultFileName(defaultFileName);	
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File outputFile  = fc.getSelectedFile();
			baseExportDirectory = outputFile.getParentFile();
			
			Path outputPath = Paths.get(outputFile.getAbsolutePath());
			try {
				Files.writeString(outputPath, 
						fbfString, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}

	private void exportSelectedMultiplexForPCDLImport() {
		
		CompoundMultiplexMixture plex = 
				compoundMultiplexListingTable.getSelectedMultiplexMixture();
		if(plex == null)
			return;
		
		String pcdlString =
				CompoundMultiplexUtils.createPCDLImportInputForMultiplex(plex);
		
		if(pcdlString == null) {
			MessageDialog.showErrorMsg("Failed to create PCDL import file", this.getContentPane());
			return;
		}		
		if(baseExportDirectory == null)
			baseExportDirectory = new File(MRC2ToolBoxCore.libraryDir);
		
		JnaFileChooser fc = new JnaFileChooser(baseExportDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("CSV files", "csv", "CSV");
		fc.setTitle("Export Multiplex data formatted for PCDL import:");
		fc.setMultiSelectionEnabled(false);
//		fc.setSaveButtonText("Set output file");
		String defaultFileName = plex.getName() + "_4PCDL_" + 
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".csv";
		fc.setDefaultFileName(defaultFileName);	
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File outputFile  = fc.getSelectedFile();
			baseExportDirectory = outputFile.getParentFile();
			
			Path outputPath = Paths.get(outputFile.getAbsolutePath());
			try {
				Files.writeString(outputPath, 
						pcdlString, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	
	private void showSearchDialog() {
		
		propertySearchDialog = new PropertySearchDialog(this);
		propertySearchDialog.setLocationRelativeTo(this.getContentPane());
		propertySearchDialog.setVisible(true);
	}
	
	private void searchCompoundsByProperties() {
		// TODO Auto-generated method stub
		
		
		propertySearchDialog.dispose();
	}
	
	private void setupMultiplexDataExport() {

		Collection<CompoundMultiplexMixture> selectedMultiplexes = 
				compoundMultiplexListingTable.getSelectedMultiplexMixtures();
		if(selectedMultiplexes == null || selectedMultiplexes.isEmpty()) {
			MessageDialog.showWarningMsg(
					"Please select compound multiplexes to export", 
					this.getContentPane());
			return;
		}	
		multiplexExportSetupDialog = 
				new MultiplexExportSetupDialog(selectedMultiplexes, this);
		multiplexExportSetupDialog.setLocationRelativeTo(this.getContentPane());
		multiplexExportSetupDialog.setVisible(true);
	}

	private void exportSelectedMultiplexData() {

		Collection<CompoundMultiplexMixture>selectedMultiplexes = 
				multiplexExportSetupDialog.getMultiplexes();		
		Collection<CpdMetadataField>exportFields =  
				multiplexExportSetupDialog.getSelectedProperties();
		if(exportFields == null || exportFields.isEmpty()) {
			
			MessageDialog.showErrorMsg(
					"No fields selected for export", 
					this.getContentPane());
			return;
		}
		File outputFile = multiplexExportSetupDialog.getOutputFile();
		if(outputFile == null) {
			
			MessageDialog.showErrorMsg(
					"Output file not specified", 
					this.getContentPane());
			return;
		}
		MultiplexDataExportTask task = 
				new MultiplexDataExportTask(
						selectedMultiplexes, exportFields, outputFile);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);	
		
		multiplexExportSetupDialog.dispose();
	}
	
	private void loadCompoundMultiplexes() {

		LoadCompoundMultiplexesTask task = 
				new LoadCompoundMultiplexesTask();
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);	
	}

	private void selectCompoundCollection() {
		// TODO Auto-generated method stub
		compoundCollectionSelectorDialog = new CompoundCollectionSelectorDialog(this);
		compoundCollectionSelectorDialog.setLocationRelativeTo(this.getContentPane());
		compoundCollectionSelectorDialog.setVisible(true);
	}

	private void loadCompoundCollection() {
		
		CompoundCollection collection = 
				compoundCollectionSelectorDialog.getSelectedCompoundCollection();
		if(collection == null)
			return;
		
		LoadCompoundCollectionTask task = 
				new LoadCompoundCollectionTask(collection);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);		
		compoundCollectionSelectorDialog.dispose();
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			// Load compound collection
			if (e.getSource().getClass().equals(LoadCompoundCollectionTask.class))
				finalizeCompoundCollectionLoad((LoadCompoundCollectionTask) e.getSource());
			
			if (e.getSource().getClass().equals(LoadCompoundMultiplexesTask.class))
				finalizeCompoundMultiplexesLoad((LoadCompoundMultiplexesTask) e.getSource());
			
			if (e.getSource().getClass().equals(MultiplexDataExportTask.class))
				finalizeMultiplexDataExportTask((MultiplexDataExportTask) e.getSource());		
		}
		if (e.getStatus() == TaskStatus.ERROR || e.getStatus() == TaskStatus.CANCELED) {
			MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
			MainWindow.hideProgressDialog();
		}
	}
	
	private void finalizeCompoundMultiplexesLoad(LoadCompoundMultiplexesTask task) {
		clearPanel();
		Collection<CompoundMultiplexMixture> multiplexes = task.getMultiplexes();
		loadCompoundMultiplexesData(multiplexes);
	}

	private void loadCompoundMultiplexesData(Collection<CompoundMultiplexMixture> multiplexes) {
		// TODO Auto-generated method stub
		//		System.out.println("***");
		compoundMultiplexListingTable.
				setTableModelFromCompoundMultiplexMixtureCollection(multiplexes);
	}
	
	private void finalizeMultiplexDataExportTask(MultiplexDataExportTask task) {
		
		File results = task.getOutputFile();
		if(results.exists()) {

			if(MessageDialog.showChoiceMsg("Export file created, do you want to open containing folder?",
				this.getContentPane()) == JOptionPane.YES_OPTION) {
				try {
					Desktop.getDesktop().open(results.getParentFile());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		else {
			MessageDialog.showErrorMsg("Failed to create results file", this.getContentPane());
		}
	}

	private void finalizeCompoundCollectionLoad(LoadCompoundCollectionTask task){
		clearPanel();
		CompoundCollection collection = task.getCollection();
		loadCompoundCollectionData(collection);
	}

	private void loadCompoundCollectionData(CompoundCollection collection) {
		// TODO Auto-generated method stub
//		System.out.println("***");
	}

	@Override
	public void designStatusChanged(ExperimentDesignEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void designSetStatusChanged(ExperimentDesignSubsetEvent e) {
		// TODO Auto-generated method stub

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
	public void valueChanged(ListSelectionEvent e) {

		if(e.getValueIsAdjusting() || e.getSource() == null) 
			return;

		for(ListSelectionListener listener : ((DefaultListSelectionModel)e.getSource()).getListSelectionListeners()) {
			
			if(listener.equals(compoundMultiplexListingTable.getTable())) {
				
				Collection<CompoundMultiplexMixture> selectedMultiplexes = 
						compoundMultiplexListingTable.getSelectedMultiplexMixtures();
				if(selectedMultiplexes != null && !selectedMultiplexes.isEmpty()) {
					
					clearDataPanels();
					multiplexComponentsListingTable.setTableModelFromCompoundMultiplexMixtures(selectedMultiplexes);
					return;
				}				
			}
			if(listener.equals(multiplexComponentsListingTable.getTable())) {
				
				CompoundMultiplexMixtureComponent mpc = 						
						multiplexComponentsListingTable.getSelectedCompoundMultiplexMixtureComponent();
				if(mpc != null) {
					
					clearDataPanels();
					CompoundCollectionComponent ccComponent = mpc.getCCComponent();
					propertiesTable.setTableModelFromCompoundCollectionComponent(ccComponent);
					
					CpdMetadataField origSmilesEntry = ccComponent.getMetadata().keySet().stream().
							filter(p -> p.getId().equals("CCCMF00123")).
							findFirst().orElse(null);
					if(origSmilesEntry != null)
						sourceMolStructurePanel.showStructure(ccComponent.getMetadata().get(origSmilesEntry));
					
					CpdMetadataField primarySmilesEntry = ccComponent.getMetadata().keySet().stream().
							filter(p -> p.getId().equals("CCCMF00127")).
							findFirst().orElse(null);
					if(primarySmilesEntry != null)
						primaryMolStructurePanel.showStructure(ccComponent.getMetadata().get(primarySmilesEntry));
					
					if(ccComponent.getMsReadySmiles() != null)
						msReadyMolStructurePanel.showStructure(ccComponent.getMsReadySmiles());
					
					return;
				}
			}
		}	
	}
	
	public void loadCompoundData(CompoundIdentity cpd) {
		
	}
	
	private void clearDataPanels() {

		sourceMolStructurePanel.clearPanel();
		primaryMolStructurePanel.clearPanel();
		msReadyMolStructurePanel.clearPanel();
		propertiesTable.clearTable();
	}

	@Override
	public void clearPanel() {
		
		clearDataPanels();
		multiplexComponentsListingTable.clearTable();
		compoundMultiplexListingTable.clearTable();
	}

	@Override
	public void reloadDesign() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void executeAdminCommand(String command) {
		// TODO Auto-generated method stub

	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}
}
