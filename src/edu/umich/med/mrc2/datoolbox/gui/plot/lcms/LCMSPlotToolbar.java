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

package edu.umich.med.mrc2.datoolbox.gui.plot.lcms;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.PlotToolbar;
import edu.umich.med.mrc2.datoolbox.gui.plot.PlotType;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.chromatogram.ChromatogramRenderingType;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

/**
 * @author Sasha
 *
 */
public class LCMSPlotToolbar extends PlotToolbar implements ItemListener {

	protected static final long serialVersionUID = 3326538358094303867L;
	
	protected PlotType plotType;	
	protected JButton toggleTailHeadButton;
	protected JComboBox chromatogramTypeComboBox;
	protected ActionListener secondaryActionListener;
		
//	public LCMSPlotToolbar(ActionListener plotTypeSwitchListener) {
//		
//		super(null);
//		this.plotTypeSwitchListener = plotTypeSwitchListener;
//	}

	public LCMSPlotToolbar(
			MasterPlotPanel parentPlot, 
			PlotType plotType, 
			ActionListener plotTypeSwitchListener) {

		super(parentPlot);
		this.secondaryActionListener = plotTypeSwitchListener;
		this.plotType = plotType;
		
		initToolbar(plotType);
	}
	
	protected void createToggleTailHeadBlock() {
		
//		toggleTailHeadButton = GuiUtils.addButton(this, null, msHead2headIcon, plotTypeSwitchListener,
//				LCMSPlotPanel.TOGGLE_MS_HEAD_TO_TAIL_COMMAND, "Reference display type", buttonDimension);
//		toggleTailHeadButton.addActionListener(this);
//		buttonSet.add(toggleTailHeadButton);
	}
	
	protected void initToolbar(PlotType pt) {
		
		plotType = pt;

		createAnnotationsToggle();

		if (plotType.equals(PlotType.CHROMATOGRAM)) {
			
			xAxisUnits = "time";
			
			//	Show Chromatogram type selector
			chromatogramTypeComboBox = new JComboBox<ChromatogramRenderingType>(
					new DefaultComboBoxModel<ChromatogramRenderingType>(
							ChromatogramRenderingType.values()));			
			chromatogramTypeComboBox.setMaximumSize(new Dimension(120, 26));			
			chromatogramTypeComboBox.setSelectedItem(ChromatogramRenderingType.Lines);
			chromatogramTypeComboBox.addItemListener(this);
			add(chromatogramTypeComboBox);

			createDataPointsToggle();
			
			createSmoothingBlock();
		}
		if (plotType.equals(PlotType.SPECTRUM)) {

			xAxisUnits = "m/z";
			createToggleTailHeadBlock();
		}

		addSeparator(buttonDimension);

		createZoomBlock();
		if(plotType.equals(PlotType.SPECTRUM) && secondaryActionListener != null) {
			resetDomainButton.addActionListener(secondaryActionListener);
			resetBothButton.addActionListener(secondaryActionListener);
		}

		addSeparator(buttonDimension);

		createLegendToggle();

		addSeparator(buttonDimension);

		createServiceBlock();
	}
	
	protected void toggleTailHeadIcon(boolean isHeadToTail) {

		if(toggleTailHeadButton == null)
			return;

		if (isHeadToTail) {

			toggleTailHeadButton.setIcon(msHead2tailIcon);
			toggleTailHeadButton.setToolTipText("Show overlay");
		} else {
			toggleTailHeadButton.setIcon(msHead2headIcon);
			toggleTailHeadButton.setToolTipText("Had-to-tail");
		}		
	}

	@Override
	public void updateGuiFromExperimentAndDataPipeline(
			DataAnalysisProject experiment, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();
		
		if(command.equals(LCMSPlotPanel.TOGGLE_MS_HEAD_TO_TAIL_COMMAND)) {
			
			boolean isHeadToTail = 
					toggleTailHeadButton.getIcon().equals(msHead2tailIcon);
			toggleTailHeadIcon(isHeadToTail);
		}
		super.actionPerformed(e);
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

			if(parentPlot instanceof LCMSPlotPanel)
				((LCMSPlotPanel)parentPlot).redrawChromatograms(
						(ChromatogramRenderingType)chromatogramTypeComboBox.getSelectedItem());
		}
	}
}
