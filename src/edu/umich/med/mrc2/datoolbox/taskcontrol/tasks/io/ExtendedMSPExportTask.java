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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.MSMSExportUtils;

public class ExtendedMSPExportTask extends AbstractTask {

	private Collection<MSFeatureInfoBundle>featuresToExport;
	private File exportFile;
	private Map<String,Injection>injectionMap;

	public ExtendedMSPExportTask(
			Collection<MSFeatureInfoBundle> featuresToExport,
			File exportFile) {
		super();
		this.featuresToExport = featuresToExport;
		this.exportFile =
			FIOUtils.changeExtension(
				exportFile, MsLibraryFormat.MSP.getFileExtension());
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			//	Filter features that have MSMS
			List<MSFeatureInfoBundle> msmsFeatures = 
					featuresToExport.stream().
					filter(f -> Objects.nonNull(f.getMsFeature().getSpectrum())).
					filter(f -> Objects.nonNull(f.getMsFeature().getSpectrum().getExperimentalTandemSpectrum())).
					collect(Collectors.toList());
			if(msmsFeatures.isEmpty()) {
				setStatus(TaskStatus.FINISHED);
				return;
			}
			injectionMap = MSMSExportUtils.createInjectionMap(msmsFeatures);
			writeMspFile(msmsFeatures);
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
	}

	private void writeMspFile(List<MSFeatureInfoBundle> msmsFeatures) throws IOException {

		taskDescription = "Wtiting MSP output";
		total = msmsFeatures.size();
		processed = 0;
		Collection<String> mspOutput = new ArrayList<String>();

		for (MSFeatureInfoBundle bundle : msmsFeatures) {

			Injection injection = null;
			if (bundle.getInjectionId() != null)
				injection = injectionMap.get(bundle.getInjectionId());

			Collection<String> featureMSPBlock = 
					MSMSExportUtils.createFeatureMSPBlock(bundle, injection);
			mspOutput.addAll(featureMSPBlock);

			processed++;
		}
		try {
			Files.write(exportFile.toPath(), 
					mspOutput, 
					StandardCharsets.UTF_8, 
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Task cloneTask() {
		return new ExtendedMSPExportTask(
			featuresToExport, exportFile);
	}
}














