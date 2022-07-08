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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.project.store.ExtractedIonDataFields;
import edu.umich.med.mrc2.datoolbox.project.store.MsFeatureChromatogramBundleFields;
import edu.umich.med.mrc2.datoolbox.project.store.XICDefinitionFields;
import edu.umich.med.mrc2.datoolbox.utils.ChromatogramUtils;

public class MsFeatureChromatogramBundle {
	
	private ChromatogramDefinition chromatogramDefinition;
	private Map<DataFile, Collection<ExtractedIonData>>chromatograms;
	private String featureId;
	
	public MsFeatureChromatogramBundle(
			String featureId, 
			ChromatogramDefinition chromatogramDefinition) {
		super();
		this.featureId = featureId;
		this.chromatogramDefinition = chromatogramDefinition;
		chromatograms = new TreeMap<DataFile, Collection<ExtractedIonData>>();
	}

	public void addChromatogramsForDataFile(
			DataFile df, Collection<ExtractedIonData>chromatograms2add) {
		
		if(chromatograms.get(df) == null)
			chromatograms.put(df, new TreeSet<ExtractedIonData>(ChromatogramUtils.eidComparator));
			
		if(chromatograms2add != null && !chromatograms2add.isEmpty()) {
			try {
				chromatograms.get(df).addAll(chromatograms2add);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void addChromatogramForDataFile(
			DataFile df, ExtractedIonData chromatogram2add) {
		
		if(chromatograms.get(df) == null)
			chromatograms.put(df, new TreeSet<ExtractedIonData>(ChromatogramUtils.eidComparator));
			
		try {
			chromatograms.get(df).add(chromatogram2add);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Collection<ExtractedIonData>getChromatogramsForDataFile(DataFile df){
		return chromatograms.get(df);
	}
	
	public ChromatogramDefinition getChromatogramDefinition() {
		return chromatogramDefinition;
	}

	public Map<DataFile, Collection<ExtractedIonData>> getChromatograms() {
		return chromatograms;
	}
	
	public Element getXmlElement(String featureId) {
		
		Element msFeatureChromatogramBundleElement = 
				new Element(MsFeatureChromatogramBundleFields.FChrBundle.name());
		msFeatureChromatogramBundleElement.setAttribute(
				MsFeatureChromatogramBundleFields.FID.name(), featureId);
		msFeatureChromatogramBundleElement.addContent(
				chromatogramDefinition.getXmlElement());
		
		if(!chromatograms.isEmpty()) {
			
			for(Entry<DataFile, Collection<ExtractedIonData>> ce : chromatograms.entrySet()) {
				
				Element dfElement = new Element(
						MsFeatureChromatogramBundleFields.DF.name());	
				dfElement.setAttribute(
						MsFeatureChromatogramBundleFields.FName.name(), ce.getKey().getName());
				Element xicListElement = new Element(
						MsFeatureChromatogramBundleFields.XICList.name());			
				for(ExtractedIonData xic : ce.getValue())	
					xicListElement.addContent(xic.getXmlElement());
				
				dfElement.addContent(xicListElement);				
				msFeatureChromatogramBundleElement.addContent(dfElement);
			}
		}		
		return msFeatureChromatogramBundleElement;
	}
	
	public MsFeatureChromatogramBundle(Element cbElement, Collection<DataFile>dataFiles) {
		
		featureId = 
				cbElement.getAttributeValue(MsFeatureChromatogramBundleFields.FID.name());
		Element cdElement = 
				cbElement.getChild(XICDefinitionFields.XICDefinition.name());		
		this.chromatogramDefinition = new ChromatogramDefinition(cdElement);
		
		chromatograms = new TreeMap<DataFile, Collection<ExtractedIonData>>();
		List<Element> dfElementList = 
				cbElement.getChildren(MsFeatureChromatogramBundleFields.DF.name());
		for (Element dfElement : dfElementList) {
			
			String fileName = dfElement.getAttributeValue(
					MsFeatureChromatogramBundleFields.FName.name());
			DataFile df = dataFiles.stream().
				filter(f -> f.getName().equals(fileName)).
				findFirst().orElse(null);
			if(df != null) {
				
				List<Element> xicList = 
						dfElement.getChild(MsFeatureChromatogramBundleFields.XICList.name()).
						getChildren(ExtractedIonDataFields.XICData.name());
				Collection<ExtractedIonData>xicObjects = 
						new TreeSet<ExtractedIonData>(ChromatogramUtils.eidComparator);
				for (Element xicElement : xicList) 					
					xicObjects.add(new ExtractedIonData(xicElement));	
				
				chromatograms.put(df, xicObjects);
			}
		}		
	}

	public String getFeatureId() {
		return featureId;
	}
}











