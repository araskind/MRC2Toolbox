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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MGFUtils;

public class XYMetaDecoyMgfImportTask extends AbstractTask {

	private File mgfFile;
	private Polarity polarity;
	private Collection<TandemMassSpectrum> mgfFeatures;
	
	private static final DecimalFormat intensityFormat = new DecimalFormat("###.#");


	public XYMetaDecoyMgfImportTask(File mgfFile, Polarity polarity) {

		super();
		this.mgfFile = mgfFile;
		this.polarity = polarity;
		mgfFeatures = new ArrayList<TandemMassSpectrum>();		
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			parseMgf();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			writeMSPFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void parseMgf() {

		taskDescription = "Reading " + mgfFile.getName();
		total = 100;
		processed = 20;
		Collection<String[]>mgfBlocks = MGFUtils.getMGFTextBlocksFromFile(mgfFile);	
		
		taskDescription = "Parsing mgf data ...";
		total = mgfBlocks.size();
		processed = 0;
		mgfFeatures = new ArrayList<TandemMassSpectrum>();
		for(String[]block : mgfBlocks) {			
			TandemMassSpectrum spectrum = MGFUtils.parseXYMetaMGFBlock(block, polarity);
			mgfFeatures.add(spectrum);
			processed++;
		}			
	}
	
	private void writeMSPFile() throws IOException {

		File exportFile = Paths.get(mgfFile.getParentFile().getAbsolutePath(), 
				FilenameUtils.getBaseName(mgfFile.getName()) + 
				"." + MsLibraryFormat.MSP.getFileExtension()).toFile();
		
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));
		for(TandemMassSpectrum tandemMs : mgfFeatures) {

			writer.append(MSPField.NAME.getName() + ": " + tandemMs.getId() + "\n");		
			writer.append(MSPField.ION_MODE.getName() + ": " + polarity.getCode() + "\n");
			writer.append(MSPField.PRECURSORMZ.getName() + ": " +
				MRC2ToolBoxConfiguration.getMzFormat().format(tandemMs.getParent().getMz()) + "\n");
			writer.append(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(tandemMs.getSpectrum().size()) + "\n");
			int pointCount = 0;
			for(MsPoint point : tandemMs.getSpectrum()) {

				writer.append(
					MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz())
					+ " " + intensityFormat.format(point.getIntensity()) + "; ") ;
				pointCount++;
				if(pointCount % 5 == 0)
					writer.append("\n");
			}
			writer.append("\n\n");
			processed++;
		}
		writer.flush();
		writer.close();
	}

	@Override
	public Task cloneTask() {
		return new XYMetaDecoyMgfImportTask(mgfFile, polarity);
	}
}











