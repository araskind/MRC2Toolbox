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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.msms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.rawdata.MSMSExtractionParameterSet;

@SuppressWarnings("serial")
public class MSMSFeatureExtractionParametersModel extends BasicTableModel {

	public static final String METHOD_ID_COLUMN = "ID";
	public static final String METHOD_COLUMN = "Method";
	public static final String POLARITY_COLUMN = "Polarity";
	
	public MSMSFeatureExtractionParametersModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(METHOD_ID_COLUMN, String.class, false),
			new ColumnContext(METHOD_COLUMN, Double.class, false),
			new ColumnContext(POLARITY_COLUMN, String.class, false),
		};
	}

	public void setModelFromParametersList(
			Collection<MSMSExtractionParameterSet>parameterList) {

		setRowCount(0);		
		if(parameterList == null || parameterList.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (MSMSExtractionParameterSet parSet : parameterList) {
			
			String polarityName = "Any";
			if(parSet.getPolarity() != null)
				polarityName = parSet.getPolarity().name();
			
			Object[] obj = new Object[] { 
					parSet.getId(), 
					parSet,
					polarityName,
				};
			rowData.add(obj);
		}	
		addRows(rowData);
	}	
}












