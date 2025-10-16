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

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
 
/**
 * @author Crunchify.com 
 * Best way to generate very secure random Password automatically
 * 
 * From https://crunchify.com/java-generate-strong-random-password-securerandom/
 */
 
public class CrunchifyRandomPasswordGenerator {
 
	//	SecureRandom() constructs a secure random number generator 
	//	(RNG) implementing the default random number algorithm.
	private SecureRandom crunchifyRandomNo = new SecureRandom();
 
	private ArrayList<Object> crunchifyValueObj;

	// Just initialize ArrayList crunchifyValueObj and add ASCII Decimal Values
	public CrunchifyRandomPasswordGenerator() {
 
		crunchifyValueObj = new ArrayList<>();
 
		// Adding ASCII Decimal value between 33 and 53
		for (int i = 33; i < 53; i++) {
			crunchifyValueObj.add((char) i);
		}
 
		// Adding ASCII Decimal value between 54 and 85
		for (int i = 54; i < 85; i++) {
			crunchifyValueObj.add((char) i);
		}
 
		// Adding ASCII Decimal value between 86 and 128
		for (int i = 86; i < 127; i++) {
			crunchifyValueObj.add((char) i);
		}
		crunchifyValueObj.add((char) 64);
 
		//	rotate() rotates the elements in the specified list 
		//	by the specified distance. This will create strong password
		Collections.rotate(crunchifyValueObj, 5);
	}
	
	public String getRandomPassword(int length) {
		
		StringBuilder crunchifyBuffer = new StringBuilder();
		for (int i = 0; i < length; i++) 
			crunchifyBuffer.append(crunchifyGetRandom());
		
		return crunchifyBuffer.toString();
	}
 
 
	// Get Char value from above added Decimal values
	// Enable Logging below if you want to debug
	private char crunchifyGetRandom() {
		char crunchifyChar = (char) this.crunchifyValueObj.get(crunchifyRandomNo.nextInt(this.crunchifyValueObj.size()));
 
		// log(String.valueOf(crunchifyChar));
		return crunchifyChar;
	}
	
	public static void main(String[] args) {
		CrunchifyRandomPasswordGenerator passwordGenerator = new CrunchifyRandomPasswordGenerator();

		log("Crunchify Password Generator Utility: \n");
		StringBuilder crunchifyBuffer = new StringBuilder();

		// Let's print total 8 passwords
		for (int loop = 1; loop <= 8; loop++) {
			// Password length should be 23 characters
			for (int length = 0; length < 23; length++) {
				crunchifyBuffer.append(passwordGenerator.crunchifyGetRandom());
			}

			log(loop, crunchifyBuffer.toString());
			crunchifyBuffer.setLength(0);
		}
	}
		
	// Simple log util
	private static void log(String string) {
		System.out.println(string);
 
	}
 
	// Simple log util
	private static void log(int count, String password) {
		System.out.println("Password sample " + count + ": " + password);
 
	}
 
}
