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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.worklist;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;

public class BatchSampleAssignmentDialog extends JDialog implements ItemListener{

	/**
	 *
	 */
	private static final long serialVersionUID = 3086453967218181125L;
	private static final Icon batchDropdownIcon = GuiUtils.getIcon("dropdown", 32);
	private Collection<? extends ExperimentalSample>samples;
	private Collection<DataFile> dataFiles;
	private JButton assignSampleButton;
	private JComboBox<? extends ExperimentalSample> sampleComboBox;
	private JComboBox prepItemsComboBox;
	private LIMSSamplePreparation activeSamplePrep;

	public BatchSampleAssignmentDialog(
			Collection<DataFile> selectedDataFiles,
			LIMSExperiment experiment,
			LIMSSamplePreparation activeSamplePrep,
			ActionListener listener){

		super();
		setTitle("Batch assign samples to data files");
		samples = new TreeSet<>();
		if(experiment != null && experiment.getExperimentDesign() != null) {				
			samples = experiment.getExperimentDesign().getSamples();
		}
		else {
			if(activeSamplePrep != null) {
				try {
					samples = IDTUtils.getSamplesForPrep(activeSamplePrep);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}	
		this.dataFiles = selectedDataFiles;
		this.activeSamplePrep = activeSamplePrep;

		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setIconImage(((ImageIcon) batchDropdownIcon).getImage());
		setSize(new Dimension(400, 220));
		setPreferredSize(new Dimension(400, 220));

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(20, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		//	Instructions
		JPanel instructionsPanel = new JPanel(new BorderLayout(10,10));
		instructionsPanel.setBorder(new TitledBorder(null, "Instructions", 
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_Instructions = new GridBagConstraints();
		gbc_Instructions.fill = GridBagConstraints.HORIZONTAL;
		gbc_Instructions.gridwidth = 2;
		gbc_Instructions.insets = new Insets(0, 0, 5, 0);
		gbc_Instructions.gridx = 0;
		gbc_Instructions.gridy = 0;
		panel.add(instructionsPanel, gbc_Instructions);

		String instructionText =
			"<html>Choose the desired <b>sample</b> and <b>prep item</b><br>"
			+ "to link to all selected <b>data files</b>" +
			"</html>";
		JLabel lblNewLabel = new JLabel(instructionText);
		instructionsPanel.add(lblNewLabel, BorderLayout.CENTER);

		JLabel lblFactor = new JLabel("Samples");
		GridBagConstraints gbc_lblBatch = new GridBagConstraints();
		gbc_lblBatch.anchor = GridBagConstraints.EAST;
		gbc_lblBatch.insets = new Insets(0, 0, 5, 5);
		gbc_lblBatch.gridx = 0;
		gbc_lblBatch.gridy = 1;
		panel.add(lblFactor, gbc_lblBatch);

		sampleComboBox = 
				new JComboBox<ExperimentalSample>(
						new SortedComboBoxModel(samples));
		sampleComboBox.setSelectedIndex(-1);
		sampleComboBox.addItemListener(this);

		GridBagConstraints gbc_fCombo = new GridBagConstraints();
		gbc_fCombo.insets = new Insets(0, 0, 5, 0);
		gbc_fCombo.fill = GridBagConstraints.HORIZONTAL;
		gbc_fCombo.gridx = 1;
		gbc_fCombo.gridy = 1;
		panel.add(sampleComboBox, gbc_fCombo);

		JLabel lblPrepItems = new JLabel("Prep items");
		GridBagConstraints gbc_lblPrepItems = new GridBagConstraints();
		gbc_lblPrepItems.anchor = GridBagConstraints.EAST;
		gbc_lblPrepItems.insets = new Insets(0, 0, 5, 5);
		gbc_lblPrepItems.gridx = 0;
		gbc_lblPrepItems.gridy = 2;
		panel.add(lblPrepItems, gbc_lblPrepItems);

		prepItemsComboBox = new JComboBox();
		GridBagConstraints gbc_prepItemsComboBox = new GridBagConstraints();
		gbc_prepItemsComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_prepItemsComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_prepItemsComboBox.gridx = 1;
		gbc_prepItemsComboBox.gridy = 2;
		panel.add(prepItemsComboBox, gbc_prepItemsComboBox);

		JLabel label = new JLabel(" ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.gridx = 1;
		gbc_label.gridy = 3;
		panel.add(label, gbc_label);

		JPanel panel2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel2.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel2, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel2.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);
		assignSampleButton = new JButton("Assign values");
		assignSampleButton.setActionCommand(MainActionCommands.ASSIGN_SAMPLE_TO_DATA_FILES_COMMAND.getName());
		assignSampleButton.addActionListener(listener);
		panel2.add(assignSampleButton);
		JRootPane rootPane = SwingUtilities.getRootPane(assignSampleButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(assignSampleButton);

		pack();
	}

	public ExperimentalSample getSelectedSample() {
		return (ExperimentalSample)sampleComboBox.getSelectedItem();
	}

	public String getSelectedPrepItem() {
		return (String)prepItemsComboBox.getSelectedItem();
	}

	public Collection<DataFile> getDataFiles(){
		return dataFiles;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED) {

			ExperimentalSample sample = (ExperimentalSample)e.getItem();
			Map<String,String>samplePrepItems = new TreeMap<String,String>();
			activeSamplePrep.getPrepItemMap().entrySet().stream().
				filter(m -> m.getValue().equals(sample.getId())).
				forEach(s -> samplePrepItems.put(s.getKey(), s.getValue()));

			prepItemsComboBox.setModel(new SortedComboBoxModel(samplePrepItems.keySet()));
		}
	}
}
















