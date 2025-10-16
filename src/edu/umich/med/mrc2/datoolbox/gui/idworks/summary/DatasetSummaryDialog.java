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

package edu.umich.med.mrc2.datoolbox.gui.idworks.summary;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.byexp.DockableDataPipelinesTable;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.byexp.DockableExperimentsTable;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DatasetSummaryDialog extends JDialog
		implements ActionListener, ItemListener, ListSelectionListener, PersistentLayout {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1012978681503321256L;
	private static final Icon dsSummaryIcon = GuiUtils.getIcon("calcStats", 32);
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "DatasetSummaryDialog.layout");
	
	private CControl control;
	private CGrid grid;
	private DockableExperimentsTable experimentsTable;
	private DockableDataPipelinesTable dataPipelinesTable;
	private DockableDataSetStatsPanel dataSetStatsPanel;
	private Window idp;
	private MsFeatureInfoBundleCollection activeFeatureCollection;
	
	public DatasetSummaryDialog(MsFeatureInfoBundleCollection activeFeatureCollection) {
		
		super();
		this.activeFeatureCollection = activeFeatureCollection;
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Active data set summary");
		setIconImage(((ImageIcon)dsSummaryIcon).getImage());
		setPreferredSize(new Dimension(800, 800));	
		
		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);
		
		experimentsTable = new DockableExperimentsTable(this);		
		dataPipelinesTable = new DockableDataPipelinesTable();
		dataSetStatsPanel = new DockableDataSetStatsPanel(activeFeatureCollection);
		
		grid.add(0, 0, 1, 1, 
				dataSetStatsPanel,
				experimentsTable, 
				dataPipelinesTable);
		
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
		JButton btnCancel = new JButton("Close");
		panel_1.add(btnCancel);
		btnCancel.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(btnCancel);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		loadLayout(layoutConfigFile);
		pack();
	}
	
	public void generateDataSetStats() {

		CreateDataSetSummaryTask task = 
				new CreateDataSetSummaryTask(activeFeatureCollection);
		idp = new IndeterminateProgressDialog(
				"Generating data set summary ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}
	
	class CreateDataSetSummaryTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */	
		private MsFeatureInfoBundleCollection featureCollection;
		
		public CreateDataSetSummaryTask(MsFeatureInfoBundleCollection featureCollection) {			
			this.featureCollection = featureCollection;
		}

		@Override
		public Void doInBackground() {

			try {
				createDataSetSummary(featureCollection);
					
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	
	private void createDataSetSummary(MsFeatureInfoBundleCollection featureCollection) {
		// TODO Auto-generated method stub
		
		List<LIMSExperiment> experiments = 
				featureCollection.getFeatures().stream().
				map(b -> b.getExperiment()).distinct().
				sorted().collect(Collectors.toList());
		experimentsTable.setTableModelFromExperimentList(experiments);
		
		
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

//		if(command.equals(MainActionCommands.IDTRACKER_RESET_FORM_COMMAND.getName()))
//			resetForm();
//
//		if(command.equals(MainActionCommands.IDTRACKER_REFRESH_FORM_OPTIONS_COMMAND.getName()))
//			populateSelectorsFromDatabase();
//
//		if(command.equals(MainActionCommands.SEARCH_ID_TRACKER_BY_EXPERIMENT_MZ_RT_COMMAND.getName()))
//			searchIdTracker();
	}

	private void resetForm() {
		// TODO Auto-generated method stub
		
	}

	private void populateSelectorsFromDatabase() {
		// TODO Auto-generated method stub
		
	}

	private void searchIdTracker() {
		// TODO Auto-generated method stub
		
		
		
		dispose();
	}

	@Override
	public void dispose() {

		saveLayout(layoutConfigFile);
		super.dispose();
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		if(!e.getValueIsAdjusting()) {
			
			Collection<LIMSExperiment> selectedExperiments = 
					experimentsTable.getSelectedExperiments();
			if(selectedExperiments.isEmpty())
				return;
			
			Collection<DataPipeline>allPipelines = new TreeSet<DataPipeline>();
			for(LIMSExperiment experiment : selectedExperiments) {
				
				Collection<DataPipeline> pipelines = 
						IDTDataCache.getDataPipelinesForExperiment(experiment);
				if(pipelines != null)
					allPipelines.addAll(pipelines);
			}
			dataPipelinesTable.setTableModelFromDataPipelineCollection(allPipelines);
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub

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
}
