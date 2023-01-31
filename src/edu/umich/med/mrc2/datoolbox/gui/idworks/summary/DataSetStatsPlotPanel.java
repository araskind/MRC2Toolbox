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

package edu.umich.med.mrc2.datoolbox.gui.idworks.summary;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.statistics.SimpleHistogramDataset;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.ReferenceMsMsLibraryMatch;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSScoringParameter;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.PieChartDrawingSupplier;
import edu.umich.med.mrc2.datoolbox.gui.utils.ColorUtils;
import edu.umich.med.mrc2.datoolbox.utils.HistogramUtils;

public class DataSetStatsPlotPanel extends MasterPlotPanel {

		
	/**
	 * 
	 */
	private static final long serialVersionUID = 3359294404327470333L;
	private MsFeatureInfoBundleCollection activeFeatureCollection;
	private List<Color>pieChartColorList;

	public DataSetStatsPlotPanel(MsFeatureInfoBundleCollection activeFeatureCollection) {
		super();
		this.activeFeatureCollection = activeFeatureCollection;
		pieChartColorList = new ArrayList<Color>();
		initChart();
		createPieChart(DataSetSummaryPlotType.PERCENT_IDENTIFIED_ANNOTATED);		
	}
	
	@Override
	protected void initChart() {
		initPieChart();
	}
	
	private void initPieChart() {
		
		chart = ChartFactory.createPieChart(
				null, // chart title
				null, // data
				true, // include legend
				true, 
				false);
		chart.setBackgroundPaint(Color.white);
		setBasicPlotGui(chart.getPlot());
		setChart(chart);
	}
	
	private void initHistogram() {
		
		chart = ChartFactory.createHistogram(
				"MSMS match score distribution",
	            null, 
	            "Frequency", 
	            null,
	            PlotOrientation.VERTICAL, 
	            true, 
	            false,
	            false);
		chart.setBackgroundPaint(Color.white);
		setBasicPlotGui(chart.getPlot());
		setChart(chart);
	}
	
	private void initBarChart() {
		
		chart = ChartFactory.createStackedBarChart(
	            "Hits by library / match type",  // chart title
	            null,                  // domain axis label
	            "# of hits",                     // range axis label
	            null,                     // data
	            PlotOrientation.VERTICAL,    // the plot orientation
	            true,                        // legend
	            true,                        // tooltips
	            false                        // urls
	        );
		chart.setBackgroundPaint(Color.white);
		setBasicPlotGui(chart.getPlot());
		setChart(chart);
	}
	
	@Override
	protected void initAxes() {
		// TODO Auto-generated method stub

	}
	
	private void setBasicPlotGui(Plot newPlot) {
		
		newPlot.setBackgroundPaint(Color.white);
		
		if(newPlot instanceof XYPlot) {
			
			XYPlot xyPlot = (XYPlot)newPlot;
			
			xyPlot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
			xyPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
			xyPlot.setDomainGridlinePaint(GRID_COLOR);
			xyPlot.setRangeGridlinePaint(GRID_COLOR);
			xyPlot.setDomainCrosshairVisible(false);
			xyPlot.setRangeCrosshairVisible(false);
			xyPlot.setDomainPannable(true);
			xyPlot.setRangePannable(true);
		}
		if(newPlot instanceof CategoryPlot) {
			
			CategoryPlot catPlot = (CategoryPlot)newPlot;
			catPlot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
			catPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
			catPlot.setDomainGridlinePaint(GRID_COLOR);
			catPlot.setRangeGridlinePaint(GRID_COLOR);
			catPlot.setDomainCrosshairVisible(false);
			catPlot.setRangeCrosshairVisible(false);
			catPlot.setRangePannable(false);
		}
	}
	
	public void createPieChart(DataSetSummaryPlotType plotType) {
		
		if(!PiePlot.class.isAssignableFrom(chart.getPlot().getClass()))
			initPieChart();
			
		removeAllDataSets();
		chart.setTitle(plotType.getName());	
		if(plotType.equals(DataSetSummaryPlotType.PERCENT_IDENTIFIED_ANNOTATED) 
				|| plotType.equals(DataSetSummaryPlotType.BY_ID_LEVEL) 
				|| plotType.equals(DataSetSummaryPlotType.BY_MATCH_TYPE)) {
			
			PieDataset pds = createPieDataset(plotType);				
			PiePlot piePlot = (PiePlot) chart.getPlot();
			piePlot.setDataset(pds);
			piePlot.setShadowPaint(null);
			piePlot.setDrawingSupplier(new PieChartDrawingSupplier(pieChartColorList));			
		}
	}
	
	public void createLibraryHitBarChart() {
		
		if(!CategoryPlot.class.isAssignableFrom(chart.getPlot().getClass()))
			initBarChart();
		
		 List<ReferenceMsMsLibraryMatch> msmsMatches = activeFeatureCollection.getFeatures().stream().
			filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
			filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch())).
			map(f -> f.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch()).
			collect(Collectors.toList());

		 Collection<MSMSMatchType> matchTypes = msmsMatches.stream().
		 	map(m -> m.getMatchType()).
		 	collect(Collectors.toCollection(TreeSet::new));
		 pieChartColorList.clear();
		 for(MSMSMatchType mt : matchTypes)
			 pieChartColorList.add(MSMSMatchType.getColorCode(mt));
		 
		 Collection<String> libIdSet = msmsMatches.stream().
				map(m -> m.getMatchedLibraryFeature().getMsmsLibraryIdentifier()).
				collect(Collectors.toCollection(TreeSet::new));
		 String[]libNames = new String[libIdSet.size()];
		 int libCount = 0;
		 for(String libId : libIdSet) {
			 libNames[libCount] = IDTDataCash.getReferenceMsMsLibraryNameById(libId);
			 libCount++;
		 }
		 double[][] data = new double[matchTypes.size()][libIdSet.size()];
			 
		 int mtCount = 0;		 
		 for(MSMSMatchType mt : matchTypes) {
			 
			 libCount = 0;
			 for(String libId : libIdSet) {
				 
				 long numMatches = msmsMatches.stream().
				 	filter(m -> m.getMatchedLibraryFeature().getMsmsLibraryIdentifier().equals(libId)).
				 	filter(m -> m.getMatchType().equals(mt)).count();
				 
				 data[mtCount][libCount] = (double)numMatches;
				 libCount++;
			 }
			 mtCount++;
		 }
		 MSMSMatchType[] mtArray = matchTypes.toArray(new MSMSMatchType[matchTypes.size()]);
		 CategoryDataset ds =  DatasetUtils.createCategoryDataset(mtArray, libNames, data);
		 CategoryPlot barPlot = (CategoryPlot) chart.getPlot();
		 
		 StackedBarRenderer renderer = (StackedBarRenderer)barPlot.getRenderer();
		 renderer.setBarPainter(new StandardBarPainter());
			
		 barPlot.setDataset(ds);
		 barPlot.setDrawingSupplier(new PieChartDrawingSupplier(pieChartColorList));	
	}
	
	public void createScoreHistogramByMatchType(MSMSScoringParameter item) {

		if(!XYPlot.class.isAssignableFrom(chart.getPlot().getClass()))
			initHistogram();
		
		XYPlot histogram = (XYPlot) chart.getPlot();
		histogram.getDomainAxis().setLabel(item.getName());
		histogram.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
		pieChartColorList.clear();
		
		int dsCount = 0;
		for(MSMSMatchType mt : MSMSMatchType.values()) {
			
			double[] scores = activeFeatureCollection.getFeatures().stream().
					filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
					filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch())).
					filter(f -> f.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch().getMatchType().equals(mt)).
					mapToDouble(f -> f.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch().getScoreOfType(item)).	
					filter(s -> s > 0.0d).
					toArray();
				
			SimpleHistogramDataset dataSet = 
					HistogramUtils.calcHistogram(scores, mt.name(), false);

			if(dataSet != null) {
				
				XYBarRenderer renderer = new XYBarRenderer();
				renderer.setBarPainter(new StandardXYBarPainter());
				Color dsColor = ColorUtils.addTrasparency(MSMSMatchType.getColorCode(mt), 120);
				pieChartColorList.add(dsColor);
			    renderer.setDrawBarOutline(false);
			    renderer.setShadowVisible(false);		    

				histogram.setDataset(dsCount, dataSet);
				histogram.setRenderer(dsCount, renderer);
			}
			dsCount++;
		}
		histogram.setDrawingSupplier(new PieChartDrawingSupplier(pieChartColorList));
	}
		
	public void createScoreHistogram(MSMSScoringParameter item) {

		if(!XYPlot.class.isAssignableFrom(chart.getPlot().getClass()))
			initHistogram();
		
		XYPlot histogram = (XYPlot) chart.getPlot();
		histogram.getDomainAxis().setLabel(item.getName());
		
		XYBarRenderer renderer = (XYBarRenderer)histogram.getRenderer();
		renderer.setBarPainter(new StandardXYBarPainter());
		renderer.setDefaultPaint(Color.BLUE);		
		double[] scores = activeFeatureCollection.getFeatures().stream().
			filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
			filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch())).
			mapToDouble(f -> f.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch().getScoreOfType(item)).			
			toArray();

		
		SimpleHistogramDataset dataSet = 
			HistogramUtils.calcHistogram(scores, item.getName(), false);
		histogram.setDataset(dataSet);		 
	}
	
	private PieDataset createPieDataset(DataSetSummaryPlotType plotType) {

		DefaultPieDataset dataset = new DefaultPieDataset();
		pieChartColorList.clear();
		List<MSFeatureInfoBundle> identified = 
				activeFeatureCollection.getFeatures().stream().
					filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity())).
					collect(Collectors.toList());

		if (plotType.equals(DataSetSummaryPlotType.PERCENT_IDENTIFIED_ANNOTATED)) {
		 
			long annotated =  identified.stream().filter(f -> isFeatureManuallyAnnotated(f)).count();
			 
			dataset.setValue("Annotated", annotated);
			pieChartColorList.add(Color.GREEN);
			dataset.setValue("Tentative ID", identified.size() - annotated);
			pieChartColorList.add(IDTDataCash.getMSFeatureIdentificationLevelById("IDS002").getColorCode());
			dataset.setValue("Unknowns", activeFeatureCollection.getFeatures().size() - identified.size());
			pieChartColorList.add(Color.RED);
			return dataset;
		}
		if(plotType.equals(DataSetSummaryPlotType.BY_ID_LEVEL)) {

			Map<MSFeatureIdentificationLevel, Long> idCountsByLevel = identified.stream().
			 	filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity().getIdentificationLevel())).
			 	map(f -> f.getMsFeature().getPrimaryIdentity().getIdentificationLevel()).
				collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
			idCountsByLevel.entrySet().stream().
			 	forEach(c -> {
			 				dataset.setValue(c.getKey().getName(), c.getValue());
			 				pieChartColorList.add(c.getKey().getColorCode());
			 			}
			 		);
			return dataset;
		}
		if(plotType.equals(DataSetSummaryPlotType.BY_MATCH_TYPE)) {

			Map<MSMSMatchType, Long> idCountsByLevel = identified.stream().
			 	filter(f -> Objects.nonNull(f.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch())).
			 	map(f -> f.getMsFeature().getPrimaryIdentity().getReferenceMsMsLibraryMatch().getMatchType()).
				collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
			idCountsByLevel.entrySet().stream().
			 	forEach(c -> {
			 				dataset.setValue(c.getKey().name(), c.getValue());
			 				pieChartColorList.add(MSMSMatchType.getColorCode(c.getKey()));
			 			}
			 		);
			return dataset;
		}
		return null;
	}
	
	private boolean isFeatureManuallyAnnotated(MSFeatureInfoBundle f) {
		
		MsFeatureIdentity id = f.getMsFeature().getPrimaryIdentity();
		if(id.getIdentificationLevel() != null && !id.getIdentificationLevel().getId().equals("IDS002"))
			return true;
		
		if(f.getStandadAnnotations() != null && !f.getStandadAnnotations().isEmpty())
			return true;
		
		if(f.getMsFeature().getAnnotations() != null && !f.getMsFeature().getAnnotations().isEmpty())
			return true;
				
		return false;
	}

	@Override
	protected void initPlot() {
		
		if(chart.getPlot() instanceof XYPlot) {
			
			XYPlot xyPlot = (XYPlot)chart.getPlot();
			xyPlot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
			xyPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
			xyPlot.setDomainGridlinePaint(GRID_COLOR);
			xyPlot.setRangeGridlinePaint(GRID_COLOR);
			xyPlot.setDomainCrosshairVisible(false);
			xyPlot.setRangeCrosshairVisible(false);
			xyPlot.setDomainPannable(true);
			xyPlot.setRangePannable(true);
		}
		if(chart.getPlot() instanceof CategoryPlot) {
			
			CategoryPlot catPlot = (CategoryPlot)chart.getPlot();
			catPlot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
			catPlot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
			catPlot.setDomainGridlinePaint(GRID_COLOR);
			catPlot.setRangeGridlinePaint(GRID_COLOR);
			catPlot.setDomainCrosshairVisible(false);
			catPlot.setRangeCrosshairVisible(false);
			catPlot.setRangePannable(false);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void removeAllDataSets() {

		Plot activePlot = chart.getPlot();			
		if(activePlot == null)
			return;
			
		if(activePlot instanceof XYPlot) {
			
			XYPlot p = (XYPlot)activePlot;
			int count = p.getDatasetCount();
			for (int i = 0; i < count; i++)
				p.setDataset(i, null);	
			
			p.clearAnnotations();
		}
		if(activePlot instanceof CategoryPlot) {
			
			CategoryPlot p = (CategoryPlot)activePlot;
			int count = p.getDatasetCount();
			for (int i = 0; i < count; i++)
				p.setDataset(i, null);
			
			p.clearAnnotations();
		}		
		if(activePlot instanceof PiePlot)			
			((PiePlot)activePlot).setDataset(null);

		numberOfDataSets = 0;
	}
}
