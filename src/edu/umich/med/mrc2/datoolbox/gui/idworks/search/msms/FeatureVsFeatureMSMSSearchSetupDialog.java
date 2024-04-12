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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.msms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.features.FeatureCollectionsTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class FeatureVsFeatureMSMSSearchSetupDialog extends JDialog 
		implements ActionListener, BackedByPreferences{
	
	private static final long serialVersionUID = -7638441884088309136L;
	
	private static final Icon featureVsFeatureMSMSSearchIcon = 
			GuiUtils.getIcon("msmsSearch", 32);
	
	private MSMSSearchParametersPanel msmsSearchParametersPanel;
	private FeatureCollectionsTable featureCollectionsTable;
	
	public FeatureVsFeatureMSMSSearchSetupDialog(ActionListener commandListener) {
		
		super();
		
		setTitle("Set up MSMS search vs MSMS feature library");
		setIconImage(((ImageIcon) featureVsFeatureMSMSSearchIcon).getImage());
		setPreferredSize(new Dimension(800, 600));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLayout(new BorderLayout());
		
		msmsSearchParametersPanel = new MSMSSearchParametersPanel();
		add(msmsSearchParametersPanel, BorderLayout.NORTH);
				
		featureCollectionsTable = new FeatureCollectionsTable();
		populateCollectiosTable();
		
		JScrollPane tableScroll= new JScrollPane(featureCollectionsTable);
		tableScroll.setBorder(new CompoundBorder(new EmptyBorder(5, 5, 5, 5), 
				new CompoundBorder(
					new TitledBorder(
						new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
								new Color(160, 160, 160)), "Select MSMS feature collection to use as library", 
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), 
					new EmptyBorder(10, 10, 10, 10))));
		
		add(tableScroll, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		add(panel, BorderLayout.SOUTH);
		
		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = 
				new JButton(MainActionCommands.FEATURE_VS_FEATURE_MSMS_SEARCH_RUN_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.FEATURE_VS_FEATURE_MSMS_SEARCH_RUN_COMMAND.getName());
		btnSave.addActionListener(commandListener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		pack();
	}
	
	private void populateCollectiosTable() {
		
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null) {
			
			FeatureCollectionManager.refreshMsFeatureInfoBundleCollections();
			featureCollectionsTable.setTableModelFromFeatureCollectionList(
					FeatureCollectionManager.getEditableMsFeatureInfoBundleCollections());
		}
		else {
			featureCollectionsTable.setTableModelFromFeatureCollectionList(
					MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment().
					getEditableMsFeatureInfoBundleCollections());			
		}
		featureCollectionsTable.clearSelection();
	}
	
	public Collection<String>validateSearchSetup(){
		
		Collection<String>errors = new ArrayList<String>();
		
	
		return errors;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

	}

	@Override
	public void loadPreferences(Preferences preferences) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadPreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void savePreferences() {
		// TODO Auto-generated method stub
		
	}
}


