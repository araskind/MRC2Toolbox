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

package edu.umich.med.mrc2.datoolbox.gui.preferences;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.automator.MethodFilter;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.fc.ImprovedFileChooser;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class DockableTemplatesPanel extends DefaultSingleCDockable implements BackedByPreferences, ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -2252017167616094542L;

	private static final Icon componentIcon = GuiUtils.getIcon("open", 16);

	private Preferences prefs;

	private JButton 
		defaultDataDirBrowseButton,
		defaultExperimentDirBrowseButton,
		lib2nistBrowseButton,
		percolatorBrowseButton,
		methodDirBrowseButton,
		msConvertBrowseButton,
		nistMsBrowseButton,
		nistPepSearchBrowseButton,
		qualAutomationBrowseButton,
		rBinaryBrowseButton,
		rJavaBrowseButton,
		rawDataRepositoryBrowseButton,
		siriusBrowseButton,
		xicTemplateBrowseButton;

	private JTextField 
		defaultDataDirField,
		defaultExperimentDirField,
		dirForMethodsTextField,
		lib2nistTextField,
		percolatorTextField,
		msConvertTextField,
		nistMsTextField,
		nistPepSearchTextField,
		qualAutomationTextField,
		rBinaryTextField,
		rJavaTextField,
		rawDataRepositoryField,
		siriusBinaryField,
		xicTemplateTextField;
	
	private JFormattedTextField xicAccuracyTextField;
	
	private FileNameExtensionFilter exeFilter;
	private MethodFilter methodFileFilter;

	public DockableTemplatesPanel() {

		super("DockableTemplatesPanel", componentIcon, "External programs/templates", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		GridBagLayout gbl_templatesPanel = new GridBagLayout();
		gbl_templatesPanel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_templatesPanel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gbl_templatesPanel);

		int rowCount = 0;

		//	Default project directory
		JLabel lblDefaultExperimentDirectory = new JLabel("Default experiment directory");
		GridBagConstraints gbc_lblDefaultExperimentDirectory = new GridBagConstraints();
		gbc_lblDefaultExperimentDirectory.anchor = GridBagConstraints.EAST;
		gbc_lblDefaultExperimentDirectory.insets = new Insets(0, 0, 5, 5);
		gbc_lblDefaultExperimentDirectory.gridx = 0;
		gbc_lblDefaultExperimentDirectory.gridy = rowCount;
		add(lblDefaultExperimentDirectory, gbc_lblDefaultExperimentDirectory);

		defaultExperimentDirField = new JTextField();
		defaultExperimentDirField.setEditable(false);
		GridBagConstraints gbc_defaultExperimentDirField = new GridBagConstraints();
		gbc_defaultExperimentDirField.insets = new Insets(0, 0, 5, 5);
		gbc_defaultExperimentDirField.fill = GridBagConstraints.HORIZONTAL;
		gbc_defaultExperimentDirField.gridx = 1;
		gbc_defaultExperimentDirField.gridy = rowCount;
		add(defaultExperimentDirField, gbc_defaultExperimentDirField);
		defaultExperimentDirField.setColumns(10);

		defaultExperimentDirBrowseButton = new JButton("Browse");
		defaultExperimentDirBrowseButton.setActionCommand(MainActionCommands.DEFAULT_DIR_BROWSE_COMMAND.getName());
		defaultExperimentDirBrowseButton.addActionListener(this);
		GridBagConstraints gbc_defaultExperimentDirBrowseButton = new GridBagConstraints();
		gbc_defaultExperimentDirBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_defaultExperimentDirBrowseButton.gridx = 2;
		gbc_defaultExperimentDirBrowseButton.gridy = rowCount;
		add(defaultExperimentDirBrowseButton, gbc_defaultExperimentDirBrowseButton);

		rowCount++;
		
		//	Default data directory
		JLabel lblDefaultDataDirectory = new JLabel("Default data directory");
		GridBagConstraints gbc_lblDefaultDataDirectory = new GridBagConstraints();
		gbc_lblDefaultDataDirectory.anchor = GridBagConstraints.EAST;
		gbc_lblDefaultDataDirectory.insets = new Insets(0, 0, 5, 5);
		gbc_lblDefaultDataDirectory.gridx = 0;
		gbc_lblDefaultDataDirectory.gridy = rowCount;
		add(lblDefaultDataDirectory, gbc_lblDefaultDataDirectory);

		defaultDataDirField = new JTextField();
		defaultDataDirField.setEditable(false);
		GridBagConstraints gbc_defaultDataDirField = new GridBagConstraints();
		gbc_defaultDataDirField.insets = new Insets(0, 0, 5, 5);
		gbc_defaultDataDirField.fill = GridBagConstraints.HORIZONTAL;
		gbc_defaultDataDirField.gridx = 1;
		gbc_defaultDataDirField.gridy = rowCount;
		add(defaultDataDirField, gbc_defaultDataDirField);
		defaultDataDirField.setColumns(10);

		defaultDataDirBrowseButton = new JButton("Browse");
		defaultDataDirBrowseButton.setActionCommand(
				MainActionCommands.DEFAULT_DATA_DIR_BROWSE_COMMAND.getName());
		defaultDataDirBrowseButton.addActionListener(this);
		GridBagConstraints gbc_defaultDataDirBrowseButton = new GridBagConstraints();
		gbc_defaultDataDirBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_defaultDataDirBrowseButton.gridx = 2;
		gbc_defaultDataDirBrowseButton.gridy = rowCount;
		add(defaultDataDirBrowseButton, gbc_defaultDataDirBrowseButton);

		rowCount++;

		//	Raw data repository
		JLabel rawRepositoryDirectory = new JLabel("Raw data repository");
		GridBagConstraints gbc_lblRawRepositoryDirectory = new GridBagConstraints();
		gbc_lblRawRepositoryDirectory.anchor = GridBagConstraints.EAST;
		gbc_lblRawRepositoryDirectory.insets = new Insets(0, 0, 5, 5);
		gbc_lblRawRepositoryDirectory.gridx = 0;
		gbc_lblRawRepositoryDirectory.gridy = rowCount;
		add(rawRepositoryDirectory, gbc_lblRawRepositoryDirectory);

		rawDataRepositoryField = new JTextField();
		rawDataRepositoryField.setEditable(false);
		GridBagConstraints gbc_rawDataRepositoryField = new GridBagConstraints();
		gbc_rawDataRepositoryField.insets = new Insets(0, 0, 5, 5);
		gbc_rawDataRepositoryField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rawDataRepositoryField.gridx = 1;
		gbc_rawDataRepositoryField.gridy = rowCount;
		add(rawDataRepositoryField, gbc_rawDataRepositoryField);
		rawDataRepositoryField.setColumns(10);

		rawDataRepositoryBrowseButton = new JButton("Browse");
		rawDataRepositoryBrowseButton.setActionCommand(
				MainActionCommands.RAW_DATA_REPOSITORY_DIR_BROWSE_COMMAND.getName());
		rawDataRepositoryBrowseButton.addActionListener(this);
		GridBagConstraints gbc_rawDataRepositoryBrowseButton = new GridBagConstraints();
		gbc_rawDataRepositoryBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_rawDataRepositoryBrowseButton.gridx = 2;
		gbc_rawDataRepositoryBrowseButton.gridy = rowCount;
		add(rawDataRepositoryBrowseButton, gbc_rawDataRepositoryBrowseButton);
		
		rowCount++;

		//	Agilent Qual automation binary
		JLabel lblAgilentQualAutomation = new JLabel("Agilent Qual automation binary");
		GridBagConstraints gbc_lblAgilentQualAutomation = new GridBagConstraints();
		gbc_lblAgilentQualAutomation.insets = new Insets(0, 0, 5, 5);
		gbc_lblAgilentQualAutomation.anchor = GridBagConstraints.EAST;
		gbc_lblAgilentQualAutomation.gridx = 0;
		gbc_lblAgilentQualAutomation.gridy = rowCount;
		add(lblAgilentQualAutomation, gbc_lblAgilentQualAutomation);

		qualAutomationTextField = new JTextField();
		qualAutomationTextField.setEditable(false);
		GridBagConstraints gbc_qualAutomationTextField = new GridBagConstraints();
		gbc_qualAutomationTextField.insets = new Insets(0, 0, 5, 5);
		gbc_qualAutomationTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_qualAutomationTextField.gridx = 1;
		gbc_qualAutomationTextField.gridy = rowCount;
		add(qualAutomationTextField, gbc_qualAutomationTextField);
		qualAutomationTextField.setColumns(10);

		qualAutomationBrowseButton = new JButton("Browse");
		qualAutomationBrowseButton.setActionCommand(MainActionCommands.QUAL_AUTOMATION_BROWSE_COMMAND.getName());
		qualAutomationBrowseButton.addActionListener(this);
		GridBagConstraints gbc_qualAutomationBrowseButton = new GridBagConstraints();
		gbc_qualAutomationBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_qualAutomationBrowseButton.gridx = 2;
		gbc_qualAutomationBrowseButton.gridy = rowCount;
		add(qualAutomationBrowseButton, gbc_qualAutomationBrowseButton);

		rowCount++;

		//	XIC template method
		JLabel lblXicTemplateMethod = new JLabel("XIC template method");
		GridBagConstraints gbc_lblXicTemplateMethod = new GridBagConstraints();
		gbc_lblXicTemplateMethod.anchor = GridBagConstraints.EAST;
		gbc_lblXicTemplateMethod.insets = new Insets(0, 0, 5, 5);
		gbc_lblXicTemplateMethod.gridx = 0;
		gbc_lblXicTemplateMethod.gridy = rowCount;
		add(lblXicTemplateMethod, gbc_lblXicTemplateMethod);

		xicTemplateTextField = new JTextField();
		xicTemplateTextField.setEditable(false);
		xicTemplateTextField.setColumns(10);
		GridBagConstraints gbc_xicTemplateTextField = new GridBagConstraints();
		gbc_xicTemplateTextField.insets = new Insets(0, 0, 5, 5);
		gbc_xicTemplateTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_xicTemplateTextField.gridx = 1;
		gbc_xicTemplateTextField.gridy = rowCount;
		add(xicTemplateTextField, gbc_xicTemplateTextField);

		xicTemplateBrowseButton = new JButton("Browse");
		xicTemplateBrowseButton.setActionCommand(MainActionCommands.XIC_TEMPLATE_BROWSE_COMMAND.getName());
		xicTemplateBrowseButton.addActionListener(this);
		GridBagConstraints gbc_xicTeblateBrowseButton = new GridBagConstraints();
		gbc_xicTeblateBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_xicTeblateBrowseButton.gridx = 2;
		gbc_xicTeblateBrowseButton.gridy = rowCount;
		add(xicTemplateBrowseButton, gbc_xicTeblateBrowseButton);

		rowCount++;

		//	Directory for generated methods
		JLabel lblNewLabel_1 = new JLabel("Directory for generated methods");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = rowCount;
		add(lblNewLabel_1, gbc_lblNewLabel_1);

		dirForMethodsTextField = new JTextField();
		dirForMethodsTextField.setEditable(false);
		GridBagConstraints gbc_dirForMethodsTextField = new GridBagConstraints();
		gbc_dirForMethodsTextField.insets = new Insets(0, 0, 5, 5);
		gbc_dirForMethodsTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_dirForMethodsTextField.gridx = 1;
		gbc_dirForMethodsTextField.gridy = rowCount;
		add(dirForMethodsTextField, gbc_dirForMethodsTextField);
		dirForMethodsTextField.setColumns(10);

		methodDirBrowseButton = new JButton("Browse");
		methodDirBrowseButton.setActionCommand(MainActionCommands.METHOD_DIR_BROWSE_COMMAND.getName());
		methodDirBrowseButton.addActionListener(this);
		GridBagConstraints gbc_methodDirBrowseButton = new GridBagConstraints();
		gbc_methodDirBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_methodDirBrowseButton.gridx = 2;
		gbc_methodDirBrowseButton.gridy = rowCount;
		add(methodDirBrowseButton, gbc_methodDirBrowseButton);

		rowCount++;

		//	Mass accuracy for XIC, ppm
		JLabel lblNewLabel_2 = new JLabel("Mass accuracy for XIC, ppm");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = rowCount;
		add(lblNewLabel_2, gbc_lblNewLabel_2);

		xicAccuracyTextField = new JFormattedTextField(new DecimalFormat("#.0"));
		xicAccuracyTextField.setColumns(10);
		GridBagConstraints gbc_xicAccuracyTextField = new GridBagConstraints();
		gbc_xicAccuracyTextField.anchor = GridBagConstraints.WEST;
		gbc_xicAccuracyTextField.insets = new Insets(0, 0, 5, 5);
		gbc_xicAccuracyTextField.gridx = 1;
		gbc_xicAccuracyTextField.gridy = rowCount;
		add(xicAccuracyTextField, gbc_xicAccuracyTextField);

		rowCount++;

		//	R binary
		JLabel lblRBinary = new JLabel("R binary");
		GridBagConstraints gbc_lblRBinary = new GridBagConstraints();
		gbc_lblRBinary.anchor = GridBagConstraints.EAST;
		gbc_lblRBinary.insets = new Insets(0, 0, 5, 5);
		gbc_lblRBinary.gridx = 0;
		gbc_lblRBinary.gridy = rowCount;
		add(lblRBinary, gbc_lblRBinary);

		rBinaryTextField = new JTextField();
		rBinaryTextField.setEditable(false);
		GridBagConstraints gbc_rBinaryTextField = new GridBagConstraints();
		gbc_rBinaryTextField.insets = new Insets(0, 0, 5, 5);
		gbc_rBinaryTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rBinaryTextField.gridx = 1;
		gbc_rBinaryTextField.gridy = rowCount;
		add(rBinaryTextField, gbc_rBinaryTextField);
		rBinaryTextField.setColumns(10);

		rBinaryBrowseButton = new JButton("Browse");
		rBinaryBrowseButton.setActionCommand(MainActionCommands.R_BINARY_BROWSE_COMMAND.getName());
		rBinaryBrowseButton.addActionListener(this);
		GridBagConstraints gbc_rBinaryBrowseButton = new GridBagConstraints();
		gbc_rBinaryBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_rBinaryBrowseButton.gridx = 2;
		gbc_rBinaryBrowseButton.gridy = rowCount;
		add(rBinaryBrowseButton, gbc_rBinaryBrowseButton);

		rowCount++;

		//	RJava
		JLabel lblRjavaDirectory = new JLabel("RJava directory");
		GridBagConstraints gbc_lblRjavaDirectory = new GridBagConstraints();
		gbc_lblRjavaDirectory.anchor = GridBagConstraints.EAST;
		gbc_lblRjavaDirectory.insets = new Insets(0, 0, 0, 5);
		gbc_lblRjavaDirectory.gridx = 0;
		gbc_lblRjavaDirectory.gridy = rowCount;
		add(lblRjavaDirectory, gbc_lblRjavaDirectory);

		rJavaTextField = new JTextField();
		rJavaTextField.setEditable(false);
		GridBagConstraints gbc_rJavaTextField = new GridBagConstraints();
		gbc_rJavaTextField.insets = new Insets(0, 0, 0, 5);
		gbc_rJavaTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_rJavaTextField.gridx = 1;
		gbc_rJavaTextField.gridy = rowCount;
		add(rJavaTextField, gbc_rJavaTextField);
		rJavaTextField.setColumns(10);

		rJavaBrowseButton = new JButton("Browse");
		rJavaBrowseButton.setActionCommand(MainActionCommands.R_JAVA_BROWSE_COMMAND.getName());
		rJavaBrowseButton.addActionListener(this);
		GridBagConstraints gbc_rJavaBrowseButton = new GridBagConstraints();
		gbc_rJavaBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_rJavaBrowseButton.gridx = 2;
		gbc_rJavaBrowseButton.gridy = rowCount;
		add(rJavaBrowseButton, gbc_rJavaBrowseButton);

		rowCount++;

		//	NIST MS
		JLabel lblNISTMSDirectory = new JLabel("NIST MS directory");
		GridBagConstraints gbc_lblNISTMSDirectory = new GridBagConstraints();
		gbc_lblNISTMSDirectory.anchor = GridBagConstraints.EAST;
		gbc_lblNISTMSDirectory.insets = new Insets(0, 0, 0, 5);
		gbc_lblNISTMSDirectory.gridx = 0;
		gbc_lblNISTMSDirectory.gridy = rowCount;
		add(lblNISTMSDirectory, gbc_lblNISTMSDirectory);

		nistMsTextField = new JTextField();
		nistMsTextField.setEditable(false);
		GridBagConstraints gbc_nistMsTextField = new GridBagConstraints();
		gbc_nistMsTextField.insets = new Insets(0, 0, 0, 5);
		gbc_nistMsTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_nistMsTextField.gridx = 1;
		gbc_nistMsTextField.gridy = rowCount;
		add(nistMsTextField, gbc_nistMsTextField);
		nistMsTextField.setColumns(10);

		nistMsBrowseButton = new JButton("Browse");
		nistMsBrowseButton.setActionCommand(MainActionCommands.NIST_MS_BROWSE_COMMAND.getName());
		nistMsBrowseButton.addActionListener(this);
		GridBagConstraints gbc_nistMsBrowseButton = new GridBagConstraints();
		gbc_nistMsBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_nistMsBrowseButton.gridx = 2;
		gbc_nistMsBrowseButton.gridy = rowCount;
		add(nistMsBrowseButton, gbc_nistMsBrowseButton);

		rowCount++;

		//	NIST PepSearchMS
		JLabel lblNISTPepSearchDirectory = new JLabel("NIST MSPepSearch directory");
		GridBagConstraints gbc_lblNISTPepSearchDirectory = new GridBagConstraints();
		gbc_lblNISTPepSearchDirectory.anchor = GridBagConstraints.EAST;
		gbc_lblNISTPepSearchDirectory.insets = new Insets(0, 0, 0, 5);
		gbc_lblNISTPepSearchDirectory.gridx = 0;
		gbc_lblNISTPepSearchDirectory.gridy = rowCount;
		add(lblNISTPepSearchDirectory, gbc_lblNISTPepSearchDirectory);

		nistPepSearchTextField = new JTextField();
		nistPepSearchTextField.setEditable(false);
		GridBagConstraints gbc_NISTPepSearchTextField = new GridBagConstraints();
		gbc_NISTPepSearchTextField.insets = new Insets(0, 0, 0, 5);
		gbc_NISTPepSearchTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_NISTPepSearchTextField.gridx = 1;
		gbc_NISTPepSearchTextField.gridy = rowCount;
		add(nistPepSearchTextField, gbc_NISTPepSearchTextField);
		nistPepSearchTextField.setColumns(10);

		nistPepSearchBrowseButton = new JButton("Browse");
		nistPepSearchBrowseButton.setActionCommand(MainActionCommands.NIST_PEPSEARCH_BROWSE_COMMAND.getName());
		nistPepSearchBrowseButton.addActionListener(this);
		GridBagConstraints gbc_NISTPepSearchBrowseButton = new GridBagConstraints();
		gbc_NISTPepSearchBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_NISTPepSearchBrowseButton.gridx = 2;
		gbc_NISTPepSearchBrowseButton.gridy = rowCount;
		add(nistPepSearchBrowseButton, gbc_NISTPepSearchBrowseButton);

		rowCount++;
		
		//	msconvert
		JLabel lblMsConvert = new JLabel("msconvert binary");
		GridBagConstraints gbc_lblMsConvert = new GridBagConstraints();
		gbc_lblMsConvert.insets = new Insets(0, 0, 5, 5);
		gbc_lblMsConvert.anchor = GridBagConstraints.EAST;
		gbc_lblMsConvert.gridx = 0;
		gbc_lblMsConvert.gridy = rowCount;
		add(lblMsConvert, gbc_lblMsConvert);

		msConvertTextField = new JTextField();
		msConvertTextField.setEditable(false);
		GridBagConstraints gbc_msConvertTextField = new GridBagConstraints();
		gbc_msConvertTextField.insets = new Insets(0, 0, 5, 5);
		gbc_msConvertTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_msConvertTextField.gridx = 1;
		gbc_msConvertTextField.gridy = rowCount;
		add(msConvertTextField, gbc_msConvertTextField);
		msConvertTextField.setColumns(10);

		msConvertBrowseButton = new JButton("Browse");
		msConvertBrowseButton.setActionCommand(MainActionCommands.MS_CONVERT_BROWSE_COMMAND.getName());
		msConvertBrowseButton.addActionListener(this);
		GridBagConstraints gbc_msConvertBrowseButton = new GridBagConstraints();
		gbc_msConvertBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_msConvertBrowseButton.gridx = 2;
		gbc_msConvertBrowseButton.gridy = rowCount;
		add(msConvertBrowseButton, gbc_msConvertBrowseButton);
		
		rowCount++;
		
		//	Sirius
		JLabel lblSirius = new JLabel("Sirius CLI binary");
		GridBagConstraints gbc_lblSirius = new GridBagConstraints();
		gbc_lblSirius.insets = new Insets(0, 0, 5, 5);
		gbc_lblSirius.anchor = GridBagConstraints.EAST;
		gbc_lblSirius.gridx = 0;
		gbc_lblSirius.gridy = rowCount;
		add(lblSirius, gbc_lblSirius);

		siriusBinaryField = new JTextField();
		siriusBinaryField.setEditable(false);
		GridBagConstraints gbc_siriusBinaryField = new GridBagConstraints();
		gbc_siriusBinaryField.insets = new Insets(0, 0, 5, 5);
		gbc_siriusBinaryField.fill = GridBagConstraints.HORIZONTAL;
		gbc_siriusBinaryField.gridx = 1;
		gbc_siriusBinaryField.gridy = rowCount;
		add(siriusBinaryField, gbc_siriusBinaryField);
		siriusBinaryField.setColumns(10);

		siriusBrowseButton = new JButton("Browse");
		siriusBrowseButton.setActionCommand(MainActionCommands.SIRIUS_BROWSE_COMMAND.getName());
		siriusBrowseButton.addActionListener(this);
		GridBagConstraints gbc_siriusBrowseButton = new GridBagConstraints();
		gbc_siriusBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_siriusBrowseButton.gridx = 2;
		gbc_siriusBrowseButton.gridy = rowCount;
		add(siriusBrowseButton, gbc_siriusBrowseButton);
		
		rowCount++;
		
		//	lib2nist
		JLabel lblLib2Nist = new JLabel("lib2nist binary");
		GridBagConstraints gbc_lblLib2Nist = new GridBagConstraints();
		gbc_lblLib2Nist.insets = new Insets(0, 0, 5, 5);
		gbc_lblLib2Nist.anchor = GridBagConstraints.EAST;
		gbc_lblLib2Nist.gridx = 0;
		gbc_lblLib2Nist.gridy = rowCount;
		add(lblLib2Nist, gbc_lblLib2Nist);

		lib2nistTextField = new JTextField();
		lib2nistTextField.setEditable(false);
		GridBagConstraints gbc_lib2nistTextField = new GridBagConstraints();
		gbc_lib2nistTextField.insets = new Insets(0, 0, 5, 5);
		gbc_lib2nistTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_lib2nistTextField.gridx = 1;
		gbc_lib2nistTextField.gridy = rowCount;
		add(lib2nistTextField, gbc_lib2nistTextField);
		lib2nistTextField.setColumns(10);

		lib2nistBrowseButton = new JButton("Browse");
		lib2nistBrowseButton.setActionCommand(MainActionCommands.LIB2NIST_BROWSE_COMMAND.getName());
		lib2nistBrowseButton.addActionListener(this);
		GridBagConstraints gbc_lib2nistBrowseButton = new GridBagConstraints();
		gbc_lib2nistBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_lib2nistBrowseButton.gridx = 2;
		gbc_lib2nistBrowseButton.gridy = rowCount;
		add(lib2nistBrowseButton, gbc_lib2nistBrowseButton);
		
		rowCount++;

		//	Percolator
		JLabel lblLibPercolator = new JLabel("Percolator binary");
		GridBagConstraints gbc_lblPercolator = new GridBagConstraints();
		gbc_lblPercolator.insets = new Insets(0, 0, 5, 5);
		gbc_lblPercolator.anchor = GridBagConstraints.EAST;
		gbc_lblPercolator.gridx = 0;
		gbc_lblPercolator.gridy = rowCount;
		add(lblLibPercolator, gbc_lblPercolator);

		percolatorTextField = new JTextField();
		percolatorTextField.setEditable(false);
		GridBagConstraints gbc_percolatorTextField = new GridBagConstraints();
		gbc_percolatorTextField.insets = new Insets(0, 0, 5, 5);
		gbc_percolatorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_percolatorTextField.gridx = 1;
		gbc_percolatorTextField.gridy = rowCount;
		add(percolatorTextField, gbc_percolatorTextField);
		percolatorTextField.setColumns(10);

		percolatorBrowseButton = new JButton("Browse");
		percolatorBrowseButton.setActionCommand(MainActionCommands.PERCOLATOR_BROWSE_COMMAND.getName());
		percolatorBrowseButton.addActionListener(this);
		GridBagConstraints gbc_percolatorBrowseButton = new GridBagConstraints();
		gbc_percolatorBrowseButton.insets = new Insets(0, 0, 5, 0);
		gbc_percolatorBrowseButton.gridx = 2;
		gbc_percolatorBrowseButton.gridy = rowCount;
		add(percolatorBrowseButton, gbc_percolatorBrowseButton);
		
		rowCount++;
		
		rowCount++;

		int[] rh = new int[rowCount];
		Arrays.fill(rh, 0);
		gbl_templatesPanel.rowHeights = rh;

		double[] rw = new double[rowCount];
		Arrays.fill(rw, 0.0d);
		gbl_templatesPanel.rowWeights = rw;
		
		exeFilter = new FileNameExtensionFilter("Executable files", "exe", "EXE");

	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
		selectFileOrDirectory(command);
		
//		if (command.equals(MainActionCommands.QUAL_AUTOMATION_BROWSE_COMMAND.getName()))
//			selectFileOrDirectory(command);
//		
//		if (command.equals(MainActionCommands.MS_CONVERT_BROWSE_COMMAND.getName()))
//			selectFileOrDirectory(command);
//
//		if (command.equals(MainActionCommands.DEFAULT_DIR_BROWSE_COMMAND.getName()))
//			selectFileOrDirectory(command);
//		
//		if (command.equals(MainActionCommands.RAW_DATA_REPOSITORY_DIR_BROWSE_COMMAND.getName()))
//			selectFileOrDirectory(command);
//
//		if (command.equals(MainActionCommands.DEFAULT_DATA_DIR_BROWSE_COMMAND.getName()))
//			selectFileOrDirectory(command);
//
//		if (command.equals(MainActionCommands.XIC_TEMPLATE_BROWSE_COMMAND.getName()))
//			selectFileOrDirectory(command);
//
//		if (command.equals(MainActionCommands.METHOD_DIR_BROWSE_COMMAND.getName()))
//			selectFileOrDirectory(command);
//
//		if (command.equals(MainActionCommands.R_BINARY_BROWSE_COMMAND.getName()))
//			selectFileOrDirectory(command);
//
//		if (command.equals(MainActionCommands.R_JAVA_BROWSE_COMMAND.getName()))
//			selectFileOrDirectory(command);
//
//		if (command.equals(MainActionCommands.NIST_MS_BROWSE_COMMAND.getName()))
//			selectFileOrDirectory(command);
//
//		if (command.equals(MainActionCommands.NIST_PEPSEARCH_BROWSE_COMMAND.getName()))
//			selectFileOrDirectory(command);
//		
//		if (command.equals(MainActionCommands.SIRIUS_BROWSE_COMMAND.getName()))
//			selectFileOrDirectory(command);
//		
//		if (command.equals(MainActionCommands.LIB2NIST_BROWSE_COMMAND.getName()))
//			selectFileOrDirectory(command);
//		
//		if (command.equals(MainActionCommands.PERCOLATOR_BROWSE_COMMAND.getName()))
//			selectFileOrDirectory(command);		
	}

	private void selectFileOrDirectory(String command) {

		JFileChooser chooser = new ImprovedFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setMultiSelectionEnabled(false);
		chooser.setPreferredSize(new Dimension(800, 640));
		File inputFile = null;
		String title = "";

		if (command.equals(MainActionCommands.QUAL_AUTOMATION_BROWSE_COMMAND.getName())) {

			title = "Select QualAutomation binary";
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setFileFilter(exeFilter);
		}
		if (command.equals(MainActionCommands.MS_CONVERT_BROWSE_COMMAND.getName())) {

			title = "Select msconvert binary";
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setFileFilter(exeFilter);
		}	
		if (command.equals(MainActionCommands.XIC_TEMPLATE_BROWSE_COMMAND.getName())) {

			title = "Select XIC template method file:";
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setCurrentDirectory(new File(MRC2ToolBoxCore.qualMethodsDir));
			chooser.setFileFilter(methodFileFilter);
		}
		if (command.equals(MainActionCommands.METHOD_DIR_BROWSE_COMMAND.getName())) {

			title = "Select directory for generated methods:";
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setCurrentDirectory(new File(MRC2ToolBoxCore.qualMethodsDir));
		}
		if (command.equals(MainActionCommands.DEFAULT_DIR_BROWSE_COMMAND.getName())) {

			title = "Select default experiment directory:";
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setCurrentDirectory(new File(MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory()));
		}
		if (command.equals(MainActionCommands.RAW_DATA_REPOSITORY_DIR_BROWSE_COMMAND.getName())) {

			title = "Select raw data repository:";
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setCurrentDirectory(new File(MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory()));
		}	
		if (command.equals(MainActionCommands.DEFAULT_DATA_DIR_BROWSE_COMMAND.getName())) {

			title = "Select default data directory:";
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setCurrentDirectory(new File(MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
		}
		if (command.equals(MainActionCommands.NIST_MS_BROWSE_COMMAND.getName())) {

			title = "Select NIST MSSEARCH directory location";
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		if (command.equals(MainActionCommands.NIST_PEPSEARCH_BROWSE_COMMAND.getName())){

			title = "Select NIST MS PepSearch binary";
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setFileFilter(exeFilter);
		}
		if (command.equals(MainActionCommands.SIRIUS_BROWSE_COMMAND.getName())){

			title = "Select Sirius CLI binary";
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setFileFilter(exeFilter);
		}
		if (command.equals(MainActionCommands.LIB2NIST_BROWSE_COMMAND.getName())){

			title = "Select lib2nist binary";
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setFileFilter(exeFilter);
		}
		if (command.equals(MainActionCommands.PERCOLATOR_BROWSE_COMMAND.getName())){

			title = "Select Percolator binary";
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setFileFilter(exeFilter);
		}
		chooser.setDialogTitle(title);

		if (chooser.showOpenDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {

			inputFile = chooser.getSelectedFile();

			if (inputFile.exists()) {

				if (command.equals(MainActionCommands.QUAL_AUTOMATION_BROWSE_COMMAND.getName()))
					qualAutomationTextField.setText(inputFile.getAbsolutePath());
				
				if (command.equals(MainActionCommands.MS_CONVERT_BROWSE_COMMAND.getName()))
					msConvertTextField.setText(inputFile.getAbsolutePath());

				if (command.equals(MainActionCommands.XIC_TEMPLATE_BROWSE_COMMAND.getName()))
					xicTemplateTextField.setText(inputFile.getAbsolutePath());

				if (command.equals(MainActionCommands.METHOD_DIR_BROWSE_COMMAND.getName()))
					dirForMethodsTextField.setText(inputFile.getAbsolutePath());

				if (command.equals(MainActionCommands.R_BINARY_BROWSE_COMMAND.getName()))
					rBinaryTextField.setText(inputFile.getAbsolutePath());

				if (command.equals(MainActionCommands.R_JAVA_BROWSE_COMMAND.getName()))
					rJavaTextField.setText(inputFile.getAbsolutePath());

				if (command.equals(MainActionCommands.DEFAULT_DIR_BROWSE_COMMAND.getName()))
					defaultExperimentDirField.setText(inputFile.getAbsolutePath());
				
				if (command.equals(MainActionCommands.RAW_DATA_REPOSITORY_DIR_BROWSE_COMMAND.getName()))
					rawDataRepositoryField.setText(inputFile.getAbsolutePath());
				
				if (command.equals(MainActionCommands.DEFAULT_DATA_DIR_BROWSE_COMMAND.getName()))
					defaultDataDirField.setText(inputFile.getAbsolutePath());

				if (command.equals(MainActionCommands.NIST_MS_BROWSE_COMMAND.getName())) {

					Collection<File> searchExec = FileUtils.listFiles(inputFile, new WildcardFileFilter("nistms*.exe"), null);
					if(!searchExec.isEmpty())
						nistMsTextField.setText(inputFile.getAbsolutePath());
					else {
						MessageDialog.showErrorMsg("NIST MSSEARCH program not found in the directory!", this.getContentPane());
						return;
					}
				}
				if (command.equals(MainActionCommands.NIST_PEPSEARCH_BROWSE_COMMAND.getName())) {
					nistPepSearchTextField.setText(inputFile.getAbsolutePath());
				}
				if (command.equals(MainActionCommands.SIRIUS_BROWSE_COMMAND.getName())) {
					siriusBinaryField.setText(inputFile.getAbsolutePath());
				}
				if (command.equals(MainActionCommands.LIB2NIST_BROWSE_COMMAND.getName())) {
					lib2nistTextField.setText(inputFile.getAbsolutePath());
				}
				if (command.equals(MainActionCommands.PERCOLATOR_BROWSE_COMMAND.getName())) {
					percolatorTextField.setText(inputFile.getAbsolutePath());
				}				
			}
		}
	}

	@Override
	public void loadPreferences(Preferences preferences) {

		prefs = preferences;

		defaultExperimentDirField.setText(MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory());
		defaultDataDirField.setText(MRC2ToolBoxConfiguration.getDefaultDataDirectory());
		rawDataRepositoryField.setText(MRC2ToolBoxConfiguration.getRawDataRepository());
		qualAutomationTextField.setText(MRC2ToolBoxConfiguration.getQualAutomationExecutableFile());
		msConvertTextField.setText(MRC2ToolBoxConfiguration.getMsConvertExecutableFile());
		xicTemplateTextField.setText(MRC2ToolBoxConfiguration.getXicTemplateFile());
		dirForMethodsTextField.setText(MRC2ToolBoxConfiguration.getQualXicMethodDir());
		xicAccuracyTextField.setText(Double.toString(MRC2ToolBoxConfiguration.getXicMassAcuracy()));
		rBinaryTextField.setText(MRC2ToolBoxConfiguration.getrBinaryPath());
		rJavaTextField.setText(MRC2ToolBoxConfiguration.getrJavaDir());
		nistMsTextField.setText(MRC2ToolBoxConfiguration.getrNistMsDir());
		nistPepSearchTextField.setText(MRC2ToolBoxConfiguration.getNISTPepSearchExecutableFile());	
		siriusBinaryField.setText(MRC2ToolBoxConfiguration.getSiriusBinaryPath());
		lib2nistTextField.setText(MRC2ToolBoxConfiguration.getLib2NistBinaryPath());
		percolatorTextField.setText(MRC2ToolBoxConfiguration.getPercolatorBinaryPath());
	}

	@Override
	public void savePreferences() {

		if(!defaultExperimentDirField.getText().isEmpty()) {

			File defaultExperimentDir = new File(defaultExperimentDirField.getText());
			if(defaultExperimentDir.exists())
					MRC2ToolBoxConfiguration.setDefaultExperimentsDirectory(defaultExperimentDir.getAbsolutePath());
			else {
				MessageDialog.showErrorMsg("Invalid default experiment directory!", this.getContentPane());
				return;
			}
		}
		if(!defaultDataDirField.getText().isEmpty()) {

			File defaultDataDir = new File(defaultDataDirField.getText());
			if(defaultDataDir.exists())
					MRC2ToolBoxConfiguration.setDefaultdataDirectory(defaultDataDir.getAbsolutePath());
			else {
				MessageDialog.showErrorMsg("Invalid default data directory!", this.getContentPane());
				return;
			}
		}
		if(!rawDataRepositoryField.getText().isEmpty()) {

			File rawDataRepositoryDir = new File(rawDataRepositoryField.getText());
			if(rawDataRepositoryDir.exists())
					MRC2ToolBoxConfiguration.setRawDataRepository(rawDataRepositoryDir.getAbsolutePath());
			else {
				MessageDialog.showErrorMsg("Invalid raw data repository directory!", this.getContentPane());
				return;
			}
		}
		if(!qualAutomationTextField.getText().isEmpty()) {

			File qualAutomation = new File(qualAutomationTextField.getText());
			if(qualAutomation.exists())
					MRC2ToolBoxConfiguration.setQualAutomationExecutableFile(qualAutomation.getAbsolutePath());
			else {
				MessageDialog.showErrorMsg("Invalid QualAutomation executable file!", this.getContentPane());
				return;
			}
		}
		if(!msConvertTextField.getText().isEmpty()) {

			File msConvert = new File(msConvertTextField.getText());
			if(msConvert.exists())
					MRC2ToolBoxConfiguration.setMsConvertExecutableFile(msConvert.getAbsolutePath());
			else {
				MessageDialog.showErrorMsg("Invalid msconvert executable file!", this.getContentPane());
				return;
			}
		}
		if(!xicTemplateTextField.getText().isEmpty()) {

			File xicTemplate = new File(xicTemplateTextField.getText());
			if(xicTemplate.exists())
					MRC2ToolBoxConfiguration.setQualTemplate(xicTemplate.getAbsolutePath());
			else {
				MessageDialog.showErrorMsg("Invalid XIC template file!", this.getContentPane());
				return;
			}
		}
		if(!dirForMethodsTextField.getText().isEmpty()) {

			File xicMethodDir = new File(dirForMethodsTextField.getText());
			if(xicMethodDir.exists())
					MRC2ToolBoxConfiguration.setQualXicMethodDir(xicMethodDir.getAbsolutePath());
			else {
				MessageDialog.showErrorMsg("Invalid XIC method directory!", this.getContentPane());
				return;
			}
		}
		double xicAccuracy = Double.parseDouble(xicAccuracyTextField.getText());
		MRC2ToolBoxConfiguration.setXicMassAcuracy(xicAccuracy);

		if(!rBinaryTextField.getText().isEmpty()) {

			File rBinaryFile = new File(rBinaryTextField.getText());
			if(rBinaryFile.exists())
					MRC2ToolBoxConfiguration.setrBinaryPath(rBinaryFile.getAbsolutePath());
			else {
				MessageDialog.showErrorMsg("Invalid R binary file!", this.getContentPane());
				return;
			}
		}
		if(!rJavaTextField.getText().isEmpty()) {

			File rJavaDir = new File(rJavaTextField.getText());
			if(rJavaDir.exists())
					MRC2ToolBoxConfiguration.setrJavaDir(rJavaDir.getAbsolutePath());
			else {
				MessageDialog.showErrorMsg("Invalid RJava directory!", this.getContentPane());
				return;
			}
		}
		if(!nistMsTextField.getText().isEmpty()) {

			File nistMsDir = new File(nistMsTextField.getText());
			if(nistMsDir.exists()) {

				Collection<File> searchExec = FileUtils.listFiles(nistMsDir, new WildcardFileFilter("nistms*.exe"), null);

				if(!searchExec.isEmpty())
					MRC2ToolBoxConfiguration.setNistMsDir(nistMsDir.getAbsolutePath());
				else {
					MessageDialog.showErrorMsg("NIST MSSEARCH program not found in the directory!", this.getContentPane());
					return;
				}
			}
			else {
				MessageDialog.showErrorMsg("Invalid NIST MSSEARCH directory!", this.getContentPane());
				return;
			}
		}
		if(!nistPepSearchTextField.getText().isEmpty()) {

			File nistPepSearch = new File(nistPepSearchTextField.getText());
			if(nistPepSearch.exists())
					MRC2ToolBoxConfiguration.setNISTPepSearchExecutableFile(nistPepSearch.getAbsolutePath());
			else {
				MessageDialog.showErrorMsg("Invalid NIST PepSearch executable file!", this.getContentPane());
				return;
			}
		}
		if(!siriusBinaryField.getText().isEmpty()) {

			File sirius = new File(siriusBinaryField.getText());
			if(sirius.exists())
					MRC2ToolBoxConfiguration.setSiriusBinaryPath(sirius.getAbsolutePath());
			else {
				MessageDialog.showErrorMsg("Invalid Sirius CLI executable file!", this.getContentPane());
				return;
			}
		}
		if(!lib2nistTextField.getText().isEmpty()) {

			File lib2nist = new File(lib2nistTextField.getText());
			if(lib2nist.exists())
					MRC2ToolBoxConfiguration.setLib2NistBinaryPath(lib2nist.getAbsolutePath());
			else {
				MessageDialog.showErrorMsg("Invalid lib2nist executable file!", this.getContentPane());
				return;
			}
		}
		if(!percolatorTextField.getText().isEmpty()) {

			File percolator = new File(percolatorTextField.getText());
			if(percolator.exists())
					MRC2ToolBoxConfiguration.setPercolatorBinaryPath(percolator.getAbsolutePath());
			else {
				MessageDialog.showErrorMsg("Invalid Percolator executable file!", this.getContentPane());
				return;
			}
		}
	}

	@Override
	public void loadPreferences() {

		if(prefs != null)
			loadPreferences(prefs);
	}
}















