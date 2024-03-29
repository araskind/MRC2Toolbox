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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.stock.lookup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSBioSpecies;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class SpeciesTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -7585520467989443675L;
	public static final String SPECIES_COLUMN = "Species name";
	public static final String TAX_ID_COLUMN = "NCBI Taxonomy ID";
	public static final String NAME_CLASS_COLUMN = "Name class";

	public SpeciesTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(TAX_ID_COLUMN, TAX_ID_COLUMN, LIMSBioSpecies.class, false),
			new ColumnContext(SPECIES_COLUMN, SPECIES_COLUMN, String.class, false),
		};
	}

	public void setModelFromSpeciesList(Collection<LIMSBioSpecies>speciesList) {

		setRowCount(0);
		if(speciesList == null || speciesList.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(LIMSBioSpecies species : speciesList) {

			Object[] obj = {
				species,
				species.getSpeciesPrimaryName(),
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
