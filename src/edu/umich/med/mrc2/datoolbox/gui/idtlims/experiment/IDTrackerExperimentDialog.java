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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.experiment;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;

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

import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProject;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class IDTrackerExperimentDialog  extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = 8046770417831235265L;
	private static final Icon newCdpIdExperimentIcon = GuiUtils.getIcon("newIdExperiment", 32);

	private JButton saveButton;
	private IDTrackerLimsManagerPanel parentPanel;
	private IDTExperimentDefinitionPanel experimentDefinitionPanel;
	private LIMSExperiment experiment;

	public IDTrackerExperimentDialog(LIMSExperiment experiment, ActionListener actionListener) {

		super();
		this.experiment = experiment;
		setIconImage(((ImageIcon) newCdpIdExperimentIcon).getImage());
		setPreferredSize(new Dimension(640, 480));
		setSize(new Dimension(640, 480));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		parentPanel = (IDTrackerLimsManagerPanel)actionListener;
		experimentDefinitionPanel = new IDTExperimentDefinitionPanel(experiment);
		getContentPane().add(experimentDefinitionPanel, BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel_1.add(btnCancel);

		saveButton = new JButton("Save experiment details");
		saveButton.addActionListener(actionListener);
		panel_1.add(saveButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		btnCancel.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);

		loadExperimentData();
		pack();
	}

	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}
	
	private void loadExperimentData() {

		if(experimentDefinitionPanel.getExperiment() == null) {
			setTitle("Create new ID tracker experiment");
			saveButton.setActionCommand(MainActionCommands.NEW_IDTRACKER_EXPERIMENT_COMMAND.getName());
			experimentDefinitionPanel.setProject(parentPanel.getSelectedProject());
		}
		else {
			setTitle("Edit experiment \"" + experimentDefinitionPanel.getExperiment().getName() + "\"");
			saveButton.setActionCommand(MainActionCommands.SAVE_IDTRACKER_EXPERIMENT_COMMAND.getName());
		}
	}

	public LIMSExperiment getExperiment() {
		return experimentDefinitionPanel.getExperiment();
	}

	public LIMSProject getExperimentProject() {
		return experimentDefinitionPanel.getExperimentProject();
	}

	public LIMSInstrument getInstrument() {
		return experimentDefinitionPanel.getInstrument();
	}
	
	public String getExperimentName() {
		return experimentDefinitionPanel.getExperimentName();
	}

	public String getExperimentDescription() {
		return experimentDefinitionPanel.getExperimentDescription();
	}

	public String getExperimentNotes() {
		return experimentDefinitionPanel.getExperimentNotes();
	}
	
	public Collection<String>validateExperimentDefinition(){
		return experimentDefinitionPanel.validateExperimentDefinition();
	}
}





















