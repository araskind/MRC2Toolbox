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

public enum DocumentFormat {

	CEF("Agilent Compound Exchange Format","application/xml"),
	CML("Chemical markup language","application/xml"),
	CSV("Comma-separated values","text/csv"),
	DOC("Microsoft Word (old format)","application/msword"),
	DOCX("Microsoft Word","application/msword"),
	GIF("Graphics Interchange Format image","image/gif"),
	JPEG("Joint Photographic Experts Group image","image/jpeg"),
	JPG("Joint Photographic Experts Group image","image/jpeg"),
	PDF("Adobe Portable Document Format","application/pdf"),
	PHP("PHP script","text/plain"),
	PNG("Portable Network Graphics image","image/png"),
	PPT("Microsoft PowerPoint (old format)","application/powerpoint"),
	PPTX("Microsoft PowerPoint ","application/powerpoint"),
	R("R script","text/plain"),
	RTF("Rich text format document","application/rtf"),
	SVG("Scalable Vector Graphics document","image/svg+xml"),
	TXT("Plain text","text/plain"),
	TIFF("Tagged Image File Format image","image/tiff"),
	XLS("Microsoft Excel (old format)","application/excel"),
	XLSX("Microsoft Excel","application/excel"),
	XML("Extensible Markup Language","text/xml"),
	ZIP("ZIP archive","application/zip"),
	UNK("Unknown format","application/unk"),
	;

	private final String uiName;
	private final String mime;

	DocumentFormat(String uiName, String mime) {

		this.uiName = uiName;
		this.mime = mime;
	}

	public static DocumentFormat getFormatByFileExtension(String extension) {

		for(DocumentFormat f : DocumentFormat.values()) {
			if(f.name().equalsIgnoreCase(extension))
				return f;
		}
		return DocumentFormat.UNK;
	}

	public String getMime() {
		return mime;
	}

	public String getName() {
		return uiName;
	}

	@Override
	public String toString() {
		return uiName;
	}
}
