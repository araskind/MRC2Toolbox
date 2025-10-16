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

package edu.umich.med.mrc2.datoolbox.rqc;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.FilePreferencesFactory;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class BatchMatch2MetabCombinerResultsComparator {

	
	public static void main(String[] args) {

		System.setProperty("java.util.prefs.PreferencesFactory", 
				FilePreferencesFactory.class.getName());
		System.setProperty(FilePreferencesFactory.SYSTEM_PROPERTY_FILE, 
				MRC2ToolBoxCore.configDir + "MRC2ToolBoxPrefs.txt");
		MRC2ToolBoxConfiguration.initConfiguration();

		try {
			compareEX01094RPPOSresults();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void compareEX01094RPPOSresults() {

		File batchMatchFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01242 - preCovid adipose Shipment W20000044X\\"
				+ "A049 - Central carbon metabolism profiling\\Documents\\4MetabCombinner\\EX01242-IONP-NEG-BM-aligned-data.txt");
		File metabCombinerFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01242 - preCovid adipose Shipment W20000044X\\"
				+ "A049 - Central carbon metabolism profiling\\Documents\\4MetabCombinner\\EX01242-IONP-NEG-MC-aligned-data.txt");	
		File outputFile = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01242 - preCovid adipose Shipment W20000044X\\"
				+ "A049 - Central carbon metabolism profiling\\Documents\\4MetabCombinner\\EX01242-IONP-NEG-BatchMatch-to-MetabCombiner-feature-overlay.txt");
		
		compareBatchMatchToMetabCombinerResults(
				batchMatchFeatureData, metabCombinerFeatureData, outputFile, 20.0d, 0.05d);
	}
	
	private static void compareEX01190IONPNEGresults() {

		File batchMatchFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\Report\\Final-IP-Neg\\"
				+ "Unnamed\\EX01190-IONP-NEG-BM-aligned-data.txt");
		File metabCombinerFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\"
				+ "Report\\Final-IP-Neg\\Unnamed\\EX01190-IONP-NEG-MC-aligned-data.txt");	
		File outputFile = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A049 - Central carbon metabolism profiling\\Report\\"
				+ "Final-IP-Neg\\Unnamed\\EX01190-IONP-NEG-BatchMatch-to-MetabCombiner-feature-overlay.txt");
		
		compareBatchMatchToMetabCombinerResults(
				batchMatchFeatureData, metabCombinerFeatureData, outputFile, 20.0d, 0.05d);
	}	
	
	private static void compareEX01190RPNEGresults() {
		
		File batchMatchFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A003 - Untargeted\\Documents\\NEG\\"
				+ "_4MetabCombiner\\ALL\\EX01190_RP_NEG_BatchMatch_features.txt");
		File metabCombinerFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A003 - Untargeted\\Documents\\NEG\\"
				+ "_4MetabCombiner\\ALL\\EX01190_RP_NEG_MetabCombiner_features.txt");
		File outputFile = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A003 - Untargeted\\Documents\\NEG\\"
				+ "_4MetabCombiner\\ALL\\EX01190-RP-NEG-BatchMatch-to-MetabCombiner-feature-overlay.txt");
		
		compareBatchMatchToMetabCombinerResults(
				batchMatchFeatureData, metabCombinerFeatureData, outputFile, 20.0d, 0.05d);
	}
	
	private static void compareEX01190RPPOSresults() {
		
		File batchMatchFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A003 - Untargeted\\Documents\\POS\\"
				+ "_4MetabCombiner\\CLEANED\\EX01190_RP_POS_BatchMatch_features.txt");
		File metabCombinerFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A003 - Untargeted\\Documents\\POS\\"
				+ "_4MetabCombiner\\CLEANED\\EX01190_RP_POS_HC_MetabCombiner_features.txt");		
		File outputFile = new File(
				"Y:\\DataAnalysis\\_Reports\\EX01190 - MoTrPAC\\A003 - Untargeted\\Documents\\POS\\"
				+ "_4MetabCombiner\\CLEANED\\EX01190-RP-POS-HC-BatchMatch-to-MetabCombiner-feature-overlay.txt");
		
		compareBatchMatchToMetabCombinerResults(
				batchMatchFeatureData, metabCombinerFeatureData, outputFile, 20.0d, 0.05d);
	}
	
	private static void compareEX01283RPPOSnoB2results() {
		
		File batchMatchFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\EX01283\\POS\\NO_BATCH02\\"
				+ "EX01283-RP-POS-BatchMatch-Aligned-feature-data.txt");
		File metabCombinerFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\EX01283\\POS\\NO_BATCH02\\"
				+ "EX01283-RP-POS-NOB2-MetabCombiner-Aligned-feature-data.txt");
		
		File outputFile = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\EX01283\\POS\\NO_BATCH02\\"
				+ "EX01283-RP-POS-NOB2-BatchMatch-to-MetabCombiner-feature-overlay.txt");
		compareBatchMatchToMetabCombinerResults(
				batchMatchFeatureData, metabCombinerFeatureData, outputFile, 20.0d, 0.05d);
	}
	
	private static void compareEX01283RPPOSresults() {
		
		File batchMatchFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\"
				+ "EX01283-RP-POS-BatchMatch-Aligned-feature-data.txt");
		File metabCombinerFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\"
				+ "EX01283-RP-POS-MetabCombiner-Aligned-feature-data.txt");
		
		File outputFile = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\"
				+ "EX01283-RP-POS-BatchMatch-to-MetabCombiner-feature-overlay.txt");
		compareBatchMatchToMetabCombinerResults(
				batchMatchFeatureData, metabCombinerFeatureData, outputFile, 20.0d, 0.05d);
	}
	
	private static void compareEX01283RPNEGresults() {
		
		File batchMatchFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\EX01283\\NEG\\"
				+ "EX01283-RP-NEG-BatchMatch-Aligned-feature-data.txt");
		File metabCombinerFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\EX01283\\NEG\\"
				+ "EX01283-RP-NEG-MetabCombiner-Aligned-feature-data.txt");
		
		File outputFile = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\EX01283\\NEG\\"
				+ "EX01283-RP-NEG-BatchMatch-to-MetabCombiner-feature-overlay.txt");
		compareBatchMatchToMetabCombinerResults(
				batchMatchFeatureData, metabCombinerFeatureData, outputFile, 20.0d, 0.05d);
	}
	
	private static void compareEX01010and1089RPNEGresults() {
		
		File batchMatchFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\EX01010and1089\\NEG\\"
				+ "EX01010-1089-RP-NEG-BatchMatch-Aligned-feature-data.txt");
		File metabCombinerFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\EX01010and1089\\NEG\\"
				+ "EX01010-1089-RP-NEG-MetabCombiner-Aligned-feature-data.txt");
		
		File outputFile = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\EX01010and1089\\NEG\\"
				+ "EX01010-1089-RP-NEG-BatchMatch-to-MetabCombiner-feature-overlay.txt");
		compareBatchMatchToMetabCombinerResults(
				batchMatchFeatureData, metabCombinerFeatureData, outputFile, 20.0d, 0.05d);
	}

	private static void compareEX01010and1089RPPOSresults() {
		
		File batchMatchFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\EX01010and1089\\POS\\"
				+ "EX01010-1089-RP-POS-BatchMatch-Aligned-feature-data.txt");
		File metabCombinerFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\"
				+ "EX01010and1089\\POS\\EX01010-1089-RP-POS-MetabCombiner-Aligned-feature-data.txt");

		File outputFile = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\EX01010and1089\\POS\\"
				+ "EX01010-1089-RP-POS-BatchMatch-to-MetabCombiner-feature-overlay.txt");
		compareBatchMatchToMetabCombinerResults(
				batchMatchFeatureData, metabCombinerFeatureData, outputFile, 20.0d, 0.05d);
	}
	
	private static void compareEX01010RPPOSresults() {
		
		File batchMatchFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\EX01010\\POS\\EX01010-RP-POS-BatchMatch-Aligned-feature-data.txt");
		File metabCombinerFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\EX01010\\POS\\EX01010-RP-POS-MetabCombiner-Aligned-feature-data.txt");
		File outputFile = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\EX01010\\POS\\"
				+ "EX01010-RP-POS-BatchMatch-to-MetabCombiner-feature-overlay.txt");
		compareBatchMatchToMetabCombinerResults(
				batchMatchFeatureData, metabCombinerFeatureData, outputFile, 20.0d, 0.05d);
	}
	
	private static void compareEX01010RPNEGresults() {
		
		File batchMatchFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\EX01010\\NEG\\EX01010-RP-NEG-BatchMatch-Aligned-feature-data.txt");
		File metabCombinerFeatureData = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\EX01010\\NEG\\EX01010-RP-NEG-MetabCombiner-Aligned-feature-data.txt");
		File outputFile = new File(
				"Y:\\DataAnalysis\\_Reports\\"
				+ "EX01010 - EX01089 - EX01235 - EX01283 - EX01392 - Starr County Metabolomics I-V\\"
				+ "Unnamed\\MetabCombiner\\EX01010\\NEG\\"
				+ "EX01010-RP-NEG-BatchMatch-to-MetabCombiner-feature-overlay.txt");
		compareBatchMatchToMetabCombinerResults(
				batchMatchFeatureData, metabCombinerFeatureData, outputFile, 20.0d, 0.05d);
	}
	
	private static void compareBatchMatchToMetabCombinerResults(
			File batchMatchFeatureData,
			File metabCombinerFeatureData,
			File outputFile,
			double mzTolerance,
			double rtTolerance) {
		
		Set<MinimalMSOneFeature>batchMatchFeatures = 
				readFeaturesFromFile(batchMatchFeatureData);
		Set<MinimalMSOneFeature>metabCombinerFeatures = 
				readFeaturesFromFile(metabCombinerFeatureData);
		
		//	Find metabCombiner features matching to BatchMatch features
		Map<MinimalMSOneFeature,List<MinimalMSOneFeature>>mc2bmMatchMap =  findMatches(
				batchMatchFeatures, metabCombinerFeatures, mzTolerance, rtTolerance);
		
		//	Find BatchMatch features matching to metabCombiner features
		Map<MinimalMSOneFeature,List<MinimalMSOneFeature>>bm2mcMatchMap =  findMatches(
				metabCombinerFeatures, batchMatchFeatures, mzTolerance, rtTolerance);
		
		List<String>report = new ArrayList<String>();
		report.add("Comparison cutoffs:");
		report.add("Mass tolerance, ppm: " + MRC2ToolBoxConfiguration.getPpmFormat().format(mzTolerance));
		report.add("RT tolerance, min: " + MRC2ToolBoxConfiguration.getRtFormat().format(rtTolerance));
		report.add("\n******************************************\n");
		report.add("# Of features aligned by BatchMatch: " + Integer.toString(batchMatchFeatures.size() - 1));
		report.add("# Of features aligned by MetabCombiner: " + Integer.toString(metabCombinerFeatures.size() - 1));
		
		double diff = ((double)(metabCombinerFeatures.size() - batchMatchFeatures.size())/
				(double)(batchMatchFeatures.size()-1)) * 100.0d;
		report.add("% Difference in # of aligned features: " 
				+ MRC2ToolBoxConfiguration.getPpmFormat().format(diff) + "%");
		
		double bmMatchPercent = ((double)(mc2bmMatchMap.size() )/(double)(batchMatchFeatures.size()-1)) * 100.0d;
		report.add("# Of features in BatchMatch alignment having a match in MetabCombiner alignment: "
				+ Integer.toString(mc2bmMatchMap.size()) + " - " 
				+ MRC2ToolBoxConfiguration.getPpmFormat().format(bmMatchPercent) + "%");
		
		double mcMatchPercent = ((double)(bm2mcMatchMap.size() )/(double)(metabCombinerFeatures.size()-1)) * 100.0d;
		report.add("# Of features in MetabCombiner alignment having a match in BatchMatch alignment: "
				+ Integer.toString(bm2mcMatchMap.size()) + " - " 
				+ MRC2ToolBoxConfiguration.getPpmFormat().format(mcMatchPercent) + "%");
		
		int bmNoMcMatch = batchMatchFeatures.size() - 1 - mc2bmMatchMap.size();
		report.add("# Of features in BatchMatch alignment WITHOUT a match in MetabCombiner alignment: "
				+ Integer.toString(bmNoMcMatch));
		
		int mcNoBmMatch = metabCombinerFeatures.size() - 1 - bm2mcMatchMap.size();
		report.add("# Of features in MetabCombiner alignment WITHOUT a match in BatchMatch alignment: "
				+ Integer.toString(mcNoBmMatch));

		try {
		    Files.write(outputFile.toPath(), 
		    		report,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	private static Map<MinimalMSOneFeature,List<MinimalMSOneFeature>> findMatches(
			Set<MinimalMSOneFeature>primarySet,
			Set<MinimalMSOneFeature>secondarySet,
			double mzTolerance,
			double rtTolerance) {
		
		Map<MinimalMSOneFeature,List<MinimalMSOneFeature>>matchMap = 
				new TreeMap<MinimalMSOneFeature,List<MinimalMSOneFeature>>();
		for(MinimalMSOneFeature bmFeature : primarySet) {
			
			Range mzRange = MsUtils.createPpmMassRange(bmFeature.getMz(), mzTolerance);
			Range rtRange = new Range(
					bmFeature.getRt() - rtTolerance, 
					bmFeature.getRt() + rtTolerance);
			
			List<MinimalMSOneFeature> matches = secondarySet.stream().
				filter(f -> mzRange.contains(f.getMz())).
				filter(f -> rtRange.contains(f.getRt())).
				collect(Collectors.toList());
			if(!matches.isEmpty())
				matchMap.put(bmFeature, matches);
		}
		return matchMap;
	}
	
	private static Set<MinimalMSOneFeature>readFeaturesFromFile(File inputFile){
		
		Set<MinimalMSOneFeature>features = new TreeSet<MinimalMSOneFeature>();
		String[][] featureData = null;
		try {
			featureData = DelimitedTextParser.parseTextFileWithEncoding(inputFile, '\t');
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(featureData != null && featureData.length > 1) {
			
			for(int i=1; i<featureData.length; i++) {
				
				String featureName = featureData[i][0];
				double mz = Double.parseDouble(featureData[i][1]);
				double rt = Double.parseDouble(featureData[i][2]);
				features.add(new MinimalMSOneFeature(featureName, mz, rt));
			}
		}		
		return features;
	}
	
}
