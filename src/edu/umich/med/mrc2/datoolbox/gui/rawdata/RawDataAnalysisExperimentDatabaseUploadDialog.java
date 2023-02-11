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

package edu.umich.med.mrc2.datoolbox.gui.rawdata;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.rawdata.MSMSExtractionParameterSet;

public class RawDataAnalysisExperimentDatabaseUploadDialog 
		extends JDialog implements BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1951718448449783609L;
	
	private static final Icon sendexperimentToDatabaseIcon = GuiUtils.getIcon("xml2Database", 32);
	
	private Preferences preferences;
	private static final String PREFERENCES_NODE = "RawDataAnalysisexperimentDatabaseUploadDialog";
	private static final String MS_ONE_MZ_WINDOW = "MS_ONE_MZ_WINDOW";
	
	private JFormattedTextField msOneMzWindowTextField;
	private JTextField methodNameTextField;
	private JTextArea descriptionTextArea;
	private MSMSExtractionParameterSet ps;
	private DataExtractionMethod deMethod;
	
	public RawDataAnalysisExperimentDatabaseUploadDialog(RawDataExaminerPanel parentPanel) {
		super();
		setTitle("Upload raw data experiment as new MetIDTracker experiment");
		setIconImage(((ImageIcon) sendexperimentToDatabaseIcon).getImage());
		setResizable(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setPreferredSize(new Dimension(640, 320));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel_2 = new JLabel("Data analysis method name");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 0;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		methodNameTextField = new JTextField();
		GridBagConstraints gbc_methodNameTextField = new GridBagConstraints();
		gbc_methodNameTextField.gridwidth = 3;
		gbc_methodNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_methodNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_methodNameTextField.gridx = 0;
		gbc_methodNameTextField.gridy = 1;
		panel.add(methodNameTextField, gbc_methodNameTextField);
		methodNameTextField.setColumns(10);
		
		JLabel lblNewLabel_3 = new JLabel("Description");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 2;
		panel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		descriptionTextArea = new JTextArea();
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		descriptionTextArea.setBorder(new BevelBorder(
				BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_descriptionTextArea = new GridBagConstraints();
		gbc_descriptionTextArea.gridwidth = 3;
		gbc_descriptionTextArea.insets = new Insets(0, 0, 5, 0);
		gbc_descriptionTextArea.fill = GridBagConstraints.BOTH;
		gbc_descriptionTextArea.gridx = 0;
		gbc_descriptionTextArea.gridy = 3;
		panel.add(descriptionTextArea, gbc_descriptionTextArea);
		
		JLabel lblNewLabel = new JLabel("MS1 extraction window for database upload: +/-");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 4;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		msOneMzWindowTextField = 
				new JFormattedTextField(new DecimalFormat("###.#"));
		msOneMzWindowTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 0, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 4;
		panel.add(msOneMzWindowTextField, gbc_formattedTextField);
		
		JLabel lblNewLabel_1 = new JLabel("Da around parent ion");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 4;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		JButton cancelButton = new JButton("Cancel");
		panel_1.add(cancelButton);

		JButton uploadButton = 
				new JButton(MainActionCommands.SEND_EXPERIMENT_DATA_TO_DATABASE_COMMAND.getName());
		uploadButton.setActionCommand(
				MainActionCommands.SEND_EXPERIMENT_DATA_TO_DATABASE_COMMAND.getName());
		uploadButton.addActionListener(parentPanel);
		panel_1.add(uploadButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(uploadButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(uploadButton);
		loadPreferences();
		pack();
	}
	
	@Override
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}
	
	public void setMSMSExtractionParameterSet(MSMSExtractionParameterSet ps) {
		
		this.ps = ps;
		if(ps.getName() != null) {
			methodNameTextField.setText(ps.getName());
			methodNameTextField.setEditable(false);
		}
		
		if(ps.getDescription() != null) {
			descriptionTextArea.setText(ps.getDescription());
			descriptionTextArea.setEditable(false);
		}
	}
	
	public MSMSExtractionParameterSet getMSMSExtractionParameterSet() {
		
		if(ps == null)
			return null;
		
		if(ps.getName() == null || ps.getName().isEmpty())
			ps.setName(getMethodName());
		
		if(ps.getDescription() == null || ps.getDescription().isEmpty())
			ps.setDescription(getMethodDescription());
		
		return ps;
	}	

	public DataExtractionMethod getDataExtractionMethod() {
		return deMethod;
	}

	public void setDataExtractionMethod(DataExtractionMethod deMethod, boolean allowEdit) {
		this.deMethod = deMethod;
		methodNameTextField.setText(deMethod.getName());
		methodNameTextField.setEditable(allowEdit);
		methodNameTextField.setEnabled(allowEdit);
		descriptionTextArea.setText(deMethod.getDescription());
		descriptionTextArea.setEditable(allowEdit);
		descriptionTextArea.setEnabled(allowEdit);
	}
	
	public boolean isMethodEditable() {
		return methodNameTextField.isEditable();
	}
	
	public String getMethodName() {
		return methodNameTextField.getText().trim();
	}
	
	public String getMethodDescription() {
		return descriptionTextArea.getText().trim();
	}
	
	public double getMsOneMZWindow() {
		double msOneMZWindow = 
				Double.parseDouble(msOneMzWindowTextField.getText().trim());
		return msOneMZWindow;
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		double msOneMZWindow = preferences.getDouble(MS_ONE_MZ_WINDOW, 10.0d);
		msOneMzWindowTextField.setText(Double.toString(msOneMZWindow));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFERENCES_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFERENCES_NODE);
		double msOneMZWindow = Double.parseDouble(msOneMzWindowTextField.getText().trim());
		preferences.putDouble(MS_ONE_MZ_WINDOW, msOneMZWindow);
	}
}
