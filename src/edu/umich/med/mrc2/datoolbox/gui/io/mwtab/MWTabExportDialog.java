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

package edu.umich.med.mrc2.datoolbox.gui.io.mwtab;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.MWtabReportStyle;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.MWTabReportTask;

public class MWTabExportDialog extends JDialog implements BackedByPreferences, ActionListener{

	/**
	 *
	 */
	private static final long serialVersionUID = -2058034847456342065L;
	private static final Icon exportMwTabIcon = GuiUtils.getIcon("mwTabReport", 32);
	private Preferences preferences;
	private File baseDirectory;
	private ImprovedFileChooser chooser;
	private DataAnalysisProject project;
	private JButton btnSave;
	private JComboBox dataPipelineComboBox;
	public static final String PREFS_NODE = MWTabExportDialog.class.getName();
	private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
	private JComboBox reportStyleComboBox;
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private static final String SAVE_REPORT = "Save report";

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MWTabExportDialog() {
		super();
		setPreferredSize(new Dimension(640, 500));
		setIconImage(((ImageIcon) exportMwTabIcon).getImage());
		setTitle("Save MWTab report for experiment");
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(640, 500));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		project = MRC2ToolBoxCore.getCurrentProject();
		//	loadPreferences();
		initChooser();

		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(10, 5, 10, 5));
		panel_1.add(panel_2, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0, 0};
		gbl_panel_2.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);

		JLabel lblSelectAssayMethod = new JLabel("Assay method");
		GridBagConstraints gbc_lblSelectAssayMethod = new GridBagConstraints();
		gbc_lblSelectAssayMethod.insets = new Insets(0, 0, 5, 5);
		gbc_lblSelectAssayMethod.anchor = GridBagConstraints.EAST;
		gbc_lblSelectAssayMethod.gridx = 0;
		gbc_lblSelectAssayMethod.gridy = 0;
		panel_2.add(lblSelectAssayMethod, gbc_lblSelectAssayMethod);

		dataPipelineComboBox = new JComboBox<DataPipeline>();
		//	TODO this is temp solution to create DRCC reports
		dataPipelineComboBox.setModel(
				new SortedComboBoxModel<DataPipeline>(project.getDataPipelines()));
		dataPipelineComboBox.setSelectedIndex(-1);

		GridBagConstraints gbc_assayComboBox = new GridBagConstraints();
		gbc_assayComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_assayComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_assayComboBox.gridx = 1;
		gbc_assayComboBox.gridy = 0;
		panel_2.add(dataPipelineComboBox, gbc_assayComboBox);
		panel_1.add(chooser, BorderLayout.CENTER);
		
		JLabel lblReportType = new JLabel("Report style");
		GridBagConstraints gbc_lblReportType = new GridBagConstraints();
		gbc_lblReportType.anchor = GridBagConstraints.EAST;
		gbc_lblReportType.insets = new Insets(0, 0, 0, 5);
		gbc_lblReportType.gridx = 0;
		gbc_lblReportType.gridy = 1;
		panel_2.add(lblReportType, gbc_lblReportType);
		
		reportStyleComboBox = new JComboBox<MWtabReportStyle>(
				new DefaultComboBoxModel<MWtabReportStyle>(MWtabReportStyle.values()));
		reportStyleComboBox.setSelectedIndex(-1);
		GridBagConstraints gbc_reportTypeComboBox = new GridBagConstraints();
		gbc_reportTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_reportTypeComboBox.gridx = 1;
		gbc_reportTypeComboBox.gridy = 1;
		panel_2.add(reportStyleComboBox, gbc_reportTypeComboBox);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		btnSave = new JButton(SAVE_REPORT);
		btnSave.setActionCommand(SAVE_REPORT);
		btnSave.addActionListener(this);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals(SAVE_REPORT)) {

			chooser.approveSelection();
			File exportFile = chooser.getSelectedFile();
			if(exportFile == null)
				return;

			ArrayList<String>errors = validateProjectData();
			if(!errors.isEmpty()) {
				MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
				return;
			}
			DataPipeline assay = (DataPipeline) dataPipelineComboBox.getSelectedItem();
			MWtabReportStyle style = (MWtabReportStyle)reportStyleComboBox.getSelectedItem();
			ExperimentDesignSubset designSubset = project.getExperimentDesign().getActiveDesignSubset();
			MWTabReportTask task = new MWTabReportTask(
					exportFile, 
					project, 
					assay, 
					designSubset, 
					style);			
			task.addTaskListener(MRC2ToolBoxCore.getMainWindow());
			MRC2ToolBoxCore.getTaskController().addTask(task);
			baseDirectory = chooser.getCurrentDirectory();
			savePreferences();
			this.dispose();
		}
	}

	private ArrayList<String> validateProjectData() {

		ArrayList<String>errors = new ArrayList<String>();

		if(project.getLimsProject() == null)
			errors.add("LIMS project data are missing.");

		if(project.getLimsExperiment() == null)
			errors.add("LIMS experiment data are missing.");

		if(dataPipelineComboBox.getSelectedItem() == null)
			errors.add("Assay method should be specified.");
		
		if(reportStyleComboBox.getSelectedItem() == null)
			errors.add("Report style should be specified.");

		return errors;
	}

	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setCurrentDirectory(baseDirectory);
		chooser.setControlButtonsAreShown(false);
		//	chooser.getActionMap().get("viewTypeDetails").actionPerformed(null); //	?
		FileNameExtensionFilter mwTabFilter = new FileNameExtensionFilter("MWTab files", MWTabReportTask.MWTAB_EXTENSION);
		chooser.addChoosableFileFilter(mwTabFilter);
		chooser.setFileFilter(mwTabFilter);

		chooser.setSelectedFile(Paths.get(
				project.getExportsDirectory().getAbsolutePath(),
				createExportFileName()).toFile());
	}

	private String createExportFileName() {

		DataAnalysisProject currentProject = MRC2ToolBoxCore.getCurrentProject();
		String timestamp = dateTimeFormat.format(new Date());
		String fileName =
				currentProject.getName() + "_MWTAB_REPORT_" +
				timestamp + "." + MWTabReportTask.MWTAB_EXTENSION;
		return fileName;
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
}

















