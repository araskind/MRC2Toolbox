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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.QualAutomation;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.gui.automator.AutomatorPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class QualAutomationDataProcessingTask extends AbstractTask {

	private File dataFile;
	private File methodFile;
	private File qualBinary;
	private AutomatorPanel automatorPanel;
	private String colorCode;
	
	public QualAutomationDataProcessingTask(
			File dataFile, 
			File methodFile, 
			File qualBinary) {

		super();
		this.dataFile = dataFile;
		this.methodFile = methodFile;
		this.qualBinary = qualBinary;

		total = 100;
		processed = 20;
		colorCode = "black";
		if(dataFile.getName().matches("(?i).+\\-N\\.d$"))
			colorCode = "blue";
		
		if(dataFile.getName().matches("(?i).+\\-P\\.d$"))
			colorCode = "red";
		
		taskDescription = "<HTML><font color = \"" + colorCode + "\">Data file " 
				+ dataFile.getName() + "</font> [ waiting ]";
		automatorPanel = (AutomatorPanel) MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.AUTOMATOR);		
	}
	
	@Override
	public void run() {
		
		setStatus(TaskStatus.PROCESSING);
		try {
			runQualAutomation();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cleanResultsDirectory();
		setStatus(TaskStatus.FINISHED);
	}
	
	protected void runQualAutomation() throws InterruptedException {

		taskDescription = "<HTML><font color = \"" + colorCode + "\">Processing " + dataFile.getName() + 
				"</font> data file using " + methodFile.getName() + " method";
		ProcessBuilder pb = new ProcessBuilder(
				qualBinary.getAbsolutePath(), 
				dataFile.getAbsolutePath(),
				methodFile.getAbsolutePath());
		File logFile = Paths
				.get(dataFile.getParentFile().getAbsolutePath(), 
						FilenameUtils.getBaseName(dataFile.getName()) + ".log")
				.toFile();
		try {
			pb.redirectErrorStream(true);
			pb.redirectOutput(Redirect.appendTo(logFile));
			Process p = pb.start();
			assert pb.redirectInput() == Redirect.PIPE;
			assert pb.redirectOutput().file() == logFile;
			assert p.getInputStream().read() == -1;
			int exitCode = p.waitFor();
			if (getStatus().equals(TaskStatus.CANCELED)) {
				try {
					p.destroyForcibly();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (exitCode == 0 && getStatus().equals(TaskStatus.PROCESSING)) {

				p.destroy();
				File cefOut = new File(FilenameUtils.removeExtension(dataFile.getAbsolutePath()) + ".cef");
				if (cefOut.exists()) {
					
					if(logFile.exists())
						logFile.delete();
					
					processed = 100;
					setStatus(TaskStatus.FINISHED);
				} else {
					processed = 0;
					setStatus(TaskStatus.EXTERNAL_ERROR);
					automatorPanel.addTaskToRerunQueue(this.cloneTask());
				}
			} else {
				automatorPanel.addTaskToRerunQueue(this.cloneTask());
				setStatus(TaskStatus.ERROR);
return;

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void cleanResultsDirectory() {
		
		File resultsDir = Paths.get(dataFile.getAbsolutePath(), "Results").toFile();
		if(resultsDir.exists()) {
			try {
				FileUtils.deleteDirectory(resultsDir);
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("Cleaning " + dataFile.getName());
		}
	}
	
	@Override
	public Task cloneTask() {

		QualAutomationDataProcessingTask clonedTask = new QualAutomationDataProcessingTask(
				this.dataFile, 
				this.methodFile, 
				this.qualBinary);

		return clonedTask;
	}

	public File getDataFile() {
		return dataFile;
	}

	public File getMethodFile() {
		return methodFile;
	}
}
