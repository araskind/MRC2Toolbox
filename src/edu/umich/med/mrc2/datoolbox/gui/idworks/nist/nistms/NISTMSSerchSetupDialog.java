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

package edu.umich.med.mrc2.datoolbox.gui.idworks.nist.nistms;

import java.awt.BorderLayout;
import java.awt.Color;
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
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
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
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTMSPreSearchType;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTMassErrorType;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch.NISTLibraryTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class NISTMSSerchSetupDialog extends JDialog implements ActionListener, ItemListener, BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3978164057347820054L;

	protected static final Icon dialogIcon = GuiUtils.getIcon("NISTMS", 32);

	protected Preferences preferences;
	// Input source
	public static final String INPUT_FILE = "INPUT_FILE";
	public static final String INPUT_FILE_DIR = "INPUT_FILE_DIR";
	// Library source
	public static final String LIBRARY_DIR = "INPUT_FILE_DIR";
	public static final String USE_INPUT_FILE = "USE_INPUT_FILE";
	public static final String LIB_LIST = "LIB_LIST";

	private ActionListener listener;
	private File 
		resultFile, 
		tmpInputFile;
	private JButton 
	inputFileBrowseButton,
		searchButton,
		setLibraryDirectoryButton;
	private JCheckBox 
		chckbxApplyLimits,
		chckbxIgnorePeaksAround,
		chckbxInSpectrum,
		chckbxMatchIonMode,
		chckbxMaximumMz,
		chckbxMinimumAbundance,
		chckbxPenalizeRareCompounds,
		chckbxReverseSearch;
	private JComboBox 
		fragmentMzUnitsomboBox,
		identitySimilarityComboBox,
		ignorePeaksAroundPrecursorUnitsomboBox,
		minMzTypeComboBox,
		precursorMzUnitsComboBox,
		searchTypeComboBox,
		preSearchTypeComboBox;
	private JFormattedTextField 
		fragmentMzToleranceTextField,
		ignorePeaksAroundPrecursorTextField,
		maxHitsFormattedTextField,
		maxMzFormattedTextField,
		minMzFormattedTextField,
		minScoreFormattedTextField,
		minimumAbundanceTextField,
		precursorMzToleranceTextField,
		presearchMwFormattedTextField;
	private JRadioButton 
		fileSource, 
		internalSource;
	private JTextField 
		inchiKeyTextField,
		inputFileTextField,
		libDirTextField;
	private NISTLibraryTable libraryTable;

	private JLabel lblMw;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public NISTMSSerchSetupDialog(ActionListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "NIST MS Search setup");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(640, 800));
		setPreferredSize(new Dimension(640, 800));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.listener = listener;

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		panel.add(tabbedPane, BorderLayout.CENTER);

		// JPanel searchOptionsPanel = createSearchOptionsPanel();

		JPanel searchOptionsPanel = new JPanel();
		searchOptionsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		tabbedPane.addTab("Search options", null, searchOptionsPanel, null);
		GridBagLayout gbl_searchOptionsPanel = new GridBagLayout();
		gbl_searchOptionsPanel.columnWidths = new int[] { 0, 0 };
		gbl_searchOptionsPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_searchOptionsPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_searchOptionsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE };
		searchOptionsPanel.setLayout(gbl_searchOptionsPanel);

		JPanel searchTypePanel = new JPanel();
		searchTypePanel
				.setBorder(new CompoundBorder(
						new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Search type",
								TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)),
						new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_searchTypePanel = new GridBagConstraints();
		gbc_searchTypePanel.insets = new Insets(0, 0, 5, 0);
		gbc_searchTypePanel.fill = GridBagConstraints.BOTH;
		gbc_searchTypePanel.gridx = 0;
		gbc_searchTypePanel.gridy = 0;
		searchOptionsPanel.add(searchTypePanel, gbc_searchTypePanel);
		GridBagLayout gbl_searchTypePanel = new GridBagLayout();
		gbl_searchTypePanel.columnWidths = new int[] { 0, 200, 0 };
		gbl_searchTypePanel.rowHeights = new int[] { 0, 0, 0 };
		gbl_searchTypePanel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_searchTypePanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		searchTypePanel.setLayout(gbl_searchTypePanel);

		JLabel lblIdentitysimilarity = new JLabel("Identity/Similarity");
		GridBagConstraints gbc_lblIdentitysimilarity = new GridBagConstraints();
		gbc_lblIdentitysimilarity.insets = new Insets(0, 0, 5, 5);
		gbc_lblIdentitysimilarity.anchor = GridBagConstraints.EAST;
		gbc_lblIdentitysimilarity.gridx = 0;
		gbc_lblIdentitysimilarity.gridy = 0;
		searchTypePanel.add(lblIdentitysimilarity, gbc_lblIdentitysimilarity);

		identitySimilarityComboBox = new JComboBox(
				new DefaultComboBoxModel<NISTMSSearchTypeGroup>(NISTMSSearchTypeGroup.values()));
		identitySimilarityComboBox.setSelectedItem(NISTMSSearchTypeGroup.Identity);
		GridBagConstraints gbc_identitySimilarityComboBox = new GridBagConstraints();
		gbc_identitySimilarityComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_identitySimilarityComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_identitySimilarityComboBox.gridx = 1;
		gbc_identitySimilarityComboBox.gridy = 0;
		searchTypePanel.add(identitySimilarityComboBox, gbc_identitySimilarityComboBox);

		JLabel lblSearchType = new JLabel("Search Type");
		GridBagConstraints gbc_lblSearchType = new GridBagConstraints();
		gbc_lblSearchType.insets = new Insets(0, 0, 0, 5);
		gbc_lblSearchType.anchor = GridBagConstraints.EAST;
		gbc_lblSearchType.gridx = 0;
		gbc_lblSearchType.gridy = 1;
		searchTypePanel.add(lblSearchType, gbc_lblSearchType);

		NISTMSSearchType[] typeOptions = NISTMSSearchType.getSearchTypesForGroup(NISTMSSearchTypeGroup.Identity);
		searchTypeComboBox = new JComboBox(new DefaultComboBoxModel<NISTMSSearchType>(typeOptions));
		GridBagConstraints gbc_searchTypeComboBox = new GridBagConstraints();
		gbc_searchTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchTypeComboBox.gridx = 1;
		gbc_searchTypeComboBox.gridy = 1;
		searchTypePanel.add(searchTypeComboBox, gbc_searchTypeComboBox);

		identitySimilarityComboBox.addItemListener(this);
		searchTypeComboBox.addItemListener(this);

		JPanel presearchOptionsPanel = new JPanel();
		presearchOptionsPanel
				.setBorder(new CompoundBorder(
						new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Presearch options",
								TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)),
						new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_presearchOptionsPanel = new GridBagConstraints();
		gbc_presearchOptionsPanel.insets = new Insets(0, 0, 5, 0);
		gbc_presearchOptionsPanel.fill = GridBagConstraints.BOTH;
		gbc_presearchOptionsPanel.gridx = 0;
		gbc_presearchOptionsPanel.gridy = 1;
		searchOptionsPanel.add(presearchOptionsPanel, gbc_presearchOptionsPanel);
		GridBagLayout gbl_presearchOptionsPanel = new GridBagLayout();
		gbl_presearchOptionsPanel.columnWidths = new int[] { 0, 200, 0, 0, 0, 0, 0 };
		gbl_presearchOptionsPanel.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_presearchOptionsPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_presearchOptionsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		presearchOptionsPanel.setLayout(gbl_presearchOptionsPanel);

		JLabel lblPresearchType = new JLabel("Presearch type");
		GridBagConstraints gbc_lblPresearchType = new GridBagConstraints();
		gbc_lblPresearchType.insets = new Insets(0, 0, 5, 5);
		gbc_lblPresearchType.anchor = GridBagConstraints.EAST;
		gbc_lblPresearchType.gridx = 0;
		gbc_lblPresearchType.gridy = 0;
		presearchOptionsPanel.add(lblPresearchType, gbc_lblPresearchType);

		preSearchTypeComboBox = new JComboBox<NISTMSPreSearchType>(
				new DefaultComboBoxModel<NISTMSPreSearchType>(NISTMSPreSearchType.values()));
		preSearchTypeComboBox.addItemListener(this);
		GridBagConstraints gbc_preSearchTypeComboBox = new GridBagConstraints();
		gbc_preSearchTypeComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_preSearchTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_preSearchTypeComboBox.gridx = 1;
		gbc_preSearchTypeComboBox.gridy = 0;
		presearchOptionsPanel.add(preSearchTypeComboBox, gbc_preSearchTypeComboBox);
		
		chckbxInSpectrum = new JCheckBox("In Spectrum");
		GridBagConstraints gbc_chckbxInSpectrum = new GridBagConstraints();
		gbc_chckbxInSpectrum.anchor = GridBagConstraints.WEST;
		gbc_chckbxInSpectrum.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxInSpectrum.gridx = 2;
		gbc_chckbxInSpectrum.gridy = 0;
		presearchOptionsPanel.add(chckbxInSpectrum, gbc_chckbxInSpectrum);

		lblMw = new JLabel("Precursor Ion m/z");
		GridBagConstraints gbc_lblMw = new GridBagConstraints();
		gbc_lblMw.insets = new Insets(0, 0, 5, 5);
		gbc_lblMw.anchor = GridBagConstraints.EAST;
		gbc_lblMw.gridx = 4;
		gbc_lblMw.gridy = 0;
		presearchOptionsPanel.add(lblMw, gbc_lblMw);

		presearchMwFormattedTextField = new JFormattedTextField(new DecimalFormat("###"));
		presearchMwFormattedTextField.setEnabled(false);
		presearchMwFormattedTextField.setColumns(8);
		GridBagConstraints gbc_presearchMwFormattedTextField = new GridBagConstraints();
		gbc_presearchMwFormattedTextField.insets = new Insets(0, 0, 5, 0);
		gbc_presearchMwFormattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_presearchMwFormattedTextField.gridx = 5;
		gbc_presearchMwFormattedTextField.gridy = 0;
		presearchOptionsPanel.add(presearchMwFormattedTextField, gbc_presearchMwFormattedTextField);

		JLabel lblInchikey = new JLabel("InChIKey");
		GridBagConstraints gbc_lblInchikey = new GridBagConstraints();
		gbc_lblInchikey.anchor = GridBagConstraints.EAST;
		gbc_lblInchikey.insets = new Insets(0, 0, 5, 5);
		gbc_lblInchikey.gridx = 0;
		gbc_lblInchikey.gridy = 1;
		presearchOptionsPanel.add(lblInchikey, gbc_lblInchikey);

		inchiKeyTextField = new JTextField();
		inchiKeyTextField.setEnabled(false);
		GridBagConstraints gbc_inchiKeyTextField = new GridBagConstraints();
		gbc_inchiKeyTextField.insets = new Insets(0, 0, 5, 0);
		gbc_inchiKeyTextField.gridwidth = 5;
		gbc_inchiKeyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_inchiKeyTextField.gridx = 1;
		gbc_inchiKeyTextField.gridy = 1;
		presearchOptionsPanel.add(inchiKeyTextField, gbc_inchiKeyTextField);
		inchiKeyTextField.setColumns(10);

		JLabel lblNewLabel = new JLabel("Blank = match search spectrum InChiKey");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.gridwidth = 5;
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 2;
		presearchOptionsPanel.add(lblNewLabel, gbc_lblNewLabel);

		JPanel msmsOptionsPanel = new JPanel();
		msmsOptionsPanel.setBorder(new CompoundBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), "MS/MS and In-source HiRes search options",
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)),
				new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_msmsOptionsPanel = new GridBagConstraints();
		gbc_msmsOptionsPanel.insets = new Insets(0, 0, 5, 0);
		gbc_msmsOptionsPanel.fill = GridBagConstraints.BOTH;
		gbc_msmsOptionsPanel.gridx = 0;
		gbc_msmsOptionsPanel.gridy = 2;
		searchOptionsPanel.add(msmsOptionsPanel, gbc_msmsOptionsPanel);
		GridBagLayout gbl_msmsOptionsPanel = new GridBagLayout();
		gbl_msmsOptionsPanel.columnWidths = new int[] { 114, 86, 60, 0, 0 };
		gbl_msmsOptionsPanel.rowHeights = new int[] { 20, 0, 0, 0 };
		gbl_msmsOptionsPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_msmsOptionsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		msmsOptionsPanel.setLayout(gbl_msmsOptionsPanel);

		JLabel precMzTolNewLabel = new JLabel("Precursor m/z tolerance");
		GridBagConstraints gbc_precMzTolNewLabel = new GridBagConstraints();
		gbc_precMzTolNewLabel.anchor = GridBagConstraints.WEST;
		gbc_precMzTolNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_precMzTolNewLabel.gridx = 0;
		gbc_precMzTolNewLabel.gridy = 0;
		msmsOptionsPanel.add(precMzTolNewLabel, gbc_precMzTolNewLabel);

		precursorMzToleranceTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		precursorMzToleranceTextField.setColumns(10);
		GridBagConstraints gbc_precursorMzToleranceTextField = new GridBagConstraints();
		gbc_precursorMzToleranceTextField.anchor = GridBagConstraints.NORTHWEST;
		gbc_precursorMzToleranceTextField.insets = new Insets(0, 0, 5, 5);
		gbc_precursorMzToleranceTextField.gridx = 1;
		gbc_precursorMzToleranceTextField.gridy = 0;
		msmsOptionsPanel.add(precursorMzToleranceTextField, gbc_precursorMzToleranceTextField);

		precursorMzUnitsComboBox = new JComboBox(
				new DefaultComboBoxModel<NISTMassErrorType>(NISTMassErrorType.values()));
		precursorMzUnitsComboBox.setSize(new Dimension(60, 20));
		precursorMzUnitsComboBox.setPreferredSize(new Dimension(60, 20));
		GridBagConstraints gbc_precursorMzUnitsComboBox = new GridBagConstraints();
		gbc_precursorMzUnitsComboBox.anchor = GridBagConstraints.NORTHWEST;
		gbc_precursorMzUnitsComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_precursorMzUnitsComboBox.gridx = 2;
		gbc_precursorMzUnitsComboBox.gridy = 0;
		msmsOptionsPanel.add(precursorMzUnitsComboBox, gbc_precursorMzUnitsComboBox);

		JLabel lblFragmentsMzTolerance = new JLabel("Fragments m/z tolerance");
		GridBagConstraints gbc_lblFragmentsMzTolerance = new GridBagConstraints();
		gbc_lblFragmentsMzTolerance.anchor = GridBagConstraints.WEST;
		gbc_lblFragmentsMzTolerance.insets = new Insets(0, 0, 5, 5);
		gbc_lblFragmentsMzTolerance.gridx = 0;
		gbc_lblFragmentsMzTolerance.gridy = 1;
		msmsOptionsPanel.add(lblFragmentsMzTolerance, gbc_lblFragmentsMzTolerance);

		fragmentMzToleranceTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		fragmentMzToleranceTextField.setColumns(10);
		GridBagConstraints gbc_fragmentMzToleranceTextField = new GridBagConstraints();
		gbc_fragmentMzToleranceTextField.anchor = GridBagConstraints.NORTHWEST;
		gbc_fragmentMzToleranceTextField.insets = new Insets(0, 0, 5, 5);
		gbc_fragmentMzToleranceTextField.gridx = 1;
		gbc_fragmentMzToleranceTextField.gridy = 1;
		msmsOptionsPanel.add(fragmentMzToleranceTextField, gbc_fragmentMzToleranceTextField);

		fragmentMzUnitsomboBox = new JComboBox(new DefaultComboBoxModel<NISTMassErrorType>(NISTMassErrorType.values()));
		fragmentMzUnitsomboBox.setPreferredSize(new Dimension(60, 20));
		fragmentMzUnitsomboBox.setSize(new Dimension(60, 20));
		GridBagConstraints gbc_fragmentMzUnitsomboBox = new GridBagConstraints();
		gbc_fragmentMzUnitsomboBox.insets = new Insets(0, 0, 5, 5);
		gbc_fragmentMzUnitsomboBox.anchor = GridBagConstraints.NORTHWEST;
		gbc_fragmentMzUnitsomboBox.gridx = 2;
		gbc_fragmentMzUnitsomboBox.gridy = 1;
		msmsOptionsPanel.add(fragmentMzUnitsomboBox, gbc_fragmentMzUnitsomboBox);

		chckbxIgnorePeaksAround = new JCheckBox("Ignore peaks around precursor +/-");
		GridBagConstraints gbc_chckbxIgnorePeaksAround = new GridBagConstraints();
		gbc_chckbxIgnorePeaksAround.anchor = GridBagConstraints.WEST;
		gbc_chckbxIgnorePeaksAround.gridwidth = 2;
		gbc_chckbxIgnorePeaksAround.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxIgnorePeaksAround.gridx = 0;
		gbc_chckbxIgnorePeaksAround.gridy = 2;
		msmsOptionsPanel.add(chckbxIgnorePeaksAround, gbc_chckbxIgnorePeaksAround);

		ignorePeaksAroundPrecursorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		ignorePeaksAroundPrecursorTextField.setColumns(10);
		GridBagConstraints gbc_ignorePeaksAroundPrecursorTextField = new GridBagConstraints();
		gbc_ignorePeaksAroundPrecursorTextField.insets = new Insets(0, 0, 0, 5);
		gbc_ignorePeaksAroundPrecursorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_ignorePeaksAroundPrecursorTextField.gridx = 2;
		gbc_ignorePeaksAroundPrecursorTextField.gridy = 2;
		msmsOptionsPanel.add(ignorePeaksAroundPrecursorTextField, gbc_ignorePeaksAroundPrecursorTextField);

		ignorePeaksAroundPrecursorUnitsomboBox = new JComboBox(
				new DefaultComboBoxModel<NISTMassErrorType>(NISTMassErrorType.values()));
		ignorePeaksAroundPrecursorUnitsomboBox.setSize(new Dimension(60, 20));
		ignorePeaksAroundPrecursorUnitsomboBox.setPreferredSize(new Dimension(60, 20));
		GridBagConstraints gbc_ignorePeaksAroundPrecursorUnitsomboBox = new GridBagConstraints();
		gbc_ignorePeaksAroundPrecursorUnitsomboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_ignorePeaksAroundPrecursorUnitsomboBox.gridx = 3;
		gbc_ignorePeaksAroundPrecursorUnitsomboBox.gridy = 2;
		msmsOptionsPanel.add(ignorePeaksAroundPrecursorUnitsomboBox, gbc_ignorePeaksAroundPrecursorUnitsomboBox);

		JPanel otherOptionsPanel = new JPanel();
		otherOptionsPanel
				.setBorder(new CompoundBorder(
						new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Other options",
								TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)),
						new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_otherOptionsPanel = new GridBagConstraints();
		gbc_otherOptionsPanel.insets = new Insets(0, 0, 5, 0);
		gbc_otherOptionsPanel.fill = GridBagConstraints.BOTH;
		gbc_otherOptionsPanel.gridx = 0;
		gbc_otherOptionsPanel.gridy = 3;
		searchOptionsPanel.add(otherOptionsPanel, gbc_otherOptionsPanel);
		GridBagLayout gbl_otherOptionsPanel = new GridBagLayout();
		gbl_otherOptionsPanel.columnWidths = new int[] { 0, 0 };
		gbl_otherOptionsPanel.rowHeights = new int[] { 0, 0, 0, 0 };
		gbl_otherOptionsPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_otherOptionsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		otherOptionsPanel.setLayout(gbl_otherOptionsPanel);

		chckbxReverseSearch = new JCheckBox("Reverse Search");
		GridBagConstraints gbc_chckbxReverseSearch = new GridBagConstraints();
		gbc_chckbxReverseSearch.anchor = GridBagConstraints.WEST;
		gbc_chckbxReverseSearch.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxReverseSearch.gridx = 0;
		gbc_chckbxReverseSearch.gridy = 0;
		otherOptionsPanel.add(chckbxReverseSearch, gbc_chckbxReverseSearch);

		chckbxMatchIonMode = new JCheckBox("Match Ion Mode");
		GridBagConstraints gbc_chckbxMatchIonMode = new GridBagConstraints();
		gbc_chckbxMatchIonMode.anchor = GridBagConstraints.WEST;
		gbc_chckbxMatchIonMode.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxMatchIonMode.gridx = 0;
		gbc_chckbxMatchIonMode.gridy = 1;
		otherOptionsPanel.add(chckbxMatchIonMode, gbc_chckbxMatchIonMode);

		chckbxPenalizeRareCompounds = new JCheckBox("Penalize Rare Compounds");
		GridBagConstraints gbc_chckbxPenalizeRareCompounds = new GridBagConstraints();
		gbc_chckbxPenalizeRareCompounds.anchor = GridBagConstraints.WEST;
		gbc_chckbxPenalizeRareCompounds.gridx = 0;
		gbc_chckbxPenalizeRareCompounds.gridy = 2;
		otherOptionsPanel.add(chckbxPenalizeRareCompounds, gbc_chckbxPenalizeRareCompounds);

		JPanel limitsPanel = new JPanel();
		limitsPanel.setBorder(new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Limits",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_limitsPanel = new GridBagConstraints();
		gbc_limitsPanel.insets = new Insets(0, 0, 5, 0);
		gbc_limitsPanel.fill = GridBagConstraints.BOTH;
		gbc_limitsPanel.gridx = 0;
		gbc_limitsPanel.gridy = 4;
		searchOptionsPanel.add(limitsPanel, gbc_limitsPanel);
		GridBagLayout gbl_limitsPanel = new GridBagLayout();
		gbl_limitsPanel.columnWidths = new int[] { 0, 0, 110, 0, 0 };
		gbl_limitsPanel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_limitsPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_limitsPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		limitsPanel.setLayout(gbl_limitsPanel);

		chckbxApplyLimits = new JCheckBox("Apply Limits");
		GridBagConstraints gbc_chckbxApplyLimits = new GridBagConstraints();
		gbc_chckbxApplyLimits.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxApplyLimits.gridx = 0;
		gbc_chckbxApplyLimits.gridy = 0;
		limitsPanel.add(chckbxApplyLimits, gbc_chckbxApplyLimits);

		chckbxMinimumAbundance = new JCheckBox("Minimum Abundance");
		GridBagConstraints gbc_chckbxMinimumAbundance = new GridBagConstraints();
		gbc_chckbxMinimumAbundance.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxMinimumAbundance.anchor = GridBagConstraints.WEST;
		gbc_chckbxMinimumAbundance.gridx = 1;
		gbc_chckbxMinimumAbundance.gridy = 1;
		limitsPanel.add(chckbxMinimumAbundance, gbc_chckbxMinimumAbundance);

		minimumAbundanceTextField = new JFormattedTextField(new DecimalFormat("###"));
		minimumAbundanceTextField.setColumns(10);
		GridBagConstraints gbc_minimumAbundanceTextField = new GridBagConstraints();
		gbc_minimumAbundanceTextField.insets = new Insets(0, 0, 5, 5);
		gbc_minimumAbundanceTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_minimumAbundanceTextField.gridx = 2;
		gbc_minimumAbundanceTextField.gridy = 1;
		limitsPanel.add(minimumAbundanceTextField, gbc_minimumAbundanceTextField);

		JLabel label = new JLabel("1 - 999");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.WEST;
		gbc_label.insets = new Insets(0, 0, 5, 0);
		gbc_label.gridx = 3;
		gbc_label.gridy = 1;
		limitsPanel.add(label, gbc_label);

		chckbxMaximumMz = new JCheckBox("Maximum M/Z");
		GridBagConstraints gbc_chckbxMaximumMz = new GridBagConstraints();
		gbc_chckbxMaximumMz.anchor = GridBagConstraints.WEST;
		gbc_chckbxMaximumMz.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxMaximumMz.gridx = 1;
		gbc_chckbxMaximumMz.gridy = 2;
		limitsPanel.add(chckbxMaximumMz, gbc_chckbxMaximumMz);
		
		maxMzFormattedTextField = new JFormattedTextField(new DecimalFormat("###"));
		maxMzFormattedTextField.setColumns(10);
		GridBagConstraints gbc_maxMzFormattedTextField = new GridBagConstraints();
		gbc_maxMzFormattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_maxMzFormattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxMzFormattedTextField.gridx = 2;
		gbc_maxMzFormattedTextField.gridy = 2;
		limitsPanel.add(maxMzFormattedTextField, gbc_maxMzFormattedTextField);
		
		JLabel label_1 = new JLabel("1 - 2000");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.insets = new Insets(0, 0, 5, 0);
		gbc_label_1.anchor = GridBagConstraints.WEST;
		gbc_label_1.gridx = 3;
		gbc_label_1.gridy = 2;
		limitsPanel.add(label_1, gbc_label_1);
		
		JLabel lblMinimumMz = new JLabel("Minimum M/Z");
		GridBagConstraints gbc_lblMinimumMz = new GridBagConstraints();
		gbc_lblMinimumMz.anchor = GridBagConstraints.EAST;
		gbc_lblMinimumMz.insets = new Insets(0, 0, 0, 5);
		gbc_lblMinimumMz.gridx = 0;
		gbc_lblMinimumMz.gridy = 3;
		limitsPanel.add(lblMinimumMz, gbc_lblMinimumMz);
		
		minMzTypeComboBox = new JComboBox();
		GridBagConstraints gbc_minMzTypeComboBox = new GridBagConstraints();
		gbc_minMzTypeComboBox.insets = new Insets(0, 0, 0, 5);
		gbc_minMzTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_minMzTypeComboBox.gridx = 1;
		gbc_minMzTypeComboBox.gridy = 3;
		limitsPanel.add(minMzTypeComboBox, gbc_minMzTypeComboBox);
		
		minMzFormattedTextField = new JFormattedTextField(new DecimalFormat("###"));
		minMzFormattedTextField.setColumns(10);
		GridBagConstraints gbc_minMzFormattedTextField = new GridBagConstraints();
		gbc_minMzFormattedTextField.insets = new Insets(0, 0, 0, 5);
		gbc_minMzFormattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_minMzFormattedTextField.gridx = 2;
		gbc_minMzFormattedTextField.gridy = 3;
		limitsPanel.add(minMzFormattedTextField, gbc_minMzFormattedTextField);
		
		JPanel resultFilterPanel = new JPanel();
		resultFilterPanel.setBorder(new CompoundBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Result filtering", 
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(10, 10, 10, 10)));
		GridBagConstraints gbc_resultFilterPanel = new GridBagConstraints();
		gbc_resultFilterPanel.fill = GridBagConstraints.BOTH;
		gbc_resultFilterPanel.gridx = 0;
		gbc_resultFilterPanel.gridy = 5;
		searchOptionsPanel.add(resultFilterPanel, gbc_resultFilterPanel);
		GridBagLayout gbl_resultFilterPanel = new GridBagLayout();
		gbl_resultFilterPanel.columnWidths = new int[]{0, 94, 0, 130, 0};
		gbl_resultFilterPanel.rowHeights = new int[]{0, 0};
		gbl_resultFilterPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_resultFilterPanel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		resultFilterPanel.setLayout(gbl_resultFilterPanel);
		
		JLabel lblMaxHits = new JLabel("Max. hits");
		GridBagConstraints gbc_lblMaxHits = new GridBagConstraints();
		gbc_lblMaxHits.insets = new Insets(0, 0, 0, 5);
		gbc_lblMaxHits.anchor = GridBagConstraints.EAST;
		gbc_lblMaxHits.gridx = 0;
		gbc_lblMaxHits.gridy = 0;
		resultFilterPanel.add(lblMaxHits, gbc_lblMaxHits);
		
		maxHitsFormattedTextField = new JFormattedTextField(new DecimalFormat("###"));
		maxHitsFormattedTextField.setColumns(10);
		GridBagConstraints gbc_maxHitsFormattedTextField = new GridBagConstraints();
		gbc_maxHitsFormattedTextField.insets = new Insets(0, 0, 0, 5);
		gbc_maxHitsFormattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxHitsFormattedTextField.gridx = 1;
		gbc_maxHitsFormattedTextField.gridy = 0;
		resultFilterPanel.add(maxHitsFormattedTextField, gbc_maxHitsFormattedTextField);
		
		JLabel lblMinScore = new JLabel("Min. score");
		GridBagConstraints gbc_lblMinScore = new GridBagConstraints();
		gbc_lblMinScore.insets = new Insets(0, 0, 0, 5);
		gbc_lblMinScore.anchor = GridBagConstraints.EAST;
		gbc_lblMinScore.gridx = 2;
		gbc_lblMinScore.gridy = 0;
		resultFilterPanel.add(lblMinScore, gbc_lblMinScore);
		
		minScoreFormattedTextField = new JFormattedTextField(new DecimalFormat("###"));
		GridBagConstraints gbc_minScoreFormattedTextField = new GridBagConstraints();
		gbc_minScoreFormattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_minScoreFormattedTextField.gridx = 3;
		gbc_minScoreFormattedTextField.gridy = 0;
		resultFilterPanel.add(minScoreFormattedTextField, gbc_minScoreFormattedTextField);

		// JPanel inputAndLibraryPanel = createInputAndLibraryPanel();

		JPanel inputAndLibraryPanel = new JPanel();
		inputAndLibraryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		inputAndLibraryPanel.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Input data",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(10, 5, 10, 5)));
		inputAndLibraryPanel.add(panel_1, BorderLayout.NORTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		JLabel lblSource = new JLabel("Source: ");
		GridBagConstraints gbc_lblSource = new GridBagConstraints();
		gbc_lblSource.anchor = GridBagConstraints.EAST;
		gbc_lblSource.insets = new Insets(0, 0, 5, 5);
		gbc_lblSource.gridx = 0;
		gbc_lblSource.gridy = 0;
		panel_1.add(lblSource, gbc_lblSource);

		ButtonGroup inputSourceButtongroup = new ButtonGroup();
		fileSource = new JRadioButton("File", true);
		internalSource = new JRadioButton("Internal");
		inputSourceButtongroup.add(fileSource);
		inputSourceButtongroup.add(internalSource);

		GridBagConstraints gbc_fileSourceLabel = new GridBagConstraints();
		gbc_fileSourceLabel.anchor = GridBagConstraints.WEST;
		gbc_fileSourceLabel.insets = new Insets(0, 0, 5, 5);
		gbc_fileSourceLabel.gridx = 1;
		gbc_fileSourceLabel.gridy = 0;
		panel_1.add(fileSource, gbc_fileSourceLabel);

		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = 0;
		panel_1.add(internalSource, gbc_lblNewLabel_1);

		JLabel lblFile = new JLabel("File: ");
		GridBagConstraints gbc_lblFile = new GridBagConstraints();
		gbc_lblFile.insets = new Insets(0, 0, 0, 5);
		gbc_lblFile.anchor = GridBagConstraints.EAST;
		gbc_lblFile.gridx = 0;
		gbc_lblFile.gridy = 1;
		panel_1.add(lblFile, gbc_lblFile);

		inputFileTextField = new JTextField();
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.gridwidth = 3;
		gbc_textField_1.insets = new Insets(0, 0, 0, 5);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 1;
		gbc_textField_1.gridy = 1;
		panel_1.add(inputFileTextField, gbc_textField_1);
		inputFileTextField.setColumns(10);

		inputFileBrowseButton = new JButton("Browse");
		inputFileBrowseButton.setActionCommand(MainActionCommands.SELECT_PEPSEARCH_INPUT_FILE_COMMAND.getName());
		inputFileBrowseButton.addActionListener(this);
		GridBagConstraints gbc_inputFileBrowseButton = new GridBagConstraints();
		gbc_inputFileBrowseButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_inputFileBrowseButton.gridx = 4;
		gbc_inputFileBrowseButton.gridy = 1;
		panel_1.add(inputFileBrowseButton, gbc_inputFileBrowseButton);

		libraryTable = new NISTLibraryTable();
		JScrollPane scrollPane = new JScrollPane(libraryTable);
		inputAndLibraryPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(10, 0, 0, 0));
		inputAndLibraryPanel.add(panel_2, BorderLayout.SOUTH);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[] { 86, 85, 0 };
		gbl_panel_2.rowHeights = new int[] { 23, 0 };
		gbl_panel_2.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel_2.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		panel_2.setLayout(gbl_panel_2);

		libDirTextField = new JTextField();
		libDirTextField.setEditable(false);
		GridBagConstraints gbc_libDirTextField = new GridBagConstraints();
		gbc_libDirTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_libDirTextField.insets = new Insets(0, 0, 0, 5);
		gbc_libDirTextField.gridx = 0;
		gbc_libDirTextField.gridy = 0;
		panel_2.add(libDirTextField, gbc_libDirTextField);
		libDirTextField.setColumns(10);

		setLibraryDirectoryButton = new JButton("Set library directory");
		setLibraryDirectoryButton.setActionCommand(MainActionCommands.ADD_PEPSEARCH_LIBRARY_COMMAND.getName());
		setLibraryDirectoryButton.addActionListener(this);
		GridBagConstraints gbc_setLibraryDirectoryButton = new GridBagConstraints();
		gbc_setLibraryDirectoryButton.anchor = GridBagConstraints.NORTHEAST;
		gbc_setLibraryDirectoryButton.gridx = 1;
		gbc_setLibraryDirectoryButton.gridy = 0;
		panel_2.add(setLibraryDirectoryButton, gbc_setLibraryDirectoryButton);

		tabbedPane.addTab("Input data and libraries", null, inputAndLibraryPanel, null);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		JPanel buttonPanel = new JPanel();
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		FlowLayout fl_buttonPanel = (FlowLayout) buttonPanel.getLayout();
		fl_buttonPanel.setAlignment(FlowLayout.RIGHT);
		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		btnCancel.addActionListener(al);

		searchButton = new JButton(MainActionCommands.NIST_MS_SEARCH_RUN_COMMAND.getName());
		buttonPanel.add(searchButton);
		searchButton.setActionCommand(MainActionCommands.NIST_MS_SEARCH_RUN_COMMAND.getName());
		searchButton.addActionListener(listener);
		JRootPane rootPane = SwingUtilities.getRootPane(searchButton);
		rootPane.setDefaultButton(searchButton);

		loadPreferences();
		initFileChoosers();
		pack();
	}

	private void initFileChoosers() {
		// TODO Auto-generated method stub

	}

	private JPanel createSearchOptionsPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	private JPanel createInputAndLibraryPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void itemStateChanged(ItemEvent event) {

		if (event.getStateChange() == ItemEvent.SELECTED) {

			Object item = event.getItem();
			if (item instanceof NISTMSSearchTypeGroup)
				updateSearchTypes();

			if (item instanceof NISTMSSearchType)
				updateOptionsBasedOnSearchType((NISTMSSearchType) item);
		}
	}

	@SuppressWarnings("unchecked")
	private void updateSearchTypes() {

		searchTypeComboBox.removeItemListener(this);
		NISTMSSearchType[] typeOptions = NISTMSSearchType
				.getSearchTypesForGroup((NISTMSSearchTypeGroup) identitySimilarityComboBox.getSelectedItem());
		searchTypeComboBox.setModel(new DefaultComboBoxModel<NISTMSSearchType>(typeOptions));
		updateOptionsBasedOnSearchType((NISTMSSearchType) searchTypeComboBox.getSelectedItem());
		searchTypeComboBox.addItemListener(this);
	}

	private void updateOptionsBasedOnSearchType(NISTMSSearchType searchType) {

		//	MW presearch option
		if(searchType.equals(NISTMSSearchType.IDENTITY_MSMS) ||
				searchType.equals(NISTMSSearchType.IDENTITY_IN_SOURCE_HIGH_RES) ||
				searchType.equals(NISTMSSearchType.SIMILARITY_HYBRID_MSMS)) {
			removeMwPresearchOption();
		}
		else {
			addMwPresearchOption();
		}
		if(searchType.equals(NISTMSSearchType.IDENTITY_MSMS) ||
				searchType.equals(NISTMSSearchType.IDENTITY_IN_SOURCE_HIGH_RES) ||
				searchType.equals(NISTMSSearchType.SIMILARITY_SIMPLE)) {
			
		}
		
//		chckbxInSpectrum
//		lblMw
	}
	
	@SuppressWarnings("unchecked")
	private void removeMwPresearchOption() {
		
		ArrayList<NISTMSPreSearchType>active = new ArrayList<NISTMSPreSearchType>();
		for(NISTMSPreSearchType p : NISTMSPreSearchType.values()) {
			if(!p.equals(NISTMSPreSearchType.MW))
				active.add(p);
		}		
		DefaultComboBoxModel<NISTMSPreSearchType> model = 
				new DefaultComboBoxModel<NISTMSPreSearchType>(active.toArray(new NISTMSPreSearchType[active.size()]));
		preSearchTypeComboBox.setModel(model);
	}
	
	@SuppressWarnings("unchecked")
	private void addMwPresearchOption() {
		
		preSearchTypeComboBox.setModel(
				new DefaultComboBoxModel<NISTMSPreSearchType>(NISTMSPreSearchType.values()));
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadPreferences() {
		// TODO Auto-generated method stub

	}

	@Override
	public void savePreferences() {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

}
