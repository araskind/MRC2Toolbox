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

import java.io.File;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import org.apache.commons.compress.utils.FileNameUtils;

public class FileBaseNameFormat extends Format {

	/**
	 *
	 */
	private static final long serialVersionUID = -1444711571718369398L;

	private File file;

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

		if(obj instanceof File) {
			file = (File)obj;
			return toAppendTo.append(FileNameUtils.getBaseName(file.getName()));
		}
		return toAppendTo.append("");
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		return file;
	}
}
