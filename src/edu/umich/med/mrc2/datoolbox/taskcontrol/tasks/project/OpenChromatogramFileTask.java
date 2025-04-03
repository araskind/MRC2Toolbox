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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class OpenChromatogramFileTask extends AbstractTask {
	
	private File chromatogramFile;
	private Map<String, MsFeatureChromatogramBundle>chromatogramMap;
	private Collection<DataFile>experimentDataFiles;

	public OpenChromatogramFileTask(
			File chromatogramFile, 
			Collection<DataFile>experimentDataFiles) {
		super();
		this.chromatogramFile = chromatogramFile;
		this.experimentDataFiles = experimentDataFiles;
		chromatogramMap = 
				new HashMap<String, MsFeatureChromatogramBundle>();
	}

	@Override
	public void run() {
		setStatus(TaskStatus.PROCESSING);
		try {
			extractMsFeatureBundles();
		} catch (Exception ex) {
			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void extractMsFeatureBundles() throws Exception {

		taskDescription = "Reading chromatograms from " + 
				chromatogramFile.getName();
		total = 100;
		processed = 20;
		try {
			SAXBuilder sax = new SAXBuilder();
			Document doc = sax.build(chromatogramFile);
			Element rootNode = doc.getRootElement();
			List<Element> list = 
					rootNode.getChildren(ObjectNames.FChrBundle.name());
			total = list.size();
			processed = 0;
			for (Element chromBundleElement : list) {

				MsFeatureChromatogramBundle fcb = 
						new MsFeatureChromatogramBundle(
								chromBundleElement, experimentDataFiles);
				chromatogramMap.put(fcb.getFeatureId(), fcb);
				processed++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public Map<String, MsFeatureChromatogramBundle> getChromatogramMap() {
		return chromatogramMap;
	}
	
	@Override
	public Task cloneTask() {
		return new OpenChromatogramFileTask(
				chromatogramFile, experimentDataFiles);
	}
}
