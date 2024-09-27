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

package edu.umich.med.mrc2.datoolbox.gui.datexp.msone;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.ujmp.core.Matrix;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;

public class MultiSpectraPlotFrame extends JFrame
		implements ActionListener, BackedByPreferences, PersistentLayout, ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Preferences preferences;
	public static final String PREFS_NODE = MultiSpectraPlotFrame.class.getName();
	
	private static final Icon multiSpectraIcon = GuiUtils.getIcon("multiSpectra", 32);
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "MultiSpectraPlotFrame.layout");
	
	private MultiSpectraToolbar toolbar;
	private CControl control;
	private CGrid grid;
	private DockableMZandRTvariationPlotPanel mzRTvariationPlotPanel;
	private DockableMultispectrumPlotPanel multispectrumPlotPanel;
	
	private DataAnalysisProject currentExperiment;
	private DataPipeline dataPipeline;
	private Matrix featureDataMatrix;
	private MsFeature activeFeature;
		
	public MultiSpectraPlotFrame(
			DataAnalysisProject currentExperiment, 
			DataPipeline dataPipeline) {

		super("MS1 multi-spectra plot");
		this.currentExperiment = currentExperiment;
		this.dataPipeline = dataPipeline;
		
		setIconImage(((ImageIcon) multiSpectraIcon).getImage());
		setSize(new Dimension(800, 1000));
		setPreferredSize(new Dimension(800, 1000));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		toolbar = new MultiSpectraToolbar(this);
		add(toolbar, BorderLayout.NORTH);

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);
		
		mzRTvariationPlotPanel = new DockableMZandRTvariationPlotPanel();
		multispectrumPlotPanel = new DockableMultispectrumPlotPanel();
		grid.add(0, 0, 1, 1,
				mzRTvariationPlotPanel,
				multispectrumPlotPanel);
		
		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
		loadLayout(layoutConfigFile);
		loadPreferences();
		
	}
	
	@Override
	public void setVisible(boolean b) {
		
		super.setVisible(b);
		if(b)
			initDataMatrix();
	}
	
	@Override
	public void dispose() {
		
		savePreferences();
		saveLayout(layoutConfigFile);
		super.dispose();
	}

	private void initDataMatrix() {

		ExperimentDesignRetrievalTask task = 
				new ExperimentDesignRetrievalTask(currentExperiment, dataPipeline);
		IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
				"Reading feature data matrix ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	class ExperimentDesignRetrievalTask extends LongUpdateTask {

		private DataAnalysisProject currentExperiment;
		private DataPipeline dataPipeline;

		public ExperimentDesignRetrievalTask(
				DataAnalysisProject currentExperiment, 
				DataPipeline dataPipeline) {
			super();
			this.currentExperiment = currentExperiment;
			this.dataPipeline = dataPipeline;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Void doInBackground() {

			try {
				featureDataMatrix = 
						ExperimentUtils.readFeatureMatrix(currentExperiment, dataPipeline);
				if(featureDataMatrix == null) {
					return null;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
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
	public void loadPreferences(Preferences prefs) {
		// TODO Auto-generated method stub
		preferences = prefs;
	}

	@Override
	public void loadPreferences() {
		
		preferences = Preferences.userRoot().node(PREFS_NODE);
		loadPreferences(preferences);
	}

	@Override
	public void savePreferences() {
		// TODO Auto-generated method stub
		preferences = Preferences.userRoot().node(PREFS_NODE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	public void loadFeatureData(MsFeature feature) {

		mzRTvariationPlotPanel.loadFeatureData(feature);
	}
	

}















