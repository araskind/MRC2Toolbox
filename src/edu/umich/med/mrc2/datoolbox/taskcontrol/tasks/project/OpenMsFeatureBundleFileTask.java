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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.project.store.DataFileFields;
import edu.umich.med.mrc2.datoolbox.project.store.MsFeatureInfoBundleFields;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class OpenMsFeatureBundleFileTask extends AbstractTask {
	
	private DataFile dataFile;
	private File featureFile;
	private Collection<MSFeatureInfoBundle>features;

	public OpenMsFeatureBundleFileTask(DataFile dataFile, File featureFile) {
		super();
		this.dataFile = dataFile;
		this.featureFile = featureFile;
		features = new ArrayList<MSFeatureInfoBundle>();
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

		taskDescription = "Opening data for " + dataFile.getName();
		total = 100;
		processed = 20;
		try {
			SAXBuilder sax = new SAXBuilder();
			Document doc = sax.build(featureFile);
			Element rootNode = doc.getRootElement();
			List<Element> list = 
					rootNode.getChild(DataFileFields.FeatureList.name()).
					getChildren(MsFeatureInfoBundleFields.MFIB.name());
			taskDescription = "Extracting MS features for " + dataFile.getName();
			total = list.size();
			processed = 0;
			for (Element featureElement : list) {

				MSFeatureInfoBundle bundle = 
						new MSFeatureInfoBundle(featureElement);
				bundle.setDataFile(dataFile);
				if(bundle.getAcquisitionMethod() == null)
					bundle.setAcquisitionMethod(dataFile.getDataAcquisitionMethod());
				
				IDTExperimentalSample ps = (IDTExperimentalSample) dataFile.getParentSample();
				if(bundle.getSample() == null)
					bundle.setSample(ps);
				
				if(bundle.getStockSample() == null && ps != null)
					bundle.setStockSample(ps.getParentStockSample());
				
				features.add(bundle);
				processed++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	public DataFile getDataFile() {
		return dataFile;
	}

	public Collection<MSFeatureInfoBundle> getFeatures() {
		return features;
	}
	
	@Override
	public Task cloneTask() {
		return new OpenMsFeatureBundleFileTask(dataFile, featureFile);
	}

}
