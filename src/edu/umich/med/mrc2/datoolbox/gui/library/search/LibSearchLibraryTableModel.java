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

package edu.umich.med.mrc2.datoolbox.gui.library.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class LibSearchLibraryTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -210488394549028786L;

	public static final String USE_COLUMN = "Use";
	public static final String LIBRARY_COLUMN = "Library name";
	public static final String DESCRIPTION_COLUMN = "Description";	
	public static final String POLARITY_COLUMN = "Polarity";
	public static final String NUM_ENTRIES_COLUMN = "# Compounds";
	public static final String DATE_CREATED_COLUMN = "Created on";

	public LibSearchLibraryTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(USE_COLUMN, "Include the library in the search", Boolean.class, true),
			new ColumnContext(LIBRARY_COLUMN, LIBRARY_COLUMN, CompoundLibrary.class, false),
			new ColumnContext(DESCRIPTION_COLUMN, DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(POLARITY_COLUMN, POLARITY_COLUMN, Polarity.class, false),
			new ColumnContext(NUM_ENTRIES_COLUMN, "Number of entries in the library", Integer.class, false),
			new ColumnContext(DATE_CREATED_COLUMN, DATE_CREATED_COLUMN, Date.class, false),
		};
	}

	public void setTableModelFromLibraryCollection(
			Collection<CompoundLibrary> libraryCollection) {

		CompoundLibrary[] libraries =
				libraryCollection.toArray(new CompoundLibrary[libraryCollection.size()]);
		setTableModelFromLibraryList(libraries);
	}

	private void setTableModelFromLibraryList(CompoundLibrary[] libraries) {
		
		setRowCount(0);
		if(libraries == null || libraries.length == 0)
			return;
		
		Arrays.sort(libraries);		
		Map<String, Integer> countsMap = 
				IDTDataCache.getMsRtLibraryEntryCount();
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(CompoundLibrary lib : libraries){
				
			Object[] obj = {
					false,
					lib,
					lib.getLibraryDescription(),
					lib.getPolarity(),
					countsMap.get(lib.getLibraryId()),
					lib.getDateCreated(),
				};
			rowData.add(obj);			
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}











