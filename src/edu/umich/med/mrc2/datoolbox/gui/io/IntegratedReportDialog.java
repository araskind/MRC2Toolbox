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

package edu.umich.med.mrc2.datoolbox.gui.io;

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
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.DataExportFields;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.io.IntegratedExcelReportExportTask;

public class IntegratedReportDialog  extends JDialog implements ActionListener{

	/**
	 *
	 */
	private static final long serialVersionUID = -496848808289756909L;

	private static final Icon exportExcelIcon = GuiUtils.getIcon("excel", 32);
	private static final String SAVE_REPORT = "Save report";
	private static final String BROWSE = "BROWSE";

	private File baseDirectory;
	private JPanel featureSelectorGridPanel;
	private ExperimentDesignSubset activeSet;
	private HashSet<FeatureSetSelectionPanel>featureSetSelectors;
	private File exportFile;

	private JComboBox designComboBox;
	private JComboBox namingComboBox;
	private JComboBox integratedSeComboBox;	
	private DataAnalysisProject currentProject;
	private JTextField exportFileTextField;

	public IntegratedReportDialog(DataAnalysisProject currentProject) {

		super(MRC2ToolBoxCore.getMainWindow(), "Export integrated report to Excel");
		setPreferredSize(new Dimension(640, 800));
		setIconImage(((ImageIcon) exportExcelIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(640, 800));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		this.currentProject = currentProject;

		JPanel panel_3 = new JPanel();
		getContentPane().add(panel_3, BorderLayout.CENTER);
		panel_3.setLayout(new BorderLayout(0, 0));

		JPanel panel_4 = new JPanel();
		panel_4.setBorder(
			new CompoundBorder(new EmptyBorder(10, 10, 10, 10), 
					new CompoundBorder(new TitledBorder(
					new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
					"Select experimental design to export", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), 
							new EmptyBorder(10, 0, 10, 0))));
		panel_3.add(panel_4, BorderLayout.NORTH);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[]{300, 45, 28, 0};
		gbl_panel_4.rowHeights = new int[]{25, 0};
		gbl_panel_4.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_4.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_4.setLayout(gbl_panel_4);

		designComboBox = new JComboBox<ExperimentDesignSubset>();
		designComboBox.setPreferredSize(new Dimension(300, 25));
		GridBagConstraints gbc_designComboBox = new GridBagConstraints();
		gbc_designComboBox.anchor = GridBagConstraints.NORTHWEST;
		gbc_designComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_designComboBox.gridx = 0;
		gbc_designComboBox.gridy = 0;
		panel_4.add(designComboBox, gbc_designComboBox);

		JLabel lblNameBy = new JLabel("Name by ");
		GridBagConstraints gbc_lblNameBy = new GridBagConstraints();
		gbc_lblNameBy.anchor = GridBagConstraints.WEST;
		gbc_lblNameBy.insets = new Insets(0, 0, 0, 5);
		gbc_lblNameBy.gridx = 1;
		gbc_lblNameBy.gridy = 0;
		panel_4.add(lblNameBy, gbc_lblNameBy);

		DefaultComboBoxModel<DataExportFields> namingModel = 
				new DefaultComboBoxModel<DataExportFields>(
					new DataExportFields[] { 
							DataExportFields.SAMPLE_EXPORT_NAME, 
							DataExportFields.SAMPLE_EXPORT_ID });
		namingComboBox = new JComboBox<DataExportFields>(namingModel);
		namingComboBox.setPreferredSize(new Dimension(200, 25));
		GridBagConstraints gbc_namingComboBox = new GridBagConstraints();
		gbc_namingComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_namingComboBox.gridx = 2;
		gbc_namingComboBox.gridy = 0;
		panel_4.add(namingComboBox, gbc_namingComboBox);

		featureSetSelectors = new HashSet<FeatureSetSelectionPanel>();
		featureSelectorGridPanel = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) featureSelectorGridPanel.getLayout();
		flowLayout_3.setAlignOnBaseline(true);
		flowLayout_3.setAlignment(FlowLayout.LEFT);
		featureSelectorGridPanel.setBorder(
				new CompoundBorder(new EmptyBorder(10, 10, 10, 10), 
				new CompoundBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
				new Color(160, 160, 160)), "Select feature sets to export for each assay", 
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(10, 0, 10, 0))));
		panel_3.add(featureSelectorGridPanel, BorderLayout.CENTER);

		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel_3.add(panel_5, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_5 = new GridBagLayout();
		gbl_panel_5.columnWidths = new int[]{0, 0, 0};
		gbl_panel_5.rowHeights = new int[]{0, 0};
		gbl_panel_5.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_5.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_5.setLayout(gbl_panel_5);

		JLabel lblIntegratedDataSet = new JLabel("Integrated data set:");
		GridBagConstraints gbc_lblIntegratedDataSet = new GridBagConstraints();
		gbc_lblIntegratedDataSet.insets = new Insets(0, 0, 0, 5);
		gbc_lblIntegratedDataSet.anchor = GridBagConstraints.EAST;
		gbc_lblIntegratedDataSet.gridx = 0;
		gbc_lblIntegratedDataSet.gridy = 0;
		panel_5.add(lblIntegratedDataSet, gbc_lblIntegratedDataSet);

		integratedSeComboBox = new JComboBox();
		GridBagConstraints gbc_integratedSeComboBox = new GridBagConstraints();
		gbc_integratedSeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_integratedSeComboBox.gridx = 1;
		gbc_integratedSeComboBox.gridy = 0;
		panel_5.add(integratedSeComboBox, gbc_integratedSeComboBox);

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);

		JButton cancelButton = new JButton("Cancel");
		panel_1.add(cancelButton);

		JButton saveButton = new JButton("Save report");
		saveButton.addActionListener(this);
		saveButton.setActionCommand(SAVE_REPORT);
		panel_1.add(saveButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		JRootPane rootPane = SwingUtilities.getRootPane(saveButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelButton.addActionListener(al);
		rootPane.setDefaultButton(saveButton);
		getContentPane().add(panel_1, BorderLayout.SOUTH);
		
		JPanel panel = new JPanel();
		panel.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10), 
				new CompoundBorder(new TitledBorder(
						new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
						"Export file", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), 
						new EmptyBorder(10, 0, 10, 0))));
		getContentPane().add(panel, BorderLayout.NORTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		exportFileTextField = new JTextField();
		exportFileTextField.setEditable(false);
		GridBagConstraints gbc_exportFileTextField = new GridBagConstraints();
		gbc_exportFileTextField.insets = new Insets(0, 0, 0, 5);
		gbc_exportFileTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_exportFileTextField.gridx = 0;
		gbc_exportFileTextField.gridy = 0;
		panel.add(exportFileTextField, gbc_exportFileTextField);
		exportFileTextField.setColumns(10);
		
		JButton btnNewButton = new JButton("Browse");
		btnNewButton.setActionCommand(BROWSE);
		btnNewButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 0;
		panel.add(btnNewButton, gbc_btnNewButton);

		setPanelDataFromProject(currentProject);
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		if (command.equals(BROWSE))
			selectReportFile();

		if (command.equals(SAVE_REPORT))
			saveReport();
	}
	
	public void selectReportFile() {
		
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("Excel files", "xlsx", "XLSX");
		fc.setTitle("Set report file name and location:");
		fc.setMultiSelectionEnabled(false);
		String defaultFileName = currentProject.getName() + "_INTEGRATED_REPORT_" 
				+ MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date()) + ".xlsx";
		fc.setDefaultFileName(defaultFileName);
		
		if (fc.showSaveDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {
				
			exportFile = fc.getSelectedFile();
			try {
				exportFileTextField.setText(exportFile.getCanonicalPath());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void saveReport() {
		
		if(exportFile == null)
			return;

		ExperimentDesignSubset design = (ExperimentDesignSubset) designComboBox.getSelectedItem();
		Map<DataPipeline, MsFeatureSet>featureMap = new TreeMap<DataPipeline, MsFeatureSet>();
		for(FeatureSetSelectionPanel panel : featureSetSelectors)
			featureMap.put(panel.getDataPipeline(), panel.getSelectedFeatureSet());

		DataExportFields namingField = (DataExportFields) namingComboBox.getSelectedItem();

		MsFeatureSet integratedSet = null;
		if(integratedSeComboBox.getSelectedIndex() > -1)
			integratedSet = ((MsFeatureClusterSet)integratedSeComboBox.getSelectedItem()).getPrimaryFeatures();

		IntegratedExcelReportExportTask task = 
				new IntegratedExcelReportExportTask(
						exportFile,
						design, 
						featureMap, 
						integratedSet, 
						namingField);
		MRC2ToolBoxCore.getTaskController().addTask(task);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setPanelDataFromProject(DataAnalysisProject currentProject) {

		baseDirectory = currentProject.getExportsDirectory();
		//	Design selector
		ExperimentDesign design = currentProject.getExperimentDesign();
		ExperimentDesignSubset[] designSets =
				design.getDesignSubsets().toArray(new ExperimentDesignSubset[design.getDesignSubsets().size()]);
		activeSet = design.getActiveDesignSubset();
		designComboBox.setModel(new SortedComboBoxModel(designSets));
		designComboBox.setSelectedItem(activeSet);

		//	Feature selector
		featureSetSelectors.clear();
		featureSelectorGridPanel.removeAll();

		for(DataPipeline pipeline : currentProject.getDataPipelines()){

			FeatureSetSelectionPanel fsp = new FeatureSetSelectionPanel();
			fsp.setDataPipeline(pipeline);
			featureSelectorGridPanel.add(fsp);
			featureSetSelectors.add(fsp);
		}
		MsFeatureClusterSet[] ifs =
				currentProject.getIntergratedFeatureSets().stream().
				sorted().toArray(MsFeatureClusterSet[]::new);
		integratedSeComboBox.setModel(new DefaultComboBoxModel<MsFeatureClusterSet>(ifs));
	}
}
