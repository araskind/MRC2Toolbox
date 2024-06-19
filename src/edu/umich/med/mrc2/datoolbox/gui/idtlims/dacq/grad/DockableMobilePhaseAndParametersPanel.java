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
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DockableMobilePhaseAndParametersPanel extends DefaultSingleCDockable implements ActionListener {

	private static final Icon componentIcon = GuiUtils.getIcon("editMobilePhaseIcon", 16);
	private GradientMobilePhaseTable gradientMobilePhaseTable;
	private GradientMobilePhaseManagerToolbar toolbar;
	private JFormattedTextField columnTemperatureFormattedTextField;
	private MobilePhaseSelectorDialog mobilePhaseSelectorDialog;
	private ChromatographicGradient gradient;
	
	public DockableMobilePhaseAndParametersPanel() {

		super("DockableMobilePhaseAndParametersPanel", componentIcon, 
				"Mobile phases and parameters", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0,0));
		toolbar = new GradientMobilePhaseManagerToolbar(this);
		add(toolbar, BorderLayout.NORTH);
		
		gradientMobilePhaseTable = new GradientMobilePhaseTable();
		add(new JScrollPane(gradientMobilePhaseTable), BorderLayout.CENTER);
		
		JPanel parametersPanel = new JPanel();
		parametersPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gbl_gradientStepPanel = new GridBagLayout();
		gbl_gradientStepPanel.columnWidths = new int[]{0, 0, 0};
		gbl_gradientStepPanel.rowHeights = new int[]{0, 0};
		gbl_gradientStepPanel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_gradientStepPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		parametersPanel.setLayout(gbl_gradientStepPanel);
		
		JLabel lblEndTimeMin = new JLabel("Column compartment temperature");
		GridBagConstraints gbc_lblEndTimeMin = new GridBagConstraints();
		gbc_lblEndTimeMin.anchor = GridBagConstraints.EAST;
		gbc_lblEndTimeMin.fill = GridBagConstraints.VERTICAL;
		gbc_lblEndTimeMin.insets = new Insets(0, 0, 0, 5);
		gbc_lblEndTimeMin.gridx = 0;
		gbc_lblEndTimeMin.gridy = 0;
		parametersPanel.add(lblEndTimeMin, gbc_lblEndTimeMin);
		
		add(parametersPanel, BorderLayout.SOUTH);		
		
		columnTemperatureFormattedTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		columnTemperatureFormattedTextField.setColumns(10);
		GridBagConstraints gbc_columnTemperatureFormattedTextField = new GridBagConstraints();
		gbc_columnTemperatureFormattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_columnTemperatureFormattedTextField.gridx = 1;
		gbc_columnTemperatureFormattedTextField.gridy = 0;
		parametersPanel.add(columnTemperatureFormattedTextField, 
				gbc_columnTemperatureFormattedTextField);
	}

	public synchronized void clearPanel() {
		
		gradient = null;
		gradientMobilePhaseTable.clearTable();
		columnTemperatureFormattedTextField.setText("");
	}
	
	public void loadGradientData(ChromatographicGradient gradient) {
		
		this.gradient = gradient;
		gradientMobilePhaseTable.setTableModelFromMobilePhaseArray(gradient.getMobilePhases());
		columnTemperatureFormattedTextField.setText("");
		if(gradient.getColumnCompartmentTemperature() > 0)
			columnTemperatureFormattedTextField.setText(Double.toString(gradient.getColumnCompartmentTemperature()));
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.SET_MOBILE_PHASE_FOR_GRADIENT_CHANEL_DIALOG_COMMAND.getName())) 
			showMobilePhaseSelector();
		
		if(e.getActionCommand().equals(MainActionCommands.SET_MOBILE_PHASE_FOR_GRADIENT_CHANEL_COMMAND.getName())) 
			setMobilePhaseForChanel();
		
		if(e.getActionCommand().equals(MainActionCommands.CLEAR_MOBILE_PHASE_FOR_GRADIENT_CHANEL_COMMAND.getName())) 
			clearMobilePhaseForChanel();		
	}
	
	private void showMobilePhaseSelector() {
		
		int channel = gradientMobilePhaseTable.getSelectedRow();
		if(channel < 0)
			return;
		
		MobilePhase mp = gradientMobilePhaseTable.getSelectedMobilePhase();
		mobilePhaseSelectorDialog = 
				new MobilePhaseSelectorDialog(channel, mp, this);
		mobilePhaseSelectorDialog.setLocationRelativeTo(this.getContentPane());
		mobilePhaseSelectorDialog.setVisible(true);
	}

	private void setMobilePhaseForChanel() {
		
		int channel = mobilePhaseSelectorDialog.getChannel();
		MobilePhase mp = mobilePhaseSelectorDialog.getSelectedMobilePhase();
		if(channel >= 0 && mp != null) {
			
			gradient.getMobilePhases()[channel] = mp;
			gradientMobilePhaseTable.setTableModelFromMobilePhaseArray(gradient.getMobilePhases());
			mobilePhaseSelectorDialog.dispose();
		}
	}

	private void clearMobilePhaseForChanel() {
		
		MobilePhase mp = gradientMobilePhaseTable.getSelectedMobilePhase();
		if(mp == null)
			return;
		
		int row = gradientMobilePhaseTable.getSelectedRow();
		String message = "Do you want to remove mobile phase "+ mp.getName() + 
				" for " + GradientMobilePhaseTableModel.channelArray[row] + "?";
		int res = MessageDialog.showChoiceWithWarningMsg(message, this.getContentPane());
		if(res == JOptionPane.YES_OPTION) {
			gradient.getMobilePhases()[row] = null;
			gradientMobilePhaseTable.setTableModelFromMobilePhaseArray(gradient.getMobilePhases());
		}		
	}
	
	//	TODO
	public Collection<String>validateMobilePhaseData(){
		
		Collection<String>errors = new ArrayList<String>();

		return errors;
	}
}












