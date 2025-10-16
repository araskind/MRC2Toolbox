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

import java.io.Serializable;

import org.apache.commons.math3.util.Precision;

import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class MsPoint implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1920814623661946188L;
	private double mz, intensity, rt;
	private String adductType;
	private int charge, scanNum;

	public MsPoint(double mz, double intensity) {

		this.mz = mz;
		this.intensity = intensity;
	}
	
	public MsPoint(double mz, double intensity, int scanNum) {

		this.mz = mz;
		this.intensity = intensity;
		this.scanNum = scanNum;
	}

	public MsPoint(double mz, double intensity, String adductType) {

		this.mz = mz;
		this.intensity = intensity;
		this.adductType = adductType;
	}

	public MsPoint(double mz, double intensity, String adductType, double rt) {

		this.mz = mz;
		this.intensity = intensity;
		this.adductType = adductType;
		this.rt = rt;
	}

	public MsPoint(double mz, double intensity, String adductType, int charge) {

		this.mz = mz;
		this.intensity = intensity;
		this.adductType = adductType;
		this.charge = charge;
	}

	public MsPoint(MsPoint parent) {

		this.mz = parent.getMz();
		this.intensity = parent.getIntensity();
		this.adductType = parent.getAdductType();
	}

	public String getAdductType() {
		return adductType;
	}

	public int getCharge() {
		return charge;
	}

	public double getIntensity() {
		return intensity;
	}

	public double getMz() {
		return mz;
	}

	public double getRt() {
		return rt;
	}

	public String toString() {
		return MsUtils.spectrumMzExportFormat.format(mz) + "_" + 
				MsUtils.spectrumIntensityFormat.format(intensity);
	}	

	public MsPoint(String msPointString) {
		
		if(msPointString == null || msPointString.isEmpty() || !msPointString.contains("_"))
			throw (new IllegalArgumentException("Invalid string"));
		
		String[]parts = msPointString.split("_");
		if(parts.length != 2)
			throw (new IllegalArgumentException("Invalid string"));
		
		mz = Double.parseDouble(parts[0]);
		intensity = Double.parseDouble(parts[1]);
	}

	/**
	 * @param adductType the adductType to set
	 */
	public void setAdductType(String adductType) {
		this.adductType = adductType;
	}

	public int getScanNum() {
		return scanNum;
	}

	public void setScanNum(int scanNum) {
		this.scanNum = scanNum;
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!MsPoint.class.isAssignableFrom(obj.getClass()))
            return false;

        final MsPoint other = (MsPoint) obj;

        if (!Precision.equals(this.mz, other.getMz(), Precision.EPSILON))
            return false;
              
        if (!Precision.equals(this.intensity, other.getIntensity(), Precision.EPSILON))
            return false;
        
        if (this.scanNum != other.getScanNum())
            return false;
        
        if (this.charge != other.getCharge())
            return false;

        return true;
    }
}










