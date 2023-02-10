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

package edu.umich.med.mrc2.datoolbox.gui.library.search;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class LibrarySearchSetupDialog extends JDialog implements BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = 4926779789933757918L;

	private LibSearchLibraryTable libraryTable;
	private JFormattedTextField massErrorTextField;
	private JComboBox massErrorTypeComboBox;
	private JFormattedTextField rtWindowTextField;
	private JButton cancelButton;
	private JButton searchButton;
	private Preferences preferences;
	private JCheckBox ignoreAddudctTypeCheckBox;
	private JCheckBox relaxMassErrorCheckBox;
	private Set<MsFeature>featuresToSearch;
	private JSpinner maxHitsSpinner;
	private JCheckBox useCustomRtWindowsCheckBox;

	public static final String MASS_ERROR_VALUE = "MASS_ERROR_VALUE";
	public static final String MASS_ERROR_TYPE = "MASS_ERROR_TYPE";
	public static final String RETENTION_WINDOW = "RETENTION_WINDOW";
	public static final String MAX_HITS = "MAX_HITS";
	public static final String IGNORE_ADDUCT_TYPE = "IGNORE_ADDUCT_TYPE";
	public static final String RELAX_MASS_ERROR = "RELAX_MASS_ERROR";
	public static final String USE_CUSTOM_RT_WINDOWS = "USE_CUSTOM_RT_WINDOWS";

	private static final Icon searchLibraryIcon = GuiUtils.getIcon("searchLibrary", 32);

	public LibrarySearchSetupDialog(ActionListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "Setup library search", true);
		setIconImage(((ImageIcon) searchLibraryIcon).getImage());

		setSize(new Dimension(640, 480));
		setPreferredSize(new Dimension(640, 480));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		JSplitPane splitPane = new JSplitPane();
		splitPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		splitPane.setResizeWeight(0.3);
		splitPane.setDividerSize(1);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Search parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPane.setLeftComponent(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{89, 0, 52, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblMassError = new JLabel("Mass error");
		GridBagConstraints gbc_lblMassError = new GridBagConstraints();
		gbc_lblMassError.insets = new Insets(0, 0, 5, 5);
		gbc_lblMassError.anchor = GridBagConstraints.EAST;
		gbc_lblMassError.gridx = 0;
		gbc_lblMassError.gridy = 0;
		panel.add(lblMassError, gbc_lblMassError);

		massErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		massErrorTextField.setPreferredSize(new Dimension(6, 25));
		massErrorTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 0;
		panel.add(massErrorTextField, gbc_formattedTextField);

		massErrorTypeComboBox = new JComboBox();
		massErrorTypeComboBox.setModel(new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		massErrorTypeComboBox.setPreferredSize(new Dimension(100, 25));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 2;
		gbc_comboBox.gridy = 0;
		panel.add(massErrorTypeComboBox, gbc_comboBox);

		JLabel lblRtWindow = new JLabel("RT window");
		GridBagConstraints gbc_lblRtWindow = new GridBagConstraints();
		gbc_lblRtWindow.anchor = GridBagConstraints.EAST;
		gbc_lblRtWindow.insets = new Insets(0, 0, 5, 5);
		gbc_lblRtWindow.gridx = 0;
		gbc_lblRtWindow.gridy = 1;
		panel.add(lblRtWindow, gbc_lblRtWindow);

		rtWindowTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtWindowTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField_1 = new GridBagConstraints();
		gbc_formattedTextField_1.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField_1.gridx = 1;
		gbc_formattedTextField_1.gridy = 1;
		panel.add(rtWindowTextField, gbc_formattedTextField_1);

		JLabel lblMin = new JLabel("min.");
		GridBagConstraints gbc_lblMin = new GridBagConstraints();
		gbc_lblMin.insets = new Insets(0, 0, 5, 5);
		gbc_lblMin.anchor = GridBagConstraints.WEST;
		gbc_lblMin.gridx = 2;
		gbc_lblMin.gridy = 1;
		panel.add(lblMin, gbc_lblMin);

		useCustomRtWindowsCheckBox = new JCheckBox("Use individual RT window when available");
		GridBagConstraints gbc_useCustomRtWindowsCheckBox = new GridBagConstraints();
		gbc_useCustomRtWindowsCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_useCustomRtWindowsCheckBox.gridx = 3;
		gbc_useCustomRtWindowsCheckBox.gridy = 1;
		panel.add(useCustomRtWindowsCheckBox, gbc_useCustomRtWindowsCheckBox);

		JLabel lblMaxHits = new JLabel("Max. hits");
		GridBagConstraints gbc_lblMaxHits = new GridBagConstraints();
		gbc_lblMaxHits.anchor = GridBagConstraints.EAST;
		gbc_lblMaxHits.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaxHits.gridx = 0;
		gbc_lblMaxHits.gridy = 2;
		panel.add(lblMaxHits, gbc_lblMaxHits);

		maxHitsSpinner = new JSpinner();
		maxHitsSpinner.setModel(new SpinnerNumberModel(1, 1, 20, 1));
		GridBagConstraints gbc_maxHitsSpinner = new GridBagConstraints();
		gbc_maxHitsSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxHitsSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_maxHitsSpinner.gridx = 1;
		gbc_maxHitsSpinner.gridy = 2;
		panel.add(maxHitsSpinner, gbc_maxHitsSpinner);

		ignoreAddudctTypeCheckBox = new JCheckBox("Ignore addudct type");
		GridBagConstraints gbc_ignoreAddudctTypeCheckBox = new GridBagConstraints();
		gbc_ignoreAddudctTypeCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_ignoreAddudctTypeCheckBox.gridx = 1;
		gbc_ignoreAddudctTypeCheckBox.gridy = 3;
		panel.add(ignoreAddudctTypeCheckBox, gbc_ignoreAddudctTypeCheckBox);

		relaxMassErrorCheckBox = new JCheckBox("Relax mass error for minor isotopes");
		GridBagConstraints gbc_relaxMassErrorCheckBox = new GridBagConstraints();
		gbc_relaxMassErrorCheckBox.anchor = GridBagConstraints.WEST;
		gbc_relaxMassErrorCheckBox.gridwidth = 2;
		gbc_relaxMassErrorCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_relaxMassErrorCheckBox.gridx = 2;
		gbc_relaxMassErrorCheckBox.gridy = 3;
		panel.add(relaxMassErrorCheckBox, gbc_relaxMassErrorCheckBox);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(new TitledBorder(null, "Library selection", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splitPane.setRightComponent(scrollPane);

		libraryTable = new LibSearchLibraryTable();
		scrollPane.setViewportView(libraryTable);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(null);
		getContentPane().add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

		cancelButton = new JButton("Cancel");
		panel_1.add(cancelButton);

		searchButton = new JButton("Search");
		searchButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		searchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		searchButton.setActionCommand(MainActionCommands.SEARCH_FEATURES_AGAINST_LIBRARIES_COMMAND.getName());
		searchButton.addActionListener(listener);
		panel_1.add(searchButton);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		cancelButton.addActionListener(al);

		JRootPane rootPane = SwingUtilities.getRootPane(searchButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(searchButton);

		featuresToSearch = new HashSet<MsFeature>();
		loadPreferences();

		pack();
		setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		setVisible(false);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;

		double massError = preferences.getDouble(MASS_ERROR_VALUE, 10.0d);
		massErrorTextField.setText(Double.toString(massError));

		String massErrorType = preferences.get(MASS_ERROR_TYPE, MassErrorType.ppm.name());

		for(MassErrorType errorType : MassErrorType.values()) {

			if(errorType.name().equals(massErrorType)) {
				massErrorTypeComboBox.setSelectedItem(errorType);
				break;
			}
		}
		double rtWindow = preferences.getDouble(RETENTION_WINDOW, 0.15d);
		rtWindowTextField.setText(Double.toString(rtWindow));

		boolean useCustomRtWindows = preferences.getBoolean(USE_CUSTOM_RT_WINDOWS, Boolean.TRUE);
		useCustomRtWindowsCheckBox.setSelected(useCustomRtWindows);

		int maxHits = preferences.getInt(MAX_HITS, 2);
		maxHitsSpinner.setValue(maxHits);

		boolean ignoreAdductType = preferences.getBoolean(IGNORE_ADDUCT_TYPE, Boolean.FALSE);
		ignoreAddudctTypeCheckBox.setSelected(ignoreAdductType);

		boolean relaxMassError = preferences.getBoolean(RELAX_MASS_ERROR, Boolean.TRUE);
		relaxMassErrorCheckBox.setSelected(relaxMassError);
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.putDouble(MASS_ERROR_VALUE, Double.parseDouble(massErrorTextField.getText()));
		preferences.put(MASS_ERROR_TYPE, ((MassErrorType)massErrorTypeComboBox.getSelectedItem()).name());
		preferences.putDouble(RETENTION_WINDOW, Double.parseDouble(rtWindowTextField.getText()));
		preferences.putBoolean(USE_CUSTOM_RT_WINDOWS, useCustomRtWindowsCheckBox.isSelected());
		preferences.putInt(MAX_HITS, (int) maxHitsSpinner.getValue());
		preferences.putBoolean(IGNORE_ADDUCT_TYPE, ignoreAddudctTypeCheckBox.isSelected());
		preferences.putBoolean(RELAX_MASS_ERROR, relaxMassErrorCheckBox.isSelected());
	}

	@Override
	public void setVisible(boolean visible) {

		if(!visible)
			savePreferences();
		else
			repopulateLibraryTable();

		super.setVisible(visible);
	}

	private void repopulateLibraryTable() {

		TreeSet<CompoundLibrary> libSet = new TreeSet<CompoundLibrary>();
		DataAnalysisProject project = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		if(project != null) {

			if(project.getActiveDataPipeline() != null) {

				for(CompoundLibrary l : MRC2ToolBoxCore.getActiveMsLibraries()) {

					//	TODO check for polarity?
					//if(l.getAssayMethod().getPolarity().equals(project.getActiveAssay().getPolarity()))
						libSet.add(l);
				}
			}
		}
		libraryTable.setTableModelFromLibraryCollection(libSet);
	}

	public void setSingleFeatureToSearch(MsFeature f) {

		featuresToSearch.clear();
		featuresToSearch.add(f);
	}

	public void setMultipleFeaturesToSearch(Collection<MsFeature> fc) {

		featuresToSearch.clear();
		featuresToSearch.addAll(fc);
	}

	public List<CompoundLibrary>getSelectedLibraries(){

		return libraryTable.getSelectedLibraries();
	}

	public Set<MsFeature> getFeaturesToSearch() {
		return featuresToSearch;
	}

	public double getMassError() {

		return Double.parseDouble(massErrorTextField.getText());
	}

	public MassErrorType getMassErrorType() {

		return (MassErrorType) massErrorTypeComboBox.getSelectedItem();
	}

	public double getRetentionWindow() {

		return Double.parseDouble(rtWindowTextField.getText());
	}

	public int getMaxHits() {

		return (int) maxHitsSpinner.getValue();
	}

	public boolean relaxMassError() {

		return relaxMassErrorCheckBox.isSelected();
	}

	public boolean ignoreAddudctType() {

		return ignoreAddudctTypeCheckBox.isSelected();
	}

	public boolean useCustomRtWindows() {

		return useCustomRtWindowsCheckBox.isSelected();
	}

	public void updateData() {
		// TODO Auto-generated method stub

	}
}
