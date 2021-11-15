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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.ChromatogramDefinition;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExtractedChromatogram;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.idworks.xic.ChromatogramExtractionType;
import edu.umich.med.mrc2.datoolbox.gui.plot.chromatogram.ChromatogramPlotMode;
import edu.umich.med.mrc2.datoolbox.gui.utils.ColorUtils;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.filter.Filter;
import edu.umich.med.mrc2.datoolbox.utils.filter.SavitzkyGolayWidth;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.scan.IScan;
import umich.ms.datatypes.scan.StorageStrategy;
import umich.ms.datatypes.scancollection.IScanCollection;
import umich.ms.datatypes.scancollection.ScanIndex;
import umich.ms.fileio.exceptions.FileParsingException;

public class ChromatogramExtractionTask extends AbstractTask {
	
	private Collection<DataFile> files;
	private ChromatogramPlotMode mode;
	private Polarity polarity;
	private int msLevel;
	private Collection<Double>mzList;
	private boolean sumAllMassChromatograms;
	private Double mzWindowValue;
	private MassErrorType massErrorType;
	private Range rtRange;
	private Filter smoothingFilter;
	private ChromatogramExtractionType chexType;
	
	private boolean doSmooth; 
	private SavitzkyGolayWidth filterWidth;
	private ChromatogramDefinition chromatogramDefinition;
	private Collection<ExtractedChromatogram>extractedChromatograms;
	private boolean releaseMemory;
	
	public ChromatogramExtractionTask(
			Collection<DataFile> files, 
			ChromatogramPlotMode mode, 
			Polarity polarity,
			int msLevel, 
			Collection<Double> mzList, 
			boolean sumAllMassChromatograms, 
			Double mzWindowValue,
			MassErrorType massErrorType, 
			Range rtRange, 
			Filter smoothingFilter, 
			ChromatogramExtractionType chexType) {
		super();
		this.files = files;
		this.mode = mode;
		this.polarity = polarity;
		this.msLevel = msLevel;
		this.mzList = mzList;
		this.sumAllMassChromatograms = sumAllMassChromatograms;
		this.mzWindowValue = mzWindowValue;
		this.massErrorType = massErrorType;
		this.rtRange = rtRange;
		this.smoothingFilter = smoothingFilter;
		this.chexType = chexType;
		if(smoothingFilter != null)
			doSmooth = true;
		
		extractedChromatograms = new ArrayList<ExtractedChromatogram>();
	}

	public ChromatogramExtractionTask(
			Collection<DataFile> files, 
			ChromatogramPlotMode mode, 
			Polarity polarity,
			int msLevel, 
			Collection<Double> mzList, 
			boolean sumAllMassChromatograms, 
			Double mzWindowValue, 
			MassErrorType massErrorType, 
			Range rtRange) {
		
		super();
		this.files = files;
		this.mode = mode;
		this.polarity = polarity;
		this.msLevel = msLevel;
		this.mzList = mzList;
		this.sumAllMassChromatograms = sumAllMassChromatograms;
		this.mzWindowValue = mzWindowValue;
		this.massErrorType = massErrorType;
		this.rtRange = rtRange;
		this.doSmooth = false;
		this.filterWidth = SavitzkyGolayWidth.NINE;
		releaseMemory = false;
		
		extractedChromatograms = new ArrayList<ExtractedChromatogram>();
	}

	public ChromatogramExtractionTask(
			Collection<DataFile> files, 
			ChromatogramPlotMode mode, 
			Polarity polarity,
			int msLevel, 
			Collection<Double> mzList, 
			boolean sumAllMassChromatograms, 
			Double mzWindowValue, 
			MassErrorType massErrorType, 
			Range rtRange, 
			boolean doSmooth, 
			SavitzkyGolayWidth filterWidth) {
		
		super();
		this.files = files;
		this.mode = mode;
		this.polarity = polarity;
		this.msLevel = msLevel;
		this.mzList = mzList;
		this.sumAllMassChromatograms = sumAllMassChromatograms;
		this.mzWindowValue = mzWindowValue;
		this.massErrorType = massErrorType;
		this.rtRange = rtRange;
		this.doSmooth = doSmooth;
		this.filterWidth = filterWidth;
		releaseMemory = false;
		
		extractedChromatograms = new ArrayList<ExtractedChromatogram>();		
	}

	@Override
	public void run() {

		taskDescription = "Extracting chromatograms ";
		setStatus(TaskStatus.PROCESSING);
		try {
			//	Map<DataFile, LCMSData> dataSources = RawDataUtils.createDataSources(files, msLevel, this);
			Map<DataFile, LCMSData> dataSources =  new HashMap<DataFile,LCMSData>();
			for(DataFile f : files) {
				
				LCMSData rawData = RawDataManager.getRawData(f);
				if(rawData != null)
					dataSources.put(f, rawData);
			}			
			if(mode.equals(ChromatogramPlotMode.TIC))
				extractTics(dataSources);
			if(mode.equals(ChromatogramPlotMode.BASEPEAK))
				extractBpcs(dataSources);
			if(mode.equals(ChromatogramPlotMode.XIC)) {
				
				if(!sumAllMassChromatograms) {
					
					for(double mz : mzList)
						extractXic(dataSources, mz);
					
					if(releaseMemory) {
						for (Entry<DataFile, LCMSData> entry : dataSources.entrySet())
							entry.getValue().releaseMemory();
					}
					setStatus(TaskStatus.FINISHED);	
				}
				else {
					extractCombinedXic(dataSources, mzList);
					setStatus(TaskStatus.FINISHED);	
				}
			}				
			setStatus(TaskStatus.FINISHED);			
		} 
		catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);		
		}
	}
	
	private void extractTics(Map<DataFile, LCMSData> dataSourcesmap) {
		
//		chromatogramDefinition = 
//				new ChromatogramDefinition(
//						mode, 
//						polarity, 
//						msLevel, 
//						mzList, 
//						sumAllMassChromatograms, 
//						mzWindowValue, 
//						massErrorType, 
//						rtRange,
//						doSmooth, 
//						filterWidth);

		for (Entry<DataFile, LCMSData> entry : dataSourcesmap.entrySet()) {
			
			taskDescription = "Extracting TIC for " + entry.getKey().getName();
			
//			ExtractedChromatogram tic = new ExtractedChromatogram(entry.getKey(), chromatogramDefinition);
			ArrayList<Double>time = new ArrayList<Double>();
			ArrayList<Double>intensity = new ArrayList<Double>();
			
			IScanCollection scans = entry.getValue().getScans();
			scans.isAutoloadSpectra(true);
			scans.setDefaultStorageStrategy(StorageStrategy.SOFT);
			TreeMap<Integer, ScanIndex> msLevel2index = scans.getMapMsLevel2index();
			ScanIndex ms2idx = msLevel2index.get(msLevel);
			
			if(ms2idx != null) {
				
				TreeMap<Integer, IScan> num2scan = ms2idx.getNum2scan();
				Set<Map.Entry<Integer, IScan>> scanEntries = num2scan.entrySet();
				
				total = scanEntries.size();
				processed = 0;
				
				for (Map.Entry<Integer, IScan> scanEntry : scanEntries) {

					IScan scan = scanEntry.getValue();
					if(polarity != null && scan.getPolarity().getSign() != polarity.getSign())
						continue;
					
					if(scan.getSpectrum() == null) {
						try {
							scan.fetchSpectrum();
						} catch (FileParsingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					time.add(scan.getRt());
					intensity.add(scan.getTic());
					processed++;
				}
//				tic.setTimeValues(time.stream().mapToDouble(Double::doubleValue).toArray());
//				tic.setIntensityValues(intensity.stream().mapToDouble(Double::doubleValue).toArray());
//				extractedChromatograms.add(tic);
//				entry.getKey().getChromatograms().add(tic);
				
				finaliseChromatogramExtraction(
						entry.getKey(), null, time, intensity);
			}			
			if(releaseMemory)
				entry.getValue().releaseMemory();
		}
	}

	private void extractBpcs(Map<DataFile, LCMSData> dataSourcesmap) {
		
//		chromatogramDefinition = 
//				new ChromatogramDefinition(
//						mode, 
//						polarity, 
//						msLevel, 
//						mzList, 
//						sumAllMassChromatograms, 
//						mzWindowValue, 
//						massErrorType, 
//						rtRange,
//						doSmooth, 
//						filterWidth);

		for (Entry<DataFile, LCMSData> entry : dataSourcesmap.entrySet()) {
			
			taskDescription = "Extracting BPC for " + entry.getKey().getName();
			
//			ExtractedChromatogram tic = new ExtractedChromatogram(entry.getKey(), chromatogramDefinition);
			ArrayList<Double>time = new ArrayList<Double>();
			ArrayList<Double>intensity = new ArrayList<Double>();
			
			IScanCollection scans = entry.getValue().getScans();
			scans.isAutoloadSpectra(true);
			scans.setDefaultStorageStrategy(StorageStrategy.SOFT);
			TreeMap<Integer, ScanIndex> msLevel2index = scans.getMapMsLevel2index();
			ScanIndex ms2idx = msLevel2index.get(msLevel);
			
			if(ms2idx != null) {
				
				TreeMap<Integer, IScan> num2scan = ms2idx.getNum2scan();
				Set<Map.Entry<Integer, IScan>> scanEntries = num2scan.entrySet();
				
				total = scanEntries.size();
				processed = 0;
				
				for (Map.Entry<Integer, IScan> scanEntry : scanEntries) {

					IScan scan = scanEntry.getValue();
					if(polarity != null && scan.getPolarity().getSign() != polarity.getSign())
						continue;
					
					if(scan.getSpectrum() == null) {
						try {
							scan.fetchSpectrum();
						} catch (FileParsingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					time.add(scan.getRt());
					intensity.add(scan.getBasePeakIntensity());
					processed++;
				}
//				tic.setTimeValues(time.stream().mapToDouble(Double::doubleValue).toArray());
//				tic.setIntensityValues(intensity.stream().mapToDouble(Double::doubleValue).toArray());
//				extractedChromatograms.add(tic);
//				entry.getKey().getChromatograms().add(tic);
				
				finaliseChromatogramExtraction(
						entry.getKey(), null, time, intensity);
			}			
			if(releaseMemory)
				entry.getValue().releaseMemory();
		}
	}

	private void extractCombinedXic(Map<DataFile, LCMSData> dataSourcesmap, Collection<Double> mzList2) {
		
		Collection<Double>xicMasses = new ArrayList<Double>();
		Collection<Range>mzRanges = new ArrayList<Range>();
		for(Double mz : mzList2) {
			Range mzRange = MsUtils.createMassRange(mz, mzWindowValue, massErrorType);
			mzRanges.add(mzRange);
			xicMasses.add(mz);
		}
		for (Entry<DataFile, LCMSData> entry : dataSourcesmap.entrySet()) {
			
			taskDescription = "Creating extracted ion chromatogram for " + entry.getKey().getName();
			
			ArrayList<Double>time = new ArrayList<Double>();
			ArrayList<Double>intensity = new ArrayList<Double>();			
			IScanCollection scans = entry.getValue().getScans();
			scans.isAutoloadSpectra(true);
			scans.setDefaultStorageStrategy(StorageStrategy.SOFT);
			TreeMap<Integer, ScanIndex> msLevel2index = scans.getMapMsLevel2index();
			ScanIndex ms2idx = msLevel2index.get(msLevel);			
			if(ms2idx != null) {
				
				Map<Integer, IScan> num2scan = ms2idx.getNum2scan();				
				if(rtRange != null) {					
					num2scan = num2scan.entrySet().stream().
							filter(x -> rtRange.contains(x.getValue().getRt())).
							collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
				}
				Set<Map.Entry<Integer, IScan>> scanEntries = num2scan.entrySet();				
				total = scanEntries.size();
				processed = 0;					
				for (Map.Entry<Integer, IScan> scanEntry : scanEntries) {

					IScan scan = scanEntry.getValue();
					if(polarity != null && scan.getPolarity().getSign() != polarity.getSign())
						continue;
					
					if(scan.getSpectrum() == null) {
						try {
							scan.fetchSpectrum();
						} catch (FileParsingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					time.add(scan.getRt());	
					double sum = 0.0;	
					for(Range mzRange : mzRanges) {
						
						int[] mIdx = scan.getSpectrum().findMzIdxs(mzRange.getMin(), mzRange.getMax());											
						if(mIdx != null) {						
							for(int i = mIdx[0]; i<=  mIdx[1]; i++)
								sum += scan.getSpectrum().getIntensities()[i];
						}
						intensity.add(sum);
					}
					processed++;
				}
				finaliseChromatogramExtraction(entry.getKey(), mzList2, time, intensity);
				
//				double[] timeArray = time.stream().mapToDouble(Double::doubleValue).toArray();
//				double[] intensityArray = intensity.stream().mapToDouble(Double::doubleValue).toArray();
//				if(chexType.equals(ChromatogramExtractionType.RAW) 
//						|| chexType.equals(ChromatogramExtractionType.BOTH)) {
//					ChromatogramDefinition rawChromatogramDefinition = 
//							new ChromatogramDefinition(
//									mode, 
//									polarity, 
//									msLevel, 
//									mzList2, 
//									sumAllMassChromatograms, 
//									mzWindowValue, 
//									massErrorType, 
//									rtRange,
//									null);
//					ExtractedChromatogram rawXic = 
//							new ExtractedChromatogram(entry.getKey(), rawChromatogramDefinition);
//					rawXic.setTimeValues(timeArray);
//					rawXic.setIntensityValues(intensityArray);
//					extractedChromatograms.add(rawXic);
//					entry.getKey().getChromatograms().add(rawXic);
//				}
//				if(doSmooth) {
//					double[] smoothIntensityArray = 
//							smoothingFilter.filter(timeArray, intensityArray);
//					ChromatogramDefinition smoothChromatogramDefinition = 
//							new ChromatogramDefinition(
//									mode, 
//									polarity, 
//									msLevel, 
//									mzList2, 
//									sumAllMassChromatograms, 
//									mzWindowValue, 
//									massErrorType, 
//									rtRange,
//									smoothingFilter);
//					ExtractedChromatogram smoothXic = 
//							new ExtractedChromatogram(entry.getKey(), smoothChromatogramDefinition);
//					smoothXic.setTimeValues(timeArray);
//					smoothXic.setIntensityValues(smoothIntensityArray);
//					extractedChromatograms.add(smoothXic);
//					entry.getKey().getChromatograms().add(smoothXic);
//				}
			}
		}
		if(releaseMemory) {
			for (Entry<DataFile, LCMSData> entry : dataSourcesmap.entrySet())
				entry.getValue().releaseMemory();
		}
	}

	private void extractXic(Map<DataFile, LCMSData> dataSourcesmap, Double mz) {
		
//		chromatogramDefinition = 
//				new ChromatogramDefinition(
//						mode, 
//						polarity, 
//						msLevel, 
//						Collections.singleton(mz), 
//						sumAllMassChromatograms, 
//						mzWindowValue, 
//						massErrorType, 
//						rtRange,
//						smoothingFilter);
		
		for (Entry<DataFile, LCMSData> entry : dataSourcesmap.entrySet()) {
			
			taskDescription = "Creating extracted ion chromatogram for " 
					+ entry.getKey().getName() + " M/Z " + MRC2ToolBoxConfiguration.getMzFormat().format(mz);
			
			Collection<Double>xicMasses = new ArrayList<Double>();
			xicMasses.add(mz);
			
//			ExtractedChromatogram xic = new ExtractedChromatogram(entry.getKey(), chromatogramDefinition);
			ArrayList<Double>time = new ArrayList<Double>();
			ArrayList<Double>intensity = new ArrayList<Double>();
			
			IScanCollection scans = entry.getValue().getScans();
			scans.isAutoloadSpectra(true);
			scans.setDefaultStorageStrategy(StorageStrategy.SOFT);
			TreeMap<Integer, ScanIndex> msLevel2index = scans.getMapMsLevel2index();
			ScanIndex ms2idx = msLevel2index.get(msLevel);
			Range mzRange = MsUtils.createMassRange(mz, mzWindowValue, massErrorType);
			if(ms2idx != null) {
				
				Map<Integer, IScan> num2scan = ms2idx.getNum2scan();				
				if(rtRange != null) {
					
					num2scan = num2scan.entrySet().stream().
							filter(x -> rtRange.contains(x.getValue().getRt())).
							collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
				}
				Set<Map.Entry<Integer, IScan>> scanEntries = num2scan.entrySet();
				
				total = scanEntries.size();
				processed = 0;				
				for (Map.Entry<Integer, IScan> scanEntry : scanEntries) {

					IScan scan = scanEntry.getValue();
					if(polarity != null && scan.getPolarity().getSign() != polarity.getSign())
						continue;

					if(scan.getSpectrum() == null) {
						try {
							scan.fetchSpectrum();
						} catch (FileParsingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					time.add(scan.getRt());				
					int[] mIdx = scan.getSpectrum().findMzIdxs(mzRange.getMin(), mzRange.getMax());				
					double sum = 0.0;				
					if(mIdx != null) {						
						for(int i = mIdx[0]; i<=  mIdx[1]; i++)
							sum += scan.getSpectrum().getIntensities()[i];
					}
					intensity.add(sum);
					processed++;
				}
				finaliseChromatogramExtraction(
						entry.getKey(), Collections.singleton(mz), time, intensity);
				
//				double[] timeArray = time.stream().mapToDouble(Double::doubleValue).toArray();
//				double[] intensityArray = intensity.stream().mapToDouble(Double::doubleValue).toArray();
//				if(doSmooth) {
//					SavitzkyGolayFilter sgFilter = new SavitzkyGolayFilter(filterWidth.getWidth());
//					intensityArray = sgFilter.filter(timeArray, intensityArray);
//				}
//				xic.setTimeValues(timeArray);
//				xic.setIntensityValues(intensityArray);
//				extractedChromatograms.add(xic);
//				entry.getKey().getChromatograms().add(xic);
			}
		}
	}
	
	private void finaliseChromatogramExtraction(
			DataFile dataFile, 
			Collection<Double> mzList2, 
			ArrayList<Double>time,
			ArrayList<Double>intensity) {
		
		int chromCount = files.size() + 
				(int)files.stream().
				flatMap(f -> f.getChromatograms().stream()).count() + 1;
		
		double[] timeArray = time.stream().mapToDouble(Double::doubleValue).toArray();
		double[] intensityArray = intensity.stream().mapToDouble(Double::doubleValue).toArray();
		if(chexType.equals(ChromatogramExtractionType.RAW) 
				|| chexType.equals(ChromatogramExtractionType.BOTH)) {
			ChromatogramDefinition rawChromatogramDefinition = 
					new ChromatogramDefinition(
							mode, 
							polarity, 
							msLevel, 
							mzList2, 
							sumAllMassChromatograms, 
							mzWindowValue, 
							massErrorType, 
							rtRange,
							null);
			ExtractedChromatogram rawXic = 
					new ExtractedChromatogram(dataFile, rawChromatogramDefinition);
			if(mode.equals(ChromatogramPlotMode.TIC) || mode.equals(ChromatogramPlotMode.BASEPEAK))
				rawXic.setColor(dataFile.getColor());
			else
				rawXic.setColor(ColorUtils.getBrewerColor(chromCount));
								
			rawXic.setTimeValues(timeArray);
			rawXic.setIntensityValues(intensityArray);
			extractedChromatograms.add(rawXic);
			dataFile.getChromatograms().add(rawXic);
		}
		if(doSmooth) {
			double[] smoothIntensityArray = 
					smoothingFilter.filter(timeArray, intensityArray);
			ChromatogramDefinition smoothChromatogramDefinition = 
					new ChromatogramDefinition(
							mode, 
							polarity, 
							msLevel, 
							mzList2, 
							sumAllMassChromatograms, 
							mzWindowValue, 
							massErrorType, 
							rtRange,
							smoothingFilter);
			ExtractedChromatogram smoothXic = 
					new ExtractedChromatogram(dataFile, smoothChromatogramDefinition);
			if(mode.equals(ChromatogramPlotMode.TIC) || mode.equals(ChromatogramPlotMode.BASEPEAK))
				smoothXic.setColor(dataFile.getColor());
			else
				smoothXic.setColor(ColorUtils.getBrewerColor(chromCount));
			
			smoothXic.setTimeValues(timeArray);
			smoothXic.setIntensityValues(smoothIntensityArray);
			extractedChromatograms.add(smoothXic);
			dataFile.getChromatograms().add(smoothXic);
		}
	}
	
	private Collection<Integer> findInflectionPoints(double[] intensityArray) {
		
		ArrayList<Integer>inflectionPoint = new ArrayList<Integer>();
		for(int i=0; i<intensityArray.length-2; i++) {
			
			double deltaOne = intensityArray[i+1] - intensityArray[i];
			double deltaTwo = intensityArray[i+2] - intensityArray[i+1];
			if(deltaOne != 0.0d && deltaTwo != 0.0d) {
				
			if(deltaOne / deltaTwo < 0.0d)
				inflectionPoint.add(i+1);
			}
		}
		return inflectionPoint;
	}

	@Override
	public Task cloneTask() {

		return new ChromatogramExtractionTask(
				files, 
				mode, 
				polarity, 
				msLevel,
				mzList, 
				sumAllMassChromatograms, 
				mzWindowValue, 
				massErrorType, 
				rtRange, 
				doSmooth, 
				filterWidth);		
	}
	
	public ChromatogramDefinition getChromatogramDefinition() {
		return chromatogramDefinition;
	}

	public Collection<ExtractedChromatogram> getExtractedChromatograms() {
		return extractedChromatograms;
	}

	public void setReleaseMemory(boolean releaseMemory) {
		this.releaseMemory = releaseMemory;
	}
}







