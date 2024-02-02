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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeMap;

import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.AbstractXYZDataset;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;

public class MsFeatureBubbleDataSet extends AbstractXYZDataset{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4892175963127546985L;
	private TreeMap<String, MsFeature[]>seriesMap;
	private String[] keySet;
	private DataScale dataScale;
	private Collection<MsFeature> allFeatures;

	public MsFeatureBubbleDataSet() {
		super();
		seriesMap = new TreeMap<String, MsFeature[]>();
		allFeatures = new ArrayList<MsFeature>();
		dataScale = DataScale.LN;
	}

	public MsFeatureBubbleDataSet(
			String seriesName, Collection<MsFeature> features, DataScale newScale) {

		super();
		dataScale = newScale;
		seriesMap = new TreeMap<String, MsFeature[]>();
		seriesMap.put(seriesName, features.toArray(new MsFeature[features.size()]));
		keySet = seriesMap.keySet().toArray(new String[seriesMap.size()]);
		allFeatures = new ArrayList<MsFeature>();
		allFeatures.addAll(features);
	}

	public void addSeries(String seriesName, Collection<MsFeature> features) {

		seriesMap.put(seriesName, features.toArray(new MsFeature[features.size()]));
		keySet = seriesMap.keySet().toArray(new String[seriesMap.size()]);
		allFeatures.addAll(features);
		notifyListeners(new DatasetChangeEvent(this, this));
	}

    public void removeSeries(String seriesName) {
    	
    	MsFeature[] removed = seriesMap.remove(seriesName);
    	if(removed != null) {

    		allFeatures.removeAll(Arrays.asList(removed));
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

	// Returns feature retention time
	@Override
	public Number getX(int series, int item) {

		return new Double(getXValue(series, item));
	}
	// Returns feature retention time
	@Override
	public double getXValue(int series, int item) {

		return seriesMap.get(keySet[series])[item].getRetentionTime();
	}
	// Returns feature base peak M/Z
	@Override
	public Number getY(int series, int item) {

		return new Double(getYValue(series, item));
	}
	// Returns feature base peak M/Z
	@Override
	public double getYValue(int series, int item) {
		return seriesMap.get(keySet[series])[item].getBasePeakMz();
	}
	//	Get average area
	@Override
	public Number getZ(int series, int item) {
		return new Double(getZValue(series, item));
	}
	//	Get average area
	@Override
	public double getZValue(int series, int item) {

		MsFeature feature = seriesMap.get(keySet[series])[item];
		double total = 1;
		if(feature.getStatsSummary() != null)
			total = feature.getStatsSummary().getTotalMedian();
		
		if(dataScale.equals(DataScale.LN))
			return Math.log1p(total);

		if(dataScale.equals(DataScale.LOG10))
			return Math.log10(total);

		if(dataScale.equals(DataScale.SQRT))
			return Math.sqrt(total);

		return total;
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

	public DataScale getDataScale() {
		return dataScale;
	}

	public void setDataScale(DataScale newScale) {
		
		if(newScale.equals(DataScale.LN) 
				|| newScale.equals(DataScale.LOG10) 
				||  newScale.equals(DataScale.SQRT)) {
			
			boolean scaleChanged = false;
			if(!dataScale.equals(newScale))
				scaleChanged = true;
			
			this.dataScale = newScale;
			
			if(scaleChanged)
				notifyListeners(new DatasetChangeEvent(this, this));
		}
	}
		
	public MsFeature getMsFeature(int series, int item) {
		return (MsFeature) seriesMap.get(keySet[series])[item];
	}
	
	public Collection<MsFeature>getAllFeatures(){
		return allFeatures;
	}
}
