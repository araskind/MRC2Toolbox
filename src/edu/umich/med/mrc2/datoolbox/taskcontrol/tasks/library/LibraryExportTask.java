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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.gui.library.LibraryFeatureTableModel;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef.CEFProcessingTask;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class LibraryExportTask extends CEFProcessingTask {
	
	private CompoundLibrary currentLibrary;
	private Collection<LibraryMsFeature>targetSubset;
	private Collection<MsFeature>featureSubset;
	private MsLibraryFormat libraryFormat;
	private boolean combineAdducts;
	private Map<LibraryMsFeature,MsFeatureStatisticalSummary>statsMap;

	private static final String lineSeparator = System.getProperty("line.separator");

	public LibraryExportTask(
			File sourceLibraryFile,
			File outputLibraryFile,
			boolean combineAdducts,
			CompoundLibrary currentLibrary,
			Collection<LibraryMsFeature> targetSubset,
			Collection<MsFeature>featureSubset,
			MsLibraryFormat libraryFormat) {

		super();
		this.inputCefFile = sourceLibraryFile;
		this.outputCefFile = outputLibraryFile;
		this.combineAdducts = combineAdducts;
		this.currentLibrary = currentLibrary;
		this.targetSubset = targetSubset;
		this.featureSubset = featureSubset;
		this.libraryFormat = libraryFormat;
		
		statsMap =  new HashMap<LibraryMsFeature,MsFeatureStatisticalSummary>();
		
		if(!FilenameUtils.getExtension(outputCefFile.getName()).equalsIgnoreCase(libraryFormat.getFileExtension()))
			outputCefFile = FIOUtils.changeExtension(outputCefFile, libraryFormat.getFileExtension());		
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		
		if(currentLibrary != null)
			taskDescription = "Exporting library '" + 
				currentLibrary.getLibraryName() + "' to " + libraryFormat.getName();
		else
			taskDescription = "Exporting features to " + libraryFormat.getName();

		createTargetList();
		if(!unmatchedAdducts.isEmpty()){
			setStatus(TaskStatus.ERROR);
			return;
		}
		total = libraryFeatureListForExport.size();
		processed = 0;

		if(libraryFormat.equals(MsLibraryFormat.CEF)){
			try {
				writeCefLibrary(libraryFeatureListForExport, combineAdducts);
			} catch (Exception e) {
				reportErrorAndExit(e);
				return;				
			}
		}
		if(libraryFormat.equals(MsLibraryFormat.MSP))
			writeMspLibrary();

		if(libraryFormat.equals(MsLibraryFormat.TSV)) {
			try {
				writeTextLibrary();
			} catch (IOException e) {
				reportErrorAndExit(e);
				return;	
			}
		}
		if(libraryFormat.equals(MsLibraryFormat.IDTRACKER)){
			
			try {
				writeIDTrackerLibrary();
			} catch (Exception e) {
				reportErrorAndExit(e);
				return;	
			}
		}
		setStatus(TaskStatus.FINISHED);
	}

	private Collection<LibraryMsFeature> convertFeaturesToTargets(Collection<MsFeature> featureSubset2) {

		total = featureSubset2.size();
		processed = 0;
		Collection<LibraryMsFeature>convertedFeatures = new ArrayList<LibraryMsFeature>();
		for(MsFeature f : featureSubset2){

			LibraryMsFeature lt = new LibraryMsFeature(f);

			//	Correct RT
			if(f.getStatsSummary().getMedianObservedRetention() > 0.0d)
				lt.setRetentionTime(f.getStatsSummary().getMedianObservedRetention());

			statsMap.put(lt, f.getStatsSummary());
			convertedFeatures.add(lt);
			processed++;
		}
		return convertedFeatures;
	}
	
	private void createTargetList() {

		libraryFeatureListForExport = new ArrayList<LibraryMsFeature>();
		if(inputCefFile != null){
			
			createLibraryFeaturetListFromCefFile();
			
			if(libraryFeatureListForExport == null || libraryFeatureListForExport.isEmpty()) {			
				errorMessage = "Failed to parse input file, no features to export";
				setStatus(TaskStatus.ERROR);
				return;
			}
		}
		//	Export complete library
		if(targetSubset == null && featureSubset == null && currentLibrary != null) {

			List<LibraryMsFeature> features = currentLibrary.getFeatures().stream()
			    	.filter(LibraryMsFeature.class::isInstance)
			    	.map(LibraryMsFeature.class::cast)
			    	.filter(f -> f.isActive())
			    	.collect(Collectors.toList());

			libraryFeatureListForExport.addAll(features);
		}
		//	Export filtered library
		if(targetSubset != null)
			libraryFeatureListForExport.addAll(targetSubset);

		//	Export feature subset
		if(featureSubset != null){

			Collection<LibraryMsFeature>convertedFeatures = 
					convertFeaturesToTargets(featureSubset);
			libraryFeatureListForExport.addAll(convertedFeatures);
		}
		libraryFeatureListForExport = libraryFeatureListForExport.stream().
				sorted(new MsFeatureComparator(SortProperty.RT)).
				collect(Collectors.toSet());
	}


	private void writeTextLibrary() throws IOException {

		total = libraryFeatureListForExport.size();
		processed = 0;
		final Writer writer = new BufferedWriter(new FileWriter(outputCefFile));
		char columnSeparator = MRC2ToolBoxConfiguration.getTabDelimiter();

		//	Create header
		String[] header = new String[] {
			LibraryFeatureTableModel.ID_COLUMN,
			LibraryFeatureTableModel.ID_CONFIDENCE_COLUMN,
			LibraryFeatureTableModel.FEATURE_COLUMN,
			LibraryFeatureTableModel.COMPOUND_NAME_COLUMN,
			LibraryFeatureTableModel.FORMULA_COLUMN,
			LibraryFeatureTableModel.MASS_COLUMN,
			LibraryFeatureTableModel.CHARGE_COLUMN,
			LibraryFeatureTableModel.RT_COLUMN,
			LibraryFeatureTableModel.QC_COLUMN
		};
		writer.append(StringUtils.join(header, columnSeparator));
		writer.append(lineSeparator);

		//	Write feature data
		for(LibraryMsFeature lf : libraryFeatureListForExport){

			MsFeatureIdentity identity = lf.getPrimaryIdentity();

			String formula = "";
			String linkLabel = "";
			int innateCharge = 0;

			if(identity != null) {
				formula = identity.getCompoundIdentity().getFormula();
				String smiles = identity.getCompoundIdentity().getSmiles();

				if(smiles != null)
					innateCharge = StringUtils.countMatches(smiles, "+") - StringUtils.countMatches(smiles, "-");

				linkLabel = identity.getPrimaryLinkLabel();
			}
			String compoundName = lf.getName();
			CompoundIdentificationConfidence idc = null;
			if(lf.getPrimaryIdentity() != null) {

				compoundName = lf.getPrimaryIdentity().getCompoundName();
				idc= lf.getPrimaryIdentity().getConfidenceLevel();
			}
			String[] line = new String[] {
				linkLabel,
				idc.getName(),
				lf.getName(),
				compoundName,
				formula,
				MRC2ToolBoxConfiguration.getMzFormat().format(lf.getNeutralMass()),
				Integer.toString(innateCharge),
				MRC2ToolBoxConfiguration.getRtFormat().format(lf.getRetentionTime()),
				Boolean.toString(lf.isQcStandard())
			};
			writer.append(StringUtils.join(line, columnSeparator));
			writer.append(lineSeparator);
		}
		writer.flush();
		writer.close();
	}
	
	private void writeIDTrackerLibrary() throws Exception {
		
		taskDescription = "Writing librarys in IDTracker XML format ... ";
		total = 100;
		processed = 20;
		
		Document libDocument = new Document();
		Element featureListElement =  currentLibrary.getXmlElement();
		libDocument.setRootElement(featureListElement);
		XmlUtils.writeCompactXMLtoFile(libDocument, outputCefFile);			
	}

	private void writeMspLibrary() {
		// TODO Auto-generated method stub
		total = libraryFeatureListForExport.size();
		processed = 0;
	}
	
	@Override
	public Task cloneTask() {

		return new LibraryExportTask(
				inputCefFile,
				outputCefFile,
				combineAdducts,
				currentLibrary,
				targetSubset,
				featureSubset,
				libraryFormat);
	}
}









































