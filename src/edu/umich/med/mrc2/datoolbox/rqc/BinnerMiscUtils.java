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

package edu.umich.med.mrc2.datoolbox.rqc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class BinnerMiscUtils {

	public static void main(String[] args) {
		
		try {
			classifyBinnerAnnotations();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void classifyBinnerAnnotations() {
		
		File annotationsDir = new File("E:\\DataAnalysis\\Binner\\Annotations2\\Carriers");
		File[]annotations = annotationsDir.listFiles();
		Map<Integer,Set<String>>annotationsMap = new HashMap<Integer,Set<String>>();
		
		for(File annotation : annotations) {
			
			if(annotation.isDirectory())
				continue;
			
			String contents = FIOUtils.readFileToString(annotation);
			if(contents != null) {
				
				Integer contentsHash = contents.hashCode();
				annotationsMap.computeIfAbsent(contentsHash, s -> new TreeSet<String>());
				annotationsMap.get(contentsHash).add(annotation.getName());
			}
		}
		int counter = 1;
		for(Entry<Integer,Set<String>>me : annotationsMap.entrySet()) {
			
			System.out.println("\nGroup #" + Integer.toString(counter));

			for(String fn : me.getValue()) {
				System.out.println(fn);
			}
			counter++;
		}
	}
}
