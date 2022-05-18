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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dextr;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.AbstractIDTrackerLimsPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DockableDataExtractionMethodManagerPanel extends AbstractIDTrackerLimsPanel {

	private static final Icon componentIcon = GuiUtils.getIcon("editDataProcessingMethod", 16);
	private static final Icon editMethodIcon = GuiUtils.getIcon("editDataProcessingMethod", 24);
	private static final Icon addMethodIcon = GuiUtils.getIcon("addDataProcessingMethod", 24);
	private static final Icon deleteMethodIcon = GuiUtils.getIcon("deleteDataProcessingMethod", 24);
	private static final Icon downloadMethodIcon = GuiUtils.getIcon("downloadDataProcessingMethod", 24);
	private static final Icon linkToDataAcquisitionMethodIcon = GuiUtils.getIcon("linkToDataAcquisitionMethod", 24);

//	private DataExtractionMethodManagerToolbar toolbar;
	private DataExtractionMethodTable dataExtractionMethodTable;
	private DataExtractionMethodEditorDialog dataExtractionMethodEditorDialog;
	private ImprovedFileChooser chooser;
	private File baseDirectory;
	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.cefanalyzer.gui.DataExtractionMethodManagerPanel";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";

	public DockableDataExtractionMethodManagerPanel(IDTrackerLimsManagerPanel idTrackerLimsManager) {

		super(idTrackerLimsManager, "DockableDataExtractionMethodManagerPanel", 
				componentIcon, "Data extraction methods", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

//		toolbar = new DataExtractionMethodManagerToolbar(this);
//		getContentPane().add(toolbar, BorderLayout.NORTH);

		dataExtractionMethodTable = new DataExtractionMethodTable();
		JScrollPane designScrollPane = new JScrollPane(dataExtractionMethodTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
		dataExtractionMethodTable.addMouseListener(

				new MouseAdapter() {

					public void mouseClicked(MouseEvent e) {

						if (e.getClickCount() == 2) {

							DataExtractionMethod selectedMethod = dataExtractionMethodTable.getSelectedMethod();
							if(selectedMethod != null)
								showDataExtractionMethodEditor(selectedMethod);
						}											
					}
				});
		initActions();
		loadPreferences();
	}

	@Override
	protected void initActions() {
		// TODO Auto-generated method stub
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ADD_DATA_EXTRACTION_METHOD_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_DATA_EXTRACTION_METHOD_DIALOG_COMMAND.getName(), 
				addMethodIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EDIT_DATA_EXTRACTION_METHOD_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_DATA_EXTRACTION_METHOD_DIALOG_COMMAND.getName(), 
				editMethodIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_DATA_EXTRACTION_METHOD_COMMAND.getName(),
				MainActionCommands.DELETE_DATA_EXTRACTION_METHOD_COMMAND.getName(), 
				deleteMethodIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DOWNLOAD_DATA_EXTRACTION_METHOD_COMMAND.getName(),
				MainActionCommands.DOWNLOAD_DATA_EXTRACTION_METHOD_COMMAND.getName(), 
				downloadMethodIcon, this));
	}

	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(baseDirectory);
	}

	public void loadMethods() {
		dataExtractionMethodTable.setTableModelFromMethods(IDTDataCash.getDataExtractionMethods());
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(!isConnected())
			return;
		
		String command = e.getActionCommand();

		if(command.equals(MainActionCommands.ADD_DATA_EXTRACTION_METHOD_DIALOG_COMMAND.getName()))
			showDataExtractionMethodEditor(null);

		if(command.equals(MainActionCommands.EDIT_DATA_EXTRACTION_METHOD_DIALOG_COMMAND.getName())) {

			DataExtractionMethod selectedMethod = dataExtractionMethodTable.getSelectedMethod();
			if(selectedMethod != null)
				showDataExtractionMethodEditor(selectedMethod);
		}
		if(command.equals(MainActionCommands.ADD_DATA_EXTRACTION_METHOD_COMMAND.getName()) ||
				command.equals(MainActionCommands.EDIT_DATA_EXTRACTION_METHOD_COMMAND.getName()))
			saveDataExtractionMethod();

		if(command.equals(MainActionCommands.DELETE_DATA_EXTRACTION_METHOD_COMMAND.getName()))
			deleteDataExtractionMethod();

		if(command.equals(MainActionCommands.DOWNLOAD_DATA_EXTRACTION_METHOD_COMMAND.getName()))
			showMethodSaveDialog();

		if(e.getSource().equals(chooser) && e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))
			downloadDataExtractionMethodFile();

		if(command.equals(MainActionCommands.LINK_DATA_EXTRACTION_METHOD_TO_ACQUISITION_COMMAND.getName()))
			linkDataExtractionMethodToAcquisitionMethod();
	}

	private void linkDataExtractionMethodToAcquisitionMethod() {

		DataExtractionMethod deMethod = dataExtractionMethodTable.getSelectedMethod();
		DataAcquisitionMethod acqMethod = idTrackerLimsManager.getSelectedAcquisitionMethod();

		if(deMethod == null || acqMethod == null)
			return;

	}

	private void showMethodSaveDialog() {

		DataExtractionMethod method = 
				dataExtractionMethodTable.getSelectedMethod();
		if(method == null)
			return;

		if(chooser == null)
			initChooser();

		chooser.setDialogTitle("Save method \"" + 
					method.getName() + "\" to local drive");
		chooser.setSelectedFile(null);
		chooser.showSaveDialog(this.getContentPane());
	}

	private void saveDataExtractionMethod() {

		Collection<String>errors = 
				dataExtractionMethodEditorDialog.validateMethodData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), 
					dataExtractionMethodEditorDialog);
			return;
		}
		DataExtractionMethod selectedMethod = 
				dataExtractionMethodEditorDialog.getMethod();
		if(selectedMethod == null) {

			selectedMethod = new DataExtractionMethod(
					null,
					dataExtractionMethodEditorDialog.getMethodName(),
					dataExtractionMethodEditorDialog.getMethodDescription(),
					MRC2ToolBoxCore.getIdTrackerUser(),
					new Date());
			selectedMethod.setSoftware(
					dataExtractionMethodEditorDialog.getSoftware());
			String methodId = null;
			try {
				methodId = IDTUtils.addNewDataExtractionMethod(
						selectedMethod, dataExtractionMethodEditorDialog.getMethodFile());
				selectedMethod.setId(methodId);
				IDTDataCash.getDataExtractionMethods().add(selectedMethod);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			if(dataExtractionMethodEditorDialog.getMethodFile() != null) {

				String yesNoQuestion = "Do you want to replace existing method file?";
				if (MessageDialog.showChoiceWithWarningMsg(yesNoQuestion,
						dataExtractionMethodEditorDialog) != JOptionPane.YES_OPTION)
					return;
			}
			selectedMethod.setName(
					dataExtractionMethodEditorDialog.getMethodName());
			selectedMethod.setDescription(
					dataExtractionMethodEditorDialog.getMethodDescription());			
			selectedMethod.setSoftware(
					dataExtractionMethodEditorDialog.getSoftware());
			try {
				IDTUtils.updateDataExtractionMethod(
						selectedMethod, 
						dataExtractionMethodEditorDialog.getMethodFile());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//	TODO remove when tested
		//	 IDTDataCash.refreshDataExtractionMethodList();
		loadMethods();
		dataExtractionMethodEditorDialog.dispose();
	}

	private void showDataExtractionMethodEditor(DataExtractionMethod selectedMethod) {
		dataExtractionMethodEditorDialog = new DataExtractionMethodEditorDialog(selectedMethod, this);
		dataExtractionMethodEditorDialog.setLocationRelativeTo(this.getContentPane());
		dataExtractionMethodEditorDialog.setVisible(true);
	}

	private void deleteDataExtractionMethod() {

		DataExtractionMethod selectedMethod = dataExtractionMethodTable.getSelectedMethod();
		if(selectedMethod == null)
			return;
			
		if(!IDTUtils.isSuperUser(this.getContentPane()))
			return;

		String yesNoQuestion = "Do you want to delete method \"" + selectedMethod.getName() + "\"?";
		int result = MessageDialog.showChoiceWithWarningMsg(yesNoQuestion, this.getContentPane());
		if(result == JOptionPane.YES_OPTION) {

			try {
				IDTUtils.deleteDataExtractionMethod(selectedMethod);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			IDTDataCash.refreshDataExtractionMethodList();
			loadMethods();
		}
	}

	private void downloadDataExtractionMethodFile() {

		DataExtractionMethod selectedMethod = dataExtractionMethodTable.getSelectedMethod();
		File destination = chooser.getSelectedFile();
		baseDirectory = destination;
		try {
			IDTUtils.getDataExtractionMethodFile(selectedMethod, destination);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		savePreferences();
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		loadPreferences();
	}

	@Override
	public void loadPreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		baseDirectory =  new File(preferences.get(BASE_DIRECTORY, MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

	public synchronized void clearPanel() {
		dataExtractionMethodTable.clearTable();
	}
}




















