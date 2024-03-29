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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.SiriusMsMsCluster;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;

public abstract class IDTrackerSiriusMsExportTask extends AbstractTask {
	
	protected static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");	
	
	protected File outputFile;
	protected TreeMap<String, Injection> injectionMap;
	protected Collection<SiriusMsMsCluster> msmsclusters;

	public IDTrackerSiriusMsExportTask(File outputFile) {
		super();
		this.outputFile = outputFile;
	}
	
	protected void createInjectionMap(Collection<MSFeatureInfoBundle> msmsFeatures) {

		injectionMap = new TreeMap<String,Injection>();
		List<String> injIds = msmsFeatures.stream().
				map(f -> f.getInjectionId()).distinct().
				filter(i -> !i.equals(null)).sorted().
				collect(Collectors.toList());

		if(injIds.isEmpty())
			return;

		for(String id : injIds) {

			try {
				Injection injection = IDTUtils.getInjectionById(id);
				if(injection != null)
					injectionMap.put(id, injection);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	protected void writeMsFile() throws IOException {

		taskDescription = "Wtiting MS output";
		total = msmsclusters.size();
		processed = 0;
		final Writer writer = new BufferedWriter(new FileWriter(outputFile));
		for(SiriusMsMsCluster cluster : msmsclusters) {
			
			String mspBlock = cluster.getSiriusMsBlock();
			if(mspBlock != null) {
				
				writer.append(cluster.getSiriusMsBlock());
				writer.append("\n");
			}
			processed++;
		}
		writer.flush();
		writer.close();
	}

	protected String createComment(MSFeatureInfoBundle bundle) {

		String comment = MSPField.COMMENTS.getName() + ": ";
		comment += "RT "+ MRC2ToolBoxConfiguration.getRtFormat().format(
				bundle.getMsFeature().getRetentionTime()) + " min. | ";
		String injId = bundle.getInjectionId();
		if(injId != null) {
			Injection injection = injectionMap.get(injId);
			if(injection != null) {
				comment += "Data file: " + injection.getDataFileName() + "; ";
				comment += "Timestamp: " + dateFormat.format(injection.getTimeStamp()) + "; ";
			}
		}
		comment += "Acq. method: " + bundle.getAcquisitionMethod().getName() + "; ";
		comment += "DA method: " + bundle.getDataExtractionMethod().getName();
		return comment;
	}
		
	public File getOutputFile() {
		return outputFile;
	}
}
