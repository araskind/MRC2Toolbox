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

package edu.umich.med.mrc2.datoolbox.gui.plot.tooltip;

import java.text.SimpleDateFormat;
import java.util.Map.Entry;
import java.util.NavigableMap;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.gui.datexp.msone.LCMSPlotType;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class TooltipUtils {
	
	private TooltipUtils() {
		
	}

	private static final SimpleDateFormat injectionTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	public static String generateLabelForFeature(FileFeatureTooltipInputObject fftio) {

		DataFile df = fftio.getDataFile();
		SimpleMsFeature msf = fftio.getSimpleMsFeature();
		MsFeature feature = fftio.getMsFeature();
		String seriesKey = fftio.getSeriesKey();
		LCMSPlotType plotValueType = fftio.getPlotValueType();

		StringBuilder stringBuilder = new StringBuilder(600);
		stringBuilder.append("<HTML><B>Data file: </B>" + df.getName());
		stringBuilder.append("<BR><B>Injection time: </B>");
		stringBuilder.append(injectionTimeFormat.format(df.getInjectionTime()));
		stringBuilder.append("<BR>");
		if (msf != null)
			addDataFromSimpleMsFeature(stringBuilder, msf, plotValueType);
		else {
			if (feature != null)
				addDataFromMsFeature(stringBuilder, feature, plotValueType);
		}
		NavigableMap<ExperimentDesignFactor, ExperimentDesignLevel> desCell = null;
		if (df.getParentSample() != null) {
			desCell = df.getParentSample().getDesignCell();
			stringBuilder.append("<HR><B>Sample: </B>");
			stringBuilder.append(df.getParentSample().getName());
			stringBuilder.append(" (");
			stringBuilder.append(df.getParentSample().getId());
			stringBuilder.append(")");
		}
		if (desCell != null && !desCell.isEmpty()) {

			for (Entry<ExperimentDesignFactor, ExperimentDesignLevel> e : desCell.entrySet()) {
				stringBuilder.append("<BR><B>");
				stringBuilder.append(e.getKey().getName());
				stringBuilder.append(": </B>" );
				stringBuilder.append(e.getValue().getName());
			}
			stringBuilder.append("<BR>");
		}
		stringBuilder.append("<B>Series: </B>");
		stringBuilder.append(seriesKey + "<BR>");
		return stringBuilder.toString();
	}

	private static void addDataFromMsFeature(StringBuilder stringBuilder, MsFeature msf, LCMSPlotType plotValueType) {

		if (plotValueType.equals(LCMSPlotType.MZ))
			stringBuilder.append("<B>M/Z: </B>"
					+ MRC2ToolBoxConfiguration.getMzFormat().format(msf.getSpectrum().getMonoisotopicMz()));

		if (plotValueType.equals(LCMSPlotType.RT_AND_PEAK_WIDTH)) {

			stringBuilder.append("<B>RT: </B>");
			stringBuilder.append(MRC2ToolBoxConfiguration.getRtFormat().format(msf.getRetentionTime()));
			stringBuilder.append(" min<BR>");
			stringBuilder.append("<B>RT range: </B>");
			stringBuilder.append(msf.getRtRange().getFormattedString(MRC2ToolBoxConfiguration.getRtFormat()));
			stringBuilder.append(" min<BR>");
			stringBuilder.append("<B>Peak width: </B>");
			stringBuilder.append(MRC2ToolBoxConfiguration.getRtFormat().format(msf.getRtRange().getSize()));
			stringBuilder.append(" min");
		}
		if (plotValueType.equals(LCMSPlotType.FEATURE_QUALITY)) {
			stringBuilder.append("<B>Quality score: </B>");
			stringBuilder.append(MRC2ToolBoxConfiguration.getPpmFormat().format(msf.getQualityScore()));
		}
		if (plotValueType.equals(LCMSPlotType.PEAK_AREA)) {
			stringBuilder.append("<B>Quality score: </B>");
			stringBuilder.append(MRC2ToolBoxConfiguration.getIntensityFormat().format(msf.getArea()));
		}
	}

	private static void addDataFromSimpleMsFeature(StringBuilder stringBuilder, SimpleMsFeature msf,
			LCMSPlotType plotValueType) {

		if (plotValueType.equals(LCMSPlotType.MZ))
			stringBuilder.append("<B>M/Z: </B>"
					+ MRC2ToolBoxConfiguration.getMzFormat().format(
							msf.getObservedSpectrum().getMonoisotopicMz()));

		if (plotValueType.equals(LCMSPlotType.RT_AND_PEAK_WIDTH)) {

			stringBuilder.append("<B>RT: </B>");
			stringBuilder.append(MRC2ToolBoxConfiguration.getRtFormat().format(msf.getRetentionTime()));
			stringBuilder.append(" min<BR>");
			stringBuilder.append("<B>RT range: </B>");
			stringBuilder.append(msf.getRtRange().getFormattedString(MRC2ToolBoxConfiguration.getRtFormat()));
			stringBuilder.append(" min<BR>");
			stringBuilder.append("<B>Peak width: </B>");
			stringBuilder.append(MRC2ToolBoxConfiguration.getRtFormat().format(msf.getRtRange().getSize()));
			stringBuilder.append(" min");
		}
		if (plotValueType.equals(LCMSPlotType.FEATURE_QUALITY)) {
			stringBuilder.append("<B>Quality score: </B>");
			stringBuilder.append(MRC2ToolBoxConfiguration.getPpmFormat().format(msf.getQualityScore()));
		}
		if (plotValueType.equals(LCMSPlotType.PEAK_AREA)) {
			stringBuilder.append("<B>Quality score: </B>");
			stringBuilder.append(MRC2ToolBoxConfiguration.getIntensityFormat().format(msf.getArea()));
		}
	}
}
