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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.BinnerAdductList;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class AnnotationListEditorDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	private static final Icon newListIcon = GuiUtils.getIcon("newFeatureSubset", 24);
	private static final Icon editListIcon = GuiUtils.getIcon("editCollection", 24);
	
	private BinnerAdductList binnerAdductList;
	private JTextField dataSetNameField;
	private JTextArea descriptionTextArea;

	private SimpleBinnerAnnotationsTable availableAnnotationsTable;

	private SimpleBinnerAnnotationsTable usedAnnotationsTable;

	public AnnotationListEditorDialog(
			BinnerAdductList binnerAdductList,
			ActionListener listener) {
		
		super();
		this.binnerAdductList = binnerAdductList;
		
		setPreferredSize(new Dimension(1000, 800));
		setSize(new Dimension(1000, 800));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel(new BorderLayout(0, 0));
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		dataPanel.add(panel_1, BorderLayout.NORTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel = new JLabel("Name: ");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		dataSetNameField = new JTextField();
		GridBagConstraints gbc_dataSetNameField = new GridBagConstraints();
		gbc_dataSetNameField.insets = new Insets(0, 0, 5, 0);
		gbc_dataSetNameField.fill = GridBagConstraints.HORIZONTAL;
		gbc_dataSetNameField.gridx = 1;
		gbc_dataSetNameField.gridy = 0;
		panel_1.add(dataSetNameField, gbc_dataSetNameField);
		dataSetNameField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Description: ");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		descriptionTextArea = new JTextArea();
		descriptionTextArea.setBorder(
				new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		descriptionTextArea.setRows(3);
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		GridBagConstraints gbc_descriptionTextArea = new GridBagConstraints();
		gbc_descriptionTextArea.fill = GridBagConstraints.BOTH;
		gbc_descriptionTextArea.gridx = 1;
		gbc_descriptionTextArea.gridy = 1;
		panel_1.add(descriptionTextArea, gbc_descriptionTextArea);
		
		JPanel panel_2 = new JPanel();
		dataPanel.add(panel_2, BorderLayout.CENTER);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0};
		gbl_panel_2.columnWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
		
		availableAnnotationsTable = new SimpleBinnerAnnotationsTable(false);
		JScrollPane scrollPane = new JScrollPane(availableAnnotationsTable);
		scrollPane.setBorder(new CompoundBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
				"Available annotations", TitledBorder.LEADING, 
				TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panel_2.add(scrollPane, gbc_scrollPane);
		
		JPanel panel_3 = new JPanel();
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.insets = new Insets(0, 0, 0, 5);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 1;
		gbc_panel_3.gridy = 0;
		panel_2.add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[]{0, 0};
		gbl_panel_3.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel_3.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel_3.rowWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel_3.setLayout(gbl_panel_3);
		
		Component verticalStrut = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut = new GridBagConstraints();
		gbc_verticalStrut.insets = new Insets(0, 0, 5, 0);
		gbc_verticalStrut.gridx = 0;
		gbc_verticalStrut.gridy = 0;
		panel_3.add(verticalStrut, gbc_verticalStrut);
		
		JButton btnNewButton = new JButton("New button");
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 1;
		panel_3.add(btnNewButton, gbc_btnNewButton);
		
		JButton btnNewButton_1 = new JButton("New button");
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton_1.gridx = 0;
		gbc_btnNewButton_1.gridy = 2;
		panel_3.add(btnNewButton_1, gbc_btnNewButton_1);
		
		Component verticalStrut_1 = Box.createVerticalStrut(20);
		GridBagConstraints gbc_verticalStrut_1 = new GridBagConstraints();
		gbc_verticalStrut_1.gridx = 0;
		gbc_verticalStrut_1.gridy = 3;
		panel_3.add(verticalStrut_1, gbc_verticalStrut_1);
		
		usedAnnotationsTable = new SimpleBinnerAnnotationsTable(true);
		JScrollPane scrollPane_1 = new JScrollPane(usedAnnotationsTable);
		scrollPane_1.setBorder(new CompoundBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
				"Annotations in the list", TitledBorder.LEADING, 
				TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 2;
		gbc_scrollPane_1.gridy = 0;
		panel_2.add(scrollPane_1, gbc_scrollPane_1);
		
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

		JButton btnSave = new JButton("Save");
		btnSave.setActionCommand(MainActionCommands.SAVE_BINNER_ANNOTATION_LIST_COMMAND.getName());
		btnSave.addActionListener(listener);
		
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		loadAnnotationList();
	}

	private void loadAnnotationList() {

		if(binnerAdductList == null) {

			setTitle("Add new Binner annotation list");
			setIconImage(((ImageIcon) newListIcon).getImage());
			availableAnnotationsTable.setTableModelFromBinnerAdductCollection(
					AdductManager.getBinnerAdductList());
		}
		else {
			setTitle("Edit Binner annotation list \"" + binnerAdductList.getName() + "\"");
			setIconImage(((ImageIcon) editListIcon).getImage());
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	public BinnerAdductList getBinnerAdductList() {
		return binnerAdductList;
	}
}










