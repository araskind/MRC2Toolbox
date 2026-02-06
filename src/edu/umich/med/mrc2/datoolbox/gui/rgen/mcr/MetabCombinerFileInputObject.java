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

package edu.umich.med.mrc2.datoolbox.gui.rgen.mcr;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Attribute;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.project.store.MetabCombinerAlignmentSettingsFields;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;
import edu.umich.med.mrc2.datoolbox.rqc.SummaryInputColumns;

public class MetabCombinerFileInputObject implements Comparable<MetabCombinerFileInputObject>, XmlStorable{

	private File dataFile;
	private Map<SummaryInputColumns,String>propertiesMap;
	
	public MetabCombinerFileInputObject(File dataFile, String experimentId, String batchId) {
		super();
		this.dataFile = dataFile;
		propertiesMap = new EnumMap<>(SummaryInputColumns.class);
		propertiesMap.put(SummaryInputColumns.EXPERIMENT, experimentId);
		propertiesMap.put(SummaryInputColumns.BATCH, batchId);
	}

	public File getDataFile() {
		return dataFile;
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
	
	public boolean isDefined() {
		
		if(dataFile == null || !dataFile.exists())
			return false;
		
		if(getExperimentId() == null || getExperimentId().isBlank())
			return false;
		
		if(getBatchId() == null || getBatchId().isBlank())
			return false;
		
		return true;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (obj == null)
			return false;
		
		if (obj == this)
			return true;
		
		if (!MetabCombinerFileInputObject.class.isAssignableFrom(obj.getClass()))
			return false;
		
		final MetabCombinerFileInputObject other = (MetabCombinerFileInputObject) obj;
		
		if ((this.dataFile == null) ? (other.getDataFile() != null) : !this.dataFile.equals(other.getDataFile())) 
			return false; 
		
		if ((this.getExperimentId() == null) ? (other.getExperimentId() != null) : !this.getExperimentId().equals(other.getExperimentId())) 
			return false; 
		
		if ((this.getBatchId() == null) ? (other.getBatchId() != null) : !this.getBatchId().equals(other.getBatchId())) 
			return false; 
		
		//	TODO compare all properties

		return true;
	}

	@Override
	public int hashCode() {
		
		int hash = 3;
		hash = 53 * hash + (this.dataFile != null ? this.dataFile.hashCode() : 0)
        		+ (this.getExperimentId() != null ? this.getExperimentId().hashCode() : 0)
        		+ (this.getBatchId() != null ? this.getBatchId().hashCode() : 0);
		//	TODO use all properties
		return hash;
	}

	@Override
	public int compareTo(MetabCombinerFileInputObject o) {

		int res = this.getExperimentId().compareTo(o.getExperimentId());
		if(res == 0)
			res = this.getBatchId().compareTo(o.getBatchId());
		
		if(res == 0)
			res = this.dataFile.compareTo(o.getDataFile());
		//	TODO use all properties
		return res;
	}

	@Override
	public Element getXmlElement() {

		Element metabCombinerFileInputObjectElement = 
				new Element(MetabCombinerAlignmentSettingsFields.MetabCombinerFileIO.name());
		for(Entry<SummaryInputColumns, String> pe : propertiesMap.entrySet())			
			metabCombinerFileInputObjectElement.setAttribute(pe.getKey().name(), pe.getValue());
		
		metabCombinerFileInputObjectElement.setText(dataFile.getAbsolutePath());
		
		return metabCombinerFileInputObjectElement;
	}
	
	public MetabCombinerFileInputObject(Element metabCombinerFileInputObjectElement) {
		
		propertiesMap = new EnumMap<>(SummaryInputColumns.class);
		for(Attribute at : metabCombinerFileInputObjectElement.getAttributes()) {
			
			SummaryInputColumns col = SummaryInputColumns.getOptionByName(at.getName());
			if(col != null && at.getValue() != null)
				propertiesMap.put(col, at.getValue());
		}
		Path dataFilePath = Paths.get(metabCombinerFileInputObjectElement.getText());
		dataFile = dataFilePath.toFile();
	}
}
