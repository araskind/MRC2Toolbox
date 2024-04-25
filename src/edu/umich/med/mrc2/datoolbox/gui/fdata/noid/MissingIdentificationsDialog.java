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

package edu.umich.med.mrc2.datoolbox.gui.fdata.noid;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.Set;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.plot.PlotType;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.LCMSPlotPanel;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.LCMSPlotToolbar;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.tables.ms.MsOneTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class MissingIdentificationsDialog extends JDialog implements ActionListener, ListSelectionListener, BackedByPreferences {

	/**
	 *
	 */
	private static final long serialVersionUID = -3201790077208992057L;

	private Preferences preferences;
	private MissingIdsTable missingIdsTable;
	//private MissingIdsToolbar toolBar;
	private JFormattedTextField massAccuracyTextField;
	private JFormattedTextField rtWindowTextField;
	private JButton closeButton;
	private JSplitPane splitPane;
	private boolean painted;
	private JSplitPane splitPane_1;
	private JScrollPane scrollPane_1;
	private MsOneTable libraryMsTable;
	private LCMSPlotPanel msPlot;
	private LCMSPlotToolbar msPlotToolbar;
	private LibraryMsFeature activeFeature;
	private Box horizontalBox;

	public static final String MASS_ACCURACY = "MASS_ACCURACY";
	public static final String RT_WINDOW = "RT_WINDOW";

	private static final Icon missingIdentificationsIcon = GuiUtils.getIcon("missingIdentifications", 32);

	public MissingIdentificationsDialog() {

		super(MRC2ToolBoxCore.getMainWindow(), "Missing identifications", false);
		setIconImage(((ImageIcon) missingIdentificationsIcon).getImage());

		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		setSize(new Dimension(800, 640));
		setPreferredSize(new Dimension(800, 640));
		preferences = Preferences.userNodeForPackage(this.getClass());

		//toolBar = new MissingIdsToolbar(this);
		JPanel searchParamsPanel = new JPanel();
		searchParamsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		searchParamsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		searchParamsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(searchParamsPanel, BorderLayout.NORTH);
		GridBagLayout gbl_searchParamsPanel = new GridBagLayout();
		gbl_searchParamsPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0};
		searchParamsPanel.setLayout(gbl_searchParamsPanel);

		JLabel lblMassAccuracyPpm = new JLabel("Mass accuracy, ppm");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 0;
		searchParamsPanel.add(lblMassAccuracyPpm, gbc_lblNewLabel_1);

		massAccuracyTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getPpmFormat());
		massAccuracyTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField = new GridBagConstraints();
		gbc_formattedTextField.insets = new Insets(0, 0, 0, 5);
		gbc_formattedTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField.gridx = 1;
		gbc_formattedTextField.gridy = 0;
		searchParamsPanel.add(massAccuracyTextField, gbc_formattedTextField);

		JLabel lblNewLabel = new JLabel("RT window, min");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.gridx = 2;
		gbc_lblNewLabel_2.gridy = 0;
		searchParamsPanel.add(lblNewLabel, gbc_lblNewLabel_2);

		rtWindowTextField = new JFormattedTextField(MRC2ToolBoxConfiguration.getRtFormat());
		rtWindowTextField.setColumns(10);
		GridBagConstraints gbc_formattedTextField_1 = new GridBagConstraints();
		gbc_formattedTextField_1.insets = new Insets(0, 0, 0, 5);
		gbc_formattedTextField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_formattedTextField_1.gridx = 3;
		gbc_formattedTextField_1.gridy = 0;
		searchParamsPanel.add(rtWindowTextField, gbc_formattedTextField_1);

		horizontalBox = Box.createHorizontalBox();
		GridBagConstraints gbc_horizontalBox = new GridBagConstraints();
		gbc_horizontalBox.gridx = 4;
		gbc_horizontalBox.gridy = 0;
		searchParamsPanel.add(horizontalBox, gbc_horizontalBox);

		splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		missingIdsTable = new MissingIdsTable(this);
		missingIdsTable.getSelectionModel().addListSelectionListener(this);
		JScrollPane scrollPane = new JScrollPane(missingIdsTable);
		splitPane.setLeftComponent(scrollPane);

		splitPane_1 = new JSplitPane();
		splitPane.setRightComponent(splitPane_1);

		libraryMsTable = new MsOneTable();
		scrollPane_1 = new JScrollPane(libraryMsTable);
		splitPane_1.setLeftComponent(scrollPane_1);

		JPanel plotPanel = new JPanel(new BorderLayout(0, 0));
		msPlot = new LCMSPlotPanel(PlotType.SPECTRUM);
		plotPanel.add(msPlot, BorderLayout.CENTER);
		msPlotToolbar = 
				new LCMSPlotToolbar(msPlot, PlotType.SPECTRUM, this);
		plotPanel.add(msPlotToolbar, BorderLayout.NORTH);
		splitPane_1.setRightComponent(plotPanel);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		closeButton = new JButton("Close dialog");
		panel.add(closeButton);
		closeButton.addActionListener(al);

		JRootPane rootPane = SwingUtilities.getRootPane(closeButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		loadPreferences();

		pack();
	}

	public void loadFeatureData(LibraryMsFeature feature) {

		activeFeature = feature;

		//	TODO
		//	msPlot.showMsForLibraryFeature(activeFeature, true);
		libraryMsTable.setTableModelFromMsFeature(activeFeature);
	}

	public double getMassAccuracy() {

		return Double.parseDouble(massAccuracyTextField.getText());
	}

	public double getRtWindow() {

		return Double.parseDouble(rtWindowTextField.getText());
	}

	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.FIND_REATURES_BY_ADDUCT_MASS.getName()))
			findFeaturesByAdductMass();
	}

	private void findFeaturesByAdductMass() {

		if(missingIdsTable.getSelectedRow() == -1)
			return;

		Collection<MsFeature> features = missingIdsTable.getSelectedFeatures();
		if(features.isEmpty())
			return;

		//	TODO

/*			if(features.isEmpty()) {

				MsFeature feature = features.iterator().next();
				if(feature == null)
					return;

				if(feature.getSpectrum() == null) {

					MessageDialogue.showErrorMsg("No spectra available!", this);
					return;
				}
				Collection<Double> monoisotopicAdductMasses = new HashSet<Double>();
				for(ChemicalModification adduct : feature.getSpectrum().getAdducts())
					monoisotopicAdductMasses.add(feature.getSpectrum().getMsForAdduct(adduct)[0].getMz());

				double massAccuracyPpm = getMassAccuracy();
				Range rtRange = new Range(feature.getRetentionTime() - getRtWindow(), feature.getRetentionTime() + getRtWindow());

				FeatureStatisticsPanel panel = (FeatureStatisticsPanel) CefAnalyzerCore.getMainWindow().getPanel(PanelList.FEATURE_DATA);
				panel.findFeaturesByAdductMasses(monoisotopicAdductMasses, massAccuracyPpm, rtRange );
				CefAnalyzerCore.getMainWindow().showPanel(PanelList.FEATURE_DATA);
			}*/

	}

	public void populateMissingIdsTable(Set<MsFeature> identified, Collection<CompoundLibrary> libraries) {

		clearPanel();

		//	TODO

/*		Collection<String>presentIds =
			identified.stream().map(f -> f.getPrimaryIdentity().getLibraryTargetId()).distinct().collect(Collectors.toSet());

		HashMap<CompoundLibrary, Collection<MsFeature>>unidentified =
				new HashMap<CompoundLibrary, Collection<MsFeature>>();
		for(CompoundLibrary l : libraries) {

			for(CompoundLibrary loaded : CefAnalyzerCore.getActiveMsLibraries()) {

				if(l.getLibraryId().equals(loaded.getLibraryId()))
					unidentified.put(loaded,  loaded.getFeaturesNotInList(presentIds));
			}
		}
		missingIdsTable.setTableModelFromFeatureMap(unidentified);*/
	}

	@Override
	public void paint(Graphics g) {

		super.paint(g);

		if (!painted) {

			painted = true;
			splitPane.setDividerLocation(0.5);
			splitPane.setResizeWeight(0.5);
			splitPane_1.setDividerLocation(0.25);
			splitPane_1.setResizeWeight(0.25);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()) {

			if(missingIdsTable.getSelectedRow() > -1) {

				Collection<MsFeature> features = missingIdsTable.getSelectedFeatures();
				if(!features.isEmpty()) {
					loadFeatureData((LibraryMsFeature) features.iterator().next());
				}
				else {
					clearFeatureDataPanel();
				}
			}
		}
	}

	@Override
	public void setVisible(boolean b) {

		if(!b)
			savePreferences();

		super.setVisible(b);
	}

	private void clearFeatureDataPanel() {

		msPlot.removeAllDataSets();
		libraryMsTable.clearTable();
	}

	public synchronized void clearPanel() {

		clearFeatureDataPanel();
		missingIdsTable.clearTable();
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;

		double massAcc = preferences.getDouble(MASS_ACCURACY, 20.0d);
		massAccuracyTextField.setText(Double.toString(massAcc));

		double maxRt = preferences.getDouble(RT_WINDOW, 2.0d);
		rtWindowTextField.setText(Double.toString(maxRt));
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());

		preferences.putDouble(MASS_ACCURACY, Double.parseDouble(massAccuracyTextField.getText()));
		preferences.putDouble(RT_WINDOW, Double.parseDouble(rtWindowTextField.getText()));
	}
}






















