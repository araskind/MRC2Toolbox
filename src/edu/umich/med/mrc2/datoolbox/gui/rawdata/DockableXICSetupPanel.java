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

package edu.umich.med.mrc2.datoolbox.gui.rawdata;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.xic.XICSetupPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.ChromatogramExtractionTask;

public class DockableXICSetupPanel extends DefaultSingleCDockable implements ActionListener {

	private static final Icon chromIcon = GuiUtils.getIcon("chromatogram", 16);
	private XICSetupPanel xicSetupPanel;
	private Preferences preferences;

	public DockableXICSetupPanel(ActionListener parentPanel) {

		super("DockableXICSetupPanel", chromIcon, "Define chromatograms", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));
		xicSetupPanel = new XICSetupPanel(parentPanel); 
		add(xicSetupPanel, BorderLayout.CENTER);
		
		preferences = Preferences.userRoot().node(DockableXICSetupPanel.class.getName());
		xicSetupPanel.loadPreferences(preferences);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

	}

	public Collection<String> veryfyParameters() {
		return xicSetupPanel.veryfyParameters();
	}
	
	public ChromatogramExtractionTask createChromatogramExtractionTask() {
		return xicSetupPanel.createChromatogramExtractionTask();
	}

	public synchronized void clearPanel() {
		xicSetupPanel.clearPanel();
	}
	
	public void loadData(Collection<DataFile> dataFiles, boolean append) {	
		xicSetupPanel.loadData(dataFiles, append);
	}
	
	public void selectFiles(Collection<DataFile> dataFiles) {
		xicSetupPanel.selectFiles(dataFiles);
	}

	public void removeDataFiles(Collection<DataFile> filesToRemove) {
		xicSetupPanel.removeDataFiles(filesToRemove);
	}
	
	public XICSetupPanel getPanel() {
		return xicSetupPanel;
	}
}
