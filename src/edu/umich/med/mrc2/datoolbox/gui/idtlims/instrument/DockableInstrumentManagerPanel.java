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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.instrument;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.database.idt.AcquisitionMethodUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.AbstractIDTrackerLimsPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class DockableInstrumentManagerPanel extends AbstractIDTrackerLimsPanel {

	private static final Icon componentIcon = GuiUtils.getIcon("editInstrument", 16);
	private static final Icon addInstrumentIcon = GuiUtils.getIcon("addInstrument", 24);
	private static final Icon editInstrumentIcon = GuiUtils.getIcon("editInstrument", 24);	
	private static final Icon deleteInstrumentIcon = GuiUtils.getIcon("deleteInstrument", 24);

	private InstrunmentManagerToolbar toolbar;
	private InstrumentTable instrumentTable;
	private InstrumentEditorDialog instrumentEditorDialog;

	public DockableInstrumentManagerPanel(IDTrackerLimsManagerPanel idTrackerLimsManager) {

		super(idTrackerLimsManager, "DockableInstrumentManagerPanel", 
				componentIcon, "Instruments", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

		toolbar = new InstrunmentManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		instrumentTable = new InstrumentTable();
		instrumentTable.addTablePopupMenu(
				new BasicTablePopupMenu(null, instrumentTable, true));
		JScrollPane designScrollPane = new JScrollPane(instrumentTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
		instrumentTable.addMouseListener(

				new MouseAdapter() {

					public void mouseClicked(MouseEvent e) {

						if (e.getClickCount() == 2) {

							LIMSInstrument selectedInstrument = instrumentTable.getSelectedInstrument();
							if(selectedInstrument != null)
								showInstrumentEditor(selectedInstrument);
						}											
					}
				});
		initActions();
	}

	@Override
	protected void initActions() {

		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ADD_INSTRUMENT_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_INSTRUMENT_DIALOG_COMMAND.getName(), 
				addInstrumentIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EDIT_INSTRUMENT_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_INSTRUMENT_DIALOG_COMMAND.getName(), 
				editInstrumentIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_INSTRUMENT_COMMAND.getName(),
				MainActionCommands.DELETE_INSTRUMENT_COMMAND.getName(), 
				deleteInstrumentIcon, this));
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(!isConnected())
			return;
		
		super.actionPerformed(e);
		
		String command = e.getActionCommand();

		if(command.equals(MainActionCommands.ADD_INSTRUMENT_DIALOG_COMMAND.getName()))
			showInstrumentEditor(null);

		if(command.equals(MainActionCommands.EDIT_INSTRUMENT_DIALOG_COMMAND.getName())) {

			LIMSInstrument selectedInstrument = instrumentTable.getSelectedInstrument();
			if(selectedInstrument != null)
				showInstrumentEditor(selectedInstrument);
		}
		if(e.getActionCommand().equals(MainActionCommands.ADD_INSTRUMENT_COMMAND.getName()) ||
				e.getActionCommand().equals(MainActionCommands.EDIT_INSTRUMENT_COMMAND.getName()))
			saveInstrumentData();

		if(command.equals(MainActionCommands.DELETE_INSTRUMENT_COMMAND.getName())) {

			if(instrumentTable.getSelectedInstrument() == null)
				return;
			
			reauthenticateAdminCommand(MainActionCommands.DELETE_INSTRUMENT_COMMAND.getName());
		}
	}
	
	private ArrayList<String> verifyInstrumentData() {
		
		ArrayList<String>errors = new ArrayList<String>();
		
		if(instrumentEditorDialog.getInstrumentName().isEmpty())
			errors.add("Instrument name can not be empty.");
		
		if(instrumentEditorDialog.getInstrumentModel().isEmpty())
			errors.add("Instrument model name can not be empty.");

		if(instrumentEditorDialog.getInstrumentManufacturer().isEmpty())
			errors.add("Instrument manufacturer can not be empty.");

		if(instrumentEditorDialog.getSerialNumber().isEmpty())
			errors.add("Instrument serial number can not be empty.");

		if(instrumentEditorDialog.getMassAnalyzerType() == null)
			errors.add("Mass analyzer type should be specified.");

		if(instrumentEditorDialog.getChromatographicSeparationType() == null)
			errors.add("Chromatographic separation type should be specified.");
		
		return errors;
	}

	private void saveInstrumentData() {

		ArrayList<String>errors = verifyInstrumentData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), instrumentEditorDialog);
			return;
		}
		LIMSInstrument selectedInstrument = instrumentEditorDialog.getInstrument();
		
		if(selectedInstrument == null)
			addNewInstrument();
		else {
			updateInstrumentData();
		}
		IDTDataCache.refreshInstrumentList();
		loadInstruments();
		instrumentEditorDialog.dispose();
	}

	private void updateInstrumentData() {
		
		LIMSInstrument instrument = instrumentEditorDialog.getInstrument();
		instrument.setInstrumentName(instrumentEditorDialog.getInstrumentName());
		instrument.setDescription(instrumentEditorDialog.getInstrumentDescription());
		instrument.setMassAnalyzerType(instrumentEditorDialog.getMassAnalyzerType());
		instrument.setChromatographicSeparationType(instrumentEditorDialog.getChromatographicSeparationType());
		instrument.setManufacturer(instrumentEditorDialog.getInstrumentManufacturer());
		instrument.setModel(instrumentEditorDialog.getInstrumentModel());
		instrument.setSerialNumber(instrumentEditorDialog.getSerialNumber());
		try {
			AcquisitionMethodUtils.updateInstrument(instrument);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	private void addNewInstrument() {
		
		LIMSInstrument newInstrument = new LIMSInstrument(
				instrumentEditorDialog.getInstrumentName(),
				instrumentEditorDialog.getInstrumentDescription(),
				instrumentEditorDialog.getMassAnalyzerType(),
				instrumentEditorDialog.getChromatographicSeparationType(),
				instrumentEditorDialog.getInstrumentManufacturer(),
				instrumentEditorDialog.getInstrumentModel(),
				instrumentEditorDialog.getSerialNumber());
		try {
			AcquisitionMethodUtils.addNewInstrument(newInstrument);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void loadInstruments() {
		instrumentTable.setTableModelFromInstrumentList(IDTDataCache.getInstrumentList());
	}

	private void deleteInstrument() {
		
		LIMSInstrument selectedInstrument = instrumentTable.getSelectedInstrument();
		if(selectedInstrument == null)
			return;
			
		if(!IDTUtils.isSuperUser(this.getContentPane()))
			return;

		String yesNoQuestion = "Do you want to delete method \"" + selectedInstrument.getInstrumentName() + "\"?\n"
				+ "All data genenerated on this instrument will be deleted as well!";
		int result = MessageDialog.showChoiceWithWarningMsg(yesNoQuestion, this.getContentPane());
		if(result == JOptionPane.YES_OPTION) {

			try {
				AcquisitionMethodUtils.deleteInstrument(selectedInstrument);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			IDTDataCache.refreshInstrumentList();
			loadInstruments();
		}
	}

	private void showInstrumentEditor(LIMSInstrument instrument) {

		instrumentEditorDialog = new InstrumentEditorDialog(instrument, this);
		instrumentEditorDialog.setLocationRelativeTo(this.getContentPane());
		instrumentEditorDialog.setVisible(true);
	}

	public synchronized void clearPanel() {
		instrumentTable.clearTable();
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadPreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void savePreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void executeAdminCommand(String command) {
		
		if(command.equals(MainActionCommands.DELETE_INSTRUMENT_COMMAND.getName()))
			deleteInstrument();		
	}
}




















