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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.idt.AcquisitionMethodUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.AbstractIDTrackerLimsPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DockableAcquisitionMethodManagerPanel extends AbstractIDTrackerLimsPanel {

	private static final Icon componentIcon = GuiUtils.getIcon("editDataAcquisitionMethod", 16);
	private static final Icon editMethodIcon = GuiUtils.getIcon("editDataAcquisitionMethod", 24);
	private static final Icon addMethodIcon = GuiUtils.getIcon("addDataAcquisitionMethod", 24);
	private static final Icon deleteMethodIcon = GuiUtils.getIcon("deleteDataAcquisitionMethod", 24);
	private static final Icon downloadMethodIcon = GuiUtils.getIcon("downloadDataAcquisitionMethod", 24);
	private static final Icon linkToIdExperimentIcon = GuiUtils.getIcon("linkToIdExperiment", 24);

	private Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.cefanalyzer.gui.AcquisitionMethodManagerPanel";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";

	private AcquisitionMethodManagerToolbar toolbar;
	private AcquisitionMethodTable methodTable;
	private AcquisitionMethodExtendedEditorDialog acquisitionMethodEditorDialog;
//	private JFileChooser chooser;
	private File baseDirectory;

	public DockableAcquisitionMethodManagerPanel(IDTrackerLimsManagerPanel idTrackerLimsManager) {

		super(idTrackerLimsManager, "DockableAcquisitionMethodManagerPanel", 
				componentIcon, "Acquisition methods", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

		toolbar = new AcquisitionMethodManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		methodTable = new AcquisitionMethodTable();
		JScrollPane designScrollPane = new JScrollPane(methodTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
		methodTable.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				if (e.getClickCount() == 2) {
					DataAcquisitionMethod selectedMethod = methodTable.getSelectedMethod();
					if(selectedMethod != null)
						showAcquisitionMethodEditor(selectedMethod);
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
				MainActionCommands.ADD_ACQUISITION_METHOD_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_ACQUISITION_METHOD_DIALOG_COMMAND.getName(), 
				addMethodIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EDIT_ACQUISITION_METHOD_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_ACQUISITION_METHOD_DIALOG_COMMAND.getName(), 
				editMethodIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_ACQUISITION_METHOD_COMMAND.getName(),
				MainActionCommands.DELETE_ACQUISITION_METHOD_COMMAND.getName(), 
				deleteMethodIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DOWNLOAD_ACQUISITION_METHOD_COMMAND.getName(),
				MainActionCommands.DOWNLOAD_ACQUISITION_METHOD_COMMAND.getName(), 
				downloadMethodIcon, this));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(!isConnected())
			return;
		
		super.actionPerformed(e);
		
		String command = e.getActionCommand();

		if(command.equals(MainActionCommands.ADD_ACQUISITION_METHOD_DIALOG_COMMAND.getName()))
			showAcquisitionMethodEditor(null);

		if(command.equals(MainActionCommands.EDIT_ACQUISITION_METHOD_DIALOG_COMMAND.getName())) {

			DataAcquisitionMethod selectedMethod = methodTable.getSelectedMethod();
			if(selectedMethod != null)
				showAcquisitionMethodEditor(selectedMethod);
		}
		if(e.getActionCommand().equals(MainActionCommands.ADD_ACQUISITION_METHOD_COMMAND.getName()) ||
				e.getActionCommand().equals(MainActionCommands.EDIT_ACQUISITION_METHOD_COMMAND.getName()))
			saveAcquisitionMethodData();

		if(command.equals(MainActionCommands.DELETE_ACQUISITION_METHOD_COMMAND.getName())) {
			
			if(methodTable.getSelectedMethod() == null)
				return;
			
			reauthenticateAdminCommand(MainActionCommands.DELETE_ACQUISITION_METHOD_COMMAND.getName());
		}
			
		if(command.equals(MainActionCommands.DOWNLOAD_ACQUISITION_METHOD_COMMAND.getName()))
			downloadAcquisitionMethodFile();

		if(command.equals(MainActionCommands.LINK_ACQUISITION_METHOD_TO_EXPERIMENT_COMMAND.getName()))
			linkAcquisitionMethodToExperiment();

	}

	private void linkAcquisitionMethodToExperiment() {

		DataAcquisitionMethod method = methodTable.getSelectedMethod();
		LIMSExperiment experiment = idTrackerLimsManager.getSelectedExperiment();

		if(method == null || experiment == null)
			return;


	}

	private void saveAcquisitionMethodData() {

		Collection<String>errors = acquisitionMethodEditorDialog.validateMethodData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), acquisitionMethodEditorDialog);
			return;
		}
		DataAcquisitionMethod selectedMethod = acquisitionMethodEditorDialog.getMethod();
		DockableAcquisitionMethodDataPanel methodData = acquisitionMethodEditorDialog.getDataPanel();
		if(selectedMethod == null) {

			selectedMethod = new DataAcquisitionMethod(
					null,
					methodData.getMethodName(),
					methodData.getMethodDescription(),
					MRC2ToolBoxCore.getIdTrackerUser(),
					new Date());

			selectedMethod.setPolarity(methodData.getMethodPolarity());
			selectedMethod.setMsType(methodData.getMethodMsType());
			selectedMethod.setColumn(methodData.getColumn());
			selectedMethod.setIonizationType(methodData.getIonizationType());
			selectedMethod.setMassAnalyzerType(methodData.getMassAnalyzerType());
			selectedMethod.setSeparationType(methodData.getChromatographicSeparationType());
			selectedMethod.setSoftware(methodData.getSoftware());		
			try {
				AcquisitionMethodUtils.addNewAcquisitionMethod(selectedMethod, methodData.getMethodFile());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			IDTDataCash.getAcquisitionMethods().add(selectedMethod);
		}
		else {
			if(methodData.getMethodFile() != null) {

				String yesNoQuestion = "Do you want to replace existing method file?";
				if (MessageDialog.showChoiceWithWarningMsg(yesNoQuestion,
						acquisitionMethodEditorDialog) == JOptionPane.NO_OPTION)
					return;
			}
			selectedMethod.setName(methodData.getMethodName());
			selectedMethod.setDescription(methodData.getMethodDescription());
			selectedMethod.setPolarity(methodData.getMethodPolarity());
			selectedMethod.setMsType(methodData.getMethodMsType());
			selectedMethod.setColumn(methodData.getColumn());
			selectedMethod.setIonizationType(methodData.getIonizationType());
			selectedMethod.setMassAnalyzerType(methodData.getMassAnalyzerType());
			selectedMethod.setSoftware(methodData.getSoftware());
			try {
				AcquisitionMethodUtils.updateAcquisitionMethod(selectedMethod, methodData.getMethodFile());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		loadAcquisitionMethods();
		acquisitionMethodEditorDialog.dispose();
	}

	public void loadAcquisitionMethods() {
		methodTable.setTableModelFromAcquisitionMethods(IDTDataCash.getAcquisitionMethods());
	}

	private void deleteAcquisitionMethod() {
		
		DataAcquisitionMethod selectedMethod = methodTable.getSelectedMethod();
		if(selectedMethod == null)
			return;

		if(!IDTUtils.isSuperUser(this.getContentPane()))
			return;
		
		String yesNoQuestion = "Do you want to delete method \"" + selectedMethod.getName() + "\"?";
		int result = MessageDialog.showChoiceWithWarningMsg(yesNoQuestion, this.getContentPane());
		if(result == JOptionPane.YES_OPTION) {

			try {
				AcquisitionMethodUtils.deleteAcquisitionMethod(selectedMethod);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			IDTDataCash.refreshAcquisitionMethodList();
			loadAcquisitionMethods();
		}
	}

	private void showAcquisitionMethodEditor(DataAcquisitionMethod method) {

		acquisitionMethodEditorDialog = new AcquisitionMethodExtendedEditorDialog(method, this);
		acquisitionMethodEditorDialog.setLocationRelativeTo(idTrackerLimsManager.getContentPane());
		acquisitionMethodEditorDialog.setVisible(true);
	}

	private void downloadAcquisitionMethodFile() {

		DataAcquisitionMethod selectedMethod = methodTable.getSelectedMethod();
		if(selectedMethod == null)
			return;
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Save method \"" + selectedMethod.getName() + "\" to local drive");
		fc.setMultiSelectionEnabled(false);
		fc.setAllowOverwrite(true);
		fc.setSaveButtonText("Select destination folder");
		
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File destination = fc.getSelectedFile();
			baseDirectory = destination;
			try {
				AcquisitionMethodUtils.getAcquisitionMethodFile(selectedMethod, destination);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			savePreferences();
		}
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =  new File(preferences.get(BASE_DIRECTORY, MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

	public synchronized void clearPanel() {
		methodTable.clearTable();
	}

	@Override
	protected void executeAdminCommand(String command) {

		if(command.equals(MainActionCommands.DELETE_ACQUISITION_METHOD_COMMAND.getName()))
			deleteAcquisitionMethod();
	}
}




















