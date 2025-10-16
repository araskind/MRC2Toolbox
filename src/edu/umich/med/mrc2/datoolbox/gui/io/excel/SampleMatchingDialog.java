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

package edu.umich.med.mrc2.datoolbox.gui.io.excel;

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

import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.gui.io.matcher.ExtendedDataFileSampleMatchTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class SampleMatchingDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = -237582699033732129L;

	private static final Icon matchIcon = GuiUtils.getIcon("match", 32);

	private JButton btnSave;
	private ExtendedDataFileSampleMatchTable sampleMatchTable;

	public SampleMatchingDialog(ActionListener actionListener) {
		super();
		setTitle("Match imported data to experiment desing");
		setIconImage(((ImageIcon) matchIcon).getImage());
		setPreferredSize(new Dimension(600, 350));
		setSize(new Dimension(600, 350));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		sampleMatchTable = new ExtendedDataFileSampleMatchTable();
		JScrollPane dataPanel = new JScrollPane(sampleMatchTable);
		getContentPane().add(dataPanel, BorderLayout.CENTER);

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
		btnSave = new JButton("Accept assignment");
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		pack();
	}

	public void setTableModelFromReportData(
			String[] sampleIds,
			String[] sampleNames,
			String[] dataFileNames,
			DataAcquisitionMethod acquisitionMethod) {

		sampleMatchTable.setTableModelFromReportData(
			sampleIds, 
			sampleNames, 
			dataFileNames, 
			acquisitionMethod);
	}
}
















