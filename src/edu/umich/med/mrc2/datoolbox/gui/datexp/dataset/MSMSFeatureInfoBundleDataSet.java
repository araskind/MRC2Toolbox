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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.AbstractXYDataset;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;

public class MSMSFeatureInfoBundleDataSet extends AbstractXYDataset{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4892175963127546985L;
	private Map<String, MsFeatureInfoBundle[]>seriesMap;
	private String[] keySet;
	
	public static final String UNKNOWN_SERIES_NAME = "Unknowns";
	public static final String IDENTIFIED_WITHOUT_LEVEL_SERIES_NAME = "ID level missing";

	public MSMSFeatureInfoBundleDataSet() {
		super();
		seriesMap = new LinkedHashMap<String, MsFeatureInfoBundle[]>();
	}

	public MSMSFeatureInfoBundleDataSet(Collection<MsFeatureInfoBundle> featureBundles) {

		super();
		seriesMap = new LinkedHashMap<String, MsFeatureInfoBundle[]>();
		populateSeries(featureBundles);
	}
	
	private void populateSeries(Collection<MsFeatureInfoBundle> featureBundles) {

		List<MsFeatureInfoBundle> identified = featureBundles.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
				filter(f -> Objects.nonNull(f.getMsFeature().
						getPrimaryIdentity().getIdentificationLevel())).
				collect(Collectors.toList());
		List<MSFeatureIdentificationLevel> idLevels = identified.stream().
			map(f -> f.getMsFeature().getPrimaryIdentity().getIdentificationLevel()).
			distinct().sorted().collect(Collectors.toList());
		
		for(MSFeatureIdentificationLevel level : idLevels) {
			
			MsFeatureInfoBundle[] levelFeatures = identified.stream().
					filter(f -> f.getMsFeature().getPrimaryIdentity().
							getIdentificationLevel().equals(level)).
					toArray(size -> new MsFeatureInfoBundle[size]);
			if(levelFeatures.length > 0)
				seriesMap.put(level.getName(), levelFeatures);			
		}
		MsFeatureInfoBundle[] missingIdLevel = featureBundles.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
				filter(f -> Objects.isNull(f.getMsFeature().
						getPrimaryIdentity().getIdentificationLevel())).
				toArray(size -> new MsFeatureInfoBundle[size]);		
		if(missingIdLevel.length > 0)
			seriesMap.put(IDENTIFIED_WITHOUT_LEVEL_SERIES_NAME, missingIdLevel);
		
		MsFeatureInfoBundle[] unknowns = featureBundles.stream().
				filter(f -> Objects.isNull(f.getMsFeature().getPrimaryIdentity())).
				toArray(size -> new MsFeatureInfoBundle[size]);		
		if(unknowns.length > 0)
			seriesMap.put(UNKNOWN_SERIES_NAME, unknowns);
		
		keySet = seriesMap.keySet().toArray(new String[seriesMap.size()]);
	}

	public void addSeries(String seriesName, Collection<MsFeatureInfoBundle> featureBundles) {
				
		seriesMap.put(seriesName, featureBundles.toArray(new MsFeatureInfoBundle[featureBundles.size()]));
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
	
	// Returns feature retention time
	@Override
	public double getXValue(int series, int item) {
		
		return seriesMap.get(keySet[series])[item].getRetentionTime();
	}
	
	// Returns feature monoisotopic M/Z mass defect, Kendrick adjusted if necessary
	@Override
	public Number getY(int series, int item) {
		return new Double(getYValue(series, item));
	}
	
	// Returns feature monoisotopic M/Z mass defect, Kendrick adjusted if necessary
	@Override
	public double getYValue(int series, int item) {
		
		MassSpectrum sp = seriesMap.get(keySet[series])[item].
		getMsFeature().getSpectrum();
		
		TandemMassSpectrum msms = seriesMap.get(keySet[series])[item].
				getMsFeature().getSpectrum().getExperimentalTandemSpectrum();
		if(msms == null)
			return 0.0d;
				
		return msms.getParent().getMz();
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
		
	public MsFeatureInfoBundle getMsFeatureInfoBundle(int series, int item) {
		return (MsFeatureInfoBundle) seriesMap.get(keySet[series])[item];
	}
}
