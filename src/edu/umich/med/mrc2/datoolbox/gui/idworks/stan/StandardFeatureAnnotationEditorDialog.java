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

package edu.umich.med.mrc2.datoolbox.gui.idworks.stan;

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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.AbstractDocument;

import edu.umich.med.mrc2.datoolbox.data.StandardFeatureAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.JTextFieldLimit;
import edu.umich.med.mrc2.datoolbox.gui.utils.UppercaseDocumentFilter;

public class StandardFeatureAnnotationEditorDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2451814964627936355L;
	
	private static final Icon addStandardFeatureAnnotationIcon = GuiUtils.getIcon("addCollection", 32);
	private static final Icon editStandardFeatureAnnotationIcon = GuiUtils.getIcon("editCollection", 32);

	private JButton saveButton, cancelButton;
	private JPanel panel_1;
	private JLabel lblTitle;
	private JTextArea descriptionTextArea;
	private StandardFeatureAnnotation annotation;
	private JLabel lblNewLabel;
	private JTextField codeTextField;

	public StandardFeatureAnnotationEditorDialog(StandardFeatureAnnotation annotation, ActionListener listener) {
		super((JDialog)listener);
		setSize(new Dimension(600, 150));
		setPreferredSize(new Dimension(600, 200));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);
		this.annotation = annotation;

		panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 155, 155, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		lblNewLabel = new JLabel("Code");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		codeTextField = new JTextField();
		codeTextField.setMaximumSize(new Dimension(80, 20));
		codeTextField.setMinimumSize(new Dimension(80, 20));
		codeTextField.setDocument(new JTextFieldLimit(6));
		((AbstractDocument) codeTextField.getDocument()).setDocumentFilter(new UppercaseDocumentFilter());

		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		panel_1.add(codeTextField, gbc_textField);
		codeTextField.setColumns(10);

		lblTitle = new JLabel("Description");
		GridBagConstraints gbc_lblTitle = new GridBagConstraints();
		gbc_lblTitle.insets = new Insets(0, 0, 0, 5);
		gbc_lblTitle.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblTitle.gridx = 0;
		gbc_lblTitle.gridy = 1;
		panel_1.add(lblTitle, gbc_lblTitle);

		descriptionTextArea = new JTextArea();
		descriptionTextArea.setBorder(new LineBorder(Color.GRAY));
		descriptionTextArea.setWrapStyleWord(true);
		descriptionTextArea.setLineWrap(true);
		GridBagConstraints gbc_statusNameTextField = new GridBagConstraints();
		gbc_statusNameTextField.gridwidth = 2;
		gbc_statusNameTextField.insets = new Insets(0, 0, 0, 5);
		gbc_statusNameTextField.fill = GridBagConstraints.BOTH;
		gbc_statusNameTextField.gridx = 1;
		gbc_statusNameTextField.gridy = 1;
		panel_1.add(descriptionTextArea, gbc_statusNameTextField);
		descriptionTextArea.setColumns(10);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		cancelButton = new JButton("Cancel");
		panel.add(cancelButton);

		saveButton = new JButton("Save");
		saveButton.addActionListener(listener);
		panel.add(saveButton);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);

		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);
		loadStandardFeatureAnnotation();
		pack();
	}
	
	private void loadStandardFeatureAnnotation() {
		
		if(annotation == null) {
			setTitle("Create new standard feature annotation");
			setIconImage(((ImageIcon) addStandardFeatureAnnotationIcon).getImage());
			saveButton.setActionCommand(
					MainActionCommands.ADD_STANDARD_FEATURE_ANNOTATION_COMMAND.getName());
		}
		else {
			setTitle("Edit standard feature annotation");
			setIconImage(((ImageIcon) editStandardFeatureAnnotationIcon).getImage());
			saveButton.setActionCommand(
					MainActionCommands.EDIT_STANDARD_FEATURE_ANNOTATION_COMMAND.getName());
			
			codeTextField.setText(annotation.getCode());
			descriptionTextArea.setText(annotation.getText());
		}
	}

	public StandardFeatureAnnotation getStandardFeatureAnnotation() {
		return annotation;
	}
	
	public String getAnnotationCode() {
		return codeTextField.getText().trim();
	}
	
	public String getAnnotationText() {
		return descriptionTextArea.getText().trim();
	}
}


















