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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.ChromatogramDefinition;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExtractedIonData;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.scan.IScan;
import umich.ms.datatypes.scancollection.ScanIndex;
import umich.ms.fileio.exceptions.FileParsingException;

public class MsFeatureChromatogramExtractionTask extends AbstractTask {
	
	private DataFile rawDataFile;
	private Map<MsFeatureInfoBundle, ChromatogramDefinition>featureChromatogramDefinitions;
	private Map<MsFeatureInfoBundle, Collection<ExtractedIonData>>chromatogramMap;
	private TreeMap<Integer, ScanIndex>msLevel2index;
	
	public MsFeatureChromatogramExtractionTask(
			DataFile rawDataFile,
			Map<MsFeatureInfoBundle, ChromatogramDefinition> featureChromatogramDefinitions) {
		super();
		this.rawDataFile = rawDataFile;
		this.featureChromatogramDefinitions = featureChromatogramDefinitions;
		chromatogramMap = 
				new HashMap<MsFeatureInfoBundle, Collection<ExtractedIonData>>();
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Extracting chromatograms from " + rawDataFile.getName();
		total = 100;
		processed = 20;
		try {
			createDataSource();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			extractCromatograms();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		RawDataManager.removeDataSource(rawDataFile);
		setStatus(TaskStatus.FINISHED);
	}
	
	private void extractCromatograms() {
		total = featureChromatogramDefinitions.size();
		processed = 0;
		for(Entry<MsFeatureInfoBundle, ChromatogramDefinition> entry : featureChromatogramDefinitions.entrySet()) {
			
			if (isCanceled()) {
				RawDataManager.removeDataSource(rawDataFile);
				return;
			}			
			taskDescription = "Extracting chromatograms for " + 
						entry.getKey().getMsFeature().toString();
			Collection<ExtractedIonData> chromatograms = 
					getDefinedChromatogram(entry.getValue());
			if (chromatograms != null)
				chromatogramMap.put(entry.getKey(), chromatograms);

			processed++;
		}		
	}
	
	private Collection<ExtractedIonData> getDefinedChromatogram(ChromatogramDefinition cd) {
	
		Map<Integer, IScan> num2scan = getFilteredScans(cd);
		if(num2scan == null)
			return null;
		
		Collection<ExtractedIonData>chromatograms = new ArrayList<ExtractedIonData>();
		Map<Double,Range>mzRanges = new TreeMap<Double,Range>();
		ArrayList<Double>time = new ArrayList<Double>();
		Map<Double,Collection<Double>>intensityMap = new TreeMap<Double,Collection<Double>>();
		for(double mz : cd.getMzList()) {
			Range mzRange = MsUtils.createMassRange(
					mz,
					cd.getMzWindowValue(), 
					cd.getMassErrorType());
			mzRanges.put(mz, mzRange);
			intensityMap.put(mz, new ArrayList<Double>());
		}		
		for (Map.Entry<Integer, IScan> scanEntry : num2scan.entrySet()) {

			IScan scan = scanEntry.getValue();
			if(scan.getSpectrum() == null) {
				try {
					scan.fetchSpectrum();
				} catch (FileParsingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			time.add(scan.getRt());		
			for(Entry<Double, Range> e : mzRanges.entrySet()) {
				
				int[] mIdx = scan.getSpectrum().findMzIdxs(e.getValue().getMin(), e.getValue().getMax());				
				double sum = 0.0;				
				if(mIdx != null) {						
					for(int i = mIdx[0]; i<=  mIdx[1]; i++)
						sum += scan.getSpectrum().getIntensities()[i];
				}
				intensityMap.get(e.getKey()).add(sum);
			}
		}
		double[] timeArray = time.stream().mapToDouble(Double::doubleValue).toArray();
		if(cd.getSumAllMassChromatograms() && intensityMap.size() > 1) {
			
			double[] intensityArray = sumIntensities(intensityMap.values());
			if(cd.isDoSmooth() && cd.getSmoothingFilter() != null)
				intensityArray = cd.getSmoothingFilter().filter(timeArray, intensityArray);
			
			ExtractedIonData eid = new ExtractedIonData(
					intensityMap.keySet().iterator().next(), 
					timeArray, 
					intensityArray);
			chromatograms.add(eid);
		}
		else {
			for(Entry<Double, Collection<Double>> e : intensityMap.entrySet()) {				
				
				double[] intensityArray = e.getValue().stream().mapToDouble(Double::doubleValue).toArray();
				if(cd.isDoSmooth() && cd.getSmoothingFilter() != null)
					intensityArray = cd.getSmoothingFilter().filter(timeArray, intensityArray);
				
				ExtractedIonData eid = new ExtractedIonData(
						e.getKey(), 
						timeArray, 
						intensityArray);
				chromatograms.add(eid);
			}
		}
		return chromatograms;
	}
	
	private double[] sumIntensities(Collection<Collection<Double>> values) {
		
		List<Collection<Double>>valueList = new ArrayList<Collection<Double>>(values);
		double[]sums = valueList.get(0).stream().mapToDouble(Double::doubleValue).toArray();
		for(int i=0; i<valueList.size(); i++) {
			double[]intensity = valueList.get(i).stream().mapToDouble(Double::doubleValue).toArray();
			for(int j=0; j<sums.length; j++)
				sums[j] += intensity[j];
		}		
		return sums;
	}

	private Map<Integer, IScan> getFilteredScans(ChromatogramDefinition cd){
		
		ScanIndex ms2idx = msLevel2index.get(cd.getMsLevel());
		if(ms2idx == null)
			return null;
		
		Map<Integer, IScan> num2scan = ms2idx.getNum2scan();				
		if(cd.getRtRange() != null) {
			
			num2scan = num2scan.entrySet().stream().
					filter(x -> cd.getRtRange().contains(x.getValue().getRt())).
					collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
		}
		if(num2scan.isEmpty())
			return null;
		
		Polarity pol = cd.getPolarity();
		if(pol != null) {
			
			num2scan = num2scan.entrySet().stream().
					filter(x -> x.getValue().getPolarity().getSign() == pol.getSign()).
					collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
		}
		if(num2scan.isEmpty())
			return null;
		else
			return num2scan;
	}

	private void createDataSource() throws FileParsingException {
		
		if (isCanceled())
			return;
		
		LCMSData data = RawDataManager.getRawData(rawDataFile);
		msLevel2index = data.getScans().getMapMsLevel2index();
	}
	
	@Override
	public Task cloneTask() {
		return new MsFeatureChromatogramExtractionTask(rawDataFile,
				featureChromatogramDefinitions);
	}

	public DataFile getRawDataFile() {
		return rawDataFile;
	}

	public Map<MsFeatureInfoBundle, Collection<ExtractedIonData>> getChromatogramMap() {
		return chromatogramMap;
	}
}
