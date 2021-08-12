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

package edu.umich.med.mrc2.datoolbox.data.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import edu.umich.med.mrc2.datoolbox.data.SampleDataResultObject;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;

public class SampleDataResultObjectFormat  extends Format {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6272941157267186017L;
	private SortProperty field;
	private SampleDataResultObject sdro;

	public SampleDataResultObjectFormat(SortProperty field) {
		this.field = field;
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

		if(obj instanceof SampleDataResultObject) {

			sdro = (SampleDataResultObject)obj;
			
			if(field.equals(SortProperty.sample))
				return toAppendTo.append(sdro.getSample().getId());

			if(field.equals(SortProperty.dataFile))
				return toAppendTo.append(sdro.getDataFile().getName());
			
			if(field.equals(SortProperty.resultFile))
				return toAppendTo.append(sdro.getResultFile().getName());
		}
		return toAppendTo.append("");
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		return sdro;
	}

}
