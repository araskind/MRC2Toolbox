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

package edu.umich.med.mrc2.datoolbox.gui.library.feditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DecimalFormat;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.MSMSImportTask;

public class MsMsChooserFilterDialog extends JDialog implements ActionListener, TaskListener, BackedByPreferences {

	/**
	 *
	 */
	private static final long serialVersionUID = -8398380251982220161L;
	private JCheckBox chckbxRemoveAllMassesAboveParent;
	private JCheckBox chckbxRemoveAllMassesBelowCounts;
	private JFormattedTextField minimalCountsTextField;
	private JCheckBox chckbxLeaveOnly;
	private JSpinner maxFragmentsSpinner;
	private JButton btnCancel;
	private JButton btnFilter;

	private Preferences preferences;
	public static final String REMOVE_MASSES_ABOVE_PARENT = "REMOVE_MASSES_ABOVE_PARENT";
	public static final String REMOVE_MASSES_BELOW_COUNT = "REMOVE_MASSES_BELOW_COUNT";
	public static final String MINIMAL_COUNTS = "MINIMAL_COUNTS";
	public static final String LEAVE_MAX_FRAGMENTS = "LEAVE_MAX_FRAGMENTS";
	public static final String MAX_FRAGMENTS_COUNT = "MAX_FRAGMENTS_COUNT";
	private JFileChooser fileChooser;
	private File baseDirectory;
	private FileNameExtensionFilter xmlFilter;
	private FileNameExtensionFilter mspFilter;
	private DockableMsMsDataEditorPanel parent;

	private static final Icon filterMsMsIcon = GuiUtils.getIcon("filterMsMs", 32);

	private LibraryMsFeature activeFeature;

	public MsMsChooserFilterDialog(DockableMsMsDataEditorPanel parent) {

		super(MRC2ToolBoxCore.getMainWindow(), "Load MSMS data from file", true);
		this.parent = parent;

		setIconImage(((ImageIcon) filterMsMsIcon).getImage());
		setSize(new Dimension(600, 600));
		setPreferredSize(new Dimension(600, 600));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		baseDirectory = new File(MRC2ToolBoxConfiguration.getDefaultProjectsDirectory()).getAbsoluteFile();
		initFileChooser();
		getContentPane().add(fileChooser, BorderLayout.NORTH);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 87, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
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
		gbc_chckbxLeaveOnly.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxLeaveOnly.gridx = 0;
		gbc_chckbxLeaveOnly.gridy = 2;
		panel.add(chckbxLeaveOnly, gbc_chckbxLeaveOnly);

		maxFragmentsSpinner = new JSpinner();
		maxFragmentsSpinner.setModel(new SpinnerNumberModel(20, 1, 1000, 1));
		maxFragmentsSpinner.setPreferredSize(new Dimension(80, 20));
		maxFragmentsSpinner.setSize(new Dimension(80, 20));
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.anchor = GridBagConstraints.WEST;
		gbc_spinner.insets = new Insets(0, 0, 5, 5);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 2;
		panel.add(maxFragmentsSpinner, gbc_spinner);

		JLabel lblMajorFragments = new JLabel("major fragments");
		GridBagConstraints gbc_lblMajorFragments = new GridBagConstraints();
		gbc_lblMajorFragments.insets = new Insets(0, 0, 5, 0);
		gbc_lblMajorFragments.anchor = GridBagConstraints.WEST;
		gbc_lblMajorFragments.gridx = 2;
		gbc_lblMajorFragments.gridy = 2;
		panel.add(lblMajorFragments, gbc_lblMajorFragments);

		JLabel label = new JLabel(" ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 3;
		panel.add(label, gbc_label);

		btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.WEST;
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 4;
		panel.add(btnCancel, gbc_btnCancel);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		btnCancel.addActionListener(al);

		btnFilter = new JButton(MainActionCommands.IMPORT_FILTERED_MSMS_DATA_COMMAND.getName());
		btnFilter.setActionCommand(MainActionCommands.IMPORT_FILTERED_MSMS_DATA_COMMAND.getName());
		btnFilter.addActionListener(this);
		GridBagConstraints gbc_btnFilter = new GridBagConstraints();
		gbc_btnFilter.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnFilter.gridx = 2;
		gbc_btnFilter.gridy = 4;
		panel.add(btnFilter, gbc_btnFilter);

		JRootPane rootPane = SwingUtilities.getRootPane(btnFilter);
		rootPane.setDefaultButton(btnFilter);

		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		loadPreferences();
		pack();
		setVisible(false);
	}

	private void initFileChooser() {

		fileChooser = new ImprovedFileChooser();
		fileChooser.setBorder(new EmptyBorder(10, 10, 10, 10));
		fileChooser.addActionListener(this);
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setMultiSelectionEnabled(false);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setControlButtonsAreShown(false);
		fileChooser.getActionMap().get("viewTypeDetails").actionPerformed(null);

		baseDirectory = new File(MRC2ToolBoxConfiguration.getDefaultProjectsDirectory()).getAbsoluteFile();
		fileChooser.setCurrentDirectory(baseDirectory);

		xmlFilter = new FileNameExtensionFilter("Agilent XML MSMS export files", "xml", "XML");
		fileChooser.addChoosableFileFilter(xmlFilter);
		mspFilter = new FileNameExtensionFilter("NIST MSP MSMS files", "msp", "MSP");
		fileChooser.addChoosableFileFilter(mspFilter);
	}

	public int getMaxFragmentsCount() {

		if(chckbxLeaveOnly.isSelected())
			return (int) maxFragmentsSpinner.getValue();
		else
			return 0;
	}

	public boolean removeMassesAboveParent() {

		return chckbxRemoveAllMassesAboveParent.isSelected();
	}

	public double getMinimalIntensityCutoff() {

		if(chckbxLeaveOnly.isSelected())
			return Double.parseDouble(minimalCountsTextField.getText());
		else
			return 0.0;
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		// TODO Auto-generated method stub
		preferences = prefs;

		boolean removeAllMassesAboveParent = preferences.getBoolean(REMOVE_MASSES_ABOVE_PARENT, true);
		chckbxRemoveAllMassesAboveParent.setSelected(removeAllMassesAboveParent);

		boolean removeAllMassesBelowCounts = preferences.getBoolean(REMOVE_MASSES_BELOW_COUNT, false);
		chckbxRemoveAllMassesBelowCounts.setSelected(removeAllMassesBelowCounts);

		double minCounts = preferences.getDouble(MINIMAL_COUNTS, 10.0d);
		minimalCountsTextField.setText(Double.toString(minCounts));

		boolean leaveOnly = preferences.getBoolean(LEAVE_MAX_FRAGMENTS, false);
		chckbxLeaveOnly.setSelected(leaveOnly);

		int maxFragments = preferences.getInt(MAX_FRAGMENTS_COUNT, 1000);
		maxFragmentsSpinner.setValue(maxFragments);
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.putBoolean(REMOVE_MASSES_ABOVE_PARENT, chckbxRemoveAllMassesAboveParent.isSelected());
		preferences.putBoolean(REMOVE_MASSES_BELOW_COUNT, chckbxRemoveAllMassesBelowCounts.isSelected());
		preferences.putDouble(MINIMAL_COUNTS, Double.parseDouble(minimalCountsTextField.getText()));
		preferences.putBoolean(LEAVE_MAX_FRAGMENTS, chckbxLeaveOnly.isSelected());
		preferences.putInt(MAX_FRAGMENTS_COUNT, (int) maxFragmentsSpinner.getValue());
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.IMPORT_FILTERED_MSMS_DATA_COMMAND.getName())) {

			fileChooser.approveSelection();
			baseDirectory = fileChooser.getCurrentDirectory();
			File msmsFile = fileChooser.getSelectedFile();

			MSMSImportTask mit = new MSMSImportTask(
					activeFeature,
					msmsFile,
					chckbxRemoveAllMassesAboveParent.isSelected(),
					chckbxRemoveAllMassesBelowCounts.isSelected(),
					Double.parseDouble(minimalCountsTextField.getText()),
					chckbxLeaveOnly.isSelected(),
					(int)maxFragmentsSpinner.getValue());

			mit.addTaskListener(this);
			MRC2ToolBoxCore.getTaskController().addTask(mit);
		}
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(MSMSImportTask.class)) {

				MSMSImportTask mit = (MSMSImportTask) e.getSource();
				mit.removeTaskListener(this);

				if(mit.getActiveMsMs() != null)
					parent.loadMsMsData(mit.getActiveMsMs());

				this.setVisible(false);
			}
		}
	}

	/**
	 * @return the activeFeature
	 */
	public LibraryMsFeature getActiveFeature() {
		return activeFeature;
	}

	/**
	 * @param activeFeature the activeFeature to set
	 */
	public void setActiveFeature(LibraryMsFeature activeFeature) {
		this.activeFeature = activeFeature;
	}
}


































