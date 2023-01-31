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

public class PlotToolbar extends CommonToolbar {

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
	protected static final Icon hideLegendIcon = GuiUtils.getIcon("hideLegend", 24);
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
	
	protected Dimension buttonDimension = new Dimension(28, 28);

	protected JButton
		toggleLegendButton,
		toggleLabelsButton,
		toggleDataPointsButton,
		saveButton,
		toggleSmoothingButton,
		smoothingPreferencesButton;

	protected String xAxisUnits;

	protected JPopupMenu saveAs;
	protected JMenuItem saveAsPngMenuItem;
	protected JMenuItem saveAsPdfMenuItem;
	protected JMenuItem saveAsSvgMenuItem;

	public PlotToolbar(ActionListener listener) {

		super(listener);
	}

	protected void createLegendToggle() {

		toggleLegendButton = GuiUtils.addButton(this, null, hideLegendIcon, commandListener,
				LCMSPlotPanel.TOGGLE_LEGEND_COMMAND, "Hide legend", buttonDimension);
	}

	protected void createServiceBlock() {

		GuiUtils.addButton(this, null, copyIcon, commandListener, ChartPanel.COPY_COMMAND, "Copy graph",
				buttonDimension);

//		GuiUtils.addButton(this, null, saveIcon, commandListener, ChartPanel.SAVE_COMMAND, "Save graph",
//				buttonDimension);

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

		GuiUtils.addButton(this, null, autoRangeHorizontalIcon, commandListener, ChartPanel.ZOOM_RESET_DOMAIN_COMMAND,
				"Fit to " + xAxisUnits + " range", buttonDimension);

		GuiUtils.addButton(this, null, autoRangeIcon, commandListener, ChartPanel.ZOOM_RESET_BOTH_COMMAND,
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
		
		smoothingPreferencesButton = GuiUtils.addButton(this, null, smoothingPreferencesIcon, commandListener,
				MainActionCommands.SHOW_SMOOTHING_PREFERENCES_COMMAND.getName(), 
				MainActionCommands.SHOW_SMOOTHING_PREFERENCES_COMMAND.getName(), buttonDimension);
	}
	
	public void toggleSmoothingIcon(boolean smoothChromatogram) {
				
		if(toggleSmoothingButton == null)
			return;

		if (smoothChromatogram) {

			toggleSmoothingButton.setIcon(smoothingOnIcon);
			toggleSmoothingButton.setActionCommand(MainActionCommands.SHOW_RAW_CHROMATOGRAM_COMMAND.getName());
			toggleSmoothingButton.setToolTipText(MainActionCommands.SHOW_RAW_CHROMATOGRAM_COMMAND.getName());
		} else {
			toggleSmoothingButton.setIcon(smoothingOffIcon);
			toggleSmoothingButton.setActionCommand(MainActionCommands.SMOOTH_CHROMATOGRAM_COMMAND.getName());
			toggleSmoothingButton.setToolTipText(MainActionCommands.SMOOTH_CHROMATOGRAM_COMMAND.getName());
		}
	}

	public void toggleAnnotationsIcon(boolean isAnnotationVisible) {

		if(toggleLabelsButton == null)
			return;
		
		if (isAnnotationVisible) {

			toggleLabelsButton.setIcon(labelActiveIcon);
			toggleLabelsButton.setToolTipText("Hide labels");
		} else {
			toggleLabelsButton.setIcon(labelInactiveIcon);
			toggleLabelsButton.setToolTipText("Show labels");
		}
	}

	public void toggleDataPointsIcon(boolean dataPointsVisibleVisible) {

		if(toggleDataPointsButton == null)
			return;
		
		if (toggleDataPointsButton != null) {

			if (dataPointsVisibleVisible) {

				toggleDataPointsButton.setIcon(dataPointsOnIcon);
				toggleDataPointsButton.setToolTipText("Hide data points");
			} else {
				toggleDataPointsButton.setIcon(dataPointsOffIcon);
				toggleDataPointsButton.setToolTipText("Show data points");
			}
		}
	}

	public void toggleLegendIcon(boolean isLegendVisible) {

		if(toggleLegendButton == null)
			return;
		
		if (toggleLegendButton != null) {

			if (isLegendVisible) {

				toggleLegendButton.setIcon(showLegendIcon);
				toggleLegendButton.setToolTipText("Hide legend");
			} else {
				toggleLegendButton.setIcon(hideLegendIcon);
				toggleLegendButton.setToolTipText("Show legend");
			}
		}
	}

	@Override
	public void updateGuiFromProjectAndDataPipeline(DataAnalysisProject project, DataPipeline newDataPipeline) {
		// TODO Auto-generated method stub

	}
}
