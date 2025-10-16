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

package edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.features.oper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureInfoBundleCollection;
import edu.umich.med.mrc2.datoolbox.data.compare.MsFeatureInfoBundleCollectionComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.database.ConnectionManager;
import edu.umich.med.mrc2.datoolbox.database.idt.FeatureCollectionUtils;
import edu.umich.med.mrc2.datoolbox.gui.idworks.fcolls.features.FeatureCollectionsTable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.SortedListModel;
import edu.umich.med.mrc2.datoolbox.main.FeatureCollectionManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class FeatureCollectionsOperationDialog extends JDialog implements ActionListener, ListSelectionListener {

	private static final long serialVersionUID = 1L;

	private static final Icon combSubFeatureCollectionIcon = GuiUtils.getIcon("combSubCollection", 32);

	private FeatureCollectionsTable featureCollectionsTable;
	private JList<MsFeatureInfoBundleCollection>collectionsToCombineList;
	private JList<MsFeatureInfoBundleCollection>collectionsToSubtractList;
	
	private static final String COMBINE_LIST_POPUP = "COMBINE_LIST_POPUP";
	private static final String SUBTRACT_LIST_POPUP = "SUBTRACT_LIST_POPUP";
	private JTextField newCollectionNameTextField;
	private JTextArea newCollectionDescriptionTextArea;

	public FeatureCollectionsOperationDialog() {
		super();
		setTitle("Combine / subtract feature collections");
		setIconImage(((ImageIcon) combSubFeatureCollectionIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		setSize(new Dimension(800, 800));
		setPreferredSize(new Dimension(800, 800));
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JPanel mainPanel = new JPanel(new BorderLayout(0,0));
		mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(mainPanel, BorderLayout.CENTER);
		
		featureCollectionsTable = new FeatureCollectionsTable();
		FeatureCollectionManager.refreshMsFeatureInfoBundleCollections();
		featureCollectionsTable.setTableModelFromFeatureCollectionList(
				FeatureCollectionManager.getEditableMsFeatureInfoBundleCollections());
		featureCollectionsTable.addTablePopupMenu(
				new FcollCombSubTablePopupMenu(this, featureCollectionsTable));
		 		
		mainPanel.add(new JScrollPane(featureCollectionsTable), BorderLayout.NORTH);
		
		JPanel panel_1 = new JPanel();
		mainPanel.add(panel_1, BorderLayout.CENTER);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		collectionsToCombineList = new JList<MsFeatureInfoBundleCollection>(
				new SortedListModel<MsFeatureInfoBundleCollection>(
						new MsFeatureInfoBundleCollectionComparator(SortProperty.Name)));
		
		FcollCombSubListPopupMenu combListPopup = new FcollCombSubListPopupMenu(this,null);
		combListPopup.setName(COMBINE_LIST_POPUP);
		collectionsToCombineList.setComponentPopupMenu(combListPopup);

		JScrollPane scrollPane = new JScrollPane(collectionsToCombineList);
		scrollPane.setBorder(new CompoundBorder(new EmptyBorder(10, 5, 5, 0), 
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
						new Color(160, 160, 160)), "Feature collections to combine", 
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		panel_1.add(scrollPane, gbc_scrollPane);
		
		collectionsToSubtractList = new JList<MsFeatureInfoBundleCollection>(
				new SortedListModel<MsFeatureInfoBundleCollection>(
						new MsFeatureInfoBundleCollectionComparator(SortProperty.Name)));
		FcollCombSubListPopupMenu subtrListPopup = new FcollCombSubListPopupMenu(this,null);
		subtrListPopup.setName(SUBTRACT_LIST_POPUP);
		collectionsToSubtractList.setComponentPopupMenu(subtrListPopup);
		
		JScrollPane scrollPane_1 = new JScrollPane(collectionsToSubtractList);
		scrollPane_1.setBorder(new CompoundBorder(new EmptyBorder(10, 5, 5, 0), 
				new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
						new Color(160, 160, 160)), "Feature collections to subtract", 
						TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0))));
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.gridx = 2;
		gbc_scrollPane_1.gridy = 0;
		panel_1.add(scrollPane_1, gbc_scrollPane_1);
		
		JLabel lblNewLabel = new JLabel("New collection name");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 1;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		newCollectionNameTextField = new JTextField();
		GridBagConstraints gbc_newCollectionNameTextField = new GridBagConstraints();
		gbc_newCollectionNameTextField.insets = new Insets(0, 0, 5, 0);
		gbc_newCollectionNameTextField.gridwidth = 2;
		gbc_newCollectionNameTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_newCollectionNameTextField.gridx = 1;
		gbc_newCollectionNameTextField.gridy = 1;
		panel_1.add(newCollectionNameTextField, gbc_newCollectionNameTextField);
		newCollectionNameTextField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Description");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 2;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		newCollectionDescriptionTextArea = new JTextArea();
		newCollectionDescriptionTextArea.setWrapStyleWord(true);
		newCollectionDescriptionTextArea.setLineWrap(true);
		newCollectionDescriptionTextArea.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_newCollectionDescriptionTextArea = new GridBagConstraints();
		gbc_newCollectionDescriptionTextArea.gridwidth = 3;
		gbc_newCollectionDescriptionTextArea.insets = new Insets(0, 0, 0, 5);
		gbc_newCollectionDescriptionTextArea.fill = GridBagConstraints.BOTH;
		gbc_newCollectionDescriptionTextArea.gridx = 0;
		gbc_newCollectionDescriptionTextArea.gridy = 3;
		panel_1.add(newCollectionDescriptionTextArea, gbc_newCollectionDescriptionTextArea);
		
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
				MainActionCommands.COMBINE_SUBTRACT_SELECTED_FEATURE_COLLECTIONS.getName());
		btnSave.setActionCommand(
				MainActionCommands.COMBINE_SUBTRACT_SELECTED_FEATURE_COLLECTIONS.getName());
		btnSave.addActionListener(this);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.ADD_FEATURE_COLLECTION_TO_COMBINE_LIST_COMMAND.getName()))
			addSelectedToCombineList();
		
		if(command.equals(MainActionCommands.ADD_FEATURE_COLLECTION_TO_SUBTRACT_LIST_COMMAND.getName())) 
			addSelectedToSubtractList();		
				
		if(command.equals(MainActionCommands.REMOVE_FEATURE_COLLECTION_FROM_LIST_COMMAND.getName())) {
			
			Component parent = ((Component)e.getSource()).getParent();
			removeCollectionFromList(parent.getName());
		}	
		if(command.equals(MainActionCommands.COMBINE_SUBTRACT_SELECTED_FEATURE_COLLECTIONS.getName()))
			combineSubtractSelectedFeatureCollections();
	}
	
	private void removeCollectionFromList(String name) {

		if(name.equals(COMBINE_LIST_POPUP)) {
			
			Collection<MsFeatureInfoBundleCollection> selected = getCollectionsToCombineList();
			if(selected.isEmpty())
				return;
			
			for(MsFeatureInfoBundleCollection c : selected) {
				
				((SortedListModel<MsFeatureInfoBundleCollection>)
						collectionsToCombineList.getModel()).removeElement(c);
			}
		}
		if(name.equals(SUBTRACT_LIST_POPUP)) {
			
			Collection<MsFeatureInfoBundleCollection> selected = getCollectionsToSubtractList();
			if(selected.isEmpty())
				return;
			
			for(MsFeatureInfoBundleCollection c : selected) {
				
				((SortedListModel<MsFeatureInfoBundleCollection>)
						collectionsToSubtractList.getModel()).removeElement(c);
			}
		}
	}

	private void addSelectedToCombineList() {

		MsFeatureInfoBundleCollection selected = 
				featureCollectionsTable.getSelectedCollection();
		if(selected == null || getCollectionsToCombineList().contains(selected))
			return;
		
		((SortedListModel<MsFeatureInfoBundleCollection>)
				collectionsToCombineList.getModel()).addElement(selected);		
	}

	private void addSelectedToSubtractList() {

		MsFeatureInfoBundleCollection selected = 
				featureCollectionsTable.getSelectedCollection();
		if(selected == null || getCollectionsToSubtractList().contains(selected))
			return;
		
		((SortedListModel<MsFeatureInfoBundleCollection>)
				collectionsToSubtractList.getModel()).addElement(selected);
	}

	private void combineSubtractSelectedFeatureCollections() {

		Collection<String>errors = new ArrayList<String>();

		Collection<MsFeatureInfoBundleCollection>toCombine = getCollectionsToCombineList();
		Collection<MsFeatureInfoBundleCollection>toSubtract = getCollectionsToSubtractList();
		
		if(toCombine.isEmpty())
			errors.add("No feature collections selected to combine.");
		
		int fCount = 0;
		for(MsFeatureInfoBundleCollection fc : toCombine) 
			fCount += fc.getCollectionSize();
		
		if(!toCombine.isEmpty() && fCount == 0)
			errors.add("No features in collections selected to combine.");
		
		if(toCombine.size() == 1 && toSubtract.isEmpty())
			errors.add("There must be more than one collection selected to combine\n"
					+ "or at least one collection in both \"To combine\" and \"To subtract\" lists.");
		
		String newName = getNewCollectionName();
		if(newName == null || newName.isEmpty())
			errors.add("No name specified for the new collection.");
		
		MsFeatureInfoBundleCollection existing = 
				FeatureCollectionManager.getEditableMsFeatureInfoBundleCollections().stream().
				filter(f -> f.getName().equalsIgnoreCase(newName)).
				findFirst().orElse(null);
		if(existing != null)
			errors.add("Collection \"" + newName + "\" already exists.");
			
		if(!errors.isEmpty()){
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), this);
			return;
		}
		UploadNewFeatureCollectionTask task = 
				new UploadNewFeatureCollectionTask(
						toCombine, toSubtract, newName, getNewCollectionDescription());
    	IndeterminateProgressDialog idp = new IndeterminateProgressDialog(
    			"Uploading new feature collection to database ...", this, task);
    	idp.setLocationRelativeTo(this);
    	idp.setVisible(true);	
	}

	class UploadNewFeatureCollectionTask extends LongUpdateTask {
		
		private Collection<MsFeatureInfoBundleCollection>toCombine;
		private Collection<MsFeatureInfoBundleCollection>toSubtract;
		private String collectionName;
		private String collectionDescription;
		private Collection<String>errors;
		
		public UploadNewFeatureCollectionTask(
				Collection<MsFeatureInfoBundleCollection> toCombine,
				Collection<MsFeatureInfoBundleCollection> toSubtract, 
				String collectionName,
				String collectionDescription) {
			super();
			this.toCombine = toCombine;
			this.toSubtract = toSubtract;
			this.collectionName = collectionName;
			this.collectionDescription = collectionDescription;
			errors = new ArrayList<String>();
		}

		@Override
		public Void doInBackground() {
						
			Set<String>newCollectionFeatureIds = new TreeSet<String>();
			try {
				newCollectionFeatureIds = getFeatureIdsForNewCollection();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				errors.add("Failed to retrieve feature IDs from the database.");
				return null;
			}
			if(newCollectionFeatureIds == null || newCollectionFeatureIds.isEmpty()) {
				errors.add("No features in the new collection.");
				return null;
			}	
			try {
				insertNewCollectionInDatabase(newCollectionFeatureIds);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			FeatureCollectionManager.refreshMsFeatureInfoBundleCollections();
			return null;
		}
		
		private void insertNewCollectionInDatabase(
				Set<String>newCollectionFeatureIds) throws Exception{
			
			Connection conn = ConnectionManager.getConnection();			
			MsFeatureInfoBundleCollection newCollection = 
					new MsFeatureInfoBundleCollection(
							null, 
							collectionName,
							collectionDescription,
							new Date(), 
							new Date(),
							MRC2ToolBoxCore.getIdTrackerUser());
			String newCollectionId = 
					FeatureCollectionUtils.addNewMsFeatureInformationBundleCollection(
							newCollection, conn);			
			FeatureCollectionUtils.addFeaturesToCollection(
					newCollectionId, 
					newCollectionFeatureIds,
					conn);
			ConnectionManager.releaseConnection(conn);
		}
		
		private Set<String>getFeatureIdsForNewCollection() throws Exception{
			
			Set<String>idSet = new TreeSet<String>();
			
			Connection conn = ConnectionManager.getConnection();
			for(MsFeatureInfoBundleCollection fc : toCombine) {
				Set<String>idsToAdd = 
						FeatureCollectionUtils.getFeatureIdsForMsFeatureInfoBundleCollection(
								fc.getId(), conn);
				if(idsToAdd != null && !idsToAdd.isEmpty())
					idSet.addAll(idsToAdd);
			}
			for(MsFeatureInfoBundleCollection fc : toSubtract) {
				Set<String>idsToRemove = 
						FeatureCollectionUtils.getFeatureIdsForMsFeatureInfoBundleCollection(
								fc.getId(), conn);
				if(idsToRemove != null && !idsToRemove.isEmpty())
					idSet.removeAll(idsToRemove);
			}	
			ConnectionManager.releaseConnection(conn);
			
			return idSet;
		}
		
		public void done() {			
			
			super.done();
			if(!errors.isEmpty()){
				MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), 
						FeatureCollectionsOperationDialog.this);
				return;
			}
			else
				FeatureCollectionsOperationDialog.this.dispose();
		}
	}
	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub

	}
	
	private Collection<MsFeatureInfoBundleCollection> getCollectionsToCombineList() {
		
		Collection<MsFeatureInfoBundleCollection>toCombine = 
				new ArrayList<MsFeatureInfoBundleCollection>();
        for(int i = 0; i< collectionsToCombineList.getModel().getSize();i++)
        	toCombine.add(collectionsToCombineList.getModel().getElementAt(i));
        
		return toCombine;
	}
	
	private Collection<MsFeatureInfoBundleCollection> getCollectionsToSubtractList() {
		
		Collection<MsFeatureInfoBundleCollection>toSubtract = 
				new ArrayList<MsFeatureInfoBundleCollection>();
        for(int i = 0; i< collectionsToSubtractList.getModel().getSize();i++)
        	toSubtract.add(collectionsToSubtractList.getModel().getElementAt(i));
        
		return toSubtract;
	}
	
	private String getNewCollectionName() {
		return newCollectionNameTextField.getText().trim();
	}
	
	private String getNewCollectionDescription() {
		return newCollectionDescriptionTextArea.getText().trim();
	}
}








