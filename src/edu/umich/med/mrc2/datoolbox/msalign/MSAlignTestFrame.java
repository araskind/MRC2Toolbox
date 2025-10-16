/*******************************************************************************
 *
 * (C) Copyright 2018-2025 MRC2 (http://mrc2.umich.edu).
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

package edu.umich.med.mrc2.datoolbox.msalign;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.analysis.interpolation.LoessInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.gui.plot.stats.CorrelationPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.jnafilechooser.api.JnaFileChooser;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.filter.MovingAverageFilter;

public class MSAlignTestFrame extends JFrame implements WindowListener, ActionListener, BackedByPreferences {

	private static final long serialVersionUID = 2455693081638877662L;
	
	private Preferences preferences;
	
	public static final String WINDOW_WIDTH = "WINDOW_WIDTH";
	public static final String WINDOW_HEIGTH = "WINDOW_HEIGTH";
	public static final String WINDOW_X = "WINDOW_X";
	public static final String WINDOW_Y = "WINDOW_Y";
	
	public static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	public static final String NUM_RT_INTERVALS = "NUM_RT_INTERVALS";
	public static final String FEATURES_PER_INTERVAL = "FEATURES_PER_INTERVAL";
	public static final String MASS_ACCURACY = "MASS_ACCURACY";
		
	private static final String EXIT_COMMAND = "Exit";
	private static final String SELECT_FILE_ONE_COMMAND = "Select file one";
	private static final String SELECT_FILE_TWO_COMMAND = "Select file two";
	private static final String RUN_ALIGNMENT_COMMAND = "Run alignment";
	
	private File baseDirectory;
	private File cefFileOne;
	private File cefFileTwo;
	
	private CorrelationPlotPanel corrPlot;	
	private JTextField fileOneTextField;
	private JTextField fileTwoTextField;
	private JSpinner numRTIntervalsSpinner;
	private JSpinner featuresPerIntervalSpinner;
	private JFormattedTextField massAccuracyTextField;

	private AlignmentProcessor alignmentProcessor;
	
	public MSAlignTestFrame() throws HeadlessException {

		super("MS align test");
		initWindow();
		loadPreferences();
	}

	private synchronized void initWindow() {

		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			// handle exception
		} catch (ClassNotFoundException e) {
			// handle exception
		} catch (InstantiationException e) {
			// handle exception
		} catch (IllegalAccessException e) {
			// handle exception
		}
		setSize(new Dimension(1000, 800));
		setPreferredSize(new Dimension(1000, 800));
		
		JPanel panel = new JPanel(new BorderLayout(0, 0));
		getContentPane().add(panel, BorderLayout.CENTER);
		corrPlot = new CorrelationPlotPanel();
		panel.add(corrPlot, BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(panel_1, BorderLayout.NORTH);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel = new JLabel("CEF file 1");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		fileOneTextField = new JTextField();
		fileOneTextField.setEditable(false);
		GridBagConstraints gbc_fileOneTextField = new GridBagConstraints();
		gbc_fileOneTextField.gridwidth = 5;
		gbc_fileOneTextField.insets = new Insets(0, 0, 5, 5);
		gbc_fileOneTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_fileOneTextField.gridx = 1;
		gbc_fileOneTextField.gridy = 0;
		panel_1.add(fileOneTextField, gbc_fileOneTextField);
		fileOneTextField.setColumns(10);
		
		JButton btnNewButton_1 = new JButton("...");
		btnNewButton_1.setActionCommand(SELECT_FILE_ONE_COMMAND);
		btnNewButton_1.addActionListener(this);
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton_1.gridx = 6;
		gbc_btnNewButton_1.gridy = 0;
		panel_1.add(btnNewButton_1, gbc_btnNewButton_1);
		
		JLabel lblNewLabel_1 = new JLabel("CEF file 2");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 1;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		fileTwoTextField = new JTextField();
		fileTwoTextField.setEditable(false);
		GridBagConstraints gbc_fileTwoTextField = new GridBagConstraints();
		gbc_fileTwoTextField.gridwidth = 5;
		gbc_fileTwoTextField.insets = new Insets(0, 0, 5, 5);
		gbc_fileTwoTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_fileTwoTextField.gridx = 1;
		gbc_fileTwoTextField.gridy = 1;
		panel_1.add(fileTwoTextField, gbc_fileTwoTextField);
		fileTwoTextField.setColumns(10);
		
		JButton btnNewButton_2 = new JButton("...");
		btnNewButton_2.setActionCommand(SELECT_FILE_TWO_COMMAND);
		btnNewButton_2.addActionListener(this);
		GridBagConstraints gbc_btnNewButton_2 = new GridBagConstraints();
		gbc_btnNewButton_2.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton_2.gridx = 6;
		gbc_btnNewButton_2.gridy = 1;
		panel_1.add(btnNewButton_2, gbc_btnNewButton_2);
		
		JLabel lblNewLabel_2 = new JLabel("# RT intervals");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 2;
		panel_1.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		numRTIntervalsSpinner = new JSpinner();
		numRTIntervalsSpinner.setModel(new SpinnerNumberModel(10, 2, 500, 1));
		numRTIntervalsSpinner.setPreferredSize(new Dimension(80, 20));
		numRTIntervalsSpinner.setMinimumSize(new Dimension(80, 20));
		GridBagConstraints gbc_numRTIntervalsSpinner = new GridBagConstraints();
		gbc_numRTIntervalsSpinner.anchor = GridBagConstraints.WEST;
		gbc_numRTIntervalsSpinner.insets = new Insets(0, 0, 0, 5);
		gbc_numRTIntervalsSpinner.gridx = 1;
		gbc_numRTIntervalsSpinner.gridy = 2;
		panel_1.add(numRTIntervalsSpinner, gbc_numRTIntervalsSpinner);
		
		JLabel lblNewLabel_3 = new JLabel("# Features per interval");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_3.gridx = 2;
		gbc_lblNewLabel_3.gridy = 2;
		panel_1.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		featuresPerIntervalSpinner = new JSpinner();
		featuresPerIntervalSpinner.setModel(new SpinnerNumberModel(20, 2, 1000, 1));
		featuresPerIntervalSpinner.setMinimumSize(new Dimension(80, 20));
		featuresPerIntervalSpinner.setPreferredSize(new Dimension(80, 20));
		GridBagConstraints gbc_featuresPerIntervalSpinner = new GridBagConstraints();
		gbc_featuresPerIntervalSpinner.anchor = GridBagConstraints.WEST;
		gbc_featuresPerIntervalSpinner.insets = new Insets(0, 0, 0, 5);
		gbc_featuresPerIntervalSpinner.gridx = 3;
		gbc_featuresPerIntervalSpinner.gridy = 2;
		panel_1.add(featuresPerIntervalSpinner, gbc_featuresPerIntervalSpinner);
		
		JLabel lblNewLabel_4 = new JLabel("Mass accuracy, ppm");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_4.gridx = 4;
		gbc_lblNewLabel_4.gridy = 2;
		panel_1.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		massAccuracyTextField = 
				new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		GridBagConstraints gbc_massAccuracyTextField = new GridBagConstraints();
		gbc_massAccuracyTextField.insets = new Insets(0, 0, 0, 5);
		gbc_massAccuracyTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_massAccuracyTextField.gridx = 5;
		gbc_massAccuracyTextField.gridy = 2;
		panel_1.add(massAccuracyTextField, gbc_massAccuracyTextField);
		
		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_2, BorderLayout.SOUTH);
		
		JButton btnNewButton = new JButton("Exit");
		btnNewButton.setActionCommand(EXIT_COMMAND);
		btnNewButton.addActionListener(this);
		
		JButton btnNewButton_3 = new JButton(RUN_ALIGNMENT_COMMAND);
		btnNewButton_3.setActionCommand(RUN_ALIGNMENT_COMMAND);
		btnNewButton_3.addActionListener(this);
		panel_2.add(btnNewButton_3);
		panel_2.add(btnNewButton);
		
		addWindowListener(this);
	}	

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if(e.getActionCommand().equals(EXIT_COMMAND))
			MSAlignmentDevTest.exitProgram();
		
		if(e.getActionCommand().equals(SELECT_FILE_ONE_COMMAND))
			selectCEFFile(1);
			
		if(e.getActionCommand().equals(SELECT_FILE_TWO_COMMAND))
			selectCEFFile(2);
				
		if(e.getActionCommand().equals(RUN_ALIGNMENT_COMMAND))
			runAlignment();
	}
	
	private void runAlignment() {

		Collection<String>errors = validateProjectData();
		if(!errors.isEmpty()){
		    MessageDialog.showErrorMsg(
		            StringUtils.join(errors, "\n"), this);
		    return;
		}
		savePreferences();
		alignmentProcessor = new AlignmentProcessor(
				cefFileOne, 
				cefFileTwo, 
				getNumberOfRTIntervals(), 
				getNumberOfFeaturesPerRTInterval(), 
				getMassAccuracy());
		alignmentProcessor.runAlignment();
		
		System.out.println("Reference data file: " + alignmentProcessor.getReferenceList().getName());
		Map<MsFeature,MsFeature>anchorMap = alignmentProcessor.getAnchorMap();
		System.out.println("# of pairs: " + anchorMap.size());
		
		double[]refRtArray = anchorMap.keySet().stream().
				map(f -> f.getRetentionTime()).mapToDouble(Double::doubleValue).toArray();
		refRtArray = ArrayUtils.insert(0, refRtArray, 0.0d);
		
		double[]queryRtArray = anchorMap.values().stream().
				map(f -> f.getRetentionTime()).mapToDouble(Double::doubleValue).toArray();
		queryRtArray = ArrayUtils.insert(0, queryRtArray, 0.0d);
		
		double maxRt =
				Math.max(refRtArray[refRtArray.length - 1], queryRtArray[queryRtArray.length - 1]) * 1.05d;
		refRtArray = ArrayUtils.insert(refRtArray.length, refRtArray, maxRt);
		queryRtArray = ArrayUtils.insert(queryRtArray.length, queryRtArray, maxRt);
		MovingAverageFilter filter = new MovingAverageFilter(5);
		double[]queryRtArrayFiltered = filter.filter(refRtArray, queryRtArray);
		
		LoessInterpolator interpolator = new LoessInterpolator(0.4d, 1);
		//	PolynomialSplineFunction loesFit = interpolator.interpolate(refRtArray, queryRtArray);
		PolynomialSplineFunction loesFit = interpolator.interpolate(queryRtArrayFiltered, refRtArray);
		
		for(int i=0; i<refRtArray.length; i++) {
			
			double deltaOrig = refRtArray[i] - queryRtArray[i];
			double deltaFitted = refRtArray[i] - loesFit.value(queryRtArrayFiltered[i]);
			System.out.println(
					MRC2ToolBoxConfiguration.getRtFormat().format(refRtArray[i]) 
					+ "\t" + MRC2ToolBoxConfiguration.getRtFormat().format(queryRtArray[i])
					+ "\t" + MRC2ToolBoxConfiguration.getRtFormat().format(queryRtArrayFiltered[i])
					+ "\t" + MRC2ToolBoxConfiguration.getRtFormat().format(loesFit.value(queryRtArrayFiltered[i]))
					+ "\t" + MRC2ToolBoxConfiguration.getRtFormat().format(Math.abs(deltaOrig))
					+ "\t" + MRC2ToolBoxConfiguration.getRtFormat().format(Math.abs(deltaFitted))
					);
		}
//		for(Entry<MsFeature,MsFeature>anchor : alignmentProcessor.getAnchorMap().entrySet()) {
//			
//			double refRt  = anchor.getKey().getRetentionTime();
//			double queryRt  = anchor.getValue().getRetentionTime();
//			double fitted = loesFit.value(queryRt);
//			//	double rtDiff = fitted - queryRt;
//			//double fittedDiff = refRt - loesFit.value(queryRt);
//			
//			System.out.println(
//					MRC2ToolBoxConfiguration.getRtFormat().format(refRt) 
//					+ "\t" + MRC2ToolBoxConfiguration.getRtFormat().format(queryRt)
//					+ "\t" + MRC2ToolBoxConfiguration.getRtFormat().format(queryRt)
//					+ "\t" + MRC2ToolBoxConfiguration.getRtFormat().format(refRt - queryRt)
//					+ "\t" + MRC2ToolBoxConfiguration.getRtFormat().format(fitted)
//					+ "\t" + MRC2ToolBoxConfiguration.getRtFormat().format(queryRt - fitted)
//					);
//		}		
	}
	
	private int getNumberOfRTIntervals() {
		return (int)numRTIntervalsSpinner.getValue();
	}
	
	private int getNumberOfFeaturesPerRTInterval() {
		return (int)featuresPerIntervalSpinner.getValue();
	}
	
	private double getMassAccuracy() { 
		return Double.parseDouble(massAccuracyTextField.getText());
	}
	
	public Collection<String>validateProjectData(){
	    
	    Collection<String>errors = new ArrayList<String>();
		if(cefFileOne == null || !cefFileOne.exists())
			errors.add("CEF file one not defined");
			
		if(cefFileTwo == null || !cefFileTwo.exists())
			errors.add("CEF file two not defined");
	
	    return errors;
	}

	private void selectCEFFile(int number) {
				
		JnaFileChooser fc = new JnaFileChooser(baseDirectory);
		fc.setMode(JnaFileChooser.Mode.Files);
		fc.addFilter("CEF files", "cef", "CEF");
		fc.setTitle("Select library CEF file");
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(this)) {
			
			if(number == 1) {
				cefFileOne = fc.getSelectedFile();
				fileOneTextField.setText(cefFileOne.getPath());
			}
			if(number == 2) {
				cefFileTwo = fc.getSelectedFile();
				fileTwoTextField.setText(cefFileTwo.getPath());
			}
			baseDirectory = fc.getSelectedFile().getParentFile();
			savePreferences();	
		}
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
	    preferences = prefs;
	    
		int width = preferences.getInt(WINDOW_WIDTH, 1000);
		int heigh = preferences.getInt(WINDOW_HEIGTH, 800);
		setSize(new Dimension(width, heigh));
		setPreferredSize(new Dimension(width, heigh));		
		int x = preferences.getInt(WINDOW_X, 100);
		int y = preferences.getInt(WINDOW_Y, 100);		
		setLocation(x,y);
		
	    baseDirectory =
	        new File(preferences.get(BASE_DIRECTORY,
	            MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
	    numRTIntervalsSpinner.setValue(preferences.getInt(NUM_RT_INTERVALS, 10));
	    featuresPerIntervalSpinner.setValue(preferences.getInt(FEATURES_PER_INTERVAL, 20));	    
	    massAccuracyTextField.setText(Double.toString(preferences.getDouble(MASS_ACCURACY, 30.0d)));	    
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
		preferences.putInt(NUM_RT_INTERVALS, getNumberOfRTIntervals());
		preferences.putInt(FEATURES_PER_INTERVAL, getNumberOfFeaturesPerRTInterval());
		preferences.putDouble(MASS_ACCURACY, getMassAccuracy());
		
		preferences.putInt(WINDOW_WIDTH, getWidth());
		preferences.putInt(WINDOW_HEIGTH, getHeight());	
		Point location = getLocation();
		preferences.putInt(WINDOW_X, location.x);
		preferences.putInt(WINDOW_Y, location.y);
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {
		savePreferences();
		MSAlignmentDevTest.exitProgram();
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}
}
