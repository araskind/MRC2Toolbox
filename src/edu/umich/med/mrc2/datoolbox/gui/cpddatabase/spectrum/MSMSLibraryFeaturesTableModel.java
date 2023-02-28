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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.spectrum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MSMSLibraryFeaturesTableModel extends BasicTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 136539053290554728L;
	public static final String FEATURE_COLUMN = "Name";
	public static final String LIBRARY_NAME_COLUMN = "Library";
	public static final String POLARITY_COLUMN = "Polarity";
	public static final String PARENT_MZ_COLUMN = "Parent M/Z";
	public static final String IONIZATION_TYPE_COLUMN = "Ionization type";
	public static final String COLLISION_ENERGY_COLUMN = "CE, V";
	public static final String ENTROPY_COLUMN = "PRE";
	public static final String PEAK_NUMBER_COLUMN = "#Peaks";

	public MSMSLibraryFeaturesTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(FEATURE_COLUMN, MsMsLibraryFeature.class, true),
			new ColumnContext(LIBRARY_NAME_COLUMN, String.class, true),
			new ColumnContext(POLARITY_COLUMN, String.class, true),
			new ColumnContext(PARENT_MZ_COLUMN, Double.class, false),
			new ColumnContext(IONIZATION_TYPE_COLUMN, String.class, true),
			new ColumnContext(COLLISION_ENERGY_COLUMN, String.class, false),			
			new ColumnContext(ENTROPY_COLUMN, Double.class, false),
			new ColumnContext(PEAK_NUMBER_COLUMN, Integer.class, false),
		};
	}

	public void setTableModelFromLibraryFeatureCollection(
			Collection<MsMsLibraryFeature> libraryFeatures) {

		setRowCount(0);
		if(libraryFeatures == null || libraryFeatures.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();		
		for(MsMsLibraryFeature lf : libraryFeatures) {
			
			double parentMz = 0.0d;
			if(lf.getParent() != null)
				parentMz = lf.getParent().getMz();
			
			String ionizationType = null;
			if(lf.getIonizationType() != null)
				ionizationType = lf.getIonizationType().getDescription();
			
			String libName = null;
			ReferenceMsMsLibrary refLib = 
					IDTDataCash.getReferenceMsMsLibraryById(lf.getMsmsLibraryIdentifier());
			if(refLib != null)
				libName = refLib.getName();
			
			Object[] obj = {
				lf,
				libName,
				lf.getPolarity().name(),
				parentMz,
				ionizationType,
				lf.getCollisionEnergyValue(),
				lf.getSpectrumEntropy(),
				lf.getSpectrum().size()
			};
			rowData.add(obj);
		}
		addRows(rowData);
	}
}
