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

package edu.umich.med.mrc2.datoolbox.gui.library.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
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

	public LibSearchLibraryTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(USE_COLUMN, Boolean.class, true),
			new ColumnContext(LIBRARY_COLUMN, CompoundLibrary.class, false),
			new ColumnContext(DESCRIPTION_COLUMN, String.class, false)
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
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(CompoundLibrary l : libraries){

			Object[] obj = {
				false,
				l,
				l.getLibraryDescription()
			};
			rowData.add(obj);
		}
		addRows(rowData);
	}
}











