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

package edu.umich.med.mrc2.datoolbox.gui.plot.lcms.chromatogram;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;

import javax.swing.Icon;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExtractedChromatogram;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureChromatogramBundle;
import edu.umich.med.mrc2.datoolbox.gui.plot.PlotType;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.LCMSPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.LCMSPlotToolbar;
import edu.umich.med.mrc2.datoolbox.gui.preferences.SmoothingFilterManager;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class DockableChromatogramPlot extends DefaultSingleCDockable implements ActionListener {

	private LCMSPlotPanel chromatogramPlot;
	private LCMSPlotToolbar chromatogramToolbar;

	private static final Icon componentIcon = GuiUtils.getIcon("chromatogramDotted", 16);

	public DockableChromatogramPlot(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0,0));

		chromatogramPlot = new LCMSPlotPanel(PlotType.CHROMATOGRAM);
		chromatogramPlot.setFilterId(id);
		chromatogramPlot.setSmoothingFilter(SmoothingFilterManager.getFilter(id));
		add(chromatogramPlot, BorderLayout.CENTER);

		chromatogramToolbar = 
				new LCMSPlotToolbar(chromatogramPlot, PlotType.CHROMATOGRAM, this);
		add(chromatogramToolbar, BorderLayout.NORTH);
	}
	
	public synchronized void clearPanel() {
		chromatogramPlot.removeAllDataSets();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	public void showExtractedChromatogramCollection(
			Collection<ExtractedChromatogram> chromatograms) {

		chromatogramPlot.removeAllDataSets();
		chromatogramPlot.showExtractedChromatogramCollection(
				chromatograms, chromatogramToolbar.getChromatogramRenderingType());
	}

	public Range getSelectedRTRange() {
		return chromatogramPlot.getSelectedRTRange();
	}
	
	public void removeChromatogramsForFiles(Collection<DataFile>files) {
		chromatogramPlot.removeChromatogramsForFiles(files);
	}
	
	public void showMsFeatureChromatogramBundle(
			MsFeatureChromatogramBundle xicBundle, Collection<Double>markers) {
		
		chromatogramPlot.removeAllDataSets();
		chromatogramPlot.showMsFeatureChromatogramBundles(Collections.singleton(xicBundle), markers,
				chromatogramToolbar.getChromatogramRenderingType());
	}
	
	public void showMultipleMsFeatureChromatogramBundles(
			Collection<MsFeatureChromatogramBundle> xicBundles, Collection<Double>markers) {
		
		chromatogramPlot.removeAllDataSets();
		chromatogramPlot.showMsFeatureChromatogramBundles(xicBundles, markers,
				chromatogramToolbar.getChromatogramRenderingType());
	}
	
	public void setRetentionMarker(double rt) {
		chromatogramPlot.setRetentionMarker(rt);
	}
}


















