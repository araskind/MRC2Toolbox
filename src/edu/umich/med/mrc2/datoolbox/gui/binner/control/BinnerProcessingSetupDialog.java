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

package edu.umich.med.mrc2.datoolbox.gui.binner.control;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import edu.umich.med.mrc2.datoolbox.data.BinnerPreferencesObject;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.ValidatableForm;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class BinnerProcessingSetupDialog extends JDialog 
		implements ActionListener, BackedByPreferences, ValidatableForm{

	private static final long serialVersionUID = 1L;
	
	private static final Icon setupBinnerAnnotationsIcon = 
			GuiUtils.getIcon("setupBinnerAnnotations", 32);
	private Preferences preferences;
	
	private DataFileSelectionPanel dataFileSelectionPanel;
	private DataCleaningOptionsPanel dataCleaningOptionsPanel;
	private FeatureGroupingOptionsPanel featureGroupingOptionsPanel;
	private AnnotationsSelectorPanel annotationsSelectorPanel;
	
	private DataAnalysisProject currentExperiment;
	private DataPipeline activeDataPipeline;

	public BinnerProcessingSetupDialog(
			DataAnalysisProject currentExperiment, 
			DataPipeline activeDataPipeline,
			ActionListener listener) {

		super();
		setTitle("Binner processing setup");
		setIconImage(((ImageIcon) setupBinnerAnnotationsIcon).getImage());
		setSize(new Dimension(1000,800));
		setPreferredSize(new Dimension(1000,800));
		setModalityType(ModalityType.APPLICATION_MODAL);		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		this.currentExperiment = currentExperiment;
		this.activeDataPipeline = activeDataPipeline;
		
		JTabbedPane wrapperPanel = new JTabbedPane();
		wrapperPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(wrapperPanel, BorderLayout.CENTER);
		
		dataFileSelectionPanel = new DataFileSelectionPanel();
		dataFileSelectionPanel.setTableModelFromFileCollection(
				currentExperiment.getDataFilesForPipeline(activeDataPipeline, false));
		wrapperPanel.addTab("Select data files", null, dataFileSelectionPanel);
		
		dataCleaningOptionsPanel = new DataCleaningOptionsPanel();
		wrapperPanel.addTab("Data cleanup options", null, dataCleaningOptionsPanel);
		
		featureGroupingOptionsPanel = new FeatureGroupingOptionsPanel();
		wrapperPanel.addTab("Feature grouping options", null, featureGroupingOptionsPanel);
		
		annotationsSelectorPanel = new AnnotationsSelectorPanel();
		wrapperPanel.addTab("Annotations parameters", null, annotationsSelectorPanel);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton(
				MainActionCommands.GENERATE_BINNER_ANNOTATIONS_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.GENERATE_BINNER_ANNOTATIONS_COMMAND.getName());
		btnSave.addActionListener(listener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		loadPreferences();
		pack();
	}
	
	@Override
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}	
	
	public void loadPreferencesFromObject(BinnerPreferencesObject bpo, boolean loadDataFiles) {

		//	Input files
		if(loadDataFiles && bpo.getInputFiles() != null)
				dataFileSelectionPanel.setTableModelFromFileCollection(bpo.getInputFiles());		
		
	    //	Data cleaning
	    dataCleaningOptionsPanel.setOutlierSDdeviation(bpo.getOutlierSDdeviation());
	    dataCleaningOptionsPanel.setMissingRemovalThreshold(bpo.getMissingRemovalThreshold());
	    dataCleaningOptionsPanel.setTreatZerosAsMisssing(bpo.isZeroAsMissing());
	    dataCleaningOptionsPanel.setLogTransformData(bpo.isLogTransform());	    
	    dataCleaningOptionsPanel.setDeisotopingMassTolerance(bpo.getDeisotopingMassTolerance());
	    dataCleaningOptionsPanel.setDeisotopingRTtolerance(bpo.getDeisotopingRTtolerance());
	    dataCleaningOptionsPanel.setDeisotopingCorrelationCutoff(bpo.getDeisotopingCorrCutoff());	    
	    dataCleaningOptionsPanel.setDeisotopeMassDifferenceDistribution(bpo.isDeisoMassDiffDistr());	    
	    dataCleaningOptionsPanel.setDeisotopeData(bpo.isDeisotope());
	    
	    //	Feature grouping
	    featureGroupingOptionsPanel.setCorrelationFunctionType(bpo.getCorrelationFunctionType());
	    featureGroupingOptionsPanel.setRTgap(bpo.getRtGap());	
	    featureGroupingOptionsPanel.setMinSubclusterRTgap(bpo.getMinSubclusterRTgap());
	    featureGroupingOptionsPanel.setMaxSubclusterRTgap(bpo.getMaxSubclusterRTgap());
	    featureGroupingOptionsPanel.setClusterGroupingMethod(bpo.getClusterGroupingMethod());	    
	    featureGroupingOptionsPanel.setBinClusteringCutoff(bpo.getBinClusteringCutoff());	    
	    featureGroupingOptionsPanel.setBinClusteringCutoffType(bpo.getBinClusteringCutoffType());
	    
	    featureGroupingOptionsPanel.setBinSizeLimitForAnalysis(bpo.getBinSizeLimitForAnalysis()); 
	    featureGroupingOptionsPanel.setBinSizeLimitForOutput(bpo.getBinSizeLimitForOutput()); 	    
	    featureGroupingOptionsPanel.setLimitMaxBinSizeForAnalysis(bpo.isLimitBinSizeForAnalysis());
	    featureGroupingOptionsPanel.setLimitMaxBinSizeForOutput(bpo.isLimitBinSizeForOutput());
	    
	    //	Annotations
	    annotationsSelectorPanel.setAnnotationMassTolerance(bpo.getAnnotationMassTolerance());
	    annotationsSelectorPanel.setAnnotationRTTolerance(bpo.getAnnotationRTTolerance());
	    if(bpo.getAnnotationList() != null)
	    	annotationsSelectorPanel.loadBinnerAdductList(bpo.getAnnotationList());
	}

	@Override
	public void loadPreferences(Preferences prefs) {
		
	    preferences = prefs;
	    
	    //	Data cleaning
	    dataCleaningOptionsPanel.setOutlierSDdeviation(
	    		preferences.getDouble(BinnerParameters.DCOutlierStDevCutoff.name(), 
	    				Double.parseDouble(BinnerParameters.DCOutlierStDevCutoff.getDefaultValue())));
	    dataCleaningOptionsPanel.setMissingRemovalThreshold(
	    		preferences.getDouble(BinnerParameters.DCMissingnessCutoff.name(), 
	    				Double.parseDouble(BinnerParameters.DCMissingnessCutoff.getDefaultValue())));
	    dataCleaningOptionsPanel.setTreatZerosAsMisssing(
	    		preferences.getBoolean(BinnerParameters.DCZeroAsMissing.name(), 
	    				Boolean.parseBoolean(BinnerParameters.DCZeroAsMissing.getDefaultValue())));
	    dataCleaningOptionsPanel.setLogTransformData(
	    		preferences.getBoolean(BinnerParameters.DCNormalize.name(), 
	    				Boolean.parseBoolean(BinnerParameters.DCNormalize.getDefaultValue())));	    
	    dataCleaningOptionsPanel.setDeisotopingMassTolerance(
	    		preferences.getDouble(BinnerParameters.DCDeisotopeMassTolerance.name(), 
	    				Double.parseDouble(BinnerParameters.DCDeisotopeMassTolerance.getDefaultValue())));
	    dataCleaningOptionsPanel.setDeisotopingRTtolerance(
	    		preferences.getDouble(BinnerParameters.DCDeisotopeRTTolearance.name(), 
	    				Double.parseDouble(BinnerParameters.DCDeisotopeRTTolearance.getDefaultValue())));
	    dataCleaningOptionsPanel.setDeisotopingCorrelationCutoff(
	    		preferences.getDouble(BinnerParameters.DCDeisotopeCorrCutoff.name(), 
	    				Double.parseDouble(BinnerParameters.DCDeisotopeCorrCutoff.getDefaultValue())));	    
	    dataCleaningOptionsPanel.setDeisotopeMassDifferenceDistribution(
	    		preferences.getBoolean(BinnerParameters.DCDeisotopeMassDiffDistribution.name(), 
	    				Boolean.parseBoolean(BinnerParameters.DCDeisotopeMassDiffDistribution.getDefaultValue())));	    
	    dataCleaningOptionsPanel.setDeisotopeData(
	    		preferences.getBoolean(BinnerParameters.DCDeisotope.name(), 
	    				Boolean.parseBoolean(BinnerParameters.DCDeisotope.getDefaultValue())));
	    
	    //	Feature grouping
	    String corrFunctionName = preferences.get
	    		(BinnerParameters.FGCorrFunction.name(), 
    				CorrelationFunctionType.getOptionByName(
    					BinnerParameters.FGCorrFunction.getDefaultValue()).name());
	    featureGroupingOptionsPanel.setCorrelationFunctionType(
	    		CorrelationFunctionType.getOptionByName(corrFunctionName));
	    featureGroupingOptionsPanel.setRTgap(
	    		preferences.getDouble(BinnerParameters.FGRTGap.name(), 
	    				Double.parseDouble(BinnerParameters.FGRTGap.getDefaultValue())));	
	    featureGroupingOptionsPanel.setMinSubclusterRTgap(
	    		preferences.getDouble(BinnerParameters.FGMinRtGap.name(), 
	    				Double.parseDouble(BinnerParameters.FGMinRtGap.getDefaultValue())));
	    featureGroupingOptionsPanel.setMaxSubclusterRTgap(
	    		preferences.getDouble(BinnerParameters.FGMaxRtGap.name(), 
	    				Double.parseDouble(BinnerParameters.FGMaxRtGap.getDefaultValue())));
	    String clusterGroupMethodName = preferences.get
	    		(BinnerParameters.FGGroupingMethod.name(), 
    				ClusterGroupingMethod.getOptionByName(
    					BinnerParameters.FGGroupingMethod.getDefaultValue()).name());
	    featureGroupingOptionsPanel.setClusterGroupingMethod(
	    		ClusterGroupingMethod.getOptionByName(clusterGroupMethodName));	    
	    featureGroupingOptionsPanel.setBinClusteringCutoff(
	    		preferences.getInt(BinnerParameters.FGBinningCutoffValue.name(), 
	    				Integer.parseInt(BinnerParameters.FGBinningCutoffValue.getDefaultValue())));    
	    String binClusteringCutoffTypeMethodName = preferences.get
	    		(BinnerParameters.FGBinningCutoffType.name(), 
    				BinClusteringCutoffType.getOptionByName(
    					BinnerParameters.FGBinningCutoffType.getDefaultValue()).name());
	    featureGroupingOptionsPanel.setBinClusteringCutoffType(
	    		BinClusteringCutoffType.getOptionByName(binClusteringCutoffTypeMethodName));
	    
	    featureGroupingOptionsPanel.setBinSizeLimitForAnalysis(
	    		preferences.getInt(BinnerParameters.FGBinSizeLimitForAnalysis.name(), 
	    				Integer.parseInt(BinnerParameters.FGBinSizeLimitForAnalysis.getDefaultValue()))); 
	    featureGroupingOptionsPanel.setBinSizeLimitForOutput(
	    		preferences.getInt(BinnerParameters.FGBinSizeLimitForOutput.name(), 
	    				Integer.parseInt(BinnerParameters.FGBinSizeLimitForOutput.getDefaultValue()))); 
	    
	    featureGroupingOptionsPanel.setLimitMaxBinSizeForAnalysis(
	    		preferences.getBoolean(BinnerParameters.FGBOverrideBinSizeLimitForAnalysis.name(), 
	    				Boolean.parseBoolean(BinnerParameters.FGBOverrideBinSizeLimitForAnalysis.getDefaultValue())));
	    featureGroupingOptionsPanel.setLimitMaxBinSizeForOutput(
	    		preferences.getBoolean(BinnerParameters.FGBOverrideBinSizeLimitForOutput.name(), 
	    				Boolean.parseBoolean(BinnerParameters.FGBOverrideBinSizeLimitForOutput.getDefaultValue())));
	    
	    //	Annotations
	    annotationsSelectorPanel.setAnnotationMassTolerance(
	    		preferences.getDouble(BinnerParameters.ANMassTolerance.name(), 
	    				Double.parseDouble(BinnerParameters.ANMassTolerance.getDefaultValue())));
	    annotationsSelectorPanel.setAnnotationRTTolerance(
	    		preferences.getDouble(BinnerParameters.ANRTTolerance.name(), 
	    				Double.parseDouble(BinnerParameters.ANRTTolerance.getDefaultValue())));
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void savePreferences() {

		 preferences = Preferences.userNodeForPackage(this.getClass());
		 
		 //	Data cleaning
		 preferences.putDouble(
				 BinnerParameters.DCOutlierStDevCutoff.name(), 
				 dataCleaningOptionsPanel.getOutlierSDdeviation());
		 preferences.putDouble(
				 BinnerParameters.DCMissingnessCutoff.name(), 
				 dataCleaningOptionsPanel.getMissingRemovalThreshold());
		 preferences.putBoolean(
				 BinnerParameters.DCZeroAsMissing.name(), 
				 dataCleaningOptionsPanel.treatZerosAsMisssing());
		 preferences.putBoolean(
				 BinnerParameters.DCNormalize.name(), 
				 dataCleaningOptionsPanel.logTransformData());
		 preferences.putBoolean(
				 BinnerParameters.DCDeisotope.name(), 
				 dataCleaningOptionsPanel.deisotopeData()); 
		 preferences.putDouble(
				 BinnerParameters.DCDeisotopeMassTolerance.name(), 
				 dataCleaningOptionsPanel.getDeisotopingMassTolerance());
		 preferences.putDouble(
				 BinnerParameters.DCDeisotopeRTTolearance.name(), 
				 dataCleaningOptionsPanel.getDeisotopingRTtolerance());
		 preferences.putDouble(
				 BinnerParameters.DCDeisotopeCorrCutoff.name(), 
				 dataCleaningOptionsPanel.getDeisotopingCorrelationCutoff());  
		 preferences.putBoolean(
				 BinnerParameters.DCDeisotopeMassDiffDistribution.name(), 
				 dataCleaningOptionsPanel.deisotopeMassDifferenceDistribution()); 
		    
		 //	Feature grouping
		 preferences.put(
				 BinnerParameters.FGCorrFunction.name(), 
				 featureGroupingOptionsPanel.getCorrelationFunctionType().name());
		 preferences.putDouble(
				 BinnerParameters.FGRTGap.name(), 
				 featureGroupingOptionsPanel.getRTgap());
		 preferences.putDouble(
				 BinnerParameters.FGMinRtGap.name(), 
				 featureGroupingOptionsPanel.getMinSubclusterRTgap());		 
		 preferences.putDouble(
				 BinnerParameters.FGMaxRtGap.name(), 
				 featureGroupingOptionsPanel.getMaxSubclusterRTgap());	
		 preferences.put(
				 BinnerParameters.FGGroupingMethod.name(), 
				 featureGroupingOptionsPanel.getClusterGroupingMethod().name());
		 preferences.putInt(
				 BinnerParameters.FGBinningCutoffValue.name(), 
				 featureGroupingOptionsPanel.getBinClusteringCutoff());
		 preferences.put(
				 BinnerParameters.FGBinningCutoffType.name(), 
				 featureGroupingOptionsPanel.getBinClusteringCutoffType().name());
		 preferences.putInt(
				 BinnerParameters.FGBinSizeLimitForAnalysis.name(), 
				 featureGroupingOptionsPanel.getBinSizeLimitForAnalysis());
		 preferences.putInt(
				 BinnerParameters.FGBinSizeLimitForOutput.name(), 
				 featureGroupingOptionsPanel.getBinSizeLimitForOutput());
		 preferences.putBoolean(
				 BinnerParameters.FGBOverrideBinSizeLimitForAnalysis.name(), 
				 featureGroupingOptionsPanel.limitMaxBinSizeForAnalysis());
		 preferences.putBoolean(
				 BinnerParameters.FGBOverrideBinSizeLimitForOutput.name(), 
				 featureGroupingOptionsPanel.limitMaxBinSizeForOutput());	    
		    
		    //	Annotations
		 preferences.putDouble(
				 BinnerParameters.ANMassTolerance.name(), 
				 annotationsSelectorPanel.getAnnotationMassTolerance());
		 preferences.putDouble(
				 BinnerParameters.ANRTTolerance.name(), 
				 annotationsSelectorPanel.getAnnotationRTTolerance());
	}
	
	@Override
	public Collection<String> validateFormData() {

		Collection<String>errors = new ArrayList<String>();
		errors.addAll(dataFileSelectionPanel.validateFormData());
		errors.addAll(dataCleaningOptionsPanel.validateFormData());
		errors.addAll(featureGroupingOptionsPanel.validateFormData());
		errors.addAll(annotationsSelectorPanel.validateFormData());
		
		return errors;
	}

	public BinnerPreferencesObject getBinnerPreferencesObject() {
		
		BinnerPreferencesObject bpo = new BinnerPreferencesObject(activeDataPipeline);
		
		//	Input data
		bpo.getInputFiles().addAll(dataFileSelectionPanel.getEnabledFiles());		
		Set<Polarity>polSet = bpo.getInputFiles().stream().
				map(f -> f.getDataAcquisitionMethod().getPolarity()).
				distinct().collect(Collectors.toSet());
		bpo.setPolarity(polSet.iterator().next());
		
		//	Data cleaning
		bpo.setOutlierSDdeviation(dataCleaningOptionsPanel.getOutlierSDdeviation());
		bpo.setMissingRemovalThreshold(dataCleaningOptionsPanel.getMissingRemovalThreshold());
		bpo.setLogTransform(dataCleaningOptionsPanel.logTransformData());
		bpo.setZeroAsMissing(dataCleaningOptionsPanel.treatZerosAsMisssing());
		bpo.setDeisotope(dataCleaningOptionsPanel.deisotopeData());
		bpo.setDeisoMassDiffDistr(dataCleaningOptionsPanel.deisotopeMassDifferenceDistribution());
		bpo.setDeisotopingMassTolerance(dataCleaningOptionsPanel.getDeisotopingMassTolerance());
		bpo.setDeisotopingRTtolerance(dataCleaningOptionsPanel.getDeisotopingRTtolerance());
		bpo.setDeisotopingCorrCutoff(dataCleaningOptionsPanel.getDeisotopingCorrelationCutoff());

		//	Feature Grouping
		bpo.setCorrelationFunctionType(featureGroupingOptionsPanel.getCorrelationFunctionType());
		bpo.setRtGap(featureGroupingOptionsPanel.getRTgap());
		bpo.setClusterGroupingMethod(featureGroupingOptionsPanel.getClusterGroupingMethod());
		bpo.setMinSubclusterRTgap(featureGroupingOptionsPanel.getMinSubclusterRTgap());
		bpo.setMaxSubclusterRTgap(featureGroupingOptionsPanel.getMaxSubclusterRTgap());
		bpo.setBinClusteringCutoffType(featureGroupingOptionsPanel.getBinClusteringCutoffType());
		bpo.setLimitBinSizeForAnalysis(featureGroupingOptionsPanel.limitMaxBinSizeForAnalysis());
		bpo.setBinSizeLimitForAnalysis(featureGroupingOptionsPanel.getBinSizeLimitForAnalysis());
		bpo.setLimitBinSizeForOutput(featureGroupingOptionsPanel.limitMaxBinSizeForOutput());
		bpo.setBinSizeLimitForOutput(featureGroupingOptionsPanel.getBinSizeLimitForOutput());
		bpo.setBinClusteringCutoff(featureGroupingOptionsPanel.getBinClusteringCutoff());

		//	Annotation Parameters
		bpo.setAnnotationList(annotationsSelectorPanel.getBinnerAdductList());
		bpo.setAnnotationMassTolerance(annotationsSelectorPanel.getAnnotationMassTolerance());
		bpo.setAnnotationRTTolerance(annotationsSelectorPanel.getAnnotationRTTolerance());
		bpo.setUseNeutralMassForChargeCarrierAssignment(annotationsSelectorPanel.useNeutralMassForChargeCarrierAssignment());
		bpo.setAllowVariableChargeWithoutIsotopeInformation(annotationsSelectorPanel.allowVariableChargeWithoutIsotopeInformation());
		
		return bpo;
	}
}
