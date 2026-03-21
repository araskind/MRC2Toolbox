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

package edu.umich.med.mrc2.datoolbox.gui.rgen.modality;

import java.io.File;
import java.util.Set;

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.gui.rgen.mcr.RMultibatchAnalysisInputObject;
import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class ModalityAnalysisParametersObject implements XmlStorable{

	private File projectParentDirectory;
	private File projectDirectory;
	private Set<RMultibatchAnalysisInputObject>metabCombinerFileInputObjectSet;
	private int maxPercenrMissing;
	private File featureAlignmentFile;
	private double pValueCutoff;
	
	public ModalityAnalysisParametersObject() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public ModalityAnalysisParametersObject(
			File projectParentDirectory, 
			File projectDirectory,
			Set<RMultibatchAnalysisInputObject> metabCombinerFileInputObjectSet, 
			int maxPercenrMissing,
			double pValueCutoff,
			File featureAlignmentFile) {
		super();
		this.projectParentDirectory = projectParentDirectory;
		this.projectDirectory = projectDirectory;
		this.metabCombinerFileInputObjectSet = metabCombinerFileInputObjectSet;
		this.maxPercenrMissing = maxPercenrMissing;
		this.pValueCutoff = pValueCutoff;
		this.featureAlignmentFile = featureAlignmentFile;
	}
	
	public ModalityAnalysisParametersObject(Element modalityAnalysisParametersObjectElement) {
		
	}

	@Override
	public Element getXmlElement() {
		// TODO Auto-generated method stub
		return null;
	}

	public File getProjectParentDirectory() {
		return projectParentDirectory;
	}

	public void setProjectParentDirectory(File projectParentDirectory) {
		this.projectParentDirectory = projectParentDirectory;
	}

	public File getProjectDirectory() {
		return projectDirectory;
	}

	public void setProjectDirectory(File projectDirectory) {
		this.projectDirectory = projectDirectory;
	}

	public Set<RMultibatchAnalysisInputObject> getMetabCombinerFileInputObjectSet() {
		return metabCombinerFileInputObjectSet;
	}

	public void setMetabCombinerFileInputObjectSet(Set<RMultibatchAnalysisInputObject> metabCombinerFileInputObjectSet) {
		this.metabCombinerFileInputObjectSet = metabCombinerFileInputObjectSet;
	}
	
	public int getMaxPercenrMissing() {
		return maxPercenrMissing;
	}

	public void setMaxPercenrMissing(int maxPercenrMissing) {
		this.maxPercenrMissing = maxPercenrMissing;
	}

	public File getFeatureAlignmentFile() {
		return featureAlignmentFile;
	}

	public void setFeatureAlignmentFile(File featureAlignmentFile) {
		this.featureAlignmentFile = featureAlignmentFile;
	}

	public double getpValueCutoff() {
		return pValueCutoff;
	}

	public void setpValueCutoff(double pValueCutoff) {
		this.pValueCutoff = pValueCutoff;
	}

}
