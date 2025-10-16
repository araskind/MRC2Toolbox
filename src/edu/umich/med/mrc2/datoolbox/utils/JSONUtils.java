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
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.json.JSONObject;

public class JSONUtils {

	public static JSONObject readJsonFromUrl(String url) {

		InputStream is = null;
		try {
			is = new URL(url).openStream();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			//	e1.printStackTrace();
			return null;
		}
		String jsonText = null;
		JSONObject json = null;
		if (is != null) {
			try {
				jsonText = IOUtils.toString(is, Charset.defaultCharset());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(jsonText == null || jsonText.isEmpty() || !jsonText.startsWith("{"))
				return null;
			
			json = new JSONObject(jsonText);
		}
		return json;
	}
	
	public static JSONObject readJsonFromFile(File jsonFile) {
		
		String jsString = null;
		try {
			jsString = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONObject json = null;
		if(jsString != null) {
			try {
				json = new JSONObject(jsString);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		return json;
	}
	
	public static void writeJSON2File(JSONObject json, File jsonFile) {
		
		String outputString = json.toString();
		Path outputPath = Paths.get(jsonFile.getAbsolutePath());
		try {
			Files.writeString(outputPath, 
					outputString, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static File[] getJsonFileList(File jsonFolder) {
		
		if(jsonFolder == null || !jsonFolder.exists())
			return null;
		
		FileFilter jsonFilter = new RegexFileFilter(".+\\.json$");
		return jsonFolder.listFiles(jsonFilter);
	}
}








