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

package edu.umich.med.mrc2.datoolbox.data.enums;

public enum ImageExportFormat {

	PNG("PNG files", "png"),
	JPG("JPEG files", "jpg"),
	PDF("PDF files", "pdf"),
	SVG("SVG files", "svg");

	private final String uiName;
	private final String extension;

	ImageExportFormat(String uiName, String extension) {

		this.uiName = uiName;
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}

	public String getName() {
		return uiName;
	}
		
	public static ImageExportFormat getOptionByName(String name) {
		
		for(ImageExportFormat u : ImageExportFormat.values()) {
			
			if(u.name().equals(name))
				return u;				
		}		
		return null;
	}
	
	public static ImageExportFormat getOptionByUIName(String sname) {
		
		for(ImageExportFormat v : ImageExportFormat.values()) {
			
			if(v.getName().equals(sname))
				return v;
		}		
		return null;
	}
}
