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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz;

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
import java.util.UUID;

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

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.enums.DataPrefix;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProtocol;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.communication.SamplePrepEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.SamplePrepListener;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.RawDataExaminerPanel;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.design.RDPExperimentDesignPanel;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.methods.RDPMethodsPanel;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.project.wiz.wkl.RDPWorklistPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.RawDataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.project.SaveStoredRawDataAnalysisProjectTask;

public class RDPMetadataWizard extends JDialog 
			implements ActionListener, TaskListener, SamplePrepListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3308258276718203363L;
	private static final Icon newCdpIdExperimentIcon = GuiUtils.getIcon("newIdExperiment", 32);
	private RawDataExaminerPanel parentPanel;
	private RawDataAnalysisProject project;
	private RDPMetadataWizardToolbar toolbar;
	private JPanel stagePanel;
	private JButton saveButton;
	private LinkedHashMap<RDPMetadataDefinitionStage, RDPMetadataWizardPanel> panels;
	private LinkedHashMap<RDPMetadataDefinitionStage, Boolean> stageCompleted;
	private RDPMetadataDefinitionStage activeStage;
	private GridBagConstraints gbc_panel;
	private RDPMetadataProgressToolbar progressToolbar;
	private LIMSExperiment newExperiment;
	private IndeterminateProgressDialog idp;
	private int processedFiles, fileNumber;

	public RDPMetadataWizard(
			RawDataExaminerPanel parentPanel,
			RawDataAnalysisProject project) {
		
		super();
		setIconImage(((ImageIcon) newCdpIdExperimentIcon).getImage());
		setPreferredSize(new Dimension(1000, 800));
		setSize(new Dimension(1000, 800));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.parentPanel = parentPanel;
		this.project = project;
		
		toolbar = new RDPMetadataWizardToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		stagePanel = new JPanel();
		stagePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(stagePanel, BorderLayout.CENTER);
		
		initWizardPanels();
		RDPSamplePrepPanel prepPanel = 
				(RDPSamplePrepPanel)panels.get(
						RDPMetadataDefinitionStage.ADD_SAMPLE_PREPARATION_DATA);
		prepPanel.getSamplePrepEditorPanel().addSamplePrepListener(this);
				
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
		
		stagePanel.add(panels.get(RDPMetadataDefinitionStage.CREATE_EXPERIMENT), gbc_panel);		
		setTitle(RDPMetadataDefinitionStage.CREATE_EXPERIMENT.getName());
		toolbar.highlightStageButton(RDPMetadataDefinitionStage.CREATE_EXPERIMENT);
		activeStage = RDPMetadataDefinitionStage.CREATE_EXPERIMENT;
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{737, 76, 65, 91, 0};
		gbl_panel_1.rowHeights = new int[]{23, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		progressToolbar = new RDPMetadataProgressToolbar(this);
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

		saveButton = new JButton(MainActionCommands.SAVE_PROJECT_METADATA_COMMAND.getName());
		saveButton.setActionCommand(MainActionCommands.SAVE_PROJECT_METADATA_COMMAND.getName());
		saveButton.addActionListener(this);
		GridBagConstraints gbc_saveButton = new GridBagConstraints();
		gbc_saveButton.anchor = GridBagConstraints.NORTHWEST;
		gbc_saveButton.gridx = 3;
		gbc_saveButton.gridy = 0;
		panel_1.add(saveButton, gbc_saveButton);
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.setDefaultButton(saveButton);

		populateWizardByProjectData();
		pack();
	}
	
	private void populateWizardByProjectData() {

		LIMSUser createdBy = project.getCreatedBy();
		if(createdBy == null)
			createdBy  = MRC2ToolBoxCore.getIdTrackerUser();
		
		if(project.getIdTrackerExperiment() == null) {
			
			newExperiment = new LIMSExperiment(
							null, 
							project.getName(), 
							project.getDescription(), 
							null, 
							null, 
							project.getDateCreated());
			
			newExperiment.setCreator(createdBy);
			newExperiment.setDesign(new ExperimentDesign());
		}	
		else {
			newExperiment = new LIMSExperiment(project.getIdTrackerExperiment());
			if(newExperiment.getCreator() == null)
				newExperiment.setCreator(createdBy);
		}
		populateExperimentDefinitionPanel();
		populateSampleAndPrepPanels();
		populateMethodsPanel();
		populateWorklistPanel();	
	}

	private void populateExperimentDefinitionPanel() {
		
		RDPExperimentDefinitionPanel experimentDefinitionPanel = 
				(RDPExperimentDefinitionPanel)panels.get(RDPMetadataDefinitionStage.CREATE_EXPERIMENT);
		experimentDefinitionPanel.setExperiment(newExperiment);	
		experimentDefinitionPanel.setInstrument(project.getInstrument());
	}
	
	private void populateSampleAndPrepPanels() {
		
		RDPExperimentDesignPanel designPanel = 
				(RDPExperimentDesignPanel)panels.get(RDPMetadataDefinitionStage.ADD_SAMPLES);
		designPanel.setExperiment(newExperiment);
	
		if(newExperiment.getSamplePreps().isEmpty())
			return;
		
		LIMSSamplePreparation prep = 
				newExperiment.getSamplePreps().iterator().next();		
		RDPSamplePrepPanel prepPanel = 
				(RDPSamplePrepPanel)panels.get(RDPMetadataDefinitionStage.ADD_SAMPLE_PREPARATION_DATA);
		prepPanel.loadPrepDataForExperiment(prep, newExperiment);
		
		if(prep.getId() != null) {
			LIMSSamplePreparation existingPrep = 
					IDTDataCash.getSamplePrepById(prep.getId());
			if(existingPrep != null){
				designPanel.setDesignEditable(false);
				prepPanel.setPrepEditable(false);
			}
		}
	}
		
	private void populateMethodsPanel() {
		
		Collection<DataAcquisitionMethod> acqMethods = project.getDataAcquisitionMethods();
		if(acqMethods == null || acqMethods.isEmpty())
			return;
		
		RDPMethodsPanel methodPanel = 
				(RDPMethodsPanel)panels.get(RDPMetadataDefinitionStage.ADD_SAMPLES);
		
		methodPanel.updateAcqusitionMethodList(acqMethods);
	}
	
	private void populateWorklistPanel() {
		
		//	Raw data files
		Collection<DataFile> dataFileList = project.getDataFiles();
		if(dataFileList == null || dataFileList.isEmpty())
			return;
		
		LIMSSamplePreparation prep = null;
		if(!newExperiment.getSamplePreps().isEmpty())
			prep = newExperiment.getSamplePreps().iterator().next();
		
		Worklist worklist = project.getWorklist();
		RDPWorklistPanel wklPanel = 
				((RDPWorklistPanel)panels.get(RDPMetadataDefinitionStage.ADD_WORKLISTS));
		
		wklPanel.loadWorklistWithoutValidation(worklist, newExperiment, prep);	
	}
		
	private void initWizardPanels() {
		
		panels = new LinkedHashMap<RDPMetadataDefinitionStage, RDPMetadataWizardPanel>();
		stageCompleted = new LinkedHashMap<RDPMetadataDefinitionStage, Boolean>();

		for(RDPMetadataDefinitionStage panelType : RDPMetadataDefinitionStage.values()) {
			
			try {
				panels.put(panelType, (RDPMetadataWizardPanel) 
						panelType.getPanelClass().
						getDeclaredConstructor(RDPMetadataWizard.class).newInstance(this));			
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
		for(RDPMetadataDefinitionStage stage : RDPMetadataDefinitionStage.values()) {
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
		
		if(command.equals(MainActionCommands.SAVE_PROJECT_METADATA_COMMAND.getName()))
			saveProjectMetadata();
	}
	
	public void showStagePanel(RDPMetadataDefinitionStage stage) {
		
		stagePanel.remove(panels.get(activeStage));
		revalidate();
		repaint();
		activeStage = stage;
		stagePanel.add(panels.get(stage), gbc_panel);
		setTitle(stage.getName());
		toolbar.highlightStageButton(stage);
	}
	
	private boolean completeExperimentDefinitionStage() {

		RDPExperimentDefinitionPanel experimentPanel = 
				(RDPExperimentDefinitionPanel)panels.get(RDPMetadataDefinitionStage.CREATE_EXPERIMENT);
		Collection<String>errors = experimentPanel.validateExperimentDefinition();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return false;
		}
		newExperiment.setName(experimentPanel.getExperimentName());
		newExperiment.setDescription(experimentPanel.getExperimentDescription());
		newExperiment.setNotes(experimentPanel.getExperimentNotes());
		newExperiment.setProject(experimentPanel.getExperimentProject());
				
		for(RDPMetadataDefinitionStage panelType : RDPMetadataDefinitionStage.values()) {
			panels.get(panelType).setExperiment(newExperiment);
			panels.get(panelType).setSamplePrep(getSamplePrep());
		}
		stageCompleted.put(RDPMetadataDefinitionStage.CREATE_EXPERIMENT, true);
		progressToolbar.markStageCompletedStatus(RDPMetadataDefinitionStage.CREATE_EXPERIMENT, true);
		showStagePanel(RDPMetadataDefinitionStage.ADD_SAMPLES);
		return true;
	}

	private boolean completeSampleListDefinitionStage() {
		
		RDPExperimentDesignPanel designPanel = 
				(RDPExperimentDesignPanel)panels.get(RDPMetadataDefinitionStage.ADD_SAMPLES);
		Collection<String>errors = designPanel.validateExperimentDesign();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return false;
		}
		stageCompleted.put(RDPMetadataDefinitionStage.ADD_SAMPLES, true);
		progressToolbar.markStageCompletedStatus(RDPMetadataDefinitionStage.ADD_SAMPLES, true);
		RDPSamplePrepPanel prepPanel = 
				(RDPSamplePrepPanel)panels.get(RDPMetadataDefinitionStage.ADD_SAMPLE_PREPARATION_DATA);

		prepPanel.loadPrepDataForExperiment(getSamplePrep(), newExperiment);
		showStagePanel(RDPMetadataDefinitionStage.ADD_SAMPLE_PREPARATION_DATA);		
		return true;
	}

	private boolean completeSamplePrepDefinitionStage() {
		
		if(!stageCompleted.get(RDPMetadataDefinitionStage.ADD_SAMPLES)) {

			if(!completeSampleListDefinitionStage())
				return false;
		}
		RDPSamplePrepPanel prepPanel = 
				(RDPSamplePrepPanel)panels.get(RDPMetadataDefinitionStage.ADD_SAMPLE_PREPARATION_DATA);
		Collection<String>errors = prepPanel.validateSamplePrepDefinition();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return false;
		}
		LIMSSamplePreparation samplePrep = getSamplePrep();
		
		//	New sample prep
		if(samplePrep == null) {
			
			samplePrep = new LIMSSamplePreparation(
					null, 
					prepPanel.getPrepName(), 
					prepPanel.getPrepDate(), 
					prepPanel.getPrepUser());
			samplePrep.setId(DataPrefix.SAMPLE_PREPARATION.getName() + 
					UUID.randomUUID().toString().substring(0, 7));		
			
			for(LIMSProtocol sop : prepPanel.getPrepSops())
				samplePrep.addProtocol(sop);
			
			for(ObjectAnnotation annotation : prepPanel.getPrepAnnotations())
				samplePrep.addAnnotation(annotation);
			
			int count = 1;
			for(ExperimentalSample sample : newExperiment.getExperimentDesign().getSamples()) {
				samplePrep.addPrepItem(sample.getId(), DataPrefix.PREPARED_SAMPLE.getName() 
						+ StringUtils.leftPad(Integer.toString(count), 7, '0'));
				count++;
			}
		}
		else {
			//	Sample prep not in database
			if(samplePrep.getId() != null 
					&& IDTDataCash.getSamplePrepById(samplePrep.getId()) == null) {
				
				samplePrep.setName(prepPanel.getPrepName());
				samplePrep.setPrepDate(prepPanel.getPrepDate());
				samplePrep.setCreator(prepPanel.getPrepUser());
				
				samplePrep.getProtocols().clear();
				for(LIMSProtocol sop : prepPanel.getPrepSops())
					samplePrep.addProtocol(sop);
				
				samplePrep.getAnnotations().clear();
				for(ObjectAnnotation annotation : prepPanel.getPrepAnnotations())
					samplePrep.addAnnotation(annotation);
				
				samplePrep.getPrepItemMap().clear();
				int count = 1;
				for(ExperimentalSample sample : newExperiment.getExperimentDesign().getSamples()) {
					samplePrep.addPrepItem(sample.getId(), DataPrefix.PREPARED_SAMPLE.getName() 
							+ StringUtils.leftPad(Integer.toString(count), 7, '0'));
					count++;
				}
			}

		}
		prepPanel.loadPrepDataForExperiment(samplePrep, newExperiment);							
		RDPWorklistPanel worklistPanel = 
				(RDPWorklistPanel)panels.get(RDPMetadataDefinitionStage.ADD_WORKLISTS);
		worklistPanel.setExperiment(newExperiment);
		worklistPanel.setSamplePrep(samplePrep);
		
		stageCompleted.put(RDPMetadataDefinitionStage.ADD_SAMPLE_PREPARATION_DATA, true);
		progressToolbar.markStageCompletedStatus(RDPMetadataDefinitionStage.ADD_SAMPLE_PREPARATION_DATA, true);
		showStagePanel(RDPMetadataDefinitionStage.ADD_ACQ_DA_METHODS);	
		return true;
	}

	private boolean completeAnalysisMethodsDefinitionStage() {

		Collection<String>errors = ((RDPMethodsPanel)panels.get(
				RDPMetadataDefinitionStage.ADD_ACQ_DA_METHODS)).validateMethodsData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return false;
		}
		//	TODO upload new methods to the database
				
		stageCompleted.put(RDPMetadataDefinitionStage.ADD_ACQ_DA_METHODS, true);
		progressToolbar.markStageCompletedStatus(RDPMetadataDefinitionStage.ADD_ACQ_DA_METHODS, true);
		showStagePanel(RDPMetadataDefinitionStage.ADD_WORKLISTS);
		return true;
	}

	private boolean completeWorklistVerificationStage() {
		
		if(!stageCompleted.get(RDPMetadataDefinitionStage.ADD_ACQ_DA_METHODS)) {
			
			if(!completeAnalysisMethodsDefinitionStage())
				return false;
		}
		Collection<String>errors = ((RDPWorklistPanel)panels.get(
				RDPMetadataDefinitionStage.ADD_WORKLISTS)).validateWorklistData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return false;
		}		
		stageCompleted.put(RDPMetadataDefinitionStage.ADD_WORKLISTS, true);
		progressToolbar.markStageCompletedStatus(RDPMetadataDefinitionStage.ADD_WORKLISTS, true);	
		return true;
	}
	
	public LIMSExperiment getExperiment() {
		return newExperiment;
	}
	
	public LIMSSamplePreparation getSamplePrep() {
		
//		if(!stageCompleted.get(RDPMetadataDefinitionStage.ADD_SAMPLES)
//				||	!stageCompleted.get(RDPMetadataDefinitionStage.ADD_SAMPLE_PREPARATION_DATA))
//			return null;
		
		return ((RDPSamplePrepPanel)
				panels.get(RDPMetadataDefinitionStage.ADD_SAMPLE_PREPARATION_DATA)).getSamplePrep();
	}
	
//	public Collection<DataExtractionMethod> getDataExtractionMethods() {
//
//		return ((WizardMethodsPanel)
//				panels.get(RawDataProjectMetadataDefinitionStage.ADD_ACQ_DA_METHODS)).getDataExtractionMethods();
//	}

	public Collection<DataAcquisitionMethod> getDataAcquisitionMethods() {
		return ((RDPMethodsPanel)
				panels.get(RDPMetadataDefinitionStage.ADD_ACQ_DA_METHODS)).getDataAcquisitionMethods();
	}
	
	public Worklist getWorklist() {
		return ((RDPWorklistPanel)
				panels.get(RDPMetadataDefinitionStage.ADD_WORKLISTS)).getWorklist();
	}
	
	public Collection<String>checkStageCompletion(){
		
		Collection<String>errors = new ArrayList<String>();		
		for(RDPMetadataDefinitionStage stage : RDPMetadataDefinitionStage.values()) {
			
			if(!stageCompleted.get(stage))
				errors.add("Step \"" + stage.getName() + "\" not completed.");
		}
		return errors;
	}

	private void saveProjectMetadata() {
		
		if(!completeExperimentDefinitionStage()) {
			showStagePanel(RDPMetadataDefinitionStage.CREATE_EXPERIMENT);
			return;
		}
		if(!completeSampleListDefinitionStage()) {
			showStagePanel(RDPMetadataDefinitionStage.ADD_SAMPLES);
			return;
		}
		if(!completeSamplePrepDefinitionStage()) {
			showStagePanel(RDPMetadataDefinitionStage.ADD_SAMPLE_PREPARATION_DATA);
			return;
		}
		if(!completeAnalysisMethodsDefinitionStage()) {
			showStagePanel(RDPMetadataDefinitionStage.ADD_ACQ_DA_METHODS);
			return;
		}
		if(!completeWorklistVerificationStage()) {
			showStagePanel(RDPMetadataDefinitionStage.ADD_WORKLISTS);
			return;
		}	
		Collection<String>errors = checkStageCompletion();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}
		//	Experiment
		project.setIdTrackerExperiment(newExperiment);
		RDPExperimentDefinitionPanel experimentPanel = 
				(RDPExperimentDefinitionPanel)panels.get(RDPMetadataDefinitionStage.CREATE_EXPERIMENT);
		project.setInstrument(experimentPanel.getInstrument());
		
		//	Sample prep
		RDPSamplePrepPanel prepPanel = 
				(RDPSamplePrepPanel)panels.get(RDPMetadataDefinitionStage.ADD_SAMPLE_PREPARATION_DATA);
		//	TODO this will need a different handling if multiple preps are present
		newExperiment.getSamplePreps().clear();
		newExperiment.getSamplePreps().add(prepPanel.getSamplePrep());
		
		//	Worklist/injections
		RDPWorklistPanel worklistPanel = 
				(RDPWorklistPanel)panels.get(RDPMetadataDefinitionStage.ADD_WORKLISTS);
		Worklist worklist = worklistPanel.getWorklist();
		project.updateMetadataFromWorklist(worklist);
		
		SaveStoredRawDataAnalysisProjectTask task = 
				new SaveStoredRawDataAnalysisProjectTask(
						MRC2ToolBoxCore.getActiveRawDataAnalysisProject());
		task.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}	

	/**
	 *
	 */
	@Override
	public void samplePrepStatusChanged(SamplePrepEvent e) {
		
		if(newExperiment == null)
			return;
		
		LIMSSamplePreparation prep = (LIMSSamplePreparation)e.getSource();
		RDPSamplePrepPanel prepPanel = 
				(RDPSamplePrepPanel)panels.get(RDPMetadataDefinitionStage.ADD_SAMPLE_PREPARATION_DATA);
		RDPExperimentDesignPanel designPanel = 
				(RDPExperimentDesignPanel)panels.get(RDPMetadataDefinitionStage.ADD_SAMPLES);
		
		Collection<IDTExperimentalSample>samples = new ArrayList<IDTExperimentalSample>();		
		try {
			samples = IDTUtils.getSamplesForPrep(prep);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}				
		if(e.getStatus().equals(ParameterSetStatus.REMOVED)) {
			
			//	If samples were inferred from existing sample prep, 
			//	remove them when removing the prep
			if(!samples.isEmpty())
				newExperiment.getExperimentDesign().removeSamples(samples);
			
			newExperiment.getSamplePreps().remove(prep);
			for(RDPMetadataDefinitionStage panelType : RDPMetadataDefinitionStage.values()) {
				
				if(!panelType.equals(RDPMetadataDefinitionStage.ADD_SAMPLE_PREPARATION_DATA)) {
					
					panels.get(panelType).setExperiment(newExperiment);
					panels.get(panelType).setSamplePrep(null);
				}
			}
			prepPanel.setPrepEditable(true);
			designPanel.setDesignEditable(true);
		}
		if(e.getStatus().equals(ParameterSetStatus.ADDED)) {
			
			if(!samples.isEmpty()) {
				newExperiment.getExperimentDesign().getSamples().clear();
				newExperiment.getExperimentDesign().addSamples(samples);
			}			
			//	TODO this will need a different handling if multiple preps are present
			newExperiment.getSamplePreps().clear();
			newExperiment.getSamplePreps().add(prep);
			for(RDPMetadataDefinitionStage panelType : RDPMetadataDefinitionStage.values()) {
				
				if(!panelType.equals(RDPMetadataDefinitionStage.ADD_SAMPLE_PREPARATION_DATA)) {
					
					panels.get(panelType).setExperiment(newExperiment);
					panels.get(panelType).setSamplePrep(prep);
				}
			}
			if(prep.getId() != null) {
				LIMSSamplePreparation existingPrep = IDTDataCash.getSamplePrepById(prep.getId());
				if(existingPrep != null) {
					prepPanel.setPrepEditable(false);
					designPanel.setDesignEditable(false);
				}
			}
		}
		RDPWorklistPanel worklistPanel = 
				(RDPWorklistPanel)panels.get(RDPMetadataDefinitionStage.ADD_WORKLISTS);
		worklistPanel.updateColumnEditorsFromSamplesAndPrep();
	}

	public void updateAcqusitionMethodList(
			Collection<DataAcquisitionMethod> dataAcquisitionMethods) {
		// TODO Auto-generated method stub
		RDPMethodsPanel methodsPanel = 
				(RDPMethodsPanel)panels.get(RDPMetadataDefinitionStage.ADD_ACQ_DA_METHODS);
		methodsPanel.updateAcqusitionMethodList(dataAcquisitionMethods);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);
			MRC2ToolBoxCore.getTaskController().getTaskQueue().clear();
			MainWindow.hideProgressDialog();
			dispose();
		}
	}
}













