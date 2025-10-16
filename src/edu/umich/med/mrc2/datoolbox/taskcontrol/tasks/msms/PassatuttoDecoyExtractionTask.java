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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.DelimitedTextParser;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class PassatuttoDecoyExtractionTask extends AbstractTask {

	private File passatuttoOutputFolder;
	private File mspOutputFile;
	private Path mspOutputPath;
	private static final DecimalFormat intensityFormat = new DecimalFormat("###.#");
	
	public PassatuttoDecoyExtractionTask(File passatuttoOutputFolder, File mspOutputFile) {
		super();
		this.passatuttoOutputFolder = passatuttoOutputFolder;
		this.mspOutputFile = mspOutputFile;		
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		if (passatuttoOutputFolder == null || !passatuttoOutputFolder.exists() 
				|| !passatuttoOutputFolder.canRead()) {
			setStatus(TaskStatus.ERROR);
			return;
		}
		IOFileFilter featureDirFilter = FileFilterUtils.makeDirectoryOnly(
				new RegexFileFilter(".+" + DataPrefix.MSMS_LIBRARY_ENTRY.getName() + "\\d{9}$"));
		Collection<File> featureDirs = FileUtils.listFilesAndDirs(
				passatuttoOutputFolder,
				DirectoryFileFilter.DIRECTORY,
				featureDirFilter);

		if (!featureDirs.isEmpty()) {

			try {
				extractDecoyData(featureDirs);
			} catch (Exception e) {

				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
				return;
			}
		}		
		setStatus(TaskStatus.FINISHED);
	}
	
	protected void initOutputFile() {
		
		mspOutputPath = Paths.get(mspOutputFile.getAbsolutePath());
	    try {
			Files.createFile(mspOutputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void extractDecoyData(Collection<File> featureDirs) {

		IOFileFilter decoyFileFilter = FileFilterUtils.makeFileOnly(new RegexFileFilter(".+\\.tsv$"));
		Pattern pattern = Pattern.compile(DataPrefix.MSMS_LIBRARY_ENTRY.getName() + "\\d{9}$");
		Matcher matcher = null;		
		String fidm = null;
		for(File fDir : featureDirs) {
			
			File decoyDir = Paths.get(fDir.getAbsolutePath(), "decoys").toFile();
			if(decoyDir.exists() && decoyDir.canRead()) {
				matcher = pattern.matcher(decoyDir.getParent());
				if (matcher.find())
					fidm = matcher.group(0);	
				
				Collection<File> decoys = FileUtils.listFilesAndDirs(
						decoyDir,
						decoyFileFilter,
						null);
				
				for(File decoy : decoys) {
					if(decoy.isFile()) {
						addSpectrumFromDecoyFile(decoy, fidm);
					}
				}
			}
		}		
	}

	private void addSpectrumFromDecoyFile(File decoy, String fidm) {

		ArrayList<String>mspEntry = new ArrayList<String>();
		ArrayList<MsPoint>msms = new ArrayList<MsPoint>();
		String[][] decoyData = DelimitedTextParser.parseTextFile(
				decoy, MRC2ToolBoxConfiguration.getTabDelimiter());
		
		mspEntry.add(MSPField.NAME.getName() + ": " + fidm + "\n");
		for(int i=1; i<decoyData.length; i++) {
			
			double mz = Double.parseDouble(decoyData[i][0]);
			double relInt = Double.parseDouble(decoyData[i][01]);
			msms.add(new MsPoint(mz, relInt));
		}
		MsPoint[] msmsNorm = MsUtils.normalizeAndSortMsPattern(msms);
		MsPoint parent = msmsNorm[msmsNorm.length - 1];
		mspEntry.add(MSPField.PRECURSORMZ.getName() + ": "
				+ MRC2ToolBoxConfiguration.getMzFormat().format(parent.getMz()) + "\n");
		mspEntry.add(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(msmsNorm.length) + "\n");
		for(MsPoint point : msms) {
			mspEntry.add(
				MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz())
				+ " " 
				+ intensityFormat.format(point.getIntensity()) + "\n");
		}
		mspEntry.add("\n");
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

	@Override
	public Task cloneTask() {
		return new PassatuttoDecoyExtractionTask(
				passatuttoOutputFolder, mspOutputFile);
	}



}
