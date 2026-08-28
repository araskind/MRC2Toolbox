/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.umich.med.mrc2.datoolbox.data.ChromatogramDefinition;
import edu.umich.med.mrc2.datoolbox.data.XicDataBundle;
import edu.umich.med.mrc2.datoolbox.data.compare.ExtractedIonDataComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;

public class ChromatogramUtils {
	
	private static final Logger logger = LogManager.getLogger(ChromatogramUtils.class);

	private ChromatogramUtils() {
		/* This utility class should not be instantiated */
	}
	
	private static final PearsonsCorrelation pearsonsCorrelation = 
			new PearsonsCorrelation();
	public static final ExtractedIonDataComparator eidComparator = 
			new ExtractedIonDataComparator(SortProperty.MZ);

	public static double calculateXicCorrelationInPeak(
			XicDataBundle bundleOne,
			XicDataBundle bundleTwo, 
			Range peakRange) {
		return calculateXicCorrelationWithinRanges(
				bundleOne, peakRange, bundleTwo, peakRange);
	}
	
	public static double calculateXicCorrelationWithinRanges(
			XicDataBundle bundleOne, Range rangeOne,
			XicDataBundle bundleTwo, Range rangeTwo) {		

		Map<Integer, Double> smoothIntOneTrimmed = 
				bundleOne.getSmoothedIntensityByScanWithinRtRange(rangeOne);		
		Map<Integer, Double> smoothIntTwoTrimmed = 
				bundleTwo.getSmoothedIntensityByScanWithinRtRange(rangeTwo);
						
		TreeSet<Integer>scans = new TreeSet<Integer>();
		scans.addAll(bundleOne.getScansWithinRtRange(rangeOne));
		scans.addAll(bundleTwo.getScansWithinRtRange(rangeTwo));
	
		Integer[]scanArray = scans.toArray(new Integer[scans.size()]);
		double[] c1areas = new double[scanArray.length];
		double[] c2areas = new double[scanArray.length];
		for(int i=0; i<scanArray.length; i++) {

			if(smoothIntOneTrimmed.get(i) == null)
				c1areas[i] = 0.0d;
			else
				c1areas[i] = smoothIntOneTrimmed.get(i);
							
			if(smoothIntTwoTrimmed.get(i) == null)
				c2areas[i] = 0.0d;
			else
				c2areas[i] = smoothIntTwoTrimmed.get(i);
		
			Double corr = pearsonsCorrelation.correlation(c1areas, c2areas);
			if(corr.equals(Double.NaN))
				corr = Double.valueOf(0.0d);
		}	
		return 0.0d;
	}
	
	public static String getChromatogramDefinitionHash(ChromatogramDefinition cd){

		List<String> chunks = new ArrayList<String>();
		chunks.add(cd.getMode().name());
		if(cd.getPolarity() != null)
			chunks.add(cd.getPolarity().getCode());
		
		chunks.add(Integer.toString(cd.getMsLevel()));		
		if(cd.getMzList() != null && !cd.getMzList().isEmpty()) {
			List<String> stringList = cd.getMzList().stream().
					map(MsUtils.spectrumMzExportFormat::format).
					collect(Collectors.toList());
			chunks.add(StringUtils.join(stringList));
		}		
		chunks.add(MsUtils.spectrumMzExportFormat.format(cd.getMzWindowValue()));
		if(cd.getMassErrorType() != null)
			chunks.add(cd.getMassErrorType().name());
		
		if(cd.getRtRange() != null)
			chunks.add(cd.getRtRange().getStorableString());		
		
		//	TODO smoothing filter 
		//	chunks.add("");
		
		chunks.add(Boolean.toString(cd.isDoSmooth()));
	    try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(StringUtils.join(chunks).getBytes(Charset.forName("windows-1252")));
			return DatatypeConverter.printHexBinary(md.digest()).toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			logger.error("Failed to encode Chromatogram Definition Hash", e);
		}
	    return null;
	}
}
