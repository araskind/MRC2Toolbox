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

package edu.umich.med.mrc2.datoolbox.gui.plot.tooltip;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.gui.datexp.msone.LCMSPlotType;

public class FileFeatureTooltipInputObject {

	private DataFile df;
	private SimpleMsFeature smsf;
	private MsFeature msf;
	private String seriesKey;
	private LCMSPlotType plotValueType;
	
	public FileFeatureTooltipInputObject(
			DataFile df, 
			SimpleMsFeature smsf, 
			MsFeature msf,
			String seriesKey,
			LCMSPlotType plotValueType) {
		super();
		this.df = df;
		this.smsf = smsf;
		this.msf = msf;
		this.seriesKey = seriesKey;
		this.plotValueType = plotValueType;
	}

	public DataFile getDataFile() {
		return df;
	}

	public SimpleMsFeature getSimpleMsFeature() {
		return smsf;
	}
	
	public MsFeature getMsFeature() {
		return msf;
	}

	public String getSeriesKey() {
		return seriesKey;
	}

	public LCMSPlotType getPlotValueType() {
		return plotValueType;
	}
}
