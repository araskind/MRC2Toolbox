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

package edu.umich.med.mrc2.datoolbox.data;

import java.util.Collection;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

/**
 * @author Sasha
 *
 */
public class MinimalNISTTandemMassSpectrum {

	private int nistId;
	private String mrc2id;
	private MsPoint parent;
	private Collection<MsPoint>spectrum;
	private String spectrumHash;
	private double entropy;

	public MinimalNISTTandemMassSpectrum(
			int nistId,
			MsPoint parent,
			Collection<MsPoint> points) {
		super();
		this.nistId = nistId;
		this.parent = parent;
		this.spectrum = new TreeSet<MsPoint>(MsUtils.mzSorter);
		spectrum.addAll(points);
		spectrumHash = MsUtils.calculateSpectrumHash(spectrum);
		entropy = MsUtils.calculateCleanedSpectrumEntropyNatLog(spectrum);
	}
	
	public MinimalNISTTandemMassSpectrum(
			String mrc2id,
			MsPoint parent,
			Collection<MsPoint> points) {
		super();
		this.mrc2id = mrc2id;
		this.parent = parent;
		this.spectrum = new TreeSet<MsPoint>(MsUtils.mzSorter);
		spectrum.addAll(points);
		spectrumHash = MsUtils.calculateSpectrumHash(spectrum);
		entropy = MsUtils.calculateCleanedSpectrumEntropyNatLog(spectrum);
	}

	public MsPoint getParent() {
		return parent;
	}
	
	public MsPoint getParentFromSpectrum(double mzerrorPpm) {
		
		if(parent == null)
			return null;
		
		Range parentMzRange = MsUtils.createPpmMassRange(parent.getMz(), mzerrorPpm);
		return spectrum.stream().
				filter(p -> parentMzRange.contains(p.getMz())).
				sorted(MsUtils.reverseIntensitySorter).findFirst().orElse(null);
	}
	
	public double getMinimalIntensity() {	
		
		if(spectrum == null || spectrum.isEmpty())
			return 0.0d;
		
		return spectrum.stream().
				mapToDouble(p -> p.getIntensity()).min().getAsDouble();		
	}
	
	public Collection<MsPoint> getSpectrum() {
		return spectrum;
	}

	public int getId() {
		return nistId;
	}

    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MinimalNISTTandemMassSpectrum.class.isAssignableFrom(obj.getClass()))
            return false;

        if (this.nistId != ((MinimalNISTTandemMassSpectrum) obj).getId())
        	return false;

        return true;
    }

    @Override
    public int hashCode() {
        return spectrumHash.hashCode();
    }

	public double getEntropy() {
		return entropy;
	}

	public String getSpectrumHash() {
		return spectrumHash;
	}

	public String getMrc2id() {
		return mrc2id;
	}
}

















