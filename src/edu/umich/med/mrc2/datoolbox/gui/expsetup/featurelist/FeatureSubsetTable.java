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

package edu.umich.med.mrc2.datoolbox.gui.expsetup.featurelist;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.MsFeatureSet;
import edu.umich.med.mrc2.datoolbox.data.compare.NameComparator;
import edu.umich.med.mrc2.datoolbox.data.enums.FeatureSetProperties;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.fdata.cleanup.FeatureCleanupParameters;
import edu.umich.med.mrc2.datoolbox.gui.fdata.cleanup.FeatureDataCleanupDialog;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;
import edu.umich.med.mrc2.datoolbox.utils.MetabolomicsProjectUtils;

public class FeatureSubsetTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3122391202783544387L;
	private static final Icon enableSelectedIcon = GuiUtils.getIcon("checkboxFull", 24);
	private static final Icon infoIcon = GuiUtils.getIcon("infoGreen", 24);

	public FeatureSubsetTable() {

		super();

		model = new FeatureSubsetTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<FeatureSubsetTableModel>((FeatureSubsetTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(FeatureSubsetTableModel.FEATURES_SUBSET_COLUMN),
				new NameComparator());
		
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		columnModel.getColumnById(FeatureSubsetTableModel.ACTIVE_COLUMN)
			.setCellRenderer(new RadioButtonRenderer());
		columnModel.getColumnById(FeatureSubsetTableModel.FEATURES_SUBSET_COLUMN)
			.setCellRenderer(new WordWrapCellRenderer());

		columnModel.getColumnById(
				FeatureSubsetTableModel.ACTIVE_COLUMN).setWidth(50);
		columnModel.getColumnById(
				FeatureSubsetTableModel.NUM_FEATURES_COLUMN).setWidth(150);	
//		columnModel.getColumnById(
//				FeatureSubsetTableModel.NUM_FEATURES_COLUMN).setMinWidth(100);	
		fixedWidthColumns.add(model.getColumnIndex(
				FeatureSubsetTableModel.ACTIVE_COLUMN));
		fixedWidthColumns.add(model.getColumnIndex(
				FeatureSubsetTableModel.NUM_FEATURES_COLUMN));
		
		this.addMouseListener(

		        new MouseAdapter(){
		          public void mouseClicked(MouseEvent e){

		            if (e.getClickCount() == 2)
		            	activateSelectedMsFeatureSet();
		          }
	        });
		addFeatureSetPropertiesPopup();
		finalizeLayout();
	}
	
	public void actionPerformed(ActionEvent event) {
		
		String command = event.getActionCommand();
		for(FeatureSetProperties property : FeatureSetProperties.values()) {
			
			if(command.equals(property.name())) {
				
				showMsFeatureSetproperty(property);
				return;
			}
		}
		if(command.equals(MainActionCommands.SET_ACTIVE_FEATURE_SUBSET_COMMAND.getName()))
			activateSelectedMsFeatureSet();
		
		super.actionPerformed(event);
	}
	
	private void showMsFeatureSetproperty(FeatureSetProperties property) {
		
		MsFeatureSet msfSet = getSelectedMsFeatureSet();
		if(msfSet == null || msfSet.getProperties().isEmpty())
			return;
		
		Object propertyObject = msfSet.getProperty(property);
		if(propertyObject == null)
			return;
		
		if(FeatureCleanupParameters.class.isAssignableFrom(propertyObject.getClass()))			
			showFeatureCleanupParameters((FeatureCleanupParameters)propertyObject);		
	}

	private void showFeatureCleanupParameters(FeatureCleanupParameters propertyObject) {
		
		FeatureDataCleanupDialog featureDataCleanupDialog = 
				new FeatureDataCleanupDialog(this, getSelectedMsFeatureSet());
		featureDataCleanupDialog.loadParameters(propertyObject);
		featureDataCleanupDialog.setEditable(false);
		featureDataCleanupDialog.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow().getContentPane());
		featureDataCleanupDialog.setVisible(true);
	}

	private void addFeatureSetPropertiesPopup() {
		
		JPopupMenu popupMenu = new JPopupMenu();		
		popupMenu.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						int rowAtPoint = rowAtPoint(
								SwingUtilities.convertPoint(popupMenu, new Point(0, 0), FeatureSubsetTable.this));
						
						if (rowAtPoint > -1)
							setRowSelectionInterval(rowAtPoint, rowAtPoint);
						
						generateTablePopupMenu(rowAtPoint);
					}
				});
			}

			private void generateTablePopupMenu(int rowAtPoint) {
				
			    popupMenu.removeAll();
			    
			    MsFeatureSet msfSet = (MsFeatureSet) model.getValueAt(convertRowIndexToModel(rowAtPoint),
						model.getColumnIndex(FeatureSubsetTableModel.FEATURES_SUBSET_COLUMN));
			    
			    JMenuItem setActiveMenuItem = GuiUtils.addMenuItem(
			    		popupMenu, MainActionCommands.SET_ACTIVE_FEATURE_SUBSET_COMMAND.getName(), 
			    		FeatureSubsetTable.this, MainActionCommands.SET_ACTIVE_FEATURE_SUBSET_COMMAND.getName());
			    setActiveMenuItem.setIcon(enableSelectedIcon);
			    setActiveMenuItem.setEnabled(!msfSet.isActive());
			    
//			    if(msfSet == null || msfSet.getProperties() == null 
//			    		|| msfSet.getProperties().isEmpty()) {
//			    	popupMenu.revalidate();
//			    	return;
//			    }
			    for(FeatureSetProperties property : FeatureSetProperties.values()) {
			    	
			    	JMenuItem propItem = GuiUtils.addMenuItem(
			    			popupMenu, property.getName(), FeatureSubsetTable.this, property.name());
			    	propItem.setIcon(infoIcon);
			    	if(msfSet.getProperties() == null || msfSet.getProperty(property) == null)
			    		propItem.setEnabled(false);
			    }
			    popupMenu.revalidate();
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				// TODO Auto-generated method stub

			}
		});
		setComponentPopupMenu(popupMenu);
	}

	private void activateSelectedMsFeatureSet() {
		
		MsFeatureSet toActivate = getSelectedMsFeatureSet();
		if(toActivate == null || toActivate.isActive())
			return;
		
		MetabolomicsProjectUtils.switchActiveMsFeatureSet(toActivate);
	}
	
	public MsFeatureSet getSelectedMsFeatureSet() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (MsFeatureSet) model.getValueAt(convertRowIndexToModel(row),
				model.getColumnIndex(FeatureSubsetTableModel.FEATURES_SUBSET_COLUMN));
	}

	public void setModelFromProject(
			DataAnalysisProject currentProject, 
			DataPipeline activeDataPipeline) {

		((FeatureSubsetTableModel)model).setModelFromProject(currentProject, activeDataPipeline);
		adjustColumns();
	}
}
