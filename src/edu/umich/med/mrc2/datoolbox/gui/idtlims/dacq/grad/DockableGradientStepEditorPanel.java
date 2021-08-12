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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.grad;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableGradientStepEditorPanel extends DefaultSingleCDockable implements ActionListener{

	private static final Icon componentIcon = GuiUtils.getIcon("addSop", 16);
	
	private final String CLEAR_PANEL_COMMAND = "CLEAR_PANEL_COMMAND";	
	private JPanel gradientStepPanel;
	private JFormattedTextField endTimeFormattedTextField;
	private JFormattedTextField flowRateFormattedTextField;
	private JFormattedTextField filalPercentAformattedTextField;
	private JFormattedTextField filalPercentBformattedTextField;
	private JFormattedTextField filalPercentCformattedTextField;
	private JFormattedTextField filalPercentDformattedTextField;	
	private JButton btnClearPanel;
	private JButton btnAddStepToGradient;
	
	public DockableGradientStepEditorPanel(ActionListener listener) {

		super("DockableGradientStepEditorPanel", componentIcon, "Gradient step definition", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		
		setLayout(new BorderLayout(0,0));
		gradientStepPanel = new JPanel();
		gradientStepPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		add(new JScrollPane(gradientStepPanel), BorderLayout.CENTER);
		GridBagLayout gbl_gradientStepPanel = new GridBagLayout();
		gbl_gradientStepPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_gradientStepPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_gradientStepPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_gradientStepPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gradientStepPanel.setLayout(gbl_gradientStepPanel);
		
		JLabel lblEndTimeMin = new JLabel("End time, min");
		GridBagConstraints gbc_lblEndTimeMin = new GridBagConstraints();
		gbc_lblEndTimeMin.insets = new Insets(0, 0, 5, 5);
		gbc_lblEndTimeMin.anchor = GridBagConstraints.EAST;
		gbc_lblEndTimeMin.gridx = 0;
		gbc_lblEndTimeMin.gridy = 0;
		gradientStepPanel.add(lblEndTimeMin, gbc_lblEndTimeMin);
		
		endTimeFormattedTextField = new JFormattedTextField();
		endTimeFormattedTextField.setColumns(10);
		GridBagConstraints gbc_endTimeFormattedTextField = new GridBagConstraints();
		gbc_endTimeFormattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_endTimeFormattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_endTimeFormattedTextField.gridx = 1;
		gbc_endTimeFormattedTextField.gridy = 0;
		gradientStepPanel.add(endTimeFormattedTextField, gbc_endTimeFormattedTextField);
		
		JLabel lblFlowRateMlmin = new JLabel("Flow rate, ml/min");
		GridBagConstraints gbc_lblFlowRateMlmin = new GridBagConstraints();
		gbc_lblFlowRateMlmin.insets = new Insets(0, 0, 5, 5);
		gbc_lblFlowRateMlmin.anchor = GridBagConstraints.EAST;
		gbc_lblFlowRateMlmin.gridx = 3;
		gbc_lblFlowRateMlmin.gridy = 0;
		gradientStepPanel.add(lblFlowRateMlmin, gbc_lblFlowRateMlmin);
		
		flowRateFormattedTextField = new JFormattedTextField();
		flowRateFormattedTextField.setColumns(10);
		GridBagConstraints gbc_flowRateFormattedTextField = new GridBagConstraints();
		gbc_flowRateFormattedTextField.insets = new Insets(0, 0, 5, 0);
		gbc_flowRateFormattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_flowRateFormattedTextField.gridx = 4;
		gbc_flowRateFormattedTextField.gridy = 0;
		gradientStepPanel.add(flowRateFormattedTextField, gbc_flowRateFormattedTextField);
		
		JLabel label = new JLabel(" ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 1;
		gradientStepPanel.add(label, gbc_label);
		
		JLabel lblFinala = new JLabel("Final %A");
		GridBagConstraints gbc_lblFinala = new GridBagConstraints();
		gbc_lblFinala.anchor = GridBagConstraints.EAST;
		gbc_lblFinala.insets = new Insets(0, 0, 5, 5);
		gbc_lblFinala.gridx = 0;
		gbc_lblFinala.gridy = 2;
		gradientStepPanel.add(lblFinala, gbc_lblFinala);
		
		filalPercentAformattedTextField = new JFormattedTextField();
		filalPercentAformattedTextField.setColumns(10);
		GridBagConstraints gbc_filalPercentAformattedTextField = new GridBagConstraints();
		gbc_filalPercentAformattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_filalPercentAformattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_filalPercentAformattedTextField.gridx = 1;
		gbc_filalPercentAformattedTextField.gridy = 2;
		gradientStepPanel.add(filalPercentAformattedTextField, gbc_filalPercentAformattedTextField);
		
		JLabel lblFinalb = new JLabel("Final %B");
		GridBagConstraints gbc_lblFinalb = new GridBagConstraints();
		gbc_lblFinalb.anchor = GridBagConstraints.EAST;
		gbc_lblFinalb.insets = new Insets(0, 0, 5, 5);
		gbc_lblFinalb.gridx = 0;
		gbc_lblFinalb.gridy = 3;
		gradientStepPanel.add(lblFinalb, gbc_lblFinalb);
		
		filalPercentBformattedTextField = new JFormattedTextField();
		filalPercentBformattedTextField.setColumns(10);
		GridBagConstraints gbc_filalPercentBformattedTextField = new GridBagConstraints();
		gbc_filalPercentBformattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_filalPercentBformattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_filalPercentBformattedTextField.gridx = 1;
		gbc_filalPercentBformattedTextField.gridy = 3;
		gradientStepPanel.add(filalPercentBformattedTextField, gbc_filalPercentBformattedTextField);
		
		JLabel lblFinalc = new JLabel("Final %C");
		GridBagConstraints gbc_lblFinalc = new GridBagConstraints();
		gbc_lblFinalc.anchor = GridBagConstraints.EAST;
		gbc_lblFinalc.insets = new Insets(0, 0, 5, 5);
		gbc_lblFinalc.gridx = 0;
		gbc_lblFinalc.gridy = 4;
		gradientStepPanel.add(lblFinalc, gbc_lblFinalc);
		
		filalPercentCformattedTextField = new JFormattedTextField();
		filalPercentCformattedTextField.setColumns(10);
		GridBagConstraints gbc_filalPercentCformattedTextField = new GridBagConstraints();
		gbc_filalPercentCformattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_filalPercentCformattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_filalPercentCformattedTextField.gridx = 1;
		gbc_filalPercentCformattedTextField.gridy = 4;
		gradientStepPanel.add(filalPercentCformattedTextField, gbc_filalPercentCformattedTextField);
		
		JLabel lblFinald = new JLabel("Final %D");
		GridBagConstraints gbc_lblFinald = new GridBagConstraints();
		gbc_lblFinald.anchor = GridBagConstraints.EAST;
		gbc_lblFinald.insets = new Insets(0, 0, 5, 5);
		gbc_lblFinald.gridx = 0;
		gbc_lblFinald.gridy = 5;
		gradientStepPanel.add(lblFinald, gbc_lblFinald);
		
		filalPercentDformattedTextField = new JFormattedTextField();
		filalPercentDformattedTextField.setColumns(10);
		GridBagConstraints gbc_filalPercentDformattedTextField = new GridBagConstraints();
		gbc_filalPercentDformattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_filalPercentDformattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_filalPercentDformattedTextField.gridx = 1;
		gbc_filalPercentDformattedTextField.gridy = 5;
		gradientStepPanel.add(filalPercentDformattedTextField, gbc_filalPercentDformattedTextField);
		
		JLabel label_1 = new JLabel(" ");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.insets = new Insets(0, 0, 5, 5);
		gbc_label_1.gridx = 0;
		gbc_label_1.gridy = 6;
		gradientStepPanel.add(label_1, gbc_label_1);
		
		btnClearPanel = new JButton("Clear panel");
		btnClearPanel.addActionListener(this);
		btnClearPanel.setActionCommand(CLEAR_PANEL_COMMAND);
		GridBagConstraints gbc_btnClearPanel = new GridBagConstraints();
		gbc_btnClearPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnClearPanel.insets = new Insets(0, 0, 0, 5);
		gbc_btnClearPanel.gridx = 1;
		gbc_btnClearPanel.gridy = 7;
		gradientStepPanel.add(btnClearPanel, gbc_btnClearPanel);
		
		btnAddStepToGradient = new JButton("Add step to gradient");
		btnAddStepToGradient.setActionCommand(MainActionCommands.ADD_GRADIENT_STEP_COMMAND.getName());
		btnAddStepToGradient.addActionListener(listener);
		GridBagConstraints gbc_btnAddStepToGradient = new GridBagConstraints();
		gbc_btnAddStepToGradient.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAddStepToGradient.gridwidth = 2;
		gbc_btnAddStepToGradient.gridx = 3;
		gbc_btnAddStepToGradient.gridy = 7;
		gradientStepPanel.add(btnAddStepToGradient, gbc_btnAddStepToGradient);
		
//		ADD_GRADIENT_STEP_COMMAND("Add gradient step"),
//		DELETE_GRADIENT_STEP_COMMAND("Delete gradient step"),
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(CLEAR_PANEL_COMMAND)) {
			
			endTimeFormattedTextField.setText("");
			flowRateFormattedTextField.setText("");
			filalPercentAformattedTextField.setText("");
			filalPercentBformattedTextField.setText("");
			filalPercentCformattedTextField.setText("");
			filalPercentDformattedTextField.setText("");
		}
	}

}
