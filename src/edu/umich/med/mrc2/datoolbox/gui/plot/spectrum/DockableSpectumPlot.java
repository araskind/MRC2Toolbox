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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.prefs.Preferences;

import javax.swing.Icon;

import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.Marker;
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
import edu.umich.med.mrc2.datoolbox.data.DataFile;
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
import edu.umich.med.mrc2.datoolbox.main.RawDataManager;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import umich.ms.datatypes.LCMSData;
import umich.ms.datatypes.scan.IScan;

public class DockableSpectumPlot extends DefaultSingleCDockable implements ActionListener, BackedByPreferences {

	private static final Icon componentIcon = GuiUtils.getIcon("msms", 16);
	private static final Icon multipleIdsIcon = GuiUtils.getIcon("multipleIds", 16);
	private static final Icon uniqueIdsIcon = GuiUtils.getIcon("checkboxFull", 16);	
	protected static final Icon normalizeSpectrumIcon = GuiUtils.getIcon("scale", 16);
	protected static final Icon rawSpectrumIcon = GuiUtils.getIcon("noScale", 16);
		
	private LCMSPlotPanel spectrumPlot;
	private LCMSPlotToolbar msPlotToolbar;
	
	private Preferences preferences;
	private String PREFERENCES_NODE;
	private static final String ZOOM_TO_MSMS_PRECURSOR = "ZOOM_TO_MSMS_PRECURSOR";
	private static final String NORMALIZE_SPECTRA = "NORMALIZE_SPECTRA";
	private SimpleButtonAction zoomToMSMSPrecursorButton;
	private SimpleButtonAction toggleSpectraNormalizationButton;
	private boolean zoomToMSMSPrecursor;
	private boolean normalizeSpectra;
	private MsDataSet activeMsDataSet;
	private MsPlotType msPlotType;
	private static final double markerSize = 16.0;
	private static final Shape precursorTopMarker = new Ellipse2D.Double(
			-markerSize/4.0, -markerSize/4.0, markerSize/2.0, markerSize/2.0);	
	
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
		toggleSpectraNormalization(normalizeSpectra);
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
		
		if(normalizeSpectra) {
			
			toggleSpectraNormalizationButton = GuiUtils.setupButtonAction(
					MainActionCommands.SHOW_NORMALIZED_SPECTRA_COMMAND.getName(), 
					MainActionCommands.SHOW_RAW_SPECTRA_COMMAND.getName(), 
					normalizeSpectrumIcon, this);
		}
		else {
			toggleSpectraNormalizationButton = GuiUtils.setupButtonAction(
					MainActionCommands.SHOW_RAW_SPECTRA_COMMAND.getName(), 
					MainActionCommands.SHOW_NORMALIZED_SPECTRA_COMMAND.getName(), 
					rawSpectrumIcon, this);
		}
		actions.add(toggleSpectraNormalizationButton);

		actions.addSeparator();
		intern().setActionOffers(actions);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();

		if(command.equals(LCMSPlotPanel.TOGGLE_MS_HEAD_TO_TAIL_COMMAND)) {
			//	TODO
		}
		if(command.equals(MainActionCommands.ZOOM_TO_MSMS_PRECURSOR_COMMAND.getName()))
			togglePrecursorZoom(true);
		
		if(command.equals(MainActionCommands.SHOW_FULL_MS_RANGE_COMMAND.getName()))
			togglePrecursorZoom(false);	
		
		if(command.equals(MainActionCommands.SHOW_NORMALIZED_SPECTRA_COMMAND.getName()))
			toggleSpectraNormalization(true);
		
		if(command.equals(MainActionCommands.SHOW_RAW_SPECTRA_COMMAND.getName()))
			toggleSpectraNormalization(false);	
	}
	
	private void toggleSpectraNormalization(boolean norm) {

		normalizeSpectra = norm;
		if(normalizeSpectra) {
			
			toggleSpectraNormalizationButton.setText(
					MainActionCommands.SHOW_NORMALIZED_SPECTRA_COMMAND.getName());
			toggleSpectraNormalizationButton.setCommand(
					MainActionCommands.SHOW_RAW_SPECTRA_COMMAND.getName());
			toggleSpectraNormalizationButton.setIcon(normalizeSpectrumIcon);
		}
		else {
			toggleSpectraNormalizationButton.setText(
					MainActionCommands.SHOW_RAW_SPECTRA_COMMAND.getName());
			toggleSpectraNormalizationButton.setCommand(
					MainActionCommands.SHOW_NORMALIZED_SPECTRA_COMMAND.getName());
			toggleSpectraNormalizationButton.setIcon(rawSpectrumIcon);		
		}
		if(activeMsDataSet != null) {
			
			activeMsDataSet.setNormalized(normalizeSpectra);
			activeMsDataSet.fireDatasetChanged();
			setPlotMargins(activeMsDataSet);
		}
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

		removeAllDataSets();
		activeMsDataSet = msDataSet;
		activeMsDataSet.setNormalized(normalizeSpectra);
		MassSpectrumRenderer msRenderer = spectrumPlot.getDefaultMsRenderer();
		for (int i = 0; i < msDataSet.getSeriesCount(); i++)
			msRenderer.setSeriesPaint(i, MasterPlotPanel.getColor(i));

		((XYPlot) spectrumPlot.getPlot()).setRenderer(0, msRenderer);
		((XYPlot) spectrumPlot.getPlot()).setDataset(0, activeMsDataSet);
		
		setPlotMargins(activeMsDataSet);
	}

	private void setPlotMargins(MsDataSet msDataSet) {

		activeMsDataSet = msDataSet;	
		
		//	MZ axis
		edu.umich.med.mrc2.datoolbox.utils.Range massRange = activeMsDataSet.getMassRange();
		if(massRange.getSize() < 6) {
			Range plotMassRange = new Range(
					massRange.getAverage() - 3.0d * massRange.getSize(),
					massRange.getAverage() + 3.0d * massRange.getSize());
			((XYPlot) spectrumPlot.getPlot()).getDomainAxis().setRange(plotMassRange);
		}
		else {
			((XYPlot) spectrumPlot.getPlot()).getDomainAxis().setAutoRange(true);
		}
		//	Intensity axis
		XYPlot plot = ((XYPlot) spectrumPlot.getPlot());
		double border  = plot.getDataRange(plot.getRangeAxis()).getUpperBound() * 1.15;
		//	double border  = msDataSet.getIntensityRange().getMax() * 1.15;
		if(msDataSet instanceof HeadToTaleMsDataSet) {
			
			((XYPlot) spectrumPlot.getPlot()).getRangeAxis().
			setRange(new Range(-border, border));
		}
		else {
			((XYPlot) spectrumPlot.getPlot()).getRangeAxis().
				setRange(new Range(0.0d, border));
		}
	}

	public void showMsForPointCollection(
			Collection<MsPoint> pattern, String seriesLabel) {

		Set<MsPoint> pointSet = new TreeSet<MsPoint>(MsUtils.mzSorter);
		pointSet.addAll(pattern);
		activeMsDataSet = new MsDataSet(pointSet, seriesLabel);
		showMsDataSet(activeMsDataSet);
	}

	public void showMsForCluster(MsMsCluster selectedCluster) {

		activeMsDataSet = new MsDataSet(selectedCluster);
		showMsDataSet(activeMsDataSet);
	}

	public void showSimpleMsMs(List<SimpleMsMs> selectedFeatures) {

		activeMsDataSet = new MsDataSet(selectedFeatures);
		showMsDataSet(activeMsDataSet);
	}

	public void showMsForFeature(MsFeature cf) {

		activeMsDataSet = new MsDataSet(cf);
		showMsDataSet(activeMsDataSet);
		
		if(cf.getSpectrum() == null 
				|| cf.getSpectrum().getExperimentalTandemSpectrum() == null)
			return;

		addMSMSPrecursorMarker(cf.getSpectrum().getExperimentalTandemSpectrum());
	}
	
	private void addMSMSPrecursorMarker(TandemMassSpectrum msms) {
		
		if(msms.getIsolationWindow() != null 
				&& msms.getIsolationWindow().getSize() > 0.0d) {
			
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
		((XYPlot) spectrumPlot.getPlot()).getRangeAxis().
				setRange(new Range(0.0d, maxIntensity * 1.15));
	}

	public void showMsForFeatureList(
			Collection<MsFeature> featureList) {

		activeMsDataSet = new MsDataSet(featureList);
		showMsDataSet(activeMsDataSet);
	}

	public void showMsForLibraryFeature(LibraryMsFeature lt) {

		activeMsDataSet = new MsDataSet(lt);
		showMsDataSet(activeMsDataSet);
		
		if(lt.getSpectrum() == null 
				|| lt.getSpectrum().getExperimentalTandemSpectrum() == null)
			return;

		addMSMSPrecursorMarker(lt.getSpectrum().getExperimentalTandemSpectrum());
	}
	
	public void showLibraryTandemMs(MsMsLibraryFeature libFeature) {

		removeAllDataSets();
		Set<MsPoint> libraryPoints = new TreeSet<MsPoint>(MsUtils.mzSorter);
		libraryPoints.addAll(libFeature.getSpectrum());
		activeMsDataSet = new MsDataSet(libraryPoints);
		activeMsDataSet.setNormalized(normalizeSpectra);
		
		//	Add MSMS points
		MassSpectrumRenderer msRenderer = spectrumPlot.getDefaultMsRenderer();
		msRenderer.setSeriesPaint(0, Color.BLACK);
		((XYPlot) spectrumPlot.getPlot()).setRenderer(1, msRenderer);
		((XYPlot) spectrumPlot.getPlot()).setDataset(1, activeMsDataSet);

		//	Add parent ion
		XYSeriesCollection parentSet = new XYSeriesCollection();
		XYSeries parentSeries = new XYSeries("Parent ion");
		MsPoint parentIon = libFeature.getParent();
		if(parentIon != null) {
			
			activeMsDataSet.getMassRange().extendRange(parentIon.getMz());
			double parentIntensity = parentIon.getIntensity();
			if(normalizeSpectra)
				parentIntensity = parentIntensity / libFeature.getMaxRawIntensity() * 100.0d;
				
			parentSeries.add(parentIon.getMz(), parentIntensity);
			parentSet.addSeries(parentSeries);
			XYItemRenderer parentRenderer = new XYLineAndShapeRenderer(false, true);
			parentRenderer.setSeriesPaint(0, Color.RED);
			parentRenderer.setSeriesShape(0, precursorTopMarker);
			((XYPlot) spectrumPlot.getPlot()).setRenderer(0, parentRenderer);
			((XYPlot) spectrumPlot.getPlot()).setDataset(0, parentSet);
		}
		setPlotMargins(activeMsDataSet);
	}
	
	private MsPoint getParentIonForPlot(TandemMassSpectrum msms) {

		MsPoint parent = msms.getParent();
		if(parent == null)
			return null;
		
		if(normalizeSpectra) {
			return new MsPoint(
					parent.getMz(), 
					msms.getNormalizedParentIonIntensity());
		}
		else {
			return new MsPoint(
					parent.getMz(), 
					msms.getRawParentIonIntensity());
		}
	}

	public void showTandemMs(TandemMassSpectrum msms) {

		removeAllDataSets();
		activeMsDataSet = new MsDataSet(msms);
		activeMsDataSet.setNormalized(normalizeSpectra);

		//	Add MSMS points
		MassSpectrumRenderer msRenderer = spectrumPlot.getDefaultMsRenderer();
		msRenderer.setSeriesPaint(0, Color.BLACK);
		((XYPlot) spectrumPlot.getPlot()).setRenderer(1, msRenderer);
		((XYPlot) spectrumPlot.getPlot()).setDataset(1, activeMsDataSet);

		//	Add parent ion
		XYSeriesCollection parentSet = new XYSeriesCollection();
		XYSeries parentSeries = new XYSeries("Parent ion");		
		MsPoint parentIon = getParentIonForPlot(msms);
		if(parentIon != null) {
			
			activeMsDataSet.getMassRange().extendRange(parentIon.getMz());
			parentSeries.add(parentIon.getMz(), parentIon.getIntensity());					
			parentSet.addSeries(parentSeries);
			XYItemRenderer parentRenderer = new XYLineAndShapeRenderer(false, true);
			parentRenderer.setSeriesPaint(0, Color.RED);
			parentRenderer.setSeriesShape(0, precursorTopMarker);
			((XYPlot) spectrumPlot.getPlot()).setRenderer(0, parentRenderer);
			((XYPlot) spectrumPlot.getPlot()).setDataset(0, parentSet);
		}
		setPlotMargins(activeMsDataSet);
	}

	public void showTandemMsWithReference(
			TandemMassSpectrum msms, 
			TandemMassSpectrum reference) {

		removeAllDataSets();
		activeMsDataSet = 
				new HeadToTaleMsDataSet(msms, reference);
		finalizeTandemMsWithReferencePlotSetup(
				(HeadToTaleMsDataSet) activeMsDataSet, 
				msms.getNormalisedParentIon(), 
				reference.getNormalisedParentIon());
	}

	public void showTandemMsWithReference(
			TandemMassSpectrum instrumentSpectrum, 
			MsMsLibraryFeature libFeature) {

		activeMsDataSet = 
				new HeadToTaleMsDataSet(instrumentSpectrum, libFeature);	
		finalizeTandemMsWithReferencePlotSetup(
				(HeadToTaleMsDataSet) activeMsDataSet, 
				instrumentSpectrum.getNormalisedParentIon(), 
				libFeature.getNormalisedParentIon());
	}
	
	private void finalizeTandemMsWithReferencePlotSetup(
			HeadToTaleMsDataSet dataSet,
			MsPoint featureParentIon,
			MsPoint referenceParentIon) {
		
		XYPlot plot = (XYPlot) spectrumPlot.getPlot();

		//	Add MSMS points
		MassSpectrumRenderer msRenderer = spectrumPlot.getDefaultMsRenderer();
		msRenderer.setSeriesPaint(0, Color.BLACK);
		msRenderer.setSeriesPaint(1, Color.RED);
		plot.setRenderer(1, msRenderer);
		plot.setDataset(1, dataSet);
		
		//	Add feature and reference parent ions
		XYSeriesCollection parentSet = new XYSeriesCollection();

		if(featureParentIon != null) {
			dataSet.getMassRange().extendRange(featureParentIon.getMz());
			XYSeries parentSeries = new XYSeries("Feature parent ion");
			parentSeries.add(featureParentIon.getMz(), featureParentIon.getIntensity());			
			parentSet.addSeries(parentSeries);
		}	
		if(referenceParentIon != null) {
			dataSet.getMassRange().extendRange(referenceParentIon.getMz());
			XYSeries refParentSeries = new XYSeries("Reference parent ion");
			refParentSeries.add(referenceParentIon.getMz(), -referenceParentIon.getIntensity());			
			parentSet.addSeries(refParentSeries);
		}
		if(parentSet.getSeriesCount() > 0) {
			
			XYItemRenderer parentRenderer = new XYLineAndShapeRenderer(false, true);
			
			int featureIndex = parentSet.getSeriesIndex("Feature parent ion");
			if(featureIndex >= 0) {
				parentRenderer.setSeriesPaint(featureIndex, Color.RED);
				parentRenderer.setSeriesShape(featureIndex, precursorTopMarker);
			}
			int refIndex = parentSet.getSeriesIndex("Reference parent ion");
			if(refIndex >= 0) {
				parentRenderer.setSeriesPaint(refIndex, Color.BLACK);
				parentRenderer.setSeriesShape(refIndex, precursorTopMarker);
			}
			plot.setRenderer(0, parentRenderer);
			plot.setDataset(0, parentSet);
		}
		ValueMarker marker = new ValueMarker(0.0d);
		marker.setPaint(Color.GRAY);
		plot.addRangeMarker(marker);
//		spectrumPlot.restoreAutoBounds();
//
//		((XYPlot) spectrumPlot.getPlot()).getRangeAxis().
//			setRange(new Range(
//					dataSet.getIntensityRange().getMin() * 1.15, 
//					dataSet.getIntensityRange().getMax() * 1.15));
		setPlotMargins(dataSet);
	}
		
	/*
	 * Mass spectra display
	 */
	
	public void showScan(IScan s) {
		
		spectrumPlot.removeAllDataSets();
		activeMsDataSet = new MsDataSet(s);
		activeMsDataSet.setNormalized(normalizeSpectra);
		
		((XYPlot) spectrumPlot.getPlot()).setRenderer(0, spectrumPlot.getDefaultMsRenderer());
		((XYPlot) spectrumPlot.getPlot()).setDataset(0, activeMsDataSet);
				
		//	Mark precursor ranges or individual precursors
		//	Highest MS level
		if(s.getChildScans() == null || s.getChildScans().isEmpty())
			addParentIonDataSeriesToHighestLevelMSMSPlot(s);
		else 	//	Show precursors in parent scan
			addParentIonDataSeriesToParentMSMSPlot(s);		
		
		setPlotMargins(activeMsDataSet);
	}
	
	private void addParentIonDataSeriesToParentMSMSPlot(IScan s) {
		
		DataFile df = spectrumPlot.getRawDataExaminerPanel().getDataFileForScan(s);
		if(df == null)
			return;

		LCMSData data = RawDataManager.getRawData(df);
		if(data == null) 
			return;
			
		XYSeriesCollection parentSet = new XYSeriesCollection();
		XYSeries parentSeries = new XYSeries("Parent ions");
		XYItemRenderer parentRenderer = new XYLineAndShapeRenderer(false, true);
		parentRenderer.setSeriesPaint(0, Color.RED);
		parentRenderer.setSeriesShape(0, precursorTopMarker);
		for(Integer child : s.getChildScans()) {
			
			IScan childScan = data.getScans().getScanByNum(child);
			if(childScan.getPrecursor() == null)
				continue;
			
			Double precursorMz = null;
			if (childScan.getPrecursor().getMzRangeStart() != null 
					&& childScan.getPrecursor().getMzRangeEnd() != null) {
				
				//	Highlight window
		        Marker isolationWindow = 
	        		new IntervalMarker(
        				childScan.getPrecursor().getMzRangeStart(), 
        				childScan.getPrecursor().getMzRangeEnd(), 
        				Color.RED, new BasicStroke( 2.0f ), null, null, 0.5f );
		        isolationWindow.setPaint( Color.RED );
		        ((XYPlot) spectrumPlot.getPlot()).addDomainMarker(isolationWindow);					
				
			} else if (childScan.getPrecursor().getMzTarget() != null) {
				precursorMz = childScan.getPrecursor().getMzTarget();
			} else {
				if (childScan.getPrecursor().getMzTargetMono() != null) {
					precursorMz = childScan.getPrecursor().getMzTargetMono();
				}
			}
			if(precursorMz != null) {
				
				double intensity = 0.0;
				Integer precursorIndex = s.getSpectrum().findClosestMzIdx(precursorMz);
				if(precursorIndex != null)
					intensity = s.getSpectrum().getIntensities()[precursorIndex];
				else
					intensity = s.getBasePeakIntensity() / 5.0d;
				
				MsPoint trueParent = new MsPoint(precursorMz, intensity);
				parentSeries.add(trueParent.getMz(), trueParent.getIntensity());			
			}
		}
		if(parentSeries.getItemCount() > 0) {
			parentSet.addSeries(parentSeries);
			((XYPlot) spectrumPlot.getPlot()).setRenderer(1, parentRenderer);
			((XYPlot) spectrumPlot.getPlot()).setDataset(1, parentSet);
		}
	}

	public void addParentIonDataSeriesToHighestLevelMSMSPlot(IScan s) {

		if(s.getPrecursor() != null) {
			
			Double precursorMz = null;
			if (s.getPrecursor().getMzRangeStart() != null && s.getPrecursor().getMzRangeEnd() != null) {
				
				//	Highlight window
		        Marker isolationWindow = 
	        		new IntervalMarker(
        				s.getPrecursor().getMzRangeStart(), 
        				s.getPrecursor().getMzRangeEnd(), 
        				Color.RED, new BasicStroke( 2.0f ), null, null, 0.5f );
		        isolationWindow.setPaint( Color.RED );
		        ((XYPlot) spectrumPlot.getPlot()).addDomainMarker(isolationWindow);					
				return;
				
			} else if (s.getPrecursor().getMzTarget() != null) {
				precursorMz = s.getPrecursor().getMzTarget();
			} else {
				if (s.getPrecursor().getMzTargetMono() != null) {
					precursorMz = s.getPrecursor().getMzTargetMono();
				}
			}
			if(precursorMz != null) {
				//	Add parent ion
				XYSeriesCollection parentSet = new XYSeriesCollection();
				XYSeries parentSeries = new XYSeries("Parent ion");
				
				double intensity = 0.0;
				Integer precursorIndex = s.getSpectrum().findClosestMzIdx(precursorMz);
				if(precursorIndex != null)
					intensity = s.getSpectrum().getIntensities()[precursorIndex];
				else
					intensity = s.getBasePeakIntensity() / 5.0d;
				
				MsPoint trueParent = new MsPoint(precursorMz, intensity);
				parentSeries.add(trueParent.getMz(), trueParent.getIntensity());
				parentSet.addSeries(parentSeries);
				XYItemRenderer parentRenderer = new XYLineAndShapeRenderer(false, true);
				parentRenderer.setSeriesPaint(0, Color.RED);
				parentRenderer.setSeriesShape(0, precursorTopMarker);
				((XYPlot) spectrumPlot.getPlot()).setRenderer(1, parentRenderer);
				((XYPlot) spectrumPlot.getPlot()).setDataset(1, parentSet);
			}
		}
	}

	public void setRawDataExaminerPanel(RawDataExaminerPanel rawDataExaminerPanel) {
		spectrumPlot.setRawDataExaminerPanel(rawDataExaminerPanel);
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		zoomToMSMSPrecursor = preferences.getBoolean(ZOOM_TO_MSMS_PRECURSOR, false);
		normalizeSpectra = preferences.getBoolean(NORMALIZE_SPECTRA, false);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFERENCES_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFERENCES_NODE);
		preferences.putBoolean(ZOOM_TO_MSMS_PRECURSOR, zoomToMSMSPrecursor);  
		preferences.putBoolean(NORMALIZE_SPECTRA, normalizeSpectra);  
	}
}


















