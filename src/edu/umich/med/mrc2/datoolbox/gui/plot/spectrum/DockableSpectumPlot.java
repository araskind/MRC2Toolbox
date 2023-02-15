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

package edu.umich.med.mrc2.datoolbox.gui.plot.spectrum;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.Icon;

import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.Layer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import bibliothek.gui.dock.action.DefaultDockActionSource;
import bibliothek.gui.dock.action.LocationHint;
import bibliothek.gui.dock.action.actions.SimpleButtonAction;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsMsCluster;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsMs;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.LCMSPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.LCMSPlotToolbar;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.PlotType;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.HeadToTaleMsDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.MsDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.MassSpectrumRenderer;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.RawDataExaminerPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import umich.ms.datatypes.scan.IScan;

public class DockableSpectumPlot extends DefaultSingleCDockable implements ActionListener, BackedByPreferences {

	private static final Icon componentIcon = GuiUtils.getIcon("msms", 16);
	private static final Icon multipleIdsIcon = GuiUtils.getIcon("multipleIds", 16);
	private static final Icon uniqueIdsIcon = GuiUtils.getIcon("checkboxFull", 16);
		
	private LCMSPlotPanel spectrumPlot;
	private LCMSPlotToolbar msPlotToolbar;
	
	private Preferences preferences;
	private String PREFERENCES_NODE;
	private static final String ZOOM_TO_MSMS_PRECURSOR = "ZOOM_TO_MSMS_PRECURSOR";
	private SimpleButtonAction zoomToMSMSPrecursorButton;
	private boolean zoomToMSMSPrecursor;

	private MsDataSet activeMsDataSet;
	
	public DockableSpectumPlot(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));

		spectrumPlot = new LCMSPlotPanel(PlotType.SPECTRUM);
		add(spectrumPlot, BorderLayout.CENTER);

		msPlotToolbar = new LCMSPlotToolbar(spectrumPlot, this);
		add(msPlotToolbar, BorderLayout.NORTH);
		
		PREFERENCES_NODE = "mrc2.datoolbox." + id;
		loadPreferences();
		initButtons();
	}
	
	private void initButtons() {

		DefaultDockActionSource actions = new DefaultDockActionSource(
				new LocationHint(LocationHint.DOCKABLE, LocationHint.LEFT));

		if (zoomToMSMSPrecursor) {
			zoomToMSMSPrecursorButton = GuiUtils.setupButtonAction(
					MainActionCommands.SHOW_FULL_MS_RANGE_COMMAND.getName(), 
					MainActionCommands.SHOW_FULL_MS_RANGE_COMMAND.getName(), 
					multipleIdsIcon, this);
		}
		else {
			zoomToMSMSPrecursorButton = GuiUtils.setupButtonAction(
					MainActionCommands.ZOOM_TO_MSMS_PRECURSOR_COMMAND.getName(), 
					MainActionCommands.ZOOM_TO_MSMS_PRECURSOR_COMMAND.getName(), 
					uniqueIdsIcon, this);
		}
		actions.add(zoomToMSMSPrecursorButton);
		actions.addSeparator();
		intern().setActionOffers(actions);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(LCMSPlotPanel.TOGGLE_MS_HEAD_TO_TAIL_COMMAND)) {

		}
		if(e.getActionCommand().equals(MainActionCommands.ZOOM_TO_MSMS_PRECURSOR_COMMAND.getName()))
			togglePrecursorZoom(true);
		
		if(e.getActionCommand().equals(MainActionCommands.SHOW_FULL_MS_RANGE_COMMAND.getName()))
			togglePrecursorZoom(false);	
	}
	
	private void togglePrecursorZoom(boolean zoom) {

		zoomToMSMSPrecursor = zoom;		
		if (zoomToMSMSPrecursor) {
			zoomToMSMSPrecursorButton.setText(
					MainActionCommands.SHOW_FULL_MS_RANGE_COMMAND.getName());
			zoomToMSMSPrecursorButton.setCommand(
					MainActionCommands.SHOW_FULL_MS_RANGE_COMMAND.getName());
			zoomToMSMSPrecursorButton.setIcon(multipleIdsIcon);
			
			if(activeMsDataSet == null)
				return;
			
			//	TODO this may need change to handle library MSMS and multiple MSMS
			if(MsFeature.class.isAssignableFrom(activeMsDataSet.getSpectrumSource().getClass())) {
				
				MsFeature f = (MsFeature)activeMsDataSet.getSpectrumSource();
				if(f.getSpectrum() != null 
						&& f.getSpectrum().getExperimentalTandemSpectrum() != null
						&& f.getSpectrum().getExperimentalTandemSpectrum().getParent() != null) {
					zoomToInterval(f.getSpectrum().getExperimentalTandemSpectrum().getParent().getMz(), 10.0d);
				}
			}
		}
		else {
			zoomToMSMSPrecursorButton.setText(
					MainActionCommands.ZOOM_TO_MSMS_PRECURSOR_COMMAND.getName());
			zoomToMSMSPrecursorButton.setCommand(
					MainActionCommands.ZOOM_TO_MSMS_PRECURSOR_COMMAND.getName());
			zoomToMSMSPrecursorButton.setIcon(uniqueIdsIcon);
			//((XYPlot)spectrumPlot.getPlot()).getDomainAxis().setAutoRange(true);
			spectrumPlot.restoreAutoBounds();
		}
		savePreferences();
	}

	public void removeAllDataSets() {
		spectrumPlot.removeAllDataSets();
	}
	
	public synchronized void clearPanel() {
		spectrumPlot.removeAllDataSets();
	}

	public void showMsDataSet(MsDataSet msDataSet) {

		activeMsDataSet = msDataSet;
		MassSpectrumRenderer msRenderer = spectrumPlot.getDefaultMsRenderer();
		for (int i = 0; i < msDataSet.getSeriesCount(); i++)
			msRenderer.setSeriesPaint(i, MasterPlotPanel.getColor(i));

		((XYPlot) spectrumPlot.getPlot()).setRenderer(1, msRenderer);
		((XYPlot) spectrumPlot.getPlot()).setDataset(1, activeMsDataSet);
		
		setPlotMargins(activeMsDataSet);
	}

	private void setPlotMargins(MsDataSet msDataSet) {

		activeMsDataSet = msDataSet;
		double margin = 1.05;
//		Range msRange = new Range(activeMsDataSet.getMassRange().getMin(), activeMsDataSet.getMassRange().getMax());
//
//		if (msRange.getLength() < 23.0d) {
//			margin = 1.5;
//			double minMass = msRange.getCentralValue() - msRange.getLength() * margin;
//			double maxMass = msRange.getCentralValue() + msRange.getLength() * margin;
//			msRange = new Range(minMass, maxMass);
//		}
//		if(msRange.getLowerBound() < 0)
//			msRange = new Range(0.0d, msRange.getUpperBound());

		((XYPlot) spectrumPlot.getPlot()).getDomainAxis().setAutoRange(true);
		double maxIntensity = msDataSet.getHighestIntensityInRange(activeMsDataSet.getMassRange());
		((XYPlot) spectrumPlot.getPlot()).getRangeAxis().setRange(new Range(0.0d, maxIntensity * 1.15));
	}

	public void showMsForPointCollection(Collection<MsPoint> pattern, boolean scaleMs, String seriesLabel) {

		spectrumPlot.removeAllDataSets();
		activeMsDataSet = new MsDataSet(pattern, scaleMs, seriesLabel);
		showMsDataSet(activeMsDataSet);
	}

	public void showMsForCluster(MsMsCluster selectedCluster) {

		spectrumPlot.removeAllDataSets();
		activeMsDataSet = new MsDataSet(selectedCluster, false);
		showMsDataSet(activeMsDataSet);
	}

	public void showSimpleMsMs(List<SimpleMsMs> selectedFeatures) {

		spectrumPlot.removeAllDataSets();
		MsDataSet clusterMs = new MsDataSet(selectedFeatures, true);
		showMsDataSet(clusterMs);
	}

	public void showMsForFeature(MsFeature cf, boolean scaleMs) {

		spectrumPlot.removeAllDataSets();
		activeMsDataSet = new MsDataSet(cf, scaleMs);
		showMsDataSet(activeMsDataSet);
		
		if(cf.getSpectrum() == null || cf.getSpectrum().getExperimentalTandemSpectrum() == null)
			return;
				
		TandemMassSpectrum msms = cf.getSpectrum().getExperimentalTandemSpectrum();
		if(msms.getIsolationWindow() != null && msms.getIsolationWindow().getSize() > 0.0d) {
			
			IntervalMarker marker = new IntervalMarker(
					msms.getIsolationWindow().getMin(), 
					msms.getIsolationWindow().getMax());
			marker.setPaint(LCMSPlotPanel.markerColor);
			marker.setAlpha(0.5f);
			((XYPlot) spectrumPlot.getPlot()).addDomainMarker(marker, Layer.FOREGROUND);
		}
		if(zoomToMSMSPrecursor && msms.getParent() != null)
			zoomToInterval(msms.getParent().getMz(), 5.0d);		
	}
	
	public void zoomToInterval(double center, double halfWidth) {
		
		double low = center - halfWidth;
		if(low < 0.0d)
			low = 0.0d;
			
		Range mzRange = new Range(low, center + halfWidth);
		((XYPlot) spectrumPlot.getPlot()).getDomainAxis().setRange(mzRange);
		double maxIntensity = activeMsDataSet.getHighestIntensityInRange(mzRange);
		((XYPlot) spectrumPlot.getPlot()).getRangeAxis().setRange(new Range(0.0d, maxIntensity * 1.15));
	}

	public void showMsForFeatureList(Collection<MsFeature> featureList, boolean scaleMs) {

		spectrumPlot.removeAllDataSets();
		activeMsDataSet = new MsDataSet(featureList, scaleMs);
		showMsDataSet(activeMsDataSet);
	}

	public void showMsForLibraryFeature(LibraryMsFeature lt, boolean scaleMs) {

		spectrumPlot.removeAllDataSets();
		activeMsDataSet = new MsDataSet(lt, scaleMs);
		showMsDataSet(activeMsDataSet);
		
		if(lt.getSpectrum() == null || lt.getSpectrum().getExperimentalTandemSpectrum() == null)
			return;
		
		//	TODO check if MSMS is set as library
		TandemMassSpectrum msms = lt.getSpectrum().getExperimentalTandemSpectrum();
		if(msms.getIsolationWindow() != null && msms.getIsolationWindow().getSize() > 0.0d) {
			
			IntervalMarker marker = new IntervalMarker(
					msms.getIsolationWindow().getMin(), 
					msms.getIsolationWindow().getMax());
			marker.setPaint(LCMSPlotPanel.markerColor);
			marker.setAlpha(0.5f);
			((XYPlot) spectrumPlot.getPlot()).addDomainMarker(marker, Layer.FOREGROUND);
		}
		if(zoomToMSMSPrecursor && msms.getParent() != null)
			zoomToInterval(msms.getParent().getMz(), 5.0d);	
	}
	
	public void showTandemMs(MsMsLibraryFeature libFeature) {

		activeMsDataSet = new MsDataSet(libFeature.getSpectrum());
		double size = 16.0;

		//	Add MSMS points
		MassSpectrumRenderer msRenderer = spectrumPlot.getDefaultMsRenderer();
		msRenderer.setSeriesPaint(2, Color.BLACK);
		((XYPlot) spectrumPlot.getPlot()).setRenderer(2, msRenderer);
		((XYPlot) spectrumPlot.getPlot()).setDataset(2, activeMsDataSet);

		//	Add parent ion
		XYSeriesCollection parentSet = new XYSeriesCollection();
		XYSeries parentSeries = new XYSeries("Parent ion");
		MsPoint trueParent = libFeature.getParent();
		if(trueParent != null)
			parentSeries.add(trueParent.getMz(), trueParent.getIntensity());
		parentSet.addSeries(parentSeries);
		XYItemRenderer parentRenderer = new XYLineAndShapeRenderer(false, true);
		parentRenderer.setSeriesPaint(0, Color.RED);
		parentRenderer.setSeriesShape(0, new Ellipse2D.Double(-size/4.0, -size/4.0, size/2.0, size/2.0));
		((XYPlot) spectrumPlot.getPlot()).setRenderer(1, parentRenderer);
		((XYPlot) spectrumPlot.getPlot()).setDataset(1, parentSet);

		setPlotMargins(activeMsDataSet);
	}	

	public void showTandemMs(TandemMassSpectrum msms) {

		activeMsDataSet = new MsDataSet(msms);
		double size = 16.0;

		//	Add MSMS points
		MassSpectrumRenderer msRenderer = spectrumPlot.getDefaultMsRenderer();
		msRenderer.setSeriesPaint(2, Color.BLACK);
		((XYPlot) spectrumPlot.getPlot()).setRenderer(2, msRenderer);
		((XYPlot) spectrumPlot.getPlot()).setDataset(2, activeMsDataSet);

		//	Add parent ion
		XYSeriesCollection parentSet = new XYSeriesCollection();
		XYSeries parentSeries = new XYSeries("Parent ion");		
		MsPoint trueParent = msms.getParent();
		if(trueParent == null)
			trueParent = msms.getActualParentIon();
		
		if(trueParent != null) {
			
			double parentIntensity = msms.getTopIntensityForMzRange(
					new edu.umich.med.mrc2.datoolbox.utils.Range(
							trueParent.getMz() - 0.1d, trueParent.getMz() + 0.1d));
			if(parentIntensity == 0.0d)
				parentIntensity = msms.getTopIntensity() / 5.0d;
			
			parentSeries.add(trueParent.getMz(), parentIntensity);
		}				
		parentSet.addSeries(parentSeries);
		XYItemRenderer parentRenderer = new XYLineAndShapeRenderer(false, true);
		parentRenderer.setSeriesPaint(0, Color.RED);
		parentRenderer.setSeriesShape(0, new Ellipse2D.Double(-size/4.0, -size/4.0, size/2.0, size/2.0));
		((XYPlot) spectrumPlot.getPlot()).setRenderer(1, parentRenderer);
		((XYPlot) spectrumPlot.getPlot()).setDataset(1, parentSet);

		setPlotMargins(activeMsDataSet);
	}

	public void showTandemMsWithReference(TandemMassSpectrum msms, TandemMassSpectrum reference) {

		HeadToTaleMsDataSet dataSet = new HeadToTaleMsDataSet(msms, reference);
		double size = 16.0;
		XYPlot plot = (XYPlot) spectrumPlot.getPlot();

		//	Add MSMS points
		MassSpectrumRenderer msRenderer = spectrumPlot.getDefaultMsRenderer();
		msRenderer.setSeriesPaint(0, Color.BLACK);
		msRenderer.setSeriesPaint(1, Color.RED);
		plot.setRenderer(2, msRenderer);
		plot.setDataset(2, dataSet);

		//	Add parent ion
		XYSeriesCollection parentSet = new XYSeriesCollection();
		XYSeries parentSeries = new XYSeries("Parent ion");
		
		MsPoint trueParent = msms.getParent();
		if(trueParent == null)
			trueParent = msms.getActualParentIon();
		
		if(trueParent != null)
			parentSeries.add(trueParent.getMz(), 100.0d);
		parentSet.addSeries(parentSeries);
		XYItemRenderer parentRenderer = new XYLineAndShapeRenderer(false, true);
		parentRenderer.setSeriesPaint(0, Color.RED);
		parentRenderer.setSeriesShape(0, new Ellipse2D.Double(-size/4.0, -size/4.0, size/2.0, size/2.0));
		plot.setRenderer(1, parentRenderer);
		plot.setDataset(1, parentSet);

		ValueMarker marker = new ValueMarker(0.0d);
		marker.setPaint(Color.GRAY);
		plot.addRangeMarker(marker);
		spectrumPlot.restoreAutoBounds();
	}

	public void showTandemMsWithReference(TandemMassSpectrum instrumentSpectrum, MsMsLibraryFeature libFeature) {

		HeadToTaleMsDataSet dataSet = new HeadToTaleMsDataSet(instrumentSpectrum, libFeature);
		double size = 16.0;
		XYPlot plot = (XYPlot) spectrumPlot.getPlot();

		//	Add MSMS points
		MassSpectrumRenderer msRenderer = spectrumPlot.getDefaultMsRenderer();
		msRenderer.setSeriesPaint(0, Color.BLACK);
		msRenderer.setSeriesPaint(1, Color.RED);
		plot.setRenderer(2, msRenderer);
		plot.setDataset(2, dataSet);

		//	Add parent ion
		XYSeriesCollection parentSet = new XYSeriesCollection();
		XYSeries parentSeries = new XYSeries("Parent ion");
		MsPoint trueParent = instrumentSpectrum.getActualParentIon();
		if(trueParent != null)
			parentSeries.add(trueParent.getMz(), 100.0d);
		parentSet.addSeries(parentSeries);
		XYItemRenderer parentRenderer = new XYLineAndShapeRenderer(false, true);
		parentRenderer.setSeriesPaint(0, Color.RED);
		parentRenderer.setSeriesShape(0, new Ellipse2D.Double(-size/4.0, -size/4.0, size/2.0, size/2.0));
		plot.setRenderer(1, parentRenderer);
		plot.setDataset(1, parentSet);

		ValueMarker marker = new ValueMarker(0.0d);
		marker.setPaint(Color.GRAY);
		plot.addRangeMarker(marker);
		spectrumPlot.restoreAutoBounds();
	}

	public void showScan(IScan s) {
		
		spectrumPlot.removeAllDataSets();
		spectrumPlot.showScan(s);
	}
	
	public void setRawDataExaminerPanel(RawDataExaminerPanel rawDataExaminerPanel) {
		spectrumPlot.setRawDataExaminerPanel(rawDataExaminerPanel);
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		zoomToMSMSPrecursor = preferences.getBoolean(ZOOM_TO_MSMS_PRECURSOR, false);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFERENCES_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFERENCES_NODE);
		preferences.putBoolean(ZOOM_TO_MSMS_PRECURSOR, zoomToMSMSPrecursor);  
	}
}



















