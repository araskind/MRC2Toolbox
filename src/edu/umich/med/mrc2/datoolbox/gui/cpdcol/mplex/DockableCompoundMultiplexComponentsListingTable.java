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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.mplex;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixture;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixtureComponent;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableCompoundMultiplexComponentsListingTable 
		extends DefaultSingleCDockable implements ActionListener{

	private static final Icon componentIcon = GuiUtils.getIcon("compoundCollection", 16);
	private CompoundMultiplexComponentsListingTable multiplexComponentsListingTable;
	private EditMSReadyStructureDialog editMSReadyStructureDialog;

	public DockableCompoundMultiplexComponentsListingTable() {

		super("DockableCompoundMultiplexComponentsListingTable", componentIcon, 
				"Compound multiplex mixture components", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0,0));

		multiplexComponentsListingTable = 
				new CompoundMultiplexComponentsListingTable();
		MultiplexListingTablePopupMenu popup = 
				new MultiplexListingTablePopupMenu(multiplexComponentsListingTable, this);
		multiplexComponentsListingTable.addTablePopupMenu(popup);

		add(new JScrollPane(multiplexComponentsListingTable));
	}

	public void setTableModelFromCompoundMultiplexMixtureComponents(
			Collection<CompoundMultiplexMixtureComponent>components) {
		multiplexComponentsListingTable.
			setTableModelFromCompoundMultiplexMixtureComponents(components);
	}
	
	public void setTableModelFromCompoundMultiplexMixtures(
			Collection<CompoundMultiplexMixture> mixtures) {
		multiplexComponentsListingTable.
			setTableModelFromCompoundMultiplexMixtures(mixtures);
	}

	/**
	 * @return the compoundDatabaseTable
	 */
	public CompoundMultiplexComponentsListingTable getTable() {
		return multiplexComponentsListingTable;
	}

	public synchronized void clearTable() {
		multiplexComponentsListingTable.clearTable();
	}

	public CompoundMultiplexMixtureComponent getSelectedCompoundMultiplexMixtureComponent() {
		return multiplexComponentsListingTable.getSelectedCompoundMultiplexMixtureComponent();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.EDIT_COMPOUND_MS_READY_STRUCTURE_COMMAND.getName()))
			showStructureEditDialog();
		
		if(command.equals(MainActionCommands.SAVE_COMPOUND_MS_READY_STRUCTURE_COMMAND.getName()))
			saveMsReadyStructure();
	}

	private void showStructureEditDialog() {
		// TODO Auto-generated method stub
		CompoundMultiplexMixtureComponent selected = 
				getSelectedCompoundMultiplexMixtureComponent();
		if(selected == null)
			return;
		
		editMSReadyStructureDialog = 
				new EditMSReadyStructureDialog (selected, this);
		editMSReadyStructureDialog.setLocationRelativeTo(this.getContentPane());
		editMSReadyStructureDialog.setVisible(true);
	}

	private void saveMsReadyStructure() {
		// TODO Auto-generated method stub
		
		
		editMSReadyStructureDialog.dispose();
	}
}












