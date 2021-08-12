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

package edu.umich.med.mrc2.datoolbox.gui.adducts.adduct;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.BinnerNeutralMassDifference;
import edu.umich.med.mrc2.datoolbox.data.CompositeAdduct;
import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ChemicalModificationRenderer;

public class CompositeAdductComponentsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1511489263877797538L;
	private CompositeAdductComponentsTableModel model;

	public CompositeAdductComponentsTable() {

		super();
		model = new CompositeAdductComponentsTableModel();
		setModel(model);
		
		rowSorter = new TableRowSorter<CompositeAdductComponentsTableModel>(model);
		setRowSorter(rowSorter);

		chmodRenderer = new ChemicalModificationRenderer();
		setDefaultRenderer(Adduct.class, chmodRenderer);

		columnModel.getColumnById(CompositeAdductComponentsTableModel.MASS_CORRECTION_COLUMN)
				.setCellRenderer(mzRenderer);

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}
	
	public SimpleAdduct getSelectedModification() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (SimpleAdduct)getValueAt(row, 
				getColumnIndex(CompositeAdductComponentsTableModel.CHEM_MOD_COLUMN));		
	}

	public void setTableModelFromCompositAdduct(CompositeAdduct composite) {
		model.setTableModelFromCompositeAdduct(composite);
		tca.adjustColumns();
	}
	
	public Collection<SimpleAdduct>getAllAdducts(){
		
		Collection<SimpleAdduct>components = new ArrayList<SimpleAdduct>();
		int col = model.getColumnIndex(CompositeAdductComponentsTableModel.CHEM_MOD_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			SimpleAdduct comp = (SimpleAdduct)model.getValueAt(i, col);
			components.add(comp);
		}
		return components;
	}

	public void addModification(SimpleAdduct mod) {

		Collection<SimpleAdduct>current = getAllAdducts();
		current.add(mod);
		model.setTableModelFromAdductList(current);
	}
	
	public void removeModification(SimpleAdduct mod) {

		Collection<SimpleAdduct>current = getAllAdducts();
		current.remove(mod);
		model.setTableModelFromAdductList(current);
	}

	public void setTableModelFromBinnerNeutralMassDifference(BinnerNeutralMassDifference binnerNeutralMassDifference) {
		model.setTableModelFromBinnerNeutralMassDifference(binnerNeutralMassDifference);
		tca.adjustColumns();
	}
}





