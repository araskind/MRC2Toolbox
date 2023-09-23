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
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.MSPField;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class MSPExportTask extends AbstractTask {

	private Collection<MsFeature>featuresToExport;
	private File exportFile;
	private boolean instrumentOnly;
	private static final DecimalFormat intensityFormat = new DecimalFormat("###");

	public MSPExportTask(Collection<MsFeature> featuresToExport, File exportFile, boolean instrumentOnly) {
		super();
		this.featuresToExport = featuresToExport;
		this.exportFile =
			FIOUtils.changeExtension(
				exportFile, MsLibraryFormat.MSP.getFileExtension());
		this.instrumentOnly = instrumentOnly;
	}

	@Override
	public void run() {

		setStatus(TaskStatus.PROCESSING);
		try {
			//	Filter features that have MSMS
			List<MsFeature> msmsFeatures = featuresToExport.stream().
				filter(f -> !f.getSpectrum().getTandemSpectra().isEmpty()).
				collect(Collectors.toList());
			if(msmsFeatures.isEmpty()) {
				setStatus(TaskStatus.FINISHED);
				return;
			}
			if(instrumentOnly) {

				msmsFeatures = msmsFeatures.stream().
					filter(f -> f.hasInstrumentMsMs()).
					collect(Collectors.toList());
				if(msmsFeatures.isEmpty()) {
					setStatus(TaskStatus.FINISHED);
					return;
				}
			}
			writeMspFile(msmsFeatures);
			setStatus(TaskStatus.FINISHED);
		}
		catch (Exception e1) {
			setStatus(TaskStatus.ERROR);
			e1.printStackTrace();
		}
	}

	private void writeMspFile(List<MsFeature> msmsFeatures) throws IOException {

		taskDescription = "Wtiting MSP output";
		total = msmsFeatures.size();
		processed = 0;
		final Writer writer = new BufferedWriter(new FileWriter(exportFile));
		for(MsFeature msf : msmsFeatures) {

			Collection<TandemMassSpectrum> tandemSpectra = msf.getSpectrum().getTandemSpectra();
			if(instrumentOnly)
				tandemSpectra = tandemSpectra.stream().filter(t -> t.getSpectrumSource().equals(SpectrumSource.EXPERIMENTAL)).
					collect(Collectors.toList());

			for(TandemMassSpectrum tandemMs : tandemSpectra) {

				//	writer.append(MSPField.NAME.getName() + ": " + msf.getId() + "\n");
				writer.append(MSPField.NAME.getName() + ": " + tandemMs.getId() + "\n");
				writer.append("Feature name: " + msf.getName() + "\n");
				if(msf.isIdentified()) {
					CompoundIdentity cid = msf.getPrimaryIdentity().getCompoundIdentity();
					writer.append(MSPField.SYNONYM.getName() + ": " + cid.getName() + "\n");
					if(cid.getFormula() != null)
						writer.append(MSPField.FORMULA.getName() + ": " + cid.getFormula() + "\n");
					if(cid.getInChiKey() != null)
						writer.append(MSPField.INCHI_KEY.getName() + ": " + cid.getInChiKey() + "\n");
				}
				String polarity = "P";
				if(msf.getPolarity().equals(Polarity.Negative))
					polarity = "N";
				writer.append(MSPField.ION_MODE.getName() + ": " + polarity + "\n");

				if(tandemMs.getCidLevel() >0)
					writer.append(MSPField.COLLISION_ENERGY.getName() + ": " + Double.toString(tandemMs.getCidLevel()) + "\n");

				writer.append(MSPField.PRECURSORMZ.getName() + ": " +
					MRC2ToolBoxConfiguration.getMzFormat().format(tandemMs.getParent().getMz()) + "\n");
				writer.append(MSPField.NUM_PEAKS.getName() + ": " + Integer.toString(tandemMs.getSpectrum().size()) + "\n");

				MsPoint[] msms = MsUtils.normalizeAndSortMsPattern(tandemMs.getSpectrum());
				int pointCount = 0;
				for(MsPoint point : msms) {

					writer.append(
						MRC2ToolBoxConfiguration.getMzFormat().format(point.getMz())
						+ " " + intensityFormat.format(point.getIntensity()) + "; ") ;
					pointCount++;
					if(pointCount % 5 == 0)
						writer.append("\n");
				}
				writer.append("\n\n");
			}
			processed++;
		}
		writer.flush();
		writer.close();
	}

	@Override
	public Task cloneTask() {
		return new MSPExportTask(
			featuresToExport, exportFile, instrumentOnly);
	}
}














