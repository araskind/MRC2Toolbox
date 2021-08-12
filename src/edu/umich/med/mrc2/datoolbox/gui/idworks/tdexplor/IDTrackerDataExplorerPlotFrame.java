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

package edu.umich.med.mrc2.datoolbox.gui.idworks.tdexplor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.gui.datexp.DockableMzRtMSMSPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class IDTrackerDataExplorerPlotFrame extends JFrame implements PersistentLayout, BackedByPreferences  {


	/**
	 * 
	 */
	private static final long serialVersionUID = 4992640328861229722L;
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.datoolbox.gui.IDTrackerDataExplorerPlotDialog";
	private DockableMzRtMSMSPlotPanel mzRtMSMSPlotPanel;
			
	public static final String MSMS_FEATURE_TABLE_ROW_SUBSET = "MSMS_FEATURE_TABLE_ROW_SUBSET";	
	public static final String START_RT = "START_RT";
	public static final String END_RT = "END_RT";
	
	private IDWorkbenchPanel parentPanel;
	private CControl control;
	private CGrid grid;

	private static final Icon bubbleIcon = GuiUtils.getIcon("bubble", 32);
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "IDTrackerDataExplorerPlotDialog.layout");
	
	public IDTrackerDataExplorerPlotFrame(IDWorkbenchPanel parentPanel) {

		super("ID-Tracker data explorer");
		this.parentPanel = parentPanel;
		setIconImage(((ImageIcon) bubbleIcon).getImage());
		setSize(new Dimension(800, 600));
		setPreferredSize(new Dimension(800, 600));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);

		mzRtMSMSPlotPanel = new DockableMzRtMSMSPlotPanel();
		mzRtMSMSPlotPanel.setParentPanel(parentPanel);
		grid.add(0, 0, 1, 1, mzRtMSMSPlotPanel);
		
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
		mzRtMSMSPlotPanel.clearPanel();
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
		TableRowSubset msmsRowSubset = TableRowSubset.getSubsetByName(
				preferences.get(MSMS_FEATURE_TABLE_ROW_SUBSET, TableRowSubset.ALL.name()));
		
		mzRtMSMSPlotPanel.setActiveTableRowSubset(msmsRowSubset);
		
		double rtStart = preferences.getDouble(START_RT, 0.0d);
		double rtEnd = preferences.getDouble(END_RT, 300.0d);
		Range rtRange = new Range(rtStart, rtEnd);
		mzRtMSMSPlotPanel.setRtRange(rtRange);
	}

	@Override
	public void loadPreferences() {		
		preferences = Preferences.userRoot().node(PREFS_NODE);
		loadPreferences(preferences);
	}

	@Override
	public void savePreferences() {
		
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(MSMS_FEATURE_TABLE_ROW_SUBSET, mzRtMSMSPlotPanel.getActiveTableRowSubset().name());		
		Range rtRange = mzRtMSMSPlotPanel.getRtRange();
		if(rtRange != null) {
			preferences.putDouble(START_RT, rtRange.getMin());
			preferences.putDouble(END_RT, rtRange.getMax());
		}
	}
}





