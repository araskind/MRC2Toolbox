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

package edu.umich.med.mrc2.datoolbox.gui.annotation;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class AnnotationTextEditor extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = -8499142430512415429L;
	private JButton saveButton, cancelButton;
	private JTextArea textArea;
	private ObjectAnnotation currentAnnotation;

	public AnnotationTextEditor(ActionListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "Edit annotation text", true);

		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(400, 200));
		setMinimumSize(new Dimension(400, 200));

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		cancelButton = new JButton("Cancel");
		panel.add(cancelButton);

		saveButton = new JButton("Save");
		saveButton.addActionListener(listener);
		saveButton.setActionCommand(MainActionCommands.SAVE_OBJECT_ANNOTATION_COMMAND.getName());
		panel.add(saveButton);

		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		scrollPane.setViewportView(textArea);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		cancelButton.addActionListener(al);

		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(saveButton);
		pack();
	}

	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}
	
	public synchronized void clearPanel() {

		textArea.setText("");
	}

	public ObjectAnnotation getAnnotation() {

//		if (currentAnnotation != null) {
//
//			if (!textArea.getText().trim().equals(currentAnnotation.getText())) {
//
//				currentAnnotation.setText(textArea.getText().trim());
//				currentAnnotation.setLastModified(new Date());
//			}
//		} else {
//			currentAnnotation = new ObjectAnnotation(textArea.getText().trim());
//		}
		return currentAnnotation;
	}

	public void loadAnnotation(ObjectAnnotation annotation) {

		textArea.setText("");
		currentAnnotation = annotation;

		if (annotation != null)
			textArea.setText(annotation.getText(-1));
	}

	@Override
	public void setVisible(boolean visible){

		if(visible)
			textArea.requestFocusInWindow();

		super.setVisible(visible);
	}
}
