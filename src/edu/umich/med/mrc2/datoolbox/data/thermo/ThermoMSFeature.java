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

package edu.umich.med.mrc2.datoolbox.data.thermo;

import java.util.UUID;

public class ThermoMSFeature implements Comparable<ThermoMSFeature> {

	private String id;
	private int bestHitId;
	private ThermoBestHitType bestHitType;
	private int msId;
	private String adduct;
	private int charge;
	private double mz;
	private double mw;
	private double rt;
	private double bestHitRt;
	private double intensity;
	private double area;
	private int fileId;
	private ThermoMSOrderType msOrder;
	private ThermoPolarityType polarity;
	private int resolutionAsMass200;
	private ThermoActivationType activationType;
	private ThermoScanType scanType;
	private ThermoIonizationType ionization;
	private ThermoMassAnalyzerType massAnalyzer;	
	private double isolationWidth;
	
	public ThermoMSFeature() {
		super();
		id = UUID.randomUUID().toString();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getBestHitId() {
		return bestHitId;
	}

	public void setBestHitId(int bestHitId) {
		this.bestHitId = bestHitId;
	}

	public ThermoBestHitType getBestHitType() {
		return bestHitType;
	}

	public void setBestHitType(ThermoBestHitType bestHitType) {
		this.bestHitType = bestHitType;
	}

	public int getMsId() {
		return msId;
	}

	public void setMsId(int msId) {
		this.msId = msId;
	}

	public String getAdduct() {
		return adduct;
	}

	public void setAdduct(String adduct) {
		this.adduct = adduct;
	}

	public int getCharge() {
		return charge;
	}

	public void setCharge(int charge) {
		this.charge = charge;
	}

	public double getMz() {
		return mz;
	}

	public void setMz(double mz) {
		this.mz = mz;
	}

	public double getMw() {
		return mw;
	}

	public void setMw(double mw) {
		this.mw = mw;
	}

	public double getRt() {
		return rt;
	}

	public void setRt(double rt) {
		this.rt = rt;
	}

	public double getBestHitRt() {
		return bestHitRt;
	}

	public void setBestHitRt(double bestHitRt) {
		this.bestHitRt = bestHitRt;
	}

	public double getIntensity() {
		return intensity;
	}

	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}

	public double getArea() {
		return area;
	}

	public void setArea(double area) {
		this.area = area;
	}

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public ThermoMSOrderType getMsOrder() {
		return msOrder;
	}

	public void setMsOrder(ThermoMSOrderType msOrder) {
		this.msOrder = msOrder;
	}

	public ThermoPolarityType getPolarity() {
		return polarity;
	}

	public void setPolarity(ThermoPolarityType polarity) {
		this.polarity = polarity;
	}

	public int getResolutionAsMass200() {
		return resolutionAsMass200;
	}

	public void setResolutionAsMass200(int resolutionAsMass200) {
		this.resolutionAsMass200 = resolutionAsMass200;
	}

	public ThermoActivationType getActivationType() {
		return activationType;
	}

	public void setActivationType(ThermoActivationType activationType) {
		this.activationType = activationType;
	}

	public ThermoScanType getScanType() {
		return scanType;
	}

	public void setScanType(ThermoScanType scanType) {
		this.scanType = scanType;
	}

	public ThermoIonizationType getIonization() {
		return ionization;
	}

	public void setIonization(ThermoIonizationType ionization) {
		this.ionization = ionization;
	}

	public ThermoMassAnalyzerType getMassAnalyzer() {
		return massAnalyzer;
	}

	public void setMassAnalyzer(ThermoMassAnalyzerType massAnalyzer) {
		this.massAnalyzer = massAnalyzer;
	}

	public double getIsolationWidth() {
		return isolationWidth;
	}

	public void setIsolationWidth(double isolationWidth) {
		this.isolationWidth = isolationWidth;
	}
	
	@Override
	public int compareTo(ThermoMSFeature o) {
		return id.compareTo(o.getId());
	}
	
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!ThermoMSFeature.class.isAssignableFrom(obj.getClass()))
            return false;

        final ThermoMSFeature other = (ThermoMSFeature) obj;

        if ((this.id == null) ? (other.getId() != null) : !this.id.equals(other.getId()))
            return false;

        return true;
    }
    
    @Override
    public int hashCode() {

        int hash = 3;
        hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {
    	return Double.toString(mz) + "@" + Double.toString(rt) + 
    			" for file ID " + Integer.toString(fileId);
    }
	
//	H.ID AS BHID, M.ID AS MSID, H.BESTHITTYPE, 
//	H.IONDESCRIPTION, H.CHARGE, H.MOLECULARWEIGHT, H.MASS, H.RETENTIONTIME AS BHRT,
//	M.RETENTIONTIME AS MSRT, H.INTENSITY, H.AREA, H.STUDYFILEID, M.MSORDER, 
//	M.POLARITY, M.RESOLUTIONATMASS200, M.ACTIVATIONTYPE, M.SCANTYPE, M.IONIZATION,
//	M.MASSANALYZER, M.ISOLATIONWIDTH
}
