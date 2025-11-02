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

package edu.umich.med.mrc2.datoolbox.dmutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.output.XMLOutputter;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class MZMLBatchRenamer {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		File sourceDirectory = 
				new File("Y:\\DataAnalysis\\4Demo\\EX01533 - best group separation\\MZML\\RP-POS");
		File renameMapFile = new File(
				"Y:\\DataAnalysis\\4Demo\\EX01533 - best group separation\\MZML\\RP-POS\\rename_map_no_groups.txt");
		batchMZMLFileRename(sourceDirectory, renameMapFile);
	}

	private static void batchMZMLFileRename(
			File sourceDirectory, 
			File renameMapFile) {		
		
		String[][] renameMapping = DelimitedTextParser.parseTextFile(
				renameMapFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		Map<String,String>fileNameMap = new TreeMap<>();
		for(int i=0; i<renameMapping.length; i++)
			fileNameMap.put(renameMapping[i][0], renameMapping[i][1]);
		
		//List<Path> filesToRename = FIOUtils.findFilesByExtension(sourceDirectory.toPath(), "mzml");
		File[] filesToRename = 
				sourceDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".mzml"));
		
		if(filesToRename.length == 0 || fileNameMap.isEmpty())
			return;
		
		Path renamedDir = null;
		try {
			renamedDir = Files.createDirectories(
					Paths.get(sourceDirectory.getAbsolutePath(), "RENAMED"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(renamedDir == null || !renamedDir.toFile().exists())
			return;
			
		for(File mzmlFile : filesToRename) {
			
			String newFileName = fileNameMap.get(mzmlFile.getName().toString());
			if(newFileName == null) {
				System.out.println("No replacement found for " + mzmlFile.getName().toString());
				continue;
			}
			Path newPath = Paths.get(mzmlFile.getParent().toString(), "RENAMED", newFileName);
			System.out.println(newPath.toString());		
			boolean renamed = false;
			try {
				renamed = renameMzMlInSampleInfo(mzmlFile, newFileName);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(renamed) {
				try {
					Files.move(mzmlFile.toPath(), newPath);
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}	
		}
	}
	
	private static boolean renameMzMlInSampleInfo(File inputMzMlFile, String newName) throws Exception {

		Document sampleInfo = XmlUtils.readXmlFile(inputMzMlFile);
		if (sampleInfo != null) {
			
			Namespace ns = sampleInfo.getRootElement().getNamespace();
			List<Element> fieldNodes = new ArrayList<>();
			try {
				fieldNodes = sampleInfo.getRootElement().getChild("mzML", ns).getChild("fileDescription", ns)
						.getChild("sourceFileList", ns).getChildren("sourceFile", ns);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (fieldNodes.isEmpty())
				return false;

			for (Element fieldElement : fieldNodes)
				fieldElement.setAttribute("location", newName);

			XMLOutputter xmlOutputter = new XMLOutputter();
			try (FileOutputStream fileOutputStream = new FileOutputStream(inputMzMlFile)) {
				xmlOutputter.output(sampleInfo, fileOutputStream);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}
}
