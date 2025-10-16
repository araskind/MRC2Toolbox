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

package edu.umich.med.mrc2.datoolbox.gui.annotation;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;

import org.apache.commons.io.FilenameUtils;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.AnnotatedObject;
import edu.umich.med.mrc2.datoolbox.data.enums.DocumentFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.database.idt.AnnotationUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.DocumentUtils;
import edu.umich.med.mrc2.datoolbox.gui.annotation.editors.DocumentAnnotationDialog;
import edu.umich.med.mrc2.datoolbox.gui.annotation.editors.ObjectAnnotationEditor;
import edu.umich.med.mrc2.datoolbox.gui.jcp.JChemPaintCA;
import edu.umich.med.mrc2.datoolbox.gui.jcp.StructuralAnnotationEditor;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MSFeatureBundleDataUpdater;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import rtf.AdvancedRTFEditorKit;

public class DockableObjectAnnotationPanel extends DefaultSingleCDockable 
	implements ActionListener, BackedByPreferences, ListSelectionListener {	

	private static final Icon componentIcon = GuiUtils.getIcon("annotations", 16);	
	private Preferences preferences;
	public static final String PREFS_NODE = DockableObjectAnnotationPanel.class.getName();
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private ObjectAnnotationToolbar toolBar;
	private JScrollPane scrollPane;
	private ObjectAnnotationTable objectAnnotationTable;
	private AnnotatedObject currentObject;
	private ObjectAnnotationEditor objectAnnotationEditor;
	private StructuralAnnotationEditor structuralAnnotationEditor;
	private DocumentAnnotationDialog documentAnnotationDialog;
//	private ImprovedFileChooser chooser;
	private File baseDirectory;
	private JChemPaintCA jcp;
	private	AnnotationMetadataPanel annotationMetadataPanel; 
	private MSFeatureBundleDataUpdater msFeatureBundleDataUpdater;

	public DockableObjectAnnotationPanel(String id, String title, int maxAnnotationPreviewLength) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));
		toolBar = new ObjectAnnotationToolbar(this);
		add(toolBar, BorderLayout.NORTH);
		scrollPane = new JScrollPane();
		objectAnnotationTable = new ObjectAnnotationTable(maxAnnotationPreviewLength);
		objectAnnotationTable.addTablePopupMenu(new ObjectAnnotationTablePopupMenu(this));
		objectAnnotationTable.addMouseListener(

				new MouseAdapter() {

					public void mouseClicked(MouseEvent e) {

						if (e.getClickCount() == 2) {

							ObjectAnnotation annotation = getSelectedAnnotation();
							if(annotation == null)
								return;
							
							if(annotation.getRtfDocument() != null || annotation.getChemModel() != null)
								editSelectedAnnotation();
							
							if(annotation.getLinkedDocumentId() != null)
								previewSelectedAnnotation();
						}
					}
				});
		objectAnnotationTable.getSelectionModel().addListSelectionListener(this);
		scrollPane.add(objectAnnotationTable);
		scrollPane.setViewportView(objectAnnotationTable);
		scrollPane.setPreferredSize(objectAnnotationTable.getPreferredScrollableViewportSize());
		add(scrollPane, BorderLayout.CENTER);
		
		annotationMetadataPanel = new AnnotationMetadataPanel();
		add(annotationMetadataPanel, BorderLayout.SOUTH);
		
		loadPreferences();
//		initChooser();
		currentObject = null;
	}

//	private void initChooser() {
//
//		chooser = new ImprovedFileChooser();
//		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
//		chooser.addActionListener(this);
//		chooser.setAcceptAllFileFilterUsed(true);
//		chooser.setMultiSelectionEnabled(false);
//		// chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//		chooser.setCurrentDirectory(baseDirectory);
//	}

	public synchronized void clearPanel() {
		objectAnnotationTable.clearTable();
		currentObject = null;
		annotationMetadataPanel.clearPanel();
	}

	public ObjectAnnotation getSelectedAnnotation() {

		if (objectAnnotationTable.getSelectedRow() == -1)
			return null;

		return (ObjectAnnotation) objectAnnotationTable
				.getValueAt(objectAnnotationTable.getSelectedRow(), 0);
	}

	public void loadFeatureData(AnnotatedObject feature) {

		currentObject = feature;
		objectAnnotationTable.setTableModelFromAnnotatedObject(feature);
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {

		ObjectAnnotation annotation = getSelectedAnnotation();
		if(annotation == null)
			return;
		
		annotationMetadataPanel.loadAnnotation(annotation);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if(currentObject == null)
			return;

		if(MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg("You are not logged in ID tracker!", this.getContentPane());
			return;
		}
		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.ADD_OBJECT_ANNOTATION_COMMAND.getName()))
			createNewAnnotation();

		if (command.equals(MainActionCommands.ATTACH_DOCUMENT_ANNOTATION_COMMAND.getName()))
			createNewDocumentAnnotation();
		
		if (command.equals(MainActionCommands.ADD_STRUCTURAL_ANNOTATION_COMMAND.getName()))
			createNewStructuralAnnotation();	

		if (command.equals(MainActionCommands.EDIT_OBJECT_ANNOTATION_COMMAND.getName()))
			editSelectedAnnotation();

		if (command.equals(MainActionCommands.SAVE_OBJECT_ANNOTATION_COMMAND.getName()))
			saveAnnotation();
		
		if (command.equals(MainActionCommands.SAVE_OBJECT_STRUCTURAL_ANNOTATION_COMMAND.getName()))
			saveStructuralAnnotation();

		if (command.equals(MainActionCommands.SAVE_OBJECT_DOCUMENT_ANNOTATION_COMMAND.getName()))
			saveDocumentAnnotation();

		if (command.equals(MainActionCommands.DELETE_OBJECT_ANNOTATION_COMMAND.getName()))
			deleteSelectedAnnotation();
		
		if (command.equals(MainActionCommands.PREVIEW_ANNOTATION_COMMAND.getName()))
			previewSelectedAnnotation();
		
		if (command.equals(MainActionCommands.DOWNLOAD_ANNOTATION_COMMAND.getName()))
			downloadSelectedAnnotation();
	}

	private void createNewAnnotation() {

		objectAnnotationEditor = new ObjectAnnotationEditor(this);
		ObjectAnnotation annotation = 
				new ObjectAnnotation(currentObject, MRC2ToolBoxCore.getIdTrackerUser());

		objectAnnotationEditor.loadAnnotation(annotation);			
		objectAnnotationEditor.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		objectAnnotationEditor.setVisible(true);
	}

	private void createNewDocumentAnnotation(){

		documentAnnotationDialog = new DocumentAnnotationDialog(this);
		ObjectAnnotation annotation = 
				new ObjectAnnotation(currentObject, MRC2ToolBoxCore.getIdTrackerUser());
		documentAnnotationDialog.loadAnnotation(annotation);
		documentAnnotationDialog.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		documentAnnotationDialog.setVisible(true);
	}
	
	private void createNewStructuralAnnotation() {

		structuralAnnotationEditor = new StructuralAnnotationEditor(this);		
		ObjectAnnotation annotation = 
				new ObjectAnnotation(currentObject, MRC2ToolBoxCore.getIdTrackerUser());
		structuralAnnotationEditor.loadAnnotation(annotation);
		structuralAnnotationEditor.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		structuralAnnotationEditor.setVisible(true);
	}
	
	private void previewSelectedAnnotation() {

		ObjectAnnotation annotation = getSelectedAnnotation();
		if(annotation == null)
			return;
		
		if(annotation.getChemModel() != null)
			return;
		
		//	MessageDialogue.showInfoMsg("Document preview", this.getContentPane());
		AnnotationPreviewDialog apvd = new AnnotationPreviewDialog(annotation);
		apvd.setLocationRelativeTo(this.getContentPane());
		apvd.setVisible(true);
	}	
	
	private void downloadSelectedAnnotation() {

		ObjectAnnotation annotation = getSelectedAnnotation();
		if(annotation == null)
			return;
			
		if(annotation.getRtfDocument() != null) {			
			saveRTFDocument(annotation);
			return;
		}
		if(annotation.getLinkedDocumentId() != null) {
			saveLinkedDocument(annotation);
			return;
		}
	}	
	
	private void saveRTFDocument(ObjectAnnotation annotation) {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("RTF files", "rtf", "RTF");
		fc.setTitle("Specify file name");
		fc.setMultiSelectionEnabled(false);
		String defaultFileName = "Annotation_" + annotation.getUniqueId() +
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".RTF";
		fc.setDefaultFileName(defaultFileName);		
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			AdvancedRTFEditorKit editor = new AdvancedRTFEditorKit();
			Document rtfDocument = annotation.getRtfDocument();	
			File output = FIOUtils.changeExtension(
					fc.getSelectedFile(), DocumentFormat.RTF.name());
			try {
				editor.write(output.getAbsolutePath(), rtfDocument);
			} catch (Exception ex) {
				ex.printStackTrace();
			}				
			baseDirectory = fc.getSelectedFile().getParentFile();
			savePreferences();
		}
		
//		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//		chooser.setDialogTitle("Specify file name");		
//		if(chooser.showSaveDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {
//			AdvancedRTFEditorKit editor = new AdvancedRTFEditorKit();
//			Document rtfDocument = annotation.getRtfDocument();	
//			File output = FIOUtils.changeExtension(
//					chooser.getSelectedFile(), DocumentFormat.RTF.name());
//			try {
//				editor.write(output.getAbsolutePath(), rtfDocument);
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}				
//			baseDirectory = chooser.getSelectedFile().getParentFile();
//			savePreferences();
//		}
	}
	
	private void saveLinkedDocument(ObjectAnnotation annotation) {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select destination folder");
		fc.setMultiSelectionEnabled(false);		
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			try {
				DocumentUtils.saveDocumentToFile(
						annotation.getLinkedDocumentId(), 
						fc.getSelectedFile());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			baseDirectory = fc.getSelectedFile();
			savePreferences();
		}
		
//		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//		chooser.setDialogTitle("Select destination folder");
//		if(chooser.showSaveDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {				
//			try {
//				DocumentUtils.saveDocumentToFile(
//						annotation.getLinkedDocumentId(), 
//						chooser.getSelectedFile());
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			baseDirectory = chooser.getSelectedFile();
//			savePreferences();
//		}
	}

	private void editSelectedAnnotation() {

		ObjectAnnotation annotation = getSelectedAnnotation();
		if(annotation == null)
			return;
		
		if(annotation.getRtfDocument() != null) {
			
			objectAnnotationEditor = new ObjectAnnotationEditor(this);
			objectAnnotationEditor.loadAnnotation(annotation);
			objectAnnotationEditor.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
			objectAnnotationEditor.setVisible(true);
		}
		if (annotation.getLinkedDocumentId() != null) {

			documentAnnotationDialog = new DocumentAnnotationDialog(this);
			documentAnnotationDialog.loadAnnotation(annotation);
			documentAnnotationDialog.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
			documentAnnotationDialog.setVisible(true);
		}
		if(annotation.getChemModel() != null) {
			
			structuralAnnotationEditor =  new StructuralAnnotationEditor(this);
			structuralAnnotationEditor.loadAnnotation(annotation);
			structuralAnnotationEditor.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
			structuralAnnotationEditor.setVisible(true);
		}
	}

	private void deleteSelectedAnnotation() {

		ObjectAnnotation annotation = getSelectedAnnotation();
		if (annotation != null) {

			int approve = MessageDialog.showChoiceWithWarningMsg(
					"Delete selected annotation?\n(NO UNDO!)", this.getContentPane());

			if (approve == JOptionPane.YES_OPTION) {

				currentObject.removeAnnotation(annotation);
				try {
					AnnotationUtils.deleteAnnotation(annotation);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				loadFeatureData(currentObject);
				
				if(msFeatureBundleDataUpdater != null)
					msFeatureBundleDataUpdater.updateSelectedFeatures();
			}
		}
	}

	private void saveAnnotation() {

		ObjectAnnotation annotation = objectAnnotationEditor.getAnnotation();
		if(annotation == null)
			return;
		
		//	Update existing annotation - unique ID is CHAR 12
		if(annotation.getUniqueId().length() == 12) {
			try {
				AnnotationUtils.updateAnnotation(annotation, null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {	//	Insert new annotation
			try {
				AnnotationUtils.insertNewAnnotation(annotation, null);
				currentObject.addAnnotation(annotation);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(msFeatureBundleDataUpdater != null)
				msFeatureBundleDataUpdater.updateSelectedFeatures();
		}
		loadFeatureData(currentObject);
		objectAnnotationEditor.dispose();
	}	

	private void saveStructuralAnnotation() {

		ObjectAnnotation annotation = structuralAnnotationEditor.getAnnotation();
		if(annotation == null)
			return;
		
		if(structuralAnnotationEditor.isJCPModelEmpty()) {
			MessageDialog.showWarningMsg("Please create the structure or reaction.", structuralAnnotationEditor);
			return;
		}
		String chemModelNotes = structuralAnnotationEditor.getAnnotationNotes();
		if(chemModelNotes.isEmpty()) {
			MessageDialog.showWarningMsg("Please add a note to the structure.", structuralAnnotationEditor);
			return;
		}
		annotation.setChemModelNotes(chemModelNotes);
		
		//	Update existing annotation - unique ID is CHAR 12
		if(annotation.getUniqueId().length() == 12) {
			try {
				AnnotationUtils.updateAnnotation(annotation, null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {	//	Insert new annotation
			try {
				AnnotationUtils.insertNewAnnotation(annotation, null);
				currentObject.addAnnotation(annotation);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(msFeatureBundleDataUpdater != null)
				msFeatureBundleDataUpdater.updateSelectedFeatures();
		}
		loadFeatureData(currentObject);
		structuralAnnotationEditor.dispose();
	}

	private void saveDocumentAnnotation() {

		ObjectAnnotation annotation = documentAnnotationDialog.getAnnotation();
		if(annotation == null)
			return;
		
		if(documentAnnotationDialog.getDocumentTitle().isEmpty()) {
			MessageDialog.showErrorMsg(
					"Please specify document title.", documentAnnotationDialog);
			return;
		}
		annotation.setLinkedDocumentName(documentAnnotationDialog.getDocumentTitle());
		
		//	Update existing annotation - unique ID is CHAR 12
		if(annotation.getUniqueId().length() == 12) {
			try {
				AnnotationUtils.updateAnnotation(annotation, null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {	//	Insert new annotation
			if(documentAnnotationDialog.getDocumentSourceFile() == null) {
				MessageDialog.showErrorMsg("Please specify document source file.", documentAnnotationDialog);
				return;
			}
			try {
				String extension = FilenameUtils.getExtension(documentAnnotationDialog.getDocumentSourceFile().getName());
				DocumentFormat format = DocumentFormat.getFormatByFileExtension(extension);
				annotation.setLinkedDocumentFormat(format);
				annotation.setLinkedDocumentFile(documentAnnotationDialog.getDocumentSourceFile());
				AnnotationUtils.insertNewAnnotation(annotation);
				currentObject.addAnnotation(annotation);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(msFeatureBundleDataUpdater != null)
				msFeatureBundleDataUpdater.updateSelectedFeatures();
		}
		loadFeatureData(currentObject);
		documentAnnotationDialog.dispose();
	}

	public AnnotatedObject getCurrentAnnotatedObject() {
		return objectAnnotationTable.getCurrentFeature();
	}

	/**
	 * @return the currentObject
	 */
	public AnnotatedObject getCurrentObject() {
		return currentObject;
	}

	/**
	 * @param currentObject the currentObject to set
	 */
	public void setCurrentObject(AnnotatedObject currentObject) {
		this.currentObject = currentObject;
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		loadPreferences();
	}

	@Override
	public void loadPreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		baseDirectory =  new File(preferences.get(BASE_DIRECTORY, MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}

	/**
	 * @param msFeatureBundleDataUpdater the msFeatureBundleDataUpdater to set
	 */
	public void setMsFeatureBundleDataUpdater(MSFeatureBundleDataUpdater msFeatureBundleDataUpdater) {
		this.msFeatureBundleDataUpdater = msFeatureBundleDataUpdater;
	}

}
