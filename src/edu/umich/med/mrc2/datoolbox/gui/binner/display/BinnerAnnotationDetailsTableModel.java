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

package edu.umich.med.mrc2.datoolbox.gui.binner.display;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotation;
import edu.umich.med.mrc2.datoolbox.data.BinnerAnnotationCluster;
import edu.umich.med.mrc2.datoolbox.data.msclust.BinnerBasedMsFeatureInfoBundleCluster;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class BinnerAnnotationDetailsTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 5386419982114129215L;

	public static final String PRIMARY_COLUMN = "Primary";
	public static final String DETECTED_COLUMN = "Found";
	public static final String ANNOTATION_COLUMN = "Annotation";
	public static final String CHARGE_CARRIER_COLUMN = "Charge carrier";
	public static final String MZ_COLUMN = "Binner M/Z";
	public static final String RT_COLUMN = "Binner RT";
	public static final String MASS_ERROR_COLUMN = "Mass error";
	public static final String RMD_COLUMN = "RMD";
	public static final String ADDITIONAL_GROUP_ANNOTATIONS_COLUMN = "Addl group annot";
	public static final String FURTHER_ANNOTATIONS_COLUMN = "Further annotations";
	public static final String DERIVATIONS_COLUMN = "Derivations";
	public static final String ISOTOPES_COLUMN = "Isotopes";
	public static final String ADDITIONAL_ISOTOPES_COLUMN = "Addl isotopes";
	public static final String ADDITIONAL_ADDUCTS_COLUMN = "Addl adducts";
	
	public BinnerAnnotationDetailsTableModel() {

		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(PRIMARY_COLUMN, PRIMARY_COLUMN, Boolean.class, false),
			new ColumnContext(DETECTED_COLUMN, DETECTED_COLUMN, Boolean.class, false),
			new ColumnContext(ANNOTATION_COLUMN, ANNOTATION_COLUMN, BinnerAnnotation.class, false),
			new ColumnContext(CHARGE_CARRIER_COLUMN, CHARGE_CARRIER_COLUMN, String.class, false),
			new ColumnContext(MZ_COLUMN, MZ_COLUMN, Double.class, false),
			new ColumnContext(RT_COLUMN, RT_COLUMN, Double.class, false),
			new ColumnContext(MASS_ERROR_COLUMN, MASS_ERROR_COLUMN, Double.class, false),
			new ColumnContext(RMD_COLUMN, RMD_COLUMN, Double.class, false),			
			new ColumnContext(ADDITIONAL_GROUP_ANNOTATIONS_COLUMN, "Additional group annotations", String.class, false),
			new ColumnContext(FURTHER_ANNOTATIONS_COLUMN, FURTHER_ANNOTATIONS_COLUMN, String.class, false),
			new ColumnContext(DERIVATIONS_COLUMN, DERIVATIONS_COLUMN, String.class, false),
			new ColumnContext(ISOTOPES_COLUMN, ISOTOPES_COLUMN, String.class, false),
			new ColumnContext(ADDITIONAL_ISOTOPES_COLUMN, "Additional isotopes", String.class, false),
			new ColumnContext(ADDITIONAL_ADDUCTS_COLUMN, "Additional adducts", String.class, false),
		};
	}

	public void setTableModelFromBinnerAnnotationCluster(
			BinnerBasedMsFeatureInfoBundleCluster baCluster) {

		setRowCount(0);
		BinnerAnnotationCluster bac = baCluster.getBinnerAnnotationCluster();
		if(bac == null)
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (BinnerAnnotation ba : bac.getAnnotations()) {

			Object[] obj = {
					ba.equals(bac.getPrimaryFeatureAnnotation()),
					!baCluster.getComponentMap().get(ba).isEmpty(),
					ba,
					ba.getChargeCarrier(),
					ba.getBinnerMz(),
					ba.getBinnerRt(),
					ba.getMassError(),
					ba.getRmd(),
					ba.getCleanAdditionalGroupAnnotations(),
					ba.getCleanFurtherAnnotations(),
					ba.getCleanDerivations(),
					ba.getCleanIsotopes(),
					ba.getCleanAdditionalIsotopes(),
					ba.getCleanAdditionalAdducts()
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
	
	public void setTableModelFromBinnerAnnotations(
			Collection<BinnerAnnotation> annotations) {
		
		setRowCount(0);
		if(annotations == null || annotations.isEmpty())
			return;
		
		List<Object[]>rowData = new ArrayList<Object[]>();
		for (BinnerAnnotation ba : annotations) {

			Object[] obj = {
					ba.isPrimary(),
					true,
					ba,
					ba.getChargeCarrier(),
					ba.getBinnerMz(),
					ba.getBinnerRt(),
					ba.getMassError(),
					ba.getRmd(),
					ba.getCleanAdditionalGroupAnnotations(),
					ba.getCleanFurtherAnnotations(),
					ba.getCleanDerivations(),
					ba.getCleanIsotopes(),
					ba.getCleanAdditionalIsotopes(),
					ba.getCleanAdditionalAdducts()
			};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}
	
}










