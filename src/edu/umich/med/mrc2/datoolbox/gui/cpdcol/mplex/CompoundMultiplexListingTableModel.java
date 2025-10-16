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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.mplex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixture;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class CompoundMultiplexListingTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3707538261469467980L;


	public static final String ID_COLUMN = "ID";
	public static final String NAME_COLUMN = "Name";
	public static final String SIZE_COLUMN = "#Components";

	public CompoundMultiplexListingTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ID_COLUMN, "Multiplex mixture ID", String.class, false),
			new ColumnContext(NAME_COLUMN, NAME_COLUMN, CompoundMultiplexMixture.class, false),
			new ColumnContext(SIZE_COLUMN, "Number of compounds in the mixture", Integer.class, false),
		};
	}

	public void setTableModelFromCompoundMultiplexMixtureCollection(
			Collection<CompoundMultiplexMixture> multiplexes) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();

		for(CompoundMultiplexMixture multiplex : multiplexes){

			Object[] obj = {
					multiplex.getId(),
					multiplex,
					multiplex.getComponents().size(),
				};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}



































