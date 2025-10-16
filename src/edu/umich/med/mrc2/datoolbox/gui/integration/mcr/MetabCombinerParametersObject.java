/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.gui.integration.mcr;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.PeakAbundanceMeasure;
import edu.umich.med.mrc2.datoolbox.data.enums.RtFittingModelType;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class MetabCombinerParametersObject implements XmlStorable{
	
	private File workDirectory;
	private Set<MetabCombinerFileInputObject>metabCombinerFileInputObjectSet;
	private Range alignmentRTRange;
	private double maxMissingPercent;
	private PeakAbundanceMeasure peakAbundanceMeasure;
	private double binGap;
	private boolean mcDataSetRtOrderFlag;
	private boolean imputeMissingData;
	private double anchorMzTolerance;
	private double anchorAreaQuantileTolerance;
	private double anchorRtQuantileTolerance;
	private double primaryDataSetAnchorRtExclusionWindow;
	private double secondaryDataSetAnchorRtExclusionWindow;
	private double scoringMZweight;
	private double scoringRTweight;
	private double scoringAbundanceWeight;
	private int maxMissingBatchCount;
	private boolean usePPMforScoringMz;
	private RtFittingModelType rtFittingModelType;
	private boolean useAdductsToAdjustScore;
	private double minimalAlignmentScore;
	private int maxFeatureRankForPrimaryDataSet;
	private int maxFeatureRankForSecondaryDataSet;
	private double subgroupScoreCutoff;
	private double  maxRTerrorForAlignedFeatures;
	private boolean resolveAlignmentConflictsInOutput;
	private boolean rtOrderFlagInOutput;

	public MetabCombinerParametersObject() {
		super();
		// TODO Auto-generated constructor stub
	}

	public File getWorkDirectory() {
		return workDirectory;
	}

	public void setWorkDirectory(File workDirectory) {
		this.workDirectory = workDirectory;
	}

	public Set<MetabCombinerFileInputObject> getMetabCombinerFileInputObjectSet() {
		return metabCombinerFileInputObjectSet;
	}

	public void setMetabCombinerFileInputObjectSet(
			Collection<MetabCombinerFileInputObject> metabCombinerFileInputObjectCollection) {
		this.metabCombinerFileInputObjectSet = new TreeSet<>();
		metabCombinerFileInputObjectSet.addAll(metabCombinerFileInputObjectCollection);
	}

	public Range getAlignmentRTRange() {
		return alignmentRTRange;
	}

	public void setAlignmentRTRange(Range alignmentRTRange) {
		this.alignmentRTRange = alignmentRTRange;
	}

	public double getMaxMissingPercent() {
		return maxMissingPercent;
	}

	public void setMaxMissingPercent(double maxMissingPercent) {
		this.maxMissingPercent = maxMissingPercent;
	}

	public PeakAbundanceMeasure getPeakAbundanceMeasure() {
		return peakAbundanceMeasure;
	}

	public void setPeakAbundanceMeasure(PeakAbundanceMeasure peakAbundanceMeasure) {
		this.peakAbundanceMeasure = peakAbundanceMeasure;
	}

	public double getBinGap() {
		return binGap;
	}

	public void setBinGap(double binGap) {
		this.binGap = binGap;
	}

	public boolean isMcDataSetRtOrderFlag() {
		return mcDataSetRtOrderFlag;
	}

	public void setMcDataSetRtOrderFlag(boolean mcDataSetRtOrderFlag) {
		this.mcDataSetRtOrderFlag = mcDataSetRtOrderFlag;
	}

	public boolean isImputeMissingData() {
		return imputeMissingData;
	}

	public void setImputeMissingData(boolean imputeMissingData) {
		this.imputeMissingData = imputeMissingData;
	}

	public double getAnchorMzTolerance() {
		return anchorMzTolerance;
	}

	public void setAnchorMzTolerance(double anchorMzTolerance) {
		this.anchorMzTolerance = anchorMzTolerance;
	}

	public double getAnchorAreaQuantileTolerance() {
		return anchorAreaQuantileTolerance;
	}

	public void setAnchorAreaQuantileTolerance(double anchorAreaQuantileTolerance) {
		this.anchorAreaQuantileTolerance = anchorAreaQuantileTolerance;
	}

	public double getAnchorRtQuantileTolerance() {
		return anchorRtQuantileTolerance;
	}

	public void setAnchorRtQuantileTolerance(double anchorRtQuantileTolerance) {
		this.anchorRtQuantileTolerance = anchorRtQuantileTolerance;
	}

	public double getPrimaryDataSetAnchorRtExclusionWindow() {
		return primaryDataSetAnchorRtExclusionWindow;
	}

	public void setPrimaryDataSetAnchorRtExclusionWindow(double primaryDataSetAnchorRtExclusionWindow) {
		this.primaryDataSetAnchorRtExclusionWindow = primaryDataSetAnchorRtExclusionWindow;
	}

	public double getSecondaryDataSetAnchorRtExclusionWindow() {
		return secondaryDataSetAnchorRtExclusionWindow;
	}

	public void setSecondaryDataSetAnchorRtExclusionWindow(double secondaryDataSetAnchorRtExclusionWindow) {
		this.secondaryDataSetAnchorRtExclusionWindow = secondaryDataSetAnchorRtExclusionWindow;
	}

	public double getScoringMZweight() {
		return scoringMZweight;
	}

	public void setScoringMZweight(double scoringMZweight) {
		this.scoringMZweight = scoringMZweight;
	}

	public double getScoringRTweight() {
		return scoringRTweight;
	}

	public void setScoringRTweight(double scoringRTweight) {
		this.scoringRTweight = scoringRTweight;
	}

	public double getScoringAbundanceWeight() {
		return scoringAbundanceWeight;
	}

	public void setScoringAbundanceWeight(double scoringAbundanceWeight) {
		this.scoringAbundanceWeight = scoringAbundanceWeight;
	}

	public int getMaxMissingBatchCount() {
		return maxMissingBatchCount;
	}

	public void setMaxMissingBatchCount(int maxMissingBatchCount) {
		this.maxMissingBatchCount = maxMissingBatchCount;
	}

	public boolean isUsePPMforScoringMz() {
		return usePPMforScoringMz;
	}

	public void setUsePPMforScoringMz(boolean usePPMforScoringMz) {
		this.usePPMforScoringMz = usePPMforScoringMz;
	}

	public RtFittingModelType getRtFittingModelType() {
		return rtFittingModelType;
	}

	public void setRtFittingModelType(RtFittingModelType rtFittingModelType) {
		this.rtFittingModelType = rtFittingModelType;
	}

	public boolean isUseAdductsToAdjustScore() {
		return useAdductsToAdjustScore;
	}

	public void setUseAdductsToAdjustScore(boolean useAdductsToAdjustScore) {
		this.useAdductsToAdjustScore = useAdductsToAdjustScore;
	}

	public double getMinimalAlignmentScore() {
		return minimalAlignmentScore;
	}

	public void setMinimalAlignmentScore(double minimalAlignmentScore) {
		this.minimalAlignmentScore = minimalAlignmentScore;
	}

	public int getMaxFeatureRankForPrimaryDataSet() {
		return maxFeatureRankForPrimaryDataSet;
	}

	public void setMaxFeatureRankForPrimaryDataSet(int maxFeatureRankForPrimaryDataSet) {
		this.maxFeatureRankForPrimaryDataSet = maxFeatureRankForPrimaryDataSet;
	}

	public int getMaxFeatureRankForSecondaryDataSet() {
		return maxFeatureRankForSecondaryDataSet;
	}

	public void setMaxFeatureRankForSecondaryDataSet(int maxFeatureRankForSecondaryDataSet) {
		this.maxFeatureRankForSecondaryDataSet = maxFeatureRankForSecondaryDataSet;
	}

	public double getSubgroupScoreCutoff() {
		return subgroupScoreCutoff;
	}

	public void setSubgroupScoreCutoff(double subgroupScoreCutoff) {
		this.subgroupScoreCutoff = subgroupScoreCutoff;
	}

	public double getMaxRTerrorForAlignedFeatures() {
		return maxRTerrorForAlignedFeatures;
	}

	public void setMaxRTerrorForAlignedFeatures(double maxRTerrorForAlignedFeatures) {
		this.maxRTerrorForAlignedFeatures = maxRTerrorForAlignedFeatures;
	}

	public boolean isResolveAlignmentConflictsInOutput() {
		return resolveAlignmentConflictsInOutput;
	}

	public void setResolveAlignmentConflictsInOutput(boolean resolveAlignmentConflictsInOutput) {
		this.resolveAlignmentConflictsInOutput = resolveAlignmentConflictsInOutput;
	}

	public boolean isRtOrderFlagInOutput() {
		return rtOrderFlagInOutput;
	}

	public void setRtOrderFlagInOutput(boolean rtOrderFlagInOutput) {
		this.rtOrderFlagInOutput = rtOrderFlagInOutput;
	}
	
	public MetabCombinerParametersObject(Element metabCombinerParametersObjectElement) {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public Element getXmlElement() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
