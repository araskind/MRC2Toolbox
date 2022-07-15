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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.data;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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

import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.dextr.DataExtractionMethodTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DataAnalysisMethodSelectionDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = -3962126555199879212L;
	private static final Icon addMethodIcon = GuiUtils.getIcon("addDataProcessingMethod", 32);
	private JButton btnSelect;
	private DataExtractionMethodTable dataExtractionMethodsTable;

	public DataAnalysisMethodSelectionDialog(ActionListener actionListener) {

		super();
		setTitle("Select data analysis method");
		setIconImage(((ImageIcon) addMethodIcon).getImage());
		setPreferredSize(new Dimension(800, 300));
		setSize(new Dimension(800, 300));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		dataExtractionMethodsTable = new DataExtractionMethodTable();
		dataExtractionMethodsTable.setTableModelFromMethods(IDTDataCash.getDataExtractionMethods());
		dataExtractionMethodsTable.addMouseListener(
				new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2) {
							btnSelect.doClick();
						}
					}
				});
		getContentPane().add(new JScrollPane(dataExtractionMethodsTable), BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel_1.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);
		btnSelect = new JButton(MainActionCommands.SELECT_DA_METHOD_COMMAND.getName());
		btnSelect.setActionCommand(MainActionCommands.SELECT_DA_METHOD_COMMAND.getName());
		btnSelect.addActionListener(actionListener);
		panel_1.add(btnSelect);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSelect);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSelect);

		pack();
	}

	public DataExtractionMethod getSelectedMethod() {
		return dataExtractionMethodsTable.getSelectedMethod();
	}
}














