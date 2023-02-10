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

package edu.umich.med.mrc2.datoolbox.gui.dereplication.vis;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorGradient;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorScale;
import edu.umich.med.mrc2.datoolbox.gui.plot.HeatMapDataRange;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedComboBoxModel;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class ClusterVisToolbar extends CommonToolbar {

	/**
	 *
	 */
	private static final long serialVersionUID = 7197153605057272646L;

	private static final Icon heatmapIcon = GuiUtils.getIcon("heatmap", 32);
	private static final Icon treeIcon = GuiUtils.getIcon("tree", 32);
	private static final Icon tableIcon = GuiUtils.getIcon("table", 32);

	private JButton showHeatmapButton, showTreeButton, showTableButton;
	private JSpinner spinner;
	private JComboBox colorSchemeComboBox;
	private JComboBox colorScaleComboBox;
	private JComboBox dataRangeComboBox;

	public ClusterVisToolbar(ActionListener commandListener) {

		super(commandListener);

		showHeatmapButton = GuiUtils.addButton(this, null, heatmapIcon, commandListener,
				MainActionCommands.SHOW_HEATMAP_COMMAND.getName(), "Show heatmap", buttonDimension);
		showTreeButton = GuiUtils.addButton(this, null, treeIcon, commandListener,
				MainActionCommands.SHOW_DENDROGRAMM_COMMAND.getName(), "Show dendrogram", buttonDimension);

		addSeparator(buttonDimension);

		JLabel colorSchemeLabel = new JLabel("Palette ");
		add(colorSchemeLabel);

		SortedComboBoxModel<ColorGradient> colorSchemeModel = new SortedComboBoxModel<ColorGradient>(ColorGradient.values());
		colorSchemeComboBox = new JComboBox(colorSchemeModel);
		colorSchemeComboBox.setSelectedItem(ColorGradient.GREEN_RED);
		colorSchemeComboBox.setPreferredSize(new Dimension(100, 25));
		colorSchemeComboBox.setSize(new Dimension(100, 25));
		colorSchemeComboBox.addItemListener((ItemListener) commandListener);
		add(colorSchemeComboBox);

		JLabel colorScaleLabel = new JLabel("Color scale ");
		add(colorScaleLabel);

		SortedComboBoxModel<ColorScale> colorSclaleModel = new SortedComboBoxModel<ColorScale>(ColorScale.values());
		colorScaleComboBox = new JComboBox(colorSclaleModel);
		colorScaleComboBox.setSelectedItem(ColorScale.LINEAR);
		colorScaleComboBox.setPreferredSize(new Dimension(100, 25));
		colorScaleComboBox.setSize(new Dimension(100, 25));
		colorScaleComboBox.addItemListener((ItemListener) commandListener);
		add(colorScaleComboBox);

		JLabel dataRangeLabel = new JLabel("Dara range ");
		add(dataRangeLabel);

		SortedComboBoxModel<HeatMapDataRange> dataRangeModel = new SortedComboBoxModel<HeatMapDataRange>(HeatMapDataRange.values());
		dataRangeComboBox = new JComboBox(dataRangeModel);
		dataRangeComboBox.setSelectedItem(HeatMapDataRange.FULL);
		dataRangeComboBox.setPreferredSize(new Dimension(100, 25));
		dataRangeComboBox.setSize(new Dimension(100, 25));
		dataRangeComboBox.addItemListener((ItemListener) commandListener);
		add(dataRangeComboBox);

		// showTableButton =
		// GuiUtils.addButton(this, null, tableIcon, commandListener,
		// HeatMapFrame.SHOW_TABLE_COMMAND, "Show table", buttonDimension);

	}

	public int getClusterNumber() {

		return (int) spinner.getValue();
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

}
