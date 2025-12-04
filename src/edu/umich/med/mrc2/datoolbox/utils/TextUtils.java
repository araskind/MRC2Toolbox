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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class TextUtils {
	
	private static final DecimalFormat sciFormatter = new DecimalFormat("0.###E0");
	private static final DecimalFormat threeDigitsFormatter = new DecimalFormat("###.###");
	
	public static String getCapitalForNumber(int i) {

		String ch = null;

		if (i > 0 && i < 27)
			ch = String.valueOf((char) (i + 'A' - 1));

		return ch;
	}

	public static String implode(String glue, Collection<String> pieces) {

		String output = "";

		// Remove empty values
		pieces.removeAll(Arrays.asList("", null));

		if (pieces.size() == 0)
			return output;

		if (pieces.size() == 1)
			return pieces.iterator().next();

		if (pieces.size() > 1) {
			StringBuilder builder = new StringBuilder();
			builder.append(pieces.remove(0));

			for (String s : pieces) {
				builder.append(glue);
				builder.append(s);
			}
			output = builder.toString();
		}
		return output;
	}

	public static String implode(String glue, String[] pieces) {

		List<String> toImplode = Arrays.asList(pieces);

		String output = TextUtils.implode(glue, toImplode);

		return output;
	}

	public static int letterToNumber(char letter) {

		int number = 0;
		int temp = (int) letter;

		// Lower case
		if (temp <= 122 & temp >= 97)
			number = temp - 96;

		// Upper case
		if (temp <= 90 & temp >= 65)
			number = temp - 64;

		return number;
	}

	/**
	 * Reads a line of text from a given input stream or null if the end of the
	 * stream is reached.
	 */
	public static String readLineFromStream(InputStream in) throws IOException {
		byte buf[] = new byte[1024];
		int pos = 0;
		while (true) {
			int ch = in.read();
			if ((ch == '\n') || (ch < 0))
				break;
			buf[pos++] = (byte) ch;
			if (pos == buf.length)
				buf = Arrays.copyOf(buf, pos * 2);
		}
		if (pos == 0)
			return null;

		return new String(Arrays.copyOf(buf, pos), "UTF-8");
	}

	/**
	 * Wraps the words of the given (long) text to several lines of maximum
	 * given length
	 */
	public static String wrapText(String text, int len) {

		// return text if less than length
		if (text.length() <= len)
			return text;

		StringBuffer result = new StringBuffer();
		StringBuffer line = new StringBuffer();
		StringBuffer word = new StringBuffer();

		char[] chars = text.toCharArray();

		for (int i = 0; i < chars.length; i++) {
			word.append(chars[i]);

			if (chars[i] == ' ') {
				if ((line.length() + word.length()) > len) {
					if (result.length() != 0)
						result.append("\n");
					result.append(line.toString());
					line.delete(0, line.length());
				}

				line.append(word);
				word.delete(0, word.length());
			}
		}

		// handle any extra chars in current word
		if (word.length() > 0) {
			if ((line.length() + word.length()) > len) {
				if (result.length() != 0)
					result.append("\n");
				result.append(line.toString());
				line.delete(0, line.length());
			}
			line.append(word);
		}

		// handle extra line
		if (line.length() > 0) {
			result.append("\n");
			result.append(line.toString());
		}

		return result.toString();
	}

	public static String escapeForSqlLike(String stringToEscape, char escapeChar) {

		return stringToEscape
			    .replace("!", escapeChar + "!")
			    .replace("%", escapeChar + "%")
			    .replace("_", escapeChar + "_")
			    .replace("[", escapeChar + "[")
			    .replace("]", escapeChar + "]");
	}

	public static List<String> readFileInList(String filePath) {

		List<String> lines = Collections.emptyList();
		try {
			lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

	public static List<String>readTextFileToList(String filePath){

		List<String> list = new ArrayList<>();
		try (BufferedReader br = Files.newBufferedReader(
				Paths.get(filePath), StandardCharsets.UTF_8)) {
			list = br.lines().collect(Collectors.toList());
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public static String formatDoubleWithSwitchToScientificNotation(double numValue) {
		
		if(numValue < 0.001d || numValue > 1000.d)
			return sciFormatter.format(numValue);
		else
			return threeDigitsFormatter.format(numValue);
	}
	
	public static String trimAndJoinStringCollectionForPreferences(Collection<String>parts, String joinString) {
		
		StringBuilder sb = new StringBuilder();
		int charCount = 0;
		int joinLength = joinString.length();
		boolean dataAdded = false;
		
		for(String part : parts) {
			
			charCount += part.length() + joinLength;
			if(charCount < Preferences.MAX_VALUE_LENGTH) {
				
				if(dataAdded)
					sb.append(joinString);
				
				sb.append(part);
				dataAdded = true;
			}			
		}		
		return sb.toString();
	}	
	
    public static boolean isValidRegex(String regex) {
        try {
            Pattern.compile(regex);
            return true; // Regex is syntactically valid
        } catch (PatternSyntaxException e) {
            return false; // Regex is not syntactically valid
        }
    }
}






















