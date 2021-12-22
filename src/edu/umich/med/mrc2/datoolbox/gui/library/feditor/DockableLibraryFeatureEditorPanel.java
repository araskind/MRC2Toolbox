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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.annotation.DockableObjectAnnotationPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class DockableLibraryFeatureEditorPanel extends DefaultSingleCDockable implements ActionListener, PersistentLayout{

	private LibraryMsFeature activeFeature;

	private JTextField featureNameTextField;
	private JLabel lblRetention;
	private JFormattedTextField retentionTextField;
	private FeatureEditorToolbar featureEditorToolbar;
	private JLabel lblIdConfidence;
	private JComboBox idConfidenceComboBox;
	private JLabel lblFrom;
	private JFormattedTextField leftRtWindowTextField;
	private JLabel lblTo;
	private JFormattedTextField rightRtWindowTextField;
	private JLabel lblMassErrror;
	private JFormattedTextField massErrorTextField;

	private DockableMsMsDataEditorPanel msMsPanel;
	private DockableObjectAnnotationPanel featureAnnotationPanel;
	private DockableMSOneDataEditorPanel adductsPanel;

	private CControl control;
	private CGrid grid;

	private static final Icon componentIcon = GuiUtils.getIcon("editLibrary", 16);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "LibraryFeatureEditorPanel.layout");

	public DockableLibraryFeatureEditorPanel(ActionListener listener) {

		super("DockableLibraryFeatureEditorPanel", componentIcon, "Edit library feature data", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));
		add(createHeader(listener),  BorderLayout.NORTH);

		adductsPanel = new DockableMSOneDataEditorPanel();
		msMsPanel = new DockableMsMsDataEditorPanel();
		featureAnnotationPanel = new DockableObjectAnnotationPanel(
				"DockableLibraryFeatureEditorPanelAnnotations", "Library feature annotations", 80);

		control = new CControl( MRC2ToolBoxCore.getMainWindow() );
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid( control );
		grid.add( 0, 0, 10, 100, adductsPanel, msMsPanel, featureAnnotationPanel);
		control.getContentArea().deploy( grid );

		add(control.getContentArea(), BorderLayout.CENTER);

		loadLayout(layoutConfigFile);
	}

	private JPanel createHeader(ActionListener listener) {

		JPanel header = new JPanel();
		GridBagLayout gbl_header = new GridBagLayout();
		gbl_header.columnWidths = new int[]{0, 0, 0};
		gbl_header.rowHeights = new int[]{0, 0};
		gbl_header.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_header.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		header.setLayout(gbl_header);

		featureEditorToolbar =  new FeatureEditorToolbar(listener);
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		header.add(featureEditorToolbar, gbc_lblNewLabel);

		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 0;
		header.add(createDataEditorPanel(), gbc_lblNewLabel_1);

		return header;
	}

	private JPanel createDataEditorPanel() {

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 239, 0, 0, 0, 0, 0, 35, 0, 0, 95, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel lblFeatureName = new JLabel("Name");
		lblFeatureName.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblFeatureName = new GridBagConstraints();
		gbc_lblFeatureName.insets = new Insets(0, 0, 0, 5);
		gbc_lblFeatureName.anchor = GridBagConstraints.EAST;
		gbc_lblFeatureName.gridx = 0;
		gbc_lblFeatureName.gridy = 0;
		panel.add(lblFeatureName, gbc_lblFeatureName);

		featureNameTextField = new JTextField();
		featureNameTextField.setMinimumSize(new Dimension(200, 26));
		featureNameTextField.setPreferredSize(new Dimension(400, 26));
		featureNameTextField.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.insets = new Insets(0, 0, 0, 5);
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		panel.add(featureNameTextField, gbc_textField);
		featureNameTextField.setColumns(10);

		lblRetention = new JLabel("Peak RT, min");
		lblRetention.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblRetention = new GridBagConstraints();
		gbc_lblRetention.insets = new Insets(0, 0, 0, 5);
		gbc_lblRetention.anchor = GridBagConstraints.EAST;
		gbc_lblRetention.gridx = 2;
		gbc_lblRetention.gridy = 0;
		panel.add(lblRetention, gbc_lblRetention);

		retentionTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		retentionTextField.setPreferredSize(new Dimension(50, 26));
		retentionTextField.setFont(new Font("Tahoma", Font.PLAIN, 12));
		retentionTextField.setColumns(6);
		GridBagConstraints gbc_fragVoltageTextField = new GridBagConstraints();
		gbc_fragVoltageTextField.insets = new Insets(0, 0, 0, 5);
		gbc_fragVoltageTextField.anchor = GridBagConstraints.WEST;
		gbc_fragVoltageTextField.gridx = 3;
		gbc_fragVoltageTextField.gridy = 0;
		panel.add(retentionTextField, gbc_fragVoltageTextField);

		lblFrom = new JLabel("from");
		lblFrom.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblFrom = new GridBagConstraints();
		gbc_lblFrom.anchor = GridBagConstraints.EAST;
		gbc_lblFrom.insets = new Insets(0, 0, 0, 5);
		gbc_lblFrom.gridx = 4;
		gbc_lblFrom.gridy = 0;
		panel.add(lblFrom, gbc_lblFrom);

		leftRtWindowTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		leftRtWindowTextField.setPreferredSize(new Dimension(50, 26));
		leftRtWindowTextField.setFont(new Font("Tahoma", Font.PLAIN, 12));
		leftRtWindowTextField.setColumns(6);
		GridBagConstraints gbc_minimalRtTextField = new GridBagConstraints();
		gbc_minimalRtTextField.insets = new Insets(0, 0, 0, 5);
		gbc_minimalRtTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_minimalRtTextField.gridx = 5;
		gbc_minimalRtTextField.gridy = 0;
		panel.add(leftRtWindowTextField, gbc_minimalRtTextField);

		lblTo = new JLabel("to");
		lblTo.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblTo = new GridBagConstraints();
		gbc_lblTo.anchor = GridBagConstraints.EAST;
		gbc_lblTo.insets = new Insets(0, 0, 0, 5);
		gbc_lblTo.gridx = 6;
		gbc_lblTo.gridy = 0;
		panel.add(lblTo, gbc_lblTo);

		rightRtWindowTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rightRtWindowTextField.setPreferredSize(new Dimension(50, 26));
		rightRtWindowTextField.setFont(new Font("Tahoma", Font.PLAIN, 12));
		rightRtWindowTextField.setColumns(6);
		GridBagConstraints gbc_maxRtTextField = new GridBagConstraints();
		gbc_maxRtTextField.insets = new Insets(0, 0, 0, 5);
		gbc_maxRtTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxRtTextField.gridx = 7;
		gbc_maxRtTextField.gridy = 0;
		panel.add(rightRtWindowTextField, gbc_maxRtTextField);

		lblMassErrror = new JLabel("Mass errror");
		lblMassErrror.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblMassErrror = new GridBagConstraints();
		gbc_lblMassErrror.anchor = GridBagConstraints.EAST;
		gbc_lblMassErrror.insets = new Insets(0, 0, 0, 5);
		gbc_lblMassErrror.gridx = 8;
		gbc_lblMassErrror.gridy = 0;
		panel.add(lblMassErrror, gbc_lblMassErrror);

		massErrorTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		massErrorTextField.setPreferredSize(new Dimension(50, 26));
		massErrorTextField.setFont(new Font("Tahoma", Font.PLAIN, 12));
		massErrorTextField.setColumns(6);
		GridBagConstraints gbc_massErrorTextField = new GridBagConstraints();
		gbc_massErrorTextField.insets = new Insets(0, 0, 0, 5);
		gbc_massErrorTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_massErrorTextField.gridx = 9;
		gbc_massErrorTextField.gridy = 0;
		panel.add(massErrorTextField, gbc_massErrorTextField);

		lblIdConfidence = new JLabel("ID confidence");
		lblIdConfidence.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblIdConfidence = new GridBagConstraints();
		gbc_lblIdConfidence.insets = new Insets(0, 0, 0, 5);
		gbc_lblIdConfidence.anchor = GridBagConstraints.EAST;
		gbc_lblIdConfidence.gridx = 10;
		gbc_lblIdConfidence.gridy = 0;
		panel.add(lblIdConfidence, gbc_lblIdConfidence);

		idConfidenceComboBox = new JComboBox<CompoundIdentificationConfidence>(
				new DefaultComboBoxModel<CompoundIdentificationConfidence>(CompoundIdentificationConfidence.values()));
		idConfidenceComboBox.setSize(new Dimension(120, 26));
		idConfidenceComboBox.setSelectedIndex(-1);
		idConfidenceComboBox.setFont(new Font("Tahoma", Font.PLAIN, 12));
		idConfidenceComboBox.setPreferredSize(new Dimension(120, 26));
		GridBagConstraints gbc_idConfidenceComboBox = new GridBagConstraints();
		gbc_idConfidenceComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_idConfidenceComboBox.gridx = 11;
		gbc_idConfidenceComboBox.gridy = 0;
		panel.add(idConfidenceComboBox, gbc_idConfidenceComboBox);

		return panel;
	}

	public void loadFeature(LibraryMsFeature feature, Polarity polarity) {

		clearPanel();
		activeFeature = feature;

		featureNameTextField.setText(activeFeature.getName());
		retentionTextField.setText(Double.toString(activeFeature.getRetentionTime()));
		idConfidenceComboBox.setSelectedItem(activeFeature.getPrimaryIdentity().getConfidenceLevel());

		//	TODO custom mass accuracy

		double leftBorder = activeFeature.getRetentionTime();
		double rightBorder = activeFeature.getRetentionTime();
		if(activeFeature.getRtRange() != null) {

			leftBorder = activeFeature.getRetentionTime() - activeFeature.getRtRange().getMin();
			rightBorder = activeFeature.getRtRange().getMax() - activeFeature.getRetentionTime();
		}
		leftRtWindowTextField.setText(MRC2ToolBoxConfiguration.getRtFormat().format(leftBorder));
		rightRtWindowTextField.setText(MRC2ToolBoxConfiguration.getRtFormat().format(rightBorder));

		adductsPanel.loadFeatureData(activeFeature, polarity);
		msMsPanel.loadFeatureData(activeFeature);
		featureAnnotationPanel.loadFeatureData(activeFeature);
	}

	public CompoundIdentificationConfidence getIdConfidence() {

		return (CompoundIdentificationConfidence) idConfidenceComboBox.getSelectedItem();
	}

	public double getRetentionTime() {

		if(retentionTextField.getText().isEmpty())
			return 0.0d;
		else
			return Double.parseDouble(retentionTextField.getText());
	}

	public String getFeatureName () {

		return featureNameTextField.getText().trim();
	}

	public Collection<Adduct>getAdducts(){

		return adductsPanel.getActiveAdducts();
	}

	public Range getRtRange() {

		double left = 0.0d;
		if(!leftRtWindowTextField.getText().isEmpty())
			left = Double.parseDouble(leftRtWindowTextField.getText());

		double right = 0.0d;
		if(!rightRtWindowTextField.getText().isEmpty())
			right = Double.parseDouble(rightRtWindowTextField.getText());

		double rt = getRetentionTime();

		if(rt == 0.0)
			return new Range(0.0);

		Range rtRange = new Range(rt - left, rt + right);
		if(rtRange.getMin() < 0.0)
			rtRange = new Range(0.0, rt + right);

		return rtRange;
	}

	public double getMassError() {

		if(massErrorTextField.getText().trim().isEmpty())
			return 0.0;
		else
			return Double.parseDouble(massErrorTextField.getText().trim());
	}

	public synchronized void clearPanel() {

		featureNameTextField.setText("");
		retentionTextField.setText("");
		leftRtWindowTextField.setText("");
		rightRtWindowTextField.setText("");
		massErrorTextField.setText("");
		adductsPanel.clearPanel();
		msMsPanel.clearPanel();
		featureAnnotationPanel.clearPanel();
		idConfidenceComboBox.setSelectedIndex(-1);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	public LibraryMsFeature getActiveFeature() {
		return activeFeature;
	}

	public void setDuplicateButtonStatus(boolean enabled) {

		featureEditorToolbar.setDuplicateButtonStatus(enabled);
	}

	@Override
	public void loadLayout(File layoutFile) {

		if(layoutConfigFile.exists()) {
			try {
				control.readXML(layoutFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				control.writeXML(layoutFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void saveLayout(File layoutFile) {

		if(control != null) {

			for(int i=0; i<control.getCDockableCount(); i++) {

				CDockable uiObject = control.getCDockable(i);
				if(uiObject instanceof PersistentLayout)
					((PersistentLayout)uiObject).saveLayout(((PersistentLayout)uiObject).getLayoutFile());
			}
			try {
				control.writeXML(layoutFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}
}










