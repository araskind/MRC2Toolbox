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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.data.AdductMatch;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSubject;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.enums.DataExportFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MWtabReportStyle;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.StandardFactors;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.data.lims.IdTrackerOrganization;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSAcquisitionDetails;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProject;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProtocol;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCache;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DataExportUtils;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.MapUtils;

public class MWTabReportTask extends AbstractTask {

	public static final String MWTAB_EXTENSION = "mwtab";
	public static final String TXT_EXTENSION = "txt";
	public static final String TAB = "\t";
	public static final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
	public static final String PEAK_AREA = "Peak area";

	private File reportFile;
	private DataAnalysisProject experiment;
	private DataPipeline dataPipeline;
	private IdTrackerOrganization mrcOrganization;
	private LIMSUser mrc2user;
	private LIMSUser projectOwner;
	private LIMSProject limsProject;
	private LIMSExperiment limsExperiment;
	private int reportVersion;
	private ExperimentDesign design;
	private ExperimentDesignSubset experimentDesignSubset;
	private Collection<ExperimentalSample>activeSamples;
	private Collection<ExperimentalSubject>subjects;
	private LIMSAcquisitionDetails acquisitionDetails;
	private File samplePrepSop;
	MWtabReportStyle reportStyle;

	private enum SOPCategory{

		SAMPLE_COLLECTION,
		SAMPLE_PREPARATION,
		DATA_ACQUISITION,
		DATA_ANALYSIS,
		INSTRUMENT_MAINTENANCE,
		ASSAY_DESCRIPTION,
		;
	}

	private enum AnnotationFields{

		metabolite_name,
		mz,
		rt,
		pubchem_id,
		inchi_key,
		kegg_id,
		other_id,
		other_id_type,
		id_confidence,
		binner_annotaton,
		binner_derivation,
		binner_corr_to_pi,
		binner_annotation_mass_error,
		binner_isotope_group,
		;
	}

	public MWTabReportTask(
			File reportFile,
			DataAnalysisProject project,
			DataPipeline dataPipeline,
			ExperimentDesignSubset experimentDesignSubset,
			MWtabReportStyle reportStyle) {
		super();
		this.reportFile = FIOUtils.changeExtension(reportFile, TXT_EXTENSION);
		this.experiment = project;
		this.dataPipeline = dataPipeline;
		this.experimentDesignSubset = experimentDesignSubset;
		this.reportStyle = reportStyle;
		reportVersion = 1;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Creating MWTab report for assay \"" + dataPipeline.getName() +"\"";
		initProjectData();
		try {
			final Writer writer = new BufferedWriter(new FileWriter(reportFile));
			createReportHeader(writer);
			createProjectBlock(writer);
			createStudyBlock(writer);
			createExperimentalSubjectBlock(writer);
			
			if(reportStyle.equals(MWtabReportStyle.METABOLOMICS_WORKBENCH))
				createStudyDesignBlock(writer);
			
			createSampleCollectionBlock(writer);
			createTreatmentBlock(writer);
			createSamplePreparationBlock(writer);
			createChromatographyBlock(writer);
			createAnalysisBlock(writer);
			createMassSpecBlock(writer);
			
			if(reportStyle.equals(MWtabReportStyle.METABOLOMICS_WORKBENCH)) {
				createMetaboliteDataBlock(writer);
				createMetaboliteAnnotationsBlock(writer);			
			}
			writer.append("#END\n");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void initProjectData() {

		design = experiment.getExperimentDesign();
		limsProject = experiment.getLimsProject();
		if (limsProject.getClient().getPrincipalInvestigator() != null)
			projectOwner = limsProject.getClient().getPrincipalInvestigator();

		if(projectOwner == null) {

			if (limsProject.getContactPerson() != null)
				projectOwner = limsProject.getContactPerson();
			else {
				if (limsProject.getClient().getContactPerson() != null)
					projectOwner = limsProject.getClient().getContactPerson();
			}
		}
		limsExperiment = experiment.getLimsExperiment();
		mrcOrganization = LIMSDataCache.getOrganizationById(
				LIMSUtils.MRC2_IDT_ORGANIZATION_ID);
		mrc2user = LIMSDataCache.getUserById(LIMSUtils.MRC2_ADMIN_ID);
		activeSamples = design.getActiveSamplesForDesignSubsetAndDataPipeline(dataPipeline, experimentDesignSubset);
		try {
			subjects = LIMSUtils.getSubjectListForExperiment(limsExperiment.getId());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//		TODO change to real Acq method details
//		try {
//			
//			acquisitionDetails = LIMSUtils.getAcquisitionDetails(
//					limsExperiment.getId(), dataPipeline.getAcquisitionMethod());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		samplePrepSop = getSamplePrepProtocol();
	}

	private File getSamplePrepProtocol() {

		LIMSProtocol prepProtocol = null;
		File sopFile = null;
		try {
			prepProtocol = LIMSUtils.getAssaySop(dataPipeline.getAssay(), SOPCategory.SAMPLE_PREPARATION.name());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(prepProtocol != null) {

			try {
				sopFile = LIMSUtils.getSopProtocolFile(prepProtocol, reportFile.getParentFile());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sopFile;
	}

	private void appendDataBlock(
			String prefix,
			int prefixWidth,
			String data,
			int maxdataWidth,
			String separator,
			Writer writer) throws IOException {

		String prefixAdjusted = StringUtils.rightPad(prefix, prefixWidth);
		if(data == null) {
			writer.append(prefixAdjusted + separator + "\n");
			return;
		}
		String dataClean = data.replaceAll("(\\r|\\n)", " ");
		if(dataClean.length() > maxdataWidth) {
			String[] dataLines = StringUtils.split(WordUtils.wrap(dataClean, maxdataWidth, "\n", false), "\n");
			for(String line : dataLines)
				writer.append(prefixAdjusted + separator + line + "\n");
		}
		else {
			writer.append(prefixAdjusted + separator + dataClean + "\n");
		}
	}

	private void createReportHeader(Writer writer) throws IOException {

		writer.append("#METABOLOMICS WORKBENCH\n");
		writer.append(StringUtils.rightPad("VERSION", 20) + "\t" +
				Integer.toString(reportVersion) + "\n");

		writer.append(StringUtils.rightPad("CREATED_ON", 20) + "\t" + dateFormat.format(new Date()) + "\n");
	}

	private void createProjectBlock(Writer writer) throws IOException {

		writer.append("#PROJECT\n");
		appendDataBlock("PR:PROJECT_TITLE", 20, limsProject.getName(), 80, TAB, writer);
		appendDataBlock("PR:PROJECT_TYPE", 20, "MS analysis", 80, TAB, writer);
		appendDataBlock("PR:PROJECT_SUMMARY", 20, limsProject.getDescription(), 80, TAB, writer);
		appendDataBlock("PR:INSTITUTE", 20, limsProject.getOrganization().getName(), 80, TAB, writer);
		appendDataBlock("PR:DEPARTMENT", 20, limsProject.getClient().getDepartment(), 80, TAB, writer);
		appendDataBlock("PR:LABORATORY", 20, limsProject.getClient().getLaboratory(), 80, TAB, writer);
		appendDataBlock("PR:LAST_NAME", 20, projectOwner.getLastName(), 80, TAB, writer);
		appendDataBlock("PR:FIRST_NAME", 20, projectOwner.getFirstName(), 80, TAB, writer);
		appendDataBlock("PR:ADDRESS", 20, limsProject.getOrganization().getAddress(), 80, TAB, writer);
		appendDataBlock("PR:EMAIL", 20, projectOwner.getEmail(), 80, TAB, writer);
		appendDataBlock("PR:PHONE", 20, projectOwner.getPhone(), 80, TAB, writer);
		appendDataBlock("PR:FUNDING_SOURCE", 20, limsExperiment.getNihGrant(), 80, TAB, writer);
		appendDataBlock("PR:PROJECT_COMMENTS", 20, limsProject.getNotes(), 80, TAB, writer);
		appendDataBlock("PR:PUBLICATIONS", 20, null, 80, TAB, writer);
	}

	private void createStudyBlock(Writer writer) throws IOException {

		writer.append("#STUDY\n");
		appendDataBlock("ST:STUDY_TITLE", 20, limsExperiment.getName(), 80, TAB, writer);
		appendDataBlock("ST:STUDY_TYPE", 20, "MS analysis", 80, TAB, writer);
		appendDataBlock("ST:STUDY_SUMMARY", 20, limsExperiment.getDescription(), 80, TAB, writer);
		appendDataBlock("ST:INSTITUTE", 20, mrcOrganization.getName(), 80, TAB, writer);
		appendDataBlock("ST:DEPARTMENT", 20, mrcOrganization.getDepartment(), 80, TAB, writer);
		appendDataBlock("ST:LABORATORY", 20, mrcOrganization.getLaboratory(), 80, TAB, writer);
		appendDataBlock("ST:LAST_NAME", 20, mrc2user.getLastName(), 80, TAB, writer);
		appendDataBlock("ST:FIRST_NAME", 20, mrc2user.getFirstName(), 80, TAB, writer);
		appendDataBlock("ST:ADDRESS", 20, mrcOrganization.getAddress(), 80, TAB, writer);
		appendDataBlock("ST:EMAIL", 20, mrc2user.getEmail(), 80, TAB, writer);
		appendDataBlock("ST:PHONE", 20, mrc2user.getPhone(), 80, TAB, writer);
		appendDataBlock("ST:SUBMIT_DATE", 20, dateFormat.format(limsExperiment.getStartDate()), 80, TAB, writer);
		appendDataBlock("ST:NUM_GROUPS", 20, Integer.toString(design.getTotalLevels()), 80, TAB, writer);
		appendDataBlock("ST:TOTAL_SUBJECTS", 20, Integer.toString(design.getSubjectCount()), 80, TAB, writer);
		appendDataBlock("ST:STUDY_COMMENTS", 20, limsExperiment.getNotes(), 80, TAB, writer);
	}

	private void createStudyDesignBlock(Writer writer) throws IOException {

		writer.append(StringUtils.rightPad("#SUBJECT_SAMPLE_FACTORS:", 33) +
			"\tSUBJECT(optional)[tab]SAMPLE[tab]FACTORS(NAME:VALUE pairs separated by |)[tab]Additional sample data\n");

		ExperimentDesignFactor subjectFactor = design.getFactorByName(StandardFactors.SUBJECT.getName());
		Collection<String>lineElements = new ArrayList<String>();
		Collection<String>factorElements = new ArrayList<String>();

		//	TODO temp; remove
		if(activeSamples.isEmpty())
			activeSamples = design.getSamples();

		for(ExperimentalSample s : activeSamples) {

			lineElements.clear();
			factorElements.clear();
			lineElements.add(StringUtils.rightPad("SUBJECT_SAMPLE_FACTORS", 33));
			if(subjectFactor !=null)
				lineElements.add(Objects.toString(s.getLevel(subjectFactor), ""));
			else
				lineElements.add("");
			lineElements.add(s.getId());
			for(ExperimentDesignFactor factor : experimentDesignSubset.getOrderedDesign().keySet()) {

				if(subjectFactor != null && !factor.equals(subjectFactor))
					factorElements.add(factor.getName() + ":" + Objects.toString(s.getLevel(factor), ""));
			}
			lineElements.add(StringUtils.join(factorElements, " | "));
			lineElements.add(s.getName());
			writer.append(StringUtils.join(lineElements, "\t") + "\n");
		}
	}

	private void createExperimentalSubjectBlock(Writer writer) throws IOException {

		writer.append("#SUBJECT\n");
		writer.append(StringUtils.rightPad("SU:SUBJECT_TYPE", 33) + "\n");

		if(subjects != null) {

			Map<String, Long> speciesMap = subjects.stream().
					collect(Collectors.groupingBy(ExperimentalSubject::getTaxonomyName, Collectors.counting()));

			if(speciesMap.size() > 0) {
				
				Entry<String, Long> topSpec = MapUtils.getTopEntryByValue(speciesMap, SortDirection.DESC);
				String speciesName = topSpec.getKey();
				writer.append(StringUtils.rightPad("SU:SUBJECT_SPECIES", 33) + TAB + Objects.toString(speciesName, "") + "\n");
			
				Integer taxId = null;
				Optional<ExperimentalSubject> subj = subjects.stream().filter(s -> s.getTaxonomyName().equals(speciesName)).findFirst();
				if(subj.isPresent())
					taxId = subj.get().getTaxonomyId();
	
				writer.append(StringUtils.rightPad("SU:TAXONOMY_ID", 33) + TAB + Objects.toString(taxId, "") + "\n");
			}
		}
		else {
			writer.append(StringUtils.rightPad("SU:SUBJECT_SPECIES", 33) + "\n");
			writer.append(StringUtils.rightPad("SU:TAXONOMY_ID", 33) + "\n");
		}
	}

	private void createSampleCollectionBlock(Writer writer) throws IOException {

		writer.append("#COLLECTION\n");
		appendDataBlock("CO:COLLECTION_SUMMARY", 33, null, 80, TAB, writer);
	}

	private void createTreatmentBlock(Writer writer) throws IOException {

		writer.append("#TREATMENT\n");
		appendDataBlock("TR:TREATMENT_SUMMARY", 33, null, 80, TAB, writer);
	}

	private void createSamplePreparationBlock(Writer writer) throws IOException {

		writer.append("#SAMPLEPREP\n");
		appendDataBlock("SP:SAMPLEPREP_SUMMARY", 33, null, 80, TAB, writer);

		String protocolFileName = null;
		if(samplePrepSop != null)
			protocolFileName = samplePrepSop.getName();

		appendDataBlock("SP:SAMPLEPREP_PROTOCOL_FILENAME", 33, protocolFileName, 80, TAB, writer);
	}

	private void createChromatographyBlock(Writer writer) throws IOException {

		writer.append("#CHROMATOGRAPHY\n");
		if(acquisitionDetails != null) {

			appendDataBlock("CH:CHROMATOGRAPHY_TYPE", 33, acquisitionDetails.getColumnChemistry(), 80, TAB, writer);
			appendDataBlock("CH:INSTRUMENT_NAME", 33, acquisitionDetails.getInstrument(), 80, TAB, writer);
			appendDataBlock("CH:COLUMN_NAME", 33, acquisitionDetails.getColumnName(), 80, TAB, writer);
			appendDataBlock("CH:METHODS_FILENAME", 33, null, 80, TAB, writer); //	TODO
		}
		else {
			appendDataBlock("CH:CHROMATOGRAPHY_TYPE", 33, null, 80, TAB, writer);
			appendDataBlock("CH:INSTRUMENT_NAME", 33, null, 80, TAB, writer);
			appendDataBlock("CH:COLUMN_NAME", 33, dataPipeline.getAcquisitionMethod().getColumn().getColumnName(), 80, TAB, writer);
			appendDataBlock("CH:METHODS_FILENAME", 33, dataPipeline.getAcquisitionMethod().getName(), 80, TAB, writer);
		}
	}

	private void createAnalysisBlock(Writer writer) throws IOException {

		writer.append("#ANALYSIS\n");
		if(acquisitionDetails != null) {

			appendDataBlock("AN:ANALYSIS_TYPE", 33, acquisitionDetails.getMsType(), 80, TAB, writer);
			appendDataBlock("AN:INSTRUMENT_NAME", 33, acquisitionDetails.getInstrument(), 80, TAB, writer);

			String protocolFileName = null;
			if(samplePrepSop != null)
				protocolFileName = samplePrepSop.getName();

			appendDataBlock("AN:ANALYSIS_PROTOCOL_FILE", 33, protocolFileName, 80, TAB, writer);
			appendDataBlock("AN:ACQUISITION_PARAMETERS_FILE", 33, null, 80, TAB, writer); //	TODO
			appendDataBlock("AN:PROCESSING_PARAMETERS_FILE", 33, null, 80, TAB, writer); //	TODO
		}
		else {
			appendDataBlock("AN:ANALYSIS_TYPE", 33, "MS", 80, TAB, writer);
			appendDataBlock("AN:INSTRUMENT_NAME", 33, null, 80, TAB, writer);
			appendDataBlock("AN:ANALYSIS_PROTOCOL_FILE", 33, null, 80, TAB, writer);
			appendDataBlock("AN:ACQUISITION_PARAMETERS_FILE", 33, dataPipeline.getAcquisitionMethod().getName(), 80, TAB, writer);
			appendDataBlock("AN:PROCESSING_PARAMETERS_FILE", 33, dataPipeline.getDataExtractionMethod().getName(), 80, TAB, writer);
		}
	}

	private void createMassSpecBlock(Writer writer) throws IOException {

		writer.append("#MS\n");
		if(acquisitionDetails != null) {
			appendDataBlock("MS:INSTRUMENT_TYPE", 33, acquisitionDetails.getMassAnalyzer(), 80, TAB, writer);
			appendDataBlock("MS:INSTRUMENT_NAME", 33, acquisitionDetails.getInstrument(), 80, TAB, writer);
			appendDataBlock("MS:MS_TYPE", 33, acquisitionDetails.getIonization_Type(), 80, TAB, writer);
			String methodPolarity = null;
			Polarity p = Polarity.getPolarityByCode(acquisitionDetails.getPolarity());
			if(p != null)
				methodPolarity = p.name();
			appendDataBlock("MS:ION_MODE", 33, methodPolarity, 80, TAB, writer);
		}
		else {
			appendDataBlock("MS:INSTRUMENT_TYPE", 33, null, 80, TAB, writer);
			appendDataBlock("MS:MS_TYPE", 33, null, 80, TAB, writer);
			appendDataBlock("MS:ION_MODE", 33, null, 80, TAB, writer);
		}
	}

	private void createMetaboliteDataBlock(Writer writer) throws IOException {

		writer.append("#MS_METABOLITE_DATA\n");
		appendDataBlock("MS_METABOLITE_DATA:UNITS", 33, PEAK_AREA, 80, TAB, writer); //	TODO units should come from data
		writer.append("MS_METABOLITE_DATA_START\n");

		final Matrix dataMatrix = experiment.getDataMatrixForDataPipeline(dataPipeline);
		if(dataMatrix == null) {
			writer.append("MS_METABOLITE_DATA_END\n");
			return;
		}
		// Create header
		TreeMap<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>>sampleFileMap =
				DataExportUtils.createSampleFileMapForDataPipeline(
						experiment, experimentDesignSubset, dataPipeline, DataExportFields.SAMPLE_EXPORT_ID);
		String[] columnList =
				DataExportUtils.createSampleColumnNameArrayForDataPipeline(
						sampleFileMap, DataExportFields.SAMPLE_EXPORT_ID, dataPipeline);

		String[] header = new String[columnList.length + 1];
		int columnCount = 0;
		header[columnCount] = "Samples";

		HashMap<DataFile, Integer> fileColumnMap =
				DataExportUtils.createFileColumnMap(sampleFileMap, columnCount);

		HashMap<DataFile, Long>matrixFileMap = new HashMap<DataFile, Long>();
		fileColumnMap.keySet().stream().forEach(f -> matrixFileMap.put(f, dataMatrix.getRowForLabel(f)));

		for(String columnName : columnList)
			header[++columnCount] = columnName.replaceAll("\\p{Punct}+", "-");

		writer.append(StringUtils.join(header, TAB) + "\n");

		Collection<MsFeature> msFeatureSet4export =
				experiment.getActiveFeatureSetForDataPipeline(dataPipeline).getFeatures();

		//	Write out data
		long[] coordinates = new long[2];
		total = msFeatureSet4export.size();
		processed = 0;

		for( MsFeature msf : msFeatureSet4export){

			String[] line = new String[header.length];
			String compoundName = msf.getName();
			if(msf.isIdentified()){

				compoundName = msf.getPrimaryIdentity().getName();
				if(msf.getPrimaryIdentity().getMsRtLibraryMatch() != null) {

					AdductMatch tam = msf.getPrimaryIdentity().getMsRtLibraryMatch().getTopAdductMatch();
					if(tam != null)
						compoundName += " (" + tam.getLibraryMatch().getName() + ")";
				}
			}
			columnCount = 0;
			line[columnCount] = compoundName;
			coordinates[1] = dataMatrix.getColumnForLabel(msf);
			for (Entry<ExperimentalSample, TreeMap<DataPipeline, DataFile[]>> entry : sampleFileMap.entrySet()) {

				for(DataFile df : entry.getValue().get(dataPipeline)) {

					coordinates[0] = matrixFileMap.get(df);
					double value = Math.round(dataMatrix.getAsDouble(coordinates));
					String valueString = Double.toString(value).replaceAll("\n|\r|\\u000a|\\u000d|\t|\\u0009|\\u00ad", " ");
					if (value == 0.0d)
						valueString =  "";

					line[fileColumnMap.get(df)] = valueString;
				}
			}
			writer.append(StringUtils.join(line, TAB) + "\n");
			processed++;
		}
		writer.append("MS_METABOLITE_DATA_END\n");
	}

	private void createMetaboliteAnnotationsBlock(Writer writer) throws IOException {

		writer.append("#METABOLITES\n");
		writer.append("METABOLITES_START\n");

		//	TODO
		writer.append(StringUtils.join(AnnotationFields.values(), "\t") + "\n");


		writer.append("METABOLITES_END\n");
	}



	@Override
	public Task cloneTask() {
		return new MWTabReportTask(
				reportFile,
				experiment,
				dataPipeline,
				experimentDesignSubset,
				reportStyle);
	}
}










