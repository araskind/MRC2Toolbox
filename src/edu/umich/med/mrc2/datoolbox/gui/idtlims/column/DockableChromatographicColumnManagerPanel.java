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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.column;

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

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;
import edu.umich.med.mrc2.datoolbox.database.idt.AcquisitionMethodUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.AbstractIDTrackerLimsPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class DockableChromatographicColumnManagerPanel extends AbstractIDTrackerLimsPanel {

	
	private static final Icon componentIcon = GuiUtils.getIcon("editColumn", 16);
	private static final Icon editColumnIcon = GuiUtils.getIcon("editColumn", 24);
	private static final Icon addColumnIcon = GuiUtils.getIcon("addColumn", 24);
	private static final Icon deleteColumnIcon = GuiUtils.getIcon("deleteColumn", 24);
	
	private ChromatographicColumnManagerToolbar toolbar;
	private ChromatographicColumnTable cromatographicColumnTable;
	private ChromatographicColumnEditorDialog cromatographicColumnEditorDialog;

	public DockableChromatographicColumnManagerPanel(IDTrackerLimsManagerPanel idTrackerLimsManager) {

		super(idTrackerLimsManager, 
			"DockableChromatographicColumnManagerPanel", componentIcon, 
			"Chromatographic columns", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));

		toolbar = new ChromatographicColumnManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		cromatographicColumnTable = new ChromatographicColumnTable();
		JScrollPane designScrollPane = new JScrollPane(cromatographicColumnTable);
		getContentPane().add(designScrollPane, BorderLayout.CENTER);
		
		cromatographicColumnTable.addMouseListener(
				new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {

						if (e.getClickCount() == 2) 
							editColumnDialog();						
					}
				});
		initActions();
	}

	@Override
	protected void initActions() {
		// TODO Auto-generated method stub
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ADD_CHROMATOGRAPHIC_COLUMN_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_CHROMATOGRAPHIC_COLUMN_DIALOG_COMMAND.getName(), 
				addColumnIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EDIT_CHROMATOGRAPHIC_COLUMN_DIALOG_COMMAND.getName(),
				MainActionCommands.EDIT_CHROMATOGRAPHIC_COLUMN_DIALOG_COMMAND.getName(), 
				editColumnIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_CHROMATOGRAPHIC_COLUMN_COMMAND.getName(),
				MainActionCommands.DELETE_CHROMATOGRAPHIC_COLUMN_COMMAND.getName(), 
				deleteColumnIcon, this));
	}
	
	public void loadColumnData() {
		cromatographicColumnTable.setTableModelFromColumns(IDTDataCash.getChromatographicColumns());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(!isConnected())
			return;

		if(e.getActionCommand().equals(MainActionCommands.ADD_CHROMATOGRAPHIC_COLUMN_DIALOG_COMMAND.getName()))
			addColumnDialog();

		if(e.getActionCommand().equals(MainActionCommands.ADD_CHROMATOGRAPHIC_COLUMN_COMMAND.getName()) ||
				e.getActionCommand().equals(MainActionCommands.EDIT_CHROMATOGRAPHIC_COLUMN_COMMAND.getName()))
			saveCromatographicColumnData();

		if(e.getActionCommand().equals(MainActionCommands.EDIT_CHROMATOGRAPHIC_COLUMN_DIALOG_COMMAND.getName()))
			editColumnDialog();

		if(e.getActionCommand().equals(MainActionCommands.DELETE_CHROMATOGRAPHIC_COLUMN_COMMAND.getName()))
			deleteCromatographicColumn();
	}
	
	private void addColumnDialog() {
		
		cromatographicColumnEditorDialog = new ChromatographicColumnEditorDialog(null, this);
		cromatographicColumnEditorDialog.setLocationRelativeTo(this.getContentPane());
		cromatographicColumnEditorDialog.setVisible(true);
	}
	
	private void editColumnDialog() {
		
		LIMSChromatographicColumn column = cromatographicColumnTable.getSelectedChromatographicColumn();
		if(column == null)
			return;

		cromatographicColumnEditorDialog = new ChromatographicColumnEditorDialog(column, this);
		cromatographicColumnEditorDialog.setLocationRelativeTo(this.getContentPane());
		cromatographicColumnEditorDialog.setVisible(true);
	}

	private void deleteCromatographicColumn() {

		LIMSChromatographicColumn column = cromatographicColumnTable.getSelectedChromatographicColumn();
		if(column == null)
			return;
	
		if(!IDTUtils.isSuperUser(this.getContentPane()))
			return;
		
		int result = MessageDialog.showChoiceWithWarningMsg(
				"Do you really want to delete cromatographic column \"" + column.getColumnName() + "\"?\n" +
				"All associated data will be purged from the database!",
				this.getContentPane());

		if(result == JOptionPane.YES_OPTION) {

			try {
				AcquisitionMethodUtils.deleteChromatographicColumn(column);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			IDTDataCash.refreshChromatographicColumnList();
			loadColumnData();
		}
	}

	private void saveCromatographicColumnData() {

		ArrayList<String>errors = new ArrayList<String>();
		if(cromatographicColumnEditorDialog.getColumnName().isEmpty())
			errors.add("Column name can not be empty.");

		if(cromatographicColumnEditorDialog.getChromatographicSeparationType() == null)
			errors.add("Chromatographic separation type not selected.");

		if(cromatographicColumnEditorDialog.getManufacturer() == null)
			errors.add("Manufacturer not selected.");

		if(cromatographicColumnEditorDialog.getCatalogNumber().isEmpty())
			errors.add("Catalog number can not be empty.");

		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), cromatographicColumnEditorDialog);
			return;
		}
		LIMSChromatographicColumn column =
				new LIMSChromatographicColumn(
						null,
						cromatographicColumnEditorDialog.getColumnName(),
						cromatographicColumnEditorDialog.getColumnChemistry(),
						cromatographicColumnEditorDialog.getCatalogNumber());

		column.setSeparationType(cromatographicColumnEditorDialog.getChromatographicSeparationType());
		column.setManufacturer(cromatographicColumnEditorDialog.getManufacturer());

		//	Create new column
		if(cromatographicColumnEditorDialog.getColumn() == null) {

			try {
				AcquisitionMethodUtils.addNewChromatographicColumn(column);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {	//	Update existing column
			column.setColumnId(cromatographicColumnEditorDialog.getColumn().getColumnId());
			try {
				AcquisitionMethodUtils.updateChromatographicColumn(column);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		IDTDataCash.refreshChromatographicColumnList();;
		loadColumnData();
		cromatographicColumnEditorDialog.dispose();
	}

	public synchronized void clearPanel() {
		cromatographicColumnTable.clearTable();
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
}































