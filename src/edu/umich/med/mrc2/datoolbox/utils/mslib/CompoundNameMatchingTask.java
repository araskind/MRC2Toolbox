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

package edu.umich.med.mrc2.datoolbox.utils.mslib;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeatureDbBundle;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.gui.library.MsLibraryPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class CompoundNameMatchingTask extends LongUpdateTask {

	private Collection<String> errors;
	private Collection<String> compoundErrors;
	private CompoundLibrary referenceLibrary;
	private Map<String,LibraryMsFeature>nameFeatureMap;
	private String[] compoundNames;
	private boolean writeLog;
	private File logDir;
	
	private static final String[]compoundAnnotationMasks 
		= new String[] {
				"\\{.+\\}",
				"\\[contaminant\\]",
				"\\(duplicate \\d\\)",
				"\\[fragment\\]",
				" \\(variant\\)"
			};
	
	private static final Pattern excludePattern = Pattern.compile("\\[[a-zA-Z]+\\]");
	
	//	TODO keep variant and duplicate as features

	public CompoundNameMatchingTask(
			String[] compoundNames, 
			CompoundLibrary referenceLibrary,
			Collection<String> errors, 
			Map<String, LibraryMsFeature> nameFeatureMap, 
			boolean writeLog,
			File logDir) {
		super();
		this.errors = errors;
		this.referenceLibrary = referenceLibrary;
		this.nameFeatureMap = nameFeatureMap;
		this.compoundNames = compoundNames;
		this.writeLog = writeLog;
		this.logDir = logDir;
	}

	@Override
	public Void doInBackground() {

		fetchLibraryCompounds();
		try {
			matchCompounds();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public void done() {
		
		((MsLibraryPanel)MRC2ToolBoxCore.getMainWindow().
				getPanel(PanelList.MS_LIBRARY)).updateLibraryMenuAndLabel();
		super.done();
	}
	
	private void fetchLibraryCompounds() {
		
		if(referenceLibrary != null) {
			
			if(MRC2ToolBoxCore.getActiveMsLibraries().contains(referenceLibrary)) {
				
				referenceLibrary = MRC2ToolBoxCore.getActiveMsLibraries().stream().
					filter(l -> l.getLibraryId().equals(referenceLibrary.getLibraryId())).
					findFirst().orElse(null);
			}
			else {
				try {
					Connection conn = ConnectionManager.getConnection();
					Collection<LibraryMsFeatureDbBundle>bundles =
							MSRTLibraryUtils.createFeatureBundlesForLibrary(referenceLibrary.getLibraryId(), conn);
					
					for(LibraryMsFeatureDbBundle fBundle : bundles) {

						if(fBundle.getConmpoundDatabaseAccession() != null) {

							LibraryMsFeature newTarget = fBundle.getFeature();
							MSRTLibraryUtils.attachIdentity(
									newTarget, fBundle.getConmpoundDatabaseAccession(), fBundle.isQcStandard(), conn);

							if(newTarget.getPrimaryIdentity() != null) {

								newTarget.getPrimaryIdentity().setConfidenceLevel(fBundle.getIdConfidence());
								MSRTLibraryUtils.attachMassSpectrum(newTarget, conn);
								MSRTLibraryUtils.attachTandemMassSpectrum(newTarget, conn);
								MSRTLibraryUtils.attachAnnotations(newTarget, conn);
								referenceLibrary.addFeature(newTarget);
							}
						}
					}
					ConnectionManager.releaseConnection(conn);
					MRC2ToolBoxCore.getActiveMsLibraries().add(referenceLibrary);	
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}			
	}
	
	private void matchCompounds() {

		compoundErrors = new TreeSet<>();
		for (String cpdName : compoundNames) {

			String cleanCompoundName = getCleanCompoundName(cpdName);
			if(cleanCompoundName == null)
				continue;
			
			LibraryMsFeature libFeature = referenceLibrary.getFeatureByNameIgnoreCase(cleanCompoundName);
			if (libFeature == null)
				compoundErrors.add(cleanCompoundName);
			else {
				nameFeatureMap.put(cpdName, libFeature);
			}				
		}
		if(!compoundErrors.isEmpty()) {
			
			errors.add("\nNo match found in reference library for the following compounds:\n");
			errors.addAll(compoundErrors);
			if(writeLog && logDir != null)
				writeAndOpenLogFile();
		}
	}
	
	private String getCleanCompoundName(String compoundName) {
		
		Matcher m = excludePattern.matcher(compoundName.trim());
		if(m.find())
			return null;
		
		String cleanCompoundName = compoundName;
		for(String annotationMask : compoundAnnotationMasks)			
			cleanCompoundName = cleanCompoundName.replaceAll(annotationMask, "");
		
		return cleanCompoundName.trim();
	}
	
	private void writeAndOpenLogFile() {
		
		Path logPath = Paths.get( logDir.getAbsolutePath(),
				 "Unmatched-compounds-" + referenceLibrary.getLibraryName() 
				 	+ "-" + FIOUtils.getTimestamp() + ".txt");
		try {
		    Files.write(logPath, 
		    		compoundErrors,
		            StandardCharsets.UTF_8,
		            StandardOpenOption.CREATE, 
		            StandardOpenOption.TRUNCATE_EXISTING);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		if (Desktop.isDesktopSupported()) {
		    try {
		        Desktop.getDesktop().open(logPath.toFile());
		    } catch (IOException ex) {
		        ex.printStackTrace();
		    }
		}
	}
}
