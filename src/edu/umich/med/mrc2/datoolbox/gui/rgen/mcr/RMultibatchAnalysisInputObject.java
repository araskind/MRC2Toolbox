/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.gui.rgen.mcr;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Attribute;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.project.store.MetabCombinerAlignmentSettingsFields;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;
import edu.umich.med.mrc2.datoolbox.rqc.SummaryInputColumns;

public class RMultibatchAnalysisInputObject implements XmlStorable{

	public static final List<SummaryInputColumns> propertyColumns = 
			Arrays.asList(
					SummaryInputColumns.EXPERIMENT,
					SummaryInputColumns.ASSAY,
					SummaryInputColumns.BATCH, 
					SummaryInputColumns.MFE_CUTOFF,
					SummaryInputColumns.EM_VOLTAGE);
	
	public static final List<SummaryInputColumns> dataFileColumns = 
			Arrays.asList(
					SummaryInputColumns.MANIFEST,
					SummaryInputColumns.PEAK_AREAS,
					SummaryInputColumns.MZ_VALUES, 
					SummaryInputColumns.RT_VALUES,
					SummaryInputColumns.PEAK_WIDTH,
					SummaryInputColumns.PEAK_QUALITY);
	
	private Map<SummaryInputColumns,String>propertiesMap;
	private Map<SummaryInputColumns,File>filesMap;
	
	public RMultibatchAnalysisInputObject() {
		super();
		propertiesMap = new EnumMap<>(SummaryInputColumns.class);
		filesMap = new EnumMap<>(SummaryInputColumns.class);
	}
	
	public RMultibatchAnalysisInputObject(
			File dataFile, 
			SummaryInputColumns fileType,
			String experimentId, 
			String batchId) {
		this();
		filesMap.put(fileType, dataFile);				
		propertiesMap.put(SummaryInputColumns.EXPERIMENT, experimentId);
		propertiesMap.put(SummaryInputColumns.BATCH, batchId);
	}

	public File getDataFile(SummaryInputColumns fileType) {
		return filesMap.get(fileType);
	}
	
	public void setDataFile(SummaryInputColumns fileType, File inputDataFile) {
		filesMap.put(fileType, inputDataFile);
	}

	public String getExperimentId() {
		return propertiesMap.get(SummaryInputColumns.EXPERIMENT);
	}

	public String getBatchId() {
		return propertiesMap.get(SummaryInputColumns.BATCH);
	}
	
	public String getProperty(SummaryInputColumns property) {
		return propertiesMap.get(property);
	}
	
	public void setProperty(SummaryInputColumns property, String propertyValue) {
		propertiesMap.put(property, propertyValue);
	}
	
	public File getFile(SummaryInputColumns property) {
		return filesMap.get(property);
	}
	
	public boolean isDefined(List<SummaryInputColumns> requiredProperties) {
		
		for(SummaryInputColumns prop : requiredProperties) {
			
			if(propertyColumns.contains(prop) 
					&& (getProperty(prop) == null || getProperty(prop).isBlank()))
				return false;

			if(dataFileColumns.contains(prop) && getFile(prop) == null)
				return false;
		}
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null)
			return false;
		
		if (obj == this)
			return true;
		
		if (!RMultibatchAnalysisInputObject.class.isAssignableFrom(obj.getClass()))
			return false;
		
		final RMultibatchAnalysisInputObject other = (RMultibatchAnalysisInputObject) obj;
		
		for (SummaryInputColumns col : SummaryInputColumns.values()) {

			if(propertyColumns.contains(col)){
				
				if ((this.getProperty(col) == null) ? (other.getProperty(col) != null)
						: !this.getProperty(col).equals(other.getProperty(col)))
					return false;	
			}
			if(dataFileColumns.contains(col)) {
				
				if ((this.getFile(col) == null) ? (other.getFile(col) != null)
						: !this.getFile(col).equals(other.getFile(col)))
					return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
	
		int filesHash = filesMap.values().stream().mapToInt(File::hashCode).sum();
		int propHash = propertiesMap.values().stream().mapToInt(String::hashCode).sum();
		return filesHash + propHash;
	}

	@Override
	public Element getXmlElement() {

		Element metabCombinerFileInputObjectElement = 
				new Element(MetabCombinerAlignmentSettingsFields.MetabCombinerFileIO.name());
		for(Entry<SummaryInputColumns, String> pe : propertiesMap.entrySet())			
			metabCombinerFileInputObjectElement.setAttribute(pe.getKey().name(), pe.getValue());
		
		Element fileSetElement = 
				new Element(MetabCombinerAlignmentSettingsFields.EntryFileSet.name());
		
		for(Entry<SummaryInputColumns, File> pe : filesMap.entrySet())	{		
			
			Element fileElement = new Element(pe.getKey().name());
			fileElement.setText(pe.getValue().getAbsolutePath());
			fileSetElement.addContent(fileElement);
		}
		metabCombinerFileInputObjectElement.addContent(fileSetElement);
		return metabCombinerFileInputObjectElement;
	}
	
	public RMultibatchAnalysisInputObject(Element metabCombinerFileInputObjectElement) {
		
		propertiesMap = new EnumMap<>(SummaryInputColumns.class);
		filesMap = new EnumMap<>(SummaryInputColumns.class);		
		for(Attribute at : metabCombinerFileInputObjectElement.getAttributes()) {
			
			SummaryInputColumns col = SummaryInputColumns.getOptionByName(at.getName());
			if(col != null && at.getValue() != null && propertyColumns.contains(col))
				propertiesMap.put(col, at.getValue());			
		}
		List<Element>fileElements = metabCombinerFileInputObjectElement.getChildren(
				MetabCombinerAlignmentSettingsFields.EntryFileSet.name());
		for(Element fe : fileElements) {
			
			SummaryInputColumns fileCol = SummaryInputColumns.getOptionByName(fe.getName());
			if(fileCol != null && fe.getText() != null && !fe.getText().isBlank() 
					&& dataFileColumns.contains(fileCol))
				filesMap.put(fileCol, Paths.get(fe.getText()).toFile());	
		}
	}
}
