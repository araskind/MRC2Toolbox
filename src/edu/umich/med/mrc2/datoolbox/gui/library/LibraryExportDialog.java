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

package edu.umich.med.mrc2.datoolbox.gui.library;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.MsLibraryFormat;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.LibraryExportTask;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;

public class LibraryExportDialog extends JDialog implements ActionListener, TaskListener{

	/**
	 *
	 */
	private static final long serialVersionUID = -5710215500366663782L;
	private JFileChooser chooser;
	private File baseDirectory;
	private JPanel panel;
	private CompoundLibrary currentLibrary;
	private Collection<LibraryMsFeature>targetSubset;
	private Collection<MsFeature>featureSubset;
	private JCheckBox combineAdductsCheckBox;

	private static final Icon exportLibraryIcon = GuiUtils.getIcon("exportLibrary", 32);

	public LibraryExportDialog(String exportCommand) {

		super(MRC2ToolBoxCore.getMainWindow(), "Export library");
		setIconImage(((ImageIcon) exportLibraryIcon).getImage());
		setPreferredSize(new Dimension(640, 480));

		String title = "";

		if (exportCommand.equals(MainActionCommands.EXPORT_COMPOUND_LIBRARY_COMMAND.getName()))
			title = "Export complete library to file";

		if(exportCommand.equals(MainActionCommands.EXPORT_FILTERED_COMPOUND_LIBRARY_COMMAND.getName()))
			title = "Export filtered library to file";

		if(exportCommand.equals(MainActionCommands.EXPORT_FEATURE_SUBSET_COMMAND.getName()))
			title = "Export feature subset as library file";

		setTitle(title);

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(640, 480));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		panel = new JPanel();
		panel.setLayout(new BorderLayout(0, 0));
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);

		chooser = new ImprovedFileChooser();
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setBorder(new TitledBorder(null, "Output", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setApproveButtonText("Export library");
		chooser.addActionListener(this);

		for(MsLibraryFormat f : MsLibraryFormat.values()){

			FileNameExtensionFilter txtFilter = new FileNameExtensionFilter(f.getName(), f.getFileExtension());
			chooser.addChoosableFileFilter(txtFilter);
		}
		baseDirectory = new File(MRC2ToolBoxCore.libraryDir);
		if(MRC2ToolBoxCore.getCurrentProject() != null)
			baseDirectory = MRC2ToolBoxCore.getCurrentProject().getExportsDirectory();

		chooser.setCurrentDirectory(baseDirectory);
		panel.add(chooser);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_1, BorderLayout.NORTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{404, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		combineAdductsCheckBox = new JCheckBox("Combine all adducts in a single library entry");
		GridBagConstraints gbc_combineAdductsCheckBox = new GridBagConstraints();
		gbc_combineAdductsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_combineAdductsCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_combineAdductsCheckBox.gridx = 0;
		gbc_combineAdductsCheckBox.gridy = 0;
		panel_1.add(combineAdductsCheckBox, gbc_combineAdductsCheckBox);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		JRootPane rootPane = SwingUtilities.getRootPane(chooser);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if (event.getSource().equals(chooser)) {

			if (event.getActionCommand().equals(JFileChooser.CANCEL_SELECTION))
				this.dispose();

			if (event.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))
				exportLibrary();
		}
	}

	private void exportLibrary() {

		File selectedFile = chooser.getSelectedFile();
		if(selectedFile == null)
			return;

		MsLibraryFormat libraryFormat = MsLibraryFormat.getFormatByDescription(
				chooser.getFileFilter().getDescription());
		LibraryExportTask let = null;
		if(featureSubset == null){

			let = new LibraryExportTask(
					selectedFile,
					currentLibrary,
					targetSubset,
					libraryFormat,
					combineAdductsCheckBox.isSelected());
		}
		//	Export features only
		if(targetSubset == null && featureSubset != null){

			let = new LibraryExportTask(
					selectedFile,
					currentLibrary,
					featureSubset,
					combineAdductsCheckBox.isSelected(),
					libraryFormat);
		}
		//	Export merged library
		if(targetSubset != null && featureSubset != null){

			let = new LibraryExportTask(
					selectedFile,
					currentLibrary,
					targetSubset,
					featureSubset,
					combineAdductsCheckBox.isSelected(),
					libraryFormat);
		}
		if(let != null) {
			let.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(let);
		}
	}

	public void setCurrentLibrary(CompoundLibrary currentLibrary) {
		this.currentLibrary = currentLibrary;
		MsLibraryFormat libraryFormat = MsLibraryFormat.getFormatByDescription(
				chooser.getFileFilter().getDescription());
		File selectedFile= Paths.get(baseDirectory.getAbsolutePath(),
				currentLibrary.getLibraryName()).toFile();
		File libExportFile = FIOUtils.changeExtension(selectedFile, libraryFormat.getFileExtension());
		chooser.setSelectedFile(libExportFile);
	}

	public void setFeatureSubset(Collection<MsFeature> features) {
		featureSubset = features;
	}

	public void setTargetSubset(Collection<LibraryMsFeature> targetSubset) {
		this.targetSubset = targetSubset;
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);
			if (e.getSource().getClass().equals(LibraryExportTask.class)) {

				MessageDialog.showInfoMsg("Library export completed.", this);
				dispose();
			}
		}
	}
}





















