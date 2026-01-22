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

package edu.umich.med.mrc2.datoolbox.gui.adducts.adduct;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.SimpleAdduct;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class ChargeCarrierSelectorDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6532806418845095432L;
	
	public static final Icon dialogIcon = GuiUtils.getIcon("calculateAnnotation", 32);
	
	private SimpleAdductTable simpleAdductTable;
	
	public ChargeCarrierSelectorDialog(ActionListener listener) {

		super();
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Select charge carrier");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(600, 450));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		
		simpleAdductTable = new SimpleAdductTable();
		simpleAdductTable.setTableModelFromAdductList(AdductManager.getChargeCarriers());
		getContentPane().add(new JScrollPane(simpleAdductTable), BorderLayout.CENTER);
				
		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(al);	
		buttonPanel.add(cancelButton);
		
		JButton saveButton = new JButton(
				MainActionCommands.SELECT_CHARGE_CARRIER_COMMAND.getName());
		saveButton.setActionCommand(
				MainActionCommands.SELECT_CHARGE_CARRIER_COMMAND.getName());
		saveButton.addActionListener(listener);
		buttonPanel.add(saveButton);
		
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);
		
		pack();
	}
	
	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}
	
	public SimpleAdduct getSelectedChargeCarrier() {
		return simpleAdductTable.getSelectedSimpleAdduct();
	}
}
