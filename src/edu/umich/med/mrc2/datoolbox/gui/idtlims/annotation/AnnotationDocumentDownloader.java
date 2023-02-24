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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.annotation;

import java.awt.Component;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.SwingUtilities;

import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.database.idt.DocumentUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class AnnotationDocumentDownloader {

	private static Preferences preferences;
	public static final String PREFS_NODE = "edu.umich.med.mrc2.datoolbox.AnnotationDocumentDownloader";
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private static File baseDirectory;
	
	public static void downloadLinkedDocumentFile(
			ObjectAnnotation annotation, Component parent) {

		if(annotation.getLinkedDocumentId() == null)
			return;
		
		loadPreferences();
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.setTitle("Save document \"" + 
				annotation.getLinkedDocumentName() + "\" to local drive");
		fc.setMultiSelectionEnabled(false);
		fc.setAllowOverwrite(false);
		fc.setDefaultFileName(annotation.getLinkedDocumentName() + 
				"." + annotation.getLinkedDocumentFormat().name());
		//	fc.setSaveButtonText("Select destination folder");

		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(parent))) {
			
			File destination = fc.getSelectedFile().getParentFile();
			baseDirectory = destination;
			try {
				DocumentUtils.saveDocumentToFile(
						annotation.getLinkedDocumentId(), destination);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			savePreferences();
		}
	}

	public static void loadPreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		baseDirectory =  
				new File(preferences.get(BASE_DIRECTORY, 
						MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	public static void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

}
