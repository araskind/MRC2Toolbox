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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.util.ArrayList;
import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSScoreCalculator;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class SpectrumEntropyTest {

	public static void main(String[] args) {
		
		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();
		try {
			testEntropy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void testEntropy() {

		double mzWindowValue = 0.05d;
		MassErrorType massErrorType = MassErrorType.Da;
		double relIntNoiseCutoff = 0.01d;
		
		String spectrumString = 
				"[[39.0227, 15.6], [41.038, 29.4], [42.0332, 6.3], [43.0178, 7.2], [43.0406, 14], " + 
				"[51.0228, 31.1], [55.0179, 6.3], [65.0381, 246.3], [67.0541, 75.6], [77.0383, 478], " + 
				"[79.0539, 25.5], [81.0685, 17.5], [91.0537, 999], [92.0616, 7.3], [93.0541, 5.9], " + 
				"[94.0417, 15], [95.0489, 145.7], [101.0393, 5.9], [107.0483, 136.8], " + 
				"[109.0642, 16], [117.0571, 7.7], [118.0636, 18.5], [119.0488, 24.3], " + 
				"[120.0525, 5.9], [123.0428, 21.6], [135.0681, 5.8]]";
		
		String libSpectrumString = 
				"[[41.0377, 319.7], [43.0171, 999], [43.0405, 20], [44.9905, 30], [51.0223, 139.9], " + 
				"[53.038, 69.9], [55.017, 40], [63.0223, 10], [65.0384, 579.4], [66.0336, 119.9], " + 
				"[67.054, 89.9], [75.0227, 20], [77.0384, 389.6], [78.0417, 30], [79.0539, 59.9], " + 
				"[81.0698, 69.9], [91.0541, 709.3], [92.0568, 69.9], [93.0335, 99.9], [93.057, 99.9], " + 
				"[93.0687, 40], [94.04, 319.7], [94.0627, 30], [95.0492, 179.8], [96.0526, 10], " + 
				"[96.0526, 20], [103.054, 20], [104.0569, 20], [105.0345, 20], [107.0495, 119.9], " + 
				"[108.0451, 89.9], [108.0808, 20], [109.0644, 40], [117.0576, 20], [118.0649, 50], " + 
				"[119.0493, 79.9], [120.0534, 10], [122.0713, 20], [123.0439, 50], [135.0804, 20], " + 
				"[136.0759, 89.9], [147.0449, 20], [165.0551, 59.9], [182.0818, 99.9], [204.0633, 10]]";

		Collection<MsPoint>unknownSpectrum = convertArrayToMsPoints(spectrumString);
		Collection<MsPoint>librarySpectrum = convertArrayToMsPoints(libSpectrumString);
		
//		Collection<MsPoint>unkAvgSpectrum = 
//				MsUtils.averageAndDenoiseMassSpectrum(
//						unknownSpectrum, mzWindowValue, massErrorType, relIntNoiseCutoff);
//		Collection<MsPoint>libAvgSpectrum = 
//				MsUtils.averageAndDenoiseMassSpectrum(
//						librarySpectrum, mzWindowValue, massErrorType, relIntNoiseCutoff);
//		
//		MsPoint[] unkWeighted = MSMSScoreCalculator.createEntropyWeigtedPattern(unkAvgSpectrum);
//		MsPoint[] libWeighted = MSMSScoreCalculator.createEntropyWeigtedPattern(libAvgSpectrum);
//		
//		for(MsPoint p : unkWeighted) {
//			System.err.println(MsUtils.spectrumMzExportFormat.format(p.getMz()) + 
//					"\t" + MsUtils.spectrumMzExportFormat.format(p.getIntensity()));
//		}
//		System.err.println("**********");
//		for(MsPoint p : libWeighted) {
//			System.err.println(MsUtils.spectrumMzExportFormat.format(p.getMz()) + 
//					"\t" + MsUtils.spectrumMzExportFormat.format(p.getIntensity()));
//		}
		
//		String unkEnt = MsUtils.spectrumMzExportFormat.format(
//				MsUtils.calculateSpectrumEntropyNatLog(unkAvgSpectrum));		
//		String libEnt = MsUtils.spectrumMzExportFormat.format(
//				MsUtils.calculateSpectrumEntropyNatLog(libAvgSpectrum));
		
//		String pyString = MsUtils.getSpectrumAsPythonArray(unkAvgSpectrum);
//		System.err.println("Unk: " + unkEnt);
//		System.err.println("Lib: " + libEnt);
		
		double score = MSMSScoreCalculator.calculateEntropyBasedMatchScore(			
				unknownSpectrum, 
				librarySpectrum,
				mzWindowValue, 
				massErrorType,
				relIntNoiseCutoff);
		System.err.println("Score " + MsUtils.spectrumMzExportFormat.format(score));
	}
	
	private static Collection<MsPoint>convertArrayToMsPoints(String spectrumString){
		
		String[]numList = spectrumString.replaceAll("[\\[\\]]", "").split(", ");
		ArrayList<MsPoint>unknownSpectrum = new ArrayList<MsPoint>();
		for(int i=0; i<numList.length-1; i=i+2) {
			MsPoint p = new MsPoint(Double.parseDouble(numList[i]), Double.parseDouble(numList[i+1]));
			unknownSpectrum.add(p);
		}
		return unknownSpectrum;
	}
}
