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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.ValidatableForm;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class BinnerProcessingSetupDialog extends JDialog 
		implements ActionListener, BackedByPreferences, ValidatableForm{

	private static final long serialVersionUID = 1L;
	
	private static final Icon setupBinnerAnnotationsIcon = 
			GuiUtils.getIcon("setupBinnerAnnotations", 32);
	private Preferences preferences;
	
	private DataFileSelectionPanel dataFileSelectionPanel;
	private DataCleaningOptionsPanel dataCleaningOptionsPanel;
	private FeatureGroupingOptionsPanel featureGroupingOptionsPanel;
	private AnnotationsSelectorPanel annotationsSelectorPanel;
	
	private DataAnalysisProject currentExperiment;
	private DataPipeline activeDataPipeline;

	public BinnerProcessingSetupDialog(
			DataAnalysisProject currentExperiment, 
			DataPipeline activeDataPipeline,
			ActionListener listener) {

		super();
		setTitle("Binner processing setup");
		setIconImage(((ImageIcon) setupBinnerAnnotationsIcon).getImage());
		setSize(new Dimension(1000,800));
		setPreferredSize(new Dimension(1000,800));
		setModalityType(ModalityType.APPLICATION_MODAL);		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		this.currentExperiment = currentExperiment;
		this.activeDataPipeline = activeDataPipeline;
		
		JTabbedPane wrapperPanel = new JTabbedPane();
		wrapperPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(wrapperPanel, BorderLayout.CENTER);
		
		dataFileSelectionPanel = new DataFileSelectionPanel();
		dataFileSelectionPanel.setTableModelFromFileCollection(
				currentExperiment.getDataFilesForPipeline(activeDataPipeline, false));
		wrapperPanel.addTab("Select data files", null, dataFileSelectionPanel);
		
		dataCleaningOptionsPanel = new DataCleaningOptionsPanel();
		wrapperPanel.addTab("Data cleanup options", null, dataCleaningOptionsPanel);
		
		featureGroupingOptionsPanel = new FeatureGroupingOptionsPanel();
		wrapperPanel.addTab("Feature grouping options", null, featureGroupingOptionsPanel);
		
		annotationsSelectorPanel = new AnnotationsSelectorPanel();
		wrapperPanel.addTab("Annotations selector", null, annotationsSelectorPanel);
		
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

		JButton btnSave = new JButton(
				MainActionCommands.GENERATE_BINNER_ANNOTATIONS_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.GENERATE_BINNER_ANNOTATIONS_COMMAND.getName());
		btnSave.addActionListener(listener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		loadPreferences();
		pack();
	}
	
	@Override
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadPreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void savePreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Collection<String> validateFormData() {

		Collection<String>errors = new ArrayList<String>();
		
		
		return errors;
	}

}
