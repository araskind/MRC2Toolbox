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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureStatisticalSummary;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.gui.library.LibraryFeatureTableModel;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef.CEFProcessingTask;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class LibraryExportTask extends CEFProcessingTask {
	
	private CompoundLibrary currentLibrary;
	private Collection<LibraryMsFeature>targetSubset;
	private Collection<MsFeature>featureSubset;
	private MsLibraryFormat libraryFormat;
	private boolean combineAdducts;
	private Map<LibraryMsFeature,MsFeatureStatisticalSummary>statsMap;

	private static final String lineSeparator = System.getProperty("line.separator");

	public LibraryExportTask(
			File sourceLibraryFile,
			File outputLibraryFile,
			boolean combineAdducts,
			CompoundLibrary currentLibrary,
			Collection<LibraryMsFeature> targetSubset,
			Collection<MsFeature>featureSubset,
			MsLibraryFormat libraryFormat) {

		super();
		this.inputCefFile = sourceLibraryFile;
		this.outputCefFile = outputLibraryFile;
		this.combineAdducts = combineAdducts;
		this.currentLibrary = currentLibrary;
		this.targetSubset = targetSubset;
		this.featureSubset = featureSubset;
		this.libraryFormat = libraryFormat;
		
		statsMap =  new HashMap<LibraryMsFeature,MsFeatureStatisticalSummary>();
		
		if(!FilenameUtils.getExtension(outputCefFile.getName()).equalsIgnoreCase(libraryFormat.getFileExtension()))
			outputCefFile = FIOUtils.changeExtension(outputCefFile, libraryFormat.getFileExtension());		
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		
		if(currentLibrary != null)
			taskDescription = "Exporting library '" + 
				currentLibrary.getLibraryName() + "' to " + libraryFormat.getName();
		else
			taskDescription = "Exporting features to " + libraryFormat.getName();

		createTargetList();
		if(!unmatchedAdducts.isEmpty()){
			setStatus(TaskStatus.ERROR);
			return;
		}
		total = libraryFeatureListForExport.size();
		processed = 0;

		if(libraryFormat.equals(MsLibraryFormat.CEF)){
			try {
				writeCefLibrary(libraryFeatureListForExport, combineAdducts);
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
				return;				
			}
		}
		if(libraryFormat.equals(MsLibraryFormat.MSP))
			writeMspLibrary();

		if(libraryFormat.equals(MsLibraryFormat.TSV)) {
			try {
				writeTextLibrary();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
				return;	
			}
		}
		setStatus(TaskStatus.FINISHED);
	}

	@Override
	public Task cloneTask() {

		return new LibraryExportTask(
				inputCefFile,
				outputCefFile,
				combineAdducts,
				currentLibrary,
				targetSubset,
				featureSubset,
				libraryFormat);
	}

	private Collection<LibraryMsFeature> convertFeaturesToTargets(Collection<MsFeature> featureSubset2) {

		total = featureSubset2.size();
		processed = 0;
		Collection<LibraryMsFeature>convertedFeatures = new ArrayList<LibraryMsFeature>();
		for(MsFeature f : featureSubset2){

			LibraryMsFeature lt = new LibraryMsFeature(f);

			//	Correct RT
			if(f.getStatsSummary().getMedianObservedRetention() > 0.0d)
				lt.setRetentionTime(f.getStatsSummary().getMedianObservedRetention());

			statsMap.put(lt, f.getStatsSummary());
			convertedFeatures.add(lt);
			processed++;
		}
		return convertedFeatures;
	}

//	private  double getAveragePeakArea(MsFeatureStatisticalSummary statsSummary) {
//
//		if(statsSummary == null)
//			return 0.0d;
//
//		if(statsSummary.getSampleMean() > 0.0d)
//			return statsSummary.getSampleMean();
//
//		if(statsSummary.getPooledMean() > 0.0d)
//			return statsSummary.getPooledMean();
//
//		return 0.0d;
//	}

//	private Element createCefElement(LibraryMsFeature lt, Adduct adduct, boolean createNewId) {
//
//		Element compound = (Element) exportedLibraryDocument.createElement("Compound");
//
//		String mass = MRC2ToolBoxConfiguration.getMzFormat().format(lt.getNeutralMass());
//		String rt = MRC2ToolBoxConfiguration.getRtFormat().format(lt.getRetentionTime());
//		compound.setAttribute("mppid", mass + "_" + rt);
//		compound.setAttribute("algo", "FindByFormula");
//
//		//	Location
//		Element location = exportedLibraryDocument.createElement("Location");
//		location.setAttribute("m", mass);
//		location.setAttribute("rt", rt);
//		double area = 100.0d;
//		MsFeatureStatisticalSummary statsSummary = statsMap.get(lt);
//		if(statsSummary != null)
//			area = getAveragePeakArea(statsSummary);
//
//		location.setAttribute("a", Double.toString(area));
//		compound.appendChild(location);
//
//		//	Identification
//		Element results = exportedLibraryDocument.createElement("Results");
//		Element molecule = exportedLibraryDocument.createElement("Molecule");
//
//		String compoundName = "";
//
//		if(lt.getPrimaryIdentity() != null)
//			compoundName = lt.getPrimaryIdentity().getName();
//		else
//			compoundName = lt.getName();
//
//		if(adduct != null && addAdductName)
//			compoundName = compoundName + " (" + adduct.getName() + ")";
//
//		molecule.setAttribute("name", compoundName);
//
//		Element database = exportedLibraryDocument.createElement("Database");
//
//		Element libId = exportedLibraryDocument.createElement("Accession");
//		libId.setAttribute("db", "CAS ID");
//		if(createNewId)
//			libId.setAttribute("id", DataPrefix.MS_FEATURE.getName() + UUID.randomUUID().toString());
//		else
//			libId.setAttribute("id", lt.getId());
//		
//		database.appendChild(libId);
//
//		if(lt.getPrimaryIdentity() != null){
//
//			Element accession = exportedLibraryDocument.createElement("Accession");
//			accession.setAttribute("db", "KEGG ID");
//			accession.setAttribute("id", lt.getPrimaryIdentity().getPrimaryLinkLabel());
//			database.appendChild(accession);
//		}
//		molecule.appendChild(database);
//		results.appendChild(molecule);
//		compound.appendChild(results);
//
//		//	Spectrum
//		Element spectrum = exportedLibraryDocument.createElement("Spectrum");
//		spectrum.setAttribute("type", "FbF");
//		spectrum.setAttribute("cpdAlgo", "FindByFormula");
//
//		Element msDetails = exportedLibraryDocument.createElement("MSDetails");
//		String sign = (lt.getPolarity().equals(Polarity.Positive)) ? "+" : "-";
//		msDetails.setAttribute("p", sign);
//		spectrum.appendChild(msDetails);
//
//		Element msPeaks = exportedLibraryDocument.createElement("MSPeaks");
//
//		if(adduct != null){
//
//			String charge = Integer.toString(adduct.getCharge());
//			MsPoint[] adductMs = lt.getSpectrum().getMsForAdduct(adduct);
//
//			for(int i=0; i<adductMs.length; i++){
//
//				Element peak = exportedLibraryDocument.createElement("p");
//				peak.setAttribute("x", MRC2ToolBoxConfiguration.getMzFormat().format(adductMs[i].getMz()));
//				peak.setAttribute("y", Double.toString(adductMs[i].getIntensity()));
//				peak.setAttribute("z", charge);
//
//				String aname = adduct.getCefNotation();
//
//				if(i>0)
//					aname = aname + "+" + i;
//
//				peak.setAttribute("s", aname);
//				msPeaks.appendChild(peak);
//			}
//		}
//		else{
//			for(Adduct ad : lt.getSpectrum().getAdducts()){
//
//				String charge = Integer.toString(ad.getCharge());
//				MsPoint[] adductMs = lt.getSpectrum().getMsForAdduct(ad);
//
//				for(int i=0; i<adductMs.length; i++){
//
//					Element peak = exportedLibraryDocument.createElement("p");
//					peak.setAttribute("x", MRC2ToolBoxConfiguration.getMzFormat().format(adductMs[i].getMz()));
//					peak.setAttribute("y", Double.toString(adductMs[i].getIntensity()));
//					peak.setAttribute("z", charge);
//
//					String aname = ad.getCefNotation();
//
//					if(i>0)
//						aname = aname + "+" + i;
//
//					peak.setAttribute("s", aname);
//					msPeaks.appendChild(peak);
//				}
//			}
//		}
//		spectrum.appendChild(msPeaks);
//		compound.appendChild(spectrum);
//		return compound;
//	}
	
	private void createTargetList() {

		libraryFeatureListForExport = new ArrayList<LibraryMsFeature>();
		if(inputCefFile != null){
			
			createLibraryFeaturetListFromCefFile();
			
			if(libraryFeatureListForExport == null || libraryFeatureListForExport.isEmpty()) {			
				errorMessage = "Failed to parse input file, no features to export";
				setStatus(TaskStatus.ERROR);
				return;
			}
		}
		//	Export complete library
		if(targetSubset == null && featureSubset == null && currentLibrary != null) {

			List<LibraryMsFeature> features = currentLibrary.getFeatures().stream()
			    	.filter(LibraryMsFeature.class::isInstance)
			    	.map(LibraryMsFeature.class::cast)
			    	.filter(f -> f.isActive())
			    	.collect(Collectors.toList());

			libraryFeatureListForExport.addAll(features);
		}
		//	Export filtered library
		if(targetSubset != null)
			libraryFeatureListForExport.addAll(targetSubset);

		//	Export feature subset
		if(featureSubset != null){

			Collection<LibraryMsFeature>convertedFeatures = 
					convertFeaturesToTargets(featureSubset);
			libraryFeatureListForExport.addAll(convertedFeatures);
		}
		libraryFeatureListForExport = libraryFeatureListForExport.stream().
				sorted(new MsFeatureComparator(SortProperty.RT)).
				collect(Collectors.toSet());
	}
//
//	private void createTargetListOld() {
//
//		exportTargetList = new ArrayList<LibraryMsFeature>();
//		if(inputCefFile != null){
//			try {
//				readTargetsFromFile();
//			} catch (Exception e) {
//				setStatus(TaskStatus.ERROR);
//				e.printStackTrace();
//			}
//			if(!unmatchedAdducts.isEmpty()){
//
//				@SuppressWarnings("unused")
//				InformationDialog id = new InformationDialog(
//						"Unmatched features",
//						"Not all adducts were matched to the database.\nBelow is the list of unmatched adducts.",
//						StringUtils.join(unmatchedAdducts, "\n"),
//						null);
//				setStatus(TaskStatus.ERROR);
//				return;
//			}
//		}
//		//	Export complete library
//		if(targetSubset == null && featureSubset == null && currentLibrary != null) {
//
//			List<LibraryMsFeature> features = currentLibrary.getFeatures().stream()
//			    	.filter(LibraryMsFeature.class::isInstance)
//			    	.map(LibraryMsFeature.class::cast)
//			    	.filter(f -> f.isActive())
//			    	.collect(Collectors.toList());
//
//			exportTargetList.addAll(features);
//		}
//		//	Export filtered library
//		if(targetSubset != null)
//			exportTargetList.addAll(targetSubset);
//
//		//	Export feature subset
//		if(featureSubset != null){
//
//			Collection<LibraryMsFeature>convertedFeatures = 
//					convertFeaturesToTargets(featureSubset);
//			exportTargetList.addAll(convertedFeatures);
//		}
//		exportTargetList = exportTargetList.stream().sorted(new MsFeatureComparator(SortProperty.RT)).collect(Collectors.toSet());
//	}
//
//	private Set<LibraryMsFeature>parseCefLibrary(Document cefLibrary) throws Exception{
//
//		Set<LibraryMsFeature>targets = new HashSet<LibraryMsFeature>();
//
//		NodeList targetNodes = cefLibrary.getElementsByTagName("Compound");
//		total = targetNodes.getLength();
//		processed = 0;
//		Connection conn = ConnectionManager.getConnection();
//
//		for (int i = 0; i < targetNodes.getLength(); i++) {
//
//			Element cpdElement = (Element) targetNodes.item(i);
//			Element locationElement = (Element) cpdElement.getElementsByTagName("Location").item(0);
//			double rt = Double.parseDouble(locationElement.getAttribute("rt"));
//			double neutralMass = Double.parseDouble(locationElement.getAttribute("m"));
//			Element moleculeElement = (Element) cpdElement.getElementsByTagName("Molecule").item(0);
//			String name = "";
//			Element msDetailsElement = (Element) cpdElement.getElementsByTagName("MSDetails").item(0);			
//
//			if(moleculeElement != null){
//				name = moleculeElement.getAttribute("name");
//
//				// Work-around for old data
//				if (name.isEmpty())
//					name = moleculeElement.getAttribute("formula");
//			}
//			else{
//				name = DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName() +
//					MRC2ToolBoxConfiguration.getMzFormat().format(neutralMass) + "_" + 
//					MRC2ToolBoxConfiguration.getRtFormat().format(rt);
//			}
//			// Parse spectrum
//			MassSpectrum spectrum = new MassSpectrum();
//			NodeList peaks = cpdElement.getElementsByTagName("p");
//
//			for (int j = 0; j < peaks.getLength(); j++) {
//
//				Element peakElement = (Element) peaks.item(j);
//				String adduct = peakElement.getAttribute("s");
//				double mz = Double.parseDouble(peakElement.getAttribute("x"));
//				double intensity = Double.parseDouble(peakElement.getAttribute("y"));
//				int charge = Integer.parseInt(peakElement.getAttribute("z"));
//
//				spectrum.addDataPoint(mz, intensity, adduct, charge);
//			}
//			spectrum.finalizeCefImportSpectrum();
//			LibraryMsFeature lt = new LibraryMsFeature(name, spectrum, rt);
//			lt.setNeutralMass(neutralMass);		
//			Polarity pol = Polarity.Positive;
//			if(msDetailsElement.getAttribute("p").equals("-"))
//				pol = Polarity.Negative;
//			
//			lt.setPolarity(pol);
//
//			// Parse identifications
//			NodeList dbReference = cpdElement.getElementsByTagName("Accession");
//
//			if (dbReference.getLength() > 0) {
//
//				for (int j = 0; j < dbReference.getLength(); j++) {
//
//					Element idElement = (Element) dbReference.item(j);
//					String dbName = idElement.getAttribute("db");
//					CompoundDatabaseEnum database = LibraryUtils.parseCompoundDatabaseName(dbName);
//					String accession = idElement.getAttribute("id");
//
//					if(database != null && !accession.isEmpty()){
//
//						//	Check if library feature ID is embedded in CAS #
//						if(dbName.equals(AgilentDatabaseFields.CAS_ID.getName())
//								&& accession.startsWith(DataPrefix.MS_LIBRARY_TARGET.getName())){
//
//								lt.setId(accession);
//						}
//						else{
//							CompoundIdentity newId = 
//									CompoundDatabaseUtils.getCompoundById(accession, conn);
//							if(newId != null) {
//
//								MsFeatureIdentity msId = new MsFeatureIdentity(
//										newId, CompoundIdentificationConfidence.ACCURATE_MASS_RT);
//								lt.setPrimaryIdentity(msId);
//							}
//						}
//					}
//				}
//			}
//			// Add ID by name if not specified in the library
//			if(!lt.getName().startsWith(DataPrefix.MS_LIBRARY_UNKNOWN_TARGET.getName())
//					&& lt.getName().indexOf('@') == -1
//					&& lt.getPrimaryIdentity() == null) {
//
//				CompoundIdentity pcId = 
//						CompoundDatabaseUtils.getCompoundIdentityByName(name, conn);
//				if( pcId != null){
//
//					MsFeatureIdentity msId2 = 
//							new MsFeatureIdentity(pcId, CompoundIdentificationConfidence.ACCURATE_MASS_RT);
//					if (lt.getPrimaryIdentity() == null)
//						lt.setPrimaryIdentity(msId2);
//					else{
//						for(Entry<CompoundDatabaseEnum, String> entry : pcId.getDbIdMap().entrySet()){
//
//							if(lt.getPrimaryIdentity().getCompoundIdentity().getDbId(entry.getKey()) == null)
//								lt.getPrimaryIdentity().getCompoundIdentity().addDbId(entry.getKey(), entry.getValue());
//						}
//					}
//				}
//			}
//			targets.add(lt);
//			processed++;
//		}
//		LibraryMsFeature[] targetArray = targets.toArray(new LibraryMsFeature[targets.size()]);
//		Arrays.sort(targetArray, new MsFeatureComparator(SortProperty.Name, SortDirection.ASC));
//		targets.clear();
//		targets.addAll(Arrays.asList(targetArray));
//		ConnectionManager.releaseConnection(conn);
//		return targets;
//	}
//
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


//	private void writeCefLibrary() throws Exception {
//
//		total = exportTargetList.size();
//		processed = 0;
//
//		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//
//		exportedLibraryDocument = dBuilder.newDocument();
//		Element cefRoot = exportedLibraryDocument.createElement("CEF");
//		cefRoot.setAttribute("version", "1.0.0.0");
//
//		String libId = DataPrefix.MS_LIBRARY.getName() + UUID.randomUUID().toString();
//		if(currentLibrary != null)
//			libId = currentLibrary.getLibraryId();
//
//		cefRoot.setAttribute("library_id", libId);
//		exportedLibraryDocument.appendChild(cefRoot);
//		Element compoundListElement = exportedLibraryDocument.createElement("CompoundList");
//		cefRoot.appendChild(compoundListElement);
//
//		for(LibraryMsFeature lt : exportTargetList){
//			
//			if(lt.getSpectrum() == null) {
//				System.out.println(lt.getName() + " has no spectrum.");
//				continue;
//			}
//			if(combineAdducts){
//				Element compound = createCefElement(lt, null, false);
//				compoundListElement.appendChild(compound);
//			}
//			else{
//				boolean createNewId = false;	//	Create new feature ID if more than 1 adduct is added as separate entry
//				for(Adduct adduct : lt.getSpectrum().getAdducts()){
//
//					//	TODO make this check optional
//					if(lt.getSpectrum().getMsForAdduct(adduct).length > 1){
//
//						Element compound = createCefElement(lt, adduct, createNewId);
//						compoundListElement.appendChild(compound);
//					}
//					createNewId = true;
//				}
//			}
//			processed++;
//		}
//		TransformerFactory transfac = TransformerFactory.newInstance();
//		Transformer transformer = transfac.newTransformer();
//		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
//		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
//		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
//
//		String extension = FilenameUtils.getExtension(outputLibraryFile.getAbsolutePath());
//
//		if(!extension.equalsIgnoreCase(MsLibraryFormat.CEF.getFileExtension()))
//			outputLibraryFile = new File(FilenameUtils.removeExtension(outputLibraryFile.getAbsolutePath()) + "."
//					+ MsLibraryFormat.CEF.getFileExtension());
//
//		StreamResult result = new StreamResult(new FileOutputStream(outputLibraryFile));
//		DOMSource source = new DOMSource(exportedLibraryDocument);
//		transformer.transform(source, result);
//		result.getOutputStream().close();
//	}

	private void writeMspLibrary() {
		// TODO Auto-generated method stub
		total = libraryFeatureListForExport.size();
		processed = 0;
	}

	private void writeTextLibrary() throws IOException {

		total = libraryFeatureListForExport.size();
		processed = 0;
		final Writer writer = new BufferedWriter(new FileWriter(outputCefFile));
		char columnSeparator = MRC2ToolBoxConfiguration.getTabDelimiter();

		//	Create header
		String[] header = new String[] {
			LibraryFeatureTableModel.ID_COLUMN,
			LibraryFeatureTableModel.ID_CONFIDENCE_COLUMN,
			LibraryFeatureTableModel.FEATURE_COLUMN,
			LibraryFeatureTableModel.COMPOUND_NAME_COLUMN,
			LibraryFeatureTableModel.FORMULA_COLUMN,
			LibraryFeatureTableModel.MASS_COLUMN,
			LibraryFeatureTableModel.CHARGE_COLUMN,
			LibraryFeatureTableModel.RT_COLUMN,
			LibraryFeatureTableModel.QC_COLUMN
		};
		writer.append(StringUtils.join(header, columnSeparator));
		writer.append(lineSeparator);

		//	Write feature data
		for(LibraryMsFeature lf : libraryFeatureListForExport){

			MsFeatureIdentity identity = lf.getPrimaryIdentity();

			String formula = "";
			String linkLabel = "";
			int innateCharge = 0;

			if(identity != null) {
				formula = identity.getCompoundIdentity().getFormula();
				String smiles = identity.getCompoundIdentity().getSmiles();

				if(smiles != null)
					innateCharge = StringUtils.countMatches(smiles, "+") - StringUtils.countMatches(smiles, "-");

				linkLabel = identity.getPrimaryLinkLabel();
			}
			String compoundName = lf.getName();
			CompoundIdentificationConfidence idc = null;
			if(lf.getPrimaryIdentity() != null) {

				compoundName = lf.getPrimaryIdentity().getCompoundName();
				idc= lf.getPrimaryIdentity().getConfidenceLevel();
			}
			String[] line = new String[] {
				linkLabel,
				idc.getName(),
				lf.getName(),
				compoundName,
				formula,
				MRC2ToolBoxConfiguration.getMzFormat().format(lf.getNeutralMass()),
				Integer.toString(innateCharge),
				MRC2ToolBoxConfiguration.getRtFormat().format(lf.getRetentionTime()),
				Boolean.toString(lf.isQcStandard())
			};
			writer.append(StringUtils.join(line, columnSeparator));
			writer.append(lineSeparator);
		}
		writer.flush();
		writer.close();
	}

	/**
	 * @param addAdductName the addAdductName to set
	 */
//	public void setAddAdductName(boolean addAdductName) {
//		this.addAdductName = addAdductName;
//	}
}









































