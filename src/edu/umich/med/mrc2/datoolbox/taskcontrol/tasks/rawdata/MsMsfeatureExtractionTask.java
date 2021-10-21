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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SupportedRawDataTypes;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.Range;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.LCMSDataSubset;
import umich.ms.datatypes.scan.IScan;
import umich.ms.datatypes.scancollection.IScanCollection;
import umich.ms.datatypes.scancollection.ScanIndex;
import umich.ms.fileio.exceptions.FileParsingException;
import umich.ms.fileio.filetypes.mzml.MZMLFile;
import umich.ms.fileio.filetypes.mzxml.MZXMLFile;
import umich.ms.fileio.filetypes.xmlbased.AbstractXMLBasedDataSource;

public class MsMsfeatureExtractionTask extends AbstractTask {

	private File sourceRawFile;
	private Range dataExtractionRtRange;
	private boolean removeAllMassesAboveParent;
	private double msMsCountsCutoff;
	private int maxFragmentsCutoff;

	private LCMSData data;
	private Collection<MsFeature>features;

	public MsMsfeatureExtractionTask(
			File sourceRawFile,
			Range dataExtractionRtRange,
			boolean removeAllMassesAboveParent,
			double msMsCountsCutoff,
			int maxFragmentsCutoff) {
		super();
		this.sourceRawFile = sourceRawFile;
		this.dataExtractionRtRange = dataExtractionRtRange;
		this.removeAllMassesAboveParent = removeAllMassesAboveParent;
		this.msMsCountsCutoff = msMsCountsCutoff;
		this.maxFragmentsCutoff = maxFragmentsCutoff;
		features = new ArrayList<MsFeature>();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		setStatus(TaskStatus.PROCESSING);
		taskDescription = "Importing data from file " + sourceRawFile.getName();
		total = 100;
		processed = 0;
		try {
			createDataSource();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			filterAndDenoise();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
		try {
			extractMSMSFeatures();
		}
		catch (Exception e1) {
			e1.printStackTrace();
			setStatus(TaskStatus.ERROR);
		}
	}

	private void filterAndDenoise() {
		// TODO Auto-generated method stub
		taskDescription = "Filtering and de-noising " + sourceRawFile.getName();
		total = 100;
		processed = 0;
	}

	private void extractMSMSFeatures() {

		taskDescription = "Extracting MSMS features from " + sourceRawFile.getName();
		ScanIndex msmsScansIndex = data.getScans().getMapMsLevel2index().get(2);
		total =  msmsScansIndex.getNum2scan().size();
		processed = 0;		
		
		for(Entry<Integer, IScan> entry: msmsScansIndex.getNum2scan().entrySet()) {
			
			IScan s = entry.getValue();		
			IScan parentScan = getParentScan(s);
			if(parentScan == null) {
				processed++;
				continue;			
			}
			Polarity polarity = Polarity.Positive;
			if(s.getPolarity().equals(umich.ms.datatypes.scan.props.Polarity.NEGATIVE))
				polarity = Polarity.Negative;
			
			MsFeature f = new MsFeature(s.getRt(), polarity);
			MassSpectrum spectrum = new MassSpectrum();
			spectrum.addDataPoints(getDataPointsForScan(parentScan));			
			MsPoint parent = new MsPoint(
					s.getPrecursor().getMzTarget(), 
					s.getPrecursor().getIntensity());			
			TandemMassSpectrum msms = new TandemMassSpectrum(
					2, 
					parent,
					getDataPointsForScan(s),
					polarity);
			spectrum.addTandemMs(msms);		
			f.setSpectrum(spectrum);
			features.add(f);
			processed++;
		}
	}
	
	private Collection<MsPoint>getDataPointsForScan(IScan s){
		
		Collection<MsPoint>points = new ArrayList<MsPoint>();
		double[]mzs = s.getSpectrum().getMZs();
		double[]intensities = s.getSpectrum().getIntensities();
		for(int i=0; i<mzs.length; i++)
			points.add(new MsPoint(mzs[i], intensities[i]));		
		
		return points;
	}
	
	private IScan getParentScan(IScan s) {
		
		if(s.getPrecursor() == null)
			return null;
					
		int parentScanNumber = s.getPrecursor().getParentScanNum();
		return data.getScans().getScanByNum(parentScanNumber);
	}
	
	private void createDataSource() throws FileParsingException {
	
		if (isCanceled())
			return;
		
		data = RawDataManager.getRawData(sourceRawFile);
		
		if(!data.getScans().getMapMsLevel2index().containsKey(2)) {
			System.out.println("No MSMS data in file " + sourceRawFile.getName());
			setStatus(TaskStatus.ERROR);
			return;
		}
		data.load(LCMSDataSubset.WHOLE_RUN, this);		
	}

	@Override
	public Task cloneTask() {
		return new MsMsfeatureExtractionTask(
				sourceRawFile,
				dataExtractionRtRange,
				removeAllMassesAboveParent,
				msMsCountsCutoff,
				maxFragmentsCutoff);
	}

	public Collection<MsFeature> getMSMSFeatures() {
		return features;
	}
}
