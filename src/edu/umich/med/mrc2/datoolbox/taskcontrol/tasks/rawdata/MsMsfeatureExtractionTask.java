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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.XicDataBundle;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.IntensityMeasure;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ChromatogramUtils;
import edu.umich.med.mrc2.datoolbox.utils.ClusterUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.RawDataUtils;
import edu.umich.med.mrc2.datoolbox.utils.filter.Filter;
import edu.umich.med.mrc2.datoolbox.utils.filter.WeightedMovingAverageFilter;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.LCMSDataSubset;
import umich.ms.datatypes.scan.IScan;
import umich.ms.datatypes.scan.props.PrecursorInfo;
import umich.ms.datatypes.spectrum.ISpectrum;
import umich.ms.fileio.exceptions.FileParsingException;

public class MsMsfeatureExtractionTask extends AbstractTask {

	private DataFile rawDataFile;
	private Range dataExtractionRtRange;
	private boolean removeAllMassesAboveParent;
	private double msMsCountsCutoff;
	private int maxFragmentsCutoff;
	private IntensityMeasure filterIntensityMeasure;
	private double msmsIsolationWindowLowerBorder;
	private double msmsIsolationWindowUpperBorder;
	private double msmsGroupingRtWindow;
	private double precursorGroupingMassError;
	private MassErrorType precursorGroupingMassErrorType;
	private boolean flagMinorIsotopesPrecursors;
	private int maxPrecursorCharge;
	private LCMSData data;
	private Collection<MsFeature>features;
	private double xicWindowHalfWidth;
	
	private Filter smoothingFilter;

	public MsMsfeatureExtractionTask(
			DataFile rawDataFile,
			Range dataExtractionRtRange,
			boolean removeAllMassesAboveParent,
			double msMsCountsCutoff,
			int maxFragmentsCutoff,
			IntensityMeasure filterIntensityMeasure,
			double msmsIsolationWindowLowerBorder,
			double msmsIsolationWindowUpperBorder,
			double msmsGroupingRtWindow,
			double precursorGroupingMassError,
			MassErrorType precursorGroupingMassErrorType,
			boolean flagMinorIsotopesPrecursors,
			int maxPrecursorCharge) {
		super();
		this.rawDataFile = rawDataFile;
		this.dataExtractionRtRange = dataExtractionRtRange;
		this.removeAllMassesAboveParent = removeAllMassesAboveParent;
		this.msMsCountsCutoff = msMsCountsCutoff;
		this.maxFragmentsCutoff = maxFragmentsCutoff;
		this.filterIntensityMeasure = filterIntensityMeasure;
		this.msmsIsolationWindowLowerBorder = msmsIsolationWindowLowerBorder;
		this.msmsIsolationWindowUpperBorder = msmsIsolationWindowUpperBorder;
		this.msmsGroupingRtWindow = msmsGroupingRtWindow;
		this.precursorGroupingMassError = precursorGroupingMassError;
		this.precursorGroupingMassErrorType = precursorGroupingMassErrorType;
		this.flagMinorIsotopesPrecursors = flagMinorIsotopesPrecursors;
		this.maxPrecursorCharge = maxPrecursorCharge;
		
		xicWindowHalfWidth = 0.2d;	//	TODO pass as parameter
		smoothingFilter = new WeightedMovingAverageFilter(9);	//	TODO pass as parameter
		features = new ArrayList<MsFeature>();
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Importing data from file " + rawDataFile.getName();
		total = 100;
		processed = 0;
		try {
			createDataSource();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			extractMSMSFeatures();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			filterAndDenoise();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			mergeRelatedMsmsSpectra();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		if(flagMinorIsotopesPrecursors) {
			try {
				flagFeaturesWithMinorIsotopesPrecursors();
			}
			catch (Exception e1) {
				e1.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void flagFeaturesWithMinorIsotopesPrecursors() {
		
		taskDescription = "Flagging MSMS with minor isotope parent ions "
				+ "and assigning adducts in " + rawDataFile.getName();
		total = features.size();
		processed = 0;	

		for(MsFeature feature : features) {
			
			//	Extract XIC for parent ion
			double mz = feature.getSpectrum().getExperimentalTandemSpectrum().getParent().getMz();
			int scanNum = feature.getSpectrum().getExperimentalTandemSpectrum().getParentScanNumber();
			Range mzRange = MsUtils.createMassRange(
					mz, precursorGroupingMassError, precursorGroupingMassErrorType);
			Range rtRange = new Range(
					feature.getRetentionTime() - xicWindowHalfWidth, 
					feature.getRetentionTime() + xicWindowHalfWidth);			
			XicDataBundle parentBundle = getXicDataBundleForMzAroundRt(mzRange, rtRange);
			findPeakBordersForScan(parentBundle, scanNum);
			
			Collection<MsPoint>ms1pattern = new TreeSet<MsPoint>(MsUtils.mzSorter);
			ms1pattern.add(parentBundle.getRawPointForScan(scanNum));
			
			//	Create lookup ranges for lower M/Z isotopes		
			for (int i = maxPrecursorCharge; i >= 1; i--) {

				mzRange = MsUtils.createMassRange(mz - MsUtils.NEUTRON_MASS / (double) i, precursorGroupingMassError,
						precursorGroupingMassErrorType);
				XicDataBundle isotopeBundle = 
						getXicDataBundleForMzAroundRt(mzRange, parentBundle.getPeakRtRange());
				
				// TODO check for correlation?
				if (isotopeBundle.getmaxSmoothIntensity() > parentBundle.getmaxSmoothIntensity()) {
					
					double corr = ChromatogramUtils.calculateXicCorrelationInPeak(
							parentBundle,
							isotopeBundle, 
							parentBundle.getPeakRtRange());
//					if(corr < 0.3)
//						continue;

					feature.getSpectrum().getExperimentalTandemSpectrum().setParentIonIsMinorIsotope(true);
					ms1pattern.add(isotopeBundle.getRawPointForScan(scanNum));
					
					// Check if there is one more isotope present
					mzRange = MsUtils.createMassRange(mz - (MsUtils.NEUTRON_MASS * 2) / (double) i,
							precursorGroupingMassError, precursorGroupingMassErrorType);
					XicDataBundle nextIsotopeBundle = getXicDataBundleForMzAroundRt(mzRange,
							parentBundle.getPeakRtRange());
					if (nextIsotopeBundle.getmaxSmoothIntensity() > isotopeBundle.getmaxSmoothIntensity())
						ms1pattern.add(nextIsotopeBundle.getRawPointForScan(scanNum));
					
					if(ms1pattern.size() > 1) {
						int charge = feature.getPolarity().getSign() * i;
						Adduct adduct = AdductManager.getDefaultAdductForCharge(charge);
						try {
							feature.getSpectrum().silentlyAddSpectrumForAdduct(adduct, ms1pattern);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					break;
				}
			}
			//	Look up minor isotopes
			if(ms1pattern.size() == 1) {
				
				for (int i = maxPrecursorCharge; i >= 1; i--) {

					mzRange = MsUtils.createMassRange(mz + MsUtils.NEUTRON_MASS / (double) i, precursorGroupingMassError,
							precursorGroupingMassErrorType);
					XicDataBundle isotopeBundle = 
							getXicDataBundleForMzAroundRt(mzRange, parentBundle.getPeakRtRange());
					if (isotopeBundle.getmaxSmoothIntensity() < parentBundle.getmaxSmoothIntensity()) {
						
						double corr = ChromatogramUtils.calculateXicCorrelationInPeak(
								parentBundle,
								isotopeBundle, 
								parentBundle.getPeakRtRange());
//						if(corr < 0.3)
//							continue;

						try {
							ms1pattern.add(isotopeBundle.getRawPointForScan(scanNum));
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
						// Check if there is one more isotope present
						mzRange = MsUtils.createMassRange(mz + (MsUtils.NEUTRON_MASS * 2) / (double) i,
								precursorGroupingMassError, precursorGroupingMassErrorType);
						XicDataBundle nextIsotopeBundle = getXicDataBundleForMzAroundRt(mzRange,
								parentBundle.getPeakRtRange());
						if (nextIsotopeBundle.getmaxSmoothIntensity() < isotopeBundle.getmaxSmoothIntensity())
							ms1pattern.add(nextIsotopeBundle.getRawPointForScan(scanNum));
						
						if(ms1pattern.size() > 1) {
							int charge = feature.getPolarity().getSign() * i;
							Adduct adduct = AdductManager.getDefaultAdductForCharge(charge);
							feature.getSpectrum().silentlyAddSpectrumForAdduct(adduct, ms1pattern);
						}
						break;
					}
				}
			}
			processed++;
		}
	}
	
	private XicDataBundle getXicDataBundleForMzAroundRt(Range mzRange, Range rtRange) {

		double mz = mzRange.getAverage();		
		Map<Integer, IScan>scansInRange = 
				data.getScans().getScansByRtSpanAtMsLevel(rtRange.getMin(), rtRange.getMax(), 1);
		if(scansInRange == null || scansInRange.isEmpty())
			return null;

		TreeSet<MsPoint>rawXic = new TreeSet<MsPoint>(MsUtils.scanSorter);		
		TreeMap<Integer, Double>scanRtMap = new TreeMap<Integer, Double>();
		TreeMap<Double, Double>rtIntensityMap = new TreeMap<Double, Double>();
		Collection<MsPoint>p2average = new ArrayList<MsPoint>();
		for(Entry<Integer, IScan> sce : scansInRange.entrySet()) {
			
			IScan scan = sce.getValue();
			if(scan.getSpectrum() == null)
				try {
					scan.fetchSpectrum();
				} catch (FileParsingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			int scanNum = sce.getKey();
			int[]mzIdx = scan.getSpectrum().findMzIdxs(mzRange.getMin(), mzRange.getMax());
			MsPoint newPoint = null;
			if(mzIdx == null) {
				newPoint = new MsPoint(mz, 0.0d, scanNum);
			}
			else if(mzIdx[0] == mzIdx[1]){
				newPoint = new MsPoint(
						scan.getSpectrum().getMZs()[mzIdx[0]], 
						scan.getSpectrum().getIntensities()[mzIdx[0]], 
						scanNum);
			}
			else {
				p2average.clear();
				for(int i=mzIdx[0]; i<=mzIdx[1]; i++) {
					p2average.add(new MsPoint(
							scan.getSpectrum().getMZs()[i], 
							scan.getSpectrum().getIntensities()[i], 
							scanNum));
					newPoint = MsUtils.getAveragePoint(p2average);
					newPoint.setScanNum(scanNum);				
				}
			}
			rawXic.add(newPoint);
			scanRtMap.put(scanNum, scan.getRt());
			rtIntensityMap.put(scan.getRt(), newPoint.getIntensity());
		}
		double[] intensityArray = rtIntensityMap.values().stream().mapToDouble(Double::doubleValue).toArray();
		double[] timeArray = rtIntensityMap.keySet().stream().mapToDouble(Double::doubleValue).toArray();
		double[] filtered = smoothingFilter.filter(timeArray, intensityArray);
		TreeSet<MsPoint>smoothXicPoints = new TreeSet<MsPoint>(MsUtils.scanSorter);
		MsPoint[] rawPoints = rawXic.toArray(new MsPoint[rawXic.size()]);
		for(int i=0; i < filtered.length; i++) 			
			smoothXicPoints.add(new MsPoint(rawPoints[i].getMz(), filtered[i], rawPoints[i].getScanNum()));
		
		XicDataBundle bundle = new XicDataBundle(rawXic, smoothXicPoints, rtRange, rtRange);
		bundle.setScanRtMap(scanRtMap);
		return bundle;
	}
	
	private void findPeakBordersForScan(XicDataBundle xicBundle, int scanNum){
		
		Map<Integer, Double> smoothInt = xicBundle.getSmoothedIntensityByScan();		
		double prev = 0.0d;
		double current = 0.0d;
		double next = 0.0d;
		for(int i : smoothInt.keySet()) {
			if(i<scanNum)
				prev = smoothInt.get(i);
			else if(i==scanNum)
				current = smoothInt.get(i);
			if(i>scanNum) {
				next = smoothInt.get(i);
				break;
			}
		}
		int[]scanNums = smoothInt.keySet().stream().mapToInt(Integer::intValue).toArray();
		int refIdx = 0;
		for(int i=0; i<scanNums.length; i++) {
			if(scanNums[i] == scanNum) {
				refIdx = i;
				break;
			}
		}	
		double smoothMax = xicBundle.getmaxSmoothIntensity();
		int leftMinIdx = 0;
		int leftMaxIdx = 0;
		int rightMinIdx = scanNums.length-1;
		int rightMaxIdx = 0;
		
		//	If on the left slope
		if(current > prev && current < next) {			
			
			//	Left min
			double leftMin = smoothMax;
			for(int i=refIdx-1; i>=0; i--) {
				if(smoothInt.get(scanNums[i]) < leftMin) {
					leftMin = smoothInt.get(scanNums[i]);
					leftMinIdx = i;
				}
			}
			//	Right max
			double rightMax = 0.0d;
			for(int i=refIdx+1; i<scanNums.length; i++) {
				if(smoothInt.get(scanNums[i]) > rightMax) {
					rightMax = smoothInt.get(scanNums[i]);
					rightMaxIdx = i;
				}
			}			
			//	Right min
			double rightMin = rightMax;
			for(int i=rightMaxIdx+1; i<scanNums.length; i++) {
				if(smoothInt.get(scanNums[i]) < rightMin) {
					rightMin = smoothInt.get(scanNums[i]);
					rightMinIdx = i;
				}
			}
		}
		//	If on the right slope
		if(current < prev && current > next) {			
			
			//	Right min
			double rightMin = smoothMax;
			for(int i=refIdx+1; i<scanNums.length; i++) {
				if(smoothInt.get(scanNums[i]) < rightMin) {
					rightMin = smoothInt.get(scanNums[i]);
					rightMinIdx = i;
				}
			}
			//	Left max
			double leftMax = 0.0d;
			for(int i=refIdx-1; i>=0; i--) {
				if(smoothInt.get(scanNums[i]) > leftMax) {
					leftMax = smoothInt.get(scanNums[i]);
					leftMaxIdx = i;
				}
			}
			//	Left min
			double leftMin = leftMax;
			for(int i=leftMaxIdx-1; i>=0; i--) {
				if(smoothInt.get(scanNums[i]) < leftMin) {
					leftMin = smoothInt.get(scanNums[i]);
					leftMinIdx = i;
				}
			}			
		}
		if(current == smoothMax) {
			
			//	Right min
			double rightMin = smoothMax;
			for(int i=refIdx+1; i<scanNums.length; i++) {
				if(smoothInt.get(scanNums[i]) < rightMin) {
					rightMin = smoothInt.get(scanNums[i]);
					rightMinIdx = i;
				}
			}
			//	Left min
			double leftMin = smoothMax;
			for(int i=refIdx-1; i>=0; i--) {
				if(smoothInt.get(scanNums[i]) < leftMin) {
					leftMin = smoothInt.get(scanNums[i]);
					leftMinIdx = i;
				}
			}
		}
		Range peakRange = new Range(
				xicBundle.getScanRtMap().get(scanNums[leftMinIdx]),
				xicBundle.getScanRtMap().get(scanNums[rightMinIdx]));
		xicBundle.setPeakRtRange(peakRange);
	}

	private void filterAndDenoise() {
		// TODO Auto-generated method stub
		taskDescription = "Filtering and de-noising MSMS features in " + rawDataFile.getName();
		total = features.size();
		processed = 0;		
		Collection<MsFeature>discarded = new ArrayList<MsFeature>();
		for(MsFeature feature : features) {
			
			//	MS1
//			List<MsPoint> msOneRaw = 
//					feature.getSpectrum().getMsPoints().stream().
//					sorted(MsUtils.reverseIntensitySorter).
//					collect(Collectors.toList());
//			Collection<MsPoint> msOneFiltered = msOneRaw;
//			if(msMsCountsCutoff > 0) {
//				
//				if(filterIntensityMeasure.equals(IntensityMeasure.ABSOLUTE)) {
//					msOneFiltered = msOneRaw.stream().
//						filter(p -> p.getIntensity() > msMsCountsCutoff).
//						collect(Collectors.toList());
//				}
//				if(filterIntensityMeasure.equals(IntensityMeasure.RELATIVE)) {
//					
//					double topIntensity = feature.getSpectrum().getBasePeak().getIntensity() / 100;
//					msOneFiltered = msOneRaw.stream().
//							filter(p -> p.getIntensity()/topIntensity > msMsCountsCutoff).
//							collect(Collectors.toList());
//				}
//			}
//			msOneFiltered  = msOneFiltered.stream().
//					sorted(MsUtils.mzSorter).collect(Collectors.toList());
//			feature.getSpectrum().replaceDataPoints(msOneFiltered);
			
			//	MS2
			TandemMassSpectrum msms = feature.getSpectrum().getExperimentalTandemSpectrum();
			if(msms != null) {
							
				List<MsPoint> msmsRaw = 
						msms.getSpectrum().stream().
						sorted(MsUtils.reverseIntensitySorter).
						collect(Collectors.toList());
				Collection<MsPoint> msmsFiltered = msmsRaw;
				if(msMsCountsCutoff > 0) {
					
					if(filterIntensityMeasure.equals(IntensityMeasure.ABSOLUTE)) {
						msmsFiltered = msmsRaw.stream().
							filter(p -> p.getIntensity() > msMsCountsCutoff).
							collect(Collectors.toList());
					}
					if(filterIntensityMeasure.equals(IntensityMeasure.RELATIVE)) {				
						double topIntensity = msms.getBasePeak().getIntensity() / 100;
						msmsFiltered = msmsRaw.stream().
								filter(p -> p.getIntensity()/topIntensity > msMsCountsCutoff).
								collect(Collectors.toList());
					}
				}
				if(maxFragmentsCutoff > 0) {
					msmsFiltered  = msmsFiltered.stream().
							sorted(MsUtils.reverseIntensitySorter).
							limit(maxFragmentsCutoff).collect(Collectors.toList());
				}
				msmsFiltered  = msmsFiltered.stream().
						sorted(MsUtils.mzSorter).collect(Collectors.toList());
				
				if(removeAllMassesAboveParent) {
					double precursorMz = msms.getParent().getMz() + 0.5d;
					msmsFiltered  = msmsFiltered.stream().filter(p -> p.getMz() <= precursorMz).
							sorted(MsUtils.mzSorter).collect(Collectors.toList());
				}	
				if(msmsFiltered.isEmpty()) {
					discarded.add(feature);
				}
				else {
					msms.setSpecrum(msmsFiltered);
					msms.setEntropy(MsUtils.calculateSpectrumEntropy(msmsFiltered));
				}
			}
			processed++;
		}
		if(!discarded.isEmpty())
			features.removeAll(discarded);
	}
	
	private void mergeRelatedMsmsSpectra() {

		taskDescription = "Grouping related MSMS features in " + rawDataFile.getName();
		total = features.size();
		processed = 0;
		HashSet<MsFeature>assigned = new HashSet<MsFeature>();
		ArrayList<MsFeatureCluster> clusters = new ArrayList<MsFeatureCluster>();
		DataPipeline dataPipeline = createDummyDataPipeline();
		features = features.stream().
				sorted(new MsFeatureComparator(SortProperty.RT)).
				collect(Collectors.toList());
		
		for (MsFeature cf : features) {

			for (MsFeatureCluster fClust : clusters) {

				if (fClust.matchesOnMSMSParentIon(cf,
						precursorGroupingMassError, 
						precursorGroupingMassErrorType, 
						msmsGroupingRtWindow)) {

					fClust.addFeature(cf, dataPipeline);
					assigned.add(cf);
					break;
				}
			}
			if (!assigned.contains(cf)) {

				MsFeatureCluster newCluster = new MsFeatureCluster();
				newCluster.addFeature(cf, dataPipeline);
				assigned.add(cf);
				clusters.add(newCluster);
			}
			processed++;
		}
		clusters.stream().forEach(c -> c.setPrimaryFeature(ClusterUtils.getMostIntensiveMsmsFeature(c)));
		List<MsFeature> uniqueFeatures = clusters.stream().
			filter(c -> c.getFeatures().size() == 1).
			map(c -> c.getPrimaryFeature()).
			collect(Collectors.toList());
		
		Collection<MsFeatureCluster>duplicateList = clusters.stream().
				filter(c -> c.getFeatures().size() > 1).
				collect(Collectors.toList());
		taskDescription = "Averaging redundant MSMS features in " + rawDataFile.getName();
		total = duplicateList.size();
		processed = 0;
		for (MsFeatureCluster clust : duplicateList){
			
			MsFeature avg = clust.getAveragedMSMSFeature(
					precursorGroupingMassError, 
					precursorGroupingMassErrorType);
			if(avg != null)
				uniqueFeatures.add(avg);
			
			processed++;
		}
		features = uniqueFeatures.stream().
				sorted(new MsFeatureComparator(SortProperty.RT)).
				collect(Collectors.toList());
	}
	
	private DataPipeline createDummyDataPipeline() {
		DataAcquisitionMethod acqMethod = new DataAcquisitionMethod(
				"QQQ",
				"Dummy DataAcquisitionMethod",
				"",
				MRC2ToolBoxCore.getIdTrackerUser(),
				new Date());
		DataExtractionMethod dacq = new DataExtractionMethod(
				"ZZZ",
				"Dummy DataAcquisitionMethod",
				"",
				MRC2ToolBoxCore.getIdTrackerUser(),
				new Date());
		return new DataPipeline(acqMethod, dacq);
	}

	private void extractMSMSFeatures() {

		taskDescription = "Extracting MSMS features from " + rawDataFile.getName();
		Map<Integer, IScan>num2scan = data.getScans().getMapMsLevel2index().get(2).getNum2scan();
		total =  num2scan.size();
		processed = 0;			
		if(dataExtractionRtRange != null) 			
			num2scan = data.getScans().getScansByRtSpanAtMsLevel(
					dataExtractionRtRange.getMin(), dataExtractionRtRange.getMax(), 2);
		
		for(Entry<Integer, IScan> entry: num2scan.entrySet()) {
			
			IScan s = entry.getValue();	
			int scanNum = entry.getKey();
			IScan parentScan = getParentScan(s);
			if(parentScan == null) {
				processed++;
				continue;			
			}
			Polarity polarity = Polarity.Positive;
			if(s.getPolarity().equals(umich.ms.datatypes.scan.props.Polarity.NEGATIVE))
				polarity = Polarity.Negative;
			
			MsFeature f = new MsFeature(s.getRt(), polarity);
			MassSpectrum spectrum = new MassSpectrum();
			spectrum.addDataPoints(RawDataUtils.getScanPoints(parentScan));		
			PrecursorInfo precursor = s.getPrecursor();
			Double targetMz = precursor.getMzTarget();
			if(targetMz == null)
				targetMz = precursor.getMzTargetMono();
			Range isolationWindow = new Range(
					targetMz - msmsIsolationWindowLowerBorder, 
					targetMz + msmsIsolationWindowUpperBorder);			
			if(precursor.getMzRange() != null)
				isolationWindow = new Range(isolationWindow.getMin(), isolationWindow.getMax());
			
			MsPoint parent = getActualPrecursor(parentScan, isolationWindow);
			if(parent == null)
				parent = new MsPoint(targetMz, s.getPrecursor().getIntensity());				
			
			String name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
					MRC2ToolBoxConfiguration.defaultMzFormat.format(parent.getMz()) + "_" + 
					MRC2ToolBoxConfiguration.defaultRtFormat.format(parentScan.getRt());
			f.setName(name);
			Collection<MsPoint> msmsPoints = RawDataUtils.getScanPoints(s);
			if(!msmsPoints.isEmpty()) {
				
				TandemMassSpectrum msms = new TandemMassSpectrum(
						2, 
						parent,
						RawDataUtils.getScanPoints(s),
						polarity);				
				msms.setIsolationWindow(isolationWindow);
				if(precursor.getActivationInfo() != null) {
					Double ach = precursor.getActivationInfo().getActivationEnergyHi();
					Double acl = precursor.getActivationInfo().getActivationEnergyLo();
					if(ach != null && acl != null)
						msms.setCidLevel((acl + ach)/2.0d);
				}
				msms.setScanNumber(scanNum);
				msms.setParentScanNumber(parentScan.getNum());
				msms.setSpectrumSource(SpectrumSource.EXPERIMENTAL);				
				Collection<MsPoint>minorParentIons = getMinorParentIons(
						parentScan, isolationWindow, parent.getMz());
				if(minorParentIons != null && !minorParentIons.isEmpty())
					msms.setMinorParentIons(minorParentIons);
				
				spectrum.addTandemMs(msms);		
				f.setSpectrum(spectrum);
				features.add(f);
			}
			processed++;
		}
	}
	
	private Collection<MsPoint>getMinorParentIons(
			IScan ms1can, Range isolationWindow, double parentMz){
				
		ISpectrum spectrum = ms1can.getSpectrum();
		int[] precursorIdx = spectrum.findMzIdxs(isolationWindow.getMin(), isolationWindow.getMax());
		if(precursorIdx == null)
			return null;

		Collection<MsPoint>minorParentIons = new ArrayList<MsPoint>();
		for(int i=precursorIdx[0]; i<=precursorIdx[1]; i++) {
			if(spectrum.getMZs()[i] != parentMz)
				minorParentIons.add(new MsPoint(
						spectrum.getMZs()[i], spectrum.getIntensities()[i]));
		}
		return minorParentIons;
	}
	
	private MsPoint getActualPrecursor(IScan ms1can, Range isolationWindow) {
	
		ISpectrum spectrum = ms1can.getSpectrum();
		int[] precursorIdx = spectrum.findMzIdxs(isolationWindow.getMin(), isolationWindow.getMax());
		if(precursorIdx == null)
			return null;
		
		int maxIdx = -1;
		double maxIntensity = 0.0d;
		for(int i=precursorIdx[0]; i<=precursorIdx[1]; i++) {

			if(spectrum.getIntensities()[i] > maxIntensity) {
				maxIntensity = spectrum.getIntensities()[i];
				maxIdx = i;
			}
		}
		if(maxIdx > -1)
			return new MsPoint(spectrum.getMZs()[maxIdx], spectrum.getIntensities()[maxIdx]);
		else
			return null;
	}
	
	private IScan getParentScan(IScan s) {
		
		if(s.getPrecursor() == null)
			return null;
					
		int parentScanNumber = s.getPrecursor().getParentScanNum();
		return data.getScans().getScanByNum(parentScanNumber);
	}
	
	private void createDataSource() throws FileParsingException {
	
		if (isCanceled())
			return;
		
		data = RawDataManager.getRawData(rawDataFile);
		
		if(!data.getScans().getMapMsLevel2index().containsKey(2)) {
			System.out.println("No MSMS data in file " + rawDataFile.getName());
			setStatus(TaskStatus.ERROR);
			return;
		}
		data.load(LCMSDataSubset.WHOLE_RUN, this);		
	}

	@Override
	public Task cloneTask() {
		return new MsMsfeatureExtractionTask(
				rawDataFile,
				dataExtractionRtRange,
				removeAllMassesAboveParent,
				msMsCountsCutoff,
				maxFragmentsCutoff,
				filterIntensityMeasure,
				msmsIsolationWindowLowerBorder,
				msmsIsolationWindowUpperBorder,
				msmsGroupingRtWindow,
				precursorGroupingMassError,
				precursorGroupingMassErrorType,
				flagMinorIsotopesPrecursors,
				maxPrecursorCharge);
	}

	public Collection<MsFeature> getMSMSFeatures() {
		return features;
	}

	public DataFile getRawDataFile() {
		return rawDataFile;
	}
}
