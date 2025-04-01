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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.mozilla.universalchardet.UniversalDetector;

import com.Ostermiller.util.BadDelimiterException;
import com.Ostermiller.util.CSVParser;

public class DelimitedTextParser {

	public static final char TAB_DELIMITER = '\t';
	
	public static String[][] parseTextFile(File fileToParse, char delimiter) {

		String[][] parsedFile = null;
		FileReader dbFileReader = null;
		//StandardCharsets.UTF_8

		if (fileToParse.exists()) {

			try {
				dbFileReader = new FileReader(fileToParse);
				parsedFile = CSVParser.parse(dbFileReader, delimiter);
			} catch (FileNotFoundException e) {

				e.printStackTrace();
			} catch (BadDelimiterException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
		}
		return parsedFile;
	}

	public static String getEncoding(File fileToParse) {

		String encoding = null;
		InputStream inputStream = null;
		UniversalDetector detector = new UniversalDetector(null);
		int nread;
		byte[] buf = new byte[4096];

		try {
			inputStream = new FileInputStream(fileToParse);

			while ((nread = inputStream.read(buf)) > 0 && !detector.isDone())
				detector.handleData(buf, 0, nread);

			inputStream.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		detector.dataEnd();
		encoding = detector.getDetectedCharset();

		if(encoding == null)
			encoding = "UTF-8";

		return encoding;
	}

	public static String[][] parseTextFileWithEncoding(File fileToParse, char delimiter) throws IOException {

		String[][] parsedFile = null;
		InputStream inputStream = new FileInputStream(fileToParse);
		String encoding = DelimitedTextParser.getEncoding(fileToParse);
		Reader inputStreamReader = new InputStreamReader(inputStream, encoding);

		if (fileToParse.exists()) {

			try {
				parsedFile = CSVParser.parse(inputStreamReader, delimiter);
				inputStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (BadDelimiterException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return parsedFile;
	}

	public static String[][] parseTextFileWithEncodingSkippingComments(
			File fileToParse, char delimiter, String commentDelims) throws IOException {

		String[][] parsedFile = null;
		InputStream inputStream = new FileInputStream(fileToParse);
		String encoding = DelimitedTextParser.getEncoding(fileToParse);
		Reader inputStreamReader = new InputStreamReader(inputStream, encoding);
		if (fileToParse.exists()) {
			try {
				parsedFile = CSVParser.parse(inputStreamReader, delimiter, "", "", commentDelims);
				inputStream.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (BadDelimiterException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return parsedFile;
	}
}
