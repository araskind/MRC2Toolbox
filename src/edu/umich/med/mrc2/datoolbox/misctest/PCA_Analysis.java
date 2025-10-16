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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.io.File;
import java.io.IOException;

import org.ujmp.core.Matrix;

import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;

public class PCA_Analysis{

	public static void main(String[] arg){

    	//	Read in data
    	File dataFile = new File("E:\\Eclipse\\git2\\CefAnalyzer\\data\\projects\\_DATA_IN\\iris-numbers.txt");
		String[][] data = null;
		String delimiter = "\t";
		try {
			data = DelimitedTextParser.parseTextFileWithEncoding(dataFile, delimiter.charAt(0));
			double[][] doubleArray = Matrix.Factory.linkToArray(data).toDoubleArray();



			System.out.println();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
