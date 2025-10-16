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

package edu.umich.med.mrc2.datoolbox.gui.utils;

import java.util.TreeSet;

import org.jdom2.Document;

public class CefUtils {

	//	TODO move functionality to where it is actually used
	public static TreeSet<String> getUnmatchedAdducts(Document cefLibrary, int total, int processed) throws Exception{

		TreeSet<String> allAdducts = new TreeSet<String>();
		TreeSet<String> unmatchedAdducts = new TreeSet<String>();
//		NodeList peakNodes = cefLibrary.get;
//
//		//	Collect all adducts
//		total = peakNodes.getLength();
//		processed = 0;
//		for (int i = 0; i < peakNodes.getLength(); i++) {
//
//			String adductName = ((Element) peakNodes.item(i)).getAttribute("s").replaceAll("\\+[0-9]+$", "").trim();
//			allAdducts.add(adductName);
//			processed++;
//		}
//		//	Check against existing database
//		for(String adduct : allAdducts){
//
//			if(AdductManager.getAdductByCefNotation(adduct) == null)
//				unmatchedAdducts.add(adduct);
//		}
		return unmatchedAdducts;
	}
}




























