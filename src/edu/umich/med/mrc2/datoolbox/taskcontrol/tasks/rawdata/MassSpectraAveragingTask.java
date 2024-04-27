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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.AverageMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import edu.umich.med.mrc2.datoolbox.utils.RawDataUtils;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.scan.IScan;
import umich.ms.datatypes.scan.StorageStrategy;
import umich.ms.datatypes.scancollection.IScanCollection;
import umich.ms.datatypes.scancollection.ScanIndex;

public class MassSpectraAveragingTask extends AbstractTask {

	private Collection<DataFile> files;
	private Range rtRange;
	private Double mzBinWidth;
	private MassErrorType errorType;
	private Collection<AverageMassSpectrum>extractedSpectra;
	private Map<DataFile, LCMSData> dataSources;
	private int msLevel;
	
	public MassSpectraAveragingTask(
			Collection<DataFile> files, 
			Range rtRange, 
			Double mzBinWidth,
			MassErrorType errorType,
			int msLevel) {
		super();
		this.files = files;
		this.rtRange = rtRange;
		this.mzBinWidth = mzBinWidth;
		this.errorType = errorType;
		this.msLevel = msLevel;
		extractedSpectra = new ArrayList<AverageMassSpectrum>();
		taskDescription = "Extracting spectra";
	}
	
	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		total = files.size();
		processed = 0;
		try {
			dataSources = RawDataUtils.createDataSources(files, msLevel, this);
			for(DataFile f : files) {
				extractAverageMs(f, rtRange, mzBinWidth, errorType, msLevel);
				processed++;
			}
		} 
		catch (Throwable e) {
			e.printStackTrace();
			setStatus(TaskStatus.ERROR);
			return;
		}
		this.setStatus(TaskStatus.FINISHED);
	}

	private void extractAverageMs(
			DataFile f, 
			Range rtRange2, 
			Double mzBinWidth2, 
			MassErrorType errorType2,
			int msLevel2) {

		taskDescription = "Extracting average spectrum from " + f.getName();
		AverageMassSpectrum avgMs = new AverageMassSpectrum(f, msLevel2, rtRange2);
		LCMSData dataSource = dataSources.get(f);
		IScanCollection scans = dataSource.getScans();
		scans.isAutoloadSpectra(true);
		scans.setDefaultStorageStrategy(StorageStrategy.SOFT);
		TreeMap<Integer, ScanIndex> msLevel2index = scans.getMapMsLevel2index();
		ScanIndex ms2idx = msLevel2index.get(msLevel2);		
		if(ms2idx != null) {
				
			Set<IScan> filteredScans = ms2idx.getNum2scan().values().stream().
					filter(s -> rtRange.contains(s.getRt())).collect(Collectors.toSet());			
			
			Collection<MsPoint> inputPoints = 
					filteredScans.stream().
					flatMap(s -> RawDataUtils.getScanPoints(s).stream()).
					collect(Collectors.toList());
			
			Collection<MsPoint> avgPoints = 
					MsUtils.averageMassSpectrum(inputPoints, mzBinWidth2, errorType2);
			avgMs.getMasSpectrum().addDataPoints(avgPoints);
			extractedSpectra.add(avgMs);
			f.getAverageSpectra().add(avgMs);
		}		
		dataSource.releaseMemory();
	}

	@Override
	public Task cloneTask() {
		return new MassSpectraAveragingTask(files, rtRange, mzBinWidth, errorType, msLevel);
	}

	public Collection<AverageMassSpectrum> getExtractedSpectra() {
		return extractedSpectra;
	}
}




