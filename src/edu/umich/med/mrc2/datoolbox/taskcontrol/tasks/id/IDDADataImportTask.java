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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.id;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.rawdata.MsMsfeatureExtractionTask;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class IDDADataImportTask extends AbstractTask implements TaskListener{

	private DataFile[] sourceRawFiles;
	private Range dataExtractionRtRange;
	private double precursorAlignmentRtWindow;
	private double precursorAlignmentMzWindow;
	private MassErrorType precursorAlignmentMzErrorType;
	private boolean removeAllMassesAboveParent;
	private double msMsCountsCutoff;
	private int maxFragmentsCutoff;

	public IDDADataImportTask(
			DataFile[] sourceRawFiles,
			Range dataExtractionRtRange,
			double precursorAlignmentRtWindow,
			double precursorAlignmentMzWindow,
			MassErrorType precursorAlignmentMzErrorType,
			boolean removeAllMassesAboveParent,
			double msMsCountsCutoff,
			int maxFragmentsCutoff) {
		super();
		this.sourceRawFiles = sourceRawFiles;
		this.dataExtractionRtRange = dataExtractionRtRange;
		this.precursorAlignmentRtWindow = precursorAlignmentRtWindow;
		this.precursorAlignmentMzWindow = precursorAlignmentMzWindow;
		this.precursorAlignmentMzErrorType = precursorAlignmentMzErrorType;
		this.removeAllMassesAboveParent = removeAllMassesAboveParent;
		this.msMsCountsCutoff = msMsCountsCutoff;
		this.maxFragmentsCutoff = maxFragmentsCutoff;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Importing MSMS features from raw data files";
		total = sourceRawFiles.length;
		processed = 0;
		try {
			for(DataFile f : sourceRawFiles) {
//				
//				MsMsfeatureExtractionTask fet = new MsMsfeatureExtractionTask(
//						f,
//						dataExtractionRtRange,
//						removeAllMassesAboveParent,
//						msMsCountsCutoff,
//						maxFragmentsCutoff);
//
//				fet.addTaskListener(this);
//				MRC2ToolBoxCore.getTaskController().addTask(fet);
			}
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
return;

		}
	}

	@Override
	public Task cloneTask() {

		return new IDDADataImportTask(
				sourceRawFiles,
				dataExtractionRtRange,
				precursorAlignmentRtWindow,
				precursorAlignmentMzWindow,
				precursorAlignmentMzErrorType,
				removeAllMassesAboveParent,
				msMsCountsCutoff,
				maxFragmentsCutoff);
	}

	@Override
	public void statusChanged(TaskEvent e) {
		// TODO Auto-generated method stub
		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(MsMsfeatureExtractionTask.class))
				finalizeMsMsfeatureExtractionTask((MsMsfeatureExtractionTask)e.getSource());
		}
	}
	
	private synchronized void finalizeMsMsfeatureExtractionTask(MsMsfeatureExtractionTask task) {
		
		processed++;

		if(processed == total)
			alignmsMsFeatures();
	}

	private void alignmsMsFeatures() {
		// TODO Auto-generated method stub
		taskDescription = "Aligning imported MSMS features";
		total = 100;
		processed = 0;

		setStatus(TaskStatus.FINISHED);
	}
}












