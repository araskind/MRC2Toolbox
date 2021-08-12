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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class RawDataCoversionTask extends AbstractTask {
	
	private File outputDir ;
	private File fileToConvert;

	public RawDataCoversionTask(File outputDir, File fileToConvert) {
		super();
		this.outputDir = outputDir;
		this.fileToConvert = fileToConvert;
	}

	@Override
	public void run() {
		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Converting " + fileToConvert.getName() + " ...";
		total = 100;
		processed = 30;		
		setStatus(TaskStatus.PROCESSING);
		String msConvertBinary = MRC2ToolBoxConfiguration.getMsConvertExecutableFile();
		String parameters = "--mzML --64 -z --filter \"peakPicking vendor\" -o ";
		String command = "\"" + msConvertBinary + "\" \"" + fileToConvert.getAbsolutePath() + "\" " + 
				parameters + " \"" + outputDir.getAbsolutePath() + "\"";
//		Runtime runtime = Runtime.getRuntime();
//		Process process = null;
//		try {
//			process = runtime.exec(command);
//			if (getStatus().equals(TaskStatus.CANCELED)) {
//				try {
//					process.destroyForcibly();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//			process.waitFor();
//			setStatus(TaskStatus.FINISHED);
//		} catch (IOException e1) {
//			e1.printStackTrace();
//			setStatus(TaskStatus.ERROR);
//		} catch (InterruptedException e2) {
//			e2.printStackTrace();
//			setStatus(TaskStatus.ERROR);
//		}
		try {
			Runtime runtime = Runtime.getRuntime();
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos, true, "UTF-8");
			System.setErr(ps);
			Process process = runtime.exec(command);
			int exitCode = process.waitFor();
			if (exitCode == 0) {
				process.destroy();
				processed = 100;
				setStatus(TaskStatus.FINISHED);
			} else {
				errorMessage = new String(baos.toByteArray(), StandardCharsets.UTF_8);
				System.out.println("PepSearch error");
				System.out.println(errorMessage);
				System.out.println(command);
				setStatus(TaskStatus.ERROR);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		} catch (InterruptedException e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	@Override
	public Task cloneTask() {
		// TODO Auto-generated method stub
		return null;
	}



}
