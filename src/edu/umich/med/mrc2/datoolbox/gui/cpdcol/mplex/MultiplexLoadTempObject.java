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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.mplex;

public class MultiplexLoadTempObject {

	private int mixNum;
	private String inchiKey;
	private String name;
	private String solventId;
	private double conc;
	private Double xlogp;
	private double volume;
	
	public MultiplexLoadTempObject(
			int mixNum, 
			String name, 
			String solventId, 
			double conc, 
			Double xlogp,
			double volume) {
		super();
		this.mixNum = mixNum;
		this.name = name;
		this.solventId = solventId;
		this.conc = conc;
		this.xlogp = xlogp;
		this.volume = volume;
	}

	public int getMixNum() {
		return mixNum;
	}

	public String getInchiKey() {
		return inchiKey;
	}

	public String getSolventId() {
		return solventId;
	}

	public double getConc() {
		return conc;
	}

	public Double getXlogp() {
		return xlogp;
	}

	public double getVolume() {
		return volume;
	}

	public String getName() {
		return name;
	}
	
	
}
