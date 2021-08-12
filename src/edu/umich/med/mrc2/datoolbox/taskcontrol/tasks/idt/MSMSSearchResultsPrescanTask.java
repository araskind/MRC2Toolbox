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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentCefFields;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentDatabaseFields;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class MSMSSearchResultsPrescanTask extends AbstractTask {

	private File inputFile;
	private Document dataDocument;
	private Connection conn;
	private Collection<CompoundIdentity>missingIdentities;
	private Map<CompoundIdentity, Collection<TandemMassSpectrum>>idSpectrumMap;

	public MSMSSearchResultsPrescanTask(File inputFile) {
		super();
		this.inputFile = inputFile;
		missingIdentities = new HashSet<CompoundIdentity>();
		idSpectrumMap = new HashMap<CompoundIdentity, Collection<TandemMassSpectrum>>();
	}

	@Override
	public void run() {

		taskDescription = "Importing data from " + inputFile.getName();
		setStatus(TaskStatus.PROCESSING);
		try {
			dataDocument = null;
			// Read CEF file
			try {
				dataDocument = XmlUtils.readXmlFile(inputFile);
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
			//	Parse CEF data
			if(dataDocument != null) {
				try {
					parseCefData();
					if(!missingIdentities.isEmpty())
						System.out.println(Integer.toString(missingIdentities.size()) + "missing IDs in " + inputFile.getName());
				}
				catch (Exception e) {
					e.printStackTrace();
					setStatus(TaskStatus.ERROR);
				}
			}
		} catch (Exception e) {
			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
		setStatus(TaskStatus.FINISHED);
	}

	protected void parseCefData() throws Exception {

		conn = ConnectionManager.getConnection();
		String idQuery =
				"SELECT C.MRC2_LIB_ID, SPECTRUM_HASH " +
				"FROM REF_MSMS_LIBRARY_COMPONENT C " +
				"WHERE C.ORIGINAL_LIBRARY_ID = ? " +
				"AND C.LIBRARY_NAME = 'METLIN' " +
				"AND C.COLLISION_ENERGY = ? " +
				"AND C.POLARITY = ?";
		PreparedStatement idPs = conn.prepareStatement(idQuery);

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr = xpath.compile("//CEF/CompoundList/Compound");
		NodeList targetNodes = (NodeList) expr.evaluate(dataDocument, XPathConstants.NODESET);
		total = targetNodes.getLength();
		processed = 0;
		for (int i = 0; i < targetNodes.getLength(); i++) {

			processed++;
			Element cpdElement = (Element) targetNodes.item(i);
			if(cpdElement.getElementsByTagName("Molecule").getLength() == 0)
				continue;

			Element molecule = (Element) cpdElement.getElementsByTagName("Molecule").item(0);
			String cpdName = molecule.getAttribute("name").trim();
			String molecularFormula = molecule.getAttribute("formula").replaceAll("\\s+", "");
			if(molecularFormula.isEmpty())
				molecularFormula = null;

			NodeList iDlist = cpdElement.getElementsByTagName("Accession");
			if (iDlist.getLength() == 0)
				continue;

			CompoundIdentity cid = new CompoundIdentity(cpdName, molecularFormula);
			String metlinId = null;

			for (int j = 0; j < iDlist.getLength(); j++) {

				Element idElement = (Element) iDlist.item(j);
				String database = idElement.getAttribute("db").trim();
				String accession = idElement.getAttribute("id").trim();
				if (!database.isEmpty() && !accession.isEmpty()) {

					CompoundDatabaseEnum db = AgilentDatabaseFields.getDatabaseByName(database);
					if(db == null)
						db = CompoundDatabaseEnum.getCompoundDatabaseByName(database);

					if(db != null) {

						if(db.equals(CompoundDatabaseEnum.METLIN)) {
							metlinId = "METLIN:" + accession;
							accession = metlinId;
						}
						if(db.equals(CompoundDatabaseEnum.HMDB))
							accession = accession.replace("HMDB", "HMDB00");

						cid.addDbId(db, accession);
					}
				}
			}
			CompoundIdentity mappedId =
				CompoundDatabaseUtils.mapLibraryCompoundIdentity(cid, conn);

			if(mappedId == null) {
				mappedId = cid;
				missingIdentities.add(cid);
			}
			//	Check if METLIN spectra are not in database yet and add them in
			if(metlinId == null)
				continue;

			//	To make sure METLIN ID is present in the original form
			mappedId.addDbId(CompoundDatabaseEnum.METLIN, metlinId.replace("METLIN:", ""));

			NodeList spectra = cpdElement.getElementsByTagName("Spectrum");
			TandemMassSpectrum msms = parseSpectrumNode(spectra);
			if(msms == null)
				continue;

			String spectrumHash = MsUtils.calculateSpectrumHash(msms.getSpectrum());

			//	Get existing METLIN spectra
			boolean msmsInDatabase = false;
			idPs.setString(1, metlinId.replace("METLIN:", ""));
			idPs.setDouble(2, msms.getCidLevel());
			idPs.setString(3, msms.getPolarity().getCode());
			ResultSet rs = idPs.executeQuery();
			while(rs.next()) {
				if(spectrumHash.equals(rs.getString("SPECTRUM_HASH"))) {
					msmsInDatabase = true;
					break;
				}
			}
			rs.close();
			if(!msmsInDatabase) {

				if(!idSpectrumMap.containsKey(mappedId))
					idSpectrumMap.put(mappedId, new HashSet<TandemMassSpectrum>());

				TandemMassSpectrum existingMsms = idSpectrumMap.get(mappedId).stream().
					filter(s -> s.getPolarity().equals(msms.getPolarity())).
					filter(s -> s.getCidLevel() == msms.getCidLevel()).findFirst().orElse(null);

				if(existingMsms == null)
					idSpectrumMap.get(mappedId).add(msms);
			}
		}
		idPs.close();
		ConnectionManager.releaseConnection(conn);
	}

	private TandemMassSpectrum parseSpectrumNode(NodeList spectra) {

		Polarity pol = null;
		String collisionEnergy = null;
		TandemMassSpectrum msms = null;

		for (int j = 0; j < spectra.getLength(); j++) {

			Element spectrumElement = (Element) spectra.item(j);
			//	Get collision energy from experimental spectrum
			if(spectrumElement.getAttribute("type").equals(AgilentCefFields.MS2_SPECTRUM.getName())) {

				Element msDetails = (Element) spectrumElement.getElementsByTagName("MSDetails").item(0);
				String sign = msDetails.getAttribute("p");

				if(sign.equals("+"))
					pol = Polarity.Positive;

				if(sign.equals("-"))
					pol = Polarity.Negative;

				collisionEnergy = msDetails.getAttribute("ce").replaceAll("V", "");
			}
		}
		//	Parse library spectrum
		for (int j = 0; j < spectra.getLength(); j++) {

			Element spectrumElement = (Element) spectra.item(j);
			if(spectrumElement.getAttribute("type").equals(AgilentCefFields.LIBRARY_MS2_SPECTRUM.getName())) {

				NodeList precursors = spectrumElement.getElementsByTagName("mz");
				double mz = Double.parseDouble(precursors.item(0).getFirstChild().getNodeValue());
				msms = new TandemMassSpectrum(2, new MsPoint(mz, 999.0d), pol);
				msms.setSpectrumSource(SpectrumSource.LIBRARY);
				NodeList msmsPeaks = spectrumElement.getElementsByTagName("p");
				Collection<MsPoint>msmsPoints = new ArrayList<MsPoint>();
				for (int k = 0; k < msmsPeaks.getLength(); k++) {

					Element peakElement = (Element) msmsPeaks.item(k);
					msmsPoints.add(new MsPoint(
							Double.parseDouble(peakElement.getAttribute("x")),
							Double.parseDouble(peakElement.getAttribute("y"))));
				}
				msms.setSpectrum(msmsPoints);
				msms.setPolarity(pol);
				if(!collisionEnergy.isEmpty()) {
					double ce = Double.parseDouble(collisionEnergy);
					msms.setCidLevel(ce);
				}
			}
		}
		return msms;
	}

	@Override
	public Task cloneTask() {
		return new MSMSSearchResultsPrescanTask(inputFile);
	}

	/**
	 * @return the missingIdentities
	 */
	public Collection<CompoundIdentity> getMissingIdentities() {
		return missingIdentities;
	}

	/**
	 * @return the idSpectrumMap
	 */
	public Map<CompoundIdentity, Collection<TandemMassSpectrum>> getIdSpectrumMap() {
		return idSpectrumMap;
	}
}
