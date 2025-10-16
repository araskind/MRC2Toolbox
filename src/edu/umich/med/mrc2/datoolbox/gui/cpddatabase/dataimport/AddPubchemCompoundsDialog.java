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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.dataimport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class AddPubchemCompoundsDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = 1895644188920830351L;
	private static final Icon pubchemIcon = GuiUtils.getIcon("pubChemDownload", 32);
	private JButton btnSave;
	private JTextArea textArea;

	public AddPubchemCompoundsDialog(ActionListener actionListener) {
		super();
		setTitle("Add compounds from PubChem");
		setIconImage(((ImageIcon) pubchemIcon).getImage());
		setSize(new Dimension(400, 300));
		setPreferredSize(new Dimension(400, 300));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		dataPanel.setLayout(new BorderLayout(0, 0));

		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setBorder(new LineBorder(new Color(0, 0, 0)));
		dataPanel.add(textArea, BorderLayout.CENTER);

		JLabel lblPastePubchemIds = new JLabel("Paste PubChem Ids to fetch:");
		lblPastePubchemIds.setFont(new Font("Tahoma", Font.BOLD, 12));
		lblPastePubchemIds.setBorder(new EmptyBorder(0, 0, 10, 0));
		dataPanel.add(lblPastePubchemIds, BorderLayout.NORTH);

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

		btnSave = new JButton("Fetch PubChem data");
		btnSave.setActionCommand(MainActionCommands.FETCH_PUBCHEM_DATA.getName());
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
	}

	public Collection<String> getIdList() {

		 String[] idArray =
				 textArea.getText().trim().replaceAll("[\\,,;,:,\\(,\\),\\[,\\],\\{,\\}]", " ").
				 replaceAll("\\s+", " ").trim().split(" ", 0);
		Collection<String> idList = new TreeSet<String>();
		for (int i = 0; i < idArray.length; i++) {

			if (idArray[i].trim().isEmpty())
				continue;

			int convertedId = 0;
			try {
				convertedId = Integer.valueOf(idArray[i].trim());
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
			if (convertedId > 0)
				idList.add(idArray[i].trim());
		}
		return idList;
	}
}






















