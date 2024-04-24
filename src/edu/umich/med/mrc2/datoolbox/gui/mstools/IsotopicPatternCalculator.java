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

package edu.umich.med.mrc2.datoolbox.gui.mstools;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AbstractDocument;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.spectrum.DockableSpectumPlot;
import edu.umich.med.mrc2.datoolbox.gui.utils.ChemicalModificationSelector;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.UppercaseDocumentFilter;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class IsotopicPatternCalculator extends JPanel implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 1652220610725836L;

	private UppercaseDocumentFilter filter;
	private JTextField formulaValueField;
	private JCheckBox capsCheckBox;
	private JButton calculateButton;
	private DockableSpectumPlot msPlot;
	private JLabel lblSmiles;
	private JTextField smilesTextField;
	private ChemicalModificationSelector chModPanel;
	private JButton btnClearPanel;
	private DockableIsotopePatternTable ipTable;

	public IsotopicPatternCalculator(Frame parent) {

		setBorder(new EmptyBorder(5, 5, 5, 5));
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 135, 0, 172, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		setLayout(gbl_panel);

		JLabel label = new JLabel("  ");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 1;
		gbc_label.gridy = 0;
		add(label, gbc_label);

		JLabel formulaLabel = new JLabel("Formula:");
		GridBagConstraints gbc_lblFormula = new GridBagConstraints();
		gbc_lblFormula.anchor = GridBagConstraints.EAST;
		gbc_lblFormula.insets = new Insets(0, 0, 5, 5);
		gbc_lblFormula.gridx = 0;
		gbc_lblFormula.gridy = 1;
		add(formulaLabel, gbc_lblFormula);

		filter = new UppercaseDocumentFilter();

		formulaValueField = new JTextField();
		((AbstractDocument) formulaValueField.getDocument()).setDocumentFilter(filter);

		GridBagConstraints gbc_formulaTextField = new GridBagConstraints();
		gbc_formulaTextField.gridwidth = 3;
		gbc_formulaTextField.insets = new Insets(0, 0, 5, 5);
		gbc_formulaTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formulaTextField.gridx = 1;
		gbc_formulaTextField.gridy = 1;
		add(formulaValueField, gbc_formulaTextField);
		formulaValueField.setColumns(10);

		capsCheckBox = new JCheckBox(MainActionCommands.TOGGLE_CAPSLOCK.getName());
		capsCheckBox.setSelected(true);
		capsCheckBox.addActionListener(this);
		capsCheckBox.setActionCommand(MainActionCommands.TOGGLE_CAPSLOCK.getName());

		GridBagConstraints gbc_capsCheckBox = new GridBagConstraints();
		gbc_capsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_capsCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_capsCheckBox.gridx = 4;
		gbc_capsCheckBox.gridy = 1;
		add(capsCheckBox, gbc_capsCheckBox);

		lblSmiles = new JLabel("SMILES");
		GridBagConstraints gbc_lblSmiles = new GridBagConstraints();
		gbc_lblSmiles.anchor = GridBagConstraints.EAST;
		gbc_lblSmiles.insets = new Insets(0, 0, 5, 5);
		gbc_lblSmiles.gridx = 0;
		gbc_lblSmiles.gridy = 2;
		add(lblSmiles, gbc_lblSmiles);

		smilesTextField = new JTextField();
		GridBagConstraints gbc_smilesTextField = new GridBagConstraints();
		gbc_smilesTextField.gridwidth = 4;
		gbc_smilesTextField.insets = new Insets(0, 0, 5, 0);
		gbc_smilesTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_smilesTextField.gridx = 1;
		gbc_smilesTextField.gridy = 2;
		add(smilesTextField, gbc_smilesTextField);
		smilesTextField.setColumns(10);

		chModPanel = new ChemicalModificationSelector();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 4;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 3;
		add(chModPanel, gbc_panel);

		btnClearPanel = new JButton("Clear panel");
		btnClearPanel.setActionCommand(MainActionCommands.CLEAR_ISOTOPE_DISTRIBUTION_PANEL.getName());
		btnClearPanel.addActionListener(this);
		GridBagConstraints gbc_btnClearPanel = new GridBagConstraints();
		gbc_btnClearPanel.insets = new Insets(0, 0, 5, 5);
		gbc_btnClearPanel.gridx = 3;
		gbc_btnClearPanel.gridy = 4;
		add(btnClearPanel, gbc_btnClearPanel);

		calculateButton = new JButton("Calculate pattern");
		calculateButton.setActionCommand(MainActionCommands.CALCULATE_ISOTOPE_DISTRIBUTION.getName());
		calculateButton.addActionListener(this);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 4;
		gbc_btnNewButton.gridy = 4;
		add(calculateButton, gbc_btnNewButton);

		msPlot = new DockableSpectumPlot(
				"IsotopicPatternCalculatorDockableSpectumPlot", "Isotopic pattern plot");
		ipTable = new DockableIsotopePatternTable(
				"IsotopicPatternCalculatorDockableIsotopePatternTable", "Isotopic pattern table");

		CControl control = new CControl( MRC2ToolBoxCore.getMainWindow() );
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		CGrid grid = new CGrid( control );
		grid.add(0, 0, 100, 100, msPlot, ipTable);
		grid.select(0, 0, 100, 100, msPlot);
		control.getContentArea().deploy( grid );

//		DockController controller = new DockController();
//		controller.setRootWindow( parent );
//		controller.setTheme(new EclipseTheme());
//		final SplitDockStation station = new SplitDockStation();
//		controller.add( station );
//
//		SplitDockGrid grid = new SplitDockGrid();
//		grid.addDockable( 0, 0, 100, 100, msPlot, ipTable);
//		grid.setSelected(0, 0, 100, 100, msPlot);
//		station.dropTree( grid.toTree() );

		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridwidth = 5;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 5;
		add(control.getContentArea(), gbc_tabbedPane);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.CALCULATE_ISOTOPE_DISTRIBUTION.getName()))
			calculateIsotopeDistribution();

		if (command.equals(MainActionCommands.CLEAR_ISOTOPE_DISTRIBUTION_PANEL.getName()))
			clearPanel();

		if (command.equals(MainActionCommands.TOGGLE_CAPSLOCK.getName()))
			toggleCapslock();
	}

	private void clearPanel() {

		formulaValueField.setText("");
		smilesTextField.setText("");
		msPlot.removeAllDataSets();
		ipTable.clearTable();
	}

	private void calculateIsotopeDistribution() {

		if(!smilesTextField.getText().trim().isEmpty()) {
			calculateIsotopeDistributionFromSmiles();
		}
		else {
			if(!formulaValueField.getText().trim().isEmpty()) {
				calculateIsotopeDistributionFromFormula();
			}
		}
	}

	private void calculateIsotopeDistributionFromSmiles() {

		String smileString = smilesTextField.getText().trim();
		IAtomContainer mlab = null;
		try {
			SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
			mlab = sp.parseSmiles(smileString);
		}
		catch (InvalidSmilesException e) {
			MessageDialog.showWarningMsg("Invalid SMILES string!");
		}
		if(mlab != null) {

			Collection<MsPoint> msPoints = MsUtils.calculateIsotopeDistributionFromSmiles(
					smileString, chModPanel.getSelectedModification());
			ipTable.setTableModelFromMsPointCollection(msPoints);
			msPlot.showMsForPointCollection(msPoints, "Predicted spectrum");
		}
	}

	private void calculateIsotopeDistributionFromFormula() {

		String formulaString = formulaValueField.getText().trim();
		IMolecularFormula queryFormula = null;

		try {
			queryFormula = MolecularFormulaManipulator.getMolecularFormula(formulaString,
					DefaultChemObjectBuilder.getInstance());
		} catch (Exception e) {
			MessageDialog.showWarningMsg("Invalid formula");
		}
		if (queryFormula != null) {

			Collection<MsPoint> msPoints = MsUtils.calculateIsotopeDistribution(
					queryFormula, chModPanel.getSelectedModification(), true);
			ipTable.setTableModelFromMsPointCollection(msPoints);
			msPlot.showMsForPointCollection(msPoints, "Predicted spectrum");
		}
	}

	private void toggleCapslock() {

		AbstractDocument ad = (AbstractDocument) formulaValueField.getDocument();

		if (capsCheckBox.isSelected())
			ad.setDocumentFilter(filter);
		else
			ad.setDocumentFilter(null);
	}
}










