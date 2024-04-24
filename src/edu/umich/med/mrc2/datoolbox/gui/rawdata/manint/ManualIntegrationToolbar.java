package edu.umich.med.mrc2.datoolbox.gui.rawdata.manint;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.PlotToolbar;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class ManualIntegrationToolbar extends PlotToolbar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4724658623720396507L;
	
	private static final Icon integrateIcon = GuiUtils.getIcon("peakIntegrate", 24);
	private static final Icon acceptResultsIcon = GuiUtils.getIcon("approvedBin", 24);
	private static final Icon clearSelectionsIcon = GuiUtils.getIcon("clear", 24);
	private static final Icon resetToOriginalIcon = GuiUtils.getIcon("rerun", 24);
	private static final Icon settingsIcon = GuiUtils.getIcon("preferences", 24);
	private JButton 
		preferencesButton, 
		resetToOriginalButton, 
		integrateButton, 
		acceptResultsButton, 
		clearSelectionsButton;
	
	public ManualIntegrationToolbar(
			PeakIntegrationPlotPanel parentPlot, 
			ActionListener integrationActionListener) {

		super(parentPlot);
		xAxisUnits = "RT";
		toggleDataPointsButton = GuiUtils.addButton(this, null, dataPointsOffIcon, commandListener,
				MasterPlotPanel.TOGGLE_DATA_POINTS_COMMAND, "Show data points", buttonDimension);
		
		addSeparator(buttonDimension);
		createZoomBlock();
		addSeparator(buttonDimension);
		createLegendToggle();
		addSeparator(buttonDimension);
		createServiceBlock();
		
		addSeparator(buttonDimension);
		
		integrateButton = GuiUtils.addButton(this, null, integrateIcon, integrationActionListener,
				MainActionCommands.INTEGRATE_HIGHLIGHTED_RANGES.getName(), 
				MainActionCommands.INTEGRATE_HIGHLIGHTED_RANGES.getName(), buttonDimension);
		
		acceptResultsButton = GuiUtils.addButton(this, null, acceptResultsIcon, integrationActionListener,
				MainActionCommands.ACCEPT_INTEGRATION_RESULTS.getName(), 
				MainActionCommands.ACCEPT_INTEGRATION_RESULTS.getName(), buttonDimension);
		
		addSeparator(buttonDimension);
		
		clearSelectionsButton = GuiUtils.addButton(this, null, clearSelectionsIcon, integrationActionListener,
				MainActionCommands.CLEAR_HIGHLIGHTED_RANGES.getName(), 
				MainActionCommands.CLEAR_HIGHLIGHTED_RANGES.getName(), buttonDimension);
		
		resetToOriginalButton = GuiUtils.addButton(this, null, resetToOriginalIcon, integrationActionListener,
				MainActionCommands.RELOAD_ORIGINAL_CHROMATOGRAMS.getName(), 
				MainActionCommands.RELOAD_ORIGINAL_CHROMATOGRAMS.getName(), buttonDimension);
		
		addSeparator(buttonDimension);
		
		preferencesButton = GuiUtils.addButton(this, null, settingsIcon, integrationActionListener,
				MainActionCommands.SHOW_MANUAL_INTEGRATOR_SETTINGS.getName(), 
				MainActionCommands.SHOW_MANUAL_INTEGRATOR_SETTINGS.getName(), buttonDimension);
		
		toggleLegendIcon(parentPlot.isLegendVisible());
		toggleAnnotationsIcon(parentPlot.areAnnotationsVisible());
		toggleDataPointsIcon(parentPlot.areDataPointsVisible());
	}
}
