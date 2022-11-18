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

package edu.umich.med.mrc2.datoolbox.gui.idworks.project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.ProjectType;

public class NewIDTrackerBasedProjectDialog extends JDialog implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 3164807824653540814L;
	public static final String CREATE_PROJECT_COMMAND = "CREATE_NEW_PROJECT";
	public static final String CHOOSE_PARENT_DIR_COMMAND = "CHOOSE_PARENT_DIR";
	private ActionListener alistener;
	private JFileChooser chooser;
	private File baseDirectory;
	private JTextArea descriptionTextArea;
	private ProjectType projectType;
	private ExperimentDesign design;
	private LIMSExperiment activeExperiment;

	private static final Icon newProjectIcon = GuiUtils.getIcon("newProject", 32);
	private static final Icon newIdProjectIcon = GuiUtils.getIcon("newIdProject", 32);

	private IDTrackerExperimentsTable idTrackerExperimentsTable;

	public NewIDTrackerBasedProjectDialog(ActionListener listener) {

		super();
		alistener = listener;

		setTitle("Create new IDTracker-based compound identification project");
		setIconImage(((ImageIcon) newIdProjectIcon).getImage());
		setPreferredSize(new Dimension(640, 800));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(640, 640));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(15, 15, 15, 15));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 124, 0 };
		gbl_panel.rowHeights = new int[] { 420, 186, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 1.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		initChooser();

		GridBagConstraints gbc_parentDirTextField = new GridBagConstraints();
		gbc_parentDirTextField.insets = new Insets(0, 0, 5, 0);
		gbc_parentDirTextField.fill = GridBagConstraints.BOTH;
		gbc_parentDirTextField.gridx = 0;
		gbc_parentDirTextField.gridy = 0;
		panel.add(chooser, gbc_parentDirTextField);

		idTrackerExperimentsTable = new IDTrackerExperimentsTable();
		idTrackerExperimentsTable.setModelFromExperimentCollection(IDTDataCash.getExperiments());
		JScrollPane scrollPane = new JScrollPane(idTrackerExperimentsTable);
		scrollPane.setBorder(
			new TitledBorder(UIManager.getBorder("TitledBorder.border"),
					"Select IDTracker experiment",
					TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		panel.add(scrollPane, gbc_scrollPane);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(
			new TitledBorder(UIManager.getBorder("TitledBorder.border"),
				"Project description (optional)",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 2;
		panel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{124, 226, 162, 0};
		gbl_panel_1.rowHeights = new int[]{0, 23, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		descriptionTextArea = new JTextArea();
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.setWrapStyleWord(true);
		GridBagConstraints gbc_descriptionTextArea = new GridBagConstraints();
		gbc_descriptionTextArea.fill = GridBagConstraints.BOTH;
		gbc_descriptionTextArea.gridheight = 2;
		gbc_descriptionTextArea.gridwidth = 3;
		gbc_descriptionTextArea.insets = new Insets(0, 0, 5, 5);
		gbc_descriptionTextArea.gridx = 0;
		gbc_descriptionTextArea.gridy = 0;
		panel_1.add(descriptionTextArea, gbc_descriptionTextArea);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		if (event.getSource().equals(chooser)) {

			if (event.getActionCommand().equals(JFileChooser.APPROVE_SELECTION))
				createNewProject();

			if (event.getActionCommand().equals(JFileChooser.CANCEL_SELECTION))
				dispose();
		}
		if (event.getActionCommand().equals(NewIDTrackerBasedProjectDialog.CHOOSE_PARENT_DIR_COMMAND)) {

		}
	}

	private void createNewProject() {

		File projFile = chooser.getSelectedFile();
		if(projFile == null)
			return;

		String pdesc = descriptionTextArea.getText().trim();
		activeExperiment = idTrackerExperimentsTable.getSelectedExperiment();
		if(activeExperiment == null) {
			MessageDialog.showErrorMsg("No IDTracker experiment selected", this);
			return;
		}
		MRC2ToolBoxCore.getMainWindow().
			createNewProjectFromIDTrackerExperiment(projFile, pdesc, activeExperiment);
		dispose();
	}

	@Override
	public void setVisible(boolean visible) {

		if(visible) {
			chooser.rescanCurrentDirectory();
			File f = new File("");
			File [] files = {f};
			chooser.setSelectedFile(f);
			chooser.setSelectedFiles(files);

			if(activeExperiment != null) {

				Path path = Paths.get(chooser.getCurrentDirectory().getPath(),
						activeExperiment.getId() + " - " + activeExperiment.getName());
				File projectFile = path.toFile();
				chooser.setSelectedFile(projectFile);
			}
		}
		super.setVisible(visible);
	}

	public String getProjectDescription() {
		return descriptionTextArea.getText().trim();
	}

	public File getProjectParentDirectory() {

		File projectParentDir = chooser.getSelectedFile();

		if (chooser.getSelectedFile() == null)
			projectParentDir = baseDirectory;

		return projectParentDir;
	}

	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setPreferredSize(new Dimension(800, 640));
		chooser.setBorder(null);
		chooser.addActionListener(this);
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setApproveButtonText("Create new project");
		//	chooser.setControlButtonsAreShown(false);
		chooser.getActionMap().get("viewTypeDetails").actionPerformed(null);
		baseDirectory = new File(MRC2ToolBoxConfiguration.getDefaultProjectsDirectory());
		chooser.setCurrentDirectory(baseDirectory);
		chooser.setSelectedFile(new File(""));
	}

	public void setProjectType(ProjectType type) {

		projectType = type;

		if(projectType.equals(ProjectType.DATA_ANALYSIS)) {

			setTitle("Create new data analysis project");
			setIconImage(((ImageIcon) newProjectIcon).getImage());
		}
		if(projectType.equals(ProjectType.FEATURE_IDENTIFICATION)) {

			setTitle("Create new feature identification project");
			setIconImage(((ImageIcon) newIdProjectIcon).getImage());
		}
	}

	/**
	 * @return the projectType
	 */
	public ProjectType getProjectType() {
		return projectType;
	}

	/**
	 * @return the design
	 */
	public ExperimentDesign getDesign() {
		return design;
	}

	/**
	 * @param design the design to set
	 */
	public void setDesign(ExperimentDesign design) {
		this.design = design;
	}

	public void setLimsExperiment(LIMSExperiment activeExperiment) {

		this.activeExperiment = activeExperiment;
		if(activeExperiment != null) {
			descriptionTextArea.setText(activeExperiment.getDescription() + "\n" + activeExperiment.getNotes());

//			Path path = Paths.get(chooser.getCurrentDirectory().getPath() +
//					File.separator + activeExperiment.getId() + " - " + activeExperiment.getName());
//			chooser.setSelectedFile(path.toFile());
		}
	}

	public LIMSExperiment getLimsExperiment() {
		return activeExperiment;
	}
}
































