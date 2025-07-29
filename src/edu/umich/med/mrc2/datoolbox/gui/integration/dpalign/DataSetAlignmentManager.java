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

package edu.umich.med.mrc2.datoolbox.gui.integration.dpalign;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.umich.med.mrc2.datoolbox.data.DataPipelineAlignmentResults;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class DataSetAlignmentManager extends JDialog implements ListSelectionListener{

	private static final long serialVersionUID = -2902853446757764068L;

	private static final Icon dialogIcon = GuiUtils.getIcon("alignmentManager", 32);
	private AlignedDataSetTable table;
	private JLabel refPipelineLabel;
	private JLabel queryPipelineLabel;
	private JLabel massWindowLabel;
	private JLabel rtWindowLabel;
	
	public DataSetAlignmentManager(
			ActionListener actionListener,
			DataAnalysisProject project) {
		super();
		setTitle("Data set alignment manager");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(600, 480));
		setSize(new Dimension(600, 480));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		DataSetAlignmentManagerToolbar toolbar = 
				new DataSetAlignmentManagerToolbar(actionListener);
		getContentPane().add(toolbar, BorderLayout.NORTH);
		
		JPanel dataPanel = new JPanel(new BorderLayout(0, 0));
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);

		table = new AlignedDataSetTable();
		table.getSelectionModel().addListSelectionListener(this);
		table.setTableModelFromExperiment(project);
		ActionEvent event = new ActionEvent(
				this, ActionEvent.ACTION_FIRST, 
				MainActionCommands.LOAD_DATA_PIPELINE_ALIGNMENT_RESULTS_COMMAND.getName());
		table.addMouseListener(
				new MouseAdapter() {
					
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2) {
							actionListener.actionPerformed(event);
						}
					}
				});
		dataPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(new TitledBorder(new EtchedBorder(
				EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
				"Details", TitledBorder.LEADING, TitledBorder.TOP, 
				null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		dataPanel.add(panel, BorderLayout.SOUTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JLabel lblNewLabel = new JLabel("Reference pipeline");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel.add(lblNewLabel, gbc_lblNewLabel);
		
		refPipelineLabel = new JLabel("");
		refPipelineLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_refPipelineLabel = new GridBagConstraints();
		gbc_refPipelineLabel.anchor = GridBagConstraints.WEST;
		gbc_refPipelineLabel.insets = new Insets(0, 0, 5, 0);
		gbc_refPipelineLabel.gridx = 1;
		gbc_refPipelineLabel.gridy = 0;
		panel.add(refPipelineLabel, gbc_refPipelineLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Query pipeline");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panel.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		queryPipelineLabel = new JLabel("");
		queryPipelineLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_queryPipelineLabel = new GridBagConstraints();
		gbc_queryPipelineLabel.insets = new Insets(0, 0, 5, 0);
		gbc_queryPipelineLabel.gridx = 1;
		gbc_queryPipelineLabel.gridy = 1;
		panel.add(queryPipelineLabel, gbc_queryPipelineLabel);
		
		JLabel lblNewLabel_2 = new JLabel("Mass window");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 2;
		panel.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		massWindowLabel = new JLabel("");
		massWindowLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_massWindowLabel = new GridBagConstraints();
		gbc_massWindowLabel.anchor = GridBagConstraints.WEST;
		gbc_massWindowLabel.insets = new Insets(0, 0, 5, 0);
		gbc_massWindowLabel.gridx = 1;
		gbc_massWindowLabel.gridy = 2;
		panel.add(massWindowLabel, gbc_massWindowLabel);
		
		JLabel lblNewLabel_3 = new JLabel("RT window, min");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 3;
		panel.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		rtWindowLabel = new JLabel("");
		rtWindowLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_rtWindowLabel = new GridBagConstraints();
		gbc_rtWindowLabel.anchor = GridBagConstraints.WEST;
		gbc_rtWindowLabel.gridx = 1;
		gbc_rtWindowLabel.gridy = 3;
		panel.add(rtWindowLabel, gbc_rtWindowLabel);

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);
		pack();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting())
			showDataAlignmentDetails();	
	}
	
	public DataPipelineAlignmentResults getSelectedDataPipelineAlignmentResults() {
		return  table.getSelectedDataPipelineAlignmentResults();
	}

	private void showDataAlignmentDetails() {

		clearDetails();
		DataPipelineAlignmentResults alignment = table.getSelectedDataPipelineAlignmentResults();
		if(alignment == null)
			return;

		refPipelineLabel.setText(alignment.getAlignmentSettings().getReferencePipeline().getName());
		queryPipelineLabel.setText(alignment.getAlignmentSettings().getQueryPipeline().getName());
		String massWindow = MRC2ToolBoxConfiguration.getMzFormat().format(
				alignment.getAlignmentSettings().getMassWindow()) + " " 
				+ alignment.getAlignmentSettings().getMassErrorType().name();
		massWindowLabel.setText(massWindow);
		rtWindowLabel.setText(MRC2ToolBoxConfiguration.getRtFormat().format(
				alignment.getAlignmentSettings().getRetentionWindow()));
	}
	
	private void clearDetails() {
		
		refPipelineLabel.setText("");
		queryPipelineLabel.setText("");
		massWindowLabel.setText("");
		rtWindowLabel.setText("");
	}
	
	

}





















