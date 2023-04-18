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

package edu.umich.med.mrc2.datoolbox.gui.library;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.utils.MolFormulaUtils;

public class LibraryFeatureTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3415537237177072525L;

	public static final String ORDER_COLUMN = "##";
	public static final String ENABLED_COLUMN = "Active";
	public static final String QC_COLUMN = "QC standard";
	public static final String ID_COLUMN = "ID";
	public static final String FEATURE_COLUMN = "Entry name";
	public static final String COMPOUND_NAME_COLUMN = "Compound name";
	public static final String FORMULA_COLUMN = "Formula";
	public static final String MASS_COLUMN = "Monoisotopic mass";
	public static final String RT_COLUMN = "RT";
	public static final String MS_COLUMN = "Has MS";
	public static final String MSMS_COLUMN = "Has MS-MS";
	public static final String CHARGE_COLUMN = "Innate charge";
	public static final String ID_CONFIDENCE_COLUMN = "ID confidence";
	public static final String ANNOTATIONS_COLUMN = "Annotations";

	public LibraryFeatureTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ORDER_COLUMN, Integer.class, false),
			new ColumnContext(ENABLED_COLUMN, Boolean.class, true),
			new ColumnContext(QC_COLUMN, Boolean.class, true),
			new ColumnContext(ID_COLUMN, MsFeatureIdentity.class, false),
			new ColumnContext(FEATURE_COLUMN, LibraryMsFeature.class, false),
			new ColumnContext(COMPOUND_NAME_COLUMN, String.class, false),
			new ColumnContext(FORMULA_COLUMN, String.class, false),
			new ColumnContext(MASS_COLUMN, Double.class, false),
			new ColumnContext(RT_COLUMN, Double.class, false),
			new ColumnContext(MS_COLUMN, Boolean.class, false),
			new ColumnContext(MSMS_COLUMN, Boolean.class, false),
			new ColumnContext(CHARGE_COLUMN, Integer.class, false),
			new ColumnContext(ID_CONFIDENCE_COLUMN, CompoundIdentificationConfidence.class, false),
			new ColumnContext(ANNOTATIONS_COLUMN, Boolean.class, false)
		};
	}

	public void setTableModelFromFeatureList(LibraryMsFeature[] allFeatures) {

		setTableModelFromFeatureList(Arrays.asList(allFeatures));
	}

	public void setTableModelFromFeatureList(Collection<LibraryMsFeature> featureList) {

		setRowCount(0);
		if(featureList == null || featureList.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();		
		int count = 1;
		for (LibraryMsFeature lf : featureList) {

			MsFeatureIdentity identity = lf.getPrimaryIdentity();
			String formula = "";
			int innateCharge = 0;
			if(identity != null) {
				formula = identity.getCompoundIdentity().getFormula();

				String smiles = identity.getCompoundIdentity().getSmiles();
				if(smiles != null)
					innateCharge = StringUtils.countMatches(smiles, "+") - StringUtils.countMatches(smiles, "-");
			}
			boolean hasMs = false;
			boolean hasMsMs = false;
			if(lf.getSpectrum() != null) {

				if(!lf.getSpectrum().getAdducts().isEmpty())
					hasMs = true;

				if(!lf.getSpectrum().getTandemSpectra().isEmpty())
					hasMsMs = true;
			}
			String compoundName = lf.getName();
			CompoundIdentificationConfidence idc = null;
			if(lf.getPrimaryIdentity() != null) {

				compoundName = lf.getPrimaryIdentity().getName();
				idc= lf.getPrimaryIdentity().getConfidenceLevel();
			}
			Object[] obj = {
				count,
				lf.isActive(),
				lf.isQcStandard(),
				identity,
				lf,
				compoundName,
				formula,
				lf.getNeutralMass(),
				lf.getRetentionTime(),
				hasMs,
				hasMsMs,
				innateCharge,
				idc,
				!lf.getAnnotations().isEmpty()
			};
			rowData.add(obj);
			count++;
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}

	public void updateFeatureData(LibraryMsFeature lf) {

		int row = getFeatureRow(lf);
		if(row == -1)
			return;

		int count = (int) getValueAt(row, getColumnIndex(ORDER_COLUMN));
		setValueAt(count, row, getColumnIndex(ORDER_COLUMN));

		MsFeatureIdentity identity = lf.getPrimaryIdentity();

		String formula = "";
		int innateCharge = 0;
		CompoundIdentificationConfidence idc = null;

		if(identity != null) {
			formula = identity.getCompoundIdentity().getFormula();

			String smiles = identity.getCompoundIdentity().getSmiles();

			if(smiles != null)
				innateCharge = MolFormulaUtils.getChargeFromSmiles(smiles);

			idc = identity.getConfidenceLevel();
		}
		boolean hasMs = false;
		boolean hasMsMs = false;

		if(lf.getSpectrum() != null) {

			if(!lf.getSpectrum().getAdducts().isEmpty())
				hasMs = true;

			if(!lf.getSpectrum().getTandemSpectra().isEmpty())
				hasMsMs = true;
		}
		String compoundName = lf.getName();
		if(lf.getPrimaryIdentity() != null)
			compoundName = lf.getPrimaryIdentity().getName();

		setValueAt(lf.isActive(), row, getColumnIndex(ENABLED_COLUMN));
		setValueAt(lf.isQcStandard(), row, getColumnIndex(QC_COLUMN));
		setValueAt(identity, row, getColumnIndex(ID_COLUMN));
		setValueAt(lf, row, getColumnIndex(FEATURE_COLUMN));
		setValueAt(compoundName, row, getColumnIndex(COMPOUND_NAME_COLUMN));
		setValueAt(formula, row, getColumnIndex(FORMULA_COLUMN));
		setValueAt(lf.getNeutralMass(), row, getColumnIndex(MASS_COLUMN));
		setValueAt(lf.getRetentionTime(), row, getColumnIndex(RT_COLUMN));
		setValueAt(hasMs, row, getColumnIndex(MS_COLUMN));
		setValueAt(hasMsMs, row, getColumnIndex(MSMS_COLUMN));
		setValueAt(innateCharge, row, getColumnIndex(CHARGE_COLUMN));
		setValueAt(idc, row, getColumnIndex(ID_CONFIDENCE_COLUMN));
		setValueAt(!lf.getAnnotations().isEmpty(), row, getColumnIndex(ANNOTATIONS_COLUMN));
	}

	public int getFeatureRow(MsFeature feature) {

		int col = getColumnIndex(FEATURE_COLUMN);
		for (int i = 0; i < getRowCount(); i++) {

			if (feature.equals((LibraryMsFeature)getValueAt(i, col)))
				return i;
		}
		return -1;
	}
}
