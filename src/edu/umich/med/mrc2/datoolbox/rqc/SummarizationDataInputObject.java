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

package edu.umich.med.mrc2.datoolbox.rqc;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class SummarizationDataInputObject {

	private String uniqueId;
	private Map<SummaryInputColumns,String>dataInputMap;

	public SummarizationDataInputObject() {
		super();
		uniqueId = "SDIO_" +
				UUID.randomUUID().toString().substring(0, 12);
		dataInputMap = new TreeMap<SummaryInputColumns,String>();
	}
	
	public void setField(SummaryInputColumns field, String value) {
		dataInputMap.put(field, value);
	}
	
	public String getField(SummaryInputColumns field) {
		return dataInputMap.get(field);
	}
	
    @Override
    public int hashCode() {
        return uniqueId.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {

		if (obj == this)
			return true;

        if (obj == null)
            return false;

        if (!SummarizationDataInputObject.class.isAssignableFrom(obj.getClass()))
            return false;

        final SummarizationDataInputObject other = (SummarizationDataInputObject) obj;

        if (!this.uniqueId.equals(other.getUniqueId()))
            return false;

        return true;
    }

	public String getUniqueId() {
		return uniqueId;
	}
}
