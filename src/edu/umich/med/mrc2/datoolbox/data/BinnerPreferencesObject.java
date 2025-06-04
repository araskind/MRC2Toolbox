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

import java.util.Set;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.binner.control.BinClusteringCutoffType;
import edu.umich.med.mrc2.datoolbox.gui.binner.control.ClusterGroupingMethod;
import edu.umich.med.mrc2.datoolbox.gui.binner.control.CorrelationFunctionType;

public class BinnerPreferencesObject {
	
	//	Input data
	private DataPipeline dataPipeline;
	private Set<DataFile>inputFiles;
	
	//	Data cleaning
	private double outlierSDdeviation;
	private double missingRemovalThreshold;
	private boolean logTransform;
	private boolean zeroAsMissing;
	private boolean deisotope;
	private boolean deisoMassDiffDistr;
	private double deisotopingMassTolerance;
	private double deisotopingRTtolerance;
	private double deisotopingCorrCutoff;
	
	//	Feature grouping
	private CorrelationFunctionType correlationFunctionType;
	private double rtGap;
	private ClusterGroupingMethod clusterGroupingMethod;
	private double minSubclusterRTgap;
	private double maxSubclusterRTgap;
	private BinClusteringCutoffType binClusteringCutoffType;
	private boolean limitBinSizeForAnalysis;
	private int binSizeLimitForAnalysis;
	private boolean limitBinSizeForOutput;
	private int binSizeLimitForOutput;	
	private int binClusteringCutoff;
	
	//	Annotation parameters
	private Polarity polarity;
	private BinnerAdductList annotationList;	
	private double annotationMassTolerance;
	private double annotationRTTolerance;
	private boolean useNeutralMassForChargeCarrierAssignment;
	private boolean allowVariableChargeWithoutIsotopeInformation;
		
	public BinnerPreferencesObject(DataPipeline dataPipeline) {
		super();
		this.dataPipeline = dataPipeline;
		inputFiles = new TreeSet<DataFile>();
	}

	public Set<DataFile> getInputFiles() {
		return inputFiles;
	}

	public void setInputFiles(Set<DataFile> inputFiles) {
		this.inputFiles = inputFiles;
	}

	public double getOutlierSDdeviation() {
		return outlierSDdeviation;
	}

	public void setOutlierSDdeviation(double outlierSDdeviation) {
		this.outlierSDdeviation = outlierSDdeviation;
	}

	public double getMissingRemovalThreshold() {
		return missingRemovalThreshold;
	}

	public void setMissingRemovalThreshold(double missingRemovalThreshold) {
		this.missingRemovalThreshold = missingRemovalThreshold;
	}

	public boolean isLogTransform() {
		return logTransform;
	}

	public void setLogTransform(boolean logTransform) {
		this.logTransform = logTransform;
	}

	public boolean isZeroAsMissing() {
		return zeroAsMissing;
	}

	public void setZeroAsMissing(boolean zeroAsMissing) {
		this.zeroAsMissing = zeroAsMissing;
	}

	public boolean isDeisotope() {
		return deisotope;
	}

	public void setDeisotope(boolean deisotope) {
		this.deisotope = deisotope;
	}

	public boolean isDeisoMassDiffDistr() {
		return deisoMassDiffDistr;
	}

	public void setDeisoMassDiffDistr(boolean deisoMassDiffDistr) {
		this.deisoMassDiffDistr = deisoMassDiffDistr;
	}

	public double getDeisotopingMassTolerance() {
		return deisotopingMassTolerance;
	}

	public void setDeisotopingMassTolerance(double deisotopingMassTolerance) {
		this.deisotopingMassTolerance = deisotopingMassTolerance;
	}

	public double getDeisotopingRTtolerance() {
		return deisotopingRTtolerance;
	}

	public void setDeisotopingRTtolerance(double deisotopingRTtolerance) {
		this.deisotopingRTtolerance = deisotopingRTtolerance;
	}

	public double getDeisotopingCorrCutoff() {
		return deisotopingCorrCutoff;
	}

	public void setDeisotopingCorrCutoff(double deisotopingCorrCutoff) {
		this.deisotopingCorrCutoff = deisotopingCorrCutoff;
	}

	public CorrelationFunctionType getCorrelationFunctionType() {
		return correlationFunctionType;
	}

	public void setCorrelationFunctionType(CorrelationFunctionType correlationFunctionType) {
		this.correlationFunctionType = correlationFunctionType;
	}

	public double getRtGap() {
		return rtGap;
	}

	public void setRtGap(double rtGap) {
		this.rtGap = rtGap;
	}

	public ClusterGroupingMethod getClusterGroupingMethod() {
		return clusterGroupingMethod;
	}

	public void setClusterGroupingMethod(ClusterGroupingMethod clusterGroupingMethod) {
		this.clusterGroupingMethod = clusterGroupingMethod;
	}

	public double getMinSubclusterRTgap() {
		return minSubclusterRTgap;
	}

	public void setMinSubclusterRTgap(double minSubclusterRTgap) {
		this.minSubclusterRTgap = minSubclusterRTgap;
	}

	public double getMaxSubclusterRTgap() {
		return maxSubclusterRTgap;
	}

	public void setMaxSubclusterRTgap(double maxSubclusterRTgap) {
		this.maxSubclusterRTgap = maxSubclusterRTgap;
	}

	public BinClusteringCutoffType getBinClusteringCutoffType() {
		return binClusteringCutoffType;
	}

	public void setBinClusteringCutoffType(BinClusteringCutoffType binClusteringCutoffType) {
		this.binClusteringCutoffType = binClusteringCutoffType;
	}

	public boolean isLimitBinSizeForAnalysis() {
		return limitBinSizeForAnalysis;
	}

	public void setLimitBinSizeForAnalysis(boolean limitBinSizeForAnalysis) {
		this.limitBinSizeForAnalysis = limitBinSizeForAnalysis;
	}

	public int getBinSizeLimitForAnalysis() {
		return binSizeLimitForAnalysis;
	}

	public void setBinSizeLimitForAnalysis(int binSizeLimitForAnalysis) {
		this.binSizeLimitForAnalysis = binSizeLimitForAnalysis;
	}

	public boolean isLimitBinSizeForOutput() {
		return limitBinSizeForOutput;
	}

	public void setLimitBinSizeForOutput(boolean limitBinSizeForOutput) {
		this.limitBinSizeForOutput = limitBinSizeForOutput;
	}

	public int getBinSizeLimitForOutput() {
		return binSizeLimitForOutput;
	}

	public void setBinSizeLimitForOutput(int binSizeLimitForOutput) {
		this.binSizeLimitForOutput = binSizeLimitForOutput;
	}

	public int getBinClusteringCutoff() {
		return binClusteringCutoff;
	}

	public void setBinClusteringCutoff(int binClusteringCutoff) {
		this.binClusteringCutoff = binClusteringCutoff;
	}

	public BinnerAdductList getAnnotationList() {
		return annotationList;
	}

	public void setAnnotationList(BinnerAdductList annotationList) {
		this.annotationList = annotationList;
	}

	public double getAnnotationMassTolerance() {
		return annotationMassTolerance;
	}

	public void setAnnotationMassTolerance(double annotationMassTolerance) {
		this.annotationMassTolerance = annotationMassTolerance;
	}

	public double getAnnotationRTTolerance() {
		return annotationRTTolerance;
	}

	public void setAnnotationRTTolerance(double annotationRTTolerance) {
		this.annotationRTTolerance = annotationRTTolerance;
	}

	public boolean isUseNeutralMassForChargeCarrierAssignment() {
		return useNeutralMassForChargeCarrierAssignment;
	}

	public void setUseNeutralMassForChargeCarrierAssignment(boolean useNeutralMassForChargeCarrierAssignment) {
		this.useNeutralMassForChargeCarrierAssignment = useNeutralMassForChargeCarrierAssignment;
	}

	public boolean isAllowVariableChargeWithoutIsotopeInformation() {
		return allowVariableChargeWithoutIsotopeInformation;
	}

	public void setAllowVariableChargeWithoutIsotopeInformation(boolean allowVariableChargeWithoutIsotopeInformation) {
		this.allowVariableChargeWithoutIsotopeInformation = allowVariableChargeWithoutIsotopeInformation;
	}

	public Polarity getPolarity() {
		return polarity;
	}

	public void setPolarity(Polarity polarity) {
		this.polarity = polarity;
	}

	public DataPipeline getDataPipeline() {
		return dataPipeline;
	}
}
