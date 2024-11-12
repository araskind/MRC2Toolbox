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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.ujmp.core.Matrix;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.NoTypePermission;
import com.thoughtworks.xstream.security.NullPermission;
import com.thoughtworks.xstream.security.PrimitiveTypePermission;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignListener;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetListener;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetListener;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureListener;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ExperimentUtils;

public class SaveExperimentTask extends AbstractTask {

	private DataAnalysisProject experimentToSave;
	private File experimentFile;
	private HashMap<MsFeature, LinkedList<MsFeatureListener>> featureListeners;
	private HashMap<MsFeatureSet, LinkedList<FeatureSetListener>> featureSetListeners;
	private HashMap<ExperimentDesignSubset, LinkedList<ExperimentDesignSubsetListener>> designSetListeners;
	private HashSet<ExperimentDesignListener> designListeners;

	public SaveExperimentTask(DataAnalysisProject experiment) {

		this.experimentToSave = experiment;
		experimentFile = experimentToSave.getExperimentFile();
		total = 100;
		processed = 0;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		long startTime = System.currentTimeMillis();
		experimentToSave.setLastModified(new Date());
		backUpListeners();

		// Save experiment file
		try {
			processed = 10;
			taskDescription = "Parsing experiment code ... ";
			XStream experimentXstream = initXstream();
			File xmlFile = Paths.get(experimentFile.getParentFile().getAbsolutePath(), 
					experimentToSave.getName() + ".xml").toFile();
	        RandomAccessFile raf = new RandomAccessFile(xmlFile.getAbsolutePath(), "rw");
	        FileOutputStream fout = new FileOutputStream(raf.getFD());	        
			BufferedOutputStream bout = new BufferedOutputStream(fout);
			experimentXstream.toXML(experimentToSave, bout);
			bout.close();
			fout.close();
			raf.close();

			processed = 50;		
			if(xmlFile.exists()) {
				
				taskDescription = "Compressing XML file";
				processed = 60;
		        OutputStream archiveStream = new FileOutputStream(experimentFile);
		        ZipArchiveOutputStream archive =
		        	(ZipArchiveOutputStream) new ArchiveStreamFactory().
		        	createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream);
		        archive.setUseZip64(Zip64Mode.Always);
		        ZipArchiveEntry entry = new ZipArchiveEntry(experimentToSave.getName());
		        archive.putArchiveEntry(entry);

		        BufferedInputStream input = new BufferedInputStream(new FileInputStream(xmlFile));
		        org.apache.commons.io.IOUtils.copy(input, archive);
		        input.close();
		        archive.closeArchiveEntry();
		        archive.finish();
		        archive.close();
		        archiveStream.close();	        
		        xmlFile.delete();
		        processed = 99;
		        
				long endTime = System.currentTimeMillis();
				System.out.println("Experiment save time : " 
						+ Math.round((endTime - startTime) / 1000) + " seconds");
			}
			processed = 100;
		} catch (Exception ex) {

			ex.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
		restoreListeners();
		saveDataMatrixes();
		setStatus(TaskStatus.FINISHED);
	}
	
	private void saveDataMatrixes() {
		
		total = (experimentToSave.getDataPipelines().size() * 2) + 1;
		processed = 0;
		for (DataPipeline dp : experimentToSave.getDataPipelines()) {

			if (experimentToSave.getDataMatrixForDataPipeline(dp) != null) {

				taskDescription = "Saving data matrix for  " + experimentToSave.getName() +
						"(" + dp.getName() + ")";
								
				ExperimentUtils.saveDataMatrixForPipeline(experimentToSave, dp);
				processed++;
				
				taskDescription = "Saving feature matrix for  " + experimentToSave.getName() +
						"(" + dp.getName() + ")";
								
				Matrix msFeatureMatrix = Matrix.Factory
						.linkToArray(experimentToSave.getFeatureMatrixForDataPipeline(dp).toObjectArray());

				ExperimentUtils.saveFeatureMatrixToFile(
						msFeatureMatrix,
						experimentToSave, 
						dp);
				processed++;
				experimentToSave.setFeatureMatrixForDataPipeline(dp, null);
				msFeatureMatrix = null;

				
//				File dataMatrixFile = Paths.get(experimentToSave.getExperimentDirectory().getAbsolutePath(), 
//						experimentToSave.getDataMatrixFileNameForDataPipeline(dp)).toFile();
//				try {
//					Matrix dataMatrix = Matrix.Factory
//							.linkToArray(experimentToSave.getDataMatrixForDataPipeline(dp).
//									toDoubleArray());
//					dataMatrix.save(dataMatrixFile);
//					processed = 80;
//				} catch (IOException e) {
//					e.printStackTrace();
//					setStatus(TaskStatus.ERROR);
//return;
//
//				}
//				if(experimentToSave.getFeatureMatrixFileNameForDataPipeline(dp) != null
//						&& experimentToSave.getFeatureMatrixForDataPipeline(dp) != null) {
//					
//					taskDescription = "Saving feature matrix for  " + experimentToSave.getName() +
//							"(" + dp.getName() + ")";
//					
//					File featureMatrixFile = Paths.get(experimentToSave.getExperimentDirectory().getAbsolutePath(), 
//							experimentToSave.getFeatureMatrixFileNameForDataPipeline(dp)).toFile();
//					try {
//						Matrix featureMatrix = Matrix.Factory
//								.linkToArray(experimentToSave.getFeatureMatrixForDataPipeline(dp).toObjectArray());
//						featureMatrix.save(featureMatrixFile);
//						processed = 100;
//					} catch (IOException e) {
//						e.printStackTrace();
//						setStatus(TaskStatus.ERROR);
//return;
//
//					}
//				}
			}
			System.gc();
			processed = total;
		}
	}

	private void backUpListeners() {

		featureListeners = new HashMap<MsFeature, 
				LinkedList<MsFeatureListener>>();
		featureSetListeners = new HashMap<MsFeatureSet, 
				LinkedList<FeatureSetListener>>();
		designSetListeners = new HashMap<ExperimentDesignSubset, 
				LinkedList<ExperimentDesignSubsetListener>>();
		designListeners = new HashSet<ExperimentDesignListener>();

		for (DataPipeline dp : experimentToSave.getDataPipelines()) {

			for (MsFeature f : experimentToSave.getMsFeaturesForDataPipeline(dp)) {

				if (!f.getFeatureListeners().isEmpty()) {

					LinkedList<MsFeatureListener> listeners = new LinkedList<MsFeatureListener>();
					listeners.addAll(f.getFeatureListeners());
					featureListeners.put(f, listeners);
					f.removeAllListeners();
				}
			}
			for (MsFeatureSet set : experimentToSave.getMsFeatureSetsForDataPipeline(dp)) {

				if (!set.getListeners().isEmpty()) {

					LinkedList<FeatureSetListener> listeners = 
							new LinkedList<FeatureSetListener>();
					listeners.addAll(set.getListeners());
					featureSetListeners.put(set, listeners);
					set.removeAllListeners();
				}
			}
		}
		for (ExperimentDesignSubset dset : experimentToSave.getExperimentDesign().getDesignSubsets()) {

			if (!dset.getEventListeners().isEmpty()) {

				LinkedList<ExperimentDesignSubsetListener> listeners = 
						new LinkedList<ExperimentDesignSubsetListener>();
				listeners.addAll(dset.getEventListeners());
				designSetListeners.put(dset, listeners);
				dset.removeAllListeners();
			}
		}
		designListeners.addAll(experimentToSave.getExperimentDesign().getListeners());
		experimentToSave.getExperimentDesign().removeAllListeners();
	}

	private XStream initXstream() {

		XStream xstream = new XStream(new StaxDriver());

		xstream.setMode(XStream.XPATH_RELATIVE_REFERENCES);
		xstream.addPermission(NoTypePermission.NONE);
		xstream.addPermission(NullPermission.NULL);
		xstream.addPermission(PrimitiveTypePermission.PRIMITIVES);
		
		xstream.omitField(DataAnalysisProject.class, "dataMatrixMap");
		xstream.omitField(DataAnalysisProject.class, "featureMatrixMap");
		xstream.omitField(DataAnalysisProject.class, "corrMatrixMap");
		xstream.omitField(MsFeature.class, "eventListeners");
		xstream.omitField(MsFeatureSet.class, "eventListeners");
		xstream.omitField(ExperimentDesignSubset.class, "eventListeners");
		xstream.omitField(ExperimentDesign.class, "eventListeners");

		return xstream;
	}

	private void restoreListeners() {

		for (Entry<MsFeature, LinkedList<MsFeatureListener>> entry : featureListeners.entrySet())
			entry.getValue().stream().forEach(l -> entry.getKey().addListener(l));

		for (Entry<MsFeatureSet, LinkedList<FeatureSetListener>> entry : featureSetListeners.entrySet())
			entry.getValue().stream().forEach(l -> entry.getKey().addListener(l));

		for (Entry<ExperimentDesignSubset, LinkedList<ExperimentDesignSubsetListener>> entry : designSetListeners.entrySet())
			entry.getValue().stream().forEach(l -> entry.getKey().addListener(l));

		designListeners.stream().forEach(l -> experimentToSave.getExperimentDesign().addListener(l));
	}

	@Override
	public Task cloneTask() {
		return new SaveExperimentTask(experimentToSave);
	}

	public DataAnalysisProject getExperimentToSave() {
		return experimentToSave;
	}
}
