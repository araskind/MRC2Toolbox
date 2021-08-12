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

package edu.umich.med.mrc2.datoolbox.gui.annotation.editors;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.Document;

import edu.umich.med.mrc2.datoolbox.data.AnnotatedObject;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.rtf.RTFEditorPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import rtf.AdvancedRTFEditorKit;

public class ObjectAnnotationEditor extends JDialog implements ActionListener, ItemListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 1481350935164911990L;
	private static final Icon editAnnotationIcon = GuiUtils.getIcon("editCollection", 32);

	private JButton saveButton, cancelButton;
	private ObjectAnnotation currentAnnotation;
	private AnnotatedObject currentObject;
	//private ObjectAnnotationEditorToolbar toolbar;
	private IndeterminateProgressDialog idp;
	private RTFEditorPanel rtfEditor;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ObjectAnnotationEditor(ActionListener listener) {

		super();
		setTitle("Edit annotation");
		setIconImage(((ImageIcon) editAnnotationIcon).getImage());
		setPreferredSize(new Dimension(850, 480));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);

		rtfEditor = new RTFEditorPanel(false);
		getContentPane().add(rtfEditor, BorderLayout.CENTER);

//		toolbar= new ObjectAnnotationEditorToolbar(this, listener);
//		getContentPane().add(toolbar, BorderLayout.NORTH);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		cancelButton = new JButton("Cancel");
		panel.add(cancelButton);

		saveButton = new JButton("Save annotation");
		saveButton.addActionListener(listener);
		saveButton.setActionCommand(
				MainActionCommands.SAVE_OBJECT_ANNOTATION_COMMAND.getName());
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

		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String command = e.getActionCommand();

	}

	public void clearPanel() {

		rtfEditor.clearPanel();

		//	TODO
	}

	public ObjectAnnotation getAnnotation() {
		return currentAnnotation;
	}

	public void loadAnnotation(ObjectAnnotation annotation) {

		clearPanel();
		currentAnnotation = annotation;
		if(currentAnnotation == null)
			return;

		if(currentAnnotation.getRtfDocument() == null)
			currentAnnotation.setRtfDocument(rtfEditor.getDocument());
		else
			rtfEditor.loadDocument(currentAnnotation.getRtfDocument());
	}

	public Document getDocument() {
		return rtfEditor.getDocument();
	}

	/**
	 * @return the w_kit
	 */
	public AdvancedRTFEditorKit getRTFEditorKit() {
		return rtfEditor.getRTFEditorKit();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void itemStateChanged(ItemEvent event) {
		// TODO Auto-generated method stub
		if (event.getStateChange() == ItemEvent.SELECTED) {

		}
	}

	class ExperimentDesignRetrievalTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */
		private LIMSExperiment limsExperiment;

		public ExperimentDesignRetrievalTask(LIMSExperiment limsExperiment) {
			this.limsExperiment = limsExperiment;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Void doInBackground() {

			try {

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
}
