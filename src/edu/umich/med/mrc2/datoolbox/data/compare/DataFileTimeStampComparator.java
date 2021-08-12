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

package edu.umich.med.mrc2.datoolbox.data.compare;

import java.io.Serializable;
import java.util.Comparator;

import edu.umich.med.mrc2.datoolbox.data.DataFile;

public class DataFileTimeStampComparator implements Serializable, Comparator<DataFile> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2481166823923912519L;

	@Override
	public int compare(DataFile fileOne, DataFile fileTwo) {

		int compared = 0;
		if (fileOne.getInjectionTime() != null && fileTwo.getInjectionTime() != null)
			compared = fileOne.getInjectionTime().compareTo(fileTwo.getInjectionTime());

		return compared;
	}
}
