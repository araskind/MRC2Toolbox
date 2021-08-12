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

import javax.swing.Icon;

import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.Range;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsMsCluster;
import edu.umich.med.mrc2.datoolbox.data.MsMsLibraryFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsMs;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.gui.plot.LCMSPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.LCMSPlotToolbar;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.PlotType;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.HeadToTaleMsDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.MsDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.MassSpectrumRenderer;
import edu.umich.med.mrc2.datoolbox.gui.rawdata.RawDataExaminerPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import umich.ms.datatypes.scan.IScan;

public class DockableSpectumPlot extends DefaultSingleCDockable implements ActionListener {

	private LCMSPlotPanel spectrumPlot;
	private LCMSPlotToolbar msPlotToolbar;

	private static final Icon componentIcon = GuiUtils.getIcon("msms", 16);
	private MsDataSet activeMsDataSet;	

	public DockableSpectumPlot(String id, String title) {

		super(id, componentIcon, title, null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));

		spectrumPlot = new LCMSPlotPanel(PlotType.SPECTRUM);
		add(spectrumPlot, BorderLayout.CENTER);

		msPlotToolbar = new LCMSPlotToolbar(spectrumPlot, this);
		add(msPlotToolbar, BorderLayout.NORTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getActionCommand().equals(LCMSPlotPanel.TOGGLE_MS_HEAD_TO_TAIL_COMMAND)) {

		}
	}

	public void removeAllDataSets() {
		spectrumPlot.removeAllDataSets();
	}
	
	public void clearPanel() {
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
		double maxIntensity = msDataSet.getHighestIntensity(activeMsDataSet.getMassRange());
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
		MsPoint trueParent = msms.getActualParentIon();
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
		MsPoint trueParent = msms.getActualParentIon();
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
}



















