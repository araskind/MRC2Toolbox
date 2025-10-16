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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.MsDataPointComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsImportUtils;

public class MSMSImportTask extends AbstractTask {

	private LibraryMsFeature activeFeature;
	private File msmsDataFile;
	private boolean removeAllMassesAboveParent;
	private boolean removeAllMassesBelowCounts;
	private double minimalCounts;
	boolean limitPeaks;
	private int maxPeaks;

	private TandemMassSpectrum activeMsMs;

	public MSMSImportTask(
			LibraryMsFeature activeFeature,
			File msmsDataFile,
			boolean removeAllMassesAboveParent,
			boolean removeAllMassesBelowCounts,
			double minimalCounts,
			boolean limitPeaks,
			int maxPeaks) {
		super();
		this.activeFeature = activeFeature;
		this.msmsDataFile = msmsDataFile;
		this.removeAllMassesAboveParent = removeAllMassesAboveParent;
		this.removeAllMassesBelowCounts = removeAllMassesBelowCounts;
		this.minimalCounts = minimalCounts;
		this.limitPeaks = limitPeaks;
		this.maxPeaks = maxPeaks;
		activeMsMs = null;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		total = 100;
		processed = 20;
		taskDescription = "Reading data from MSMS file ...";
		activeMsMs = null;

		if(FilenameUtils.getExtension(msmsDataFile.getName()).equalsIgnoreCase("MSP")){

			try {
				activeMsMs = MsImportUtils.parseMSPspectrum(msmsDataFile, activeFeature);
			} catch (Exception e1) {

				e1.printStackTrace();
				setStatus(TaskStatus.ERROR);
return;

			}
		}
		if(FilenameUtils.getExtension(msmsDataFile.getName()).equalsIgnoreCase("XML")){

			try {
				activeMsMs = MsImportUtils.parseAgilentMsMsExportFile(msmsDataFile);
			} catch (Exception e1) {

				e1.printStackTrace();
				setStatus(TaskStatus.ERROR);
return;

			}
		}
		if(activeMsMs != null) {

			processed = 80;
			taskDescription = "RFiltering MSMS data ...";
			filterMsMs();
		}
		setStatus(TaskStatus.FINISHED);
	}

	private void filterMsMs() {

		TandemMassSpectrum filteredMsMs = new TandemMassSpectrum(activeMsMs);
		Collection<MsPoint> filteredPoints = filteredMsMs.getSpectrum();
		if(removeAllMassesAboveParent) {

			double maxMass = filteredMsMs.getParent().getMz() + 1.0;
			filteredPoints = filteredPoints.stream().filter(dp -> dp.getMz() < maxMass).collect(Collectors.toSet());
		}
		if(removeAllMassesBelowCounts && minimalCounts > 0.0d)
			filteredPoints = filteredPoints.stream().filter(dp -> dp.getIntensity() > minimalCounts).collect(Collectors.toSet());

		if(limitPeaks && maxPeaks > 0) {

			filteredPoints =
					filteredPoints.stream().sorted(new MsDataPointComparator(SortProperty.Intensity, SortDirection.DESC)).
					limit(maxPeaks).collect(Collectors.toSet());

			filteredPoints.add(filteredMsMs.getActualParentIon());
		}
		filteredMsMs.setSpectrum(filteredPoints);
		activeMsMs = filteredMsMs;
	}

	@Override
	public Task cloneTask() {

		return new MSMSImportTask(
				activeFeature,
				msmsDataFile,
				removeAllMassesAboveParent,
				removeAllMassesBelowCounts,
				minimalCounts,
				limitPeaks,
				maxPeaks);
	}

	/**
	 * @return the activeFeature
	 */
	public LibraryMsFeature getActiveFeature() {
		return activeFeature;
	}

	/**
	 * @return the activeMsMs
	 */
	public TandemMassSpectrum getActiveMsMs() {
		return activeMsMs;
	}
}



















