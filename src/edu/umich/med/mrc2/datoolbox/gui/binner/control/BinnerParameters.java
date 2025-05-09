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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

public enum BinnerParameters {

	DCOutlierStDevCutoff("10.0"),
	DCMissingnessCutoff("70.0"),
	DCZeroAsMissing(Boolean.toString(true)),
	DCNormalize(Boolean.toString(true)),
	DCDeisotope(Boolean.toString(false)),
	DCDeisotopeMassDiffDistribution(Boolean.toString(true)),
	DCDeisotopeMassTolerance("0.002"),
	DCDeisotopeRTTolearance("0.1"),
	DCDeisotopeCorrCutoff("0.6"),
	FGCorrFunction(CorrelationFunctionType.PEARSON.name()),
	FGRTGap("0.03"),
	FGGroupingMethod(ClusterGroupingMethod.CLUSTER_ON_RT.name()),
	FGMinRtGap("0.025"),
	FGMaxRtGap("0.04"),
	FGBinningCutoffType(BinClusteringCutoffType.BELOW_SCORE.name()),
	FGBinningCutoffValue("1"),
	FGBOverrideBinSizeLimitForAnalysis(Boolean.toString(false)),
	FGBinSizeLimitForAnalysis("4000"),
	FGBOverrideBinSizeLimitForOutput(Boolean.toString(false)),
	FGBinSizeLimitForOutput("6000")
	;
	
	private final String defaultValue;

	BinnerParameters(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
}
