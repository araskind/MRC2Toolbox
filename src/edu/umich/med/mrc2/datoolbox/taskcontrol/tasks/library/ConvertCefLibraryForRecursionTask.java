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
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentDatabaseFields;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.cpd.CompoundDatabaseUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.InformationDialog;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cef.CEFProcessingTask;
import edu.umich.med.mrc2.datoolbox.utils.LibraryUtils;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class ConvertCefLibraryForRecursionTask extends CEFProcessingTask {

	private boolean combineAdducts;
	private TreeSet<String> unmatchedAdducts;

	public ConvertCefLibraryForRecursionTask(			
			File sourceLibraryFile,
			File outputLibraryFile,
			boolean combineAdducts) {

		this.inputCefFile = sourceLibraryFile;
		this.outputCefFile = outputLibraryFile;
		this.combineAdducts = combineAdducts;
		unmatchedAdducts = new TreeSet<String>();
	}

	@Override
	public void run() {

		taskDescription = 
				"Creating library for recursion from " + inputCefFile.getName();
		setStatus(TaskStatus.PROCESSING);
		createLibraryFeaturetListFromCefFile();
		if(!unmatchedAdducts.isEmpty()){
			errorMessage = "Unmatched adducts: " + 
					StringUtils.join(unmatchedAdducts, "; ");
			setStatus(TaskStatus.ERROR);
			return;
		}
		if(libraryFeatureListForExport == null || libraryFeatureListForExport.isEmpty()) {			
			errorMessage = "Failed to parse input file, no features to export";
			setStatus(TaskStatus.ERROR);
			return;
		}
		try {
			writeCefLibrary(
					libraryFeatureListForExport,
					combineAdducts);
		} catch (Exception e) {
			setStatus(TaskStatus.ERROR);
			e.printStackTrace();
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	@Override
	public Task cloneTask() {

		return new ConvertCefLibraryForRecursionTask(
				inputCefFile,
				outputCefFile,
				combineAdducts);
	}
}









































