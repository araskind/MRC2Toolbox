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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;

public class CompoundUtils {
	
	private static final Logger logger = LogManager.getLogger(CompoundUtils.class);
	
	private CompoundUtils() {
		/* This utility class should not be instantiated */
	}

	public static String getPrimaryLinkAddress(CompoundIdentity cid) {

		CompoundDatabaseEnum primaryDatabase = cid.getPrimaryDatabase();
		if(primaryDatabase == null)
			return "";
		
		String primaryLinkId = null;
		String primaryLinkAddress = "";

		//	TODO handle ID cleanup in a more centralized way
		primaryLinkId =  cid.getPrimaryDatabaseId().replace("METLIN:","").replace("ALDRICH:", "");
		if(primaryDatabase.equals(CompoundDatabaseEnum.LIPIDMAPS_BULK)) {

			String[] split = StringUtils.split(primaryLinkId, '-');
			primaryLinkAddress =
				primaryDatabase.getDbLinkPrefix() + split[0] +
				"&Formula=" + split[1] +
				"&ExactMass=" + cid.getExactMass() +
				"&ExactMassOffSet=0.1";
			return primaryLinkAddress;
		}
		else if(primaryDatabase.equals(CompoundDatabaseEnum.REFMET)) {
			try {
				primaryLinkAddress = URLEncoder.encode(primaryLinkId, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.error("Failed to encode RefMet name", e);
			}
			return primaryDatabase.getDbLinkPrefix() + primaryLinkAddress;
		}
		else
			return primaryDatabase.getDbLinkPrefix() + primaryLinkId + primaryDatabase.getDbLinkSuffix();
	}
}
