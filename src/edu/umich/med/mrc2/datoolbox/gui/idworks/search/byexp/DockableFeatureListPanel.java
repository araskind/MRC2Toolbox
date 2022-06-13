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
import java.io.File;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.FeatureListImportPanel;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DockableFeatureListPanel extends DefaultSingleCDockable implements BackedByPreferences {

	private static final Icon componentIcon = GuiUtils.getIcon("editCollection", 16);
	
	private File baseDirectory;
	
	private Preferences preferences;
	private static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private static final String PREFS_NODE = 
			"edu.umich.med.mrc2.datoolbox.gui.idworks.DockableFeatureListPanel";
	private FeatureListImportPanel featureListImportPanel;
	
	public DockableFeatureListPanel() {
		
		super("DockableFeatureListPanel", componentIcon, "Feature list manager", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		featureListImportPanel = new FeatureListImportPanel();
		add(featureListImportPanel, BorderLayout.CENTER);
		loadPreferences();
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =  new File(preferences.get(
				BASE_DIRECTORY, MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
		featureListImportPanel.setBaseDirectory(baseDirectory);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		baseDirectory = featureListImportPanel.getBaseDirectory();
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}
	
	public Collection<MinimalMSOneFeature>getSelectedFeatures(){
		return featureListImportPanel.getSelectedFeatures();
	}
	
	public Collection<MinimalMSOneFeature>getAllFeatures(){
		return featureListImportPanel.getAllFeatures();
	}
}







