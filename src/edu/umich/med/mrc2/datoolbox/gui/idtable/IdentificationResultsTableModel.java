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

package edu.umich.med.mrc2.datoolbox.gui.idtable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class IdentificationResultsTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 8319459935748558385L;

	public static final String DEFAULT_ID_COLUMN = "Default";
	public static final String QC_COLUMN = "QC";
	public static final String FEATURE_NAME_COLUMN = "Library target";
	public static final String IDENTIFICATION_COLUMN = "Compound name";
	public static final String COMPOUND_ID_COLUMN = "Compound ID";
	public static final String FORMULA_COLUMN = "Formula";
	public static final String NEUTRAL_MASS_COLUMN = "Monoisotopic mass";
	public static final String MASS_ERROR_COLUMN = "Mass error, ppm";
	public static final String EXPECTED_RETENTION_COLUMN = "Expected RT, min";
	public static final String RETENTION_ERROR_COLUMN = "RT error, min";
	public static final String ID_SCORE_COLUMN = "ID score";
	public static final String ID_CONFIDENCE_COLUMN = "ID confidence";
	public static final String BEST_MATCH_ADDUCT_COLUMN = "Best match adduct";
	public static final String ID_SOURCE_COLUMN = "ID source";

	private MsFeature parentFeature;

	public IdentificationResultsTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(DEFAULT_ID_COLUMN, Boolean.class, true),
			new ColumnContext(QC_COLUMN, Boolean.class, false),
			new ColumnContext(FEATURE_NAME_COLUMN, String.class, false),
			new ColumnContext(IDENTIFICATION_COLUMN, String.class, false),
			new ColumnContext(COMPOUND_ID_COLUMN, MsFeatureIdentity.class, false),
			new ColumnContext(FORMULA_COLUMN, String.class, false),
			new ColumnContext(NEUTRAL_MASS_COLUMN, Double.class, false),
			new ColumnContext(MASS_ERROR_COLUMN, Double.class, false),
			new ColumnContext(EXPECTED_RETENTION_COLUMN, Double.class, false),
			new ColumnContext(RETENTION_ERROR_COLUMN, Double.class, false),
			new ColumnContext(BEST_MATCH_ADDUCT_COLUMN, Adduct.class, false),
			new ColumnContext(ID_SCORE_COLUMN, Double.class, false),
			new ColumnContext(ID_CONFIDENCE_COLUMN, String.class, false),
			new ColumnContext(ID_SOURCE_COLUMN, String.class, false),
		};
	}

	public void setModelFromFeature(MsFeature parentFeature) {

		this.parentFeature = parentFeature;
		setModelFromIdList(parentFeature.getIdentifications(), parentFeature.getPrimaryIdentity());
	}

	public void setModelFromIdList(Set<MsFeatureIdentity> idList, MsFeatureIdentity defaultId) {

		setRowCount(0);
		if(idList == null || idList.isEmpty() || defaultId == null)
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (MsFeatureIdentity id : idList) {
			
			if(id == null)
				continue;

			double deltaMz = MsUtils.getPpmMassErrorForIdentity(parentFeature, id);
			Double deltaRt = calculateRetentionShift(id);
			Double expectedRt = null;
			Adduct adductMatch = null;
			if(id.getMsRtLibraryMatch() != null) {
				expectedRt = id.getMsRtLibraryMatch().getExpectedRetention();
				adductMatch = id.getMsRtLibraryMatch().getTopAdductMatch().getLibraryMatch();
			}
			String formula = null;
			Double exactMass = null;
			if(id.getCompoundIdentity() != null) {
				formula = id.getCompoundIdentity().getFormula();
				exactMass = id.getCompoundIdentity().getExactMass();
			}
			
			Object[] obj = {
				id.equals(defaultId),
				id.isQcStandard(),
				id.getIdentityName(),
				id.getCompoundName(),
				id,
				formula,
				exactMass,
				deltaMz,
				expectedRt,
				deltaRt,
				adductMatch,
				id.getScore(),
				id.getConfidenceLevel().getName(),
				id.getIdSource()
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
	
	private Double calculateRetentionShift(MsFeatureIdentity id) {

		if(id.getMsRtLibraryMatch() == null)
			return null;

		double expectedRt = id.getMsRtLibraryMatch().getExpectedRetention();
		if(expectedRt == 0.0d) {
			return null;
		}
		else {
			if(parentFeature.getStatsSummary() != null) {
				if(parentFeature.getStatsSummary().getMedianObservedRetention() > 0)
					return parentFeature.getStatsSummary().getMedianObservedRetention() - expectedRt;
				else
					return parentFeature.getRetentionTime() - expectedRt;
			}
			else
				return parentFeature.getRetentionTime() - expectedRt;
		}
	}

	/**
	 * @param parentFeature the parentFeature to set
	 */
	public void setParentFeature(MsFeature parentFeature) {
		this.parentFeature = parentFeature;
	}
}























