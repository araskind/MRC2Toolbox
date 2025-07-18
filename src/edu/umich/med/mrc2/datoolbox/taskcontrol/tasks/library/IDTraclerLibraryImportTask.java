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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library;

import java.io.File;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class IDTraclerLibraryImportTask extends AbstractTask {
	
	private File libraryFile;
	private CompoundLibrary library;
	
	public IDTraclerLibraryImportTask(File libraryFile) {
		super();
		this.libraryFile = libraryFile;
	}

	@Override
	public void run() {

		taskDescription = "Importing IDTracker library from " + libraryFile.getName();
		setStatus(TaskStatus.PROCESSING);
		try {
			readIDTrackerLibraryFromFile();
		}
		catch (Exception e) {
			reportErrorAndExit(e);
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	private void readIDTrackerLibraryFromFile() {

		SAXBuilder sax = new SAXBuilder();
		Document doc = null;
		try {			
			doc = sax.build(libraryFile);
		} catch (Exception e) {
			reportErrorAndExit(e);
		}
		if(doc != null) {
			
			Element rootNode = doc.getRootElement();
			try {
				library = new CompoundLibrary(rootNode);
			} catch (Exception e) {
				reportErrorAndExit(e);
			}
		}
	}

	@Override
	public Task cloneTask() {
		return new IDTraclerLibraryImportTask(libraryFile);
	}

	public CompoundLibrary getLibrary() {
		return library;
	}
}
