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

package edu.umich.med.mrc2.datoolbox.gui.datexp.mdef;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.swing.Icon;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.KendrickUnits;
import edu.umich.med.mrc2.datoolbox.gui.datexp.DataExplorerPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.datexp.DataExplorerPlotType;
import edu.umich.med.mrc2.datoolbox.gui.datexp.dataset.MsFeatureMassDefectDataSet;
import edu.umich.med.mrc2.datoolbox.gui.datexp.tooltip.MsFeatureTooltipGenerator;
import edu.umich.med.mrc2.datoolbox.gui.fdata.FeatureDataPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class DockableMzMassDefectPlotPanel extends DefaultSingleCDockable 
	implements ActionListener, ItemListener, ChartMouseListener {

	private static final Icon bubbleIcon = GuiUtils.getIcon("bubbleDelta", 16);
	private DataExplorerPlotPanel plotPanel;
	private MzMassDefectPlotToolbar toolbar;
	private MzMassDefectPlotSettingsToolbar settingsToolbar;
	private MsFeatureTooltipGenerator msFeatureTooltipGenerator;
	private DockableMRC2ToolboxPanel parentPanel;
	private Collection<MsFeature>msFeatures;
	private String featureSetTitle;
	private IndeterminateProgressDialog idp;
	
	public DockableMzMassDefectPlotPanel() {

		super("DockableMzMassDefectBubblePlotPanel", bubbleIcon, 
				"MZ/mass defect bubble-plot", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		plotPanel = new DataExplorerPlotPanel(DataExplorerPlotType.MASS_DEFECT_MZ);
		plotPanel.addChartMouseListener(this);
		add(plotPanel, BorderLayout.CENTER);

		toolbar = new MzMassDefectPlotToolbar(plotPanel);
		add(toolbar, BorderLayout.NORTH);		
		settingsToolbar = new MzMassDefectPlotSettingsToolbar(this, this);
		add(settingsToolbar, BorderLayout.SOUTH);
		
		msFeatureTooltipGenerator = new MsFeatureTooltipGenerator();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command  = e.getActionCommand();
		if(command.equals(MainActionCommands.RECALCULATE_MASS_DEFECTS_FOR_RT_RANGE.getName()))
			createMassDefectDataSet();
	}
	
	private void createMassDefectDataSet() {

		if(msFeatures == null || msFeatures.isEmpty()) {
			plotPanel.removeAllDataSets();
			return;
		}
		CreateMassDefectDataSetTask task = 
				new CreateMassDefectDataSetTask(featureSetTitle, msFeatures);
		idp = new IndeterminateProgressDialog(
				"Creating mass defect data set ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}
	
	class CreateMassDefectDataSetTask extends LongUpdateTask {

		private String featureSetTitle;
		private Collection<MsFeature>features;
				
		public CreateMassDefectDataSetTask(String featureSetTitle, Collection<MsFeature>features) {
			this.features = features;
			this.featureSetTitle = featureSetTitle;
		}
		@Override
		public Void doInBackground() {

			Range rtRange = settingsToolbar.getRtRange();
			if(rtRange != null && rtRange.getAverage() > 0) {
				features = features.stream().
						filter(f -> rtRange.contains(f.getRetentionTime())).
						collect(Collectors.toList());
			}
			try {
				plotPanel.removeAllDataSets();
				changeKendrickUnits(settingsToolbar.getKendrickUnits());
				MsFeatureMassDefectDataSet dataSet = new MsFeatureMassDefectDataSet(
						featureSetTitle, 
						features, 
						settingsToolbar.getKendrickUnits());
				((XYPlot) plotPanel.getPlot()).setDataset(0, dataSet);
				((XYPlot) plotPanel.getPlot()).getRenderer(0).setDefaultToolTipGenerator(
						msFeatureTooltipGenerator);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getItem() instanceof KendrickUnits && e.getStateChange() == ItemEvent.SELECTED)
			changeKendrickUnits((KendrickUnits)e.getItem());
	}
	
	private void changeKendrickUnits(KendrickUnits units) {

		MsFeatureMassDefectDataSet dataSet = null;
		XYPlot plot = (XYPlot) plotPanel.getPlot();
		try {
			dataSet = (MsFeatureMassDefectDataSet)plot.getDataset(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		if(dataSet != null)
			dataSet.setKendrickUnits(units);
			
		updatePlotAxisNames(units);
	}
	
	private void updatePlotAxisNames(KendrickUnits units) {
		
		XYPlot plot = (XYPlot) plotPanel.getPlot();
		if(units.equals(KendrickUnits.NONE)) {
			plot.getDomainAxis().setLabel(DataExplorerPlotPanel.DEFAULT_MASS_DEFECT_X_AXIS_TITLE);
			plot.getRangeAxis().setLabel(DataExplorerPlotPanel.DEFAULT_MASS_DEFECT_Y_AXIS_TITLE);
		}
		else if(units.equals(KendrickUnits.RELATIVE)) {
			plot.getDomainAxis().setLabel(DataExplorerPlotPanel.DEFAULT_MASS_DEFECT_X_AXIS_TITLE);
			plot.getRangeAxis().setLabel("Relative mass defect, ppm");
		}
		else {
			plot.getDomainAxis().setLabel("Kendrick nominal mass (" + units.getName() + ")");
			plot.getRangeAxis().setLabel("Kendrick mass defect (" + units.getName() + ")");
		}
	}
	
	public void setKendrickUnits(KendrickUnits newUnits) {
		settingsToolbar.setKendrickUnits(newUnits);
	}
	
	public KendrickUnits getKendrickUnits() {
		return settingsToolbar.getKendrickUnits();
	}

	public void setRtRange(Range rtRange) {
		settingsToolbar.setRtRange(rtRange);
	}
	
	public Range getRtRange() {
		return settingsToolbar.getRtRange();
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {

		XYItemEntity xyitem = null;				
		try {
			xyitem = (XYItemEntity) event.getEntity();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//	e.printStackTrace();
		}
		if(xyitem == null)
			return;
		
		XYDataset dataset = (XYDataset)xyitem.getDataset();
		MsFeature f = null;
		if(dataset instanceof MsFeatureMassDefectDataSet) 		
			f = ((MsFeatureMassDefectDataSet)dataset).getMsFeature(xyitem.getSeriesIndex(), xyitem.getItem());
		
		if(f != null) {
			if(parentPanel instanceof FeatureDataPanel) {
				((FeatureDataPanel)parentPanel).showFeatureData(f);
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

	public void setMsFeatures(Collection<MsFeature> msFeatures) {
		this.msFeatures = msFeatures;
	}

	public void setFeatureSetTitle(String featureSetTitle) {
		this.featureSetTitle = featureSetTitle;
	}
	
	public synchronized void clearPanel() {
		plotPanel.removeAllDataSets();
	}
}
