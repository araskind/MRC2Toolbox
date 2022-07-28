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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.MsDataPointComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.data.lims.Injection;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.sirius.SiriusMsField;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class SiriusMsExportTask extends AbstractTask {

	private Collection<MSFeatureInfoBundle>featuresToExport;
	private File exportFile;
	private boolean instrumentOnly;
	private static final DecimalFormat intensityFormat = new DecimalFormat("###");
	private static final DateFormat dateFormat = 
			new SimpleDateFormat(MRC2ToolBoxConfiguration.DATE_TIME_FORMAT_DEFAULT);
	private Map<String,Injection>injectionMap;

	public SiriusMsExportTask(
			Collection<MSFeatureInfoBundle> featuresToExport,
			File exportFile,
			boolean instrumentOnly) {
		super();
		this.featuresToExport = featuresToExport;
		this.exportFile =
			FIOUtils.changeExtension(
				exportFile, MsLibraryFormat.SIRIUS_MS.getFileExtension());
		this.instrumentOnly = instrumentOnly;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			//	Filter features that have MSMS
			List<MSFeatureInfoBundle> msmsFeatures = featuresToExport.stream().
				filter(f -> !f.getMsFeature().getSpectrum().getTandemSpectra().isEmpty()).
				collect(Collectors.toList());
			if(msmsFeatures.isEmpty()) {
				setStatus(TaskStatus.FINISHED);
				return;
			}
			if(instrumentOnly) {

				msmsFeatures = msmsFeatures.stream().
					filter(f -> f.getMsFeature().hasInstrumentMsMs()).
					collect(Collectors.toList());
				if(msmsFeatures.isEmpty()) {
					setStatus(TaskStatus.FINISHED);
					return;
				}
			}
			createInjectionMap(msmsFeatures);
			writeMsFile(msmsFeatures);
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e1) {
			setStatus(TaskStatus.ERROR);
			e1.printStackTrace();
		}
	}

	private void createInjectionMap(List<MSFeatureInfoBundle> msmsFeatures) {

		injectionMap = new TreeMap<String,Injection>();
		List<String> injIds = msmsFeatures.stream().
				map(f -> f.getInjectionId()).distinct().
				filter(i -> !i.equals(null)).sorted().
				collect(Collectors.toList());

		if(injIds.isEmpty())
			return;

		for(String id : injIds) {

			try {
				Injection injection = IDTUtils.getInjectionById(id);
				if(injection != null)
					injectionMap.put(id, injection);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void writeMsFile(List<MSFeatureInfoBundle> msmsFeatures) throws IOException {

		taskDescription = "Wtiting MSP output";
		total = msmsFeatures.size();
		processed = 0;
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));
		for(MSFeatureInfoBundle bundle : msmsFeatures) {

			MsFeature msf = bundle.getMsFeature();	
			
			//	Sirius doesn't support multiple charges at the moment
			if(Math.abs(msf.getCharge()) > 1)
				continue;
			
			Collection<TandemMassSpectrum> tandemSpectra = msf.getSpectrum().getTandemSpectra();
			if(instrumentOnly)
				tandemSpectra = tandemSpectra.stream().filter(t -> t.getSpectrumSource().equals(SpectrumSource.EXPERIMENTAL)).
					collect(Collectors.toList());
			
			if(tandemSpectra.isEmpty())
				continue;

			for(TandemMassSpectrum tandemMs : tandemSpectra) {

				if(tandemMs.getSpectrum().isEmpty())
					continue;

				writer.append(">" + SiriusMsField.COMPOUND.getName() + " " 
						+ msf.getId() + "; " + msf.getName() + "; " + createComment(bundle) + "\n");
				if(msf.isIdentified()) {
					CompoundIdentity cid = msf.getPrimaryIdentity().getCompoundIdentity();
					if(cid.getFormula() != null)
						writer.append(">" + SiriusMsField.FORMULA.getName() + " " + cid.getFormula() + "\n");
				}
				writer.append(">" + SiriusMsField.PARENT_MASS.getName() + " " + 
						MRC2ToolBoxConfiguration.getMzFormat().format(tandemMs.getParent().getMz()) + "\n");

				writer.append(">" + SiriusMsField.CHARGE.getName() + " " + Integer.toString(msf.getCharge()) + "\n");
				
				//	TODO Write MS1 if available but NOT the whole scan, only if proper MS1 feature is present
//				if(msf.getSpectrum().getPrimaryAdduct() != null) {
//					
//					MsPoint[] paMs = msf.getSpectrum().getMsForAdduct(msf.getSpectrum().getPrimaryAdduct(), true);
//					if(paMs != null && paMs.length > 1) {
//						writer.append("\n>" + SiriusMsField.MS1.getName() + "\n");
//						for(MsPoint point : paMs) {
//
//							writer.append(
//								CaConfiguration.getMzFormat().format(point.getMz()) + " " + 
//								intensityFormat.format(point.getIntensity()) + "\n") ;
//						}
//					}
//				}				
				//	Write MS2
				if(tandemMs.getCidLevel() >0) {
					writer.append("\n>" + SiriusMsField.COLLISION_ENERGY.getName() + " " + 
							Double.toString(tandemMs.getCidLevel()) + "\n");
				}
				else {
					writer.append(">" + SiriusMsField.MS2.getName() + " " + 
							Double.toString(tandemMs.getCidLevel()) + "\n");
				}
				MsPoint[] msms = normalizeAndSortMsPatternForMsp(tandemMs.getSpectrum());
				for(MsPoint point : msms) {

					writer.append(
						MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz()) + " " + 
						intensityFormat.format(point.getIntensity()) + "\n") ;
				}
				writer.append("\n");
			}
			processed++;
		}
		writer.flush();
		writer.close();
	}

	private String createComment(MSFeatureInfoBundle bundle) {

		String comment = MSPField.COMMENT.getName() + ": ";
		comment += "RT "+ MRC2ToolBoxConfiguration.getRtFormat().format(bundle.getMsFeature().getRetentionTime()) + " min. | ";
		String injId = bundle.getInjectionId();
		if(injId != null) {
			Injection injection = injectionMap.get(injId);
			if(injection != null) {
				comment += "Data file: " + injection.getDataFileName() + "; ";
				comment += "Timestamp: " + dateFormat.format(injection.getTimeStamp()) + "; ";
			}
		}
		comment += "Acq. method: " + bundle.getAcquisitionMethod().getName() + "; ";
		comment += "DA method: " + bundle.getDataExtractionMethod().getName();
		return comment;
	}

	private MsPoint[] normalizeAndSortMsPatternForMsp(Collection<MsPoint>pattern) {

		MsPoint basePeak = Collections.max(pattern, Comparator.comparing(MsPoint::getIntensity));
		double maxIntensity  = basePeak.getIntensity();

		return pattern.stream()
				.map(dp -> new MsPoint(dp.getMz(), Math.round(dp.getIntensity()/maxIntensity*999.0d)))
				.sorted(new MsDataPointComparator(SortProperty.MZ)).
				toArray(size -> new MsPoint[size]);
	}

	@Override
	public Task cloneTask() {
		return new SiriusMsExportTask(
			featuresToExport, exportFile, instrumentOnly);
	}
}














