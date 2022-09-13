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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

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
import edu.umich.med.mrc2.datoolbox.gui.utils.InformationDialog;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef.CEFProcessingTask;
import edu.umich.med.mrc2.datoolbox.utils.LibraryUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class ConvertCefLibraryForRecursionTask extends CEFProcessingTask {

	private Document exportedLibraryDocument;
	private boolean combineAdducts;
	private List<LibraryMsFeature> exportTargetList;
	private TreeSet<String> unmatchedAdducts;

	public ConvertCefLibraryForRecursionTask(			
			File sourceLibraryFile,
			File outputLibraryFile,
			boolean combineAdducts) {

		this.inputCefFile = sourceLibraryFile;
		this.outputCefFile = outputLibraryFile;
		this.combineAdducts = combineAdducts;
		unmatchedAdducts = new TreeSet<String>();
		exportTargetList = new ArrayList<LibraryMsFeature>();
	}

	@Override
	public void run() {

		taskDescription = 
				"Creating library for recursion from " + inputCefFile.getName();
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
	
	private void createTargetList() {

		if(inputCefFile != null){
			try {
				parseInputCefFile(inputCefFile);
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
		inputFeatureList.stream().
			forEach(f -> exportTargetList.add(new LibraryMsFeature(f)));
		exportTargetList = exportTargetList.stream().
				sorted(new MsFeatureComparator(SortProperty.RT)).
				collect(Collectors.toList());
	}
	
	private void writeCefLibrary() throws Exception {

		total = exportTargetList.size();
		processed = 0;
		
		exportedLibraryDocument = new Document();
        Element cefRoot = new Element("CEF");
        cefRoot.setAttribute("version", "1.0.0.0");
		String libId = DataPrefix.MS_LIBRARY.getName() + UUID.randomUUID().toString();
		cefRoot.setAttribute("library_id", libId);
		exportedLibraryDocument.addContent(cefRoot);	
		Element compoundListElement = new Element("CompoundList");
		cefRoot.addContent(compoundListElement);

		for(LibraryMsFeature lt : exportTargetList){
			
			if(lt.getSpectrum() == null) {
				System.err.println(lt.getName() + " has no spectrum.");
				continue;
			}
			if(combineAdducts){
				Element compound = createCefElement(lt, null, false);
				compoundListElement.addContent(compound);
			}
			else{
				boolean createNewId = false;	//	Create new feature ID if more than 1 adduct is added as separate entry
				for(Adduct adduct : lt.getSpectrum().getAdducts()){

					//	TODO make this check optional
					if(lt.getSpectrum().getMsForAdduct(adduct).length > 1){

						Element compound = createCefElement(lt, adduct, createNewId);
						compoundListElement.addContent(compound);
					}
					createNewId = true;
				}
			}
			processed++;
		}
		String extension = FilenameUtils.getExtension(outputCefFile.getAbsolutePath());
		if(!extension.equalsIgnoreCase(MsLibraryFormat.CEF.getFileExtension()))
			outputCefFile = new File(FilenameUtils.removeExtension(outputCefFile.getAbsolutePath()) + "."
					+ MsLibraryFormat.CEF.getFileExtension());

		XmlUtils.writeXMLDocumentToFile(exportedLibraryDocument, outputCefFile);
	}

	private Element createCefElement(LibraryMsFeature lt, Adduct adduct, boolean createNewId) {

		//Element compound = (Element) exportedLibraryDocument.createElement("Compound");
		Element compound = new Element("Compound");

		String mass = MRC2ToolBoxConfiguration.getMzFormat().format(lt.getNeutralMass());
		String rt = MRC2ToolBoxConfiguration.getRtFormat().format(lt.getRetentionTime());
		compound.setAttribute("mppid", mass + "_" + rt);
		compound.setAttribute("algo", "FindByFormula");

		//	Location
		Element location = new Element("Location");
		location.setAttribute("m", mass);
		location.setAttribute("rt", rt);
		double area = 100.0d;
		location.setAttribute("a", Double.toString(area));
		compound.addContent(location);

		//	Identification
		Element results = new Element("Results");
		Element molecule = new Element("Molecule");

		String compoundName = "";
		String formula = "";
		if(lt.getPrimaryIdentity() != null) {
			compoundName = lt.getPrimaryIdentity().getName();
			formula = lt.getPrimaryIdentity().getCompoundIdentity().getFormula();
		}
		else {
			compoundName = lt.getName();
		}
		molecule.setAttribute("name", compoundName);
		if(formula != null && !formula.isEmpty())
			molecule.setAttribute("formula", compoundName);
		
		Element database = new Element("Database");
		Element libId = new Element("Accession");
		libId.setAttribute("db", "CAS ID");
		if(createNewId)
			libId.setAttribute("id", DataPrefix.MS_LIBRARY_TARGET.getName() + UUID.randomUUID().toString());
		else
			libId.setAttribute("id", lt.getId());
		
		database.addContent(libId);

		if(lt.getPrimaryIdentity() != null 
				&& lt.getPrimaryIdentity().getPrimaryLinkLabel() != null
				&& !lt.getPrimaryIdentity().getPrimaryLinkLabel().isEmpty()){

			Element accession = new Element("Accession");
			accession.setAttribute("db", "KEGG ID");
			accession.setAttribute("id", lt.getPrimaryIdentity().getPrimaryLinkLabel());
			database.addContent(accession);
		}
		molecule.addContent(database);
		results.addContent(molecule);
		compound.addContent(results);

		//	Spectrum
		Element spectrum = new Element("Spectrum");
		spectrum.setAttribute("type", "FbF");
		spectrum.setAttribute("cpdAlgo", "FindByFormula");

		Element msDetails = new Element("MSDetails");
		String sign = (lt.getPolarity().equals(Polarity.Positive)) ? "+" : "-";
		msDetails.setAttribute("p", sign);
		spectrum.addContent(msDetails);

		Element msPeaks = new Element("MSPeaks");
		if(adduct != null){

			String charge = Integer.toString(adduct.getCharge());
			MsPoint[] adductMs = lt.getSpectrum().getMsForAdduct(adduct);

			for(int i=0; i<adductMs.length; i++){

				Element peak = new Element("p");
				peak.setAttribute("x", MRC2ToolBoxConfiguration.getMzFormat().format(adductMs[i].getMz()));
				peak.setAttribute("y", Double.toString(adductMs[i].getIntensity()));
				peak.setAttribute("z", charge);
				
				String aname = adduct.getCefNotation();
				if(i>0)
					aname = aname + "+" + i;

				peak.setAttribute("s", aname);
				msPeaks.addContent(peak);
			}
		}
		else{
			for(Adduct ad : lt.getSpectrum().getAdducts()){

				String charge = Integer.toString(ad.getCharge());
				MsPoint[] adductMs = lt.getSpectrum().getMsForAdduct(ad);

				for(int i=0; i<adductMs.length; i++){

					Element peak = new Element("p");
					peak.setAttribute("x", MRC2ToolBoxConfiguration.getMzFormat().format(adductMs[i].getMz()));
					peak.setAttribute("y", Double.toString(adductMs[i].getIntensity()));
					peak.setAttribute("z", charge);
					String aname = ad.getCefNotation();

					if(i>0)
						aname = aname + "+" + i;

					peak.setAttribute("s", aname);
					msPeaks.addContent(peak);
				}
			}
		}
		spectrum.addContent(msPeaks);
		compound.addContent(spectrum);
		return compound;
	}

	private Set<LibraryMsFeature>parseCefLibrary(Document cefLibrary) throws Exception{

		Set<LibraryMsFeature>targets = new HashSet<LibraryMsFeature>();
		List<Element>targetNodes = 
				cefLibrary.getRootElement().getChild("CompoundList").getChildren("Compound");
		
		total = targetNodes.size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();

		for (Element cpdElement : targetNodes) {

			Element location = cpdElement.getChild("Location");
			double rt = location.getAttribute("rt").getDoubleValue();
			double neutralMass = location.getAttribute("m").getDoubleValue();
					
			String name = "";
			Element resultsElement = cpdElement.getChild("Results");
			Element moleculeElement = null;
			if(resultsElement != null) {
				
				moleculeElement = resultsElement.getChild("Molecule");						
				if(moleculeElement != null){
					
					name = moleculeElement.getAttributeValue("name");

					// Work-around for old data
					if (name.isEmpty())
						name = moleculeElement.getAttributeValue("formula");
				}
				else{
					name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
						MRC2ToolBoxConfiguration.getMzFormat().format(neutralMass) + "_" + 
						MRC2ToolBoxConfiguration.getRtFormat().format(rt);
				}
			}
			else{
				name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
					MRC2ToolBoxConfiguration.getMzFormat().format(neutralMass) + "_" + 
					MRC2ToolBoxConfiguration.getRtFormat().format(rt);
			}
			// Parse spectrum
			Element spectrumElement = cpdElement.getChild("Spectrum");	
			MassSpectrum spectrum = new MassSpectrum();
			List<Element> peaks = spectrumElement.getChild("MSPeaks").getChildren("p");
			for (Element peakElement : peaks) {

				String adduct = peakElement.getAttributeValue("s");
				if(AdductManager.getAdductByCefNotation(adduct) == null) {
					unmatchedAdducts.add(adduct);
				}
				else {
					double mz = peakElement.getAttribute("x").getDoubleValue();
					double intensity = peakElement.getAttribute("y").getDoubleValue();
					int charge = peakElement.getAttribute("z").getIntValue();
					spectrum.addDataPoint(mz, intensity, adduct, charge);
				}
			}
			spectrum.finalizeCefImportSpectrum();
			
			//	In case of unusual adducts producing empty spectrum. Very unlikely to happen
			if(spectrum.getMsPoints().isEmpty()) {
				processed++;
				continue;
			}
			
			LibraryMsFeature lt = new LibraryMsFeature(name, spectrum, rt);
			lt.setNeutralMass(neutralMass);	
			
			Polarity pol = Polarity.Positive;
			if(spectrumElement.getChild("MSDetails").getAttributeValue("p").equals("-"))
				pol = Polarity.Negative;
			
			lt.setPolarity(pol);

			// Parse identifications
			if(moleculeElement != null) {
				
				List<Element>dbReferenceList = 
						moleculeElement.getChild("Database").getChildren("Accession");
				for(Element idElement : dbReferenceList) {
					
					String dbName = idElement.getAttributeValue("db");
					CompoundDatabaseEnum database = 
							LibraryUtils.parseCompoundDatabaseName(dbName);
					String accession = idElement.getAttributeValue("id");

					if(database != null && accession != null && !accession.isEmpty()){

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

//	private void readTargetsFromFile() throws Exception {
//
//		taskDescription = "Reading library file...";
//		Document cefLibrary = XmlUtils.readXmlFile(sourceLibraryFile);
//		if(cefLibrary != null){
//
//			unmatchedAdducts = CefUtils.getUnmatchedAdducts(cefLibrary, total, processed);
//			if(!unmatchedAdducts.isEmpty())
//				return;
//			else{
//				Set<LibraryMsFeature> fileTargets = new HashSet<LibraryMsFeature>();
//				try {
//					fileTargets = parseCefLibrary(cefLibrary);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				exportTargetList.addAll(fileTargets);
//			}
//		}
//	}
	
	@Override
	public Task cloneTask() {

		return new ConvertCefLibraryForRecursionTask(
				inputCefFile,
				outputCefFile,
				combineAdducts);
	}
}









































