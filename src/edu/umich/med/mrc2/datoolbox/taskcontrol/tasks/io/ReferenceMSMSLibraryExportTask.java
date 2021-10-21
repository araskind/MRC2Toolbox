package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.enums.MGFFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSComponentTableFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTReferenceLibraries;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class ReferenceMSMSLibraryExportTask extends AbstractTask {
	
	private static final DecimalFormat intensityFormat = new DecimalFormat("###.##");
	private static final NumberFormat mzFormat =  MRC2ToolBoxConfiguration.getMzFormat();
	
	private ReferenceMsMsLibrary library;
	private MsLibraryFormat exportFormat;
	private Polarity polarity;
	private File outputDirectory;
	private ArrayList<MsMsLibraryFeature> featuresToExport;
	private int maxEntriesPerFile;
	private boolean highResOnly;

	public ReferenceMSMSLibraryExportTask(
			ReferenceMsMsLibrary library, 
			MsLibraryFormat exportFormat, 
			Polarity polarity,
			File outputFile,
			int maxEntriesPerFile,
			boolean highResOnly) {
		super();
		this.library = library;
		this.exportFormat = exportFormat;
		this.polarity = polarity;
		this.outputDirectory = outputFile;
		this.maxEntriesPerFile = maxEntriesPerFile;
		this.highResOnly = highResOnly;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			fetchLibraryData();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			createOutputLibraryFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void fetchLibraryData() throws Exception {
		
		taskDescription = "Getting library features from database";
		total = 100;
		processed = 20;
		featuresToExport = new ArrayList<MsMsLibraryFeature>();
		
		Connection conn = ConnectionManager.getConnection();
		String query =
				"SELECT MRC2_LIB_ID, POLARITY, IONIZATION, COLLISION_ENERGY, PRECURSOR_MZ, ADDUCT, "
				+ "COLLISION_GAS, INSTRUMENT, INSTRUMENT_TYPE, IN_SOURCE_VOLTAGE, "
				+ "MSN_PATHWAY, PRESSURE, SAMPLE_INLET, SPECIAL_FRAGMENTATION, "
				+ "SPECTRUM_TYPE, CHROMATOGRAPHY_TYPE, CONTRIBUTOR, SPLASH, "
				+ "RESOLUTION, SPECTRUM_SOURCE, IONIZATION_TYPE, LIBRARY_NAME, "
				+ "ORIGINAL_LIBRARY_ID, ACCESSION, SPECTRUM_HASH, ENTROPY "
				+ "FROM REF_MSMS_LIBRARY_COMPONENT "
				+ "WHERE LIBRARY_NAME = ? AND POLARITY = ? ";
		if(highResOnly)
			query += "AND MAX_DIGITS > 2 ";

		query +=  "ORDER BY 1 ";
		
		PreparedStatement ps = conn.prepareStatement(query,
		         ResultSet.TYPE_SCROLL_INSENSITIVE ,
		         ResultSet.CONCUR_UPDATABLE);
		ps.setString(1, library.getSearchOutputCode());
		ps.setString(2, polarity.getCode());

		String msmsQuery =
				"SELECT MZ, INTENSITY, FRAGMENT_COMMENT "
				+ "FROM REF_MSMS_LIBRARY_PEAK WHERE MRC2_LIB_ID = ? ORDER BY 1";
		PreparedStatement msmsps = conn.prepareStatement(msmsQuery);

		ResultSet msmsrs = null;
		ResultSet rs = ps.executeQuery();
		total = 100;
		processed = 0;
		if (rs.last()) {
			total = rs.getRow();
		  rs.beforeFirst();
		}
		while (rs.next()) {
			
			String mrc2msmsId = rs.getString("MRC2_LIB_ID");			
			MsMsLibraryFeature feature = new MsMsLibraryFeature(
					mrc2msmsId,
					Polarity.getPolarityByCode(
							rs.getString(MSMSComponentTableFields.POLARITY.name())));
			feature.setSpectrumSource(
					SpectrumSource.getSpectrumSourceByName(
							rs.getString(MSMSComponentTableFields.SPECTRUM_SOURCE.name())));
			feature.setIonizationType(
					IDTDataCash.getIonizationTypeById(
					rs.getString(MSMSComponentTableFields.IONIZATION_TYPE.name())));
			feature.setCollisionEnergyValue(
					rs.getString(MSMSComponentTableFields.COLLISION_ENERGY.name()));
			feature.setSpectrumEntropy(
					rs.getDouble(MSMSComponentTableFields.ENTROPY.name()));
			
			Map<String, String> properties = feature.getProperties();
			for(MSMSComponentTableFields field : MSMSComponentTableFields.values()) {

				if(!field.equals(MSMSComponentTableFields.PRECURSOR_MZ) 
						&& !field.equals(MSMSComponentTableFields.MRC2_LIB_ID)) {
					
					String value = rs.getString(field.name());
					if(value != null && !value.trim().isEmpty())
						properties.put(field.getName(), value);
				}
			}
			feature.setMsmsLibraryIdentifier(library.getUniqueId());

			//	Add spectrum
			double precursorMz = rs.getDouble(MSMSComponentTableFields.PRECURSOR_MZ.name());
			msmsps.setString(1, mrc2msmsId);
			msmsrs = msmsps.executeQuery();
			while(msmsrs.next()) {
				
				double intensity = msmsrs.getDouble("INTENSITY");
				if(Math.round(intensity) > 0) { 
					
					MsPoint p = new MsPoint(msmsrs.getDouble("MZ"), intensity);
					feature.getSpectrum().add(p);
					if(p.getMz() == precursorMz)
						feature.setParent(p);

					if(msmsrs.getString("FRAGMENT_COMMENT") != null)
						feature.getMassAnnotations().put(p, msmsrs.getString("FRAGMENT_COMMENT"));
				}
			}
			if(feature.getParent() == null)
				feature.setParent(new MsPoint(precursorMz, 100.0d));
			
			msmsrs.close();
			if(rs.getString("ACCESSION") != null) {
				
				CompoundIdentity compoundIdentity =
						CompoundDatabaseUtils.getCompoundById(rs.getString("ACCESSION"), conn);

				feature.setCompoundIdentity(compoundIdentity);
			}
			featuresToExport.add(feature);		
			processed++;
		}
		rs.close();
		ps.close();
		msmsps.close();	
		ConnectionManager.releaseConnection(conn);
	}
	
	private void createOutputLibraryFile() {
		
		if(featuresToExport.isEmpty())
			return;

		if(exportFormat.equals(MsLibraryFormat.MSP))
			try {
				writeMSPOutput();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		if(exportFormat.equals(MsLibraryFormat.SIRIUS_MS)) {
			if(maxEntriesPerFile == 1) {
				writeSiriusOutputOneEntryPerFile();
			}
			else {
				try {
					writeSiriusOutput();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if(exportFormat.equals(MsLibraryFormat.MGF))
			try {
				writeMGFOutput();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		if(exportFormat.equals(MsLibraryFormat.XY_META_MGF))
			try {
				writeXYMetaMGFOutput();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	private void writeMSPOutput() throws Exception {
				
		taskDescription = "Wtiting MSP output";
		total = featuresToExport.size();
		processed = 0;
		
		Collection<MSPField>individual = new ArrayList<MSPField>();
		individual.add(MSPField.NAME);
		individual.add(MSPField.FORMULA);
		individual.add(MSPField.EXACTMASS);
		individual.add(MSPField.MW);
		individual.add(MSPField.INCHI_KEY);
		individual.add(MSPField.PRECURSORMZ);
		individual.add(MSPField.NUM_PEAKS);
		individual.add(MSPField.NOTES);	//	TODO Skip notes for now, not required for most tasks
		
		String timestamp = MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
		String fileName = library.getSearchOutputCode() + "." + exportFormat.getFileExtension();
		int fileCount = 1;
		if(maxEntriesPerFile > 0) {			
			fileName = library.getSearchOutputCode() + "_" 
					+ StringUtils.leftPad(Integer.toString(fileCount), 8, '0') + 
					"." + exportFormat.getFileExtension();
		}
		File exportFile = Paths.get(outputDirectory.getAbsolutePath(), fileName).toFile();	
		Writer writer = new BufferedWriter(new FileWriter(exportFile));
		for(MsMsLibraryFeature feature : featuresToExport) {

//	TODO add to GUI option to write out compound information instead of feature ID
//			CompoundIdentity cid = feature.getCompoundIdentity();
//			if(cid != null) {
//				writer.append(MSPField.NAME.getName() + ": " + cid.getName() + "\n");
//
//				if (cid.getFormula() != null)
//					writer.append(MSPField.FORMULA.getName() + ": " + cid.getFormula() + "\n");
//				writer.append(MSPField.EXACTMASS.getName() + ": "
//						+ mzFormat.format(cid.getExactMass()) + "\n");
//				writer.append(MSPField.MW.getName() + ": " + 
//						Integer.toString((int) Math.round(cid.getExactMass())) + "\n");
//				if (cid.getInChiKey() != null)
//					writer.append(MSPField.INCHI_KEY.getName() + ": " + cid.getInChiKey() + "\n");
//			}
//			else {
				writer.append(MSPField.NAME.getName() + ": " + feature.getUniqueId() + "\n");
//			}
			Map<String, String> properties = feature.getProperties();
			if(library.getPrimaryLibraryId() != null) {
				
				if((library.getPrimaryLibraryId().equals(NISTReferenceLibraries.nist_msms.name()) || 
						library.getPrimaryLibraryId().equals(NISTReferenceLibraries.hr_msms_nist.name()))
						&& properties.get(MSMSComponentTableFields.ORIGINAL_LIBRARY_ID.getName()) != null)
					writer.append(MSPField.NIST_NUM.getName() + ": " + 
							properties.get(MSMSComponentTableFields.ORIGINAL_LIBRARY_ID.getName()) + "\n");
			}
			if(feature.getCompoundIdentity() != null && feature.getCompoundIdentity().getInChiKey() != null)
				writer.append(MSPField.INCHI_KEY.getName() + ": " + feature.getCompoundIdentity().getInChiKey() + "\n");
							
			for (MSMSComponentTableFields field : MSMSComponentTableFields.values()) {

				if (individual.contains(field.getMSPField()))
					continue;
				
				String prop = properties.get(field.getName());
				if(prop == null || prop.isEmpty())
					continue;
				
				if(field.equals(MSMSComponentTableFields.COLLISION_ENERGY)) {
					
					double ce = 0.0d;
					try {
						ce = Double.parseDouble(prop);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						//	e.printStackTrace();
					}
					if(ce == 0.0d)
						continue;			
				}	
				writer.append(field.getMSPField().getName() + ": " + prop + "\n");					
			}
			writer.append(MSPField.PRECURSORMZ.getName() + ": "
					+ mzFormat.format(feature.getParent().getMz()) + "\n");
			writer.append(MSPField.NUM_PEAKS.getName() + ": " 
					+ Integer.toString(feature.getSpectrum().size()) + "\n");

			for(MsPoint point : feature.getSpectrum()) {

				if(point.getIntensity() > 0.0d) {
					
					writer.append(mzFormat.format(point.getMz()) + " " 
							+ intensityFormat.format(point.getIntensity()));
					
//					String annotation = feature.getMassAnnotations().get(point);
//					if(annotation != null)
//						writer.append(" \"" + annotation + "\"");
					
					writer.append("\n");
				}				
			}
			writer.append("\n");		
			processed++;
			if (maxEntriesPerFile > 0 && processed % maxEntriesPerFile == 0) {
				fileCount++;
				writer.flush();
				writer.close();
				fileName = library.getName().replaceAll("\\s+", "_") + "_" + polarity.getCode() + "_" + timestamp
						 + "_" +  StringUtils.leftPad(Integer.toString(fileCount), 8, '0') + "."
						+ exportFormat.getFileExtension();

				exportFile = Paths.get(outputDirectory.getAbsolutePath(), fileName).toFile();
				writer = new BufferedWriter(new FileWriter(exportFile));
			}
		}
		writer.flush();
		writer.close();
	}
	
	private void writeSiriusOutputOneEntryPerFile() {
		
		taskDescription = "Wtiting Sirius MS output one entry per file ...";
		total = featuresToExport.size();
		processed = 0;
		Adduct adduct = AdductManager.getDefaultAdductForPolarity(polarity);
		
		for(MsMsLibraryFeature feature : featuresToExport) {						
			
			String formula = null;
			String compoundName = "";
			CompoundIdentity cid = feature.getCompoundIdentity();
			if(cid != null) {
				compoundName = cid.getName();
				formula = cid.getFormula();
			}
			if(formula == null || formula.isEmpty())
				continue;
			
			if(feature.getSpectrum() == null || feature.getSpectrum().isEmpty())
				continue;
			
			Collection<MsPoint> msPoints = MsUtils.calculateIsotopeDistribution(formula, adduct);
			if(msPoints == null || msPoints.isEmpty())
				continue;
							
			Collection<String>msBlock = new ArrayList<String>();			
			msBlock.add(">compound " + feature.getUniqueId());
			msBlock.add(">parentmass " + mzFormat.format(feature.getParent().getMz()));
			msBlock.add(">ionization " + adduct.getName());
			//	msBlock.add(">comments " + compoundName);
			if(formula != null)
				msBlock.add(">formula " + formula);		
						
			//	msBlock.add(">collision " + feature.getCollisionEnergyValue());
			msBlock.add("");
			msBlock.add(">ms2");
			for(MsPoint p : feature.getSpectrum()) {
				
				if(Math.round(p.getIntensity()) > 0)
					msBlock.add(mzFormat.format(p.getMz()) + " " + 
								intensityFormat.format(p.getIntensity()));
			}
			msBlock.add("");
			msBlock.add(">ms1");
			for(MsPoint p : msPoints) {
				
				msBlock.add(
						mzFormat.format(p.getMz()) + " " + 
								intensityFormat.format(p.getIntensity()));
			}		
			try {		
				File exportFile = Paths.get(outputDirectory.getAbsolutePath(), feature.getUniqueId() + ".ms").toFile();	
				Writer writer = new BufferedWriter(new FileWriter(exportFile));						
				writer.append(StringUtils.join(msBlock, "\n") + "\n");	
				writer.flush();
				writer.close();
			} catch (IOException e) {
				System.out.println("Failed to write file for " + feature.getUniqueId());
				e.printStackTrace();
			}
			processed++;
		}
	}
	
	private void writeSiriusOutput() throws Exception {
		
		taskDescription = "Wtiting Sirius MS output";
		total = featuresToExport.size();
		processed = 0;
			
		String timestamp = MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
		String fileName = library.getName().replaceAll("\\s+", "_") + "_" + polarity.getCode() + "_" 
				+ timestamp + "." + exportFormat.getFileExtension();
		int fileCount = 1;
		if(maxEntriesPerFile > 0) {
			fileName = library.getName().replaceAll("\\s+", "_") + "_" + polarity.getCode() + "_" 
					+ timestamp + "_" +  StringUtils.leftPad(Integer.toString(fileCount), 8, '0') + 
					"." + exportFormat.getFileExtension();
		}
		File exportFile = Paths.get(outputDirectory.getAbsolutePath(), fileName).toFile();	
		Writer writer = new BufferedWriter(new FileWriter(exportFile));
		for(MsMsLibraryFeature feature : featuresToExport) {
			
			String compoundName = "";
			String formula = null;
			CompoundIdentity cid = feature.getCompoundIdentity();
			if(cid != null) {
				compoundName = cid.getName();
				formula = cid.getFormula();
			}

			Collection<String>msBlock = new ArrayList<String>();
			Adduct adduct = AdductManager.getDefaultAdductForPolarity(polarity);
			msBlock.add(">compound " + feature.getUniqueId());
			msBlock.add(">parentmass " + mzFormat.format(feature.getParent().getMz()));
			msBlock.add(">ionization " + adduct.getName());
			//	msBlock.add(">comments " + compoundName);
			if(formula != null)
				msBlock.add(">formula " + formula);		
						
			//	msBlock.add(">collision " + feature.getCollisionEnergyValue());
			msBlock.add("");
			msBlock.add(">ms2");
			for(MsPoint p : feature.getSpectrum()) {
				
				if(Math.round(p.getIntensity()) > 0)
					msBlock.add(mzFormat.format(p.getMz()) + " " + 
								intensityFormat.format(p.getIntensity()));
			}
			msBlock.add("");
			msBlock.add(">ms1");
			
			//	MS1 assuming default adduct
			if(formula != null) {
				Collection<MsPoint> msPoints = MsUtils.calculateIsotopeDistribution(formula, adduct);
				if(msPoints != null) {
					
					for(MsPoint p : msPoints) {
						
						msBlock.add(
								mzFormat.format(p.getMz()) + " " + 
										intensityFormat.format(p.getIntensity()));
					}
				}
			}
			else {
				msBlock.add(
						mzFormat.format(feature.getParent().getMz()) + " " + 
								intensityFormat.format(feature.getParent().getIntensity()));
			}
			msBlock.add("");
			writer.append(StringUtils.join(msBlock, "\n") + "\n");		
			processed++;
			if (maxEntriesPerFile > 0 && processed % maxEntriesPerFile == 0) {
				fileCount++;
				writer.flush();
				writer.close();
				fileName = library.getName().replaceAll("\\s+", "_") + "_" + polarity.getCode() + "_" + timestamp
						 + "_" +  StringUtils.leftPad(Integer.toString(fileCount), 8, '0') + "."
						+ exportFormat.getFileExtension();

				exportFile = Paths.get(outputDirectory.getAbsolutePath(), fileName).toFile();
				writer = new BufferedWriter(new FileWriter(exportFile));
			}
		}
		writer.flush();
		writer.close();
	}
	
	private void writeMGFOutput() throws Exception {
		
		taskDescription = "Wtiting MGF output";
		total = featuresToExport.size();
		processed = 0;
		
		String timestamp = MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
		String fileName = library.getName().replaceAll("\\s+", "_") + "_" + polarity.getCode() + "_" 
				+ timestamp + "." + exportFormat.getFileExtension();
		int fileCount = 1;
		if(maxEntriesPerFile > 0) {
			fileName = library.getName().replaceAll("\\s+", "_") + "_" + polarity.getCode() + "_" 
					+ timestamp + "_" +  StringUtils.leftPad(Integer.toString(fileCount), 8, '0') + 
					"." + exportFormat.getFileExtension();
		}
		File exportFile = Paths.get(outputDirectory.getAbsolutePath(), fileName).toFile();	
		Writer writer = new BufferedWriter(new FileWriter(exportFile));
		for(MsMsLibraryFeature feature : featuresToExport) {
			
			writer.append(MGFFields.BEGIN_BLOCK.getName() + "\n");
			writer.append(MGFFields.PEPMASS + "=" + 
					mzFormat.format(feature.getParent().getMz()) + "\n");
			writer.append(MGFFields.CHARGE + "=" + Integer.toString(polarity.getSign()) + "\n"); //	TODO handle multiple charges
			writer.append(MGFFields.TITLE.getName() + "=" + feature.getUniqueId() + "\n");
			if(feature.getCompoundIdentity() != null)
				writer.append(MGFFields.NAME.getName() + "=" + feature.getCompoundIdentity().getName() + "\n");

			for(MsPoint point : feature.getSpectrum()) {

				writer.append(
					mzFormat.format(point.getMz())
					+ " " + intensityFormat.format(point.getIntensity())) ;
				
//				String annotation = feature.getMassAnnotations().get(point);
//				if(annotation != null)
//					writer.append(" \"" + annotation + "\"");

				writer.append("\n");
			}
			writer.append(MGFFields.END_IONS.getName() + "\n\n");	
			processed++;
			if (maxEntriesPerFile > 0 && processed % maxEntriesPerFile == 0) {
				fileCount++;
				writer.flush();
				writer.close();
				fileName = library.getName().replaceAll("\\s+", "_") + "_" + polarity.getCode() + "_" + timestamp
						 + "_" +  StringUtils.leftPad(Integer.toString(fileCount), 8, '0') + "."
						+ exportFormat.getFileExtension();

				exportFile = Paths.get(outputDirectory.getAbsolutePath(), fileName).toFile();
				writer = new BufferedWriter(new FileWriter(exportFile));
			}
		}
		writer.flush();
		writer.close();
	}
	
	private void writeXYMetaMGFOutput()throws Exception {
		
		taskDescription = "Wtiting XY-meta MGF output";
		total = featuresToExport.size();
		processed = 0;
		
		String timestamp = MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
		String fileName = library.getName().replaceAll("\\s+", "_") + "_" + polarity.getCode() + "_" 
				+ timestamp + "." + exportFormat.getFileExtension();
		int fileCount = 1;
		if(maxEntriesPerFile > 0) {
			fileName = library.getName().replaceAll("\\s+", "_") + "_" + polarity.getCode() + "_" 
					+ timestamp + "_" +  StringUtils.leftPad(Integer.toString(fileCount), 8, '0') + 
					"." + exportFormat.getFileExtension();
		}
		File exportFile = Paths.get(outputDirectory.getAbsolutePath(), fileName).toFile();	
		Writer writer = new BufferedWriter(new FileWriter(exportFile));
//		Adduct defaultAdduct = AdductManager.getDefaultAdductForPolarity(polarity);
		
		for(MsMsLibraryFeature feature : featuresToExport) {
			
			writer.append(MGFFields.BEGIN_BLOCK.getName() + "\n");
			double pepMass = 0.0d;
//			if(feature.getCompoundIdentity() != null)				
//				pepMass = feature.getCompoundIdentity().getExactMass();
//			else 
//				pepMass = MsUtils.getNeutralMass(feature.getParent(), defaultAdduct);
			
			writer.append(MGFFields.PEPMASS + "=" + mzFormat.format(feature.getParent().getMz()) + "\n");						
			writer.append(MGFFields.CHARGE + "=" + Integer.toString(polarity.getSign()) + "\n");
			writer.append(MGFFields.MSLEVEL + "=2\n");			
			writer.append(MGFFields.TITLE.getName() + "=" + feature.getUniqueId() + "\n");
			writer.append(MGFFields.NAME.getName() + "=" + feature.getUniqueId() + "\n");
			for(MsPoint point : feature.getSpectrum()) {

				writer.append(
					mzFormat.format(point.getMz())
					+ " " + intensityFormat.format(point.getIntensity())) ;
				
				writer.append("\n");
			}
			writer.append(MGFFields.END_IONS.getName() + "\n\n");	
			processed++;
			if (maxEntriesPerFile > 0 && processed % maxEntriesPerFile == 0) {
				fileCount++;
				writer.flush();
				writer.close();
				fileName = library.getName().replaceAll("\\s+", "_") + "_" + polarity.getCode() + "_" + timestamp
						 + "_" +  StringUtils.leftPad(Integer.toString(fileCount), 8, '0') + "."
						+ exportFormat.getFileExtension();

				exportFile = Paths.get(outputDirectory.getAbsolutePath(), fileName).toFile();
				writer = new BufferedWriter(new FileWriter(exportFile));
			}
		}
		writer.flush();
		writer.close();
	}

	@Override
	public Task cloneTask() {

		return new ReferenceMSMSLibraryExportTask(
				 library, 
				 exportFormat, 
				 polarity,
				 outputDirectory,
				 maxEntriesPerFile,
				 highResOnly);
	}

	public File getOutputFile() {
		return outputDirectory;
	}
}
