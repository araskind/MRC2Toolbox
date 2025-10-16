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

package edu.umich.med.mrc2.datoolbox.gui.lims.experiment.design;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignDisplay;
import edu.umich.med.mrc2.datoolbox.gui.expdesign.editor.ExperimentDesignTable;
import edu.umich.med.mrc2.datoolbox.gui.lims.experiment.LIMSTablePopupMenu;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class DockableLIMSDesignEditorPanel  extends DefaultSingleCDockable implements ActionListener, ExperimentDesignDisplay {

	private static final Icon componentIcon = GuiUtils.getIcon("editDesignSubset", 16);

	private LIMSDesignEditorToolbar designEditorToolbar;
	private ExperimentDesignTable expDesignTable;
	private JScrollPane designScrollPane;
	private ExperimentDesign experimentDesign;
	private FileFilter txtFilter;
	private File baseDirectory;

	public DockableLIMSDesignEditorPanel() {

		super("DockableLIMSDesignEditorPanel", componentIcon, "LIMS experiment design", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		designEditorToolbar = new LIMSDesignEditorToolbar(this);
		getContentPane().add(designEditorToolbar, BorderLayout.NORTH);

		expDesignTable = new ExperimentDesignTable();
		expDesignTable.addTablePopupMenu(new LIMSTablePopupMenu(this));	
		designScrollPane = new JScrollPane();
		designScrollPane.add(expDesignTable);
		designScrollPane.setViewportView(expDesignTable);
		designScrollPane.setPreferredSize(expDesignTable.getPreferredScrollableViewportSize());
		getContentPane().add(designScrollPane, BorderLayout.CENTER);

		txtFilter = new FileNameExtensionFilter("Text files", "txt", "TXT");
		baseDirectory = new File(MRC2ToolBoxCore.dataDir);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.EXPORT_DESIGN_COMMAND.getName()))
			exportDesign();

		if (command.equals(MainActionCommands.ADD_FACTOR_COMMAND.getName()))
			addFactor();

		if (command.equals(MainActionCommands.EDIT_FACTOR_COMMAND.getName()))
			editFactor();

		if (command.equals(MainActionCommands.DELETE_FACTOR_COMMAND.getName()))
			deleteFactor();

		if (command.equals(MainActionCommands.ADD_SAMPLE_COMMAND.getName()))
			addSample();

		if (command.equals(MainActionCommands.DELETE_SAMPLE_COMMAND.getName()))
			deleteSample();
		
		if(command.equals(MainActionCommands.COPY_SELECTED_TABLE_ROWS_COMMAND.getName()))
			expDesignTable.copySelectedRowsToClipboard();
		
		if(command.equals(MainActionCommands.COPY_VISIBLE_TABLE_ROWS_COMMAND.getName()))
			expDesignTable.copyVisibleTableRowsToClipboard();
	}

	private void addSample() {
		// TODO Auto-generated method stub

	}

	private void editSample() {
		// TODO Auto-generated method stub

	}

	private void deleteSample() {
		// TODO Auto-generated method stub

	}

	private void deleteFactor() {
		// TODO Auto-generated method stub

	}

	private void editFactor() {
		// TODO Auto-generated method stub

	}

	private void addFactor() {
		// TODO Auto-generated method stub

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
				MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".TXT";
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
//		JFileChooser chooser = new ImprovedFileChooser();
//		chooser.setAcceptAllFileFilterUsed(false);
//		chooser.setMultiSelectionEnabled(false);
//		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
//		chooser.setDialogTitle("Save experiment design to file:");
//		chooser.setApproveButtonText("Save design");
//		chooser.setCurrentDirectory(baseDirectory);
//		chooser.setFileFilter(txtFilter);
//
//		if (chooser.showSaveDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {
//
//			File outputFile = FIOUtils.changeExtension(chooser.getSelectedFile(), "txt") ;
//			baseDirectory = chooser.getSelectedFile().getParentFile();
//			String designString = expDesignTable.getDesignDataAsString();
//			try {
//				FileUtils.writeStringToFile(outputFile, designString);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}

	public void showExperimentDesign(ExperimentDesign newDesign) {

		experimentDesign = newDesign;
		reloadDesign();
	}

	@Override
	public void reloadDesign() {

		expDesignTable.clearTable();
		if (experimentDesign != null)
			expDesignTable.setModelFromDesign(experimentDesign);
	}

	public synchronized void clearPanel() {

		experimentDesign = null;
		expDesignTable.clearTable();
	}

	/**
	 * @return the experimentDesign
	 */
	public ExperimentDesign getExperimentDesign() {
		return experimentDesign;
	}

	/**
	 * @param experimentDesign the experimentDesign to set
	 */
	public void setExperimentDesign(ExperimentDesign experimentDesign) {
		this.experimentDesign = experimentDesign;
	}
}
