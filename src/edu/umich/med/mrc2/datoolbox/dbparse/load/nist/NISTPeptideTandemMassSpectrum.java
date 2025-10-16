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

package edu.umich.med.mrc2.datoolbox.dbparse.load.nist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;

public class NISTPeptideTandemMassSpectrum extends TandemMassSpectrum {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8712174061563450674L;
	
	private double exactMass;
	private int charge;
	private Map<NISTPeptideMSPField,String>properties;
	private Collection<MsPoint>precursors;
	private String comments;
	private String peptideModifications;
	private String peptideSequence;

	public NISTPeptideTandemMassSpectrum(Polarity polarity) {

		super(polarity);
		properties = new TreeMap<NISTPeptideMSPField,String>();
//		for(NISTPeptiteMSPField field : NISTPeptiteMSPField.values())
//			properties.put(field, "");

		precursors = new ArrayList<MsPoint>();
	}
	
	public void addPrecursor(MsPoint precursor) {
		precursors.add(precursor);
	}
	
	public double getExactMass() {
		return exactMass;
	}

	public Map<NISTPeptideMSPField, String> getProperties() {
		return properties;
	}
	
	public String getProperty(NISTPeptideMSPField property) {
		return properties.get(property);
	}

	public void setExactMass(double exactMass) {
		this.exactMass = exactMass;
	}

	public void addProperty(NISTPeptideMSPField field,  String value) {
		properties.put(field, value);
	}

	public Collection<MsPoint> getPrecursors() {
		return precursors;
	}

	public String getPeptideModifications() {
		return peptideModifications;
	}

	public void setPeptideModifications(String peptideModifications) {
		this.peptideModifications = peptideModifications;
	}

	public String getPeptideSequence() {
		return peptideSequence;
	}

	public void setPeptideSequence(String peptideSequence) {
		this.peptideSequence = peptideSequence;
	}

	public int getCharge() {
		return charge;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
}




















