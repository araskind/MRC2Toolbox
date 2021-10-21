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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class PassatuttoDecoyGeneratorTask extends AbstractTask {
	
	private File outputDirectory;
	private File msFile;
	private File decoyDir;
	private MsMsLibraryFeature feature;
	private Polarity polarity;
	
	private static final DecimalFormat intensityFormat = new DecimalFormat("###");
	private static final NumberFormat mzFormat =  MRC2ToolBoxConfiguration.getMzFormat();
	private static final DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	
	private Semaphore outputSem;
	private Semaphore errorSem;
	private String output;	
	private String error;
	private Process p;
	private Path logPath;
	private List<Path> decoyList;
	private long maxTime;
	
	private static final String SIRIUS_BINARY_PATH = MRC2ToolBoxConfiguration.getSiriusBinaryPath(); 

	public PassatuttoDecoyGeneratorTask(
			File msFile, MsMsLibraryFeature feature, Polarity polarity, Path logPath, long maxTime) {
		super();
		this.outputDirectory = msFile;
		this.feature = feature;
		this.polarity = polarity;
		this.logPath = logPath;
		this.maxTime = maxTime;
		decoyList = new ArrayList<Path>();
		taskDescription = feature.getUniqueId() + " [ waiting ]";
	}

	@Override
	public Task cloneTask() {
		return new PassatuttoDecoyGeneratorTask(
				outputDirectory, feature, polarity, logPath, maxTime);		
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Processinf feature " + feature.getUniqueId();
		msFile = Paths.get(outputDirectory.getAbsolutePath(), feature.getUniqueId() + 
				"." + MsLibraryFormat.SIRIUS_MS.getFileExtension()).toFile();
		total = 100;
		processed = 30;
		try {
			writeSiriusOutput(feature, msFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(msFile.exists() && msFile.canRead()) {
			runDecoyGenerator();
			getGeneratedDecoys();
		}
		processed = 100;
		setStatus(TaskStatus.FINISHED);
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
		
		// Set precursor M/Z based on formula and polarity
		Collection<MsPoint> msPoints = null;
		if (formula != null)
			msPoints = MsUtils.calculateIsotopeDistribution(formula, adduct);
		
		msBlock.add(">compound " + feature.getUniqueId());
//		msBlock.add(">parentmass_orig " + mzFormat.format(feature.getParent().getMz()));	//	Debug only
		msBlock.add(">parentmass " + mzFormat.format(((List<MsPoint>)msPoints).get(0).getMz()));
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
		if (msPoints != null) {	// MS1 assuming default adduct

			for (MsPoint p : msPoints)
				msBlock.add(mzFormat.format(p.getMz()) + " " + intensityFormat.format(p.getIntensity()));		
		} else {
			msBlock.add(mzFormat.format(feature.getParent().getMz()) + " "
					+ intensityFormat.format(feature.getParent().getIntensity()));
		}
		msBlock.add("");
		writer.append(StringUtils.join(msBlock, "\n") + "\n");
		writer.flush();
		writer.close();
	}

	protected void runDecoyGenerator() {
		
		String baseName = FilenameUtils.getBaseName(msFile.getName());
		decoyDir = Paths.get(outputDirectory.getAbsolutePath(), baseName + "_decoys").toFile();
		taskDescription = "Running Passatutto for " + baseName;
		processed = 20;
		ArrayList<String>commandParts = new ArrayList<String>();
		commandParts.add("\"" + SIRIUS_BINARY_PATH + "\"");
		commandParts.add("-i");
		commandParts.add("\"" + msFile.getAbsolutePath() + "\"");
		commandParts.add("-o");
		commandParts.add("\"" + decoyDir.getAbsolutePath() + "\"");
		commandParts.add("formula passatutto");
		String searchCommand = StringUtils.join(commandParts, " ");
		long startTime = System.currentTimeMillis();
		try {
			
			Runtime runtime = Runtime.getRuntime();	
			p = runtime.exec(searchCommand);
			new OutputReader().start();
			new ErrorReader().start();
			
			 //timeout - kill the process. 
			if(!p.waitFor(maxTime, TimeUnit.MINUTES))
			    p.destroyForcibly();
			else {				
				//int exitCode = p.waitFor();
				if(p.exitValue() == 0) {
	        		p.destroy();
//	       		addLogLine(getOutput());
	        		processed = 98;
				}
				else {
					errorMessage = getError();
//					System.out.println("Sirius error");
//					System.out.println(errorMessage);
//					System.out.println(searchCommand);
//					addLogLine(getOutput());
	        		addLogLine(errorMessage);
	        		addLogLine("Failed to create decoy for " + feature.getUniqueId());
					setStatus(TaskStatus.ERROR);
				}
			}
		}
		catch (IOException e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		} catch (InterruptedException e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
	}
	
	private void getGeneratedDecoys() {
			
		if (!decoyDir.exists() || !decoyDir.canRead()) {
			addLogLine("Failed to create decoy for " + feature.getUniqueId());
			return;
		}				
		try {
			decoyList = Files.find(Paths.get(decoyDir.getAbsolutePath()), 3,
					(filePath, fileAttr) -> (filePath.toString().contains(File.separator + "decoys" + File.separator) &&
					filePath.toString().endsWith(".tsv")) && fileAttr.isRegularFile()).
					sorted().collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(decoyList.isEmpty())			
			addLogLine("Failed to create decoy for " + feature.getUniqueId());
		
		return;
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

	protected void addLogLine(String line) {
	    try {
			Files.writeString(logPath, 
					line + "\n", 
					StandardCharsets.UTF_8,
					StandardOpenOption.WRITE,
					StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public MsMsLibraryFeature getFeature() {
		return feature;
	}

	public List<Path> getDecoyList() {
		return decoyList;
	}
}
