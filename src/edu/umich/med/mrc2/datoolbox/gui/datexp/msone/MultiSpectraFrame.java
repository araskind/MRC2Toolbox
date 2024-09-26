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

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class MultiSpectraFrame extends JFrame
		implements ActionListener, BackedByPreferences, PersistentLayout, ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Preferences preferences;
	public static final String PREFS_NODE = MultiSpectraFrame.class.getName();
	
	private static final Icon bubbleIcon = GuiUtils.getIcon("bubble", 32);
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "MultiSpectraFrame.layout");
	
	private CControl control;
	private CGrid grid;
	
	
	public MultiSpectraFrame() {

		super("Data explorer plot");
		setIconImage(((ImageIcon) bubbleIcon).getImage());

		setSize(new Dimension(800, 600));
		setPreferredSize(new Dimension(800, 600));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);
		
//		grid.add(0, 0, 1, 1,
//				mzRtBubblePlotPanel, 
//				qcHistogramPanel, 
//				featureHeatMapPanel,
//				mzMassDefectBubblePlotPanel
//				);
		
		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
		loadLayout(layoutConfigFile);
		loadPreferences();
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

}
