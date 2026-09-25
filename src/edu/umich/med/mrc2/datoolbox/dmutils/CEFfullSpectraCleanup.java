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

package edu.umich.med.mrc2.datoolbox.dmutils;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class CEFfullSpectraCleanup {

	public static void main(String[] args) {

		File ceFolder = new File("Y:\\DataAnalysis\\_Reports\\EX01607 - Motrpac Tranche 5 Muscle\\"
				+ "A003 - Untargeted\\MFE\\NEG\\BATCH01");
		cleanCefFiles(ceFolder);
	}
	
	private static void cleanCefFiles(File ceFolder) {
		
		List<Path> cefFiles = FIOUtils.findFilesByExtension(ceFolder.toPath(), "cef");
		for(Path cp : cefFiles) {
			System.out.print(String.format("\nProcessing %s\n", cp.getFileName()));
			Document cefDocument = XmlUtils.readXmlFile(cp.toFile());
			removeFullSpectraElements(cefDocument);
			XmlUtils.writePrettyPrintXMLtoFile(cefDocument, cp.toFile());
		}
	}
	
	private static void removeFullSpectraElements(Document cefDocument) {

		List<Element>compoundNodes = 
				cefDocument.getRootElement().getChild("CompoundList").getChildren("Compound");
		int count = 0;
		for(Element compoundNode : compoundNodes) {
			
			List<Element>spectra = compoundNode.getChildren("Spectrum");
			for(Element spectrum : spectra) {
				if(spectrum.getAttributeValue("type").equals("TOF-MS1"))
					compoundNode.removeContent(spectrum);
				
				count++;
				if(count % 50 == 0)
					System.out.print(".");
				if(count % 5000 == 0)
					System.out.print("\n");
			}
		}
	}
}
