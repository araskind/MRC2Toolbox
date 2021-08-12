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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;

public class SerumCpdExtract {

	public static void main(String[] args) {

		TreeSet<String>properties = new TreeSet<String>();

		File sdfFile = new File("E:\\DataAnalysis\\Databases\\MINE\\KEGG\\KEGG_MINE.SDF");
		IteratingSDFReader reader;
		try {
			reader = new IteratingSDFReader(new FileInputStream(sdfFile), DefaultChemObjectBuilder.getInstance());

			while (reader.hasNext()) {
				IAtomContainer molecule = (IAtomContainer)reader.next();
				properties.addAll(molecule.getProperties().keySet().stream().map(String.class::cast).collect(Collectors.toList()));
				//	System.out.println(molecule.toString());
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		properties.stream().sorted().forEach(p -> System.out.println(p));

/*		String idField ="HMDB_ID";
		try {
			FileWriter writer = new FileWriter(new File("E:\\DataAnalysis\\Databases\\HMDB\\idList.txt"));
			File inputFile = new File("E:\\DataAnalysis\\Databases\\HMDB\\serum_metabolites_structures.sdf");
			IteratingSDFReader reader = new IteratingSDFReader(new FileInputStream(inputFile), DefaultChemObjectBuilder.getInstance());
			while (reader.hasNext()) {
				IAtomContainer molecule = (IAtomContainer)reader.next();
				writer.append(molecule.getProperties().get(idField).toString() + "\n");
			}
			writer.close();
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("List completed");*/
	}
}
