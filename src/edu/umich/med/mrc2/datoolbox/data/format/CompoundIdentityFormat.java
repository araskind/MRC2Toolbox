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

package edu.umich.med.mrc2.datoolbox.data.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;

public class CompoundIdentityFormat extends Format {

	/**
	 *
	 */
	private static final long serialVersionUID = 9182311196668041506L;
	private CompoundIdentityField idField;
	private CompoundIdentity cid;

	public CompoundIdentityFormat(CompoundIdentityField idField) {
		super();
		this.idField = idField;
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

		if(obj instanceof MsFeatureIdentity) {

			cid = (CompoundIdentity)obj;

			String text = "";
			if(cid == null)
				return toAppendTo.append(text);

			if (idField.equals(CompoundIdentityField.DB_ID))
				text = cid.getPrimaryDatabaseId();

			if (idField.equals(CompoundIdentityField.NAME))
				text = cid.getName();

			if (idField.equals(CompoundIdentityField.COMMON_NAME))
				text = cid.getCommonName();

			if (idField.equals(CompoundIdentityField.SYS_NAME))
				text = cid.getSysName();

			if (idField.equals(CompoundIdentityField.CLASS_NAME))
				text = cid.getClassName();

			if (idField.equals(CompoundIdentityField.FORMULA))
				text = cid.getFormula();

			if (idField.equals(CompoundIdentityField.SMILES))
				text = cid.getSmiles();

			return toAppendTo.append(text);
		}
		return toAppendTo.append("");
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {

		return cid;
	}
}
