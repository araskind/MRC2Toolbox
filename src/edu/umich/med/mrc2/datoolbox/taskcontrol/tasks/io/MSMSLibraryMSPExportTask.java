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
import java.sql.Connection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSComponentTableFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSResolution;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSLibraryUtils;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTReferenceLibraries;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class MSMSLibraryMSPExportTask extends AbstractTask {

	private Collection<MsMsLibraryFeature>featuresToExport;
	private File exportFile;
	private static final DecimalFormat intensityFormat = new DecimalFormat("###");
	private static final DateFormat dateFormat = 
			new SimpleDateFormat(MRC2ToolBoxConfiguration.DATE_TIME_FORMAT_DEFAULT);
	
	private String libraryId;
	private Polarity polarity;
	private MSMSResolution resolution;
	private SpectrumSource spectrumSource;

	public MSMSLibraryMSPExportTask(
			String libraryId, 
			Polarity polarity, 
			MSMSResolution resolution, 
			SpectrumSource spectrumSource,
			File exportFile) {
		super();
		this.exportFile = exportFile;
		this.libraryId = libraryId;
		this.polarity = polarity;
		this.resolution = resolution;
		
		this.exportFile =
				FIOUtils.changeExtension(
					exportFile, MsLibraryFormat.MSP.getFileExtension());
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			//	Filter features that have MSMS
			fetchLibraryFeatures();
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}		
		try {
			//	Filter features that have MSMS
			writeMspFile();
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
	}

	private void fetchLibraryFeatures() throws Exception {
		
		taskDescription = "Getting library features from database";
		total = 100;
		processed = 20;
		featuresToExport = new ArrayList<MsMsLibraryFeature>();
		
		Connection conn = ConnectionManager.getConnection();
		Collection<String>libIds = MSMSLibraryUtils.getFilteredLibraryIds(
				 libraryId, 
				 polarity, 
				 resolution, 
				 spectrumSource,
				 conn);
		
		if(!libIds.isEmpty()) {
			
			total = libIds.size();
			
			for(String mrc2msmsId : libIds) {
				MsMsLibraryFeature feature = MSMSLibraryUtils.getMsMsLibraryFeatureById(mrc2msmsId, conn);
				featuresToExport.add(feature);		
				processed++;
			}			
		}	
		ConnectionManager.releaseConnection(conn);	
	}

	private void writeMspFile() throws IOException {

		if(featuresToExport.isEmpty())
			return;
		
		taskDescription = "Wtiting MSP output";
		total = featuresToExport.size();
		processed = 0;
		
		Collection<MSPField>individual = new ArrayList<MSPField>();
		individual.add(MSPField.NAME);
		individual.add(MSPField.FORMULA);
		individual.add(MSPField.EXACT_MASS);
		individual.add(MSPField.MW);
		individual.add(MSPField.INCHI_KEY);
		individual.add(MSPField.PRECURSORMZ);
		individual.add(MSPField.NUM_PEAKS);
			
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));
		for(MsMsLibraryFeature feature : featuresToExport) {
	
			CompoundIdentity cid = feature.getCompoundIdentity();
			writer.append(MSPField.NAME.getName() + ": " + cid.getName() + "\n");

			if (cid.getFormula() != null)
				writer.append(MSPField.FORMULA.getName() + ": " + cid.getFormula() + "\n");
			writer.append(MSPField.EXACT_MASS.getName() + ": "
					+ MRC2ToolBoxConfiguration.getMzFormat().format(cid.getExactMass()) + "\n");
			writer.append(MSPField.MW.getName() + ": " + 
					Integer.toString((int) Math.round(cid.getExactMass())) + "\n");
			if (cid.getInChiKey() != null)
				writer.append(MSPField.INCHI_KEY.getName() + ": " + cid.getInChiKey() + "\n");

			Map<String, String> properties = feature.getProperties();
			if((libraryId.contentEquals(NISTReferenceLibraries.nist_msms.name()) || 
					libraryId.contentEquals(NISTReferenceLibraries.hr_msms_nist.name()))
					&& properties.get(MSMSComponentTableFields.ORIGINAL_LIBRARY_ID.getName()) != null)
				writer.append(MSPField.NIST_NUM.getName() + ": " + 
						properties.get(MSMSComponentTableFields.ORIGINAL_LIBRARY_ID.getName()) + "\n");
							
			for (MSMSComponentTableFields field : MSMSComponentTableFields.values()) {

				if (individual.contains(field.getMSPField()))
					continue;
				
				String prop = properties.get(field.getName());
				if(prop == null || prop.isEmpty())
					continue;
					
				writer.append(field.getMSPField().getName() + ": " + prop + "\n");					
			}
			writer.append(MSPField.PRECURSORMZ.getName() + ": "
					+ MRC2ToolBoxConfiguration.getMzFormat().format(feature.getParent().getMz()) + "\n");
			writer.append(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(feature.getSpectrum().size()) + "\n");

			for(MsPoint point : feature.getSpectrum()) {

				writer.append(
					MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz())
					+ " " + intensityFormat.format(point.getIntensity())) ;
				
				String annotation = feature.getMassAnnotations().get(point);
				if(annotation != null)
					writer.append(" \"" + annotation + "\"");

				writer.append("\n");
			}
			writer.append("\n\n");		
			processed++;
		}
		writer.flush();
		writer.close();
	}

	@Override
	public Task cloneTask() {
		return new MSMSLibraryMSPExportTask(
				 libraryId, 
				 polarity, 
				 resolution, 
				 spectrumSource,
				 exportFile);
	}
}














