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

package edu.umich.med.mrc2.datoolbox.motrpac;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.utils.CompressionUtils;

public class MoTrPACRawFileCompressor {

	public static void main(String[] args) {
		
		if(args.length != 3) {
			System.err.println("Wrong numgber pf arguments.");
			return;
		}
		File fileList = new File(args[0]);
		File dirList = new File(args[1]);
		File destinationFolder = new File(args[2]);
		
		if(fileList == null || !fileList.exists()) {
			System.err.println("Raw data file list not found.");
			return;
		}
		if(dirList == null || !dirList.exists()) {
			System.err.println("Raw data source directories list not found.");
			return;
		}
		if(destinationFolder == null || !destinationFolder.exists()) {
			System.err.println("Destination directory not found.");
			return;
		}
		compressRawData(fileList,  dirList,  destinationFolder);
	}
	
	private static void compressRawData(File fileList, File dirList, File destinationFolder) {
		
		List<String> includeFileList = new ArrayList<String>();
		Path includeFileListPath = Paths.get(fileList.getAbsolutePath());
		try {
			includeFileList = Files.readAllLines(includeFileListPath);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		List<String>sourceFolderList = new ArrayList<String>();
		Path sourceFolderListFilePath = Paths.get(dirList.getAbsolutePath());
		try {
			sourceFolderList = Files.readAllLines(sourceFolderListFilePath);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		compressRawDataFromMutibatchRun(includeFileList, sourceFolderList, destinationFolder);
	}

	private static void compressRawDataFromMutibatchRun(
			List<String> includeFileList, 
			List<String>sourceFolders, 
			File destinationFolder) {
		
		Path destinationPath = Paths.get(destinationFolder.getAbsolutePath());
		for(String rawDataDirectory : sourceFolders) {
			
			Path sourcePath = Paths.get(rawDataDirectory);
			List<Path> pathList = new ArrayList<Path>();
			try {
				pathList = Files.find(sourcePath,
						1, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".d"))).
					collect(Collectors.toList());
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			for(Path rdp : pathList) {
				
				String rawFileName = FilenameUtils.getBaseName(rdp.toString());
				if(includeFileList.contains(rawFileName)) {
					
					try {
						FileUtils.deleteDirectory(Paths.get(rdp.toString(), "Results").toFile());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					File destination = Paths.get(destinationPath.toString(),
							FilenameUtils.getBaseName(rdp.toString()) + ".zip").toFile();
					try {
						CompressionUtils.zipFolder(rdp.toFile(), destination);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ArchiveException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}								
				}
			}
		}		
	}
}
