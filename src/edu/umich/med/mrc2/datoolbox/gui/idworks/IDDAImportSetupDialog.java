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

package edu.umich.med.mrc2.datoolbox.gui.idworks;

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
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;

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
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class IDDAImportSetupDialog extends JDialog implements ActionListener, BackedByPreferences{

	/**
	 *
	 */
	private static final long serialVersionUID = 1085071927183807587L;
	private static final Icon iddaIcon = GuiUtils.getIcon("importIDDAdata", 32);
	private TaskListener taskListener;
	private Preferences preferences;
	private ImprovedFileChooser chooser;
	private File baseDirectory;
	private JCheckBox limitExtractionRtCheckBox;
	private JFormattedTextField rtFromTextField;
	private JFormattedTextField rtToTextField;
	private JFormattedTextField precursorRtAlignTextField;
	private JFormattedTextField precursorMzAlignTextField;
	private JComboBox mzAlignUnitsComboBox;
	private JButton importButton;
	private JButton cancelButton;
	private JCheckBox chckbxRemoveAllMassesAboveParent;
	private JCheckBox chckbxRemoveAllMassesBelowCounts;
	private JFormattedTextField minimalCountsTextField;
	private JCheckBox chckbxLeaveOnly;
	private JSpinner maxFragmentsSpinner;

	public static final String CURRENT_DIRECTORY = "CURRENT_DIRECTORY";
	public static final String USE_RT_RANGE = "USE_RT_RANGE";
	public static final String RT_BORDER_LEFT_MIN = "RT_BORDER_LEFT_MIN";
	public static final String RT_BORDER_RIGHT_MIN = "RT_BORDER_RIGHT_MIN";
	public static final String PRECURSOR_MASS_WINDOW = "PRECURSOR_MASS_WINDOW";
	public static final String PRECURSOR_MASS_WINDOW_UNITS = "PRECURSOR_MASS_WINDOW_UNITS";
	public static final String PRECURSOR_RT_WINDOW_MIN = "PRECURSOR_RT_WINDOW_MIN";
	public static final String REMOVE_MASSES_ABOVE_PARENT = "REMOVE_MASSES_ABOVE_PARENT";
	public static final String REMOVE_MASSES_BELOW_COUNT = "REMOVE_MASSES_BELOW_COUNT";
	public static final String MINIMAL_COUNTS = "MINIMAL_COUNTS";
	public static final String LEAVE_MAX_FRAGMENTS = "LEAVE_MAX_FRAGMENTS";
	public static final String MAX_FRAGMENTS_COUNT = "MAX_FRAGMENTS_COUNT";

	public IDDAImportSetupDialog(TaskListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "Delect IDDA experiment files and import settings");
		setPreferredSize(new Dimension(640, 700));
		setIconImage(((ImageIcon) iddaIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(640, 700));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.taskListener = listener;
		preferences = Preferences.userNodeForPackage(this.getClass());

		JSplitPane mainSplitPane = new JSplitPane();
		mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		mainSplitPane.setDividerSize(0);
		getContentPane().add(mainSplitPane, BorderLayout.CENTER);

		initChooser();
		mainSplitPane.setLeftComponent(chooser);

		JPanel panel0 = new JPanel();
		panel0.setBorder(new EmptyBorder(10, 10, 10, 10));
		mainSplitPane.setRightComponent(panel0);
		panel0.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "iDDA import settings", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel0.add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		limitExtractionRtCheckBox = new JCheckBox("Extract data only for retention time from ");
		GridBagConstraints gbc_limitExtractionRtCheckBox = new GridBagConstraints();
		gbc_limitExtractionRtCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_limitExtractionRtCheckBox.gridx = 0;
		gbc_limitExtractionRtCheckBox.gridy = 0;
		panel_1.add(limitExtractionRtCheckBox, gbc_limitExtractionRtCheckBox);

		rtFromTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtFromTextField.setColumns(10);
		GridBagConstraints gbc_rtFromTextField = new GridBagConstraints();
		gbc_rtFromTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtFromTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtFromTextField.gridx = 1;
		gbc_rtFromTextField.gridy = 0;
		panel_1.add(rtFromTextField, gbc_rtFromTextField);

		JLabel lblTo = new JLabel(" to ");
		GridBagConstraints gbc_lblTo = new GridBagConstraints();
		gbc_lblTo.insets = new Insets(0, 0, 5, 5);
		gbc_lblTo.anchor = GridBagConstraints.EAST;
		gbc_lblTo.gridx = 2;
		gbc_lblTo.gridy = 0;
		panel_1.add(lblTo, gbc_lblTo);

		rtToTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtToTextField.setColumns(10);
		GridBagConstraints gbc_rtToTextField = new GridBagConstraints();
		gbc_rtToTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rtToTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rtToTextField.gridx = 3;
		gbc_rtToTextField.gridy = 0;
		panel_1.add(rtToTextField, gbc_rtToTextField);

		JLabel lblMin = new JLabel("min");
		GridBagConstraints gbc_lblMin = new GridBagConstraints();
		gbc_lblMin.insets = new Insets(0, 0, 5, 0);
		gbc_lblMin.anchor = GridBagConstraints.WEST;
		gbc_lblMin.gridx = 4;
		gbc_lblMin.gridy = 0;
		panel_1.add(lblMin, gbc_lblMin);

		JLabel lblRtAlignmentWindow = new JLabel("Precursor RT alignment window");
		GridBagConstraints gbc_lblRtAlignmentWindow = new GridBagConstraints();
		gbc_lblRtAlignmentWindow.anchor = GridBagConstraints.EAST;
		gbc_lblRtAlignmentWindow.insets = new Insets(0, 0, 5, 5);
		gbc_lblRtAlignmentWindow.gridx = 0;
		gbc_lblRtAlignmentWindow.gridy = 1;
		panel_1.add(lblRtAlignmentWindow, gbc_lblRtAlignmentWindow);

		precursorRtAlignTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		precursorRtAlignTextField.setColumns(10);
		GridBagConstraints gbc_precursorRtAlignTextField = new GridBagConstraints();
		gbc_precursorRtAlignTextField.insets = new Insets(0, 0, 5, 5);
		gbc_precursorRtAlignTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_precursorRtAlignTextField.gridx = 1;
		gbc_precursorRtAlignTextField.gridy = 1;
		panel_1.add(precursorRtAlignTextField, gbc_precursorRtAlignTextField);

		JLabel lblMin_1 = new JLabel("min");
		GridBagConstraints gbc_lblMin_1 = new GridBagConstraints();
		gbc_lblMin_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblMin_1.gridx = 2;
		gbc_lblMin_1.gridy = 1;
		panel_1.add(lblMin_1, gbc_lblMin_1);

		JLabel lblPrecursorMzAlignment = new JLabel("Precursor m/z alignment window");
		GridBagConstraints gbc_lblPrecursorMzAlignment = new GridBagConstraints();
		gbc_lblPrecursorMzAlignment.anchor = GridBagConstraints.EAST;
		gbc_lblPrecursorMzAlignment.insets = new Insets(0, 0, 5, 5);
		gbc_lblPrecursorMzAlignment.gridx = 0;
		gbc_lblPrecursorMzAlignment.gridy = 2;
		panel_1.add(lblPrecursorMzAlignment, gbc_lblPrecursorMzAlignment);

		precursorMzAlignTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		precursorMzAlignTextField.setColumns(10);
		GridBagConstraints gbc_precursorMzAlignTextField = new GridBagConstraints();
		gbc_precursorMzAlignTextField.insets = new Insets(0, 0, 5, 5);
		gbc_precursorMzAlignTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_precursorMzAlignTextField.gridx = 1;
		gbc_precursorMzAlignTextField.gridy = 2;
		panel_1.add(precursorMzAlignTextField, gbc_precursorMzAlignTextField);

		mzAlignUnitsComboBox = new JComboBox(new DefaultComboBoxModel<MassErrorType>(MassErrorType.values()));
		GridBagConstraints gbc_mzAlignUnitsComboBox = new GridBagConstraints();
		gbc_mzAlignUnitsComboBox.gridwidth = 2;
		gbc_mzAlignUnitsComboBox.insets = new Insets(0, 0, 5, 5);
		gbc_mzAlignUnitsComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_mzAlignUnitsComboBox.gridx = 2;
		gbc_mzAlignUnitsComboBox.gridy = 2;
		panel_1.add(mzAlignUnitsComboBox, gbc_mzAlignUnitsComboBox);

		//		MSMS filtering
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "MSMS filtering", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.gridwidth = 5;
		gbc_panel_3.insets = new Insets(0, 0, 0, 5);
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 3;
		panel_1.add(panel, gbc_panel_3);

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 87, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		chckbxRemoveAllMassesAboveParent = new JCheckBox("Remove all masses above parent ion");
		GridBagConstraints gbc_chckbxRemoveAllMassesAboveParent = new GridBagConstraints();
		gbc_chckbxRemoveAllMassesAboveParent.anchor = GridBagConstraints.WEST;
		gbc_chckbxRemoveAllMassesAboveParent.gridwidth = 2;
		gbc_chckbxRemoveAllMassesAboveParent.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxRemoveAllMassesAboveParent.gridx = 0;
		gbc_chckbxRemoveAllMassesAboveParent.gridy = 0;
		panel.add(chckbxRemoveAllMassesAboveParent, gbc_chckbxRemoveAllMassesAboveParent);

		chckbxRemoveAllMassesBelowCounts = new JCheckBox("Remove all masses below ");
		GridBagConstraints gbc_chckbxRemoveAllMasses = new GridBagConstraints();
		gbc_chckbxRemoveAllMasses.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxRemoveAllMasses.anchor = GridBagConstraints.WEST;
		gbc_chckbxRemoveAllMasses.gridx = 0;
		gbc_chckbxRemoveAllMasses.gridy = 1;
		panel.add(chckbxRemoveAllMassesBelowCounts, gbc_chckbxRemoveAllMasses);

		minimalCountsTextField = new JFormattedTextField(new DecimalFormat("###.##"));
		minimalCountsTextField.setSize(new Dimension(80, 20));
		minimalCountsTextField.setPreferredSize(new Dimension(80, 20));
		minimalCountsTextField.setColumns(9);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.anchor = GridBagConstraints.WEST;
		gbc_formattedTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 1;
		panel.add(minimalCountsTextField, gbc_formattedTextField);

		JLabel lblCounts = new JLabel("counts");
		GridBagConstraints gbc_lblCounts = new GridBagConstraints();
		gbc_lblCounts.insets = new Insets(0, 0, 5, 0);
		gbc_lblCounts.anchor = GridBagConstraints.WEST;
		gbc_lblCounts.gridx = 2;
		gbc_lblCounts.gridy = 1;
		panel.add(lblCounts, gbc_lblCounts);

		chckbxLeaveOnly = new JCheckBox("Leave only");
		GridBagConstraints gbc_chckbxLeaveOnly = new GridBagConstraints();
		gbc_chckbxLeaveOnly.anchor = GridBagConstraints.EAST;
		gbc_chckbxLeaveOnly.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxLeaveOnly.gridx = 0;
		gbc_chckbxLeaveOnly.gridy = 2;
		panel.add(chckbxLeaveOnly, gbc_chckbxLeaveOnly);

		maxFragmentsSpinner = new JSpinner();
		maxFragmentsSpinner.setModel(new SpinnerNumberModel(20, 1, null, 1));
		maxFragmentsSpinner.setPreferredSize(new Dimension(80, 20));
		maxFragmentsSpinner.setSize(new Dimension(80, 20));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.anchor = GridBagConstraints.WEST;
		gbc_spinner.insets = new Insets(0, 0, 0, 5);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 2;
		panel.add(maxFragmentsSpinner, gbc_spinner);

		JLabel lblMajorFragments = new JLabel("major fragments");
		GridBagConstraints gbc_lblMajorFragments = new GridBagConstraints();
		gbc_lblMajorFragments.anchor = GridBagConstraints.WEST;
		gbc_lblMajorFragments.gridx = 2;
		gbc_lblMajorFragments.gridy = 2;
		panel.add(lblMajorFragments, gbc_lblMajorFragments);

		//	Buttons
		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		panel0.add(panel_2, BorderLayout.SOUTH);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		JRootPane rootPane = SwingUtilities.getRootPane(mainSplitPane);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(al);
		panel_2.add(cancelButton);

		importButton = new JButton("Import iDDA data");
		importButton.setActionCommand(MainActionCommands.IDDA_IMPORT_COMMAND.getName());
		importButton.addActionListener(this);
		panel_2.add(importButton);
		rootPane.setDefaultButton(importButton);

		loadPreferences();
		pack();
	}

	private void initChooser() {

		chooser = new ImprovedFileChooser();
		chooser.setDialogType(JFileChooser.OPEN_DIALOG);
		chooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		//	chooser.addActionListener(this);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(true);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setControlButtonsAreShown(false);
		chooser.getActionMap().get("viewTypeDetails").actionPerformed(null);

		baseDirectory = new File(MRC2ToolBoxConfiguration.getDefaultProjectsDirectory()).getAbsoluteFile();
		chooser.setCurrentDirectory(baseDirectory);

		FileNameExtensionFilter msFileFilter = new FileNameExtensionFilter("MS raw files", "mzml", "mzxml", "mzML", "mzXML");
		chooser.addChoosableFileFilter(msFileFilter);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getActionCommand().equals(MainActionCommands.IDDA_IMPORT_COMMAND.getName())) {

			savePreferences();
			chooser.approveSelection();
			File[] sourceRawFiles = chooser.getSelectedFiles();
			if(sourceRawFiles.length == 0) {
				MessageDialog.showErrorMsg("No files selected!", this);
				return;
			}
			Range dataExtractionRtRange = null;
			if(limitExtractionRtCheckBox.isSelected()) {

				double left = Double.valueOf(rtFromTextField.getText());
				double right = Double.valueOf(rtToTextField.getText());
				if(left >= right) {
					MessageDialog.showErrorMsg("RT range is invalid!", this);
					return;
				}
			}

//			IDDADataImportTask task = new IDDADataImportTask(
//					sourceRawFiles,
//					dataExtractionRtRange,
//					getPrecursorAlignmentRtWindow(),
//					getPrecursorAlignmentMzWindow(),
//					getPrecursorAlignmentMzErrorType(),
//					removeAllMassesAboveParent(),
//					getMsMsCountsCutoff(),
//					getMaxFragmentsCutoff());
//
//			task.addTaskListener(taskListener);
//			MRC2ToolBoxCore.getTaskController().addTask(task);
			setVisible(false);
		}
	}

	public double getPrecursorAlignmentRtWindow() {
		return Double.valueOf(precursorRtAlignTextField.getText());
	}

	public double getPrecursorAlignmentMzWindow() {
		return Double.valueOf(precursorMzAlignTextField.getText());
	}

	public MassErrorType getPrecursorAlignmentMzErrorType() {
		return (MassErrorType) mzAlignUnitsComboBox.getSelectedItem();
	}

	public boolean removeAllMassesAboveParent() {
		return chckbxRemoveAllMassesAboveParent.isSelected();
	}

	public double getMsMsCountsCutoff() {
		if(!chckbxRemoveAllMassesBelowCounts.isSelected())
			return 0.0d;
		else
			return Double.parseDouble(minimalCountsTextField.getText());
	}

	public int getMaxFragmentsCutoff() {

		if(!chckbxLeaveOnly.isSelected())
			return -1;
		else
			return (int) maxFragmentsSpinner.getValue();
	}

	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;
		baseDirectory = Paths.get(
				preferences.get(CURRENT_DIRECTORY,
				MRC2ToolBoxConfiguration.getDefaultProjectsDirectory())).toFile();
		limitExtractionRtCheckBox.setSelected(preferences.getBoolean(USE_RT_RANGE, false));
		rtFromTextField.setText(Double.toString(preferences.getDouble(RT_BORDER_LEFT_MIN, 0.0d)));
		rtToTextField.setText(Double.toString(preferences.getDouble(RT_BORDER_RIGHT_MIN, 0.0d)));
		precursorRtAlignTextField.setText(Double.toString(preferences.getDouble(PRECURSOR_RT_WINDOW_MIN, 0.1d)));
		precursorMzAlignTextField.setText(Double.toString(preferences.getDouble(PRECURSOR_MASS_WINDOW, 20.0d)));
		mzAlignUnitsComboBox.setSelectedItem(
				MassErrorType.getTypeByName(preferences.get(PRECURSOR_MASS_WINDOW_UNITS, MassErrorType.ppm.name())));

		chckbxRemoveAllMassesAboveParent.setSelected(preferences.getBoolean(REMOVE_MASSES_ABOVE_PARENT, true));
		chckbxRemoveAllMassesBelowCounts.setSelected(preferences.getBoolean(REMOVE_MASSES_BELOW_COUNT, false));
		minimalCountsTextField.setText(Double.toString(preferences.getDouble(MINIMAL_COUNTS, 0.0d)));
		chckbxLeaveOnly.setSelected(preferences.getBoolean(LEAVE_MAX_FRAGMENTS, false));
		maxFragmentsSpinner.setValue(preferences.getInt(MAX_FRAGMENTS_COUNT, 100000));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(CURRENT_DIRECTORY, baseDirectory.getAbsolutePath());
		preferences.putBoolean(USE_RT_RANGE, limitExtractionRtCheckBox.isSelected());
		preferences.putDouble(RT_BORDER_LEFT_MIN, Double.valueOf(rtFromTextField.getText()));
		preferences.putDouble(RT_BORDER_RIGHT_MIN, Double.valueOf(rtToTextField.getText()));
		preferences.putDouble(PRECURSOR_RT_WINDOW_MIN, Double.valueOf(precursorRtAlignTextField.getText()));
		preferences.putDouble(PRECURSOR_MASS_WINDOW, Double.valueOf(precursorMzAlignTextField.getText()));
		preferences.put(PRECURSOR_MASS_WINDOW_UNITS, ((MassErrorType)mzAlignUnitsComboBox.getSelectedItem()).name());
		preferences.putBoolean(REMOVE_MASSES_ABOVE_PARENT, chckbxRemoveAllMassesAboveParent.isSelected());
		preferences.putBoolean(REMOVE_MASSES_BELOW_COUNT, chckbxRemoveAllMassesBelowCounts.isSelected());
		preferences.putDouble(MINIMAL_COUNTS, Double.parseDouble(minimalCountsTextField.getText()));
		preferences.putBoolean(LEAVE_MAX_FRAGMENTS, chckbxLeaveOnly.isSelected());
		preferences.putInt(MAX_FRAGMENTS_COUNT, (int) maxFragmentsSpinner.getValue());
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}
}




















