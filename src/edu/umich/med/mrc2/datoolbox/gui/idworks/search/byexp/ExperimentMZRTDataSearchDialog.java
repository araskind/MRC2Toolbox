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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.byexp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang.StringUtils;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.DefaultCDockable.Permissions;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.communication.FormChangeEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FormChangeListener;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.MSMSClusteringParametersPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTMSMSFeatureDataPullWithFilteringTask;

public class ExperimentMZRTDataSearchDialog extends JDialog
		implements ActionListener, BackedByPreferences, ItemListener, 
		ListSelectionListener, PersistentLayout, FormChangeListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1012978681503321256L;
	private static final Icon searchIcon = GuiUtils.getIcon("searchIdExperiment", 32);
	public static final Icon paramsDefinedIcon = GuiUtils.getIcon("multipleIds", 16);
	public static final Icon paramsIcon = GuiUtils.getIcon("cluster", 16);
	
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "ExperimentDataSearchDialog.layout");
	
	private static final String PREFS_NODE = 
			"edu.umich.med.mrc2.datoolbox.ExperimentDataSearchDialog";
	private Preferences preferences;
	
	private IDWorkbenchPanel parentPanel;
	private CControl control;
	private CGrid grid;
	private DockableExperimentsTable experimentsTable;
	private DockableFeatureListPanel featureListPanel;
	private DockableDataPipelinesTable dataPipelinesTable;
	private MSMSClusteringParametersPanel msmsClusteringParametersPanel;
		
	public ExperimentMZRTDataSearchDialog(IDWorkbenchPanel parentPanel) {
		super();
		this.parentPanel = parentPanel;
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Search ID tracker data by experiment");
		setIconImage(((ImageIcon)searchIcon).getImage());
		setPreferredSize(new Dimension(800, 800));	
		
		msmsClusteringParametersPanel = new MSMSClusteringParametersPanel();
		DefaultSingleCDockable cpWrapper  = new DefaultSingleCDockable(
				"DockableMSMSClusteringParametersPanel", paramsIcon, "MSMS clustering parameters", 
				msmsClusteringParametersPanel, Permissions.MIN_MAX_STACK);
		cpWrapper.setCloseable(false);
				
		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);
		
		experimentsTable = new DockableExperimentsTable(this);
		experimentsTable.setTableModelFromExperimentList(IDTDataCache.getExperiments());
		experimentsTable.addFormChangeListener(this);
		
		dataPipelinesTable = new DockableDataPipelinesTable();
		dataPipelinesTable.addFormChangeListener(this);
		
		featureListPanel = new DockableFeatureListPanel();	
		featureListPanel.addFormChangeListener(this);
		grid.add(0, 0, 75, 100, experimentsTable, 
				dataPipelinesTable, featureListPanel, cpWrapper);
		
		control.getContentArea().deploy(grid);
		getContentPane().add(control.getContentArea(), BorderLayout.CENTER);
		
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
		
		JButton refreshButton = new JButton("Refresh data");
		refreshButton.addActionListener(this);
		refreshButton.setActionCommand(MainActionCommands.IDTRACKER_REFRESH_FORM_OPTIONS_COMMAND.getName());
		panel_1.add(refreshButton);

		JButton resetButton = new JButton("Reset form");
		resetButton.addActionListener(this);
		resetButton.setActionCommand(MainActionCommands.IDTRACKER_RESET_FORM_COMMAND.getName());
		panel_1.add(resetButton);

		JButton searchButton = new JButton("Search ...");
		searchButton.addActionListener(this);
		searchButton.setActionCommand(MainActionCommands.SEARCH_IDTRACKER_BY_EXPERIMENT_MZ_RT_COMMAND.getName());
		panel_1.add(searchButton);
		
		JRootPane rootPane = SwingUtilities.getRootPane(searchButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(searchButton);
		
		loadPreferences();
		loadLayout(layoutConfigFile);
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();

		if(command.equals(MainActionCommands.IDTRACKER_RESET_FORM_COMMAND.getName()))
			resetForm();

		if(command.equals(MainActionCommands.IDTRACKER_REFRESH_FORM_OPTIONS_COMMAND.getName()))
			populateSelectorsFromDatabase();

		if(command.equals(MainActionCommands.SEARCH_IDTRACKER_BY_EXPERIMENT_MZ_RT_COMMAND.getName()))
			searchIdTracker();
	}

	private void resetForm() {

		experimentsTable.resetPanel(null);
		dataPipelinesTable.resetPanel(null);
		featureListPanel.resetPanel(null);
	}

	private void populateSelectorsFromDatabase() {
		// TODO Auto-generated method stub
		DataRefreshTask task = new DataRefreshTask();
		IndeterminateProgressDialog idp = 
				new IndeterminateProgressDialog("Refreshing data ...", this, task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	class DataRefreshTask extends LongUpdateTask {

		public DataRefreshTask() {
		}

		@SuppressWarnings("unchecked")
		@Override
		public Void doInBackground() {

			try {
				IDTDataCache.refreshUserList();
				IDTDataCache.refreshOrganizationList();
				IDTDataCache.refreshProjectList();
				IDTDataCache.refreshExperimentList();
				IDTDataCache.refreshStockSampleList();
				IDTDataCache.refreshExperimentStockSampleMap();
				IDTDataCache.refreshExperimentPolarityMap();
				IDTDataCache.refreshManufacturers();
				IDTDataCache.refreshExperimentSamplePrepMap();
				IDTDataCache.refreshSamplePrepDataPipelineMap();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			experimentsTable.setTableModelFromExperimentList(IDTDataCache.getExperiments());
			return null;
		}
	}
	
	private void searchIdTracker() {

		Collection<LIMSExperiment>selectedExperiments =  
				experimentsTable.getSelectedExperiments();
		
		if(selectedExperiments.isEmpty()) {
			MessageDialog.showErrorMsg("Please select one or more experiment(s)", 
					this.getContentPane());
			return;
		}
		Collection<MinimalMSOneFeature>mzrtFeatureList = 
				featureListPanel.getAllFeatures();
			
		if(mzrtFeatureList.isEmpty()) {	
			
			int res = MessageDialog.showChooseOrCancelMsg(
					"M/Z-RT list is empty, do you want to cluster "
					+ "all the features in selected experiments using specified parameters? (YES)\n"
					+ "or just retrieve all the features for selected experiment(s) (NO)?", 
					this.getContentPane());
			if(res == JOptionPane.CANCEL_OPTION)
				return;
			
			if(res == JOptionPane.YES_OPTION)
				getExperimentDataAndClusterFeatures(selectedExperiments, mzrtFeatureList);

			if(res == JOptionPane.NO_OPTION)
				getExperimentFeatures(selectedExperiments);
		}
		else {
			getExperimentDataAndClusterFeatures(selectedExperiments, mzrtFeatureList);
		}
	}
	
	private void getExperimentFeatures(Collection<LIMSExperiment> selectedExperiments) {

		Collection<DataPipeline> dataPipelines = 
				dataPipelinesTable.getSelectedDataPipelines();
		if(dataPipelines.isEmpty()) {
			dataPipelines = dataPipelinesTable.getAllDataPipelines().stream().
				filter(p -> p.getAcquisitionMethod().getPolarity().equals(experimentsTable.getSelectedPolarity())).
				collect(Collectors.toList());
		}
		if(dataPipelines.isEmpty()) {

				MessageDialog.showErrorMsg("No data available for selected experiment(s)", 
						this.getContentPane());
				return;			
		}		
		IDTMSMSFeatureDataPullWithFilteringTask task = 
				new IDTMSMSFeatureDataPullWithFilteringTask(
						selectedExperiments, 
						dataPipelines,
						null,
						null);
		task.addTaskListener(parentPanel);
		MRC2ToolBoxCore.getTaskController().addTask(task);
		dispose();
	}

	private void getExperimentDataAndClusterFeatures(
			Collection<LIMSExperiment>selectedExperiments,
			Collection<MinimalMSOneFeature>mzrtFeatureList) {

		Collection<String>paramErrors = 
				msmsClusteringParametersPanel.validateParameters();
		if(!paramErrors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(paramErrors, "\n"), 
					this.getContentPane());
			return;
		}
		Collection<DataPipeline> dataPipelines = 
				dataPipelinesTable.getSelectedDataPipelines();
		if(dataPipelines.isEmpty()) {
			int res = MessageDialog.showChoiceMsg(
					"Do you want to analyze the complete set of data for selected experiment?", 
					this.getContentPane());
			if(res != JOptionPane.YES_OPTION)
				return;
			else
				dataPipelines = dataPipelinesTable.getAllDataPipelines().stream().
					filter(p -> p.getAcquisitionMethod().getPolarity().equals(experimentsTable.getSelectedPolarity())).
					collect(Collectors.toList());
		}		
		MSMSClusteringParameterSet clusteringParams = 
				msmsClusteringParametersPanel.getParameters();
		
		IDTMSMSFeatureDataPullWithFilteringTask task = 
				new IDTMSMSFeatureDataPullWithFilteringTask(
						selectedExperiments, 
						dataPipelines,
						mzrtFeatureList,
						clusteringParams);
		task.addTaskListener(parentPanel);
		MRC2ToolBoxCore.getTaskController().addTask(task);
		dispose();
	}

	@Override
	public void dispose() {
		
		savePreferences();
		saveLayout(layoutConfigFile);
		super.dispose();
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()) {
			
			adjustDataPipelineListing();
			markPopulatedTabs();
		}
	}

	private void adjustDataPipelineListing() {

		Collection<LIMSExperiment> selectedExperiments = 
				experimentsTable.getSelectedExperiments();
		if(selectedExperiments.isEmpty()) {
			dataPipelinesTable.clearPanel();
			return;
		}			
		Collection<DataPipeline>allPipelines = new TreeSet<DataPipeline>();		
		for(LIMSExperiment experiment : selectedExperiments) {
			
			Collection<DataPipeline> pipelines = 
					IDTDataCache.getDataPipelinesForExperiment(experiment);
			if(pipelines != null)
				allPipelines.addAll(pipelines);
		}
		Polarity pol = experimentsTable.getSelectedPolarity();
		if(pol != null) {
			allPipelines = allPipelines.stream().
			filter(p -> p.getAcquisitionMethod().getPolarity().equals(pol)).
			collect(Collectors.toList());
		}
		dataPipelinesTable.setTableModelFromDataPipelineCollection(allPipelines);
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadPreferences(Preferences prefs) {
		
		this.preferences = prefs;
		msmsClusteringParametersPanel.loadPreferences();
		featureListPanel.loadPreferences();
		markPopulatedTabs();
	}

	@Override
	public void loadPreferences() {		
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		msmsClusteringParametersPanel.savePreferences();
		featureListPanel.savePreferences();
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

			for(int i=0; i<control.getCDockableCount(); i++) {

				CDockable uiObject = control.getCDockable(i);
				if(uiObject instanceof PersistentLayout)
					((PersistentLayout)uiObject).saveLayout(((PersistentLayout)uiObject).getLayoutFile());
			}
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

	@Override
	public void formDataChanged(FormChangeEvent e) {
		
		markPopulatedTabs();
		
		if(!DataPipelinesTable.class.isAssignableFrom(e.getSource().getClass()))
			adjustDataPipelineListing();
	}

	private void markPopulatedTabs() {
		
		if(experimentsTable.hasSpecifiedConstraints())
			experimentsTable.setTitleIcon(paramsDefinedIcon);
		else
			experimentsTable.setTitleIcon(null);
		
		if(dataPipelinesTable.hasSpecifiedConstraints())
			dataPipelinesTable.setTitleIcon(paramsDefinedIcon);
		else
			dataPipelinesTable.setTitleIcon(null);
		
		if(featureListPanel.hasSpecifiedConstraints())
			featureListPanel.setTitleIcon(paramsDefinedIcon);
		else
			featureListPanel.setTitleIcon(null);		
	}
}
