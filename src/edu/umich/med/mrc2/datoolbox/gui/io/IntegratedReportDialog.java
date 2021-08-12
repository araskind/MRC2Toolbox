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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.SimpleDateFormat;
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
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignSubset;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.DataExportFields;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
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
	private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
	private ImprovedFileChooser chooser;
	private File baseDirectory;
	private FileNameExtensionFilter excelFilter;
	private boolean painted;

	private JSplitPane mainSplitPane;
	private JButton cancelButton, saveButton;
	private JPanel panel_2, featureSelectorGridPanel;
	private JLabel lblNewLabel;

	private ExperimentDesignSubset activeSet;
	private HashSet<FeatureSetSelectionPanel>featureSetSelectors;
	private JPanel panel_3;
	private JPanel panel_4;

	private JComboBox designComboBox;
	private JLabel lblNameBy;
	private JComboBox namingComboBox;
	private JPanel panel_5;
	private JLabel lblIntegratedDataSet;
	private JComboBox integratedSeComboBox;

	public IntegratedReportDialog() {

		super(MRC2ToolBoxCore.getMainWindow(), "Export integrated report to Excel");
		setPreferredSize(new Dimension(640, 800));
		setIconImage(((ImageIcon) exportExcelIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(640, 800));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

		mainSplitPane = new JSplitPane();
		mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		mainSplitPane.setDividerSize(0);
		getContentPane().add(mainSplitPane, BorderLayout.CENTER);

		initChooser();
		mainSplitPane.setLeftComponent(chooser);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		mainSplitPane.setRightComponent(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		panel.add(panel_1, BorderLayout.SOUTH);

		cancelButton = new JButton("Cancel");
		panel_1.add(cancelButton);

		saveButton = new JButton("Save report");
		saveButton.addActionListener(this);
		saveButton.setActionCommand(SAVE_REPORT);
		panel_1.add(saveButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		JRootPane rootPane = SwingUtilities.getRootPane(mainSplitPane);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelButton.addActionListener(al);

		rootPane.setDefaultButton(saveButton);

		panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(0, 0, 10, 0));
		FlowLayout flowLayout_1 = (FlowLayout) panel_2.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		panel.add(panel_2, BorderLayout.NORTH);

		lblNewLabel = new JLabel("Specify parameters for integrated report:");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		panel_2.add(lblNewLabel);

		panel_3 = new JPanel();
		panel.add(panel_3, BorderLayout.CENTER);
		panel_3.setLayout(new BorderLayout(0, 0));

		panel_4 = new JPanel();
		panel_4.setBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select experimental design to export",
						TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 12), new Color(0, 0, 0)));
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

		lblNameBy = new JLabel("Name by ");
		GridBagConstraints gbc_lblNameBy = new GridBagConstraints();
		gbc_lblNameBy.anchor = GridBagConstraints.WEST;
		gbc_lblNameBy.insets = new Insets(0, 0, 0, 5);
		gbc_lblNameBy.gridx = 1;
		gbc_lblNameBy.gridy = 0;
		panel_4.add(lblNameBy, gbc_lblNameBy);

		DefaultComboBoxModel<DataExportFields> namingModel = new DefaultComboBoxModel<DataExportFields>(
				new DataExportFields[] { DataExportFields.SAMPLE_EXPORT_NAME, DataExportFields.SAMPLE_EXPORT_ID });
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
		featureSelectorGridPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"),
				"Select feature sets to export for each assay", TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 12),
				new Color(0, 0, 0)));
		panel_3.add(featureSelectorGridPanel, BorderLayout.CENTER);

		panel_5 = new JPanel();
		panel_5.setBorder(new EmptyBorder(10, 10, 10, 10));
		panel_3.add(panel_5, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_5 = new GridBagLayout();
		gbl_panel_5.columnWidths = new int[]{0, 0, 0};
		gbl_panel_5.rowHeights = new int[]{0, 0};
		gbl_panel_5.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_5.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_5.setLayout(gbl_panel_5);

		lblIntegratedDataSet = new JLabel("Integrated data set:");
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

		pack();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(SAVE_REPORT)) {

			chooser.approveSelection();
			File exportFile = chooser.getSelectedFile();
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
			setVisible(false);
		}
	}

	@SuppressWarnings("unchecked")
	private void clearPanel() {

		integratedSeComboBox.setModel(new DefaultComboBoxModel<MsFeatureSet>());
		featureSelectorGridPanel.removeAll();
	}

	private String createExportFile() {

		DataAnalysisProject currentProject = MRC2ToolBoxCore.getCurrentProject();
		String timestamp = dateTimeFormat.format(new Date());
		String fileName = currentProject.getName() + "_INTEGRATED_REPORT_" + timestamp + ".xlsx";
		return fileName;
	}

	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setControlButtonsAreShown(false);
		chooser.getActionMap().get("viewTypeDetails").actionPerformed(null);

		baseDirectory = new File(MRC2ToolBoxConfiguration.getDefaultProjectsDirectory()).getAbsoluteFile();
		chooser.setCurrentDirectory(baseDirectory);

		excelFilter = new FileNameExtensionFilter("Excel files", "xlsx");
		chooser.addChoosableFileFilter(excelFilter);
	}

	@Override
	public void paint(Graphics g) {

		super.paint(g);

		if (!painted) {

			painted = true;

			mainSplitPane.setDividerLocation(0.5);
			mainSplitPane.setResizeWeight(0.5);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void setPanelDataFromProject(DataAnalysisProject currentProject) {

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
		revalidate();
		repaint();
	}


	@Override
	public void setVisible(boolean visible){

		if(visible){

			DataAnalysisProject currentProject = MRC2ToolBoxCore.getCurrentProject();
			if(currentProject != null){

				baseDirectory = currentProject.getExportsDirectory();
				chooser.setCurrentDirectory(baseDirectory);
				setPanelDataFromProject(currentProject);
				String exportFileName = createExportFile();
				chooser.setSelectedFile(new File(baseDirectory.getPath() + File.separator + exportFileName));
			}
			else{
				baseDirectory = new File(MRC2ToolBoxConfiguration.getDefaultProjectsDirectory()).getAbsoluteFile();
				chooser.setCurrentDirectory(baseDirectory);
				clearPanel();
			}
			chooser.rescanCurrentDirectory();
		}
		super.setVisible(visible);
	}

}
