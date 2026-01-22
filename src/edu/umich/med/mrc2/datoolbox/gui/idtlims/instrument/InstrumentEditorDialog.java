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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.instrument;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.MassAnalyzerType;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicSeparationType;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSInstrument;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;

public class InstrumentEditorDialog extends JDialog{

	/**
	 *
	 */
	private static final long serialVersionUID = 7684989595475342241L;
	
	private static final Icon editMethodIcon = GuiUtils.getIcon("editInstrument", 32);
	private static final Icon addMethodIcon = GuiUtils.getIcon("addInstrument", 32);

	private LIMSInstrument instrument;
	private JButton btnSave;
	private JTextField instrumentNameTextField;
	private JLabel idValueLabel;
	private JTextArea descriptionTextArea;
	private JTextField manufacturerTextField;
	private JComboBox separationTypeComboBox;
	private JTextField serialNumTextField;
	private JComboBox massAnalyzerComboBox;
	private JTextField instrumentModelTextField;


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public InstrumentEditorDialog(LIMSInstrument instrument, ActionListener actionListener) {
		super();
		this.instrument = instrument;

		setPreferredSize(new Dimension(600, 350));
		setSize(new Dimension(600, 350));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		GridBagLayout gbl_dataPanel = new GridBagLayout();
		gbl_dataPanel.columnWidths = new int[]{0, 137, 82, 144, 0, 0};
		gbl_dataPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_dataPanel.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_dataPanel.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		dataPanel.setLayout(gbl_dataPanel);

		JLabel lblId = new JLabel("ID");
		lblId.setForeground(Color.BLUE);
		lblId.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblId = new GridBagConstraints();
		gbc_lblId.anchor = GridBagConstraints.EAST;
		gbc_lblId.insets = new Insets(0, 0, 5, 5);
		gbc_lblId.gridx = 0;
		gbc_lblId.gridy = 0;
		dataPanel.add(lblId, gbc_lblId);

		idValueLabel = new JLabel("");
		GridBagConstraints gbc_idValueLabel = new GridBagConstraints();
		gbc_idValueLabel.anchor = GridBagConstraints.WEST;
		gbc_idValueLabel.insets = new Insets(0, 0, 5, 5);
		gbc_idValueLabel.gridx = 1;
		gbc_idValueLabel.gridy = 0;
		dataPanel.add(idValueLabel, gbc_idValueLabel);

		JLabel lblName = new JLabel("Name");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		dataPanel.add(lblName, gbc_lblName);

		instrumentNameTextField = new JTextField();
		GridBagConstraints gbc_methodNameTextField = new GridBagConstraints();
		gbc_methodNameTextField.gridwidth = 4;
		gbc_methodNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_methodNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_methodNameTextField.gridx = 1;
		gbc_methodNameTextField.gridy = 1;
		dataPanel.add(instrumentNameTextField, gbc_methodNameTextField);
		instrumentNameTextField.setColumns(10);

		JLabel lblDescription = new JLabel("Description");
		GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.anchor = GridBagConstraints.NORTH;
		gbc_lblDescription.insets = new Insets(0, 0, 5, 5);
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 2;
		dataPanel.add(lblDescription, gbc_lblDescription);

		descriptionTextArea = new JTextArea();
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridwidth = 4;
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 1;
		gbc_textArea.gridy = 2;
		dataPanel.add(descriptionTextArea, gbc_textArea);
		
		JLabel lblModel = new JLabel("Model");
		GridBagConstraints gbc_lblModel = new GridBagConstraints();
		gbc_lblModel.anchor = GridBagConstraints.EAST;
		gbc_lblModel.insets = new Insets(0, 0, 5, 5);
		gbc_lblModel.gridx = 0;
		gbc_lblModel.gridy = 3;
		dataPanel.add(lblModel, gbc_lblModel);
		
		instrumentModelTextField = new JTextField();
		GridBagConstraints gbc_instrumentModelTextField = new GridBagConstraints();
		gbc_instrumentModelTextField.gridwidth = 4;
		gbc_instrumentModelTextField.insets = new Insets(0, 0, 5, 5);
		gbc_instrumentModelTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_instrumentModelTextField.gridx = 1;
		gbc_instrumentModelTextField.gridy = 3;
		dataPanel.add(instrumentModelTextField, gbc_instrumentModelTextField);
		instrumentModelTextField.setColumns(10);

		JLabel lblPolarity = new JLabel("Manufacturer");
		GridBagConstraints gbc_lblPolarity = new GridBagConstraints();
		gbc_lblPolarity.anchor = GridBagConstraints.EAST;
		gbc_lblPolarity.insets = new Insets(0, 0, 5, 5);
		gbc_lblPolarity.gridx = 0;
		gbc_lblPolarity.gridy = 4;
		dataPanel.add(lblPolarity, gbc_lblPolarity);

		manufacturerTextField =
			new JTextField();
		GridBagConstraints gbc_manufacturerTextField = new GridBagConstraints();
		gbc_manufacturerTextField.insets = new Insets(0, 0, 5, 5);
		gbc_manufacturerTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_manufacturerTextField.gridx = 1;
		gbc_manufacturerTextField.gridy = 4;
		dataPanel.add(manufacturerTextField, gbc_manufacturerTextField);

		JLabel lblMsType = new JLabel("Separation type");
		GridBagConstraints gbc_lblMsType = new GridBagConstraints();
		gbc_lblMsType.anchor = GridBagConstraints.EAST;
		gbc_lblMsType.insets = new Insets(0, 0, 5, 5);
		gbc_lblMsType.gridx = 2;
		gbc_lblMsType.gridy = 4;
		dataPanel.add(lblMsType, gbc_lblMsType);

		separationTypeComboBox = new JComboBox(
				new SortedComboBoxModel<ChromatographicSeparationType>(IDTDataCache.getChromatographicSeparationTypes()));
		GridBagConstraints gbc_msTypeComboBox = new GridBagConstraints();
		gbc_msTypeComboBox.gridwidth = 2;
		gbc_msTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_msTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_msTypeComboBox.gridx = 3;
		gbc_msTypeComboBox.gridy = 4;
		dataPanel.add(separationTypeComboBox, gbc_msTypeComboBox);

		JLabel lblIonization = new JLabel("Serial #");
		GridBagConstraints gbc_lblIonization = new GridBagConstraints();
		gbc_lblIonization.anchor = GridBagConstraints.EAST;
		gbc_lblIonization.insets = new Insets(0, 0, 0, 5);
		gbc_lblIonization.gridx = 0;
		gbc_lblIonization.gridy = 5;
		dataPanel.add(lblIonization, gbc_lblIonization);

		serialNumTextField =
				new JTextField();
		GridBagConstraints gbc_serialNumTextField = new GridBagConstraints();
		gbc_serialNumTextField.insets = new Insets(0, 0, 0, 5);
		gbc_serialNumTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_serialNumTextField.gridx = 1;
		gbc_serialNumTextField.gridy = 5;
		dataPanel.add(serialNumTextField, gbc_serialNumTextField);

		JLabel lblMassAnalyzer = new JLabel("Mass analyzer");
		GridBagConstraints gbc_lblMassAnalyzer = new GridBagConstraints();
		gbc_lblMassAnalyzer.anchor = GridBagConstraints.EAST;
		gbc_lblMassAnalyzer.insets = new Insets(0, 0, 0, 5);
		gbc_lblMassAnalyzer.gridx = 2;
		gbc_lblMassAnalyzer.gridy = 5;
		dataPanel.add(lblMassAnalyzer, gbc_lblMassAnalyzer);

		massAnalyzerComboBox =
				new JComboBox(new SortedComboBoxModel<MassAnalyzerType>(IDTDataCache.getMassAnalyzerTypes()));
		GridBagConstraints gbc_massAnalyzerComboBox = new GridBagConstraints();
		gbc_massAnalyzerComboBox.gridwidth = 2;
		gbc_massAnalyzerComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_massAnalyzerComboBox.gridx = 3;
		gbc_massAnalyzerComboBox.gridy = 5;
		dataPanel.add(massAnalyzerComboBox, gbc_massAnalyzerComboBox);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		btnCancel.addActionListener(al);

		btnSave = new JButton("Save");
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		loadInstrumentData();
		pack();
	}
	
	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}

	@SuppressWarnings("unchecked")
	private void loadInstrumentData() {
		if(instrument == null) {

			setTitle("Add new instrument");
			setIconImage(((ImageIcon) addMethodIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.ADD_INSTRUMENT_COMMAND.getName());
			massAnalyzerComboBox.setSelectedIndex(-1);
			separationTypeComboBox.setSelectedIndex(-1);
		}
		else {
			setTitle("Edit information for " + instrument.toString());
			setIconImage(((ImageIcon) editMethodIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.EDIT_INSTRUMENT_COMMAND.getName());
			idValueLabel.setText(instrument.getInstrumentId());
			instrumentNameTextField.setText(instrument.getInstrumentName());
			descriptionTextArea.setText(instrument.getDescription());
			instrumentModelTextField.setText(instrument.getModel());
			manufacturerTextField.setText(instrument.getManufacturer());
			serialNumTextField.setText(instrument.getSerialNumber());
			massAnalyzerComboBox.setSelectedItem(instrument.getMassAnalyzerType());
			separationTypeComboBox.setSelectedItem(instrument.getChromatographicSeparationType());
		}
	}

	public LIMSInstrument getInstrument() {
		return instrument;
	}

	public String getInstrumentName() {
		return instrumentNameTextField.getText().trim();
	}

	public String getInstrumentDescription() {
		return descriptionTextArea.getText().trim();
	}
	
	public String getInstrumentModel() {
		return instrumentModelTextField.getText().trim();
	}

	public String getInstrumentManufacturer() {
		return manufacturerTextField.getText().trim();
	}
	
	public String getSerialNumber() {
		return serialNumTextField.getText().trim();
	}

	public MassAnalyzerType getMassAnalyzerType() {
		return (MassAnalyzerType)massAnalyzerComboBox.getSelectedItem();
	}

	public ChromatographicSeparationType getChromatographicSeparationType() {
		return (ChromatographicSeparationType)separationTypeComboBox.getSelectedItem();
	}
}








