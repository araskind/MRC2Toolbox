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

package edu.umich.med.mrc2.datoolbox.dbparse.load.mona;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.compress.utils.FileNameUtils;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.SDFWriter;

public class MonaSDFRewrite {

	public static void main(String[] args) {
		
		File sdfFile = new File(
				"E:\\DataAnalysis\\Databases\\MONA\\MoNA-export-All_LC-MS-MS_Agilent_QTOF-sdf\\"
				+ "MoNA-export-All_LC-MS-MS_Agilent_QTOF.sdf");
//		File sdfFile = new File(
//				"E:\\DataAnalysis\\Databases\\MONA\\MoNA-export-All_LC-MS-MS_Agilent_QTOF-sdf\\test.sdf");
		 try {
			rewriteMONAsdf(sdfFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void rewriteMONAsdf(File sdfFile) {
		
		List<List<String>> sdfChunks = new ArrayList<List<String>>();
		List<String> chunk = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = Files.newBufferedReader(Paths.get(sdfFile.getAbsolutePath()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			for (String line = null; (line = br.readLine()) != null;) {

				if (line.trim().equals(MonaParser.SDF_RECORD_SEPARATOR)) {
					sdfChunks.add(chunk);
					chunk = new ArrayList<String>();
					continue;
				} else
					chunk.add(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		Collection<IAtomContainer>molecules = new ArrayList<IAtomContainer>();
		Collection<IAtomContainer>badMsMolecules = new ArrayList<IAtomContainer>();
		for(List<String>sdfChunk : sdfChunks) {
			IAtomContainer mol = null;
			try {
				mol = MonaParser.parseChunk(sdfChunk);
			} catch (CDKException e) {
				e.printStackTrace();
			}
			if(mol != null) {
				if(mol.getProperty(MonaNameFields.PRECURSOR_MZ.getName()) == null
						|| mol.getProperty(MonaNameFields.ION_MODE.getName()) == null
						|| mol.getProperty(MonaNameFields.NUM_PEAKS.getName()) == null
						|| mol.getProperty(MonaParser.MSMS_ELEMENT) == null) {
					badMsMolecules.add(mol);
				}
				else {
					molecules.add(mol);
				}
			}
			else {
				System.out.println(Arrays.toString(chunk.toArray(new String[chunk.size()])));
				System.out.println("***");
			}
		}		
		File exportFile = Paths.get(sdfFile.getParentFile().getAbsolutePath(),
				FileNameUtils.getBaseName(sdfFile.getName()) + "_reexport.sdf").toFile();
		try {
			writeOutSDF(molecules, exportFile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(!badMsMolecules.isEmpty()) {
			
			File bmExportFile = Paths.get(sdfFile.getParentFile().getAbsolutePath(),
					FileNameUtils.getBaseName(sdfFile.getName()) + "_bad_MS.sdf").toFile();
			try {
				writeOutSDF(badMsMolecules, bmExportFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void writeOutSDF(
			Collection<IAtomContainer>molecules, File exportFile) throws IOException {
		
		FileWriter fWriter = new FileWriter(exportFile);
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));
		SDFWriter sdfWriter = new SDFWriter(writer);
		for(IAtomContainer mol : molecules) {
			try {
				sdfWriter.write(mol);
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sdfWriter.close();
		writer.close();
		fWriter.close();
	}
}











