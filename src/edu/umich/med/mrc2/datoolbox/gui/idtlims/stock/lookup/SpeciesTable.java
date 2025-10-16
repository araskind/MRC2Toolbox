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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.stock.lookup;

import java.util.Collection;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.BioSpeciesComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.BioSpeciesFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSBioSpecies;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.BioSpeciesRenderer;

public class SpeciesTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1837501767078275796L;

	public SpeciesTable() {
		super();
		model = new SpeciesTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<SpeciesTableModel>((SpeciesTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(SpeciesTableModel.TAX_ID_COLUMN),
				new BioSpeciesComparator(SortProperty.ID));

		setDefaultRenderer(LIMSBioSpecies.class, new BioSpeciesRenderer(SortProperty.ID));
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(LIMSBioSpecies.class, new BioSpeciesFormat(SortProperty.ID));
		thf.getParserModel().setComparator(LIMSBioSpecies.class, new BioSpeciesComparator(SortProperty.ID));
		finalizeLayout();
	}

	public void setModelFromSpeciesList(Collection<LIMSBioSpecies>speciesList) {

		thf.setTable(null);
		((SpeciesTableModel)model).setModelFromSpeciesList(speciesList);
		thf.setTable(this);
		adjustColumns();
	}

	public LIMSBioSpecies getSelectedSpecies() {

		int row = getSelectedRow(); 
		if(row == -1)
			return null;

		return (LIMSBioSpecies)model.getValueAt(convertRowIndexToModel(row),
			model.getColumnIndex(SpeciesTableModel.TAX_ID_COLUMN));
	}
}
