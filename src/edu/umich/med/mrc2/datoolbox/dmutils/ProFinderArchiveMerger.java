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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.file.PathUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.utils.CompressionUtils;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class ProFinderArchiveMerger {
	
	private static Path tempFolder;
	private static Path tempMergeFolder;

	public static void main(String[] args) {

		File archiveOne = new File("Y:\\DataAnalysis\\CPDMatch\\EX01426\\RP-POS\\EX01426 - RP Pos - Batch11-custom PCDL-final-simple.pfa");
		File archiveTwo = new File("Y:\\DataAnalysis\\CPDMatch\\EX01426\\RP-POS\\EX01426 - RP Pos - Batch11-custom PCDL-missing-simple.pfa");
		File mergedArchive = new File("Y:\\DataAnalysis\\CPDMatch\\EX01426\\RP-POS\\EX01426-RP-POS-BATCH11-merged.pfa");
		mergeProFinderArchives(archiveOne, archiveTwo, mergedArchive);
	}
	
	private static void mergeProFinderArchives(
			File archiveOne, File archiveTwo, File mergedArchive) {
		
		//	Create temp folder to hold extracted archives
		tempFolder = Paths.get(mergedArchive.getParent(), "PFTEMP");
		tempMergeFolder = Paths.get(mergedArchive.getParent(), "PFTEMP", "MERGED");
		try {
			Files.createDirectories(tempMergeFolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<Path>cefListOne = new ArrayList<>();
		List<Path>cefListTwo = new ArrayList<>();
		if(tempFolder.toFile().exists()) {
			
			Path tempDirOnePath = Paths.get(tempFolder.toString(), PathUtils.getBaseName(archiveOne.toPath()));
			CompressionUtils.extractZip(archiveOne.toPath(), tempDirOnePath);
			cefListOne = FIOUtils.findFilesByExtension(tempDirOnePath, "cef");

			Path tempDirTwoPath = Paths.get(tempFolder.toString(), PathUtils.getBaseName(archiveTwo.toPath()));
			CompressionUtils.extractZip(archiveTwo.toPath(), tempDirTwoPath);
			cefListTwo = FIOUtils.findFilesByExtension(tempDirTwoPath, "cef");
			createMergedCefFiles(cefListOne, cefListTwo);
			
			List<Path>mergedCefs = FIOUtils.findFilesByExtension(tempMergeFolder, "cef");
			CompressionUtils.createZipFileFromMultipleFiles(mergedArchive.toPath(),mergedCefs);
			try {
				FIOUtils.deleteDirectory(tempFolder);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void createMergedCefFiles(List<Path> cefListOne, List<Path> cefListTwo) {
		
		if(cefListOne.isEmpty() || cefListTwo.isEmpty())
			return;
		
		if(cefListOne.size() != cefListTwo.size())
			return;
				
		for(Path cefOne : cefListOne) {
			String nameOne = cefOne.getFileName().toString();
			Path cefTwo = cefListTwo.stream().
					filter(p -> p.getFileName().toString().equalsIgnoreCase(nameOne)).
					findFirst().orElse(null);
			if(cefTwo != null) {
				mergeCefs(cefOne, cefTwo);
			}
		}
	}

	private static void mergeCefs(Path cefOne, Path cefTwo) {
		
		Document cefDocOne = XmlUtils.readXmlFile(cefOne.toFile());
		Document cefDocTwo = XmlUtils.readXmlFile(cefTwo.toFile());
		Element destination = cefDocOne.getRootElement().getChild("CompoundList");
		List<Element>featureTwoNodes = 
				cefDocTwo.getRootElement().getChild("CompoundList").getChildren("Compound");
		for(Element fe : featureTwoNodes) {
			Element copy = fe.clone();
			destination.addContent(copy);
		}
		Path mergedFilePath = Paths.get(tempMergeFolder.toString(), cefOne.getFileName().toString());
		XmlUtils.writePrettyPrintXMLtoFile(cefDocOne, mergedFilePath.toFile());
	}
}
