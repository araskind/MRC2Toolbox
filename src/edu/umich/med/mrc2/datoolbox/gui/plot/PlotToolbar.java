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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jfree.chart.ChartPanel;

import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.CommonToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class PlotToolbar extends CommonToolbar implements ActionListener{

	/**
	 *
	 */
	private static final long serialVersionUID = 5809328110952861549L;
	
	protected static final Icon zoomInHorizontalIcon = GuiUtils.getIcon("zoomX", 24);
	protected static final Icon zoomOutHorizontalIcon = GuiUtils.getIcon("zoomOutX", 24);
	protected static final Icon zoomInVerticalIcon = GuiUtils.getIcon("zoomY", 24);
	protected static final Icon zoomOutVerticalIcon = GuiUtils.getIcon("zoomOutY", 24);
	protected static final Icon autoRangeVerticalIcon = GuiUtils.getIcon("fitY", 24);
	protected static final Icon autoRangeHorizontalIcon = GuiUtils.getIcon("fitX", 24);
	protected static final Icon autoRangeIcon = GuiUtils.getIcon("fitAll", 24);
	protected static final Icon copyIcon = GuiUtils.getIcon("clipBoard", 24);
	protected static final Icon saveIcon = GuiUtils.getIcon("savePlot", 24);
	protected static final Icon printIcon = GuiUtils.getIcon("print", 24);
	protected static final Icon settingsIcon = GuiUtils.getIcon("plotSettings", 24);
	protected static final Icon showLegendIcon = GuiUtils.getIcon("showLegend", 24);
	protected static final Icon hideLegendIcon = GuiUtils.getIcon("hiddenLegend", 24);
	protected static final Icon savePngIcon = GuiUtils.getIcon("savePng", 24);
	protected static final Icon savePdfIcon = GuiUtils.getIcon("savePdf", 24);
	protected static final Icon saveSvgIcon = GuiUtils.getIcon("saveSvg", 24);
	protected static final Icon labelActiveIcon = GuiUtils.getIcon("plotLabelActive", 24);
	protected static final Icon labelInactiveIcon = GuiUtils.getIcon("plotLabelInactive", 24);
	protected static final Icon dataPointsOnIcon = GuiUtils.getIcon("chromatogram_dotted", 24);
	protected static final Icon dataPointsOffIcon = GuiUtils.getIcon("chromatogram", 24);	
	protected static final Icon smoothingOnIcon = GuiUtils.getIcon("smoothChromatogram", 24);
	protected static final Icon smoothingOffIcon = GuiUtils.getIcon("rawChromatogram", 24);
	protected static final Icon smoothingPreferencesIcon = GuiUtils.getIcon("smoothingPreferences", 24);	
	protected static final Icon msHead2tailIcon = GuiUtils.getIcon("msHead2tail", 24);
	protected static final Icon msHead2headIcon = GuiUtils.getIcon("msHead2head", 24);
	
	protected MasterPlotPanel parentPlot;
	protected Dimension buttonDimension = new Dimension(28, 28);

	protected JButton
		toggleLegendButton,
		toggleAnnotationsButton,
		toggleDataPointsButton,
		saveButton,
		toggleSmoothingButton,
		smoothingPreferencesButton,
		resetDomainButton,
		resetBothButton;

	protected String xAxisUnits;

	protected JPopupMenu saveAs;
	protected JMenuItem saveAsPngMenuItem;
	protected JMenuItem saveAsPdfMenuItem;
	protected JMenuItem saveAsSvgMenuItem;

	public PlotToolbar(MasterPlotPanel parentPlot) {

		super(parentPlot);
		this.parentPlot = parentPlot;
	}

	protected void createLegendToggle() {

		toggleLegendButton = GuiUtils.addButton(this, null, hideLegendIcon, commandListener,
				MainActionCommands.SHOW_PLOT_LEGEND_COMMAND.getName(), 
				MainActionCommands.SHOW_PLOT_LEGEND_COMMAND.getName(), buttonDimension);

		toggleLegendIcon(parentPlot.isLegendVisible());
		toggleLegendButton.addActionListener(this);
		buttonSet.add(toggleLegendButton);
	}
	
	protected void createAnnotationsToggle() {
		
		toggleAnnotationsButton = GuiUtils.addButton(this, null, labelInactiveIcon, commandListener,
				MainActionCommands.SHOW_PLOT_LABELS_COMMAND.getName(), 
				MainActionCommands.SHOW_PLOT_LABELS_COMMAND.getName(), buttonDimension);
		toggleAnnotationsIcon(parentPlot.areAnnotationsVisible());
		toggleAnnotationsButton.addActionListener(this);
		buttonSet.add(toggleAnnotationsButton);
	}
	
	protected void createDataPointsToggle() {
		
		toggleDataPointsButton = GuiUtils.addButton(this, null, dataPointsOffIcon, commandListener,
				MainActionCommands.SHOW_PLOT_DATA_POINTS_COMMAND.getName(), 
				MainActionCommands.SHOW_PLOT_DATA_POINTS_COMMAND.getName(), buttonDimension);
		toggleDataPointsIcon(parentPlot.areDataPointsVisible());
		toggleDataPointsButton.addActionListener(this);
		buttonSet.add(toggleDataPointsButton);
	}
	
	protected void createServiceBlock() {

		GuiUtils.addButton(this, null, copyIcon, commandListener, ChartPanel.COPY_COMMAND, "Copy graph",
				buttonDimension);
		
		saveAs = new JPopupMenu("Save as");

		saveAsPngMenuItem = GuiUtils.addMenuItem(saveAs,
				MainActionCommands.SAVE_AS_PNG.getName(), commandListener,
				MainActionCommands.SAVE_AS_PNG.name(), savePngIcon);

		saveAsPdfMenuItem = GuiUtils.addMenuItem(saveAs,
				MainActionCommands.SAVE_AS_PDF.getName(), commandListener,
				MainActionCommands.SAVE_AS_PDF.name(), savePdfIcon);

		saveAsSvgMenuItem = GuiUtils.addMenuItem(saveAs,
				MainActionCommands.SAVE_AS_SVG.getName(), commandListener,
				MainActionCommands.SAVE_AS_SVG.name(), saveSvgIcon);

		saveButton = GuiUtils.addButton(this, "Save plot", saveIcon, null, null, null, new Dimension(105, 35));
		saveButton.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				saveAs.show(e.getComponent(), e.getX(), e.getY());
			}
		});
		GuiUtils.addButton(this, null, printIcon, commandListener, ChartPanel.PRINT_COMMAND, "Print graph",
				buttonDimension);

		GuiUtils.addButton(this, null, settingsIcon, commandListener, ChartPanel.PROPERTIES_COMMAND,
				"Setup graph properties", buttonDimension);
	}

	protected void createZoomBlock() {

		// Fit icons
		GuiUtils.addButton(this, null, autoRangeVerticalIcon, commandListener, ChartPanel.ZOOM_RESET_RANGE_COMMAND,
				"Fit to intensity range", buttonDimension);

		resetDomainButton = GuiUtils.addButton(this, null, autoRangeHorizontalIcon, commandListener, ChartPanel.ZOOM_RESET_DOMAIN_COMMAND,
				"Fit to " + xAxisUnits + " range", buttonDimension);

		resetBothButton = GuiUtils.addButton(this, null, autoRangeIcon, commandListener, ChartPanel.ZOOM_RESET_BOTH_COMMAND,
				"Fit to  " + xAxisUnits + "  and intensity ranges", buttonDimension);

		addSeparator(buttonDimension);

		// Zoom buttons
		GuiUtils.addButton(this, null, zoomInHorizontalIcon, commandListener, ChartPanel.ZOOM_IN_DOMAIN_COMMAND,
				"Zoom in  " + xAxisUnits + " axis", buttonDimension);

		GuiUtils.addButton(this, null, zoomOutHorizontalIcon, commandListener, ChartPanel.ZOOM_OUT_DOMAIN_COMMAND,
				"Zoom out  " + xAxisUnits + " axis", buttonDimension);

		GuiUtils.addButton(this, null, zoomInVerticalIcon, commandListener, ChartPanel.ZOOM_IN_RANGE_COMMAND,
				"Zoom in intensity axis", buttonDimension);

		GuiUtils.addButton(this, null, zoomOutVerticalIcon, commandListener, ChartPanel.ZOOM_OUT_RANGE_COMMAND,
				"Zoom out intensity axis", buttonDimension);
	}
	
	protected void createSmoothingBlock() {
		
		toggleSmoothingButton = GuiUtils.addButton(this, null, smoothingOffIcon, commandListener,
				MainActionCommands.SMOOTH_CHROMATOGRAM_COMMAND.getName(), 
				MainActionCommands.SMOOTH_CHROMATOGRAM_COMMAND.getName(), buttonDimension);
		toggleSmoothingButton.addActionListener(this);
		
		smoothingPreferencesButton = GuiUtils.addButton(this, null, smoothingPreferencesIcon, commandListener,
				MainActionCommands.SHOW_SMOOTHING_PREFERENCES_COMMAND.getName(), 
				MainActionCommands.SHOW_SMOOTHING_PREFERENCES_COMMAND.getName(), buttonDimension);
	}
	
	public void toggleSmoothingIcon(boolean smoothChromatogram) {
				
		if(toggleSmoothingButton == null)
			return;

		if (smoothChromatogram) {

			toggleSmoothingButton.setIcon(smoothingOnIcon);
			toggleSmoothingButton.setActionCommand(
					MainActionCommands.SHOW_RAW_CHROMATOGRAM_COMMAND.getName());
			toggleSmoothingButton.setToolTipText(
					MainActionCommands.SHOW_RAW_CHROMATOGRAM_COMMAND.getName());
		} else {
			toggleSmoothingButton.setIcon(smoothingOffIcon);
			toggleSmoothingButton.setActionCommand(
					MainActionCommands.SMOOTH_CHROMATOGRAM_COMMAND.getName());
			toggleSmoothingButton.setToolTipText(
					MainActionCommands.SMOOTH_CHROMATOGRAM_COMMAND.getName());
		}
	}

	protected void toggleAnnotationsIcon(boolean isAnnotationVisible) {

		if(toggleAnnotationsButton == null)
			return;
		
		if (isAnnotationVisible) {

			toggleAnnotationsButton.setIcon(labelActiveIcon);
			toggleAnnotationsButton.setActionCommand(
					MainActionCommands.HIDE_PLOT_LABELS_COMMAND.getName());
			toggleAnnotationsButton.setToolTipText(
					MainActionCommands.HIDE_PLOT_LABELS_COMMAND.getName());
		} else {
			toggleAnnotationsButton.setIcon(labelInactiveIcon);
			toggleAnnotationsButton.setActionCommand(
					MainActionCommands.SHOW_PLOT_LABELS_COMMAND.getName());
			toggleAnnotationsButton.setToolTipText(
					MainActionCommands.SHOW_PLOT_LABELS_COMMAND.getName());
		}
	}

	protected void toggleDataPointsIcon(boolean dataPointsVisibleVisible) {

		if(toggleDataPointsButton == null)
			return;

		if (dataPointsVisibleVisible) {

			toggleDataPointsButton.setIcon(dataPointsOnIcon);
			toggleDataPointsButton.setActionCommand(
					MainActionCommands.HIDE_PLOT_DATA_POINTS_COMMAND.getName());
			toggleDataPointsButton.setToolTipText(
					MainActionCommands.HIDE_PLOT_DATA_POINTS_COMMAND.getName());
		} else {
			toggleDataPointsButton.setIcon(dataPointsOffIcon);
			toggleDataPointsButton.setActionCommand(
					MainActionCommands.SHOW_PLOT_DATA_POINTS_COMMAND.getName());
			toggleDataPointsButton.setToolTipText(
					MainActionCommands.SHOW_PLOT_DATA_POINTS_COMMAND.getName());
		}		
	}

	protected void toggleLegendIcon(boolean isLegendVisible) {

		if(toggleLegendButton == null)
			return;
		
		if (isLegendVisible) {

			toggleLegendButton.setIcon(showLegendIcon);
			toggleLegendButton.setActionCommand(
					MainActionCommands.HIDE_PLOT_LEGEND_COMMAND.getName());
			toggleLegendButton.setToolTipText(
					MainActionCommands.HIDE_PLOT_LEGEND_COMMAND.getName());
		} 
		else {			
			toggleLegendButton.setIcon(hideLegendIcon);
			toggleLegendButton.setActionCommand(
					MainActionCommands.SHOW_PLOT_LEGEND_COMMAND.getName());
			toggleLegendButton.setToolTipText(
					MainActionCommands.SHOW_PLOT_LEGEND_COMMAND.getName());
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
		
		if(command.equals(MainActionCommands.SHOW_PLOT_LEGEND_COMMAND.getName()))
			toggleLegendIcon(true);
		
		if(command.equals(MainActionCommands.HIDE_PLOT_LEGEND_COMMAND.getName()))
			toggleLegendIcon(false);
		
		if(command.equals(MainActionCommands.SHOW_PLOT_LABELS_COMMAND.getName()))
			toggleAnnotationsIcon(true);
		
		if(command.equals(MainActionCommands.HIDE_PLOT_LABELS_COMMAND.getName()))
			toggleAnnotationsIcon(false);
		
		if(command.equals(MainActionCommands.SHOW_PLOT_DATA_POINTS_COMMAND.getName()))
			toggleDataPointsIcon(true);
		
		if(command.equals(MainActionCommands.HIDE_PLOT_DATA_POINTS_COMMAND.getName()))
			toggleDataPointsIcon(false);
		 
		if(command.equals(MainActionCommands.SMOOTH_CHROMATOGRAM_COMMAND.getName())) 
			toggleSmoothingIcon(true);
		
		if(command.equals(MainActionCommands.SHOW_RAW_CHROMATOGRAM_COMMAND.getName())) 
			toggleSmoothingIcon(false);
	}
}








