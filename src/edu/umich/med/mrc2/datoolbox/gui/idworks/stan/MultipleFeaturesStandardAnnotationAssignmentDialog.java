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

package edu.umich.med.mrc2.datoolbox.gui.idworks.stan;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.StandardFeatureAnnotation;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedListModel;

public class MultipleFeaturesStandardAnnotationAssignmentDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3375170113378450351L;

	private static final Icon editStandardFeatureAnnotationIcon = 
			GuiUtils.getIcon("editCollection", 32);
	
	private JList availableStandardFeatureAnnotationList;

	@SuppressWarnings("unchecked")
	public MultipleFeaturesStandardAnnotationAssignmentDialog(ActionListener listener) {

		super();

		setTitle("Select standard annotations to apply");
		setIconImage(((ImageIcon) editStandardFeatureAnnotationIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(600, 400));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{200, 200};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 1.0};
		gbl_panel.rowWeights = new double[]{0.0, 1.0, 1.0, 1.0, 1.0, 0.0};
		panel.setLayout(gbl_panel);

		JLabel lblAvailableReferenceSamples = new JLabel("Available standard annotations");
		GridBagConstraints gbc_lblAvailableReferenceSamples = new GridBagConstraints();
		gbc_lblAvailableReferenceSamples.gridwidth = 2;
		gbc_lblAvailableReferenceSamples.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblAvailableReferenceSamples.insets = new Insets(0, 0, 5, 0);
		gbc_lblAvailableReferenceSamples.gridx = 0;
		gbc_lblAvailableReferenceSamples.gridy = 0;
		panel.add(lblAvailableReferenceSamples, gbc_lblAvailableReferenceSamples);

		availableStandardFeatureAnnotationList = new JList<StandardFeatureAnnotation>();
		availableStandardFeatureAnnotationList.setSelectionMode(
				ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);		
		availableStandardFeatureAnnotationList.setCellRenderer(
				new StandardFeatureAnnotationListRenderer());
		availableStandardFeatureAnnotationList.setModel(
				new SortedListModel<StandardFeatureAnnotation>(
						IDTDataCache.getStandardFeatureAnnotationList()));
		JScrollPane scrollPane = new JScrollPane(availableStandardFeatureAnnotationList);
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridheight = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		panel.add(scrollPane, gbc_scrollPane);
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.EAST;
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 0;
		gbc_btnCancel.gridy = 5;
		panel.add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(al);
		
		JButton btnSaveChanges = 
				new JButton(MainActionCommands.SAVE_STANDARD_FEATURE_ANNOTATION_ASSIGNMENT_COMMAND.getName());
		btnSaveChanges.setActionCommand(
				MainActionCommands.SAVE_STANDARD_FEATURE_ANNOTATION_ASSIGNMENT_COMMAND.getName());
		btnSaveChanges.addActionListener(listener);
		GridBagConstraints gbc_btnSaveChanges = new GridBagConstraints();
		gbc_btnSaveChanges.anchor = GridBagConstraints.EAST;
		gbc_btnSaveChanges.gridx = 1;
		gbc_btnSaveChanges.gridy = 5;
		panel.add(btnSaveChanges, gbc_btnSaveChanges);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSaveChanges);
		rootPane.setDefaultButton(btnSaveChanges);

		pack();
	}

	@SuppressWarnings("unchecked")
	public Collection<StandardFeatureAnnotation>getSelectedAnnotations(){
		return availableStandardFeatureAnnotationList.getSelectedValuesList();
	}
}





































