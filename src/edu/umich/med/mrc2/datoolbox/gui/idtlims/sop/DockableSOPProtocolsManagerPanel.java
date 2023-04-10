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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.sop;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProtocol;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.AbstractIDTrackerLimsPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DockableSOPProtocolsManagerPanel extends AbstractIDTrackerLimsPanel {

	private ProtocolManagerToolbar toolbar;
	private static final Icon componentIcon = GuiUtils.getIcon("editSop", 16);
	private static final Icon editProtocolIcon = GuiUtils.getIcon("editSop", 24);
	private static final Icon addProtocolIcon = GuiUtils.getIcon("addSop", 24);
	private static final Icon deleteProtocolIcon = GuiUtils.getIcon("deleteSop", 24);
	private static final Icon downloadProtocolIcon = GuiUtils.getIcon("downloadSop", 24);

	private ProtocolTable protocolTable;
	private ProtocolEditorDialog protocolEditorDialog;

	private Preferences preferences;
	private File baseDirectory;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.cefanalyzer.gui.ProtocolManagerPanel";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";

	public DockableSOPProtocolsManagerPanel(IDTrackerLimsManagerPanel idTrackerLimsManager) {

		super(idTrackerLimsManager, "DockableSOPProtocolsManagerPanel", 
				componentIcon, "SOP protocols", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

		toolbar = new ProtocolManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		protocolTable = new ProtocolTable();
		JScrollPane designScrollPane = new JScrollPane(protocolTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
		protocolTable.addMouseListener(

				new MouseAdapter() {

					public void mouseClicked(MouseEvent e) {

						if (e.getClickCount() == 2)
							editProtocolDialog();																
					}
				});
		initActions();
		loadPreferences();
	}

	@Override
	protected void initActions() {

		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ADD_SOP_PROTOCOL_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_SOP_PROTOCOL_DIALOG_COMMAND.getName(), 
				addProtocolIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EDIT_SOP_PROTOCOL_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_SOP_PROTOCOL_DIALOG_COMMAND.getName(), 
				editProtocolIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_SOP_PROTOCOL_COMMAND.getName(),
				MainActionCommands.DELETE_SOP_PROTOCOL_COMMAND.getName(), 
				deleteProtocolIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DOWNLOAD_SOP_PROTOCOL_COMMAND.getName(),
				MainActionCommands.DOWNLOAD_SOP_PROTOCOL_COMMAND.getName(), 
				downloadProtocolIcon, this));
	}

	public void loadProtocolData() {
		protocolTable.setTableModelFromProtocols(IDTDataCache.getProtocols());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(!isConnected())
			return;;

		super.actionPerformed(e);
		
		String command = e.getActionCommand();
			
		if(command.equals(MainActionCommands.ADD_SOP_PROTOCOL_DIALOG_COMMAND.getName()))
			newProtocolDialog();

		if(command.equals(MainActionCommands.ADD_SOP_PROTOCOL_COMMAND.getName()) ||
				command.equals(MainActionCommands.EDIT_SOP_PROTOCOL_COMMAND.getName()))
			saveSopProtocolData();

		if(command.equals(MainActionCommands.EDIT_SOP_PROTOCOL_DIALOG_COMMAND.getName()))
			editProtocolDialog();

		if(command.equals(MainActionCommands.DELETE_SOP_PROTOCOL_COMMAND.getName())) {

			if(protocolTable.getSelectedProtocol() == null)
				return;
			
			reauthenticateAdminCommand(
					MainActionCommands.DELETE_SOP_PROTOCOL_COMMAND.getName());
		}			
		if(command.equals(MainActionCommands.DOWNLOAD_SOP_PROTOCOL_COMMAND.getName()))
			downloadProtocol();
	}
	
	private void newProtocolDialog() {
		
		protocolEditorDialog = new ProtocolEditorDialog(null, this);
		protocolEditorDialog.setLocationRelativeTo(this.getContentPane());
		protocolEditorDialog.setVisible(true);
	}

	private void editProtocolDialog() {
		
		LIMSProtocol protocol = protocolTable.getSelectedProtocol();
		if(protocol == null)
			return;

		protocolEditorDialog = new ProtocolEditorDialog(protocol, this);
		protocolEditorDialog.setLocationRelativeTo(this.getContentPane());
		protocolEditorDialog.setVisible(true);
	}

	private void downloadProtocol() {

		LIMSProtocol protocol = protocolTable.getSelectedProtocol();
		if(protocol == null)
			return;
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Save SOP protocol \"" + protocol.getSopName() + "\" to local drive");
		fc.setMultiSelectionEnabled(false);
		fc.setAllowOverwrite(true);
		fc.setSaveButtonText("Select destination folder");
		
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File destination = fc.getSelectedFile();
			baseDirectory = destination;
			try {
				IDTUtils.getSopProtocolFile(protocol, destination);;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			savePreferences();
		}
	}

	private void deleteProtocol() {

		LIMSProtocol protocol = protocolTable.getSelectedProtocol();
		if(protocol == null)
			return;
	
		if(!IDTUtils.isSuperUser(this.getContentPane()))
			return;
		
		int result = MessageDialog.showChoiceWithWarningMsg(
				"Do you really want to delete SOP protocol \"" + protocol.getSopName() + "\"?\n" +
				"All associated data will be purged from the database!",
				this.getContentPane());

		if(result == JOptionPane.YES_OPTION) {

			try {
				IDTUtils.deleteProtocol(protocol);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			IDTDataCache.refreshProtocols();
			loadProtocolData();
		}
	}

	private void saveSopProtocolData() {

		ArrayList<String>errors = new ArrayList<String>();
		String version = null;
		LIMSProtocol protocol = protocolEditorDialog.getProtocol();
		if(protocolEditorDialog.getProtocolName().isEmpty())
			errors.add("Protocol name can not be empty.");

		if(protocol == null) {

			if(protocolEditorDialog.getSopCategory() == null)
				errors.add("Protocol category must be specified.");

			if(protocolEditorDialog.getSopFile() == null)
				errors.add("Protocol file must be specified.");
		}
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), protocolEditorDialog);
			return;
		}
		if(protocol == null) {

			version = DataPrefix.VERSION.getName() + StringUtils.leftPad("1", 3, '0');
			protocol = new LIMSProtocol(
					null,
					null,
					protocolEditorDialog.getProtocolName(),
					protocolEditorDialog.getProtocolDescription(),
					version,
					new Date(),
					MRC2ToolBoxCore.getIdTrackerUser());
			protocol.setSopCategory(protocolEditorDialog.getSopCategory());
			try {
				IDTUtils.addNewSop(protocol, protocolEditorDialog.getSopFile());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			protocol.setSopName(protocolEditorDialog.getProtocolName());
			protocol.setSopDescription(protocolEditorDialog.getProtocolDescription());

			//	Update only name and description
			if(protocolEditorDialog.getSopFile() == null) {

				try {
					IDTUtils.updateSopMetadata(protocol);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else { //	Add new version
				int currentVersion = Integer.parseInt(protocol.getSopVersion().replaceAll(DataPrefix.VERSION.getName() + "0+", ""));
				version = DataPrefix.VERSION.getName() + StringUtils.leftPad(Integer.toString(currentVersion + 1), 3, '0');
				LIMSProtocol newVervion = new LIMSProtocol(
						null,
						protocol.getSopGroup(),
						protocolEditorDialog.getProtocolName(),
						protocolEditorDialog.getProtocolDescription(),
						version,
						new Date(),
						MRC2ToolBoxCore.getIdTrackerUser());
				newVervion.setSopCategory(protocolEditorDialog.getSopCategory());
				try {
					IDTUtils.addNewSopVersion(newVervion, protocolEditorDialog.getSopFile());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		IDTDataCache.refreshProtocols();
		loadProtocolData();
		protocolEditorDialog.dispose();
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
		protocolTable.clearTable();
	}

	@Override
	protected void executeAdminCommand(String command) {
		
		if(command.equals(MainActionCommands.DELETE_SOP_PROTOCOL_COMMAND.getName()))
			deleteProtocol();		
	}
}































