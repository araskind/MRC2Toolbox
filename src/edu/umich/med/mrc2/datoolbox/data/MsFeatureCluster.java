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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
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
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.ClusterUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MsFeatureCluster implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -5100249593042159601L;

	
	private String clusterId;
	private Map<DataPipeline, Collection<MsFeature>>clusterFeatures;
	private Collection<MsFeature>disabledFeatures;
	private Range rtRange;
	private Matrix clusterCorrMatrix;
	private Collection<ModificationBlock> chemicalModificationsMap;
	private Map<MsFeature, Set<Adduct>> annotationMap;
	private boolean locked;
	private MsFeature primaryFeature;

	public MsFeatureCluster() {

		this.clusterId = 
				DataPrefix.MS_FEATURE_CLUSTER.getName() + UUID.randomUUID().toString();
		clusterFeatures = new TreeMap<DataPipeline, Collection<MsFeature>>();
		chemicalModificationsMap = new HashSet<ModificationBlock>();
		rtRange = null;
		clusterCorrMatrix = null;
		primaryFeature = null;
		locked = false;
		disabledFeatures = new HashSet<MsFeature>();
	}

	public void addFeature(MsFeature cf, DataPipeline pipeline) {
		
		if(!clusterFeatures.containsKey(pipeline))
			clusterFeatures.put(pipeline, 
					new TreeSet<MsFeature>(new MsFeatureComparator(SortProperty.MZ)));
		
		clusterFeatures.get(pipeline).add(cf);
		if (rtRange == null)
			rtRange = new Range(cf.getRetentionTime());
		else
			rtRange.extendRange(cf.getRetentionTime());		
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
				flatMap(c -> c.stream()).
				filter(f -> f.isIdentified()).
				findFirst().isPresent();
	}

	public boolean isSingleDataPipeline() {
		return clusterFeatures.keySet().size() == 1;
	}

	public Matrix createClusterCorrelationMatrix(boolean activeOnly) {

		//	TODO this may change to accomodate comparison of different methods
		if(clusterFeatures.isEmpty() || !isSingleDataPipeline())
			return null;

		DataPipeline assay = clusterFeatures.keySet().iterator().next();
		Matrix dataMatrix = MRC2ToolBoxCore.getCurrentProject().
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

		//	TODO	Select active samples - this is a temporary fix untill design subsets are implemented
		ArrayList<Long>rowList = new ArrayList<Long>();
		for(DataFile file : MRC2ToolBoxCore.getCurrentProject().
				getDataFilesForAcquisitionMethod(assay.getAcquisitionMethod())) {

			if(file.isEnabled())
				rowList.add(dataMatrix.getRowForLabel(file));
		}
		long[] rowIndex = ArrayUtils.toPrimitive(rowList.toArray(new Long[rowList.size()]));
		Matrix raw = dataMatrix.select(Ret.LINK, rowIndex, columnIndex).replace(Ret.NEW, 0.0d, Double.NaN);
		corrMatrix = raw.corrcoef(Ret.LINK, true, false).replace(Ret.NEW, Double.NaN, 0.0d);
		corrMatrix.setMetaDataDimensionMatrix(0, Matrix.Factory.linkToArray(sorted));
		corrMatrix.setMetaDataDimensionMatrix(1, Matrix.Factory.linkToArray(sorted).transpose(Ret.NEW));
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
		return clusterCorrMatrix;
	}

	public String getClusterId() {

		return clusterId;
	}

	public double getCorrelation(MsFeature compoundFeature, MsFeature compoundFeature2) {

		long[] coordinate = new long[] { clusterCorrMatrix.getRowForLabel(compoundFeature),
				clusterCorrMatrix.getRowForLabel(compoundFeature2) };
		return clusterCorrMatrix.getAsDouble(coordinate);
	}

	public Matrix getCorrMatrix() {
		return clusterCorrMatrix;
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
		return Matrix.Factory.fill(1, clusterCorrMatrix.getSize()).
				minus(clusterCorrMatrix).replace(Ret.NEW, Double.NaN, 0.0d);
	}

	public HashSet<Adduct> getModificationsForFeaturePair(MsFeature featureOne, MsFeature featureTwo) {

		HashSet<Adduct> featureMods = new HashSet<Adduct>();

		for (ModificationBlock b : chemicalModificationsMap) {

			if (b.getFeatureOne().equals(featureOne) && b.getFeatureTwo().equals(featureTwo))
				featureMods.add(b.getModification());
		}
		return featureMods;
	}

	public String getNameForXicMethod() {

		String clusterName = "Cluster" + "_" + 
				MRC2ToolBoxConfiguration.getMzFormat().format(getTopMass()) + "@"
				+ MRC2ToolBoxConfiguration.getRtFormat().format(rtRange.getAverage());

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

	public Range getRtRange() {
		return rtRange;
	}

	public double getTopArea() {
		return clusterFeatures.values().stream().
				flatMap(c -> c.stream()).
				sorted(new MsFeatureComparator(SortProperty.Area, SortDirection.DESC)).
				findFirst().get().getAveragePeakArea();
	}
	
	public MsFeature getMostIntensiveFeature() {
		
		return clusterFeatures.values().stream().
				flatMap(c -> c.stream()).
				sorted(new MsFeatureComparator(SortProperty.Area, SortDirection.DESC)).
				findFirst().orElse(null);
	}

	public double getTopMass() {
		
		return clusterFeatures.values().stream().
				flatMap(c -> c.stream()).
				sorted(new MsFeatureComparator(SortProperty.Area, SortDirection.DESC)).
				findFirst().get().getMonoisotopicMz();
	}

	public boolean isLocked() {
		return locked;
	}

	public boolean matches(
			MsFeature cf, 
			DataPipeline pipeline,
			double massAccuracy, 
			double rtWindow) {
		
		if(clusterFeatures.get(pipeline) == null)
			return false;

		return clusterFeatures.get(pipeline).stream().
				filter(f -> MsUtils.matchesFeature(f, cf, massAccuracy, rtWindow, true)).
				findFirst().isPresent();
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
	
	public boolean matches(
			MsFeature cf, 
			double massAccuracy, 
			MassErrorType massErrorType,
			double rtWindow) {

		return clusterFeatures.values().stream().
				flatMap(c -> c.stream()).
				filter(f -> MsUtils.matchesFeature(f, cf, massAccuracy, massErrorType, rtWindow, true)).
				findFirst().isPresent();
	}
	
	public boolean matchesOnMSMSParentIon(
			MsFeature cf, 
			double massAccuracy, 
			MassErrorType massErrorType,
			double rtWindow) {

		return clusterFeatures.values().stream().
				flatMap(c -> c.stream()).
				filter(f -> MsUtils.matchesOnMSMSParentIon(f, cf, massAccuracy, massErrorType, rtWindow, true)).
				findFirst().isPresent();
	}

	private void recalculateClusterParameters() {

		if (!clusterFeatures.isEmpty()) {

			MsFeature topFeature = ClusterUtils.getMostIntensiveFeature(getFeatures());
			rtRange = new Range(topFeature.getRetentionTime());
			clusterFeatures.values().stream().flatMap(c -> c.stream()).
				forEach(f -> rtRange.extendRange(f.getRetentionTime()));
		}
	}

	public void removeFeature(MsFeature cf) {

		clusterFeatures.values().stream().forEach(c -> c.remove(cf));
		clusterCorrMatrix = createClusterCorrelationMatrix(false);
		recalculateClusterParameters();
	}

	public void removeFeatures(Collection<MsFeature> cfList) {

		clusterFeatures.values().stream().forEach(c -> c.removeAll(cfList));
		clusterCorrMatrix = createClusterCorrelationMatrix(false);
		recalculateClusterParameters();
	}

	public void setAnnotationMap(Map<MsFeature, Set<Adduct>> annotationMap) {
		this.annotationMap = annotationMap;
	}

	public void setClusterCorrMatrix(Matrix clusterCorrMatrix) {
		this.clusterCorrMatrix = clusterCorrMatrix;
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
		if(getPrimaryIdentity() != null)
			clusterName = getPrimaryIdentity().getName() + " ";

		clusterName += 
				"RT " + MRC2ToolBoxConfiguration.getRtFormat().format(rtRange.getAverage()) + 
				" | group of " + getFeatures().size() + 
				" | top M/Z " + MRC2ToolBoxConfiguration.getMzFormat().format(getTopMass());

		return clusterName;
	}

	//	TODO may need more refined approach
	public MsFeatureIdentity getPrimaryIdentity() {

		if(primaryFeature == null)
			primaryFeature = ClusterUtils.getMostIntensiveFeature(getFeatures());;
		
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
				filter(f -> f.getSpectrum() != null).
				flatMap(f -> f.getSpectrum().getMsPoints().stream()).
				collect(Collectors.toList());
		Collection<MsPoint>averageMS1Spectrum = 
				MsUtils.averageMassSpectrum(inputPoints, mzBinWidth, errorType);
		spectrum.replaceDataPoints(averageMS1Spectrum);
		
		//	MS2
		List<TandemMassSpectrum> msmsList = getFeatures().stream().
			filter(f -> f.getSpectrum() != null).
			filter(f -> f.getSpectrum().getExperimentalTandemSpectrum() != null).
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
		msms.setEntropy(MsUtils.calculateSpectrumEntropyNatLog(msms.getSpectrum()));
		
		Range parentMzRange = 
				MsUtils.createMassRange(parent.getMz(), mzBinWidth, errorType);
		Range iw = msms.getIsolationWindow();
		Collection<MsPoint>minorParentIons = spectrum.getMsPoints().stream().
				filter(p -> !parentMzRange.contains(p.getMz())).
				filter(p -> iw.contains(p.getMz())).
				sorted(MsUtils.mzSorter).collect(Collectors.toList());
		if(!minorParentIons.isEmpty())
			msms.setMinorParentIons(minorParentIons);
		
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
}





















