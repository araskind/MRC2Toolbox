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
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentDatabaseFields;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.CefUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.InformationDialog;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.LibraryUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class ConvertCefLibraryForRecursionTask extends AbstractTask {

	private File outputLibraryFile, sourceLibraryFile;
	private Document exportedLibraryDocument;
	private boolean combineAdducts;
	private List<LibraryMsFeature> exportTargetList;
	private TreeSet<String> unmatchedAdducts;

	private static final String lineSeparator = System.getProperty("line.separator");

	public ConvertCefLibraryForRecursionTask(			
			File sourceLibraryFile,
			File outputLibraryFile,
			boolean combineAdducts) {

		this.sourceLibraryFile = sourceLibraryFile;
		this.outputLibraryFile = outputLibraryFile;
		this.combineAdducts = combineAdducts;
		unmatchedAdducts = new TreeSet<String>();
	}

	@Override
	public void run() {

		taskDescription = "Creating library for recursion from " + sourceLibraryFile.getName();
		setStatus(TaskStatus.PROCESSING);
		createTargetList();
		if(!unmatchedAdducts.isEmpty()){
			setStatus(TaskStatus.ERROR);
			return;
		}
		total = exportTargetList.size();
		processed = 0;
		try {
			writeCefLibrary();
		} catch (Exception e) {
			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
		setStatus(TaskStatus.FINISHED);
	}

	@Override
	public Task cloneTask() {

		return new ConvertCefLibraryForRecursionTask(
				sourceLibraryFile,
				outputLibraryFile,
				combineAdducts);
	}

	private Element createCefElement(LibraryMsFeature lt, Adduct adduct, boolean createNewId) {

		Element compound = (Element) exportedLibraryDocument.createElement("Compound");

		String mass = MRC2ToolBoxConfiguration.getMzFormat().format(lt.getNeutralMass());
		String rt = MRC2ToolBoxConfiguration.getRtFormat().format(lt.getRetentionTime());
		compound.setAttribute("mppid", mass + "_" + rt);
		compound.setAttribute("algo", "FindByFormula");

		//	Location
		Element location = exportedLibraryDocument.createElement("Location");
		location.setAttribute("m", mass);
		location.setAttribute("rt", rt);
		double area = 100.0d;
		location.setAttribute("a", Double.toString(area));
		compound.appendChild(location);

		//	Identification
		Element results = exportedLibraryDocument.createElement("Results");
		Element molecule = exportedLibraryDocument.createElement("Molecule");

		String compoundName = "";
		if(lt.getPrimaryIdentity() != null)
			compoundName = lt.getPrimaryIdentity().getName();
		else
			compoundName = lt.getName();

		molecule.setAttribute("name", compoundName);
		Element database = exportedLibraryDocument.createElement("Database");
		Element libId = exportedLibraryDocument.createElement("Accession");
		libId.setAttribute("db", "CAS ID");
		if(createNewId)
			libId.setAttribute("id", DataPrefix.MS_FEATURE.getName() + UUID.randomUUID().toString());
		else
			libId.setAttribute("id", lt.getId());
		
		database.appendChild(libId);

		if(lt.getPrimaryIdentity() != null){

			Element accession = exportedLibraryDocument.createElement("Accession");
			accession.setAttribute("db", "KEGG ID");
			accession.setAttribute("id", lt.getPrimaryIdentity().getPrimaryLinkLabel());
			database.appendChild(accession);
		}
		molecule.appendChild(database);
		results.appendChild(molecule);
		compound.appendChild(results);

		//	Spectrum
		Element spectrum = exportedLibraryDocument.createElement("Spectrum");
		spectrum.setAttribute("type", "FbF");
		spectrum.setAttribute("cpdAlgo", "FindByFormula");

		Element msDetails = exportedLibraryDocument.createElement("MSDetails");
		String sign = (lt.getPolarity().equals(Polarity.Positive)) ? "+" : "-";
		msDetails.setAttribute("p", sign);
		spectrum.appendChild(msDetails);

		Element msPeaks = exportedLibraryDocument.createElement("MSPeaks");

		if(adduct != null){

			String charge = Integer.toString(adduct.getCharge());
			MsPoint[] adductMs = lt.getSpectrum().getMsForAdduct(adduct);

			for(int i=0; i<adductMs.length; i++){

				Element peak = exportedLibraryDocument.createElement("p");
				peak.setAttribute("x", MRC2ToolBoxConfiguration.getMzFormat().format(adductMs[i].getMz()));
				peak.setAttribute("y", Double.toString(adductMs[i].getIntensity()));
				peak.setAttribute("z", charge);
				
				String aname = adduct.getCefNotation();
				if(i>0)
					aname = aname + "+" + i;

				peak.setAttribute("s", aname);
				msPeaks.appendChild(peak);
			}
		}
		else{
			for(Adduct ad : lt.getSpectrum().getAdducts()){

				String charge = Integer.toString(ad.getCharge());
				MsPoint[] adductMs = lt.getSpectrum().getMsForAdduct(ad);

				for(int i=0; i<adductMs.length; i++){

					Element peak = exportedLibraryDocument.createElement("p");
					peak.setAttribute("x", MRC2ToolBoxConfiguration.getMzFormat().format(adductMs[i].getMz()));
					peak.setAttribute("y", Double.toString(adductMs[i].getIntensity()));
					peak.setAttribute("z", charge);

					String aname = ad.getCefNotation();

					if(i>0)
						aname = aname + "+" + i;

					peak.setAttribute("s", aname);
					msPeaks.appendChild(peak);
				}
			}
		}
		spectrum.appendChild(msPeaks);
		compound.appendChild(spectrum);
		return compound;
	}

	private void createTargetList() {

		exportTargetList = new ArrayList<LibraryMsFeature>();
		if(sourceLibraryFile != null){
			try {
				readTargetsFromFile();
			} catch (Exception e) {
				setStatus(TaskStatus.ERROR);
				e.printStackTrace();
			}
			if(!unmatchedAdducts.isEmpty()){

				@SuppressWarnings("unused")
				InformationDialog id = new InformationDialog(
						"Unmatched features",
						"Not all adducts were matched to the database.\n"
						+ "Below is the list of unmatched adducts.",
						StringUtils.join(unmatchedAdducts, "\n"),
						null);
				setStatus(TaskStatus.ERROR);
				return;
			}
		}
		exportTargetList = exportTargetList.stream().
				sorted(new MsFeatureComparator(SortProperty.RT)).
				collect(Collectors.toList());
	}

	private Set<LibraryMsFeature>parseCefLibrary(Document cefLibrary) throws Exception{

		Set<LibraryMsFeature>targets = new HashSet<LibraryMsFeature>();

		NodeList targetNodes = cefLibrary.getElementsByTagName("Compound");
		total = targetNodes.getLength();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();

		for (int i = 0; i < targetNodes.getLength(); i++) {

			Element cpdElement = (Element) targetNodes.item(i);
			Element locationElement = (Element) cpdElement.getElementsByTagName("Location").item(0);
			double rt = Double.parseDouble(locationElement.getAttribute("rt"));
			double neutralMass = Double.parseDouble(locationElement.getAttribute("m"));
			Element moleculeElement = (Element) cpdElement.getElementsByTagName("Molecule").item(0);
			String name = "";
			Element msDetailsElement = (Element) cpdElement.getElementsByTagName("MSDetails").item(0);			

			if(moleculeElement != null){
				name = moleculeElement.getAttribute("name");

				// Work-around for old data
				if (name.isEmpty())
					name = moleculeElement.getAttribute("formula");
			}
			else{
				name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
					MRC2ToolBoxConfiguration.getMzFormat().format(neutralMass) + "_" + 
					MRC2ToolBoxConfiguration.getRtFormat().format(rt);
			}
			// Parse spectrum
			MassSpectrum spectrum = new MassSpectrum();
			NodeList peaks = cpdElement.getElementsByTagName("p");

			for (int j = 0; j < peaks.getLength(); j++) {

				Element peakElement = (Element) peaks.item(j);
				String adduct = peakElement.getAttribute("s");
				double mz = Double.parseDouble(peakElement.getAttribute("x"));
				double intensity = Double.parseDouble(peakElement.getAttribute("y"));
				int charge = Integer.parseInt(peakElement.getAttribute("z"));

				spectrum.addDataPoint(mz, intensity, adduct, charge);
			}
			spectrum.finalizeCefImportSpectrum();
			LibraryMsFeature lt = new LibraryMsFeature(name, spectrum, rt);
			lt.setNeutralMass(neutralMass);		
			Polarity pol = Polarity.Positive;
			if(msDetailsElement.getAttribute("p").equals("-"))
				pol = Polarity.Negative;
			
			lt.setPolarity(pol);

			// Parse identifications
			NodeList dbReference = cpdElement.getElementsByTagName("Accession");
			if (dbReference.getLength() > 0) {

				for (int j = 0; j < dbReference.getLength(); j++) {

					Element idElement = (Element) dbReference.item(j);
					String dbName = idElement.getAttribute("db");
					CompoundDatabaseEnum database = LibraryUtils.parseCompoundDatabaseName(dbName);
					String accession = idElement.getAttribute("id");

					if(database != null && !accession.isEmpty()){

						//	Check if library feature ID is embedded in CAS #
						if(dbName.equals(AgilentDatabaseFields.CAS_ID.getName())
								&& accession.startsWith(DataPrefix.MS_LIBRARY_TARGET.getName())){

								lt.setId(accession);
						}
						else{
							CompoundIdentity newId = 
									CompoundDatabaseUtils.getCompoundById(accession, conn);
							if(newId != null) {

								MsFeatureIdentity msId = new MsFeatureIdentity(
										newId, CompoundIdentificationConfidence.ACCURATE_MASS_RT);
								lt.setPrimaryIdentity(msId);
							}
						}
					}
				}
			}
			// Add ID by name if not specified in the library
			if(!lt.getName().startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName())
					&& lt.getName().indexOf('@') == -1
					&& lt.getPrimaryIdentity() == null) {

				CompoundIdentity pcId = 
						CompoundDatabaseUtils.getCompoundIdentityByName(name, conn);
				if( pcId != null){

					MsFeatureIdentity msId2 = 
							new MsFeatureIdentity(pcId, CompoundIdentificationConfidence.ACCURATE_MASS_RT);
					if (lt.getPrimaryIdentity() == null)
						lt.setPrimaryIdentity(msId2);
					else{
						for(Entry<CompoundDatabaseEnum, String> entry : pcId.getDbIdMap().entrySet()){

							if(lt.getPrimaryIdentity().getCompoundIdentity().getDbId(entry.getKey()) == null)
								lt.getPrimaryIdentity().getCompoundIdentity().addDbId(entry.getKey(), entry.getValue());
						}
					}
				}
			}
			targets.add(lt);
			processed++;
		}
		LibraryMsFeature[] targetArray = targets.toArray(new LibraryMsFeature[targets.size()]);
		Arrays.sort(targetArray, new MsFeatureComparator(SortProperty.Name, SortDirection.ASC));
		targets.clear();
		targets.addAll(Arrays.asList(targetArray));
		ConnectionManager.releaseConnection(conn);
		return targets;
	}

	private void readTargetsFromFile() throws Exception {

		taskDescription = "Reading library file...";
		Document cefLibrary = XmlUtils.readXmlFile(sourceLibraryFile);
		if(cefLibrary != null){

			unmatchedAdducts = CefUtils.getUnmatchedAdducts(cefLibrary, total, processed);
			if(!unmatchedAdducts.isEmpty())
				return;
			else{
				Set<LibraryMsFeature> fileTargets = new HashSet<LibraryMsFeature>();
				try {
					fileTargets = parseCefLibrary(cefLibrary);
				} catch (Exception e) {
					e.printStackTrace();
				}
				exportTargetList.addAll(fileTargets);
			}
		}
	}

	private void writeCefLibrary() throws Exception {

		total = exportTargetList.size();
		processed = 0;

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

		exportedLibraryDocument = dBuilder.newDocument();
		Element cefRoot = exportedLibraryDocument.createElement("CEF");
		cefRoot.setAttribute("version", "1.0.0.0");

		String libId = DataPrefix.MS_LIBRARY.getName() + UUID.randomUUID().toString();
		cefRoot.setAttribute("library_id", libId);
		exportedLibraryDocument.appendChild(cefRoot);
		Element compoundListElement = exportedLibraryDocument.createElement("CompoundList");
		cefRoot.appendChild(compoundListElement);

		for(LibraryMsFeature lt : exportTargetList){
			
			if(lt.getSpectrum() == null) {
				System.out.println(lt.getName() + " has no spectrum.");
				continue;
			}
			if(combineAdducts){
				Element compound = createCefElement(lt, null, false);
				compoundListElement.appendChild(compound);
			}
			else{
				boolean createNewId = false;	//	Create new feature ID if more than 1 adduct is added as separate entry
				for(Adduct adduct : lt.getSpectrum().getAdducts()){

					//	TODO make this check optional
					if(lt.getSpectrum().getMsForAdduct(adduct).length > 1){

						Element compound = createCefElement(lt, adduct, createNewId);
						compoundListElement.appendChild(compound);
					}
					createNewId = true;
				}
			}
			processed++;
		}
		TransformerFactory transfac = TransformerFactory.newInstance();
		Transformer transformer = transfac.newTransformer();
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		String extension = FilenameUtils.getExtension(outputLibraryFile.getAbsolutePath());

		if(!extension.equalsIgnoreCase(MsLibraryFormat.CEF.getFileExtension()))
			outputLibraryFile = new File(FilenameUtils.removeExtension(outputLibraryFile.getAbsolutePath()) + "."
					+ MsLibraryFormat.CEF.getFileExtension());

		StreamResult result = new StreamResult(new FileOutputStream(outputLibraryFile));
		DOMSource source = new DOMSource(exportedLibraryDocument);
		transformer.transform(source, result);
		result.getOutputStream().close();
	}
}









































