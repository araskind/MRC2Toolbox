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
import java.util.stream.Collectors;

import javax.swing.Icon;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MSFeatureIdentificationLevel;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.gui.datexp.dataset.MSMSFeatureInfoBundleDataSet;
import edu.umich.med.mrc2.datoolbox.gui.datexp.tooltip.MSMSFeatureInfoBundleTooltipGenerator;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class DockableMzRtMSMSPlotPanel extends DefaultSingleCDockable 
	implements ActionListener, ItemListener, ChartMouseListener {

	private static final Icon bubbleIcon = GuiUtils.getIcon("bubble", 16);
	private DataExplorerPlotPanel plotPanel;
	private MzRtPlotToolbar toolbar;
	private MSMSExplorerPlotSettingsToolbar settingsToolbar;
	private MSMSFeatureInfoBundleTooltipGenerator msmsFeatureInfoBundleTooltipGenerator;
	private DockableMRC2ToolboxPanel parentPanel;
	private Collection<MsFeatureInfoBundle>msFeatureInfoBundles;
	private TableRowSubset activeTableRowSubset;
	private Range rtRange;
	private IndeterminateProgressDialog idp;
	private static final Shape defaultShape = new Ellipse2D.Double(-3, -3, 6, 6);

	public DockableMzRtMSMSPlotPanel() {

		super("DockableMzRtMSMSPlotPanel", bubbleIcon, "MSMS features MZ/RT plot", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		plotPanel = new DataExplorerPlotPanel(DataExplorerPlotType.MSMS_MZ_RT);
		plotPanel.addChartMouseListener(this);
		plotPanel.setFeaturePlotPopupMenu(new MSMSFeaturePlotPopupMenu(this));
		add(plotPanel, BorderLayout.CENTER);

		toolbar = new MzRtPlotToolbar(plotPanel, this);
		add(toolbar, BorderLayout.NORTH);
		settingsToolbar = new MSMSExplorerPlotSettingsToolbar(this, this);
		add(settingsToolbar, BorderLayout.SOUTH);
		
		msmsFeatureInfoBundleTooltipGenerator 
			= new MSMSFeatureInfoBundleTooltipGenerator();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.REFRESH_MSMS_FEATURE_PLOT.getName()))
			refreshMSMSFeaturePlot();	
		
		if(command.equals(MainActionCommands.CREATE_NEW_MSMS_FEATURE_COLLECTION_FROM_SELECTED.getName()))
			createNewMsmsFeatureCollectionFromSelected();	
		
		if(command.equals(MainActionCommands.ADD_SELECTED_TO_EXISTING_MSMS_FEATURE_COLLECTION.getName()))
			addSelectedFeaturesToExistingMsMsFeatureCollection();	

		if(command.equals(MainActionCommands.FILTER_SELECTED_MSMS_FEATURES_IN_TABLE.getName()))
			filterSelectedFeaturesInTable();		
	}
	
	private void refreshMSMSFeaturePlot() {
		
		plotPanel.removeAllDataSets();
//		if(msFeatureInfoBundles == null)
		
			msFeatureInfoBundles = ((IDWorkbenchPanel)parentPanel).
				getMsMsFeatureBundles(settingsToolbar.getTableRowSubset());
		
		if(msFeatureInfoBundles == null || msFeatureInfoBundles.isEmpty()) {
			MessageDialog.showWarningMsg("No features available based on selected settings!", this.getContentPane());
			return;
		}
		rtRange = settingsToolbar.getRtRange();
		Collection<MsFeatureInfoBundle>rtFilteredMsFeatureInfoBundles = msFeatureInfoBundles;
		if(rtRange != null && rtRange.getAverage() > 0) {
			rtFilteredMsFeatureInfoBundles = 
					msFeatureInfoBundles.stream().
					filter(b -> rtRange.contains(b.getRetentionTime())).
					collect(Collectors.toList());
		}
		if(rtFilteredMsFeatureInfoBundles.isEmpty()) {
			MessageDialog.showWarningMsg("No features in the selected RT range!", this.getContentPane());
			return;
		}
		CreateMSMSFeatureInfoBundleDataSetTask task = 
				new CreateMSMSFeatureInfoBundleDataSetTask(rtFilteredMsFeatureInfoBundles);
		idp = new IndeterminateProgressDialog("Creating new plot ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}
	
	class CreateMSMSFeatureInfoBundleDataSetTask extends LongUpdateTask {
		
		private Collection<MsFeatureInfoBundle>featurBundles;
		
		public CreateMSMSFeatureInfoBundleDataSetTask(
				Collection<MsFeatureInfoBundle>featurBundles) {
			this.featurBundles = featurBundles;
		}

		@Override
		public Void doInBackground() {

			try {
				MSMSFeatureInfoBundleDataSet dataSet = 
						new MSMSFeatureInfoBundleDataSet(featurBundles);
				((XYPlot) plotPanel.getPlot()).setDataset(0, dataSet);
				XYItemRenderer renderer = ((XYPlot) plotPanel.getPlot()).getRenderer(0);
				renderer.setDefaultShape(defaultShape);
						
				for(int i=0; i<dataSet.getSeriesCount(); i++) {
					
					String seriesName = (String)dataSet.getSeriesKey(i);
					if(seriesName.equals(MSMSFeatureInfoBundleDataSet.UNKNOWN_SERIES_NAME)) 
						renderer.setSeriesPaint(i, Color.GRAY);
					
					else if(seriesName.equals(MSMSFeatureInfoBundleDataSet.IDENTIFIED_WITHOUT_LEVEL_SERIES_NAME))
						renderer.setSeriesPaint(i, Color.BLACK);
					
					else {
						MSFeatureIdentificationLevel level = IDTDataCash.getMSFeatureIdentificationLevelByName(seriesName);
						if(level != null)
							renderer.setSeriesPaint(i, level.getColorCode());
					}					
				}
				renderer.setDefaultToolTipGenerator(msmsFeatureInfoBundleTooltipGenerator);
			}
			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		
		if(event.getEntity().getClass().isAssignableFrom(XYItemEntity.class)) {
			
			XYItemEntity xyitem=(XYItemEntity) event.getEntity();
			XYDataset dataset = (XYDataset)xyitem.getDataset();
			MsFeatureInfoBundle f = null;
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
	
	public void clearPanel() {
		plotPanel.removeAllDataSets();
		msFeatureInfoBundles = null;
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
				
		if (e.getItem() instanceof TableRowSubset && e.getStateChange() == ItemEvent.SELECTED) {
			
			if(parentPanel instanceof IDWorkbenchPanel) {
				
				TableRowSubset subset = (TableRowSubset)e.getItem();
				msFeatureInfoBundles = ((IDWorkbenchPanel)parentPanel).getMsMsFeatureBundles(subset);
			}
		}		
	}

	public TableRowSubset getActiveTableRowSubset() {
		return activeTableRowSubset;
	}

	public void setActiveTableRowSubset(TableRowSubset activeTableRowSubset) {
		this.activeTableRowSubset = activeTableRowSubset;
		settingsToolbar.setTableRowSubset(this.activeTableRowSubset);
	}

	public Range getRtRange() {
		return rtRange;
	}

	public void setRtRange(Range rtRange) {
		this.rtRange = rtRange;
		settingsToolbar.setRtRange(this.rtRange);
	}
	
	public Collection<MsFeatureInfoBundle>getSelectedFeatures(){
		
		Collection<MsFeatureInfoBundle>selectedFeatures = 
				new ArrayList<MsFeatureInfoBundle>();
		
		if(msFeatureInfoBundles == null || msFeatureInfoBundles .isEmpty())
			return selectedFeatures;
		
		if(plotPanel.getSelectedRtRange() != null && plotPanel.getSelectedMzRange() != null) {
			
			Range rtRange = plotPanel.getSelectedRtRange();
			Range mzRange = plotPanel.getSelectedMzRange();
			
			return msFeatureInfoBundles.stream().
				filter(b -> rtRange.contains(b.getRetentionTime())).
				filter(b -> b.getMsFeature().getSpectrum().getExperimentalTandemSpectrum() != null).
				filter(b -> mzRange.contains(b.getMsFeature().getSpectrum().getExperimentalTandemSpectrum().getParent().getMz())).
				collect(Collectors.toList());
		}		
		return selectedFeatures;
	}
	
	private void createNewMsmsFeatureCollectionFromSelected() {
		
		Collection<MsFeatureInfoBundle>selected = getSelectedFeatures();
		if(selected.isEmpty()) {
			MessageDialog.showWarningMsg(
					"No features in highlighted region.", this.getContentPane());
			return;
		}
		if(parentPanel != null && parentPanel instanceof IDWorkbenchPanel) 		
			((IDWorkbenchPanel)parentPanel).createNewMsmsFeatureCollectionFromSelectedFeatures(selected);
		
//		MessageDialog.showWarningMsg(Integer.toString(selected.size()) + 
//				" features in highlighted region.", this.getContentPane());
	}
	
	private void addSelectedFeaturesToExistingMsMsFeatureCollection() {
		
		Collection<MsFeatureInfoBundle>selected = getSelectedFeatures();
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
		
		Collection<MsFeatureInfoBundle>selected = getSelectedFeatures();
		if(selected.isEmpty()) {
			MessageDialog.showWarningMsg(
					"No features in highlighted region.", this.getContentPane());
			return;
		}
		if(parentPanel instanceof IDWorkbenchPanel)
			((IDWorkbenchPanel)parentPanel).filterMSMSFeatures(selected);
	}
}










