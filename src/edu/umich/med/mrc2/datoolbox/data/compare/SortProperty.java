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

package edu.umich.med.mrc2.datoolbox.data.compare;

public enum SortProperty {

	MZ("M/Z"),
	RT("Retention time"),
	Intensity("Intensity"),
	ID("ID"),
	Height("Peak height"),
	Area("Peak area"),
	Name("Name"),
	Description("Description"),
	Quality("Quality"),
	featureCount("Number of features"),
	pimaryId("Primary ID"),
	injectionTime("Injection time"),
	Version("Version"),
	scanNumber("Scan number"),
	msmsScore("MSMS match score"),
	msmsEntropyScore("MSMS entropy match score"),
	NameAndId("Name and ID"),
	dotProduct("Dot-product"),
	reverseDotProduct("Reverse dot-product"),
	probability("Probability"),
	sample("Sample ID"),
	sampleName("Sample name"),
	dataFile("Data file name"),
	resultFile("Result file name"),
	spectrumEntropy("Spectrum entropy"),
	msmsIntensity("MSMS total intensity"),
	Rank("Rank"),
	BasePeakMZ("Base peak M/Z"),
	ParentIonMZ("Parent ion M/Z"),
	rangeMidpoint("Range mid-point"),
	frequency("Frequency"),
	RSD("Relative standard deviation"),
	;
	
	private final String uiName;

	SortProperty(String uiName) {
		this.uiName = uiName;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}	
	
	public static SortProperty getOptionByName(String name) {

		for(SortProperty field : SortProperty.values()) {

			if(field.name().equals(name))
				return field;
		}
		return null;
	}
}
