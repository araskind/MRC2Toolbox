/*******************************************************************************
 *
 * (C) Copyright 2018-2026 MRC2 (http://mrc2.umich.edu).
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

public class DefaultFormatStore {

	public static final String MZ_FORMAT_DEFAULT = "#.####";
    public static final String RT_FORMAT_DEFAULT = "#.###";
    public static final String INTENSITY_FORMAT_DEFAULT = "#,###";
    public static final String SPECTRUM_INTENSITY_FORMAT_DEFAULT = "#.#";
    public static final String PPM_FORMAT_DEFAULT = "#.#";
	public static final String TIME_STAMP_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss";
	public static final String FILE_TIMESTAMP_FORMAT_DEFAULT = "yyyyMMdd_HHmmss";
	
	private DefaultFormatStore() {
			
	}
	
	public static NumberFormat getDefaultMZformat() {
		return new DecimalFormat(MZ_FORMAT_DEFAULT);
	}
	
	public static NumberFormat getDefaultRTformat() {
		return new DecimalFormat(RT_FORMAT_DEFAULT);
	}
	
	public static NumberFormat getDefaultIntensityFormat() {
		return new DecimalFormat(INTENSITY_FORMAT_DEFAULT);
	}
	
	public static NumberFormat getDefaultSpectrumIntensityFormat() {
		return new DecimalFormat(SPECTRUM_INTENSITY_FORMAT_DEFAULT);
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
	
	public static NumberFormat getDefaultPpmFormat() {
		return new DecimalFormat(PPM_FORMAT_DEFAULT);
	}
	
	public static DateFormat getDefaultTimeStampFormat() {		
		return new SimpleDateFormat(TIME_STAMP_FORMAT_DEFAULT);
	}
	
	public static DateFormat getDefaultFileNameTimeStampFormat() {		
		return new SimpleDateFormat(FILE_TIMESTAMP_FORMAT_DEFAULT);
	}
	
}
