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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.expdesign;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class ExperimentalGroupsTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3441207603128061315L;

	public static final String FACTOR_COLUMN = "Factor";
	public static final String LEVELS_COLUMN = "Levels";
	public static final String NUM_SAMPLES_COLUMN = "# of samples";

	public ExperimentalGroupsTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(FACTOR_COLUMN, "Experimental factor", ExperimentDesignFactor.class, false),
			new ColumnContext(LEVELS_COLUMN, "Included levels", String.class, false)
			// , new ColumnContext(NUM_SAMPLES_COLUMN, Integer.class, false)
		};
	}

	public void setModelFromDesignSubset(ExperimentDesignSubset activeDesignSubset) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		TreeMap<ExperimentDesignFactor, TreeSet<String>> levelFactorMap =
				new TreeMap<ExperimentDesignFactor, TreeSet<String>>();

		for (ExperimentDesignLevel l : activeDesignSubset.getDesignMap()) {

			if (!levelFactorMap.containsKey(l.getParentFactor()))
				levelFactorMap.put(l.getParentFactor(), new TreeSet<String>());

			levelFactorMap.get(l.getParentFactor()).add(l.getName());
		}
		for (Entry<ExperimentDesignFactor, TreeSet<String>> entry : levelFactorMap.entrySet()) {

			Object[] obj = new Object[] {
				entry.getKey(),
				StringUtils.join(entry.getValue(), ", ")
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}












