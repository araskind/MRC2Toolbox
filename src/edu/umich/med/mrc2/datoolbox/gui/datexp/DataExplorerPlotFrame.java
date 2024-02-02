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

package edu.umich.med.mrc2.datoolbox.gui.datexp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.KendrickUnits;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class DataExplorerPlotFrame extends JFrame implements PersistentLayout, BackedByPreferences  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4781460173985140279L;
	private Preferences preferences;
	public static final String PREFS_NODE = DataExplorerPlotFrame.class.getName();
	public static final String FEATURE_BUBBLE_PLOT_DATA_SCALE = "FEATURE_BUBBLE_PLOT_DATA_SCALE";
	public static final String MASS_DEFECT_BUBBLE_PLOT_DATA_SCALE = "MASS_DEFECT_BUBBLE_PLOT_DATA_SCALE";
	public static final String MASS_DEFECT_KENDRICK_UNITS = "MASS_DEFECT_KENDRICK_UNITS";
	
	public static final String MASS_DEFECT_START_RT = "MASS_DEFECT_START_RT";
	public static final String MASS_DEFECT_END_RT = "MASS_DEFECT_END_RT";
	
	private CControl control;
	private CGrid grid;
	private DockableMzRtBubblePlotPanel mzRtBubblePlotPanel;
	private DockableMzMassDefectPlotPanel mzMassDefectBubblePlotPanel;
	private IndeterminateProgressDialog idp;

	private static final Icon bubbleIcon = GuiUtils.getIcon("bubble", 32);
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "DataExplorerPlotDialog.layout");
	
	public DataExplorerPlotFrame() {

		super("Data explorer plot");
		setIconImage(((ImageIcon) bubbleIcon).getImage());

		setSize(new Dimension(800, 600));
		setPreferredSize(new Dimension(800, 600));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);

		mzRtBubblePlotPanel = new DockableMzRtBubblePlotPanel();
		mzMassDefectBubblePlotPanel = new DockableMzMassDefectPlotPanel();
		grid.add(0, 0, 1, 1,
				mzRtBubblePlotPanel, mzMassDefectBubblePlotPanel);
		
		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
		loadLayout(layoutConfigFile);
		loadPreferences();
	}
	
	@Override
	public void setVisible(boolean b) {
		
		if(!b) {
			saveLayout(layoutConfigFile);
			savePreferences();
		}
		super.setVisible(b);
		if(b)
			toFront();
	}
		
	@Override
	public void dispose() {
		
		saveLayout(layoutConfigFile);
		savePreferences();
		super.dispose();
	}
	
	public void clearPanels() {
		mzRtBubblePlotPanel.clearPanel();
		mzMassDefectBubblePlotPanel.clearPanel();
	}

	public void loadMzRtFromFeatureCollection(String title, Collection<MsFeature>features) {

		mzMassDefectBubblePlotPanel.setMsFeatures(features);
		mzMassDefectBubblePlotPanel.setFeatureSetTitle(title);
		CreateMzRtDataSetTask task = new CreateMzRtDataSetTask(title, features);
		idp = new IndeterminateProgressDialog("Creating MZ/RT data set ...", this, task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
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
		preferences = prefs;
		DataScale bubblePlotDataScale = DataScale.getOptionByUIName(
				preferences.get(FEATURE_BUBBLE_PLOT_DATA_SCALE, DataScale.LN.name()));
		if(bubblePlotDataScale != null)
			mzRtBubblePlotPanel.setDataScale(bubblePlotDataScale);
		
		KendrickUnits kendrickUnits = KendrickUnits.getOptionByName(
				preferences.get(MASS_DEFECT_KENDRICK_UNITS, KendrickUnits.NONE.name()));
		if(kendrickUnits != null)
			mzMassDefectBubblePlotPanel.setKendrickUnits(kendrickUnits);
		
		double rtStart = preferences.getDouble(MASS_DEFECT_START_RT, 0.0d);
		double rtEnd = preferences.getDouble(MASS_DEFECT_END_RT, 10.0d);
		Range rtRange = new Range(rtStart, rtEnd);
		mzMassDefectBubblePlotPanel.setRtRange(rtRange);
	}

	@Override
	public void loadPreferences() {
		
		preferences = Preferences.userRoot().node(PREFS_NODE);
		loadPreferences(preferences);
	}

	@Override
	public void savePreferences() {
		
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(FEATURE_BUBBLE_PLOT_DATA_SCALE, mzRtBubblePlotPanel.getDataScale().name());
		preferences.put(MASS_DEFECT_KENDRICK_UNITS, mzMassDefectBubblePlotPanel.getKendrickUnits().name());
		
		Range rtRange = mzMassDefectBubblePlotPanel.getRtRange();
		preferences.putDouble(MASS_DEFECT_START_RT, rtRange.getMin());
		preferences.putDouble(MASS_DEFECT_END_RT, rtRange.getMax());
	}
	
	public void setParentPanel(DockableMRC2ToolboxPanel parentPanel) {
		mzRtBubblePlotPanel.setParentPanel(parentPanel);
		mzMassDefectBubblePlotPanel.setParentPanel(parentPanel);
	}
	
	class CreateMzRtDataSetTask extends LongUpdateTask {

		private String title;
		private Collection<MsFeature>features;
		
		public CreateMzRtDataSetTask(String title, Collection<MsFeature>features) {
			this.title = title;
			this.features = features;
		}

		@Override
		public Void doInBackground() {

			try {
				mzRtBubblePlotPanel.loadFeatureCollection(title, features);
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
}
