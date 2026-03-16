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

package edu.umich.med.mrc2.datoolbox.gui.rgen.modality;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.Icon;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableModalityAnalysisScriptGenerator extends DefaultSingleCDockable
		implements ActionListener, BackedByPreferences {

	private static final Icon componentIcon = GuiUtils.getIcon("rScriptMC", 32);
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.DockableModalityAnalysisScriptGenerator";
	
	public DockableModalityAnalysisScriptGenerator() {
		
		super("DockableModalityAnalysisScriptGenerator", componentIcon, 
				"Generate MetabCombiner script for multiple batch alignment", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		
		
		
		loadPreferences();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}	
	
	@Override
	public void loadPreferences(Preferences prefs) {
	    preferences = prefs;
//	    baseDirectory =
//	        new File(preferences.get(BASE_DIRECTORY,
//	            MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void loadPreferences() {
	    loadPreferences(Preferences.userRoot().node(PREFS_NODE));
//		 Preferences.userNodeForPackage(this.getClass());
	}

	@Override
	public void savePreferences() {
	    preferences = Preferences.userRoot().node(PREFS_NODE);
//		 preferences = Preferences.userNodeForPackage(this.getClass());
//	    preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

}
