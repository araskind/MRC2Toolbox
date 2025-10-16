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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProtocol;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSWorklistItem;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.design.WizardExperimentDesignPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.methods.WizardMethodsPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.ms.WizardMsDataVerifierPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.wizard.worklist.WizardWorklistPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.idt.IDTCefMSMSPrescanOrImportTask;

public class IDTrackerDataLoadWizard extends JDialog implements ActionListener, TaskListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3308258276718203363L;
	private static final Icon newCdpIdExperimentIcon = GuiUtils.getIcon("newIdExperiment", 32);
	private IDTrackerLimsManagerPanel parentPanel;
	private IDTrackerDataLoadWizardToolbar toolbar;
	private JPanel stagePanel;
	private JButton saveButton;
	private LinkedHashMap<IDTrackerDataUploadStage, IDTrackerDataLoadWizardPanel> panels;
	private LinkedHashMap<IDTrackerDataUploadStage, Boolean> stageCompleted;
	private IDTrackerDataUploadStage activeStage;
	private GridBagConstraints gbc_panel;
	private IDTrackerDataLoadProgressToolbar progressToolbar;
	private LIMSExperiment newExperiment;
	private IndeterminateProgressDialog idp;
	private int processedFiles, fileNumber;

	public IDTrackerDataLoadWizard(IDTrackerLimsManagerPanel parentPanel) {
		
		super();
		setIconImage(((ImageIcon) newCdpIdExperimentIcon).getImage());
		setPreferredSize(new Dimension(1000, 800));
		setSize(new Dimension(1000, 800));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.parentPanel = parentPanel;
		
		toolbar = new IDTrackerDataLoadWizardToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		stagePanel = new JPanel();
		stagePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(stagePanel, BorderLayout.CENTER);
		
		initWizardPanels();
				
		GridBagLayout gbl_stagePanel = new GridBagLayout();
		gbl_stagePanel.columnWidths = new int[]{0, 0};
		gbl_stagePanel.rowHeights = new int[]{0, 0};
		gbl_stagePanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_stagePanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		stagePanel.setLayout(gbl_stagePanel);

		gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		stagePanel.add(panels.get(IDTrackerDataUploadStage.CREATE_EXPERIMENT), gbc_panel);
		
		setTitle(IDTrackerDataUploadStage.CREATE_EXPERIMENT.getName());
		toolbar.highlightStageButton(IDTrackerDataUploadStage.CREATE_EXPERIMENT);
		activeStage = IDTrackerDataUploadStage.CREATE_EXPERIMENT;
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{737, 76, 65, 91, 0};
		gbl_panel_1.rowHeights = new int[]{23, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		progressToolbar = new IDTrackerDataLoadProgressToolbar(this);
		progressToolbar.setFloatable(false);
		GridBagConstraints gbc_toolBar = new GridBagConstraints();
		gbc_toolBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_toolBar.insets = new Insets(0, 0, 0, 5);
		gbc_toolBar.gridx = 0;
		gbc_toolBar.gridy = 0;
		panel_1.add(progressToolbar, gbc_toolBar);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.NORTHWEST;
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		panel_1.add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(al);

		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		saveButton = new JButton(MainActionCommands.UPLOAD_DATA_TO_IDTRACKER_COMMAND.getName());
		saveButton.setActionCommand(MainActionCommands.UPLOAD_DATA_TO_IDTRACKER_COMMAND.getName());
		saveButton.addActionListener(this);
		GridBagConstraints gbc_saveButton = new GridBagConstraints();
		gbc_saveButton.anchor = GridBagConstraints.NORTHWEST;
		gbc_saveButton.gridx = 3;
		gbc_saveButton.gridy = 0;
		panel_1.add(saveButton, gbc_saveButton);
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.setDefaultButton(saveButton);

		pack();
	}
	
	private void initWizardPanels() {
		
		panels = new LinkedHashMap<IDTrackerDataUploadStage, IDTrackerDataLoadWizardPanel>();
		stageCompleted = new LinkedHashMap<IDTrackerDataUploadStage, Boolean>();

		for(IDTrackerDataUploadStage panelType : IDTrackerDataUploadStage.values()) {
			
			try {
				panels.put(panelType, (IDTrackerDataLoadWizardPanel) 
						panelType.getPanelClass().
						getDeclaredConstructor(IDTrackerDataLoadWizard.class).newInstance(this));			
				stageCompleted.put(panelType, false);
			}
			catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		for(IDTrackerDataUploadStage stage : IDTrackerDataUploadStage.values()) {
			if(command.equals(stage.getName())) 
				showStagePanel(stage);
		}
		if(command.equals(MainActionCommands.COMPLETE_EXPERIMENT_DEFINITION_COMMAND.getName()))
			completeExperimentDefinitionStage();
		
		if(command.equals(MainActionCommands.COMPLETE_SAMPLE_LIST_DEFINITION_COMMAND.getName()))
			completeSampleListDefinitionStage();
		
		if(command.equals(MainActionCommands.COMPLETE_SAMPLE_PREP_DEFINITION_COMMAND.getName()))
			completeSamplePrepDefinitionStage();
		
		if(command.equals(MainActionCommands.COMPLETE_ANALYSIS_METHODS_DEFINITION_COMMAND.getName()))
			completeAnalysisMethodsDefinitionStage();
		
		if(command.equals(MainActionCommands.COMPLETE_ANALYSIS_WORKLIST_VERIFICATION_COMMAND.getName()))
			completeWorklistVerificationStage();
		
		if(command.equals(MainActionCommands.COMPLETE_DATA_VERIFICATION_COMMAND.getName()))
			completeDataVerificationStage();
		
		if(command.equals(MainActionCommands.FINALIZE_DATA_UPLOAD_COMMAND.getName()))
			finalizeDataUpload();
		
		if(command.equals(MainActionCommands.UPLOAD_DATA_TO_IDTRACKER_COMMAND.getName()))
			uploadDataToIDdTracker();
	}
	
	private void showStagePanel(IDTrackerDataUploadStage stage) {
		
		stagePanel.remove(panels.get(activeStage));
		revalidate();
		repaint();
		activeStage = stage;
		stagePanel.add(panels.get(stage), gbc_panel);
		setTitle(stage.getName());
		toolbar.highlightStageButton(stage);
	}

	private void completeExperimentDefinitionStage() {

		WizardExperimentDefinitionPanel experimentPanel = 
				(WizardExperimentDefinitionPanel)panels.get(IDTrackerDataUploadStage.CREATE_EXPERIMENT);
		Collection<String>errors = experimentPanel.validateExperimentDefinition();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}
		if(newExperiment == null) {
			
			newExperiment = new LIMSExperiment(
					experimentPanel.getExperimentName(),
					experimentPanel.getExperimentDescription(), 
					experimentPanel.getExperimentNotes(),
					experimentPanel.getExperimentProject());
			newExperiment.setDesign(new ExperimentDesign());
			newExperiment.setCreator(MRC2ToolBoxCore.getIdTrackerUser());
		}
		else {
			newExperiment.setName(experimentPanel.getExperimentName());
			newExperiment.setDescription(experimentPanel.getExperimentDescription());
		}
		experimentPanel.loadExperiment(newExperiment);
		
		WizardExperimentDesignPanel designPanel = 
				(WizardExperimentDesignPanel)panels.get(IDTrackerDataUploadStage.ADD_SAMPLES);
		designPanel.loadExperiment(newExperiment);

		stageCompleted.put(IDTrackerDataUploadStage.CREATE_EXPERIMENT, true);
		progressToolbar.markStageCompletedStatus(IDTrackerDataUploadStage.CREATE_EXPERIMENT, true);
		showStagePanel(IDTrackerDataUploadStage.ADD_SAMPLES);
	}

	private void completeSampleListDefinitionStage() {

		if(!stageCompleted.get(IDTrackerDataUploadStage.CREATE_EXPERIMENT))
			return;
		
		WizardExperimentDesignPanel designPanel = 
				(WizardExperimentDesignPanel)panels.get(IDTrackerDataUploadStage.ADD_SAMPLES);
		Collection<String>errors = designPanel.validateExperimentDesign();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}
		WizardSamplePrepPanel prepPanel = 
				(WizardSamplePrepPanel)panels.get(IDTrackerDataUploadStage.ADD_SAMPLE_PREPARATION_DATA);

		prepPanel.loadPrepDataForExperiment(getSamplePrep(), newExperiment);
		stageCompleted.put(IDTrackerDataUploadStage.ADD_SAMPLES, true);
		progressToolbar.markStageCompletedStatus(IDTrackerDataUploadStage.ADD_SAMPLES, true);
		showStagePanel(IDTrackerDataUploadStage.ADD_SAMPLE_PREPARATION_DATA);		
	}

	private void completeSamplePrepDefinitionStage() {
		
		if(!stageCompleted.get(IDTrackerDataUploadStage.CREATE_EXPERIMENT) 
				|| !stageCompleted.get(IDTrackerDataUploadStage.ADD_SAMPLES) )
			return;

		WizardSamplePrepPanel prepPanel = 
				(WizardSamplePrepPanel)panels.get(IDTrackerDataUploadStage.ADD_SAMPLE_PREPARATION_DATA);
		Collection<String>errors = prepPanel.validateSamplePrepDefinition();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}
		LIMSSamplePreparation samplePrep = getSamplePrep();
		if(samplePrep == null) {
			
			samplePrep = new LIMSSamplePreparation(
					null, 
					prepPanel.getPrepName(), 
					prepPanel.getPrepDate(), 
					prepPanel.getPrepUser());
			samplePrep.setId(StringUtils.rightPad(DataPrefix.SAMPLE_PREPARATION.getName(), 7, 'X'));		
			
			for(LIMSProtocol sop : prepPanel.getPrepSops())
				samplePrep.addProtocol(sop);
			
			for(ObjectAnnotation annotation : prepPanel.getPrepAnnotations())
				samplePrep.addAnnotation(annotation);
		}
		else {
			samplePrep.setName(prepPanel.getPrepName());
			samplePrep.setPrepDate(prepPanel.getPrepDate());
			samplePrep.setCreator(prepPanel.getPrepUser());
			
			samplePrep.getProtocols().clear();
			for(LIMSProtocol sop : prepPanel.getPrepSops())
				samplePrep.addProtocol(sop);
			
			samplePrep.getAnnotations().clear();
			for(ObjectAnnotation annotation : prepPanel.getPrepAnnotations())
				samplePrep.addAnnotation(annotation);
		}
		samplePrep.getPrepItemMap().clear();
		int count = 1;
		for(ExperimentalSample sample : newExperiment.getExperimentDesign().getSamples()) {
			samplePrep.addPrepItem(sample.getId(), DataPrefix.PREPARED_SAMPLE.getName() 
					+ StringUtils.leftPad(Integer.toString(count), 7, '0'));
			count++;
		}
		prepPanel.loadPrepDataForExperiment(samplePrep, newExperiment);							
		WizardWorklistPanel worklistPanel = 
				(WizardWorklistPanel)panels.get(IDTrackerDataUploadStage.ADD_WORKLISTS);
		worklistPanel.setExperiment(newExperiment);
		worklistPanel.setSamplePrep(samplePrep);
		
		stageCompleted.put(IDTrackerDataUploadStage.ADD_SAMPLE_PREPARATION_DATA, true);
		progressToolbar.markStageCompletedStatus(IDTrackerDataUploadStage.ADD_SAMPLE_PREPARATION_DATA, true);
		showStagePanel(IDTrackerDataUploadStage.ADD_ACQ_DA_METHODS);		
	}

	private void completeAnalysisMethodsDefinitionStage() {

		Collection<String>errors = ((WizardMethodsPanel)panels.get(
				IDTrackerDataUploadStage.ADD_ACQ_DA_METHODS)).validateMethodsData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}
		//	TODO upload new methods to the database
				
		stageCompleted.put(IDTrackerDataUploadStage.ADD_ACQ_DA_METHODS, true);
		progressToolbar.markStageCompletedStatus(IDTrackerDataUploadStage.ADD_ACQ_DA_METHODS, true);
		showStagePanel(IDTrackerDataUploadStage.ADD_WORKLISTS);
	}

	private void completeWorklistVerificationStage() {

		Collection<String>errors = ((WizardWorklistPanel)panels.get(
				IDTrackerDataUploadStage.ADD_WORKLISTS)).validateWorklistData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}
		WizardMsDataVerifierPanel msVerifierPanel = 
				(WizardMsDataVerifierPanel)panels.get(IDTrackerDataUploadStage.VERIFY_DATA_FOR_UPLOAD);
		msVerifierPanel.setExperiment(newExperiment);	
		
		stageCompleted.put(IDTrackerDataUploadStage.ADD_WORKLISTS, true);
		progressToolbar.markStageCompletedStatus(IDTrackerDataUploadStage.ADD_WORKLISTS, true);
		showStagePanel(IDTrackerDataUploadStage.VERIFY_DATA_FOR_UPLOAD);		
	}

	private void completeDataVerificationStage() {

		Collection<String>errors = ((WizardMsDataVerifierPanel)panels.get(
				IDTrackerDataUploadStage.VERIFY_DATA_FOR_UPLOAD)).verifyDataForUpload();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}
		stageCompleted.put(IDTrackerDataUploadStage.VERIFY_DATA_FOR_UPLOAD, true);
		progressToolbar.markStageCompletedStatus(IDTrackerDataUploadStage.VERIFY_DATA_FOR_UPLOAD, true);
		//	showStagePanel(IDTrackerDataUploadStage.FINALIZE_DATA_UPLOAD);
	}

	private void finalizeDataUpload() {

//		Collection<String>errors = ((WizardFinalizePanel)panels.get(
//				IDTrackerDataUploadStage.FINALIZE_DATA_UPLOAD)).validateDataSeyForUpload();
//		if(!errors.isEmpty()) {
//			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
//			return;
//		}
//		//	TODO create new experiment object
//		
//		
//		stageCompleted.put(IDTrackerDataUploadStage.FINALIZE_DATA_UPLOAD, true);
//		progressToolbar.markStageCompletedStatus(IDTrackerDataUploadStage.FINALIZE_DATA_UPLOAD, true);
	}
	
	public LIMSExperiment getExperiment() {
		if(!stageCompleted.get(IDTrackerDataUploadStage.CREATE_EXPERIMENT) ||
				!stageCompleted.get(IDTrackerDataUploadStage.ADD_SAMPLES))
			return null;
		
		return newExperiment;
	}
	
	public LIMSSamplePreparation getSamplePrep() {
		
		if(!stageCompleted.get(IDTrackerDataUploadStage.CREATE_EXPERIMENT) 
				||	!stageCompleted.get(IDTrackerDataUploadStage.ADD_SAMPLES)
				||	!stageCompleted.get(IDTrackerDataUploadStage.ADD_SAMPLE_PREPARATION_DATA))
			return null;
		
		return ((WizardSamplePrepPanel)
				panels.get(IDTrackerDataUploadStage.ADD_SAMPLE_PREPARATION_DATA)).getSamplePrep();
	}
	
	public Collection<DataExtractionMethod> getDataExtractionMethods() {

		return ((WizardMethodsPanel)
				panels.get(IDTrackerDataUploadStage.ADD_ACQ_DA_METHODS)).getDataExtractionMethods();
	}

	public Collection<DataAcquisitionMethod> getDataAcquisitionMethods() {
		return ((WizardMethodsPanel)
				panels.get(IDTrackerDataUploadStage.ADD_ACQ_DA_METHODS)).getDataAcquisitionMethods();
	}
	
	public Worklist getWorklist() {
		return ((WizardWorklistPanel)
				panels.get(IDTrackerDataUploadStage.ADD_WORKLISTS)).getWorklist();
	}
	
	public Map<DataFile,DataExtractionMethod>getFileDaMethodMap(){
		
		return ((WizardMsDataVerifierPanel)
				panels.get(IDTrackerDataUploadStage.VERIFY_DATA_FOR_UPLOAD)).getFileDaMethodMap();
	}
	
	public Collection<String>checkStageCompletion(){
		
		Collection<String>errors = new ArrayList<String>();		
		for(IDTrackerDataUploadStage stage : IDTrackerDataUploadStage.values()) {
			
			if(!stageCompleted.get(stage))
				errors.add("Step \"" + stage.getName() + "\" not completed.");
		}
		return errors;
	}

	private void uploadDataToIDdTracker() {
		
		Collection<String>errors = checkStageCompletion();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}
		ExperimentUploadTask task = new ExperimentUploadTask();
		idp = new IndeterminateProgressDialog("Uploading experiment data ...", this, task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
		
		// Initiate MSMS data upload
	}
	
	class ExperimentUploadTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		public ExperimentUploadTask() {
			super();
		}

		@Override
		public Void doInBackground() {

			saveExperiment();
			saveSampleData();
			saveSamplePrepData();
			sendWorklistToDatabase();		
			return null;
		}
		
	    @Override
	    public void done() {
	    	idp.dispose();
			parentPanel.reloadProjectTree();
			parentPanel.getProjectTreePanel().selectNodeForObject(getSamplePrep());
			initMSMSdataLoad();
	    }
	}
	
	private void saveExperiment() {

		try {
			String experimentId = IDTUtils.addNewExperiment(newExperiment);
			newExperiment.setId(experimentId);
			WizardExperimentDefinitionPanel experimentPanel = 
					(WizardExperimentDefinitionPanel)panels.get(IDTrackerDataUploadStage.CREATE_EXPERIMENT);			
			experimentPanel.getExperimentProject().getExperiments().add(newExperiment);
			IDTDataCache.getExperiments().add(newExperiment);			
			IDTUtils.setInstrumentForExperiment(
					newExperiment, experimentPanel.getInstrument());			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void saveSampleData() {
		
		Collection<IDTExperimentalSample>samples = 
				newExperiment.getExperimentDesign().getSamples().stream().
				filter(IDTExperimentalSample.class::isInstance).
				map(IDTExperimentalSample.class::cast).
				collect(Collectors.toList());
				
		for(IDTExperimentalSample sample : samples) {			
			try {
				String sampleId = IDTUtils.addNewIDTSample(sample, newExperiment);
				sample.setId(sampleId);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void saveSamplePrepData() {
		
		LIMSSamplePreparation prep2save = getSamplePrep();
		Map<LIMSExperiment, Collection<LIMSSamplePreparation>> espMap = 
				IDTDataCache.getExperimentSamplePrepMap();	
		Collection<IDTExperimentalSample>samples = 
				newExperiment.getExperimentDesign().getSamples().stream().
				filter(IDTExperimentalSample.class::isInstance).
				map(IDTExperimentalSample.class::cast).
				collect(Collectors.toList());
		try {
			IDTUtils.addNewSamplePrepWithSopsAndAnnotations(prep2save, samples);
			IDTDataCache.getSamplePreps().add(prep2save);
			IDTDataCache.getExperimentSamplePrepMap().put(newExperiment, new TreeSet<LIMSSamplePreparation>());
			IDTDataCache.getExperimentSamplePrepMap().get(newExperiment).add(prep2save);
			WizardSamplePrepPanel prepPanel = 
					(WizardSamplePrepPanel)panels.get(IDTrackerDataUploadStage.ADD_SAMPLE_PREPARATION_DATA);
			prepPanel.loadPrepDataForExperiment(prep2save, newExperiment);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendWorklistToDatabase() {

		Worklist newWorklist = getWorklist();
		Map<String, String> prepItemMap = getSamplePrep().getPrepItemMap();		
		newWorklist.getWorklistItems().stream().
			filter(LIMSWorklistItem.class::isInstance).
			map(LIMSWorklistItem.class::cast).
			forEach(i -> i.setPrepItemId(prepItemMap.get(i.getSample().getId())));	
		try {
			IDTUtils.uploadInjectionData(newWorklist);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IDTDataCache.refreshSamplePrepDataPipelineMap();
	}
	
	private void initMSMSdataLoad() {

		Map<DataFile,DataExtractionMethod>fileDaMethodMap = getFileDaMethodMap();
			
		Collection<LIMSWorklistItem>wlItems = getWorklist().getWorklistItems().stream().
			filter(LIMSWorklistItem.class::isInstance).
			map(LIMSWorklistItem.class::cast).collect(Collectors.toList());
			
		fileNumber = fileDaMethodMap.size();
		processedFiles = 0;
		for (Entry<DataFile, DataExtractionMethod> entry : fileDaMethodMap.entrySet()) {
			
			String baseName = FilenameUtils.getBaseName(entry.getKey().getName());
			LIMSWorklistItem rawFileItem = wlItems.stream().
					filter(i -> FilenameUtils.getBaseName(i.getDataFile().getName()).equals(baseName)).
					findFirst().orElse(null);
			if(rawFileItem == null || rawFileItem.getDataFile().getInjectionId() == null) {
				System.out.println("ERROR! - No injection for " + entry.getKey().getName());
			}
			else {
				entry.getKey().setInjectionId(rawFileItem.getDataFile().getInjectionId());
//				IDTCefImportTask task = new IDTCefImportTask(entry.getKey(), entry.getValue());
//				task.addTaskListener(this);
//				MRC2ToolBoxCore.getTaskController().addTask(task);
				
				IDTCefMSMSPrescanOrImportTask task = 
						new IDTCefMSMSPrescanOrImportTask(entry.getKey(), entry.getValue(), true);
				task.addTaskListener(this);
				MRC2ToolBoxCore.getTaskController().addTask(task);
			}
		}
	}
	
	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);
			if (e.getSource().getClass().equals(IDTCefMSMSPrescanOrImportTask.class)) 
				finalizeIDTCefMSMSPrescanOrImportTask((IDTCefMSMSPrescanOrImportTask)e.getSource());
		}
	}
	
	private synchronized void finalizeIDTCefMSMSPrescanOrImportTask(IDTCefMSMSPrescanOrImportTask task) {
		
		processedFiles++;
		
		if(processedFiles == fileNumber) {
	
			MessageDialog.showInfoMsg("Data import for experiment \"" + 
					newExperiment.getName() + "\" completed.", this);
			dispose();
		}
	}
}













