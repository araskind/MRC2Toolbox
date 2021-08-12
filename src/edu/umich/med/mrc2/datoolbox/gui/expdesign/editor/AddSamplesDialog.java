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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class AddSamplesDialog extends JDialog implements BackedByPreferences, ActionListener{

	/**
	 *
	 */
	private static final long serialVersionUID = -968012229358345143L;
	private static final Icon addsampleIcon = GuiUtils.getIcon("addSample", 32);
	private Collection<ExperimentalSample>newSamples;
	private JTextField idPrefixTextField;
	private JTextField sampleNamePrefixTextField;
	private JSpinner idDigitsSpinner;
	private JSpinner idStartSpinner;
	private JSpinner numSamplesSpinner;
	private JButton cancelButton;
	private JButton addSamplesButton;
	private Preferences preferences;

	public static final String SAMPLE_ID_PREFIX = "SAMPLE_ID_PREFIX";
	public static final String SAMPLE_ID_DIGITS_NUMBER = "SAMPLE_ID_DIGITS_NUMBER";
	public static final String SAMPLE_NAME_PREFIX = "SAMPLE_NAME_PREFIX";

	public AddSamplesDialog() {

		super();
		setModal(true);
		setTitle("Add experimental samples");
		setIconImage(((ImageIcon) addsampleIcon).getImage());
		setSize(new Dimension(300, 220));
		setPreferredSize(new Dimension(300, 220));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblSampleIdPrefix = new JLabel("Sample ID prefix");
		GridBagConstraints gbc_lblSampleIdPrefix = new GridBagConstraints();
		gbc_lblSampleIdPrefix.insets = new Insets(0, 0, 5, 5);
		gbc_lblSampleIdPrefix.anchor = GridBagConstraints.EAST;
		gbc_lblSampleIdPrefix.gridx = 0;
		gbc_lblSampleIdPrefix.gridy = 0;
		panel.add(lblSampleIdPrefix, gbc_lblSampleIdPrefix);

		idPrefixTextField = new JTextField();
		GridBagConstraints gbc_idPrefixTextField = new GridBagConstraints();
		gbc_idPrefixTextField.insets = new Insets(0, 0, 5, 0);
		gbc_idPrefixTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_idPrefixTextField.gridx = 1;
		gbc_idPrefixTextField.gridy = 0;
		panel.add(idPrefixTextField, gbc_idPrefixTextField);
		idPrefixTextField.setColumns(10);

		JLabel lblIdCounterStart = new JLabel("ID counter start");
		GridBagConstraints gbc_lblIdCounterStart = new GridBagConstraints();
		gbc_lblIdCounterStart.anchor = GridBagConstraints.EAST;
		gbc_lblIdCounterStart.insets = new Insets(0, 0, 5, 5);
		gbc_lblIdCounterStart.gridx = 0;
		gbc_lblIdCounterStart.gridy = 1;
		panel.add(lblIdCounterStart, gbc_lblIdCounterStart);

		idStartSpinner = new JSpinner();
		idStartSpinner.setModel(new SpinnerNumberModel(1, 1, null, 1));
		idStartSpinner.setPreferredSize(new Dimension(60, 20));
		GridBagConstraints gbc_idStartSpinner = new GridBagConstraints();
		gbc_idStartSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_idStartSpinner.anchor = GridBagConstraints.WEST;
		gbc_idStartSpinner.gridx = 1;
		gbc_idStartSpinner.gridy = 1;
		panel.add(idStartSpinner, gbc_idStartSpinner);

		JLabel lblIdCounterDigitt = new JLabel("ID counter digit #");
		GridBagConstraints gbc_lblIdCounterDigitt = new GridBagConstraints();
		gbc_lblIdCounterDigitt.anchor = GridBagConstraints.EAST;
		gbc_lblIdCounterDigitt.insets = new Insets(0, 0, 5, 5);
		gbc_lblIdCounterDigitt.gridx = 0;
		gbc_lblIdCounterDigitt.gridy = 2;
		panel.add(lblIdCounterDigitt, gbc_lblIdCounterDigitt);

		idDigitsSpinner = new JSpinner();
		idDigitsSpinner.setModel(new SpinnerNumberModel(3, 2, null, 1));
		idDigitsSpinner.setPreferredSize(new Dimension(60, 20));
		idDigitsSpinner.setMinimumSize(new Dimension(60, 20));
		GridBagConstraints gbc_idDigitsSpinner = new GridBagConstraints();
		gbc_idDigitsSpinner.anchor = GridBagConstraints.WEST;
		gbc_idDigitsSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_idDigitsSpinner.gridx = 1;
		gbc_idDigitsSpinner.gridy = 2;
		panel.add(idDigitsSpinner, gbc_idDigitsSpinner);

		JLabel lblOfSamples = new JLabel("# of samples to add");
		GridBagConstraints gbc_lblOfSamples = new GridBagConstraints();
		gbc_lblOfSamples.anchor = GridBagConstraints.EAST;
		gbc_lblOfSamples.insets = new Insets(0, 0, 5, 5);
		gbc_lblOfSamples.gridx = 0;
		gbc_lblOfSamples.gridy = 3;
		panel.add(lblOfSamples, gbc_lblOfSamples);

		numSamplesSpinner = new JSpinner();
		numSamplesSpinner.setModel(new SpinnerNumberModel(1, 1, null, 1));
		numSamplesSpinner.setPreferredSize(new Dimension(60, 20));
		GridBagConstraints gbc_numSamplesSpinner = new GridBagConstraints();
		gbc_numSamplesSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_numSamplesSpinner.anchor = GridBagConstraints.WEST;
		gbc_numSamplesSpinner.gridx = 1;
		gbc_numSamplesSpinner.gridy = 3;
		panel.add(numSamplesSpinner, gbc_numSamplesSpinner);

		JLabel lblSampleNamePrefix = new JLabel("Sample name prefix");
		GridBagConstraints gbc_lblSampleNamePrefix = new GridBagConstraints();
		gbc_lblSampleNamePrefix.anchor = GridBagConstraints.EAST;
		gbc_lblSampleNamePrefix.insets = new Insets(0, 0, 0, 5);
		gbc_lblSampleNamePrefix.gridx = 0;
		gbc_lblSampleNamePrefix.gridy = 4;
		panel.add(lblSampleNamePrefix, gbc_lblSampleNamePrefix);

		sampleNamePrefixTextField = new JTextField();
		GridBagConstraints gbc_sampleNamePrefixTextField = new GridBagConstraints();
		gbc_sampleNamePrefixTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_sampleNamePrefixTextField.gridx = 1;
		gbc_sampleNamePrefixTextField.gridy = 4;
		panel.add(sampleNamePrefixTextField, gbc_sampleNamePrefixTextField);
		sampleNamePrefixTextField.setColumns(10);

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		cancelButton = new JButton("Cancel");
		panel_1.add(cancelButton);

		addSamplesButton = new JButton(MainActionCommands.ADD_SAMPLE_COMMAND.getName());
		addSamplesButton.setActionCommand(MainActionCommands.ADD_SAMPLE_COMMAND.getName());
		addSamplesButton.addActionListener(this);
		panel_1.add(addSamplesButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(addSamplesButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(addSamplesButton);

		pack();
		loadPreferences();
		newSamples = new TreeSet<ExperimentalSample>();
	}

	public Collection<ExperimentalSample>getNewSamples(){
		return newSamples;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.ADD_SAMPLE_COMMAND.getName())) {

			Collection<String>errors = addSamples();
			if(errors.isEmpty()) {
				savePreferences();
				dispose();
			}
			else {
				MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			}
		}
	}

	private Collection<String> addSamples() {

		ArrayList<String>errors = new ArrayList<String>();
		ArrayList<ExperimentalSample>samplesToAdd = new ArrayList<ExperimentalSample>();
		DataAnalysisProject project = MRC2ToolBoxCore.getCurrentProject();
		if(project == null)
			return errors;

		ExperimentDesign design = project.getExperimentDesign();
		int numSamples = (int) numSamplesSpinner.getValue();
		int countStart = (int) idStartSpinner.getValue();
		int digits = (int) idDigitsSpinner.getValue();

		for(int i=countStart; i<(countStart + numSamples); i++) {

			String sampleId = idPrefixTextField.getText() + StringUtils.leftPad(Integer.toString(i), digits, '0');
			String sampleName = sampleNamePrefixTextField.getText() + " " + Integer.toString(i);
			ExperimentalSample newSample = new ExperimentalSample(sampleId, sampleName);
			if(design.getSampleById(sampleId) == null)
				samplesToAdd.add(newSample);
			else
				errors.add("Sample " + sampleId + " already exists.");
		}
		if(errors.isEmpty()) {

			design.setSuppressEvents(true);
			samplesToAdd.stream().forEach(s -> design.addSample(s));
			design.setSuppressEvents(false);
			design.fireExperimentDesignEvent(ParameterSetStatus.CHANGED);
		}
		return errors;
	}

	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;
		idPrefixTextField.setText(preferences.get(SAMPLE_ID_PREFIX, "S"));
		sampleNamePrefixTextField.setText(preferences.get(SAMPLE_NAME_PREFIX, "Sample"));
		idDigitsSpinner.setValue(preferences.getInt(SAMPLE_ID_DIGITS_NUMBER, 8));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(SAMPLE_ID_PREFIX, idPrefixTextField.getText());
		preferences.put(SAMPLE_NAME_PREFIX, sampleNamePrefixTextField.getText());
		preferences.putInt(SAMPLE_ID_DIGITS_NUMBER, (int) idDigitsSpinner.getValue());
	}
}
























