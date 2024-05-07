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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotationCluster;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.IDTrackerDataExportParameters;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.MzFrequencyObject;
import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.SiriusMsMsCluster;
import edu.umich.med.mrc2.datoolbox.data.StandardFeatureAnnotation;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureSubsetByIdentification;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSComponentTableFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.MsDepth;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerAnnotationLookupDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerBasedMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.FeatureLookupList;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.IMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusterDataSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.data.msclust.MsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureChromatogramUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IdFollowupUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IdLevelUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IdentificationUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSClusteringDBUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.StandardAnnotationUtils;
import edu.umich.med.mrc2.datoolbox.gui.annotation.DockableObjectAnnotationPanel;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.CompoundDatabasePanel;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo.DockableCompoundClasyFireViewer;
import edu.umich.med.mrc2.datoolbox.gui.idtable.DockableUniversalIdentificationResultsTable;
import edu.umich.med.mrc2.datoolbox.gui.idtable.IDTrackerIdentificationTableModelListener;
import edu.umich.med.mrc2.datoolbox.gui.idtable.UniversalIdentificationResultsTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.binner.DockableBinnerAnnotationDetailsPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.clustree.DockableMSMSFeatureClusterTree;
import edu.umich.med.mrc2.datoolbox.gui.idworks.clustree.MSMSFeatureClusterTree;
import edu.umich.med.mrc2.datoolbox.gui.idworks.clustree.MajorClusterFeatureDefiningProperty;
import edu.umich.med.mrc2.datoolbox.gui.idworks.clustree.MajorClusterFeatureExtractionSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.clustree.filter.MSMSClusterFilterDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.clustree.filter.MSMSClusterMZRTListFilterDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.clustree.lookup.DockableLookupFeatureTable;
import edu.umich.med.mrc2.datoolbox.gui.idworks.clustree.summary.MSMSCLusterDataSetSummaryDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.export.IDTrackerDataExportDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.export.IDTrackerMSMSClusterDataSetExportDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.export.SiriusDataExportDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.DataCollectionsManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.clusters.MSMSClusterDataSetEditorDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.features.AddFeaturesToCollectionDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.features.oper.FeatureCollectionsOperationDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fdr.FDREstimationSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idfus.DockableFollowupStepTable;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idfus.FollowupStepAssignmentDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idfus.IdFollowupStepManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.idlevel.IdLevelManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.manid.ManualIdentificationDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms1.DockableReferenceMsOneFeatureTable;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms1.ReferenceMsOneFeaturePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.DockableMSMSFeatureTable;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.DockableMSMSLibraryEntryPropertiesTable;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.DockablePepSearchParameterListingPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.MsMsFeaturePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.filter.AnnotationFilterDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.filter.FilterTrackerMSMSFeaturesDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.filter.IDLAnnotationFilterParameters;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.filter.MSMSFilterParameters;
import edu.umich.med.mrc2.datoolbox.gui.idworks.ms2.rtid.MSMSFeatureRTIDSearchDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTReferenceLibraries;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.nistms.NISTMSSerchSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.PepSearchSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.io.PepserchResultsImportDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.ads.ActiveDataSetMZRTDataSearchDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.binner.ActiveDataSetBinnerAnnotationsSearchDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.byexp.ExperimentMZRTDataSearchDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.IDTrackerDataSearchDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.msms.FeatureVsFeatureMSMSSearchSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.stan.DockableStandardFeatureAnnotationTable;
import edu.umich.med.mrc2.datoolbox.gui.idworks.stan.StandardFeatureAnnotationAssignmentDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.stan.StandardFeatureAnnotationManagerDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.summary.DatasetSummaryDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.tdexplor.IDTrackerDataExplorerPlotFrame;
import edu.umich.med.mrc2.datoolbox.gui.idworks.tophit.ReassignDefaultMSMSLibraryHitDialog;
import edu.umich.med.mrc2.datoolbox.gui.library.feditor.DockableMsMsInfoPanel;
import edu.umich.med.mrc2.datoolbox.gui.library.feditor.DockableMsMsTable;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.main.StatusBar;
import edu.umich.med.mrc2.datoolbox.gui.mzfreq.MzFrequencyAnalysisResultsDialog;
import edu.umich.med.mrc2.datoolbox.gui.mzfreq.MzFrequencyAnalysisSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.chromatogram.DockableChromatogramPlot;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.multi.MultipleSpectraDisplayDialog;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.spectrum.DockableSpectumPlot;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.spectrum.MSReferenceDisplayType;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.spectrum.MsReferenceType;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.RawDataExaminerPanel;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.xic.XICSetupDialog;
import edu.umich.med.mrc2.datoolbox.gui.structure.DockableMolStructurePanel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ms.DockableMsTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MSFeatureBundleDataUpdater;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.MSMSClusterDataSetManager;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.main.RecentDataManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSSearchParameterSet;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisExperiment;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id.DefaultMSMSLibraryHitReassignmentTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id.NISTMsPepSearchRoundTripTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id.NISTMsSearchTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id.NISTMspepSearchOfflineTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id.PercolatorFDREstimationTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.BinnerAnnotationLookupTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.FeatureVsFeatureMSMSSearchTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMS1FeatureSearchTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMSMSClusterDataPullTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMSMSDuplicateMSMSFeatureCleanupTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMSMSFeatureDataPullTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMSMSFeatureDataPullWithFilteringTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMSMSFeatureSearchTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTrackerDataExportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTrackerExperimentDataFetchTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTrackerMSMSClusterDataExportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTrackerSiriusMsClusterExportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTrackerSiriusMsExportWithClusteringTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.MSMSClusterDataSetUploadTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.MSMSFeatureClusteringTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.SpectrumEntropyRecalculationTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.ExtendedMSPExportTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.ChromatogramExtractionTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.RawDataLoadForInjectionsTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.RawDataRepositoryIndexingTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.MzFrequencyAnalysisTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.stats.MzFrequencyType;
import edu.umich.med.mrc2.datoolbox.utils.MSMSClusteringUtils;
import edu.umich.med.mrc2.datoolbox.utils.MSMSExportUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsFeatureStatsUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.NISTPepSearchUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import umich.ms.datatypes.LCMSData;

public class IDWorkbenchPanel extends DockableMRC2ToolboxPanel 
		implements MSFeatureBundleDataUpdater, TreeSelectionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("missingIdentifications", 16);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "IDWorkbenchPanel.layout");
	private static final int MASK =
		    Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();	
    
	private DockableReferenceMsOneFeatureTable msOneFeatureTable;
	private DockableMSMSFeatureTable msTwoFeatureTable;
	private DockableMolStructurePanel molStructurePanel;
	private DockableMolStructurePanel referenceMolStructurePanel;
	private DockablePepSearchParameterListingPanel pepSearchParameterListingPanel;
	private DockableObjectAnnotationPanel featureAnnotationPanel;
	private DockableSpectumPlot msOnePlot;
	private DockableSpectumPlot msTwoPlot;
	private DockableMsTable msOneTable;
	private DockableMsMsTable msTwoTable;
	private DockableMsMsInfoPanel msMsInfoPanel;
	private DockableMSMSLibraryEntryPropertiesTable msmsLibraryEntryPropertiesTable;
	private DockableCompoundClasyFireViewer clasyFireViewer;
	private DockableChromatogramPlot chromatogramPanel;
	private DockableMSMSFeatureClusterTree msmsFeatureClusterTreePanel;	
	private DockableLookupFeatureTable lookupFeatureTable;
	private DockableBinnerAnnotationDetailsPanel binnerAnnotationDetailsPanel;
	private IDSetupDialog idSetupDialog;
	private JFileChooser chooser;
	private File baseDirectory;
	private FileNameExtensionFilter txtFilter;
	private FileNameExtensionFilter xmlFilter;
	private FileNameExtensionFilter mgfFilter;
	private IDTrackerLimsManagerPanel idTrackerManager;
//	private OpenIDTrackerExperimentDialog openIDTrackerExperimentDialog;
	private LIMSExperiment idTrackerExperiment;
	private PepSearchSetupDialog pepSearchSetupDialog;
	private NISTMSSerchSetupDialog nistMSSerchSetupDialog;
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
	private UniversalIdentificationResultsTablePopupMenu idTablePopupMenu;
	private MsMsFeaturePopupMenu msmsFeaturePopupMenu;
	private ReferenceMsOneFeaturePopupMenu referenceMsOneFeaturePopupMenu;
	private IDTrackerDataExportDialog idTrackerDataExportDialog;
	private XICSetupDialog xicSetupDialog;
	private SiriusDataExportDialog siriusDataExportDialog;
	private IDTrackerDataSearchDialog idTrackerDataSearchDialog;
	private PepserchResultsImportDialog pepSearchResultVerifierDialog;	
	private IDTrackerDataExplorerPlotFrame idTrackerDataExplorerPlotDialog;	
	private DataCollectionsManagerDialog dataCollectionManagerDialog;
	private AddFeaturesToCollectionDialog addFeaturesToCollectionDialog;
	private MsFeatureInfoBundleCollection activeFeatureCollection;
	private IMSMSClusterDataSet activeMSMSClusterDataSet;
	private IMsFeatureInfoBundleCluster activeCluster;
	private FDREstimationSetupDialog fdrEstimationSetupDialog;
	private ReassignDefaultMSMSLibraryHitDialog reassignDefaultMSMSLibraryHitDialog;
	private FilterTrackerMSMSFeaturesDialog filterTrackerFeaturesDialog;
	private MSMSFeatureRTIDSearchDialog msmsFeatureRTIDSearchDialog;	
	private EntropyScoringSetupDialog entropyScoringSetupDialog;
	private ExperimentMZRTDataSearchDialog experimentMzRtDataSearchDialog;
	private ActiveDataSetMZRTDataSearchDialog activeDataSetMZRTDataSearchDialog;
	private DatasetSummaryDialog datasetSummaryDialog;	
	private MSMSClusterDataSetEditorDialog msmsClusterDataSetEditorDialog;	
	private IDTrackerMSMSClusterDataSetExportDialog idTrackerMSMSClusterDataSetExportDialog;
	private MSMSClusterFilterDialog msmsClusterFilterDialog;
	private MajorClusterFeatureExtractionSetupDialog majorClusterFeatureExtractionSetupDialog;
	private MzFrequencyAnalysisSetupDialog mzFrequencyAnalysisSetupDialog;
	private ActiveDataSetBinnerAnnotationsSearchDialog activeDataSetBinnerAnnotationsSearchDialog;
	private AnnotationFilterDialog annotationFilterDialog;
	private MSMSClusterMZRTListFilterDialog msmsClusterMZRTListFilterDialog;
	private MzFrequencyAnalysisResultsDialog mzFrequencyAnalysisResultsDialog;
	private FeatureVsFeatureMSMSSearchSetupDialog featureVsFeatureMSMSSearchSetupDialog;
	private MultipleSpectraDisplayDialog multipleSpectraDisplayDialog;
	
	private static final Icon searchIdTrackerIcon = GuiUtils.getIcon("searchDatabase", 24);
	private static final Icon searchExperimentIcon = GuiUtils.getIcon("searchIdExperiment", 24);
	private static final Icon searchIdActiveDataSetIcon = GuiUtils.getIcon("searchIdActiveDataSet", 24);
	private static final Icon dsSummaryIcon = GuiUtils.getIcon("infoGreen", 24);
	private static final Icon openCdpIdExperimentIcon = GuiUtils.getIcon("openIdExperiment", 24);
	private static final Icon loadLibraryIcon = GuiUtils.getIcon("loadLibrary", 24);
	private static final Icon idSetupIcon = GuiUtils.getIcon("searchCompounds", 24);
	private static final Icon nistMsIcon = GuiUtils.getIcon("NISTMS", 24);
	private static final Icon nistPepMsIcon = GuiUtils.getIcon("NISTMS-pep", 24);
	private static final Icon nistPepMsOfflineIcon = GuiUtils.getIcon("NISTMS-pep-offline", 24);
	private static final Icon nistPepMsOfflineUploadIcon = GuiUtils.getIcon("NISTMS-pep-upload", 24);
	private static final Icon validateNistPepMsIcon = GuiUtils.getIcon("acceptMsMs", 24);
	private static final Icon iddaIcon = GuiUtils.getIcon("importIDDAdata", 24);
	private static final Icon trackerManegerIcon = GuiUtils.getIcon("experimentDatabase", 24);
	private static final Icon exportMSPIcon = GuiUtils.getIcon("exportToMSP", 24);
	private static final Icon siriusIcon = GuiUtils.getIcon("sirius", 24);
	private static final Icon exportSiriusMSIcon = GuiUtils.getIcon("exportSiriusMs", 24);
	private static final Icon exportTrackerDataIcon = GuiUtils.getIcon("saveList", 24);
	private static final Icon idStatusManagerIcon = GuiUtils.getIcon("idStatusManager", 24);
	private static final Icon idFollowupStepManagerIcon = GuiUtils.getIcon("idFollowupStepManager", 24);
	private static final Icon standardFeatureAnnotationManagerIcon = GuiUtils.getIcon("editCollection", 24);
	private static final Icon openMsMsDataFileIcon = GuiUtils.getIcon("openMsMsDataFile", 24);
	private static final Icon clearDuplicatesIcon = GuiUtils.getIcon("clearDuplicates", 24);	
	private static final Icon bubblePlotIcon = GuiUtils.getIcon("bubble", 24);
	private static final Icon editFeatureCollectionIcon = GuiUtils.getIcon("clusterFeatureTable", 24);
	private static final Icon fdrIcon = GuiUtils.getIcon("fdr", 24);	
	private static final Icon reassignTopHitsIcon = GuiUtils.getIcon("recalculateScores", 24);
	private static final Icon filterIcon = GuiUtils.getIcon("filter", 24);
	private static final Icon entropyIcon = GuiUtils.getIcon("spectrumEntropy", 24);

	public IDWorkbenchPanel() {

		super("IDWorkbenchPanel", PanelList.ID_WORKBENCH.getName(), componentIcon);
		setLayout(new BorderLayout(0, 0));

		//	toolbar = new IDWorkbenchToolbar(this);
		menuBar = new IDWorkbenchMenuBar(this);
		add(menuBar, BorderLayout.NORTH);

		msOneFeatureTable = new DockableReferenceMsOneFeatureTable(this);
		referenceMsOneFeaturePopupMenu = 
				new ReferenceMsOneFeaturePopupMenu(this, msOneFeatureTable.getTable());
		msOneFeatureTable.getTable().addTablePopupMenu(referenceMsOneFeaturePopupMenu);

		msTwoFeatureTable = new DockableMSMSFeatureTable(this);
		msmsFeaturePopupMenu = 
				new MsMsFeaturePopupMenu(this, msTwoFeatureTable.getTable());
		msTwoFeatureTable.getTable().addTablePopupMenu(msmsFeaturePopupMenu);
				
		identificationsTable = new DockableUniversalIdentificationResultsTable(
				"IDWorkbenchPanelDockableUniversalIdentificationResultsTable", "Identifications");
		identificationsTable.getTable().getSelectionModel().addListSelectionListener(this);
		identificationsTable.getTable().setIdentificationTableModelListener(
				new IDTrackerIdentificationTableModelListener(this));
		
		idTablePopupMenu = 
				new UniversalIdentificationResultsTablePopupMenu(this, identificationsTable.getTable());
		identificationsTable.getTable().addTablePopupMenu(idTablePopupMenu);
		
		initIdTableActions();
		
		molStructurePanel = new DockableMolStructurePanel(
				"IDWorkbenchPanelDockableMolStructurePanel", "ID structure");
		referenceMolStructurePanel  = new DockableMolStructurePanel(
				"IDWorkbenchPanelDockableRefMolStructurePanel", "Reference structure");
		clasyFireViewer = new DockableCompoundClasyFireViewer();
		
		chromatogramPanel =  new DockableChromatogramPlot(
				"IDWorkbenchPanelDockableChromatogramPlot", "Chromatograms");
		
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
		
		msmsFeatureClusterTreePanel = new DockableMSMSFeatureClusterTree(
				"IdTrackerDockableMSMSFeatureClusterTree",
				"MSMS Feature Clusters",
				this, this);
		
		lookupFeatureTable = new DockableLookupFeatureTable(
				"IdTrackerDockableLookupFeatureTable",
				"Lookup feature list",
				this, this);
		
		binnerAnnotationDetailsPanel = new DockableBinnerAnnotationDetailsPanel();

		grid.add(0, 0, 80, 30, msOneFeatureTable, msTwoFeatureTable, 
				msmsFeatureClusterTreePanel, lookupFeatureTable);
		grid.add(80, 0, 20, 30, molStructurePanel, referenceMolStructurePanel, 
				clasyFireViewer, chromatogramPanel);
		grid.add(0, 30, 100, 20, identificationsTable);
//		grid.add(0, 50, 50, 50, narrativeDataPanel, synonymsTable,
//				propertiesTable, concentrationsTable, spectraTable);
		grid.add(50, 50, 50, 50, msOnePlot, msOneTable,
				msTwoPlot, msTwoTable, msMsInfoPanel, 
				msmsLibraryEntryPropertiesTable, 
				pepSearchParameterListingPanel, 
				featureAnnotationPanel, followupStepTable,
				standardFeatureAnnotationTable,
				binnerAnnotationDetailsPanel);

//		grid.select(0, 50, 50, 50, narrativeDataPanel);
		grid.select(50, 50, 50, 50, msOnePlot);

		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
		initActions();
		loadLayout(layoutConfigFile);
		populatePanelsMenu();
		
		idSetupDialog = new IDSetupDialog(this);
		initDataMaps();
	}
	
	@Override
	protected void initActions() {	
		
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_IDTRACKER_SEARCH_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_IDTRACKER_SEARCH_DIALOG_COMMAND.getName(), 
				searchIdTrackerIcon, this));
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_IDTRACKER_BY_EXPERIMENT_MZ_RT_SEARCH_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_IDTRACKER_BY_EXPERIMENT_MZ_RT_SEARCH_DIALOG_COMMAND.getName(), 
				searchExperimentIcon, this));

		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_ACTIVE_DATA_SET_MZ_RT_SEARCH_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_ACTIVE_DATA_SET_MZ_RT_SEARCH_DIALOG_COMMAND.getName(), 
				searchIdActiveDataSetIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_ACTIVE_DATA_SET_SUMMARY_COMMAND.getName(),
				MainActionCommands.SHOW_ACTIVE_DATA_SET_SUMMARY_COMMAND.getName(), 
				dsSummaryIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.NIST_MS_PEPSEARCH_SETUP_COMMAND.getName(),
				MainActionCommands.NIST_MS_PEPSEARCH_SETUP_COMMAND.getName(), 
				nistPepMsIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.NIST_MS_OFFLINE_PEPSEARCH_SETUP_COMMAND.getName(),
				MainActionCommands.NIST_MS_OFFLINE_PEPSEARCH_SETUP_COMMAND.getName(), 
				nistPepMsOfflineIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.VALIDATE_PEPSEARCH_RESULTS_COMMAND.getName(),
				MainActionCommands.VALIDATE_PEPSEARCH_RESULTS_COMMAND.getName(), 
				nistPepMsOfflineUploadIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SETUP_DEFAULT_MSMS_LIBRARY_MATCH_REASSIGNMENT.getName(),
				MainActionCommands.SETUP_DEFAULT_MSMS_LIBRARY_MATCH_REASSIGNMENT.getName(), 
				reassignTopHitsIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SETUP_SPECTRUM_ENTROPY_SCORING.getName(),
				MainActionCommands.SETUP_SPECTRUM_ENTROPY_SCORING.getName(), 
				entropyIcon, this));
		
		menuActions.addSeparator();		
		
		SimpleButtonAction fdrItem = GuiUtils.setupButtonAction(
				MainActionCommands.SETUP_FDR_ESTIMATION_FOR_LIBRARY_MATCHES.getName(),
				MainActionCommands.SETUP_FDR_ESTIMATION_FOR_LIBRARY_MATCHES.getName(), 
				fdrIcon, this);
		fdrItem.setEnabled(false);
		menuActions.add(fdrItem);
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_ID_LEVEL_MANAGER_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_ID_LEVEL_MANAGER_DIALOG_COMMAND.getName(), 
				idStatusManagerIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_ID_FOLLOWUP_STEP_MANAGER_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_ID_FOLLOWUP_STEP_MANAGER_DIALOG_COMMAND.getName(), 
				idFollowupStepManagerIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_STANDARD_FEATURE_ANNOTATION_MANAGER_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_STANDARD_FEATURE_ANNOTATION_MANAGER_DIALOG_COMMAND.getName(), 
				standardFeatureAnnotationManagerIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.LOAD_RAW_DATA_FOR_CURRENT_MSMS_FEATURE_SET_COMMAND.getName(),
				MainActionCommands.LOAD_RAW_DATA_FOR_CURRENT_MSMS_FEATURE_SET_COMMAND.getName(), 
				openMsMsDataFileIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_IDTRACKER_DATA_EXPLORER_PLOT.getName(),
				MainActionCommands.SHOW_IDTRACKER_DATA_EXPLORER_PLOT.getName(), 
				bubblePlotIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_DATA_COLLECTIONS_MANAGER_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_DATA_COLLECTIONS_MANAGER_DIALOG_COMMAND.getName(), 
				editFeatureCollectionIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EXPORT_FEATURES_TO_MSP_COMMAND.getName(),
				MainActionCommands.EXPORT_FEATURES_TO_MSP_COMMAND.getName(), 
				exportMSPIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_SIRIUS_MS_EXPORT_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_SIRIUS_MS_EXPORT_DIALOG_COMMAND.getName(), 
				siriusIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_IDTRACKER_DATA_EXPORT_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_IDTRACKER_DATA_EXPORT_DIALOG_COMMAND.getName(), 
				exportTrackerDataIcon, this));
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
	
	@Override
	public void actionPerformed(ActionEvent event) {

		if(MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg(
					"You are not logged in ID tracker!", 
					this.getContentPane());
			return;
		}
		super.actionPerformed(event);
		
		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.ID_SETUP_DIALOG_COMMAND.getName()))
			runIdentificationTask();
		
		if (command.equals(MainActionCommands.SHOW_IDTRACKER_SEARCH_DIALOG_COMMAND.getName()))
			showTrackerSearchDialog();	
		
		if (command.equals(MainActionCommands.SHOW_IDTRACKER_BY_EXPERIMENT_MZ_RT_SEARCH_DIALOG_COMMAND.getName()))
			showTrackerSearchByExperimentMzRtDialog();

		if (command.equals(MainActionCommands.SHOW_ACTIVE_DATA_SET_MZ_RT_SEARCH_DIALOG_COMMAND.getName()))
			showTrackerSearchActiveDataSetBMzRtDialog();
		
		if (command.equals(MainActionCommands.SEARCH_ACTIVE_DATA_SET_BY_MZ_RT_COMMAND.getName()))
			searchActiveDataSetBMzRt();	
		
		if (command.equals(MainActionCommands.SHOW_ACTIVE_DATA_SET_BINNER_ANNOTATION_SEARCH_DIALOG_COMMAND.getName()))
			showTrackerSearchByBinnerAnnotationsDialog();
		
		if (command.equals(MainActionCommands.SEARCH_ACTIVE_DATA_SET_BY_BINNER_ANNOTATION_COMMAND.getName()))
			searchActiveDataSetByBinnerAnnotations();
		
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
		
		if (command.equals(MainActionCommands.EXPORT_FEATURES_TO_MSP_COMMAND.getName()))
			exportMsMsfeaturesToMspFile();
				
		if (command.equals(MainActionCommands.SHOW_SIRIUS_MS_EXPORT_DIALOG_COMMAND.getName()))
			siriusMsMsExportSetup();
		
		if (command.equals(MainActionCommands.EXPORT_FEATURES_TO_SIRIUS_MS_COMMAND.getName()))
			exportMsMsfeaturesToSiriusMsFile();
		
		if (command.equals(MainActionCommands.SHOW_MSMS_CLUSTER_DATA_EXPORT_DIALOG_COMMAND.getName()))
			msmsClusterDataExportSetup();

		if (command.equals(MainActionCommands.EXPORT_MSMS_CLUSTER_DATA_COMMAND.getName()))
			exportMSMSClusterData();

		if (command.equals(MainActionCommands.EXPORT_MSMS_CLUSTER_DATA_FOR_SIRIUS_COMMAND.getName()))
			exportClustersToSiriusMSFile();
		
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
			clearIdentificationsForSelectedFeatures();
			
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

		for(MSFeatureIdentificationLevel level : IDTDataCache.getMsFeatureIdentificationLevelList()) {
			
			if (command.equals(level.getName()) 
					|| command.equals(MSFeatureIdentificationLevel.SET_PRIMARY + level.getName())) {
				
				String componentId = (String)((JComponent)event.getSource()).getClientProperty(
						MRC2ToolBoxCore.COMPONENT_IDENTIFIER);
				runIdLevelUpdate(command, componentId);
				break;
			}			
		}
		
		if (command.equals(MainActionCommands.CLEAR_ID_LEVEL_COMMAND.getName())) {
			String componentId = (String)((JComponent)event.getSource()).getClientProperty(
					MRC2ToolBoxCore.COMPONENT_IDENTIFIER);
			runIdLevelUpdate(command, componentId);
		}			
						
		if (command.equals(MainActionCommands.SHOW_IDTRACKER_DATA_EXPORT_DIALOG_COMMAND.getName()))
			showIdTrackerDataExportDialog();
		
		if (command.equals(MainActionCommands.EXPORT_IDTRACKER_DATA_COMMAND.getName()))	
			exportIdTrackerData();
		
		if (command.equals(MainActionCommands.XIC_FOR_FEATURE_DIALOG_COMMAND.getName()))	
			showXicSetupDialog();
		
		if (command.equals(MainActionCommands.EXTRACT_CHROMATOGRAM.getName()))	
			extractXic();		
		
		if (command.equals(MainActionCommands.LOAD_RAW_DATA_FOR_CURRENT_MSMS_FEATURE_SET_COMMAND.getName()))	
			loadRawDataForCurrentMsMsFeatureSet();	
		
		if (command.equals(MainActionCommands.MERGE_DUPLICATES_COMMAND.getName()))	
			mergeDuplicateMsMsFeatures();
		
		if (command.equals(MainActionCommands.SHOW_IDTRACKER_DATA_EXPLORER_PLOT.getName()))	
			showIDTrackerDataExplorerDialog();
		
		if (command.equals(MainActionCommands.RELOAD_COMPLETE_DATA_SET_COMMAND.getName()))
			reloadCompleteDataSet();
		
		if (command.equals(MainActionCommands.RELOAD_ACTIVE_MSMS_FEATURES.getName()))
			reloadActiveMSMSFeatureCollection();
		
		if (command.equals(MainActionCommands.RELOAD_ACTIVE_MSMS_CLUSTER_SET_FEATURES.getName()))	
			reloadActiveMSMSClusterSetFeatures();
		
		if (command.equals(MainActionCommands.RELOAD_ACTIVE_MS_ONE_FEATURES.getName()))	
			reloadCompleteActiveMSOneFeatureSet();
		
		if (command.equals(MainActionCommands.SHOW_DATA_COLLECTIONS_MANAGER_DIALOG_COMMAND.getName()))	
			showDataCollectionsManager();	
		
		if (command.equals(MainActionCommands.SHOW_FEATURE_COLLECTION_COMBSUB_DIALOG_COMMAND.getName()))	
			showFeatureCollectionCombSubDialog();
		
		if(command.equals(MainActionCommands.CREATE_NEW_FEATURE_COLLECTION_FROM_SELECTED.getName()))
			createNewMsmsFeatureCollectionFromSelectedFeatures(msTwoFeatureTable.getBundles(TableRowSubset.SELECTED));
		
		if(command.startsWith(MainActionCommands.ADD_FEATURES_TO_RECENT_FEATURE_COLLECTION_COMMAND.name()))
			addSelectedFeaturesToRecentCollection(command);	
		
		if (command.equals(MainActionCommands.ADD_SELECTED_TO_EXISTING_FEATURE_COLLECTION.getName()))
			addSelectedFeaturesToExistingMsMsFeatureCollection(msTwoFeatureTable.getBundles(TableRowSubset.SELECTED));
		
		if (command.equals(MainActionCommands.REMOVE_SELECTED_FROM_ACTIVE_MSMS_FEATURE_COLLECTION.getName()))
			removeFeaturesFromActiveMsMsFeatureCollection(msTwoFeatureTable.getBundles(TableRowSubset.SELECTED));
		
		if (command.equals(MainActionCommands.SETUP_FDR_ESTIMATION_FOR_LIBRARY_MATCHES.getName()))
			setupFDRforMSMSlibraryIdentifications();
		
		if (command.equals(MainActionCommands.CALCULATE_FDR_FOR_LIBRARY_MATCHES.getName()))
			calculateFDRforMSMSlibraryIdentifications();
		
		if (command.equals(MainActionCommands.SETUP_DEFAULT_MSMS_LIBRARY_MATCH_REASSIGNMENT.getName()))
			setupTopMSMSHitsReassignment();
		
		if (command.equals(MainActionCommands.REASSIGN_DEFAULT_MSMS_LIBRARY_MATCHES.getName()))
			reassignTopMSMSHits();
		
		if (command.equals(MainActionCommands.FEATURE_VS_FEATURE_MSMS_SEARCH_SETUP_COMMAND.getName()))
			setupFeatureVsFeatureMSMSSearch();
		
		if (command.equals(MainActionCommands.FEATURE_VS_FEATURE_MSMS_SEARCH_RUN_COMMAND.getName()))
			runFeatureVsFeatureMSMSSearch();		
		
		if (command.equals(MainActionCommands.SHOW_FEATURE_FILTER_COMMAND.getName())) {
			
			if(activeFeatureCollection != null && !activeFeatureCollection.getFeatures().isEmpty())
				showFeatureFilter(activeFeatureCollection.getFeatures());
		}
		if (command.equals(MainActionCommands.SHOW_FEATURE_IDL_ANNOTATION_FILTER_COMMAND.getName())) {
			
			if(activeFeatureCollection != null && !activeFeatureCollection.getFeatures().isEmpty())
				showIDLFeatureFilter(activeFeatureCollection.getFeatures());
		}		
		if (command.equals(MainActionCommands.SHOW_CLUSTERED_FEATURE_FILTER_COMMAND.getName())) {
			
			if(activeMSMSClusterDataSet != null && !activeMSMSClusterDataSet.getClusters().isEmpty())
				showFeatureFilter(activeMSMSClusterDataSet.getAllFeatures());
		}	
		if (command.equals(MainActionCommands.FILTER_FEATURES_COMMAND.getName()))
			filterFeatureTable();
		
		if (command.equals(MainActionCommands.FILTER_FEATURES_BY_IDL_ANNOTATION_COMMAND.getName()))
			filterFeatureTableByIDLAnnotations();
		
		if (command.equals(MainActionCommands.SHOW_FEATURE_SEARCH_BY_RT_ID_COMMAND.getName())) 
			showFeatureRTIDSearchDialog();
			
		if (command.equals(MainActionCommands.SEARCH_FEATURES_BY_RT_ID_COMMAND.getName()))
			searchFeaturesByRtId();
		
		if (command.equals(MainActionCommands.COPY_FEATURE_SPECTRUM_AS_MSP_COMMAND.getName()))
			copySelectedMSMSFeatureSpectrumAsMSP();
		
		if (command.equals(MainActionCommands.COPY_FEATURE_SPECTRUM_AS_ARRAY_COMMAND.getName()))
			copySelectedMSMSFeatureSpectrumAsArray();
				
		if (command.equals(MainActionCommands.COPY_LIBRARY_SPECTRUM_AS_MSP_COMMAND.getName()))
			copySelectedLibraryFeatureSpectrumAsMSP();	
		
		if (command.equals(MainActionCommands.COPY_LIBRARY_SPECTRUM_AS_ARRAY_COMMAND.getName()))
			copySelectedLibraryFeatureSpectrumAsArray();	
		
		if (command.equals(MainActionCommands.SETUP_SPECTRUM_ENTROPY_SCORING.getName()))
			setupSpectrumEntropyScoringParameters();
		
		if (command.equals(MainActionCommands.RECALCULATE_SPECTRUM_ENTROPY_SCORES.getName()))
			recalculateSpectrumEntropyScores();		
		
		if (command.equals(MainActionCommands.SHOW_ACTIVE_DATA_SET_SUMMARY_COMMAND.getName()))
			showActiveDataSetSummary();
		
		if (command.equals(MainActionCommands.SHOW_FILTERED_DATA_SUMMARY_COMMAND.getName()))
			showFilteredDataSetSummary();
		
		if (command.equals(MainActionCommands.ADD_MSMS_CLUSTER_DATASET_DIALOG_COMMAND.getName()))
			showAddNewMSMSClusterDataSetDialog();
		
		if (command.equals(MainActionCommands.ADD_MSMS_CLUSTER_DATASET_COMMAND.getName())) 
			insertNewMSMSClusterDataSet();
		
		if (command.equals(MainActionCommands.CLEAR_IDTRACKER_WORKBENCH_PANEL.getName()))
			clearWorkbench();	
		
		if (command.equals(MainActionCommands.SET_AS_PRIMARY_ID_FOR_CLUSTER.getName()))
			setSelectedIDasPrimaryForFeatureCluster();
			
		if (command.equals(MainActionCommands.SHOW_MSMS_CLUSTER_FILTER_COMMAND.getName()))
			showMSMSClusterFilter();
		
		if (command.equals(MainActionCommands.FILTER_MSMS_CLUSTERS_COMMAND.getName()))
			filterMSMSClusters();
		
		if (command.equals(MainActionCommands.SHOW_MSMS_CLUSTER_MZ_RT_LIST_FILTER_COMMAND.getName()))
			showMSMSClusterMZRTListFilter();
		
		if (command.equals(MainActionCommands.FILTER_MSMS_CLUSTERS_WITH_MZ_RT_LIST_COMMAND.getName()))
			filterMSMSClustersByMZRTList();
		
		if (command.equals(MainActionCommands.RELOAD_ACTIVE_MSMS_CLUSTERS_SET_COMMAND.getName()))
			reloadActiveMSMSClustersDataSet();
		
		if (command.equals(MainActionCommands.SHOW_MSMS_CLUSTERS_SUMMARY_COMMAND.getName()))
			showMSMSClustersSummary();
		
		if (command.equals(MainActionCommands.SETUP_MAJOR_CLUSTER_FEATURE_EXTRACTION_COMMAND.getName()))
			majorClusterFeatureExtractionSetup();
		
		if (command.equals(MainActionCommands.EXTRACT_MAJOR_CLUSTER_FEATURES_COMMAND.getName()))
			extractMajorClusterFeatures();
		
		if (command.equals(MainActionCommands.SET_UP_MZ_FREQUENCY_ANALYSIS_COMMAND.getName()))
			setUpMSMSParentIonFrequencyAnalysis();
				
		if (command.equals(MainActionCommands.RUN_MZ_FREQUENCY_ANALYSIS_COMMAND.getName()))
			runMSMSParentIonFrequencyAnalysis();	
		
		//	Feature cluster commands
		if (command.equals(MainActionCommands.EDIT_CLUSTER_COMMAND.getName()))
			editFeatureCluster();
		
		if (command.equals(MainActionCommands.DELETE_CLUSTER_COMMAND.getName()))
			deleteFeatureClusters();
		
//		if (command.equals(MainActionCommands.ANNOTATE_CLUSTER_COMMAND.getName()))
//			annotateFeatureCluster();	//	TODO
		
		if (command.equals(MainActionCommands.COPY_CLUSTER_FEATURES_AS_MSP.getName()))
			copyFeatureClusterMSMS(MsLibraryFormat.MSP);
		
		if (command.equals(MainActionCommands.COPY_CLUSTER_FEATURES_AS_SIRIUS_MS.getName()))
			copyFeatureClusterMSMS(MsLibraryFormat.SIRIUS_MS);
		
		if (command.equals(MainActionCommands.SHOW_MULTISPECTRA_DISPLAY_COMMAND.getName()))
			showMultipleSpectraDisplayDialog();
	}

	private void editFeatureCluster() {
		// TODO Auto-generated method stub
		IMsFeatureInfoBundleCluster[] clusters = 
				msmsFeatureClusterTreePanel.getSelectedClusters();
		if(clusters.length == 0)
			return;
		
		IMsFeatureInfoBundleCluster selectedCluster = clusters[0];
		
		//	TODO
		MessageDialog.showInfoMsg("Feature under development", this.getContentPane());
	}

	private void deleteFeatureClusters() {
		
		Collection<IMsFeatureInfoBundleCluster> clusters = 
				msmsFeatureClusterTreePanel.getSelectedClustersCollection();
		if(clusters.isEmpty() || activeMSMSClusterDataSet == null)
			return;
		
		int res = MessageDialog.showChoiceWithWarningMsg(
				"Do you want to delete selected feature clusters?", this.getContentPane());
		if(res == JOptionPane.YES_OPTION) {
			
			activeMSMSClusterDataSet.getClusters().removeAll(clusters);
			if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
				try {
					MSMSClusteringDBUtils.deleteClusters(clusters);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			loadMSMSClusterDataSetInGUI(activeMSMSClusterDataSet);
			if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
				MessageDialog.showWarningMsg(
						"You must save the offline project at the end "
						+ "of the session to preseve this change.", 
						this.getContentPane());
			}
		}
	}

	private void copyFeatureClusterMSMS(MsLibraryFormat format) {
		// TODO Auto-generated method stub
		IMsFeatureInfoBundleCluster[] clusters = 
				msmsFeatureClusterTreePanel.getSelectedClusters();
		if(clusters.length == 0)
			return;
		
		IMsFeatureInfoBundleCluster selectedCluster = clusters[0];
		List<MSFeatureInfoBundle> msmsFeatures = 
			selectedCluster.getComponents().stream().
			filter(f -> Objects.nonNull(f.getMsFeature().getSpectrum())).
			filter(f -> Objects.nonNull(f.getMsFeature().getSpectrum().getExperimentalTandemSpectrum())).
			collect(Collectors.toList());
		if(msmsFeatures.isEmpty()) {
			MessageDialog.showWarningMsg(
					"No features contatining MSMS spectra in the selected cluster", 
					this.getContentPane());
			return;
		}
		String copied = "";		
		Collection<String> mspOutput = new ArrayList<String>();
		
		if(format.equals(MsLibraryFormat.MSP)) {

			Map<String, Injection> injectionMap = 
					MSMSExportUtils.createInjectionMap(msmsFeatures);
			for (MSFeatureInfoBundle bundle : msmsFeatures) {

				Injection injection = null;
				if (bundle.getInjectionId() != null)
					injection = injectionMap.get(bundle.getInjectionId());

				Collection<String> featureMSPBlock = 
						MSMSExportUtils.createFeatureMSPBlock(bundle, injection);
				mspOutput.addAll(featureMSPBlock);
			}			
		}
		if(format.equals(MsLibraryFormat.SIRIUS_MS)) {
			
			if(selectedCluster instanceof BinnerBasedMsFeatureInfoBundleCluster) {
				
				Collection<SiriusMsMsCluster>bsSiriusClusters = 
						MSMSClusteringUtils.createMultipleSiriusMsClustersFromBinnerAnnotattedCluster(
						(BinnerBasedMsFeatureInfoBundleCluster)selectedCluster);
				for(SiriusMsMsCluster sc : bsSiriusClusters) {
					
					String siriusMsBlock = sc.getSiriusMsBlock();
					if(siriusMsBlock != null)
						mspOutput.add(siriusMsBlock);
				}
			}
			if(selectedCluster instanceof MsFeatureInfoBundleCluster) {
				
				SiriusMsMsCluster bsSiriusCluster = 
						new SiriusMsMsCluster((MsFeatureInfoBundleCluster)selectedCluster);
					
				String siriusMsBlock = bsSiriusCluster.getSiriusMsBlock();
				if(siriusMsBlock != null)
					mspOutput.add(siriusMsBlock);				
			}
		}
		copied = StringUtils.join(mspOutput, "\n");
		if(copied == null || copied.isEmpty())
			return;

		StringSelection stringSelection = new StringSelection(copied);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}

	private void setUpMSMSParentIonFrequencyAnalysis() {
		
		if(mzFrequencyAnalysisResultsDialog != null 
				&& mzFrequencyAnalysisResultsDialog.isVisible()) {
			mzFrequencyAnalysisResultsDialog.toFront();
			return;
		}
		if(msTwoFeatureTable.getTable().getModel().getRowCount() == 0)
			return;

		mzFrequencyAnalysisSetupDialog = 
				new MzFrequencyAnalysisSetupDialog(this, "Analyze MSMS parent ion frequency");
		mzFrequencyAnalysisSetupDialog.setLocationRelativeTo(this.getContentPane());
		mzFrequencyAnalysisSetupDialog.setVisible(true);
	}

	private void runMSMSParentIonFrequencyAnalysis() {

		double massWindowSize = 
				mzFrequencyAnalysisSetupDialog.getMZWindow();
		MassErrorType massWindowType = 
				mzFrequencyAnalysisSetupDialog.getMassErrorType();
		if(massWindowType == null || massWindowSize == 0) {
			MessageDialog.showErrorMsg("Invalid parameters!", mzFrequencyAnalysisSetupDialog);
			return;
		}
		mzFrequencyAnalysisSetupDialog.dispose();
		Collection<MsFeature> featuresToProcess = 
				msTwoFeatureTable.getBundles(TableRowSubset.ALL).stream().
				map(b -> b.getMsFeature()).collect(Collectors.toList());
		MzFrequencyAnalysisTask task = new MzFrequencyAnalysisTask(
				featuresToProcess, 
				MzFrequencyType.MSMS_PARENT_FREQUENCY,
				massWindowSize, 
				massWindowType);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);		
	}

	private void majorClusterFeatureExtractionSetup() {

		if(activeMSMSClusterDataSet == null 
				|| activeMSMSClusterDataSet.getClusters().isEmpty())
			return;
		
		majorClusterFeatureExtractionSetupDialog = 
				new MajorClusterFeatureExtractionSetupDialog(this);
		majorClusterFeatureExtractionSetupDialog.setLocationRelativeTo(this.getContentPane());
		majorClusterFeatureExtractionSetupDialog.setVisible(true);
	}
	
	private void extractMajorClusterFeatures() {

		MajorClusterFeatureDefiningProperty mcfp = 
				majorClusterFeatureExtractionSetupDialog.getMajorClusterFeatureDefiningProperty();
		
		if(activeMSMSClusterDataSet == null || activeMSMSClusterDataSet.getClusters().isEmpty())
			return;
		
		Collection<MSFeatureInfoBundle>definingFeatures = new ArrayList<MSFeatureInfoBundle>();

		for(IMsFeatureInfoBundleCluster cluster : activeMSMSClusterDataSet.getClusters()) {
			
			MSFeatureInfoBundle b = cluster.getDefiningFeature(mcfp);
			if(b != null)
				definingFeatures.add(b);
		}
		if(majorClusterFeatureExtractionSetupDialog.includeIdentifiedOnly()) {
			definingFeatures = definingFeatures.stream().
				filter(b -> b.getMsFeature().isIdentified()).
				collect(Collectors.toList());
		}
		majorClusterFeatureExtractionSetupDialog.dispose();	
		safelyLoadMSMSFeatures(definingFeatures);
	}
	
	private void showActiveDataSetSummary() {
		
		// TODO - deal with MS1 stuff later 
		
		if(activeFeatureCollection == null || activeFeatureCollection.getFeatures().isEmpty())
			return;

		datasetSummaryDialog = new DatasetSummaryDialog(activeFeatureCollection);
		datasetSummaryDialog.setLocationRelativeTo(IDWorkbenchPanel.this.getContentPane());
		datasetSummaryDialog.generateDataSetStats();
		datasetSummaryDialog.setVisible(true);	
	}
	
	private void showFilteredDataSetSummary() {
		
		// TODO - deal with MS1 stuff later 
		
		Collection<MSFeatureInfoBundle> msmsTableFeatures = 
				msTwoFeatureTable.getBundles(TableRowSubset.FILTERED);
		if(msmsTableFeatures.isEmpty())
			return;
		
		MsFeatureInfoBundleCollection filteredCollection = 
				new MsFeatureInfoBundleCollection("Filtered MSMS features");
		filteredCollection.addFeatures(msmsTableFeatures);
		
		datasetSummaryDialog = new DatasetSummaryDialog(filteredCollection);
		datasetSummaryDialog.setLocationRelativeTo(IDWorkbenchPanel.this.getContentPane());
		datasetSummaryDialog.generateDataSetStats();
		datasetSummaryDialog.setVisible(true);	
	}

	private void msmsClusterDataExportSetup() {

		if(activeMSMSClusterDataSet == null 
				|| activeMSMSClusterDataSet.getClusters().isEmpty())
			return;

		idTrackerMSMSClusterDataSetExportDialog = 
				new IDTrackerMSMSClusterDataSetExportDialog(this, activeMSMSClusterDataSet);
		idTrackerMSMSClusterDataSetExportDialog.setLocationRelativeTo(this.getContentPane());
		idTrackerMSMSClusterDataSetExportDialog.setVisible(true);
	}

	private void exportMSMSClusterData() {
		
		Collection<String> errors = 
				idTrackerMSMSClusterDataSetExportDialog.validateFormParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors), idTrackerMSMSClusterDataSetExportDialog);
			return;
		}	
		IDTrackerDataExportParameters params = 
				idTrackerMSMSClusterDataSetExportDialog.getIDTrackerDataExportParameters();
		IDTrackerMSMSClusterDataExportTask task = 
				new IDTrackerMSMSClusterDataExportTask(
					activeMSMSClusterDataSet,
					params,
					idTrackerMSMSClusterDataSetExportDialog.getOutputFile());
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);	
		idTrackerMSMSClusterDataSetExportDialog.dispose();
	}

	private void showMSMSClusterFilter() {
		
		if(activeMSMSClusterDataSet == null || activeMSMSClusterDataSet.getClusters().isEmpty())
			return;

		msmsClusterFilterDialog = new MSMSClusterFilterDialog(this);
		msmsClusterFilterDialog.setLocationRelativeTo(this.getContentPane());
		msmsClusterFilterDialog.setVisible(true);
	}

	private void filterMSMSClusters() {

		Collection<String>errors = msmsClusterFilterDialog.validateInput();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), msmsClusterFilterDialog);
			return;
		}
		Collection<IMsFeatureInfoBundleCluster> filteredClusters = 
				new ArrayList<IMsFeatureInfoBundleCluster>();
		filteredClusters.addAll(activeMSMSClusterDataSet.getClusters());
		Range mzRange = msmsClusterFilterDialog.getMzRange();
		if(mzRange != null) {
			filteredClusters = filteredClusters.stream().
				filter(c -> mzRange.contains(c.getMz())).collect(Collectors.toList());
		}
		Range rtRange = msmsClusterFilterDialog.getRTRange();
		if(rtRange != null) {
			filteredClusters = filteredClusters.stream().
				filter(c -> rtRange.contains(c.getRt())).collect(Collectors.toList());
		}
		String lookupName = msmsClusterFilterDialog.getLookupFeatureName();
		if(!lookupName.isEmpty()) {
			
			filteredClusters = filteredClusters.stream().
					filter(c -> c.getLookupFeature() != null).
					filter(c -> c.getLookupFeature().getName().equalsIgnoreCase(lookupName)).
					collect(Collectors.toList());
		}
		String compoundName = 
				msmsClusterFilterDialog.getCompoundName().toUpperCase();
		if(!compoundName.isEmpty()) {
			
			filteredClusters = filteredClusters.stream().
					filter(c -> c.getPrimaryIdentity() != null).
					filter(c -> c.getPrimaryIdentity().getCompoundIdentity().getName().toUpperCase().contains(compoundName)).
					collect(Collectors.toList());
		}
		String formula = msmsClusterFilterDialog.getFormula();
		if(!formula.isEmpty()) {
			
			filteredClusters = filteredClusters.stream().
					filter(c -> c.getPrimaryIdentity() != null).
					filter(c -> c.getPrimaryIdentity().getCompoundIdentity().getFormula().equals(formula)).
					collect(Collectors.toList());
		}
		if(msmsClusterFilterDialog.showIdentifiedOnly()) {
			
			filteredClusters = filteredClusters.stream().
					filter(c -> c.getPrimaryIdentity() != null).					
					collect(Collectors.toList());
		}
		if(filteredClusters.isEmpty()) {
			
			MessageDialog.showWarningMsg(
					"No clusters found matching all selected criteria", 
					msmsClusterFilterDialog);
			return;
		}
		else {
			clearMSMSFeatureData();
			msmsFeatureClusterTreePanel.loadFeatureClusters(filteredClusters);			
			Collection<MinimalMSOneFeature>lookupFeatures = 
					filteredClusters.stream().filter(c -> Objects.nonNull(c.getLookupFeature())).
					map(c -> c.getLookupFeature()).collect(Collectors.toSet());
			lookupFeatureTable.loadLookupFeatures(lookupFeatures);
			activeCluster = null;
			msmsClusterFilterDialog.dispose();
		}		
	}
	
	private void showMSMSClusterMZRTListFilter() {

		if(activeMSMSClusterDataSet == null || activeMSMSClusterDataSet.getClusters().isEmpty())
			return;

		msmsClusterMZRTListFilterDialog = new MSMSClusterMZRTListFilterDialog(this);
		msmsClusterMZRTListFilterDialog.setLocationRelativeTo(this.getContentPane());
		msmsClusterMZRTListFilterDialog.setVisible(true);
	}
	
	private void filterMSMSClustersByMZRTList() {
		
		Collection<String>errors = msmsClusterMZRTListFilterDialog.validateInput();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), msmsClusterMZRTListFilterDialog);
			return;
		}
		Collection<MinimalMSOneFeature> filterFeatures = 
				msmsClusterMZRTListFilterDialog.getSelectedFeatures();
		if(filterFeatures.isEmpty())
			filterFeatures = msmsClusterMZRTListFilterDialog.getAllFeatures();
		
		double mzWindow = msmsClusterMZRTListFilterDialog.getMzError();
		MassErrorType mzErrorType = msmsClusterMZRTListFilterDialog.getMassErrorType();
		double rtWindow = msmsClusterMZRTListFilterDialog.getRTError();
		
		Set<IMsFeatureInfoBundleCluster> clusters = activeMSMSClusterDataSet.getClusters();
		Collection<IMsFeatureInfoBundleCluster> filteredClusters = 
				new ArrayList<IMsFeatureInfoBundleCluster>();
		
		for(MinimalMSOneFeature ff : filterFeatures) {
			
			final Range rtRange = new Range(ff.getRt() - rtWindow, ff.getRt() + rtWindow);
			final Range mzRange = MsUtils.createMassRange(ff.getMz(), mzWindow, mzErrorType);
			clusters.stream().
				filter(c -> !c.containsFeaturesWithinRanges(rtRange, mzRange).isEmpty()).
				forEach(c -> {
						c.setLookupFeature(ff);
						filteredClusters.add(c);
					});				
		}
		if(filteredClusters.isEmpty()) {
			
			MessageDialog.showWarningMsg(
					"No matching clusters found", 
					msmsClusterMZRTListFilterDialog);
			return;
		}
		else {
			clearMSMSFeatureData();
			msmsFeatureClusterTreePanel.loadFeatureClusters(filteredClusters);			
			Collection<MinimalMSOneFeature>lookupFeatures = 
					filteredClusters.stream().filter(c -> Objects.nonNull(c.getLookupFeature())).
					map(c -> c.getLookupFeature()).collect(Collectors.toSet());
			lookupFeatureTable.loadLookupFeatures(lookupFeatures);
			activeCluster = null;
			msmsClusterMZRTListFilterDialog.dispose();
		}
	}

	private void reloadActiveMSMSClustersDataSet() {

		clearMSMSFeatureData();
		if(activeMSMSClusterDataSet == null 
				|| activeMSMSClusterDataSet.getClusters() == null)
			return;
		
		msmsFeatureClusterTreePanel.loadFeatureClusters(
					activeMSMSClusterDataSet.getClusters());
		lookupFeatureTable.loadDataSet(activeMSMSClusterDataSet);
			
		activeCluster = null; 
	}

	private void showMSMSClustersSummary() {

		if(activeMSMSClusterDataSet == null)
			return;
		
		MSMSCLusterDataSetSummaryDialog summaryDialog = 
				new MSMSCLusterDataSetSummaryDialog(activeMSMSClusterDataSet);
		summaryDialog.setLocationRelativeTo(this.getContentPane());
		summaryDialog.setVisible(true);
	}

	private void setSelectedIDasPrimaryForFeatureCluster() {
		
		if(activeCluster == null)
			return;
		
		MsFeatureIdentity newId = 
				identificationsTable.getSelectedIdentity();
		if(newId == null)
			return;
		
		activeCluster.setPrimaryIdentity(newId);		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
			
			try {
				MSMSClusteringDBUtils.updateMSMSClusterPrimaryIdentity(activeCluster);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		msmsFeatureClusterTreePanel.getTree().updateElement(activeCluster);
	}

	private void insertNewMSMSClusterDataSet() {

		Collection<String>errors = 
				msmsClusterDataSetEditorDialog.validateDataSet();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), msmsClusterDataSetEditorDialog);
			return;
		}	
		IMSMSClusterDataSet dataSet = msmsClusterDataSetEditorDialog.getMSMSClusterDataSet();
		if(dataSet == null) {
			
			dataSet = new MSMSClusterDataSet(
					msmsClusterDataSetEditorDialog.getMSMSClusterDataSetName(), 
					msmsClusterDataSetEditorDialog.getMSMSClusterDataSetDescription(), 
					MRC2ToolBoxCore.getIdTrackerUser());
			
//			dataSet.setParameters(
//					msmsClusterDataSetEditorDialog.getMsmsExtractionParameters());
//			if(msmsClusterDataSetEditorDialog.getClustersToAdd() != null)
//				dataSet.getClusters().addAll(msmsClusterDataSetEditorDialog.getClustersToAdd());
//			
//			dataSet.setFeatureLookupDataSet(
//					msmsClusterDataSetEditorDialog.getFeatureLookupDataSet());
		}
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
			
			MSMSClusterDataSetUploadTask task = 
					new MSMSClusterDataSetUploadTask(dataSet);
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
		else {
			MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().
				getMsmsClusterDataSets().add(dataSet);
			loadMSMSClusterDataSetInGUI(dataSet);
		}
		msmsClusterDataSetEditorDialog.dispose();		
	}

	private void showAddNewMSMSClusterDataSetDialog() {

		if(activeMSMSClusterDataSet == null)
			return;
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
			
			MessageDialog.showErrorMsg(
					"This option is not available for offline experiments", 
					this.getContentPane());
			return;
		}
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
			
			if(MSMSClusterDataSetManager.getMSMSClusterDataSetById(activeMSMSClusterDataSet.getId()) != null){
				
				MessageDialog.showErrorMsg(
						"Data set \"" + activeMSMSClusterDataSet.getName() + 
						"\" is already uploaded to the database", this.getContentPane());
				return;
			}	
		}
		else {
			IMSMSClusterDataSet existingSameNameSet = 
					MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().
							getMsmsClusterDataSets().stream().
							filter(s -> s.getName().equals(activeMSMSClusterDataSet.getName())).
							findFirst().orElse(null);
			if(existingSameNameSet != null) {
				
				if(activeMSMSClusterDataSet.equals(existingSameNameSet)) {
					
					MessageDialog.showWarningMsg(
							"Data set \"" + activeMSMSClusterDataSet.getName() + 
							"\" already exists in the experiment", this.getContentPane());
					return;
				}
				else {
					MessageDialog.showErrorMsg(
							"A different data set \"" + existingSameNameSet.getName() + 
							"\" already exists in the experiment", this.getContentPane());
					return;
				}				
			}
		}
		msmsClusterDataSetEditorDialog = 
				new MSMSClusterDataSetEditorDialog(
						activeMSMSClusterDataSet, 
						//	activeMSMSClusterDataSet.getClusters(), 
						MainActionCommands.ADD_MSMS_CLUSTER_DATASET_COMMAND,
						this);
//		msmsClusterDataSetEditorDialog.setMSMSClusterDataSetName(activeMSMSClusterDataSet.getName());
//		msmsClusterDataSetEditorDialog.setMSMSClusterDataSetDescription(activeMSMSClusterDataSet.getDescription());
//		msmsClusterDataSetEditorDialog.setMsmsExtractionParameters(activeMSMSClusterDataSet.getParameters());
//		msmsClusterDataSetEditorDialog.setFeatureLookupDataSet(activeMSMSClusterDataSet.getFeatureLookupDataSet());
		msmsClusterDataSetEditorDialog.setLocationRelativeTo(this.getContentPane());
		msmsClusterDataSetEditorDialog.setVisible(true);
	}

	private void setupSpectrumEntropyScoringParameters() {
		
		Collection<MSFeatureInfoBundle> bundles = 
				getMsMsFeatureBundles(TableRowSubset.ALL);
		if(bundles == null || bundles.isEmpty())
			return;

		entropyScoringSetupDialog = new EntropyScoringSetupDialog(this);
		entropyScoringSetupDialog.setLocationRelativeTo(this.getContentPane());
		entropyScoringSetupDialog.setVisible(true);
	}	
	
	private void recalculateSpectrumEntropyScores() {

		//	Check if parameters changed
		Collection<String> errors = entropyScoringSetupDialog.validateParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), entropyScoringSetupDialog);
			return;		
		}	
		entropyScoringSetupDialog.savePreferences();
		entropyScoringSetupDialog.dispose();
		
		//	Recalculate entropies/scores
		Collection<MSFeatureInfoBundle> bundles = 
				getMsMsFeatureBundles(TableRowSubset.ALL);
		if(bundles == null || bundles.isEmpty())
			return;
		
		SpectrumEntropyRecalculationTask task = 
				new SpectrumEntropyRecalculationTask(bundles);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	private void copySelectedLibraryFeatureSpectrumAsArray() {

		MsFeatureIdentity selectedIdentity = 
				identificationsTable.getSelectedIdentity();
		if(selectedIdentity == null)
			return;
		
		ReferenceMsMsLibraryMatch refMatch = 
				selectedIdentity.getReferenceMsMsLibraryMatch();
		if(refMatch == null)
			return;
		
		MsMsLibraryFeature feature = refMatch.getMatchedLibraryFeature();		
		String mspString = feature.getSpectrumAsPythonArray();
		StringSelection stringSelection = new StringSelection(mspString);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}
	
	private void copySelectedLibraryFeatureSpectrumAsMSP() {

		MsFeatureIdentity selectedIdentity = 
				identificationsTable.getSelectedIdentity();
		if(selectedIdentity == null)
			return;
		
		ReferenceMsMsLibraryMatch refMatch = 
				selectedIdentity.getReferenceMsMsLibraryMatch();
		if(refMatch == null)
			return;
		
		MsMsLibraryFeature feature = refMatch.getMatchedLibraryFeature();
		ArrayList<String>contents = new ArrayList<String>();
		
		Collection<MSPField>individual = new ArrayList<MSPField>();
		individual.add(MSPField.NAME);
		individual.add(MSPField.FORMULA);
		individual.add(MSPField.EXACT_MASS);
		individual.add(MSPField.MW);
		individual.add(MSPField.INCHI_KEY);
		individual.add(MSPField.PRECURSORMZ);
		individual.add(MSPField.NUM_PEAKS);
	
		CompoundIdentity cid = feature.getCompoundIdentity();
		contents.add(MSPField.NAME.getName() + ": " + cid.getName());

		if (cid.getFormula() != null)
			contents.add(MSPField.FORMULA.getName() + ": " + cid.getFormula());
		contents.add(MSPField.EXACT_MASS.getName() + ": "
				+ MRC2ToolBoxConfiguration.getMzFormat().format(cid.getExactMass()));
		contents.add(MSPField.MW.getName() + ": " + 
				Integer.toString((int) Math.round(cid.getExactMass())));
		if (cid.getInChiKey() != null)
			contents.add(MSPField.INCHI_KEY.getName() + ": " + cid.getInChiKey());

		Map<String, String> properties = feature.getProperties();
		properties.put(MSMSComponentTableFields.MRC2_LIB_ID.getName(), feature.getUniqueId());
		ReferenceMsMsLibrary refLib =
				IDTDataCache.getReferenceMsMsLibraryById(feature.getMsmsLibraryIdentifier());
		
		if((refLib.getPrimaryLibraryId().equals(NISTReferenceLibraries.nist_msms.name()) || 
				refLib.getPrimaryLibraryId().equals(NISTReferenceLibraries.hr_msms_nist.name()))
				&& properties.get(MSMSComponentTableFields.ORIGINAL_LIBRARY_ID.getName()) != null)
			contents.add(MSPField.NIST_NUM.getName() + ": " + 
					properties.get(MSMSComponentTableFields.ORIGINAL_LIBRARY_ID.getName()));
						
		for (MSMSComponentTableFields field : MSMSComponentTableFields.values()) {

			if (individual.contains(field.getMSPField()))
				continue;
			
			String prop = properties.get(field.getName());
			if(prop == null || prop.isEmpty())
				continue;
				
			contents.add(field.getMSPField().getName() + ": " + prop);					
		}
		contents.add(MSPField.PRECURSORMZ.getName() + ": "
				+ MRC2ToolBoxConfiguration.getMzFormat().format(feature.getParent().getMz()));
		contents.add(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(feature.getSpectrum().size()));

		for(MsPoint point : feature.getSpectrum()) {

			String msPoint = MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz())
					+ " " + MsUtils.mspIntensityFormat.format(point.getIntensity());
			String annotation = feature.getMassAnnotations().get(point);
			if(annotation != null)
				msPoint += " \"" + annotation + "\"";
				
			contents.add(msPoint);
		}
		contents.add("");	
		String mspString = StringUtils.join(contents, "\n");
		if(mspString == null || mspString.isEmpty())
			return;

		StringSelection stringSelection = new StringSelection(mspString);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}
	
	private void copySelectedMSMSFeatureSpectrumAsArray(){
		
		Collection<MSFeatureInfoBundle> selected = 
				msTwoFeatureTable.getBundles(TableRowSubset.SELECTED);
		if(selected.isEmpty())
			return;
		
		ArrayList<String>contents = new ArrayList<String>();
		for(MSFeatureInfoBundle bundle : selected) {
			
			MsFeature msf = bundle.getMsFeature();
			Collection<TandemMassSpectrum> tandemSpectra = msf.getSpectrum().getTandemSpectra().stream().
					filter(t -> t.getSpectrumSource().equals(SpectrumSource.EXPERIMENTAL)).
					collect(Collectors.toList());

			if(tandemSpectra.isEmpty())
				continue;
			
			for(TandemMassSpectrum tandemMs : tandemSpectra)
				contents.add(tandemMs.getSpectrumAsPythonArray());			
		}
		String mspString = StringUtils.join(contents, "\n");
		StringSelection stringSelection = new StringSelection(mspString);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}	

	private void copySelectedMSMSFeatureSpectrumAsMSP() {

		Collection<MSFeatureInfoBundle> selected = 
				msTwoFeatureTable.getBundles(TableRowSubset.SELECTED);
		if(selected.isEmpty())
			return;
		
		String copied = "";
		Map<String, Injection> injectionMap = 
				MSMSExportUtils.createInjectionMap(selected);
		Collection<String> mspOutput = new ArrayList<String>();
		for (MSFeatureInfoBundle bundle : selected) {

			Injection injection = null;
			if (bundle.getInjectionId() != null)
				injection = injectionMap.get(bundle.getInjectionId());

			Collection<String> featureMSPBlock = 
					MSMSExportUtils.createFeatureMSPBlock(bundle, injection);
			mspOutput.addAll(featureMSPBlock);
		}
		copied = StringUtils.join(mspOutput, "\n");
				
//		ArrayList<String>contents = new ArrayList<String>();
//		for(MSFeatureInfoBundle bundle : selected) {
//
//			MsFeature msf = bundle.getMsFeature();
//			Collection<TandemMassSpectrum> tandemSpectra = msf.getSpectrum().getTandemSpectra().stream().
//					filter(t -> t.getSpectrumSource().equals(SpectrumSource.EXPERIMENTAL)).
//					collect(Collectors.toList());
//
//			if(tandemSpectra.isEmpty())
//				continue;
//			
//			for(TandemMassSpectrum tandemMs : tandemSpectra) {
//
//				if(tandemMs.getSpectrum().isEmpty())
//					continue;
//
//				contents.add(MSPField.NAME.getName() + ": " + msf.getId());
//				contents.add("Feature name: " + msf.getName());
//				if(msf.isIdentified()) {
//					CompoundIdentity cid = msf.getPrimaryIdentity().getCompoundIdentity();
//					contents.add(MSPField.SYNONYM.getName() + ": " + cid.getName());
//					if(cid.getFormula() != null)
//						contents.add(MSPField.FORMULA.getName() + ": " + cid.getFormula());
//					if(cid.getInChiKey() != null)
//						contents.add(MSPField.INCHI_KEY.getName() + ": " + cid.getInChiKey());
//				}
//				String polarity = "P";
//				if(msf.getPolarity().equals(Polarity.Negative))
//					polarity = "N";
//				contents.add(MSPField.ION_MODE.getName() + ": " + polarity);
//
//				if(tandemMs.getCidLevel() >0)
//					contents.add(MSPField.COLLISION_ENERGY.getName() + ": " + Double.toString(tandemMs.getCidLevel()));
//
//				//	RT
//				contents.add(MSPField.RETENTION_INDEX.getName() + ": " +
//						MRC2ToolBoxConfiguration.getRtFormat().format(msf.getRetentionTime()) + " min. ");
//
//				contents.add(MSPField.PRECURSORMZ.getName() + ": " +
//					MRC2ToolBoxConfiguration.getMzFormat().format(tandemMs.getParent().getMz()));
//				contents.add(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(tandemMs.getSpectrum().size()));
//
//				MsPoint[] msms = MsUtils.normalizeAndSortMsPattern(tandemMs.getSpectrum());
//				for(MsPoint point : msms) {
//
//					contents.add(
//						MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz())
//						+ " " + MsUtils.mspIntensityFormat.format(point.getIntensity()) + "; ") ;
//				}
//				contents.add("");
//			}
//		}
//		String mspString = StringUtils.join(contents, "\n");
//		if(mspString == null || mspString.isEmpty())
//			return;
		
		if(copied == null || copied.isEmpty())
			return;

		StringSelection stringSelection = new StringSelection(copied);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}


	private void showFeatureRTIDSearchDialog() {
		// TODO Auto-generated method stub
		msmsFeatureRTIDSearchDialog = new MSMSFeatureRTIDSearchDialog(this);
		msmsFeatureRTIDSearchDialog.setLocationRelativeTo(this.getContentPane());
		msmsFeatureRTIDSearchDialog.setVisible(true);
	}

	private void searchFeaturesByRtId() {
		// TODO Auto-generated method stub
		
		
		msmsFeatureRTIDSearchDialog.dispose();
	}

	private void showFeatureFilter(Collection<MSFeatureInfoBundle>featuresToFilter) {
		
		if(featuresToFilter == null || featuresToFilter.isEmpty())
			return;
		
		filterTrackerFeaturesDialog = new FilterTrackerMSMSFeaturesDialog(this, featuresToFilter);
		filterTrackerFeaturesDialog.setLocationRelativeTo(this.getContentPane());
		filterTrackerFeaturesDialog.setVisible(true);
	}
	
	private void filterFeatureTable() {
		
		Collection<String> errors = 
				filterTrackerFeaturesDialog.verifyParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), filterTrackerFeaturesDialog);
			return;
		}	
		FilterMSMSFeaturesTask task = 
				new FilterMSMSFeaturesTask(filterTrackerFeaturesDialog.getFeaturesToFilter());
		IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
				"Filtering MSMS features ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}
	
	class FilterMSMSFeaturesTask extends LongUpdateTask {

		private Collection<MSFeatureInfoBundle>featuresToFilter;
		
		public FilterMSMSFeaturesTask(Collection<MSFeatureInfoBundle>featuresToFilter) {
			this.featuresToFilter = featuresToFilter;
		}

		@Override
		public Void doInBackground() {

			MSMSFilterParameters filterParameters = 
					filterTrackerFeaturesDialog.getMSMSFilterParameters();
			filterTrackerFeaturesDialog.dispose();

			Collection<MSFeatureInfoBundle>filtered = 
					MsFeatureStatsUtils.filterMSMSFeatureTable(
							featuresToFilter, 
							filterParameters);
			safelyLoadMSMSFeatures(filtered);			
			return null;
		}
	}
		
	private void showIDLFeatureFilter(Collection<MSFeatureInfoBundle>featuresToFilter) {
		
		if(featuresToFilter == null || featuresToFilter.isEmpty())
			return;
		
		annotationFilterDialog = new AnnotationFilterDialog(this, featuresToFilter);
		annotationFilterDialog.setLocationRelativeTo(this.getContentPane());
		annotationFilterDialog.setVisible(true);
	}
	
	private void filterFeatureTableByIDLAnnotations() {;
	
		Collection<String> errors = 
				annotationFilterDialog.verifyParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), annotationFilterDialog);
			return;
		}		
		FilterByIDLAnnotationMSMSFeaturesTask task = 
				new FilterByIDLAnnotationMSMSFeaturesTask(
						annotationFilterDialog.getFeaturesToFilter());
		IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
				"Filtering MSMS features by ID levels / annotations...", 
				this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}
	
	class FilterByIDLAnnotationMSMSFeaturesTask extends LongUpdateTask {

		private Collection<MSFeatureInfoBundle>featuresToFilter;
		
		public FilterByIDLAnnotationMSMSFeaturesTask(Collection<MSFeatureInfoBundle>featuresToFilter) {
			this.featuresToFilter = featuresToFilter;
		}

		@Override
		public Void doInBackground() {

			IDLAnnotationFilterParameters filterParameters = 
					annotationFilterDialog.getFilterParameters();
			annotationFilterDialog.dispose();

			Collection<MSFeatureInfoBundle>filtered = 
					MsFeatureStatsUtils.filterMSMSFeaturesByIDLAnnotation(
							featuresToFilter, 
							filterParameters);
			safelyLoadMSMSFeatures(filtered);			
			return null;
		}
	}

	private void setupTopMSMSHitsReassignment() {
		
		Collection<MSFeatureInfoBundle> allFeatures = 
				msTwoFeatureTable.getBundles(TableRowSubset.ALL);
		if(allFeatures.isEmpty())
			return;

		reassignDefaultMSMSLibraryHitDialog = new ReassignDefaultMSMSLibraryHitDialog(this);
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null)
			reassignDefaultMSMSLibraryHitDialog.blockCommitToDatabase();
		
		reassignDefaultMSMSLibraryHitDialog.setLocationRelativeTo(this.getContentPane());
		reassignDefaultMSMSLibraryHitDialog.setVisible(true);
	}
		
	private void reassignTopMSMSHits() {
		
		Collection<MSFeatureInfoBundle> allFeatures = 
				msTwoFeatureTable.getBundles(TableRowSubset.ALL);
		if(allFeatures.isEmpty())
			return;
		
		DefaultMSMSLibraryHitReassignmentTask task = 
				new DefaultMSMSLibraryHitReassignmentTask(
						allFeatures, 
						reassignDefaultMSMSLibraryHitDialog.getTopHitReassignmentOption(),
						reassignDefaultMSMSLibraryHitDialog.useEntropyScore(),
						reassignDefaultMSMSLibraryHitDialog.ignoreDecoys(),
						reassignDefaultMSMSLibraryHitDialog.commitChangesTodatabase());
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
		reassignDefaultMSMSLibraryHitDialog.dispose();
	}
	
	private void setupFeatureVsFeatureMSMSSearch(){
		//	TODO
		
		if(activeFeatureCollection == null || activeFeatureCollection.isEmpty())
			return;
		
		featureVsFeatureMSMSSearchSetupDialog = new FeatureVsFeatureMSMSSearchSetupDialog(this);
		featureVsFeatureMSMSSearchSetupDialog.setLocationRelativeTo(this.getContentPane());	
		featureVsFeatureMSMSSearchSetupDialog.setVisible(true);	
	}

	private void runFeatureVsFeatureMSMSSearch(){
		
		Collection<String>errors = 
				featureVsFeatureMSMSSearchSetupDialog.validateSearchSetup();
		
		TableRowSubset trs = 
				featureVsFeatureMSMSSearchSetupDialog.getFeaturesTableRowSubset();
		
		Collection<MSFeatureInfoBundle>inputFeatures = null;
		if(trs == null)
			inputFeatures = activeFeatureCollection.getFeatures();
		else
			inputFeatures = msTwoFeatureTable.getBundles(trs);
		
		if(inputFeatures == null || inputFeatures.isEmpty()) 
			errors.add("No input features selected for the search.")		;
		
		if(!errors.isEmpty()){
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), 
					featureVsFeatureMSMSSearchSetupDialog);
			return;
		}
		MsFeatureInfoBundleCollection featureLib = 
				featureVsFeatureMSMSSearchSetupDialog.getSelectedFeatureCollection();
		MSMSSearchParameterSet searchParameters =  
				featureVsFeatureMSMSSearchSetupDialog.getMSMSSearchParameters();
		featureVsFeatureMSMSSearchSetupDialog.dispose();
		
		FeatureVsFeatureMSMSSearchTask task = 
				new FeatureVsFeatureMSMSSearchTask(
						inputFeatures, featureLib, searchParameters);
		task.addTaskListener(this);			
		MRC2ToolBoxCore.getTaskController().addTask(task);		
	}

	private void setupFDRforMSMSlibraryIdentifications() {
		
		Collection<MSFeatureInfoBundle> allFeatures = 
				msTwoFeatureTable.getBundles(TableRowSubset.ALL);
		if(allFeatures.isEmpty())
			return;
		
		Map<NISTPepSearchParameterObject, Long>paramCounts = 
				NISTPepSearchUtils.getPepSearchParameterSetCountsForDataSet(allFeatures);
		
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
	
	private void calculateFDRforMSMSlibraryIdentifications() {
		
		Collection<String> errors = fdrEstimationSetupDialog.validateParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), fdrEstimationSetupDialog);
			return;
		}
		NISTPepSearchParameterObject paramSet = 
				fdrEstimationSetupDialog.getActiveNISTPepSearchParameterObject();		
		Collection<File> decoys = fdrEstimationSetupDialog.getSelectedDecoyLibraries();
		paramSet.getLibraryFiles().clear();
		paramSet.getLibraryFiles().addAll(decoys);
		Collection<MSFeatureInfoBundle> featuresToSearch = 
				NISTPepSearchUtils.fiterMSMSFeaturesByPepSearchParameterSet(
						msTwoFeatureTable.getBundles(TableRowSubset.ALL), 
						paramSet.getId());
		
		//	Check # of decoy hits relative to normal hits 
		//	(consider only best hits/primary IDs based on library search 
		PercolatorFDREstimationTask task = 
				new PercolatorFDREstimationTask(
						featuresToSearch,
						paramSet);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
		
		fdrEstimationSetupDialog.dispose();
	}
	
	private void showDataCollectionsManager() { 

		dataCollectionManagerDialog = new DataCollectionsManagerDialog();
		dataCollectionManagerDialog.setLocationRelativeTo(IDWorkbenchPanel.this.getContentPane());
		dataCollectionManagerDialog.setVisible(true);
	}
	
	private void showFeatureCollectionCombSubDialog(){
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
			MessageDialog.showWarningMsg(
					"This option is only available for database-stored data.\n"
					+ "Please close active offline raw data analysis experiment first.", 
					this.getContentPane());
			return;
		}	
		FeatureCollectionsOperationDialog featureCollectionsOperationDialog = 
				new FeatureCollectionsOperationDialog();
		featureCollectionsOperationDialog.setLocationRelativeTo(this.getContentPane());
		featureCollectionsOperationDialog.setVisible(true);	
	}
	
	class ShowFeatureAndClusterCollectionManagerTask extends LongUpdateTask {

		public ShowFeatureAndClusterCollectionManagerTask() {

		}

		@Override
		public Void doInBackground() {
			
			FeatureCollectionManager.refreshMsFeatureInfoBundleCollections();
			MSMSClusterDataSetManager.refreshMSMSClusterDataSetList();
			
			dataCollectionManagerDialog = new DataCollectionsManagerDialog();
			dataCollectionManagerDialog.setLocationRelativeTo(IDWorkbenchPanel.this.getContentPane());
			dataCollectionManagerDialog.setVisible(true);
			return null;
		}
	}
	
	private void showIDTrackerDataExplorerDialog() {	
		
		if(idTrackerDataExplorerPlotDialog == null) {
			idTrackerDataExplorerPlotDialog = new IDTrackerDataExplorerPlotFrame(this);
			idTrackerDataExplorerPlotDialog.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		}		
		idTrackerDataExplorerPlotDialog.setVisible(true);
	}
	
	private void mergeDuplicateMsMsFeatures() {
		
		LIMSExperiment experiment = IDTDataCache.getExperimentById("IDX0065");
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
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
			MessageDialog.showWarningMsg(
					"Please close active offline raw data analysis experiment first.", 
					this.getContentPane());
			return;
		}		
		idTrackerDataSearchDialog = new IDTrackerDataSearchDialog(this);
		idTrackerDataSearchDialog.setLocationRelativeTo(this.getContentPane());
		idTrackerDataSearchDialog.setVisible(true);
	}
	
	private void showTrackerSearchByExperimentMzRtDialog(){
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
			MessageDialog.showWarningMsg(
					"Please close active offline raw data analysis experiment first.", 
					this.getContentPane());
			return;
		}	
		experimentMzRtDataSearchDialog = new ExperimentMZRTDataSearchDialog(this);
		experimentMzRtDataSearchDialog.setLocationRelativeTo(this.getContentPane());
		experimentMzRtDataSearchDialog.setVisible(true);
	}
	
	private void showTrackerSearchActiveDataSetBMzRtDialog(){

		Collection<MSFeatureInfoBundle> allFeatures = 
				msTwoFeatureTable.getBundles(TableRowSubset.ALL);
		if(allFeatures.isEmpty())
			return;
		
		activeDataSetMZRTDataSearchDialog = new ActiveDataSetMZRTDataSearchDialog(this);
		activeDataSetMZRTDataSearchDialog.setLocationRelativeTo(this.getContentPane());
		activeDataSetMZRTDataSearchDialog.setVisible(true);
	}

	private void searchActiveDataSetBMzRt(){

		Collection<String>errors = 
				activeDataSetMZRTDataSearchDialog.validateParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), 
					activeDataSetMZRTDataSearchDialog);
			return;
		}
		Collection<MSFeatureInfoBundle> msmsFeatures = 
				msTwoFeatureTable.getBundles(TableRowSubset.ALL);
		
		FeatureLookupList flds = 
				activeDataSetMZRTDataSearchDialog.getDataSet();
		if(flds == null) {
			
			Collection<MinimalMSOneFeature> lookupFeatures = 
					activeDataSetMZRTDataSearchDialog.getAllFeatures();
			if(lookupFeatures == null || lookupFeatures.isEmpty()) {
				int res = MessageDialog.showChoiceWithWarningMsg(
						"Do you want to cluster all the features in the active data set?", 
						activeDataSetMZRTDataSearchDialog);
				if(res != JOptionPane.YES_OPTION)
					return;
			}
			flds = new FeatureLookupList(
					activeDataSetMZRTDataSearchDialog.getFeatureSetName(), 
					activeDataSetMZRTDataSearchDialog.getFeatureSetDescription(), 
					lookupFeatures);
		}
		MSMSClusteringParameterSet params = 
				activeDataSetMZRTDataSearchDialog.getParameters();

		MSMSFeatureClusteringTask task = 
				new MSMSFeatureClusteringTask(msmsFeatures, params, flds);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
		activeDataSetMZRTDataSearchDialog.dispose();
	}
	
	private void showTrackerSearchByBinnerAnnotationsDialog() {

//		Collection<MSFeatureInfoBundle> allFeatures = 
//				msTwoFeatureTable.getBundles(TableRowSubset.ALL);
//		if(allFeatures.isEmpty())
//			return;
		
		if(activeFeatureCollection == null || activeFeatureCollection.isEmpty())
			return;
		
		activeDataSetBinnerAnnotationsSearchDialog = 
				new ActiveDataSetBinnerAnnotationsSearchDialog(this);
		activeDataSetBinnerAnnotationsSearchDialog.setLocationRelativeTo(this.getContentPane());
		activeDataSetBinnerAnnotationsSearchDialog.setVisible(true);
	}

	private void searchActiveDataSetByBinnerAnnotations() {

		Collection<String>errors = 
				activeDataSetBinnerAnnotationsSearchDialog.validateParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), 
					activeDataSetBinnerAnnotationsSearchDialog);
			return;
		}
		Collection<MSFeatureInfoBundle> msmsFeatures = 
				msTwoFeatureTable.getBundles(TableRowSubset.ALL);
		
		BinnerAnnotationLookupDataSet balds = 
				activeDataSetBinnerAnnotationsSearchDialog.getDataSet();
		if(balds == null) {
			
			Collection<BinnerAnnotationCluster> annotationClusters = 
					activeDataSetBinnerAnnotationsSearchDialog.getAllClusters();

			balds = new BinnerAnnotationLookupDataSet(
					activeDataSetBinnerAnnotationsSearchDialog.getFeatureSetName(), 
					activeDataSetBinnerAnnotationsSearchDialog.getFeatureSetDescription(), 
					annotationClusters);
		}
		MSMSClusteringParameterSet params = 
				activeDataSetBinnerAnnotationsSearchDialog.getParameters();
		
		BinnerAnnotationLookupTask task = 
				new BinnerAnnotationLookupTask(msmsFeatures, params, balds);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
		
		activeDataSetBinnerAnnotationsSearchDialog.dispose();
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

	private void loadRawDataForCurrentMsMsFeatureSet() {
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
			MessageDialog.showWarningMsg(
					"Please close active offline raw data analysis experiment first.", 
					this.getContentPane());
			return;
		}	
		Collection<MSFeatureInfoBundle> featureSet = 
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
		
		MSFeatureInfoBundle bundle = null;
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
		
		Collection<MSFeatureInfoBundle>featuresToExport = null;	
		if(msLevel.equals(MsDepth.MS1))
			featuresToExport = msOneFeatureTable.getBundles(featureSubset);
		
		if(msLevel.equals(MsDepth.MS2)) 
			featuresToExport = msTwoFeatureTable.getBundles(featureSubset);
			
		featuresToExport = 
				MsFeatureStatsUtils.filterFeaturesByIdSubset(featuresToExport, idSubset);			
		if(featuresToExport.isEmpty()) {
			MessageDialog.showWarningMsg("No features to export using selected settings", 
					idTrackerDataExportDialog);
			return;
		}
		Collection<String> errors = idTrackerDataExportDialog.validateFormParameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), idTrackerDataExportDialog);
			return;
		}
		IDTrackerDataExportTask task = new IDTrackerDataExportTask(
				 featuresToExport,
				 idTrackerDataExportDialog.getIDTrackerDataExportParameters(),
				 idTrackerDataExportDialog.getOutputFile());
		
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);		
		idTrackerDataExportDialog.dispose();
	}

	private void setIdLevelForIdentification(String idLevelName) {

		MsFeatureIdentity identity = identificationsTable.getSelectedIdentity();
		if(identity == null)
			return;
		
		MSFeatureIdentificationLevel idLevel = null;
		if(idLevelName != null)
			idLevel = IDTDataCache.getMSFeatureIdentificationLevelByName(idLevelName);
		
		if(idLevel == null && identity.getIdentificationLevel() == null)
			return;
		
		if(idLevel != null && identity.getIdentificationLevel() != null 
				&& idLevel.equals(identity.getIdentificationLevel())) 
				return;
		
		identity.setIdentificationLevel(idLevel);
		MSFeatureInfoBundle msOneBundle = msOneFeatureTable.getSelectedBundle();
		if(msOneBundle != null) {
			
			if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
				
				try {
					IdLevelUtils.setIdLevelForReferenceMS1FeatureIdentification(identity);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//	updateTableDisplayForMSFeatures(Collections.singleton(msOneFeatureTable.getSelectedBundle()));
			msOneFeatureTable.getTable().updateFeatureData(Collections.singleton(msOneBundle));
			identificationsTable.refreshTable();
			return;
		}
		MSFeatureInfoBundle msTwoBundle = msTwoFeatureTable.getSelectedBundle();
		if(msTwoBundle != null) {
			
			if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
				
				String msmsId = msTwoBundle.getMsFeature().
						getSpectrum().getExperimentalTandemSpectrum().getId();
				try {
					IdLevelUtils.setIdLevelForMSMSFeatureIdentification(identity, msmsId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			msTwoFeatureTable.getTable().updateFeatureData(Collections.singleton(msTwoBundle));
			identificationsTable.refreshTable();
			return;
		}
	}
	
	private void initIdTableActions() {
		
        InputMap identificationsTableInputMap = 
        		identificationsTable.getTable().getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap identificationsTableActionMap = 
        		identificationsTable.getTable().getActionMap();

        InputMap msTwoFeatureTableInputMap = 
        		msTwoFeatureTable.getTable().getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap msTwoFeatureTableActionMap = 
        		msTwoFeatureTable.getTable().getActionMap();
        
        for(MSFeatureIdentificationLevel level : IDTDataCache.getMsFeatureIdentificationLevelList()) {
        	
        	if(level.getShorcut() != null) {
        		
        		identificationsTableInputMap.put(
	        			KeyStroke.getKeyStroke(level.getShorcut().charAt(0), MASK | InputEvent.SHIFT_DOWN_MASK), 
	        			level.getName()); 
        		identificationsTableActionMap.put(level.getName(), new IdLevelAction(level.getName()));
        		
        		msTwoFeatureTableInputMap.put(
	        			KeyStroke.getKeyStroke(level.getShorcut().charAt(0), MASK | InputEvent.SHIFT_DOWN_MASK), 
	        			MSFeatureIdentificationLevel.SET_PRIMARY + level.getName());       		
	        	msTwoFeatureTableActionMap.put(
	        			MSFeatureIdentificationLevel.SET_PRIMARY + level.getName(), 
	        			new IdLevelAction(MSFeatureIdentificationLevel.SET_PRIMARY + level.getName()));
        	}
        }
 	}
	
	public void runIdLevelUpdate(String idLevelCommand, String componentId) {
		
    	UpdateIdLevelTask task = new UpdateIdLevelTask(idLevelCommand, componentId);
    	IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
    			"Updating identification confidence level ...", 
    			IDWorkbenchPanel.this.getContentPane(), task);
    	idp.setLocationRelativeTo(IDWorkbenchPanel.this.getContentPane());
    	idp.setVisible(true);
	}
	
	private class IdLevelAction extends AbstractAction {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 558489381332029108L;
		private String idLevelCommand;
		
		public IdLevelAction(String idLevelCommand) {
			super();
			this.idLevelCommand = idLevelCommand;
		}
		
        @Override
        public void actionPerformed(ActionEvent e) {
        	
    		String componentId = (String)((JComponent)e.getSource()).getClientProperty(
    				MRC2ToolBoxCore.COMPONENT_IDENTIFIER);
    		
        	runIdLevelUpdate(idLevelCommand, componentId);			    			
    	} 
    }	
	
	class UpdateIdLevelTask extends LongUpdateTask {
	
		private String idLevelCommand;
		private String componentId;
		
		public UpdateIdLevelTask(
				String idLevelCommand, String componentId) {
			super();
			this.idLevelCommand = idLevelCommand;
			this.componentId = componentId;
		}
	
		@Override
		public Void doInBackground() {
						
			String idLevelName = idLevelCommand;
			identificationsTable.getTable().toggleIdentificationTableModelListener(false);
			if(idLevelCommand.startsWith(MSFeatureIdentificationLevel.SET_PRIMARY))
				idLevelName = idLevelCommand.replace(MSFeatureIdentificationLevel.SET_PRIMARY, "");
			
			if(idLevelCommand.equals(MainActionCommands.CLEAR_ID_LEVEL_COMMAND.getName()))
				idLevelName = null;
			
			// If command comes from identifications table
			if(componentId.equals(identificationsTable.getTable().
					getClientProperty(MRC2ToolBoxCore.COMPONENT_IDENTIFIER))) {
				
				identificationsTable.getTable().getSelectionModel().
						removeListSelectionListener(IDWorkbenchPanel.this);
							
				setIdLevelForIdentification(idLevelName);
				
	        	identificationsTable.getTable().getSelectionModel().
						addListSelectionListener(IDWorkbenchPanel.this);
			}
			//	If command comes from MSMS feature table
			if(componentId.equals(msTwoFeatureTable.getTable().
					getClientProperty(MRC2ToolBoxCore.COMPONENT_IDENTIFIER))) {
				
				msTwoFeatureTable.getTable().getSelectionModel().
						removeListSelectionListener(IDWorkbenchPanel.this);
				
				setPrimaryIdLevelForMultipleSelectedFeatures(idLevelName);
				
				msTwoFeatureTable.getTable().getSelectionModel().
						addListSelectionListener(IDWorkbenchPanel.this);
			}
			//	TODO If command comes from MSOne feature table
			if(componentId.equals(msOneFeatureTable.getTable().
					getClientProperty(MRC2ToolBoxCore.COMPONENT_IDENTIFIER))) {
				
			}			
			identificationsTable.getTable().toggleIdentificationTableModelListener(true);
			return null;
		}
	}
	
	public void setPrimaryIdLevelForMultipleFeatures(
			MSFeatureIdentificationLevel idLevel, 
			int msLevel,
			Collection<MsFeature>msFeatures,
			boolean updateSelectedOnly) throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		for(MsFeature msf : msFeatures) {
			
			MsFeatureIdentity identity = msf.getPrimaryIdentity();
			if(identity == null)
				continue;				

			if(idLevel == null && identity.getIdentificationLevel() == null)
				continue;
			
			if(idLevel != null && identity.getIdentificationLevel() != null) {
				
				if(idLevel.equals(identity.getIdentificationLevel())) 
					continue;
			}
			identity.setIdentificationLevel(idLevel);
			if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
				
				if(msLevel == 1) {
					
					try {
						IdLevelUtils.setIdLevelForReferenceMS1FeatureIdentification(identity, conn);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if(msLevel == 2) {
					
					String msmsId = msf.getSpectrum().getExperimentalTandemSpectrum().getId();
					try {
						IdLevelUtils.setIdLevelForMSMSFeatureIdentification(identity, msmsId, conn);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		ConnectionManager.releaseConnection(conn);
		Collection<MSFeatureInfoBundle>featureBundles = 
				getMsFeatureBundlesForFeatures(msFeatures, msLevel);

		updateMultipleFeatureDataInTable(featureBundles, msLevel, updateSelectedOnly);
	}
		
	private void setPrimaryIdLevelForMultipleSelectedFeatures(String idLevelName) {

		MSFeatureIdentificationLevel idLevel = 
				IDTDataCache.getMSFeatureIdentificationLevelByName(idLevelName);
		
		Collection<MsFeature> selectedMSfeatures = 
				msOneFeatureTable.getTableFeatures(TableRowSubset.SELECTED);
		if(!selectedMSfeatures.isEmpty()) {
			
			try {
				setPrimaryIdLevelForMultipleFeatures(idLevel, 1, selectedMSfeatures, true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		Collection<MsFeature> selectedMSMSfeatures = 
				msTwoFeatureTable.getTableFeatures(TableRowSubset.SELECTED);
		if(!selectedMSMSfeatures.isEmpty()) {
			
			try {
				setPrimaryIdLevelForMultipleFeatures(idLevel, 2, selectedMSMSfeatures, true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
	}

	private void showFeatureIdFollowupStepManager() {

		if(msOneFeatureTable.getBundles(TableRowSubset.SELECTED).isEmpty() 
				&& msTwoFeatureTable.getBundles(TableRowSubset.SELECTED).isEmpty())
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
		
		Collection<MSFeatureInfoBundle> selectedMSfeatures = 
				msOneFeatureTable.getBundles(TableRowSubset.SELECTED);
		Collection<MSFeatureIdentificationFollowupStep> followupSteps = 
				followupStepAssignmentDialog.getUsedFollowupSteps();
		
		if(!selectedMSfeatures.isEmpty()) {

			try {
				setIDFollowUpStepsForMultipleFeatures(
						followupSteps, 
						1,
						selectedMSfeatures,
						true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			return;
		}
		
		Collection<MSFeatureInfoBundle> selectedMSMSfeatures = 
				msTwoFeatureTable.getBundles(TableRowSubset.SELECTED);
		
		if(!selectedMSMSfeatures.isEmpty()) {

			try {
				setIDFollowUpStepsForMultipleFeatures(
						followupSteps, 
						2,
						selectedMSMSfeatures,
						true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
	}
	
	public void setIDFollowUpStepsForMultipleFeatures(
			Collection<MSFeatureIdentificationFollowupStep>followUpSteps, 
			int msLevel,
			Collection<MSFeatureInfoBundle> featureBundles,
			boolean updateSelectedOnly) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		for(MSFeatureInfoBundle fib : featureBundles) {
			
			fib.getIdFollowupSteps().clear();
			fib.getIdFollowupSteps().addAll(followUpSteps);
			
			if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
				
				if(msLevel == 1) {
					
					if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
						try {
							IdFollowupUtils.setIdFollowupStepsForMS1Feature(fib, conn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}					
				}
				if(msLevel == 2) {
					
					if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
						try {
							IdFollowupUtils.setIdFollowupStepsForMSMSFeature(fib, conn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		ConnectionManager.releaseConnection(conn);
		updateMultipleFeatureDataInTable(featureBundles, msLevel, updateSelectedOnly);
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

		Collection<StandardFeatureAnnotation> selectedAnnotations = 
				standardFeatureAnnotationAssignmentDialog.getUsedAnnotations();
		Collection<MSFeatureInfoBundle> selectedMSfeatures = 
				msOneFeatureTable.getBundles(TableRowSubset.SELECTED);
		
		if(!selectedMSfeatures.isEmpty()) {

			try {
				setStandardAnnotationsForSelectedFeatures(
						selectedAnnotations, 
						1,
						selectedMSfeatures,
						true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			return;
		}
		
		Collection<MSFeatureInfoBundle> selectedMSMSfeatures = 
				msTwoFeatureTable.getBundles(TableRowSubset.SELECTED);
		
		if(!selectedMSMSfeatures.isEmpty()) {
			
			try {
				setStandardAnnotationsForSelectedFeatures(
						selectedAnnotations, 
						2,
						selectedMSMSfeatures,
						true);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
	}
	
	public void setStandardAnnotationsForSelectedFeatures(
			Collection<StandardFeatureAnnotation>annotations, 
			int msLevel,
			Collection<MSFeatureInfoBundle> featureBundles,
			boolean updateSelectedOnly) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		for(MSFeatureInfoBundle fib : featureBundles) {
			
			fib.getStandadAnnotations().clear();
			fib.getStandadAnnotations().addAll(annotations);
			
			if(msLevel == 1) {
				
				if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
					try {
						StandardAnnotationUtils.setStandardFeatureAnnotationsForMS1Feature(fib, conn);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}					
			}
			if(msLevel == 2) {
				
				if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
					try {
						StandardAnnotationUtils.setStandardFeatureAnnotationsForMSMSFeature(fib, conn);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		ConnectionManager.releaseConnection(conn);		
		updateMultipleFeatureDataInTable(featureBundles, msLevel, updateSelectedOnly);
	}
	
	private void updateMultipleFeatureDataInTable(
			Collection<MSFeatureInfoBundle> featureBundles,
			int msLevel, 
			boolean updateSelectedOnly) {
		
		if(msLevel == 1) {
			
			if(updateSelectedOnly)
				updateTableDisplayForMSFeatures(featureBundles);
			else
				reloadCompleteActiveMSOneFeatureSet();	//	TODO when MS1 features are handled
		}
		if(msLevel == 2) {
			
			if(updateSelectedOnly)
				updateTableDisplayForMSMSFeatures(featureBundles);
			else
				reloadActiveMSMSFeatureCollection();
		}
	}
	
	public void setStandardAnnotationsForMultipleFeatures(
			Collection<StandardFeatureAnnotation>annotations, 
			int msLevel,
			Collection<MsFeature>msFeatures,
			boolean updateSelectedOnly) throws Exception{

		Collection<MSFeatureInfoBundle>featureBundles = null;
		if(msLevel == 1) 
			featureBundles = getMsFeatureBundlesForFeatures(msFeatures, 1);
		
		if(msLevel == 2) 
			featureBundles = getMsFeatureBundlesForFeatures(msFeatures, 2);

		Connection conn = ConnectionManager.getConnection();
		for(MSFeatureInfoBundle fib : featureBundles) {
			
			fib.getStandadAnnotations().clear();
			fib.getStandadAnnotations().addAll(annotations);
			
			if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
				
				if(msLevel == 1) {
					
					if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
						try {
							StandardAnnotationUtils.setStandardFeatureAnnotationsForMS1Feature(fib, conn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				if(msLevel == 2) {
					
					if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
						try {
							StandardAnnotationUtils.setStandardFeatureAnnotationsForMSMSFeature(fib, conn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		ConnectionManager.releaseConnection(conn);
		updateMultipleFeatureDataInTable(featureBundles, msLevel, updateSelectedOnly);
	}
	
	public Collection<MSFeatureInfoBundle>getMsFeatureBundlesForFeatures(
			Collection<MsFeature>features, int msLevel) {
		Collection<MSFeatureInfoBundle>bundles = new ArrayList<MSFeatureInfoBundle>();
		if(msLevel == 1) {
			
			Collection<MSFeatureInfoBundle> allMSOneBundles = 
					msOneFeatureTable.getBundles(TableRowSubset.ALL);					
			if(allMSOneBundles.isEmpty())
				return bundles;
			
			return allMSOneBundles.stream().
					filter(b -> features.contains(b.getMsFeature())).
					collect(Collectors.toList());
		}
		if(msLevel == 2) {
			
			Collection<MSFeatureInfoBundle> allMSMSBundles = 
					msTwoFeatureTable.getBundles(TableRowSubset.ALL);					
			if(allMSMSBundles.isEmpty())
				return bundles;
			
			return allMSMSBundles.stream().
					filter(b -> features.contains(b.getMsFeature())).
					collect(Collectors.toList());
		}
		return bundles;
	}
		
	public void createNewMsmsFeatureCollectionFromSelectedFeatures(
			Collection<MSFeatureInfoBundle> featuresToAdd) {
		
		if(featuresToAdd.isEmpty())
			return;
		
		dataCollectionManagerDialog = new DataCollectionsManagerDialog();
		dataCollectionManagerDialog.setFeaturesToAdd(featuresToAdd);
		dataCollectionManagerDialog.setLocationRelativeTo(this.getContentPane());
		dataCollectionManagerDialog.showMsFeatureCollectionEditorDialog(null);
	}
	
	public void addSelectedFeaturesToExistingMsMsFeatureCollection(
			Collection<MSFeatureInfoBundle> featuresToAdd) {
		
		if(featuresToAdd.isEmpty())
			return;
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
			
			if(FeatureCollectionManager.getEditableMsFeatureInfoBundleCollections().isEmpty()) {
				MessageDialog.showWarningMsg("No custom collections defined yet.\n"
						+ "Use \"Create new feature collection\" option instead.", this.getContentPane());
				return;
			}
		}
		else {
			if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().getEditableMsFeatureInfoBundleCollections().isEmpty()) {
				MessageDialog.showWarningMsg("No custom collections defined for the experiment yet.\n"
						+ "Use \"Create new feature collection\" option instead.", this.getContentPane());
				return;
			}
		}
		addFeaturesToCollectionDialog = new AddFeaturesToCollectionDialog(featuresToAdd, this);
		addFeaturesToCollectionDialog.setLocationRelativeTo(this.getContentPane());
		addFeaturesToCollectionDialog.setVisible(true);
	}
	
	private void addSelectedFeaturesToRecentCollection(String command) {
		
		Collection<MSFeatureInfoBundle> selectedMSMSFeatures = 
				msTwoFeatureTable.getBundles(TableRowSubset.SELECTED);
		if(selectedMSMSFeatures == null || selectedMSMSFeatures.isEmpty())
			return;
		
		String fsId = command.replace(
				MainActionCommands.ADD_FEATURES_TO_RECENT_FEATURE_COLLECTION_COMMAND.name() + "|", "");
		MsFeatureInfoBundleCollection fColl = 
				FeatureCollectionManager.getMsFeatureInfoBundleCollectionById(fsId);
		if(fColl == null) {
			MessageDialog.showErrorMsg(
					"Requested feature collection not found", this.getContentPane());
			return;
		}
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null)
			FeatureCollectionManager.addFeaturesToCollection(fColl, selectedMSMSFeatures);
		else
			fColl.addFeatures(selectedMSMSFeatures);
	}
	
	public void removeFeaturesFromActiveMsMsFeatureCollection(
			Collection<MSFeatureInfoBundle> featuresToRemove) {
		
		if(activeFeatureCollection == null || activeFeatureCollection.getFeatures().isEmpty())
			return;
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
						
			if(!FeatureCollectionManager.getEditableMsFeatureInfoBundleCollections().contains(activeFeatureCollection)) {
				MessageDialog.showWarningMsg("Collection \"" + activeFeatureCollection.getName() + 
						"\" represents database search results and can not be edited.", this.getContentPane());
				return;
			}
			String message = 
					"Do you really want to remove " + Integer.toString(featuresToRemove.size()) + 
					" selected features from collection \"" + 
					activeFeatureCollection.getName() + "\"?";
			int res = MessageDialog.showChoiceWithWarningMsg(message, this.getContentPane());
			if(res == JOptionPane.YES_OPTION) {
				activeFeatureCollection.removeFeatures(featuresToRemove);
				FeatureCollectionManager.removeFeaturesFromCollection(
						activeFeatureCollection, featuresToRemove);
			}
		}
		else {
			if(!MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().
					getEditableMsFeatureInfoBundleCollections().contains(activeFeatureCollection)) {
				MessageDialog.showWarningMsg("Collection \"" + activeFeatureCollection.getName() + 
						"\" can not be edited.", this.getContentPane());
				return;
			}
			String message = 
					"Do you really want to remove " + Integer.toString(featuresToRemove.size()) + 
					" selected features from collection \"" + 
					activeFeatureCollection.getName() + "\"?";
			int res = MessageDialog.showChoiceWithWarningMsg(message, this.getContentPane());
			if(res == JOptionPane.YES_OPTION)
				activeFeatureCollection.removeFeatures(featuresToRemove);		
		}		
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

	private void clearIdentificationsForSelectedFeatures() {

		Collection<MSFeatureInfoBundle> msOneSelectedBundles = 
				msOneFeatureTable.getBundles(TableRowSubset.SELECTED);
		Collection<MSFeatureInfoBundle> msTwoSelectedBundles = 
				msTwoFeatureTable.getBundles(TableRowSubset.SELECTED);
		
		if(msOneSelectedBundles.isEmpty() && msTwoSelectedBundles.isEmpty())
			return;
		
		boolean updated = false;
		if(!msOneSelectedBundles.isEmpty()) {
			try {
				updated = clearIdentificationsForMSOneFeatures(msOneSelectedBundles);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(updated)
				updateTableDisplayForMSFeatures(msOneSelectedBundles);
			
			return;
		}
		if(!msTwoSelectedBundles.isEmpty()) {			
			try {
				updated = clearIdentificationForMSMSFeatures(msTwoSelectedBundles);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(updated)
				updateTableDisplayForMSMSFeatures(msTwoSelectedBundles);
			
			return;
		}
	}
	
	public boolean clearIdentificationsForMSOneFeatures(
			Collection<MSFeatureInfoBundle> msOneBundles) throws Exception {
		
		if(msOneBundles == null || msOneBundles.isEmpty())
			return false;
		
		String yesNoQuestion = "Do you want to remove all identifications for ";
		if(msOneBundles.size() == 1) {
			MsFeature feature = msOneBundles.iterator().next().getMsFeature();
			yesNoQuestion += feature.getName() + "?";
		}
		else
			yesNoQuestion += "selected features?";
		
		if(MessageDialog.showChoiceWithWarningMsg(yesNoQuestion, this.getContentPane()) == JOptionPane.YES_OPTION) {
			
			Connection conn = ConnectionManager.getConnection();
			for(MSFeatureInfoBundle bundle : msOneBundles) {
				
				MsFeature msOneFeature = bundle.getMsFeature();
				msOneFeature.clearIdentification();
				
				if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
					try {
						IdentificationUtils.clearAllReferenceMS1FeatureIdentifications(msOneFeature.getId(), conn);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}				
			}
			ConnectionManager.releaseConnection(conn);
			return true;
		}	
		else
			return false;
	}
	
	public boolean clearIdentificationForMSMSFeatures(
			Collection<MSFeatureInfoBundle> msmsBundles) throws Exception {
		
		if(msmsBundles == null || msmsBundles.isEmpty())
			return false;
		
		String yesNoQuestion = "Do you want to remove all identifications for ";
		if(msmsBundles.size() == 1) {
			MsFeature feature = msmsBundles.iterator().next().getMsFeature();
			yesNoQuestion += feature.getName() + "?";
		}
		else
			yesNoQuestion += "selected features?";
		
		if(MessageDialog.showChoiceWithWarningMsg(yesNoQuestion, this.getContentPane()) == JOptionPane.YES_OPTION) {
			
			Connection conn = ConnectionManager.getConnection();
			for(MSFeatureInfoBundle bundle : msmsBundles) {			
				
				MsFeature feature = bundle.getMsFeature();
				TandemMassSpectrum msms = feature.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
				if(msms != null) {
					if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
						try {					
							IdentificationUtils.clearAllMSMSFeatureIdentifications(msms.getId(), conn);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					feature.clearIdentification();
				}
			}
			ConnectionManager.releaseConnection(conn);
			return true;
		}
		else
			return false;
	}
	
	private void disablePrimaryIdentificationForSelectedFeatures() {

		Collection<MSFeatureInfoBundle> msOneSelectedBundles = 
				msOneFeatureTable.getBundles(TableRowSubset.SELECTED);
		Collection<MSFeatureInfoBundle> msTwoSelectedBundles = 
				msTwoFeatureTable.getBundles(TableRowSubset.SELECTED);
		
		if(msOneSelectedBundles.isEmpty() && msTwoSelectedBundles.isEmpty())
			return;
		
		if(!msOneSelectedBundles.isEmpty()) {
			try {
				disablePrimaryIdentificationForMSOneFeatures(msOneSelectedBundles);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			updateTableDisplayForMSFeatures(msOneSelectedBundles);
			return;
		}
		if(!msTwoSelectedBundles.isEmpty()) {			
			try {
				disablePrimaryIdentificationForMSMSFeatures(msTwoSelectedBundles);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			updateTableDisplayForMSMSFeatures(msTwoSelectedBundles);
			return;
		}
	}
	
	public void disablePrimaryIdentificationForMSOneFeatures(
			Collection<MSFeatureInfoBundle> msOneBundles) throws Exception {
		
		if(msOneBundles == null || msOneBundles.isEmpty())
			return;
		
		Connection conn = ConnectionManager.getConnection();
		for(MSFeatureInfoBundle bundle : msOneBundles) {
			
			MsFeature msOneFeature = bundle.getMsFeature();
			msOneFeature.disablePrimaryIdentity();
			if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
				try {
					IdentificationUtils.disableReferenceMS1FeaturePrimaryIdentity(msOneFeature.getId(), conn);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}		
		ConnectionManager.releaseConnection(conn);
	}
	
	public void disablePrimaryIdentificationForMSMSFeatures(
			Collection<MSFeatureInfoBundle> msmsBundles) throws Exception {
		
		if(msmsBundles == null || msmsBundles.isEmpty())
			return;
		
		Connection conn = ConnectionManager.getConnection();
		for(MSFeatureInfoBundle bundle : msmsBundles) {
			
			MsFeature msTwoFeature = bundle.getMsFeature();
			msTwoFeature.disablePrimaryIdentity();
			if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
				try {
					String msmsId = msTwoFeature.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL).getId();
					IdentificationUtils.disableMSMSFeaturePrimaryIdentity(msmsId, conn);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}		
		ConnectionManager.releaseConnection(conn);
	}
	
	@Override
	public void updateSelectedFeatures() {
		
		Collection<MSFeatureInfoBundle> selectedMSfeatures = 
				msOneFeatureTable.getBundles(TableRowSubset.SELECTED);
		if(!selectedMSfeatures.isEmpty())
			updateTableDisplayForMSFeatures(selectedMSfeatures);

		Collection<MSFeatureInfoBundle> selectedMSMSfeatures = 
				msTwoFeatureTable.getBundles(TableRowSubset.SELECTED);
		if(!selectedMSMSfeatures.isEmpty()) 
			updateTableDisplayForMSMSFeatures(selectedMSMSfeatures);
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

		if(msOneFeatureTable.getSelectedBundle() == null 
				&& msTwoFeatureTable.getSelectedBundle() == null)
			return;
		
		MsFeatureIdentity id = identificationsTable.getSelectedIdentity();
		if(id == null)
			return;

		if(msOneFeatureTable.getSelectedBundle() != null) {
			
			deleteIdentificationForMSOneFeature(
					msOneFeatureTable.getSelectedBundle().getMsFeature(), id);
		}
		if(msTwoFeatureTable.getSelectedBundle() != null
				&& msTwoFeatureTable.getSelectedBundle().getMsFeature().
					getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL) != null) {
						
			 deleteIdentificationForMSMSFeature(
					 msTwoFeatureTable.getSelectedBundle().getMsFeature(), id);
		}
	}
	
	private void deleteIdentificationForMSOneFeature(
			MsFeature feature, MsFeatureIdentity id) {
		
		if(feature.getIdentifications().size() > 1 && feature.getPrimaryIdentity().equals(id)) {
			
			MessageDialog.showWarningMsg(
					"You are trying to remove primary identification.\n"
					+ "Please specify new primary identification first.", 
					this.getContentPane());
			return;
		}
		String yesNoQuestion = "Do you want to remove identification\n" + 
				id.getCompoundIdentity().getName() +"\nfor " + feature.getName() + "?";
		if(MessageDialog.showChoiceWithWarningMsg(
				yesNoQuestion, this.getContentPane()) == JOptionPane.YES_OPTION) {
			
			feature.removeIdentity(id);
			
			//	For database-stored data:
			if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
				
				if(id.getMsRtLibraryMatch() != null) {
					try {
						IdentificationUtils.removeReferenceMS1FeatureLibraryMatch(id);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						IdentificationUtils.removeReferenceMS1FeatureManualId(id);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}				
			}			
			updateTableDisplayForMSFeatures(
					getMsFeatureBundlesForFeatures(Collections.singleton(feature), 1));
		}
	}
	
	private void deleteIdentificationForMSMSFeature(
			MsFeature feature, MsFeatureIdentity id) {
		
		if(feature.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL) == null)
			return;
		
		if(feature.getIdentifications().size() > 1 && feature.getPrimaryIdentity().equals(id)) {
			
			MessageDialog.showWarningMsg(
					"You are trying to remove primary identification.\n"
					+ "Please specify new primary identification first.", 
					this.getContentPane());
			return;
		}
		String yesNoQuestion = "Do you want to remove identification " + 
				id.getCompoundIdentity().getName() +" for " + feature.getName() + "?";
		if(MessageDialog.showChoiceWithWarningMsg(
				yesNoQuestion, this.getContentPane()) == JOptionPane.YES_OPTION) {
			
			feature.removeIdentity(id);
			
			//	For database-stored data:
			if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
				
				if(id.getReferenceMsMsLibraryMatch() != null) {
					try {					
						IdentificationUtils.removeMSMSFeatureLibraryMatch(id);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					try {
						IdentificationUtils.removeMSMSFeatureManualIdentification(id);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			updateTableDisplayForMSMSFeatures(
					getMsFeatureBundlesForFeatures(Collections.singleton(feature), 2));			
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
		
		MsFeatureIdentity manualId = manualIdentificationDialog.getSelectedFeatureIdentity();
		if(manualId == null) {
			MessageDialog.showWarningMsg("No compound ID selected.", manualIdentificationDialog);
			return;
		}	
		manualId.setAssignedBy(MRC2ToolBoxCore.getIdTrackerUser());
		manualId.setIdentificationLevel(IDTDataCache.getTopMSFeatureIdentificationLevel());
				
		if(msOneFeatureTable.getSelectedBundle() != null) {
			
			setManualIdForMSOneFeature(
					msOneFeatureTable.getSelectedBundle().getMsFeature(), manualId);
		}
		if(msTwoFeatureTable.getSelectedBundle() != null 
				&& msTwoFeatureTable.getSelectedBundle().getMsFeature().
					getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL) != null) {
			
			setManualIdForMSMSFeature(
					msTwoFeatureTable.getSelectedBundle().getMsFeature(), manualId);
		}		
		manualIdentificationDialog.dispose();
	}
	
	private void setManualIdForMSOneFeature(
			MsFeature feature, MsFeatureIdentity manualId){
		
		feature.setPrimaryIdentity(manualId);
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
			try {
				IdentificationUtils.addReferenceMS1FeatureManualId(feature.getId(), manualId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTableDisplayForMSFeatures(
				getMsFeatureBundlesForFeatures(Collections.singleton(feature), 1));	
	}

	private void setManualIdForMSMSFeature(
			MsFeature feature, MsFeatureIdentity manualId){
		
		feature.setPrimaryIdentity(manualId);
		TandemMassSpectrum msms = 
				feature.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
			try {
				IdentificationUtils.addMSMSFeatureManualId(msms.getId(), manualId);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		updateTableDisplayForMSMSFeatures(
				getMsFeatureBundlesForFeatures(Collections.singleton(feature), 2));	
	}
	
	private void searchSelectedMsOneFeaturesAgainstCompoundDatabase() {
		// TODO Auto-generated method stub

	}

	private void searchSelectedMsOneFeaturesAgainstLibrary() {
		// TODO Auto-generated method stub

	}

	private void exportMsMsfeaturesToMspFile() {

		Collection<MSFeatureInfoBundle> toExport = null;
		Collection<MSFeatureInfoBundle> allFeatures = 
				msTwoFeatureTable.getBundles(TableRowSubset.ALL);
		Collection<MSFeatureInfoBundle> selectedFeatures = 
				msTwoFeatureTable.getBundles(TableRowSubset.SELECTED);
		if(allFeatures.isEmpty())
			return;

		if(selectedFeatures.isEmpty()) {

			int res = MessageDialog.showChoiceMsg(
				"Do you want to export all features in the table?", 
				this.getContentPane());

			if(res == JOptionPane.NO_OPTION)
				return;

			toExport = allFeatures;
		}
		else
			toExport = selectedFeatures;
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter(MsLibraryFormat.MSP.getName(), MsLibraryFormat.MSP.getFileExtension());
		fc.setTitle("Export MSMS features to MSP file:");
		fc.setMultiSelectionEnabled(false);
		fc.setSaveButtonText("Export");
		String defaultFileName = "MSMS_feature_export_" + 
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) 
				+ "." + MsLibraryFormat.MSP.getFileExtension();
		fc.setDefaultFileName(defaultFileName);
		
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File exportFile = fc.getSelectedFile();
			if(exportFile != null) {

				ExtendedMSPExportTask task = 
						new ExtendedMSPExportTask(toExport, exportFile);
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
		
		Collection<MSFeatureInfoBundle>featuresToExport = 
				MsFeatureStatsUtils.filterFeaturesByIdSubset(
						msTwoFeatureTable.getBundles(siriusDataExportDialog.getFeatureSubset()), 
						siriusDataExportDialog.getFeatureSubsetByIdentification());
		
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
		IDTrackerSiriusMsExportWithClusteringTask task = 
				new IDTrackerSiriusMsExportWithClusteringTask(
						featuresToExport,
						rtError,
						mzError,
						exportFile);
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);	
		siriusDataExportDialog.dispose();
	}
	
	private void exportClustersToSiriusMSFile() {
		
		if(activeMSMSClusterDataSet == null || activeMSMSClusterDataSet.getClusters().isEmpty())
			return;		
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter(MsLibraryFormat.SIRIUS_MS.getName(), MsLibraryFormat.SIRIUS_MS.getFileExtension());
		fc.setTitle("Export MSMS features to MSP file:");
		fc.setMultiSelectionEnabled(false);
		fc.setSaveButtonText("Export");
		String defaultFileName = "MSMS_cluster_export_4SIRIUS_" + 
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) 
				+ "." + MsLibraryFormat.SIRIUS_MS.getFileExtension();
		fc.setDefaultFileName(defaultFileName);
		
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File exportFile = fc.getSelectedFile();
			if(exportFile != null) {

				IDTrackerSiriusMsClusterExportTask task = 
						new IDTrackerSiriusMsClusterExportTask(activeMSMSClusterDataSet, exportFile);
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
		}
	}
	
	private void runNistMsPepSearch() {

		Collection<String> errors = pepSearchSetupDialog.validateparameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), pepSearchSetupDialog);
			return;
		}
		pepSearchSetupDialog.savePreferences();
		List<String>commandParts = pepSearchSetupDialog.getSearchCommandParts();
		if(pepSearchSetupDialog.getFeaturesFromDatabase()) {

			Collection<MSFeatureInfoBundle> fToSearch = 
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
			if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null)
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
		pepSearchSetupDialog.savePreferences();
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
			
			Collection<MSFeatureInfoBundle> fToSearch = 
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
		pepSearchSetupDialog.savePreferences();
		pepSearchSetupDialog.dispose();
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
			if (e.getSource().getClass().equals(IDTrackerExperimentDataFetchTask.class))
				finalizeIdTrackerExperimentLoad((IDTrackerExperimentDataFetchTask)e.getSource());

			if (e.getSource().getClass().equals(IDTMS1FeatureSearchTask.class))
				loadMsOneSearchData(((IDTMS1FeatureSearchTask)e.getSource()).getSelectedFeatures());
			
			if (e.getSource().getClass().equals(IDTMSMSFeatureSearchTask.class)) 
				finalizeMSMSFeatureSearch((IDTMSMSFeatureSearchTask)e.getSource());
			
			if (e.getSource().getClass().equals(NISTMspepSearchOfflineTask.class))
				finalizeNISTMspepSearchOfflineTask((NISTMspepSearchOfflineTask)e.getSource());

			if (e.getSource().getClass().equals(NISTMsPepSearchRoundTripTask.class))
				finalizeNISTMspepSearchRoundTripTask((NISTMsPepSearchRoundTripTask)e.getSource());
			
			if (e.getSource().getClass().equals(IDTrackerDataExportTask.class))
				finalizeIDTrackerExportTask((IDTrackerDataExportTask)e.getSource());	
			
			if (e.getSource().getClass().equals(IDTrackerMSMSClusterDataExportTask.class))
				finalizeIDTrackerMSMSClusterDataExportTask((IDTrackerMSMSClusterDataExportTask)e.getSource());	
					
			if (e.getSource().getClass().equals(RawDataLoadForInjectionsTask.class))
				finalizeRawDataLoadTask((RawDataLoadForInjectionsTask)e.getSource());
			
			if (e.getSource().getClass().equals(RawDataRepositoryIndexingTask.class))
				MessageDialog.showInfoMsg("Raw data repository scan completed.", this.getContentPane());	
					
			if (e.getSource().getClass().equals(ChromatogramExtractionTask.class))
				finalizeCromatogramExtractionTask((ChromatogramExtractionTask)e.getSource());
			
			if (e.getSource().getClass().equals(IDTMSMSFeatureDataPullTask.class))
				finalizeIDTMSMSFeatureDataPullTask((IDTMSMSFeatureDataPullTask)e.getSource());	
			
			if (e.getSource().getClass().equals(IDTMSMSFeatureDataPullWithFilteringTask.class))
				finalizeIDTMSMSFeatureDataPullWithFilteringTask((IDTMSMSFeatureDataPullWithFilteringTask)e.getSource());
			
			if (e.getSource().getClass().equals(PercolatorFDREstimationTask.class))
				finalizePercolatorFDREstimationTask((PercolatorFDREstimationTask)e.getSource());
			
			if (e.getSource().getClass().equals(DefaultMSMSLibraryHitReassignmentTask.class))
				finalizeDefaultMSMSLibraryHitReassignmentTask((DefaultMSMSLibraryHitReassignmentTask)e.getSource());
			
			if (e.getSource().getClass().equals(SpectrumEntropyRecalculationTask.class))
				finalizeSpectrumEntropyRecalculation();
			
			if (e.getSource().getClass().equals(MSMSFeatureClusteringTask.class))
				finalizeMSMSFeatureClusteringTask((MSMSFeatureClusteringTask)e.getSource());
			
			if (e.getSource().getClass().equals(IDTMSMSClusterDataPullTask.class))
				finalizeIDTMSMSClusterDataPullTask((IDTMSMSClusterDataPullTask)e.getSource());	
			
			if (e.getSource().getClass().equals(MSMSClusterDataSetUploadTask.class))
				finalizeMSMSClusterDataSetUploadTask((MSMSClusterDataSetUploadTask)e.getSource());	
			
			if (e.getSource().getClass().equals(MzFrequencyAnalysisTask.class))
				finalizeMzFrequencyAnalysisTask((MzFrequencyAnalysisTask)e.getSource());
			
			if (e.getSource().getClass().equals(BinnerAnnotationLookupTask.class))
				finalizeBinnerAnnotationLookupTask((BinnerAnnotationLookupTask)e.getSource());
			
			if (e.getSource().getClass().equals(FeatureVsFeatureMSMSSearchTask.class))
				finalizeFeatureVsFeatureMSMSSearchTask((FeatureVsFeatureMSMSSearchTask)e.getSource());		
		}
		if (e.getStatus() == TaskStatus.CANCELED || e.getStatus() == TaskStatus.ERROR) {
			((AbstractTask) e.getSource()).removeTaskListener(this);
			MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
			MainWindow.hideProgressDialog();
		}
	}

	private void finalizeFeatureVsFeatureMSMSSearchTask(FeatureVsFeatureMSMSSearchTask task) {

		IMSMSClusterDataSet featureVsFeatureSearchResults = task.getSearchResults();
		if(featureVsFeatureSearchResults == null 
				|| featureVsFeatureSearchResults.getClusters().isEmpty()) {
			
			MessageDialog.showWarningMsg("No matches found.", this.getContentPane());
			return;
		}	
		MainWindow.hideProgressDialog();
		//	MRC2ToolBoxCore.getTaskController().getTaskQueue().removeTask(task);
		MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
		
		LoadMSMSClusterDataSetInGUITask ldt = 
				new LoadMSMSClusterDataSetInGUITask(featureVsFeatureSearchResults);
    	IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
    			"Loading results ...", 
    			IDWorkbenchPanel.this.getContentPane(), ldt);
    	idp.setLocationRelativeTo(IDWorkbenchPanel.this.getContentPane());
    	idp.setVisible(true);
	}
	
	private void finalizeMzFrequencyAnalysisTask(MzFrequencyAnalysisTask task) {

		Collection<MzFrequencyObject>mzFrequencyObjects = task.getMzFrequencyObjects();
		String binningParameter = 
				MRC2ToolBoxConfiguration.getPpmFormat().format(task.getMassWindowSize()) 
				+ " " + task.getMassWindowType().name();
		
		mzFrequencyAnalysisResultsDialog = new MzFrequencyAnalysisResultsDialog(
								this, mzFrequencyObjects, binningParameter);
		mzFrequencyAnalysisResultsDialog.setLocationRelativeTo(this.getContentPane());
		mzFrequencyAnalysisResultsDialog.setVisible(true);
	}

	private void finalizeIdTrackerExperimentLoad(IDTrackerExperimentDataFetchTask task) {

		LIMSExperiment experiment = task.getIdTrackerExperiment();
		RecentDataManager.addIDTrackerExperiment(experiment);		
		StatusBar.setExperimentName(experiment.toString());
		
		FeatureCollectionManager.msmsSearchResults.clearCollection();
		FeatureCollectionManager.msmsSearchResults.addFeatures(task.getSelectedFeatures());
		
//		activeFeatureCollection = FeatureCollectionManager.msmsSearchResults;
		
		activeFeatureCollection = new MsFeatureInfoBundleCollection(
				"Features for experiment " + experiment.toString());		
		activeFeatureCollection.addFeatures(task.getSelectedFeatures());
		safelyLoadMSMSFeatures(activeFeatureCollection.getFeatures());
		StatusBar.setActiveFeatureCollection(activeFeatureCollection);
	}

	private void finalizeIDTrackerMSMSClusterDataExportTask(IDTrackerMSMSClusterDataExportTask task) {

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

	private void finalizeMSMSClusterDataSetUploadTask(MSMSClusterDataSetUploadTask task) {

		loadMSMSClusterDataSetInGUI(task.getDataSet());
		MessageDialog.showInfoMsg("MSMS cluster data set \"" + task.getDataSet().getName() + 
				"\" was saved to the database", this.getContentPane());		
	}

	private void finalizeIDTMSMSClusterDataPullTask(IDTMSMSClusterDataPullTask source) {
		loadMSMSClusterDataSetInGUI(source.getDataSet());
	}
	
	
	private void finalizeBinnerAnnotationLookupTask(BinnerAnnotationLookupTask source) {
		loadMSMSClusterDataSetInGUI(source.getMSMSClusterDataSet());
	}

	private void finalizeMSMSFeatureClusteringTask(MSMSFeatureClusteringTask source) {
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null)
			MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().
					getMsmsClusterDataSets().add(source.getMsmsClusterDataSet());
		
		loadMSMSClusterDataSetInGUI(source.getMsmsClusterDataSet());
	}
		
	class LoadMSMSClusterDataSetInGUITask extends LongUpdateTask {

		private IMSMSClusterDataSet dataSet;
		
		public LoadMSMSClusterDataSetInGUITask(IMSMSClusterDataSet dataSet) {
			this.dataSet = dataSet;
		}

		@Override
		public Void doInBackground() {
			
			loadMSMSClusterDataSetInGUI(dataSet);			
			return null;
		}
		
//		@Override
//		public void done() {
//			
//			super.done();
//		}
	}
	
	private void loadMSMSClusterDataSetInGUI(IMSMSClusterDataSet dataSet) {
		
		clearMSMSFeatureData();
		clearMSMSClusterData();
		activeMSMSClusterDataSet = dataSet;
		
		if(activeMSMSClusterDataSet != null) {
			
			Set<IMsFeatureInfoBundleCluster> clusters = dataSet.getClusters();
			msmsFeatureClusterTreePanel.loadFeatureClusters(clusters);
			lookupFeatureTable.loadDataSet(dataSet);
			activeCluster = null;
		
			activeFeatureCollection = new MsFeatureInfoBundleCollection(
					"Features for \"" + activeMSMSClusterDataSet.getName() + "\" data set");		
			activeFeatureCollection.addFeatures(activeMSMSClusterDataSet.getAllFeatures());
			safelyLoadMSMSFeatures(activeFeatureCollection.getFeatures());
			StatusBar.setActiveFeatureCollection(activeFeatureCollection);
			StatusBar.setActiveMSMSClusterDataSet(activeMSMSClusterDataSet);
		}
	}

	private void finalizeSpectrumEntropyRecalculation() {
		
		MSFeatureInfoBundle selected = msTwoFeatureTable.getSelectedBundle();
		reloadActiveMSMSFeatureCollection();			
		if(selected != null)
			msTwoFeatureTable.selectBundle(selected);
	}

	private void finalizeDefaultMSMSLibraryHitReassignmentTask(
			DefaultMSMSLibraryHitReassignmentTask source) {

		MSFeatureInfoBundle selected = msTwoFeatureTable.getSelectedBundle();
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
		
		if(task.getParentCollection() != null) {
			activeFeatureCollection = task.getParentCollection();
		}
		else {	
			//	activeFeatureCollection = FeatureCollectionManager.msmsSearchResults;
			activeFeatureCollection.clearCollection();
			activeFeatureCollection.addFeatures(task.getSelectedFeatures());
		}
		safelyLoadMSMSFeatures(activeFeatureCollection.getFeatures());
		StatusBar.setActiveFeatureCollection(activeFeatureCollection);
		activeMSMSClusterDataSet = null;
		activeCluster = null;
	}
	
	private void finalizeIDTMSMSFeatureDataPullWithFilteringTask(
			IDTMSMSFeatureDataPullWithFilteringTask task) {
		
		if(task.getMsmsClusterDataSet() != null)
			loadMSMSClusterDataSetInGUI(task.getMsmsClusterDataSet());
		else {
			activeFeatureCollection.clearCollection();
			activeFeatureCollection.setName("feature clustering results");			
			activeFeatureCollection.addFeatures(task.getSelectedFeatures());
//			FeatureCollectionManager.msmsSearchResults.clearCollection();
//			FeatureCollectionManager.msmsSearchResults.addFeatures(task.getSelectedFeatures());
//			activeFeatureCollection = FeatureCollectionManager.msmsSearchResults;
			safelyLoadMSMSFeatures(activeFeatureCollection.getFeatures());
			StatusBar.setActiveFeatureCollection(activeFeatureCollection);
			activeMSMSClusterDataSet = null;
			activeCluster = null;
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

	private void finalizeIDTrackerExportTask(IDTrackerDataExportTask task) {

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

		Collection<MSFeatureInfoBundle> bundles = msTwoFeatureTable.getTable().getFilteredBundles();
		MSFeatureInfoBundle selected = msTwoFeatureTable.getSelectedBundle();
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
							task.getPepSearchParameterObject());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadMSMSClusterDataSet(IMSMSClusterDataSet selectedDataSet) {
		
		if(selectedDataSet.equals(MSMSClusterDataSetManager.msmsClusterSearchResults)) {
			reloadCompleteActiveMSMSClusterDataSet();
			return;
		}
		if(selectedDataSet.equals(MSMSClusterDataSetManager.msOneClusterSearchResults)) {
			reloadCompleteActiveMSOneClusterDataSet();
			return;
		}
		activeMSMSClusterDataSet = selectedDataSet;
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
			
			IDTMSMSClusterDataPullTask task = 
					MSMSClusterDataSetManager.getMSMSClusterDataSetData(selectedDataSet);
			if(task == null) {
				 reloadActiveMSMSClusterDataSet();
			}
			else {
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
		}
		else {
			 loadMSMSClusterDataSetInGUI(activeMSMSClusterDataSet);
		}
		RecentDataManager.addIMSMSClusterDataSet(selectedDataSet);	
	}	
	
	private void reloadActiveMSMSClusterDataSet() {
		// TODO Auto-generated method stub
	}

	private void reloadCompleteActiveMSOneClusterDataSet() {
		// TODO Auto-generated method stub
		
	}

	private void reloadCompleteActiveMSMSClusterDataSet() {
		// TODO Auto-generated method stub
		
	}

	public void loadMSMSFeatureInformationBundleCollection(
			MsFeatureInfoBundleCollection selectedCollection) {
		
		clearMSMSFeatureData();
		clearMSMSClusterData();
		
		if(selectedCollection.equals(FeatureCollectionManager.msmsSearchResults)) {
			reloadCompleteActiveMSMSFeatureSet();
			return;
		}
		if(selectedCollection.equals(FeatureCollectionManager.msOneSearchResults)) {
			reloadCompleteActiveMSOneFeatureSet();
			return;
		}
		activeFeatureCollection = selectedCollection;
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
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
		else {
			 reloadActiveMSMSFeatureCollection();
		}
		RecentDataManager.addFeatureCollection(selectedCollection);
	}
	
	private void reloadCompleteDataSet() {

		clearMSMSClusterData();
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null) {
			
			activeFeatureCollection.clearCollection();
			activeFeatureCollection.setName(FeatureCollectionManager.activeExperimentFeatureSet.getName());
			activeFeatureCollection.addFeatures(FeatureCollectionManager.activeExperimentFeatureSet.getFeatures());			
			safelyLoadMSMSFeatures(activeFeatureCollection.getFeatures());
			
			Collection<MSFeatureInfoBundle> msOneData = 
					MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().getMsOneFeatureBundles();
			if(msOneData != null && !msOneData.isEmpty())
				safelyLoadMSOneFeatures(msOneData);
			
			StatusBar.setActiveFeatureCollection(activeFeatureCollection);
		}
		else {
			//	MSMS search data 
			if(FeatureCollectionManager.msmsSearchResults == null 
					|| FeatureCollectionManager.msmsSearchResults.isEmpty()) {
				clearMSMSFeatureData();
				return;
			}
			else {
				activeFeatureCollection.clearCollection();	
				activeFeatureCollection.setName(FeatureCollectionManager.msmsSearchResults.getName());			
				activeFeatureCollection.addFeatures(FeatureCollectionManager.msmsSearchResults.getFeatures());
				StatusBar.setActiveFeatureCollection(activeFeatureCollection);
				safelyLoadMSMSFeatures(activeFeatureCollection.getFeatures());
				activeMSMSClusterDataSet = null;
				activeCluster = null;
			}			
			//	TODO MS1 search data?
		}
	}
	
	private void finalizeMSMSFeatureSearch(IDTMSMSFeatureSearchTask task) {
		
		FeatureCollectionManager.msmsSearchResults.clearCollection();
		FeatureCollectionManager.msmsSearchResults.addFeatures(task.getSelectedFeatures());
		clearMSMSFeatureData();		
		activeFeatureCollection.clearCollection();	
		activeFeatureCollection.setName(FeatureCollectionManager.msmsSearchResults.getName());			
		activeFeatureCollection.addFeatures(FeatureCollectionManager.msmsSearchResults.getFeatures());
		StatusBar.setActiveFeatureCollection(activeFeatureCollection);
		safelyLoadMSMSFeatures(activeFeatureCollection.getFeatures());
		activeMSMSClusterDataSet = null;
		activeCluster = null;
	}

	public void filterMSMSFeatures(Collection<MSFeatureInfoBundle>featuresToInclude) {
		
		if(activeFeatureCollection == null)
			return;
		
		Collection<MSFeatureInfoBundle>filtered = 
				activeFeatureCollection.getFeatures().stream().
				filter(f -> featuresToInclude.contains(f)).collect(Collectors.toSet());
		
		safelyLoadMSMSFeatures(filtered);
	}

	public void reloadCompleteActiveMSMSFeatureSet() {
		
		if(activeFeatureCollection == null)
			activeFeatureCollection = 
				new MsFeatureInfoBundleCollection(
						FeatureCollectionManager.msmsSearchResults.getName());
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
				
			activeFeatureCollection.clearCollection();	
			activeFeatureCollection.setName(FeatureCollectionManager.msmsSearchResults.getName());			
			activeFeatureCollection.addFeatures(FeatureCollectionManager.msmsSearchResults.getFeatures());
			StatusBar.setActiveFeatureCollection(activeFeatureCollection);
			safelyLoadMSMSFeatures(activeFeatureCollection.getFeatures());
		}
		else {
			activeFeatureCollection.clearCollection();	
			activeFeatureCollection.setName(FeatureCollectionManager.activeExperimentFeatureSet.getName());			
			activeFeatureCollection.addFeatures(FeatureCollectionManager.activeExperimentFeatureSet.getFeatures());
			StatusBar.setActiveFeatureCollection(activeFeatureCollection);
			safelyLoadMSMSFeatures(activeFeatureCollection.getFeatures());
		}
	}
	
	private void reloadActiveMSMSFeatureCollection() {
		
		if(activeFeatureCollection == null)
			return;
		
		safelyLoadMSMSFeatures(activeFeatureCollection.getFeatures());			
		StatusBar.setActiveFeatureCollection(activeFeatureCollection);
	}
	
	private void reloadActiveMSMSClusterSetFeatures() {
		
		if(activeMSMSClusterDataSet == null)
			return;
		else
			loadMSMSClusterDataSetInGUI(activeMSMSClusterDataSet);
	}
	
	public void safelyLoadMSMSFeatures(
			Collection<MSFeatureInfoBundle>featuresToLoad) {
		
		clearFeatureData();
		msTwoFeatureTable.getTable().
			getSelectionModel().removeListSelectionListener(this);
		msTwoFeatureTable.getTable().
			setTableModelFromFeatureList(featuresToLoad);
		msTwoFeatureTable.getTable().
			getSelectionModel().addListSelectionListener(this);
	}

	private void loadMsOneSearchData(Collection<MSFeatureInfoBundle> features) {
		
		FeatureCollectionManager.msOneSearchResults.clearCollection();
		FeatureCollectionManager.msOneSearchResults.addFeatures(features);
		safelyLoadMSOneFeatures(FeatureCollectionManager.msOneSearchResults.getFeatures());
	}

	public void filterMsOneFeatures(
			Collection<MSFeatureInfoBundle>featuresToInclude) {

		Collection<MSFeatureInfoBundle>filtered = 
				FeatureCollectionManager.msOneSearchResults.getFeatures().stream().
				filter(f -> featuresToInclude.contains(f)).collect(Collectors.toSet());
	
		safelyLoadMSOneFeatures(filtered);
	}
	
	public void reloadCompleteActiveMSOneFeatureSet() {
		safelyLoadMSOneFeatures(FeatureCollectionManager.msOneSearchResults.getFeatures());
	}
	
	public void safelyLoadMSOneFeatures(
			Collection<MSFeatureInfoBundle>featuresToLoad) {
		
		msOneFeatureTable.getTable().getSelectionModel().removeListSelectionListener(this);
		msOneFeatureTable.getTable().
			setTableModelFromFeatureList(featuresToLoad);
		msOneFeatureTable.getTable().getSelectionModel().addListSelectionListener(this);
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
	public synchronized void clearPanel() {

		clearWorkbench();
		FeatureCollectionManager.clearDefaultCollections();
		MSMSClusterDataSetManager.clearDefaultCollections();
		
		activeCluster = null;
		if(activeFeatureCollection != null 
				&& FeatureCollectionManager.getEditableMsFeatureInfoBundleCollections().contains(activeFeatureCollection))		
			activeFeatureCollection = null;
		
		if(activeMSMSClusterDataSet != null 
				&& MSMSClusterDataSetManager.getEditableMSMSClusterDataSets().contains(activeMSMSClusterDataSet))
			activeMSMSClusterDataSet = null;
	}
	
	public void clearWorkbench() { 
		
		msOneFeatureTable.getTable().clearTable();
		msTwoFeatureTable.getTable().clearTable();
		referenceMolStructurePanel.clearPanel();
		clearFeatureData();
		clearDataMaps();
		clearMSMSClusterData();
		
		if(idTrackerDataExplorerPlotDialog != null)
			idTrackerDataExplorerPlotDialog.clearPanels();
		
		if(multipleSpectraDisplayDialog != null 
				&& multipleSpectraDisplayDialog.isVisible())
			multipleSpectraDisplayDialog.clearPanel();
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
		chromatogramPanel.clearPanel();
		binnerAnnotationDetailsPanel.clearPanel();
	}
	
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
				showMsFeatureInfoBundle(msOneFeatureTable.getSelectedBundle());
				return;
			}
			if(listener.equals(msTwoFeatureTable.getTable())) {
				
				msOneFeatureTable.getTable().getSelectionModel().removeListSelectionListener(this);
				msOneFeatureTable.getTable().clearSelection();
				msOneFeatureTable.getTable().getSelectionModel().addListSelectionListener(this);
				showMsFeatureInfoBundle(msTwoFeatureTable.getSelectedBundle());
				return;
			}
			if(listener.equals(identificationsTable.getTable())){
				
				showFeatureIdentity(identificationsTable.getSelectedIdentity());
				return;
			}
			if(listener.equals(lookupFeatureTable.getTable())){
				
				if(activeMSMSClusterDataSet == null)
					return;
				
				MinimalMSOneFeature lookupFeature = 
						lookupFeatureTable.getSelectedFeature();
				IMsFeatureInfoBundleCluster cluster = 
						activeMSMSClusterDataSet.getClusters().stream().
						filter(c -> Objects.nonNull(c.getLookupFeature())).
						filter(c -> c.getLookupFeature().equals(lookupFeature)).
						findFirst().orElse(null);
				if(cluster != null)
					msmsFeatureClusterTreePanel.selectFeatureCluster(cluster);
				else
					msTwoFeatureTable.getTable().clearTable();
			}
		}					
	}

	public MSFeatureInfoBundle getSelectedMSMSFeatureBundle() {
		return msTwoFeatureTable.getSelectedBundle();
	}

	public MSFeatureInfoBundle getSelectedMSFeatureBundle() {
		return msOneFeatureTable.getSelectedBundle();
	}
	
	public void updateIdentificationsForMSFeatures(Collection<MSFeatureInfoBundle> selectedBundles) {
		
		if(selectedBundles == null || selectedBundles.isEmpty())
			return;
		
		identificationsTable.getTable().toggleIdentificationTableModelListener(false);
		msOneFeatureTable.getTable().getSelectionModel().removeListSelectionListener(this);
		for(MSFeatureInfoBundle bundle : selectedBundles) {
			
			MsFeatureIdentity primaryId = bundle.getMsFeature().getPrimaryIdentity();
			if(primaryId != null && MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
				try {
					IdentificationUtils.setReferenceMS1FeaturePrimaryIdentity(
							bundle.getMsFeature().getId(),
							primaryId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}	
		msOneFeatureTable.getTable().updateFeatureData(selectedBundles);
		identificationsTable.getTable().toggleIdentificationTableModelListener(true);
		msOneFeatureTable.getTable().getSelectionModel().addListSelectionListener(this);
	}

	public void updateIdentificationsForMSMSFeatures(Collection<MSFeatureInfoBundle> selectedBundles) {
		
		if(selectedBundles == null || selectedBundles.isEmpty())
			return;
		
		msTwoFeatureTable.getTable().getSelectionModel().removeListSelectionListener(this);
		identificationsTable.getTable().toggleIdentificationTableModelListener(false);
		for(MSFeatureInfoBundle bundle : selectedBundles) {
			
			MsFeatureIdentity primaryId = bundle.getMsFeature().getPrimaryIdentity();			
			if(primaryId != null && MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
				
				TandemMassSpectrum msmsFeature = 
						bundle.getMsFeature().getSpectrum().getExperimentalTandemSpectrum();

				try {
					IdentificationUtils.setMSMSFeaturePrimaryIdentity(
							msmsFeature.getId(),
							primaryId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
		}
		msTwoFeatureTable.getTable().updateFeatureData(selectedBundles);
		identificationsTable.refreshTable();
		identificationsTable.getTable().toggleIdentificationTableModelListener(true);
		msTwoFeatureTable.getTable().getSelectionModel().addListSelectionListener(this);
	}
		
	public void updateTableDisplayForMSFeatures(Collection<MSFeatureInfoBundle> selectedBundles) {
		
		UpdateTableDisplayForFeaturesTask task = 
				new UpdateTableDisplayForFeaturesTask(selectedBundles, 1);
		IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
    			"Updating selected MS1 features ...", 
    			IDWorkbenchPanel.this.getContentPane(), task);
    	idp.setLocationRelativeTo(IDWorkbenchPanel.this.getContentPane());
    	idp.setVisible(true);
	}
		
	public void updateTableDisplayForMSMSFeatures(Collection<MSFeatureInfoBundle> selectedBundles) {
		
		UpdateTableDisplayForFeaturesTask task = 
				new UpdateTableDisplayForFeaturesTask(selectedBundles, 2);
		IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
    			"Updating selected MSMS features ...", 
    			IDWorkbenchPanel.this.getContentPane(), task);
    	idp.setLocationRelativeTo(IDWorkbenchPanel.this.getContentPane());
    	idp.setVisible(true);
	}
	
	class UpdateTableDisplayForFeaturesTask extends LongUpdateTask {	
		
		private Collection<MSFeatureInfoBundle> featureBundles;
		private int msLevel;
		
		public UpdateTableDisplayForFeaturesTask(
				Collection<MSFeatureInfoBundle> featureBundles, int msLevel) {
			this.featureBundles = featureBundles;
			this.msLevel = msLevel;
		}
	
		@Override
		public Void doInBackground() {
			
			if(featureBundles == null || featureBundles.isEmpty())
				return null;
			
			identificationsTable.getTable().toggleIdentificationTableModelListener(false);
			if(msLevel == 1) {
				
				msOneFeatureTable.getTable().getSelectionModel().removeListSelectionListener(IDWorkbenchPanel.this);		
				msOneFeatureTable.getTable().updateFeatureData(featureBundles);
					
				msOneFeatureTable.getTable().getSelectionModel().addListSelectionListener(IDWorkbenchPanel.this);
				showMsFeatureInfoBundle(featureBundles.iterator().next());
				msOneFeatureTable.getTable().scrollToSelected();			
			}
			if(msLevel == 2) {
					
				msTwoFeatureTable.getTable().getSelectionModel().removeListSelectionListener(IDWorkbenchPanel.this);
				msTwoFeatureTable.getTable().updateFeatureData(featureBundles);
				
				msTwoFeatureTable.getTable().getSelectionModel().addListSelectionListener(IDWorkbenchPanel.this);
				showMsFeatureInfoBundle(featureBundles.iterator().next());
				msTwoFeatureTable.getTable().scrollToSelected();			
			}	
			identificationsTable.refreshTable();
			identificationsTable.getTable().toggleIdentificationTableModelListener(true);
			return null;
		}
		
		@Override
		public void done() {			

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {

					if(standardFeatureAnnotationAssignmentDialog != null 
							&& standardFeatureAnnotationAssignmentDialog.isVisible()) {
						standardFeatureAnnotationAssignmentDialog.dispose();						
					}
					if(followupStepAssignmentDialog != null 
							&& followupStepAssignmentDialog.isVisible()) {
						followupStepAssignmentDialog.dispose();						
					}					
				}
			});
			super.done();
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
	
	public void refreshIdentificationsTable() {
		identificationsTable.refreshTable();
	}

	private void showMsFeatureInfoBundle(MSFeatureInfoBundle selectedBundle) {
				
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
		if(!msOne.isEmpty())
			msOnePlot.showMsForFeature(feature);
					
		//	Add MSMS
		if(!feature.getSpectrum().getTandemSpectra().isEmpty()) {
			
			msMsInfoPanel.loadFeatureData(feature);
			//	Get experimental spectrum
			//	TODO deal with MSn n > 2
			TandemMassSpectrum instrumentSpectrum =
				feature.getSpectrum().getExperimentalTandemSpectrum();
			
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
		
		//	Show chromatogram
		showFeatureChromatogram(selectedBundle);
		
		//	Last step since may take long time for noisy scans
		if(!msOne.isEmpty())
			msOneTable.setTableModelFromMsFeature(feature);
	}
	
	private void showFeatureChromatogram(MSFeatureInfoBundle selectedBundle) {
		
		MsFeatureChromatogramBundle msfCb = null;
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null 
				&& MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().getChromatogramMap() != null) {
			
			msfCb = MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().
					getChromatogramMap().get(selectedBundle.getMsFeature().getId());
		}
		else {
			msfCb = FeatureChromatogramUtils.retrieveFeatureChromatogramBundleFromCache(
					selectedBundle.getMSFeatureId());
			if(msfCb == null) {
				
				try {
					msfCb = FeatureChromatogramUtils.getMsFeatureChromatogramBundleForFeature(
							selectedBundle.getMSFeatureId(),
							selectedBundle.getDataFile());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(msfCb != null)
					FeatureChromatogramUtils.putFeatureChromatogramBundleInCache(msfCb);
			}
		}
		TandemMassSpectrum msms = null;	
		if(selectedBundle.getMsFeature().getSpectrum() != null)
			msms = selectedBundle.getMsFeature().getSpectrum().getExperimentalTandemSpectrum();
		
		if(msfCb != null) {
			
//			Collection<Double>markers = 
//					RawDataUtils.getMSMSScanRtMarkersForFeature(
//							selectedBundle.getMsFeature(), selectedBundle.getDataFile());
			
			Collection<Double>markers = null;
			if(msms != null)
				markers = msms.getMSMSScanRtList();
			
			chromatogramPanel.showMsFeatureChromatogramBundle(msfCb, markers);
		}		
	}
	
	private void showMultipleFeatureChromatograms(Collection<MSFeatureInfoBundle> selectedBundles) {
		
		Collection<MsFeatureChromatogramBundle> chromatograms = 
				new ArrayList<MsFeatureChromatogramBundle>();
		Collection<Double>markers = new TreeSet<Double>();
		for(MSFeatureInfoBundle bundle : selectedBundles) {
			
			MsFeatureChromatogramBundle msfCb = null;			
			if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() != null 
					&& MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().getChromatogramMap() != null) {				
				msfCb = MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().
						getChromatogramMap().get(bundle.getMsFeature().getId());
			}
			else {
				msfCb = FeatureChromatogramUtils.retrieveFeatureChromatogramBundleFromCache(
						bundle.getMSFeatureId());
			}
			if(msfCb != null)
				chromatograms.add(msfCb);
			
			if(bundle.getMsFeature().getSpectrum() != null) {
				
				TandemMassSpectrum msms = 
						bundle.getMsFeature().getSpectrum().getExperimentalTandemSpectrum();		
				if(msms != null)
					markers.addAll(msms.getMSMSScanRtList());
			}	
		}
		chromatogramPanel.showMultipleMsFeatureChromatogramBundles(chromatograms, markers);
	}
	
	private void loadPepSearchParameters(MsFeatureIdentity featureId) {
			
		if(featureId.getReferenceMsMsLibraryMatch() == null)
			return;
			
		String parId = featureId.getReferenceMsMsLibraryMatch().getSearchParameterSetId();
		if(parId == null)
			return;
			
		NISTPepSearchParameterObject pepSearchParams = IDTDataCache.getNISTPepSearchParameterObjectById(parId);
		pepSearchParameterListingPanel.loadNISTPepSearchParameterObject(pepSearchParams);
	}

	public Collection<MSFeatureInfoBundle>getMsMsFeatureBundles(TableRowSubset subset){
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
	
	public void selectMSMSFeature(MSFeatureInfoBundle toSelect) {
		
		if(toSelect != null)
			msTwoFeatureTable.selectBundle(toSelect);
	}
	
	public void selectMSFeature(MSFeatureInfoBundle toSelect) {
		
		if(toSelect != null)
			msOneFeatureTable.selectBundle(toSelect);
	}

	public MsFeatureInfoBundleCollection getActiveFeatureCollection() {
		return activeFeatureCollection;
	}

	public void loadFeaturesFromRawDataExperiment(
			RawDataAnalysisExperiment activeRawDataAnalysisProject) {
		
		clearPanel();		
		FeatureCollectionManager.activeExperimentFeatureSet.clearCollection();
		FeatureCollectionManager.activeExperimentFeatureSet.addFeatures(
				activeRawDataAnalysisProject.getMsMsFeatureBundles());
		activeRawDataAnalysisProject.addMsFeatureInfoBundleCollection(
				FeatureCollectionManager.activeExperimentFeatureSet);
		activeFeatureCollection = FeatureCollectionManager.activeExperimentFeatureSet;
		safelyLoadMSMSFeatures(activeFeatureCollection.getFeatures());
		
		Collection<MSFeatureInfoBundle> msOneData = 
				activeRawDataAnalysisProject.getMsOneFeatureBundles();
		if(msOneData != null && !msOneData.isEmpty())
			safelyLoadMSOneFeatures(msOneData);
		
		RecentDataManager.addFeatureCollection(FeatureCollectionManager.activeExperimentFeatureSet);		
		StatusBar.setActiveFeatureCollection(activeFeatureCollection);
	}

	@Override
	public void populatePanelsMenu() {
		// TODO Auto-generated method stub
		super.populatePanelsMenu();
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {

		MSMSFeatureClusterTree tree = (MSMSFeatureClusterTree)e.getSource();

		if (tree.getClickedObject() instanceof IMsFeatureInfoBundleCluster) {
			
			IMsFeatureInfoBundleCluster cluster = 
					(IMsFeatureInfoBundleCluster)tree.getClickedObject();
			activeCluster = cluster;
			//	safelyLoadMSMSFeatures(cluster.getComponents());
			
			clearFeatureData();
			msTwoFeatureTable.getTable().
				getSelectionModel().removeListSelectionListener(this);
			msTwoFeatureTable.getTable().
				setTableModelFromFeatureCluster(cluster);
			msTwoFeatureTable.getTable().
				getSelectionModel().addListSelectionListener(this);
			
			
			referenceMolStructurePanel.clearPanel();		
			if(cluster.getLookupFeature() != null && cluster.getLookupFeature().getSmiles() != null)
				referenceMolStructurePanel.showStructure(cluster.getLookupFeature().getSmiles());		
			
			if(cluster.getComponents().size() > 0) {
	
				MSFeatureInfoBundle idBundle = cluster.getMSFeatureInfoBundleForPrimaryId();
				
				if(idBundle != null) {
					
					msTwoFeatureTable.selectBundle(idBundle);
					identificationsTable.selectIdentity(cluster.getPrimaryIdentity());
				}
				else
					msTwoFeatureTable.getTable().setRowSelectionInterval(0, 0);
			}			
			if(cluster instanceof BinnerBasedMsFeatureInfoBundleCluster)
				binnerAnnotationDetailsPanel.setTableModelFromBinnerAnnotationCluster(
						(BinnerBasedMsFeatureInfoBundleCluster)cluster);			
						
			showMultipleFeatureChromatograms(cluster.getComponents());
			showAllClusterSpectra(cluster);
		}
		if (tree.getClickedObject() instanceof MSFeatureInfoBundle) {
			
			IMsFeatureInfoBundleCluster cluster = 
					(IMsFeatureInfoBundleCluster)tree.getClusterForSelectedFeature();
			if(cluster != null) {
				
				if(activeCluster != null && !activeCluster.equals(cluster)) {
					
					activeCluster = cluster;
					safelyLoadMSMSFeatures(cluster.getComponents());
					
					if(cluster.getLookupFeature() != null && cluster.getLookupFeature().getSmiles() != null)
						referenceMolStructurePanel.showStructure(cluster.getLookupFeature().getSmiles());
				}			
				msTwoFeatureTable.selectBundle((MSFeatureInfoBundle)tree.getClickedObject());
			}
		}
	}
	
	private void showAllClusterSpectra(IMsFeatureInfoBundleCluster cluster) {		
		
		if(multipleSpectraDisplayDialog == null 
				|| !multipleSpectraDisplayDialog.isVisible())
			return;
		
		MsReferenceType refType = MsReferenceType.LIBRARY_MATCH;
		if(cluster.getDefiningFeature(MajorClusterFeatureDefiningProperty.REFERENCE_FEATURE) != null)
			refType = MsReferenceType.REFERENCE_FEATURE;
		
		MsDepth msLevel = MsDepth.MS2;
		List<MSFeatureInfoBundle> msmsList = cluster.getComponents().stream().
				filter(c -> Objects.nonNull(c.getMsFeature().getSpectrum())).
				filter(c -> Objects.nonNull(c.getMsFeature().getSpectrum().getExperimentalTandemSpectrum())).
				collect(Collectors.toList());
		if(msmsList.isEmpty())
			msLevel = MsDepth.MS1;
		
		MSReferenceDisplayType displayType = MSReferenceDisplayType.HEAD_TO_TAIL;
		
		multipleSpectraDisplayDialog.showMSFeatureCluster(cluster, msLevel, refType, displayType);
	}
	
	public void clearMSMSClusterData() {
		msmsFeatureClusterTreePanel.resetTree();
		activeMSMSClusterDataSet = null;
		referenceMolStructurePanel.clearPanel();
		lookupFeatureTable.clearPanel();
	}

	@Override
	protected void executeAdminCommand(String command) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void switchDataPipeline(
			DataAnalysisProject experiment, DataPipeline newDataPipeline) {

		super.switchDataPipeline(experiment, newDataPipeline);

		if(experiment == null && newDataPipeline == null)
			clearPanel();		
	}
	
	public void toggleTableListeners(boolean enable) {
		
		if(enable) {
			identificationsTable.getTable().getSelectionModel().
					addListSelectionListener(this);
			msTwoFeatureTable.getTable().getSelectionModel().
					addListSelectionListener(this);			
		} else {
			identificationsTable.getTable().getSelectionModel().
					removeListSelectionListener(this);
			msTwoFeatureTable.getTable().getSelectionModel().
					removeListSelectionListener(this);
		}
	}

	public IMSMSClusterDataSet getActiveMSMSClusterDataSet() {
		return activeMSMSClusterDataSet;
	}

	@Override
	public void updateGuiWithRecentData() {

		msmsFeaturePopupMenu.updateRecentFeatureCollectionList();
		
		if(mzFrequencyAnalysisResultsDialog != null 
				&& mzFrequencyAnalysisResultsDialog.isVisible())
			mzFrequencyAnalysisResultsDialog.updateRecentFeatureCollectionList();
		
		if(idTrackerDataExplorerPlotDialog != null 
				&& idTrackerDataExplorerPlotDialog.isVisible())
			idTrackerDataExplorerPlotDialog.updateRecentFeatureCollectionList();
	}
	
	private void showMultipleSpectraDisplayDialog() {
	
		if(multipleSpectraDisplayDialog != null 
				&& multipleSpectraDisplayDialog.isVisible()) {
			multipleSpectraDisplayDialog.toFront();
			return;
		}
		else {
			multipleSpectraDisplayDialog = new MultipleSpectraDisplayDialog();
			multipleSpectraDisplayDialog.setLocationRelativeTo(this.getContentPane());
			multipleSpectraDisplayDialog.setVisible(true);
		}
	}
}


