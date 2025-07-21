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

package edu.umich.med.mrc2.datoolbox.gui.integration;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DataIntegrationSetupDialog extends JDialog {
	
	private JButton cancelButton, integrateButton;
	private JTextField featureSetNameTextField;
	private DataPipelineSelectionTable assaySelectionTable;

	public DataIntegrationSetupDialog(ActionListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "Data integration parameters");
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(400, 300));
		setPreferredSize(new Dimension(400, 300));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 5, 5));
		panel.add(panel_1, BorderLayout.NORTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{46, 86, 0};
		gbl_panel_1.rowHeights = new int[]{20, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel = new JLabel("Name ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		featureSetNameTextField = new JTextField();
		GridBagConstraints gbc_featureSetNameTextField = new GridBagConstraints();
		gbc_featureSetNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_featureSetNameTextField.anchor = GridBagConstraints.NORTH;
		gbc_featureSetNameTextField.gridx = 1;
		gbc_featureSetNameTextField.gridy = 0;
		panel_1.add(featureSetNameTextField, gbc_featureSetNameTextField);
		featureSetNameTextField.setColumns(10);
		
		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		panel.add(panel_2, BorderLayout.SOUTH);
		
		cancelButton = new JButton("Cancel");
		panel_2.add(cancelButton);
		
		integrateButton = new JButton("Integrate data");
		integrateButton.setActionCommand(MainActionCommands.COLLECT_IDENTIFIED_CPD_COMMAND.getName());
		integrateButton.addActionListener(listener);
		panel_2.add(integrateButton);
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		cancelButton.addActionListener(al);

		JRootPane rootPane = SwingUtilities.getRootPane(integrateButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(integrateButton);
		
		JPanel panel_3 = new JPanel(new BorderLayout(0, 0));
		panel_3.setBorder(new TitledBorder(null, 
				"Select assays to include in data integration", 
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_3, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(null);
		panel_3.add(scrollPane, BorderLayout.CENTER);
		assaySelectionTable = new DataPipelineSelectionTable(); 				
		scrollPane.add(assaySelectionTable);
		scrollPane.setViewportView(assaySelectionTable);
		scrollPane.setPreferredSize(assaySelectionTable.getPreferredScrollableViewportSize());
		
		pack();
		setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		setVisible(false);
	}
	
	public void setDataSetName(String name) {
		
		featureSetNameTextField.setText(name);
	}
	
	@Override
	public void setVisible(boolean visible) {
		
		if(visible) {
			assaySelectionTable.setTableModelFromExperiment(
					MRC2ToolBoxCore.getActiveMetabolomicsExperiment());
			setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		}			
		super.setVisible(visible);
	}
	
	public Collection<DataPipeline> getSelectedDataPipelines(){
		return assaySelectionTable.getCheckedDataPipelines();
	}
	
	public String getDataSetName() {
		
		return featureSetNameTextField.getText().trim();
	}
}
