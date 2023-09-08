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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.features;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.MSFeatureInfoBundle;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.gui.idworks.IDWorkbenchPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class AddFeaturesToCollectionDialog extends JDialog implements ActionListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7638441884088309136L;
	private static final Icon addFeatureCollectionIcon = GuiUtils.getIcon("newFeatureSubset", 32);
	private FeatureCollectionsTable featureCollectionsTable;
	private Collection<MSFeatureInfoBundle> featuresToAdd;
	private JCheckBox loadCollectionCheckBox;
	private IDWorkbenchPanel parentPanel;
	
	public AddFeaturesToCollectionDialog(
			Collection<MSFeatureInfoBundle> featuresToAdd,
			IDWorkbenchPanel parentPanel) {
		super();
		
		setTitle("Add " + Integer.toString(featuresToAdd.size()) + " features to selected collection");
		setIconImage(((ImageIcon) addFeatureCollectionIcon).getImage());
		setPreferredSize(new Dimension(800, 600));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		this.featuresToAdd = featuresToAdd;
		this.parentPanel = parentPanel;
		
		featureCollectionsTable = new FeatureCollectionsTable();
		populateCollectiosTable();
		getContentPane().add(new JScrollPane(featureCollectionsTable), BorderLayout.CENTER);

		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);
		
		loadCollectionCheckBox = 
				new JCheckBox("Load modified collection in the workbench");
		panel.add(loadCollectionCheckBox);
		
		Component horizontalStrut = Box.createHorizontalStrut(200);
		panel.add(horizontalStrut);

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
				new JButton(MainActionCommands.ADD_FEATURES_TO_SELECTED_COLLECTION_COMMAND.getName());
		btnSave.setActionCommand(
				MainActionCommands.ADD_FEATURES_TO_SELECTED_COLLECTION_COMMAND.getName());
		btnSave.addActionListener(this);
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

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.ADD_FEATURES_TO_SELECTED_COLLECTION_COMMAND.getName()))
			addFeaturesToSelectedCollection();
	}
	
	private void addFeaturesToSelectedCollection() {
		
		MsFeatureInfoBundleCollection selectedCollection = 
				featureCollectionsTable.getSelectedCollection();
		
		if(selectedCollection == null) {
			MessageDialog.showErrorMsg("Please select collection to add features.", this);
			return;
		}
		if(MRC2ToolBoxCore.getActiveOfflineRawDataAnalysisExperiment() == null)
			FeatureCollectionManager.addFeaturesToCollection(selectedCollection, featuresToAdd);
		else
			selectedCollection.addFeatures(featuresToAdd);
		
		if(loadCollectionCheckBox.isSelected())
			parentPanel.loadMSMSFeatureInformationBundleCollection(selectedCollection);
		
		dispose();
	}
}


