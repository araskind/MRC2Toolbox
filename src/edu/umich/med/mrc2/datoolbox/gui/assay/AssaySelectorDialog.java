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

package edu.umich.med.mrc2.datoolbox.gui.assay;

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

import edu.umich.med.mrc2.datoolbox.data.Assay;
import edu.umich.med.mrc2.datoolbox.database.lims.LIMSDataCash;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class AssaySelectorDialog extends JDialog  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3996911008941815573L;


	private AssayTable assayMethodsTable;
	private static final Icon acqMethodIcon = GuiUtils.getIcon("acqMethod", 32);

	public AssaySelectorDialog(ActionListener actionListener) {

		super(MRC2ToolBoxCore.getMainWindow(), "Select assay");
		setIconImage(((ImageIcon) acqMethodIcon).getImage());
		setPreferredSize(new Dimension(640, 480));

		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(640, 480));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		assayMethodsTable = new AssayTable();
		LIMSDataCash.refreshAssayList();		
		assayMethodsTable.setTableModelFromAssayCollection(
					LIMSDataCash.getAssays());
		JScrollPane scrollPane = new JScrollPane(assayMethodsTable);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
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
				
		JButton btnSave = new JButton(MainActionCommands.SELECT_ASSAY_COMMAND.getName());
		btnSave.setActionCommand(MainActionCommands.SELECT_ASSAY_COMMAND.getName());
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		pack();
	}
	
	public Assay getSelectedAssay() {
		return assayMethodsTable.getSelectedAssay();
	}
}
























