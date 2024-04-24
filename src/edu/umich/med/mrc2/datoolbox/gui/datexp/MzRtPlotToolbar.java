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

package edu.umich.med.mrc2.datoolbox.gui.datexp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.PlotToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MzRtPlotToolbar extends PlotToolbar{

	/**
	 *
	 */
	private static final long serialVersionUID = -8756954637925601169L;
	
	protected static final Icon sidePanelShowIcon = GuiUtils.getIcon("sidePanelShow", 24);
	protected static final Icon sidePanelHideIcon = GuiUtils.getIcon("sidePanelHide", 24);

	private JButton sidePanelButton;
	
	public MzRtPlotToolbar(
			DataExplorerPlotPanel parentPlot,
			ItemListener dropdownListener,
			ActionListener secondaryListener) {

		super(parentPlot);

		createZoomBlock();
		addSeparator(buttonDimension);
		createLegendToggle();
		addSeparator(buttonDimension);
		createServiceBlock();
		toggleLegendIcon(parentPlot.isLegendVisible());
		
		add(Box.createHorizontalGlue());
		
		sidePanelButton = GuiUtils.addButton(
				this, null, sidePanelHideIcon, secondaryListener, 
				MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName(), 
				MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName(), 
				buttonDimension);
		sidePanelButton.addActionListener(this);
		add(sidePanelButton);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.SHOW_CHART_SIDE_PANEL_COMMAND.getName()))
			setSidePanelVisible(true);
		
		if(command.equals(MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName())) 
			setSidePanelVisible(false);	
		
		super.actionPerformed(e);
	}
		
	private void setSidePanelVisible(boolean b){
		
		if(b) {
			sidePanelButton.setIcon(sidePanelHideIcon);
			sidePanelButton.setActionCommand(MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName());
			sidePanelButton.setToolTipText(MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName());
		}
		else {
			sidePanelButton.setIcon(sidePanelShowIcon);
			sidePanelButton.setActionCommand(MainActionCommands.SHOW_CHART_SIDE_PANEL_COMMAND.getName());
			sidePanelButton.setToolTipText(MainActionCommands.SHOW_CHART_SIDE_PANEL_COMMAND.getName());
		}
	}
}


