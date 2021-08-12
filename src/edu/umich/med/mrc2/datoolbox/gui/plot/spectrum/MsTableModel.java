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

package edu.umich.med.mrc2.datoolbox.gui.plot.spectrum;

import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class MsTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = -4864924940070524356L;

	public static final String MZ_COLUMN = "M/Z";
	public static final String INTENSITY_COLUMN = "Intensity";
	public static final String ANNOTATION_COLUMN = "Annotation";

	public MsTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(MZ_COLUMN, Double.class, false),
			new ColumnContext(INTENSITY_COLUMN, Double.class, false),
			new ColumnContext(ANNOTATION_COLUMN, String.class, false),
		};
	}

	public void setTableModelFromFeatureList(Collection<MsFeature> featureList, boolean scaleMs) {

		setRowCount(0);
		for (MsFeature cf : featureList) {

			if (cf.getSpectrum() == null)
				continue;

			for(Adduct adduct : cf.getSpectrum().getAdducts()) {

				MsPoint[] ms = cf.getSpectrum().getMsForAdduct(adduct, scaleMs);
				for (MsPoint p : ms) {

					Object[] obj = {
						p.getMz(),
						p.getIntensity(),
						adduct.getName()
					};
					super.addRow(obj);
				}
			}
		}
	}
}
