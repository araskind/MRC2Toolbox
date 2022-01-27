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

package edu.umich.med.mrc2.datoolbox.gui.labnote;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.Document;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.QcEventType;
import edu.umich.med.mrc2.datoolbox.data.lims.AnalysisQcEventAnnotation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.database.lims.QCAnnotationUtils;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.IdTrackerLoginDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.rtf.DockableRTFEditor;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.lims.LimsDataPullTask;

public class LabNoteBookPanel extends DockableMRC2ToolboxPanel {

	private static final Icon componentIcon = GuiUtils.getIcon("labNoteBook", 16);
	private static final Icon instrumentIcon = GuiUtils.getIcon("msInstrument", 24);
	private static final Icon newAnnotationIcon = GuiUtils.getIcon("newFeatureSubset", 24);
	private static final Icon editAnnotationIcon = GuiUtils.getIcon("editCollection", 24);
	private static final Icon deleteAnnotationIcon = GuiUtils.getIcon("deleteCollection", 24);
	private static final Icon searchAnnotationIcon = GuiUtils.getIcon("searchDatabase", 24);
	private static final Icon refreshDataIcon = GuiUtils.getIcon("refreshDbData", 24);

	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "LabNoteBookPanel.layout");

	private LabNotePanelToolbar toolbar;
	private IdTrackerLoginDialog idtLogin;
	private InstrumentSelectorDialog instrumentSelectorDialog;
	private LIMSInstrument activeInstrument;
	private LabNoteEditor labNoteEditor;
	private IndeterminateProgressDialog idp;
	private DockableLabNotesTable labNotesTable;
	private DockableLabNotePlainTextViewer noteViewer;
	private DockableLabNoteSearchForm labNoteSearchForm;
	private DockableRTFEditor rtfEditor;

	public LabNoteBookPanel() {

		super("LabNoteBookPanel", PanelList.LAB_NOTEBOOK.getName(), componentIcon);
		setLayout(new BorderLayout(0, 0));

		toolbar = new LabNotePanelToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		activeInstrument = null;
		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);
		labNotesTable = new DockableLabNotesTable(this);
		labNotesTable.getLabNotesTable().addTablePopupMenu(new AnnotationTablePopupMenu(this));
		noteViewer = new DockableLabNotePlainTextViewer();
		labNoteSearchForm = new DockableLabNoteSearchForm(this);
		rtfEditor = new DockableRTFEditor(
				"LabNoteBookPanelDockableRTFEditor", "Formatted annotation", true);

		grid.add(0, 0, 100, 100, labNoteSearchForm, labNotesTable, noteViewer, rtfEditor);
		control.getContentArea().deploy(grid);
		getContentPane().add(control.getContentArea(), BorderLayout.CENTER);
		initActions();
		loadLayout(layoutConfigFile);
	}

	@Override
	protected void initActions() {
		
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CREATE_NEW_LAB_NOTE_COMMAND.getName(),
				MainActionCommands.CREATE_NEW_LAB_NOTE_COMMAND.getName(), 
				newAnnotationIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EDIT_LAB_NOTE_COMMAND.getName(),
				MainActionCommands.EDIT_LAB_NOTE_COMMAND.getName(), 
				editAnnotationIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_LAB_NOTE_COMMAND.getName(),
				MainActionCommands.DELETE_LAB_NOTE_COMMAND.getName(), 
				deleteAnnotationIcon, this));
		
		menuActions.addSeparator();		
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName(),
				MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName(), 
				refreshDataIcon, this));
		
		menuActions.addSeparator();		
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_MS_INSTRUMENT_SELECTOR_COMMAND.getName(),
				MainActionCommands.SHOW_MS_INSTRUMENT_SELECTOR_COMMAND.getName(), 
				instrumentIcon, this));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg("You are not logged in ID tracker!", this.getContentPane());
			return;
		}

		String command = e.getActionCommand();

		if (command.equals(MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName()))
			refreshLimsData();

		if (command.equals(MainActionCommands.SHOW_MS_INSTRUMENT_SELECTOR_COMMAND.getName()))
			showInstrumentSelector();

		if (command.equals(MainActionCommands.SET_MS_INSTRUMENT_COMMAND.getName()))
			selectMsInstrument();

		if (command.equals(MainActionCommands.SAVE_LAB_NOTE_COMMAND.getName()))
			saveAnnotation();

		if (command.equals(MainActionCommands.CREATE_NEW_LAB_NOTE_COMMAND.getName()))
			createAnnotation();

		if (command.equals(MainActionCommands.EDIT_LAB_NOTE_COMMAND.getName()))
			editSelectedAnnotation();

		if (command.equals(MainActionCommands.DELETE_LAB_NOTE_COMMAND.getName()))
			deleteSelectedAnnotation();

		if (command.equals(MainActionCommands.SEARCH_DATABASE_COMMAND.getName()))
			searchAnnotationsDatabase();
	}

	private void refreshLimsData() {

		LimsDataPullTask lpt = new LimsDataPullTask();
		lpt.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(lpt);
	}

	private void searchAnnotationsDatabase() {

		QcEventType annotationCategory = labNoteSearchForm.getAnnotationCategory();
		LIMSInstrument instrument = labNoteSearchForm.getInstrument();
		LIMSExperiment experiment = labNoteSearchForm.getExperiment();
		Assay assay = labNoteSearchForm.getAssay();
		ExperimentalSample sample = labNoteSearchForm.getSample();
		Date startDate = labNoteSearchForm.getStartDate();
		Date endDate = labNoteSearchForm.getEndDate();
		LIMSUser author = labNoteSearchForm.getNoteAuthor();

		AnalysisQcEventAnnotationSearchTask task = new AnalysisQcEventAnnotationSearchTask(
				 annotationCategory,
				 instrument,
				 experiment,
				 assay,
				 sample,
				 startDate,
				 endDate,
				 author);

		idp = new IndeterminateProgressDialog("Looking up annotations ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	class AnalysisQcEventAnnotationSearchTask extends LongUpdateTask {

		private QcEventType annotationCategory;
		private LIMSInstrument instrument;
		private LIMSExperiment experiment;
		private Assay assay;
		private ExperimentalSample sample;
		private Date startDate;
		private Date endDate;
		private LIMSUser author;

		public AnalysisQcEventAnnotationSearchTask(
				QcEventType annotationCategory,
				LIMSInstrument instrument,
				LIMSExperiment experiment,
				Assay assay,
				ExperimentalSample sample,
				Date startDate,
				Date endDate,
				LIMSUser author) {
			super();
			this.annotationCategory = annotationCategory;
			this.instrument = instrument;
			this.experiment = experiment;
			this.assay = assay;
			this.sample = sample;
			this.startDate = startDate;
			this.endDate = endDate;
			this.author = author;
		}

		@Override
		public Void doInBackground() {

			//	Find annotations
			Collection<AnalysisQcEventAnnotation> annotations =
					new ArrayList<AnalysisQcEventAnnotation>();
			try {
				annotations = QCAnnotationUtils.findAnnotations(
						 annotationCategory,
						 instrument,
						 experiment,
						 assay,
						 sample,
						 startDate,
						 endDate,
						 author);
			} catch (Exception e) {
				e.printStackTrace();
			}
			noteViewer.clearPanel();
			labNotesTable.setTableModelFromAnnotations(annotations);
			return null;
		}
	}

	private void saveAnnotation() {

		AnalysisQcEventAnnotation annotation = labNoteEditor.getAnnotation();

		String plainTextContents = labNoteEditor.getAnnotationText();
		Document formattedDocument = null;
		if(labNoteEditor.getDocument().getLength() > 0)
			formattedDocument = labNoteEditor.getDocument();

		QcEventType annotationCategory = labNoteEditor.getAnnotationCategory();
		LIMSExperiment experiment = labNoteEditor.getExperiment();
		Assay assay = labNoteEditor.getAssay();
		ExperimentalSample sample = labNoteEditor.getSample();

		ArrayList<String>errors = new ArrayList<String>();

		if(plainTextContents.isEmpty() && formattedDocument == null)
			errors.add("Note is empty.");

		if(annotationCategory == null)
			errors.add("Event category not specified.");

/*		if(experiment == null)
			errors.add("Experiment not specified.");

		if(assay == null)
			errors.add("Assay not specified.");*/

		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), labNoteEditor);
			return;
		}
		annotation.setLastModifiedBy(MRC2ToolBoxCore.getIdTrackerUser());
		annotation.setLastModified(new Date());
		annotation.setText(plainTextContents);
		annotation.setQcEventType(annotationCategory);
		annotation.setExperiment(experiment);
		annotation.setAssay(assay);
		annotation.setSample(sample);
		annotation.setInstrument(labNoteEditor.getInstrument());
		//	Add new annotation
		if(annotation.getId() == null) {

			try {
				QCAnnotationUtils.insertAnnotation(annotation, formattedDocument);
				labNotesTable.addAnnotations(Collections.singleton(annotation));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			//	Update existing annotation
			try {
				QCAnnotationUtils.updateAnnotation(
					annotation, formattedDocument, MRC2ToolBoxCore.getIdTrackerUser());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		labNoteEditor.clearPanel();
		labNoteEditor.setVisible(false);
		labNotesTable.selectAnnotation(annotation);
	}

	private void createAnnotation() {

		AnalysisQcEventAnnotation annotation =
			new AnalysisQcEventAnnotation(
				MRC2ToolBoxCore.getIdTrackerUser(), activeInstrument);
		
		if(labNoteEditor == null)
			labNoteEditor = new LabNoteEditor(this);
		
		labNoteEditor.loadAnnotation(annotation, null);
		labNoteEditor.clearPanel();
		labNoteEditor.setLocationRelativeTo(this.getContentPane());
		labNoteEditor.setVisible(true);
	}

	private void editSelectedAnnotation() {

		AnalysisQcEventAnnotation annotation = labNotesTable.getSelectedAnnotation();
		if(annotation == null)
			return;

		if(labNoteEditor == null)
			labNoteEditor = new LabNoteEditor(this);
		
		labNoteEditor.loadAnnotation(annotation, rtfEditor.getDocument());
		labNoteEditor.setLocationRelativeTo(this.getContentPane());
		labNoteEditor.setVisible(true);
	}

	private void deleteSelectedAnnotation() {

		AnalysisQcEventAnnotation annotation = labNotesTable.getSelectedAnnotation();
		if(annotation == null)
			return;

		String yesNoQuestion = "Do you want to delete selected note?";
		if(MessageDialog.showChoiceWithWarningMsg(yesNoQuestion, this.getContentPane()) == JOptionPane.YES_OPTION) {

			try {
				QCAnnotationUtils.deleteAnnotation(annotation);
			} catch (Exception e) {
				e.printStackTrace();
			}
			noteViewer.clearPanel();
			rtfEditor.clearPanel();
			labNotesTable.removeAnnotations(Collections.singleton(annotation));
		}
	}

	private void showInstrumentSelector() {

		instrumentSelectorDialog = new InstrumentSelectorDialog(this);
		instrumentSelectorDialog.setLocationRelativeTo(this.getContentPane());
		instrumentSelectorDialog.setVisible(true);
	}

	public void selectMsInstrument() {

		if(instrumentSelectorDialog.getSelectedInstrument() == null) {
			MessageDialog.showErrorMsg("Please select the instrument.", instrumentSelectorDialog);
			return;
		}
		activeInstrument = instrumentSelectorDialog.getSelectedInstrument();
		toolbar.setInstrument(activeInstrument);
		instrumentSelectorDialog.dispose();
	}

	@Override
	public void statusChanged(TaskEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void designStatusChanged(ExperimentDesignEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void designSetStatusChanged(ExperimentDesignSubsetEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void msFeatureStatusChanged(MsFeatureEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void featureSetStatusChanged(FeatureSetEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()) {

			idp = new IndeterminateProgressDialog(
				"Loading annotations ...", this.getContentPane(), new LIMSDocumentRetrievalTask());
			idp.setLocationRelativeTo(this.getContentPane());
			idp.setVisible(true);
		}
	}

	class LIMSDocumentRetrievalTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		public LIMSDocumentRetrievalTask() {

		}

		@Override
		public Void doInBackground() {

			AnalysisQcEventAnnotation annotation = labNotesTable.getSelectedAnnotation();
			if(annotation != null) {
				noteViewer.setAnnotationText(annotation.getText());
				Document anDoc = null;
				try {
					anDoc = QCAnnotationUtils.getAnnotationDocument(annotation.getId());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				rtfEditor.loadDocument(anDoc);
			}
			return null;
		}
	}

	@Override
	public synchronized void clearPanel() {

		labNotesTable.clearTable();
		noteViewer.clearPanel();;
		labNoteSearchForm.clearForm();
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	@Override
	public void reloadDesign() {
		// TODO Auto-generated method stub
		
	}
}
