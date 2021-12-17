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

import java.io.File;
import java.util.ArrayList;

import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.PepSearchOutputObject;
import edu.umich.med.mrc2.datoolbox.data.compare.PepSearchOutputObjectComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class NISTMsPepSearchDataUploadTask extends NISTMsPepSearchTask {
	
	private boolean validateOnly;
	private boolean addMissingParameters;
	private int maxHitsPerFeature;
	
	private static final PepSearchOutputObjectComparator psoScoreComparator = 
			new PepSearchOutputObjectComparator(SortProperty.msmsScore, SortDirection.DESC);

	public NISTMsPepSearchDataUploadTask(
			File resultFile, 
			NISTPepSearchParameterObject pepSearchParameterObject,
			int maxHitsPerFeature) {
		this(resultFile, pepSearchParameterObject, false, false, maxHitsPerFeature);
		this.maxHitsPerFeature = maxHitsPerFeature;
	}
	
	public NISTMsPepSearchDataUploadTask(
			File resultFile, 
			NISTPepSearchParameterObject pepSearchParameterObject, 
			boolean validateOnly,
			boolean addMissingParameters,
			int maxHitsPerFeature) {
		super();
		this.resultFile = resultFile;
		this.validateOnly = validateOnly;
		this.pepSearchParameterObject = pepSearchParameterObject;
		this.addMissingParameters = addMissingParameters;
		this.maxHitsPerFeature = maxHitsPerFeature;
	}

	@Override
	public void run() {

		pooList = new ArrayList<PepSearchOutputObject>();
		pooListForUpdateOnly = new ArrayList<PepSearchOutputObject>();
		setStatus(TaskStatus.PROCESSING);
		initLogFile();
		try {
			parseSearchResults();
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			filterSearchResults(false);
		} catch (Exception e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		if(validateOnly) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		if(!pooList.isEmpty()) {
			
			try {
				uploadSearchResults();
			} catch (Exception e) {
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
		}
		if(!pooListForUpdateOnly.isEmpty() && addMissingParameters) {
			try {
				updateExistingHits();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				setStatus(TaskStatus.ERROR);
			}
		}
		setStatus(TaskStatus.FINISHED);
	}
	
	@Override
	public Task cloneTask() {
		return new NISTMsPepSearchDataUploadTask(
				resultFile, 
				pepSearchParameterObject, 
				validateOnly, 
				addMissingParameters, 
				maxHitsPerFeature);
	}
}
