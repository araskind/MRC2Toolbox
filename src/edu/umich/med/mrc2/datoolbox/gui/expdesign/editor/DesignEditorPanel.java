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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.ExperimentDesignSubsetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.MsFeatureEvent;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.io.ExperimentDesignParser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class DesignEditorPanel extends DockableMRC2ToolboxPanel {

	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "DesignEditorPanel.layout");
	
	private static final Icon componentIcon = GuiUtils.getIcon("editDesignSubset", 16);
	private static final Icon loadDesignIcon = GuiUtils.getIcon("loadDesign", 24);
	private static final Icon appendDesignIcon = GuiUtils.getIcon("appendDesign", 24);
	private static final Icon clearDesignIcon = GuiUtils.getIcon("clearDesign", 24);
	private static final Icon exportDesignIcon = GuiUtils.getIcon("exportDesign", 24);
	private static final Icon addFactorIcon = GuiUtils.getIcon("addFactor", 24);
	private static final Icon editFactorIcon = GuiUtils.getIcon("editFactor", 24);
	private static final Icon deleteFactorIcon = GuiUtils.getIcon("deleteFactor", 24);
	private static final Icon addSampleIcon = GuiUtils.getIcon("addSample", 24);
	private static final Icon editSampleIcon = GuiUtils.getIcon("editSample", 24);
	private static final Icon deleteSampleIcon = GuiUtils.getIcon("deleteSample", 24);
	private static final Icon editReferenceSamplesIcon = GuiUtils.getIcon("standardSample", 24);
	
	private ExperimentDesignTable expDesignTable;
	private JScrollPane designScrollPane;
	private ExperimentDesign experimentDesign;
	private boolean designIsFromCurrentProject;
	private FileFilter txtFilter;
	private ReferenceSampleDialog rsd;
	private File baseDirectory;
	private IndeterminateProgressDialog idp;

	public DesignEditorPanel() {

		super("DesignEditorPanel", "Sample properties", componentIcon);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		
		menuBar = new ExperimentDesignEditorMenuBar(this);
		getContentPane().add(menuBar, BorderLayout.NORTH);

		expDesignTable = new ExperimentDesignTable();
		expDesignTable.addTablePopupMenu(new GlobalDesignPopupMenu(this));
		expDesignTable.setTablePopupEnabled(false);
		designScrollPane = new JScrollPane(expDesignTable);
		designScrollPane.setPreferredSize(expDesignTable.getPreferredScrollableViewportSize());
		getContentPane().add(designScrollPane, BorderLayout.CENTER);

		designIsFromCurrentProject = false;
		txtFilter = new FileNameExtensionFilter("Text files", "txt", "TXT", "tsv", "TSV");
		baseDirectory = new File(MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory());
		
		initActions();
		
		loadLayout(layoutConfigFile);
		populatePanelsMenu();
	}

	@Override
	protected void initActions() {
		// TODO Auto-generated method stub
		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.LOAD_DESIGN_COMMAND.getName(),
				MainActionCommands.LOAD_DESIGN_COMMAND.getName(), 
				loadDesignIcon, this));		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.APPEND_DESIGN_COMMAND.getName(),
				MainActionCommands.APPEND_DESIGN_COMMAND.getName(), 
				appendDesignIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.CLEAR_DESIGN_COMMAND.getName(),
				MainActionCommands.CLEAR_DESIGN_COMMAND.getName(), 
				clearDesignIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EXPORT_DESIGN_COMMAND.getName(),
				MainActionCommands.EXPORT_DESIGN_COMMAND.getName(), 
				exportDesignIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ADD_FACTOR_COMMAND.getName(),
				MainActionCommands.ADD_FACTOR_COMMAND.getName(), 
				addFactorIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.EDIT_FACTOR_COMMAND.getName(),
				MainActionCommands.EDIT_FACTOR_COMMAND.getName(), 
				editFactorIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_FACTOR_COMMAND.getName(),
				MainActionCommands.DELETE_FACTOR_COMMAND.getName(), 
				deleteFactorIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.ADD_SAMPLE_DIALOG_COMMAND.getName(),
				MainActionCommands.ADD_SAMPLE_DIALOG_COMMAND.getName(), 
				addSampleIcon, this));
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.DELETE_SAMPLE_COMMAND.getName(),
				MainActionCommands.DELETE_SAMPLE_COMMAND.getName(), 
				deleteSampleIcon, this));
		
		menuActions.addSeparator();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.SHOW_REFERENCE_SAMPLES_EDIT_DIALOG_COMMAND.getName(),
				MainActionCommands.SHOW_REFERENCE_SAMPLES_EDIT_DIALOG_COMMAND.getName(), 
				editReferenceSamplesIcon, this));
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {

		if(MRC2ToolBoxCore.getIdTrackerUser() == null) {
			MessageDialog.showErrorMsg(
					"You are not logged in ID tracker!", 
					this.getContentPane());
			return;
		}
		super.actionPerformed(event);
		
		if(currentExperiment == null)
			return;

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.LOAD_DESIGN_COMMAND.getName()))
			loadDesignFromFile(false);

		if (command.equals(MainActionCommands.APPEND_DESIGN_COMMAND.getName()))
			loadDesignFromFile(true);

		if (command.equals(MainActionCommands.CLEAR_DESIGN_COMMAND.getName()))
			clearDesign();

		if (command.equals(MainActionCommands.EXPORT_DESIGN_COMMAND.getName()))
			exportDesign();

		if (command.equals(MainActionCommands.ADD_FACTOR_COMMAND.getName()))
			addFactor();

		if (command.equals(MainActionCommands.EDIT_FACTOR_COMMAND.getName()))
			editFactor();

		if (command.equals(MainActionCommands.DELETE_FACTOR_COMMAND.getName()))
			deleteFactor();

		if (command.equals(MainActionCommands.ADD_SAMPLE_DIALOG_COMMAND.getName()))
			showAddSampleDialog();

		if (command.equals(MainActionCommands.DELETE_SAMPLE_COMMAND.getName()))
			deleteSamples();

		if(command.equals(MainActionCommands.SHOW_REFERENCE_SAMPLES_EDIT_DIALOG_COMMAND.getName()))
			showReferenceSamplesEditDialog();

		if(command.equals(MainActionCommands.EDIT_REFERENCE_SAMPLES_COMMAND.getName()))
			editReferenceSamples();

		//	Table popup commands
		if (command.equals(MainActionCommands.ENABLE_SELECTED_SAMPLES_COMMAND.getName()))
			expDesignTable.setSamplesEnabledStatus(true);

		if (command.equals(MainActionCommands.DISABLE_SELECTED_SAMPLES_COMMAND.getName()))
			expDesignTable.setSamplesEnabledStatus(false);

		if (command.equals(MainActionCommands.INVERT_ENABLED_SAMPLES_COMMAND.getName()))
			expDesignTable.invertEnabledSamples();

		if (command.equals(MainActionCommands.EDIT_DESIGN_FOR_SELECTED_SAMPLES_COMMAND.getName()))
			editDesignForSelectedSamples();
	}

	private void showReferenceSamplesEditDialog() {

		if(rsd == null)
			rsd = new ReferenceSampleDialog(this, MRC2ToolBoxCore.getActiveMetabolomicsExperiment());

		rsd.setLocationRelativeTo(this.getContentPane());
		rsd.setVisible(true);
	}

	private void editDesignForSelectedSamples() {

		Collection<ExperimentalSample> samples = expDesignTable.getSelectedSamples();

		if(samples.isEmpty())
			return;

		DesignLevelAssignmentDialog dlad = new DesignLevelAssignmentDialog(samples);
		dlad.setLocationRelativeTo(this.getContentPane());
		dlad.setVisible(true);
	}

	private void editReferenceSamples() {
		
		rsd.editReferenceSamples();
		rsd.dispose();
	}

	private void showAddSampleDialog() {

		AddSamplesDialog asd = new AddSamplesDialog();
		asd.setLocationRelativeTo(this.getContentPane());
		asd.setVisible(true);
	}

	private void deleteSamples() {

		Collection<ExperimentalSample> toDelete = expDesignTable.getSelectedSamples();

		if(!toDelete.isEmpty()) {

			String yesNoQuestion = "Do you want to delete selected samples?";
			if(MessageDialog.showChoiceWithWarningMsg(yesNoQuestion , this.getContentPane()) == JOptionPane.YES_OPTION) {
				experimentDesign.setSuppressEvents(false);
				experimentDesign.removeSamples(toDelete);
			}
		}
	}

	private void deleteFactor() {

		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment() != null) {

			if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getCompleteDesignSubset() != null) {

				DeleteFactorDialog ddf = new DeleteFactorDialog();
				ddf.setLocationRelativeTo(this.getContentPane());
				ddf.setVisible(true);
			}
		}
	}

	private void editFactor() {

		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment() != null) {

			if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getCompleteDesignSubset() != null) {

				EditFactorDialog edf =
					new EditFactorDialog(MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getFactors().first());
				edf.setLocationRelativeTo(this.getContentPane());
				edf.setVisible(true);
			}
		}
	}

	private void addFactor() {

		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment() != null) {

			EditFactorDialog edf = new EditFactorDialog(null);
			edf.setLocationRelativeTo(this.getContentPane());
			edf.setVisible(true);
		}
	}

	private void exportDesign() {

		if(experimentDesign == null)
			return;

		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT");
		fc.setTitle("Save experiment design to file:");
		fc.setMultiSelectionEnabled(false);
		String defaultFileName = "Experiment_design_" + 
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".txt";
		fc.setDefaultFileName(defaultFileName);
		
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			
			File outputFile = fc.getSelectedFile();
			baseDirectory = outputFile.getParentFile();
			outputFile = FIOUtils.changeExtension(outputFile, "txt") ;
			String designString = expDesignTable.getDesignDataAsString();
			Path outputPath = Paths.get(outputFile.getAbsolutePath());
		    try {
				Files.writeString(outputPath, 
						designString, 
						StandardCharsets.UTF_8,
						StandardOpenOption.CREATE, 
						StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void loadDesignFromFile(boolean append) {

		if(MRC2ToolBoxCore.getActiveMetabolomicsExperiment() == null)
			return;

		if(!experimentDesign.isEmpty()  && !append) {

			int result = MessageDialog.showChoiceWithWarningMsg(
					"Do you want to replace existing experiment design?",
					this.getContentPane());

			if(result == JOptionPane.NO_OPTION)
				return;
		}
		File designFile = chooseDesignFile();
		if(designFile != null) {

			if (designFile.exists()) {

				ExperimentDesign newDesign = ExperimentDesignParser.parseExperimentDesign(designFile);

				if (newDesign == null) {
					MessageDialog.showWarningMsg("Design file is not in the right format!", expDesignTable);
					return;
				}
				if(append)
					experimentDesign.appendDesign(newDesign);
				else
					experimentDesign.replaceDesign(newDesign);
			}
		}
	}

	public File chooseDesignFile() {

		if (MRC2ToolBoxCore.getActiveMetabolomicsExperiment() == null)
			return null;

		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Text files", "txt", "TXT", "tsv", "TSV");
		fc.setTitle("Select experiment design file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
			File designFile = fc.getSelectedFile();
			baseDirectory = designFile.getParentFile();
			return designFile;
		}
		else
			return null;
	}

	private void clearDesign() {

		int result = MessageDialog.showChoiceWithWarningMsg(
				"Do you want to erase experiment design completely?",
				this.getContentPane());

		if(result == JOptionPane.YES_OPTION) {

			if (currentExperiment != null)
				currentExperiment.getExperimentDesign().clearDesign();

			reloadDesign();
		}
	}

	public synchronized void clearPanel() {

		expDesignTable.clearTable();
		menuBar.updateMenuFromExperiment(null, null);
	}
	
	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline newPipeline) {

		clearPanel();
		super.switchDataPipeline(project, newPipeline);
		menuBar.updateMenuFromExperiment(currentExperiment, activeDataPipeline);
		if(currentExperiment == null) 
			return;

		experimentDesign = currentExperiment.getExperimentDesign();
		expDesignTable.setModelFromProject(currentExperiment);
		designIsFromCurrentProject = true;
	}

	@Override
	public void reloadDesign() {
		switchDataPipeline(currentExperiment, activeDataPipeline);
	}

	public DataAnalysisProject getCurrentProject() {
		return currentExperiment;
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	@Override
	public void populatePanelsMenu() {
		// TODO Auto-generated method stub
		super.populatePanelsMenu();
	}

	@Override
	protected void executeAdminCommand(String command) {
		// TODO Auto-generated method stub
		
	}
}
