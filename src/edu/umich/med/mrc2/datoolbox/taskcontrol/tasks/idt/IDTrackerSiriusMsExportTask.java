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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.SiriusMsMsCluster;
import edu.umich.med.mrc2.datoolbox.data.compare.MsDataPointComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInjection;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class IDTrackerSiriusMsExportTask extends AbstractTask {
	
	private Collection<MsFeatureInfoBundle>featuresToExport;
	private File outputFile;
	
	private static final String lineSeparator = System.getProperty("line.separator");
	private static final char columnSeparator = MRC2ToolBoxConfiguration.getTabDelimiter();
	
	private static final NumberFormat rtFormat = MRC2ToolBoxConfiguration.getRtFormat();
	private static final NumberFormat mzFormat = MRC2ToolBoxConfiguration.getMzFormat();
	private static final NumberFormat ppmFormat = MRC2ToolBoxConfiguration.getPpmFormat();
	private static final NumberFormat intensityFormat = MRC2ToolBoxConfiguration.getIntensityFormat();
	private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	
	private Collection<Injection>injections;
	private TreeMap<String, LIMSInjection> injectionMap;
	private Collection<SiriusMsMsCluster> msmsclusters;
	private double rtError;
	private double mzError;

	public IDTrackerSiriusMsExportTask(
			Collection<MsFeatureInfoBundle> featuresToExport,
			double rtError, 
			double mzError, 
			File outputFile) {
		super();
		this.featuresToExport = featuresToExport;
		this.rtError = rtError;
		this.mzError = mzError;
		this.outputFile = outputFile;
	}

	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);	
		try {
			createFeatureGroups();
		}
		catch (Exception e1) {
			setStatus(TaskStatus.ERROR);
			e1.printStackTrace();
		}
		try {
			writeMsFile();
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e1) {
			setStatus(TaskStatus.ERROR);
			e1.printStackTrace();
		}
	}
	
	private void createFeatureGroups() {
		
		msmsclusters = new ArrayList<SiriusMsMsCluster>();
		MsFeatureInfoBundle[] msmsFeatures = 
				featuresToExport.stream().
				filter(f -> f.getMsFeature().getSpectrum().getTandemSpectrum(SpectrumSource.EXPERIMENTAL) != null).
				filter(f -> Math.abs(f.getMsFeature().getCharge()) == 1).
				toArray(size -> new MsFeatureInfoBundle[size]);
		
		if(msmsFeatures.length == 0)
			return;
		
		taskDescription = "Grouping related features";
		total = msmsFeatures.length;
		processed = 1;
				
		SiriusMsMsCluster firstCluster = new SiriusMsMsCluster(msmsFeatures[0], rtError, mzError);
		msmsclusters.add(firstCluster);
		for(int i=1; i<msmsFeatures.length; i++) {
			
			boolean added = false;
			for(SiriusMsMsCluster cluster : msmsclusters) {
				
				if(cluster.addFeatureBundle(msmsFeatures[i])) {
					added = true;
					break;
				}				
			}
			if(!added) {
				SiriusMsMsCluster newCluster = new SiriusMsMsCluster(msmsFeatures[i], rtError, mzError);
				msmsclusters.add(newCluster);
			}
			processed++;
		}
	}
	
	private void createInjectionMap(Collection<MsFeatureInfoBundle> msmsFeatures) {

		injectionMap = new TreeMap<String,LIMSInjection>();
		List<String> injIds = msmsFeatures.stream().
				map(f -> f.getInjectionId()).distinct().
				filter(i -> !i.equals(null)).sorted().
				collect(Collectors.toList());

		if(injIds.isEmpty())
			return;

		for(String id : injIds) {

			try {
				LIMSInjection injection = IDTUtils.getInjectionById(id);
				if(injection != null)
					injectionMap.put(id, injection);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void writeMsFile() throws IOException {

		taskDescription = "Wtiting MS output";
		total = msmsclusters.size();
		processed = 0;
		final Writer writer = new BufferedWriter(new FileWriter(outputFile));
		for(SiriusMsMsCluster cluster : msmsclusters) {
			
			writer.append(cluster.getSiriusMsBlock());
			writer.append("\n");
			processed++;
		}
		writer.flush();
		writer.close();
	}

	private String createComment(MsFeatureInfoBundle bundle) {

		String comment = MSPField.COMMENT.getName() + ": ";
		comment += "RT "+ MRC2ToolBoxConfiguration.getRtFormat().format(bundle.getMsFeature().getRetentionTime()) + " min. | ";
		String injId = bundle.getInjectionId();
		if(injId != null) {
			LIMSInjection injection = injectionMap.get(injId);
			if(injection != null) {
				comment += "Data file: " + injection.getDataFile() + "; ";
				comment += "Timestamp: " + dateFormat.format(injection.getTimestamp()) + "; ";
			}
		}
		comment += "Acq. method: " + bundle.getAcquisitionMethod().getName() + "; ";
		comment += "DA method: " + bundle.getDataExtractionMethod().getName();
		return comment;
	}

	private MsPoint[] normalizeAndSortMsPatternForMsp(Collection<MsPoint>pattern) {

		MsPoint basePeak = Collections.max(pattern, Comparator.comparing(MsPoint::getIntensity));
		double maxIntensity  = basePeak.getIntensity();

		return pattern.stream()
				.map(dp -> new MsPoint(dp.getMz(), Math.round(dp.getIntensity()/maxIntensity*999.0d)))
				.sorted(new MsDataPointComparator(SortProperty.MZ)).
				toArray(size -> new MsPoint[size]);
	}
	

	@Override
	public Task cloneTask() {

		return new IDTrackerSiriusMsExportTask(
				 featuresToExport,
				 rtError,
				 mzError,
				 outputFile);
	}
	
	public File getOutputFile() {
		return outputFile;
	}
}
