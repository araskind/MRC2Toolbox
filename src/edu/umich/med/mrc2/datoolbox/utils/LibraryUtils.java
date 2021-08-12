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

package edu.umich.med.mrc2.datoolbox.utils;

import edu.umich.med.mrc2.datoolbox.data.enums.AgilentDatabaseFields;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;

public class LibraryUtils {

	public static CompoundDatabaseEnum parseCompoundDatabaseName(String dbName) {

		CompoundDatabaseEnum db = null;

		for(AgilentDatabaseFields field : AgilentDatabaseFields.values()){

			if(dbName.equals(field.getName()))
				return field.getInternalType();
		}
		for(CompoundDatabaseEnum dtb : CompoundDatabaseEnum.values()){

			if(dbName.equals(dtb.getName()))
				return dtb;
		}
		return db;
	}
}
