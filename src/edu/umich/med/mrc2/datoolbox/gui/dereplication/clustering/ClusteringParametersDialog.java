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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.clustering;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.ujmp.core.doublematrix.calculation.general.missingvalues.Impute.ImputationMethod;

import edu.umich.med.mrc2.datoolbox.data.enums.DataImputationType;
import edu.umich.med.mrc2.datoolbox.data.enums.SlidingWindowUnit;
import edu.umich.med.mrc2.datoolbox.gui.binner.control.CorrelationFunctionType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class ClusteringParametersDialog extends JDialog implements BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = 7866159191706849074L;
	private JComboBox corrAlgoComboBox;
	private JFormattedTextField maxClusterWidthTextField;
	private JComboBox windowSlidingTypeComboBox;
	private JSpinner featureWindowSpinner;
	private JFormattedTextField timeWindowTextField;
	private JButton btnCancel;
	private JButton btnRunAnalysis;
	private JPanel buttonPanel;
	private JTabbedPane tabbedPane;
	private JPanel dataPrepSettingsPanel;
	private JPanel corrSetingsPanel;
	private JLabel lblCorrCutoff;
	private JFormattedTextField corrCutoffTextField;
	private JCheckBox limitRtCheckBox;
	private JLabel lblFrom;
	private JFormattedTextField fromRtTextField;
	private JLabel lblTo;
	private JFormattedTextField toRtTextField;
	private JCheckBox filterMissingChkBox;
	private JFormattedTextField maxMissingPercentTextField;
	private JLabel lblMissingValues;
	private JCheckBox chckbxImputeMissingData;
	private JComboBox imputationAlgorithmComboBox;
	private JLabel lblOfClusters;
	private JSpinner kMeansNumSpinner;

	private Preferences preferences;
	public static final String PREFS_NODE = "gui.dereplication.clustering.ClusteringParametersDialog";
	public static final String CORRELATION_ALGORITHM = "CORRELATION_ALGORITHM";
	public static final String CORRELATION_CUTOFF = "CORRELATION_CUTOFF";
	public static final String MAX_CLUSTER_WIDTH = "MAX_CLUSTER_WIDTH";
	public static final String WINDOW_SLIDING_UNIT = "WINDOW_SLIDING_UNIT";
	public static final String NUM_FEATURES_WINDOW = "NUM_FEATURES_WINDOW";
	public static final String TIME_WINDOW = "TIME_WINDOW";
	public static final String LIMIT_RT_RANGE = "LIMIT_RT_RANGE";
	public static final String RT_FROM = "RT_FROM";
	public static final String RT_TO = "RT_TO";
	public static final String FILTER_BY_FREQUENCY = "FILTER_BY_FREQUENCY";
	public static final String MAX_MISSING = "MAX_MISSING";
	public static final String IMPUTE_MISING = "IMPUTE_MISING";
	public static final String IMPUTATION_ALGORITHM = "IMPUTATION_ALGORITHM";
	public static final String NUM_KNN_CLUSTERS = "NUM_KNN_CLUSTERS";

	public ClusteringParametersDialog(ActionListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "Set parameters for feature correlation analysis", true);

		setSize(new Dimension(450, 250));
		setPreferredSize(new Dimension(450, 250));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.NORTH);

		createDataPrepSettingdPanel();
		tabbedPane.addTab("Data preparation settings", null, dataPrepSettingsPanel, null);

		createCorrelationSettingdPanel();
		tabbedPane.addTab("Correlation settings", null, corrSetingsPanel, null);

		buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		btnCancel.addActionListener(al);

		btnRunAnalysis = new JButton("Run analysis");
		buttonPanel.add(btnRunAnalysis);
		btnRunAnalysis.setActionCommand(MainActionCommands.FIND_FEATURE_CORRELATIONS_COMMAND.getName());
		btnRunAnalysis.addActionListener(listener);
		JRootPane rootPane = SwingUtilities.getRootPane(btnRunAnalysis);
		rootPane.setDefaultButton(btnRunAnalysis);

		pack();
	}

	private void createDataPrepSettingdPanel() {

		dataPrepSettingsPanel = new JPanel();

		dataPrepSettingsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		tabbedPane.addTab("Data preparation settings", null, dataPrepSettingsPanel, null);
		GridBagLayout gbl_dataPrepSettingsPanel = new GridBagLayout();
		gbl_dataPrepSettingsPanel.columnWidths = new int[]{0, 28, 57, 30, 54, 0};
		gbl_dataPrepSettingsPanel.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_dataPrepSettingsPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_dataPrepSettingsPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		dataPrepSettingsPanel.setLayout(gbl_dataPrepSettingsPanel);

		limitRtCheckBox = new JCheckBox("Limit retention time");
		GridBagConstraints gbc_limitRtCheckBox = new GridBagConstraints();
		gbc_limitRtCheckBox.anchor = GridBagConstraints.WEST;
		gbc_limitRtCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_limitRtCheckBox.gridx = 0;
		gbc_limitRtCheckBox.gridy = 0;
		dataPrepSettingsPanel.add(limitRtCheckBox, gbc_limitRtCheckBox);

		lblFrom = new JLabel("from");
		GridBagConstraints gbc_lblFrom = new GridBagConstraints();
		gbc_lblFrom.insets = new Insets(0, 0, 5, 5);
		gbc_lblFrom.anchor = GridBagConstraints.EAST;
		gbc_lblFrom.gridx = 1;
		gbc_lblFrom.gridy = 0;
		dataPrepSettingsPanel.add(lblFrom, gbc_lblFrom);

		fromRtTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		fromRtTextField.setColumns(10);
		GridBagConstraints gbc_fromRtTextField = new GridBagConstraints();
		gbc_fromRtTextField.insets = new Insets(0, 0, 5, 5);
		gbc_fromRtTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_fromRtTextField.gridx = 2;
		gbc_fromRtTextField.gridy = 0;
		dataPrepSettingsPanel.add(fromRtTextField, gbc_fromRtTextField);

		lblTo = new JLabel("to");
		GridBagConstraints gbc_lblTo = new GridBagConstraints();
		gbc_lblTo.insets = new Insets(0, 0, 5, 5);
		gbc_lblTo.anchor = GridBagConstraints.EAST;
		gbc_lblTo.gridx = 3;
		gbc_lblTo.gridy = 0;
		dataPrepSettingsPanel.add(lblTo, gbc_lblTo);

		toRtTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		toRtTextField.setColumns(10);
		GridBagConstraints gbc_toRtTextField = new GridBagConstraints();
		gbc_toRtTextField.insets = new Insets(0, 0, 5, 0);
		gbc_toRtTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_toRtTextField.gridx = 4;
		gbc_toRtTextField.gridy = 0;
		dataPrepSettingsPanel.add(toRtTextField, gbc_toRtTextField);

		filterMissingChkBox = new JCheckBox("Exclude data with more than");
		GridBagConstraints gbc_chckbxExcludeDataWith = new GridBagConstraints();
		gbc_chckbxExcludeDataWith.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxExcludeDataWith.gridx = 0;
		gbc_chckbxExcludeDataWith.gridy = 1;
		dataPrepSettingsPanel.add(filterMissingChkBox, gbc_chckbxExcludeDataWith);

		maxMissingPercentTextField = new JFormattedTextField(new DecimalFormat("##.#"));
		maxMissingPercentTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField_1 = new GridBagConstraints();
		gbc_formattedTextField_1.gridwidth = 2;
		gbc_formattedTextField_1.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField_1.gridx = 1;
		gbc_formattedTextField_1.gridy = 1;
		dataPrepSettingsPanel.add(maxMissingPercentTextField, gbc_formattedTextField_1);

		lblMissingValues = new JLabel("% missing values");
		GridBagConstraints gbc_lblMissingValues = new GridBagConstraints();
		gbc_lblMissingValues.insets = new Insets(0, 0, 5, 0);
		gbc_lblMissingValues.gridwidth = 2;
		gbc_lblMissingValues.anchor = GridBagConstraints.WEST;
		gbc_lblMissingValues.gridx = 3;
		gbc_lblMissingValues.gridy = 1;
		dataPrepSettingsPanel.add(lblMissingValues, gbc_lblMissingValues);

		chckbxImputeMissingData = new JCheckBox("Impute missing data using");
		GridBagConstraints gbc_chckbxImputeMissingData = new GridBagConstraints();
		gbc_chckbxImputeMissingData.anchor = GridBagConstraints.WEST;
		gbc_chckbxImputeMissingData.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxImputeMissingData.gridx = 0;
		gbc_chckbxImputeMissingData.gridy = 2;
		dataPrepSettingsPanel.add(chckbxImputeMissingData, gbc_chckbxImputeMissingData);

		imputationAlgorithmComboBox = new JComboBox();
		imputationAlgorithmComboBox.setModel(new DefaultComboBoxModel<DataImputationType>(DataImputationType.values()));
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.gridwidth = 4;
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 2;
		dataPrepSettingsPanel.add(imputationAlgorithmComboBox, gbc_comboBox);

		lblOfClusters = new JLabel("# of clusters for K-means impute");
		GridBagConstraints gbc_lblOfClusters = new GridBagConstraints();
		gbc_lblOfClusters.insets = new Insets(0, 0, 0, 5);
		gbc_lblOfClusters.gridx = 0;
		gbc_lblOfClusters.gridy = 3;
		dataPrepSettingsPanel.add(lblOfClusters, gbc_lblOfClusters);

		kMeansNumSpinner = new JSpinner();
		kMeansNumSpinner.setModel(new SpinnerNumberModel(3, 2, 10, 1));
		kMeansNumSpinner.setMinimumSize(new Dimension(50, 20));
		kMeansNumSpinner.setSize(new Dimension(50, 20));
		kMeansNumSpinner.setPreferredSize(new Dimension(50, 20));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.anchor = GridBagConstraints.WEST;
		gbc_spinner.gridwidth = 2;
		gbc_spinner.insets = new Insets(0, 0, 0, 5);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 3;
		dataPrepSettingsPanel.add(kMeansNumSpinner, gbc_spinner);
	}


	private void createCorrelationSettingdPanel() {

		corrSetingsPanel = new JPanel();
		corrSetingsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 97, 70, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		corrSetingsPanel.setLayout(gbl_panel);

		JLabel lblCorrelationAlgorithm = new JLabel("Correlation algorithm");
		GridBagConstraints gbc_lblCorrelationAlgorithm = new GridBagConstraints();
		gbc_lblCorrelationAlgorithm.insets = new Insets(0, 0, 5, 5);
		gbc_lblCorrelationAlgorithm.anchor = GridBagConstraints.EAST;
		gbc_lblCorrelationAlgorithm.gridx = 0;
		gbc_lblCorrelationAlgorithm.gridy = 0;
		corrSetingsPanel.add(lblCorrelationAlgorithm, gbc_lblCorrelationAlgorithm);

		corrAlgoComboBox = new JComboBox<CorrelationFunctionType>(
				new DefaultComboBoxModel<CorrelationFunctionType>(
						CorrelationFunctionType.values()));
		GridBagConstraints gbc_corrAlgoComboBox = new GridBagConstraints();
		gbc_corrAlgoComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_corrAlgoComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_corrAlgoComboBox.gridx = 1;
		gbc_corrAlgoComboBox.gridy = 0;
		corrSetingsPanel.add(corrAlgoComboBox, gbc_corrAlgoComboBox);

		lblCorrCutoff = new JLabel("Corr. cutoff");
		GridBagConstraints gbc_lblCorrCutoff = new GridBagConstraints();
		gbc_lblCorrCutoff.anchor = GridBagConstraints.EAST;
		gbc_lblCorrCutoff.insets = new Insets(0, 0, 5, 5);
		gbc_lblCorrCutoff.gridx = 2;
		gbc_lblCorrCutoff.gridy = 0;
		corrSetingsPanel.add(lblCorrCutoff, gbc_lblCorrCutoff);

		corrCutoffTextField = new JFormattedTextField();
		corrCutoffTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 0);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 3;
		gbc_formattedTextField.gridy = 0;
		corrSetingsPanel.add(corrCutoffTextField, gbc_formattedTextField);

		JLabel lblMaxClusterWidth = new JLabel("Max cluster width, seconds");
		GridBagConstraints gbc_lblMaxClusterWidth = new GridBagConstraints();
		gbc_lblMaxClusterWidth.anchor = GridBagConstraints.EAST;
		gbc_lblMaxClusterWidth.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaxClusterWidth.gridx = 0;
		gbc_lblMaxClusterWidth.gridy = 1;
		corrSetingsPanel.add(lblMaxClusterWidth, gbc_lblMaxClusterWidth);

		maxClusterWidthTextField = new JFormattedTextField(new DecimalFormat("##.#"));
		maxClusterWidthTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField_1 = new GridBagConstraints();
		gbc_formattedTextField_1.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField_1.gridx = 1;
		gbc_formattedTextField_1.gridy = 1;
		corrSetingsPanel.add(maxClusterWidthTextField, gbc_formattedTextField_1);

		JLabel lblSlideWindowBy = new JLabel("Slide window by");
		GridBagConstraints gbc_lblSlideWindowBy = new GridBagConstraints();
		gbc_lblSlideWindowBy.anchor = GridBagConstraints.EAST;
		gbc_lblSlideWindowBy.insets = new Insets(0, 0, 5, 5);
		gbc_lblSlideWindowBy.gridx = 0;
		gbc_lblSlideWindowBy.gridy = 2;
		corrSetingsPanel.add(lblSlideWindowBy, gbc_lblSlideWindowBy);

		windowSlidingTypeComboBox = new JComboBox();
		windowSlidingTypeComboBox.setModel(new DefaultComboBoxModel<SlidingWindowUnit>(SlidingWindowUnit.values()));
		GridBagConstraints gbc_windowSlidingTypeComboBox = new GridBagConstraints();
		gbc_windowSlidingTypeComboBox.gridwidth = 2;
		gbc_windowSlidingTypeComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_windowSlidingTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_windowSlidingTypeComboBox.gridx = 1;
		gbc_windowSlidingTypeComboBox.gridy = 2;
		corrSetingsPanel.add(windowSlidingTypeComboBox, gbc_windowSlidingTypeComboBox);

		JLabel lblFeatureNum = new JLabel("# of features");
		GridBagConstraints gbc_lblFeatureNum = new GridBagConstraints();
		gbc_lblFeatureNum.anchor = GridBagConstraints.EAST;
		gbc_lblFeatureNum.insets = new Insets(0, 0, 5, 5);
		gbc_lblFeatureNum.gridx = 0;
		gbc_lblFeatureNum.gridy = 3;
		corrSetingsPanel.add(lblFeatureNum, gbc_lblFeatureNum);

		featureWindowSpinner = new JSpinner();
		featureWindowSpinner.setModel(new SpinnerNumberModel(1, 1, 20, 1));
		featureWindowSpinner.setPreferredSize(new Dimension(50, 20));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 3;
		corrSetingsPanel.add(featureWindowSpinner, gbc_spinner);

		JLabel lblSeconds = new JLabel("Seconds");
		GridBagConstraints gbc_lblSeconds = new GridBagConstraints();
		gbc_lblSeconds.anchor = GridBagConstraints.EAST;
		gbc_lblSeconds.insets = new Insets(0, 0, 0, 5);
		gbc_lblSeconds.gridx = 0;
		gbc_lblSeconds.gridy = 4;
		corrSetingsPanel.add(lblSeconds, gbc_lblSeconds);

		timeWindowTextField = new JFormattedTextField(new DecimalFormat("##.#"));
		GridBagConstraints gbc_formattedTextField_2 = new GridBagConstraints();
		gbc_formattedTextField_2.insets = new Insets(0, 0, 0, 5);
		gbc_formattedTextField_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField_2.gridx = 1;
		gbc_formattedTextField_2.gridy = 4;
		corrSetingsPanel.add(timeWindowTextField, gbc_formattedTextField_2);
	}

	public CorrelationFunctionType getCorrelationFunctionType() {

		return (CorrelationFunctionType) corrAlgoComboBox.getSelectedItem();
	}

	public double getCorrelationCutoff() {

		return Double.parseDouble(corrCutoffTextField.getText());
	}

	public SlidingWindowUnit getWindowSlidingUnit() {

		return (SlidingWindowUnit) windowSlidingTypeComboBox.getSelectedItem();
	}

	public double getMaxClusterWidth() {

		return Double.parseDouble(maxClusterWidthTextField.getText());
	}

	public int getFeatureNumberWindow() {

		return (int) featureWindowSpinner.getValue();
	}

	public double getFeatureTimeWindow() {

		return Double.parseDouble(timeWindowTextField.getText());
	}

	public boolean limitRtRange() {

		return limitRtCheckBox.isSelected();
	}

	public Range getRetentionRange() {

		double min = Double.parseDouble(fromRtTextField.getText());
		double max = Double.parseDouble(toRtTextField.getText());

		if(max <= min)
			return null;
		else
			return new Range(min,max);
	}

	public boolean filterMissing() {

		return filterMissingChkBox.isSelected();
	}

	public double getMaxMissingPercent() {

		return Double.parseDouble(maxMissingPercentTextField.getText());
	}

	public boolean imputeMissing() {

		return chckbxImputeMissingData.isSelected();
	}

	public ImputationMethod getImputationMethod() {

		return ((DataImputationType)imputationAlgorithmComboBox.getSelectedItem()).getMethod();
	}

	public int getKnnClusterNumber() {

		return (int) kMeansNumSpinner.getValue();
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;

//		Data prep
		limitRtCheckBox.setSelected(preferences.getBoolean(LIMIT_RT_RANGE, false));
		double minRt = preferences.getDouble(RT_FROM, 0.0d);
		fromRtTextField.setText(Double.toString(minRt));
		double maxRt = preferences.getDouble(RT_TO, 100.0d);
		toRtTextField.setText(Double.toString(maxRt));
		filterMissingChkBox.setSelected(preferences.getBoolean(FILTER_BY_FREQUENCY, true));
		double maxMissing = preferences.getDouble(MAX_MISSING, 50.0d);
		maxMissingPercentTextField.setText(Double.toString(maxMissing));
		chckbxImputeMissingData.setSelected(preferences.getBoolean(IMPUTE_MISING, true));
		imputationAlgorithmComboBox.setSelectedIndex(preferences.getInt(IMPUTATION_ALGORITHM, 0));
		kMeansNumSpinner.setValue(preferences.getInt(NUM_KNN_CLUSTERS, 3));

//		Correlation
		CorrelationFunctionType cft = 
				CorrelationFunctionType.getOptionByName(
						preferences.get(CORRELATION_ALGORITHM, CorrelationFunctionType.PEARSON.name()));	
		if(cft == null)
			cft = CorrelationFunctionType.PEARSON;
		
		corrAlgoComboBox.setSelectedItem(cft);
		double corrCutoff = preferences.getDouble(CORRELATION_CUTOFF, 0.5d);
		corrCutoffTextField.setText(Double.toString(corrCutoff));
		double maxClusterWidth = preferences.getDouble(MAX_CLUSTER_WIDTH, 20.0d);
		maxClusterWidthTextField.setText(Double.toString(maxClusterWidth));
		windowSlidingTypeComboBox.setSelectedIndex(preferences.getInt(WINDOW_SLIDING_UNIT, 1));
		int featureWindowWidth = preferences.getInt(NUM_FEATURES_WINDOW, 2);
		featureWindowSpinner.setValue(featureWindowWidth);
		double timeWindowWidth = preferences.getDouble(TIME_WINDOW, 3.0d);
		timeWindowTextField.setText(Double.toString(timeWindowWidth));
	}

	public void savePreferences() {

		preferences = Preferences.userRoot().node(PREFS_NODE);

		//	Data prep
		preferences.putBoolean(LIMIT_RT_RANGE, limitRtRange());
		preferences.putDouble(RT_FROM, Double.parseDouble(fromRtTextField.getText()));
		preferences.putDouble(RT_TO, Double.parseDouble(toRtTextField.getText()));
		preferences.putBoolean(FILTER_BY_FREQUENCY, filterMissing());
		preferences.putDouble(MAX_MISSING, getMaxMissingPercent());
		preferences.putBoolean(IMPUTE_MISING, imputeMissing());
		preferences.putInt(IMPUTATION_ALGORITHM, imputationAlgorithmComboBox.getSelectedIndex());
		preferences.putInt(NUM_KNN_CLUSTERS, getKnnClusterNumber());

		//	Correlation
		preferences.put(CORRELATION_ALGORITHM, getCorrelationFunctionType().name());
		preferences.putDouble(CORRELATION_CUTOFF, getCorrelationCutoff());
		preferences.putDouble(MAX_CLUSTER_WIDTH, getMaxClusterWidth());
		preferences.putInt(WINDOW_SLIDING_UNIT, windowSlidingTypeComboBox.getSelectedIndex());
		preferences.putInt(NUM_FEATURES_WINDOW, getFeatureNumberWindow());
		preferences.putDouble(TIME_WINDOW, getFeatureTimeWindow());
	}
}
