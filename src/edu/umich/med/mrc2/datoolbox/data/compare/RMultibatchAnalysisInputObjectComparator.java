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

import java.util.Comparator;

import edu.umich.med.mrc2.datoolbox.gui.rgen.mcr.RMultibatchAnalysisInputObject;
import edu.umich.med.mrc2.datoolbox.rqc.SummaryInputColumns;

public class RMultibatchAnalysisInputObjectComparator
		implements Comparator<RMultibatchAnalysisInputObject> {

	@Override
	public int compare(RMultibatchAnalysisInputObject o1, RMultibatchAnalysisInputObject o2) {

		int result = 0;

		for(SummaryInputColumns property :RMultibatchAnalysisInputObject.propertyColumns) {
			
			if(o1.getProperty(property) == null && o2.getProperty(property) == null)
				result = 0;
			else if(o1.getProperty(property) == null)
				result = -1;
			else if(o2.getProperty(property) == null)
				result = 1;
			else
				result = o1.getProperty(property).compareTo(o2.getProperty(property));
			
            if (result != 0)
                return result;
		}
		for(SummaryInputColumns property : RMultibatchAnalysisInputObject.dataFileColumns) {
			
			if (o1.getDataFile(property) == null && o2.getDataFile(property) == null)
				result = 0;
			else if (o1.getDataFile(property) == null)
				result = -1;
			else if (o2.getDataFile(property) == null)
				result = 1;
			else
				result = o1.getDataFile(property).getName().compareTo(o2.getDataFile(property).getName());

			if (result != 0)
				return result;
		}       
        return result;
    }

}
