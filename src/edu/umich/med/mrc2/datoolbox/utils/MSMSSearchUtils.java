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

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.msmsscore.MSMSSearchParameterSet;

public class MSMSSearchUtils {

	public static final MsFeatureIdentityComparator entropyScoreComparator = 
			new MsFeatureIdentityComparator(SortProperty.msmsEntropyScore, SortDirection.DESC);

	public static String calculateMSMSSearchParametersMd5(MSMSSearchParameterSet params){

		List<String> chunks = new ArrayList<String>();
		chunks.add(MsUtils.spectrumMzExportFormat.format(params.getMzErrorValue()));
		chunks.add(params.getMassErrorType().name());
		chunks.add(MsUtils.spectrumMzExportFormat.format(params.getRtErrorValue()));
		chunks.add(Boolean.toString(params.isIgnoreRt()));
		chunks.add(MsUtils.spectrumMzExportFormat.format(params.getMsmsSimilarityCutoff()));		
		chunks.add(MsUtils.spectrumMzExportFormat.format(params.getEntropyScoreMassError()));
		chunks.add(params.getEntropyScoreMassErrorType().name());
		chunks.add(MsUtils.spectrumMzExportFormat.format(params.getEntropyScoreNoiseCutoff()));
		
	    try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(StringUtils.join(chunks).getBytes(Charset.forName("windows-1252")));
			return DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	    return null;
	}
}
