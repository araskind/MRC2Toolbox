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

package edu.umich.med.mrc2.datoolbox.gui.mzfreq;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.expsetup.featurelist.SubsetFeaturesTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.utils.FIOUtils;
import edu.umich.med.mrc2.datoolbox.utils.MetabolomicsProjectUtils;

public class AddFeatureSubsetDialog extends JDialog implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Icon newSubsetIcon = GuiUtils.getIcon("newFeatureSubset", 32);
	
	private DataAnalysisProject experiment;
	private DataPipeline pipeline;
	private Collection<MsFeature> selectedFeatures;
	private JTextField setNameField;
	private SubsetFeaturesTable featuresTable;

	public AddFeatureSubsetDialog(
			DataAnalysisProject experiment,
			DataPipeline pipeline, 
			Collection<MsFeature> selectedFeatures) {
		super();
		this.experiment = experiment;
		this.pipeline = pipeline;
		this.selectedFeatures = selectedFeatures;
		
		setTitle(MainActionCommands.CREATE_NEW_FEATURE_SUBSET_COMMAND.getName());
		setIconImage(((ImageIcon) newSubsetIcon).getImage());
		setPreferredSize(new Dimension(480, 640));
		setSize(new Dimension(480, 640));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		dataPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(
				new EmptyBorder(10, 5, 10, 5), new TitledBorder(
						new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
								new Color(160, 160, 160)), "Feature set name", 
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		dataPanel.add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		setNameField = new JTextField();
		setNameField.setText("Repeated feature subset for " 
				+ pipeline.getName() + "; " + FIOUtils.getTimestamp());

		GridBagConstraints gbc_setNameField = new GridBagConstraints();
		gbc_setNameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_setNameField.gridx = 0;
		gbc_setNameField.gridy = 0;
		panel.add(setNameField, gbc_setNameField);
		setNameField.setColumns(10);
		
		featuresTable = new SubsetFeaturesTable();
		dataPanel.add(new JScrollPane(featuresTable), BorderLayout.CENTER);
		featuresTable.setTableModelFromFeatures(selectedFeatures);

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(
				MainActionCommands.CREATE_NEW_FEATURE_SUBSET_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.CREATE_NEW_FEATURE_SUBSET_COMMAND.getName());
		btnSave.addActionListener(this);
		buttonPanel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.CREATE_NEW_FEATURE_SUBSET_COMMAND.getName())) {
			createNewFeatureSubset();
		}		
	}
	
	private String getSubsetName() {
		return setNameField.getText().trim();
	}

	private void createNewFeatureSubset() {

		Collection<String>errors = validateFeatureSubsetData();
		if(!errors.isEmpty()){
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), this);
			return;
		}
		MsFeatureSet newSet = 
				new MsFeatureSet(getSubsetName(), selectedFeatures);
		experiment.addFeatureSetForDataPipeline(newSet, pipeline);
		MRC2ToolBoxCore.getMainWindow().getExperimentSetupDraw().
			getFeatureSubsetPanel().addSetListeners(newSet);	
		MetabolomicsProjectUtils.switchActiveMsFeatureSet(newSet);
		dispose();
	}
	
	public Collection<String> validateFeatureSubsetData() {

		Collection<String> errors = new ArrayList<>();

		String newName = getSubsetName();
		if (newName.isEmpty())
			errors.add("Feature subset name cannot be empty.");

		MsFeatureSet existing = experiment.getMsFeatureSetsForDataPipeline(pipeline).stream().
				filter(s -> s.getName().equalsIgnoreCase(newName)).findFirst().orElse(null);

		if (existing != null)
			errors.add("Feature subset \"" + newName + "\" already exists.");

		return errors;
	}
}
