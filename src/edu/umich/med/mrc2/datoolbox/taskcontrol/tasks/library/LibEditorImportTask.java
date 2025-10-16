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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.time.FastDateFormat;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.LibraryEditorAdductDecoder;

public class LibEditorImportTask  extends AbstractTask {

	private static final FastDateFormat DATE_FORMATTER  =
			FastDateFormat.getInstance("yyyy-MM-dd'T'hh:mm:ss.SSSZZ",TimeZone.getDefault(), Locale.US);
	private File inputLibraryFile;
	private CompoundLibrary library;

	private LibraryEditorAdductDecoder libraryEditorAdductDecoder;

	public LibEditorImportTask(File inputFile, CompoundLibrary destination) {

		inputLibraryFile = inputFile;
		library = destination;
		taskDescription = "Importing Agilent LibEditor library from  " + inputFile.getName();
		libraryEditorAdductDecoder = new LibraryEditorAdductDecoder();

		total = 100;
		processed = 0;
	}

	@Override
	public Task cloneTask() {

		return new LibEditorImportTask(inputLibraryFile, library);
	}

	public CompoundLibrary getLibrary() {
		return library;
	}

	private void parseCompoundLibrary() throws Exception {

		taskDescription = "Parsing XML data ...";

		Document libEditorLibrary = null;
		XPathExpression acpExpr, specExpr, libExpr;
		NodeList compoundNodes, spectrumNodes, libNodes;

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		try {
			libEditorLibrary = readLibraryFile(inputLibraryFile);
		} catch (IOException e) {

			e.printStackTrace();
		}
		if(libEditorLibrary != null){

			libExpr = xpath.compile("//LibraryDataSet/Library");
			libNodes = (NodeList) libExpr.evaluate(libEditorLibrary, XPathConstants.NODESET);
			parseLibraryMetaData( (Element) libNodes.item(0));

			acpExpr = xpath.compile("//LibraryDataSet/Compound");
			specExpr = xpath.compile("//LibraryDataSet/Spectrum");

			compoundNodes = (NodeList) acpExpr.evaluate(libEditorLibrary, XPathConstants.NODESET);
			spectrumNodes = (NodeList) specExpr.evaluate(libEditorLibrary, XPathConstants.NODESET);

			HashMap<Integer, MassSpectrum>spectrumMap = parseSpectrumData(spectrumNodes);

			total = compoundNodes.getLength();
			processed = 0;

			for (int i = 0; i < compoundNodes.getLength(); i++) {

				Element cpdElement = (Element) compoundNodes.item(i);

				String name = cpdElement.getElementsByTagName("CompoundName").item(0).getFirstChild().getNodeValue();
				String formula = cpdElement.getElementsByTagName("Formula").item(0).getFirstChild().getNodeValue();
				String casId = cpdElement.getElementsByTagName("CASNumber").item(0).getFirstChild().getNodeValue();
				Integer cid = Integer.valueOf(cpdElement.getElementsByTagName("CompoundID").item(0).getFirstChild().getNodeValue());
				Date lastModified = new Date();
				try {
					lastModified = DATE_FORMATTER.parse(cpdElement.getElementsByTagName("LastEditDateTime").item(0).getFirstChild().getNodeValue());
				} catch (DOMException | ParseException e) {
					e.printStackTrace();
				}

				String rtString = "";
				double rt = 0.0d;
				if(cpdElement.getElementsByTagName("RetentionIndex").item(0) != null){

					rtString = cpdElement.getElementsByTagName("RetentionIndex").item(0).getFirstChild().getNodeValue();
					rt = Double.valueOf(rtString);
				}

				CompoundIdentity identity  = new CompoundIdentity(name, formula);
				identity.addDbId(CompoundDatabaseEnum.CAS, casId);

				double neutralMass = 0.0d;
				IMolecularFormula mf = null;
				try {
					mf = MolecularFormulaManipulator.getMolecularFormula(formula, DefaultChemObjectBuilder.getInstance());;
				} catch (Exception e) {
					//e.printStackTrace();
				}
				if(mf != null){

					neutralMass = MolecularFormulaManipulator.getMajorIsotopeMass(mf);
					LibraryMsFeature newTarget = new LibraryMsFeature(name, spectrumMap.get(cid), rt);
					newTarget.setDateCreated(library.getDateCreated());
					newTarget.setLastModified(lastModified);
					newTarget.setNeutralMass(neutralMass);

					//	TODO handle MSMS confidence level
					MsFeatureIdentity mid = new MsFeatureIdentity(identity, CompoundIdentificationConfidence.ACCURATE_MASS_RT);
					newTarget.setPrimaryIdentity(mid);
					library.addFeature(newTarget);
				}
			}
		}
	}

	private void parseLibraryMetaData(Element element) {

		Date dateCreated = new Date();
		Date lastModified = new Date();

		String description = element.getElementsByTagName("Description").item(0).getFirstChild().getNodeValue();

		try {
			dateCreated = DATE_FORMATTER.parse(element.getElementsByTagName("CreationDateTime").item(0).getFirstChild().getNodeValue());
		} catch (DOMException | ParseException e) {
			e.printStackTrace();
		}
		try {
			lastModified = DATE_FORMATTER.parse(element.getElementsByTagName("CreationDateTime").item(0).getFirstChild().getNodeValue());
		} catch (DOMException | ParseException e) {
			e.printStackTrace();
		}
		library.setLibraryDescription(description);
		library.setDateCreated(dateCreated);
		library.setLastModified(lastModified);
	}

	private HashMap<Integer, MassSpectrum> parseSpectrumData(NodeList spectrumNodes) {

		HashMap<Integer, MassSpectrum>spectrumMap = new HashMap<Integer, MassSpectrum>();


		for (int i = 0; i < spectrumNodes.getLength(); i++) {

			Element spectrumElement = (Element) spectrumNodes.item(i);
			Integer cid = Integer.valueOf(spectrumElement.getElementsByTagName("CompoundID").item(0).getFirstChild().getNodeValue());

			if(!spectrumMap.containsKey(cid))
				spectrumMap.put(cid, new MassSpectrum());

			String species = spectrumElement.getElementsByTagName("Species").item(0).getFirstChild().getNodeValue();
			Adduct mod = libraryEditorAdductDecoder.getModificationByName(species);
			String modName = "";

			if(mod != null)
				modName = mod.getName();

			String mzString = spectrumElement.getElementsByTagName("MzValues").item(0).getFirstChild().getNodeValue();
			String intensityString = spectrumElement.getElementsByTagName("AbundanceValues").item(0).getFirstChild().getNodeValue();

			//	Decode MZ
			byte[] decodedmz = Base64.getDecoder().decode(mzString);
			ByteBuffer byteBuf = ByteBuffer.wrap(decodedmz);
			byteBuf.order( ByteOrder.LITTLE_ENDIAN );
			DoubleBuffer dbuff = byteBuf.asDoubleBuffer();
			double[] mzValues = new double[dbuff.remaining()];
			dbuff.get(mzValues);

			dbuff.clear();
			byteBuf.clear();

			// Decode intensity
			byte[] decodedintensity = Base64.getDecoder().decode(intensityString);
			byteBuf = ByteBuffer.wrap(decodedintensity);
			byteBuf.order( ByteOrder.LITTLE_ENDIAN );
			dbuff = byteBuf.asDoubleBuffer();
			double[] intensityValues = new double[dbuff.remaining()];
			dbuff.get(intensityValues);

			ArrayList<MsPoint>points = new ArrayList<MsPoint>();

			for (int j = 0; j < mzValues.length; j++){

				MsPoint newPoint = new MsPoint(mzValues[j], intensityValues[j], modName);
				points.add(newPoint);
			}
			spectrumMap.get(cid).addSpectrumForAdduct(mod, points);
		}
		return spectrumMap;
	}

	private Document readLibraryFile(File file) throws IOException {

		Document libEditorLibrary = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			libEditorLibrary = dBuilder.parse(file);
		} catch (Exception e) {

			throw new IOException(e);
		}
		return libEditorLibrary;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);

		if (inputLibraryFile != null) {

			if (inputLibraryFile.exists()) {
				try {
					parseCompoundLibrary();
					writeFeaturesToDatabase();
					updateLibraryInfo();
				}
				catch (Exception e) {

					e.printStackTrace();
					setStatus(TaskStatus.ERROR);
return;

				}
			}
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void updateLibraryInfo() {
		try {
			MSRTLibraryUtils.updateLibraryInfo(library);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void writeFeaturesToDatabase() {

		taskDescription = "Writing library to database ...";

		total = library.getFeatures().size();
		processed = 0;
		String libId = library.getLibraryId();

		for(MsFeature lt : library.getFeatures()){

			try {
				MSRTLibraryUtils.loadLibraryFeature( (LibraryMsFeature) lt, libId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			processed++;
		}
	}



}
