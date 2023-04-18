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

package edu.umich.med.mrc2.datoolbox.gui.mptrack.ref.asssay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACAssay;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MotrpacAssayTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2389801131016456495L;
	public static final String ASSAY_COLUMN = "Assay ID";
	public static final String ASSAY_CODE_COLUMN = "Code";
	public static final String ASSAY_DESCRIPTION_COLUMN = "Description";
	public static final String POLARITY_COLUMN = "Polarity";
	public static final String BUCKET_CODE_COLUMN = "Bucket code";

	
	public MotrpacAssayTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ASSAY_COLUMN, MoTrPACAssay.class, false),
			new ColumnContext(ASSAY_CODE_COLUMN, String.class, false),
			new ColumnContext(ASSAY_DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(POLARITY_COLUMN, String.class, false),
			new ColumnContext(BUCKET_CODE_COLUMN, String.class, false),
		};
	}

	public void setTableModelFromAssays(Collection<MoTrPACAssay> assays) {

		setRowCount(0);
		if(assays == null || assays.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (MoTrPACAssay assay : assays) {

			Object[] obj = {
					assay,
					assay.getCode(),
					assay.getDescription(),
					assay.getPolarity(),
					assay.getBucketCode()
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
