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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cpdcoll;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundCollectionComponent;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixture;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixtureComponent;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CpdMetadataField;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class MultiplexDataExportTask extends AbstractTask {

	
	private Collection<CompoundMultiplexMixture>selectedMultiplexes;	
	private Collection<CpdMetadataField>exportFields;
	private File outputFile;
	
	
	public MultiplexDataExportTask(
			Collection<CompoundMultiplexMixture> selectedMultiplexes,
			Collection<CpdMetadataField> exportFields, 			
			File outputFile) {
		super();
		this.selectedMultiplexes = selectedMultiplexes;
		this.exportFields = exportFields;
		this.outputFile = outputFile;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			writeExportFile();
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
	}
	
	private void writeExportFile() {
		
		taskDescription = "Wtiting output";		
		total = selectedMultiplexes.size();
		processed = 0;
		List<String>dataToExport = new ArrayList<String>();
		String header = createExportFileHeader();
		dataToExport.add(header);
		for(CompoundMultiplexMixture mix : selectedMultiplexes) {
			
			for(CompoundMultiplexMixtureComponent component : mix.getComponents()) {
				
				CompoundCollectionComponent ccComponent = component.getCCComponent();
				ArrayList<String>line = new ArrayList<String>();
				line.add(mix.getName());
				line.add(ccComponent.getId());
				for(CpdMetadataField field : exportFields) {
					
					String value = ccComponent.getMetadata().get(field);
					if(value == null)
						value = "";
					
					line.add(value);
				}
				dataToExport.add(StringUtils.join(line, 
						MRC2ToolBoxConfiguration.getTabDelimiter()));
			}			
			processed++;
		}
		Path outputPath = Paths.get(outputFile.getAbsolutePath());
		try {
			Files.write(outputPath, 
					dataToExport, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String createExportFileHeader(){
		
		ArrayList<String>header = new ArrayList<String>();
		header.add("PlexID");
		header.add("ComponentID");
		exportFields.stream().forEach(f -> header.add(f.getName()));
		return StringUtils.join(header, 
				MRC2ToolBoxConfiguration.getTabDelimiter());
	}

	@Override
	public Task cloneTask() {

		return new MultiplexDataExportTask(
				selectedMultiplexes,
				exportFields, 			
				outputFile);
	}

	public File getOutputFile() {
		return outputFile;
	}
}
