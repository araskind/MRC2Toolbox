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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.annotation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.rtf.RTFEditorKit;

import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class AnnotationEditorDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = -5077125288776471548L;
	private static final Icon editAnnotationIcon = GuiUtils.getIcon("editCollection", 32);
	private static final Icon addAnnotationIcon = GuiUtils.getIcon("addCollection", 32);
	private ObjectAnnotation annotation;
	private JButton btnSave;
	private JTextField attacmentFileTextField;
	private JEditorPane editorPane;

	public AnnotationEditorDialog(ObjectAnnotation annotation, ActionListener actionListener) {
		super();
		this.annotation = annotation;
		setPreferredSize(new Dimension(640, 480));
		setSize(new Dimension(800, 640));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel();
		dataPanel.setBorder(null);
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		dataPanel.setLayout(new BorderLayout(0, 0));

		RTFEditorKit rtf = new RTFEditorKit();
		editorPane = new JEditorPane();
		editorPane.setEditorKit(rtf);
		editorPane.setBackground(Color.white);
		JScrollPane scrollPane = new JScrollPane(editorPane);
		dataPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 5, 10, 5));
		dataPanel.add(panel_1, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		attacmentFileTextField = new JTextField();
		attacmentFileTextField.setEditable(false);
		GridBagConstraints gbc_attacmentFileTextField = new GridBagConstraints();
		gbc_attacmentFileTextField.insets = new Insets(0, 0, 0, 5);
		gbc_attacmentFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_attacmentFileTextField.gridx = 0;
		gbc_attacmentFileTextField.gridy = 0;
		panel_1.add(attacmentFileTextField, gbc_attacmentFileTextField);
		attacmentFileTextField.setColumns(10);

		JButton browseButton = new JButton("Browse ...");
		GridBagConstraints gbc_browseButton = new GridBagConstraints();
		gbc_browseButton.gridx = 1;
		gbc_browseButton.gridy = 0;
		panel_1.add(browseButton, gbc_browseButton);

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
		btnSave = new JButton("Save");
		btnSave.addActionListener(actionListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		loadAnnotationData();
		pack();
	}

	private void loadAnnotationData() {

		if(annotation == null) {

			setTitle("Add new annotation");
			setIconImage(((ImageIcon) addAnnotationIcon).getImage());
			btnSave.setText(MainActionCommands.ADD_OBJECT_ANNOTATION_COMMAND.getName());
			btnSave.setActionCommand(MainActionCommands.ADD_OBJECT_ANNOTATION_COMMAND.getName());
		}
		else {

			setTitle("Edit annotation");
			setIconImage(((ImageIcon) editAnnotationIcon).getImage());
			btnSave.setText(MainActionCommands.EDIT_OBJECT_ANNOTATION_COMMAND.getName());
			btnSave.setActionCommand(MainActionCommands.EDIT_OBJECT_ANNOTATION_COMMAND.getName());
		}
	}

	/**
	 * @return the annotation
	 */
	public ObjectAnnotation getAnnotation() {
		return annotation;
	}


}




