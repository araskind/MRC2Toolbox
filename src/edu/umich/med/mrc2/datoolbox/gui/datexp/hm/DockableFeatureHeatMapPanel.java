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

package edu.umich.med.mrc2.datoolbox.gui.datexp.hm;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.Icon;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ujmp.core.Matrix;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.TableRowSubset;
import edu.umich.med.mrc2.datoolbox.gui.datexp.MZRTPlotParameterObject;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.heatmap.HeatChartType;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.vis.heatmap.JFHeatChart;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.FeatureHeatMapDataSet;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.project.Experiment;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class DockableFeatureHeatMapPanel extends DefaultSingleCDockable implements ActionListener, ItemListener {

	private static final Icon heatmapIcon = GuiUtils.getIcon("heatmap", 16);
	private MsFeatureSet featureSet;
	private JFHeatChart heatChart;
	private FeatureHeatchartToolbar toolbar;
	private FeatureHeatchartSettingsPanel chartSettingsPanel;
	private FeatureHeatMapDataSet heatMapDataSet;
	private MZRTPlotParameterObject lastUsedParameters;
	private Matrix featureSubsetMatrix;
	
	public DockableFeatureHeatMapPanel() {

		super("DockableFeatureHeatMapPanel", heatmapIcon, 
				"Feature heatmap", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);		
		setLayout(new BorderLayout(0, 0));
		heatChart = new JFHeatChart(HeatChartType.FeatureSetHeatmap);
		add(heatChart, BorderLayout.CENTER);
		
		toolbar = new FeatureHeatchartToolbar(heatChart, this);
		add(toolbar, BorderLayout.NORTH);
		
		chartSettingsPanel = new FeatureHeatchartSettingsPanel(this, this);
		add(chartSettingsPanel, BorderLayout.EAST);
	}
	
	public void loadSampleTypes(Experiment experiment) {
		chartSettingsPanel.loadSampleTypes(experiment);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.SHOW_CHART_SIDE_PANEL_COMMAND.getName()))
			chartSettingsPanel.setVisible(true);
				
		if(command.equals(MainActionCommands.HIDE_CHART_SIDE_PANEL_COMMAND.getName())) 
			chartSettingsPanel.setVisible(false);
		
		if(command.equals(MainActionCommands.REDRAW_HEAT_MAP_COMMAND.getName())) 
			initPlotRedraw(chartSettingsPanel);
	}
	
	public void clearPanel() {

		heatChart.removeAllDataSets();
		chartSettingsPanel.clearSampleGroups();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED)
			initPlotRedraw(e.getItem());
	}
	
	private void initPlotRedraw(Object redrawTrigger) {
		
		Collection<String>errors = validatePlotParameters();
		if(!errors.isEmpty()){
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), this.getContentPane());
			return;
		}
		RedrawPlotTask task = new RedrawPlotTask(redrawTrigger);
		IndeterminateProgressDialog idp = 
				new IndeterminateProgressDialog(
						"Updating plot ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	public void createFeatureHeatMap(MsFeatureSet subset) {	
		this.featureSet = subset;
		lastUsedParameters = chartSettingsPanel.getPlotParameters();
		initPlotRedraw(subset);		
	}
	
	class RedrawPlotTask extends LongUpdateTask {

		Object redrawTrigger;
		
		public RedrawPlotTask(Object redrawTrigger) {
			this.redrawTrigger = redrawTrigger;
		}

		@Override
		public Void doInBackground() {
			
//			if(redrawTrigger.equals(chartSettingsPanel)) {
//				redrawPlot();
//				return null;
//			}
			if(redrawTrigger instanceof MsFeatureSet) {
				loadFeatureCollection((MsFeatureSet)redrawTrigger);
				return null;
			}	
			else if (redrawTrigger instanceof TableRowSubset) {
				//	TODO;
				return null;
			}
			else {
				try {
					redrawPlot();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return null;
		}
	}
	
	private boolean recalculateDataSet() {
		
		MZRTPlotParameterObject currentParameters = chartSettingsPanel.getPlotParameters();
		
		if((lastUsedParameters.getMzRange() == null && currentParameters.getMzRange() != null) 
				|| (lastUsedParameters.getMzRange() != null && currentParameters.getMzRange() == null))
			return true;
		
		if((lastUsedParameters.getRtRange() == null && currentParameters.getRtRange() != null)
				|| (lastUsedParameters.getRtRange() != null && currentParameters.getRtRange() == null))
			return true;
		
		if(!currentParameters.getDataScale().equals(lastUsedParameters.getDataScale())
//				&& Boolean.compare(currentParameters.getDataScale().isDirectCalculation(), 
//						lastUsedParameters.getDataScale().isDirectCalculation()) != 0
						){
			return true;
		}	
		if(!currentParameters.getFileSortingOrder().equals(lastUsedParameters.getFileSortingOrder()))
			return true;
		
		if(!currentParameters.getFeatureSortingOrder().equals(lastUsedParameters.getFeatureSortingOrder()))
			return true;
		
		if(!CollectionUtils.isEqualCollection(
				currentParameters.getActiveSamples(), lastUsedParameters.getActiveSamples()))
			return true;
		
		return false;
	}
	
	private boolean recalculateColorScale() {
		
		MZRTPlotParameterObject currentParameters = chartSettingsPanel.getPlotParameters();
		
		if(!currentParameters.getDataScale().equals(lastUsedParameters.getDataScale()))
			return true;
		
		if(!currentParameters.getColorGradient().equals(lastUsedParameters.getColorGradient()))
			return true;
		
		if(!currentParameters.getColorScale().equals(lastUsedParameters.getColorScale()))
			return true;
		
		return false;
	}
	
	private Collection<String> validatePlotParameters() {
				
		Collection<String>errors = new ArrayList<String>();
		
		Collection<MsFeature>mzrtFilteredMsFeatures = featureSet.getFeatures();
		Range rtRange = chartSettingsPanel.getRtRange();		
						
		if(rtRange != null && rtRange.getAverage() > 0) {
			mzrtFilteredMsFeatures = 
					mzrtFilteredMsFeatures.stream().
					filter(b -> rtRange.contains(b.getRetentionTime())).
					collect(Collectors.toList());
		}
		Range mzRange = chartSettingsPanel.getMZRange();
		
		if(mzRange != null && mzRange.getAverage() > 0) {
			
			mzrtFilteredMsFeatures = 
					mzrtFilteredMsFeatures.stream().
					filter(b -> Objects.nonNull(b.getSpectrum())).
					filter(b -> Objects.nonNull(b.getSpectrum().getMonoisotopicPeak())).
					filter(b -> mzRange.contains(b.getSpectrum().getMonoisotopicPeak().getMz())).
					collect(Collectors.toList());
		}
		if(mzrtFilteredMsFeatures.isEmpty())
			errors.add("No features in the selected MZ / RT ranges.");
		
		if(chartSettingsPanel.getSelectedSamples().isEmpty())
			errors.add("No sample groups selected");
		
		return errors;
	}
	
	private void loadFeatureCollection(MsFeatureSet featureSet) {

		this.featureSet = featureSet;
		DataAnalysisProject experiment = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		chartSettingsPanel.loadSampleTypes(experiment);
		featureSubsetMatrix = 
				experiment.getDataMatrixForFeatureSetAndDesign(
						this.featureSet, 
						experiment.getExperimentDesign().getActiveDesignSubset(), 
						experiment.getActiveDataPipeline());
		lastUsedParameters = chartSettingsPanel.getPlotParameters();
		
		heatMapDataSet = new FeatureHeatMapDataSet(
				featureSubsetMatrix, lastUsedParameters);
		heatChart.showFeatureHeatMap(heatMapDataSet, lastUsedParameters);
	}
	
	private void redrawPlot() {

		MZRTPlotParameterObject currentParameters = 
				chartSettingsPanel.getPlotParameters();
		
		if(recalculateDataSet()) {
			
			lastUsedParameters = currentParameters;
			//heatMapDataSet.updateDataSetWithParameters(lastUsedParameters, true);			
			heatMapDataSet = new FeatureHeatMapDataSet(
					featureSubsetMatrix, lastUsedParameters);
			heatChart.showFeatureHeatMap(heatMapDataSet, lastUsedParameters);
		}
		else {
			if(recalculateColorScale()) {
				
				lastUsedParameters = currentParameters;
				heatChart.updatePaintScale(lastUsedParameters);
			}
		}
	}
}







