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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.AbstractXYDataset;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.gui.datexp.FeatureIdentificationMeasure;

public class MSMSFeatureInfoBundleDataSet extends AbstractXYDataset{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4892175963127546985L;
	private Map<String, MSFeatureInfoBundle[]>seriesMap;
	private String[] keySet;
	
	public static final String UNKNOWN_SERIES_NAME = "Unknowns";
	public static final String IDENTIFIED_BY_MSMS_SERIES_NAME = "Identified by MSMS";
	public static final String IDENTIFIED_WITHOUT_LEVEL_SERIES_NAME = "ID level missing";

	public MSMSFeatureInfoBundleDataSet(
			Collection<MSFeatureInfoBundle> featureBundles, 
			FeatureIdentificationMeasure colorOption) {

		super();
		seriesMap = new LinkedHashMap<String, MSFeatureInfoBundle[]>();
		populateSeries(featureBundles, colorOption);
	}
	
	private void populateSeries(
			Collection<MSFeatureInfoBundle> featureBundles, 
			FeatureIdentificationMeasure colorOption) {
		
		if(colorOption.equals(FeatureIdentificationMeasure.COLOR_BY_ID_LEVEL))			
			populateSeriesByIDLevel(featureBundles);
		
		if(colorOption.equals(FeatureIdentificationMeasure.COLOR_BY_MSMS_MATCH_TYPE))			
			populateSeriesByMSMSMatchType(featureBundles);
		
		if(colorOption.equals(FeatureIdentificationMeasure.COLOR_BY_NIST_SCORE)
				|| colorOption.equals(FeatureIdentificationMeasure.COLOR_BY_ENTROPY_SCORE)
				|| colorOption.equals(FeatureIdentificationMeasure.COLOR_BY_DOT_PRODUCT)
				|| colorOption.equals(FeatureIdentificationMeasure.COLOR_BY_PROBABILITY)) {
			populateSeriesForScoreColoring(featureBundles);
		}		
		keySet = seriesMap.keySet().toArray(new String[seriesMap.size()]);
	}

	private void populateSeriesForScoreColoring(Collection<MSFeatureInfoBundle> featureBundles) {

		MSFeatureInfoBundle[]msmsIdentified = featureBundles.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
				filter(f -> Objects.nonNull(f.getMsFeature().
						getPrimaryIdentity().getReferenceMsMsLibraryMatch())).
				filter(f -> !f.getMsFeature().getPrimaryIdentity().
						getReferenceMsMsLibraryMatch().isDecoyMatch()).
				toArray(size -> new MSFeatureInfoBundle[size]);
		if(msmsIdentified.length > 0)
			seriesMap.put(IDENTIFIED_BY_MSMS_SERIES_NAME, msmsIdentified);		
		
		MSFeatureInfoBundle[] unknowns = featureBundles.stream().
				filter(f -> !f.getMsFeature().isIdentified()).
				toArray(size -> new MSFeatureInfoBundle[size]);		
		if(unknowns.length > 0)
			seriesMap.put(UNKNOWN_SERIES_NAME, unknowns);
	}

	private void populateSeriesByMSMSMatchType(Collection<MSFeatureInfoBundle> featureBundles) {
		
		List<MSFeatureInfoBundle>msmsIdentified = featureBundles.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
				filter(f -> Objects.nonNull(f.getMsFeature().
						getPrimaryIdentity().getReferenceMsMsLibraryMatch())).
				filter(f -> !f.getMsFeature().getPrimaryIdentity().
						getReferenceMsMsLibraryMatch().isDecoyMatch()).
				collect(Collectors.toList());
		
		MSFeatureInfoBundle[]regularMatches = 
				msmsIdentified.stream().
				filter(f -> f.getMsFeature().getPrimaryIdentity().
						getReferenceMsMsLibraryMatch().getMatchType().equals(MSMSMatchType.Regular)).
				toArray(size -> new MSFeatureInfoBundle[size]);	
		if(regularMatches.length > 0)
			seriesMap.put(MSMSMatchType.Regular.getName(), regularMatches);
		
		MSFeatureInfoBundle[]inSourceMatches = 
				msmsIdentified.stream().
				filter(f -> f.getMsFeature().getPrimaryIdentity().
						getReferenceMsMsLibraryMatch().getMatchType().equals(MSMSMatchType.InSource)).
				toArray(size -> new MSFeatureInfoBundle[size]);	
		if(inSourceMatches.length > 0)
			seriesMap.put(MSMSMatchType.InSource.getName(), inSourceMatches);
		
		MSFeatureInfoBundle[]hybridMatches = 
				msmsIdentified.stream().
				filter(f -> f.getMsFeature().getPrimaryIdentity().
						getReferenceMsMsLibraryMatch().getMatchType().equals(MSMSMatchType.Hybrid)).
				toArray(size -> new MSFeatureInfoBundle[size]);	
		if(hybridMatches.length > 0)
			seriesMap.put(MSMSMatchType.Hybrid.getName(), hybridMatches);
		
		MSFeatureInfoBundle[] unknowns = featureBundles.stream().
				filter(f -> !f.getMsFeature().isIdentified()).
				toArray(size -> new MSFeatureInfoBundle[size]);		
		if(unknowns.length > 0)
			seriesMap.put(UNKNOWN_SERIES_NAME, unknowns);
	}

	private void populateSeriesByIDLevel(Collection<MSFeatureInfoBundle> featureBundles) {
		
		List<MSFeatureInfoBundle> identified = featureBundles.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
				filter(f -> Objects.nonNull(f.getMsFeature().
						getPrimaryIdentity().getIdentificationLevel())).
				collect(Collectors.toList());
		List<MSFeatureIdentificationLevel> idLevels = identified.stream().
			map(f -> f.getMsFeature().getPrimaryIdentity().getIdentificationLevel()).
			distinct().sorted().collect(Collectors.toList());
		
		for(MSFeatureIdentificationLevel level : idLevels) {
			
			MSFeatureInfoBundle[] levelFeatures = identified.stream().
					filter(f -> f.getMsFeature().getPrimaryIdentity().
							getIdentificationLevel().equals(level)).
					toArray(size -> new MSFeatureInfoBundle[size]);
			if(levelFeatures.length > 0)
				seriesMap.put(level.getName(), levelFeatures);			
		}
		MSFeatureInfoBundle[] missingIdLevel = featureBundles.stream().
				filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
				filter(f -> Objects.isNull(f.getMsFeature().
						getPrimaryIdentity().getIdentificationLevel())).
				toArray(size -> new MSFeatureInfoBundle[size]);		
		if(missingIdLevel.length > 0)
			seriesMap.put(IDENTIFIED_WITHOUT_LEVEL_SERIES_NAME, missingIdLevel);
		
		MSFeatureInfoBundle[] unknowns = featureBundles.stream().
				filter(f -> Objects.isNull(f.getMsFeature().getPrimaryIdentity())).
				toArray(size -> new MSFeatureInfoBundle[size]);		
		if(unknowns.length > 0)
			seriesMap.put(UNKNOWN_SERIES_NAME, unknowns);
	}

	public void addSeries(String seriesName, Collection<MSFeatureInfoBundle> featureBundles) {
				
		seriesMap.put(seriesName, featureBundles.toArray(new MSFeatureInfoBundle[featureBundles.size()]));
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
		
		MassSpectrum sp = 
				seriesMap.get(keySet[series])[item].getMsFeature().getSpectrum();		
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
		
	public MSFeatureInfoBundle getMsFeatureInfoBundle(int series, int item) {
		
		if(series > seriesMap.size() - 1)
			return null;
		
		if(item > seriesMap.get(keySet[series]).length - 1)
			return null;
		
		return (MSFeatureInfoBundle) seriesMap.get(keySet[series])[item];
	}
	
	public Collection<MSFeatureInfoBundle>getAllFeatures(){
		
		Collection<MSFeatureInfoBundle>allFeatures = 
				new ArrayList<MSFeatureInfoBundle>();
		for(MSFeatureInfoBundle[] v : seriesMap.values()) {
			
			allFeatures.addAll(Arrays.asList(v));
		}
		return allFeatures;
	}
}








