package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.msms;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.MSMSDecoyGenerationMethod;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSLibraryUtils;
import edu.umich.med.mrc2.datoolbox.dbparse.load.nist.NISTMSPParser;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsImportUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.SQLUtils;

public class MSMSDecoyLibraryImportTask extends AbstractTask {
	
	private File libraryFile;
	private ReferenceMsMsLibrary refLibrary;
	private Polarity polarity;
	private MSMSDecoyGenerationMethod method;
	private Collection<TandemMassSpectrum>msmsDataSet;
	private Set<String>loadedDecoys;
	private String refLibraryId;
	private boolean append;

	public MSMSDecoyLibraryImportTask(
			File libraryFile, 
			ReferenceMsMsLibrary newLibrary, 
			Polarity polarity,
			MSMSDecoyGenerationMethod method,
			boolean append) {
		
		this.libraryFile = libraryFile;
		this.refLibrary = newLibrary;
		this.polarity = polarity;
		this.method = method;
		this.append = append;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		if(append && refLibrary != null) {
			try {
				getLoadedDecoys();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			parseLibraryFile();
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
//		setStatus(TaskStatus.FINISHED);
		
		//	Insert new library	
		if(!append) {
			refLibraryId = null;
			try {
				refLibraryId = MSMSLibraryUtils.insertNewReferenceMsMsLibrary(refLibrary);
			} catch (Exception e1) {
				
				e1.printStackTrace();
				setStatus(TaskStatus.ERROR);
			} 
		}
		else {
			refLibraryId = refLibrary.getUniqueId();
		}
		try {
			uploadLibraryComponentsToDatabase();
		} catch (Exception e) {

			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}				
		setStatus(TaskStatus.FINISHED);
	}
	
	private void getLoadedDecoys() throws Exception {
		
		loadedDecoys = new TreeSet<String>();
		Connection conn = ConnectionManager.getConnection();
		String sql = 
				"SELECT R.SOURCE_LIB_ID " +
				"FROM REF_MSMS_DECOY_CROSSREF R, " +
				"REF_MSMS_LIBRARY_COMPONENT C " +
				"WHERE R.DECOY_LIB_ID = C.MRC2_LIB_ID " +
				"AND C.LIBRARY_NAME = ? " +
				"AND C.POLARITY = ?  " +
				"ORDER BY 1";
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, refLibrary.getSearchOutputCode());
		ps.setString(2, polarity.getCode());
		ResultSet rs = ps.executeQuery();
		while(rs.next())
			loadedDecoys.add(rs.getString("SOURCE_LIB_ID"));
		
		rs.close();
		ps.close();
		ConnectionManager.releaseConnection(conn);		
	}
	
	private void parseLibraryFile() {
		
		taskDescription = "Reading MSP file";
		total = 100;
		processed = 20;		
		List<List<String>> mspChunks = NISTMSPParser.parseInputMspFile(libraryFile);
		msmsDataSet = new ArrayList<TandemMassSpectrum>();
		
		taskDescription = "Parsing MSP file";
		total = mspChunks.size();
		processed = 0;

		String libIdPattern = DataPrefix.MSMS_LIBRARY_ENTRY.getName() + "\\d{9}";
		for(List<String> chunk : mspChunks) {

			TandemMassSpectrum msms = null;
			try {
				msms = MsImportUtils.parseMspDataSource(chunk, polarity);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(msms != null && msms.getDescription() != null 
					&& msms.getDescription().matches(libIdPattern)) {								
					msmsDataSet.add(msms);
			}
			processed++;
		}
		if(append && loadedDecoys != null && !loadedDecoys.isEmpty())
			msmsDataSet = msmsDataSet.stream().
				filter(f -> !loadedDecoys.contains(f.getDescription())).
				collect(Collectors.toList());
	}
	
	private void uploadLibraryComponentsToDatabase() throws Exception {
		
		taskDescription = "Inserting library components";
		total = msmsDataSet.size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();
		System.out.println("Importing " + Integer.toString(msmsDataSet.size()) + 
				" entries into " + refLibrary.getName() + " library");
		
		//	ID
		String libId = null;
//		String idQuery = "SELECT '" + DataPrefix.MSMS_LIBRARY_ENTRY.getName() +
//			"' || LPAD(MSMS_LIB_ENTRY_SEQ.NEXTVAL, 9, '0') AS MRC2ID FROM DUAL";
//		PreparedStatement idps = conn.prepareStatement(idQuery);
//		ResultSet idrs = null;
		
		//	Compound ID
		String accession = null;
		String accessionQuery = 
				"SELECT ACCESSION, PRECURSOR_MZ, IONIZATION_TYPE, MAX_DIGITS "
				+ "FROM REF_MSMS_LIBRARY_COMPONENT WHERE MRC2_LIB_ID = ?";
		PreparedStatement accessionps = conn.prepareStatement(accessionQuery);
		ResultSet accessionrs = null;
		
		//	Component data
		String cQuery =
			"INSERT INTO REF_MSMS_LIBRARY_COMPONENT ( " +
			"MRC2_LIB_ID, POLARITY, IONIZATION, COLLISION_ENERGY, PRECURSOR_MZ, SPECTRUM_TYPE,  " +
			"SPECTRUM_SOURCE, IONIZATION_TYPE, LIBRARY_NAME, ORIGINAL_LIBRARY_ID, ACCESSION, SPECTRUM_HASH, ENTROPY, MAX_DIGITS) " +
			"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
		PreparedStatement cps = conn.prepareStatement(cQuery);
		
		// 	Spectrum peaks
		String specQuery =
			"INSERT INTO REF_MSMS_LIBRARY_PEAK (MRC2_LIB_ID, MZ, INTENSITY, IS_PARENT) " +
			"VALUES(?, ?, ?, ?) ";
		PreparedStatement specPs = conn.prepareStatement(specQuery);
		
		//	Cross-reference
		String crefQuery =
				"INSERT INTO REF_MSMS_DECOY_CROSSREF (DECOY_LIB_ID, SOURCE_LIB_ID, GENERATION_METHOD) " +
				"VALUES(?, ?, ?) ";
		PreparedStatement crefPs = conn.prepareStatement(crefQuery);
		crefPs.setString(3, method.getMethodId());
		
		String libraryName = refLibrary.getSearchOutputCode();
		
		for(TandemMassSpectrum msms : msmsDataSet) {
			
			if(isCanceled()) {
				
//				idps.close();
				accessionps.close();
				cps.close();
				specPs.close();
				crefPs.close();
				ConnectionManager.releaseConnection(conn);
				return;
			}		
			//	Get next feature ID
//			idrs = idps.executeQuery();
//			while(idrs.next())
//				libId = idrs.getString("MRC2ID");
//			
//			idrs.close();
			
		
			//	Get compound ID from original feature
			Double precursorMz = null;
			MsPoint parent = null;
			int maxDigits = 0;
			accessionps.setString(1, msms.getDescription());
			accessionrs = accessionps.executeQuery();
			while(accessionrs.next()) {
				accession = accessionrs.getString("ACCESSION");
				msms.setIonisationType(accessionrs.getString("IONIZATION_TYPE"));
				precursorMz = accessionrs.getDouble("PRECURSOR_MZ");
				parent = new MsPoint(precursorMz, 33.0d);
				maxDigits = accessionrs.getInt("MAX_DIGITS");
			}		
			accessionrs.close();
			
			//	Component
			libId = SQLUtils.getNextIdFromSequence(conn, 
					"MSMS_LIB_ENTRY_SEQ",
					DataPrefix.MSMS_LIBRARY_ENTRY,
					"0",
					9);
			cps.setString(1, libId);
			cps.setString(2, msms.getPolarity().getCode());
			cps.setString(3, msms.getIonisationType());
			cps.setDouble(4, msms.getCidLevel());
			
//			MsPoint parent = msms.getParent();
//			if(parent != null)
//				precursorMz = parent.getMz();

			cps.setDouble(5, precursorMz);
			cps.setString(6, "MS" + Integer.toString(msms.getDepth()));
			cps.setString(7, SpectrumSource.THEORETICAL.name());
			cps.setString(8, msms.getIonisationType());
			cps.setString(9, libraryName);
			cps.setString(10, refLibraryId);
			cps.setString(11, accession);
			cps.setString(12, MsUtils.calculateSpectrumHash(msms.getSpectrum()));	
			
			Double enthropy = MsUtils.calculateSpectrumEntropy(msms.getSpectrum());
			if(enthropy.equals(Double.NaN))
				enthropy  = 0.0d;
			
			cps.setDouble(13, enthropy);
			cps.setInt(14, maxDigits);
			cps.addBatch();
			
			//	Spectrum
			specPs.setString(1, libId);
			boolean parentInSpectrum = false;
			for(MsPoint p : msms.getSpectrum()) {

				specPs.setDouble(2, p.getMz());
				specPs.setDouble(3, p.getIntensity());
				specPs.setString(4, null);
				if(parent != null && p.getMz() == parent.getMz()) {
					specPs.setString(4, "Y");
					parentInSpectrum = true;					
				}
				specPs.addBatch();
			}
			if(!parentInSpectrum && parent != null) {

				specPs.setDouble(2, parent.getMz());
				specPs.setDouble(3, parent.getIntensity());
				specPs.setString(4, "Y");
				specPs.addBatch();
			}
			//	Crossref
			crefPs.setString(1, libId);
			crefPs.setString(2, msms.getDescription());
			crefPs.addBatch();
			
			processed++;
			if(processed % 1000 == 0) {
				cps.executeBatch();
				specPs.executeBatch();
				crefPs.executeBatch();
			}			
		}
		cps.executeBatch();
		specPs.executeBatch();
		crefPs.executeBatch();
//		idps.close();
		accessionps.close();
		cps.close();
		specPs.close();
		crefPs.close();
		ConnectionManager.releaseConnection(conn);
	}
	
	@Override
	public Task cloneTask() {
		return new MSMSDecoyLibraryImportTask(
				libraryFile, refLibrary, polarity, method, append);
	}
}
