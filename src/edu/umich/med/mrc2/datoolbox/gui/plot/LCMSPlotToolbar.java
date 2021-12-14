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

package edu.umich.med.mrc2.datoolbox.gui.plot;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;

import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

/**
 * @author Sasha
 *
 */
public class LCMSPlotToolbar extends PlotToolbar implements ItemListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 3326538358094303867L;
	private PlotType plotType;

	protected static final Icon msHead2tailIcon = GuiUtils.getIcon("msHead2tail", 24);
	protected static final Icon msHead2headIcon = GuiUtils.getIcon("msHead2head", 24);

	protected JButton toggleTailHeadButton;
	protected JComboBox chromatogramTypeComboBox;
	protected LCMSPlotPanel parentPlot;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public LCMSPlotToolbar(LCMSPlotPanel parentPlot, ActionListener plotTypeSwitchListener) {

		super(parentPlot);
		this.parentPlot = parentPlot;

		plotType = parentPlot.getPlotType();

		toggleLabelsButton = GuiUtils.addButton(this, null, labelInactiveIcon, commandListener,
				LCMSPlotPanel.TOGGLE_ANNOTATIONS_COMMAND, "Hide labels", buttonDimension);

		if (plotType.equals(PlotType.CHROMATOGRAM)) {
			
			//	Show Chromatogram type selector
			chromatogramTypeComboBox = new JComboBox<ChromatogramRenderingType>(
					new DefaultComboBoxModel(ChromatogramRenderingType.values()));			
			chromatogramTypeComboBox.setMaximumSize(new Dimension(120, 26));			
			chromatogramTypeComboBox.setSelectedItem(ChromatogramRenderingType.Lines);
			chromatogramTypeComboBox.addItemListener(this);
			add(chromatogramTypeComboBox);

			toggleDataPointsButton = GuiUtils.addButton(this, null, dataPointsOffIcon, commandListener,
					LCMSPlotPanel.TOGGLE_DATA_POINTS_COMMAND, "Show data points", buttonDimension);
			xAxisUnits = "time";
			createSmoothingBlock();
		}
		if (plotType.equals(PlotType.SPECTRUM)) {

			xAxisUnits = "m/z";
			toggleTailHeadButton = GuiUtils.addButton(this, null, msHead2headIcon, plotTypeSwitchListener,
					LCMSPlotPanel.TOGGLE_MS_HEAD_TO_TAIL_COMMAND, "Reference display type", buttonDimension);
		}

		addSeparator(buttonDimension);

		createZoomBlock();

		addSeparator(buttonDimension);

		createLegendToggle();

		addSeparator(buttonDimension);

		createServiceBlock();

		toggleLegendIcon(parentPlot.isLegendVisible());
		toggleAnnotationsIcon(parentPlot.areAnnotationsVisible());
		toggleDataPointssIcon(parentPlot.areDataPointsVisible());

		parentPlot.setToolbar(this);
	}
	
	public ChromatogramRenderingType getChromatogramRenderingType() {
		
		if(chromatogramTypeComboBox != null)
			return (ChromatogramRenderingType)chromatogramTypeComboBox.getSelectedItem();
		
		return null;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		
		if (e.getSource().equals(chromatogramTypeComboBox) &&
				e.getStateChange() == ItemEvent.SELECTED) {

			parentPlot.redrawChromatograms((ChromatogramRenderingType)chromatogramTypeComboBox.getSelectedItem());
		}
	}
}
