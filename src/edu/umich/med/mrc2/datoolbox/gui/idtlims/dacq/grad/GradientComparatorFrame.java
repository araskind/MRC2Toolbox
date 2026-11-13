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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.dacq.grad;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdom2.Document;
import org.jdom2.Element;

import edu.umich.med.mrc2.datoolbox.data.enums.SolventChannel;
import edu.umich.med.mrc2.datoolbox.data.lims.ChromatographicGradient;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.store.ObjectNames;
import edu.umich.med.mrc2.datoolbox.utils.XmlUtils;

public class GradientComparatorFrame extends JFrame 
		implements ActionListener, ListSelectionListener, ItemListener , BackedByPreferences {

	private static final long serialVersionUID = 1L;
	
	private Preferences preferences;
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	public static final String SHOW_SOLVENT_PERCENT = "SHOW_SOLVENT_PERCENT";
	public static final String SHOW_FLOW_RATE = "BASE_DIRECTORY";
	public static final String CHANNEL_A = "CHANNEL_A";
	public static final String CHANNEL_B = "CHANNEL_B";
	public static final String CHANNEL_C = "CHANNEL_C";
	public static final String CHANNEL_D = "CHANNEL_D";

	private static final Icon frameIcon = GuiUtils.getIcon("gradient", 32);
	private GradientChartPanel gradientChartPanel;
	private ChromatographicGradientTable gradientOneTable;
	private ChromatographicGradientTable gradientTwoTable;
	private GradientMobilePhaseTable mobilePhaseTableOne;
	private GradientMobilePhaseTable mobilePhaseTableTwo;
	private GradientListingTable gradientListingTableOne;
	private GradientListingTable gradientListingTableTwo;
		
	private Collection<ChromatographicGradient>gradientList;
	private ChromatographicGradient activeGradientOne;
	private ChromatographicGradient activeGradientTwo;
	
	private JCheckBox solventPercentCheckBox;
	private JCheckBox flowRateCheckBox;
	private JCheckBox channelAcheckBox;
	private JCheckBox channelBcheckBox;
	private JCheckBox channelCcheckBox;
	private JCheckBox channelDcheckBox;
	
	private File baseDirectory;
	
	public GradientComparatorFrame() throws HeadlessException {
		super("MS1 multi-spectra plot");		
		setIconImage(((ImageIcon) frameIcon).getImage());
		setPreferredSize(new Dimension(1200, 1000));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel dataPanel = new JPanel(new BorderLayout(0, 0));
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		
		JToolBar toolBar = new JToolBar();
		dataPanel.add(toolBar, BorderLayout.NORTH);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(1, 1, 1, 1));
		dataPanel.add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{200, 300, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 1.0, 1.0, 1.0};
		gbl_panel.rowWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		gradientListingTableOne = new GradientListingTable();
		gradientListingTableOne.getSelectionModel().addListSelectionListener(this);
		GridBagConstraints gbc_table = new GridBagConstraints();
		gbc_table.insets = new Insets(0, 0, 5, 5);
		gbc_table.fill = GridBagConstraints.BOTH;
		gbc_table.gridx = 0;
		gbc_table.gridy = 0;
		JScrollPane scrollPane = new JScrollPane(gradientListingTableOne);
		scrollPane.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
						"Gradient #1", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(10, 5, 5, 5)));
		panel.add(scrollPane, gbc_table);
		
		mobilePhaseTableOne = new GradientMobilePhaseTable();
		JScrollPane scrollPaneMobPh = new JScrollPane(mobilePhaseTableOne);
		scrollPaneMobPh.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
						"Mobile phases #1", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(10, 5, 5, 5)));
		GridBagConstraints gbc_mobilePhaseTableOne = new GridBagConstraints();
		gbc_mobilePhaseTableOne.insets = new Insets(0, 0, 5, 5);
		gbc_mobilePhaseTableOne.fill = GridBagConstraints.BOTH;
		gbc_mobilePhaseTableOne.gridx = 1;
		gbc_mobilePhaseTableOne.gridy = 0;
		panel.add(scrollPaneMobPh, gbc_mobilePhaseTableOne);
		
		gradientListingTableTwo = new GradientListingTable();
		gradientListingTableTwo.getSelectionModel().addListSelectionListener(this);
		GridBagConstraints gbc_table_1 = new GridBagConstraints();
		gbc_table_1.insets = new Insets(0, 0, 5, 5);
		gbc_table_1.fill = GridBagConstraints.BOTH;
		gbc_table_1.gridx = 2;
		gbc_table_1.gridy = 0;
		JScrollPane scrollPaneTwo = new JScrollPane(gradientListingTableTwo);
		scrollPaneTwo.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
						"Gradient #2", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(10, 5, 5, 5)));
		panel.add(scrollPaneTwo, gbc_table_1);
		
		mobilePhaseTableTwo = new GradientMobilePhaseTable();
		JScrollPane scrollPaneMobPhTwo = new JScrollPane(mobilePhaseTableTwo);
		scrollPaneMobPhTwo.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
						"Mobile phases #2", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(10, 5, 5, 5)));
		GridBagConstraints gbc_mobilePhaseTableTwo = new GridBagConstraints();
		gbc_mobilePhaseTableTwo.insets = new Insets(0, 0, 5, 0);
		gbc_mobilePhaseTableTwo.fill = GridBagConstraints.BOTH;
		gbc_mobilePhaseTableTwo.gridx = 3;
		gbc_mobilePhaseTableTwo.gridy = 0;
		panel.add(scrollPaneMobPhTwo, gbc_mobilePhaseTableTwo);
		
		gradientChartPanel = new GradientChartPanel();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		panel.add(gradientChartPanel, gbc_scrollPane);		  
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new CompoundBorder(new TitledBorder(null, "Data types to show", 
				TitledBorder.LEADING, TitledBorder.TOP, null, null), new EmptyBorder(5, 5, 5, 5)));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.gridwidth = 2;
		gbc_panel_1.insets = new Insets(0, 0, 5, 5);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 2;
		panel.add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		solventPercentCheckBox = new JCheckBox("Solvent %");
		solventPercentCheckBox.addItemListener(this);
		GridBagConstraints gbc_soventPercentCheckBox = new GridBagConstraints();
		gbc_soventPercentCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_soventPercentCheckBox.gridx = 0;
		gbc_soventPercentCheckBox.gridy = 0;
		panel_1.add(solventPercentCheckBox, gbc_soventPercentCheckBox);
		
		flowRateCheckBox = new JCheckBox("Flow rate");
		flowRateCheckBox.addItemListener(this);
		GridBagConstraints gbc_flowRateCheckBox = new GridBagConstraints();
		gbc_flowRateCheckBox.gridx = 1;
		gbc_flowRateCheckBox.gridy = 0;
		panel_1.add(flowRateCheckBox, gbc_flowRateCheckBox);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new CompoundBorder(new TitledBorder(null, "Solvent channels to show", 
				TitledBorder.LEADING, TitledBorder.TOP, null, null), new EmptyBorder(5, 5, 5, 5)));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.gridwidth = 2;
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 2;
		gbc_panel_2.gridy = 2;
		panel.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0};
		gbl_panel_2.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
		
		channelAcheckBox = new JCheckBox("A");
		channelAcheckBox.addItemListener(this);
		GridBagConstraints gbc_channelAcheckBox = new GridBagConstraints();
		gbc_channelAcheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_channelAcheckBox.gridx = 0;
		gbc_channelAcheckBox.gridy = 0;
		panel_2.add(channelAcheckBox, gbc_channelAcheckBox);
		
		channelBcheckBox = new JCheckBox("B");
		channelBcheckBox.addItemListener(this);
		GridBagConstraints gbc_channelBcheckBox = new GridBagConstraints();
		gbc_channelBcheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_channelBcheckBox.gridx = 1;
		gbc_channelBcheckBox.gridy = 0;
		panel_2.add(channelBcheckBox, gbc_channelBcheckBox);
		
		channelCcheckBox = new JCheckBox("C");
		channelCcheckBox.addItemListener(this);
		GridBagConstraints gbc_channelCcheckBox = new GridBagConstraints();
		gbc_channelCcheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_channelCcheckBox.gridx = 2;
		gbc_channelCcheckBox.gridy = 0;
		panel_2.add(channelCcheckBox, gbc_channelCcheckBox);
		
		channelDcheckBox = new JCheckBox("D");
		channelDcheckBox.addItemListener(this);
		GridBagConstraints gbc_channelDcheckBox = new GridBagConstraints();
		gbc_channelDcheckBox.gridx = 3;
		gbc_channelDcheckBox.gridy = 0;
		panel_2.add(channelDcheckBox, gbc_channelDcheckBox);
		  
		gradientOneTable = new ChromatographicGradientTable();
		JScrollPane scrollPaneThree = new JScrollPane(gradientOneTable);
		scrollPaneThree.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
						"Gradient #1 timetable", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(10, 5, 5, 5)));
		GridBagConstraints gbc_table_2 = new GridBagConstraints();
		gbc_table_2.gridwidth = 2;
		gbc_table_2.insets = new Insets(0, 0, 0, 5);
		gbc_table_2.fill = GridBagConstraints.BOTH;
		gbc_table_2.gridx = 0;
		gbc_table_2.gridy = 3;
		panel.add(scrollPaneThree, gbc_table_2);
		
		gradientTwoTable = new ChromatographicGradientTable();
		JScrollPane scrollPaneFour = new JScrollPane(gradientTwoTable);
				scrollPaneFour.setBorder(new CompoundBorder(
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
						"Gradient #2 timetable", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(10, 5, 5, 5)));
		GridBagConstraints gbc_table_3 = new GridBagConstraints();
		gbc_table_3.gridwidth = 2;
		gbc_table_3.fill = GridBagConstraints.BOTH;
		gbc_table_3.gridx = 2;
		gbc_table_3.gridy = 3;
		panel.add(scrollPaneFour, gbc_table_3);
		
		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Close");
		btnCancel.addActionListener(e -> dispose());
		buttonPanel.add(btnCancel);

		JButton btnSave = new JButton(
				MainActionCommands.LOAD_GRADIENTSFROM_FILE_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.LOAD_GRADIENTSFROM_FILE_COMMAND.getName());
		btnSave.addActionListener(this);
		buttonPanel.add(btnSave);
		
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		rootPane.registerKeyboardAction(al -> { dispose(); }, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);

		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.LOAD_GRADIENTSFROM_FILE_COMMAND.getName()))
			loadGradientsFromFile();
	}

	private void loadGradientsFromFile() {

		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("XML files", "xml", "XML");	;
		fc.setTitle("Select gardient list XML file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(SwingUtilities.getWindowAncestor(this.getContentPane()))) {		

			File inputFile = fc.getSelectedFile();
			baseDirectory = inputFile.getParentFile();
			savePreferences();
			importGradientsFromXMLFile(inputFile);
		}
	}

	private void importGradientsFromXMLFile(File gradientListFile) {
		clearPanel();
		gradientList = new ArrayList<>();
		Document gradientListDocument = XmlUtils.readXmlFile(gradientListFile);
		List<Element> gradientElementList = 
				gradientListDocument.getRootElement().getChildren(ObjectNames.ChromatographicGradient.name());
		for(Element ge : gradientElementList) {
			
			ChromatographicGradient grad = null;
			try {
				grad = new ChromatographicGradient(ge);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(grad != null)
				gradientList.add(grad);
		}
		gradientListingTableOne.setTableModelFromGradientCollection(gradientList);
		gradientListingTableTwo.setTableModelFromGradientCollection(gradientList);
	}

	public void clearPanel() {		
		gradientChartPanel.removeAllDataSets();
		gradientOneTable.clearTable();
		gradientTwoTable.clearTable();
		gradientListingTableOne.clearTable();
		gradientListingTableTwo.clearTable();		
		mobilePhaseTableOne.clearTable();
		mobilePhaseTableTwo.clearTable();
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(e.getValueIsAdjusting() || e.getSource() == null)
			return;

		for(ListSelectionListener listener : ((DefaultListSelectionModel)e.getSource()).getListSelectionListeners()) {

			if(listener.equals(gradientListingTableOne)) {
				activeGradientOne = gradientListingTableOne.getSelectedGradient();
				gradientOneTable.setTableModelFromGradient(activeGradientOne);
				if(activeGradientOne != null)
					mobilePhaseTableOne.setTableModelFromMobilePhaseArray(activeGradientOne.getMobilePhases());
				else
					mobilePhaseTableOne.clearTable();

				redrawGradients();
			}
			if(listener.equals(gradientListingTableTwo)) {
				activeGradientTwo = gradientListingTableTwo.getSelectedGradient();
				gradientTwoTable.setTableModelFromGradient(activeGradientTwo);
				if(activeGradientTwo != null)
					mobilePhaseTableTwo.setTableModelFromMobilePhaseArray(activeGradientTwo.getMobilePhases());
				else
					mobilePhaseTableTwo.clearTable();

				redrawGradients();
			}
		}		
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED || e.getStateChange() == ItemEvent.DESELECTED) {
			redrawGradients();
		}
	}
	
	private void redrawGradients() {

		GradientPlotParameters plotParams = new GradientPlotParameters();
		plotParams.setShowSolventPercent(solventPercentCheckBox.isSelected());
		plotParams.setShowFlowRate(flowRateCheckBox.isSelected());		
		plotParams.setChannelVisibility(SolventChannel.A, channelAcheckBox.isSelected());
		plotParams.setChannelVisibility(SolventChannel.B, channelBcheckBox.isSelected());
		plotParams.setChannelVisibility(SolventChannel.C, channelCcheckBox.isSelected());
		plotParams.setChannelVisibility(SolventChannel.A, channelDcheckBox.isSelected());
		
		gradientChartPanel.plotGradientPair(activeGradientOne, activeGradientTwo, plotParams);
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
		String baseDirPath = preferences.get(BASE_DIRECTORY, 
						MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory());
		try {
			baseDirectory = Paths.get(baseDirPath).toFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		solventPercentCheckBox.setSelected(preferences.getBoolean(SHOW_SOLVENT_PERCENT, true));
		flowRateCheckBox.setSelected(preferences.getBoolean(SHOW_FLOW_RATE, false));
		channelAcheckBox.setSelected(preferences.getBoolean(CHANNEL_A, true));
		channelBcheckBox.setSelected(preferences.getBoolean(CHANNEL_B, true));
		channelCcheckBox.setSelected(preferences.getBoolean(CHANNEL_C, false));
		channelDcheckBox.setSelected(preferences.getBoolean(CHANNEL_D, false));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
		preferences.putBoolean(SHOW_SOLVENT_PERCENT, solventPercentCheckBox.isSelected());
		preferences.putBoolean(SHOW_FLOW_RATE, flowRateCheckBox.isSelected());		
		preferences.putBoolean(CHANNEL_A, channelAcheckBox.isSelected());
		preferences.putBoolean(CHANNEL_B, channelBcheckBox.isSelected());
		preferences.putBoolean(CHANNEL_C, channelCcheckBox.isSelected());
		preferences.putBoolean(CHANNEL_D, channelDcheckBox.isSelected());
	}
}
