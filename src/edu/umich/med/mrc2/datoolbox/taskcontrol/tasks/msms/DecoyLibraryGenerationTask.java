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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.msms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibrary;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSComponentTableFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class DecoyLibraryGenerationTask extends AbstractTask implements TaskListener {

	private ReferenceMsMsLibrary library;
	private Polarity polarity;
	private File outputDirectory;
	private File referenceLogFile, previousMspFile;
	private String lastProcessedId;
	private ArrayList<MsMsLibraryFeature> featuresToExport;
	private HashSet<String>featuresToIgnore; 
	
	private static final DecimalFormat intensityFormat = new DecimalFormat("###");
	private static final NumberFormat mzFormat =  MRC2ToolBoxConfiguration.getMzFormat();
//	private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	
	private Semaphore outputSem;
	private Semaphore errorSem;
	private String output;	
	private String error;
	private Process p;
	private Path logPath;
	private Path mspOutputPath;
	private long maxTime;
	
	private static final String SIRIUS_BINARY_PATH = MRC2ToolBoxConfiguration.getSiriusBinaryPath();
//			"E:\\Program Files\\sirius-win64-headless-4.4.29\\sirius-console-64.exe"; 
	
//	private static final IOFileFilter decoyFileFilter = 
//			FileFilterUtils.makeFileOnly(new RegexFileFilter(".+\\.tsv$"));
	
	private static final Comparator<String> fidComparator = new Comparator<String>() {
	    public int compare(String o1, String o2) {
	        return extractInt(o1) - extractInt(o2);
	    }

	    int extractInt(String s) {
	        String num = s.replaceFirst("^" + DataPrefix.MSMS_LIBRARY_ENTRY.name() + "0+", "");
	        // return 0 if no digits found
	        return num.isEmpty() ? 0 : Integer.parseInt(num);
	    }
	};
	
	public DecoyLibraryGenerationTask(
			ReferenceMsMsLibrary library, 
			Polarity polarity, 
			File outputDirectory,
			long maxTime) {
		super();
		this.library = library;
		this.polarity = polarity;
		this.outputDirectory = outputDirectory;
		this.maxTime = maxTime;
		featuresToIgnore = new HashSet<String>();
	}

	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		initLogFile();
		initOutputFile();
		if(referenceLogFile != null) {
			try {
				readFailedFeatureIdsFromPreviousLog();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(previousMspFile != null) {
			try {
				readProcessedFeatureIdsFromPreviousMspFile();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			fetchLibraryData();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
		try {
			runDecoyGeneration();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
	}
	
	private void readFailedFeatureIdsFromPreviousLog() throws IOException {
		
		if(referenceLogFile == null || !referenceLogFile.exists())
			return;
		
		taskDescription = "Reading log ...";
		total = 100;
		processed = 20;
		List<String>logLines = 
				Files.readAllLines(Paths.get(referenceLogFile.getAbsolutePath()));
		
		String LOG_LINE_START = "Failed to create decoy for ";
		
		for(String line : logLines) {
			
			if(line.startsWith(LOG_LINE_START)) {
				String id = line.replace(LOG_LINE_START, "").trim();
				featuresToIgnore.add(id);
			}
		}
	}
	
	private void readProcessedFeatureIdsFromPreviousMspFile() throws IOException {
		
		if(previousMspFile == null || !previousMspFile.exists())
			return;
		
		taskDescription = "Reading MSP ...";
		total = 100;
		processed = 40;
		
		List<String>logLines = 
				Files.readAllLines(Paths.get(previousMspFile.getAbsolutePath()));
		
		String LOG_LINE_START = MSPField.NAME.getName() + ": ";
		
		for(String line : logLines) {
			
			if(line.startsWith(LOG_LINE_START)) {
				String id = line.replace(LOG_LINE_START, "").trim();
				featuresToIgnore.add(id);
			}
		}
	}

	protected void initLogFile() {

		logPath = Paths.get(outputDirectory.getAbsolutePath(), library.getName() 
				+ "_" + polarity.name() + "_DecoyGeneration_" + FIOUtils.getTimestamp() + ".log");
		ArrayList<String>logStart = new ArrayList<String>();
		logStart.add(MRC2ToolBoxConfiguration.getDateTimeFormat().format(new Date()));
		logStart.add("Generating decoy library from reference library \"" + library.getName() 
				+ "\", " + polarity.name() + " mode.");		
		logStart.add("-------------------------------");
	    try {
			Files.write(logPath, 
					logStart, 
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
	}
	
	private void initOutputFile() {
		
		mspOutputPath = Paths.get(outputDirectory.getAbsolutePath(), library.getName() 
				+ "_" + polarity.name() + "_DECOY" + "." + MsLibraryFormat.MSP.getFileExtension());
	    try {
			Files.createFile(mspOutputPath);
		} catch (IOException e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
	}

	private void runDecoyGeneration() {
		
		taskDescription = "Generating decoys ...";
		total = featuresToExport.size();
		processed = 0;
		for(MsMsLibraryFeature feature : featuresToExport) {
			
			PassatuttoDecoyGeneratorTask task = new PassatuttoDecoyGeneratorTask(
					outputDirectory, feature, polarity, logPath, maxTime);
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
			
//			taskDescription = "Processinf feature " + feature.getUniqueId();
//			File msFile = Paths.get(outputDirectory.getAbsolutePath(), feature.getUniqueId() + 
//					"." + MsLibraryFormat.SIRIUS_MS.getFileExtension()).toFile();			
//			try {
//				writeSiriusOutput(feature, msFile);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			if(msFile.exists() && msFile.canRead()) {
//				runDecoyGenerator(msFile, feature);
//			}
//			processed++;
		}
	}
	
	private void runDecoyGenerator(File msFile, MsMsLibraryFeature feature) {
		
		String baseName = FilenameUtils.getBaseName(msFile.getName());
		File decoyDir = Paths.get(outputDirectory.getAbsolutePath(), baseName + "_decoys").toFile();
		taskDescription = "Running Passatutto for " + baseName;
		ArrayList<String>commandParts = new ArrayList<String>();
		commandParts.add("\"" + SIRIUS_BINARY_PATH + "\"");
		commandParts.add("-i");
		commandParts.add("\"" + msFile.getAbsolutePath() + "\"");
		commandParts.add("-o");
		commandParts.add("\"" + decoyDir.getAbsolutePath() + "\"");
		commandParts.add("formula passatutto");
		String searchCommand = StringUtils.join(commandParts, " ");
		try {
			Runtime runtime = Runtime.getRuntime();	
			p = runtime.exec(searchCommand);
			new OutputReader().start();
			new ErrorReader().start();
			int exitCode = p.waitFor();
			if(exitCode == 0) {
        		p.destroy();
        		addLogLine(getOutput());
        		extractDecoysToMSPFile(decoyDir, feature);
			}
			else {
				errorMessage = getError();
				System.out.println("Sirius error");
				System.out.println(errorMessage);
				System.out.println(searchCommand);
				addLogLine(getOutput());
        		addLogLine(errorMessage);
        		addLogLine("Failed to create decoy for " + feature.getUniqueId());
				setStatus(TaskStatus.ERROR);
return;

			}
		}
		catch (IOException e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		} catch (InterruptedException e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
	}
	
	protected void addLogLine(String line) {
	    try {
			Files.writeString(logPath, 
					line + "\n", 
					StandardCharsets.UTF_8,
					StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void extractDecoysToMSPFile(File decoyDir, MsMsLibraryFeature feature) {
		
		if (!decoyDir.exists() || !decoyDir.canRead()) 
			return;
		
		List<Path> decoyList = new ArrayList<Path>();				
		try {
			decoyList = Files.find(Paths.get(decoyDir.getAbsolutePath()), 3,
					(filePath, fileAttr) -> (filePath.toString().contains(File.separator + "decoys" + File.separator) &&
					filePath.toString().endsWith(".tsv")) && fileAttr.isRegularFile()).
					sorted().collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(decoyList.isEmpty()) {			
			addLogLine("Failed to create decoy for " + feature.getUniqueId());
			return;
		}
		for (Path decoy : decoyList) {
			addSpectrumFromDecoyFile(decoy.toFile(), feature.getUniqueId());	
		}
	}
	
	private void addSpectrumFromDecoyFile(File decoy, String fidm) {

		ArrayList<String>mspEntry = new ArrayList<String>();
		ArrayList<MsPoint>msms = new ArrayList<MsPoint>();
		String[][] decoyData = DelimitedTextParser.parseTextFile(
				decoy, MRC2ToolBoxConfiguration.getTabDelimiter());
		
		mspEntry.add(MSPField.NAME.getName() + ": " + fidm);
		for(int i=1; i<decoyData.length; i++) {
			
			double mz = Double.parseDouble(decoyData[i][0]);
			double relInt = Double.parseDouble(decoyData[i][01]);
			msms.add(new MsPoint(mz, relInt));
		}
		MsPoint[] msmsNorm = MsUtils.normalizeAndSortMsPattern(msms);
		MsPoint parent = msmsNorm[msmsNorm.length - 1];
		mspEntry.add(MSPField.PRECURSORMZ.getName() + ": "
				+ MRC2ToolBoxConfiguration.getMzFormat().format(parent.getMz()));
		mspEntry.add(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(msmsNorm.length));
		for(MsPoint point : msms) {
			mspEntry.add(
				MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz())
				+ " " 
				+ intensityFormat.format(point.getIntensity()));
		}
		mspEntry.add("");
	    try {
			Files.write(mspOutputPath, 
					mspEntry, 
					StandardCharsets.UTF_8,
					StandardOpenOption.WRITE, 
					StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}    
	}
	
	private void writeSiriusOutput(MsMsLibraryFeature feature, File msFile) throws Exception {
		
		Writer writer = new BufferedWriter(new FileWriter(msFile));
		String compoundName = "";
		String formula = null;
		CompoundIdentity cid = feature.getCompoundIdentity();
		if (cid != null) {
			compoundName = cid.getName();
			formula = cid.getFormula();
		}
		Collection<String> msBlock = new ArrayList<String>();
		Adduct adduct = AdductManager.getDefaultAdductForPolarity(polarity);
		msBlock.add(">compound " + feature.getUniqueId());
		msBlock.add(">parentmass " + mzFormat.format(feature.getParent().getMz()));
		msBlock.add(">ionization " + adduct.getName());
		if (formula != null)
			msBlock.add(">formula " + formula);

		msBlock.add("");
		msBlock.add(">ms2");
		for (MsPoint p : feature.getSpectrum()) {

			if (Math.round(p.getIntensity()) > 0)
				msBlock.add(mzFormat.format(p.getMz()) + " " + intensityFormat.format(p.getIntensity()));
		}
		msBlock.add("");
		msBlock.add(">ms1");

		// MS1 assuming default adduct
		if (formula != null) {
			Collection<MsPoint> msPoints = MsUtils.calculateIsotopeDistribution(formula, adduct);
			if (msPoints != null) {

				for (MsPoint p : msPoints) {

					msBlock.add(mzFormat.format(p.getMz()) + " " + intensityFormat.format(p.getIntensity()));
				}
			}
		} else {
			msBlock.add(mzFormat.format(feature.getParent().getMz()) + " "
					+ intensityFormat.format(feature.getParent().getIntensity()));
		}
		msBlock.add("");
		writer.append(StringUtils.join(msBlock, "\n") + "\n");
		writer.flush();
		writer.close();
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
				+ "WHERE LIBRARY_NAME = ? AND POLARITY = ? AND MAX_DIGITS > 1 AND ACCESSION IS NOT NULL ";
		if(lastProcessedId != null)
			query += " AND MRC2_LIB_ID > ?";
		
		query += "ORDER BY 1 ";
		
		PreparedStatement ps = conn.prepareStatement(query,
		         ResultSet.TYPE_SCROLL_INSENSITIVE ,
		         ResultSet.CONCUR_UPDATABLE);
		ps.setString(1, library.getPrimaryLibraryId());
		ps.setString(2, polarity.getCode());
		if(lastProcessedId != null)
			ps.setString(3, lastProcessedId);

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
			if(featuresToIgnore.contains(mrc2msmsId)) {
				processed++;
				continue;
			}
			MsMsLibraryFeature feature = new MsMsLibraryFeature(
					mrc2msmsId,
					Polarity.getPolarityByCode(
							rs.getString(MSMSComponentTableFields.POLARITY.name())));
			feature.setSpectrumSource(
					SpectrumSource.getOptionByName(
							rs.getString(MSMSComponentTableFields.SPECTRUM_SOURCE.name())));
			feature.setIonizationType(
					IDTDataCache.getIonizationTypeById(
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

				MsPoint p = new MsPoint(msmsrs.getDouble("MZ"), msmsrs.getDouble("INTENSITY"));
				feature.getSpectrum().add(p);
				if(p.getMz() == precursorMz)
					feature.setParent(p);

				if(msmsrs.getString("FRAGMENT_COMMENT") != null)
					feature.getMassAnnotations().put(p, msmsrs.getString("FRAGMENT_COMMENT"));
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
	

	private class OutputReader extends Thread {
		public OutputReader() {
			try {
				outputSem = new Semaphore(1);
				outputSem.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			try {
				StringBuffer readBuffer = new StringBuffer();
				BufferedReader isr = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String buff = new String();
				while ((buff = isr.readLine()) != null) {
					readBuffer.append(buff);
					System.out.println(buff);
				}
				output = readBuffer.toString();
				outputSem.release();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class ErrorReader extends Thread {
		public ErrorReader() {
			try {
				errorSem = new Semaphore(1);
				errorSem.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			try {
				StringBuffer readBuffer = new StringBuffer();
				BufferedReader isr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				String buff = new String();
				while ((buff = isr.readLine()) != null) {
					readBuffer.append(buff);
				}
				error = readBuffer.toString();
				errorSem.release();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (error.length() > 0)
				System.out.println(error);
		}
	}
		
	public String getOutput() {
		try {
			outputSem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String value = output;
		outputSem.release();
		return value;
	}

	public String getError() {
		try {
			errorSem.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String value = error;
		errorSem.release();
		return value;
	}	
	
	@Override
	public Task cloneTask() {
		return new DecoyLibraryGenerationTask(
				library, polarity, outputDirectory, maxTime);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(PassatuttoDecoyGeneratorTask.class))
				finalizePassatuttoDecoyGeneratorTask((PassatuttoDecoyGeneratorTask)e.getSource());
		}
	}
	
	private synchronized void finalizePassatuttoDecoyGeneratorTask(PassatuttoDecoyGeneratorTask task) {
		
		processed++;
		List<Path> decoys = task.getDecoyList();
		if(decoys == null || decoys.isEmpty()) {

			if(processed == featuresToExport.size()) {
				setStatus(TaskStatus.FINISHED);
				return;
			}
		}
		MsMsLibraryFeature libFeature = task.getFeature();
		for(Path p : decoys)
			addSpectrumFromDecoyFile(p.toFile(), libFeature.getUniqueId());
		
		if(processed == featuresToExport.size()) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
	}

	public File getOutputFile() {

		if(mspOutputPath != null)
			return mspOutputPath.toFile();
		else
			return null;
	}

	public File getReferenceLogFile() {
		return referenceLogFile;
	}

	public void setReferenceLogFile(File referenceLogFile) {
		this.referenceLogFile = referenceLogFile;
	}
	
	public void setPreviousMspFile(File previousMspFile) {
		this.previousMspFile = previousMspFile;
	}

	public String getLastProcessedId() {
		return lastProcessedId;
	}

	public void setLastProcessedId(String lastProcessedId) {
		this.lastProcessedId = lastProcessedId;
	}
}
