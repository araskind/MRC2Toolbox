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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class RawDataAnalysisProjectSetupDialog extends JDialog 
	implements PersistentLayout, ActionListener, BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6638353683063626416L;

	private static final Icon rdaProjectIcon = GuiUtils.getIcon("newRawDataAnalysisProject", 32);
	private static final Icon editRdaProjectIcon = GuiUtils.getIcon("editRawDataAnalysisProject", 32);
	
	private static final Icon msOneIcon = GuiUtils.getIcon("chromGradient", 16);
	private static final Icon msmsIcon = GuiUtils.getIcon("chromGradient", 16);
	
	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "RawDataAnalysisProjectSetupDialog.layout");
	private Preferences preferences;
	public static final String PREFS_NODE = 
			"edu.umich.med.mrc2.datoolbox.gui.rawdata.RawDataAnalysisProjectSetupDialog";
	public static final String MSMS_BASE_DIRECTORY = "MSMS_BASE_DIRECTORY";
	public static final String MS1_BASE_DIRECTORY = "MS1_BASE_DIRECTORY";
	public static final String PROJECT_BASE_DIRECTORY = "PROJECT_BASE_DIRECTORY";
	public static final String COPY_RAW_DATA_TO_PROJECT = "COPY_RAW_DATA_TO_PROJECT";
	private File projectBaseDir;

	private CControl control;
	private CGrid grid;
	private DockableRawDataFileSelector rawMSMSDataFileSelector;
	private DockableRawDataFileSelector rawMSOneDataFileSelector;
	private ProjectDetailsPanel projectDetailsPanel;
	private JCheckBox copyFilesCheckBox;
	
	public RawDataAnalysisProjectSetupDialog(ActionListener actionListener) {
		
		super();

		setPreferredSize(new Dimension(1000, 800));
		setSize(new Dimension(1000, 800));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setTitle("Create new raw data analysis project");
		setIconImage(((ImageIcon) rdaProjectIcon).getImage());
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);
		
		rawMSMSDataFileSelector = new DockableRawDataFileSelector("Select MSMS data files", msmsIcon);	
		rawMSOneDataFileSelector =  new DockableRawDataFileSelector("Select MS1 data files", msOneIcon);
		grid.add(0, 0, 75, 100, rawMSMSDataFileSelector, rawMSOneDataFileSelector);
		control.getContentArea().deploy(grid);
		getContentPane().add(control.getContentArea(), BorderLayout.CENTER);
		
		projectDetailsPanel = new ProjectDetailsPanel(this);
		getContentPane().add(projectDetailsPanel, BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);
		
		copyFilesCheckBox = new JCheckBox("Copy raw data to project");
		panel.add(copyFilesCheckBox);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton("Save");
		btnSave.setActionCommand(MainActionCommands.NEW_RAW_DATA_PROJECT_COMMAND.getName());
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		loadPreferences();
		loadLayout(layoutConfigFile);
		pack();
	}
		
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.SELECT_PROJECT_LOCATION_COMMAND.getName()))
			setProjectLocation();

	}
	
	private void setProjectLocation() {
		
		ImprovedFileChooser chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		if(projectBaseDir != null)
			chooser.setCurrentDirectory(projectBaseDir);
		
		if(chooser.showOpenDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {
			
			File selectedFile = chooser.getSelectedFile();
			if(selectedFile != null && selectedFile.exists()) {
				projectBaseDir = selectedFile;
				projectDetailsPanel.setProjectLocation(selectedFile);
				savePreferences();
			}
		}
	}
	
	public String getProjectName() {
		return projectDetailsPanel.getProjectName();
	}

	public String getProjectDescription() {
		return projectDetailsPanel.getProjectDescription();
	}
	
	public String getProjectLocationPath() {
		return projectDetailsPanel.getProjectLocationPath();
	}
	
	public Collection<File> getMSMSDataFiles() {
		return rawMSMSDataFileSelector.getDataFiles();
	}
	
	public Collection<File> getMSOneDataFiles() {
		return rawMSOneDataFileSelector.getDataFiles();
	}
	
	public boolean copyRawDataToProject() {
		return copyFilesCheckBox.isSelected();
	}
	
	public void dispose() {
		
		savePreferences();
		saveLayout(layoutConfigFile);
		super.dispose();
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
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		File msmsBaseDirectory =
			new File(preferences.get(MSMS_BASE_DIRECTORY,
					MRC2ToolBoxConfiguration.getRawDataRepository()));
		rawMSMSDataFileSelector.setBaseDirectory(msmsBaseDirectory);	
		
		File msOneBaseDirectory =
				new File(preferences.get(MS1_BASE_DIRECTORY,
						MRC2ToolBoxConfiguration.getRawDataRepository()));
		rawMSOneDataFileSelector.setBaseDirectory(msOneBaseDirectory);
		
		projectBaseDir =
				new File(preferences.get(PROJECT_BASE_DIRECTORY,
						MRC2ToolBoxConfiguration.getDefaultProjectsDirectory()));
		projectDetailsPanel.setProjectLocation(projectBaseDir);
		
		copyFilesCheckBox.setSelected(preferences.getBoolean(COPY_RAW_DATA_TO_PROJECT, Boolean.TRUE));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		
		preferences = Preferences.userRoot().node(PREFS_NODE);
		
		if(rawMSMSDataFileSelector.getBaseDirectory() != null)
			preferences.put(MSMS_BASE_DIRECTORY, rawMSMSDataFileSelector.getBaseDirectory().getAbsolutePath());
		
		if(rawMSOneDataFileSelector.getBaseDirectory() != null)
			preferences.put(MS1_BASE_DIRECTORY, rawMSOneDataFileSelector.getBaseDirectory().getAbsolutePath());
		
		if(projectBaseDir != null)
			preferences.put(PROJECT_BASE_DIRECTORY, projectBaseDir.getAbsolutePath());
		
		preferences.putBoolean(COPY_RAW_DATA_TO_PROJECT, copyFilesCheckBox.isSelected());		
	}
}
