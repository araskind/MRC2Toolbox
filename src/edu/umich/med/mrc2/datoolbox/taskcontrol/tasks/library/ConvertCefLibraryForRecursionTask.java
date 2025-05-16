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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library;

import java.io.File;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.openscience.cdk.formula.MolecularFormulaGenerator;
import org.openscience.cdk.formula.MolecularFormulaRange;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSScoreCalculator;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef.CEFProcessingTask;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;
import edu.umich.med.mrc2.datoolbox.utils.isopat.IsotopicPatternUtils;

public class ConvertCefLibraryForRecursionTask extends CEFProcessingTask {

	private boolean combineAdducts;
	private TreeSet<String> unmatchedAdducts;

	public ConvertCefLibraryForRecursionTask(			
			File sourceLibraryFile,
			File outputLibraryFile,
			boolean combineAdducts) {

		this.inputCefFile = sourceLibraryFile;
		this.outputCefFile = outputLibraryFile;
		this.combineAdducts = combineAdducts;
		unmatchedAdducts = new TreeSet<String>();
	}

	@Override
	public void run() {

		taskDescription = 
				"Creating library for recursion from " + inputCefFile.getName();
		setStatus(TaskStatus.PROCESSING);
		createLibraryFeaturetListFromCefFile();
		if(!unmatchedAdducts.isEmpty()){
			errorMessage = "Unmatched adducts: " + 
					StringUtils.join(unmatchedAdducts, "; ");
			setStatus(TaskStatus.ERROR);
			return;
		}
		if(libraryFeatureListForExport == null || libraryFeatureListForExport.isEmpty()) {			
			errorMessage = "Failed to parse input file, no features to export";
			setStatus(TaskStatus.ERROR);
			return;
		}
		try {
			filterFeaturesByIsotopicPatternQuality();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			writeCefLibrary(
					libraryFeatureListForExport,
					combineAdducts);
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void filterFeaturesByIsotopicPatternQuality() {
		
		taskDescription = 
				"Filtering out features with low quality isotopic patterns... ";
		total = libraryFeatureListForExport.size();
		processed = 0;
		Collection<LibraryMsFeature>filtered = new HashSet<LibraryMsFeature>();
		double error = 0.3d;
		MolecularFormulaRange ranges = 
				IsotopicPatternUtils.createElementRanges4SimpleIsoPatternScoring();
		IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
		MolecularFormulaGenerator mfg = null;
		//	Adduct adduct = AdductManager.getDefaultAdductForPolarity(Polarity.Neutral);
		NumberFormat mzFormat = MRC2ToolBoxConfiguration.getMzFormat();
		
		//	Debug only
//		Map<Range,Collection<LibraryMsFeature>>featuresByScore = 
//				new TreeMap<Range,Collection<LibraryMsFeature>>();
//		featuresByScore.put(new Range(-1.0d, 0.1d), new HashSet<LibraryMsFeature>());
//		featuresByScore.put(new Range(0.1d, 0.3d), new HashSet<LibraryMsFeature>());
//		featuresByScore.put(new Range(0.3d, 0.5d), new HashSet<LibraryMsFeature>());
//		featuresByScore.put(new Range(0.5d, 0.7d), new HashSet<LibraryMsFeature>());
//		featuresByScore.put(new Range(0.7d, 1.1d), new HashSet<LibraryMsFeature>());
		//	End debug
		
		for(LibraryMsFeature msf : libraryFeatureListForExport) {
			
			mfg = new MolecularFormulaGenerator(
					builder, msf.getNeutralMass() - error, 
					msf.getNeutralMass() + error, ranges);
			IMolecularFormula firstFormula = mfg.getNextFormula();
			if(firstFormula == null) {
				
				System.out.println("No formula for\t" + msf.getName());
				processed++;
				continue;
			}
			firstFormula = MolecularFormulaManipulator.getMolecularFormula(
					MolecularFormulaManipulator.getString(firstFormula),builder);
			
			for(Adduct adduct : msf.getSpectrum().getAdducts()){

				Collection<MsPoint> msPoints = 
						MsUtils.calculateIsotopeDistribution(firstFormula, adduct, true);
				
				double score = MSMSScoreCalculator.calculateEntropyBasedMatchScore(
						msPoints, msf.getSpectrum().getMsPointsForAdduct(adduct), 350.0, MassErrorType.mDa, 
						MSMSScoreCalculator.DEFAULT_MS_REL_INT_NOISE_CUTOFF);
				
				//	Debug only
//				for(Entry<Range,Collection<LibraryMsFeature>>fEntry : featuresByScore.entrySet()) {
//					
//					if(fEntry.getKey().containsExcludingUpperBorder(score))
//						fEntry.getValue().add(msf);
//				}							
				//	End debug
			}
			filtered.add(msf);
			processed++;
		}

		//	Debug only
//		for(Entry<Range,Collection<LibraryMsFeature>>fEntry : featuresByScore.entrySet()) {
//			
//			String outName = "featuresForScoreRange_" + 
//					MRC2ToolBoxConfiguration.getPpmFormat().format(fEntry.getKey().getMin()) + "_" + 
//					MRC2ToolBoxConfiguration.getPpmFormat().format(fEntry.getKey().getMax()) + ".cef";
//			File outputFile = new File(outputCefFile.getParent(), outName);
//			try {
//				writeDebugCefLibrary(fEntry.getValue(), outputFile);
//			} catch (Exception e) {
//				e.printStackTrace();
//				setStatus(TaskStatus.ERROR);
//				return;
//			}
//		}
		//	End debug
		
		libraryFeatureListForExport = filtered;
	}
	
	protected void writeDebugCefLibrary(
			Collection<LibraryMsFeature>exportTargetList,
			File outputFile) throws Exception {

		total = exportTargetList.size();
		processed = 0;
		
		Document exportedLibraryDocument = new Document();
        Element cefRoot = new Element("CEF");
        cefRoot.setAttribute("version", "1.0.0.0");
		String libId = DataPrefix.MS_LIBRARY.getName() + UUID.randomUUID().toString();
		cefRoot.setAttribute("library_id", libId);
		exportedLibraryDocument.addContent(cefRoot);	
		Element compoundListElement = new Element("CompoundList");
		cefRoot.addContent(compoundListElement);

		for(LibraryMsFeature lt : exportTargetList){
			
			if(lt.getSpectrum() == null) {
				System.err.println(lt.getName() + " has no spectrum.");
				continue;
			}
			boolean createNewId = false;
			for(Adduct adduct : lt.getSpectrum().getAdducts()){

				//	TODO make this check optional
				if(lt.getSpectrum().getMsForAdduct(adduct).length > 1){

					Element compound = createCefCompoundElement(lt, adduct, createNewId);
					compoundListElement.addContent(compound);
				}
				createNewId = true;
			}			
			processed++;
		}
		String extension = FilenameUtils.getExtension(outputFile.getAbsolutePath());
		if(!extension.equalsIgnoreCase(MsLibraryFormat.CEF.getFileExtension()))
			outputFile = new File(FilenameUtils.removeExtension(outputFile.getAbsolutePath()) + "."
					+ MsLibraryFormat.CEF.getFileExtension());

		XmlUtils.writePrettyPrintXMLtoFile(exportedLibraryDocument, outputFile);
	}
	
	
	@Override
	public Task cloneTask() {

		return new ConvertCefLibraryForRecursionTask(
				inputCefFile,
				outputCefFile,
				combineAdducts);
	}
}









































