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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.grad;

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
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.mobph.MobilePhaseTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MobilePhaseSelectorDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Icon dialogIcon = GuiUtils.getIcon("editMobilePhase", 32);
	
	private int channel;
	private MobilePhase mobilePhase;
	private MobilePhaseTable table;
	
	public MobilePhaseSelectorDialog(
			int channel,
			MobilePhase mobilePhase,
			ActionListener listener) {
		super();
		this.channel = channel;
		this.mobilePhase = mobilePhase;
		
		setPreferredSize(new Dimension(600, 480));
		setSize(new Dimension(600, 480));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Set mobile phase for channel");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.NORTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel = new JLabel("Channel: ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		JLabel channelNameLabel = new JLabel(GradientMobilePhaseTableModel.channelArray[channel]);
		channelNameLabel.setForeground(Color.BLUE);
		channelNameLabel.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 0;
		panel_1.add(channelNameLabel, gbc_lblNewLabel_1);	
		
		table = new MobilePhaseTable();
		table.setTableModelFromMobilePhaseCollection(
				IDTDataCache.getMobilePhaseList());
		if(mobilePhase != null)
			table.selectMobilePhase(mobilePhase);
		
		getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);

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

		JButton btnSave = new JButton(
				MainActionCommands.SET_MOBILE_PHASE_FOR_GRADIENT_CHANEL_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.SET_MOBILE_PHASE_FOR_GRADIENT_CHANEL_COMMAND.getName());
		btnSave.addActionListener(listener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		pack();
	}
	
	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}
	
	public MobilePhase getSelectedMobilePhase() {		
		return table.getSelectedMobilePhase();
	}

	public int getChannel() {
		return channel;
	}
}
