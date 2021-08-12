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

package edu.umich.med.mrc2.datoolbox.gui.adducts.exchange;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.AdductExchange;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class DockableAdductExchangeManager extends DefaultSingleCDockable implements ActionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("editExchange", 16);
	private AdductExchangeTable adductExchangeTable;
	private AdductExchangeEditorToolbar toolbar;
	private AdductExchange activeExchange;
	private JScrollPane scrollPane;
	private ExchangeDataEditorDialog exchangeDataEditorDialog;

	public DockableAdductExchangeManager() {

		super("DockableAdductExchangeManager", componentIcon, "Adduct exchange manager", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));
		toolbar = new AdductExchangeEditorToolbar(this);
		add(toolbar, BorderLayout.NORTH);

		adductExchangeTable = new AdductExchangeTable();
		scrollPane = new JScrollPane(adductExchangeTable);
		add(scrollPane, BorderLayout.CENTER);

		exchangeDataEditorDialog = new ExchangeDataEditorDialog(this);
		adductExchangeTable
			.setTableModelFromAdductExchangeList(AdductManager.getAdductExchangeList());
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.NEW_EXCHANGE_COMMAND.getName()))
			initNewExchangeDialog();

		if (command.equals(MainActionCommands.EDIT_EXCHANGE_COMMAND.getName()))
			initEditExchangeDialog();

		if (command.equals(MainActionCommands.DELETE_EXCHANGE_COMMAND.getName()))
			deleteSelectedExchange();

		if (command.equals(MainActionCommands.EXPORT_EXCHANGE_LIST_COMMAND.getName()))
			exportExchangeListToFile();

		if (command.equals(MainActionCommands.IMPORT_EXCHANGE_LIST_COMMAND.getName()))
			importExchangeListFromFile();

		if (command.equals(MainActionCommands.SAVE_EXCHANGE_DATA_COMMAND.getName())) {

			//  Save new
			if (activeExchange == null)
				createNewExchange();
			else // Edit existing
				editSelectedExchange();
		}
	}

	private void initNewExchangeDialog() {

		activeExchange = null;

		// TODO reset editor data
		exchangeDataEditorDialog.setTitle("Add adduct exchange");
		exchangeDataEditorDialog.setIconImage(((ImageIcon) ExchangeDataEditorDialog.newExchangeIcon).getImage());
		exchangeDataEditorDialog.loadExchange(activeExchange);
		exchangeDataEditorDialog.setLocationRelativeTo(this.getContentPane());
		exchangeDataEditorDialog.setVisible(true);
	}

	private void initEditExchangeDialog() {

		activeExchange = adductExchangeTable.getSelectedExchange();
		if(activeExchange == null)
			return;

		// TODO set editor data and show editor
		exchangeDataEditorDialog.setTitle("Edit adduct exchange");
		exchangeDataEditorDialog.setIconImage(((ImageIcon) ExchangeDataEditorDialog.editExchangeIcon).getImage());
		exchangeDataEditorDialog.loadExchange(activeExchange);
		exchangeDataEditorDialog.setLocationRelativeTo(this.getContentPane());
		exchangeDataEditorDialog.setVisible(true);
	}

	private void createNewExchange() {

		Collection<String>errors = validateExchange();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), exchangeDataEditorDialog);
			return;
		}
		AdductExchange newExchange = new AdductExchange(
				null,
				exchangeDataEditorDialog.getComingAdduct(),
				exchangeDataEditorDialog.getLeavingAdduct());
		AdductManager.addAdductExchange(newExchange);
		adductExchangeTable
			.setTableModelFromAdductExchangeList(AdductManager.getAdductExchangeList());
		adductExchangeTable.selectExchange(newExchange);
		adductExchangeTable.scrollToSelected();
		exchangeDataEditorDialog.setVisible(false);
	}

	private void editSelectedExchange() {

		Collection<String>errors = validateExchange();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), exchangeDataEditorDialog);
			return;
		}
		AdductExchange updated = new AdductExchange(
				null,
				exchangeDataEditorDialog.getComingAdduct(),
				exchangeDataEditorDialog.getLeavingAdduct());
		AdductManager.updateAdductExchange(
				exchangeDataEditorDialog.getActiveExchange(),
				updated);
		adductExchangeTable
			.setTableModelFromAdductExchangeList(AdductManager.getAdductExchangeList());
		adductExchangeTable.selectExchange(exchangeDataEditorDialog.getActiveExchange());
		adductExchangeTable.scrollToSelected();
		exchangeDataEditorDialog.setVisible(false);
	}

	private Collection<String> validateExchange() {

		Collection<String>errors = new ArrayList<String>();
		AdductExchange exchange = exchangeDataEditorDialog.getActiveExchange();
		Adduct leaving = exchangeDataEditorDialog.getLeavingAdduct();
		Adduct coming = exchangeDataEditorDialog.getComingAdduct();

		if(leaving == null)
			errors.add("Leaving aduct should be specified.");

		if(coming == null)
			errors.add("Incoming aduct should be specified.");

		if(leaving != null && coming != null) {
			
			if(leaving.equals(coming))
				errors.add("Incoming and leaving aducts should be different.");
			
			//	New exchange
			if(exchange == null) {

				AdductExchange newExchange = new AdductExchange(null, coming, leaving);
				if(AdductManager.adductExchangeExists(newExchange))
					errors.add("Specified exchange ( - [" + leaving.getName() +"] + [" + coming.getName() +"]) already exists");
			}
			else {	//	TODO
				if(exchange.getLeavingAdduct().equals(leaving)  && exchange.getComingAdduct().equals(coming)) {
					errors.add("No changes made.");
				}
				else {					
					if(AdductManager.adductExchangeExists(exchange.getId(), coming, leaving))
						errors.add("Another exchange ( - [" + leaving.getName() +"] + [" + coming.getName() +"]) already exists");
				}
			}
		}
		return errors;
	}

	private void deleteSelectedExchange() {

		activeExchange = adductExchangeTable.getSelectedExchange();
		if(activeExchange == null)
			return;

		if(!IDTUtils.isSuperUser(this.getContentPane()))
			return;
		
		int selectedValue = MessageDialog.showChoiceWithWarningMsg("Delete selected exchange?", this.getContentPane());
		if (selectedValue == JOptionPane.YES_OPTION) {

			try {
				AdductManager.deleteAdductExchange(activeExchange);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			adductExchangeTable
				.setTableModelFromAdductExchangeList(AdductManager.getAdductExchangeList());
		}
	}

	private void exportExchangeListToFile() {
		// TODO Auto-generated method stub

	}

	private void importExchangeListFromFile() {
		// TODO Auto-generated method stub

	}
}












