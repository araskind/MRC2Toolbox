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

public class RawMsPoint implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1674450906534585466L;
	private double mz, intensity;
	private int scan;

	public RawMsPoint(double mz, double intensity) {

		this.mz = mz;
		this.intensity = intensity;
	}
	
	public RawMsPoint(double mz, double intensity, int scan) {

		this.mz = mz;
		this.intensity = intensity;
		this.scan = scan;
	}
	
	public RawMsPoint(RawMsPoint parent) {

		this.mz = parent.getMz();
		this.intensity = parent.getIntensity();
		this.scan = parent.getScan();
	}
	
	public double getIntensity() {
		return intensity;
	}

	public double getMz() {
		return mz;
	}

	public String toString() {
		return Double.toString(mz) + " @ " + Double.toString(intensity);
	}

	public int getScan() {
		return scan;
	}

	public void setScan(int scan) {
		this.scan = scan;
	}
	
	@Override
	public boolean equals(Object p) {

        if (p == this)
            return true;

		if(p == null)
			return false;

        if (!RawMsPoint.class.isAssignableFrom(p.getClass()))
            return false;

        RawMsPoint op = (RawMsPoint)p;

        if (this.mz != op.getMz())
        	return false;
        
        if (this.intensity != op.getIntensity())
        	return false;

        if (this.scan != op.getScan())
        	return false;

        return true;
	}
	
    @Override
    public int hashCode() {

        int hash = 3;
        hash =  53 * hash + (int)Math.round(mz * 1000) + (int)Math.round(intensity) + scan;
        return hash;
    }
}
