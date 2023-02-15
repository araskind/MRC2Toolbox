package edu.umich.med.mrc2.datoolbox.gui.rawdata.manint;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import edu.umich.med.mrc2.datoolbox.data.ExtractedMsFeatureChromatogram;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class ManualIntegrationWindow extends JFrame 
		implements ActionListener, BackedByPreferences, TaskListener {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 4716040322211937021L;
	
	private final static Icon manualIntegrationIcon = GuiUtils.getIcon("peakIntegrate", 32);
	private static final NumberFormat mzFormat = MRC2ToolBoxConfiguration.getMzFormat();
	private static final DecimalFormat ppmFormat = MRC2ToolBoxConfiguration.getPpmFormat();
	private ManualIntegrationToolbar manualIntegrationToolbar;
	private Preferences preferences;
	private Collection<MsFeature>activeFeatures;
	private Collection<ExtractedMsFeatureChromatogram> reintegratedChromatograms;
	private PeakIntegrationPlotPanel peakIntegrationPlotPanel;
	private ManualIntegrationSettingsDialog manualIntegrationSettingsDialog;
	
	private final static String RT_WINDOW_EXTENSION_WIDTH = "RT_WINDOW_EXTENSION_WIDTH";
	private final static String SMOOTHING_FILTER_WIDTH = "SMOOTHING_FILTER_WIDTH";
	private final static String MZ_ERROR = "MZ_ERROR";
	private final static String MZ_ERROR_TYPE = "MZ_ERROR_TYPE";
	private final static String SUBPLOT_HEIGHT = "SUBPLOT_HEIGHT";
	private final static String MAX_ISOTOPE_ERROR_PERCENT = "MAX_ISOTOPE_ERROR_PERCENT";
	
	private int smoothingFilterWidth;
	private double mzError;
	private MassErrorType massErrorType;
	private int maxIsotopeErrorPercent;

	public ManualIntegrationWindow() {
				
		super("Manual peak integration");
		setIconImage(((ImageIcon) manualIntegrationIcon).getImage());
		
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setMinimumSize(new Dimension(800, 600));		
		setPreferredSize(new Dimension(800, 600));
				
		peakIntegrationPlotPanel = new PeakIntegrationPlotPanel();
		getContentPane().add(new JScrollPane(peakIntegrationPlotPanel), BorderLayout.CENTER);
		
		manualIntegrationToolbar = new ManualIntegrationToolbar(peakIntegrationPlotPanel, this);
		getContentPane().add(manualIntegrationToolbar, BorderLayout.NORTH);
		
		loadPreferences();
		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {
		
		String command = event.getActionCommand();
		
		if(command.equals(MainActionCommands.SHOW_MANUAL_INTEGRATOR_SETTINGS.getName()))
			showManualIntegratorSettings();
		
		if(command.equals(MainActionCommands.SAVE_MANUAL_INTEGRATOR_SETTINGS.getName()))
			saveManualIntegratorSettings();
		
		if(command.equals(MainActionCommands.INTEGRATE_HIGHLIGHTED_RANGES.getName()))
			integrateHighlightedRanges();
		
		if(command.equals(MainActionCommands.ACCEPT_INTEGRATION_RESULTS.getName()))
			acceptIntegrationResults();
		
		if(command.equals(MainActionCommands.CLEAR_HIGHLIGHTED_RANGES.getName()))
			clearHighlightedRanges();
		
		if(command.equals(MainActionCommands.RELOAD_ORIGINAL_CHROMATOGRAMS.getName()))
			reloadOriginalChromatograms();	
	}
	
	private void integrateHighlightedRanges() {
		
//		if(activeFeatures == null || activeFeatures.isEmpty())
//			return;
//		
//		Map<DataFile, org.jfree.data.Range> fileRangeMap = peakIntegrationPlotPanel.getSelectedRtRanges();
//		if(fileRangeMap.isEmpty())
//			return;
//		
//		MsFeature refFeature = activeFeatures.iterator().next();		
//		MsFeatureBin parentBin = refFeature.getParentBin();
//		
//		Map<DataFile, MsFeature> fileFeatureMap = peakIntegrationPlotPanel.getFileFeatureMap();	
//		Map<MsFeature,Range> rtRangeMap = new HashMap<MsFeature,Range>();
//		for(Entry<DataFile, org.jfree.data.Range> entry : fileRangeMap.entrySet()) {
//			
//			Range rtRange = new Range(entry.getValue().getLowerBound(), entry.getValue().getUpperBound());
//			rtRangeMap.put(fileFeatureMap.get(entry.getKey()), rtRange);
//			
////			System.out.println(entry.getKey().getFileName() + "\t" + rtRange.toString());
//		}
//		Polarity polarity = refFeature.getPolarity();
//		Collection<Double> mzList = Arrays.stream(parentBin.getIsotopicPattern()).
//				mapToDouble(p -> p.getMz()).boxed().collect(Collectors.toList());
		
//		PeakReintegrationTask task = new PeakReintegrationTask(
//				rtRangeMap,
//				polarity,
//				1, 
//				mzList, 
//				mzError, 
//				massErrorType, 
//				true, 
//				smoothingFilterWidth);
//		double maxIsotopeError = maxIsotopeErrorPercent / 100.0d;
//		PeakReintegrationTaskNew task = new PeakReintegrationTaskNew(
//				parentBin,
//				rtRangeMap, 
//				mzError, 
//				MassErrorType.ppm, 
//				true,
//				smoothingFilterWidth,
//				searchType,
//				maxIsotopeError);
//		
//		task.addTaskListener(this);
//		MainWindow.getTaskController().addTask(task);				
	}
	
	private void acceptIntegrationResults() {

//		if(reintegratedChromatograms == null || reintegratedChromatograms.isEmpty())
//			return;
//			
//		if(MessageDialog.showChoiceMsg("Accept manual integration results?", this) 
//				== JOptionPane.YES_OPTION) {
//			
//			MsFeatureBin parentBin = 
//					reintegratedChromatograms.iterator().next().getParentFeature().getParentBin();
//			for(ExtractedMsFeatureChromatogram chrom : reintegratedChromatograms) {
//				
//				MsFeature parentFeature = chrom.getParentFeature();
//				double border = parentFeature.getC12C13border();
//				parentFeature.clearDataPoints();
//				for(Entry<MsPoint, Double> entry : chrom.getPointRtMap().entrySet()) {
//					
//					if(entry.getKey().getMz() <= border)
//						parentFeature.addC12MsPoint(entry.getKey(), entry.getValue());
//					else
//						parentFeature.addC13MsPoint(entry.getKey(), entry.getValue());
//				}
//				//	TODO check if this makes a difference
//				parentFeature.finalizePeak();
//				if(!parentBin.getFeatures().contains(parentFeature))
//					parentBin.addFeature(parentFeature);
//			}
//			parentBin.finalizeBin();
//			
//			//	Recalculate normalization and display new Bin data
//			SpectrumBinnerPanel targetPanel = null;
//			Collection<MsFeatureBin> completeBins = null;
//			if(searchType.equals(SearchType.TARGETED_IROA)) {
//				targetPanel = ClusterFinderCore.getMainWindow().getTargetedAnalysisPanel();
//				ClusterFinderCore.getMainWindow().showPanel(CFPanels.TARGETED_ANALYSIS);
//				completeBins = ClusterFinderCore.getCurrentProject().
//						getTargetedResultsMap().get(ClusterFinderCore.getCurrentProject().getActiveAssay());
//			}		
//			if(searchType.equals(SearchType.UNTARGETED_IROA)) {
//				targetPanel = ClusterFinderCore.getMainWindow().getUntargetedAnalysisPanel();
//				ClusterFinderCore.getMainWindow().showPanel(CFPanels.UNTARGETED_ANALYSIS);
//				completeBins = ClusterFinderCore.getCurrentProject().
//						getUntargetedResultsMap().get(ClusterFinderCore.getCurrentProject().getActiveAssay());
//			}
//			NormalizationUtils.recalculateNormalizationValues(completeBins);
//			NormalizationUtils.calculateRatioZScores(completeBins);	
//			if(targetPanel == null)
//				return;
//						
//			targetPanel.goToBinForFeature(reintegratedChromatograms.iterator().next().getParentFeature());
//			reloadOriginalChromatograms();
//		}
	}
	
	private void clearHighlightedRanges() {
		peakIntegrationPlotPanel.clearMarkers();
	}
	
	private void reloadOriginalChromatograms() {

		if(activeFeatures != null)
			setActiveFeatures(activeFeatures);
	}
	
	public void clearWindow() {

		this.activeFeatures = null;
		setTitle("Manual peak integration");
		peakIntegrationPlotPanel.removeAllDataSets();
		peakIntegrationPlotPanel.clearMarkers();
	}
	
	private void showManualIntegratorSettings() {
		
		manualIntegrationSettingsDialog = new ManualIntegrationSettingsDialog(this);
		manualIntegrationSettingsDialog.setLocationRelativeTo(this);		
		manualIntegrationSettingsDialog.setSmoothingFilterWidth(smoothingFilterWidth);
		manualIntegrationSettingsDialog.setRtWindowExtensionWidth(peakIntegrationPlotPanel.getRtWindowExtensionWidth());
		manualIntegrationSettingsDialog.setMinPlotHeight(peakIntegrationPlotPanel.getSubPlotHeight());
		manualIntegrationSettingsDialog.setMzError(mzError);
		manualIntegrationSettingsDialog.setMzErrorType(massErrorType);	
		manualIntegrationSettingsDialog.setmaxIsotopeIntensityErrorPercent(maxIsotopeErrorPercent);
		manualIntegrationSettingsDialog.setVisible(true);
	}

	private void saveManualIntegratorSettings() {
		
		peakIntegrationPlotPanel.setRtWindowExtensionWidth(manualIntegrationSettingsDialog.getRtWindowExtensionWidth());
		peakIntegrationPlotPanel.setSubPlotHeight(manualIntegrationSettingsDialog.getMinPlotHeight());
		smoothingFilterWidth = manualIntegrationSettingsDialog.getSmoothingFilterWidth();
		mzError = manualIntegrationSettingsDialog.getMzError();		
		massErrorType = manualIntegrationSettingsDialog.getMzErrorType();	
		maxIsotopeErrorPercent = manualIntegrationSettingsDialog.getmaxIsotopeIntensityErrorPercent();
		savePreferences();
		manualIntegrationSettingsDialog.dispose();
	}
	
	@Override
	public void dispose() {
		savePreferences();
		super.dispose();
	}
	
	@Override
	public void setVisible(boolean visible) {

		if(!visible)
			savePreferences();
		
		super.setVisible(visible);
		if(visible)
			toFront();
	}
	
	@Override
	public void loadPreferences(Preferences preferences2) {
		
		preferences = preferences2;		
		peakIntegrationPlotPanel.setRtWindowExtensionWidth(preferences.getDouble(RT_WINDOW_EXTENSION_WIDTH, 0.5d));
		smoothingFilterWidth = preferences.getInt(SMOOTHING_FILTER_WIDTH, 9);
		mzError = preferences.getDouble(MZ_ERROR, 20.0d);		
		massErrorType = MassErrorType.getTypeByName(preferences.get(MZ_ERROR_TYPE, MassErrorType.ppm.name()));		
		peakIntegrationPlotPanel.setSubPlotHeight(preferences.getInt(SUBPLOT_HEIGHT, 300));		
		maxIsotopeErrorPercent =  preferences.getInt(MAX_ISOTOPE_ERROR_PERCENT, 30);		
	}
	
	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(this.getClass().getName()));		
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userRoot().node(this.getClass().getName());

		preferences.putDouble(RT_WINDOW_EXTENSION_WIDTH, peakIntegrationPlotPanel.getRtWindowExtensionWidth());
		preferences.putInt(SMOOTHING_FILTER_WIDTH, smoothingFilterWidth);
		preferences.putDouble(MZ_ERROR, mzError);
		preferences.put(MZ_ERROR_TYPE, massErrorType.name());
		preferences.putInt(SUBPLOT_HEIGHT, peakIntegrationPlotPanel.getSubPlotHeight());
		preferences.putInt(MAX_ISOTOPE_ERROR_PERCENT, maxIsotopeErrorPercent);
	}

	public Collection<MsFeature> getActiveFeatures() {	
		return activeFeatures;
	}

	public void setActiveFeatures(Collection<MsFeature> activeFeatures) {
		
//		this.activeFeatures = activeFeatures;
//		setTitle("Manually integrating " + Integer.toString(activeFeatures.size()) + 
//				" peak(s) for " + activeFeatures.iterator().next().getParentBin().toString());	
//		
//		peakIntegrationPlotPanel.showfeatureChromatograms(activeFeatures);
//		reintegratedChromatograms = null;
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {
			
			((AbstractTask)e.getSource()).removeTaskListener(this);
			
//			if (e.getSource().getClass().equals(PeakReintegrationTaskNew.class)) {
//				
//				PeakReintegrationTaskNew task = (PeakReintegrationTaskNew)e.getSource();
//				reintegratedChromatograms = task.getExtractedChromatograms();		
//				peakIntegrationPlotPanel.updateReintegratedChromatograms(reintegratedChromatograms);
//			}
		}
	}
}
