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

package edu.umich.med.mrc2.datoolbox.gui.plot;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;

import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class AxesSetupDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7535319726032588002L;
	private Axis xAxis;
	private ValueAxis yAxis;

	private Logger logger = Logger.getLogger(this.getClass().getName());

	// Buttons
	private JButton btnOK, btnApply, btnCancel;

	private JFormattedTextField fieldXMin;
	private JFormattedTextField fieldXMax;
	private JFormattedTextField fieldXTick;

	private JFormattedTextField fieldYMin;
	private JFormattedTextField fieldYMax;
	private JFormattedTextField fieldYTick;

	private JCheckBox checkXAutoRange, checkXAutoTick;
	private JCheckBox checkYAutoRange, checkYAutoTick;

	public AxesSetupDialog(CategoryPlot plot) {

	}

	/**
	 * Constructor
	 */
	public AxesSetupDialog(Plot plotIn) {

		super(MRC2ToolBoxCore.getMainWindow(), true);

		xAxis = null;
		yAxis = null;

		xAxis = ((XYPlot) plotIn).getDomainAxis();
		yAxis = ((XYPlot) plotIn).getRangeAxis();

		NumberFormat xAxisFormatter = NumberFormat.getNumberInstance();
		NumberFormat yAxisFormatter = NumberFormat.getNumberInstance();
		;

		if (xAxis instanceof NumberAxis)
			xAxisFormatter = ((NumberAxis) xAxis).getNumberFormatOverride();

		if (yAxis instanceof NumberAxis)
			yAxisFormatter = ((NumberAxis) yAxis).getNumberFormatOverride();

		// Create labels and fields
		JLabel lblXTitle = new JLabel(xAxis.getLabel());
		JLabel lblXAutoRange = new JLabel("Auto range");
		JLabel lblXMin = new JLabel("Minimum");
		JLabel lblXMax = new JLabel("Maximum");
		JLabel lblXAutoTick = new JLabel("Auto tick size");
		JLabel lblXTick = new JLabel("Tick size");

		JLabel lblYTitle = new JLabel(yAxis.getLabel());
		JLabel lblYAutoRange = new JLabel("Auto range");
		JLabel lblYMin = new JLabel("Minimum");
		JLabel lblYMax = new JLabel("Maximum");
		JLabel lblYAutoTick = new JLabel("Auto tick size");
		JLabel lblYTick = new JLabel("Tick size");

		checkXAutoRange = new JCheckBox();
		checkXAutoRange.addActionListener(this);
		checkXAutoTick = new JCheckBox();
		checkXAutoTick.addActionListener(this);
		fieldXMin = new JFormattedTextField(xAxisFormatter);
		fieldXMax = new JFormattedTextField(xAxisFormatter);
		fieldXTick = new JFormattedTextField(xAxisFormatter);

		checkYAutoRange = new JCheckBox();
		checkYAutoRange.addActionListener(this);
		checkYAutoTick = new JCheckBox();
		checkYAutoTick.addActionListener(this);
		fieldYMin = new JFormattedTextField(yAxisFormatter);
		fieldYMax = new JFormattedTextField(yAxisFormatter);
		fieldYTick = new JFormattedTextField(yAxisFormatter);

		// Create a panel for labels and fields
		JPanel pnlLabelsAndFields = new JPanel(new GridLayout(0, 2));

		pnlLabelsAndFields.add(lblXTitle);
		pnlLabelsAndFields.add(new JPanel());
		pnlLabelsAndFields.add(lblXAutoRange);
		pnlLabelsAndFields.add(checkXAutoRange);
		pnlLabelsAndFields.add(lblXMin);
		pnlLabelsAndFields.add(fieldXMin);
		pnlLabelsAndFields.add(lblXMax);
		pnlLabelsAndFields.add(fieldXMax);
		if (xAxis instanceof NumberAxis) {
			pnlLabelsAndFields.add(lblXAutoTick);
			pnlLabelsAndFields.add(checkXAutoTick);
			pnlLabelsAndFields.add(lblXTick);
			pnlLabelsAndFields.add(fieldXTick);
		}

		// Empty row
		pnlLabelsAndFields.add(new JPanel());
		pnlLabelsAndFields.add(new JPanel());

		pnlLabelsAndFields.add(lblYTitle);
		pnlLabelsAndFields.add(new JPanel());
		pnlLabelsAndFields.add(lblYAutoRange);
		pnlLabelsAndFields.add(checkYAutoRange);
		pnlLabelsAndFields.add(lblYMin);
		pnlLabelsAndFields.add(fieldYMin);
		pnlLabelsAndFields.add(lblYMax);
		pnlLabelsAndFields.add(fieldYMax);
		if (yAxis instanceof NumberAxis) {
			pnlLabelsAndFields.add(lblYAutoTick);
			pnlLabelsAndFields.add(checkYAutoTick);
			pnlLabelsAndFields.add(lblYTick);
			pnlLabelsAndFields.add(fieldYTick);
		}

		// Create buttons
		JPanel pnlButtons = new JPanel();
		btnOK = new JButton("OK");
		btnOK.addActionListener(this);
		btnApply = new JButton("Apply");
		btnApply.addActionListener(this);
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(this);

		pnlButtons.add(btnOK);
		pnlButtons.add(btnApply);
		pnlButtons.add(btnCancel);

		// Put everything into a main panel
		JPanel pnlAll = new JPanel(new BorderLayout());
		pnlAll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		add(pnlAll);

		pnlAll.add(pnlLabelsAndFields, BorderLayout.CENTER);
		pnlAll.add(pnlButtons, BorderLayout.SOUTH);

		pack();

		setTitle("Please set ranges for axes");
		setResizable(false);
		setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());

		getValuesToControls();
	}

	/**
	 * Implementation for ActionListener interface
	 */
	public void actionPerformed(ActionEvent ae) {

		Object src = ae.getSource();

		if (src == btnOK) {
			if (setValuesToPlot()) {
				dispose();
			}
		}

		if (src == btnApply) {
			if (setValuesToPlot())
				getValuesToControls();
		}

		if (src == btnCancel) {
			dispose();
		}
		if ((src == checkXAutoRange) || (src == checkYAutoRange) | (src == checkXAutoTick) || (src == checkYAutoTick))
			updateAutoRangeAvailability();

	}

	private void displayMessage(String msg) {
		try {
			logger.info(msg);
			JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
		} catch (Exception exce) {
		}
	}

	private void getValuesToControls() {

		if (xAxis instanceof NumberAxis) {

			checkXAutoRange.setSelected(((ValueAxis) xAxis).isAutoRange());
			fieldXMin.setValue(((ValueAxis) xAxis).getRange().getLowerBound());
			fieldXMax.setValue(((ValueAxis) xAxis).getRange().getUpperBound());
			checkXAutoTick.setSelected(((ValueAxis) xAxis).isAutoTickUnitSelection());
			fieldXTick.setValue(((NumberAxis) xAxis).getTickUnit().getSize());
		}
		checkYAutoRange.setSelected(yAxis.isAutoRange());
		fieldYMin.setValue(yAxis.getRange().getLowerBound());
		fieldYMax.setValue(yAxis.getRange().getUpperBound());
		if (yAxis instanceof NumberAxis) {
			checkYAutoTick.setSelected(yAxis.isAutoTickUnitSelection());
			fieldYTick.setValue(((NumberAxis) yAxis).getTickUnit().getSize());
		}

		updateAutoRangeAvailability();
	}

	private boolean setValuesToPlot() {

		if (checkXAutoRange.isSelected()) {

			if (xAxis instanceof NumberAxis)
				((ValueAxis) xAxis).setAutoRange(true);
		} else {

			double lower = ((Number) fieldXMin.getValue()).doubleValue();
			double upper = ((Number) fieldXMax.getValue()).doubleValue();
			if (lower > upper) {
				displayMessage("Invalid " + xAxis.getLabel() + " range.");
				return false;
			}
			if (xAxis instanceof NumberAxis)
				((ValueAxis) xAxis).setRange(lower, upper);
		}
		if (xAxis instanceof NumberAxis) {

			if (checkXAutoTick.isSelected() && xAxis instanceof NumberAxis) {
				((ValueAxis) xAxis).setAutoTickUnitSelection(true);

			} else {
				double tickSize = ((Number) fieldXTick.getValue()).doubleValue();
				((NumberAxis) xAxis).setTickUnit(new NumberTickUnit(tickSize));
			}
		}
		if (checkYAutoRange.isSelected()) {

			yAxis.setAutoRange(true);

		} else {

			double lower = ((Number) fieldYMin.getValue()).doubleValue();
			double upper = ((Number) fieldYMax.getValue()).doubleValue();
			if (lower > upper) {
				displayMessage("Invalid " + yAxis.getLabel() + " range.");
				return false;
			}
			yAxis.setRange(lower, upper);
		}

		if (yAxis instanceof NumberAxis) {

			if (checkYAutoTick.isSelected()) {
				yAxis.setAutoTickUnitSelection(true);

			} else {
				double tickSize = ((Number) fieldYTick.getValue()).doubleValue();
				((NumberAxis) yAxis).setTickUnit(new NumberTickUnit(tickSize));
			}

		}

		return true;
	}

	private void updateAutoRangeAvailability() {
		if (checkXAutoRange.isSelected()) {
			fieldXMax.setEnabled(false);
			fieldXMin.setEnabled(false);
		} else {
			fieldXMax.setEnabled(true);
			fieldXMin.setEnabled(true);
		}

		if (checkXAutoTick.isSelected()) {
			fieldXTick.setEnabled(false);
		} else {
			fieldXTick.setEnabled(true);
		}

		if (checkYAutoRange.isSelected()) {
			fieldYMax.setEnabled(false);
			fieldYMin.setEnabled(false);
		} else {
			fieldYMax.setEnabled(true);
			fieldYMin.setEnabled(true);
		}

		if (checkYAutoTick.isSelected()) {
			fieldYTick.setEnabled(false);
		} else {
			fieldYTick.setEnabled(true);
		}

	}
}
