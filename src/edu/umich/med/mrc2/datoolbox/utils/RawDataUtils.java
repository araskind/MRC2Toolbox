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

package edu.umich.med.mrc2.datoolbox.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.RawMsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.LCMSDataSubset;
import umich.ms.datatypes.lcmsrun.LCMSRunInfo;
import umich.ms.datatypes.scan.IScan;
import umich.ms.datatypes.scan.props.PrecursorInfo;
import umich.ms.datatypes.scancollection.IScanCollection;
import umich.ms.datatypes.scancollection.ScanIndex;
import umich.ms.datatypes.spectrum.ISpectrum;
import umich.ms.fileio.exceptions.FileParsingException;

public class RawDataUtils {

	public static Map<Integer, IScan>getCompleteScanMap(IScanCollection scans){
		
		 TreeMap<Integer, IScan> scanMap = new TreeMap<Integer, IScan>();
		 TreeMap<Integer, ScanIndex> msLevel2index = scans.getMapMsLevel2index();
		 msLevel2index.forEach((msLevel, index) -> {
			 index.getNum2scan().forEach((scanNum, scan) -> scanMap.put(scanNum, scan));
		 }); 
		 return  scanMap;
	}
		
	public static Collection<MsPoint> getScanPoints(IScan scan, double minIntensityCutoff) { 
		
		Collection<MsPoint> pattern = new ArrayList<MsPoint>();
		ISpectrum spectrum = scan.getSpectrum();
		if(spectrum == null) {
			try {
				spectrum = scan.fetchSpectrum();
			} catch (FileParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int scanNum = scan.getNum();
		if(spectrum != null) {
			
			double[] mzs = spectrum.getMZs();
			double[] intens = spectrum.getIntensities();
			for(int i=0; i<mzs.length; i++) {
				
				if(intens[i] > minIntensityCutoff)
					pattern.add(new MsPoint(mzs[i], intens[i], scanNum));
			}
		}
		return pattern;
	}
	
	public static String getScanWithMetadata(IScan scan) { 
		
		if(scan == null)
			return "";
		
		Collection<String> lines = new ArrayList<String>();
		lines.add(scan.toString());
		
		String name = scan.getScanCollection().getDataSource().getName();
		lines.add("Path: " + name);
		
		LCMSRunInfo ri = scan.getScanCollection().getDataSource().getRunInfo();
		if(ri.getOriginalFiles() != null && !ri.getOriginalFiles().isEmpty())
			lines.add("Original data file: "+ ri.getOriginalFiles().get(0).name);
		
		if(ri.getRunStartTime() != null)
			lines.add("Run start time: " + 
					MRC2ToolBoxConfiguration.getDateTimeFormat().format(ri.getRunStartTime()));
		
		if(scan.getPrecursor() != null) {			
			
			PrecursorInfo pi = scan.getPrecursor();
			lines.add("Precursor M/Z: " + 
					MsUtils.spectrumMzExportFormat.format(pi.getMzTarget()));
			Range isolationWindow = new Range(pi.getMzRangeStart(), pi.getMzRangeEnd());
			lines.add("Isolation window: " + 
					isolationWindow.getFormattedString(MsUtils.spectrumMzExportFormat));
			
			lines.add("Parent scan #: " + pi.getParentScanNum().toString());
			if(pi.getActivationInfo() != null)
				lines.add(pi.getActivationInfo().toString());
		}
		ISpectrum spectrum = scan.getSpectrum();
		if(spectrum == null) {
			try {
				spectrum = scan.fetchSpectrum();
			} catch (FileParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		double maxIntensity = scan.getBasePeakIntensity();
		lines.add("");
		lines.add("M/Z\tIntensity\tRelative intensity");
		if(spectrum != null) {
			
			double[] mzs = spectrum.getMZs();
			double[] intens = spectrum.getIntensities();
			for(int i=0; i<mzs.length; i++) {
				
				String line = 					
						MsUtils.spectrumMzExportFormat.format(mzs[i]) + "\t" + 
						MsUtils.spectrumIntensityFormat.format(intens[i]) + "\t" + 
						MsUtils.spectrumMzExportFormat.format(intens[i]/maxIntensity);
				lines.add(line);
			}
		}
		return StringUtils.join(lines, "\n");
	}
	
	public static Collection<MsPoint> getScanPoints(IScan scan) { 		
		return getScanPoints(scan, 0.0d);
	}
	
	public static Collection<RawMsPoint> getRawScanPoints(IScan scan, double minIntensityCutoff) { 
		
		Collection<RawMsPoint> pattern = new ArrayList<RawMsPoint>();
		ISpectrum spectrum = scan.getSpectrum();
		if(spectrum == null) {
			try {
				spectrum = scan.fetchSpectrum();
			} catch (FileParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		int scanNum = scan.getNum();
		if(spectrum != null) {
			
			double[] mzs = spectrum.getMZs();
			double[] intens = spectrum.getIntensities();
			for(int i=0; i<mzs.length; i++) {
				
				if(intens[i] > minIntensityCutoff)
					pattern.add(new RawMsPoint(mzs[i], intens[i], scanNum));
			}
		}
		return pattern;
	}
	
	public static Collection<RawMsPoint> getRawScanPoints(IScan scan) { 		
		return getRawScanPoints(scan, 0.0d);
	}
	
	public static Map<DataFile, LCMSData>createDataSources(
			Collection<DataFile> files, int msLevel, Object parent) throws FileParsingException{

		Map<DataFile, LCMSData>dataSourcesmap = new HashMap<DataFile,LCMSData>();
		
		for(DataFile df : files) {
			
			LCMSData fileData = RawDataManager.getRawData(df);
			if(msLevel == 1)
				fileData.load(LCMSDataSubset.MS1_WITH_SPECTRA, parent);
			
			if(msLevel == 2)
				fileData.load(LCMSDataSubset.MS2_WITH_SPECTRA, parent);
			
			dataSourcesmap.put(df,fileData);
		}		
		return dataSourcesmap;
	}
	
	public static String getScanLabel(IScan s) {
		
		String labelText = "Scan #" + s.getNum() + 
				" (RT " + MRC2ToolBoxConfiguration.getRtFormat().format(s.getRt()) + ")";
		if (s.getMsLevel() > 1) {

			if (s.getPrecursor().getMzRangeStart() != null && s.getPrecursor().getMzRangeEnd() != null) {
				labelText += "; isol. "
						+ MRC2ToolBoxConfiguration.getMzFormat().format(s.getPrecursor().getMzRangeStart()) + "~"
						+ MRC2ToolBoxConfiguration.getMzFormat().format(s.getPrecursor().getMzRangeEnd());
			} else if (s.getPrecursor().getMzTarget() != null) {
				labelText += "; Prec. "
						+ MRC2ToolBoxConfiguration.getMzFormat().format(s.getPrecursor().getMzTarget());
			} else {
				if (s.getPrecursor().getMzTargetMono() != null) {
					labelText += "; Prec. "
							+ MRC2ToolBoxConfiguration.getMzFormat().format(s.getPrecursor().getMzTargetMono());
				}
			}
		}
		labelText += " (" + s.getPolarity().toString() + ")";
		return labelText;
	}
	
	public static double getScanPrecursorMz(IScan s) {
		
		if (s.getMsLevel() > 1) {

			if (s.getPrecursor().getMzRangeStart() != null && s.getPrecursor().getMzRangeEnd() != null) {
				
				return (s.getPrecursor().getMzRangeStart() + s.getPrecursor().getMzRangeEnd()) / 2.0d;
				
			} else if (s.getPrecursor().getMzTarget() != null) {
				return s.getPrecursor().getMzTarget();
			} else {
				if (s.getPrecursor().getMzTargetMono() != null) {
					return s.getPrecursor().getMzTargetMono();
				}
			}
		}
		return 0.0d;
	}
	
	public static umich.ms.datatypes.scan.props.Polarity getScanPolarity(Polarity polarity){
		
		if(polarity.equals(Polarity.Positive))
			return umich.ms.datatypes.scan.props.Polarity.POSITIVE;
		
		if(polarity.equals(Polarity.Negative))
			return umich.ms.datatypes.scan.props.Polarity.NEGATIVE;
		
		if(polarity.equals(Polarity.Neutral))
			return umich.ms.datatypes.scan.props.Polarity.NEUTRAL;
		
		return null;
	}
	
	public static Polarity getPolarityFromScan(umich.ms.datatypes.scan.props.Polarity scanPolarity){
		
		if(scanPolarity.equals(umich.ms.datatypes.scan.props.Polarity.POSITIVE))
			return Polarity.Positive;
		
		if(scanPolarity.equals(umich.ms.datatypes.scan.props.Polarity.NEGATIVE))
			return Polarity.Negative;
		
		if(scanPolarity.equals(umich.ms.datatypes.scan.props.Polarity.NEUTRAL))
			return Polarity.Neutral;
		
		return null;
	}
	
	public static Collection<Double>getMSMSScanRtMarkersForFeature(MsFeature msFeature, DataFile df){
		
		Collection<Double>markers = new TreeSet<Double>();
		if(msFeature.getSpectrum() != null
				&& msFeature.getSpectrum().getExperimentalTandemSpectrum() != null) {
			
			Set<Integer> msmsScanNums = msFeature.getSpectrum().
					getExperimentalTandemSpectrum().getAveragedScanNumbers().keySet();
			if(!msmsScanNums.isEmpty()) {
				
				LCMSData rawData = RawDataManager.getRawData(df);	
				ScanIndex si = rawData.getScans().getMapMsLevel2index().get(2);
				if(si == null)
					return markers;
				
				TreeMap<Integer, IScan> num2scan = 
						rawData.getScans().getMapMsLevel2index().get(2).getNum2scan();

				for(int scanNum : msmsScanNums) {
					IScan scan = num2scan.get(scanNum);
					if(scan != null)
						markers.add(scan.getRt());
				}
			}
		}	
		if(markers.isEmpty())
			markers.add(msFeature.getRetentionTime());
		
		return markers;
	}
}
