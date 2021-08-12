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
import java.util.Collection;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class AverageMassSpectrum implements Serializable, Comparable<AverageMassSpectrum>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5389615971414831904L;
	private MassSpectrum masSpectrum;
	private DataFile dataFile;
	private int msLlevel;
	private Range rtRange;
	private Collection<Integer>scanNumbers;
	
	public AverageMassSpectrum(DataFile dataFile, int msLlevel, Range rtRange) {
		super();
		this.dataFile = dataFile;
		this.msLlevel = msLlevel;
		this.rtRange = rtRange;
		masSpectrum = new MassSpectrum();
	}

	public AverageMassSpectrum(DataFile dataFile, int msLlevel, Collection<Integer> scanNumbers) {
		super();
		this.dataFile = dataFile;
		this.msLlevel = msLlevel;
		this.scanNumbers = scanNumbers;
		masSpectrum = new MassSpectrum();
	}

	public MassSpectrum getMasSpectrum() {
		return masSpectrum;
	}

	public DataFile getDataFile() {
		return dataFile;
	}

	public int getMsLlevel() {
		return msLlevel;
	}

	public Range getRtRange() {
		return rtRange;
	}

	public Collection<Integer> getScanNumbers() {
		return scanNumbers;
	}
	
	@Override
	public String toString() {
		return "Averge MS for " + 
			dataFile.getName() + " | " + 
			rtRange.getFormattedString(MRC2ToolBoxConfiguration.getRtFormat());
	}

	@Override
	public int compareTo(AverageMassSpectrum o) {
		return this.toString().compareTo(o.toString());
	}
}
