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

package edu.umich.med.mrc2.datoolbox.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

public class RawDataUploadUtils {

	public static void deleteResultsFolders(String dataDirectoryPath) {

		try {
			Files.find(Paths.get(dataDirectoryPath),
			Integer.MAX_VALUE, (filePath, fileAttr) -> (filePath.toString().toLowerCase().endsWith(".d")))
	        	.forEach(path -> {
					try {
						FileUtils.deleteDirectory(Paths.get(path.toString(), "Results").toFile());
					} catch (IOException e) {
						e.printStackTrace();
					}
			});
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void zipIndividualRawDataFiles(String dataDirectoryPath, String destinationDirectoryPath) {

		File zipDir = null;
		try {
			zipDir = createOrRetrieveDirectory(destinationDirectoryPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(zipDir != null) {

/*			Path sourcePath = Paths.get(dataDirectoryPath);
			Path destinationPath = Paths.get(destinationDirectoryPath);
			Path rel = sourcePath.relativize(destinationPath);*/

			Runtime runtime = Runtime.getRuntime();
			String command =
				"for /D %d in (\"" + dataDirectoryPath + File.separator +
				"*.*\") do 7z a -tzip \"" + destinationDirectoryPath + File.separator +"%~nd.zip\" \"%d\"";

			System.out.println(command);
			try {
				Process process = runtime.exec(command);
				process.waitFor();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (InterruptedException e2) {
				e2.printStackTrace();
			}
		}
	}

	public static void calculateChecksums(String zipDirectoryPath) {

		Runtime runtime = Runtime.getRuntime();
		String command = "7z h -scrcsha256 \"" + zipDirectoryPath + File.separator + "*\" > checksum.txt";
		System.out.println(command);
		try {
			Process process = runtime.exec(command);
			process.waitFor();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e2) {
			e2.printStackTrace();
		}
	}

	/**
	 * Creates a File if the file does not exist, or returns a
	 * reference to the File if it already exists.
	 */
	public static File createOrRetrieveDirectory(final String pathToDir) throws IOException{

	    final Path path = Paths.get(pathToDir);

	    if(Files.notExists(path))
	        return Files.createDirectories(path).toFile();

	    return path.toFile();
	}
}
