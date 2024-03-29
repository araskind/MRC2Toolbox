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

package edu.umich.med.mrc2.datoolbox.gui.io;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.ProjectType;

public class NewMetabolomicsExperimentDialog extends JDialog 
	implements ActionListener, BackedByPreferences {

	/**
	 *
	 */
	private static final long serialVersionUID = 3164807824653540814L;
	public static final String CHOOSE_PARENT_DIR_COMMAND = "CHOOSE_PARENT_DIR";
	
	private Preferences preferences;
	public static final String BASE_DIR = "BASE_DIR";
	private File baseDirectory;
	
	private JTextArea descriptionTextArea;
	private ProjectType projectType;
	private LIMSExperiment activeExperiment;

	private static final Icon newProjectIcon = GuiUtils.getIcon("newProject", 32);
	private static final Icon newFormatProjectIcon = GuiUtils.getIcon("newXmlProject", 32);
	private JTextField experimentDirectoryLocationTextField;
	private JTextField projectNameTextField;

	public NewMetabolomicsExperimentDialog(
			ActionListener listener, 
			ProjectType projectType) {

		super();
		setPreferredSize(new Dimension(640, 300));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel projectPanel = new JPanel();
		projectPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(projectPanel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 519, 124, 0 };
		gbl_panel.rowHeights = new int[] { 0, 29, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		projectPanel.setLayout(gbl_panel);

		JLabel lblNewLabel = 
				new JLabel("Choose location for the project:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		projectPanel.add(lblNewLabel, gbc_lblNewLabel);
		
		experimentDirectoryLocationTextField = new JTextField();
		experimentDirectoryLocationTextField.setEditable(false);
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.anchor = GridBagConstraints.NORTH;
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 0;
		gbc_textField.gridy = 1;
		projectPanel.add(experimentDirectoryLocationTextField, gbc_textField);
		
		JButton browseButton = new JButton("Browse");
		browseButton.setActionCommand(CHOOSE_PARENT_DIR_COMMAND);
		browseButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 1;
		projectPanel.add(browseButton, gbc_btnNewButton);
		
		JLabel lblNewLabel_1 = new JLabel("Experiment name");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 2;
		projectPanel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		projectNameTextField = new JTextField();
		GridBagConstraints gbc_projectNametextField = new GridBagConstraints();
		gbc_projectNametextField.gridwidth = 2;
		gbc_projectNametextField.insets = new Insets(0, 0, 5, 0);
		gbc_projectNametextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_projectNametextField.gridx = 0;
		gbc_projectNametextField.gridy = 3;
		projectPanel.add(projectNameTextField, gbc_projectNametextField);
		projectNameTextField.setColumns(10);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), 
						"Experiment description (optional)",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 2;
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 4;
		projectPanel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{124, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		descriptionTextArea = new JTextArea();
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.setWrapStyleWord(true);
		GridBagConstraints gbc_descriptionTextArea = new GridBagConstraints();
		gbc_descriptionTextArea.fill = GridBagConstraints.BOTH;
		gbc_descriptionTextArea.gridx = 0;
		gbc_descriptionTextArea.gridy = 0;
		panel_1.add(descriptionTextArea, gbc_descriptionTextArea);

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
		
		this.projectType = projectType;
		String command = "";
		if(projectType.equals(ProjectType.DATA_ANALYSIS)) {

			setTitle("Create new data analysis experiment");
			setIconImage(((ImageIcon) newProjectIcon).getImage());
			command = MainActionCommands.CREATE_NEW_METABOLOMICS_EXPERIMENT_COMMAND.getName();
		}
		if(projectType.equals(ProjectType.DATA_ANALYSIS_NEW_FORMAT)) {

			setTitle("Create new data analysis experiment (new format)");
			setIconImage(((ImageIcon) newFormatProjectIcon).getImage());
			command = MainActionCommands.CREATE_NEW_METABOLOMICS_XML_EXPERIMENT_COMMAND.getName();
		}
		JButton btnSave = new JButton(command);
		btnSave.setActionCommand(command);
		btnSave.addActionListener(listener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		loadPreferences();
		pack();
	}
	
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		
		if (command.equals(CHOOSE_PARENT_DIR_COMMAND))
			setProjectLocation();
	}

	private void setProjectLocation() {

		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Directories);
		fc.setTitle("Select project location:");
		fc.setMultiSelectionEnabled(false);
		fc.setSaveButtonText("Set project location");
		if (fc.showOpenDialog(this)) {
			
			baseDirectory = fc.getSelectedFile();
			experimentDirectoryLocationTextField.setText(baseDirectory.getAbsolutePath());
			savePreferences();
		}
	}

	public void setLimsExperiment(LIMSExperiment activeExperiment) {

		this.activeExperiment = activeExperiment;
		if(activeExperiment != null) {
			
			String projectName = 
					activeExperiment.getId() + " - " + activeExperiment.getName();
			projectNameTextField.setText(projectName);
			
			String description = "";
			if(activeExperiment.getDescription() != null)
				description += activeExperiment.getDescription();
			
			if(activeExperiment.getNotes() != null)
				description += activeExperiment.getNotes();
			
			descriptionTextArea.setText(description);
		}
	}	
	
	public ProjectType getProjectType() {
		return projectType;
	}
	
	public String getProjectName() {
		return projectNameTextField.getText().trim();
	}
	
	public String getProjectDescription() {
		return descriptionTextArea.getText().trim();
	}
	
	public LIMSExperiment getLimsExperiment() {
		return activeExperiment;
	}
	
	public File getProjectParentFolder() {
		return baseDirectory;
	}
	
	public Collection<String>validateExperimentData(){
		
		Collection<String>errors = new ArrayList<String>();
		
		if(projectType == null)
			errors.add("Project type not specified.");

		if(getProjectName().isEmpty())
			errors.add("Project name cannot be empty.");
		
		if(baseDirectory == null || !baseDirectory.exists())
			errors.add("Invalid project directory.");
		
		if(!getProjectName().isEmpty() && baseDirectory != null) {
			
			File newProjectDir = 
					Paths.get(baseDirectory.getAbsolutePath(), getProjectName()).toFile();
			if(newProjectDir.exists()) {
				errors.add("Project \"" + getProjectName() + "\" already exists\n"
						+ "in the directory \"" + baseDirectory.getAbsolutePath() + "\"");
			}
		}		
		return errors;
	}
	
	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		baseDirectory =
				new File(preferences.get(BASE_DIR, 
						MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory())).getAbsoluteFile();		
		experimentDirectoryLocationTextField.setText(baseDirectory.getAbsolutePath());
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(BASE_DIR, baseDirectory.getAbsolutePath());
	}
}
































