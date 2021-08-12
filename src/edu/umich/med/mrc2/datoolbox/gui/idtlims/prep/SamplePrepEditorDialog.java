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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.prep;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Date;

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

import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProtocol;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class SamplePrepEditorDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = -8958656243635154039L;
	private static final Icon addPrepIcon = GuiUtils.getIcon("addSamplePrep", 32);
	private static final Icon editPrepIcon = GuiUtils.getIcon("editSamplePrep", 32);

	private JButton btnSave;
	private SamplePrepEditorPanel samplePrepEditorPanel;
	private ActionListener actionListener;

	
	/**
	 * This constructor is for the creation of the new sample preparation;
	 * @param experiment
	 * @param actionListener
	 */
	public SamplePrepEditorDialog(LIMSExperiment experiment, ActionListener actionListener) {
		super();
		this.actionListener = actionListener;
		samplePrepEditorPanel = new SamplePrepEditorPanel(experiment);
		initGui();
	}

	/**
	 * This constructor is for the editing of the existing sample preparation;
	 * @param prep
	 * @param actionListener
	 */
	public SamplePrepEditorDialog(
			LIMSSamplePreparation prep,
			ActionListener actionListener) {
		super();	
		this.actionListener = actionListener;
		samplePrepEditorPanel = new SamplePrepEditorPanel(prep);
		initGui();
	}
	
	private void initGui() {
		
		setPreferredSize(new Dimension(800, 640));
		setSize(new Dimension(800, 640));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		getContentPane().add(samplePrepEditorPanel, BorderLayout.CENTER);

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

		loadPrepData();
		pack();
	}
	
	@Override
	public void dispose() {		
		samplePrepEditorPanel.saveLayout(samplePrepEditorPanel.getLayoutFile());
		super.dispose();
	}

	private void loadPrepData() {

		if(samplePrepEditorPanel.getSamplePrep() == null) {

			setTitle("Add new sample preparation for experiment \"" + samplePrepEditorPanel.getExperiment().getName() + "\"");
			setIconImage(((ImageIcon) addPrepIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.ADD_SAMPLE_PREP_COMMAND.getName());
		}
		else {
			setTitle("Edit information for " + samplePrepEditorPanel.getSamplePrep().getName());
			setIconImage(((ImageIcon) editPrepIcon).getImage());
			btnSave.setActionCommand(MainActionCommands.EDIT_SAMPLE_PREP_COMMAND.getName());
		}
		samplePrepEditorPanel.loadPrepData(samplePrepEditorPanel.getSamplePrep());
	}

	public Collection<String>vaidateSamplePrepData() {
		return samplePrepEditorPanel.vaidateSamplePrepData();
	}

	public LIMSSamplePreparation getSamplePrep() {
		return samplePrepEditorPanel.getSamplePrep();
	}

	public LIMSExperiment getExperiment() {
		return samplePrepEditorPanel.getExperiment();
	}

	public String getPrepName() {
		return samplePrepEditorPanel.getPrepName();
	}

	public Date getPrepDate() {
		return samplePrepEditorPanel.getPrepDate();
	}

	/**
	 * @return the prepUser
	 */
	public LIMSUser getPrepUser() {
		return samplePrepEditorPanel.getPrepUser();
	}

	public Collection<LIMSProtocol> getPrepSops(){
		return samplePrepEditorPanel.getPrepSops();
	}

	public Collection<ObjectAnnotation> getPrepAnnotations(){
		return samplePrepEditorPanel.getPrepAnnotations();
	}

	public Collection<IDTExperimentalSample>getSelectedSamples(){
		return samplePrepEditorPanel.getSelectedSamples();
	}
}

































