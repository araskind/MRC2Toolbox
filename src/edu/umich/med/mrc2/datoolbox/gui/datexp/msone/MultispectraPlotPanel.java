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

package edu.umich.med.mrc2.datoolbox.gui.datexp.msone;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.ui.VerticalAlignment;
import org.jfree.data.Range;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.SimpleMsFeature;
import edu.umich.med.mrc2.datoolbox.data.compare.DataFileComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.enums.FileSortingOrder;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorGradient;
import edu.umich.med.mrc2.datoolbox.gui.plot.ColorScale;
import edu.umich.med.mrc2.datoolbox.gui.plot.LockedXYTextAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.plot.MasterPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.PlotType;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.HeadToTailMsDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.MsDataSet;
import edu.umich.med.mrc2.datoolbox.gui.plot.dataset.PlotDataSetUtils;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.LCMSPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.renderer.MassSpectrumRenderer;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.ColorCodingUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.ColorUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class MultispectraPlotPanel extends JPanel implements ActionListener, ItemListener, BackedByPreferences {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MultispectraPlotPanelToolbar toolbar;
	private JPanel plotPanel;
	private Map<Object,LCMSPlotPanel>objectPlotMap;
	private int numColumns;
	private int graphWidth;
	private int graphHeight;
	private boolean normalizeSpectra;
	private GridBagLayout gbl_plotPanel;
	
	private Preferences prefs;
	private static final String NUMBER_OF_COLUMNS = "NUMBER_OF_COLUMNS";

	public MultispectraPlotPanel() {
		super();
		setLayout(new BorderLayout(0,0));
		toolbar = new MultispectraPlotPanelToolbar(this);
		add(toolbar, BorderLayout.NORTH);
		plotPanel = new JPanel();
		plotPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		gbl_plotPanel = new GridBagLayout();
		plotPanel.setLayout(gbl_plotPanel);
		
		objectPlotMap = new LinkedHashMap<Object,LCMSPlotPanel>();
		
		graphWidth = 120;
		graphHeight = 200;
		normalizeSpectra = true;
		
		loadPreferences();
		//	createGraphGrid();
		
		add(new JScrollPane(plotPanel), BorderLayout.CENTER);
	}
	
//	private void createGraphGrid() {
//		
//		int numGraps = 20;
//		int numRows = 1;
//		if(numGraps > numColumns)
//			numRows = Math.floorDiv(numGraps, numColumns) + 1;
//
//		int count = 0;
//		int width = graphWidth * numColumns;
//		int height = graphHeight * numRows;
//		plotPanel.setPreferredSize(new Dimension(width, height));
//		
//		for(int i=0; i<numColumns; i++) {
//			
//			for(int j=0; j<numRows; j++) {
//				
//				count++;
//				String gn = "Graph# "  + Integer.toString(count);
//				LCMSPlotPanel spectrumPlot = new LCMSPlotPanel(PlotType.SPECTRUM);
//				((XYPlot)spectrumPlot.getChart().getPlot()).getRangeAxis().setLabel(null);
//
//				objectPlotMap.put(gn, spectrumPlot);
//				
//				GridBagConstraints gbc_spectrumPlot = new GridBagConstraints();
//				gbc_spectrumPlot.fill = GridBagConstraints.BOTH;
//				gbc_spectrumPlot.insets = new Insets(0, 0, 5, 5);
//				gbc_spectrumPlot.gridx = i;
//				gbc_spectrumPlot.gridy = j;
//				plotPanel.add(spectrumPlot, gbc_spectrumPlot);
//			}
//		}		
//		gbl_plotPanel.rowWeights = new double[numRows];
//		Arrays.fill(gbl_plotPanel.rowWeights, 1.0d);
//		gbl_plotPanel.columnWeights = new double[numColumns];
//		Arrays.fill(gbl_plotPanel.columnWeights, 1.0d);
//	}
	
	public void clearPanel() {
		
		removeAll();
		plotPanel = new JPanel();
		plotPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		gbl_plotPanel = new GridBagLayout();
		plotPanel.setLayout(gbl_plotPanel);		
		add(new JScrollPane(plotPanel), BorderLayout.CENTER);
		revalidate();
		repaint();
	}
	
	public void createSimpleMSFeatureSpectraPlot(
			MsFeature feature, 
			Map<DataFile, SimpleMsFeature> fileFeatureMap,
			FileSortingOrder sortingOrder,
			DataAnalysisProject currentExperiment,
			DataPipeline dataPipeline) {
		
		clearPanel();
		
		if (currentExperiment == null || currentExperiment.getExperimentDesign() == null 
				|| currentExperiment.getExperimentDesign().getSamples().isEmpty())
			return;
		
		TreeMap<DataFile, SimpleMsFeature> sortedFileFeatureMap = null;
		if(sortingOrder.equals(FileSortingOrder.NAME))
			sortedFileFeatureMap = new TreeMap<DataFile, SimpleMsFeature>(
					new DataFileComparator(SortProperty.Name));
		
		if(sortingOrder.equals(FileSortingOrder.TIMESTAMP))
			sortedFileFeatureMap = new TreeMap<DataFile, SimpleMsFeature>(
					new DataFileComparator(SortProperty.injectionTime));
		
		sortedFileFeatureMap.putAll(fileFeatureMap);

		Map<DataFile,String>fileTypeMap = PlotDataSetUtils.mapFilesBySampleType(
				currentExperiment,
				dataPipeline,
				currentExperiment.getExperimentDesign().getActiveDesignSubset());
		if(fileTypeMap == null)
			return;
		
		Map<DataFile,Color>colorMap = createColorMap(fileTypeMap, currentExperiment);
		
		int numGraps = sortedFileFeatureMap.size();
		int numRows = 1;
		if(numGraps > numColumns)
			numRows = Math.floorDiv(numGraps, numColumns) + 1;

		int count = 1;
		int width = graphWidth * numColumns;
		int height = graphHeight * numRows;
		plotPanel.setPreferredSize(new Dimension(width, height));
		
		int row = 0;
		int column = 0;
		
		LookupPaintScale qcLookupPaintScale =  ColorCodingUtils.createLookupPaintScale(
				new edu.umich.med.mrc2.datoolbox.utils.Range(0.0d, 100.0d), 
				ColorGradient.GREEN_RED, 
				ColorScale.LOGARITHMIC,
				10);
		
		for(Entry<DataFile, SimpleMsFeature> ff : sortedFileFeatureMap.entrySet()) {
			
			if(count % numColumns != 0)
				row = Math.floorDiv(count, numColumns);
			else
				row = (int)((count / numColumns) - 1);
				
			column = count - 1 - row * numColumns;
			
			LCMSPlotPanel spectrumPlot = new LCMSPlotPanel(PlotType.SPECTRUM);
			((XYPlot)spectrumPlot.getChart().getPlot()).getRangeAxis().setLabel(null);
			spectrumPlot.setBorder(BorderFactory.createLineBorder(colorMap.get(ff.getKey()), 2));
			spectrumPlot.hideLegend();
			TextTitle tt = new TextTitle(ff.getKey().getName(), 
					new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
			//	tt.setPaint(colorMap.get(ff.getKey()));
			tt.setVerticalAlignment(VerticalAlignment.TOP);
			spectrumPlot.getChart().addSubtitle(tt);
			
			if(ff.getValue() != null) {
				
				MsDataSet ds = new MsDataSet();
				ds.createDataSetFromSimpleMsFeature(ff.getValue());

				ds.setNormalized(normalizeSpectra);
				MassSpectrumRenderer msRenderer = spectrumPlot.getDefaultMsRenderer();
				for (int i = 0; i < ds.getSeriesCount(); i++)
					msRenderer.setSeriesPaint(i, MasterPlotPanel.getColor(i));

				((XYPlot) spectrumPlot.getPlot()).setRenderer(0, msRenderer);
				((XYPlot) spectrumPlot.getPlot()).setDataset(0, ds);
				
				setPlotMargins(spectrumPlot, ds);
				
				if(ff.getValue().getQualityScore() > 0) {
					
					Color qp = (Color)qcLookupPaintScale.getPaint(
							100.0d - ff.getValue().getQualityScore());
					LockedXYTextAnnotation ta = createLockedAnnotation(
							"Score: " + MRC2ToolBoxConfiguration.getPpmFormat().format(
									ff.getValue().getQualityScore()), 
							qp, TextAnchor.TOP_LEFT);
					((XYPlot)spectrumPlot.getChart().getPlot()).addAnnotation(ta);
				}
			}
			objectPlotMap.put(ff, spectrumPlot);
			
			GridBagConstraints gbc_spectrumPlot = new GridBagConstraints();
			gbc_spectrumPlot.fill = GridBagConstraints.BOTH;
			gbc_spectrumPlot.insets = new Insets(0, 0, 5, 5);
			gbc_spectrumPlot.gridx = column;
			gbc_spectrumPlot.gridy = row;
			plotPanel.add(spectrumPlot, gbc_spectrumPlot);
			
			count++;
		}
		gbl_plotPanel.rowWeights = new double[numRows];
		Arrays.fill(gbl_plotPanel.rowWeights, 1.0d);
		gbl_plotPanel.columnWeights = new double[numColumns];
		Arrays.fill(gbl_plotPanel.columnWeights, 1.0d);		
	}
	
	private void setPlotMargins(LCMSPlotPanel spectrumPlot, MsDataSet msDataSet) {
		
		//	MZ axis
		edu.umich.med.mrc2.datoolbox.utils.Range massRange = msDataSet.getMassRange();
		if(massRange.getSize() < 6 && massRange.getSize() > 0) {
			Range plotMassRange = new Range(
					massRange.getAverage() - 3.0d * massRange.getSize(),
					massRange.getAverage() + 3.0d * massRange.getSize());
			((XYPlot) spectrumPlot.getPlot()).getDomainAxis().setRange(plotMassRange);
		}
		else {
			((XYPlot) spectrumPlot.getPlot()).getDomainAxis().setAutoRange(true);
		}
		//	Intensity axis
		if(msDataSet.getIntensityRange() == null) {
			((XYPlot) spectrumPlot.getPlot()).getRangeAxis().setAutoRange(true);
			return;
		}
		XYPlot plot = ((XYPlot) spectrumPlot.getPlot());
		double border  = plot.getDataRange(plot.getRangeAxis()).getUpperBound() * 1.15;
		if(msDataSet instanceof HeadToTailMsDataSet) {
			
			((XYPlot) spectrumPlot.getPlot()).getRangeAxis().
			setRange(new Range(-border, border));
		}
		else {
			((XYPlot) spectrumPlot.getPlot()).getRangeAxis().
				setRange(new Range(0.0d, border));
		}
	}
	
	private Map<DataFile, Color> createColorMap(
			Map<DataFile, String> fileTypeMap,
			DataAnalysisProject currentExperiment) {
		Map<DataFile, Color>colorMap = new LinkedHashMap<DataFile, Color>();
		
		ExperimentDesignFactor stf = currentExperiment.getExperimentDesign().getSampleTypeFactor();
		Collection<ExperimentDesignLevel> sampleTypes = 
				currentExperiment.getExperimentDesign().getActiveDesignSubset().getLevelsForFactor(stf);
		Map<String, Color> levelMap = new LinkedHashMap<String, Color>();
		int count = 0;
		for(ExperimentDesignLevel l : sampleTypes) {
			levelMap.put(l.getName(),ColorUtils.getColor(count));
			count++;
		}
		for(Entry<DataFile,String>fm : fileTypeMap.entrySet()) {
			
			Color fileColor = Color.BLACK;
			ExperimentDesignLevel stLevel = fm.getKey().getParentSample().getLevel(stf);
			if(stLevel != null) {
				fileColor = levelMap.get(stLevel.getName());
				if(fileColor == null)
					fileColor = Color.BLACK;
			}
			colorMap.put(fm.getKey(), fileColor);
		}
		return colorMap;
	}

	private LockedXYTextAnnotation createLockedAnnotation(
			String text, Color textColor, TextAnchor textAnchor) {
		
		LockedXYTextAnnotation a = 
				new LockedXYTextAnnotation(text, 0.03d);
		a.setTextAnchor(textAnchor);
		a.setPaint(textColor);
		a.setOutlineVisible(false);
		a.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
		return a;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void loadPreferences(Preferences preferences) {
		
		prefs = preferences;
		numColumns = prefs.getInt(NUMBER_OF_COLUMNS, 3);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

		@Override
	public void savePreferences() {

		prefs = Preferences.userNodeForPackage(this.getClass());
		prefs.putInt(NUMBER_OF_COLUMNS, numColumns);
	}
}
