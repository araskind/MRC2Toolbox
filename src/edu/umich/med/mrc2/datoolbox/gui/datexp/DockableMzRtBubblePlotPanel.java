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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.Icon;

import org.apache.commons.lang.StringUtils;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.enums.DataScale;
import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.communication.FeatureSetListener;
import edu.umich.med.mrc2.datoolbox.gui.datexp.dataset.MsFeatureBubbleDataSet;
import edu.umich.med.mrc2.datoolbox.gui.datexp.tooltip.MsFeatureTooltipGenerator;
import edu.umich.med.mrc2.datoolbox.gui.fdata.FeatureDataPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.projectsetup.featurelist.SimpleFeatureSubsetDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class DockableMzRtBubblePlotPanel extends DefaultSingleCDockable 
	implements ActionListener, ItemListener, ChartMouseListener {

	private static final Icon bubbleIcon = GuiUtils.getIcon("bubble", 16);
	private DataExplorerPlotPanel plotPanel;
	private MzRtPlotToolbar toolbar;
	private MzRtPlotSettingsToolbar settingsToolbar;
	private MsFeatureTooltipGenerator msFeatureTooltipGenerator;
	private DockableMRC2ToolboxPanel parentPanel;
	private Collection<MsFeature>msFeatures;
	private SimpleFeatureSubsetDialog featureSubsetDialog;
	
	public DockableMzRtBubblePlotPanel() {

		super("DockableMzRtBubblePlotPanel", bubbleIcon, "MZ/RT bubble-plot", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		plotPanel = new DataExplorerPlotPanel(DataExplorerPlotType.MZRT);
		plotPanel.addChartMouseListener(this);
		plotPanel.setFeaturePlotPopupMenu(new MSFeaturePlotPopupMenu(this));
		add(plotPanel, BorderLayout.CENTER);

		toolbar = new MzRtPlotToolbar(plotPanel, this);
		add(toolbar, BorderLayout.NORTH);
		settingsToolbar = new MzRtPlotSettingsToolbar(plotPanel, this);
		add(settingsToolbar, BorderLayout.SOUTH);
		
		msFeatureTooltipGenerator 
			= new MsFeatureTooltipGenerator();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.CREATE_NEW_MS_FEATURE_SUBSET_FROM_SELECTED.getName()))
			showFeatureSetDialog(null);
		
		if(command.equals(MainActionCommands.ADD_SELECCTED_TO_EXISTING_MS_FEATURE_SUBSET.getName())) {
			
			DataAnalysisProject project = MRC2ToolBoxCore.getCurrentProject();
			Set<MsFeatureSet> subsets = 
					project.getUnlockedMsFeatureSetsForDataPipeline(project.getActiveDataPipeline());
			if(subsets.isEmpty()) {
				MessageDialog.showWarningMsg("No unlocked feature subsets, please create a new set.", this.getContentPane());
				return;
			}
			MsFeatureSet activeSubset = subsets.iterator().next();
			showFeatureSetDialog(activeSubset);			
		}
		if(command.equals(MainActionCommands.FILTER_SELECTED_MS_FEATURES_IN_TABLE.getName())) {
			
		}
		if(command.equals(MainActionCommands.FINISH_FEATURE_SUBSET_EDIT_COMMAND.getName()))
			finishSubsetEdit();		
	}
	
	private void showFeatureSetDialog(MsFeatureSet subset) {

		if(getSelectedFeatures().isEmpty())
			return;
		
		featureSubsetDialog = new SimpleFeatureSubsetDialog(
				subset, getSelectedFeatures(), this);
		featureSubsetDialog.setLocationRelativeTo(this.getContentPane());
		featureSubsetDialog.setVisible(true);
	}
	
	private void finishSubsetEdit() {

		DataAnalysisProject currentProject = MRC2ToolBoxCore.getCurrentProject();
		if(currentProject == null)
			return;
		
		DataPipeline activeDataPipeline = currentProject.getActiveDataPipeline();
		if(activeDataPipeline == null)
			return;
		
		Collection<String>errors = validateFeatureSubsetEdits();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), featureSubsetDialog);
			return;
		}		
		MsFeatureSet setToEdit = featureSubsetDialog.getSubset();
		if(setToEdit == null) {
			
			setToEdit = new MsFeatureSet(
					featureSubsetDialog.getSubsetName(), 
					featureSubsetDialog.getFeatures());
			setToEdit.addListener((FeatureSetListener) 
					MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.FEATURE_DATA));			
			setToEdit.addListener(MainWindow.getProjectDashBooard().getFeatureSubsetPanel());
			setToEdit.fireFeatureSetEvent(ParameterSetStatus.CREATED);
		}
		else {
			setToEdit.setName(featureSubsetDialog.getSubsetName());
			setToEdit.setFeatures(featureSubsetDialog.getFeatures());
			setToEdit.addListener((FeatureSetListener) 
					MRC2ToolBoxCore.getMainWindow().getPanel(PanelList.FEATURE_DATA));
			setToEdit.addListener(MainWindow.getProjectDashBooard().getFeatureSubsetPanel());
			setToEdit.fireFeatureSetEvent(ParameterSetStatus.CHANGED);
		}
		featureSubsetDialog.dispose();
	}
	
	private Collection<String>validateFeatureSubsetEdits(){
		
		Collection<String>errors = new ArrayList<String>();
		String newName = featureSubsetDialog.getSubsetName();
		if(newName.isEmpty()) 
			errors.add("Feature subset name has to be specified");
		else {
			DataAnalysisProject currentProject = MRC2ToolBoxCore.getCurrentProject();
			Set<MsFeatureSet> existingSubsets = 
					currentProject.getUnlockedMsFeatureSetsForDataPipeline(currentProject.getActiveDataPipeline());
			
			MsFeatureSet editedSubset = featureSubsetDialog.getSubset();
			if(featureSubsetDialog.getSubset() != null) {
				MsFeatureSet sameName = existingSubsets.stream().
						filter(s -> !s.equals(editedSubset)).
						filter(s -> s.getName().equalsIgnoreCase(newName)).
						findFirst().orElse(null);
				if(sameName != null)
					errors.add("A different subset named \"" + newName + "\" already exists");
			}
			else {
				MsFeatureSet sameName = existingSubsets.stream().
						filter(s -> s.getName().equalsIgnoreCase(newName)).
						findFirst().orElse(null);
				if(sameName != null)
					errors.add("A subset named \"" + newName + "\" already exists");
			}
		}
		if(featureSubsetDialog.getFeatures().isEmpty())
			errors.add("No features to add");
		
		return errors;
	}
	
	public Collection<MsFeature>getSelectedFeatures(){
		
		Collection<MsFeature>selectedFeatures = new ArrayList<MsFeature>();
		
		if(msFeatures == null || msFeatures .isEmpty())
			return selectedFeatures;
		
		if(plotPanel.getSelectedRtRange() != null && plotPanel.getSelectedMzRange() != null) {
			
			Range rtRange = plotPanel.getSelectedRtRange();
			Range mzRange = plotPanel.getSelectedMzRange();
			
			return msFeatures.stream().
				filter(b -> rtRange.contains(b.getRetentionTime())).
				filter(b -> mzRange.contains(b.getBasePeakMz())).
				collect(Collectors.toList());
		}		
		return selectedFeatures;
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getItem() instanceof DataScale && e.getStateChange() == ItemEvent.SELECTED)
			changeDataScale((DataScale)e.getItem());		
	}
	
	private void changeDataScale(DataScale newScale) {

		MsFeatureBubbleDataSet dataSet = null;
		try {
			dataSet = (MsFeatureBubbleDataSet)((XYPlot) plotPanel.getPlot()).getDataset(0);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		if(dataSet != null)
			dataSet.setDataScale(newScale);
	}
	
	public void setDataScale(DataScale newScale) {
		settingsToolbar.setDataScale(newScale);
	}
	
	public DataScale getDataScale() {
		return settingsToolbar.getDataScale();
	}

	public void loadFeatureCollection(String title, Collection<MsFeature>features) {

		this.msFeatures = features;
		plotPanel.removeAllDataSets();
		MsFeatureBubbleDataSet dataSet = 
				new MsFeatureBubbleDataSet(title, features, settingsToolbar.getDataScale());
		((XYPlot) plotPanel.getPlot()).setDataset(0, dataSet);
		((XYPlot) plotPanel.getPlot()).getRenderer(0).
				setDefaultToolTipGenerator(msFeatureTooltipGenerator);
	}

	@Override
	public void chartMouseClicked(ChartMouseEvent event) {
		
		if(event.getEntity() instanceof XYItemEntity) {
			
			XYItemEntity xyitem=(XYItemEntity) event.getEntity();
			XYDataset dataset = (XYDataset)xyitem.getDataset();
			MsFeature f = null;
			if(dataset instanceof MsFeatureBubbleDataSet) 		
				f = ((MsFeatureBubbleDataSet)dataset).getMsFeature(xyitem.getSeriesIndex(), xyitem.getItem());
			
			if(f != null) {
				if(parentPanel instanceof FeatureDataPanel) {
					((FeatureDataPanel)parentPanel).showFeatureData(f);
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
	}
}
