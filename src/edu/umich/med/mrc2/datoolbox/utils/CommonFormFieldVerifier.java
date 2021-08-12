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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonFormFieldVerifier {

	private static final String EMAILPATTERN = "^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
	private static Pattern pattern = Pattern.compile(EMAILPATTERN, Pattern.CASE_INSENSITIVE);
	
	public static boolean emailIsValid(String email) {
		
		Matcher m = pattern.matcher(email.toLowerCase().trim());			
		return m.matches();
	}
	
	public static boolean isPhoneValid(String phoneNumber) {
				
		String phoneNumberClean = phoneNumber.replaceAll("[^0-9]", "");	
		return phoneNumberClean.length() == 10;
	}
	
	public static String formatPhoneNumber(String phoneNumber) {	
		
		String phoneNumberClean = phoneNumber.replaceAll("[^0-9]", "").trim();
		if(phoneNumberClean.length() == 10)
			return phoneNumberClean.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1) $2-$3");
		else	//	return original if can not format
			return phoneNumber;
	}
}
