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

package edu.umich.med.mrc2.datoolbox.dmutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class AgilentBatchFileRenamer {

	public static void main(String[] args) {
		
		File sourceDirectory = 
				new File("E:\\_Downloads\\_2_rename\\EX01526\\EX01526-RP-POS-BATCH11");
		File renameMapFile = new File(
				"E:\\_Downloads\\_2_rename\\EX01526\\EX01526-RP-POS-BATCH11\\rename_map.txt");
		batchDFileRename(sourceDirectory, renameMapFile);
	}	
	
	private static void batchDFileRename(
			File sourceDirectory, 
			File renameMapFile) {		
		
		IOFileFilter dotDfilter = 
				FileFilterUtils.makeDirectoryOnly(new RegexFileFilter(".+\\.[dD]$"));
		Collection<File> dotDfiles = FileUtils.listFilesAndDirs(
				sourceDirectory,
				DirectoryFileFilter.DIRECTORY,
				dotDfilter);

		String[][] renameMapping = DelimitedTextParser.parseTextFile(
				renameMapFile, MRC2ToolBoxConfiguration.getTabDelimiter());
		Map<String,String>fileNameMap = new TreeMap<>();
		for(int i=0; i<renameMapping.length; i++)
			fileNameMap.put(renameMapping[i][0], renameMapping[i][1]);
			
		for(File ddf : dotDfiles) {
			
			String newFileName = fileNameMap.get(ddf.getName());
			if(newFileName == null) {
				System.out.println("No replacement found for " + ddf.getName());
				continue;
			}
			String newName = ddf.getAbsolutePath().replace(ddf.getName(), newFileName);
			boolean sInfoRenamed = false;
			try {
				sInfoRenamed = renameDFileInSampleInfo(ddf, newName);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(sInfoRenamed) {
				
				Path source = Paths.get(ddf.getAbsolutePath());
				Path destination = Paths.get(
						sourceDirectory.getAbsolutePath(), "RENAMED", newFileName);
				try {
					Files.move(source, destination);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}			
		}
	}
	
	private static boolean renameDFileInSampleInfo(
			File inputDFile, String newName) throws Exception {

		File sampleInfoFile = 
				Paths.get(inputDFile.getAbsolutePath(), "AcqData", "sample_info.xml").toFile();
		if (!sampleInfoFile.setWritable(true)) {
			System.err.println("Failed to re-enable writing on file.");
			return false;
		}
		if (sampleInfoFile.exists()) {

			Document sampleInfo = XmlUtils.readXmlFile(sampleInfoFile);
			if (sampleInfo != null) {

				List<Element> fieldNodes = sampleInfo.getRootElement().getChildren("Field");
				for (Element fieldElement : fieldNodes) {

					if (fieldElement.getChildText("Name").trim().equals("Data File")) {
						fieldElement.getChild("Value").setText(newName);
						break;
					}
				}
				XMLOutputter xmlOutputter = new XMLOutputter();
				try (FileOutputStream fileOutputStream = new FileOutputStream(sampleInfoFile)) {
					xmlOutputter.output(sampleInfo, fileOutputStream);
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
				return true;
			}			
		}
		return false;
	}
}


