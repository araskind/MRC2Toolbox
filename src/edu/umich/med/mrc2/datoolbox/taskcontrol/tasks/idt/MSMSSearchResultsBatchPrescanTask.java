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

package edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundDatabaseEnum;
import edu.umich.med.mrc2.datoolbox.data.enums.SpectrumSource;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.MSMSLibraryUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class MSMSSearchResultsBatchPrescanTask extends AbstractTask implements TaskListener{

	private Collection<File>inputCefFiles;
	private File missingCompoundsListFile;
	private Collection<CompoundIdentity>missingIdentities;
	private Map<CompoundIdentity, Collection<TandemMassSpectrum>>idSpectrumMap;
	private IChemObjectBuilder builder;
	private Collection<String>prescanLog;

	public MSMSSearchResultsBatchPrescanTask(Collection<File> inputCefFiles, File missingCompoundsListFile) {
		super();
		this.inputCefFiles = inputCefFiles;
		this.missingCompoundsListFile = missingCompoundsListFile;
		builder = DefaultChemObjectBuilder.getInstance();
		prescanLog = new ArrayList<String>();
	}

	@Override
	public void run() {

		taskDescription = "Scanning MSMS search results for database update";
		setStatus(TaskStatus.PROCESSING);
		total = inputCefFiles.size();
		processed = 0;
		missingIdentities = new HashSet<CompoundIdentity>();
		idSpectrumMap = new HashMap<CompoundIdentity, Collection<TandemMassSpectrum>>();
		for(File cefFile : inputCefFiles) {

			IDTCefMSMSPrescanOrImportTask task = new IDTCefMSMSPrescanOrImportTask(cefFile);
			//	MSMSSearchResultsPrescanTask task = new MSMSSearchResultsPrescanTask(cefFile);
			task.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(task);
		}
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);
			if (e.getSource().getClass().equals(MSMSSearchResultsPrescanTask.class)) {

				MSMSSearchResultsPrescanTask task = (MSMSSearchResultsPrescanTask)e.getSource();
				extractData(task);
				processed++;
				if(processed == total) {

					if(!idSpectrumMap.isEmpty()) {
						
						try {
							uploadNewMetlinSpectra();
						} catch (Exception e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
					}
					try {
						createMissingCompoundsReport();
						return;
					} catch (Exception e1) {
						setStatus(TaskStatus.ERROR);
						e1.printStackTrace();
					}
				}
			}
			if (e.getSource().getClass().equals(IDTCefMSMSPrescanOrImportTask.class)) {

				IDTCefMSMSPrescanOrImportTask task = (IDTCefMSMSPrescanOrImportTask)e.getSource();
				extractPrescanData(task);
				processed++;
				if(processed == total) {

					if(!idSpectrumMap.isEmpty()) {
						
						try {
							uploadNewMetlinSpectra();
						} catch (Exception e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
					}
					try {
						createMissingCompoundsReport();
						return;
					} catch (Exception e1) {
						setStatus(TaskStatus.ERROR);
						e1.printStackTrace();
					}
				}
			}			
		}
	}

	private void extractPrescanData(IDTCefMSMSPrescanOrImportTask task) {

		missingIdentities.addAll(task.getMissingIdentities());

		for (Entry<CompoundIdentity, Collection<TandemMassSpectrum>> entry : task.getIdSpectrumMap().entrySet()) {

			CompoundIdentity cid = entry.getKey();
			if(!idSpectrumMap.containsKey(cid))
				idSpectrumMap.put(cid, new HashSet<TandemMassSpectrum>());

			Collection<TandemMassSpectrum> msmsList = idSpectrumMap.get(cid);
			for(TandemMassSpectrum msms : entry.getValue()) {

				TandemMassSpectrum existingMsms = msmsList.stream().
						filter(s -> s.getPolarity().equals(msms.getPolarity())).
						filter(s -> s.getCidLevel() == msms.getCidLevel()).findFirst().orElse(null);

				if(existingMsms == null)
					msmsList.add(msms);
			}
		}
		if(!task.getImportLog().isEmpty()) {
			prescanLog.add("Data import log for " + task.getInputCefFile().getName());
			prescanLog.addAll(task.getImportLog());
			prescanLog.add("***********\n");
		}
	}

	private void createMissingCompoundsReport() {

		if(missingIdentities.isEmpty() && prescanLog.isEmpty()) {
			setStatus(TaskStatus.FINISHED);
			return;
		}
		if(missingCompoundsListFile != null) {
			try {
				writePeportToFile();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
			writeReportToLog();

		setStatus(TaskStatus.FINISHED);
	}
	
	private void writeReportToLog() {
		
		List<CompoundDatabaseEnum> dbList = missingIdentities.stream().
				flatMap(id -> id.getDbIdMap().keySet().stream()).
				distinct().sorted().collect(Collectors.toList());

		ArrayList<String>headerChunks = new ArrayList<String>();
		headerChunks.add("Name");
		headerChunks.add("Formula");
		headerChunks.add("Mass");
		dbList.stream().forEach(d -> headerChunks.add(d.name()));
		prescanLog.add(StringUtils.join(headerChunks, "\t"));
		for(CompoundIdentity id : missingIdentities) {

			headerChunks.clear();
			headerChunks.add(id.getName());
			headerChunks.add(id.getFormula());
			IMolecularFormula mf = null;
			try {
				mf = MolecularFormulaManipulator.getMolecularFormula(id.getFormula(), builder);
			} catch (Exception e) {
				System.out.println(id.getFormula());
				e.printStackTrace();
			}
			try {
				mf = MolecularFormulaManipulator.getMolecularFormula(id.getFormula(), builder);
			} catch (Exception e) {
				System.out.println(id.getFormula());
				e.printStackTrace();
			}
			double mass = 0.0d;
			if(mf != null) 
				mass = MolecularFormulaManipulator.getMajorIsotopeMass(mf);
			
			headerChunks.add(MRC2ToolBoxConfiguration.getMzFormat().format(mass));
			for(CompoundDatabaseEnum db : dbList) {
				String cid = id.getDbId(db);
				if(cid == null)
					cid = "";

				headerChunks.add(cid);
			}
			prescanLog.add(StringUtils.join(headerChunks, "\t"));
		}
	}
	
	private void writePeportToFile() throws Exception {
		
		final Writer writer = new BufferedWriter(new FileWriter(missingCompoundsListFile));
		writer.append("The following compounds could not be matched "
				+ "to the local compound database:\n*************************\n");
		List<CompoundDatabaseEnum> dbList = missingIdentities.stream().
				flatMap(id -> id.getDbIdMap().keySet().stream()).
				distinct().sorted().collect(Collectors.toList());

		ArrayList<String>headerChunks = new ArrayList<String>();
		headerChunks.add("Name");
		headerChunks.add("Formula");
		headerChunks.add("Mass");
		dbList.stream().forEach(d -> headerChunks.add(d.name()));
		writer.append(StringUtils.join(headerChunks, "\t") + "\n");

		for(CompoundIdentity id : missingIdentities) {

			headerChunks.clear();
			headerChunks.add(id.getName());
			headerChunks.add(id.getFormula());
			IMolecularFormula mf = null;
			try {
				mf = MolecularFormulaManipulator.getMolecularFormula(id.getFormula(), builder);
			} catch (Exception e) {
				System.out.println(id.getFormula());
				e.printStackTrace();
			}
			try {
				mf = MolecularFormulaManipulator.getMolecularFormula(id.getFormula(), builder);
			} catch (Exception e) {
				System.out.println(id.getFormula());
				e.printStackTrace();
			}
//			double mass =
//					MolecularFormulaManipulator.getMass(mf, MolecularFormulaManipulator.MonoIsotopic);
			double mass = 0.0d;
			if(mf != null) 
				mass = MolecularFormulaManipulator.getMajorIsotopeMass(mf);
			
			headerChunks.add(MRC2ToolBoxConfiguration.getMzFormat().format(mass));
			for(CompoundDatabaseEnum db : dbList) {
				String cid = id.getDbId(db);
				if(cid == null)
					cid = "";

				headerChunks.add(cid);
			}
			writer.append(StringUtils.join(headerChunks, "\t") + "\n");
		}
		writer.flush();
		writer.close();
	}

	private void uploadNewMetlinSpectra() throws Exception {

		taskDescription = "Uploading missing METLIN MSMS spectra";
		total = idSpectrumMap.size();
		processed = 0;
		Connection conn = ConnectionManager.getConnection();
		prescanLog.add("Uploading missing METLIN MSMS spectra:");
		
		for (Entry<CompoundIdentity, Collection<TandemMassSpectrum>> entry : idSpectrumMap.entrySet()) {

			CompoundIdentity cid = entry.getKey();
			System.out.println(cid.getName());
			prescanLog.add(cid.getName());
			String libraryId = cid.getDbId(CompoundDatabaseEnum.METLIN).replace("METLIN:", "");
			for(TandemMassSpectrum msms : entry.getValue()) {

				msms.setSpectrumSource(SpectrumSource.EXPERIMENTAL);
				String newLibId = MSMSLibraryUtils.insertNewReferenceMsMsLibraryFeature(
						msms,
						entry.getKey().getPrimaryDatabaseId(),
						"METLIN",
						libraryId,
						conn);
				String message = msms.getPolarity().name() +
						"\tCID " +  Double.toString(msms.getCidLevel()) +
						"V" + "\t" + newLibId;
				prescanLog.add(message);
				System.out.println(message);
			}
			processed++;
		}
	}

	private void extractData(MSMSSearchResultsPrescanTask task) {

		missingIdentities.addAll(task.getMissingIdentities());

		for (Entry<CompoundIdentity, Collection<TandemMassSpectrum>> entry : task.getIdSpectrumMap().entrySet()) {

			CompoundIdentity cid = entry.getKey();
			if(!idSpectrumMap.containsKey(cid))
				idSpectrumMap.put(cid, new HashSet<TandemMassSpectrum>());

			Collection<TandemMassSpectrum> msmsList = idSpectrumMap.get(cid);
			for(TandemMassSpectrum msms : entry.getValue()) {

				TandemMassSpectrum existingMsms = msmsList.stream().
						filter(s -> s.getPolarity().equals(msms.getPolarity())).
						filter(s -> s.getCidLevel() == msms.getCidLevel()).findFirst().orElse(null);

				if(existingMsms == null)
					msmsList.add(msms);
			}
		}
	}

	@Override
	public Task cloneTask() {
		return new  MSMSSearchResultsBatchPrescanTask(
				inputCefFiles, missingCompoundsListFile);
	}

	/**
	 * @return the missingCompoundsListFile
	 */
	public File getMissingCompoundsListFile() {
		return missingCompoundsListFile;
	}
	
	public Collection<String> getPrescanLog() {
		return prescanLog;
	}

	public Collection<CompoundIdentity> getMissingIdentities() {
		return missingIdentities;
	}
	
}
