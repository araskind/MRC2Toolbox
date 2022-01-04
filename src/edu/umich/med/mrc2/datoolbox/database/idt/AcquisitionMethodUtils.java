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

package edu.umich.med.mrc2.datoolbox.database.idt;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.TreeSet;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;

import edu.umich.med.mrc2.datoolbox.data.IonizationType;
import edu.umich.med.mrc2.datoolbox.data.MassAnalyzerType;
import edu.umich.med.mrc2.datoolbox.data.MsType;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicSeparationType;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSChromatographicColumn;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.Manufacturer;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.utils.CompressionUtils;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class AcquisitionMethodUtils {

	/*
	 * Acquisition Method
	 * */
	public static String addNewAcquisitionMethod(
			DataAcquisitionMethod selectedMethod, File methodFile) throws Exception{

		//	TODO insert or connect gradient if present
		LIMSUser sysUser = MRC2ToolBoxCore.getIdTrackerUser();
		if(sysUser == null)
			return null;

		Connection conn = ConnectionManager.getConnection();
		String newId = SQLUtils.getNextIdFromSequence(conn, 
				"ID_DATA_ACQ_METHOD_SEQ",
				DataPrefix.DATA_ACQUISITION_METHOD,
				"0",
				4);
		selectedMethod.setId(newId);	
		String query  =
			"INSERT INTO DATA_ACQUISITION_METHOD (ACQ_METHOD_ID, METHOD_NAME, " +
			"METHOD_DESCRIPTION, POLARITY, CREATED_BY, CREATED_ON, IONIZATION_TYPE, " +
			"MASS_ANALYZER, MS_TYPE, COLUMN_ID, METHOD_CONTAINER, SEPARATION_TYPE) " +
			"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, selectedMethod.getId());
		ps.setString(2, selectedMethod.getName());
		ps.setString(3, selectedMethod.getDescription());
		ps.setString(4, selectedMethod.getPolarity().getCode());
		ps.setString(5, sysUser.getId());
		ps.setDate(6, new java.sql.Date(new java.util.Date().getTime()));	
		ps.setString(7, selectedMethod.getIonizationType().getId());
		ps.setString(8, selectedMethod.getMassAnalyzerType().getId());
		ps.setString(9, selectedMethod.getMsType().getId());
		ps.setString(10, selectedMethod.getColumn().getColumnId());

		// Insert method file
		FileInputStream fis = null;
		File archive = null;
		int streamLength = 0;
		if(methodFile.exists()) {

			archive = FIOUtils.changeExtension(methodFile, "zip");
			if(methodFile.isDirectory())
				CompressionUtils.zipFolder(methodFile, archive);
			else
				CompressionUtils.zipFile(methodFile, archive);

			if(archive.exists()) {
				fis = new FileInputStream(archive);
				streamLength = (int) archive.length();
			}
			if(fis != null)
				ps.setBinaryStream(11, fis, streamLength);
			else
				ps.setBinaryStream(11, null, 0);
		} else {
			ps.setBinaryStream(11, null, 0);
		}
		ps.setString(12, selectedMethod.getSeparationType().getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);

		if(fis != null)
			fis.close();

		if(archive != null) {
			Path path = Paths.get(archive.getAbsolutePath());
	        Files.delete(path);
		}
		return newId;
	}
	
//	public static String getNextAcquisitionMethodId(Connection conn) throws Exception{
//		
//		String nextId = SQLUtils.getNextIdFromSequence(conn, 
//				"ID_DATA_ACQ_METHOD_SEQ",
//				DataPrefix.DATA_ACQUISITION_METHOD,
//				"0",
//				4);
//		String query  =
//				"SELECT '" + DataPrefix.DATA_ACQUISITION_METHOD.getName() + 
//				"' || LPAD(ID_DATA_ACQ_METHOD_SEQ.NEXTVAL, 4, '0') AS NEXT_ID FROM DUAL";
//		
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while(rs.next()) {
//			nextId = rs.getString("NEXT_ID");
//		}
//		rs.close();
//		ps.close();	
//		return nextId;
//	}

	public static void updateAcquisitionMethod(DataAcquisitionMethod selectedMethod, File methodFile) throws Exception{

		//	TODO update gradient if present
		Connection conn = ConnectionManager.getConnection();
		String query  = null;
		if(methodFile == null) {
			query  =
				"UPDATE DATA_ACQUISITION_METHOD SET METHOD_NAME = ?, METHOD_DESCRIPTION = ?, " +
				"POLARITY = ?, IONIZATION_TYPE = ?, MASS_ANALYZER = ?, MS_TYPE =?, "
				+ "COLUMN_ID = ?, SEPARATION_TYPE = ? " +
				"WHERE ACQ_METHOD_ID = ?";
		}
		else {
			query  =
				"UPDATE DATA_ACQUISITION_METHOD SET METHOD_NAME = ?, METHOD_DESCRIPTION = ?, " +
				"POLARITY = ?, IONIZATION_TYPE = ?, MASS_ANALYZER = ?, MS_TYPE =?, COLUMN_ID = ?, "
				+ "METHOD_CONTAINER = ?, SEPARATION_TYPE = ? " +
				"WHERE ACQ_METHOD_ID = ?";
		}
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, selectedMethod.getName());
		ps.setString(2, selectedMethod.getDescription());
		ps.setString(3, selectedMethod.getPolarity().getCode());
		ps.setString(4, selectedMethod.getIonizationType().getId());
		ps.setString(5, selectedMethod.getMassAnalyzerType().getId());
		ps.setString(6, selectedMethod.getMsType().getId());
		ps.setString(7, selectedMethod.getColumn().getColumnId());

		FileInputStream fis = null;
		File archive = null;

		if(methodFile == null) {
			ps.setString(8, selectedMethod.getSeparationType().getId());
			ps.setString(9, selectedMethod.getId());
		}
		else {
			// Insert new method file
			int streamLength = 0;
			if(methodFile.exists()) {

				archive = FIOUtils.changeExtension(methodFile, "zip");
				if(methodFile.isDirectory())
					CompressionUtils.zipFolder(methodFile, archive);
				else
					CompressionUtils.zipFile(methodFile, archive);

				if(archive.exists()) {
					fis = new FileInputStream(archive);
					streamLength = (int) archive.length();
				}
				if(fis != null)
					ps.setBinaryStream(8, fis, streamLength);
				else
					ps.setBinaryStream(8, null, 0);
			} else {
				ps.setBinaryStream(8, null, 0);
			}
			ps.setString(9, selectedMethod.getSeparationType().getId());
			ps.setString(10, selectedMethod.getId());
		}
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);

		if(fis != null)
			fis.close();

		if(archive != null) {
			Path path = Paths.get(archive.getAbsolutePath());
	        Files.delete(path);
		}
	}
	
	public static void deleteAcquisitionMethod(DataAcquisitionMethod selectedMethod) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM DATA_ACQUISITION_METHOD WHERE ACQ_METHOD_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, selectedMethod.getId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static void getAcquisitionMethodFile(DataAcquisitionMethod selectedMethod, File destinationFolder)  throws Exception{

		//	Get zip from database
		Connection conn = ConnectionManager.getConnection();
		String query = "SELECT METHOD_CONTAINER FROM DATA_ACQUISITION_METHOD WHERE ACQ_METHOD_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, selectedMethod.getId());
		ResultSet rs = ps.executeQuery();
		File zipFile = Paths.get(destinationFolder.getAbsolutePath(), selectedMethod.getName() + ".zip").toFile();
		while (rs.next()) {
			BufferedInputStream is = new BufferedInputStream(rs.getBinaryStream("METHOD_CONTAINER"));
			FileOutputStream fos = new FileOutputStream(zipFile);
			byte[] buffer = new byte[2048];
			int r = 0;
			try {
				while ((r = is.read(buffer)) != -1) {
					fos.write(buffer, 0, r);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fos.flush();
			fos.close();
			is.close();
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);

		//	Extract archive and delete zip;
		if(zipFile.exists()) {

            ZipArchiveInputStream zipStream = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            ZipArchiveEntry entry;
            FileOutputStream fos;
            while ((entry = zipStream.getNextZipEntry()) != null) {

                if (entry.isDirectory())
                	continue;

                File curfile = new File(destinationFolder, entry.getName());
                File parent = curfile.getParentFile();
                if (!parent.exists())
                    parent.mkdirs();

                fos = new FileOutputStream(curfile);
                IOUtils.copy(zipStream, fos);
                fos.close();
            }
            zipStream.close();
    		Path path = Paths.get(zipFile.getAbsolutePath());
    	    Files.delete(path);
		}
	}
	
	public static Collection<DataAcquisitionMethod>getAcquisitionMethodList() throws Exception{

		Collection<DataAcquisitionMethod>methodList = new TreeSet<DataAcquisitionMethod>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT ACQ_METHOD_ID, METHOD_NAME, METHOD_DESCRIPTION, POLARITY, CREATED_BY, " +
			"CREATED_ON, IONIZATION_TYPE, MASS_ANALYZER, MS_TYPE, COLUMN_ID, SEPARATION_TYPE " +
			"FROM DATA_ACQUISITION_METHOD ORDER BY 1 ";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			String userId = rs.getString("CREATED_BY");
			LIMSUser createdBy = null;
			if(userId != null)
				createdBy = IDTDataCash.getUserById(userId);
			
			Date createdOn = null;
			if(rs.getDate("CREATED_ON") != null)
				createdOn = new Date(rs.getDate("CREATED_ON").getTime());

			DataAcquisitionMethod method = new DataAcquisitionMethod(
					rs.getString("ACQ_METHOD_ID"),
					rs.getString("METHOD_NAME"),
					rs.getString("METHOD_DESCRIPTION"),
					createdBy,
					createdOn);

			method.setPolarity(Polarity.getPolarityByCode(rs.getString("POLARITY")));
			method.setCreatedBy(IDTDataCash.getUserById(rs.getString("CREATED_BY")));
			method.setColumn(IDTDataCash.getColumnById(rs.getString("COLUMN_ID")));
			method.setIonizationType(IDTDataCash.getIonizationTypeById(rs.getString("IONIZATION_TYPE")));
			method.setMassAnalyzerType(IDTDataCash.getMassAnalyzerTypeById(rs.getString("MASS_ANALYZER")));
			method.setMsType(IDTDataCash.getMsTypeById(rs.getString("MS_TYPE")));
			method.setSeparationType(IDTDataCash.getChromatographicSeparationTypeById(rs.getString("SEPARATION_TYPE")));

			methodList.add(method);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return methodList;
	}
	
	/*
	 * Chromatographic column
	 * */
	public static void addNewChromatographicColumn(
			LIMSChromatographicColumn column) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String query  =
			"INSERT INTO CHROMATOGRAPHIC_COLUMN(COLUMN_ID, COLUMN_NAME, "
			+ "SEPARATION_TYPE, CHEMISTRY, MANUFACTURER, CATALOG_NUMBER) " +
			"VALUES(?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);
		String nextId = SQLUtils.getNextIdFromSequence(conn, 
				"CHROM_COL_SEQ",
				DataPrefix.CROMATOGRAPHIC_COLUMN,
				"0",
				4);
		column.setColumnId(nextId);
		ps.setString(1, nextId);
		ps.setString(2, column.getColumnName());
		ps.setString(3, column.getSeparationType().getId());
		ps.setString(4, column.getChemistry());
		ps.setString(5, column.getManufacturer().getName());
		ps.setString(6, column.getCatalogNumber());

		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void updateChromatographicColumn(
			LIMSChromatographicColumn column) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String query  =
			"UPDATE CHROMATOGRAPHIC_COLUMN SET COLUMN_NAME = ?, SEPARATION_TYPE = ?, "+
			"CHEMISTRY = ?, MANUFACTURER = ?, CATALOG_NUMBER = ? " +
			"WHERE COLUMN_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);

		ps.setString(1, column.getColumnName());
		ps.setString(2, column.getSeparationType().getId());
		ps.setString(3, column.getChemistry());
		ps.setString(4, column.getManufacturer().getName());
		ps.setString(5, column.getCatalogNumber());
		ps.setString(6, column.getColumnId());

		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteChromatographicColumn(LIMSChromatographicColumn column) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM CHROMATOGRAPHIC_COLUMN WHERE COLUMN_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, column.getColumnId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}

	public static Collection<LIMSChromatographicColumn>getChromatographicColumnList() throws Exception{

		Collection<LIMSChromatographicColumn>chromatographicColumnsList = 
				new TreeSet<LIMSChromatographicColumn>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT COLUMN_ID, SEPARATION_TYPE, COLUMN_NAME, "
			+ "CHEMISTRY, MANUFACTURER, CATALOG_NUMBER " +
			"FROM CHROMATOGRAPHIC_COLUMN ORDER BY 1 ";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			LIMSChromatographicColumn column = new LIMSChromatographicColumn(
					rs.getString("COLUMN_ID"),
					rs.getString("COLUMN_NAME"),
					rs.getString("CHEMISTRY"),
					rs.getString("CATALOG_NUMBER"));

			String sepType = rs.getString("SEPARATION_TYPE");
			Optional<ChromatographicSeparationType> chromatographicSeparationType =
				IDTDataCash.getChromatographicSeparationTypes().stream().
				filter(t -> t.getId().equals(sepType)).findFirst();
			if(chromatographicSeparationType.isPresent())
				column.setSeparationType(chromatographicSeparationType.get());

			String mnfct = rs.getString("MANUFACTURER");
			Optional<Manufacturer> manufacturer =
				IDTDataCash.getManufacturers().stream().
					filter(m -> m.getName().equals(mnfct)).findFirst();
			if(manufacturer.isPresent())
				column.setManufacturer(manufacturer.get());

			chromatographicColumnsList.add(column);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return chromatographicColumnsList;
	}
	
	public static Collection<ChromatographicSeparationType> getChromatographicSeparationTypes() throws Exception{
		
		Connection conn = ConnectionManager.getConnection();
		Collection<ChromatographicSeparationType>separationTypes = 
				getChromatographicSeparationTypes(conn);
		ConnectionManager.releaseConnection(conn);
		return separationTypes;
	}
	
	public static Collection<ChromatographicSeparationType> 
			getChromatographicSeparationTypes(Connection conn) throws Exception{

		Collection<ChromatographicSeparationType>separationTypes = 
				new TreeSet<ChromatographicSeparationType>();
		String query  =
			"SELECT SEPARATION_TYPE, DESCRIPTION FROM SEPARATION_TYPE ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			ChromatographicSeparationType method = 
					new ChromatographicSeparationType(
						rs.getString("SEPARATION_TYPE"),
						rs.getString("DESCRIPTION"));
			separationTypes.add(method);
		}
		rs.close();
		ps.close();
		return separationTypes;
	}

	public static Collection<IonizationType> getIonizationTypes() throws Exception{

		Collection<IonizationType> ionizationTypes = 
				new TreeSet<IonizationType>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT IONIZATION_TYPE_ID, IT_DESCRIPTION FROM IONIZATION_TYPE ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			IonizationType it = new IonizationType(
					rs.getString("IONIZATION_TYPE_ID"),
					rs.getString("IT_DESCRIPTION"));
			ionizationTypes.add(it);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return ionizationTypes;
	}

	public static Collection<? extends MassAnalyzerType> getMassAnalyzerTypes() throws Exception{

		Collection<MassAnalyzerType> massAnalyzerTypes = 
				new TreeSet<MassAnalyzerType>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT ANALYZER, DESCRIPTION FROM MASS_ANALYZER ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			MassAnalyzerType ma = new MassAnalyzerType(
					rs.getString("ANALYZER"),
					rs.getString("DESCRIPTION"));
			massAnalyzerTypes.add(ma);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return massAnalyzerTypes;
	}

	public static Collection<MsType> getMsTypes() throws Exception{

		Collection<MsType> msTypes = new TreeSet<MsType>();
		Connection conn = ConnectionManager.getConnection();
		String query  =
			"SELECT MS_TYPE, DESCRIPTION FROM MS_TYPE ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			MsType ma = new MsType(
					rs.getString("MS_TYPE"),
					rs.getString("DESCRIPTION"));

			msTypes.add(ma);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return msTypes;
	}
	
	/*
	 * Instrument
	 * */
	public static void addNewInstrument(LIMSInstrument instrument) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String nextId = SQLUtils.getNextIdFromSequence(conn, 
				"INSTRUMENT_ID_SEQ",
				DataPrefix.INSTRUMENT,
				"0",
				4);
		instrument.setInstrumentId(nextId);
		String query  =
			"INSERT INTO INSTRUMENT(INSTRUMENT_ID, NAME, DESCRIPTION, MANUFACTURER, "
			+ "MODEL, SERIAL_NUMBER, SEPARATION_TYPE, MASS_ANALYZER) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement ps = conn.prepareStatement(query);

		ps.setString(1, instrument.getInstrumentId());
		ps.setString(2, instrument.getInstrumentName());
		ps.setString(3, instrument.getDescription());
		ps.setString(4, instrument.getManufacturer());
		ps.setString(5, instrument.getModel());
		ps.setString(6, instrument.getSerialNumber());
		ps.setString(7, instrument.getChromatographicSeparationType().getId());
		ps.setString(8, instrument.getMassAnalyzerType().getId());

		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
//	public static String getNextInstrumentId(Connection conn) throws Exception{
//		
//		String nextId = null;
//		String query  =
//				"SELECT '" + DataPrefix.INSTRUMENT.getName() + 
//				"' || LPAD(INSTRUMENT_ID_SEQ.NEXTVAL, 4, '0') AS NEXT_ID FROM DUAL";
//		
//		PreparedStatement ps = conn.prepareStatement(query);
//		ResultSet rs = ps.executeQuery();
//		while(rs.next()) {
//			nextId = rs.getString("NEXT_ID");
//		}
//		rs.close();
//		ps.close();	
//		return nextId;
//	}

	public static void updateInstrument(LIMSInstrument instrument) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String query  =
				"UPDATE INSTRUMENT SET NAME = ?, DESCRIPTION = ?, MANUFACTURER = ?, "
				+ "MODEL = ?, SERIAL_NUMBER = ?, SEPARATION_TYPE = ?, MASS_ANALYZER = ? " +
				"WHERE INSTRUMENT_ID = ?";
		
		PreparedStatement ps = conn.prepareStatement(query);	
		ps.setString(1, instrument.getInstrumentName());
		ps.setString(2, instrument.getDescription());
		ps.setString(3, instrument.getManufacturer());
		ps.setString(4, instrument.getModel());
		ps.setString(5, instrument.getSerialNumber());
		ps.setString(6, instrument.getChromatographicSeparationType().getId());
		ps.setString(7, instrument.getMassAnalyzerType().getId());
		ps.setString(8, instrument.getInstrumentId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static void deleteInstrument(LIMSInstrument instrument) throws Exception{

		Connection conn = ConnectionManager.getConnection();
		String query = "DELETE FROM INSTRUMENT WHERE INSTRUMENT_ID = ?";
		PreparedStatement ps = conn.prepareStatement(query);
		ps.setString(1, instrument.getInstrumentId());
		ps.executeUpdate();
		ps.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	public static Collection<LIMSInstrument>getInstrumentList() throws Exception{

		Collection<LIMSInstrument>instruments = new ArrayList<LIMSInstrument>();
		Connection conn = ConnectionManager.getConnection();
		String query =
			"SELECT INSTRUMENT_ID, NAME, DESCRIPTION, MANUFACTURER, "
			+ "MODEL, SERIAL_NUMBER, SEPARATION_TYPE, MASS_ANALYZER " +
			"FROM INSTRUMENT ORDER BY 1 ";
		PreparedStatement ps = conn.prepareStatement(query);
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			
			MassAnalyzerType massAnalyzerType = 
					IDTDataCash.getMassAnalyzerTypeById(rs.getString("MASS_ANALYZER"));
			ChromatographicSeparationType separationType = 
					IDTDataCash.getChromatographicSeparationTypeById(rs.getString("SEPARATION_TYPE"));

			LIMSInstrument st = new LIMSInstrument(
					rs.getString("INSTRUMENT_ID"),
					rs.getString("NAME"),
					rs.getString("DESCRIPTION"),
					massAnalyzerType,
					separationType,
					rs.getString("MANUFACTURER"),
					rs.getString("MODEL"),
					rs.getString("SERIAL_NUMBER"));
			
			instruments.add(st);
		}
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);
		return instruments;
	}
}














