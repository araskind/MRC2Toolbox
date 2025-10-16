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

package edu.umich.med.mrc2.datoolbox.gui.mstools.mfgen;

import java.awt.event.ActionEvent;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import org.openscience.cdk.interfaces.IMolecularFormulaSet;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;

public class FormulaResultsTable extends BasicTable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1552278354304005851L;

	public FormulaResultsTable() {

		model = new FormulaResultsTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<FormulaResultsTableModel>(
				(FormulaResultsTableModel)model);
		setRowSorter(rowSorter);
		
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		columnModel.getColumnById(FormulaResultsTableModel.FORMULA_MASS_COLUMN)
			.setCellRenderer(mzRenderer);
		columnModel.getColumnById(FormulaResultsTableModel.ADDUCT_MASS_COLUMN)
			.setCellRenderer(mzRenderer);
		columnModel.getColumnById(FormulaResultsTableModel.EXPECTED_NEUTRAL_MASS_COLUMN)
			.setCellRenderer(mzRenderer);
		columnModel.getColumnById(FormulaResultsTableModel.PPM_ERROR_COLUMN)
			.setCellRenderer(ppmRenderer);
		columnModel.getColumnById(FormulaResultsTableModel.ABS_PPM_ERROR_COLUMN)
			.setCellRenderer(ppmRenderer);

		addTablePopupMenu(new FormulaTablePopupMenu(this, this));

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromFormulaGeneratorResults(IMolecularFormulaSet formulas, double neutralMass, Adduct ad) {
		thf.setTable(null);
		((FormulaResultsTableModel)model).setFromFormulaGeneratorResults(formulas, neutralMass, ad);
		thf.setTable(this);
		adjustColumns();
	}
	
	public void actionPerformed(ActionEvent event) {
		
		super.actionPerformed(event);
		
		String command = event.getActionCommand();
		
		if(command.equals(MainActionCommands.SEARCH_FORMULA_AGAINST_DATABASE_COMMAND.getName()))
			searchSelectedFormulaAgainstDatabase();
		
		if(command.equals(MainActionCommands.SEARCH_FORMULA_AGAINST_LIBRARY_COMMAND.getName()))
			searchSelectedFormulaAgainstActiveLibrary();	
	}

	private void searchSelectedFormulaAgainstActiveLibrary() {
		// TODO Auto-generated method stub
		
	}

	private void searchSelectedFormulaAgainstDatabase() {
		// TODO Auto-generated method stub
		
	}
}



























