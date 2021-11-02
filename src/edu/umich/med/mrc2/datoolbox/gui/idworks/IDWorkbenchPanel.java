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

package edu.umich.med.mrc2.datoolbox.gui.idworks;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureSubsetByIdentification;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerFeatureIdentificationProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.IDTrackerMsFeatureProperties;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.MsDepth;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IdFollowupUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IdLevelUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IdentificationUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.StandardAnnotationUtils;
import edu.umich.med.mrc2.datoolbox.gui.annotation.DockableObjectAnnotationPanel;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.CompoundDatabasePanel;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo.DockableCompoundClasyFireViewer;
import edu.umich.med.mrc2.datoolbox.gui.idtable.uni.DockableUniversalIdentificationResultsTable;
import edu.umich.med.mrc2.datoolbox.gui.idtable.uni.UniversalIdentificationResultsTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.export.IDTrackerDataExportDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.AddFeaturesToCollectionDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.FeatureCollectionManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fdr.FDREstimationSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idfus.DockableFollowupStepTable;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idfus.FollowupStepAssignmentDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idfus.IdFollowupStepManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idlevel.IdLevelAssignmentDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idlevel.IdLevelManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.manid.ManualIdentificationDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms1.DockableReferenceMsOneFeatureTable;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms1.IdentificationTableModelListener;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms1.ReferenceMsOneFeaturePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.DockableMSMSFeatureTable;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.DockableMSMSLibraryEntryPropertiesTable;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.DockablePepSearchParameterListingPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.MsMsFeaturePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.SiriusDataExportDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.nistms.NISTMSSerchSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.PepSearchSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.io.PepserchResultsImportDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.project.OpenIDTrackerProjectDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.IDTrackerDataSearchDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.stan.DockableStandardFeatureAnnotationTable;
import edu.umich.med.mrc2.datoolbox.gui.idworks.stan.StandardFeatureAnnotationAssignmentDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.stan.StandardFeatureAnnotationManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.tdexplor.IDTrackerDataExplorerPlotFrame;
import edu.umich.med.mrc2.datoolbox.gui.idworks.tophit.ReassignDefaultMSMSLibraryHitDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.xic.XICSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.msms.DecoyMSMSLibraryImportDialog;
import edu.umich.med.mrc2.datoolbox.gui.io.msms.ReferenceMSMSLibraryExportDialog;
import edu.umich.med.mrc2.datoolbox.gui.library.feditor.DockableMsMsInfoPanel;
import edu.umich.med.mrc2.datoolbox.gui.library.feditor.DockableMsMsTable;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.main.StatusBar;
import edu.umich.med.mrc2.datoolbox.gui.plot.spectrum.DockableSpectumPlot;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.RawDataExaminerPanel;
import edu.umich.med.mrc2.datoolbox.gui.structure.DockableMolStructurePanel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ms.DockableMsTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MSFeatureBundleDataUpdater;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id.DefaultMSMSLibraryHitReassignmentTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id.NISTMsPepSearchRoundTripTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id.NISTMsSearchTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id.NISTMspepSearchOfflineTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id.PercolatorFDREstimationTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMS1FeatureSearchTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMSMSDuplicateMSMSFeatureCleanupTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMSMSFeatureDataPullTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMSMSFeatureSearchTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTrackerDataExportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTrackerProjectDataFetchTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTrackerSiriusMsExportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.ExtendedMSPExportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.ReferenceMSMSLibraryExportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.msms.DecoyLibraryGenerationTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.ChromatogramExtractionTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.RawDataLoadForInjectionsTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.RawDataRepositoryIndexingTask;
import umich.ms.datatypes.LCMSData;

public class IDWorkbenchPanel extends DockableMRC2ToolboxPanel implements MSFeatureBundleDataUpdater{

	private static final Icon componentIcon = GuiUtils.getIcon("missingIdentifications", 16);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "IDWorkbenchPanel.layout");

	private IDWorkbenchToolbar toolbar;
	
	private DockableReferenceMsOneFeatureTable msOneFeatureTable;
	private DockableMSMSFeatureTable msTwoFeatureTable;
	private DockableMolStructurePanel molStructurePanel;
	private DockablePepSearchParameterListingPanel pepSearchParameterListingPanel;
	private DockableObjectAnnotationPanel featureAnnotationPanel;
	private DockableSpectumPlot msOnePlot;
	private DockableSpectumPlot msTwoPlot;
	private DockableMsTable msOneTable;
	private DockableMsMsTable msTwoTable;
	private DockableMsMsInfoPanel msMsInfoPanel;
	private DockableMSMSLibraryEntryPropertiesTable msmsLibraryEntryPropertiesTable;
	private DockableCompoundClasyFireViewer clasyFireViewer;
	
	//	Compound data panels, hide for now
//	private NarrativeDataPanel narrativeDataPanel;
//	private DockableDatabaseLinksTable dbLinksTable;
//	private DockableSynonymsTable synonymsTable;
//	private DockableCompoundPropertiesTable propertiesTable;
//	private DockableConcentrationsTable concentrationsTable;
//	private DockableSpectraTable spectraTable;
	
	private IDSetupDialog idSetupDialog;
	private JFileChooser chooser;
	private File baseDirectory;
	private FileNameExtensionFilter txtFilter;
	private FileNameExtensionFilter xmlFilter;
	private FileNameExtensionFilter mgfFilter;

//	private IDDAImportSetupDialog iddaImportSetupDialog;
	private IDTrackerLimsManagerPanel idTrackerManager;
	private OpenIDTrackerProjectDialog openIDTrackerProjectDialog;
	private LIMSExperiment idTrackerExperiment;

	//	private NISTSearchSetupDialog nistSearchSetupDialog;
	private PepSearchSetupDialog pepSearchSetupDialog;
	private NISTMSSerchSetupDialog nistMSSerchSetupDialog;
	//	private IDTrackerFeatureSearchPanel idTrackerFeatureSearchPanel;
	private DockableUniversalIdentificationResultsTable identificationsTable;
	private FollowupStepAssignmentDialog followupStepAssignmentDialog;
	private DockableFollowupStepTable followupStepTable;
	
	private StandardFeatureAnnotationAssignmentDialog standardFeatureAnnotationAssignmentDialog;
	private DockableStandardFeatureAnnotationTable standardFeatureAnnotationTable;
	
	private Map<LIMSSamplePreparation, Collection<DataAcquisitionMethod>> samplePrepAcquisitionMethodMap;
	private Map<DataAcquisitionMethod, Collection<DataExtractionMethod>> acquisitionDataExtractionMethodMap;
	private Map<DataExtractionMethod,Collection<MsFeature>>featureSetataExtractionMethodMap;
	private Collection<DataAcquisitionMethod>dataAcquisitionMethods;
	
	private ManualIdentificationDialog manualIdentificationDialog;
	private IdFollowupStepManagerDialog idFollowupStepManagerDialog;
	private StandardFeatureAnnotationManagerDialog standardFeatureAnnotationManagerDialog;
	private IdLevelManagerDialog idLevelManagerDialog;
	private IdLevelAssignmentDialog idLevelAssignmentDialog;
	private UniversalIdentificationResultsTablePopupMenu idTablePopupMenu;
	private MsMsFeaturePopupMenu msmsFeaturePopupMenu;
	private ReferenceMsOneFeaturePopupMenu referenceMsOneFeaturePopupMenu;
	private IDTrackerDataExportDialog idTrackerDataExportDialog;
	private XICSetupDialog xicSetupDialog;
	private SiriusDataExportDialog siriusDataExportDialog;
	private IDTrackerDataSearchDialog idTrackerDataSearchDialog;
	private PepserchResultsImportDialog pepSearchResultVerifierDialog;	
	private IDTrackerDataExplorerPlotFrame idTrackerDataExplorerPlotDialog;
	
	private FeatureCollectionManagerDialog featureCollectionManagerDialog;
	private AddFeaturesToCollectionDialog addFeaturesToCollectionDialog;
	private MsFeatureInfoBundleCollection activeFeatureCollection;
	private FDREstimationSetupDialog fdrEstimationSetupDialog;
	private ReassignDefaultMSMSLibraryHitDialog reassignDefaultMSMSLibraryHitDialog;
	private IndeterminateProgressDialog idp;

	public IDWorkbenchPanel() {

		super("IDWorkbenchPanel", PanelList.ID_WORKBENCH.getName(), componentIcon);
		setLayout(new BorderLayout(0, 0));

		toolbar = new IDWorkbenchToolbar(this);
		add(toolbar, BorderLayout.NORTH);

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);

		msOneFeatureTable = new DockableReferenceMsOneFeatureTable(this);
		referenceMsOneFeaturePopupMenu = new ReferenceMsOneFeaturePopupMenu(this);
		msOneFeatureTable.getTable().addTablePopupMenu(referenceMsOneFeaturePopupMenu);

		msTwoFeatureTable = new DockableMSMSFeatureTable(this);
		msmsFeaturePopupMenu = new MsMsFeaturePopupMenu(this);
		msTwoFeatureTable.getTable().addTablePopupMenu(msmsFeaturePopupMenu);
				
		identificationsTable = new DockableUniversalIdentificationResultsTable(
				"IDWorkbenchPanelDockableUniversalIdentificationResultsTable", "Identifications");
		identificationsTable.getTable().getSelectionModel().addListSelectionListener(this);
		identificationsTable.getTable().setIdentificationTableModelListener(
				new IdentificationTableModelListener(identificationsTable.getTable(), this));
		idTablePopupMenu = new UniversalIdentificationResultsTablePopupMenu(this);
		identificationsTable.getTable().addTablePopupMenu(idTablePopupMenu);
		
		molStructurePanel = new DockableMolStructurePanel(
				"IDWorkbenchPanelDockableMolStructurePanel");
		clasyFireViewer = new DockableCompoundClasyFireViewer();
		
//		narrativeDataPanel = new NarrativeDataPanel();
//		dbLinksTable = new DockableDatabaseLinksTable();
//		synonymsTable = new DockableSynonymsTable(this);
//		propertiesTable = new DockableCompoundPropertiesTable();
//		concentrationsTable = new DockableConcentrationsTable();
//		spectraTable = new DockableSpectraTable();

		featureAnnotationPanel = new DockableObjectAnnotationPanel(
				"IDWorkbenchPanelAnnotations", "Feature annotations", 80);
		featureAnnotationPanel.setMsFeatureBundleDataUpdater(this);
		msOnePlot = new DockableSpectumPlot(
				"IDWorkbenchPanelDockableSpectumPlotMS1", "MS1 plot");
		msOneTable = new DockableMsTable(
				"IDWorkbenchPanelDockableMsTableMS1", "MS1 table");
		msTwoPlot = new DockableSpectumPlot(
				"IDWorkbenchPanelDockableSpectumPlotMS2", "MS2 plot");
		msTwoTable = new DockableMsMsTable(
				"IDWorkbenchPanelDockableMsMsTableMs2", "MS2 table");
		msMsInfoPanel = new DockableMsMsInfoPanel(this);
		msmsLibraryEntryPropertiesTable = new DockableMSMSLibraryEntryPropertiesTable();
		pepSearchParameterListingPanel = new DockablePepSearchParameterListingPanel();
		//	idTrackerFeatureSearchPanel =  new IDTrackerFeatureSearchPanel(this);
		followupStepTable = new DockableFollowupStepTable();
		standardFeatureAnnotationTable = new DockableStandardFeatureAnnotationTable();

		grid.add(0, 0, 80, 30, msOneFeatureTable, msTwoFeatureTable
				//, idTrackerFeatureSearchPanel
				);
		grid.add(80, 0, 20, 30, molStructurePanel, clasyFireViewer);
		grid.add(0, 30, 100, 20, identificationsTable);
//		grid.add(0, 50, 50, 50, narrativeDataPanel, synonymsTable,
//				propertiesTable, concentrationsTable, spectraTable);
		grid.add(50, 50, 50, 50, msOnePlot, msOneTable,
				msTwoPlot, msTwoTable, msMsInfoPanel, 
				msmsLibraryEntryPropertiesTable, 
				pepSearchParameterListingPanel, 
				featureAnnotationPanel, followupStepTable,
				standardFeatureAnnotationTable);

//		grid.select(0, 50, 50, 50, narrativeDataPanel);
		grid.select(50, 50, 50, 50, msOnePlot);

		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);

		loadLayout(layoutConfigFile);
		idSetupDialog = new IDSetupDialog(this);
		initDataMaps();
		
		idTrackerDataExplorerPlotDialog = new IDTrackerDataExplorerPlotFrame(this);
		idTrackerDataExplorerPlotDialog.setLocationRelativeTo(this.getContentPane());
	}

	private void initDataMaps() {

		samplePrepAcquisitionMethodMap =
				new TreeMap<LIMSSamplePreparation, Collection<DataAcquisitionMethod>>();
		acquisitionDataExtractionMethodMap =
				new TreeMap<DataAcquisitionMethod, Collection<DataExtractionMethod>>();
		featureSetataExtractionMethodMap =
				new TreeMap<DataExtractionMethod,Collection<MsFeature>>();

		dataAcquisitionMethods = new TreeSet<DataAcquisitionMethod>();
	}

	public void clearDataMaps() {

		samplePrepAcquisitionMethodMap.clear();
		acquisitionDataExtractionMethodMap.clear();
		featureSetataExtractionMethodMap.clear();
		dataAcquisitionMethods.clear();
	}

//	DISABLE_PRIMARY_IDENTIFICATION_COMMAND
//	DELETE_IDENTIFICATION_COMMAND
//	DELETE_ALL_IDENTIFICATIONS_COMMAND
	
	@Override
	public void actionPerformed(ActionEvent event) {

		if(MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg("You are not logged in ID tracker!", this.getContentPane());
			return;
		}
		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.SHOW_OPEN_CPD_ID_PROJECT_DIALOG_COMMAND.getName()))
			showOpenIdProjectDialogue();

		if (command.equals(MainActionCommands.OPEN_CPD_ID_PROJECT_COMMAND.getName()))
			openIdProject();

		if (command.equals(MainActionCommands.ID_SETUP_DIALOG_COMMAND.getName()))
			runIdentificationTask();
		
		if (command.equals(MainActionCommands.SHOW_ID_TRACKER_SEARCH_DIALOG_COMMAND.getName()))
			showTrackerSearchDialog();		

		if (command.equals(MainActionCommands.NIST_MS_SEARCH_SETUP_COMMAND.getName()))
			showNistSearchSetup();
		
		if (command.equals(MainActionCommands.NIST_MS_SEARCH_RUN_COMMAND.getName()))
			runNistMsSearch();

		if (command.equals(MainActionCommands.NIST_MS_PEPSEARCH_SETUP_COMMAND.getName()))
			showPepSearchSetupDiaog(false);
				
		if (command.equals(MainActionCommands.NIST_MS_OFFLINE_PEPSEARCH_SETUP_COMMAND.getName()))
			showPepSearchSetupDiaog(true);
		
		if (command.equals(MainActionCommands.NIST_MS_PEPSEARCH_RUN_COMMAND.getName()))
			runNistMsPepSearch();
		
		if (command.equals(MainActionCommands.NIST_MS_OFFLINE_PEPSEARCH_RUN_COMMAND.getName()))
			runOfflineNistMsPepSearch();
		
		if (command.equals(MainActionCommands.VALIDATE_PEPSEARCH_RESULTS_COMMAND.getName()))
			showNistMsPepSearchResultValidadtionDialog();

//		if (command.equals(MainActionCommands.IDDA_SETUP_DIALOG_COMMAND.getName()))
//			showIddaImportSetupDialog();
//		
//		if (command.equals(MainActionCommands.IDDA_IMPORT_COMMAND.getName()))
//			importIDDAExperiment();

		if (command.equals(MainActionCommands.EXPORT_FEATURES_TO_MSP_COMMAND.getName()))
			exportMsMsfeaturesToMspFile();
				
		if (command.equals(MainActionCommands.SHOW_SIRIUS_MS_EXPORT_DIALOG_COMMAND.getName()))
			siriusMsMsExportSetup();
		
		if (command.equals(MainActionCommands.EXPORT_FEATURES_TO_SIRIUS_MS_COMMAND.getName()))
			exportMsMsfeaturesToSiriusMsFile();

		//	MS1 features commands
		if (command.equals(MainActionCommands.SEARCH_FEATURE_AGAINST_LIBRARY_COMMAND.getName()))
			searchSelectedMsOneFeaturesAgainstLibrary();

		if (command.equals(MainActionCommands.SEARCH_FEATURE_AGAINST_DATABASE_COMMAND.getName()))
			searchSelectedMsOneFeaturesAgainstCompoundDatabase();

		if (command.equals(MainActionCommands.ADD_MANUAL_IDENTIFICATION_COMMAND.getName()))
			addManualIdToSelectedMsOneFeature();

		if (command.equals(MainActionCommands.COPY_SELECTED_MS1_ROWS_COMMAND.getName()))
			copySelectedMsOneFeatures(false);

		if (command.equals(MainActionCommands.COPY_SELECTED_MS1_ROWS_WITH_HEADER_COMMAND.getName()))
			copySelectedMsOneFeatures(true);

		//	MS2 features commands
		if (command.equals(MainActionCommands.ADD_MANUAL_MSMS_IDENTIFICATION_COMMAND.getName()))
			addManualIdToSelectedMsTwoFeature();
		
		if (command.equals(MainActionCommands.SET_MANUAL_FEATURE_ID_COMMAND.getName()))
			setManualIdForSelectedFeature();

		if (command.equals(MainActionCommands.COPY_SELECTED_MS2_ROWS_COMMAND.getName()))
			copySelectedMsTwoFeatures(false);

		if (command.equals(MainActionCommands.COPY_SELECTED_MS2_ROWS_WITH_HEADER_COMMAND.getName()))
			copySelectedMsTwoFeatures(true);
			
		if (command.equals(MainActionCommands.GO_TO_COMPOUND_IN_DATABASE_COMMAND.getName()))
			showDatabaseCompoundForSelectedIdentity();
		
		if (command.equals(MainActionCommands.GO_TO_PRIMARY_COMPOUND_IN_DATABASE_COMMAND.getName()))
			showDatabaseCompoundForPrimaryFeatureIdentity();
		
		if (command.equals(MainActionCommands.DISABLE_PRIMARY_IDENTIFICATION_COMMAND.getName()))
			disablePrimaryIdentificationForSelectedFeatures();
				
		if (command.equals(MainActionCommands.DELETE_IDENTIFICATION_COMMAND.getName()))
			deleteSelectedIdentificationForSelectedFeature();

		if (command.equals(MainActionCommands.DELETE_ALL_IDENTIFICATIONS_COMMAND.getName()))
			clearIdentificationsForSelectedFeature();
			
		if (command.equals(MainActionCommands.SHOW_ID_LEVEL_MANAGER_DIALOG_COMMAND.getName()))
			showIdIdLevelManager();		
		
		if (command.equals(MainActionCommands.SHOW_ID_FOLLOWUP_STEP_MANAGER_DIALOG_COMMAND.getName()))
			showIdFollowupStepManager();
		
		if (command.equals(MainActionCommands.SHOW_STANDARD_FEATURE_ANNOTATION_MANAGER_DIALOG_COMMAND.getName()))
			showStandardFeatureAnnotationManager();
		
		if (command.equals(MainActionCommands.ASSIGN_ID_FOLLOWUP_STEPS_TO_FEATURE_DIALOG_COMMAND.getName()))
			showFeatureIdFollowupStepManager();
		
		if (command.equals(MainActionCommands.SAVE_ID_FOLLOWUP_STEP_ASSIGNMENT_COMMAND.getName()))
			saveFollowupStepsForSelectedFeatures();	
		
		if (command.equals(MainActionCommands.ASSIGN_STANDARD_FEATURE_ANNOTATIONS_TO_FEATURE_DIALOG_COMMAND.getName()))
			showFeatureStandardAnnotationManager();

		if (command.equals(MainActionCommands.SAVE_STANDARD_FEATURE_ANNOTATION_ASSIGNMENT_COMMAND.getName()))
			saveStandardAnnotationsForSelectedFeatures();
		
		if (command.equals(MainActionCommands.SHOW_ID_LEVEL_ASSIGNMENT_DIALOG_COMMAND.getName()))
			showIdLevelAssignmentDialog();
		
		if (command.equals(MainActionCommands.ASSIGN_ID_LEVEL_COMMAND.getName()))
			setIdLevelForIdentification();
		
		for(MSFeatureIdentificationLevel level : IDTDataCash.getMsFeatureIdentificationLevelList()) {
			
			if (command.equals(level.getName())) {
				setIdLevelForIdentification(level.getName());
				return;
			}
			if (command.equals(MSFeatureIdentificationLevel.SET_PRIMARY + level.getName())) {
				setPrimaryIdLevelForMultipleSelectedFeatures(level.getName());
				return;
			}			
		}
		if (command.equals(MainActionCommands.SHOW_IDTRACKER_DATA_EXPORT_DIALOG_COMMAND.getName()))
			showIdTrackerDataExportDialog();
		
		if (command.equals(MainActionCommands.EXPORT_IDTRACKER_DATA_COMMAND.getName()))	
			exportIdTrackerData();
		
		if (command.equals(MainActionCommands.XIC_FOR_FEATURE_DIALOG_COMMAND.getName()))	
			showXicSetupDialog();
		
		if (command.equals(MainActionCommands.EXTRACT_CHROMATOGRAM.getName()))	
			extractXic();
		
		if (command.equals(MainActionCommands.INDEX_RAW_DATA_REPOSITORY_COMMAND.getName()))	
			indexRawDataRepository();			
		
		if (command.equals(MainActionCommands.LOAD_RAW_DATA_FOR_CURRENT_MSMS_FEATURE_SET_COMMAND.getName()))	
			loadRawDataForCurrentMsMsFeatureSet();	
		
		if (command.equals(MainActionCommands.MERGE_DUPLICATES_COMMAND.getName()))	
			mergeDuplicateMsMsFeatures();
		
		if (command.equals(MainActionCommands.EXPORT_REFERENCE_MSMS_LIBRARY_COMMAND.getName()))	
			showRefMSMSLibraryExportDialog();
		
		if (command.equals(MainActionCommands.IMPORT_DECOY_REFERENCE_MSMS_LIBRARY_COMMAND.getName()))	
			showDecoyMSMSLibraryImportDialog();
		
		if (command.equals(MainActionCommands.SHOW_ID_TRACKER_DATA_EXPLORER_PLOT.getName()))	
			showIDTrackerDataExplorerDialog();
		
		if (command.equals(MainActionCommands.RELOAD_ACTIVE_MSMS_FEATURES.getName()))	
			reloadCompleteActiveMSMSFeatureSet();
		
		if (command.equals(MainActionCommands.RELOAD_ACTIVE_MS_ONE_FEATURES.getName()))	
			reloadCompleteActiveMSOneFeatureSet();
		
		if (command.equals(MainActionCommands.SHOW_FEATURE_COLLECTION_MANAGER_DIALOG_COMMAND.getName()))	
			showFeatureCollectionManager();	
		
		if(command.equals(MainActionCommands.CREATE_NEW_MSMS_FEATURE_COLLECTION_FROM_SELECTED.getName()))
			createNewMsmsFeatureCollectionFromSelectedFeatures(msTwoFeatureTable.getBundles(TableRowSubset.SELECTED));
		
		if (command.equals(MainActionCommands.ADD_SELECTED_TO_EXISTING_MSMS_FEATURE_COLLECTION.getName()))
			addSelectedFeaturesToExistingMsMsFeatureCollection(msTwoFeatureTable.getBundles(TableRowSubset.SELECTED));
		
		if (command.equals(MainActionCommands.REMOVE_SELECTED_FROM_ACTIVE_MSMS_FEATURE_COLLECTION.getName()))
			removeSelectedFeaturesFromActiveMsMsFeatureCollection(msTwoFeatureTable.getBundles(TableRowSubset.SELECTED));
		
		if (command.equals(MainActionCommands.SETUP_FDR_ESTIMATION_FOR_LIBRARY_MATCHES.getName()))
			setupFDRforMSMSlibraryIdentifications();
		
		if (command.equals(MainActionCommands.CALCULATE_FDR_FOR_LIBRARY_MATCHES.getName()))
			calculateFDRforMSMSlibraryIdentifications();
		
		if (command.equals(MainActionCommands.SETUP_DEFAULT_MSMS_LIBRARY_MATCH_REASSIGNMENT.getName()))
			setupTopMSMSHitsReassignment();
		
		if (command.equals(MainActionCommands.REASSIGN_DEFAULT_MSMS_LIBRARY_MATCHES.getName()))
			reassignTopMSMSHits();
	}

	private void setupTopMSMSHitsReassignment() {
		
		Collection<MsFeatureInfoBundle> allFeatures = 
				msTwoFeatureTable.getBundles(TableRowSubset.ALL);
		if(allFeatures.isEmpty())
			return;

		reassignDefaultMSMSLibraryHitDialog = new ReassignDefaultMSMSLibraryHitDialog(this);
		
		if(activeFeatureCollection.isOffLine())
			reassignDefaultMSMSLibraryHitDialog.blockCommitToDatabase();
		
		reassignDefaultMSMSLibraryHitDialog.setLocationRelativeTo(this.getContentPane());
		reassignDefaultMSMSLibraryHitDialog.setVisible(true);
	}
		
	private void reassignTopMSMSHits() {
		
		Collection<MsFeatureInfoBundle> allFeatures = 
				msTwoFeatureTable.getBundles(TableRowSubset.ALL);
		if(allFeatures.isEmpty())
			return;
		
		DefaultMSMSLibraryHitReassignmentTask task = 
				new DefaultMSMSLibraryHitReassignmentTask(
						allFeatures, 
						reassignDefaultMSMSLibraryHitDialog.getTopHitReassignmentOption(),
						reassignDefaultMSMSLibraryHitDialog.commitChangesTodatabase());
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
		reassignDefaultMSMSLibraryHitDialog.dispose();
	}

	private void setupFDRforMSMSlibraryIdentifications() {
		
		Collection<MsFeatureInfoBundle> allFeatures = 
				msTwoFeatureTable.getBundles(TableRowSubset.ALL);
		if(allFeatures.isEmpty())
			return;
		
		Map<NISTPepSearchParameterObject, Long>paramCounts = 
				getPepSearchParameterSetCountsForDataSet(allFeatures);
		
		if(paramCounts.size() == 0) {
			MessageDialog.showWarningMsg(
					"No library search results available.", this.getContentPane());
			return;
		}				
		fdrEstimationSetupDialog = new FDREstimationSetupDialog(this);
		fdrEstimationSetupDialog.loadNISTPepSearchParameterObjects(paramCounts);
		fdrEstimationSetupDialog.loadDefaultDecoySearchparameters();
		fdrEstimationSetupDialog.setLocationRelativeTo(this.getContentPane());
		fdrEstimationSetupDialog.setVisible(true);
	}
	
	private Map<NISTPepSearchParameterObject, Long> getPepSearchParameterSetCountsForDataSet(
			Collection<MsFeatureInfoBundle> msmsFeatures) {
		
		Map<NISTPepSearchParameterObject, Long> paramCounts = msmsFeatures.stream().
			filter(f -> f.getMsFeature().getPrimaryIdentity() != null).
			map(f -> f.getMsFeature().getPrimaryIdentity()).
			filter(id -> id.getReferenceMsMsLibraryMatch() != null).
			map(id -> IDTDataCash.getNISTPepSearchParameterObjectById(
					id.getReferenceMsMsLibraryMatch().getSearchParameterSetId())).
			filter(o -> o != null).
//			filter(o -> o.getHiResSearchOption().equals(HiResSearchOption.z)).
			collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
				
		return paramCounts;
	}
	
	private void calculateFDRforMSMSlibraryIdentifications() {
		
		Collection<String> errors = fdrEstimationSetupDialog.validateParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), fdrEstimationSetupDialog);
			return;
		}
		NISTPepSearchParameterObject paramSet = 
				fdrEstimationSetupDialog.getActiveNISTPepSearchParameterObject();
		String paramSetId = paramSet.getId();
		
		Collection<File> decoys = fdrEstimationSetupDialog.getSelectedDecoyLibraries();
		paramSet.getLibraryFiles().clear();
		paramSet.getLibraryFiles().addAll(decoys);
		
		//	Features for selected parameter set
		Collection<MsFeatureInfoBundle> allFeatures = 
				msTwoFeatureTable.getBundles(TableRowSubset.ALL);
		Collection<MsFeatureInfoBundle> featuresToSearch = allFeatures.stream().
			filter(f -> f.getMsFeature().getPrimaryIdentity() != null).
			filter(f -> f.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch() != null).
			filter(f -> f.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch().getSearchParameterSetId() != null).
			filter(f -> f.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch().getSearchParameterSetId().equals(paramSetId)).
			collect(Collectors.toList());
		
		//	Check # of decoy hits relative to normal hits (consider only best hits/primary IDs based on library search 
		PercolatorFDREstimationTask task = 
				new PercolatorFDREstimationTask(
						featuresToSearch,
						paramSet);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
		
		fdrEstimationSetupDialog.dispose();
	}
	
	private void showFeatureCollectionManager() { 
		
		featureCollectionManagerDialog = new FeatureCollectionManagerDialog();
		featureCollectionManagerDialog.setLocationRelativeTo(this.getContentPane());
		featureCollectionManagerDialog.setVisible(true);
	}
	
	private void showIDTrackerDataExplorerDialog() {		
		idTrackerDataExplorerPlotDialog.setVisible(true);
	}
	
	private void showDecoyMSMSLibraryImportDialog() {
		
		DecoyMSMSLibraryImportDialog dialog = new DecoyMSMSLibraryImportDialog(this);
		dialog.setLocationRelativeTo(this.getContentPane());
		dialog.setVisible(true);
	}
	
	private void showRefMSMSLibraryExportDialog() {
		
		ReferenceMSMSLibraryExportDialog dialog = new ReferenceMSMSLibraryExportDialog(this);
		dialog.setLocationRelativeTo(this.getContentPane());
		dialog.setVisible(true);
	}
	
	private void mergeDuplicateMsMsFeatures() {
		
		LIMSExperiment experiment = IDTDataCash.getExperimentById("IDX0065");
		IDTMSMSDuplicateMSMSFeatureCleanupTask task = 
				new IDTMSMSDuplicateMSMSFeatureCleanupTask(Polarity.Positive, experiment);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}	

	private void showNistMsPepSearchResultValidadtionDialog() {

		pepSearchResultVerifierDialog = new PepserchResultsImportDialog();
		pepSearchResultVerifierDialog.setLocationRelativeTo(this.getContentPane());
		pepSearchResultVerifierDialog.setVisible(true);
	}

	private void showTrackerSearchDialog() {

//		ShowTrackerSearchDialogTask task = 
//				new ShowTrackerSearchDialogTask();
//		idp = new IndeterminateProgressDialog(
//				"Populating IDTracker search form data ...", this.getContentPane(), task);
//		idp.setLocationRelativeTo(this.getContentPane());
//		idp.setVisible(true);
		
		idTrackerDataSearchDialog = new IDTrackerDataSearchDialog(this);
		idTrackerDataSearchDialog.setLocationRelativeTo(this.getContentPane());
		idTrackerDataSearchDialog.setVisible(true);
	}
	
	class ShowTrackerSearchDialogTask extends LongUpdateTask {

		public ShowTrackerSearchDialogTask() {

		}

		@Override
		public Void doInBackground() {

			idTrackerDataSearchDialog = new IDTrackerDataSearchDialog(IDWorkbenchPanel.this);
			idTrackerDataSearchDialog.setLocationRelativeTo(IDWorkbenchPanel.this.getContentPane());
			idp.dispose();
			idTrackerDataSearchDialog.setVisible(true);
			return null;
		}
	}

	private void showPepSearchSetupDiaog(boolean runOffline) {

		pepSearchSetupDialog = new PepSearchSetupDialog(this);
		if(runOffline)
			pepSearchSetupDialog.runOffline();
		
		pepSearchSetupDialog.setLocationRelativeTo(this.getContentPane());
		pepSearchSetupDialog.setVisible(true);
	}

	private void showIddaImportSetupDialog() {

//		if(iddaImportSetupDialog == null)
//			iddaImportSetupDialog = new IDDAImportSetupDialog(this);
//
//		iddaImportSetupDialog.setLocationRelativeTo(this.getContentPane());
//		iddaImportSetupDialog.setVisible(true);
	}

	private void showNistSearchSetup() {

		nistMSSerchSetupDialog = new NISTMSSerchSetupDialog(this);
		nistMSSerchSetupDialog.setLocationRelativeTo(this.getContentPane());
		nistMSSerchSetupDialog.setVisible(true);
	}

	private void indexRawDataRepository() {
		
		String repositoryPath = MRC2ToolBoxConfiguration.getRawDataRepository();
		if(repositoryPath == null) {
			MessageDialog.showErrorMsg(
					"Raw data repository has to be specified in program preferences.", 
					this.getContentPane());
			return;
		}
		File rawDataRepository = new File(repositoryPath);
		if(!rawDataRepository.exists()) {
			MessageDialog.showErrorMsg(
					"Selected raw data repository at \"" + 
						MRC2ToolBoxConfiguration.getRawDataRepository() + "\" does not exist.", 
					this.getContentPane());
			return;
		}
		RawDataRepositoryIndexingTask task = new RawDataRepositoryIndexingTask();
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	private void loadRawDataForCurrentMsMsFeatureSet() {

		Collection<MsFeatureInfoBundle> featureSet = 
				msTwoFeatureTable.getBundles(TableRowSubset.ALL);
		if(featureSet.isEmpty()) {
			MessageDialog.showWarningMsg("No MSMS features loaded", this.getContentPane());
			return;
		}
//		if(!RawDataManager.rawDataIndexed()) {
//			MessageDialog.showWarningMsg("Please index the raw data repository first!", this.getContentPane());
//			return;
//		}
		Set<String> injectionIds = 
				featureSet.stream().map(b -> b.getInjectionId()).
				distinct().collect(Collectors.toSet());
		
		RawDataLoadForInjectionsTask task = new RawDataLoadForInjectionsTask(injectionIds);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	private void extractXic() {

		Collection<String>errors = xicSetupDialog.veryfyParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), xicSetupDialog);
			return;
		}
		ChromatogramExtractionTask task = xicSetupDialog.createChromatogramExtractionTask();
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
		xicSetupDialog.dispose();
	}

	private void showXicSetupDialog() {
		
		MsFeatureInfoBundle bundle = null;
		if(msOneFeatureTable.getSelectedBundle() != null)
			bundle = msOneFeatureTable.getSelectedBundle();
		
		if(msTwoFeatureTable.getSelectedBundle() != null)
			bundle = msTwoFeatureTable.getSelectedBundle();
		
		if(bundle == null)
			return;
		
		if(RawDataManager.getFileLocationMap().isEmpty()) {
			MessageDialog.showWarningMsg("Raw data repository not indexed.\n"
					+ "Please index the repository or load raw data files for current feature set",
					this.getContentPane());
			return;
		}
		DataFile dataFile = RawDataManager.getDataFileForInjectionId(bundle.getInjectionId());
		if(dataFile == null) { 
			MessageDialog.showErrorMsg("Could not find data for injection ID " + bundle.getInjectionId(),
					this.getContentPane());
			return;
		}
		if(dataFile.getFullPath() == null) { 
			MessageDialog.showErrorMsg("Raw data file \"" + dataFile.getName() + "\" is not in repository\n"
					+ "Please convert the file to mzML or mzXML format and place in in the repository first.",
					this.getContentPane());
			return;
		}
		LCMSData rawData = RawDataManager.getRawDataForInjectionId(bundle.getInjectionId());
		if(rawData == null) {
			MessageDialog.showErrorMsg("Couldn't load raw data for \"" + dataFile.getName(),
					this.getContentPane());
			return;
		}
		xicSetupDialog = new XICSetupDialog(this, bundle);
		xicSetupDialog.setLocationRelativeTo(this.getContentPane());
		xicSetupDialog.setVisible(true);
	}

	private void showIdTrackerDataExportDialog() {

		idTrackerDataExportDialog = new IDTrackerDataExportDialog(this);
		idTrackerDataExportDialog.setLocationRelativeTo(this.getContentPane());
		idTrackerDataExportDialog.setVisible(true);
	}

	private void exportIdTrackerData() {
				
		MsDepth msLevel = idTrackerDataExportDialog.getMsLevel();
		TableRowSubset featureSubset = idTrackerDataExportDialog.getFeatureSubset();
		FeatureSubsetByIdentification idSubset = 
				idTrackerDataExportDialog.getFeatureSubsetByIdentification();
		
		Collection<MsFeatureInfoBundle>featuresToExport = null;
		if(msLevel.equals(MsDepth.MS1)) {
			featuresToExport = msOneFeatureTable.getBundles(featureSubset);
			if(idSubset.equals(FeatureSubsetByIdentification.IDENTIFIED_ONLY))
				featuresToExport = featuresToExport.stream().
				filter(f -> f.getMsFeature().getPrimaryIdentity() != null).
				collect(Collectors.toList());
			
			if(idSubset.equals(FeatureSubsetByIdentification.UNKNOWN_ONLY))
				featuresToExport = featuresToExport.stream().
				filter(f -> f.getMsFeature().getPrimaryIdentity() == null).
				collect(Collectors.toList());
			
			if(featuresToExport.isEmpty()) {
				MessageDialog.showWarningMsg("No MS1 features to export using selected settings", 
						idTrackerDataExportDialog);
				return;
			}
		}
		if(msLevel.equals(MsDepth.MS2)) {
			featuresToExport = msTwoFeatureTable.getBundles(featureSubset);
			if(idSubset.equals(FeatureSubsetByIdentification.IDENTIFIED_ONLY))
				featuresToExport = featuresToExport.stream().
				filter(f -> f.getMsFeature().getPrimaryIdentity() != null).
				collect(Collectors.toList());
			
			if(idSubset.equals(FeatureSubsetByIdentification.UNKNOWN_ONLY))
				featuresToExport = featuresToExport.stream().
				filter(f -> f.getMsFeature().getPrimaryIdentity() == null).
				collect(Collectors.toList());
			
			if(featuresToExport.isEmpty()) {
				MessageDialog.showWarningMsg("No MS2 features to export using selected settings", 
						idTrackerDataExportDialog);
				return;
			}
		}		
		Collection<String> errors = idTrackerDataExportDialog.validateFormParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), idTrackerDataExportDialog);
			return;
		}	
		Collection<IDTrackerMsFeatureProperties> featurePropertyList = 
				idTrackerDataExportDialog.getSelectedFeatureProperties();
		Collection<IDTrackerFeatureIdentificationProperties>identificationDetailsList =  
				idTrackerDataExportDialog.getSelectedIdentificationProperties();
		
		boolean removeRedundant = idTrackerDataExportDialog.removeRedundant();
		double redundantMzWindow = idTrackerDataExportDialog.getRedundantMzWindow();
		MassErrorType redMzErrorType = idTrackerDataExportDialog.getRedundantMzErrorType();
		double redundantRTWindow = idTrackerDataExportDialog.getRedundantRTWindow();
		
		IDTrackerDataExportTask task = new IDTrackerDataExportTask(
				 msLevel, 
				 featuresToExport,
				 featurePropertyList,
				 identificationDetailsList, 
				 removeRedundant,
				 redundantMzWindow,
				 redMzErrorType,
				 redundantRTWindow,
				 idTrackerDataExportDialog.getOutputFile());
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);		
		idTrackerDataExportDialog.dispose();
	}

	private void setIdLevelForIdentification(String idLevelName) {

		MsFeatureIdentity identity = identificationsTable.getSelectedIdentity();
		if(identity == null)
			return;
		
		MSFeatureIdentificationLevel idLevel = 
				IDTDataCash.getMSFeatureIdentificationLevelByName(idLevelName);
		if(idLevel == null && identity.getIdentificationLevel() == null)
			return;
		
		if(idLevel != null && identity.getIdentificationLevel() != null) {
			
			if(idLevel.equals(identity.getIdentificationLevel())) 
				return;
		}
		identity.setIdentificationLevel(idLevel);
		if(msOneFeatureTable.getSelectedBundle() != null) {
			try {
				IdLevelUtils.setIdLevelForReferenceMS1FeatureIdentification(identity);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			updateSelectedMSFeatures();			
		}
		if(msTwoFeatureTable.getSelectedBundle() != null) {
			try {
				IdLevelUtils.setIdLevelForMSMSFeatureIdentification(identity);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			updateSelectedMSMSFeatures();			
		}
	}
		
	private void setPrimaryIdLevelForMultipleSelectedFeatures(String idLevelName) {

		MSFeatureIdentificationLevel idLevel = 
				IDTDataCash.getMSFeatureIdentificationLevelByName(idLevelName);
		
		if(!msOneFeatureTable.getBundles(TableRowSubset.SELECTED).isEmpty()) {
			
			
			for(MsFeatureInfoBundle fib : msOneFeatureTable.getBundles(TableRowSubset.SELECTED)) {
				
				MsFeatureIdentity identity = fib.getMsFeature().getPrimaryIdentity();
				if(identity == null)
					continue;				

				if(idLevel == null && identity.getIdentificationLevel() == null)
					continue;
				
				if(idLevel != null && identity.getIdentificationLevel() != null) {
					
					if(idLevel.equals(identity.getIdentificationLevel())) 
						continue;
				}
				identity.setIdentificationLevel(idLevel);
				try {
					IdLevelUtils.setIdLevelForReferenceMS1FeatureIdentification(identity);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//	updateMSFeatures(fib);
			}
			updateSelectedMSFeatures();
			return;
		}
		if(!msTwoFeatureTable.getBundles(TableRowSubset.SELECTED).isEmpty()) {
			
			for(MsFeatureInfoBundle fib : msTwoFeatureTable.getBundles(TableRowSubset.SELECTED)) {
				
				MsFeatureIdentity identity = fib.getMsFeature().getPrimaryIdentity();
				if(identity == null)
					continue;				

				if(idLevel == null && identity.getIdentificationLevel() == null)
					continue;
				
				if(idLevel != null && identity.getIdentificationLevel() != null) {
					
					if(idLevel.equals(identity.getIdentificationLevel())) 
						continue;
				}
				identity.setIdentificationLevel(idLevel);
				try {
					IdLevelUtils.setIdLevelForMSMSFeatureIdentification(identity);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//updateMSMSFeature(fib);
			}
			updateSelectedMSMSFeatures();
			return;
		}
	}

	private void setIdLevelForIdentification() {

		MsFeatureIdentity identity = identificationsTable.getSelectedIdentity();
		if(identity == null)
			return;
		
		MSFeatureIdentificationLevel idLevel = idLevelAssignmentDialog.getSelectedLevel();
		if(idLevel == null && identity.getIdentificationLevel() == null) {
			idLevelAssignmentDialog.dispose();
			return;
		}
		if(idLevel != null && identity.getIdentificationLevel() != null) {
			
			if(idLevel.equals(identity.getIdentificationLevel())) {
				idLevelAssignmentDialog.dispose();
				return;
			}
		}
		identity.setIdentificationLevel(idLevel);
		if(msOneFeatureTable.getSelectedBundle() != null) {
			try {
				IdLevelUtils.setIdLevelForReferenceMS1FeatureIdentification(identity);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			updateSelectedMSFeatures();			
		}
		if(msTwoFeatureTable.getSelectedBundle() != null) {
			try {
				IdLevelUtils.setIdLevelForMSMSFeatureIdentification(identity);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			updateSelectedMSMSFeatures();			
		}
		idLevelAssignmentDialog.dispose();
	}

	private void showIdLevelAssignmentDialog() {

		if(identificationsTable.getSelectedIdentity() == null)
			return;
		
		idLevelAssignmentDialog = new IdLevelAssignmentDialog(
				identificationsTable.getSelectedIdentity().getIdentificationLevel(), 
				this);
		idLevelAssignmentDialog.setLocationRelativeTo(this.getContentPane());
		idLevelAssignmentDialog.setVisible(true);
	}

	private void showFeatureIdFollowupStepManager() {

		if(msOneFeatureTable.getSelectedBundle() == null && msTwoFeatureTable.getSelectedBundle() == null)
			return;
		
		if(msOneFeatureTable.getSelectedBundle() != null)
			followupStepAssignmentDialog = 
				new FollowupStepAssignmentDialog(this, msOneFeatureTable.getSelectedBundle());
		
		if(msTwoFeatureTable.getSelectedBundle() != null)
			followupStepAssignmentDialog = 
				new FollowupStepAssignmentDialog(this, msTwoFeatureTable.getSelectedBundle());
		
		followupStepAssignmentDialog.setLocationRelativeTo(this.getContentPane());
		followupStepAssignmentDialog.setVisible(true);
	}
	
	private void saveFollowupStepsForSelectedFeatures() {
		
		if(!msOneFeatureTable.getBundles(TableRowSubset.SELECTED).isEmpty()) {
			
			for(MsFeatureInfoBundle fib : msOneFeatureTable.getBundles(TableRowSubset.SELECTED)) {
			
				fib.getIdFollowupSteps().clear();
				fib.getIdFollowupSteps().addAll(followupStepAssignmentDialog.getUsedFollowupSteps());
				if(!activeFeatureCollection.isOffLine()) {
					try {
						IdFollowupUtils.setIdFollowupStepsForMS1Feature(fib);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			updateSelectedMSFeatures();
			followupStepAssignmentDialog.dispose();
			return;
		}
		if(!msTwoFeatureTable.getBundles(TableRowSubset.SELECTED).isEmpty()) {
			
			for(MsFeatureInfoBundle fib : msTwoFeatureTable.getBundles(TableRowSubset.SELECTED)) {
			
				fib.getIdFollowupSteps().clear();
				fib.getIdFollowupSteps().addAll(followupStepAssignmentDialog.getUsedFollowupSteps());
				if(!activeFeatureCollection.isOffLine()) {
					try {
						IdFollowupUtils.setIdFollowupStepsForMSMSFeature(fib);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			updateSelectedMSMSFeatures();
			followupStepAssignmentDialog.dispose();
			return;
		}
	}
	
	private void showFeatureStandardAnnotationManager() {
		
		if(msOneFeatureTable.getSelectedBundle() == null 
				&& msTwoFeatureTable.getSelectedBundle() == null)
			return;
		
		//	StandardFeatureAnnotationAssignmentDialog standardFeatureAnnotationAssignmentDialog;
		if(msOneFeatureTable.getSelectedBundle() != null)
			standardFeatureAnnotationAssignmentDialog = 
				new StandardFeatureAnnotationAssignmentDialog(this, msOneFeatureTable.getSelectedBundle());
		
		if(msTwoFeatureTable.getSelectedBundle() != null)
			standardFeatureAnnotationAssignmentDialog = 
				new StandardFeatureAnnotationAssignmentDialog(this, msTwoFeatureTable.getSelectedBundle());
		
		standardFeatureAnnotationAssignmentDialog.setLocationRelativeTo(this.getContentPane());
		standardFeatureAnnotationAssignmentDialog.setVisible(true);
	}
	
	private void saveStandardAnnotationsForSelectedFeatures() {
		
		if(!msOneFeatureTable.getBundles(TableRowSubset.SELECTED).isEmpty()) {
			
			for(MsFeatureInfoBundle fib : msOneFeatureTable.getBundles(TableRowSubset.SELECTED)) {
			
				fib.getStandadAnnotations().clear();
				fib.getStandadAnnotations().addAll(standardFeatureAnnotationAssignmentDialog.getUsedAnnotations());
				if(!activeFeatureCollection.isOffLine()) {
					try {
						StandardAnnotationUtils.setStandardFeatureAnnotationsForMS1Feature(fib);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			updateSelectedMSFeatures();
			standardFeatureAnnotationAssignmentDialog.dispose();
			return;
		}
		if(!msTwoFeatureTable.getBundles(TableRowSubset.SELECTED).isEmpty()) {
			
			for(MsFeatureInfoBundle fib : msTwoFeatureTable.getBundles(TableRowSubset.SELECTED)) {
			
				fib.getStandadAnnotations().clear();
				fib.getStandadAnnotations().addAll(standardFeatureAnnotationAssignmentDialog.getUsedAnnotations());
				if(!activeFeatureCollection.isOffLine()) {
					try {
						StandardAnnotationUtils.setStandardFeatureAnnotationsForMSMSFeature(fib);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			updateSelectedMSMSFeatures();
			standardFeatureAnnotationAssignmentDialog.dispose();
			return;
		}
	}
		
	public void createNewMsmsFeatureCollectionFromSelectedFeatures(
			Collection<MsFeatureInfoBundle> featuresToAdd) {
		
		if(featuresToAdd.isEmpty() || activeFeatureCollection.isOffLine())
			return;
		
		featureCollectionManagerDialog = new FeatureCollectionManagerDialog();
		featureCollectionManagerDialog.setFeaturesToAdd(featuresToAdd);
		featureCollectionManagerDialog.setLocationRelativeTo(this.getContentPane());
//		featureCollectionManagerDialog.setVisible(true);
		featureCollectionManagerDialog.showMsFeatureCollectionEditorDialog(null);
	}
	
	public void addSelectedFeaturesToExistingMsMsFeatureCollection(
			Collection<MsFeatureInfoBundle> featuresToAdd) {
		
		if(featuresToAdd.isEmpty() || activeFeatureCollection.isOffLine())
			return;
		
		if(FeatureCollectionManager.getEditableMsFeatureInformationBundleCollectionList().isEmpty()) {
			MessageDialog.showWarningMsg("No custom collections defined yet.\n"
					+ "Use \"Create new feature collection\" option instead.", this.getContentPane());
			return;
		}
		addFeaturesToCollectionDialog = new AddFeaturesToCollectionDialog(featuresToAdd, this);
		addFeaturesToCollectionDialog.setLocationRelativeTo(this.getContentPane());
		addFeaturesToCollectionDialog.setVisible(true);
	}
	
	public void removeSelectedFeaturesFromActiveMsMsFeatureCollection(
			Collection<MsFeatureInfoBundle> featuresToRemove) {
		
		if(activeFeatureCollection == null 
				|| activeFeatureCollection.getFeatures().isEmpty()
				|| activeFeatureCollection.isOffLine())
			return;
		
		if(activeFeatureCollection.equals(FeatureCollectionManager.msmsSearchResults) 
				|| activeFeatureCollection.equals(FeatureCollectionManager.msOneSearchResults)) {
			MessageDialog.showWarningMsg("Collection \"" + activeFeatureCollection.getName() + 
					"\" represents database search results and can not be edited.", this.getContentPane());
			return;
		}
		String message = 
				"Do you really want to remove " + Integer.toString(featuresToRemove.size()) + 
				" selected features from collection \"" + 
				activeFeatureCollection.getName() + "\"?";
		int res = MessageDialog.showChoiceWithWarningMsg(message, this.getContentPane());
		if(res != JOptionPane.YES_OPTION)
			return;
		
		activeFeatureCollection.removeFeatures(featuresToRemove);
		FeatureCollectionManager.removeFeaturesFromCollection(
				activeFeatureCollection, featuresToRemove);
		
		loadMSMSFeatureInformationBundleCollection(activeFeatureCollection);
	}

	private void showIdFollowupStepManager() {
			
		idFollowupStepManagerDialog = new IdFollowupStepManagerDialog();
		idFollowupStepManagerDialog.setLocationRelativeTo(this.getContentPane());
		idFollowupStepManagerDialog.setVisible(true);
	}
	
	private void showStandardFeatureAnnotationManager() {
		
		standardFeatureAnnotationManagerDialog = new StandardFeatureAnnotationManagerDialog();
		standardFeatureAnnotationManagerDialog.setLocationRelativeTo(this.getContentPane());
		standardFeatureAnnotationManagerDialog.setVisible(true);
	}

	private void showIdIdLevelManager() {

		idLevelManagerDialog = new IdLevelManagerDialog();
		idLevelManagerDialog.setLocationRelativeTo(this.getContentPane());
		idLevelManagerDialog.addListener(idTablePopupMenu);		
		idLevelManagerDialog.addListener(msmsFeaturePopupMenu);
		idLevelManagerDialog.addListener(referenceMsOneFeaturePopupMenu);		
		idLevelManagerDialog.setVisible(true);		
	}

	private void copySelectedMsOneFeatures(boolean includeHeader) {
		// TODO Auto-generated method stub
		MessageDialog.showInfoMsg("Under construction ...", this.getContentPane());
	}

	private void copySelectedMsTwoFeatures(boolean includeHeader) {
		// TODO Auto-generated method stub
		MessageDialog.showInfoMsg("Under construction ...", this.getContentPane());
	}

	private void clearIdentificationsForSelectedFeature() {

		if(msOneFeatureTable.getSelectedBundle() == null && msTwoFeatureTable.getSelectedBundle() == null)
			return;
		
		if(msOneFeatureTable.getSelectedBundle() != null) {
			MsFeature feature = msOneFeatureTable.getSelectedBundle().getMsFeature();
			String yesNoQuestion = "Do you want to remove all identifications for " + feature.getName() + "?";
			if(MessageDialog.showChoiceWithWarningMsg(yesNoQuestion, this.getContentPane()) == JOptionPane.YES_OPTION) {
				
				if(!activeFeatureCollection.isOffLine()) {
					try {
						IdentificationUtils.clearReferenceMS1FeatureLibraryMatches(feature.getId());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					try {
						IdentificationUtils.clearReferenceMS1FeatureManualIds(feature.getId());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				feature.clearIdentification();
				updateSelectedMSFeatures();
			}
		}
		if(msTwoFeatureTable.getSelectedBundle() != null) {
			MsFeature feature = msTwoFeatureTable.getSelectedBundle().getMsFeature();
			String yesNoQuestion = "Do you want to remove all identifications for " + feature.getName() + "?";
			if(MessageDialog.showChoiceWithWarningMsg(yesNoQuestion, this.getContentPane()) == JOptionPane.YES_OPTION) {
				TandemMassSpectrum msms = feature.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
				if(msms != null) {
					if(!activeFeatureCollection.isOffLine()) {
						try {					
							IdentificationUtils.clearMSMSFeatureLibraryMatches(msms.getId());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							IdentificationUtils.clearMSMSFeatureManualIdentifications(msms.getId());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					feature.clearIdentification();
					updateSelectedMSMSFeatures();
				}
			}
		}
	}
	
	private void disablePrimaryIdentificationForSelectedFeatures() {

		Collection<MsFeatureInfoBundle> msOneSelectedBundles = 
				msOneFeatureTable.getBundles(TableRowSubset.SELECTED);
		Collection<MsFeatureInfoBundle> msTwoSelectedBundles = 
				msTwoFeatureTable.getBundles(TableRowSubset.SELECTED);
		
		if(msOneSelectedBundles.isEmpty() && msTwoSelectedBundles.isEmpty())
			return;
		
		if(!msOneSelectedBundles.isEmpty()) {
			
			for(MsFeatureInfoBundle bundle : msOneSelectedBundles) {
					
				MsFeature msOneFeature = bundle.getMsFeature();
				msOneFeature.disablePrimaryIdentity();
				if(!activeFeatureCollection.isOffLine()) {
					try {
						IdentificationUtils.disableReferenceMS1FeaturePrimaryIdentity(msOneFeature.getId());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		if(!msTwoSelectedBundles.isEmpty()) {
			
			for(MsFeatureInfoBundle bundle : msTwoSelectedBundles) {
				
				MsFeature msTwoFeature = bundle.getMsFeature();
				msTwoFeature.disablePrimaryIdentity();
				if(!activeFeatureCollection.isOffLine()) {
					try {
						String msmsId = msTwoFeature.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL).getId();
						IdentificationUtils.disableMSMSFeaturePrimaryIdentity(msmsId);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}	
		updateSelectedFeatures();
	}
	
	@Override
	public void updateSelectedFeatures() {
		
		if(!msOneFeatureTable.getBundles(TableRowSubset.SELECTED).isEmpty())
			updateSelectedMSFeatures();

		if(!msTwoFeatureTable.getBundles(TableRowSubset.SELECTED).isEmpty()) 
			updateSelectedMSMSFeatures();		
	}
	
	private void showDatabaseCompoundForSelectedIdentity() {
		
		MsFeatureIdentity id = identificationsTable.getSelectedIdentity();
		if(id == null)
			return;
		
		CompoundIdentity cpdId = id.getCompoundIdentity();
		if(cpdId == null)
			return;
		
		CompoundDatabasePanel cpdDatabasePanel = 
				(CompoundDatabasePanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.DATABASE);
		
		cpdDatabasePanel.loadCompoundDataByReference(Collections.singleton(cpdId));		
		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.DATABASE);
	}
	
	private void showDatabaseCompoundForPrimaryFeatureIdentity() {
		
		if(msOneFeatureTable.getSelectedBundle() == null && msTwoFeatureTable.getSelectedBundle() == null)
			return;
		
		CompoundIdentity cpdId = null;
		if(msOneFeatureTable.getSelectedBundle() != null)
			cpdId = msOneFeatureTable.getSelectedBundle().getMsFeature().getPrimaryIdentity().getCompoundIdentity();

		if(msTwoFeatureTable.getSelectedBundle() != null)
			cpdId = msTwoFeatureTable.getSelectedBundle().getMsFeature().getPrimaryIdentity().getCompoundIdentity();
		
		if(cpdId == null)
			return;
		
		CompoundDatabasePanel cpdDatabasePanel = 
				(CompoundDatabasePanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.DATABASE);
		
		cpdDatabasePanel.loadCompoundDataByReference(Collections.singleton(cpdId));		
		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.DATABASE);
	}

	private void deleteSelectedIdentificationForSelectedFeature() {

		if(msOneFeatureTable.getSelectedBundle() == null && msTwoFeatureTable.getSelectedBundle() == null)
			return;
		
		MsFeatureIdentity id = identificationsTable.getSelectedIdentity();
		if(id == null)
			return;

		if(msOneFeatureTable.getSelectedBundle() != null) {
			MsFeature feature = msOneFeatureTable.getSelectedBundle().getMsFeature();
			if(feature.getIdentifications().size() > 1 && feature.getPrimaryIdentity().equals(id)) {
				
				MessageDialog.showWarningMsg(
						"You are trying to remove primary identification.\n"
						+ "Please specify new primary identification first.", 
						this.getContentPane());
				return;
			}
			String yesNoQuestion = "Do you want to remove identification " + 
					id.getCompoundIdentity().getName() +" for " + feature.getName() + "?";
			if(MessageDialog.showChoiceWithWarningMsg(yesNoQuestion, this.getContentPane()) == JOptionPane.YES_OPTION) {
				
				if(id.getMsRtLibraryMatch() != null) {
					if(!activeFeatureCollection.isOffLine()) {
						try {
							IdentificationUtils.removeReferenceMS1FeatureLibraryMatch(id);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else {
					if(!activeFeatureCollection.isOffLine()) {
						try {
							IdentificationUtils.removeReferenceMS1FeatureManualId(id);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				feature.removeIdentity(id);
				updateSelectedMSFeatures();
			}
		}
		if(msTwoFeatureTable.getSelectedBundle() != null) {
			MsFeature feature = msTwoFeatureTable.getSelectedBundle().getMsFeature();
			if(feature.getIdentifications().size() > 1 && feature.getPrimaryIdentity().equals(id)) {
				
				MessageDialog.showWarningMsg(
						"You are trying to remove primary identification.\n"
						+ "Please specify new primary identification first.", 
						this.getContentPane());
				return;
			}
			String yesNoQuestion = "Do you want to remove identification " + 
					id.getCompoundIdentity().getName() +" for " + feature.getName() + "?";
			if(MessageDialog.showChoiceWithWarningMsg(yesNoQuestion, this.getContentPane()) == JOptionPane.YES_OPTION) {
				TandemMassSpectrum msms = feature.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
				if(msms != null) {
					
					if(id.getReferenceMsMsLibraryMatch() != null) {
						if(!activeFeatureCollection.isOffLine()) {
							try {					
								IdentificationUtils.removeMSMSFeatureLibraryMatch(id);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					else {
						if(!activeFeatureCollection.isOffLine()) {
							try {
								IdentificationUtils.removeMSMSFeatureManualIdentification(id);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					feature.removeIdentity(id);
					updateSelectedMSMSFeatures();
				}
			}
		}
	}

	private void addManualIdToSelectedMsOneFeature() {
		
		if(msOneFeatureTable.getSelectedBundle() == null)
			return;

		if(manualIdentificationDialog == null) {
			manualIdentificationDialog = new ManualIdentificationDialog(this);
			//manualIdentificationDialog.setLocationRelativeTo(this.getContentPane());
		}	
		manualIdentificationDialog.setActiveFeature(msOneFeatureTable.getSelectedBundle().getMsFeature());
		manualIdentificationDialog.setVisible(true);
	}

	private void addManualIdToSelectedMsTwoFeature() {
		
		if(msTwoFeatureTable.getSelectedBundle() == null)
			return;

		manualIdentificationDialog = new ManualIdentificationDialog(this);
		manualIdentificationDialog.setActiveFeature(
				msTwoFeatureTable.getSelectedBundle().getMsFeature());
		manualIdentificationDialog.setVisible(true);
	}
	
	private void setManualIdForSelectedFeature(){
		
		//	CompoundIdentity compoundIdentity = manualIdentificationDialog.getSelectedCompoundIdentity();
		MsFeatureIdentity manualId = manualIdentificationDialog.getSelectedFeatureIdentity();
		if(manualId == null) {
			MessageDialog.showWarningMsg("No compound ID selected.", manualIdentificationDialog);
			return;
		}	
		manualId.setAssignedBy(MRC2ToolBoxCore.getIdTrackerUser());
		manualId.setIdentificationLevel(IDTDataCash.getTopMSFeatureIdentificationLevel());
				
		if(msOneFeatureTable.getSelectedBundle() != null) {
			
			MsFeature feature = msOneFeatureTable.getSelectedBundle().getMsFeature();		
//			MsFeatureIdentity manualId = 
//					new MsFeatureIdentity(compoundIdentity, CompoundIdentificationConfidence.ACCURATE_MASS);
//			manualId.setIdSource(CompoundIdSource.MANUAL);
			if(!activeFeatureCollection.isOffLine()) {
				try {
					IdentificationUtils.addReferenceMS1FeatureManualId(feature.getId(), manualId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			feature.setPrimaryIdentity(manualId);
			updateSelectedMSFeatures();
			manualIdentificationDialog.dispose();
			return;
		}
		if(msTwoFeatureTable.getSelectedBundle() != null) {
			
			MsFeature feature = msTwoFeatureTable.getSelectedBundle().getMsFeature();			
//			MsFeatureIdentity manualId = 
//			new MsFeatureIdentity(compoundIdentity, CompoundIdentificationConfidence.ACCURATE_MASS);
//			manualId.setIdSource(CompoundIdSource.MANUAL);			
			TandemMassSpectrum msms = feature.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
			if(!activeFeatureCollection.isOffLine()) {
				try {
					IdentificationUtils.addMSMSFeatureManualId(msms.getId(), manualId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			feature.setPrimaryIdentity(manualId);
			updateSelectedMSMSFeatures();
			manualIdentificationDialog.dispose();
			return;
		}
	}

	private void searchSelectedMsOneFeaturesAgainstCompoundDatabase() {
		// TODO Auto-generated method stub

	}

	private void searchSelectedMsOneFeaturesAgainstLibrary() {
		// TODO Auto-generated method stub

	}

	private void exportMsMsfeaturesToMspFile() {

		Collection<MsFeatureInfoBundle> toExport = null;
		Collection<MsFeatureInfoBundle> allFeatures = msTwoFeatureTable.getBundles(TableRowSubset.ALL);
		Collection<MsFeatureInfoBundle> selectedFeatures = msTwoFeatureTable.getBundles(TableRowSubset.SELECTED);
		if(allFeatures.isEmpty())
			return;

		if(selectedFeatures.isEmpty()) {

			int res = MessageDialog.showChoiceMsg(
				"Do you want to export all features in the table?", this.getContentPane());

			if(res == JOptionPane.NO_OPTION)
				return;

			toExport = allFeatures;
		}
		else
			toExport = selectedFeatures;

		ImprovedFileChooser mspchooser = new ImprovedFileChooser();
		mspchooser.setDialogType(JFileChooser.SAVE_DIALOG);
		mspchooser.setBorder(new TitledBorder(null, "Output", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		mspchooser.setAcceptAllFileFilterUsed(false);
		mspchooser.setMultiSelectionEnabled(false);
		mspchooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		mspchooser.setApproveButtonText("Export library");

		//	TODO add preferences
		mspchooser.setCurrentDirectory(baseDirectory);
		FileNameExtensionFilter txtFilter = new FileNameExtensionFilter(
				MsLibraryFormat.MSP.getName(), MsLibraryFormat.MSP.getFileExtension());
		mspchooser.addChoosableFileFilter(txtFilter);
		if(mspchooser.showSaveDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION ) {

			File exportFile = mspchooser.getSelectedFile();
			if(exportFile != null) {

				ExtendedMSPExportTask task = new ExtendedMSPExportTask(toExport, exportFile, true);
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
		}
	}
	
	
	private void siriusMsMsExportSetup() {

		siriusDataExportDialog = new SiriusDataExportDialog(this);
		siriusDataExportDialog.setLocationRelativeTo(this.getContentPane());
		siriusDataExportDialog.setVisible(true);
	}
	
	private void exportMsMsfeaturesToSiriusMsFile() {
		
		TableRowSubset featureSubset = siriusDataExportDialog.getFeatureSubset();
		FeatureSubsetByIdentification idSubset = 
				siriusDataExportDialog.getFeatureSubsetByIdentification();
		Collection<MsFeatureInfoBundle>featuresToExport = msTwoFeatureTable.getBundles(featureSubset);
		if(idSubset.equals(FeatureSubsetByIdentification.IDENTIFIED_ONLY))
			featuresToExport = featuresToExport.stream().
			filter(f -> f.getMsFeature().getPrimaryIdentity() != null).
			collect(Collectors.toList());
		
		if(idSubset.equals(FeatureSubsetByIdentification.UNKNOWN_ONLY))
			featuresToExport = featuresToExport.stream().
			filter(f -> f.getMsFeature().getPrimaryIdentity() == null).
			collect(Collectors.toList());
		
		if(featuresToExport.isEmpty()) {
			MessageDialog.showWarningMsg("No MS2 features to export using selected settings", 
					siriusDataExportDialog);
			return;
		}
		File exportFile = siriusDataExportDialog.getOutputFile();
		if(exportFile == null) {
			MessageDialog.showErrorMsg("Output file not specified!", siriusDataExportDialog);
			return;
		}
		double rtError = siriusDataExportDialog.getRetentionWindow();
		double mzError = siriusDataExportDialog.getMassWindow();
		IDTrackerSiriusMsExportTask task = 
				new IDTrackerSiriusMsExportTask(
						featuresToExport,
						rtError,
						mzError,
						exportFile);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);	
		siriusDataExportDialog.dispose();
	}
	
	private void runNistMsPepSearch() {

		Collection<String> errors = pepSearchSetupDialog.validateparameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), pepSearchSetupDialog);
			return;
		}
		List<String>commandParts = pepSearchSetupDialog.getSearchCommandParts();
		if(pepSearchSetupDialog.getFeaturesFromDatabase()) {

			Collection<MsFeatureInfoBundle> fToSearch = 
					msTwoFeatureTable.getBundles(pepSearchSetupDialog.getFeatureSubset());
			if(fToSearch.isEmpty()) {
				MessageDialog.showErrorMsg("No features to search using \"" +
						pepSearchSetupDialog.getFeatureSubset().getName() + 
						"\" feature subset", pepSearchSetupDialog);
				return;
			}	
			NISTMsPepSearchRoundTripTask task = new NISTMsPepSearchRoundTripTask(
					commandParts,
					fToSearch,
					pepSearchSetupDialog.getTmpInputFile(),
					pepSearchSetupDialog.getResultFile());			
			task.setPepSearchParameterObject(pepSearchSetupDialog.getPepSearchParameterObject());
			try {
				IdentificationUtils.addNewPepSearchParameterSet(task.getPepSearchParameterObject());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			if(activeFeatureCollection.isOffLine())
				task.setSkipResultsUpload(true);
			
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
		else {			
			NISTMspepSearchOfflineTask task = new NISTMspepSearchOfflineTask(
					pepSearchSetupDialog.getSearchCommandParts(),
					null,
					pepSearchSetupDialog.getInputFile(),
					pepSearchSetupDialog.getResultFile());
			
			task.setPepSearchParameterObject(pepSearchSetupDialog.getPepSearchParameterObject());
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
		pepSearchSetupDialog.dispose();
	}
	
	private void runOfflineNistMsPepSearch() {

		Collection<String> errors = pepSearchSetupDialog.validateparameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), pepSearchSetupDialog);
			return;
		}
		NISTMspepSearchOfflineTask task = null;
		String searchCommand = pepSearchSetupDialog.buildSearchCommand();
		List<String>commandParts = pepSearchSetupDialog.getSearchCommandParts();
		if(pepSearchSetupDialog.getFeaturesFromDatabase()) {
			
			Collection<MsFeatureInfoBundle> fToSearch = 
					msTwoFeatureTable.getBundles(pepSearchSetupDialog.getFeatureSubset());
			if(fToSearch.isEmpty()) {
				MessageDialog.showErrorMsg("No features to search using \"" +
						pepSearchSetupDialog.getFeatureSubset().getName() + 
						"\" feature subset", pepSearchSetupDialog);
				return;
			}			
//			task = new NISTMspepSearchOfflineTask(
//					searchCommand,
//					fToSearch,
//					pepSearchSetupDialog.getTmpInputFile(),
//					pepSearchSetupDialog.getResultFile());
			task = new NISTMspepSearchOfflineTask(
					commandParts,
					fToSearch,
					pepSearchSetupDialog.getTmpInputFile(),
					pepSearchSetupDialog.getResultFile());
		}
		else {
//			task = new NISTMspepSearchOfflineTask(
//					searchCommand,
//					null,
//					pepSearchSetupDialog.getInputFile(),
//					pepSearchSetupDialog.getResultFile());
			task = new NISTMspepSearchOfflineTask(
					commandParts,
					null,
					pepSearchSetupDialog.getInputFile(),
					pepSearchSetupDialog.getResultFile());
		}
		task.setPepSearchParameterObject(pepSearchSetupDialog.getPepSearchParameterObject());
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
		pepSearchSetupDialog.dispose();
	}

	private void openIdProject() {

		if(openIDTrackerProjectDialog.getSelectedLimsExperiment() == null) {

			MessageDialog.showErrorMsg("Please select IDTracker experiment!", this.getContentPane());
			return;
		}
		idTrackerExperiment = openIDTrackerProjectDialog.getSelectedLimsExperiment();

		clearPanel();
		Collection<LIMSSamplePreparation> preps = IDTDataCash.getExperimentSamplePrepMap().get(idTrackerExperiment);
		if(preps != null && !preps.isEmpty()) {
			
			for(LIMSSamplePreparation prep : preps) {
				Collection<DataPipeline> dataPipelines = IDTDataCash.getSamplePrepDataPipelineMap().get(prep);
				if(dataPipelines != null && !dataPipelines.isEmpty()) {
					
					List<DataAcquisitionMethod> methods = 
							dataPipelines.stream().map(p -> p.getAcquisitionMethod()).
							distinct().collect(Collectors.toList());
					if(!methods.isEmpty())
						samplePrepAcquisitionMethodMap.put(prep, methods);
				}
			}		
		}
		dataAcquisitionMethods.addAll(
				IDTDataCash.getAcquisitionMethodsForExperiment(idTrackerExperiment));
		acquisitionDataExtractionMethodMap.clear();
		featureSetataExtractionMethodMap.clear();

		IDTrackerProjectDataFetchTask task = new IDTrackerProjectDataFetchTask(idTrackerExperiment);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
		openIDTrackerProjectDialog.dispose();
	}

	private void showOpenIdProjectDialogue() {

		if(MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg("You are not logged in ID tracker!", this.getContentPane());
			return;
		}
		//	TODO check if already opened experiment

		openIDTrackerProjectDialog = new OpenIDTrackerProjectDialog(this);
		openIDTrackerProjectDialog.setLocationRelativeTo(this.getContentPane());
		openIDTrackerProjectDialog.setVisible(true);
	}


	private void showIdTrackerManager() {

/*		if(CefAnalyzerCore.getIdTrackerUser() == null) {
			MessageDialogue.showErrorMsg("You are not logged in ID tracker!", this.getContentPane());
			return;
		}
		boolean loadData = false;
		if(idTrackerManager == null) {
			idTrackerManager =  new IDTrackerLimsManager();
			loadData = true;
		}
		idTrackerManager.setLocationRelativeTo(this.getContentPane());
		idTrackerManager.setVisible(true);
		if(loadData)
			idTrackerManager.refreshIdTrackerdata();*/
	}

	private void importIDDAExperiment() {
		// TODO Auto-generated method stub

	}

	private void runNistMsSearch() {

		//	TODO
		
//		Collection<String> errors = nistSearchSetupDialog.validateSearchParameters();
//		if(!errors.isEmpty()) {
//			MessageDialogue.showErrorMsg(StringUtils.join(errors, "\n"), nistSearchSetupDialog);
//			return;
//		}
//		nistSearchSetupDialog.savePreferences();
//		if(!nistSearchSetupDialog.createNistIniFile())
//			return;
//		
//		if(!nistSearchSetupDialog.createAutoimpMsdFile())
//			return;
//		
//		if(!nistSearchSetupDialog.createSecondLocator())
//			return;
//
//		NISTMsSearchTask task = new NISTMsSearchTask();
//		task.addTaskListener(this);
//		CefAnalyzerCore.getTaskController().addTask(task);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(NISTMsSearchTask.class)) {

				NISTMsSearchTask task  = (NISTMsSearchTask)e.getSource();
				File results = task.getResultsFile();
				MessageDialog.showInfoMsg("Results are in " + results.getAbsolutePath(), this.getContentPane());
			}
			if (e.getSource().getClass().equals(IDTrackerProjectDataFetchTask.class))
				finalizeIdTrackerProjectLoad();

			if (e.getSource().getClass().equals(IDTMS1FeatureSearchTask.class))
				loadMsOneSearchData(((IDTMS1FeatureSearchTask)e.getSource()).getSelectedFeatures());
			
			if (e.getSource().getClass().equals(IDTMSMSFeatureSearchTask.class))
				loadMsMsSearchData(((IDTMSMSFeatureSearchTask)e.getSource()).getSelectedFeatures());			

			if (e.getSource().getClass().equals(NISTMspepSearchOfflineTask.class))
				finalizeNISTMspepSearchOfflineTask((NISTMspepSearchOfflineTask)e.getSource());

			if (e.getSource().getClass().equals(NISTMsPepSearchRoundTripTask.class))
				finalizeNISTMspepSearchRoundTripTask((NISTMsPepSearchRoundTripTask)e.getSource());
			
			if (e.getSource().getClass().equals(IDTrackerDataExportTask.class))
				finalizeIDRackerExportTask((IDTrackerDataExportTask)e.getSource());	
			
			if (e.getSource().getClass().equals(RawDataLoadForInjectionsTask.class))
				finalizeRawDataLoadTask((RawDataLoadForInjectionsTask)e.getSource());
			
			if (e.getSource().getClass().equals(RawDataRepositoryIndexingTask.class))
				MessageDialog.showInfoMsg("Raw data repository scan completed.", this.getContentPane());	
					
			if (e.getSource().getClass().equals(ChromatogramExtractionTask.class))
				finalizeCromatogramExtractionTask((ChromatogramExtractionTask)e.getSource());
			
			if (e.getSource().getClass().equals(ReferenceMSMSLibraryExportTask.class))
				finalizeReferenceMSMSLibraryExportTask((ReferenceMSMSLibraryExportTask)e.getSource());
			
			if (e.getSource().getClass().equals(DecoyLibraryGenerationTask.class))
				finalizeDecoyLibraryGenerationTask((DecoyLibraryGenerationTask)e.getSource());
			
			if (e.getSource().getClass().equals(IDTMSMSFeatureDataPullTask.class))
				finalizeIDTMSMSFeatureDataPullTask((IDTMSMSFeatureDataPullTask)e.getSource());	
			
			if (e.getSource().getClass().equals(PercolatorFDREstimationTask.class))
				finalizePercolatorFDREstimationTask((PercolatorFDREstimationTask)e.getSource());
			
			if (e.getSource().getClass().equals(DefaultMSMSLibraryHitReassignmentTask.class))
				finalizeDefaultMSMSLibraryHitReassignmentTask((DefaultMSMSLibraryHitReassignmentTask)e.getSource());
		}
	}		

	private void finalizeDefaultMSMSLibraryHitReassignmentTask(
			DefaultMSMSLibraryHitReassignmentTask source) {

		MsFeatureInfoBundle selected = msTwoFeatureTable.getSelectedBundle();
		reloadActiveMSMSFeatureCollection();			
		if(selected != null)
			msTwoFeatureTable.selectBundle(selected);
	}

	private void finalizePercolatorFDREstimationTask(PercolatorFDREstimationTask task) {		
		
		MainWindow.hideProgressDialog();
		if(!task.getMessageLog().isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(task.getMessageLog(), "\n"), this.getContentPane());
		}
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		reloadActiveMSMSFeatureCollection();
	}

	private void finalizeIDTMSMSFeatureDataPullTask(IDTMSMSFeatureDataPullTask task) {
				
		activeFeatureCollection.addFeatures(task.getSelectedFeatures());
		safelyLoadMSMSFeatures(activeFeatureCollection.getFeatures());
		StatusBar.setActiveFeatureCollection(activeFeatureCollection);
	}

	private void finalizeDecoyLibraryGenerationTask(DecoyLibraryGenerationTask task) {
		
		File results = task.getOutputFile();
		if(results != null && results.exists()) {

			if(MessageDialog.showChoiceMsg("Decoy MSP file created, do you want to open containing folder?",
				this.getContentPane()) == JOptionPane.YES_OPTION) {
				try {
					Desktop.getDesktop().open(results.getParentFile());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
	}

	private void finalizeReferenceMSMSLibraryExportTask(ReferenceMSMSLibraryExportTask task) {

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
	}

	private void finalizeCromatogramExtractionTask(ChromatogramExtractionTask task) {
	
		((RawDataExaminerPanel)MRC2ToolBoxCore.getMainWindow().
				getPanel(PanelList.RAW_DATA_EXAMINER)).finalizeChromatogramExtraction(task);
		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.RAW_DATA_EXAMINER);
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();	
	}

	private void finalizeRawDataLoadTask(RawDataLoadForInjectionsTask task) {
		
		Collection<DataFile> missingFiles = task.getMissingRawFiles();
		if(!missingFiles.isEmpty()) {
			
			List<String> fileNames = missingFiles.stream().map(f -> f.getName()).sorted().collect(Collectors.toList());
			String message = "The following raw data files not in the repository:\n" +
					StringUtils.join(fileNames, "\n");
			MessageDialog.showWarningMsg(message, this.getContentPane());
		}
		Collection<DataFile> openedFiles = task.getOpenedFiles();	
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		MainWindow.hideProgressDialog();		
		((RawDataExaminerPanel)MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.RAW_DATA_EXAMINER)).loadRawData(openedFiles);
		MRC2ToolBoxCore.getMainWindow().showPanel(PanelList.RAW_DATA_EXAMINER);
	}

	private void finalizeIDRackerExportTask(IDTrackerDataExportTask task) {

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
	}

	private void finalizeNISTMspepSearchRoundTripTask(NISTMsPepSearchRoundTripTask task) {

		Collection<MsFeatureInfoBundle> bundles = msTwoFeatureTable.getTable().getFilteredBundles();
		MsFeatureInfoBundle selected = msTwoFeatureTable.getSelectedBundle();
		File results = task.getResultsFile();
		File logFile = task.getLogFile();
		if(results.exists()) {

			reloadActiveMSMSFeatureCollection();			
			if(selected != null)
				msTwoFeatureTable.selectBundle(selected);
			
			if(logFile.exists() && MessageDialog.showChoiceMsg("Search log file " + logFile.getName() + 
					" created, do you want to open containing folder?",
					this.getContentPane()) == JOptionPane.YES_OPTION) {
				try {
					Desktop.getDesktop().open(logFile.getParentFile());
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

	private void finalizeNISTMspepSearchOfflineTask(NISTMspepSearchOfflineTask task) {
		
		File results = task.getResultsFile();
		if(results.exists()) {

			if(MessageDialog.showChoiceMsg("Results file created, do you want to open containing folder?",
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
		try {
			String searchParametersId = 
					IdentificationUtils.addNewPepSearchParameterSet(
							task.getPepSearchParameterObject(), 
							ConnectionManager.getConnection());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadMSMSFeatureInformationBundleCollection(
			MsFeatureInfoBundleCollection selectedCollection) {
		
		if(selectedCollection.equals(FeatureCollectionManager.msmsSearchResults)) {
			reloadCompleteActiveMSMSFeatureSet();
			return;
		}
		if(selectedCollection.equals(FeatureCollectionManager.msOneSearchResults)) {
			reloadCompleteActiveMSOneFeatureSet();
			return;
		}
		activeFeatureCollection = selectedCollection;
		IDTMSMSFeatureDataPullTask task = 
				FeatureCollectionManager.getMsFeatureInfoBundleCollectionData(selectedCollection);
		if(task == null) {
			 reloadActiveMSMSFeatureCollection();
		}
		else {
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
	}
	
	private void reloadActiveMSMSFeatureCollection() {
		
		if(activeFeatureCollection == null)
			return;
		
		safelyLoadMSMSFeatures(activeFeatureCollection.getFeatures());			
		StatusBar.setActiveFeatureCollection(activeFeatureCollection);
	}
	
	private void closeFeatureCollectionManager() {
		
		Runnable swingCode = new Runnable() {
			public void run() {
				if(featureCollectionManagerDialog != null) {
					try {
						featureCollectionManagerDialog.dispose();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		try {
			if (SwingUtilities.isEventDispatchThread())
				swingCode.run();
			else
				SwingUtilities.invokeAndWait(swingCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadMsMsSearchData(Collection<MsFeatureInfoBundle> features) {
		
		FeatureCollectionManager.msmsSearchResults.clearCollection();
		FeatureCollectionManager.msmsSearchResults.addFeatures(features);
		idTrackerDataExplorerPlotDialog.clearPanels();
		activeFeatureCollection = FeatureCollectionManager.msmsSearchResults;
		StatusBar.setActiveFeatureCollection(activeFeatureCollection);
		safelyLoadMSMSFeatures(FeatureCollectionManager.msmsSearchResults.getFeatures());
	}

	public void filterMSMSFeatures(Collection<MsFeatureInfoBundle>featuresToInclude) {
		
		Collection<MsFeatureInfoBundle>filtered = 
				FeatureCollectionManager.msmsSearchResults.getFeatures().stream().
				filter(f -> featuresToInclude.contains(f)).collect(Collectors.toSet());
		
		safelyLoadMSMSFeatures(filtered);
	}

	public void reloadCompleteActiveMSMSFeatureSet() {
		activeFeatureCollection = FeatureCollectionManager.msmsSearchResults;
		StatusBar.setActiveFeatureCollection(activeFeatureCollection);
		safelyLoadMSMSFeatures(FeatureCollectionManager.msmsSearchResults.getFeatures());
	}
	
	public void safelyLoadMSMSFeatures(
			Collection<MsFeatureInfoBundle>featuresToLoad) {
		
		clearFeatureData();
		msTwoFeatureTable.getTable().getSelectionModel().removeListSelectionListener(this);
		msTwoFeatureTable.getTable().
			setTableModelFromFeatureList(featuresToLoad);
		msTwoFeatureTable.getTable().getSelectionModel().addListSelectionListener(this);
	}

	private void loadMsOneSearchData(Collection<MsFeatureInfoBundle> features) {
		
		FeatureCollectionManager.msOneSearchResults.clearCollection();
		FeatureCollectionManager.msOneSearchResults.addFeatures(features);
		safelyLoadMSOneFeatures(FeatureCollectionManager.msOneSearchResults.getFeatures());
	}

	public void filterMsOneFeatures(
			Collection<MsFeatureInfoBundle>featuresToInclude) {

		Collection<MsFeatureInfoBundle>filtered = 
				FeatureCollectionManager.msOneSearchResults.getFeatures().stream().
				filter(f -> featuresToInclude.contains(f)).collect(Collectors.toSet());
	
		safelyLoadMSOneFeatures(filtered);
	}
	
	public void reloadCompleteActiveMSOneFeatureSet() {
		safelyLoadMSOneFeatures(FeatureCollectionManager.msOneSearchResults.getFeatures());
	}
	
	public void safelyLoadMSOneFeatures(
			Collection<MsFeatureInfoBundle>featuresToLoad) {
		
		msOneFeatureTable.getTable().getSelectionModel().removeListSelectionListener(this);
		msOneFeatureTable.getTable().
			setTableModelFromFeatureList(featuresToLoad);
		msOneFeatureTable.getTable().getSelectionModel().addListSelectionListener(this);
	}
	

	private void finalizeIdTrackerProjectLoad() {
		// TODO Auto-generated method stub

	}

	private void runIdentificationTask() {
		// TODO Auto-generated method stub

	}

	private void loadFeaturesFromFile() {

//		MsFeatureImportDialog msFeatureImportDialog = new MsFeatureImportDialog();
//		msFeatureImportDialog.setLocationRelativeTo(this.getContentPane());
//		msFeatureImportDialog.setVisible(true);
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
	public void clearPanel() {

		msOneFeatureTable.getTable().clearTable();
		msTwoFeatureTable.getTable().clearTable();
		clearFeatureData();
		clearDataMaps();
		idTrackerDataExplorerPlotDialog.clearPanels();
		FeatureCollectionManager.clearActiveSets();
	}
	
	public void clearMSMSFeatureData() {
		clearFeatureData();
		msTwoFeatureTable.getTable().clearTable();
	}

	public void clearMSOneFeatureData() {
		clearFeatureData();
		msOneFeatureTable.getTable().clearTable();
	}
	
	public void clearFeatureData() {

//		idTable.getTable().getSelectionModel().removeListSelectionListener(this);
//		msmsLibraryMatchTable.getTable().getSelectionModel().removeListSelectionListener(this);		
//		idTable.clearTable();
//		msmsLibraryMatchTable.clearTable();
		
		identificationsTable.clearTable();
		molStructurePanel.clearPanel();
		msOneTable.clearTable();
		msOnePlot.removeAllDataSets();
		msTwoTable.clearTable();
		msTwoPlot.removeAllDataSets();
		msMsInfoPanel.clearPanel();
		msmsLibraryEntryPropertiesTable.clearTable();
		featureAnnotationPanel.clearPanel();
		followupStepTable.clearTable();
		standardFeatureAnnotationTable.clearTable();
		
		//	TODO
		
//		idTable.getTable().getSelectionModel().addListSelectionListener(this);
//		msmsLibraryMatchTable.getTable().getSelectionModel().addListSelectionListener(this);
	}

//	public void clearMSMSMatchData() {
//
//		msTwoTable.clearTable();
//		msTwoPlot.removeAllDataSets();
//		msMsInfoPanel.clearPanel();
//
//		//	TODO
//	}
	
	private void clearIdentityData() {
		
		msTwoTable.clearTable();
		msTwoPlot.removeAllDataSets();
		msMsInfoPanel.clearPanel();
		molStructurePanel.clearPanel();
		pepSearchParameterListingPanel.clearPanel();
		clasyFireViewer.clearPanel();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		
		if(e.getValueIsAdjusting() || e.getSource() == null)
			return;

		for(ListSelectionListener listener : ((DefaultListSelectionModel)e.getSource()).getListSelectionListeners()) {

			if(listener.equals(msOneFeatureTable.getTable())) {
				
				msTwoFeatureTable.getTable().getSelectionModel().removeListSelectionListener(this);
				msTwoFeatureTable.getTable().clearSelection();
				msTwoFeatureTable.getTable().getSelectionModel().addListSelectionListener(this);
				showInfoBundle(msOneFeatureTable.getSelectedBundle());
				return;
			}
			if(listener.equals(msTwoFeatureTable.getTable())) {
				
				msOneFeatureTable.getTable().getSelectionModel().removeListSelectionListener(this);
				msOneFeatureTable.getTable().clearSelection();
				msOneFeatureTable.getTable().getSelectionModel().addListSelectionListener(this);
				showInfoBundle(msTwoFeatureTable.getSelectedBundle());
				return;
			}
			if(listener.equals(identificationsTable.getTable())){
				
				showFeatureIdentity(identificationsTable.getSelectedIdentity());
				return;
			}
		}			
		
	}

	public MsFeatureInfoBundle getSelectedMSMSFeatureBundle() {
		return msTwoFeatureTable.getSelectedBundle();
	}

	public MsFeatureInfoBundle getSelectedMSFeatureBundle() {
		return msOneFeatureTable.getSelectedBundle();
	}
	
	public void updateSelectedMSFeatures() {
		
		Collection<MsFeatureInfoBundle> selectedBundles = 
				msOneFeatureTable.getBundles(TableRowSubset.SELECTED);
		if(selectedBundles.isEmpty())
			return;
		
		updateMSFeatures(selectedBundles);
		showInfoBundle(selectedBundles.iterator().next());
		msOneFeatureTable.getTable().scrollToSelected();
	}
	
	private void updateMSFeatures(Collection<MsFeatureInfoBundle> selectedBundles) {
		
		for(MsFeatureInfoBundle bundle : selectedBundles) {
			
			MsFeatureIdentity primaryId = bundle.getMsFeature().getPrimaryIdentity();
			if(primaryId != null && !activeFeatureCollection.isOffLine()) {
				try {
					IdentificationUtils.setReferenceMS1FeaturePrimaryIdentity(
							bundle.getMsFeature().getId(),
							primaryId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			msOneFeatureTable.getTable().updateFeatureData(bundle);
		}
	}

	public void updateSelectedMSMSFeatures() {

		Collection<MsFeatureInfoBundle> selectedBundles = 
				msTwoFeatureTable.getBundles(TableRowSubset.SELECTED);
		if(selectedBundles.isEmpty())
			return;

		updateMSMSFeature(selectedBundles);
		showInfoBundle(selectedBundles.iterator().next());
		msTwoFeatureTable.getTable().scrollToSelected();
	}
	
	private void updateMSMSFeature(Collection<MsFeatureInfoBundle> selectedBundles) {
		
		for(MsFeatureInfoBundle bundle : selectedBundles) {
			
			MsFeatureIdentity primaryId = bundle.getMsFeature().getPrimaryIdentity();
			TandemMassSpectrum msmsFeature = 
					bundle.getMsFeature().getSpectrum().getExperimentalTandemSpectrum();
			if(primaryId != null && !activeFeatureCollection.isOffLine()) {
				try {
					IdentificationUtils.setMSMSFeaturePrimaryIdentity(
							msmsFeature.getId(),
							primaryId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			msTwoFeatureTable.getTable().updateFeatureData(bundle);
		}
	}
	
	private void showFeatureIdentity(MsFeatureIdentity selectedIdentity) {

		clearIdentityData();
		if(selectedIdentity == null)
			return;

		//	Show MSMS match if present and just MSMS if not
		if(msTwoFeatureTable.getSelectedBundle() != null) {
			
			MsFeature feature = msTwoFeatureTable.getSelectedBundle().getMsFeature();
			msMsInfoPanel.loadFeatureData(feature);
			TandemMassSpectrum instrumentSpectrum =
					feature.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
			if(instrumentSpectrum != null) {
				msMsInfoPanel.showMsMsParameters(instrumentSpectrum);
				msTwoTable.setTableModelFromTandemMs(instrumentSpectrum);
			}
			ReferenceMsMsLibraryMatch refMatch = selectedIdentity.getReferenceMsMsLibraryMatch();
			if(refMatch != null) {
				
				MsMsLibraryFeature libFeature = refMatch.getMatchedLibraryFeature();
				if(libFeature == null)
					msTwoPlot.showTandemMs(instrumentSpectrum);
				else
					msTwoPlot.showTandemMsWithReference(instrumentSpectrum, libFeature);
				
				//	Show msms search parameters if any		
				loadPepSearchParameters(selectedIdentity);
				msmsLibraryEntryPropertiesTable.showMsMsLibraryFeatureProperties(libFeature);
			}
			else
			msTwoPlot.showTandemMs(instrumentSpectrum);
		}
		if(selectedIdentity.getCompoundIdentity() != null) {
			molStructurePanel.showStructure(selectedIdentity.getCompoundIdentity().getSmiles());
			clasyFireViewer.showCompoundData(selectedIdentity.getCompoundIdentity().getPrimaryDatabaseId());
		}
		
		//	TODO show other compound info
	}

	private void showInfoBundle(MsFeatureInfoBundle selectedBundle) {
		
		clearFeatureData();
		if(selectedBundle == null)
			return;

		MsFeature feature = selectedBundle.getMsFeature();		
		MsMsLibraryFeature libFeature = null;
		identificationsTable.setModelFromMsFeature(selectedBundle.getMsFeature());	
		followupStepTable.loadFeatureData(selectedBundle);
		standardFeatureAnnotationTable.loadFeatureData(selectedBundle);
		MsFeatureIdentity pid = feature.getPrimaryIdentity();
		if(pid != null) {
			
			if(pid.getCompoundIdentity() != null)
				molStructurePanel.showStructure(pid.getCompoundIdentity().getSmiles());

			if(pid.getReferenceMsMsLibraryMatch() != null)
				libFeature = pid.getReferenceMsMsLibraryMatch().getMatchedLibraryFeature();
		}
		List<MsPoint> msOne = Arrays.asList(feature.getSpectrum().getCompletePattern());
		if(!msOne.isEmpty()) {
//			msOnePlot.showMsForPointCollection(
//					Arrays.asList(feature.getSpectrum().getCompletePattern()), true, "MS1 scan");
			msOnePlot.showMsForFeature(feature, false);
			msOneTable.setTableModelFromSpectrum(feature);
		}
		//	Add MSMS
		if(!feature.getSpectrum().getTandemSpectra().isEmpty()) {
			
			msMsInfoPanel.loadFeatureData(feature);
			//	Get experimental spectrum
			//	TODO deal with MSn n > 2
			TandemMassSpectrum instrumentSpectrum =
				feature.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
			
			if(instrumentSpectrum != null) {
	
				msMsInfoPanel.showMsMsParameters(instrumentSpectrum);
				msTwoTable.setTableModelFromTandemMs(instrumentSpectrum);
				if(libFeature == null)
					msTwoPlot.showTandemMs(instrumentSpectrum);
				else
					msTwoPlot.showTandemMsWithReference(instrumentSpectrum, libFeature);
			}
		}
		//	Show annotations
		featureAnnotationPanel.loadFeatureData(feature);		
		identificationsTable.getTable().selectPrimaryIdentity();
	}
	
	private void loadPepSearchParameters(MsFeatureIdentity featureId) {
			
		if(featureId.getReferenceMsMsLibraryMatch() == null)
			return;
			
		String parId = featureId.getReferenceMsMsLibraryMatch().getSearchParameterSetId();
		if(parId == null)
			return;
			
		NISTPepSearchParameterObject pepSearchParams = IDTDataCash.getNISTPepSearchParameterObjectById(parId);
		pepSearchParameterListingPanel.loadNISTPepSearchParameterObject(pepSearchParams);
	}

	public Collection<MsFeatureInfoBundle>getMsMsFeatureBundles(TableRowSubset subset){
		return msTwoFeatureTable.getBundles(subset);
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	@Override
	public void reloadDesign() {
		// TODO Auto-generated method stub
		
	}
	
	public void selectMSMSFeature(MsFeatureInfoBundle toSelect) {
		
		if(toSelect != null)
			msTwoFeatureTable.selectBundle(toSelect);
	}

	public MsFeatureInfoBundleCollection getActiveFeatureCollection() {
		return activeFeatureCollection;
	}

	public void loadFeaturesFromRawDataProject(RawDataAnalysisProject activeRawDataAnalysisProject) {
		
		clearPanel();		
		activeFeatureCollection = 
				new MsFeatureInfoBundleCollection(activeRawDataAnalysisProject.getName());
		activeFeatureCollection.setOffLine(true);		
		Collection<MsFeatureInfoBundle>projectMsmsFeatures = 
				activeRawDataAnalysisProject.getMsMsFeatureBundles();
		activeFeatureCollection.addFeatures(projectMsmsFeatures);
		safelyLoadMSMSFeatures(activeFeatureCollection.getFeatures());			
		StatusBar.setActiveFeatureCollection(activeFeatureCollection);
	}	
}
