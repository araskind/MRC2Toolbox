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

public class LipidMapsOntologyObject implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9075763023697719155L;
	private String lmid;
	private int carbons;
	private int doublebonds;
	private int triplebonds;
	private int rings;
	private int nh2;
	private int oh;
	private int ooh;
	private int ketones;
	private int epoxides;
	private int cooh;
	private int methyls;
	private int sh;
	private int br;
	private int cl;
	private int f;
	private int methylenes;
	private int cho;
	private int ome;
	private int oac;
	private int et;
	private int pr;
	private int coome;
	private int no2;
	private int ester;
	private int ether;
	private int amides;
	private String fingerprint;
	
	public LipidMapsOntologyObject(String lmid, int carbons, int doublebonds, int triplebonds, int rings, int nh2,
			int oh, int ooh, int ketones, int epoxides, int cooh, int methyls, int sh, int br, int cl, int f,
			int methylenes, int cho, int ome, int oac, int et, int pr, int coome, int no2, int ester, int ether,
			int amides) {
		super();
		this.lmid = lmid;
		this.carbons = carbons;
		this.doublebonds = doublebonds;
		this.triplebonds = triplebonds;
		this.rings = rings;
		this.nh2 = nh2;
		this.oh = oh;
		this.ooh = ooh;
		this.ketones = ketones;
		this.epoxides = epoxides;
		this.cooh = cooh;
		this.methyls = methyls;
		this.sh = sh;
		this.br = br;
		this.cl = cl;
		this.f = f;
		this.methylenes = methylenes;
		this.cho = cho;
		this.ome = ome;
		this.oac = oac;
		this.et = et;
		this.pr = pr;
		this.coome = coome;
		this.no2 = no2;
		this.ester = ester;
		this.ether = ether;
		this.amides = amides;
	}
	
	public boolean equivalent(LipidMapsOntologyObject otherOntology) {
		
		if(this.fingerprint != null && otherOntology.getFingerprint() != null)
			return this.fingerprint.equals(otherOntology.getFingerprint());
				
		if(this.carbons != otherOntology.getCarbons())
			return false;

		if(this.doublebonds != otherOntology.getDoublebonds())
			return false;

		if(this.triplebonds != otherOntology.getTriplebonds())
			return false;

		if(this.rings != otherOntology.getRings())
			return false;

		if(this.nh2 != otherOntology.getNh2())
			return false;

		if(this.oh != otherOntology.getOh())
			return false;

		if(this.ooh != otherOntology.getOoh())
			return false;

		if(this.ketones != otherOntology.getKetones())
			return false;

		if(this.epoxides != otherOntology.getEpoxides())
			return false;

		if(this.cooh != otherOntology.getCooh())
			return false;

		if(this.methyls != otherOntology.getMethyls())
			return false;

		if(this.sh != otherOntology.getSh())
			return false;

		if(this.br != otherOntology.getBr())
			return false;

		if(this.cl != otherOntology.getCl())
			return false;

		if(this.f != otherOntology.getF())
			return false;

		if(this.methylenes != otherOntology.getMethylenes())
			return false;

		if(this.cho != otherOntology.getCho())
			return false;

		if(this.ome != otherOntology.getOme())
			return false;

		if(this.oac != otherOntology.getOac())
			return false;

		if(this.et != otherOntology.getEt())
			return false;

		if(this.pr != otherOntology.getPr())
			return false;

		if(this.coome != otherOntology.getCoome())
			return false;

		if(this.no2 != otherOntology.getNo2())
			return false;

		if(this.ester != otherOntology.getEster())
			return false;

		if(this.ether != otherOntology.getEther())
			return false;
		
		return true;
	}
	
	public boolean equivalentExceptCarbonsAndDoubleBonds(LipidMapsOntologyObject otherOntology) {

		if(this.triplebonds != otherOntology.getTriplebonds())
			return false;

		if(this.rings != otherOntology.getRings())
			return false;

		if(this.nh2 != otherOntology.getNh2())
			return false;

		if(this.oh != otherOntology.getOh())
			return false;

		if(this.ooh != otherOntology.getOoh())
			return false;

		if(this.ketones != otherOntology.getKetones())
			return false;

		if(this.epoxides != otherOntology.getEpoxides())
			return false;

		if(this.cooh != otherOntology.getCooh())
			return false;

		if(this.methyls != otherOntology.getMethyls())
			return false;

		if(this.sh != otherOntology.getSh())
			return false;

		if(this.br != otherOntology.getBr())
			return false;

		if(this.cl != otherOntology.getCl())
			return false;

		if(this.f != otherOntology.getF())
			return false;

		if(this.methylenes != otherOntology.getMethylenes())
			return false;

		if(this.cho != otherOntology.getCho())
			return false;

		if(this.ome != otherOntology.getOme())
			return false;

		if(this.oac != otherOntology.getOac())
			return false;

		if(this.et != otherOntology.getEt())
			return false;

		if(this.pr != otherOntology.getPr())
			return false;

		if(this.coome != otherOntology.getCoome())
			return false;

		if(this.no2 != otherOntology.getNo2())
			return false;

		if(this.ester != otherOntology.getEster())
			return false;

		if(this.ether != otherOntology.getEther())
			return false;
		
		return true;
	}

	public String getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(String fingerprint) {
		this.fingerprint = fingerprint;
	}

	public String getLmid() {
		return lmid;
	}

	public int getCarbons() {
		return carbons;
	}

	public int getDoublebonds() {
		return doublebonds;
	}

	public int getTriplebonds() {
		return triplebonds;
	}

	public int getRings() {
		return rings;
	}

	public int getNh2() {
		return nh2;
	}

	public int getOh() {
		return oh;
	}

	public int getOoh() {
		return ooh;
	}

	public int getKetones() {
		return ketones;
	}

	public int getEpoxides() {
		return epoxides;
	}

	public int getCooh() {
		return cooh;
	}

	public int getMethyls() {
		return methyls;
	}

	public int getSh() {
		return sh;
	}

	public int getBr() {
		return br;
	}

	public int getCl() {
		return cl;
	}

	public int getF() {
		return f;
	}

	public int getMethylenes() {
		return methylenes;
	}

	public int getCho() {
		return cho;
	}

	public int getOme() {
		return ome;
	}

	public int getOac() {
		return oac;
	}

	public int getEt() {
		return et;
	}

	public int getPr() {
		return pr;
	}

	public int getCoome() {
		return coome;
	}

	public int getNo2() {
		return no2;
	}

	public int getEster() {
		return ester;
	}

	public int getEther() {
		return ether;
	}

	public int getAmides() {
		return amides;
	}
}
