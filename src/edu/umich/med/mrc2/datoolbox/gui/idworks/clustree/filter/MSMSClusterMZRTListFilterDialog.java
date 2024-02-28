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

package edu.umich.med.mrc2.datoolbox.gui.idworks.clustree.filter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.FeatureListImportPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MSMSClusterMZRTListFilterDialog extends JDialog implements BackedByPreferences, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2851758377902206542L;
	
	private static final Icon filterIcon = GuiUtils.getIcon("filterClusterByMZRTList", 32);
	
	private Preferences preferences;
	public static final String PREFS_NODE = 
			"edu.umich.med.mrc2.datoolbox.gui.idworks.MSMSClusterMZRTListFilterDialog";
	private static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private static final String MZ_ERROR = "MZ_ERROR";
	private static final String MZ_ERROR_TYPE = "MZ_ERROR_TYPE";
	private static final String RT_ERROR = "RT_ERROR";
	
	private FeatureListImportPanel featureListImportPanel;
	private JFormattedTextField mzErrorTextField;
	private JComboBox mzErrorTypeComboBox;
	private JFormattedTextField rtErrorTextField;

	private File baseDirectory;
	
	public MSMSClusterMZRTListFilterDialog(ActionListener listener) {
		super();
		setTitle("Filter MSMS clusters using MZ/RT feature list");
		setIconImage(((ImageIcon)filterIcon).getImage());
		setPreferredSize(new Dimension(800, 800));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		featureListImportPanel = new FeatureListImportPanel();
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.gridwidth = 7;
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 0;
		panel_1.add(featureListImportPanel, gbc_panel_2);
		
		JLabel lblNewLabel_1 = new JLabel("M/Z window");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		mzErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		mzErrorTextField.setColumns(10);
		GridBagConstraints gbc_mzErrorTextField = new GridBagConstraints();
		gbc_mzErrorTextField.insets = new Insets(0, 0, 0, 5);
		gbc_mzErrorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mzErrorTextField.gridx = 1;
		gbc_mzErrorTextField.gridy = 1;
		panel_1.add(mzErrorTextField, gbc_mzErrorTextField);
		
		mzErrorTypeComboBox = new JComboBox<MassErrorType>(
				new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		mzErrorTypeComboBox.setPreferredSize(new Dimension(100, 22));
		mzErrorTypeComboBox.setMinimumSize(new Dimension(80, 22));
		GridBagConstraints gbc_mzErrorTypeomboBox = new GridBagConstraints();
		gbc_mzErrorTypeomboBox.insets = new Insets(0, 0, 0, 5);
		gbc_mzErrorTypeomboBox.anchor = GridBagConstraints.WEST;
		gbc_mzErrorTypeomboBox.gridx = 2;
		gbc_mzErrorTypeomboBox.gridy = 1;
		panel_1.add(mzErrorTypeComboBox, gbc_mzErrorTypeomboBox);
		
		JLabel lblNewLabel_3 = new JLabel("RT window");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.gridx = 4;
		gbc_lblNewLabel_3.gridy = 1;
		panel_1.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		rtErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtErrorTextField.setColumns(10);
		GridBagConstraints gbc_rtErrorTextField = new GridBagConstraints();
		gbc_rtErrorTextField.insets = new Insets(0, 0, 0, 5);
		gbc_rtErrorTextField.anchor = GridBagConstraints.WEST;
		gbc_rtErrorTextField.gridx = 5;
		gbc_rtErrorTextField.gridy = 1;
		panel_1.add(rtErrorTextField, gbc_rtErrorTextField);
		
		JLabel lblNewLabel_4 = new JLabel("min.");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel_4.gridx = 6;
		gbc_lblNewLabel_4.gridy = 1;
		panel_1.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton cancelButton = new JButton("Cancel");
		panel.add(cancelButton);

		JButton filterButton = 
				new JButton(MainActionCommands.FILTER_MSMS_CLUSTERS_WITH_MZ_RT_LIST_COMMAND.getName());
		filterButton.setActionCommand(
				MainActionCommands.FILTER_MSMS_CLUSTERS_WITH_MZ_RT_LIST_COMMAND.getName());
		filterButton.addActionListener(listener);
		panel.add(filterButton);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		cancelButton.addActionListener(al);

		JRootPane rootPane = SwingUtilities.getRootPane(filterButton);
		rootPane.registerKeyboardAction(al, stroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(filterButton);
		loadPreferences();
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

	}

	@Override
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}
	
	public double getMzError() {
		
		if(mzErrorTextField.getText().trim().isEmpty())
			return 0.0d;
		else
			return Double.parseDouble(mzErrorTextField.getText().trim());
	}
	
	public MassErrorType getMassErrorType() {
		return (MassErrorType)mzErrorTypeComboBox.getSelectedItem();
	}
	
	public double getRTError() {
		
		if(rtErrorTextField.getText().trim().isEmpty())
			return 0.0d;
		else
			return Double.parseDouble(rtErrorTextField.getText().trim());
	}
	
	
	public Collection<String>validateInput(){
		
		Collection<String>errors = new ArrayList<String>();

			
		if(featureListImportPanel.getAllFeatures().isEmpty())
			errors.add("No search data specified (empty MZ/RT list)");
		
		if(getMzError() <= 0.0d)
			errors.add("M/Z window must be > 0");
		
		if(getRTError() <= 0.0d)
			errors.add("RT window must be > 0");

		return errors;
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		
		preferences = prefs;
		baseDirectory =  new File(preferences.get(
				BASE_DIRECTORY, MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
		featureListImportPanel.setBaseDirectory(baseDirectory);
		
		double mzError = preferences.getDouble(MZ_ERROR, 20.0d);
		if(mzError > 0.0d)
			mzErrorTextField.setText(Double.toString(mzError));
		else
			mzErrorTextField.setText("");
		
		MassErrorType met = MassErrorType.getTypeByName(
				preferences.get(MZ_ERROR_TYPE, MassErrorType.ppm.name()));
		mzErrorTypeComboBox.setSelectedItem(met);
		
		double rtError = preferences.getDouble(RT_ERROR, 0.15d);
		if(rtError > 0.0d)
			rtErrorTextField.setText(Double.toString(rtError));
		else
			rtErrorTextField.setText("");
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		
		preferences = Preferences.userRoot().node(PREFS_NODE);
		baseDirectory = featureListImportPanel.getBaseDirectory();
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
		
		preferences.putDouble(MZ_ERROR, getMzError());
		MassErrorType met = getMassErrorType();
		if(met == null)
			met = MassErrorType.ppm;
		
		preferences.put(MZ_ERROR_TYPE, met.name());
		preferences.putDouble(RT_ERROR, getRTError());
	}
	
	public Collection<MinimalMSOneFeature>getSelectedFeatures(){
		return featureListImportPanel.getSelectedFeatures();
	}
	
	public Collection<MinimalMSOneFeature>getAllFeatures(){
		return featureListImportPanel.getAllFeatures();
	}
}
