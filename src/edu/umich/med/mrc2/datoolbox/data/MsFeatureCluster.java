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

package edu.umich.med.mrc2.datoolbox.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.util.Precision;
import org.jdom2.Element;
import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation.Ret;

import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSScoreCalculator;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.store.CommonFields;
import edu.umich.med.mrc2.datoolbox.project.store.MsFeatureClusterFields;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;
import edu.umich.med.mrc2.datoolbox.utils.ClusterUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MsFeatureCluster implements Serializable, XmlStorable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5100249593042159601L;

	private String clusterId;
	private Map<DataPipeline, Collection<MsFeature>>clusterFeatures;
	private Map<DataPipeline, DescriptiveStatistics>featureRTStatistics;	
	private Map<DataPipeline, DescriptiveStatistics>featureMZStatistics;
	private Collection<MsFeature>disabledFeatures;
	private Collection<MsFeature>markedForMerge;
	private Matrix correlationMatrix;
	private Collection<ModificationBlock> chemicalModificationsMap;
	private Map<MsFeature, Set<Adduct>> annotationMap;
	private boolean locked;
	private boolean chargeMismatch;
	private MsFeature primaryFeature;

	public MsFeatureCluster() {

		this.clusterId = DataPrefix.MS_FEATURE_CLUSTER.getName() 
				+ UUID.randomUUID().toString();
		clusterFeatures = new TreeMap<>();
		featureRTStatistics = new TreeMap<>();
		featureMZStatistics = new TreeMap<>();
		chemicalModificationsMap = new HashSet<>();
		correlationMatrix = null;
		primaryFeature = null;
		locked = false;
		disabledFeatures = new HashSet<>();
		markedForMerge = new HashSet<>();
	}

	public void addFeature(MsFeature cf, DataPipeline pipeline) {
		
		if(!clusterFeatures.containsKey(pipeline)) {
			clusterFeatures.put(pipeline, new HashSet<>());
			featureRTStatistics.put(pipeline, new DescriptiveStatistics());
			featureMZStatistics.put(pipeline, new DescriptiveStatistics());
		}		
		clusterFeatures.get(pipeline).add(cf);
		
		if(cf.getStatsSummary() != null) {
			featureRTStatistics.get(pipeline).addValue(cf.getStatsSummary().getMedianObservedRetention());
			featureMZStatistics.get(pipeline).addValue(cf.getStatsSummary().getMedianObservedMz());
		}
		else {
			featureRTStatistics.get(pipeline).addValue(cf.getRetentionTime());
			featureMZStatistics.get(pipeline).addValue(cf.getMonoisotopicMz());
		}
		chargeMismatch = clusterFeatures.get(pipeline).stream().
				map(f -> f.getCharge()).distinct().count() > 1;
				
		if(primaryFeature == null)
			primaryFeature = cf;
	}	

	public Collection<Double>getMassDifferencesWithinRange(
			Range differenceRange,
			DataPipeline pipeline){

		Collection<Double>massDifferences = new ArrayList<Double>();
		MsFeature[] fArray = clusterFeatures.get(pipeline).
				stream().toArray(size -> new MsFeature[size]);
		for(int i=0; i<fArray.length-1; i++) {

			for(int j=i+1; j<fArray.length; j++) {

				double delta = Math.abs(fArray[i].getBasePeakMz() - fArray[j].getBasePeakMz());
				if(differenceRange.contains(delta))
					massDifferences.add(delta);
			}
		}
		return massDifferences;
	}

	public void addModificationBlock(ModificationBlock newBlock) {

		if (getFeatures().contains(newBlock.getFeatureOne()) &&
				getFeatures().contains(newBlock.getFeatureTwo())) {

			chemicalModificationsMap.add(newBlock);
		}
	}

	public boolean containsFeature(MsFeature cf) {
		return getFeatures().contains(cf);
	}

	public boolean containsNamedFeatures() {

		return clusterFeatures.values().stream().
				flatMap(c -> c.stream()).anyMatch(f -> f.isIdentified());
	}

	public boolean isSingleDataPipeline() {
		return clusterFeatures.keySet().size() == 1;
	}

	public Matrix createCorrelationMatrix(boolean activeOnly) {

		//	TODO this may change to accomodate comparison of different methods
		if(clusterFeatures.isEmpty() || !isSingleDataPipeline())
			return null;

		DataPipeline assay = clusterFeatures.keySet().iterator().next();
		Matrix dataMatrix = MRC2ToolBoxCore.getActiveMetabolomicsExperiment().
				getDataMatrixForDataPipeline(assay);

		Collection<MsFeature> sorted = null;
		Matrix corrMatrix = null;

		if (activeOnly)
			sorted = this.getActiveFeatures();
		else
			sorted = clusterFeatures.get(assay).stream().
				sorted(new MsFeatureComparator(SortProperty.Name)).
				collect(Collectors.toList());
		long[] columnIndex =
				sorted.stream().
				map(f -> dataMatrix.getColumnForLabel(f)).
				mapToLong(i -> i).
				toArray();
		
		MsFeature[]sortedFeatureArray = sorted.toArray(new MsFeature[sorted.size()]);
		

		//	TODO	Select active samples - this is a temporary fix untill design subsets are implemented
		ArrayList<Long>rowList = new ArrayList<Long>();
		for(DataFile file : MRC2ToolBoxCore.getActiveMetabolomicsExperiment().
				getDataFilesForAcquisitionMethod(assay.getAcquisitionMethod())) {

			if(file.isEnabled())
				rowList.add(dataMatrix.getRowForLabel(file));
		}
		long[] rowIndex = ArrayUtils.toPrimitive(rowList.toArray(new Long[rowList.size()]));
		Matrix raw = dataMatrix.select(Ret.LINK, rowIndex, columnIndex).replace(Ret.NEW, 0.0d, Double.NaN);
		corrMatrix = raw.corrcoef(Ret.LINK, true, false).replace(Ret.NEW, Double.NaN, 0.0d);
		corrMatrix.setMetaDataDimensionMatrix(0, Matrix.Factory.linkToArray((Object[])sortedFeatureArray));
		corrMatrix.setMetaDataDimensionMatrix(1, Matrix.Factory.linkToArray((Object[])sortedFeatureArray).transpose(Ret.NEW));
		return corrMatrix;
	}

	public Collection<MsFeature>getActiveFeatures() {
		
		return clusterFeatures.values().stream().
			flatMap(c -> c.stream()).
			filter(f -> !disabledFeatures.contains(f)).
			collect(Collectors.toSet());
	}

	public Map<MsFeature, Set<Adduct>> getAnnotationMap() {
		return annotationMap;
	}

	public Collection<ModificationBlock> getChemicalModificationsMap() {
		return chemicalModificationsMap;
	}

	public Matrix getClusterCorrMatrix() {
		return correlationMatrix;
	}

	public String getClusterId() {
		return clusterId;
	}

	public double getCorrelation(MsFeature compoundFeature, MsFeature compoundFeature2) {

		long[] coordinate = new long[] { correlationMatrix.getRowForLabel(compoundFeature),
				correlationMatrix.getRowForLabel(compoundFeature2) };
		return correlationMatrix.getAsDouble(coordinate);
	}

	public double getMinimalCorrelation() {
		
		if(correlationMatrix == null)
			return 0.0d;
		else
			return correlationMatrix.getMinValue();
	}

	public List<MsFeature> getFeatures() {

		return clusterFeatures.values().stream().flatMap(c -> c.stream()).
				sorted(new MsFeatureComparator(SortProperty.Name))
				.collect(Collectors.toList());
	}

	public Map<DataPipeline, Collection<MsFeature>>getFeatureMap(){
		return clusterFeatures;
	}

	public Matrix getInverseCorrMatrix() {
		return Matrix.Factory.fill(1, correlationMatrix.getSize()).
				minus(correlationMatrix).replace(Ret.NEW, Double.NaN, 0.0d);
	}

	public HashSet<Adduct> getModificationsForFeaturePair(MsFeature featureOne, MsFeature featureTwo) {

		HashSet<Adduct> featureMods = new HashSet<Adduct>();

		for (ModificationBlock b : chemicalModificationsMap) {

			if (b.getFeatureOne().equals(featureOne) && b.getFeatureTwo().equals(featureTwo))
				featureMods.add(b.getModification());
		}
		return featureMods;
	}

	public String getNameForXicMethod(DataPipeline pipeline) {

		String clusterName = "Cluster" + "_" + 
				MRC2ToolBoxConfiguration.getMzFormat().format(getTopMass()) + "@"
				+ MRC2ToolBoxConfiguration.getRtFormat().format(
						featureRTStatistics.get(pipeline).getPercentile(50.0d));

		return clusterName;
	}

	public int getNumberOfNamed() {

		return (int) clusterFeatures.values().
				stream().flatMap(c -> c.stream()).
				filter(cf -> cf.isIdentified()).count();
	}

	public MsFeature getPrimaryFeature() {
		return primaryFeature;
	}

	public Range getRtRange(DataPipeline pipeline) {
		return new Range(featureRTStatistics.get(pipeline).getMin(), 
				featureRTStatistics.get(pipeline).getMax());
	}

	public double getTopArea() {
		
		MsFeature mif = getMostIntensiveFeature();
		if(mif != null)
			return mif.getAveragePeakArea();
		else 
			return 0.0d;
	}
	
	public MsFeature getMostIntensiveFeature() {
		
		return clusterFeatures.values().stream().
				flatMap(c -> c.stream()).
				sorted(new MsFeatureComparator(SortProperty.Area, SortDirection.DESC)).
				findFirst().orElse(null);
	}

	public double getTopMass() {
		
		MsFeature mif = getMostIntensiveFeature();
		if(mif != null)
			return mif.getMonoisotopicMz();
		else 
			return 0.0d;
	}

	public boolean isLocked() {
		return locked;
	}
	
	public boolean matches(
			MsFeature cf, 
			double massAccuracy, 
			MassErrorType massErrorType,
			double rtWindow) {
		
		for(Entry<DataPipeline, Collection<MsFeature>> plf : clusterFeatures.entrySet()) {
			
			if(plf.getValue().contains(cf))
				return matches(cf, plf.getKey(), massAccuracy, massErrorType, rtWindow);
		}
		return false;
	}
	
	public boolean matches(
			MsFeature cf, 
			DataPipeline pipeline,
			double massAccuracy, 
			MassErrorType massErrorType,
			double rtWindow) {

		if(clusterFeatures.get(pipeline) == null 
				|| clusterFeatures.get(pipeline).isEmpty())
			return false;
		
		double avgRt = featureRTStatistics.get(pipeline).getPercentile(50.0d);
		Range refRtRange = new Range(avgRt - rtWindow, avgRt + rtWindow);
		double frt = cf.getRetentionTime();
		if(cf.getStatsSummary() != null)
			frt = cf.getStatsSummary().getMedianObservedRetention();
		
		if(!refRtRange.contains(frt))
			return false;
		
		double avgMz = featureMZStatistics.get(pipeline).getPercentile(50.0d);
		Range refMZRange = MsUtils.createMassRange(avgMz, massAccuracy, massErrorType);
		double fmz = cf.getMonoisotopicMz();
		if(cf.getStatsSummary() != null)
			fmz = cf.getStatsSummary().getMedianObservedMz();
				
		if(!refMZRange.contains(fmz))
			return false;	
		
		return true;
	}
	
	public boolean matchesPrimaryFeature(
			MsFeature cf, 
			DataPipeline pipeline,
			double massAccuracy, 
			double rtWindow) {
		
		if(clusterFeatures.get(pipeline) == null)
			return false;
		
		if(primaryFeature == null)
			return false;

		return MsUtils.matchesFeature(
				primaryFeature, cf, massAccuracy, rtWindow, true);
	}
	
	public boolean matchesPrimaryFeatureOnBasePeakMz(
			MsFeature cf, 
			DataPipeline pipeline,
			double massAccuracy, 
			MassErrorType massErrorType) {
		
		if(clusterFeatures.get(pipeline) == null)
			return false;
		
		if(primaryFeature == null 
				|| primaryFeature.getSpectrum() == null 
				|| primaryFeature.getSpectrum().getBasePeak() == null)
			return false;
		
		if(cf == null 
				|| cf.getSpectrum() == null 
				|| cf.getSpectrum().getBasePeak() == null)
			return false;
		
		Range testRange = MsUtils.createMassRange(
				primaryFeature.getSpectrum().getBasePeak().getMz(), massAccuracy, massErrorType);

		return testRange.contains(cf.getSpectrum().getBasePeak().getMz());
	}
	
	public Range getBasePeakMzRange(DataPipeline pipeline) {
		
		if(primaryFeature == null 
				|| primaryFeature.getSpectrum() == null 
				|| primaryFeature.getSpectrum().getBasePeak() == null)
			return null;
		
		Range bpRange = new Range(primaryFeature.getSpectrum().getBasePeak().getMz());
		for(MsFeature cf : clusterFeatures.get(pipeline)) {
			
			if( cf.getSpectrum() != null && cf.getSpectrum().getBasePeak() != null)
				bpRange.extendRange(cf.getSpectrum().getBasePeak().getMz());
		}		
		return bpRange;
	}
	
	public boolean matchesPrimaryFeatureOnMSMSParentIonMz(
			MsFeature cf, 
			DataPipeline pipeline,
			double massAccuracy, 
			MassErrorType massErrorType) {
		
		if(clusterFeatures.get(pipeline) == null)
			return false;
		
		if(primaryFeature == null 
				|| primaryFeature.getSpectrum() == null 
				|| primaryFeature.getSpectrum().getExperimentalTandemSpectrum() == null
				|| primaryFeature.getSpectrum().getExperimentalTandemSpectrum().getParent() == null)
			return false;
		
		if(cf == null 
				|| cf.getSpectrum().getExperimentalTandemSpectrum() == null
				|| cf.getSpectrum().getExperimentalTandemSpectrum().getParent() == null)
			return false;
		
		Range testRange = MsUtils.createMassRange(
				primaryFeature.getSpectrum().getExperimentalTandemSpectrum().getParent().getMz(), massAccuracy, massErrorType);

		return testRange.contains(cf.getSpectrum().getExperimentalTandemSpectrum().getParent().getMz());
	}
	
	public Range getMSMSParentIonMzRange(DataPipeline pipeline) {
		
		if(primaryFeature == null
				|| primaryFeature.getSpectrum() == null 
				|| primaryFeature.getSpectrum().getExperimentalTandemSpectrum() == null
				|| primaryFeature.getSpectrum().getExperimentalTandemSpectrum().getParent() == null)
			return null;
		
		Range bpRange = new Range(
				primaryFeature.getSpectrum().getExperimentalTandemSpectrum().getParent().getMz());
		for(MsFeature cf : clusterFeatures.get(pipeline)) {
			
			if( cf.getSpectrum() != null 
					&& cf.getSpectrum().getExperimentalTandemSpectrum() != null
					&& cf.getSpectrum().getExperimentalTandemSpectrum().getParent() != null)
				bpRange.extendRange(cf.getSpectrum().getExperimentalTandemSpectrum().getParent().getMz());
		}		
		return bpRange;
	}
	
	public boolean nameMatches(
			MsFeature cf, 
			DataPipeline pipeline) {
		
		if(clusterFeatures.get(pipeline) == null)
			return false;

		String newName = cf.getName();
		return clusterFeatures.get(pipeline).stream().
				filter(f -> f.getName().equals(newName)).
				findFirst().isPresent();
	}
	
	public boolean matchesOnMSMSParentIon(
			MsFeature cf, 
			double massAccuracy, 
			MassErrorType massErrorType,
			double rtWindow) {

		return clusterFeatures.values().stream().
				flatMap(c -> c.stream()).
				filter(f -> MsUtils.matchesOnMSMSParentIon(
						f, cf, massAccuracy, massErrorType, rtWindow, true)).
				findFirst().isPresent();
	}
	
	public boolean matchesPrimaryFeatureOnMSMSParentIonMzRt(
			MsFeature cf, 
			double massAccuracy, 
			MassErrorType massErrorType,
			double rtWindow) {
		
		if(primaryFeature != null)
			return MsUtils.matchesOnMSMSParentIon(
					primaryFeature, cf, massAccuracy, massErrorType, rtWindow, true);
		else
			return false;
	}
	
	public boolean matchesOnCollisionEnergy(MsFeature cf) {
		
		if(cf.getSpectrum().getExperimentalTandemSpectrum() == null)
			return false;
		
		Set<Double> cidLevels = clusterFeatures.values().stream().
				flatMap(c -> c.stream()).
				filter(f -> Objects.nonNull(f.getSpectrum().getExperimentalTandemSpectrum())).
				map(f -> f.getSpectrum().getExperimentalTandemSpectrum().getCidLevel()).
				collect(Collectors.toSet());
				
		double newCid = cf.getSpectrum().getExperimentalTandemSpectrum().getCidLevel();
		for(double cid : cidLevels) {
			
			if(Precision.equalsIncludingNaN(cid, newCid, Precision.EPSILON))
				return true;
		}				
		return false;	
	}

	public void removeFeature(MsFeature cf) {
		
		for(Entry<DataPipeline, Collection<MsFeature>> dpf : clusterFeatures.entrySet()) {
			
			if(dpf.getValue().contains(cf)) {
				
				dpf.getValue().remove(cf);
				updateStatisticsForDataPipeline(dpf.getKey());
				
				//	TODO - rewrite to handle pipelines
				//	clusterCorrMatrix = createClusterCorrelationMatrix(false);				
			}
		}
	}

	public void removeFeatures(Collection<MsFeature> cfList) {

		clusterFeatures.values().stream().forEach(c -> c.removeAll(cfList));
		for(DataPipeline dp : clusterFeatures.keySet())
			updateStatisticsForDataPipeline(dp);	
		
		//	TODO - rewrite to handle pipelines
		//	clusterCorrMatrix = createClusterCorrelationMatrix(false);
	}
	
	private void updateStatisticsForDataPipeline(DataPipeline pipeline) {
		
		featureRTStatistics.get(pipeline).clear();
		featureMZStatistics.get(pipeline).clear();
		clusterFeatures.get(pipeline).stream().forEach(f -> {
			featureRTStatistics.get(pipeline).addValue(f.getRetentionTime());
			featureMZStatistics.get(pipeline).addValue(f.getMonoisotopicMz());
		});
	}

	public void setAnnotationMap(Map<MsFeature, Set<Adduct>> annotationMap) {
		this.annotationMap = annotationMap;
	}

	public void setClusterCorrMatrix(Matrix clusterCorrMatrix) {
		this.correlationMatrix = clusterCorrMatrix;
	}

	public void setLocked(boolean locked) {

		this.locked = locked;
	}

	public void setPrimaryFeature(MsFeature cf) {
		
		if(!containsFeature(cf))
			throw new IllegalArgumentException("Feature not in cluster!");

		primaryFeature = cf;
	}

	@Override
	public String toString() {

		String clusterName = "";
		if(getPrimaryIdentity() != null 
				&& getPrimaryIdentity().getCompoundIdentity() != null)
			clusterName = getPrimaryIdentity().getCompoundName() + " ";

		clusterName += 
				"RT " + MRC2ToolBoxConfiguration.getRtFormat().format(getRtRange().getAverage()) + 
				" | group of " + getFeatures().size() + 
				" | top M/Z " + MRC2ToolBoxConfiguration.getMzFormat().format(getTopMass());

		return clusterName;
	}

	//	TODO may need more refined approach
	public MsFeatureIdentity getPrimaryIdentity() {

		if(primaryFeature == null)
			primaryFeature = ClusterUtils.getMostIntensiveFeature(getFeatures());
		
		return primaryFeature.getPrimaryIdentity();
	}

	public void enableAllFeatures() {
		disabledFeatures.clear();
	}

	public void setFeatureEnabled(MsFeature f, boolean enabled) {
		
		if(enabled)
			disabledFeatures.remove(f);
		else
			disabledFeatures.add(f);
	}
	
	public boolean isFeatureEnabled(MsFeature f) {
		return !disabledFeatures.contains(f);
	}
	
	public DataPipeline getDataPipelineForFeature(MsFeature f) {
		
		for (Entry<DataPipeline, Collection<MsFeature>> entry : clusterFeatures.entrySet()) {

			if(entry.getValue().contains(f))
				return entry.getKey();
		}
		return null;
	}
	
	public MsFeature getAveragedMSMSFeature(
			Double mzBinWidth, MassErrorType errorType) {
		
		if(primaryFeature == null || primaryFeature.getSpectrum() == null)
			return null;		
		Polarity polarity = primaryFeature.getPolarity();		
		MassSpectrum spectrum = new MassSpectrum();
		
		//	MS1
		Collection<MsPoint>inputPoints = getFeatures().stream().
				filter(f -> Objects.nonNull(f.getSpectrum())).
				flatMap(f -> f.getSpectrum().getMsPoints().stream()).
				collect(Collectors.toList());
		Collection<MsPoint>averageMS1Spectrum = 
				MsUtils.averageMassSpectrum(inputPoints, mzBinWidth, errorType);
		spectrum.replaceDataPoints(averageMS1Spectrum);
		
		//	MS2
		List<TandemMassSpectrum> msmsList = getFeatures().stream().
			filter(f -> Objects.nonNull(f.getSpectrum())).
			filter(f -> Objects.nonNull(f.getSpectrum().getExperimentalTandemSpectrum())).
			map(f -> f.getSpectrum().getExperimentalTandemSpectrum()).
			collect(Collectors.toList());
		Range isolationWindow = msmsList.get(0).getIsolationWindow();
		for(int i=1; i<msmsList.size(); i++)
			isolationWindow.extendRange(msmsList.get(i).getIsolationWindow());
		
		Collection<MsPoint>inputParentPoints = msmsList.stream().
				map(s -> s.getParent()).collect(Collectors.toList());			
		MsPoint parent = MsUtils.getAveragePoint(inputParentPoints);
		Collection<MsPoint>inputMS2Points = msmsList.stream().
				flatMap(s -> s.getSpectrum().stream()).
				collect(Collectors.toList());
		Collection<MsPoint>averageMS2Spectrum = 
				MsUtils.averageMassSpectrum(inputMS2Points, mzBinWidth, errorType);
		TandemMassSpectrum msms = new TandemMassSpectrum(
				2, 
				parent,
				averageMS2Spectrum,
				polarity);
		msms.setIsolationWindow(isolationWindow);
		TandemMassSpectrum primaryTandemMs = 
				primaryFeature.getSpectrum().getExperimentalTandemSpectrum();
		msms.setCidLevel(primaryTandemMs.getCidLevel());
		msms.setSpectrumSource(SpectrumSource.EXPERIMENTAL);
		msms.setScanNumber(primaryTandemMs.getScanNumber());
		msms.setParentScanNumber(primaryTandemMs.getParentScanNumber());
		msmsList.stream().forEach(s -> msms.addAveragedScanNumbers(s.getScanNumber(), s.getParentScanNumber()));
		msmsList.stream().forEach(s -> s.getScanRtMap().entrySet().stream().forEach(e -> msms.getScanRtMap().put(e.getKey(), e.getValue())));
		msms.setEntropy(MsUtils.calculateCleanedSpectrumEntropyNatLog(msms.getSpectrum()));	
		
		Range parentMzRange = 
				MsUtils.createMassRange(parent.getMz(), mzBinWidth, errorType);
		Range iw = msms.getIsolationWindow();
		Collection<MsPoint>minorParentIons = spectrum.getMsPoints().stream().
				filter(p -> !parentMzRange.contains(p.getMz())).
				filter(p -> iw.contains(p.getMz())).
				sorted(MsUtils.mzSorter).collect(Collectors.toList());
		
		if(!minorParentIons.isEmpty()) {
			
			MsPoint msOneParent = averageMS1Spectrum.stream().
				filter(p -> parentMzRange.contains(p.getMz())).
				sorted(MsUtils.reverseIntensitySorter).
				findFirst().orElse(null);
			if(msOneParent == null)
				msOneParent = parent;
			
			msms.setMinorParentIons(minorParentIons, msOneParent);
		}		
		spectrum.addTandemMs(msms);			
		MsFeature averaged = new MsFeature(primaryFeature.getRetentionTime(), polarity);
		averaged.setRtRange(getRtRange());
		String name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
			MRC2ToolBoxConfiguration.defaultMzFormat.format(parent.getMz()) + "_" + 
			MRC2ToolBoxConfiguration.defaultRtFormat.format(averaged.getRetentionTime());
		averaged.setName(name);
		averaged.setSpectrum(spectrum);
		return averaged;
	}

	public double getMaxMS2Similarity(
			MsFeature cf, 
			double mzError, 
			MassErrorType mzErrorType) {
		
		if(cf.getSpectrum().getExperimentalTandemSpectrum() == null)
			return 0;
		
		List<Collection<MsPoint>> msmsList = clusterFeatures.values().stream().
			flatMap(c -> c.stream()).
			filter(f -> Objects.nonNull(f.getSpectrum().getExperimentalTandemSpectrum())).
			map(f -> f.getSpectrum().getExperimentalTandemSpectrum().getSpectrum()).
			collect(Collectors.toList());
		double maxScore = 0.0d;
		
		Collection<MsPoint>newMsms = cf.getSpectrum().getExperimentalTandemSpectrum().getSpectrum();
		for(Collection<MsPoint>msms : msmsList) {
			
			double score = MSMSScoreCalculator.calculateEntropyBasedMatchScore(
					msms, newMsms, mzError, mzErrorType, 0.0d);
			if(score > maxScore)
				maxScore = score;
		}		
		return maxScore;
	}
	
	public Collection<MsFeature>getSortedFeturesForDataPipeline(
			DataPipeline pipeline, SortProperty property){
		
		return clusterFeatures.get(pipeline).
				stream().sorted(new MsFeatureComparator(property)).
				collect(Collectors.toList());
	}
	
	public Collection<MsFeature>getFeturesForDataPipeline(DataPipeline pipeline){		
		return clusterFeatures.get(pipeline);
	}

	public boolean hasChargeMismatch() {
		return chargeMismatch;
	}

	public Range getRtRange() {
		
		double[]allRts = featureRTStatistics.values().
				stream().flatMapToDouble(s -> Arrays.stream(s.getValues())).
				sorted().toArray();
		return new Range(allRts[0], allRts[allRts.length - 1]);
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MsFeatureCluster.class.isAssignableFrom(obj.getClass()))
            return false;

        final MsFeatureCluster other = (MsFeatureCluster) obj;

        if ((this.clusterId == null) ? (other.getClusterId() != null) : !this.clusterId.equals(other.getClusterId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.clusterId != null ? this.clusterId.hashCode() : 0);
        return hash;
    }
    
    public MsFeature getFeatureById(String id) {
    	
    	return clusterFeatures.values().stream().
				flatMap(v -> v.stream()).filter(f -> f.getId().equals(id)).
				findFirst().orElse(null);
    }
    
	@Override
	public Element getXmlElement() {
		
		Element msFeatureClusterElement = 
				new Element(ObjectNames.MsFeatureCluster.name());
		msFeatureClusterElement.setAttribute(CommonFields.Id.name(), clusterId);
		msFeatureClusterElement.setAttribute(
				CommonFields.Locked.name(), Boolean.toString(locked));
		msFeatureClusterElement.setAttribute(
				MsFeatureClusterFields.chargeMismatch.name(), Boolean.toString(chargeMismatch));
		if(primaryFeature != null)
			msFeatureClusterElement.setAttribute(
					MsFeatureClusterFields.primaryFeature.name(), primaryFeature.getId());
		
		Element clusterFeaturesMapElement = 
				new Element(MsFeatureClusterFields.clusterFeaturesMap.name());
		for(Entry<DataPipeline, Collection<MsFeature>>mapEntry : clusterFeatures.entrySet()) {
			
			Element dpElement = new Element(ObjectNames.DataPipeline.name());
			dpElement.setAttribute(CommonFields.Name.name(), mapEntry.getKey().getName());
			List<String>featureIdList = mapEntry.getValue().stream().
					map(f -> f.getId()).collect(Collectors.toList());
			dpElement.setText(StringUtils.join(featureIdList, ","));	
			clusterFeaturesMapElement.addContent(dpElement);
		}
		msFeatureClusterElement.addContent(clusterFeaturesMapElement);
		
		if(!disabledFeatures.isEmpty()) {
			
			Element disabledElement = 
					new Element(MsFeatureClusterFields.disabledFeatures.name());
			
			List<String>featureIdList = disabledFeatures.stream().
					map(f -> f.getId()).collect(Collectors.toList());
			disabledElement.setText(StringUtils.join(featureIdList, ","));	
			msFeatureClusterElement.addContent(disabledElement);
		}
		if(!markedForMerge.isEmpty()) {
			
			Element markedForMergeElement = 
					new Element(MsFeatureClusterFields.markedForMerge.name());
			
			List<String>featureIdList = markedForMerge.stream().
					map(f -> f.getId()).collect(Collectors.toList());
			markedForMergeElement.setText(StringUtils.join(featureIdList, ","));	
			msFeatureClusterElement.addContent(markedForMergeElement);
		}		
		if(chemicalModificationsMap != null && !chemicalModificationsMap.isEmpty()) {
			
			Element chemicalModificationsMapElement = 
					new Element(MsFeatureClusterFields.chemicalModificationsMap.name());
			for(ModificationBlock mb : chemicalModificationsMap)
				chemicalModificationsMapElement.addContent(mb.getXmlElement());
			
			msFeatureClusterElement.addContent(chemicalModificationsMapElement);
		}
		if(annotationMap != null && !annotationMap.isEmpty()) {
			
			Element annotationMapElement = 
					new Element(MsFeatureClusterFields.annotationMap.name());
			for(Entry<MsFeature, Set<Adduct>>mapEntry : annotationMap.entrySet()) {
				
				Element annotationMapEntryElement = 
						new Element(MsFeatureClusterFields.annotationMapEntry.name());
				annotationMapEntryElement.setAttribute(CommonFields.Id.name(), mapEntry.getKey().getId());
				List<String>adductList = mapEntry.getValue().stream().
						map(Adduct::getId).collect(Collectors.toList());
				annotationMapEntryElement.setText(StringUtils.join(adductList, ","));									
				annotationMapElement.addContent(annotationMapEntryElement);
			}		
			msFeatureClusterElement.addContent(annotationMapElement);
		}		
		return msFeatureClusterElement;
	}
	
	public MsFeatureCluster(Element msFeatureClusterElement, DataAnalysisProject project) {
		this();
		clusterId = msFeatureClusterElement.getAttributeValue(CommonFields.Id.name());
		locked = Boolean.parseBoolean(
				msFeatureClusterElement.getAttributeValue(CommonFields.Locked.name()));
		chargeMismatch = Boolean.parseBoolean(
				msFeatureClusterElement.getAttributeValue(MsFeatureClusterFields.chargeMismatch.name()));
		
		//	Recreate feature map
		List<Element>dataPipelineElementList = 
				msFeatureClusterElement.getChild(MsFeatureClusterFields.clusterFeaturesMap.name()).
				getChildren(ObjectNames.DataPipeline.name());
		for(Element dpElement : dataPipelineElementList) {
			
			String pipelineName = dpElement.getAttributeValue(CommonFields.Name.name());
			DataPipeline dp = project.getDataPipelineByName(pipelineName);
			if(dp != null) {
				CompoundLibrary avgLib = project.getAveragedFeatureLibraryForDataPipeline(dp);
				if(avgLib != null) {
					
					String[]featureIds = dpElement.getText().split(",");
					for(String id : featureIds) {
						MsFeature feature = avgLib.getFeatureById(id);
						if(feature != null)
							addFeature(feature, dp);
					}
				}
			}
		}		
		//	Set primary feature
		String primaryId = msFeatureClusterElement.getAttributeValue(
				MsFeatureClusterFields.primaryFeature.name());
		if(primaryId != null)
			primaryFeature = getFeatureById(primaryId);
		
		//	Set disabled features
		Element disabledElement = msFeatureClusterElement.getChild(
				MsFeatureClusterFields.disabledFeatures.name());
		if(disabledElement != null) {
			String[]featureIds = disabledElement.getText().split(",");
			for(String id : featureIds) {
				MsFeature df = getFeatureById(id);
				if(df != null)
					disabledFeatures.add(df);
			}
		}
		//	Set "marked for merge" features markedForMerge
		Element markedForMergeElement = msFeatureClusterElement.getChild(
				MsFeatureClusterFields.markedForMerge.name());
		if(markedForMergeElement != null) {
			String[]featureIds = markedForMergeElement.getText().split(",");
			for(String id : featureIds) {
				MsFeature df = getFeatureById(id);
				if(df != null)
					markedForMerge.add(df);
			}
		}
		//	Recreate chemical modifications map
		Element chemicalModificationsMapElement = msFeatureClusterElement.getChild(
				MsFeatureClusterFields.chemicalModificationsMap.name());
		if(chemicalModificationsMapElement != null) {
			 List<Element>modificationBlockElementList = 
					 chemicalModificationsMapElement.getChildren(ObjectNames.ModificationBlock.name());
			 for(Element modificationBlockElement : modificationBlockElementList)
				 chemicalModificationsMap.add(new ModificationBlock(modificationBlockElement, project));
		}
		//	Recreate annotations map
		Element annotationMapElement = 
				msFeatureClusterElement.getChild(MsFeatureClusterFields.annotationMap.name());
		if(annotationMapElement != null) {
			
			List<Element>annotationMapEntryElementList = 
					annotationMapElement.getChildren(MsFeatureClusterFields.annotationMapEntry.name());
			for(Element annotationMapEntryElement : annotationMapEntryElementList) {
				
				String id = annotationMapEntryElement.getAttributeValue(CommonFields.Id.name());				
				MsFeature feature = getFeatureById(id);
				Set<Adduct>fAdducts = new TreeSet<Adduct>();
				String[]adductIds = annotationMapEntryElement.getText().split(",");
				for(String adductId : adductIds) {
					Adduct adduct = AdductManager.getAdductById(adductId);
					if(adduct != null)
						fAdducts.add(adduct);
				}
				if(feature != null && !fAdducts.isEmpty())
					annotationMap.put(feature, fAdducts);
			}
		}		
	}

	public Collection<MsFeature> getMarkedForMerge() {
		return markedForMerge;
	}
	
	public void markFeatureForMerging(MsFeature msf, boolean mark) {
		
		if(mark)
			markedForMerge.add(msf);
		else
			markedForMerge.remove(msf);
	}
	
	public boolean isFeatureMarkedForMerging(MsFeature msf) {
		return markedForMerge.contains(msf);
	}
	
	public DataPipeline getMergeDataPipeline() {
		
		if(markedForMerge == null || markedForMerge.isEmpty())
			return null;
		
		return getDataPipelineForFeature(markedForMerge.iterator().next());
	}
	
	public Collection<LibraryMsFeature>getMergedFeaturesForDataPipeline(DataPipeline pipeline){
		
		return clusterFeatures.get(pipeline).stream().
			filter(LibraryMsFeature.class::isInstance).
			map(LibraryMsFeature.class::cast).
			filter(f -> f.isMerged()).collect(Collectors.toList());
	}
}




























