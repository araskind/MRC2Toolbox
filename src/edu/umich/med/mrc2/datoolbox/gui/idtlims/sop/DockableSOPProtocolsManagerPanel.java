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
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProtocol;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DockableSOPProtocolsManagerPanel  extends DefaultSingleCDockable
	implements ActionListener, BackedByPreferences{

	private ProtocolManagerToolbar toolbar;
	private static final Icon componentIcon = GuiUtils.getIcon("editSop", 16);
	private ProtocolTable protocolTable;
	private ProtocolEditorDialog protocolEditorDialog;
	private IDTrackerLimsManagerPanel idTrackerLimsManager;

	private Preferences preferences;
	private File baseDirectory;
	private ImprovedFileChooser chooser;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.cefanalyzer.gui.ProtocolManagerPanel";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";

	public DockableSOPProtocolsManagerPanel(IDTrackerLimsManagerPanel idTrackerLimsManager) {

		super("DockableSOPProtocolsManagerPanel", componentIcon, "SOP protocols", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

		this.idTrackerLimsManager = idTrackerLimsManager;

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
		loadPreferences();
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

	public void loadProtocolData() {
		protocolTable.setTableModelFromProtocols(IDTDataCash.getProtocols());
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.ADD_SOP_PROTOCOL_DIALOG_COMMAND.getName()))
			newProtocolDialog();

		if(e.getActionCommand().equals(MainActionCommands.ADD_SOP_PROTOCOL_COMMAND.getName()) ||
				e.getActionCommand().equals(MainActionCommands.EDIT_SOP_PROTOCOL_COMMAND.getName()))
			saveSopProtocolData();

		if(e.getActionCommand().equals(MainActionCommands.EDIT_SOP_PROTOCOL_DIALOG_COMMAND.getName()))
			editProtocolDialog();

		if(e.getActionCommand().equals(MainActionCommands.DELETE_SOP_PROTOCOL_COMMAND.getName()))
			deleteProtocol();

		if(e.getActionCommand().equals(MainActionCommands.DOWNLOAD_SOP_PROTOCOL_COMMAND.getName()))
			showMethodSaveDialog();

		if(e.getSource().equals(chooser) && e.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))
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
	
	private void showMethodSaveDialog() {

		LIMSProtocol protocol = protocolTable.getSelectedProtocol();

		if(protocol == null)
			return;

		if(chooser == null)
			initChooser();

		chooser.setDialogTitle("Save SOP protocol \"" + protocol.getSopName() + "\" to local drive");
		chooser.setSelectedFile(null);
		chooser.showSaveDialog(this.getContentPane());
	}

	private void downloadProtocol() {

		LIMSProtocol protocol = protocolTable.getSelectedProtocol();
		File destination = chooser.getSelectedFile();
		baseDirectory = destination;
		try {
			IDTUtils.getSopProtocolFile(protocol, destination);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		savePreferences();
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
			IDTDataCash.refreshProtocols();
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
		IDTDataCash.refreshProtocols();
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
}































