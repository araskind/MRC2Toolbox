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

package edu.umich.med.mrc2.datoolbox.gui.integration.dsmanager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureClusterSet;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.dereplication.ClusterDisplayPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.utils.MetabolomicsProjectUtils;

public class MsFeatureClusterSetManagerDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static final Icon dialogIcon = GuiUtils.getIcon("clusterFeatureTable", 32);
	
	private MsFeatureClusterSetTable clusterSetTable;
	private JButton openDataSetButton;
	private ActionListener parentActionListener;
	private NewFeatureSubsetFromClustersDialog setNameDialog;
	
	public MsFeatureClusterSetManagerDialog(ActionListener actionListener) {
		super();
		this.parentActionListener = actionListener;
		setTitle("Feature cluster set manager");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setPreferredSize(new Dimension(600, 250));
		setSize(new Dimension(600, 250));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		JPanel dataPanel = new JPanel(new BorderLayout(0,0));
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		clusterSetTable = new MsFeatureClusterSetTable();
		clusterSetTable.addTablePopupMenu(
				new MsFeatureClusterSetTablePopupMenu(this, clusterSetTable));
		
		dataPanel.add(new JScrollPane(clusterSetTable), BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) buttonPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		JButton btnCancel = new JButton("Cancel");
		buttonPanel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);
		openDataSetButton = new JButton(
				MainActionCommands.LOAD_SELECTED_FEATURE_CLUSTER_SET.getName());
		openDataSetButton.setActionCommand(
				MainActionCommands.LOAD_SELECTED_FEATURE_CLUSTER_SET.getName());
		openDataSetButton.addActionListener(actionListener);
		clusterSetTable.addMouseListener(
				new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2) {
							openDataSetButton.doClick();
						}
					}
				});
		buttonPanel.add(openDataSetButton);
		JRootPane rootPane = SwingUtilities.getRootPane(openDataSetButton);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(openDataSetButton);

		pack();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.LOAD_SELECTED_FEATURE_CLUSTER_SET.getName()))
			openDataSetButton.doClick();
		
		if(command.equals(MainActionCommands.DELETE_SELECTED_CLUSTER_SETS.getName()))
			deleteSelectedDataSets();
		
		if(command.equals(MainActionCommands.NEW_FEATURE_COLLECTION_FROM_SELECTED_CLUSTER_SETS.getName()))
			createNewFeatureCollectionFromSelectedDataSets();
		
		if(command.equals(MainActionCommands.CREATE_NEW_FEATURE_SUBSET_COMMAND.getName()))
			addNewFeatureSubsetToProject();
	}
	
	private void addNewFeatureSubsetToProject() {
		
		DataAnalysisProject experiment = MRC2ToolBoxCore.getActiveMetabolomicsExperiment();
		Set<MsFeatureClusterSet>selected = setNameDialog.getSourceClusterSets();
		String fsName = setNameDialog.getFeatureSubsetName();
		List<String>errors = new ArrayList<>();
		if(fsName.isEmpty())
			errors.add("Feature subset name must be specified");
			
		if(experiment.featureSetNameExists(fsName))
			errors.add("Feature subset \"" + fsName + "\" already exists in this project");
			
		if(!errors.isEmpty()){
		    MessageDialog.showErrorMsg(
		            StringUtils.join(errors, "\n"), setNameDialog);
		    return;
		}
		Map<DataPipeline, Set<MsFeature>> featureMap = new TreeMap<>();
		for(MsFeatureClusterSet cs : selected) {
			
			for(DataPipeline dp : cs.getDataPipelines()) {
				featureMap.computeIfAbsent(dp, v -> new HashSet<MsFeature>());
				featureMap.get(dp).addAll(cs.getFeaturesForDataPipeline(dp));
			}			
		}
		for(Entry<DataPipeline, Set<MsFeature>> fme : featureMap.entrySet()) {
			
			String fsNameComplete = fsName;
			if(featureMap.size() > 1)
				fsNameComplete = fsName + " (" + fme.getKey().getName() + ")";
			
			MsFeatureSet newSet = new MsFeatureSet(fsNameComplete, fme.getValue());
			
			experiment.addFeatureSetForDataPipeline(newSet, fme.getKey());
			MainWindow.getExperimentSetupDraw().
				getFeatureSubsetPanel().addSetListeners(newSet);
			
			if(fme.getKey().equals(experiment.getActiveDataPipeline()))
				MetabolomicsProjectUtils.switchActiveMsFeatureSet(newSet);
		}
		setNameDialog.dispose();
	}

	private void createNewFeatureCollectionFromSelectedDataSets() {

		Set<MsFeatureClusterSet>selected = 
				clusterSetTable.getSelectedMsFeatureClusterSets();
		if(selected.isEmpty())
			return;
		
		setNameDialog = new NewFeatureSubsetFromClustersDialog(this, selected);
		setNameDialog.setLocationRelativeTo(this);
		setNameDialog.setVisible(true);
	}

	private void deleteSelectedDataSets() {

		Set<MsFeatureClusterSet>selected = 
				clusterSetTable.getSelectedMsFeatureClusterSets();
		if(selected.isEmpty())
			return;
		
		int res = MessageDialog.showChoiceWithWarningMsg(
				"Do you want to delete selected data sets?", this);

		if(res == JOptionPane.YES_OPTION) {
			
			for(MsFeatureClusterSet ds : selected) {
				
				if(ds.isActive() && parentActionListener instanceof ClusterDisplayPanel)
					((ClusterDisplayPanel)parentActionListener).clearPanel();
					
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().deleteFeatureClusterSet(ds);	
			}
		}		
	}

	public void loadDataIntegrationSetsForProject(DataAnalysisProject project) {
		clusterSetTable.setTableModelFromExperiment(project);
	}
	
	public MsFeatureClusterSet getSelectedMsFeatureClusterSet() {
		return clusterSetTable.getSelectedMsFeatureClusterSet();
	}
	
	public Set<MsFeatureClusterSet> getSelectedMsFeatureClusterSets() {
		return clusterSetTable.getSelectedMsFeatureClusterSets();
	}

}
