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
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;

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
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.io.ExperimentDesignParser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class DesignEditorPanel extends DockableMRC2ToolboxPanel {

	private static final Icon componentIcon = GuiUtils.getIcon("editDesignSubset", 16);

	private DesignEditorToolbar designEditorToolbar;
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
		designEditorToolbar = new DesignEditorToolbar(this);
		getContentPane().add(designEditorToolbar, BorderLayout.NORTH);
		designEditorToolbar.setAcceptDesignStatus(false);

		expDesignTable = new ExperimentDesignTable();
		expDesignTable.addTablePopupMenu(new GlobalDesignPopupMenu(this));
		expDesignTable.setTablePopupEnabled(false);
		designScrollPane = new JScrollPane(expDesignTable);
		designScrollPane.setPreferredSize(expDesignTable.getPreferredScrollableViewportSize());
		getContentPane().add(designScrollPane, BorderLayout.CENTER);

		designIsFromCurrentProject = false;
		txtFilter = new FileNameExtensionFilter("Text files", "txt", "TXT", "tsv", "TSV");
		baseDirectory = new File(MRC2ToolBoxConfiguration.getDefaultProjectsDirectory());
		
		initActions();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if(currentProject == null)
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
			rsd = new ReferenceSampleDialog(this, MRC2ToolBoxCore.getCurrentProject());

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

		if (MRC2ToolBoxCore.getCurrentProject() != null) {

			if (MRC2ToolBoxCore.getCurrentProject().getExperimentDesign().getCompleteDesignSubset() != null) {

				DeleteFactorDialog ddf = new DeleteFactorDialog();
				ddf.setLocationRelativeTo(this.getContentPane());
				ddf.setVisible(true);
			}
		}
	}

	private void editFactor() {

		if (MRC2ToolBoxCore.getCurrentProject() != null) {

			if (MRC2ToolBoxCore.getCurrentProject().getExperimentDesign().getCompleteDesignSubset() != null) {

				EditFactorDialog edf =
					new EditFactorDialog(MRC2ToolBoxCore.getCurrentProject().getExperimentDesign().getFactors().first());
				edf.setLocationRelativeTo(this.getContentPane());
				edf.setVisible(true);
			}
		}
	}

	private void addFactor() {

		if (MRC2ToolBoxCore.getCurrentProject() != null) {

			EditFactorDialog edf = new EditFactorDialog(null);
			edf.setLocationRelativeTo(this.getContentPane());
			edf.setVisible(true);
		}
	}

	private void exportDesign() {

		if(experimentDesign == null)
			return;

		JFileChooser chooser = new ImprovedFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle("Save experiment design to file:");
		chooser.setApproveButtonText("Save design");
		chooser.setCurrentDirectory(baseDirectory);
		chooser.setFileFilter(txtFilter);

		if (chooser.showSaveDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {

			baseDirectory = chooser.getCurrentDirectory();
			File outputFile = FIOUtils.changeExtension(chooser.getSelectedFile(), "txt") ;
			baseDirectory = chooser.getSelectedFile().getParentFile();
			String designString = expDesignTable.getDesignDataAsString();
			try {
				FileUtils.writeStringToFile(outputFile, designString);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void loadDesignFromFile(boolean append) {

		if(MRC2ToolBoxCore.getCurrentProject() == null)
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

		if (MRC2ToolBoxCore.getCurrentProject() == null)
			return null;

		JFileChooser chooser = new ImprovedFileChooser();
		chooser.setDialogTitle("Select experiment design file");
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setFileFilter(txtFilter);
		chooser.setCurrentDirectory(baseDirectory);

		if (chooser.showOpenDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {

			baseDirectory = chooser.getCurrentDirectory();
			return chooser.getSelectedFile();
		}
		else
			return null;
	}

	private void clearDesign() {

		int result = MessageDialog.showChoiceWithWarningMsg(
				"Do you want to erase experiment design completely?",
				this.getContentPane());

		if(result == JOptionPane.YES_OPTION) {

			if (currentProject != null)
				currentProject.getExperimentDesign().clearDesign();

			reloadDesign();
		}
	}

	public synchronized void clearPanel() {

		expDesignTable.clearTable();
		designEditorToolbar.updateGuiFromProjectAndDataPipeline(null, null);
	}
	
	@Override
	public void switchDataPipeline(DataAnalysisProject project, DataPipeline newPipeline) {

		clearPanel();
		super.switchDataPipeline(project, newPipeline);
		designEditorToolbar.updateGuiFromProjectAndDataPipeline(currentProject, activeDataPipeline);
		if(currentProject == null) 
			return;

		experimentDesign = currentProject.getExperimentDesign();
		expDesignTable.setModelFromProject(currentProject);
		designIsFromCurrentProject = true;
	}

	@Override
	public void reloadDesign() {
		switchDataPipeline(currentProject, activeDataPipeline);
	}

	public DataAnalysisProject getCurrentProject() {
		return currentProject;
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initActions() {
		// TODO Auto-generated method stub
		
	}
}
