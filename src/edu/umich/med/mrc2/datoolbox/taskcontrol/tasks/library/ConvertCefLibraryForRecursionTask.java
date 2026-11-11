/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef.CEFProcessingTask;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class ConvertCefLibraryForRecursionTask extends CEFProcessingTask {

	private boolean combineAdducts;
	private Collection<LibraryMsFeature>excludedFeatures;

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
		filterFeaturesByIsotopicPatternQuality();

		taskDescription = "Writing out CEF library ... ";
		writeCefLibrary(
				libraryFeatureListForExport,
				combineAdducts,
				outputCefFile);

		if(!excludedFeatures.isEmpty())
			writeExcludedCef();
				
		setStatus(TaskStatus.FINISHED);
	}

	private void filterFeaturesByIsotopicPatternQuality() {
		
		taskDescription = 
				"Filtering out features with low quality isotopic patterns... ";
		total = libraryFeatureListForExport.size();
		processed = 0;
		Collection<LibraryMsFeature>filtered = new HashSet<LibraryMsFeature>();
		excludedFeatures = new ArrayList<>();
		Set<Adduct>toRemove = new HashSet<>();
		Set<Adduct>toRemoveFromExcluded = new HashSet<>();
		for(LibraryMsFeature msf : libraryFeatureListForExport) {
			
			toRemove.clear();
			toRemoveFromExcluded.clear();
			for(Adduct adduct : msf.getSpectrum().getAdducts()){
				
				if(msf.getSpectrum().getMsPointsForAdduct(adduct).size() < 2)
					toRemove.add(adduct);
				else {
					if(!isIsotopicPatternLikely(msf.getSpectrum().getMsForAdduct(adduct))) {
						toRemove.add(adduct);
					}
					else {
						toRemoveFromExcluded.add(adduct);
					}
				}
			}
			if(!toRemove.isEmpty()) {
				
				LibraryMsFeature copy = new LibraryMsFeature(msf);
				for(Adduct adduct : toRemoveFromExcluded)
					copy.getSpectrum().removeSpectrumForAdduct(adduct);
				
				excludedFeatures.add(copy);
					
				for(Adduct adduct : toRemove)					
					msf.getSpectrum().removeSpectrumForAdduct(adduct);
			}
			if(!msf.getSpectrum().getAdducts().isEmpty())
				filtered.add(msf);
						
			processed++;
		}
		libraryFeatureListForExport = filtered;
	}
	
	private boolean isIsotopicPatternLikely(MsPoint[]pattern) {
		
		if(pattern[0].getIntensity() < pattern[1].getIntensity())
			return false;
		
		//	For now just check for long alternating intensity patterns
		if(pattern.length > 4) {
			
			for(int i=1; i<pattern.length-1; i++) {
				
				if(pattern[i].getIntensity() < pattern[i+1].getIntensity())
					return false;
			}
		}		
		return true;
	}
	
	protected void writeDebugCefLibrary(
			Collection<LibraryMsFeature>exportTargetList,
			File outputFile) {

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

			boolean createNewId = false;
			for(Adduct adduct : lt.getSpectrum().getAdducts()){

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
	
	private void writeExcludedCef() {
		
		taskDescription = "Writing out rejected features... ";
		total = 100;
		processed = 80;
		String outputPath = FilenameUtils.removeExtension(outputCefFile.getAbsolutePath()) 
				+ "_rejected_features." + MsLibraryFormat.CEF.getFileExtension();
		
		writeCefLibrary(
				excludedFeatures,
				true,
				new File(outputPath));
	}
	
	@Override
	public Task cloneTask() {

		return new ConvertCefLibraryForRecursionTask(
				inputCefFile,
				outputCefFile,
				combineAdducts);
	}
}









































