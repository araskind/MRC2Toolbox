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
import java.util.List;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.BinnerNeutralMassDifference;
import edu.umich.med.mrc2.datoolbox.data.CompositeAdduct;
import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.data.enums.ModificationType;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class CompositeAdductComponentsTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3281641294002008063L;
	public static final String TYPE_COLUMN = "Type";
	public static final String CHEM_MOD_COLUMN = "Name";
	public static final String DESCRIPTION_COLUMN = "Description";
	public static final String MASS_CORRECTION_COLUMN = '\u0394' + " mass";

	public CompositeAdductComponentsTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(TYPE_COLUMN, String.class, false),
			new ColumnContext(CHEM_MOD_COLUMN, SimpleAdduct.class, false),
			new ColumnContext(DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(MASS_CORRECTION_COLUMN, Double.class, false),
		};
	}

	public void setTableModelFromCompositeAdduct(CompositeAdduct composite) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		
		List<SimpleAdduct>losses = composite.getNeutralLosses().stream().
				sorted(AdductManager.adductTypeNameSorter).
				collect(Collectors.toList());
		
		for (SimpleAdduct ad : losses) {

			Object[] obj = {

				ad.getModificationType().getName(),
				ad,
				ad.getDescription(),
				ad.getMassCorrection(),
			};
			rowData.add(obj);
		}
		List<SimpleAdduct>repeats = composite.getNeutralAdducts().stream().
				sorted(AdductManager.adductTypeNameSorter).
				collect(Collectors.toList());
		
		for (SimpleAdduct ad : repeats) {

			Object[] obj = {

				ad.getModificationType().getName(),
				ad,
				ad.getDescription(),
				ad.getMassCorrection(),
			};
			rowData.add(obj);
		}
		addRows(rowData);
	}
	
	public void setTableModelFromAdductList(Collection<SimpleAdduct> adducts) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		
		List<SimpleAdduct>losses = adducts.stream().
				filter(a -> a.getModificationType().equals(ModificationType.LOSS)).
				sorted(AdductManager.adductTypeNameSorter).
				collect(Collectors.toList());
		
		for (SimpleAdduct ad : losses) {

			Object[] obj = {

				ad.getModificationType().getName(),
				ad,
				ad.getDescription(),
				ad.getMassCorrection(),
			};
			rowData.add(obj);
		}
		List<SimpleAdduct>repeats = adducts.stream().
				filter(a -> a.getModificationType().equals(ModificationType.REPEAT)).
				sorted(AdductManager.adductTypeNameSorter).
				collect(Collectors.toList());
		
		for (SimpleAdduct ad : repeats) {

			Object[] obj = {

				ad.getModificationType().getName(),
				ad,
				ad.getDescription(),
				ad.getMassCorrection(),
			};
			rowData.add(obj);
		}
		addRows(rowData);
	}

	public void setTableModelFromBinnerNeutralMassDifference(BinnerNeutralMassDifference binnerNeutralMassDifference) {

		setRowCount(0);
		if(binnerNeutralMassDifference == null)
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		
		for (SimpleAdduct ad : binnerNeutralMassDifference.getNeutralAdducts()) {

			Object[] obj = {

				ad.getModificationType().getName(),
				ad,
				ad.getDescription(),
				ad.getMassCorrection(),
			};
			rowData.add(obj);
		}
		addRows(rowData);
	}
}
