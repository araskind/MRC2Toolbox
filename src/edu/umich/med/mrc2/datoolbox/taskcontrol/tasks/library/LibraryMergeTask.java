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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class LibraryMergeTask extends AbstractTask {

	private Document targetLibrary, unknownsLibrary, mergedLibrary;
	private File targetLibraryFile, unknownLibraryFile;

	public LibraryMergeTask(File targetLibraryFileIn, File unknownLibraryFileIn) {

		super();

		targetLibraryFile = targetLibraryFileIn;
		unknownLibraryFile = unknownLibraryFileIn;

		total = 100;
		processed = 2;
		taskDescription = "Merging libraries for recursion ...";
	}

	@Override
	public Task cloneTask() {

		LibraryMergeTask clonedTask = new LibraryMergeTask(targetLibraryFile, unknownLibraryFile);

		return clonedTask;
	}

	private void mergeLibraries()
			throws IOException, XPathExpressionException, ParserConfigurationException, TransformerException {

		targetLibrary = null;
		unknownsLibrary = null;
		XPathExpression expr = null;
		NodeList targetNodes, unknownNodes;

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		targetLibrary = XmlUtils.readXmlFile(targetLibraryFile);
		unknownsLibrary = XmlUtils.readXmlFile(unknownLibraryFile);

		if (targetLibrary != null && unknownsLibrary != null) {

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

			mergedLibrary = dBuilder.newDocument();
			Element cefRoot = mergedLibrary.createElement("CEF");
			cefRoot.setAttribute("version", "1.0.0.0");
			mergedLibrary.appendChild(cefRoot);
			Element compoundListElement = mergedLibrary.createElement("CompoundList");
			cefRoot.appendChild(compoundListElement);

			expr = xpath.compile("//CEF/CompoundList/Compound");
			targetNodes = (NodeList) expr.evaluate(targetLibrary, XPathConstants.NODESET);
			unknownNodes = (NodeList) expr.evaluate(unknownsLibrary, XPathConstants.NODESET);

			processed = 0;
			total = targetNodes.getLength() + unknownNodes.getLength();

			// TODO eliminate formulas if necessary
			for (int i = 0; i < targetNodes.getLength(); i++) {

				compoundListElement.appendChild(mergedLibrary.importNode(targetNodes.item(i), true));
				processed++;
			}

			for (int i = 0; i < unknownNodes.getLength(); i++) {

				Element compound = (Element) mergedLibrary.importNode(unknownNodes.item(i), true);
				Element result = mergedLibrary.createElement("Results");
				Element molecule = mergedLibrary.createElement("Molecule");

				String cpdName = compound.getAttribute("mppid");

				if (!cpdName.startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName()))
					cpdName = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() + compound.getAttribute("mppid");

				molecule.setAttribute("name", cpdName);
				result.appendChild(molecule);
				compound.appendChild(result);
				compoundListElement.appendChild(compound);
				processed++;
			}
			TransformerFactory transfac = TransformerFactory.newInstance();
			Transformer transformer = transfac.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

			File mergedLibraryFile = new File(
					FilenameUtils.removeExtension(unknownLibraryFile.getAbsolutePath()) + "-4Recursion.cef");
			StreamResult result = new StreamResult(new FileOutputStream(mergedLibraryFile));
			DOMSource source = new DOMSource(mergedLibrary);
			transformer.transform(source, result);
			result.getOutputStream().close();
		}
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			mergeLibraries();
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}

}
