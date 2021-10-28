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
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureCluster;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
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
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.ClusterUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.RawDataUtils;
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

	private LCMSData data;
	private Collection<MsFeature>features;

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
			MassErrorType precursorGroupingMassErrorType) {
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
		features = new ArrayList<MsFeature>();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
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
			groupRelatedMsmsSpectra();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void filterAndDenoise() {
		// TODO Auto-generated method stub
		taskDescription = "Filtering and de-noising MSMS features in " + rawDataFile.getName();
		total = features.size();
		processed = 0;		
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
				msms.setSpecrum(msmsFiltered);
				msms.setEntropy(MsUtils.calculateSpectrumEntropy(msmsFiltered));
			}
			processed++;
		}
	}
	
	private void groupRelatedMsmsSpectra() {
		// TODO Auto-generated method stub
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
			
//			MsFeature avg = clust.getAveragedSpectrumFeature();
//			if(avg != null)
//				uniqueFeatures.add(avg);
			
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
		
		if(dataExtractionRtRange != null) {
			num2scan = num2scan.entrySet().stream().
					filter(x -> dataExtractionRtRange.contains(x.getValue().getRt())).
					collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));
		}
		for(Entry<Integer, IScan> entry: num2scan.entrySet()) {
			
			IScan s = entry.getValue();		
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
			msms.setScanNumber(s.getNum());
			msms.setSpectrumSource(SpectrumSource.EXPERIMENTAL);
			spectrum.addTandemMs(msms);		
			f.setSpectrum(spectrum);
			features.add(f);
			processed++;
		}
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
				precursorGroupingMassErrorType);
	}

	public Collection<MsFeature> getMSMSFeatures() {
		return features;
	}

	public DataFile getRawDataFile() {
		return rawDataFile;
	}
}
