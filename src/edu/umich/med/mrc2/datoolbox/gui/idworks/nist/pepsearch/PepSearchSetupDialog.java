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

package edu.umich.med.mrc2.datoolbox.gui.idworks.nist.pepsearch;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.NISTPepSearchParameterObject;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.nist.NISTPepSearchOutputColumnCode;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.msmsfdr.NISTPepSearchResultManipulator;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class PepSearchSetupDialog extends JDialog implements ActionListener, BackedByPreferences{

	/**
	 *
	 */
	protected static final long serialVersionUID = -8270328541430511802L;
	protected static final Icon dialogIcon = GuiUtils.getIcon("NISTMS-pep", 32);

	protected Preferences preferences;
	//	Input source
	public static final String INPUT_FILE = "INPUT_FILE";
	public static final String INPUT_FILE_DIR = "INPUT_FILE_DIR";
	public static final String TABLE_ROW_SUBSET = "TABLE_ROW_SUBSET";
	//	Library source
	public static final String LIBRARY_DIR = "LIBRARY_DIR";
	public static final String USE_INPUT_FILE = "USE_INPUT_FILE";
	public static final String LIB_LIST = "LIB_LIST";
	//	Search options
	public static final String PRESEARCH_MODE = "PRESEARCH_MODE";
	public static final String SEARCH_TYPE = "SEARCH_TYPE";
	public static final String SEARCH_OPTION = "SEARCH_OPTION";
	public static final String HIT_REJECTION_OPTION = "HIT_REJECTION_OPTION";
	public static final String ENABLE_REVERSE_SEARCH = "ENABLE_REVERSE_SEARCH";
	public static final String ENABLE_ALTERNATIVE_PEAK_MATCHING = "ENABLE_ALTERNATIVE_PEAK_MATCHING";
	public static final String IGNORE_PEAKS_AROUND_PRECURSOR = "IGNORE_PEAKS_AROUND_PRECURSOR";
	public static final String IGNORE_PEAKS_AROUND_PRECURSOR_WINDOW = "IGNORE_PEAKS_AROUND_PRECURSOR_WINDOW";
	public static final String IGNORE_PEAKS_AROUND_PRECURSOR_UNITS = "IGNORE_PEAKS_AROUND_PRECURSOR_UNITS";
	public static final String SEARCH_THRESHOLD_OPTION = "SEARCH_THRESHOLD_OPTION";
	public static final String PEPTIDE_SCORE_OPTION = "PEPTIDE_SCORE_OPTION";
	public static final String PRECURSOR_MASS_ACCURACY = "PRECURSOR_MASS_ACCURACY";
	public static final String PRECURSOR_MASS_ACCURACY_UNITS = "PRECURSOR_MASS_ACCURACY_UNITS";
	public static final String FRAGMENT_MASS_ACCURACY = "FRAGMENT_MASS_ACCURACY";
	public static final String FRAGMENT_MASS_ACCURACY_UNITS = "FRAGMENT_MASS_ACCURACY_UNITS";
	public static final String SEARCH_MZ_LOW_CUTOFF = "SEARCH_MZ_LOW_CUTOFF";
	public static final String SEARCH_MZ_HIGH_CUTOFF = "SEARCH_MZ_HIGH_CUTOFF";
	public static final String MIN_INTENSITY_CUTOFF = "MIN_INTENSITY_CUTOFF";
	public static final String HYBRID_SEARCH_MW_LOSS = "HYBRID_SEARCH_MW_LOSS";
	public static final String MATCH_POLARITY = "MATCH_POLARITY";
	public static final String MATCH_CHARGE = "MATCH_CHARGE";
	public static final String SET_HIGH_EXECUTION_PRIORITY = "SET_HIGH_EXECUTION_PRIORITY";
	public static final String LOAD_LIBRARIES_IN_MEMORY = "LOAD_LIBRARIES_IN_MEMORY";
	
	public static final String SEARCH_LIBS_SEPARATELY = "SEARCH_LIBS_SEPARATELY";
	
	//	Output options
	public static final String MIN_MATCH_FACTOR = "MIN_MATCH_FACTOR";
	public static final String MAX_NUM_HITS = "MAX_NUM_HITS";
	public static final String FOUND_NOT_FOUND_OUTPUT = "FOUND_NOT_FOUND_OUTPUT";
	public static final String OUTPUT_REVERSE_MATCH_FACTOR = "OUTPUT_REVERSE_MATCH_FACTOR";
	public static final String NO_HIT_PROBABILITIES = "NO_HIT_PROBABILITIES";
	
	protected NISTPepSearchParameterObject pepSearchParameterObject;
	public static final NISTPepSearchOutputColumnCode[]defaultEnabledColumns = 
			new NISTPepSearchOutputColumnCode[] {
					NISTPepSearchOutputColumnCode.PRECURSOR_MZ_COLUMN,
					NISTPepSearchOutputColumnCode.HIT_SEARCH_MZ_DIFF_COLUMN,
					NISTPepSearchOutputColumnCode.MOL_FORMULA_COLUMN,
					NISTPepSearchOutputColumnCode.SEARCH_SPECTRUM_NUMBER_COLUMN,
					NISTPepSearchOutputColumnCode.COLLISION_ENERGY_COLUMN,
					NISTPepSearchOutputColumnCode.INCHI_KEY_COLUMN,
			};

	protected File inputFile;
	protected File tmpInputFile;
	protected File resultsFile;
	protected File inputFileDirectory;
	protected File libraryDirectory;
	protected NISTLibraryTable libraryTable;
	protected JFileChooser
		inputMsMsFileChooser,
		libraryFileChooser;

	@SuppressWarnings("rawtypes")
	protected JComboBox
		preSearchModeComboBox,
		searchTypeComboBox,
		hitRejectionComboBox,
		ignoreAroundPrecursorAccuracyComboBox,
		fragmentMzErrorTypeComboBox,
		pepScoreTypeComboBox,
		precursorMzErrorTypeComboBox,
		searchOptionComboBox,
		searchThresholdComboBox,
		outputListInclusionComboBox;

	protected JButton
		addLibraryButton,
		btnRemoveLibrary,
		inputFileBrowseButton,
		searchButton;

	protected JCheckBox
		chckbxAlternativePeakMatching,
		chckbxIgnorePeaksAroundPrecursor,
		chckbxLoadLibrariesInMemory,
		chckbxMatchCharge,
		chckbxMatchPolarity,
		chckbxReverseSearch,
		chckbxSetHighPriorityProgramExecution,
		noHitProbabsCheckBox,
		outRevMatchCheckBox;

	protected JFormattedTextField
		ignoreAroundPrecursorTextField,
		lossMwTextField,
		maxHitsTextField,
		minIntensityTextField,
		minMatchFactorTextField,
		mzRangeEndTextField,
		mzRangeStartTextField,
		fragmentMzErrorTextField,
		precursorMzErrorTextField;

	protected JRadioButton
		fileSource,
		internalSource;

	protected JTextField inputFileTextField;
	

	protected ActionListener listener;
	//	protected JPanel commandPreviewPanel;
	protected JTextArea commandPreviewTextArea;
	protected JButton generateCommandButton;
	protected JButton copyButton;
	private JComboBox<TableRowSubset> featureSubsetComboBox;
	private TreeMap<NISTPepSearchOutputColumnCode, JCheckBox> outputColumnsCheckboxMap;
	
	private JPanel searchOptionsPanel,
					inputAndLibraryPanel,
					outputOptionsPanel,
					commandPreviewPanel;
	private JCheckBox searchLibrariesSeparatelyCheckBox;
	private Component horizontalStrut;
	
	public PepSearchSetupDialog() {
		//	Empty constructor to allow access to individual panel creation.
		this(null);
	}

	public PepSearchSetupDialog(ActionListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "NIST MSPepSearch setup");
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
		
		searchOptionsPanel = createSearchOptionsPanel();
		tabbedPane.addTab("Search options", null, searchOptionsPanel, null);
		
		inputAndLibraryPanel = createInputAndLibraryPanel();
		tabbedPane.addTab("Input data and libraries", null, inputAndLibraryPanel, null);

		outputOptionsPanel = createOutputOptionsPanel();
		tabbedPane.addTab("Output options", null, outputOptionsPanel, null);
	
		commandPreviewPanel = createCommandPreviewPanel();
		tabbedPane.addTab("Command preview", null, commandPreviewPanel, null);

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

		searchButton = new JButton(MainActionCommands.NIST_MS_PEPSEARCH_RUN_COMMAND.getName());
		buttonPanel.add(searchButton);
		searchButton.setActionCommand(MainActionCommands.NIST_MS_PEPSEARCH_RUN_COMMAND.getName());
		searchButton.addActionListener(listener);
		JRootPane rootPane = SwingUtilities.getRootPane(searchButton);
		rootPane.setDefaultButton(searchButton);

		loadPreferences();
		initFileChoosers();
		pack();
	}

	@Override
	public void dispose() {
		savePreferences();
		super.dispose();
	}

	protected void initFileChoosers() {

		//	Input file
		inputMsMsFileChooser = new ImprovedFileChooser();
		inputMsMsFileChooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		inputMsMsFileChooser.addActionListener(this);
		inputMsMsFileChooser.setAcceptAllFileFilterUsed(false);
		inputMsMsFileChooser.setMultiSelectionEnabled(false);
		inputMsMsFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		inputMsMsFileChooser.getActionMap().get("viewTypeDetails").actionPerformed(null);
		inputMsMsFileChooser.setCurrentDirectory(inputFileDirectory);

		FileNameExtensionFilter xmlFilter =
			new FileNameExtensionFilter("Agilent XML MSMS export files", "xml", "XML");
		inputMsMsFileChooser.addChoosableFileFilter(xmlFilter);
		FileNameExtensionFilter cefFilter =
			new FileNameExtensionFilter("Agilent CEF files", "cef", "CEF");
		inputMsMsFileChooser.addChoosableFileFilter(cefFilter);
		FileNameExtensionFilter mspFilter =
			new FileNameExtensionFilter("NIST MSP MSMS files", "msp", "MSP");
		inputMsMsFileChooser.addChoosableFileFilter(mspFilter);
		FileNameExtensionFilter mgfFilter =
				new FileNameExtensionFilter("MGF files", "mgf", "MGF");
		inputMsMsFileChooser.addChoosableFileFilter(mgfFilter);

		//	Library
		libraryFileChooser = new ImprovedFileChooser();
		libraryFileChooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		libraryFileChooser.addActionListener(this);
		libraryFileChooser.setMultiSelectionEnabled(true);
		libraryFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		libraryFileChooser.getActionMap().get("viewTypeDetails").actionPerformed(null);
		try {
			libraryFileChooser.setCurrentDirectory(libraryDirectory);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//	e.printStackTrace();
		}
	}
	
	private JPanel createInputAndLibraryPanel() {
		
		JPanel inputAndLibraryPanel = new JPanel();
		inputAndLibraryPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		inputAndLibraryPanel.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(
			new CompoundBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"),
					"Input data", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)),
				new EmptyBorder(10, 5, 10, 5)));
		inputAndLibraryPanel.add(panel_1, BorderLayout.NORTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		int rowCount = 0;

		JLabel lblSource = new JLabel("Source: ");
		GridBagConstraints gbc_lblSource = new GridBagConstraints();
		gbc_lblSource.anchor = GridBagConstraints.EAST;
		gbc_lblSource.insets = new Insets(0, 0, 5, 5);
		gbc_lblSource.gridx = 0;
		gbc_lblSource.gridy = rowCount;
		panel_1.add(lblSource, gbc_lblSource);

		ButtonGroup inputSourceButtongroup = new ButtonGroup();
		fileSource = new JRadioButton("File", true);
		internalSource = new JRadioButton("Internal");
		inputSourceButtongroup.add(fileSource);
		inputSourceButtongroup.add(internalSource);

		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = rowCount;
		panel_1.add(fileSource, gbc_lblNewLabel);

		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 2;
		gbc_lblNewLabel_1.gridy = rowCount;
		panel_1.add(internalSource, gbc_lblNewLabel_1);
		
		rowCount++;
		
		JLabel lblInternalSubset = new JLabel("For internal data: ");
		GridBagConstraints gbc_lblInternalSubset = new GridBagConstraints();
		gbc_lblInternalSubset.insets = new Insets(0, 0, 0, 5);
		gbc_lblInternalSubset.anchor = GridBagConstraints.EAST;
		gbc_lblInternalSubset.gridx = 0;
		gbc_lblInternalSubset.gridy = rowCount;
		panel_1.add(lblInternalSubset, gbc_lblInternalSubset);

		featureSubsetComboBox = new JComboBox<TableRowSubset>(
				new DefaultComboBoxModel<TableRowSubset>(TableRowSubset.values()));
		featureSubsetComboBox.setMinimumSize(new Dimension(200, 25));
		featureSubsetComboBox.setPreferredSize(new Dimension(200, 25));
		GridBagConstraints gbc_featureSubset = new GridBagConstraints();
		gbc_featureSubset.gridwidth = 3;
		gbc_featureSubset.insets = new Insets(0, 0, 0, 5);
		gbc_featureSubset.fill = GridBagConstraints.HORIZONTAL;
		gbc_featureSubset.gridx = 1;
		gbc_featureSubset.gridy = rowCount;
		panel_1.add(featureSubsetComboBox, gbc_featureSubset);
		
		rowCount++;

		JLabel lblFile = new JLabel("File: ");
		GridBagConstraints gbc_lblFile = new GridBagConstraints();
		gbc_lblFile.insets = new Insets(0, 0, 0, 5);
		gbc_lblFile.anchor = GridBagConstraints.EAST;
		gbc_lblFile.gridx = 0;
		gbc_lblFile.gridy = rowCount;
		panel_1.add(lblFile, gbc_lblFile);

		inputFileTextField = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.gridwidth = 3;
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = rowCount;
		panel_1.add(inputFileTextField, gbc_textField);
		inputFileTextField.setColumns(10);

		inputFileBrowseButton = new JButton("Browse");
		inputFileBrowseButton.setActionCommand(MainActionCommands.SELECT_PEPSEARCH_INPUT_FILE_COMMAND.getName());
		inputFileBrowseButton.addActionListener(this);
		GridBagConstraints gbc_inputFileBrowseButton = new GridBagConstraints();
		gbc_inputFileBrowseButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_inputFileBrowseButton.gridx = 4;
		gbc_inputFileBrowseButton.gridy = rowCount;
		panel_1.add(inputFileBrowseButton, gbc_inputFileBrowseButton);
		
		rowCount++;

		libraryTable = new NISTLibraryTable();
		JScrollPane scrollPane = new JScrollPane(libraryTable);
		inputAndLibraryPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel panel_2 = new JPanel();
		inputAndLibraryPanel.add(panel_2, BorderLayout.SOUTH);
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);

		btnRemoveLibrary = new JButton("Remove library");
		btnRemoveLibrary.setActionCommand(MainActionCommands.REMOVE_PEPSEARCH_LIBRARY_COMMAND.getName());
		btnRemoveLibrary.addActionListener(this);
		
		searchLibrariesSeparatelyCheckBox = 
				new JCheckBox("Search each library separately");
		panel_2.add(searchLibrariesSeparatelyCheckBox);
		
		horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setPreferredSize(new Dimension(80, 0));
		horizontalStrut.setMinimumSize(new Dimension(80, 0));
		panel_2.add(horizontalStrut);
		panel_2.add(btnRemoveLibrary);

		addLibraryButton = new JButton("Add library");
		addLibraryButton.setActionCommand(MainActionCommands.ADD_PEPSEARCH_LIBRARY_COMMAND.getName());
		addLibraryButton.addActionListener(this);
		panel_2.add(addLibraryButton);
		
		return inputAndLibraryPanel;
	}
	
	private JPanel createSearchOptionsPanel() {
		return createSearchOptionsPanel(false);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JPanel createSearchOptionsPanel(boolean addTitle) {
		
		JPanel searchOptionsPanel = new JPanel();
		if(addTitle) {
			searchOptionsPanel.setBorder(new CompoundBorder(
					new EmptyBorder(10, 10, 10, 10), 
					new TitledBorder(
							new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), 
							"PepSearch settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		}
		else
			searchOptionsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		GridBagLayout gbl_searchOptionsPanel = new GridBagLayout();
		gbl_searchOptionsPanel.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_searchOptionsPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_searchOptionsPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_searchOptionsPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		searchOptionsPanel.setLayout(gbl_searchOptionsPanel);

		JLabel lblPresearchMode = new JLabel("Presearch mode: ");
		GridBagConstraints gbc_lblPresearchMode = new GridBagConstraints();
		gbc_lblPresearchMode.insets = new Insets(0, 0, 5, 5);
		gbc_lblPresearchMode.anchor = GridBagConstraints.EAST;
		gbc_lblPresearchMode.gridx = 0;
		gbc_lblPresearchMode.gridy = 0;
		searchOptionsPanel.add(lblPresearchMode, gbc_lblPresearchMode);

		preSearchModeComboBox =
			new JComboBox(new DefaultComboBoxModel<PreSearchType>(PreSearchType.values()));
		GridBagConstraints gbc_preSearchModeComboBox = new GridBagConstraints();
		gbc_preSearchModeComboBox.gridwidth = 4;
		gbc_preSearchModeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_preSearchModeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_preSearchModeComboBox.gridx = 1;
		gbc_preSearchModeComboBox.gridy = 0;
		searchOptionsPanel.add(preSearchModeComboBox, gbc_preSearchModeComboBox);

		JLabel lblSearchType = new JLabel("Search type: ");
		GridBagConstraints gbc_lblSearchType = new GridBagConstraints();
		gbc_lblSearchType.anchor = GridBagConstraints.EAST;
		gbc_lblSearchType.insets = new Insets(0, 0, 5, 5);
		gbc_lblSearchType.gridx = 0;
		gbc_lblSearchType.gridy = 1;
		searchOptionsPanel.add(lblSearchType, gbc_lblSearchType);

		searchTypeComboBox =
			new JComboBox(new DefaultComboBoxModel<HiResSearchType>(HiResSearchType.values()));
		GridBagConstraints gbc_searchTypeComboBox = new GridBagConstraints();
		gbc_searchTypeComboBox.gridwidth = 4;
		gbc_searchTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_searchTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchTypeComboBox.gridx = 1;
		gbc_searchTypeComboBox.gridy = 1;
		searchOptionsPanel.add(searchTypeComboBox, gbc_searchTypeComboBox);

		JLabel lblSearchOption = new JLabel("Search option: ");
		GridBagConstraints gbc_lblSearchOption = new GridBagConstraints();
		gbc_lblSearchOption.anchor = GridBagConstraints.EAST;
		gbc_lblSearchOption.insets = new Insets(0, 0, 5, 5);
		gbc_lblSearchOption.gridx = 0;
		gbc_lblSearchOption.gridy = 2;
		searchOptionsPanel.add(lblSearchOption, gbc_lblSearchOption);

		searchOptionComboBox =
				new JComboBox(new DefaultComboBoxModel<HiResSearchOption>(HiResSearchOption.values()));
		GridBagConstraints gbc_searchOptionComboBox = new GridBagConstraints();
		gbc_searchOptionComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_searchOptionComboBox.gridwidth = 4;
		gbc_searchOptionComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchOptionComboBox.gridx = 1;
		gbc_searchOptionComboBox.gridy = 2;
		searchOptionsPanel.add(searchOptionComboBox, gbc_searchOptionComboBox);

		JLabel lblHitRejection = new JLabel("Hit rejection: ");
		GridBagConstraints gbc_lblHitRejection = new GridBagConstraints();
		gbc_lblHitRejection.anchor = GridBagConstraints.EAST;
		gbc_lblHitRejection.insets = new Insets(0, 0, 5, 5);
		gbc_lblHitRejection.gridx = 0;
		gbc_lblHitRejection.gridy = 3;
		searchOptionsPanel.add(lblHitRejection, gbc_lblHitRejection);

		hitRejectionComboBox =
			new JComboBox(new DefaultComboBoxModel<HitRejectionOption>(HitRejectionOption.values()));
		GridBagConstraints gbc_hitRejectionComboBox = new GridBagConstraints();
		gbc_hitRejectionComboBox.gridwidth = 4;
		gbc_hitRejectionComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_hitRejectionComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_hitRejectionComboBox.gridx = 1;
		gbc_hitRejectionComboBox.gridy = 3;
		searchOptionsPanel.add(hitRejectionComboBox, gbc_hitRejectionComboBox);

		chckbxAlternativePeakMatching = new JCheckBox("Alternative peak matching (recommended)");
		GridBagConstraints gbc_chckbxAlternativePeakMatching = new GridBagConstraints();
		gbc_chckbxAlternativePeakMatching.gridwidth = 2;
		gbc_chckbxAlternativePeakMatching.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxAlternativePeakMatching.anchor = GridBagConstraints.WEST;
		gbc_chckbxAlternativePeakMatching.gridx = 0;
		gbc_chckbxAlternativePeakMatching.gridy = 4;
		searchOptionsPanel.add(chckbxAlternativePeakMatching, gbc_chckbxAlternativePeakMatching);

		chckbxReverseSearch = new JCheckBox("Reverse search");
		GridBagConstraints gbc_chckbxReverseSearch = new GridBagConstraints();
		gbc_chckbxReverseSearch.gridwidth = 2;
		gbc_chckbxReverseSearch.anchor = GridBagConstraints.WEST;
		gbc_chckbxReverseSearch.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxReverseSearch.gridx = 2;
		gbc_chckbxReverseSearch.gridy = 4;
		searchOptionsPanel.add(chckbxReverseSearch, gbc_chckbxReverseSearch);

		chckbxIgnorePeaksAroundPrecursor = new JCheckBox("Ignore peaks around precursor within ");
		GridBagConstraints gbc_chckbxIgnorePeaksAround = new GridBagConstraints();
		gbc_chckbxIgnorePeaksAround.gridwidth = 2;
		gbc_chckbxIgnorePeaksAround.anchor = GridBagConstraints.WEST;
		gbc_chckbxIgnorePeaksAround.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxIgnorePeaksAround.gridx = 1;
		gbc_chckbxIgnorePeaksAround.gridy = 5;
		searchOptionsPanel.add(chckbxIgnorePeaksAroundPrecursor, gbc_chckbxIgnorePeaksAround);

		ignoreAroundPrecursorTextField =
				new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		GridBagConstraints gbc_ignoreAroundPrecursorTextField = new GridBagConstraints();
		gbc_ignoreAroundPrecursorTextField.insets = new Insets(0, 0, 5, 5);
		gbc_ignoreAroundPrecursorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_ignoreAroundPrecursorTextField.gridx = 3;
		gbc_ignoreAroundPrecursorTextField.gridy = 5;
		searchOptionsPanel.add(ignoreAroundPrecursorTextField, gbc_ignoreAroundPrecursorTextField);

		ignoreAroundPrecursorAccuracyComboBox =
			new JComboBox(new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		//
		GridBagConstraints gbc_ignoreAroundPrecursorAccuracyComboBox = new GridBagConstraints();
		gbc_ignoreAroundPrecursorAccuracyComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_ignoreAroundPrecursorAccuracyComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_ignoreAroundPrecursorAccuracyComboBox.gridx = 4;
		gbc_ignoreAroundPrecursorAccuracyComboBox.gridy = 5;
		searchOptionsPanel.add(ignoreAroundPrecursorAccuracyComboBox, gbc_ignoreAroundPrecursorAccuracyComboBox);

		JLabel lblSearchThreshold = new JLabel("Search threshold: ");
		GridBagConstraints gbc_lblSearchThreshold = new GridBagConstraints();
		gbc_lblSearchThreshold.anchor = GridBagConstraints.EAST;
		gbc_lblSearchThreshold.insets = new Insets(0, 0, 5, 5);
		gbc_lblSearchThreshold.gridx = 0;
		gbc_lblSearchThreshold.gridy = 6;
		searchOptionsPanel.add(lblSearchThreshold, gbc_lblSearchThreshold);

		searchThresholdComboBox =
			new JComboBox(new DefaultComboBoxModel<HiResSearchThreshold>(HiResSearchThreshold.values()));
		GridBagConstraints gbc_searchThresholdComboBox = new GridBagConstraints();
		gbc_searchThresholdComboBox.gridwidth = 2;
		gbc_searchThresholdComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_searchThresholdComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchThresholdComboBox.gridx = 1;
		gbc_searchThresholdComboBox.gridy = 6;
		searchOptionsPanel.add(searchThresholdComboBox, gbc_searchThresholdComboBox);

		JLabel lblPeptideScoring = new JLabel("Peptide scoring: ");
		GridBagConstraints gbc_lblPeptideScoring = new GridBagConstraints();
		gbc_lblPeptideScoring.anchor = GridBagConstraints.EAST;
		gbc_lblPeptideScoring.insets = new Insets(0, 0, 5, 5);
		gbc_lblPeptideScoring.gridx = 3;
		gbc_lblPeptideScoring.gridy = 6;
		searchOptionsPanel.add(lblPeptideScoring, gbc_lblPeptideScoring);

		pepScoreTypeComboBox =
			new JComboBox(new DefaultComboBoxModel<PeptideScoreOption>(PeptideScoreOption.values()));
		GridBagConstraints gbc_pepScoreTypeComboBox = new GridBagConstraints();
		gbc_pepScoreTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_pepScoreTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_pepScoreTypeComboBox.gridx = 4;
		gbc_pepScoreTypeComboBox.gridy = 6;
		searchOptionsPanel.add(pepScoreTypeComboBox, gbc_pepScoreTypeComboBox);

		JLabel lblPrecursorIonMz = new JLabel("Precursor ion m/z uncertainty: ");
		GridBagConstraints gbc_lblPrecursorIonMz = new GridBagConstraints();
		gbc_lblPrecursorIonMz.gridwidth = 2;
		gbc_lblPrecursorIonMz.anchor = GridBagConstraints.EAST;
		gbc_lblPrecursorIonMz.insets = new Insets(0, 0, 5, 5);
		gbc_lblPrecursorIonMz.gridx = 1;
		gbc_lblPrecursorIonMz.gridy = 7;
		searchOptionsPanel.add(lblPrecursorIonMz, gbc_lblPrecursorIonMz);

		precursorMzErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		GridBagConstraints gbc_precMzErrorTextField = new GridBagConstraints();
		gbc_precMzErrorTextField.insets = new Insets(0, 0, 5, 5);
		gbc_precMzErrorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_precMzErrorTextField.gridx = 3;
		gbc_precMzErrorTextField.gridy = 7;
		searchOptionsPanel.add(precursorMzErrorTextField, gbc_precMzErrorTextField);

		precursorMzErrorTypeComboBox =
			new JComboBox(new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		GridBagConstraints gbc_precMzErrorTypeComboBox = new GridBagConstraints();
		gbc_precMzErrorTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_precMzErrorTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_precMzErrorTypeComboBox.gridx = 4;
		gbc_precMzErrorTypeComboBox.gridy = 7;
		searchOptionsPanel.add(precursorMzErrorTypeComboBox, gbc_precMzErrorTypeComboBox);

		JLabel lblPeakMzUncertainty = new JLabel("Peak m/z uncertainty: ");
		GridBagConstraints gbc_lblPeakMzUncertainty = new GridBagConstraints();
		gbc_lblPeakMzUncertainty.gridwidth = 2;
		gbc_lblPeakMzUncertainty.anchor = GridBagConstraints.EAST;
		gbc_lblPeakMzUncertainty.insets = new Insets(0, 0, 5, 5);
		gbc_lblPeakMzUncertainty.gridx = 1;
		gbc_lblPeakMzUncertainty.gridy = 8;
		searchOptionsPanel.add(lblPeakMzUncertainty, gbc_lblPeakMzUncertainty);

		fragmentMzErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		GridBagConstraints gbc_peakMzErrorTextField = new GridBagConstraints();
		gbc_peakMzErrorTextField.insets = new Insets(0, 0, 5, 5);
		gbc_peakMzErrorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_peakMzErrorTextField.gridx = 3;
		gbc_peakMzErrorTextField.gridy = 8;
		searchOptionsPanel.add(fragmentMzErrorTextField, gbc_peakMzErrorTextField);

		fragmentMzErrorTypeComboBox =
			new JComboBox(new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		GridBagConstraints gbc_peakMzErrorTypeComboBox = new GridBagConstraints();
		gbc_peakMzErrorTypeComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_peakMzErrorTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_peakMzErrorTypeComboBox.gridx = 4;
		gbc_peakMzErrorTypeComboBox.gridy = 8;
		searchOptionsPanel.add(fragmentMzErrorTypeComboBox, gbc_peakMzErrorTypeComboBox);

		JLabel lblMzSearchRange = new JLabel("M/Z min.: ");
		GridBagConstraints gbc_lblMzSearchRange = new GridBagConstraints();
		gbc_lblMzSearchRange.anchor = GridBagConstraints.EAST;
		gbc_lblMzSearchRange.insets = new Insets(0, 0, 5, 5);
		gbc_lblMzSearchRange.gridx = 0;
		gbc_lblMzSearchRange.gridy = 9;
		searchOptionsPanel.add(lblMzSearchRange, gbc_lblMzSearchRange);

		mzRangeStartTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		GridBagConstraints gbc_mzRangeStartTextField = new GridBagConstraints();
		gbc_mzRangeStartTextField.insets = new Insets(0, 0, 5, 5);
		gbc_mzRangeStartTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_mzRangeStartTextField.gridx = 1;
		gbc_mzRangeStartTextField.gridy = 9;
		searchOptionsPanel.add(mzRangeStartTextField, gbc_mzRangeStartTextField);

		JLabel lblMzMax = new JLabel("M/Z max.: ");
		GridBagConstraints gbc_lblMzMax = new GridBagConstraints();
		gbc_lblMzMax.anchor = GridBagConstraints.EAST;
		gbc_lblMzMax.insets = new Insets(0, 0, 5, 5);
		gbc_lblMzMax.gridx = 2;
		gbc_lblMzMax.gridy = 9;
		searchOptionsPanel.add(lblMzMax, gbc_lblMzMax);

		mzRangeEndTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 3;
		gbc_formattedTextField.gridy = 9;
		searchOptionsPanel.add(mzRangeEndTextField, gbc_formattedTextField);

		JLabel lblInfinity = new JLabel("-1 = Infinity");
		GridBagConstraints gbc_lblInfinity = new GridBagConstraints();
		gbc_lblInfinity.anchor = GridBagConstraints.WEST;
		gbc_lblInfinity.insets = new Insets(0, 0, 5, 0);
		gbc_lblInfinity.gridx = 4;
		gbc_lblInfinity.gridy = 9;
		searchOptionsPanel.add(lblInfinity, gbc_lblInfinity);

		JLabel lblMinIntensity = new JLabel("Min. intensity: ");
		GridBagConstraints gbc_lblMinIntensity = new GridBagConstraints();
		gbc_lblMinIntensity.anchor = GridBagConstraints.EAST;
		gbc_lblMinIntensity.insets = new Insets(0, 0, 5, 5);
		gbc_lblMinIntensity.gridx = 0;
		gbc_lblMinIntensity.gridy = 10;
		searchOptionsPanel.add(lblMinIntensity, gbc_lblMinIntensity);

		minIntensityTextField =
			new JFormattedTextField(new DecimalFormat("###"));
		GridBagConstraints gbc_minIntensityTextField = new GridBagConstraints();
		gbc_minIntensityTextField.insets = new Insets(0, 0, 5, 5);
		gbc_minIntensityTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_minIntensityTextField.gridx = 1;
		gbc_minIntensityTextField.gridy = 10;
		searchOptionsPanel.add(minIntensityTextField, gbc_minIntensityTextField);

		JLabel label = new JLabel("1 - 999");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.WEST;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 2;
		gbc_label.gridy = 10;
		searchOptionsPanel.add(label, gbc_label);

		JLabel lblLossMw = new JLabel("Unknown MW: ");
		GridBagConstraints gbc_lblLossMw = new GridBagConstraints();
		gbc_lblLossMw.anchor = GridBagConstraints.EAST;
		gbc_lblLossMw.insets = new Insets(0, 0, 5, 5);
		gbc_lblLossMw.gridx = 0;
		gbc_lblLossMw.gridy = 11;
		searchOptionsPanel.add(lblLossMw, gbc_lblLossMw);

		lossMwTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getMzFormat());
		GridBagConstraints gbc_lossMwTextField = new GridBagConstraints();
		gbc_lossMwTextField.insets = new Insets(0, 0, 5, 5);
		gbc_lossMwTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_lossMwTextField.gridx = 1;
		gbc_lossMwTextField.gridy = 11;
		searchOptionsPanel.add(lossMwTextField, gbc_lossMwTextField);

		JLabel lblForHybridSearch = new JLabel("For hybrid search loss");
		GridBagConstraints gbc_lblForHybridSearch = new GridBagConstraints();
		gbc_lblForHybridSearch.anchor = GridBagConstraints.WEST;
		gbc_lblForHybridSearch.gridwidth = 2;
		gbc_lblForHybridSearch.insets = new Insets(0, 0, 5, 5);
		gbc_lblForHybridSearch.gridx = 2;
		gbc_lblForHybridSearch.gridy = 11;
		searchOptionsPanel.add(lblForHybridSearch, gbc_lblForHybridSearch);

		chckbxMatchPolarity = new JCheckBox("Match polarity");
		GridBagConstraints gbc_chckbxMatchPolarity = new GridBagConstraints();
		gbc_chckbxMatchPolarity.anchor = GridBagConstraints.WEST;
		gbc_chckbxMatchPolarity.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxMatchPolarity.gridx = 1;
		gbc_chckbxMatchPolarity.gridy = 12;
		searchOptionsPanel.add(chckbxMatchPolarity, gbc_chckbxMatchPolarity);

		chckbxMatchCharge = new JCheckBox("Match charge");
		GridBagConstraints gbc_chckbxMatchCharge = new GridBagConstraints();
		gbc_chckbxMatchCharge.anchor = GridBagConstraints.WEST;
		gbc_chckbxMatchCharge.gridwidth = 2;
		gbc_chckbxMatchCharge.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxMatchCharge.gridx = 2;
		gbc_chckbxMatchCharge.gridy = 12;
		searchOptionsPanel.add(chckbxMatchCharge, gbc_chckbxMatchCharge);

		chckbxSetHighPriorityProgramExecution = new JCheckBox("Set program execution priority above normal");
		GridBagConstraints gbc_chckbxSetProgramExecution_1 = new GridBagConstraints();
		gbc_chckbxSetProgramExecution_1.anchor = GridBagConstraints.WEST;
		gbc_chckbxSetProgramExecution_1.gridwidth = 3;
		gbc_chckbxSetProgramExecution_1.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxSetProgramExecution_1.gridx = 0;
		gbc_chckbxSetProgramExecution_1.gridy = 13;
		searchOptionsPanel.add(chckbxSetHighPriorityProgramExecution, gbc_chckbxSetProgramExecution_1);

		chckbxLoadLibrariesInMemory = new JCheckBox("Load libraries in memory");
		GridBagConstraints gbc_chckbxLoadLibrariesIn = new GridBagConstraints();
		gbc_chckbxLoadLibrariesIn.anchor = GridBagConstraints.WEST;
		gbc_chckbxLoadLibrariesIn.gridwidth = 3;
		gbc_chckbxLoadLibrariesIn.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxLoadLibrariesIn.gridx = 0;
		gbc_chckbxLoadLibrariesIn.gridy = 14;
		searchOptionsPanel.add(chckbxLoadLibrariesInMemory, gbc_chckbxLoadLibrariesIn);
		
		return searchOptionsPanel;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private JPanel createOutputOptionsPanel() {
		
		JPanel outputOptionsPanel = new JPanel();
		outputOptionsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		GridBagLayout gbl_outputOptionsPanel = new GridBagLayout();
		gbl_outputOptionsPanel.columnWidths = new int[]{131, 0, 0, 0};
		gbl_outputOptionsPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_outputOptionsPanel.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_outputOptionsPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		outputOptionsPanel.setLayout(gbl_outputOptionsPanel);

		JLabel lblMinMatchFactor = new JLabel("Min. match factor: ");
		GridBagConstraints gbc_lblMinMatchFactor = new GridBagConstraints();
		gbc_lblMinMatchFactor.anchor = GridBagConstraints.EAST;
		gbc_lblMinMatchFactor.insets = new Insets(0, 0, 5, 5);
		gbc_lblMinMatchFactor.gridx = 0;
		gbc_lblMinMatchFactor.gridy = 0;
		outputOptionsPanel.add(lblMinMatchFactor, gbc_lblMinMatchFactor);

		minMatchFactorTextField = new JFormattedTextField(new DecimalFormat("###"));
		GridBagConstraints gbc_minMatchFactorTextField = new GridBagConstraints();
		gbc_minMatchFactorTextField.insets = new Insets(0, 0, 5, 5);
		gbc_minMatchFactorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_minMatchFactorTextField.gridx = 1;
		gbc_minMatchFactorTextField.gridy = 0;
		outputOptionsPanel.add(minMatchFactorTextField, gbc_minMatchFactorTextField);

		JLabel label_1 = new JLabel("0-999");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.anchor = GridBagConstraints.WEST;
		gbc_label_1.insets = new Insets(0, 0, 5, 0);
		gbc_label_1.gridx = 2;
		gbc_label_1.gridy = 0;
		outputOptionsPanel.add(label_1, gbc_label_1);

		JLabel lblMaxOf = new JLabel("Max. # of hits: ");
		GridBagConstraints gbc_lblMaxOf = new GridBagConstraints();
		gbc_lblMaxOf.anchor = GridBagConstraints.EAST;
		gbc_lblMaxOf.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaxOf.gridx = 0;
		gbc_lblMaxOf.gridy = 1;
		outputOptionsPanel.add(lblMaxOf, gbc_lblMaxOf);

		maxHitsTextField = new JFormattedTextField(new DecimalFormat("###"));
		GridBagConstraints gbc_maxHitsTextField = new GridBagConstraints();
		gbc_maxHitsTextField.insets = new Insets(0, 0, 5, 5);
		gbc_maxHitsTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxHitsTextField.gridx = 1;
		gbc_maxHitsTextField.gridy = 1;
		outputOptionsPanel.add(maxHitsTextField, gbc_maxHitsTextField);

		JLabel label_2 = new JLabel("0-400");
		GridBagConstraints gbc_label_2 = new GridBagConstraints();
		gbc_label_2.anchor = GridBagConstraints.WEST;
		gbc_label_2.insets = new Insets(0, 0, 5, 0);
		gbc_label_2.gridx = 2;
		gbc_label_2.gridy = 1;
		outputOptionsPanel.add(label_2, gbc_label_2);

		JLabel lblOutputListInclusion = new JLabel("Output list inclusion option: ");
		GridBagConstraints gbc_lblOutputListInclusion = new GridBagConstraints();
		gbc_lblOutputListInclusion.anchor = GridBagConstraints.EAST;
		gbc_lblOutputListInclusion.insets = new Insets(0, 0, 5, 5);
		gbc_lblOutputListInclusion.gridx = 0;
		gbc_lblOutputListInclusion.gridy = 2;
		outputOptionsPanel.add(lblOutputListInclusion, gbc_lblOutputListInclusion);

		outputListInclusionComboBox = new JComboBox(
			new DefaultComboBoxModel<OutputInclusionOption>(OutputInclusionOption.values()));
		GridBagConstraints gbc_outputListInclusionComboBox = new GridBagConstraints();
		gbc_outputListInclusionComboBox.gridwidth = 2;
		gbc_outputListInclusionComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_outputListInclusionComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_outputListInclusionComboBox.gridx = 1;
		gbc_outputListInclusionComboBox.gridy = 2;
		outputOptionsPanel.add(outputListInclusionComboBox, gbc_outputListInclusionComboBox);

		outRevMatchCheckBox = new JCheckBox("Output reverse match factor");
		GridBagConstraints gbc_checkBox_1 = new GridBagConstraints();
		gbc_checkBox_1.anchor = GridBagConstraints.WEST;
		gbc_checkBox_1.insets = new Insets(0, 0, 5, 5);
		gbc_checkBox_1.gridx = 0;
		gbc_checkBox_1.gridy = 3;
		outputOptionsPanel.add(outRevMatchCheckBox, gbc_checkBox_1);

		noHitProbabsCheckBox = new JCheckBox("Do not output hit probabilities");
		GridBagConstraints gbc_noHitProbabsCheckBox = new GridBagConstraints();
		gbc_noHitProbabsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_noHitProbabsCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_noHitProbabsCheckBox.gridx = 1;
		gbc_noHitProbabsCheckBox.gridy = 3;
		outputOptionsPanel.add(noHitProbabsCheckBox, gbc_noHitProbabsCheckBox);

		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridwidth = 3;
		gbc_scrollPane_1.gridx = 0;
		gbc_scrollPane_1.gridy = 4;
		outputOptionsPanel.add(scrollPane_1, gbc_scrollPane_1);

		JPanel panel_3 = new JPanel();
		scrollPane_1.setViewportView(panel_3);
		panel_3.setBorder(new CompoundBorder(
				new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Additional output parameters",
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)),
				new EmptyBorder(10, 10, 10, 10)));
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel_3.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panel_3.setLayout(gbl_panel_3);
		
		outputColumnsCheckboxMap = new TreeMap<NISTPepSearchOutputColumnCode,JCheckBox>();
		int colCount = 1;
		int boxCol = 0;
		int rowCount = 0;
		int lastInset = 5;
		for(NISTPepSearchOutputColumnCode column : NISTPepSearchOutputColumnCode.values()) {
			
			JCheckBox colCheckBox = new JCheckBox(column.getTitle());
			outputColumnsCheckboxMap.put(column, colCheckBox);
			boxCol = (colCount % 2 == 0 ? 1 : 0);
			lastInset = (colCount % 2 == 0 ? 0 : 5);
			if((colCount+1) % 2 == 0)
				rowCount++;
			
			GridBagConstraints gbc_chckbx = new GridBagConstraints();
			gbc_chckbx.anchor = GridBagConstraints.WEST;
			gbc_chckbx.insets = new Insets(0, 0, 5, lastInset);
			gbc_chckbx.gridx = boxCol;
			gbc_chckbx.gridy = rowCount;
			panel_3.add(colCheckBox, gbc_chckbx);
			
			colCount++;
		}	
		return outputOptionsPanel;
	}
	
	private JPanel createCommandPreviewPanel() {
		
		JPanel commandPreviewPanel = new JPanel();
		commandPreviewPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		GridBagLayout gbl_commandPreviewPanel = new GridBagLayout();
		gbl_commandPreviewPanel.columnWidths = new int[]{0, 0, 0};
		gbl_commandPreviewPanel.rowHeights = new int[]{0, 0, 0};
		gbl_commandPreviewPanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_commandPreviewPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		commandPreviewPanel.setLayout(gbl_commandPreviewPanel);
		
		commandPreviewTextArea = new JTextArea();
		commandPreviewTextArea.setEditable(false);
		commandPreviewTextArea.setRows(8);
		commandPreviewTextArea.setWrapStyleWord(true);
		commandPreviewTextArea.setLineWrap(true);
		GridBagConstraints gbc_textArea = new GridBagConstraints();
		gbc_textArea.gridwidth = 2;
		gbc_textArea.insets = new Insets(0, 0, 5, 0);
		gbc_textArea.fill = GridBagConstraints.BOTH;
		gbc_textArea.gridx = 0;
		gbc_textArea.gridy = 0;
		commandPreviewPanel.add(commandPreviewTextArea, gbc_textArea);
		
		copyButton = new JButton(MainActionCommands.COPY_PEPSEARCH_CLI_COMMAND.getName());
		copyButton.setActionCommand(MainActionCommands.COPY_PEPSEARCH_CLI_COMMAND.getName());
		copyButton.addActionListener(this);
		GridBagConstraints gbc_copyButton = new GridBagConstraints();
		gbc_copyButton.insets = new Insets(0, 0, 0, 5);
		gbc_copyButton.gridx = 0;
		gbc_copyButton.gridy = 1;
		commandPreviewPanel.add(copyButton, gbc_copyButton);
		
		generateCommandButton = new JButton(MainActionCommands.GENERATE_PEPSEARCH_CLI_COMMAND.getName());
		generateCommandButton.setActionCommand(MainActionCommands.GENERATE_PEPSEARCH_CLI_COMMAND.getName());
		generateCommandButton.addActionListener(this);
		GridBagConstraints gbc_generateCommandButton = new GridBagConstraints();
		gbc_generateCommandButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_generateCommandButton.gridx = 1;
		gbc_generateCommandButton.gridy = 1;
		commandPreviewPanel.add(generateCommandButton, gbc_generateCommandButton);
		
		return commandPreviewPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();

		if(command.equals(MainActionCommands.SELECT_PEPSEARCH_INPUT_FILE_COMMAND.getName())) {

			if(inputMsMsFileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				inputFileTextField.setText(inputMsMsFileChooser.getSelectedFile().getAbsolutePath());
				inputFileDirectory = inputMsMsFileChooser.getCurrentDirectory();
				inputFile = inputMsMsFileChooser.getSelectedFile();
			}
		}
		if(command.equals(MainActionCommands.REMOVE_PEPSEARCH_LIBRARY_COMMAND.getName()))
			removeSelectedLibrary();

		if(command.equals(MainActionCommands.ADD_PEPSEARCH_LIBRARY_COMMAND.getName()))
			addNistLibrary();
		
		if(command.equals(MainActionCommands.GENERATE_PEPSEARCH_CLI_COMMAND.getName()))
			generatePepSearchCommand();
		
		if(command.equals(MainActionCommands.COPY_PEPSEARCH_CLI_COMMAND.getName()))
			copyPepSearchCommand();
	}

	protected void generatePepSearchCommand() {
		
		Collection<String> errors = validateparameters();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}
		String searchCommand = buildSearchCommand();
		commandPreviewTextArea.setText(searchCommand);
	}

	protected void copyPepSearchCommand() {
		
		String searchCommand = commandPreviewTextArea.getText();
		if(searchCommand.isEmpty())
			return;

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection commandStringSelection = new StringSelection(searchCommand);
		clipboard.setContents(commandStringSelection, commandStringSelection);
	}

	protected void addNistLibrary() {

		Collection<File> presentLibs = libraryTable.getLibraryFiles();
		if(libraryFileChooser.showOpenDialog(inputAndLibraryPanel) == JFileChooser.APPROVE_OPTION) {

			for(File libFile : libraryFileChooser.getSelectedFiles()) {

				if(libFile.exists() && !presentLibs.contains(libFile)) {
					File dbuFile = Paths.get(libFile.getAbsolutePath(), "USER.DBU").toFile();
					if(!dbuFile.exists()) {
						MessageDialog.showErrorMsg(libFile.getName() + " is not a valid NIST format library folder.", this);
						return;
					}
					libraryTable.addLibraryFile(libFile);
				}
			}
			libraryDirectory = libraryFileChooser.getCurrentDirectory();
		}
	}

	protected void removeSelectedLibrary() {

		Collection<File> files = libraryTable.getSelectedLibraryFiles();
		if(files.isEmpty())
			return;

		if(MessageDialog.showChoiceWithWarningMsg(
			"Do you want to remove selected libraries?", this) == JOptionPane.YES_OPTION) {

			libraryTable.removeLibraryFiles(files);
			return;
		}
	}

	public File getInputFile() {

		if(inputFileTextField.getText().trim().isEmpty())
			return null;

		File msmsFile = new File(inputFileTextField.getText().trim());
		if(msmsFile.exists())
			return msmsFile;
		else
			return null;
	}

	public Collection<String>validateparameters(){

		Collection<String>errors = new ArrayList<String>();
		if(MRC2ToolBoxConfiguration.getNISTPepSearchExecutableFile().isEmpty()) {
			errors.add("NIST PepSearch executable not specified (go to preferences).");
		}
		else {
			File psExe = new File(MRC2ToolBoxConfiguration.getNISTPepSearchExecutableFile());
			if(!psExe.exists())
				errors.add("Specified NIST PepSearch executable can not be found:\n" + 
						MRC2ToolBoxConfiguration.getNISTPepSearchExecutableFile());	
			if(!psExe.canExecute()) {
				errors.add("Specified NIST PepSearch command can not be executed:\n" + 
						MRC2ToolBoxConfiguration.getNISTPepSearchExecutableFile());	
			}
		}
		if(fileSource.isSelected() && getInputFile() == null)
			errors.add("No input file provided.");

		if(internalSource.isSelected()) {

			if(listener instanceof IDWorkbenchPanel) {

				if(((IDWorkbenchPanel)listener).getMsMsFeatureBundles(TableRowSubset.ALL).isEmpty())
					errors.add("No MSMS features available for the search.");
			}
		}
		if(libraryTable.getEnabledLibraryFiles().isEmpty()) {
			errors.add("No libraries specified or enabled.");
		}
		else {
			for(File libFile : libraryTable.getEnabledLibraryFiles()) {
				
				if(!libFile.exists()) {
					errors.add(libFile.getName() + " does not exist.");
				}
				else {
					File dbuFile = Paths.get(libFile.getAbsolutePath(), "USER.DBU").toFile();
					if(!dbuFile.exists())
						errors.add(libFile.getName() + " is not a valid NIST format library folder.");
				}
			}
		}
		if(precursorMzErrorTextField.getText().trim().isEmpty())
			errors.add("Precursor mass error not specified.");

		if(fragmentMzErrorTextField.getText().trim().isEmpty())
			errors.add("MSMS peak mass error not specified.");

		if(chckbxIgnorePeaksAroundPrecursor.isSelected() && ignoreAroundPrecursorTextField.getText().trim().isEmpty())
			errors.add("\"Ignore around precursor\" mass error not specified.");
		
		double mzRangeMin = Double.parseDouble(mzRangeStartTextField.getText());
		double mzRangeMax = Double.parseDouble(mzRangeEndTextField.getText());
		if(mzRangeMax != -1.0d && mzRangeMin >= mzRangeMax)
			errors.add("Invalid mass range specified: \"M/Z min\" is larger than \"M/Z max\"");

		if(((HiResSearchOption)searchOptionComboBox.getSelectedItem()).equals(HiResSearchOption.y)) {

			if(!((HiResSearchThreshold)searchThresholdComboBox.getSelectedItem()).equals(HiResSearchThreshold.l))
				errors.add("Hybrid search requires setting search threshold to \"Low\".");
		}
		return errors;
	}

	protected String getMassErrorValueString(String stringValue, MassErrorType errorType) {

		String converted = stringValue;
		if(errorType.equals(MassErrorType.mDa)) {

			double precMassWindowValue = Double.parseDouble(stringValue);
			return Double.toString(precMassWindowValue / 1000.0d);
		}
		return converted;
	}
	
	public List<String>getSearchCommandParts(){
		
		createPepSearchParameterObject();
		
		//	Results file
		String timestamp = MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
		resultsFile = Paths.get(System.getProperty("user.dir"), "data", "mssearch",
				"NIST_MSMS_PEPSEARCH_RESULTS_" + timestamp + ".TXT").toFile();
		
		//	Get input file when searching from external file
		if(fileSource.isSelected()) {
			inputFile = getInputFile();
			return NISTPepSearchResultManipulator.createPepsearchcommandFromParametersObject(
					pepSearchParameterObject,
					inputFile,
					resultsFile);
		}		
		//	Generate input file name on the fly if using database as source of features to search
		if(internalSource.isSelected()) {
			tmpInputFile = Paths.get(System.getProperty("user.dir"), "data", "mssearch",
					 "NIST_MSMS_PEPSEARCH_INPUT_"+ timestamp +".MSP").toFile();
			return NISTPepSearchResultManipulator.createPepsearchcommandFromParametersObject(
					pepSearchParameterObject,
					tmpInputFile,
					resultsFile);
		}
		return null;
	}

	public String buildSearchCommand() {

		Collection<String>commandParts = new ArrayList<String>();

		//	Add binary
		commandParts.add("\"" + MRC2ToolBoxConfiguration.getNISTPepSearchExecutableFile() + "\"");
		//	Pre-search type [{sdfmk[n]}]
		commandParts.add(((PreSearchType)preSearchModeComboBox.getSelectedItem()).name());
		//	Search options	[aijnopqrvx]
		if(chckbxAlternativePeakMatching.isSelected())
			commandParts.add("a");
		if(chckbxIgnorePeaksAroundPrecursor.isSelected())
			commandParts.add("i");
		if(hitRejectionComboBox.getSelectedItem() != null)
			commandParts.add(((HitRejectionOption)hitRejectionComboBox.getSelectedItem()).name());
		if(chckbxReverseSearch.isSelected())
			commandParts.add("r");
		if(outRevMatchCheckBox.isSelected())	//	TODO	Not saved in params object
			commandParts.add("v");
		if(noHitProbabsCheckBox.isSelected())	//	TODO	Not saved in params object
			commandParts.add("x");
		//	Hi-res search option	[{uyz}]
		commandParts.add(((HiResSearchOption)searchOptionComboBox.getSelectedItem()).name());
		//	Hi res threshold:	l e h[n]
		commandParts.add(((HiResSearchThreshold)searchThresholdComboBox.getSelectedItem()).name());
		//	HiRes search type	[{PGD}]
		commandParts.add(((HiResSearchType)searchTypeComboBox.getSelectedItem()).name());
		
		//	Peptide scoring option
		if(pepScoreTypeComboBox.getSelectedIndex() > -1)
			commandParts.add(((PeptideScoreOption)pepScoreTypeComboBox.getSelectedItem()).name());
		
		//	Precursor mass uncertainty
		String precMassWindow = "/Z ";
		if(precursorMzErrorTypeComboBox.getSelectedItem().equals(MassErrorType.ppm))
			precMassWindow = "/ZPPM ";

		precMassWindow += getMassErrorValueString(
				precursorMzErrorTextField.getText(),
				(MassErrorType)precursorMzErrorTypeComboBox.getSelectedItem());
		commandParts.add(precMassWindow);
		
		//	Peak mass uncertainty
		String peakMassWindow = "/M ";
		if(fragmentMzErrorTypeComboBox.getSelectedItem().equals(MassErrorType.ppm))
			peakMassWindow = "/MPPM ";

		peakMassWindow += getMassErrorValueString(
				fragmentMzErrorTextField.getText(),
				(MassErrorType)fragmentMzErrorTypeComboBox.getSelectedItem());
		commandParts.add(peakMassWindow);
		
		//	Exclude masses around precursor
		if(chckbxIgnorePeaksAroundPrecursor.isSelected()) {
			
			String ignoreMassWindow = "/ZI ";
			if(ignoreAroundPrecursorAccuracyComboBox.getSelectedItem().equals(MassErrorType.ppm))
				ignoreMassWindow = "/ZIPPM ";

			ignoreMassWindow += getMassErrorValueString(
					ignoreAroundPrecursorTextField.getText(),
					(MassErrorType)ignoreAroundPrecursorAccuracyComboBox.getSelectedItem());
			commandParts.add(ignoreMassWindow);
		}
		//	Search MZ range
		if(mzRangeStartTextField.getText().isEmpty())
			mzRangeStartTextField.setText("-1");
		if(mzRangeEndTextField.getText().isEmpty())
			mzRangeEndTextField.setText("-1");
		double mzStart = Double.parseDouble(mzRangeStartTextField.getText());
		double mzEnd = Double.parseDouble(mzRangeEndTextField.getText());
		if(mzStart > 0.0d || mzEnd > 0.0d)
			commandParts.add("/MzLimits " + mzRangeStartTextField.getText() + " " + mzRangeEndTextField.getText());
		
		//	Minimal peak intensity
		int minIntensity = Integer.parseInt(minIntensityTextField.getText());
		if(minIntensity > 1 && minIntensity <= 999)
			commandParts.add("/MinInt " + minIntensityTextField.getText());

		//	Add loss MW if hybrid search
		if(searchOptionComboBox.getSelectedItem().equals(HiResSearchOption.y)
				&& !lossMwTextField.getText().isEmpty()) {

			double mwForLoss = Double.parseDouble(lossMwTextField.getText());
			if(mwForLoss > 0)
				commandParts.add("/MwForLoss " + lossMwTextField.getText());
		}
		if(chckbxMatchCharge.isSelected())
			commandParts.add("/MatchCharge");

		if(chckbxMatchPolarity.isSelected())
			commandParts.add("/MatchPolarity");

		if(chckbxSetHighPriorityProgramExecution.isSelected())
			commandParts.add("/HiPri ");

		if(chckbxLoadLibrariesInMemory.isSelected())
			commandParts.add("/LibInMem");

		//	Add libraries
		for(File libFile : libraryTable.getEnabledLibraryFiles())
			commandParts.add("/LIB \"" + libFile.getAbsolutePath() + "\"");

		//	Add input file when searching from external file
		if(fileSource.isSelected()) {
			commandParts.add("/INP \"" + getInputFile().getAbsolutePath() + "\"");
		}
		String timestamp = MRC2ToolBoxConfiguration.getFileTimeStampFormat().format(new Date());
		//	Generate input file name on the fly if using database as source of features to search
		if(internalSource.isSelected()) {

			tmpInputFile = Paths.get(System.getProperty("user.dir"), "data", "mssearch",
					"NIST_MSMS_PEPSEARCH_INPUT_" + timestamp + ".MSP").toFile();
			commandParts.add("/INP \"" + tmpInputFile.getAbsolutePath() + "\"");
		}
		//	Add output file	
		resultsFile = Paths.get(System.getProperty("user.dir"), "data", "mssearch",
				"NIST_MSMS_PEPSEARCH_RESULTS_" + timestamp +  ".TXT").toFile();
		commandParts.add("/OUTTAB \"" + resultsFile.getAbsolutePath() + "\"");

		//	Add output options
		//	Minimal match factor
		commandParts.add("/MinMF " + minMatchFactorTextField.getText());
		//	Hits per compound
		commandParts.add("/HITS " + maxHitsTextField.getText());
		//	Add diagnostics to stderr
		//	commandParts.add("/TIME /PROGRESS");
		//	Inclusion list
		commandParts.add("/" + ((OutputInclusionOption)outputListInclusionComboBox.getSelectedItem()).name());

		String outputColumns = "/COL ";
		ArrayList<String>columns = new ArrayList<String>();
		for(NISTPepSearchOutputColumnCode code : NISTPepSearchOutputColumnCode.values()) {
			JCheckBox chkBox = outputColumnsCheckboxMap.get(code);
			if(chkBox != null && chkBox.isSelected())
				columns.add(code.getCode());		
		}
		commandParts.add(outputColumns + StringUtils.join(columns, ","));
		String commandString = StringUtils.join(commandParts, " ");
		//	System.out.println(commandString);
		return commandString;
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	public void loadPreferences(Preferences prefs) {

		preferences = prefs;
		loadInputAndLibraryPreferences();
		loadSearchOptionsPreferences();
		loadOutputPreferences();
	}

	protected void loadInputAndLibraryPreferences() {

		inputFileTextField.setText(preferences.get(INPUT_FILE, null));
		inputFileDirectory =
			new File(preferences.get(INPUT_FILE_DIR, MRC2ToolBoxConfiguration.getDefaultProjectsDirectory())).
			getAbsoluteFile();
		libraryDirectory =
			new File(preferences.get(LIBRARY_DIR, MRC2ToolBoxConfiguration.getDefaultProjectsDirectory())).
			getAbsoluteFile();
		
		TableRowSubset subset = TableRowSubset.getSubsetByName(
				preferences.get(TABLE_ROW_SUBSET, TableRowSubset.ALL.name()));
		featureSubsetComboBox.setSelectedItem(subset);

		String libListString = preferences.get(LIB_LIST, "");
		Map<File,Boolean>libFiles = new TreeMap<File,Boolean>();
		if(!libListString.isEmpty()) {
			String[] libPaths = libListString.split("\\|");
			if(libPaths.length > 0) {

				for(String lp : libPaths) {

					String[] libData = lp.split("@");
					File libFile = new File(libData[0]);
					Boolean active = Boolean.valueOf(libData[1]);
					if(libFile.exists())
						libFiles.put(libFile, active);				
				}
			}
		}
		libraryTable.setModelFromFiles(libFiles);
		boolean useInputFile = preferences.getBoolean(USE_INPUT_FILE, false);
		if(useInputFile)
			fileSource.setSelected(true);
		else
			internalSource.setSelected(true);
		
		searchLibrariesSeparatelyCheckBox.setSelected(
				preferences.getBoolean(SEARCH_LIBS_SEPARATELY, true));
	}

	protected void loadSearchOptionsPreferences() {

		preSearchModeComboBox.setSelectedItem(
			PreSearchType.getOptionByName(preferences.get(PRESEARCH_MODE, PreSearchType.f.name())));
		searchTypeComboBox.setSelectedItem(
			HiResSearchType.getOptionByName(preferences.get(SEARCH_TYPE, HiResSearchType.G.name())));
		searchOptionComboBox.setSelectedItem(
			HiResSearchOption.getOptionByName(preferences.get(SEARCH_OPTION, HiResSearchOption.z.name())));
		hitRejectionComboBox.setSelectedItem(
			HitRejectionOption.getOptionByName(preferences.get(HIT_REJECTION_OPTION, null)));
		chckbxReverseSearch.setSelected(preferences.getBoolean(ENABLE_REVERSE_SEARCH, false));
		chckbxAlternativePeakMatching.setSelected(preferences.getBoolean(ENABLE_ALTERNATIVE_PEAK_MATCHING, true));
		chckbxIgnorePeaksAroundPrecursor.setSelected(preferences.getBoolean(IGNORE_PEAKS_AROUND_PRECURSOR, true));
		ignoreAroundPrecursorTextField.setText(
			MRC2ToolBoxConfiguration.getPpmFormat().format(preferences.getDouble(IGNORE_PEAKS_AROUND_PRECURSOR_WINDOW, 1.6d)));
		ignoreAroundPrecursorAccuracyComboBox.setSelectedItem(
			MassErrorType.getTypeByName(preferences.get(IGNORE_PEAKS_AROUND_PRECURSOR_UNITS, MassErrorType.Da.name())));
		searchThresholdComboBox.setSelectedItem(
			HiResSearchThreshold.getOptionByName(preferences.get(SEARCH_THRESHOLD_OPTION, HiResSearchThreshold.h.name())));
		pepScoreTypeComboBox.setSelectedItem(
			PeptideScoreOption.getOptionByName(preferences.get(PEPTIDE_SCORE_OPTION, null)));
		precursorMzErrorTextField.setText(
			MRC2ToolBoxConfiguration.getPpmFormat().format(preferences.getDouble(PRECURSOR_MASS_ACCURACY, 100.0d)));
		precursorMzErrorTypeComboBox.setSelectedItem(
			MassErrorType.getTypeByName(preferences.get(PRECURSOR_MASS_ACCURACY_UNITS, MassErrorType.ppm.name())));
		fragmentMzErrorTextField.setText(
				MRC2ToolBoxConfiguration.getPpmFormat().format(preferences.getDouble(FRAGMENT_MASS_ACCURACY, 100.0d)));
		fragmentMzErrorTypeComboBox.setSelectedItem(
			MassErrorType.getTypeByName(preferences.get(FRAGMENT_MASS_ACCURACY_UNITS, MassErrorType.ppm.name())));
		mzRangeStartTextField.setText(
			MRC2ToolBoxConfiguration.getMzFormat().format(preferences.getDouble(SEARCH_MZ_LOW_CUTOFF, 0.0d)));
		mzRangeEndTextField.setText(
			MRC2ToolBoxConfiguration.getMzFormat().format(preferences.getDouble(SEARCH_MZ_HIGH_CUTOFF, 2000.0d)));
		minIntensityTextField.setText(
			(Integer.toString(preferences.getInt(MIN_INTENSITY_CUTOFF, 1))));
		lossMwTextField.setText(
			MRC2ToolBoxConfiguration.getMzFormat().format(preferences.getDouble(HYBRID_SEARCH_MW_LOSS, 0.0d)));
		chckbxMatchPolarity.setSelected(preferences.getBoolean(MATCH_POLARITY, false));
		chckbxMatchCharge.setSelected(preferences.getBoolean(MATCH_CHARGE, false));
		chckbxSetHighPriorityProgramExecution.setSelected(preferences.getBoolean(SET_HIGH_EXECUTION_PRIORITY, true));
		chckbxLoadLibrariesInMemory.setSelected(preferences.getBoolean(LOAD_LIBRARIES_IN_MEMORY, true));
	}
	
	protected void loadOutputPreferences() {

		minMatchFactorTextField.setText(
			(Integer.toString(preferences.getInt(MIN_MATCH_FACTOR, 450))));
		maxHitsTextField.setText(
			(Integer.toString(preferences.getInt(MAX_NUM_HITS, 5))));
		outputListInclusionComboBox.setSelectedItem(
			OutputInclusionOption.getOptionByName(
				preferences.get(FOUND_NOT_FOUND_OUTPUT, OutputInclusionOption.OnlyFound.name())));

		outRevMatchCheckBox.setSelected(preferences.getBoolean(OUTPUT_REVERSE_MATCH_FACTOR, false));
		noHitProbabsCheckBox.setSelected(preferences.getBoolean(NO_HIT_PROBABILITIES, false));

		//	Export columns
		for(NISTPepSearchOutputColumnCode code : NISTPepSearchOutputColumnCode.values()) {
			JCheckBox chkBox = outputColumnsCheckboxMap.get(code);
			if(chkBox != null) {
				chkBox.setSelected(preferences.getBoolean(code.name(), code.includeByDefault()));
			}
		}
	}
	
	public void loadDefaultSearchOptionsPreferences() {

		preSearchModeComboBox.setSelectedItem(PreSearchType.d);
		searchTypeComboBox.setSelectedItem(HiResSearchType.G);
		searchOptionComboBox.setSelectedItem(HiResSearchOption.z);
		hitRejectionComboBox.setSelectedItem(null);
		chckbxReverseSearch.setSelected(false);
		chckbxAlternativePeakMatching.setSelected(true);
		chckbxIgnorePeaksAroundPrecursor.setSelected(true);
		ignoreAroundPrecursorTextField.setText(MRC2ToolBoxConfiguration.getPpmFormat().format(1.6d));
		ignoreAroundPrecursorAccuracyComboBox.setSelectedItem(MassErrorType.Da);
		searchThresholdComboBox.setSelectedItem(HiResSearchThreshold.h);
		pepScoreTypeComboBox.setSelectedItem(null);
		precursorMzErrorTextField.setText(MRC2ToolBoxConfiguration.getPpmFormat().format(100.0d));
		precursorMzErrorTypeComboBox.setSelectedItem(MassErrorType.ppm);
		fragmentMzErrorTextField.setText(MRC2ToolBoxConfiguration.getPpmFormat().format(100.0d));
		fragmentMzErrorTypeComboBox.setSelectedItem(MassErrorType.ppm);
		mzRangeStartTextField.setText(MRC2ToolBoxConfiguration.getMzFormat().format(0.0d));
		mzRangeEndTextField.setText(MRC2ToolBoxConfiguration.getMzFormat().format(2000.0d));
		minIntensityTextField.setText(Integer.toString(1));
		lossMwTextField.setText(MRC2ToolBoxConfiguration.getMzFormat().format(0.0d));
		chckbxMatchPolarity.setSelected(true);
		chckbxMatchCharge.setSelected(false);
		chckbxSetHighPriorityProgramExecution.setSelected(true);
		chckbxLoadLibrariesInMemory.setSelected(true);
	}
	
	public void loadDefaultInputAndLibraryPreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		inputFileTextField.setText("");
		inputFileDirectory =
			new File(preferences.get(INPUT_FILE_DIR, MRC2ToolBoxConfiguration.getDefaultProjectsDirectory())).
			getAbsoluteFile();
		libraryDirectory =
			new File(preferences.get(LIBRARY_DIR, MRC2ToolBoxConfiguration.getDefaultProjectsDirectory())).
			getAbsoluteFile();
		
		featureSubsetComboBox.setSelectedItem(TableRowSubset.ALL);

		String libListString = preferences.get(LIB_LIST, "");
		Map<File,Boolean>libFiles = new TreeMap<File,Boolean>();
		if(!libListString.isEmpty()) {
			String[] libPaths = libListString.split("\\|");
			if(libPaths.length > 0) {

				for(String lp : libPaths) {

					String[] libData = lp.split("@");
					File libFile = new File(libData[0]);
					Boolean active = Boolean.valueOf(libData[1]);
					if(libFile.exists())
						libFiles.put(libFile, active);				
				}
			}
		}
		libraryTable.setModelFromFiles(libFiles);
		internalSource.setSelected(true);
	}

	public void loadDefaultOutputPreferences() {

		minMatchFactorTextField.setText(Integer.toString(20));
		maxHitsTextField.setText(Integer.toString(5));
		outputListInclusionComboBox.setSelectedItem(OutputInclusionOption.OnlyFound);
		outRevMatchCheckBox.setSelected(true);
		noHitProbabsCheckBox.setSelected(false);		

		for(NISTPepSearchOutputColumnCode code : defaultEnabledColumns) {
			JCheckBox chkBox = outputColumnsCheckboxMap.get(code);
			if(chkBox != null)
				chkBox.setSelected(true);
		}	
		JCheckBox chkBox = outputColumnsCheckboxMap.get(
				NISTPepSearchOutputColumnCode.BEST_HITS_ONLY_COLUMN);
		if(chkBox != null)
			chkBox.setSelected(false);
	}
	
	public void loadDefaultDecoySearchparameters() {
		
		loadDefaultSearchOptionsPreferences();
		loadDefaultInputAndLibraryPreferences();		
		loadDefaultOutputPreferences();
		
		preSearchModeComboBox.setEnabled(false);
		searchTypeComboBox.setEnabled(false);
		searchOptionComboBox.setEnabled(false);
		hitRejectionComboBox.setEnabled(false);
		chckbxReverseSearch.setEnabled(false);
		chckbxAlternativePeakMatching.setEnabled(false);
		chckbxIgnorePeaksAroundPrecursor.setEnabled(false);
		searchThresholdComboBox.setEnabled(false);
		pepScoreTypeComboBox.setEnabled(false);
		mzRangeStartTextField.setEnabled(false);
		mzRangeEndTextField.setEnabled(false);
		minIntensityTextField.setEnabled(false);
		lossMwTextField.setEnabled(false);
		chckbxMatchCharge.setEnabled(false);
		chckbxSetHighPriorityProgramExecution.setEnabled(false);
		chckbxLoadLibrariesInMemory.setEnabled(false);
		
		featureSubsetComboBox.setEnabled(false);
		fileSource.setEnabled(false);
		internalSource.setEnabled(false);
		
		minMatchFactorTextField.setText(Integer.toString(20));
		outRevMatchCheckBox.setEnabled(false);
		noHitProbabsCheckBox.setEnabled(false);	
		
		for(NISTPepSearchOutputColumnCode code : defaultEnabledColumns) {
			JCheckBox chkBox = outputColumnsCheckboxMap.get(code);
			if(chkBox != null)
				chkBox.setEnabled(false);
		}
		JCheckBox chkBox = outputColumnsCheckboxMap.get(NISTPepSearchOutputColumnCode.BEST_HITS_ONLY_COLUMN);
		if(chkBox != null) {
			chkBox.setSelected(true);
			chkBox.setEnabled(false);
		}
	}
	
	public void saveLibraryPreferences() {
		
		preferences = Preferences.userNodeForPackage(this.getClass());
		Map<File,Boolean>libMap = libraryTable.getLibraryFilesMap();
		ArrayList<String>libData = new ArrayList<String>();
		libMap.entrySet().stream().
			forEach(e -> libData.add(e.getKey().getAbsolutePath() + "@" + Boolean.toString(e.getValue())));
		preferences.put(LIB_LIST, StringUtils.join(libData, "|"));	
	}

	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());

		//	Input
		preferences.put(INPUT_FILE, inputFileTextField.getText().trim());
		preferences.put(INPUT_FILE_DIR, inputFileDirectory.getAbsolutePath());
		preferences.put(LIBRARY_DIR, libraryDirectory.getAbsolutePath());
		
		//	Libraries
		Map<File,Boolean>libMap = libraryTable.getLibraryFilesMap();
		ArrayList<String>libData = new ArrayList<String>();
		libMap.entrySet().stream().
			forEach(e -> libData.add(e.getKey().getAbsolutePath() + 
					"@" + Boolean.toString(e.getValue())));
		preferences.put(LIB_LIST, StringUtils.join(libData, "|"));		
		
		preferences.putBoolean(USE_INPUT_FILE, fileSource.isSelected());
		TableRowSubset subset = (TableRowSubset)featureSubsetComboBox.getSelectedItem();
		preferences.put(TABLE_ROW_SUBSET, subset.name());
		
		preferences.putBoolean(SEARCH_LIBS_SEPARATELY, 
				searchLibrariesSeparatelyCheckBox.isSelected());
	
		//	Search settings
		preferences.put(PRESEARCH_MODE, ((PreSearchType)preSearchModeComboBox.getSelectedItem()).name());
		preferences.put(SEARCH_TYPE, ((HiResSearchType)searchTypeComboBox.getSelectedItem()).name());
		preferences.put(SEARCH_OPTION, ((HiResSearchOption)searchOptionComboBox.getSelectedItem()).name());

		String hitRejOption = "";
		if(hitRejectionComboBox.getSelectedItem() != null)
			hitRejOption = ((HitRejectionOption)hitRejectionComboBox.getSelectedItem()).name();
		preferences.put(HIT_REJECTION_OPTION, hitRejOption);

		String pepScoreOption = "";
		if(pepScoreTypeComboBox.getSelectedItem() != null)
			pepScoreOption = ((PeptideScoreOption)pepScoreTypeComboBox.getSelectedItem()).name();
		preferences.put(PEPTIDE_SCORE_OPTION, pepScoreOption);

		preferences.putBoolean(ENABLE_REVERSE_SEARCH, chckbxReverseSearch.isSelected());
		preferences.putBoolean(ENABLE_ALTERNATIVE_PEAK_MATCHING, chckbxAlternativePeakMatching.isSelected());
		preferences.putBoolean(IGNORE_PEAKS_AROUND_PRECURSOR, chckbxIgnorePeaksAroundPrecursor.isSelected());
		preferences.putDouble(IGNORE_PEAKS_AROUND_PRECURSOR_WINDOW,
			Double.parseDouble(ignoreAroundPrecursorTextField.getText()));
		preferences.put(IGNORE_PEAKS_AROUND_PRECURSOR_UNITS,
			((MassErrorType)ignoreAroundPrecursorAccuracyComboBox.getSelectedItem()).name());
		preferences.put(SEARCH_THRESHOLD_OPTION,
			((HiResSearchThreshold)searchThresholdComboBox.getSelectedItem()).name());
		preferences.putDouble(PRECURSOR_MASS_ACCURACY,
				Double.parseDouble(precursorMzErrorTextField.getText()));
		preferences.put(PRECURSOR_MASS_ACCURACY_UNITS,
			((MassErrorType)precursorMzErrorTypeComboBox.getSelectedItem()).name());
		preferences.putDouble(FRAGMENT_MASS_ACCURACY,
				Double.parseDouble(fragmentMzErrorTextField.getText()));
		preferences.put(FRAGMENT_MASS_ACCURACY_UNITS,
			((MassErrorType)fragmentMzErrorTypeComboBox.getSelectedItem()).name());
		preferences.putDouble(SEARCH_MZ_LOW_CUTOFF, Double.parseDouble(mzRangeStartTextField.getText()));
		preferences.putDouble(SEARCH_MZ_HIGH_CUTOFF, Double.parseDouble(mzRangeEndTextField.getText()));
		preferences.putDouble(HYBRID_SEARCH_MW_LOSS, Double.parseDouble(lossMwTextField.getText()));
		preferences.putInt(MIN_INTENSITY_CUTOFF, Integer.parseInt(minIntensityTextField.getText()));
		preferences.putBoolean(MATCH_POLARITY, chckbxMatchPolarity.isSelected());
		preferences.putBoolean(MATCH_CHARGE, chckbxMatchCharge.isSelected());
		preferences.putBoolean(SET_HIGH_EXECUTION_PRIORITY, chckbxSetHighPriorityProgramExecution.isSelected());
		preferences.putBoolean(LOAD_LIBRARIES_IN_MEMORY, chckbxLoadLibrariesInMemory.isSelected());
		//	Output options
		preferences.putInt(MIN_MATCH_FACTOR, Integer.parseInt(minMatchFactorTextField.getText()));
		preferences.putInt(MAX_NUM_HITS, Integer.parseInt(maxHitsTextField.getText()));
		preferences.put(FOUND_NOT_FOUND_OUTPUT,
			((OutputInclusionOption)outputListInclusionComboBox.getSelectedItem()).name());

		preferences.putBoolean(OUTPUT_REVERSE_MATCH_FACTOR, outRevMatchCheckBox.isSelected());
		preferences.putBoolean(NO_HIT_PROBABILITIES, noHitProbabsCheckBox.isSelected());
		
		for(NISTPepSearchOutputColumnCode code : NISTPepSearchOutputColumnCode.values()) {
			JCheckBox chkBox = outputColumnsCheckboxMap.get(code);
			if(chkBox != null)
				preferences.putBoolean(code.name(), chkBox.isSelected());
		}
	}

	protected void createPepSearchParameterObject() {

		pepSearchParameterObject = new NISTPepSearchParameterObject();
		
		//	Library and feature source
		pepSearchParameterObject.setLibraryDirectory(libraryDirectory);
		pepSearchParameterObject.getLibraryFiles().addAll(libraryTable.getEnabledLibraryFiles());
		pepSearchParameterObject.setUseInputFile(fileSource.isSelected());	
		
		//	 Search parameters
		pepSearchParameterObject.setPreSearchType((PreSearchType)preSearchModeComboBox.getSelectedItem());
		pepSearchParameterObject.setHiResSearchType((HiResSearchType)searchTypeComboBox.getSelectedItem());
		pepSearchParameterObject.setHiResSearchOption((HiResSearchOption)searchOptionComboBox.getSelectedItem());
		pepSearchParameterObject.setHitRejectionOption((HitRejectionOption)hitRejectionComboBox.getSelectedItem());
		pepSearchParameterObject.setPeptideScoreOption((PeptideScoreOption)pepScoreTypeComboBox.getSelectedItem());
		pepSearchParameterObject.setEnableReverseSearch(chckbxReverseSearch.isSelected());
		pepSearchParameterObject.setEnableAlternativePeakMatching(chckbxAlternativePeakMatching.isSelected());
		pepSearchParameterObject.setIgnorePeaksAroundPrecursor(chckbxIgnorePeaksAroundPrecursor.isSelected());
		pepSearchParameterObject.setIgnorePeaksAroundPrecursorWindow(Double.parseDouble(ignoreAroundPrecursorTextField.getText()));
		pepSearchParameterObject.setIgnorePeaksAroundPrecursorAccuracyUnits((MassErrorType)ignoreAroundPrecursorAccuracyComboBox.getSelectedItem());
		pepSearchParameterObject.setHiResSearchThreshold((HiResSearchThreshold)searchThresholdComboBox.getSelectedItem());
		pepSearchParameterObject.setPrecursorMzErrorValue(Double.parseDouble(precursorMzErrorTextField.getText()));
		pepSearchParameterObject.setPrecursorMzErrorType((MassErrorType)precursorMzErrorTypeComboBox.getSelectedItem());
		pepSearchParameterObject.setFragmentMzErrorValue(Double.parseDouble(fragmentMzErrorTextField.getText()));
		pepSearchParameterObject.setFragmentMzErrorType((MassErrorType)fragmentMzErrorTypeComboBox.getSelectedItem());
		
		double mzRangeMin = Double.parseDouble(mzRangeStartTextField.getText());
		double mzRangeMax = Double.parseDouble(mzRangeEndTextField.getText());
		if(mzRangeMax == -1.0d)
			mzRangeMax = NISTPepSearchParameterObject.INFINITE_MZ_UPPER_LIMIT;
		
		Range mzRange = new Range(mzRangeMin, mzRangeMax);
		pepSearchParameterObject.setMzRange(mzRange);
		pepSearchParameterObject.setHybridSearchMassLoss(Double.parseDouble(lossMwTextField.getText()));
		pepSearchParameterObject.setMinimumIntensityCutoff(Integer.parseInt(minIntensityTextField.getText()));
		pepSearchParameterObject.setMatchPolarity(chckbxMatchPolarity.isSelected());
		pepSearchParameterObject.setMatchCharge(chckbxMatchCharge.isSelected());
		pepSearchParameterObject.setHighExecutionPriority(chckbxSetHighPriorityProgramExecution.isSelected());
		pepSearchParameterObject.setLoadLibrariesInMemory(chckbxLoadLibrariesInMemory.isSelected());
		
		//	Output filtering options
		pepSearchParameterObject.setMinMatchFactor(Integer.parseInt(minMatchFactorTextField.getText()));
		pepSearchParameterObject.setMaxNumberOfHits(Integer.parseInt(maxHitsTextField.getText()));
		pepSearchParameterObject.setOutputInclusionOption((OutputInclusionOption)outputListInclusionComboBox.getSelectedItem());
		
		//	Output columns and some other boolean output options
		Map<String, Boolean> outputColumnMap = pepSearchParameterObject.getOutputColumns();		
		outputColumnMap.put(OUTPUT_REVERSE_MATCH_FACTOR, outRevMatchCheckBox.isSelected());
		outputColumnMap.put(NO_HIT_PROBABILITIES, noHitProbabsCheckBox.isSelected());
		
		for(NISTPepSearchOutputColumnCode code : NISTPepSearchOutputColumnCode.values()) {
			JCheckBox chkBox = outputColumnsCheckboxMap.get(code);
			if(chkBox != null)
				outputColumnMap.put(code.name(), chkBox.isSelected());
		}
	}
	
	public void loadNISTPepSearchParameterObject(NISTPepSearchParameterObject parameterSet)  {
		
		loadLibrariesAndFeatureSource(parameterSet);
		loadSearchParameters(parameterSet);
		loadOutputFilteringParameters(parameterSet);
		loadOutputColumnsParameters(parameterSet);
	}
	
	public void loadLibrariesAndFeatureSource(NISTPepSearchParameterObject parameterSet) {

		ArrayList<String>missingLibraries = new ArrayList<String>();
		libraryDirectory = parameterSet.getLibraryDirectory();
		for(File f : parameterSet.getLibraryFiles()) {
			
			if(!f.exists())
				missingLibraries.add("Library " + f.getAbsolutePath() + " doesn't exist.\n");
		}
		Map<File,Boolean>libFiles = new TreeMap<File,Boolean>();
		for(File f : parameterSet.getLibraryFiles()) {
			if(!f.exists())
				libFiles.put(f, true);
		}
		libraryTable.setModelFromFiles(libFiles);
		if(libraryDirectory != null && !libraryDirectory.exists())
			missingLibraries.add("Library directory " + libraryDirectory.getAbsolutePath() + " doesn't exist.\n");
				
		fileSource.setSelected(parameterSet.isUseInputFile());
		if(!missingLibraries.isEmpty())
			MessageDialog.showWarningMsg(StringUtils.join(missingLibraries), this);
	}
	
	public void loadSearchParameters(NISTPepSearchParameterObject parameterSet) {

		preSearchModeComboBox.setSelectedItem(parameterSet.getPreSearchType());
		searchTypeComboBox.setSelectedItem(parameterSet.getHiResSearchType());
		searchOptionComboBox.setSelectedItem(parameterSet.getHiResSearchOption());
		hitRejectionComboBox.setSelectedItem(parameterSet.getHitRejectionOption());
		pepScoreTypeComboBox.setSelectedItem(parameterSet.getPeptideScoreOption());
		chckbxReverseSearch.setSelected(parameterSet.isEnableReverseSearch());
		chckbxAlternativePeakMatching.setSelected(parameterSet.isEnableAlternativePeakMatching()); 
		chckbxIgnorePeaksAroundPrecursor.setSelected(parameterSet.isIgnorePeaksAroundPrecursor());
		ignoreAroundPrecursorTextField.setText(Double.toString(parameterSet.getIgnorePeaksAroundPrecursorWindow()));
		ignoreAroundPrecursorAccuracyComboBox.setSelectedItem(parameterSet.getIgnorePeaksAroundPrecursorAccuracyUnits());
		searchThresholdComboBox.setSelectedItem(parameterSet.getHiResSearchThreshold());
		precursorMzErrorTextField.setText(Double.toString(parameterSet.getPrecursorMzErrorValue()));
		precursorMzErrorTypeComboBox.setSelectedItem(parameterSet.getPrecursorMzErrorType());
		fragmentMzErrorTextField.setText(Double.toString(parameterSet.getFragmentMzErrorValue()));
		fragmentMzErrorTypeComboBox.setSelectedItem(parameterSet.getFragmentMzErrorType());
		
		mzRangeStartTextField.setText(Double.toString(parameterSet.getMzRange().getMin()));
		double mzMax = parameterSet.getMzRange().getMax();
		if(mzMax == NISTPepSearchParameterObject.INFINITE_MZ_UPPER_LIMIT)
			mzMax = -1;	
		
		mzRangeEndTextField.setText(Double.toString(mzMax));
		lossMwTextField.setText("");
		if(parameterSet.getHybridSearchMassLoss() > 0.0d)
			lossMwTextField.setText(Double.toString(parameterSet.getHybridSearchMassLoss()));
		
		minIntensityTextField.setText(Integer.toString(parameterSet.getMinimumIntensityCutoff()));
		chckbxMatchPolarity.setSelected(parameterSet.isMatchPolarity());
		chckbxMatchCharge.setSelected(parameterSet.isMatchCharge());
		chckbxSetHighPriorityProgramExecution.setSelected(parameterSet.isHighExecutionPriority());
		chckbxLoadLibrariesInMemory.setSelected(parameterSet.isLoadLibrariesInMemory());		
	}
	
	public void adjustDefaultDecoySearchParametersFromParameterSet(NISTPepSearchParameterObject parameterSet) {
		
		preSearchModeComboBox.setSelectedItem(parameterSet.getPreSearchType());
		searchTypeComboBox.setSelectedItem(parameterSet.getHiResSearchType());
		searchOptionComboBox.setSelectedItem(parameterSet.getHiResSearchOption());
		chckbxAlternativePeakMatching.setSelected(parameterSet.isEnableAlternativePeakMatching()); 
		chckbxIgnorePeaksAroundPrecursor.setSelected(parameterSet.isIgnorePeaksAroundPrecursor());
		ignoreAroundPrecursorTextField.setText(Double.toString(parameterSet.getIgnorePeaksAroundPrecursorWindow()));
		ignoreAroundPrecursorAccuracyComboBox.setSelectedItem(parameterSet.getIgnorePeaksAroundPrecursorAccuracyUnits());
		searchThresholdComboBox.setSelectedItem(parameterSet.getHiResSearchThreshold());
		precursorMzErrorTextField.setText(Double.toString(parameterSet.getPrecursorMzErrorValue()));
		precursorMzErrorTypeComboBox.setSelectedItem(parameterSet.getPrecursorMzErrorType());
		fragmentMzErrorTextField.setText(Double.toString(parameterSet.getFragmentMzErrorValue()));
		fragmentMzErrorTypeComboBox.setSelectedItem(parameterSet.getFragmentMzErrorType());
		
		mzRangeStartTextField.setText(Double.toString(parameterSet.getMzRange().getMin()));
		double mzMax = parameterSet.getMzRange().getMax();
		if(mzMax == NISTPepSearchParameterObject.INFINITE_MZ_UPPER_LIMIT)
			mzMax = -1;	
		
		mzRangeEndTextField.setText(Double.toString(mzMax));
		minIntensityTextField.setText(Integer.toString(parameterSet.getMinimumIntensityCutoff()));
	}
	
	public void loadOutputFilteringParameters(NISTPepSearchParameterObject parameterSet) {

		minMatchFactorTextField.setText(Integer.toString(parameterSet.getMinMatchFactor()));
		maxHitsTextField.setText(Integer.toString(parameterSet.getMaxNumberOfHits()));
		outputListInclusionComboBox.setSelectedItem(parameterSet.getOutputInclusionOption());
	}
	
	public void loadOutputColumnsParameters(NISTPepSearchParameterObject parameterSet) {

		Map<String, Boolean> outputColumnMap = parameterSet.getOutputColumns();
		
		outRevMatchCheckBox.setSelected(outputColumnMap.get(OUTPUT_REVERSE_MATCH_FACTOR));
		noHitProbabsCheckBox.setSelected(outputColumnMap.get(NO_HIT_PROBABILITIES));
		for(NISTPepSearchOutputColumnCode code : NISTPepSearchOutputColumnCode.values()) {
			JCheckBox chkBox = outputColumnsCheckboxMap.get(code);
			Boolean isSelected = outputColumnMap.get(code.name());
			if(chkBox != null && isSelected != null)
				chkBox.setSelected(isSelected);
		}
	}

	public void clearSearchOptionsPanel() {

		preSearchModeComboBox.setSelectedItem(null);
		searchTypeComboBox.setSelectedItem(null);
		searchOptionComboBox.setSelectedItem(null);
		hitRejectionComboBox.setSelectedItem(null);
		pepScoreTypeComboBox.setSelectedItem(null);
		chckbxReverseSearch.setSelected(false);
		chckbxAlternativePeakMatching.setSelected(false); 
		chckbxIgnorePeaksAroundPrecursor.setSelected(false);
		ignoreAroundPrecursorTextField.setText("");
		ignoreAroundPrecursorAccuracyComboBox.setSelectedItem(null);
		searchThresholdComboBox.setSelectedItem(null);
		precursorMzErrorTextField.setText("");
		precursorMzErrorTypeComboBox.setSelectedItem(null);
		fragmentMzErrorTextField.setText("");
		fragmentMzErrorTypeComboBox.setSelectedItem(null);	
		mzRangeStartTextField.setText("");
		mzRangeEndTextField.setText("");
		lossMwTextField.setText("");
		minIntensityTextField.setText("");
		chckbxMatchPolarity.setSelected(false);
		chckbxMatchCharge.setSelected(false);
		chckbxSetHighPriorityProgramExecution.setSelected(false);
		chckbxLoadLibrariesInMemory.setSelected(false);		
	}
	
	public void lockSearchOptionsPanelForEditing() {

		preSearchModeComboBox.setEnabled(false);
		searchTypeComboBox.setEnabled(false);
		searchOptionComboBox.setEnabled(false);
		hitRejectionComboBox.setEnabled(false);
		pepScoreTypeComboBox.setEnabled(false);
		chckbxReverseSearch.setEnabled(false);
		chckbxAlternativePeakMatching.setEnabled(false); 
		chckbxIgnorePeaksAroundPrecursor.setEnabled(false);
		ignoreAroundPrecursorTextField.setEnabled(false);
		ignoreAroundPrecursorAccuracyComboBox.setEnabled(false);
		searchThresholdComboBox.setEnabled(false);
		precursorMzErrorTextField.setEnabled(false);
		precursorMzErrorTypeComboBox.setEnabled(false);
		fragmentMzErrorTextField.setEnabled(false);
		fragmentMzErrorTypeComboBox.setEnabled(false);
		mzRangeStartTextField.setEnabled(false);
		mzRangeEndTextField.setEnabled(false);
		lossMwTextField.setEnabled(false);
		minIntensityTextField.setEnabled(false);
		chckbxMatchPolarity.setEnabled(false);
		chckbxMatchCharge.setEnabled(false);
		chckbxSetHighPriorityProgramExecution.setEnabled(false);
		chckbxLoadLibrariesInMemory.setEnabled(false);
	}

	/**
	 * @return the resultFile
	 */
	public File getResultFile() {
		return resultsFile;
	}

	/**
	 * @return the tmpInputFile
	 */
	public File getTmpInputFile() {
		return tmpInputFile;
	}

	public boolean getFeaturesFromDatabase() {
		return internalSource.isSelected();
	}
	
	public TableRowSubset getFeatureSubset() {
		return (TableRowSubset)featureSubsetComboBox.getSelectedItem();
	}

	public void runOffline() {

		searchButton.setText(MainActionCommands.NIST_MS_OFFLINE_PEPSEARCH_RUN_COMMAND.getName());
		searchButton.setActionCommand(MainActionCommands.NIST_MS_OFFLINE_PEPSEARCH_RUN_COMMAND.getName());
	}

	/**
	 * @return the pepSearchParameterObject
	 */
	public NISTPepSearchParameterObject getPepSearchParameterObject() {
		createPepSearchParameterObject();
		return pepSearchParameterObject;
	}
	
	public Collection<File>getEnabledLibraryFiles(){
		return libraryTable.getEnabledLibraryFiles();
	}

	public JPanel getSearchOptionsPanel() {
		return searchOptionsPanel;
	}

	public JPanel getInputAndLibraryPanel() {
		return inputAndLibraryPanel;
	}

	public JPanel getOutputOptionsPanel() {
		return outputOptionsPanel;
	}

	public JPanel getCommandPreviewPanel() {
		return commandPreviewPanel;
	}
}



























