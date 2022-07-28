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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt;

import java.util.ArrayList;
import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsType;
import edu.umich.med.mrc2.datoolbox.data.enums.IdentifierSearchOptions;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSampleType;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public abstract class IDTFeatureSearchTask extends AbstractTask {

	protected Double basePeakMz;
	protected Double massError;
	protected MassErrorType massErrorType;
	protected boolean ignoreMz;
	protected Range rtRange;
	protected String featureName;
	protected IdentifierSearchOptions idOpt;
	protected MsType msType;
	protected LIMSChromatographicColumn chromatographicColumn;
	protected LIMSSampleType sampleType;
	protected Double collisionEnergy;
	protected Polarity polarity;
	protected LIMSExperiment experiment;
	protected Collection<MSFeatureInfoBundle>features;

	public IDTFeatureSearchTask(
			Double basePeakMz,
			Double massError,
			MassErrorType massErrorType,
			boolean ignoreMz,
			Range rtRange,
			String featureName,
			IdentifierSearchOptions idOpt,
			MsType msType,
			LIMSChromatographicColumn chromatographicColumn,
			LIMSSampleType sampleType,
			Double collisionEnergy,
			Polarity polarity,
			LIMSExperiment experiment) {

		super();
		this.basePeakMz = basePeakMz;
		this.massError = massError;
		this.massErrorType = massErrorType;
		this.ignoreMz = ignoreMz;
		this.rtRange = rtRange;
		this.featureName = featureName;
		this.idOpt = idOpt;
		this.msType = msType;
		this.chromatographicColumn = chromatographicColumn;
		this.sampleType = sampleType;
		this.collisionEnergy = collisionEnergy;
		this.polarity = polarity;
		this.experiment = experiment;

		features = new ArrayList<MSFeatureInfoBundle>();
	}

	protected String createIdQuery() {

		String idQuery =
			"SELECT DISTINCT M.ACCESSION FROM COMPOUND_DATA C WHERE ";

		if(idOpt.equals(IdentifierSearchOptions.COMPOUND_ID)) {

		}
		return idQuery;
	}

	/**
	 * @return the selectedFeatures
	 */
	public Collection<MSFeatureInfoBundle> getSelectedFeatures() {
		return features;
	}
}
