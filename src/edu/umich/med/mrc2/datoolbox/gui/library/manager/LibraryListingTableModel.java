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

package edu.umich.med.mrc2.datoolbox.gui.library.manager;

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
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class LibraryListingTableModel  extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 7186847992409868627L;
	
	public static final String LOADED_COLUMN = "Loaded";
	public static final String LIBRARY_COLUMN = "Name";
	public static final String DESCRIPTION_COLUMN = "Description";
	public static final String POLARITY_COLUMN = "Polarity";
	public static final String NUM_ENTRIES_COLUMN = "# Compounds";
	public static final String DATE_CREATED_COLUMN = "Created on";

	public LibraryListingTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(LOADED_COLUMN, Boolean.class, false),
			new ColumnContext(LIBRARY_COLUMN, CompoundLibrary.class, false),
			new ColumnContext(DESCRIPTION_COLUMN, String.class, false),
			new ColumnContext(POLARITY_COLUMN, Polarity.class, false),
			new ColumnContext(NUM_ENTRIES_COLUMN, Integer.class, false),
			new ColumnContext(DATE_CREATED_COLUMN, Date.class, false),
		};
	}

	public void setTableModelFromLibraryCollection(
			Collection<CompoundLibrary> libraryCollection, 
			CompoundLibrary activeLibrary) {

		CompoundLibrary[] libraries = 
				libraryCollection.toArray(
						new CompoundLibrary[libraryCollection.size()]);
		setTableModelFromLibraryList(libraries, activeLibrary);
	}

	private void setTableModelFromLibraryList(
			CompoundLibrary[] libraries, CompoundLibrary activeLibrary) {

		setRowCount(0);
		if(libraries == null || libraries.length == 0)
			return;
		
		Arrays.sort(libraries);		
		Map<String, Integer> countsMap = 
				IDTDataCache.getMsRtLibraryEntryCount();
		List<Object[]>rowData = new ArrayList<Object[]>();
		Collection<CompoundLibrary> activeLibs = 
				MRC2ToolBoxCore.getActiveMsLibraries();
		for(CompoundLibrary l : libraries){
			
			boolean loaded = false;
			CompoundLibrary lib = l;
			if(activeLibs.contains(l)) {
				lib = activeLibs.stream().
						filter(cl -> cl.getLibraryId().equals(l.getLibraryId())).
						findFirst().orElse(null);
				loaded = true;
			}
			if(lib != null) {
				
				Object[] obj = {
						loaded,
						lib,
						lib.getLibraryDescription(),
						lib.getPolarity(),
						countsMap.get(lib.getLibraryId()),
						lib.getDateCreated(),
					};
				rowData.add(obj);
			}
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
