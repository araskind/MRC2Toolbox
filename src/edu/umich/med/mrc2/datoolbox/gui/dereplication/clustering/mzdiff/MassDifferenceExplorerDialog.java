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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering.mzdiff;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.mzdiff.DockableMzDiffPlot;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.derepl.MassDifferenceExtractionTask;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MassDifferenceExplorerDialog extends JDialog implements ActionListener, TaskListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = -7046638794476914383L;
	private static final Icon exploreDeltasIcon = GuiUtils.getIcon("exploreDeltas", 32);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "MassDifferenceExplorerDialog.layout");

	private MassDifferenceExplorerToolbar toolbar;

	private CControl control;
	private CGrid grid;
	private DockableMzDiffPlot deltaMassPlot;
	private DockableMassDifferenceSummaryTable deltaMassTable;

	private Preferences preferences;
	public static final String PREFS_NODE = "dereplication.clustering.mzdiff.MassDifferenceExplorerDialog";
	public static final String MIN_DIFF = "MIN_DIFF";
	public static final String MAX_DIFF = "MAX_DIFF";
	public static final String BINNING_WINDOW = "BINNING_WINDOW";
	public static final String MINIMAL_FREQUENCY = "MINIMAL_FREQUENCY";
	public static final String MAXIMAL_CLUSTER_SIZE = "MAXIMAL_CLUSTER_SIZE";

	public MassDifferenceExplorerDialog() {

		super(MRC2ToolBoxCore.getMainWindow(), "Explore mass differences", false);
		setIconImage(((ImageIcon)exploreDeltasIcon).getImage());
		setSize(new Dimension(800, 640));
		setPreferredSize(new Dimension(800, 640));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		toolbar =  new MassDifferenceExplorerToolbar(this);
		add(toolbar, BorderLayout.NORTH);

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);

		deltaMassPlot = new DockableMzDiffPlot(
				"MassDifferenceExplorerDialogDockableMzDiffPlot", 
				"Mass difference plot");
		deltaMassTable = new DockableMassDifferenceSummaryTable(
				"MassDifferenceExplorerDialogDockableMassDifferenceSummaryTable", 
				"Mass difference summary table");
		grid.add(0, 0, 100, 100, deltaMassTable, deltaMassPlot);

		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);

		loadLayout(layoutConfigFile);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.EXTRACT_MASS_DIFFERENCES_COMMAND.getName()))
			extractMassDifferences();
	}

	private void extractMassDifferences() {

		savePreferences();

		Range massDiffRange = new Range(toolbar.getMinDifference(), toolbar.getMaxDifference());
		double binningWindow = toolbar.getBinningWindow();
		int minFrequency = toolbar.getMinFrequency();
		int maxClusterSize = toolbar.getMaxClusterSize();
		DataAnalysisProject currentProject = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		Set<MsFeatureCluster> clusters = currentProject.
				getCorrelationClustersForDataPipeline(currentProject.getActiveDataPipeline());

		if(clusters == null)
			return;

		if(clusters.isEmpty())
			return;

		MassDifferenceExtractionTask task = new MassDifferenceExtractionTask(
				clusters,
				massDiffRange,
				binningWindow,
				minFrequency,
				maxClusterSize);

		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;
		toolbar.setMinDifference(preferences.getDouble(MIN_DIFF, 0.0d));
		toolbar.setMaxDifference(preferences.getDouble(MAX_DIFF, 3000.0d));
		toolbar.setBinningWindow(preferences.getDouble(BINNING_WINDOW, 0.1d));
		toolbar.setMinFrequency(preferences.getInt(MINIMAL_FREQUENCY, 10));
		toolbar.setMaxClusterSize(preferences.getInt(MAXIMAL_CLUSTER_SIZE, 50));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userRoot().node(PREFS_NODE);

		preferences.putDouble(MIN_DIFF, toolbar.getMinDifference());
		preferences.putDouble(MAX_DIFF,  toolbar.getMaxDifference());
		preferences.putDouble(BINNING_WINDOW,  toolbar.getBinningWindow());
		preferences.putInt(MINIMAL_FREQUENCY, toolbar.getMinFrequency());
		preferences.putInt(MAXIMAL_CLUSTER_SIZE, toolbar.getMaxClusterSize());
	}

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

	public void saveLayout(File layoutFile) {

		if(control != null) {
			try {
				control.writeXML(layoutFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @return the layoutconfigfile
	 */
	public static File getLayoutconfigfile() {
		return layoutConfigFile;
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(MassDifferenceExtractionTask.class))
				finalizeMassDifferenceExtractionTask((MassDifferenceExtractionTask) e.getSource());			
		}
	}
	
	private void finalizeMassDifferenceExtractionTask(MassDifferenceExtractionTask task) {
		
		deltaMassPlot.showMzDifferenceDistribution(task.getMassDifferenceBins());
		deltaMassTable.setModelFromBins(task.getMassDifferenceBins());
		toFront();
	}
}

























