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

package edu.umich.med.mrc2.datoolbox.data.enums;

public enum MsLibraryFormat {

	CEF("Compound Exchange Format (XML)", "cef"),
	MSP("NIST format", "msp"),
	IDTRACKER("IDTracker XML format","idtlib"),
	TSV("Tab-separated text", "txt"),
	SIRIUS_MS("Sirius MS file", "ms"),
	MGF("MGF file", "mgf"),
	XY_META_MGF("XY-meta MGF file", "mgf"),
	;

	private final String uiName;
	private final String fileExtension;

	MsLibraryFormat(String uiName, String extension) {

		this.uiName = uiName;
		this.fileExtension = extension;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}

	public static MsLibraryFormat getFormatByName(String name) {

		for(MsLibraryFormat f : MsLibraryFormat.values()) {
			if(f.name().equals(name))
				return f;
		}
		return null;
	}
	
	public static MsLibraryFormat getFormatByDescription(String description) {

		for(MsLibraryFormat f : MsLibraryFormat.values()) {
			if(f.getName().equalsIgnoreCase(description))
				return f;
		}
		return null;
	}
	
	public static MsLibraryFormat getFormatByExtension(String extension) {

		for(MsLibraryFormat f : MsLibraryFormat.values()) {
			if(f.getFileExtension().equalsIgnoreCase(extension))
				return f;
		}
		return null;
	}
}
