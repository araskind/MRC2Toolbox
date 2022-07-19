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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.ads;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.MinimalMSOneFeature;
import edu.umich.med.mrc2.datoolbox.data.msclust.MSMSClusteringParameterSet;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.FeatureListImportPanel;
import edu.umich.med.mrc2.datoolbox.gui.idworks.search.MSMSClusteringParametersPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class ActiveDataSetMZRTDataSearchDialog extends JDialog implements ActionListener, BackedByPreferences {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6166594084842739656L;
	private static final Icon searchIcon = GuiUtils.getIcon("searchIdActiveDataSet", 32);	
	private static final String PREFS_NODE = 
			"edu.umich.med.mrc2.datoolbox.ActiveDataSetMZRTDataSearchDialog";
	private Preferences preferences;
	private static final String BASE_DIRECTORY = "BASE_DIRECTORY";
	private FeatureListImportPanel featureListImportPanel;
	private File baseDirectory;
	private MSMSClusteringParametersPanel msmsClusteringParametersPanel;
	
	public ActiveDataSetMZRTDataSearchDialog(ActionListener parent) {
		
		super();
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setTitle("Search active MSMS data set by MZ/RT list");
		setIconImage(((ImageIcon)searchIcon).getImage());
		setPreferredSize(new Dimension(800, 800));	
		
		msmsClusteringParametersPanel = new MSMSClusteringParametersPanel();
		getContentPane().add(msmsClusteringParametersPanel, BorderLayout.NORTH);
		
		featureListImportPanel = new FeatureListImportPanel();
		getContentPane().add(featureListImportPanel, BorderLayout.CENTER);
		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel_1, BorderLayout.SOUTH);
		
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JButton btnCancel = new JButton("Cancel");
		panel_1.add(btnCancel);
		btnCancel.addActionListener(al);
		btnCancel.addActionListener(al);

		JButton resetButton = new JButton("Reset form");
		resetButton.addActionListener(this);
		resetButton.setActionCommand(MainActionCommands.IDTRACKER_RESET_FORM_COMMAND.getName());
		panel_1.add(resetButton);

		JButton searchButton = new JButton("Search ...");
		searchButton.addActionListener(parent);
		searchButton.setActionCommand(MainActionCommands.SEARCH_ACTIVE_DATA_SET_BY_MZ_RT_COMMAND.getName());
		panel_1.add(searchButton);
		
		JRootPane rootPane = SwingUtilities.getRootPane(searchButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(searchButton);
		
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
	
	public Collection<MinimalMSOneFeature>getSelectedFeatures(){
		return featureListImportPanel.getSelectedFeatures();
	}
	
	public Collection<MinimalMSOneFeature>getAllFeatures(){
		return featureListImportPanel.getAllFeatures();
	}
	
	public MSMSClusteringParameterSet getParameters() {
		return msmsClusteringParametersPanel.getParameters();
	}
	
	public Collection<String>validateParameters(){
		
		Collection<String>errors = 
				msmsClusteringParametersPanel.validateParameters();
		
//		if(getAllFeatures().isEmpty())
//			errors.add("Search list is empty.");
		
		return errors;
	}
	
	@Override
	public void loadPreferences(Preferences prefs) {
		preferences = prefs;
		baseDirectory =  new File(preferences.get(
				BASE_DIRECTORY, MRC2ToolBoxConfiguration.getDefaultDataDirectory()));
		featureListImportPanel.setBaseDirectory(baseDirectory);
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userRoot().node(PREFS_NODE));
	}

	@Override
	public void savePreferences() {
		preferences = Preferences.userRoot().node(PREFS_NODE);
		baseDirectory = featureListImportPanel.getBaseDirectory();
		preferences.put(BASE_DIRECTORY, baseDirectory.getAbsolutePath());
	}
}
