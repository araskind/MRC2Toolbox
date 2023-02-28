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

package edu.umich.med.mrc2.datoolbox.gui.idworks.ms2;

import java.util.ArrayList;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MSMSLibraryMatchTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -8721639551336713264L;
	public static final String DEFAULT_ID_COLUMN = "Default";
	public static final String MS_FEATURE_ID_COLUMN = "Name";
	public static final String DATABASE_LINK_COLUMN = "Compound ID";
	public static final String NEUTRAL_MASS_COLUMN = "Neutral mass";
	public static final String PARENT_MZ_COLUMN = "Parent M/Z";
	public static final String MSMS_LIB_COLUMN = "MSMS library";
	public static final String COLLISION_ENERGY_COLUMN = "CE, V";
	public static final String LIB_SCORE_COLUMN = "Match score";
	public static final String FWD_SCORE_COLUMN = "Fwd. score";
	public static final String REV_SCORE_COLUMN = "Rvs. score";
	public static final String PROBABILITY_COLUMN = "Probability";
	public static final String DOT_PRODUCT_COLUMN = "Dot-product";

	public MSMSLibraryMatchTableModel() {
		super();
		columnArray = new ColumnContext[] {

			new ColumnContext(DEFAULT_ID_COLUMN, Boolean.class, true),
			new ColumnContext(MS_FEATURE_ID_COLUMN, MsFeatureIdentity.class, false),
			new ColumnContext(DATABASE_LINK_COLUMN, CompoundIdentity.class, false),
			new ColumnContext(NEUTRAL_MASS_COLUMN, Double.class, false),
			new ColumnContext(PARENT_MZ_COLUMN, Double.class, false),
			new ColumnContext(MSMS_LIB_COLUMN, ReferenceMsMsLibrary.class, false),
			new ColumnContext(COLLISION_ENERGY_COLUMN, Double.class, false),
			new ColumnContext(LIB_SCORE_COLUMN, Double.class, false),
			new ColumnContext(FWD_SCORE_COLUMN, Double.class, false),
			new ColumnContext(REV_SCORE_COLUMN, Double.class, false),
			new ColumnContext(PROBABILITY_COLUMN, Double.class, false),
			new ColumnContext(DOT_PRODUCT_COLUMN, Double.class, false),
		};
	}

	public void setTableModelFromFeatureBundle(MSFeatureInfoBundle feature) {

		setRowCount(0);
		MsFeature cf = feature.getMsFeature();
		if(cf.getIdentifications().isEmpty())
			return;

		double parentMz = 0.0d;
		TandemMassSpectrum experimentalMsMs = 
				cf.getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL);
		if(experimentalMsMs != null) {

			if(experimentalMsMs.getParent() != null)
				parentMz = experimentalMsMs.getParent().getMz();
		}
		MsFeatureIdentity primary = cf.getPrimaryIdentity();		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for(MsFeatureIdentity id : cf.getIdentifications()) {

			ReferenceMsMsLibraryMatch msmslibMatch = 					
					id.getReferenceMsMsLibraryMatch();
			if(msmslibMatch == null)
				continue;

			MsMsLibraryFeature matchFeature = 
					msmslibMatch.getMatchedLibraryFeature();
			ReferenceMsMsLibrary lib =
				IDTDataCash.getReferenceMsMsLibraryById(matchFeature.getMsmsLibraryIdentifier());

			Object[] obj = {
				id.equals(primary), // DEFAULT_ID_COLUMN, Boolean
				id, // MS_FEATURE_COLUMN, MsFeatureIdentity
				id.getCompoundIdentity(), // DATABASE_LINK_COLUMN, CompoundIdentity
				id.getCompoundIdentity().getExactMass(),//	NEUTRAL_MASS_COLUMN, Double
				parentMz,	//	PARENT_MZ_COLUMN, Double
				lib, //	MSMS_LIB_COLUMN, ReferenceMsMsLibrary
				matchFeature.getCollisionEnergyValue(),	//	COLLISION_ENERGY_COLUMN, Double
				msmslibMatch.getScore(), //	LIB_SCORE_COLUMN, Double
				msmslibMatch.getForwardScore(), //	FWD_SCORE_COLUMN, Double
				msmslibMatch.getReverseScore(), //	REV_SCORE_COLUMN, Double
				msmslibMatch.getProbability(),	//	PROBABILITY_COLUMN, Double
				msmslibMatch.getDotProduct(),//	DOT_PRODUCT_COLUMN, Double
			};
			rowData.add(obj);
		}
		addRows(rowData);
	}

	public void updateFeatureData(MsFeature cf) {

		//	TODO

	}

	public int getFeatureIdentityRow(MsFeatureIdentity featureId) {

		int col = getColumnIndex(MS_FEATURE_ID_COLUMN);
		for (int i = 0; i < getRowCount(); i++) {

			if (featureId.equals((MsFeatureIdentity)getValueAt(i, col)))
				return i;
		}
		return -1;
	}
}
