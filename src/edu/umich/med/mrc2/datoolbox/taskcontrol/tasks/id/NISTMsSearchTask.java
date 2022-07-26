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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id;

import static com.sun.nio.file.ExtendedWatchEventModifier.FILE_TREE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Date;

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class NISTMsSearchTask extends AbstractTask {

	private File resultCopy;

	public NISTMsSearchTask() {
		super();
		total = 100;
		processed = 20;
		taskDescription = "Running NIST MS/MS search";
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);

		// Remove old flag and results file
		File resultFile  = Paths.get(MRC2ToolBoxConfiguration.getrNistMsDir(), 
				MRC2ToolBoxConfiguration.NIST_SEARCH_HIT_LIST_FILE).toFile();
		if(resultFile.exists())
			resultFile.delete();
		
		File resultReadyFile  = Paths.get(MRC2ToolBoxConfiguration.getrNistMsDir(), 
				MRC2ToolBoxConfiguration.NIST_SEARCH_READY_FILE).toFile();
		
		if(resultReadyFile.exists())
			resultReadyFile.delete();

		String command = "\"" + MRC2ToolBoxConfiguration.getrNistMsDir() + File.separator
				+ MRC2ToolBoxConfiguration.NIST_EXECUTABLE_FILE + "\" /INSTRUMENT /PAR=2";
		
		processed = -5;
		try {
	        FileSystem fs = FileSystems.getDefault();
	        WatchService ws = fs.newWatchService();
	        Path pTemp = Paths.get(MRC2ToolBoxConfiguration.getrNistMsDir());
	        pTemp.register(ws, new WatchEvent.Kind[] {ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE}, FILE_TREE);
			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(command);
	        while(true)
	        {
	            WatchKey k = ws.take();
	            for (WatchEvent<?> e : k.pollEvents())
	            {
	                Object c = e.context();
	                if(c instanceof Path) {

	                	if(((Path)c).toFile().getName().equals(resultReadyFile.getName()) && e.kind().equals(ENTRY_MODIFY)){

	                		//	System.out.println("Search completed");
	                		ws.close();
	                		process.destroy();

	                		if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1)
	                			runtime.exec("taskkill /F /IM nistms.exe");

	                		//	Copy results file
	    					String timestamp = MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
	    					resultCopy = new File(MRC2ToolBoxCore.msSearchDir + "NIST_MSMS_SEARCH_RESULTS_" + timestamp + ".TXT");
	    					Files.copy(resultFile.toPath(), resultCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);

	    					// Cleanup working directory
	    					processed = 90;
	    					cleanupWorkingDirectory();

	    					setStatus(TaskStatus.FINISHED);
	                		return;
	                	}
	                }
	            }
	            k.reset();
	        }
		}
		catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void cleanupWorkingDirectory() {

		taskDescription = "Cleaning up ...";
		try {
			Files.find(Paths.get(MRC2ToolBoxConfiguration.getrNistMsDir()), Integer.MAX_VALUE,
				(filePath, fileAttr) -> filePath.toString().toLowerCase().endsWith(".hlm") && fileAttr.isRegularFile())
				.forEach(path -> {
					try {
						path.toFile().delete();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		processed = 100;
	}
	@Override
	public Task cloneTask() {
		return new NISTMsSearchTask();
	}

	/**
	 * @return the resultCopy
	 */
	public File getResultsFile() {
		return resultCopy;
	}


}