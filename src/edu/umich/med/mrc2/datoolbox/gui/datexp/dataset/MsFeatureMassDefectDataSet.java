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

package edu.umich.med.mrc2.datoolbox.gui.datexp.dataset;

import java.util.Collection;
import java.util.TreeMap;

import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.AbstractXYDataset;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.KendrickUnits;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class MsFeatureMassDefectDataSet extends AbstractXYDataset{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4892175963127546985L;
	private TreeMap<String, MsFeature[]>seriesMap;
	private String[] keySet;
	private KendrickUnits kendrickUnits;

	public MsFeatureMassDefectDataSet() {
		super();
		seriesMap = new TreeMap<String, MsFeature[]>();
		kendrickUnits = KendrickUnits.NONE;
	}

	public MsFeatureMassDefectDataSet(
			String seriesName, 
			Collection<MsFeature> features) {

		super();

		seriesMap = new TreeMap<String, MsFeature[]>();
		seriesMap.put(seriesName, features.toArray(new MsFeature[features.size()]));
		keySet = seriesMap.keySet().toArray(new String[seriesMap.size()]);
		kendrickUnits = KendrickUnits.NONE;
	}
	
	public MsFeatureMassDefectDataSet(
			String seriesName, 
			Collection<MsFeature> features, 
			KendrickUnits newUnits) {

		super();
		kendrickUnits = newUnits;
		seriesMap = new TreeMap<String, MsFeature[]>();
		seriesMap.put(seriesName, features.toArray(new MsFeature[features.size()]));
		keySet = seriesMap.keySet().toArray(new String[seriesMap.size()]);		
	}

	public void addSeries(String seriesName, Collection<MsFeature> features) {

		seriesMap.put(seriesName, features.toArray(new MsFeature[features.size()]));
		keySet = seriesMap.keySet().toArray(new String[seriesMap.size()]);
		notifyListeners(new DatasetChangeEvent(this, this));
	}

    public void removeSeries(String seriesName) {

    	if(seriesMap.remove(seriesName) != null) {

    		keySet = seriesMap.keySet().toArray(new String[seriesMap.size()]);
    		notifyListeners(new DatasetChangeEvent(this, this));
    	}
    }

	@Override
	public int getItemCount(int series) {

        if ((series < 0) || (series >= getSeriesCount()))
            throw new IllegalArgumentException("Series index out of bounds");

        return seriesMap.get(keySet[series]).length;
	}

	// Returns feature monoisotopic M/Z, Kendrick adjusted if necessary
	@Override
	public Number getX(int series, int item) {
		return new Double(getXValue(series, item));
	}
	
	// Returns feature monoisotopic M/Z, Kendrick adjusted if necessary
	@Override
	public double getXValue(int series, int item) {
		
		double mz = seriesMap.get(keySet[series])[item].getMonoisotopicMz();
		if(kendrickUnits.equals(KendrickUnits.NONE) || kendrickUnits.equals(KendrickUnits.RELATIVE))
			return mz;
		else
			return MsUtils.getKendrickNominalMass(mz, kendrickUnits);
	}
	
	// Returns feature monoisotopic M/Z mass defect, Kendrick adjusted if necessary
	@Override
	public Number getY(int series, int item) {
		return new Double(getYValue(series, item));
	}
	
	// Returns feature monoisotopic M/Z mass defect, Kendrick adjusted if necessary
	@Override
	public double getYValue(int series, int item) {
		
		double mz = seriesMap.get(keySet[series])[item].getMonoisotopicMz();
		if(kendrickUnits.equals(KendrickUnits.NONE))			
			return mz - Math.round(mz);		
		else if(kendrickUnits.equals(KendrickUnits.RELATIVE))
			return MsUtils.getRelativeMassDefectPpm(mz);
		else
			return MsUtils.getKendrickMassDefect(
				seriesMap.get(keySet[series])[item].getMonoisotopicMz(), 
				kendrickUnits);
	}

	@Override
	public int getSeriesCount() {
		return seriesMap.size();
	}

	@Override
	public Comparable getSeriesKey(int series) {
		return keySet[series];
	}

	@Override
	public int indexOf(Comparable seriesKey) {

        if (!seriesMap.containsKey(seriesKey))
            throw new IllegalArgumentException("Series " + seriesKey + " does not exist");

        for(int i=0; i<keySet.length; i++) {

        	if(keySet[i].equals(seriesKey))
        		return i;
        }
        return 0;
	}
		
	public MsFeature getMsFeature(int series, int item) {
		return (MsFeature) seriesMap.get(keySet[series])[item];
	}

	public void setKendrickUnits(KendrickUnits newKendrickUnits) {
		
		if(!kendrickUnits.equals(newKendrickUnits)) {
			this.kendrickUnits = newKendrickUnits;
			notifyListeners(new DatasetChangeEvent(this, this));
		}
	}
}
