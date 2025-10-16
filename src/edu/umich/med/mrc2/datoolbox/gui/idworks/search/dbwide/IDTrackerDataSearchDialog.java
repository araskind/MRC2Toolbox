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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.lang.StringUtils;

import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.intern.DefaultCDockable.Permissions;
import edu.umich.med.mrc2.datoolbox.data.IDTSearchQuery;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationFollowupStep;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MsType;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureSubsetByIdentification;
import edu.umich.med.mrc2.datoolbox.data.enums.IdentifierSearchOptions;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.MsDepth;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicSeparationType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSampleType;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTSearchQueryUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.communication.FormChangeEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FormChangeListener;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.acq.DataAcquisitionParametersPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.id.IdAnnotationSearchParametersPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.lib.LibrarySearchPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.query.IDTrackerSearchQueryManager;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.query.SaveQueryDialog;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide.se.SampleExperimentParametersPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMS1FeatureSearchTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMSMSFeatureSearchTask;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class IDTrackerDataSearchDialog extends JDialog 
		implements ActionListener, BackedByPreferences, FormChangeListener, PersistentLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6686219469167335523L;

	private static final Icon dialogIcon = GuiUtils.getIcon("componentIcon", 32);
	public static final Icon resetIcon = GuiUtils.getIcon("rerun", 24);
	public static final Icon paramsDefinedIcon = GuiUtils.getIcon("multipleIds", 16);
	
	private Preferences preferences;
	public static final String PREFS_NODE = IDTrackerDataSearchDialog.class.getName();
	
	public static final String NULL_STRING = "NULL";
	public static final String RESTRICT_POLARITY = "RESTRICT_POLARITY";
	public static final String POLARITY = "POLARITY";
	public static final String BP_MZ = "BP_MZ";
	public static final String FRAGMENT_MZ_STRING = "FRAGMENT_MZ_STRING";
	public static final String MZ_ERROR = "MZ_ERROR";
	public static final String MZ_ERROR_TYPE = "MZ_ERROR_TYPE";
	public static final String IGNORE_MZ = "IGNORE_MZ";
	public static final String MS_DEPTH = "MS_DEPTH";
	public static final String COLLISION_ENERGY = "COLLISION_ENERGY";
	public static final String START_RT = "START_RT";
	public static final String END_RT = "END_RT";
	
	public static final String FEATURE_SUBSET_BY_IDENTIFICATION = 
			"FEATURE_SUBSET_BY_IDENTIFICATION";
	public static final String ID_OPTION = "ID_OPTION";
	public static final String ID_LOOKUP_STRING = "ID_LOOKUP_STRING";
	public static final String FORMULA_STRING = "FORMULA_STRING";
	public static final String INCHI_KEY_STRING = "INCHI_KEY_STRING";
	public static final String ANNOTATED_ONLY = "ANNOTATED_ONLY";
	public static final String ID_LEVEL_LIST = "ID_LEVEL_LIST";
	public static final String FOLLOWUP_STEP_LIST = "FOLLOWUP_STEP_LIST";
	public static final String SEARCH_ALL_IDS = "SEARCH_ALL_IDS";
	
	public static final String SAMPLE_TYPE_LIST = "SAMPLE_TYPE_LIST";
	public static final String EXPERIMENT_ID_LIST = "EXPERIMENT_ID_LIST";
		
	public static final String SEPARATION_TYPE_LIST = "SEPARATION_TYPE_LIST";
	public static final String CHROMATOGRAPHIC_COLUMN_LIST = "CHROMATOGRAPHIC_COLUMN_LIST";
	public static final String MS_TYPE_LIST = "MS_TYPE_LIST";	
	public static final String ACQUISITION_METHODS_LIST = "ACQUISITION_METHODS_LIST";
	public static final String DATA_ANALYSIS_METHODS_LIST = "DATA_ANALYSIS_METHODS_LIST";
	
	public static final String ORIGINAL_MSMS_LIBRARY_ID = "ORIGINAL_MSMS_LIBRARY_ID";
	public static final String MRC2_MSMS_LIBRARY_ID = "MRC2_MSMS_LIBRARY_ID";
	public static final String MSMS_LIBRARY_LIST = "MSMS_LIBRARY_LIST";
	public static final String SEARCH_ALL_MSMS_LIBRARY_IDS = "SEARCH_ALL_MSMS_LIBRARY_IDS";
	
	public static final String WINDOW_WIDTH = "WINDOW_WIDTH";
	public static final String WINDOW_HEIGTH = "WINDOW_HEIGTH";
	public static final String WINDOW_X = "WINDOW_X";
	public static final String WINDOW_Y = "WINDOW_Y";
	
	private IDWorkbenchPanel parentPanel;
	private IDTrackerDataSearchDialogToolbar toolbar;
	
	private MZRTSearchParametersPanel mzrtSearchParametersPanel;
	private SampleExperimentParametersPanel sampleExperimentParametersPanel;
	private IdAnnotationSearchParametersPanel idAnnotationSearchParametersPanel;
	private DataAcquisitionParametersPanel dataAcquisitionParametersPanel;	
	private LibrarySearchPanel librarySearchParametersPanel;
	private IDTrackerSearchQueryManager idTrackerSearchQueryManager;
	private SaveQueryDialog saveQueryDialog;
	
	private DefaultSingleCDockable 
		mzRtParameterDockablePanel, 
		sampleExperimentParametersDockablePanel,
		idAnnotationSearchParametersDockablePanel,
		dataAcquisitionParametersDockablePanel,
		librarySearchParametersDockablePanel;
	
	private CControl control;
	private CGrid grid;
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "IDTrackerDataSearchDialog.layout");
	
	public IDTrackerDataSearchDialog(IDWorkbenchPanel parentPanel) {
		
		super();
		this.parentPanel = parentPanel;
		setPreferredSize(new Dimension(800, 800));
		int tabWidth = (int) getPreferredSize().getWidth() - 20;
		int tabHeight = (int) (getPreferredSize().getHeight() - 120);
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Search ID tracker data");
		setIconImage(((ImageIcon)dialogIcon).getImage());
		
		toolbar = new IDTrackerDataSearchDialogToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.getController().setTheme(new EclipseTheme());
		this.add( control.getContentArea() );
		grid = new CGrid(control);

		mzrtSearchParametersPanel = new MZRTSearchParametersPanel();
		mzrtSearchParametersPanel.addFormChangeListener(this);
		JPanel mzrtjp = new JPanel(new FlowLayout(FlowLayout.LEFT));
		mzrtjp.add(mzrtSearchParametersPanel);
		mzRtParameterDockablePanel = 
				new DefaultSingleCDockable("DockableMZRTSearchParameters", null, "MZ / RT", 
						mzrtjp, Permissions.MIN_MAX_STACK);
		mzRtParameterDockablePanel.setCloseable(false);
		
		sampleExperimentParametersPanel = new SampleExperimentParametersPanel();
		sampleExperimentParametersPanel.addFormChangeListener(this);
		sampleExperimentParametersDockablePanel = 
				new DefaultSingleCDockable("DockableSampleExperimentParameters", null, "Sample / Experiment", 
					sampleExperimentParametersPanel, Permissions.MIN_MAX_STACK);
		sampleExperimentParametersDockablePanel.setCloseable(false);
		
		idAnnotationSearchParametersPanel = new IdAnnotationSearchParametersPanel();
		idAnnotationSearchParametersPanel.addFormChangeListener(this);
		idAnnotationSearchParametersDockablePanel = 
				new DefaultSingleCDockable("DockableIDAnnotationSearchParameters", null, "ID / Annotations", 
					idAnnotationSearchParametersPanel, Permissions.MIN_MAX_STACK);
		idAnnotationSearchParametersDockablePanel.setCloseable(false);
		
		dataAcquisitionParametersPanel = new DataAcquisitionParametersPanel();
		dataAcquisitionParametersPanel.addFormChangeListener(this);
		dataAcquisitionParametersDockablePanel = 
				new DefaultSingleCDockable("DockableDataAcquisitionParameters", null, "Data acquisition", 
					dataAcquisitionParametersPanel, Permissions.MIN_MAX_STACK);
		dataAcquisitionParametersDockablePanel.setCloseable(false);
		
		librarySearchParametersPanel = new LibrarySearchPanel();
		librarySearchParametersPanel.addFormChangeListener(this);
		librarySearchParametersDockablePanel = 
				new DefaultSingleCDockable("DockableLibrarySearchParameters", null, "MSMS library", 
						librarySearchParametersPanel, Permissions.MIN_MAX_STACK);
		librarySearchParametersDockablePanel.setCloseable(false);

		grid.add(0, 0, 1, 1,
				mzRtParameterDockablePanel, 
				sampleExperimentParametersDockablePanel,
				idAnnotationSearchParametersDockablePanel,
				dataAcquisitionParametersDockablePanel,
				librarySearchParametersDockablePanel);

		control.getContentArea().deploy(grid);
		grid.select(0, 0, 1, 1, mzRtParameterDockablePanel);
				
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JButton btnCancel = new JButton("Cancel");
		panel_1.add(btnCancel);
		btnCancel.addActionListener(al);
		btnCancel.addActionListener(al);

		JButton resetButton = new JButton("Reset form");
		resetButton.addActionListener(this);
		resetButton.setActionCommand(MainActionCommands.IDTRACKER_RESET_FORM_COMMAND.getName());
		panel_1.add(resetButton);

		JButton searchButton = new JButton("Search ...");
		searchButton.addActionListener(this);
		searchButton.setActionCommand(MainActionCommands.IDTRACKER_FEATURE_SEARCH_COMMAND.getName());
		panel_1.add(searchButton);
		
		JRootPane rootPane = SwingUtilities.getRootPane(searchButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(searchButton);
		
		populateSelectorsFromDatabase();
		loadPreferences();
		loadLayout(layoutConfigFile);
		markPopulatedTabs();
		pack();
	}
	
	@Override
	public void formDataChanged(FormChangeEvent e) {
		markPopulatedTabs();
	}
	
	private void markPopulatedTabs() {
		
		if(mzrtSearchParametersPanel.hasSpecifiedConstraints())
			mzRtParameterDockablePanel.setTitleIcon(paramsDefinedIcon);
		else
			mzRtParameterDockablePanel.setTitleIcon(null);
		
		if(sampleExperimentParametersPanel.hasSpecifiedConstraints())
			sampleExperimentParametersDockablePanel.setTitleIcon(paramsDefinedIcon);
		else
			sampleExperimentParametersDockablePanel.setTitleIcon(null);
		
		if(idAnnotationSearchParametersPanel.hasSpecifiedConstraints())
			idAnnotationSearchParametersDockablePanel.setTitleIcon(paramsDefinedIcon);
		else
			idAnnotationSearchParametersDockablePanel.setTitleIcon(null);
		
		if(dataAcquisitionParametersPanel.hasSpecifiedConstraints())
			dataAcquisitionParametersDockablePanel.setTitleIcon(paramsDefinedIcon);
		else
			dataAcquisitionParametersDockablePanel.setTitleIcon(null);
		
		if(librarySearchParametersPanel.hasSpecifiedConstraints())
			librarySearchParametersDockablePanel.setTitleIcon(paramsDefinedIcon);
		else
			librarySearchParametersDockablePanel.setTitleIcon(null);	
	}
	
	@Override
	public void dispose() {
		
		savePreferences();
		saveLayout(layoutConfigFile);
		super.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg(
					"You are not logged in ID tracker!", 
					this.getContentPane());
			return;
		}
		String command = e.getActionCommand();

		if(command.equals(MainActionCommands.IDTRACKER_RESET_FORM_COMMAND.getName()))
			resetForm();

		if(command.equals(MainActionCommands.IDTRACKER_REFRESH_FORM_OPTIONS_COMMAND.getName()))
			populateSelectorsFromDatabase();

		if(command.equals(MainActionCommands.IDTRACKER_FEATURE_SEARCH_COMMAND.getName()))
			searchIdTracker();
		
		if(command.equals(MainActionCommands.SHOW_IDTRACKER_SAVE_QUERY_DIALOG_COMMAND.getName()))
			showSaveQueryDialog();					
		
		if(command.equals(MainActionCommands.SHOW_IDTRACKER_SAVED_QUERIES_LIST_COMMAND.getName()))
			showQueryManager();
		
		if(command.equals(MainActionCommands.LOAD_IDTRACKER_SAVED_QUERY_COMMAND.getName()))
			loadSelectedQuery();
		
		if(command.equals(MainActionCommands.IDTRACKER_SAVE_QUERY_COMMAND.getName()))
			saveCurrentQuery();
		
		//	savePreferences();
	}
	
	private void saveCurrentQuery() {

		if(saveQueryDialog.getQueryDescription().isEmpty()) {
			MessageDialog.showErrorMsg("Please provide query description", this);
			return;
		}
		LIMSUser user = MRC2ToolBoxCore.getIdTrackerUser();
		IDTSearchQuery newQuery = new IDTSearchQuery(
				null,
				saveQueryDialog.getQueryDescription(),
				user,
				new Date(),
				getQueryParameterString());
		try {
			IDTSearchQueryUtils.insertNewQuery(newQuery);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		saveQueryDialog.dispose();
	}

	private void loadSelectedQuery() {

		IDTSearchQuery query = idTrackerSearchQueryManager.getSelectedQuery();
		if(query == null) {
			MessageDialog.showWarningMsg("No query selected!", this);
			return;
		}
		idTrackerSearchQueryManager.dispose();
		loadQueryParameters(query);
	}

	private void showSaveQueryDialog() {
		
		Collection<String>errors = validateInput();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}
		saveQueryDialog = new SaveQueryDialog(this);
		saveQueryDialog.setLocationRelativeTo(this);
		saveQueryDialog.setVisible(true);
	}

	private void showQueryManager() {
		
		idTrackerSearchQueryManager = new IDTrackerSearchQueryManager(this);
		idTrackerSearchQueryManager.setLocationRelativeTo(this);
		idTrackerSearchQueryManager.setVisible(true);
	}

	private Collection<String>validateInput(){
		
		Collection<String>errors = new ArrayList<String>();
		boolean ignoreMzRtValidation = false;
		if(idAnnotationSearchParametersPanel.hasSpecifiedConstraints() || 
				librarySearchParametersPanel.hasSpecifiedConstraints())
			ignoreMzRtValidation = true;
		
		if(!ignoreMzRtValidation)		
			errors.addAll(mzrtSearchParametersPanel.validateInput());
		
		errors.addAll(idAnnotationSearchParametersPanel.validateInput());
		errors.addAll(sampleExperimentParametersPanel.validateInput());
		errors.addAll(dataAcquisitionParametersPanel.validateInput());
		errors.addAll(librarySearchParametersPanel.validateInput());
			
		return errors;
	}

	private void searchIdTracker() {

		Collection<String>errors = validateInput();
		if(!errors.isEmpty()) {			
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}
		if(mzrtSearchParametersPanel.getMsDepth().equals(MsDepth.MS1)) {
			//	MS1 search
			IDTMS1FeatureSearchTask task = new IDTMS1FeatureSearchTask(
					mzrtSearchParametersPanel.getPolarity(), 
					mzrtSearchParametersPanel.getPrecursorMz(), 
					mzrtSearchParametersPanel.getMassErrorValue(), 
					mzrtSearchParametersPanel.getMassErrorType(), 
					mzrtSearchParametersPanel.ignoreMz(), 
					mzrtSearchParametersPanel.getRtRange(),
					idAnnotationSearchParametersPanel.getFeatureSubsetByIdentification(), 
					idAnnotationSearchParametersPanel.getNameIdString(), 
					idAnnotationSearchParametersPanel.getIdentifierSearchOption(),
					idAnnotationSearchParametersPanel.getFormula(), 
					idAnnotationSearchParametersPanel.getInChIKey(), 
					idAnnotationSearchParametersPanel.getAnnotatedOnly(), 
					idAnnotationSearchParametersPanel.searchAllIds(),
					idAnnotationSearchParametersPanel.getSelectedIdLevels(),
					idAnnotationSearchParametersPanel.getSelectedFollowupSteps(), 
					sampleExperimentParametersPanel.getSelectedSampleTypes(),
					sampleExperimentParametersPanel.getSelectedExperiments(), 
					dataAcquisitionParametersPanel.getSelectedChromatographicSeparationTypes(),
					dataAcquisitionParametersPanel.getSelectedChromatographicColumns(),
					dataAcquisitionParametersPanel.getSelectedAcquisitionMethods(),
					dataAcquisitionParametersPanel.getSelectedDataExtractionMethods());
			
			task.addTaskListener(parentPanel);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
		if(mzrtSearchParametersPanel.getMsDepth().equals(MsDepth.MS2)) {
			
			IDTMSMSFeatureSearchTask task = new IDTMSMSFeatureSearchTask(
					mzrtSearchParametersPanel.getPolarity(), 
					mzrtSearchParametersPanel.getPrecursorMz(), 
					mzrtSearchParametersPanel.getFragmentList(),
					mzrtSearchParametersPanel.getMassErrorValue(), 
					mzrtSearchParametersPanel.getMassErrorType(), 
					mzrtSearchParametersPanel.ignoreMz(), 
					mzrtSearchParametersPanel.getCollisionEnergy(), 
					mzrtSearchParametersPanel.getRtRange(),
					idAnnotationSearchParametersPanel.getFeatureSubsetByIdentification(), 
					idAnnotationSearchParametersPanel.getNameIdString(), 
					idAnnotationSearchParametersPanel.getIdentifierSearchOption(),
					idAnnotationSearchParametersPanel.getFormula(), 
					idAnnotationSearchParametersPanel.getInChIKey(), 
					idAnnotationSearchParametersPanel.getAnnotatedOnly(), 
					idAnnotationSearchParametersPanel.searchAllIds(),
					idAnnotationSearchParametersPanel.getSelectedIdLevels(),
					idAnnotationSearchParametersPanel.getSelectedFollowupSteps(), 
					sampleExperimentParametersPanel.getSelectedSampleTypes(),
					sampleExperimentParametersPanel.getSelectedExperiments(), 
					dataAcquisitionParametersPanel.getSelectedChromatographicSeparationTypes(),
					dataAcquisitionParametersPanel.getSelectedChromatographicColumns(), 
					dataAcquisitionParametersPanel.getSelectedMsTypes(),
					dataAcquisitionParametersPanel.getSelectedAcquisitionMethods(),
					dataAcquisitionParametersPanel.getSelectedDataExtractionMethods(), 
					librarySearchParametersPanel.getOriginalLibraryId(), 
					librarySearchParametersPanel.getMRC2LibraryId(), 
					librarySearchParametersPanel.searchAllLibraryMatches(),
					librarySearchParametersPanel.getSelectedLibraries());

			task.addTaskListener(parentPanel);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
		if(mzrtSearchParametersPanel.getMsDepth().equals(MsDepth.All)) {
			
			//	MS1 search
			IDTMS1FeatureSearchTask msOneTask = new IDTMS1FeatureSearchTask(
					mzrtSearchParametersPanel.getPolarity(), 
					mzrtSearchParametersPanel.getPrecursorMz(), 
					mzrtSearchParametersPanel.getMassErrorValue(), 
					mzrtSearchParametersPanel.getMassErrorType(), 
					mzrtSearchParametersPanel.ignoreMz(), 
					mzrtSearchParametersPanel.getRtRange(),
					idAnnotationSearchParametersPanel.getFeatureSubsetByIdentification(), 
					idAnnotationSearchParametersPanel.getNameIdString(), 
					idAnnotationSearchParametersPanel.getIdentifierSearchOption(),
					idAnnotationSearchParametersPanel.getFormula(), 
					idAnnotationSearchParametersPanel.getInChIKey(), 
					idAnnotationSearchParametersPanel.getAnnotatedOnly(), 
					idAnnotationSearchParametersPanel.searchAllIds(),
					idAnnotationSearchParametersPanel.getSelectedIdLevels(),
					idAnnotationSearchParametersPanel.getSelectedFollowupSteps(), 
					sampleExperimentParametersPanel.getSelectedSampleTypes(),
					sampleExperimentParametersPanel.getSelectedExperiments(), 
					dataAcquisitionParametersPanel.getSelectedChromatographicSeparationTypes(),
					dataAcquisitionParametersPanel.getSelectedChromatographicColumns(),
					dataAcquisitionParametersPanel.getSelectedAcquisitionMethods(),
					dataAcquisitionParametersPanel.getSelectedDataExtractionMethods());
			
			msOneTask.addTaskListener(parentPanel);
			MRC2ToolBoxCore.getTaskController().addTask(msOneTask);
			
			//	MSMS
			IDTMSMSFeatureSearchTask task = new IDTMSMSFeatureSearchTask(
					mzrtSearchParametersPanel.getPolarity(), 
					mzrtSearchParametersPanel.getPrecursorMz(), 
					mzrtSearchParametersPanel.getFragmentList(),
					mzrtSearchParametersPanel.getMassErrorValue(), 
					mzrtSearchParametersPanel.getMassErrorType(), 
					mzrtSearchParametersPanel.ignoreMz(), 
					mzrtSearchParametersPanel.getCollisionEnergy(), 
					mzrtSearchParametersPanel.getRtRange(),
					idAnnotationSearchParametersPanel.getFeatureSubsetByIdentification(), 
					idAnnotationSearchParametersPanel.getNameIdString(), 
					idAnnotationSearchParametersPanel.getIdentifierSearchOption(),
					idAnnotationSearchParametersPanel.getFormula(), 
					idAnnotationSearchParametersPanel.getInChIKey(), 
					idAnnotationSearchParametersPanel.getAnnotatedOnly(), 
					idAnnotationSearchParametersPanel.searchAllIds(),
					idAnnotationSearchParametersPanel.getSelectedIdLevels(),
					idAnnotationSearchParametersPanel.getSelectedFollowupSteps(), 
					sampleExperimentParametersPanel.getSelectedSampleTypes(),
					sampleExperimentParametersPanel.getSelectedExperiments(), 
					dataAcquisitionParametersPanel.getSelectedChromatographicSeparationTypes(),
					dataAcquisitionParametersPanel.getSelectedChromatographicColumns(),
					dataAcquisitionParametersPanel.getSelectedMsTypes(),
					dataAcquisitionParametersPanel.getSelectedAcquisitionMethods(),
					dataAcquisitionParametersPanel.getSelectedDataExtractionMethods(), 
					librarySearchParametersPanel.getOriginalLibraryId(), 
					librarySearchParametersPanel.getMRC2LibraryId(), 
					librarySearchParametersPanel.searchAllLibraryMatches(),
					librarySearchParametersPanel.getSelectedLibraries());
			
			task.addTaskListener(parentPanel);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
		this.dispose();
	}

	private void populateSelectorsFromDatabase() {
		
		mzrtSearchParametersPanel.populateCollisionEnergySelectorFromDatabase();
		sampleExperimentParametersPanel.populateTablesFromDatabase();
		dataAcquisitionParametersPanel.populateTablesFromDatabase();
	}

	private void resetForm() {
		
		mzrtSearchParametersPanel.resetPanel(preferences);
		idAnnotationSearchParametersPanel.resetPanel(null);
		sampleExperimentParametersPanel.resetPanel(null);
		dataAcquisitionParametersPanel.resetPanel(null);
		librarySearchParametersPanel.resetPanel(null);
	}
	
	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}
	
	@Override
	public void loadPreferences(Preferences preferences) {
		
		if (!ConnectionManager.connectionDefined()) {
			MainWindow.displayErrorMessage("Connection error", 
					"Database connection not defined!");
			return;
		}	
		this.preferences = preferences;
		
		//	M/Z RT search parameters
		String polarityCode = preferences.get(POLARITY, Polarity.Positive.getCode());
		Polarity polarity = null;
		if(!polarityCode.equals(NULL_STRING))			
			polarity = Polarity.getPolarityByCode(polarityCode);
		
		mzrtSearchParametersPanel.setPolarity(polarity);
		mzrtSearchParametersPanel.setPrecursorMz(preferences.getDouble(BP_MZ, 0.0d));
		mzrtSearchParametersPanel.setMassErrorValue(
				preferences.getDouble(MZ_ERROR, 20.0d));

		MassErrorType met = MassErrorType.getTypeByName(preferences.get(MZ_ERROR_TYPE, 
					MassErrorType.ppm.name()));
		mzrtSearchParametersPanel.setMassErrorType(met);
		mzrtSearchParametersPanel.setRtRange(
				preferences.getDouble(START_RT, 0.0d), 
				preferences.getDouble(END_RT, 0.0d));

		mzrtSearchParametersPanel.setIgnoreMz(preferences.getBoolean(IGNORE_MZ, true));

		MsDepth msd = MsDepth.getMsDepthByName(
				preferences.get(MS_DEPTH, MsDepth.All.name()));
		mzrtSearchParametersPanel.setMsDepth(msd);
		
		mzrtSearchParametersPanel.setFragmentListString(
				preferences.get(FRAGMENT_MZ_STRING, ""));
		
		mzrtSearchParametersPanel.setCollisionEnergy(
				preferences.getDouble(COLLISION_ENERGY, 0.0d));
		
		//	Identifications and annotations		
		FeatureSubsetByIdentification idSubset = 
				FeatureSubsetByIdentification.getOptionByName(
						preferences.get(FEATURE_SUBSET_BY_IDENTIFICATION, 
								FeatureSubsetByIdentification.ALL.name()));
		
		idAnnotationSearchParametersPanel.setFeatureSubsetByIdentification(idSubset);
		
		IdentifierSearchOptions idOpt = 
				IdentifierSearchOptions.getOptionByName(preferences.get(ID_OPTION, ""));
		idAnnotationSearchParametersPanel.setIdentifierSearchOption(idOpt);
		
		idAnnotationSearchParametersPanel.setNameIdString(
				preferences.get(ID_LOOKUP_STRING, ""));
		idAnnotationSearchParametersPanel.setFormula(
				preferences.get(FORMULA_STRING, ""));
		idAnnotationSearchParametersPanel.setInChIKey(
				preferences.get(INCHI_KEY_STRING, ""));
		idAnnotationSearchParametersPanel.setAnnotatedOnly(
				preferences.getBoolean(ANNOTATED_ONLY, false));
		idAnnotationSearchParametersPanel.setSearchAllIds(
				preferences.getBoolean(SEARCH_ALL_IDS, false));
				
		Collection<MSFeatureIdentificationLevel>levelsToSelect = 
				new ArrayList<MSFeatureIdentificationLevel>();		
		String[] levelUids = 
				StringUtils.split(preferences.get(ID_LEVEL_LIST, ""), ';');
		if(levelUids.length > 0) {
			
			for(String id : levelUids) {
				MSFeatureIdentificationLevel level = 
						IDTDataCache.getMSFeatureIdentificationLevelById(id);
				if(level != null)
					levelsToSelect.add(level);
			}
			idAnnotationSearchParametersPanel.setSelectedIdLevels(levelsToSelect);
		}	
		Collection<MSFeatureIdentificationFollowupStep>followupStepsToSelect = 
				new ArrayList<MSFeatureIdentificationFollowupStep>();		
		String[] stepsUids = 
				StringUtils.split(preferences.get(FOLLOWUP_STEP_LIST, ""), ';');
		if(stepsUids.length > 0) {
			
			for(String id : stepsUids) {
				MSFeatureIdentificationFollowupStep step = 
						IDTDataCache.getMSFeatureIdentificationFollowupStepById(id);
				if(step != null)
					followupStepsToSelect.add(step);
			}
			idAnnotationSearchParametersPanel.setSelectedFollowupSteps(followupStepsToSelect);
		}
		//	Sample type and experiment
		Collection<LIMSSampleType> availableSampleTypes = new ArrayList<LIMSSampleType>();
		try {
			availableSampleTypes = IDTUtils.getAvailableSampleTypes();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		Collection<LIMSSampleType>sampleTypesToSelect = new ArrayList<LIMSSampleType>();		
		String[] stypesUids = 
				StringUtils.split(preferences.get(SAMPLE_TYPE_LIST, ""), ';');
		if(stypesUids.length > 0) {
			
			for(String id : stypesUids) {
				LIMSSampleType type = availableSampleTypes.stream().
						filter(t -> t.getId().equals(id)).
						findFirst().orElse(null);
				if(type != null)
					sampleTypesToSelect.add(type);
			}
			sampleExperimentParametersPanel.setSelectedSampleTypes(sampleTypesToSelect);
		}
		Collection<LIMSExperiment>experimentsToSelect = new ArrayList<LIMSExperiment>();		
		String[] experimentsUids = 
				StringUtils.split(preferences.get(EXPERIMENT_ID_LIST, ""), ';');
		if(experimentsUids.length > 0) {
			
			for(String id : experimentsUids) {
				
				LIMSExperiment experiment = IDTDataCache.getExperimentById(id);
				if(experiment != null)
					experimentsToSelect.add(experiment);
			}
			sampleExperimentParametersPanel.setSelectedExperiments(experimentsToSelect);
		}
		
		//	Acquisition parameters
		Collection<ChromatographicSeparationType>cstToSelect = 
				new ArrayList<ChromatographicSeparationType>();		
		String[] cstUids = 
				StringUtils.split(preferences.get(SEPARATION_TYPE_LIST, ""), ';');
		if(cstUids.length > 0) {
			
			for(String id : cstUids) {
				
				ChromatographicSeparationType cst = 
						IDTDataCache.getChromatographicSeparationTypeById(id);
				if(cst != null)
					cstToSelect.add(cst);
			}
			dataAcquisitionParametersPanel.setSelectedChromatographicSeparationTypes(cstToSelect);
		}
		Collection<LIMSChromatographicColumn>columnsToSelect = 
				new ArrayList<LIMSChromatographicColumn>();		
		String[] ccolumnUids = 
				StringUtils.split(preferences.get(CHROMATOGRAPHIC_COLUMN_LIST, ""), ';');
		if(ccolumnUids.length > 0) {
			
			for(String id : ccolumnUids) {
				
				LIMSChromatographicColumn column = IDTDataCache.getColumnById(id);
				if(column != null)
					columnsToSelect.add(column);
			}
			dataAcquisitionParametersPanel.setSelectedChromatographicColumns(columnsToSelect);
		}
		Collection<MsType>msTypesToSelect = new ArrayList<MsType>();		
		String[] mstUids = 
				StringUtils.split(preferences.get(MS_TYPE_LIST, ""), ';');
		if(mstUids.length > 0) {
			
			for(String id : mstUids) {
				
				MsType mstype = IDTDataCache.getMsTypeById(id);
				if(mstype != null)
					msTypesToSelect.add(mstype);
			}
			dataAcquisitionParametersPanel.setSelectedMsTypes(msTypesToSelect);
		}
		Collection<DataAcquisitionMethod>daqMethodsToSelect = 
				new ArrayList<DataAcquisitionMethod>();		
		String[]daqMethodsUids = 
				StringUtils.split(preferences.get(ACQUISITION_METHODS_LIST, ""), ';');
		if(daqMethodsUids.length > 0) {
			
			for(String id : daqMethodsUids) {
				
				DataAcquisitionMethod aqMethod = 
						IDTDataCache.getAcquisitionMethodById(id);
				if(aqMethod != null)
					daqMethodsToSelect.add(aqMethod);
			}
			dataAcquisitionParametersPanel.setSelectedDataAcquisitionMethods(daqMethodsToSelect);
		}		
		Collection<DataExtractionMethod>dataAnalysisMethodsToSelect = 
				new ArrayList<DataExtractionMethod>();		
		String[] dataAnalysisUids = 
				StringUtils.split(preferences.get(DATA_ANALYSIS_METHODS_LIST, ""), ';');
		if(dataAnalysisUids.length > 0) {
			
			for(String id : dataAnalysisUids) {
				
				DataExtractionMethod dataAnalysisMethod = 
						IDTDataCache.getDataExtractionMethodById(id);
				if(dataAnalysisMethod != null)
					dataAnalysisMethodsToSelect.add(dataAnalysisMethod);
			}
			dataAcquisitionParametersPanel.setSelectedDataExtractionMethods(dataAnalysisMethodsToSelect);
		}
		
		//	Library search 
		librarySearchParametersPanel.setOriginalLibraryId(preferences.get(ORIGINAL_MSMS_LIBRARY_ID, ""));
		librarySearchParametersPanel.setMRC2LibraryId(preferences.get(MRC2_MSMS_LIBRARY_ID, ""));
		Collection<ReferenceMsMsLibrary>msmsLibsToSelect = new ArrayList<ReferenceMsMsLibrary>();		
		String[] msmsLibsUids = 
				StringUtils.split(preferences.get(MSMS_LIBRARY_LIST, ""), ';');
		if(msmsLibsUids.length > 0) {
			
			for(String id : msmsLibsUids) {
				
				ReferenceMsMsLibrary msmsLib = IDTDataCache.getReferenceMsMsLibraryById(id);
				if(msmsLib != null)
					msmsLibsToSelect.add(msmsLib);
			}
			librarySearchParametersPanel.selectLibraries(msmsLibsToSelect);
		}
		librarySearchParametersPanel.setSearchAllLibraryMatches(preferences.getBoolean(SEARCH_ALL_MSMS_LIBRARY_IDS, false));
		
		int width = preferences.getInt(WINDOW_WIDTH, 1000);
		int heigh = preferences.getInt(WINDOW_HEIGTH, 800);
		setSize(new Dimension(width, heigh));
		setPreferredSize(new Dimension(width, heigh));		
		int x = preferences.getInt(WINDOW_X, 100);
		int y = preferences.getInt(WINDOW_Y, 100);		
		setLocation(x,y);
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userRoot().node(PREFS_NODE);
		
		//	M/Z RT search parameters
		String polarityCode = NULL_STRING;
		if(mzrtSearchParametersPanel.getPolarity() != null)
			polarityCode = mzrtSearchParametersPanel.getPolarity().getCode();
			
		preferences.put(POLARITY, polarityCode);
		
		double bpmz = 0.0d;
		if(mzrtSearchParametersPanel.getPrecursorMz() != null)
			bpmz = mzrtSearchParametersPanel.getPrecursorMz();
		preferences.putDouble(BP_MZ, bpmz);
		preferences.put(FRAGMENT_MZ_STRING, mzrtSearchParametersPanel.getFragmentListString());

		double mzError = 0.0d;
		if(mzrtSearchParametersPanel.getMassErrorValue() != null)
			mzError = mzrtSearchParametersPanel.getMassErrorValue();
		preferences.putDouble(MZ_ERROR, mzError);
		
		//	mzrtSearchParametersPanel.getMassErrorType()
		preferences.put(MZ_ERROR_TYPE, mzrtSearchParametersPanel.getMassErrorType().name());
		preferences.putBoolean(IGNORE_MZ, mzrtSearchParametersPanel.ignoreMz());
		preferences.put(MS_DEPTH, mzrtSearchParametersPanel.getMsDepth().name());

		double ce = 0.0d;
		if(mzrtSearchParametersPanel.getCollisionEnergy() != null)
			ce = mzrtSearchParametersPanel.getCollisionEnergy();
		preferences.putDouble(COLLISION_ENERGY, ce);
		
		double startTime = 0.0d;
		double endTime = 0.0d;		
		Range rtRange =  mzrtSearchParametersPanel.getRtRange();
		if(rtRange != null) {
			startTime = rtRange.getMin();
			endTime = rtRange.getMax();
		}
		preferences.putDouble(START_RT, startTime);
		preferences.putDouble(END_RT, endTime);
				
		//	Identifications and annotations	
		String idFilter = FeatureSubsetByIdentification.ALL.name();
		FeatureSubsetByIdentification selectedIdFilter = 
				idAnnotationSearchParametersPanel.getFeatureSubsetByIdentification();
		if(selectedIdFilter != null)
			idFilter = selectedIdFilter.name();
		
		preferences.put(FEATURE_SUBSET_BY_IDENTIFICATION, idFilter);	

		String idOtionString = "";
		IdentifierSearchOptions idOtion = 
				idAnnotationSearchParametersPanel.getIdentifierSearchOption();
		if(idOtion != null)
			idOtionString = idOtion.name();
		
		preferences.put(ID_OPTION, idOtionString);		
		preferences.put(ID_LOOKUP_STRING, idAnnotationSearchParametersPanel.getNameIdString());		
		preferences.put(FORMULA_STRING, idAnnotationSearchParametersPanel.getFormula());
		preferences.put(INCHI_KEY_STRING, idAnnotationSearchParametersPanel.getInChIKey());
		preferences.putBoolean(ANNOTATED_ONLY, idAnnotationSearchParametersPanel.getAnnotatedOnly());
		preferences.putBoolean(SEARCH_ALL_IDS, idAnnotationSearchParametersPanel.searchAllIds());

		String idLevelsString = "";
		Collection<MSFeatureIdentificationLevel>idLevels = 
					idAnnotationSearchParametersPanel.getSelectedIdLevels();
		if(!idLevels.isEmpty()) {
			Set<String> idSet =  
					idLevels.stream().map(t -> t.getId()).collect(Collectors.toSet());
			idLevelsString = StringUtils.join(idSet, ';');
		}
		preferences.put(ID_LEVEL_LIST, idLevelsString);	
		
		String followupStepsString = "";
		Collection<MSFeatureIdentificationFollowupStep>fuSteps = 
					idAnnotationSearchParametersPanel.getSelectedFollowupSteps();
		if(!fuSteps.isEmpty()) {
			Set<String> idSet =  
					fuSteps.stream().map(t -> t.getId()).collect(Collectors.toSet());
			followupStepsString = StringUtils.join(idSet, ';');
		}
		preferences.put(FOLLOWUP_STEP_LIST, followupStepsString);	
		
		//	Sample type and experiment
		String sampleTypesString = "";
		Collection<LIMSSampleType>sampleTypes = 
					sampleExperimentParametersPanel.getSelectedSampleTypes();
		if(!sampleTypes.isEmpty()) {
			Set<String> idSet =  
					sampleTypes.stream().map(t -> t.getId()).collect(Collectors.toSet());
			sampleTypesString = StringUtils.join(idSet, ';');
		}
		preferences.put(SAMPLE_TYPE_LIST, sampleTypesString);

		String experimentIdString = "";
		Collection<LIMSExperiment>experiments = 
					sampleExperimentParametersPanel.getSelectedExperiments();
		if(!experiments.isEmpty()) {
			Set<String> idSet =  
					experiments.stream().map(t -> t.getId()).collect(Collectors.toSet());
			experimentIdString = StringUtils.join(idSet, ';');
		}
		preferences.put(EXPERIMENT_ID_LIST, experimentIdString);
		
		//	Acquisition parameters		
		String csTypesString = "";
		Collection<ChromatographicSeparationType>csTypes = 
					dataAcquisitionParametersPanel.getSelectedChromatographicSeparationTypes();
		if(!csTypes.isEmpty()) {
			Set<String> idSet =  
					csTypes.stream().map(t -> t.getId()).collect(Collectors.toSet());
			csTypesString = StringUtils.join(idSet, ';');
		}
		preferences.put(SEPARATION_TYPE_LIST, csTypesString);
		
		String columnIdString = "";
		Collection<LIMSChromatographicColumn>columns = 
					dataAcquisitionParametersPanel.getSelectedChromatographicColumns();
		if(!columns.isEmpty()) {
			Set<String> idSet =  
					columns.stream().map(t -> t.getColumnId()).collect(Collectors.toSet());
			columnIdString = StringUtils.join(idSet, ';');
		}
		preferences.put(CHROMATOGRAPHIC_COLUMN_LIST, columnIdString);
		
		String acqMethodIdString = "";
		Collection<DataAcquisitionMethod>acqMethods = 
					dataAcquisitionParametersPanel.getSelectedAcquisitionMethods();
		if(!acqMethods.isEmpty()) {
			Set<String> idSet =  
					acqMethods.stream().map(t -> t.getId()).collect(Collectors.toSet());
			acqMethodIdString = StringUtils.join(idSet, ';');
		}
		preferences.put(ACQUISITION_METHODS_LIST, acqMethodIdString);
		
		String dataAnalysisMethodIdString = "";
		Collection<DataExtractionMethod>dataAnalysisMethods = 
					dataAcquisitionParametersPanel.getSelectedDataExtractionMethods();
		if(!dataAnalysisMethods.isEmpty()) {
			Set<String> idSet =  
					dataAnalysisMethods.stream().map(t -> t.getId()).collect(Collectors.toSet());
			dataAnalysisMethodIdString = StringUtils.join(idSet, ';');
		}
		preferences.put(DATA_ANALYSIS_METHODS_LIST, dataAnalysisMethodIdString);
		
		String msTypeString = "";
		Collection<MsType>msTypes = 
					dataAcquisitionParametersPanel.getSelectedMsTypes();
		if(!msTypes.isEmpty()) {
			Set<String> idSet =  
					msTypes.stream().map(t -> t.getId()).collect(Collectors.toSet());
			msTypeString = StringUtils.join(idSet, ';');
		}
		preferences.put(MS_TYPE_LIST, msTypeString);
		
		//	Library search 
		preferences.put(ORIGINAL_MSMS_LIBRARY_ID, librarySearchParametersPanel.getOriginalLibraryId());
		preferences.put(MRC2_MSMS_LIBRARY_ID, librarySearchParametersPanel.getMRC2LibraryId());
		
		String msmsLibString = "";
		Collection<ReferenceMsMsLibrary>selectedMsmsLibs = librarySearchParametersPanel.getSelectedLibraries();
		if(!selectedMsmsLibs.isEmpty()) {
			Set<String> idSet =  
					selectedMsmsLibs.stream().map(t -> t.getUniqueId()).collect(Collectors.toSet());
			msmsLibString = StringUtils.join(idSet, ';');
		}
		preferences.put(MSMS_LIBRARY_LIST, msmsLibString);
		preferences.putBoolean(SEARCH_ALL_MSMS_LIBRARY_IDS, librarySearchParametersPanel.searchAllLibraryMatches());
		
		preferences.putInt(WINDOW_WIDTH, getWidth());
		preferences.putInt(WINDOW_HEIGTH, getHeight());	
		Point location = getLocation();
		preferences.putInt(WINDOW_X, location.x);
		preferences.putInt(WINDOW_Y, location.y);
	}
	
	public String getQueryParameterString() {
		
		ArrayList<String>params = new ArrayList<String>();
		String polarityCode = NULL_STRING;
		
		if(mzrtSearchParametersPanel.getPolarity() != null)
			polarityCode = mzrtSearchParametersPanel.getPolarity().getCode();
		
		params.add(POLARITY + "|" + polarityCode);
		
		//	M/Z RT search parameters
		String bpmz = "";
		if(mzrtSearchParametersPanel.getPrecursorMz() != null)
			bpmz = Double.toString(mzrtSearchParametersPanel.getPrecursorMz());
		
		params.add(BP_MZ + "|" + bpmz);
		params.add(FRAGMENT_MZ_STRING + "|" + mzrtSearchParametersPanel.getFragmentListString());

		String mzError = "";
		if(mzrtSearchParametersPanel.getMassErrorValue() != null)
			mzError = Double.toString(mzrtSearchParametersPanel.getMassErrorValue());
		
		params.add(MZ_ERROR + "|" + mzError);
		params.add(MZ_ERROR_TYPE + "|" + mzrtSearchParametersPanel.getMassErrorType().name());
		params.add(IGNORE_MZ + "|" + Boolean.toString(mzrtSearchParametersPanel.ignoreMz()));
		params.add(MS_DEPTH + "|" + mzrtSearchParametersPanel.getMsDepth().name());

		String ce = "";
		if(mzrtSearchParametersPanel.getCollisionEnergy() != null)
			ce = Double.toString(mzrtSearchParametersPanel.getCollisionEnergy());
		params.add(COLLISION_ENERGY + "|" + ce);
		
		String startTime = "";
		String endTime = "";		
		Range rtRange =  mzrtSearchParametersPanel.getRtRange();
		if(rtRange != null) {
			startTime = Double.toString(rtRange.getMin());
			endTime = Double.toString(rtRange.getMax());
		}
		params.add(START_RT + "|" + startTime);
		params.add(END_RT + "|" + endTime);
		
		//	Identifications and annotations	
		String idFilter = FeatureSubsetByIdentification.ALL.name();
		FeatureSubsetByIdentification selectedIdFilter = 
				idAnnotationSearchParametersPanel.getFeatureSubsetByIdentification();
		if(selectedIdFilter != null)
			idFilter = selectedIdFilter.name();
		params.add(FEATURE_SUBSET_BY_IDENTIFICATION + "|" + idFilter);
		
		String idOtionString = "";
		IdentifierSearchOptions idOtion = 
				idAnnotationSearchParametersPanel.getIdentifierSearchOption();
		if(idOtion != null)
			idOtionString = idOtion.name();
		
		params.add(ID_OPTION + "|" + idOtionString);
		
		params.add(ID_LOOKUP_STRING + "|" + idAnnotationSearchParametersPanel.getNameIdString());
		params.add(FORMULA_STRING + "|" + idAnnotationSearchParametersPanel.getFormula());
		params.add(INCHI_KEY_STRING + "|" + idAnnotationSearchParametersPanel.getInChIKey());
		params.add(ANNOTATED_ONLY + "|" + Boolean.toString(idAnnotationSearchParametersPanel.getAnnotatedOnly()));
		params.add(SEARCH_ALL_IDS + "|" + Boolean.toString(idAnnotationSearchParametersPanel.searchAllIds()));
		
		String idLevelsString = "";
		Collection<MSFeatureIdentificationLevel>idLevels = 
					idAnnotationSearchParametersPanel.getSelectedIdLevels();
		if(!idLevels.isEmpty()) {
			Set<String> idSet =  
					idLevels.stream().map(t -> t.getId()).collect(Collectors.toSet());
			idLevelsString = StringUtils.join(idSet, ';');
		}
		params.add(ID_LEVEL_LIST + "|" + idLevelsString);	
		
		String followupStepsString = "";
		Collection<MSFeatureIdentificationFollowupStep>fuSteps = 
					idAnnotationSearchParametersPanel.getSelectedFollowupSteps();
		if(!fuSteps.isEmpty()) {
			Set<String> idSet =  
					fuSteps.stream().map(t -> t.getId()).collect(Collectors.toSet());
			followupStepsString = StringUtils.join(idSet, ';');
		}
		params.add(FOLLOWUP_STEP_LIST + "|" + followupStepsString);
	
		//	Sample type and experiment
		String sampleTypesString = "";
		Collection<LIMSSampleType>sampleTypes = 
					sampleExperimentParametersPanel.getSelectedSampleTypes();
		if(!sampleTypes.isEmpty()) {
			Set<String> idSet =  
					sampleTypes.stream().map(t -> t.getId()).collect(Collectors.toSet());
			sampleTypesString = StringUtils.join(idSet, ';');
		}
		params.add(SAMPLE_TYPE_LIST + "|" + sampleTypesString);

		String experimentIdString = "";
		Collection<LIMSExperiment>experiments = 
					sampleExperimentParametersPanel.getSelectedExperiments();
		if(!experiments.isEmpty()) {
			Set<String> idSet =  
					experiments.stream().map(t -> t.getId()).collect(Collectors.toSet());
			experimentIdString = StringUtils.join(idSet, ';');
		}
		params.add(EXPERIMENT_ID_LIST + "|" + experimentIdString);
		
		//	Acquisition parameters		
		String csTypesString = "";
		Collection<ChromatographicSeparationType>csTypes = 
					dataAcquisitionParametersPanel.getSelectedChromatographicSeparationTypes();
		if(!csTypes.isEmpty()) {
			Set<String> idSet =  
					csTypes.stream().map(t -> t.getId()).collect(Collectors.toSet());
			csTypesString = StringUtils.join(idSet, ';');
		}
		params.add(SEPARATION_TYPE_LIST + "|" + csTypesString);
		
		String columnIdString = "";
		Collection<LIMSChromatographicColumn>columns = 
					dataAcquisitionParametersPanel.getSelectedChromatographicColumns();
		if(!columns.isEmpty()) {
			Set<String> idSet =  
					columns.stream().map(t -> t.getColumnId()).collect(Collectors.toSet());
			columnIdString = StringUtils.join(idSet, ';');
		}
		params.add(CHROMATOGRAPHIC_COLUMN_LIST + "|" + columnIdString);
		
		String msTypeString = "";
		Collection<MsType>msTypes = 
					dataAcquisitionParametersPanel.getSelectedMsTypes();
		if(!msTypes.isEmpty()) {
			Set<String> idSet =  
					msTypes.stream().map(t -> t.getId()).collect(Collectors.toSet());
			msTypeString = StringUtils.join(idSet, ';');
		}
		params.add(MS_TYPE_LIST + "|" + msTypeString);
		
		String acqMethodIdString  = "";
		Collection<DataAcquisitionMethod>acqMethods = 
					dataAcquisitionParametersPanel.getSelectedAcquisitionMethods();
		if(!acqMethods.isEmpty()) {
			Set<String> idSet =  
					acqMethods.stream().map(t -> t.getId()).collect(Collectors.toSet());
			acqMethodIdString = StringUtils.join(idSet, ';');
		}
		params.add(ACQUISITION_METHODS_LIST + "|" + acqMethodIdString);

		String dataAnalysisMethodIdString = "";
		Collection<DataExtractionMethod>dataAnalysisMethods = 
					dataAcquisitionParametersPanel.getSelectedDataExtractionMethods();
		if(!dataAnalysisMethods.isEmpty()) {
			Set<String> idSet =  
					dataAnalysisMethods.stream().map(t -> t.getId()).collect(Collectors.toSet());
			dataAnalysisMethodIdString = StringUtils.join(idSet, ';');
		}
		params.add(DATA_ANALYSIS_METHODS_LIST + "|" + dataAnalysisMethodIdString);
		
		//	Library search 
		params.add(ORIGINAL_MSMS_LIBRARY_ID + "|" + librarySearchParametersPanel.getOriginalLibraryId());
		params.add(MRC2_MSMS_LIBRARY_ID + "|" + librarySearchParametersPanel.getMRC2LibraryId());
		
		String msmsLibString = "";
		Collection<ReferenceMsMsLibrary>selectedMsmsLibs = librarySearchParametersPanel.getSelectedLibraries();
		if(!selectedMsmsLibs.isEmpty()) {
			Set<String> idSet =  
					selectedMsmsLibs.stream().map(t -> t.getUniqueId()).collect(Collectors.toSet());
			msmsLibString = StringUtils.join(idSet, ';');
		}
		params.add(MSMS_LIBRARY_LIST + "|" + msmsLibString);
		params.add(SEARCH_ALL_MSMS_LIBRARY_IDS + "|" + librarySearchParametersPanel.searchAllLibraryMatches());
		
		return StringUtils.join(params, "\n");
	}
	
	public void loadQueryParameters(IDTSearchQuery query) {
		
		resetForm();
		String queryParameters = query.getQueryParameters();
		String[] paramLines = queryParameters.split("\\n");
		Map<String,String>paramsMap = new TreeMap<String,String>();
		for(String line : paramLines) {
			String[] l = line.split("\\|");
			
			String value = null;
			if(l.length == 2)
				value = l[1];
			
			paramsMap.put(l[0], value);
		}
		//	M/Z RT search parameters
		if(paramsMap.containsKey(POLARITY))
			mzrtSearchParametersPanel.setPolarity(Polarity.getPolarityByCode(paramsMap.get(POLARITY)));
		
		if(paramsMap.containsKey(BP_MZ)) {	
			Double bpMz = 0.0;
			if(paramsMap.get(BP_MZ) != null && !paramsMap.get(BP_MZ).isEmpty())
				bpMz = Double.valueOf(paramsMap.get(BP_MZ));
			
			mzrtSearchParametersPanel.setPrecursorMz(bpMz);
		}
		if(paramsMap.containsKey(MZ_ERROR)) {
			
			Double mzError = 20.0;
			if(paramsMap.get(MZ_ERROR) != null && !paramsMap.get(MZ_ERROR).isEmpty())
				mzError = Double.valueOf(paramsMap.get(MZ_ERROR));
			
			mzrtSearchParametersPanel.setMassErrorValue(mzError);
		}		
		if(paramsMap.containsKey(MZ_ERROR_TYPE)) 
			mzrtSearchParametersPanel.setMassErrorType(MassErrorType.getTypeByName(paramsMap.get(MZ_ERROR_TYPE)));
		
		if(paramsMap.containsKey(START_RT) && paramsMap.containsKey(END_RT)) {
			
			Double startRt = 0.0;
			if(paramsMap.get(START_RT) != null && !paramsMap.get(START_RT).isEmpty())
				startRt = Double.valueOf(paramsMap.get(START_RT));
			
			Double endRt = 0.0;
			if(paramsMap.get(END_RT) != null && !paramsMap.get(END_RT).isEmpty())
				endRt = Double.valueOf(paramsMap.get(END_RT));
			
			mzrtSearchParametersPanel.setRtRange(startRt,  endRt);
		}
		if(paramsMap.containsKey(IGNORE_MZ))
			mzrtSearchParametersPanel.setIgnoreMz(Boolean.valueOf(paramsMap.get(IGNORE_MZ)));
		
		if(paramsMap.containsKey(MS_DEPTH))
			mzrtSearchParametersPanel.setMsDepth(MsDepth.getMsDepthByName(paramsMap.get(MS_DEPTH)));

		if(paramsMap.containsKey(FRAGMENT_MZ_STRING))
			mzrtSearchParametersPanel.setFragmentListString(paramsMap.get(FRAGMENT_MZ_STRING));
		
		if(paramsMap.containsKey(COLLISION_ENERGY)) {
			Double ce = null;
			if(paramsMap.get(COLLISION_ENERGY) != null && !paramsMap.get(COLLISION_ENERGY).isEmpty())
				ce = Double.valueOf(paramsMap.get(COLLISION_ENERGY));
			
			mzrtSearchParametersPanel.setCollisionEnergy(ce);
		}
		
		//	Identifications and annotations	
		if(paramsMap.containsKey(FEATURE_SUBSET_BY_IDENTIFICATION))
			idAnnotationSearchParametersPanel.setFeatureSubsetByIdentification(
					FeatureSubsetByIdentification.getOptionByName(paramsMap.get(FEATURE_SUBSET_BY_IDENTIFICATION)));
		
		if(paramsMap.containsKey(ID_OPTION))
			idAnnotationSearchParametersPanel.setIdentifierSearchOption(
					IdentifierSearchOptions.getOptionByName(paramsMap.get(ID_OPTION)));

		if(paramsMap.containsKey(ID_LOOKUP_STRING))
			idAnnotationSearchParametersPanel.setNameIdString(paramsMap.get(ID_LOOKUP_STRING));
		
		if(paramsMap.containsKey(FORMULA_STRING))
			idAnnotationSearchParametersPanel.setFormula(paramsMap.get(FORMULA_STRING));
		
		if(paramsMap.containsKey(INCHI_KEY_STRING))
			idAnnotationSearchParametersPanel.setInChIKey(paramsMap.get(INCHI_KEY_STRING));
		
		if(paramsMap.containsKey(ANNOTATED_ONLY))
			idAnnotationSearchParametersPanel.setAnnotatedOnly(Boolean.valueOf(paramsMap.get(ANNOTATED_ONLY)));
		
		if(paramsMap.containsKey(SEARCH_ALL_IDS))
			idAnnotationSearchParametersPanel.setSearchAllIds(Boolean.valueOf(paramsMap.get(SEARCH_ALL_IDS)));
		
		if(paramsMap.get(ID_LEVEL_LIST) != null && !paramsMap.get(ID_LEVEL_LIST).isEmpty()) {
			
			Collection<MSFeatureIdentificationLevel>levelsToSelect = 
					new ArrayList<MSFeatureIdentificationLevel>();	
			
			String[] levelUids = 
					StringUtils.split(paramsMap.get(ID_LEVEL_LIST), ';');
			if(levelUids.length > 0) {
				
				for(String id : levelUids) {
					MSFeatureIdentificationLevel level = 
							IDTDataCache.getMSFeatureIdentificationLevelById(id);
					if(level != null)
						levelsToSelect.add(level);
				}
				idAnnotationSearchParametersPanel.setSelectedIdLevels(levelsToSelect);
			}
		}
		
		if(paramsMap.get(FOLLOWUP_STEP_LIST) != null && !paramsMap.get(FOLLOWUP_STEP_LIST).isEmpty()) {
			
			Collection<MSFeatureIdentificationFollowupStep>followupStepsToSelect = 
					new ArrayList<MSFeatureIdentificationFollowupStep>();		
			String[] stepsUids = 
					StringUtils.split(paramsMap.get(FOLLOWUP_STEP_LIST), ';');
			if(stepsUids.length > 0) {
				
				for(String id : stepsUids) {
					MSFeatureIdentificationFollowupStep step = 
							IDTDataCache.getMSFeatureIdentificationFollowupStepById(id);
					if(step != null)
						followupStepsToSelect.add(step);
				}
				idAnnotationSearchParametersPanel.setSelectedFollowupSteps(followupStepsToSelect);
			}
		}
	
		//	Sample type and experiment
		if(paramsMap.get(SAMPLE_TYPE_LIST) != null && !paramsMap.get(SAMPLE_TYPE_LIST).isEmpty()) {
			
			Collection<LIMSSampleType> availableSampleTypes = new ArrayList<LIMSSampleType>();
			try {
				availableSampleTypes = IDTUtils.getAvailableSampleTypes();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			Collection<LIMSSampleType>sampleTypesToSelect = new ArrayList<LIMSSampleType>();		
			String[] stypesUids = 
					StringUtils.split(paramsMap.get(SAMPLE_TYPE_LIST), ';');
			if(stypesUids.length > 0) {
				
				for(String id : stypesUids) {
					LIMSSampleType type = availableSampleTypes.stream().
							filter(t -> t.getId().equals(id)).
							findFirst().orElse(null);
					if(type != null)
						sampleTypesToSelect.add(type);
				}
				sampleExperimentParametersPanel.setSelectedSampleTypes(sampleTypesToSelect);
			}
		}
		if(paramsMap.get(EXPERIMENT_ID_LIST) != null && !paramsMap.get(EXPERIMENT_ID_LIST).isEmpty()) {
			
			Collection<LIMSExperiment>experimentsToSelect = new ArrayList<LIMSExperiment>();		
			String[] experimentsUids = 
					StringUtils.split(paramsMap.get(EXPERIMENT_ID_LIST), ';');
			if(experimentsUids.length > 0) {
				
				for(String id : experimentsUids) {
					
					LIMSExperiment experiment = IDTDataCache.getExperimentById(id);
					if(experiment != null)
						experimentsToSelect.add(experiment);
				}
				sampleExperimentParametersPanel.setSelectedExperiments(experimentsToSelect);
			}
		}
		
		//	Acquisition parameters
		if(paramsMap.get(SEPARATION_TYPE_LIST) != null && !paramsMap.get(SEPARATION_TYPE_LIST).isEmpty()) {
			
			Collection<ChromatographicSeparationType>cstToSelect = 
					new ArrayList<ChromatographicSeparationType>();		
			String[] cstUids = 
					StringUtils.split(paramsMap.get(SEPARATION_TYPE_LIST), ';');
			if(cstUids.length > 0) {
				
				for(String id : cstUids) {
					
					ChromatographicSeparationType cst = 
							IDTDataCache.getChromatographicSeparationTypeById(id);
					if(cst != null)
						cstToSelect.add(cst);
				}
				dataAcquisitionParametersPanel.setSelectedChromatographicSeparationTypes(cstToSelect);
			}
		}
		if(paramsMap.get(CHROMATOGRAPHIC_COLUMN_LIST) != null && !paramsMap.get(CHROMATOGRAPHIC_COLUMN_LIST).isEmpty()) {
			
			Collection<LIMSChromatographicColumn>columnsToSelect = 
					new ArrayList<LIMSChromatographicColumn>();		
			String[] ccolumnUids = 
					StringUtils.split(paramsMap.get(CHROMATOGRAPHIC_COLUMN_LIST), ';');
			if(ccolumnUids.length > 0) {
				
				for(String id : ccolumnUids) {
					
					LIMSChromatographicColumn column = IDTDataCache.getColumnById(id);
					if(column != null)
						columnsToSelect.add(column);
				}
				dataAcquisitionParametersPanel.setSelectedChromatographicColumns(columnsToSelect);
			}
		}
		if(paramsMap.get(MS_TYPE_LIST) != null && !paramsMap.get(MS_TYPE_LIST).isEmpty()) {
			
			Collection<MsType>msTypesToSelect = new ArrayList<MsType>();		
			String[] mstUids = 
					StringUtils.split(paramsMap.get(MS_TYPE_LIST), ';');
			if(mstUids.length > 0) {
				
				for(String id : mstUids) {
					
					MsType mstype = IDTDataCache.getMsTypeById(id);
					if(mstype != null)
						msTypesToSelect.add(mstype);
				}
				dataAcquisitionParametersPanel.setSelectedMsTypes(msTypesToSelect);
			}
		}
		if(paramsMap.get(ACQUISITION_METHODS_LIST) != null && !paramsMap.get(ACQUISITION_METHODS_LIST).isEmpty()) {
			
			Collection<DataAcquisitionMethod>acqMethodsToSelect = 
					new ArrayList<DataAcquisitionMethod>();		
			String[] acqMethodUids = 
					StringUtils.split(paramsMap.get(ACQUISITION_METHODS_LIST), ';');
			if(acqMethodUids.length > 0) {
				
				for(String id : acqMethodUids) {
					
					DataAcquisitionMethod acqMethod = 
							IDTDataCache.getAcquisitionMethodById(id);
					if(acqMethod != null)
						acqMethodsToSelect.add(acqMethod);
				}
				dataAcquisitionParametersPanel.setSelectedDataAcquisitionMethods(acqMethodsToSelect);
			}
		}
		if(paramsMap.get(DATA_ANALYSIS_METHODS_LIST) != null && !paramsMap.get(DATA_ANALYSIS_METHODS_LIST).isEmpty()) {
			
			Collection<DataExtractionMethod>dataAnalysisMethodsToSelect = 
					new ArrayList<DataExtractionMethod>();		
			String[] dataAnalysisMethodUids = 
					StringUtils.split(paramsMap.get(DATA_ANALYSIS_METHODS_LIST), ';');
			if(dataAnalysisMethodUids.length > 0) {
				
				for(String id : dataAnalysisMethodUids) {
					
					DataExtractionMethod dataAnalysisMethod = 
							IDTDataCache.getDataExtractionMethodById(id);
					if(dataAnalysisMethod != null)
						dataAnalysisMethodsToSelect.add(dataAnalysisMethod);
				}
				dataAcquisitionParametersPanel.setSelectedDataExtractionMethods(dataAnalysisMethodsToSelect);
			}
		}		
	
		//	Library search 
		if(paramsMap.containsKey(ORIGINAL_MSMS_LIBRARY_ID))
			librarySearchParametersPanel.setOriginalLibraryId(paramsMap.get(ORIGINAL_MSMS_LIBRARY_ID));
	
		if(paramsMap.containsKey(MRC2_MSMS_LIBRARY_ID))
			librarySearchParametersPanel.setMRC2LibraryId(paramsMap.get(MRC2_MSMS_LIBRARY_ID));
		
		if(paramsMap.get(MSMS_LIBRARY_LIST) != null && !paramsMap.get(MSMS_LIBRARY_LIST).isEmpty()) {
			
			Collection<ReferenceMsMsLibrary>msmsLibsToSelect = new ArrayList<ReferenceMsMsLibrary>();		
			String[] msmsLibsUids = 
					StringUtils.split(paramsMap.get(MSMS_LIBRARY_LIST), ';');
			if(msmsLibsUids.length > 0) {
				
				for(String id : msmsLibsUids) {
					
					ReferenceMsMsLibrary msmsLib = IDTDataCache.getReferenceMsMsLibraryById(id);
					if(msmsLib != null)
						msmsLibsToSelect.add(msmsLib);
				}
				librarySearchParametersPanel.selectLibraries(msmsLibsToSelect);
			}
		}
		if(paramsMap.containsKey(SEARCH_ALL_MSMS_LIBRARY_IDS))
			librarySearchParametersPanel.setSearchAllLibraryMatches(
					Boolean.valueOf(paramsMap.get(SEARCH_ALL_MSMS_LIBRARY_IDS)));
	}
	
	@Override
	public void loadLayout(File layoutFile) {

		if(control != null) {

			if(layoutFile.exists()) {
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
	}

	@Override
	public void saveLayout(File layoutFile) {

		if(control != null) {
			try {
				control.writeXML(layoutFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}
}










