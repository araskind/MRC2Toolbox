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

package edu.umich.med.mrc2.datoolbox.gui.fdata.noid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MissingIdsTableModel extends BasicTableModel{

	/**
	 *
	 */
	private static final long serialVersionUID = -1631277946434853599L;

	public static final String LIBRARY_COLUMN = "Library";
	public static final String FEATURE_COLUMN = "Entry name";
	public static final String COMPOUND_NAME_COLUMN = "Compound name";
	public static final String FORMULA_COLUMN = "Formula";
	public static final String MASS_COLUMN = "Monoisotopic mass";
	public static final String RT_COLUMN = "RT";
	public static final String CHARGE_COLUMN = "Innate charge";
	public static final String ID_CONFIDENCE_COLUMN = "ID confidence";

	public MissingIdsTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(LIBRARY_COLUMN, "Compound library", CompoundLibrary.class, false),
			new ColumnContext(FEATURE_COLUMN, "Library entry", LibraryMsFeature.class, false),
			new ColumnContext(COMPOUND_NAME_COLUMN, COMPOUND_NAME_COLUMN, String.class, false),
			new ColumnContext(FORMULA_COLUMN, FORMULA_COLUMN, String.class, false),
			new ColumnContext(MASS_COLUMN, MASS_COLUMN, Double.class, false),
			new ColumnContext(RT_COLUMN, "Retention time", Double.class, false),
			new ColumnContext(CHARGE_COLUMN, CHARGE_COLUMN, Integer.class, false),
			new ColumnContext(ID_CONFIDENCE_COLUMN, 
					"Identification confidence level", CompoundIdentificationConfidence.class, false),
		};
	}

	public void setTableModelFromFeatureMap(
			HashMap<CompoundLibrary, Collection<MsFeature>> unidentified) {

		setRowCount(0);
		if(unidentified.isEmpty())
			return;

		List<Object[]>rowData = new ArrayList<Object[]>();
		for (Entry<CompoundLibrary, Collection<MsFeature>> entry : unidentified.entrySet()) {

			for (MsFeature lf : entry.getValue()) {

				MsFeatureIdentity identity = lf.getPrimaryIdentity();
				String formula = "";
				int innateCharge = 0;

				if(identity != null) {
					
					formula = identity.getCompoundIdentity().getFormula();
					String smiles = identity.getCompoundIdentity().getSmiles();
					if(smiles != null)
						innateCharge = StringUtils.countMatches(smiles, "+") 
								- StringUtils.countMatches(smiles, "-");
				}
				String compoundName = lf.getName();
				CompoundIdentificationConfidence idc = null;
				if(lf.getPrimaryIdentity() != null) {

					compoundName = lf.getPrimaryIdentity().getCompoundName();
					idc= lf.getPrimaryIdentity().getConfidenceLevel();
				}
				Object[] obj = {
					entry.getKey(),
					lf,
					compoundName,
					formula,
					lf.getNeutralMass(),
					lf.getRetentionTime(),
					innateCharge,
					idc
				};
				rowData.add(obj);
			}
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
}
