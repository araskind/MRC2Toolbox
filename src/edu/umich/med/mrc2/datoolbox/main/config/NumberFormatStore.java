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

package edu.umich.med.mrc2.datoolbox.main.config;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;

public class NumberFormatStore {

	private NumberFormatStore() {
			
	}
	
	public static NumberFormat getDefaultMZformat() {
		return new DecimalFormat("#.####");
	}
	
	public static NumberFormat getDefaultRTformat() {
		return new DecimalFormat("#.###");
	}
	
	public static NumberFormat getDefaultIntensityFormat() {
		return new DecimalFormat("#,###");
	}
	
	public static NumberFormat getIntegerFormat() {
		return new DecimalFormat("###");
	}
	
	public static NumberFormat getDecimalFormatWithPrecision(int numDecimalPlaces) {
		return new DecimalFormat("#." + "#".repeat(numDecimalPlaces));
	}
	
	public static NumberFormat getDefaultScientificFormat() {
		return new DecimalFormat("0.###E0");
	}
	
	public static DateFormat getDefaultTimeStampFormat() {		
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}
	
	public static DateFormat getDefaultFileNameTimeStampFormat() {		
		return new SimpleDateFormat("yyyyMMdd_HHmmss");
	}
	
}
