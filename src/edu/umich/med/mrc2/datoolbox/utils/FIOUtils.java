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

package edu.umich.med.mrc2.datoolbox.utils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class FIOUtils {

	public static File changeExtension(File f, String newExtension) {

		if(FilenameUtils.getExtension(f.getPath()).equals(newExtension))
			return f;

		String newFileName =
				FilenameUtils.getFullPath(f.getAbsolutePath()) +
				FilenameUtils.getBaseName(f.getName()) +
				"." + newExtension;

		return new File(newFileName);
	}
	
	public static String calculateFileChecksum(String filePath) {
		
		try (InputStream is = Files.newInputStream(Paths.get(filePath))) {
		    return DigestUtils.md5Hex(is);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String calculateFileChecksum(File file) {
		
		try (InputStream is = Files.newInputStream(Paths.get(file.getAbsolutePath()))) {
		    return DigestUtils.md5Hex(is);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getTimestamp() {
		return MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
	}
	
	public static File getFileForLocation(String location) {
		
		if(location == null || location.trim().isEmpty())
			return null;
		
		Path filePath = null;
		try {
			filePath = Paths.get(location);
		} catch (Exception e) {
			System.out.println("File at " + location + " was not found.");
		}
		if(filePath != null) {
			
			File fileToReturn = filePath.toFile();
			if(fileToReturn.exists())
				return fileToReturn;
		}		
		return null;
	}
}
