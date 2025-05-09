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

package edu.umich.med.mrc2.datoolbox.data.enums;

import edu.umich.med.mrc2.datoolbox.gui.fdata.cleanup.FeatureCleanupParameters;

public enum  FeatureSetProperties {

	FILTERING_PARAMETERS("Filtering parameters", FeatureCleanupParameters.class);

	private final String uiName;
	private final Class clazz;

	FeatureSetProperties(String uiName, Class clazz) {
		this.uiName = uiName;
		this.clazz = clazz;
	}

	public String getName() {
		return uiName;
	}
	
	public Class getClazz() {
		return clazz;
	}

	@Override
	public String toString() {
		return uiName;
	}
	
	public static FeatureSetProperties getOptionByName(String name) {
		
		for(FeatureSetProperties p : FeatureSetProperties.values()) {
			
			if(p.name().equals(name))
				return p;
		}		
		return null;
	}

	public static FeatureSetProperties getOptionByUIName(String sname) {
		
		for(FeatureSetProperties v : FeatureSetProperties.values()) {
			
			if(v.getName().equals(sname))
				return v;
		}		
		return null;
	}
}
