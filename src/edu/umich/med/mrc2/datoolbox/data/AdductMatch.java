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

public class AdductMatch implements Serializable, Comparable<AdductMatch> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2942119307333672379L;
	Adduct libraryMatch;
	Adduct unknownMatch;
	double dotProductScore;
	double entropyScore;

	public AdductMatch(Adduct libraryMatch, Adduct unknownMatch, double dotProductScore) {
		super();
		this.libraryMatch = libraryMatch;
		this.unknownMatch = unknownMatch;
		this.dotProductScore = dotProductScore;
	}

	public Adduct getLibraryMatch() {
		return libraryMatch;
	}

	public Adduct getUnknownMatch() {
		return unknownMatch;
	}

	public double getDotProductScore() {
		return dotProductScore;
	}

	@Override
	public int compareTo(AdductMatch o) {
		return Double.compare(o.getDotProductScore(), dotProductScore);
	}

	public double getEntropyScore() {
		return entropyScore;
	}

	public void setEntropyScore(double entropyScore) {
		this.entropyScore = entropyScore;
	}
}
