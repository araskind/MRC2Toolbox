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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.Icon;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.Title;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYDataset;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSMatchType;
import edu.umich.med.mrc2.datoolbox.data.enums.MSMSScoringParameter;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.gui.datexp.dataset.MSMSFeatureInfoBundleDataSet;
import edu.umich.med.mrc2.datoolbox.gui.datexp.tooltip.MSMSFeatureInfoBundleTooltipGenerator;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.MSMSScoreColorRenderer;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class DockableMzRtMSMSPlotPanel extends DefaultSingleCDockable 
	implements ActionListener, ItemListener, ChartMouseListener, BackedByPreferences {

	private static final Icon bubbleIcon = GuiUtils.getIcon("bubble", 16);
	private DataExplorerPlotPanel plotPanel;
	private MzRtPlotToolbar toolbar;
	//	private MSMSExplorerPlotSettingsToolbar settingsToolbar;
	private MZRTPlotSettingsPanel mmzrtPlotSettingsPanel;
	private MSMSFeatureInfoBundleTooltipGenerator msmsFeatureInfoBundleTooltipGenerator;
	private DockableMRC2ToolboxPanel parentPanel;
	private Collection<MSFeatureInfoBundle>msFeatureInfoBundles;
	private TableRowSubset activeTableRowSubset;
	private Range rtRange, mzRange;
	private FeatureIdentificationMeasure plotColorOption;
	private IndeterminateProgressDialog idp;
	private static final Shape defaultShape = new Ellipse2D.Double(-3, -3, 6, 6);
	private Preferences preferences;
	private XYItemRenderer defaultRenderer;

	public DockableMzRtMSMSPlotPanel() {

		super("DockableMzRtMSMSPlotPanel", bubbleIcon, "MSMS features MZ/RT plot", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		plotPanel = new DataExplorerPlotPanel(DataExplorerPlotType.MSMS_MZ_RT);
		plotPanel.addChartMouseListener(this);
		plotPanel.setFeaturePlotPopupMenu(new MSMSFeaturePlotPopupMenu(this));
		add(plotPanel, BorderLayout.CENTER);

		toolbar = new MzRtPlotToolbar(plotPanel, this, this);
		add(toolbar, BorderLayout.NORTH);
//		settingsToolbar = new MSMSExplorerPlotSettingsToolbar(this, this);
//		add(settingsToolbar, BorderLayout.SOUTH);
		
		mmzrtPlotSettingsPanel = new MZRTPlotSettingsPanel(this, this, true);
		add(mmzrtPlotSettingsPanel, BorderLayout.EAST);
		
		msmsFeatureInfoBundleTooltipGenerator 
			= new MSMSFeatureInfoBundleTooltipGenerator();
	}
	
	private void createDefaultRendrer() {
		
		defaultRenderer = new XYLineAndShapeRenderer(false, true);
		defaultRenderer.setDefaultToolTipGenerator(msmsFeatureInfoBundleTooltipGenerator);
		defaultRenderer.setDefaultShape(defaultShape);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.REFRESH_MSMS_FEATURE_PLOT.getName()))
			refreshMSMSFeaturePlot();	
		
		if(command.equals(MainActionCommands.CREATE_NEW_FEATURE_COLLECTION_FROM_SELECTED.getName()))
			createNewMsmsFeatureCollectionFromSelected();	
		
		if(command.equals(MainActionCommands.ADD_SELECTED_TO_EXISTING_FEATURE_COLLECTION.getName()))
			addSelectedFeaturesToExistingMsMsFeatureCollection();	

		if(command.equals(MainActionCommands.FILTER_SELECTED_MSMS_FEATURES_IN_TABLE.getName()))
			filterSelectedFeaturesInTable();	
		
		if(command.equals(MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName()))
			setSidePanelVisible(false);
		
		if(command.equals(MainActionCommands.SHOW_CHART_SIDE_PANEL_COMMAND.getName()))
			setSidePanelVisible(true);
	}
	
	public void setSidePanelVisible(boolean b) {
		mmzrtPlotSettingsPanel.setVisible(b);
	}
	
	private void refreshMSMSFeaturePlot() {
		
		if(mmzrtPlotSettingsPanel == null)
			return;
		
		plotPanel.removeAllDataSets();
		if(mmzrtPlotSettingsPanel.getTableRowSubset() == null) {
			
			MsFeatureInfoBundleCollection fColl = 
					((IDWorkbenchPanel)parentPanel).getActiveFeatureCollection();
			if(fColl != null)
				msFeatureInfoBundles = fColl.getFeatures();
		}
		else {
			msFeatureInfoBundles = ((IDWorkbenchPanel)parentPanel).
					getMsMsFeatureBundles(mmzrtPlotSettingsPanel.getTableRowSubset());
		}
		if(msFeatureInfoBundles == null 
				|| msFeatureInfoBundles.isEmpty()) {
			MessageDialog.showWarningMsg(
					"No features available based on selected settings!", 
					this.getContentPane());
			return;
		}
		rtRange = mmzrtPlotSettingsPanel.getRtRange();
		mzRange = mmzrtPlotSettingsPanel.getMZRange();
				
		Collection<MSFeatureInfoBundle>mzrtFilteredMsFeatureInfoBundles = msFeatureInfoBundles;
		if(rtRange != null && rtRange.getAverage() > 0) {
			mzrtFilteredMsFeatureInfoBundles = 
					mzrtFilteredMsFeatureInfoBundles.stream().
					filter(b -> rtRange.contains(b.getRetentionTime())).
					collect(Collectors.toList());
		}
		if(mzRange != null && mzRange.getAverage() > 0) {
			
			mzrtFilteredMsFeatureInfoBundles = 
					mzrtFilteredMsFeatureInfoBundles.stream().
					filter(b -> Objects.nonNull(b.getMsFeature().getSpectrum())).
					filter(b -> Objects.nonNull(b.getMsFeature().getSpectrum().getExperimentalTandemSpectrum())).
					filter(b -> Objects.nonNull(b.getMsFeature().getSpectrum().getExperimentalTandemSpectrum().getParent())).
					filter(b -> mzRange.contains(b.getMsFeature().getSpectrum().getExperimentalTandemSpectrum().getParent().getMz())).
					collect(Collectors.toList());
		}
		if(mzrtFilteredMsFeatureInfoBundles.isEmpty()) {
			MessageDialog.showWarningMsg(
					"No features in the selected MZ / RT ranges!", 
					this.getContentPane());
			return;
		}
		plotColorOption = mmzrtPlotSettingsPanel.getFeatureIdentificationMeasure();
		CreateMSMSFeatureInfoBundleDataSetTask task = 
				new CreateMSMSFeatureInfoBundleDataSetTask(
						mzrtFilteredMsFeatureInfoBundles, plotColorOption);
		idp = new IndeterminateProgressDialog(
				"Creating new plot ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}
	
	class CreateMSMSFeatureInfoBundleDataSetTask extends LongUpdateTask {
		
		private Collection<MSFeatureInfoBundle>featurBundles;
		private FeatureIdentificationMeasure colorOption;
		
		public CreateMSMSFeatureInfoBundleDataSetTask(
				Collection<MSFeatureInfoBundle>featurBundles,
				FeatureIdentificationMeasure colorOption) {
			this.featurBundles = featurBundles;
			this.colorOption = colorOption;
		}

		@Override
		public Void doInBackground() {

			if(colorOption.equals(FeatureIdentificationMeasure.COLOR_BY_ID_LEVEL))
				colorByIdLevel();
			
			if(colorOption.equals(FeatureIdentificationMeasure.COLOR_BY_MSMS_MATCH_TYPE))
				colorByMatchType();
				
			if(colorOption.equals(FeatureIdentificationMeasure.COLOR_BY_NIST_SCORE)
					|| colorOption.equals(FeatureIdentificationMeasure.COLOR_BY_ENTROPY_SCORE)
					|| colorOption.equals(FeatureIdentificationMeasure.COLOR_BY_DOT_PRODUCT)
					|| colorOption.equals(FeatureIdentificationMeasure.COLOR_BY_PROBABILITY)) {
				colorByScore();
			}
			return null;
		}
		
		private void colorByScore() {

			MSMSFeatureInfoBundleDataSet dataSet = 
					new MSMSFeatureInfoBundleDataSet(featurBundles, colorOption);
			
			MSMSScoringParameter scoringParameter = lookupMSMSScoringParameter(colorOption);
			MSMSScoreColorRenderer scoreRenderer = new 
					MSMSScoreColorRenderer(dataSet, scoringParameter);
			scoreRenderer.createLookupPaintScale();
			
			for(int i=0; i<dataSet.getSeriesCount(); i++)
					scoreRenderer.setSeriesShape(i, defaultShape);
			
			scoreRenderer.setDefaultToolTipGenerator(msmsFeatureInfoBundleTooltipGenerator);
			scoreRenderer.setDefaultShape(defaultShape);
			((XYPlot) plotPanel.getPlot()).setDataset(dataSet);
			((XYPlot) plotPanel.getPlot()).setRenderer(scoreRenderer);
			addColorScale(scoreRenderer);
		}
		
		private void addColorScale(MSMSScoreColorRenderer scoreRenderer) {
			
			NumberAxis scaleAxis = new NumberAxis();
			scaleAxis.setRange(
					scoreRenderer.getDataRange().getMin(), 
					scoreRenderer.getDataRange().getMax());
			PaintScaleLegend psl = new PaintScaleLegend(
					scoreRenderer.getLookupPaintScale(), scaleAxis);
			psl.setAxisOffset(5.0);
			psl.setPosition(RectangleEdge.RIGHT);
			psl.setMargin(new RectangleInsets(5, 5, 5, 5));
			if(plotPanel.getChart().getSubtitleCount() > 0) {
				
				Title currentPsl = plotPanel.getChart().getSubtitle(0);
				if(currentPsl != null)
					plotPanel.getChart().removeSubtitle(currentPsl);
			}
			plotPanel.getChart().addSubtitle(psl);
		}
		
		private MSMSScoringParameter lookupMSMSScoringParameter(
				FeatureIdentificationMeasure plotColorOption) {
			
			if(plotColorOption.equals(FeatureIdentificationMeasure.COLOR_BY_NIST_SCORE))
				return MSMSScoringParameter.NIST_SCORE;
			else if(plotColorOption.equals(FeatureIdentificationMeasure.COLOR_BY_ENTROPY_SCORE))
				return MSMSScoringParameter.ENTROPY_SCORE;
			else if(plotColorOption.equals(FeatureIdentificationMeasure.COLOR_BY_DOT_PRODUCT))
				return MSMSScoringParameter.DOT_PRODUCT;
			else if(plotColorOption.equals(FeatureIdentificationMeasure.COLOR_BY_PROBABILITY))
				return MSMSScoringParameter.PROBABILITY;
			else
				return null;
		}

		private void colorByMatchType() {
			
			MSMSFeatureInfoBundleDataSet dataSet = 
					new MSMSFeatureInfoBundleDataSet(
							featurBundles, FeatureIdentificationMeasure.COLOR_BY_MSMS_MATCH_TYPE);			
			
			createDefaultRendrer();
			for(int i=0; i<dataSet.getSeriesCount(); i++) {
				
				String seriesName = (String)dataSet.getSeriesKey(i);
				if(seriesName.equals(MSMSFeatureInfoBundleDataSet.UNKNOWN_SERIES_NAME)) {
					defaultRenderer.setSeriesPaint(i, Color.GRAY);
					defaultRenderer.setSeriesShape(i, defaultShape);
				}						
				else {							
					MSMSMatchType mt = MSMSMatchType.getMSMSMatchTypeByUIName(seriesName);
					if(mt != null)
						defaultRenderer.setSeriesPaint(i, mt.getColorCode());
				}					
			}
			defaultRenderer.setDefaultToolTipGenerator(msmsFeatureInfoBundleTooltipGenerator);
			
			((XYPlot) plotPanel.getPlot()).setDataset(dataSet);
			((XYPlot) plotPanel.getPlot()).setRenderer(defaultRenderer);
			
			if(plotPanel.getChart().getSubtitleCount() > 0) {
				
				Title currentPsl = plotPanel.getChart().getSubtitle(0);
				if(currentPsl != null)
					plotPanel.getChart().removeSubtitle(currentPsl);
			}
		}
		
		private void colorByIdLevel() {
			
			MSMSFeatureInfoBundleDataSet dataSet = 
					new MSMSFeatureInfoBundleDataSet(
							featurBundles, FeatureIdentificationMeasure.COLOR_BY_ID_LEVEL);
			
			createDefaultRendrer();
			for(int i=0; i<dataSet.getSeriesCount(); i++) {
				
				String seriesName = (String)dataSet.getSeriesKey(i);
				if(seriesName.equals(MSMSFeatureInfoBundleDataSet.UNKNOWN_SERIES_NAME)) { 
					defaultRenderer.setSeriesPaint(i, Color.GRAY);
					defaultRenderer.setSeriesShape(i, defaultShape);
				}
				else if(seriesName.equals(MSMSFeatureInfoBundleDataSet.IDENTIFIED_WITHOUT_LEVEL_SERIES_NAME)) {
					defaultRenderer.setSeriesPaint(i, Color.BLACK);
					defaultRenderer.setSeriesShape(i, defaultShape);
				}
				else {
					MSFeatureIdentificationLevel level = IDTDataCache.getMSFeatureIdentificationLevelByName(seriesName);
					if(level != null)
						defaultRenderer.setSeriesPaint(i, level.getColorCode());
				}					
			}
			((XYPlot) plotPanel.getPlot()).setDataset(dataSet);
			((XYPlot) plotPanel.getPlot()).setRenderer(defaultRenderer);
			
			if(plotPanel.getChart().getSubtitleCount() > 0) {
				
				Title currentPsl = plotPanel.getChart().getSubtitle(0);
				if(currentPsl != null)
					plotPanel.getChart().removeSubtitle(currentPsl);
			}
		}
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		
		if(XYItemEntity.class.isAssignableFrom(event.getEntity().getClass())) {
			
			XYItemEntity xyitem=(XYItemEntity) event.getEntity();
			XYDataset dataset = (XYDataset)xyitem.getDataset();
			MSFeatureInfoBundle f = null;
			if(dataset instanceof MSMSFeatureInfoBundleDataSet) 		
				f = ((MSMSFeatureInfoBundleDataSet)dataset).getMsFeatureInfoBundle(
						xyitem.getSeriesIndex(), xyitem.getItem());
			
			if(f != null) {
				if(parentPanel instanceof IDWorkbenchPanel) {
					((IDWorkbenchPanel)parentPanel).selectMSMSFeature(f);
				}
			}
		}
	}

	@Override
	public void chartMouseMoved(ChartMouseEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void setParentPanel(DockableMRC2ToolboxPanel parentPanel) {
		this.parentPanel = parentPanel;
	}
	
	public synchronized void clearPanel() {
		plotPanel.removeAllDataSets();
		msFeatureInfoBundles = null;
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
				
//		if (e.getItem() instanceof TableRowSubset && e.getStateChange() == ItemEvent.SELECTED) {
//			
//			if(parentPanel instanceof IDWorkbenchPanel) {
//				
//				TableRowSubset subset = (TableRowSubset)e.getItem();
//				msFeatureInfoBundles = ((IDWorkbenchPanel)parentPanel).getMsMsFeatureBundles(subset);
//			}
//		}		
		if (e.getStateChange() == ItemEvent.SELECTED) 
			refreshMSMSFeaturePlot();
	}

	public TableRowSubset getActiveTableRowSubset() {
		return activeTableRowSubset;
	}

	public void setActiveTableRowSubset(TableRowSubset activeTableRowSubset) {
		this.activeTableRowSubset = activeTableRowSubset;
		mmzrtPlotSettingsPanel.setTableRowSubset(this.activeTableRowSubset);
	}

	public Range getRtRange() {
		return rtRange;
	}

	public void setRtRange(Range rtRange) {
		this.rtRange = rtRange;
		mmzrtPlotSettingsPanel.setRtRange(this.rtRange);
	}
	
	public Collection<MSFeatureInfoBundle>getSelectedFeatures(){
		
		Collection<MSFeatureInfoBundle>selectedFeatures = 
				new ArrayList<MSFeatureInfoBundle>();
		
		if(msFeatureInfoBundles == null || msFeatureInfoBundles .isEmpty())
			return selectedFeatures;
		
		if(plotPanel.getSelectedRtRange() != null && plotPanel.getSelectedMzRange() != null) {
			
			Range rtRange = plotPanel.getSelectedRtRange();
			Range mzRange = plotPanel.getSelectedMzRange();
			
			return msFeatureInfoBundles.stream().
				filter(b -> rtRange.contains(b.getRetentionTime())).
				filter(b -> Objects.nonNull(b.getMsFeature().
						getSpectrum().getExperimentalTandemSpectrum())).
				filter(b -> mzRange.contains(b.getMsFeature().getSpectrum().
						getExperimentalTandemSpectrum().getParent().getMz())).
				collect(Collectors.toList());
		}		
		return selectedFeatures;
	}
	
	private void createNewMsmsFeatureCollectionFromSelected() {
		
		Collection<MSFeatureInfoBundle>selected = getSelectedFeatures();
		if(selected.isEmpty()) {
			MessageDialog.showWarningMsg(
					"No features in highlighted region.", this.getContentPane());
			return;
		}
		if(parentPanel != null && parentPanel instanceof IDWorkbenchPanel) 		
			((IDWorkbenchPanel)parentPanel).
				createNewMsmsFeatureCollectionFromSelectedFeatures(selected);
		
//		MessageDialog.showWarningMsg(Integer.toString(selected.size()) + 
//				" features in highlighted region.", this.getContentPane());
	}
	
	private void addSelectedFeaturesToExistingMsMsFeatureCollection() {
		
		Collection<MSFeatureInfoBundle>selected = getSelectedFeatures();
		if(selected.isEmpty()) {
			MessageDialog.showWarningMsg(
					"No features in highlighted region.", this.getContentPane());
			return;
		}
		if(parentPanel != null && parentPanel instanceof IDWorkbenchPanel) 		
			((IDWorkbenchPanel)parentPanel).addSelectedFeaturesToExistingMsMsFeatureCollection(selected);
		
//		MessageDialog.showWarningMsg(Integer.toString(selected.size()) + 
//				" features in highlighted region.", this.getContentPane());
	}
	
	private void filterSelectedFeaturesInTable() {
		
		Collection<MSFeatureInfoBundle>selected = getSelectedFeatures();
		if(selected.isEmpty()) {
			MessageDialog.showWarningMsg(
					"No features in highlighted region.", this.getContentPane());
			return;
		}
		if(parentPanel instanceof IDWorkbenchPanel)
			((IDWorkbenchPanel)parentPanel).filterMSMSFeatures(selected);
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		mmzrtPlotSettingsPanel.loadPreferences();
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userNodeForPackage(this.getClass());
		mmzrtPlotSettingsPanel.savePreferences();
	}
}










