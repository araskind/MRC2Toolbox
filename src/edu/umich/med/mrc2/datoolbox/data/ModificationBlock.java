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

import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.project.store.XmlStorable;

public class ModificationBlock implements Serializable, XmlStorable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3774547182137223469L;
	private MsFeature featureOne;
	private MsFeature featureTwo;
	private Adduct modification;

	public ModificationBlock(
			MsFeature featureOne, 
			MsFeature featureTwo) {
		super();
		this.featureOne = featureOne;
		this.featureTwo = featureTwo;
		modification = null;
	}

	public ModificationBlock(
			MsFeature featureOne, 
			MsFeature featureTwo, 
			Adduct modification) {

		super();
		this.featureOne = featureOne;
		this.featureTwo = featureTwo;
		this.modification = modification;
	}

	public MsFeature getFeatureOne() {
		return featureOne;
	}

	public MsFeature getFeatureTwo() {
		return featureTwo;
	}

	public Adduct getModification() {
		return modification;
	}

	public void setModification(Adduct modification) {
		this.modification = modification;
	}

	@Override
	public Element getXmlElement() {
		// TODO Auto-generated method stub
		return null;
	}

}
