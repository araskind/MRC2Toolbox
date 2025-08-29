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

package edu.umich.med.mrc2.datoolbox.gui.fdata.corr;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.CorrelationPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.CorrelationPlotToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.VerticalFlowLayout;

public class DockableCorrelationDataPanel extends DefaultSingleCDockable {

	private static final long serialVersionUID = -5960866035813998187L;
	
	private static final Icon componentIcon = GuiUtils.getIcon("correlation", 16);
	private static final DecimalFormat corrFormat = new DecimalFormat("0.000");
	
	private CorrelationPlotPanel corrPlot;
	private CorrelationPlotToolbar corrPlotToolbar;
	private JLabel pearsonValueLabel;
	private JLabel spearmanValueLabelLabel;
	private JLabel kendallValueLabelLabel;
		
	private PearsonsCorrelation pearson;
	private SpearmansCorrelation spearman;
	private KendallsCorrelation kendall;

	public DockableCorrelationDataPanel(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));
		JPanel wrapper = new JPanel();
		add(wrapper, BorderLayout.CENTER);
		wrapper.setLayout(new VerticalFlowLayout(VerticalFlowLayout.LEADING));

		JPanel labelsPanel = new JPanel();
		labelsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		wrapper.add(labelsPanel);
		
		Font labelFont = new Font("Tahoma", Font.BOLD, 12);

		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		labelsPanel.setLayout(gbl_panel);

		JLabel lblCorrelationValues = new JLabel("Correlation values");
		lblCorrelationValues.setFont(labelFont);
		GridBagConstraints gbc_lblCorrelationValues = new GridBagConstraints();
		gbc_lblCorrelationValues.fill = GridBagConstraints.BOTH;
		gbc_lblCorrelationValues.insets = new Insets(0, 0, 5, 5);
		gbc_lblCorrelationValues.gridx = 0;
		gbc_lblCorrelationValues.gridy = 0;
		labelsPanel.add(lblCorrelationValues, gbc_lblCorrelationValues);

		JLabel lblPearson = new JLabel("Pearson: ");
		GridBagConstraints gbc_lblPearson = new GridBagConstraints();
		gbc_lblPearson.fill = GridBagConstraints.BOTH;
		gbc_lblPearson.insets = new Insets(0, 0, 5, 5);
		gbc_lblPearson.gridx = 0;
		gbc_lblPearson.gridy = 1;
		labelsPanel.add(lblPearson, gbc_lblPearson);

		pearsonValueLabel = new JLabel("");
		pearsonValueLabel.setFont(labelFont);
		GridBagConstraints gbc_pearsonValueLabel = new GridBagConstraints();
		gbc_pearsonValueLabel.fill = GridBagConstraints.BOTH;
		gbc_pearsonValueLabel.insets = new Insets(0, 0, 5, 0);
		gbc_pearsonValueLabel.gridx = 1;
		gbc_pearsonValueLabel.gridy = 1;
		labelsPanel.add(pearsonValueLabel, gbc_pearsonValueLabel);

		JLabel lblSpearman = new JLabel("Spearman: ");
		GridBagConstraints gbc_lblSpearman = new GridBagConstraints();
		gbc_lblSpearman.fill = GridBagConstraints.BOTH;
		gbc_lblSpearman.insets = new Insets(0, 0, 5, 5);
		gbc_lblSpearman.gridx = 0;
		gbc_lblSpearman.gridy = 2;
		labelsPanel.add(lblSpearman, gbc_lblSpearman);

		spearmanValueLabelLabel = new JLabel("");
		spearmanValueLabelLabel.setFont(labelFont);
		GridBagConstraints gbc_spearmanValueLabelLabel = new GridBagConstraints();
		gbc_spearmanValueLabelLabel.fill = GridBagConstraints.BOTH;
		gbc_spearmanValueLabelLabel.insets = new Insets(0, 0, 5, 0);
		gbc_spearmanValueLabelLabel.gridx = 1;
		gbc_spearmanValueLabelLabel.gridy = 2;
		labelsPanel.add(spearmanValueLabelLabel, gbc_spearmanValueLabelLabel);

		JLabel lblKendall = new JLabel("Kendall: ");
		GridBagConstraints gbc_lblKendall = new GridBagConstraints();
		gbc_lblKendall.fill = GridBagConstraints.BOTH;
		gbc_lblKendall.insets = new Insets(0, 0, 0, 5);
		gbc_lblKendall.gridx = 0;
		gbc_lblKendall.gridy = 3;
		labelsPanel.add(lblKendall, gbc_lblKendall);

		kendallValueLabelLabel = new JLabel("");
		kendallValueLabelLabel.setFont(labelFont);
		GridBagConstraints gbc_kendallValueLabelLabel = new GridBagConstraints();
		gbc_kendallValueLabelLabel.fill = GridBagConstraints.BOTH;
		gbc_kendallValueLabelLabel.gridx = 1;
		gbc_kendallValueLabelLabel.gridy = 3;
		labelsPanel.add(kendallValueLabelLabel, gbc_kendallValueLabelLabel);

		corrPlot = new CorrelationPlotPanel();
		corrPlotToolbar = new CorrelationPlotToolbar(corrPlot);
		wrapper.add(corrPlot);

		add(corrPlotToolbar, BorderLayout.NORTH);
		
		pearson = new PearsonsCorrelation();
		spearman = new SpearmansCorrelation();
		kendall = new KendallsCorrelation();
	}

	public synchronized void clearPanel() {

		corrPlot.removeAllDataSets();
		pearsonValueLabel.setText("");
		spearmanValueLabelLabel.setText("");
		kendallValueLabelLabel.setText("");
	}

	public void createCorrelationPlot(
			MsFeature fOne, 
			DataPipeline dpOne,
			MsFeature fTwo,
			DataPipeline dpTwo) {

		corrPlot.showMultiAssayCorrelationPlot(fOne, dpOne, fTwo, dpTwo);
		calculateCorrelations();
	}

	private void calculateCorrelations() {

		pearsonValueLabel.setText("");
		spearmanValueLabelLabel.setText("");
		kendallValueLabelLabel.setText("");
		RealMatrix data = corrPlot.getDataMatrix();
		if (data != null && data.getColumnDimension() > 1 && data.getRowDimension() > 1) {

			pearsonValueLabel.setText(
					corrFormat.format(pearson.correlation(data.getColumn(0), data.getColumn(1))));
			spearmanValueLabelLabel
					.setText(corrFormat.format(spearman.correlation(data.getColumn(0), data.getColumn(1))));
			kendallValueLabelLabel
					.setText(corrFormat.format(kendall.correlation(data.getColumn(0), data.getColumn(1))));		
		}
	}
}
