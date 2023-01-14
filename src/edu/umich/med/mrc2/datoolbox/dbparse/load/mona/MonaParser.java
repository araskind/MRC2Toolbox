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

package edu.umich.med.mrc2.datoolbox.dbparse.load.mona;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmiFlavor;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDbConnectionManager;
import net.sf.jniinchi.INCHI_RET;

public class MonaParser {

	private static final String COMMENT_FIELD = "COMMENT";
	private static final String MSMS_ELEMENT = "MASS SPECTRAL PEAKS";
	private static Pattern COMMENT_PATTERN = Pattern.compile("^([^=]+)=(.+)$");
    private static Pattern MDL_VERSION = Pattern.compile("[vV](2000|3000)");
    private static String  M_END = "M  END";
    private static String  MONA_M_END = ">  <";
    private static String  SDF_RECORD_SEPARATOR = "$$$$";
    private static String  SDF_DATA_HEADER      = "> ";

	private static int totalCount = 0;
	private static Connection conn;
	private static TreeSet<String>fields;

	private static final IChemObjectBuilder builder = SilentChemObjectBuilder.getInstance();
	private static final SmilesGenerator smilesGenerator = new SmilesGenerator(SmiFlavor.Isomeric);
	private static final MDLV2000Reader mdlReader = new MDLV2000Reader();
	private static InChIGeneratorFactory igfactory;
	private static InChIGenerator inChIGenerator;

	//	TODO update connection manager if needed to re-parse the MONA data
	public MonaParser() {
		super();
		try {
			conn = CompoundDbConnectionManager.getConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fields = new TreeSet<String>();

	}

	public static void initParser() {
		try {
			conn = CompoundDbConnectionManager.getConnection();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			igfactory = InChIGeneratorFactory.getInstance();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static List<List<String>> pareseInputFile(File inputFile) throws Exception {

		List<List<String>> sdfChunks = new ArrayList<List<String>>();
		List<String> chunk = new ArrayList<String>();
		try (BufferedReader br =
			Files.newBufferedReader(
				Paths.get(inputFile.getAbsolutePath()), StandardCharsets.UTF_8)) {

		    for (String line = null; (line = br.readLine()) != null;) {

//				if (line.trim().isEmpty())
//					continue;

				if(line.trim().equals(SDF_RECORD_SEPARATOR)) {
					sdfChunks.add(chunk);
					if(sdfChunks.size() == 500)
						uploadDataBatch(sdfChunks);

					chunk = new ArrayList<String>();
					totalCount++;
					continue;
				}
				else
					chunk.add(line);
		    }
		}
	    try {
	    	uploadDataBatch(sdfChunks);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(Integer.toString(totalCount) + " records");
		return sdfChunks;
	}

	private static void uploadDataBatch(List<List<String>> sdfChunks) throws SQLException {

		Collection<IAtomContainer>molecules = new ArrayList<IAtomContainer>();
		for(List<String>chunk : sdfChunks) {
			IAtomContainer mol = null;
			try {
				mol = parseChunk(chunk);
			} catch (CDKException e) {
				e.printStackTrace();
			}
			if(mol != null)
				molecules.add(mol);
			else {
				System.out.println(Arrays.toString(chunk.toArray(new String[chunk.size()])));
				System.out.println("***");
			}
		}
		//	insertMonaRecords(molecules);
		updateMonaRecords(molecules);
		sdfChunks.clear();
	}

	private static IAtomContainer parseChunk(List<String> chunk) throws CDKException {
		List<String>mdlMol = new ArrayList<String>();
		IAtomContainer molecule = null;
		int propertyStart = 0;
		for(int i=0; i<chunk.size(); i++) {

			if(chunk.get(i).startsWith(MONA_M_END)) {

				String mol =
					StringUtils.join(mdlMol, "\n").
					replaceFirst("^(\\s*\\n){2,}\\s+CDK", "\n CDK").
					replaceFirst("^(\\s*\\n){2,}\\s+Mrv", "\n Mrv");
				mdlReader.setReader(new StringReader(mol));
				molecule = mdlReader.read(builder.newAtomContainer());
				propertyStart = i;
				break;
			}
			else
				mdlMol.add(chunk.get(i));
		}
		if(molecule == null)
			return null;

		//	Add properties
		StringBuilder sb = new StringBuilder();
		for(int i=propertyStart; i<chunk.size(); i++) {

			if(chunk.get(i).startsWith(SDF_DATA_HEADER)){
				String fieldName = extractFieldName(chunk.get(i));
				String fieldValue = extractFieldData(sb, chunk, i+1);
				if(!fieldValue.isEmpty())
					molecule.setProperty(fieldName, fieldValue);

				i += fieldValue.split("\r\n|\r|\n").length;
			}
		}
		//	Parse comments
		String commentsBlock = (String) molecule.getProperties().get(COMMENT_FIELD);
		Map<String,String>comments = parseComments(commentsBlock);
		for(Entry<String, String> comment : comments.entrySet())
			molecule.setProperty(comment.getKey(), comment.getValue());

		//	Check/add SMILES
		if(molecule.getProperty(MonaNameFields.SMILES.getName()) == null
				&& molecule.getProperty("computed SMILES") != null) {

			molecule.setProperty(MonaNameFields.SMILES.getName(), molecule.getProperty("computed SMILES"));
		}
		if(molecule.getProperty(MonaNameFields.SMILES.getName()) == null) {
			String smiles = null;
			try {
				smiles = smilesGenerator.create(molecule);
			} catch (CDKException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(smiles != null)
				molecule.setProperty(MonaNameFields.SMILES.getName(), smiles);
		}
		//	Check/add InChi key
		if(molecule.getProperty(MonaNameFields.INCHIKEY.getName()) == null) {
			try {
				inChIGenerator = igfactory.getInChIGenerator(molecule);
				INCHI_RET ret = inChIGenerator.getReturnStatus();
				if (ret == INCHI_RET.WARNING || ret == INCHI_RET.OKAY)
					molecule.setProperty(MonaNameFields.INCHIKEY.getName(), inChIGenerator.getInchiKey());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return molecule;
	}

    private static String extractFieldData(StringBuilder data, List<String> chunk, int start)  {
        data.setLength(0);
        for(int i=start; i<chunk.size(); i++) {

        	if(chunk.get(i).startsWith(SDF_DATA_HEADER))
        		return data.toString();
        	else {
        		if(!chunk.get(i).trim().isEmpty()) {
        			if(data.length() > 0)
        				data.append("\n");

        			data.append(chunk.get(i));
        		}
        	}
        }
        return data.toString();
    }

    private static String extractFieldName(String str) {
        int index = str.indexOf('<');
        if (index != -1) {
            int index2 = str.indexOf('>', index);
            if (index2 != -1) {
                return str.substring(index + 1, index2);
            }
        }
        return null;
    }

	public void readFieldsFromMolecule(IAtomContainer molecule) {
		molecule.getProperties().forEach((k,v)->{
			fields.add(k.toString());
			if(k.toString().equals(COMMENT_FIELD)) {
				Map<String,String>comments = parseComments(v.toString());
				fields.addAll(comments.keySet());
			}
		});
	}

	private static Map<String,String> parseComments(String commentsBlock){

		Map<String,String>comments = new TreeMap<String,String>();
		if(commentsBlock == null)
			return comments;

		String[] lines = commentsBlock.split("\\r?\\n");
		for(String line : lines) {

            Matcher commentMatcher = COMMENT_PATTERN.matcher(line);
            if (commentMatcher.find())
            	comments.put(commentMatcher.group(1), commentMatcher.group(2));
		}
		return comments;
	}

	public static void updateMonaRecords(Collection<IAtomContainer>molecules) throws SQLException {

		String dataQuery =
				"UPDATE MONA_SPECTRUM_METADATA SET INSTRUMENT_TYPE = ? WHERE ID = ?";

		PreparedStatement ps = conn.prepareStatement(dataQuery);
		for(IAtomContainer molecule : molecules) {

			IMolecularFormula cdkFormula = MolecularFormulaManipulator.getMolecularFormula(molecule);
			String id = (String) molecule.getProperties().get(MonaNameFields.ID.getName());
			String iType = (String) molecule.getProperties().get(MonaNameFields.INSTRUMENT_TYPE.getName());
			if(iType == null)
				iType = (String) molecule.getProperties().get("spectrum analyzer type");

			ps.setString(1, iType);
			ps.setString(2, id);
			ps.addBatch();
		}
		ps.executeBatch();
		ps.clearBatch();
		ps.close();
	}


	public static void insertMonaRecords(Collection<IAtomContainer>molecules) throws SQLException {

		String dataQuery =
				"INSERT INTO MONA_SPECTRUM_METADATA " +
				"(ID, NAME, FORMULA, INCHIKEY, SMILES, EXACT_MASS, PRECURSOR_MZ, NUM_PEAKS, PRECURSOR_TYPE, " +
				"COLLISION_ENERGY, CONTRIBUTOR, INSTRUMENT, INSTRUMENT_TYPE, ION_MODE, SPECTRUM_TYPE, SYNONYMS, SPLASH) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(dataQuery);

		//	Spectrum metadata
		for(IAtomContainer molecule : molecules) {

			IMolecularFormula cdkFormula = MolecularFormulaManipulator.getMolecularFormula(molecule);
			String id = (String) molecule.getProperties().get(MonaNameFields.ID.getName());
			String formula = (String) molecule.getProperties().get(MonaNameFields.FORMULA.getName());
			if(formula == null)
				formula = MolecularFormulaManipulator.getString(cdkFormula);

			ps.setString(1, id);
			ps.setString(2, (String) molecule.getProperties().get(MonaNameFields.NAME.getName()));
			ps.setString(3, formula);
			ps.setString(4, (String) molecule.getProperties().get(MonaNameFields.INCHIKEY.getName()));
			ps.setString(5, (String) molecule.getProperties().get(MonaNameFields.SMILES.getName()));

			double exactMass = MolecularFormulaManipulator.getMajorIsotopeMass(cdkFormula);
			ps.setDouble(6, exactMass);

			double precursorMz = 0.0d;
			String precursorMzString = (String) molecule.getProperties().get(MonaNameFields.PRECURSOR_MZ.getName());
			if(precursorMzString != null) {
				try {
					precursorMz = Double.parseDouble(precursorMzString.split("/|\\s+")[0]);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
			ps.setDouble(7, precursorMz);
			int numP = 0;
			try {
				numP = Integer.parseInt((String) molecule.getProperties().get(MonaNameFields.NUM_PEAKS.getName()));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			ps.setInt(8, Integer.parseInt((String) molecule.getProperties().get(MonaNameFields.NUM_PEAKS.getName())));
			ps.setString(9, (String) molecule.getProperties().get(MonaNameFields.PRECURSOR_TYPE.getName()));
			ps.setString(10, (String) molecule.getProperties().get(MonaNameFields.COLLISION_ENERGY.getName()));
			ps.setString(11, (String) molecule.getProperties().get(MonaNameFields.CONTRIBUTOR.getName()));
			ps.setString(12, (String) molecule.getProperties().get(MonaNameFields.INSTRUMENT.getName()));
			ps.setString(13, (String) molecule.getProperties().get(MonaNameFields.INSTRUMENT_TYPE.getName()));
			ps.setString(14, (String) molecule.getProperties().get(MonaNameFields.ION_MODE.getName()));
			ps.setString(15, (String) molecule.getProperties().get(MonaNameFields.SPECTRUM_TYPE.getName()));
			ps.setString(16, (String) molecule.getProperties().get(MonaNameFields.SYNONYMS.getName()));
			ps.setString(17, (String) molecule.getProperties().get(MonaNameFields.SPLASH.getName()));

			ps.addBatch();
		}
		ps.executeBatch();
		ps.clearBatch();

		//	MSMS
		dataQuery =
			"INSERT INTO MONA_PEAK " +
			"(ID, MZ, INTENSITY, IS_PARENT) " +
			"VALUES(?, ?, ?, ?)";

		ps = conn.prepareStatement(dataQuery);
		for(IAtomContainer molecule : molecules) {

			String msmsBlock = (String) molecule.getProperties().get(MSMS_ELEMENT);
			String id = (String) molecule.getProperties().get(MonaNameFields.ID.getName());
			double precursorMz = 0.0d;
			String precursorMzString = (String) molecule.getProperties().get(MonaNameFields.PRECURSOR_MZ.getName());
			if(precursorMzString != null) {
				try {
					precursorMz = Double.parseDouble(precursorMzString.split("/|\\s+")[0]);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(msmsBlock != null) {

				ps.setString(1, id);

				//	Precursor
				if(precursorMz > 0.0d) {
					ps.setDouble(2, precursorMz);
					ps.setDouble(3, 100.0);
					ps.setString(4, "Y");
					ps.addBatch();
				}
				String[] miPairs = msmsBlock.trim().split("\\r?\\n");
				for(String pair : miPairs) {

					String[] mi = pair.trim().split("\\s+");
					double intensity = Double.parseDouble(mi[1]);
					if(intensity > 0) {

						ps.setDouble(2, Double.parseDouble(mi[0]));
						ps.setDouble(3, Double.parseDouble(mi[1]));
						ps.setString(4, "N");
						ps.addBatch();
					}
				}
			}
		}
		ps.executeBatch();
		ps.clearBatch();

		//	Comments
		dataQuery =
			"INSERT INTO MONA_MSMS_PROPERTIES " +
			"(ID, PROPERTY_NAME, PROPERTY_VALUE) " +
			"VALUES(?, ?, ?)";

		ps = conn.prepareStatement(dataQuery);
		for(IAtomContainer molecule : molecules) {

			String commentsBlock = (String) molecule.getProperties().get(COMMENT_FIELD);
			String id = (String) molecule.getProperties().get(MonaNameFields.ID.getName());
			ps.setString(1, id);
			Map<String,String>comments = parseComments(commentsBlock);
			for (Map.Entry<String, String> entry : comments.entrySet()) {

				ps.setString(2, entry.getKey());
				ps.setString(3, entry.getValue());
				ps.addBatch();
			}
		}
		ps.executeBatch();
		ps.clearBatch();
		ps.close();
	}

	public static void insertComments(String id, String commentsBlock) throws SQLException {

		String dataQuery =
				"INSERT INTO MONA_MSMS_PROPERTIES " +
				"(ID, PROPERTY_NAME, PROPERTY_VALUE) " +
				"VALUES(?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(dataQuery);
		ps.setString(1, id);

		Map<String,String>comments = parseComments(commentsBlock);
		for (Map.Entry<String, String> entry : comments.entrySet()) {

			ps.setString(2, entry.getKey());
			ps.setString(3, entry.getValue());
			ps.executeUpdate();
		}
		ps.close();
	}

	public static void insertMsMsData(String id, double precursorMz, String msmsBlock) throws SQLException {

		String dataQuery =
				"INSERT INTO MONA_PEAK " +
				"(ID, MZ, INTENSITY, IS_PARENT) " +
				"VALUES(?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(dataQuery);
		ps.setString(1, id);

		//	Precursor
		ps.setDouble(2, precursorMz);
		ps.setDouble(3, 100.0);
		ps.setString(4, "Y");
		ps.executeUpdate();

		String[] miPairs = msmsBlock.trim().split("\\r?\\n");
		for(String pair : miPairs) {

			String[] mi = pair.trim().split("\\s+");
			ps.setDouble(2, Double.parseDouble(mi[0]));
			ps.setDouble(3, Double.parseDouble(mi[1]));
			ps.setString(4, "N");
			ps.executeUpdate();
		}
		ps.close();
	}

	public static int getRecordCount(File sdfFile) throws IOException {

		int count = 0;
		try (BufferedReader br = Files.newBufferedReader(Paths.get(sdfFile.getAbsolutePath()), StandardCharsets.UTF_8)) {

		    for (String line = null; (line = br.readLine()) != null;) {

				if (line.trim().equals("$$$$"))
					count++;
		    }
		}
		return count;
	}

	/**
	 * @return the fields
	 */
	public TreeSet<String> getFields() {
		return fields;
	}
}
