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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Date;

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

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.BinnerAdductList;
import edu.umich.med.mrc2.datoolbox.database.idt.BinnerUtils;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class AnnotationListManagerDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private BinnerAnnotationListsTable balTable;
	private BinnerAnnotationListManagerToolbar toolbar;
	private AnnotationListEditorDialog annotationListEditorDialog;
	private ActionListener listener;

	public AnnotationListManagerDialog(ActionListener listener) {
		super();
		setPreferredSize(new Dimension(800, 480));
		setSize(new Dimension(800, 480));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.listener = listener;
		
		toolbar = new BinnerAnnotationListManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		JPanel dataPanel = new JPanel(new BorderLayout(0, 0));
		dataPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(dataPanel, BorderLayout.CENTER);
		balTable = new BinnerAnnotationListsTable();
		dataPanel.add(new JScrollPane(balTable), BorderLayout.CENTER);
		balTable.addMouseListener(
				new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						if (e.getClickCount() == 2) {

							BinnerAdductList binnerAdductList = balTable.getSelectedBinnerAdductList();
							if(binnerAdductList != null)
								showAnnotationListEditor(binnerAdductList);
						}
					}
				});
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);
		JButton btnLoad = new JButton(MainActionCommands.OPEN_BINNER_ANNOTATION_LIST_COMMAND.getName());
		btnLoad.setActionCommand(MainActionCommands.OPEN_BINNER_ANNOTATION_LIST_COMMAND.getName());
		btnLoad.addActionListener(this);
		panel.add(btnLoad);
		
		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);
		JRootPane rootPane = SwingUtilities.getRootPane(btnCancel);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);		
		loadAnnotationLists();
	}

	private void loadAnnotationLists() {
		// TODO Auto-generated method stub
		balTable.setTableModelFromBinnerAdductCollection(
				IDTDataCache.getBinnerAdductListCollection());
	}
	
	public void refreshAnnotationLists() {

		IDTDataCache.refreshBinnerAdductListCollection();
		balTable.setTableModelFromBinnerAdductCollection(
				IDTDataCache.getBinnerAdductListCollection());
	}
	
	public BinnerAdductList getSelectedBinnerAdductList() {
		return balTable.getSelectedBinnerAdductList();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		
		if(command.equals(MainActionCommands.NEW_BINNER_ANNOTATION_LIST_COMMAND.getName()))
			showAnnotationListEditor(null);

		if(command.equals(MainActionCommands.EDIT_BINNER_ANNOTATION_LIST_COMMAND.getName())) {
			
			BinnerAdductList binnerAdductList = balTable.getSelectedBinnerAdductList();
			if(binnerAdductList != null)
				showAnnotationListEditor(binnerAdductList);
		}

		if(command.equals(MainActionCommands.DELETE_BINNER_ANNOTATION_LIST_COMMAND.getName()))
			deleteActiveAnnotationList();

		if(command.equals(MainActionCommands.SAVE_BINNER_ANNOTATION_LIST_COMMAND.getName()))
			saveAndReloadAnnotationList();
		
		if(command.equals(MainActionCommands.OPEN_BINNER_ANNOTATION_LIST_COMMAND.getName())) 
			loadAnnotationListForBinnerProcessingSetup();
	}
	
	private void loadAnnotationListForBinnerProcessingSetup() {
		
		BinnerAdductList binnerAdductList = balTable.getSelectedBinnerAdductList();
		
		if(binnerAdductList == null)
			return;
		
		if(listener instanceof AnnotationsSelectorPanel) {		
			((AnnotationsSelectorPanel)listener).loadBinnerAdductList(binnerAdductList);
			dispose();
		}
	}

	private void deleteActiveAnnotationList() {

		BinnerAdductList binnerAdductList = balTable.getSelectedBinnerAdductList();
		
		if(binnerAdductList == null)
			return;
		
		if(!IDTUtils.isSuperUser(this))
			return;
		
		int res = MessageDialog.showChoiceWithWarningMsg(
				"Are you sure you want to delete Binner "
				+ "annotation list \"" + binnerAdductList.getName() +"\" ", this);
		if(res == JOptionPane.YES_OPTION) {
			
			try {
				BinnerUtils.deleteBinnerAdductList(binnerAdductList);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			refreshAnnotationLists();
		}
		if(listener instanceof AnnotationsSelectorPanel) {
			BinnerAdductList loaded = ((AnnotationsSelectorPanel)listener).getBinnerAdductList();
			if(loaded != null && loaded.equals(binnerAdductList))			
				((AnnotationsSelectorPanel)listener).clearAnnotationList();
		}
	}

	private void showAnnotationListEditor(BinnerAdductList binnerAdductList) {

		if(binnerAdductList != null && !MRC2ToolBoxCore.getIdTrackerUser().equals(binnerAdductList.getOwner())) {
			
			MessageDialog.showWarningMsg("Binner annotation list \"" +
					binnerAdductList.getName() + "\" may only be edited by its owner", this);
			return;
		}
		annotationListEditorDialog = new AnnotationListEditorDialog(binnerAdductList, this);
		annotationListEditorDialog.setLocationRelativeTo(this);
		annotationListEditorDialog.setVisible(true);
	}
	
	private void saveAndReloadAnnotationList() {

		Collection<String>errors = annotationListEditorDialog.validateFormData();
		if(!errors.isEmpty()){
		    MessageDialog.showErrorMsg(
		            StringUtils.join(errors, "\n"), annotationListEditorDialog);
		    return;
		}
		BinnerAdductList listToSave = null;
		if(annotationListEditorDialog.getBinnerAdductList() == null) {
			
			listToSave = new BinnerAdductList(
					null,
					annotationListEditorDialog.getAnnotationListName(),
					annotationListEditorDialog.getAnnotationListDescription(),
					MRC2ToolBoxCore.getIdTrackerUser(), 
					new Date(), 
					new Date());
			listToSave.addComponents(annotationListEditorDialog.getBinnerAdductTierMap());
			
			try {
				BinnerUtils.addNewBinnerAdductList(listToSave);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {	//	Edit existing list
			listToSave = annotationListEditorDialog.getBinnerAdductList();
			listToSave.setName(annotationListEditorDialog.getAnnotationListName());
			listToSave.setDescription(annotationListEditorDialog.getAnnotationListDescription());
			listToSave.replaceComponents(annotationListEditorDialog.getBinnerAdductTierMap());
			try {
				BinnerUtils.editBinnerAdductList(listToSave);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		refreshAnnotationLists();
		if(listener instanceof AnnotationsSelectorPanel && listToSave != null) {
			BinnerAdductList loaded = ((AnnotationsSelectorPanel)listener).getBinnerAdductList();
			if(loaded != null && loaded.equals(listToSave))			
				((AnnotationsSelectorPanel)listener).loadBinnerAdductList(loaded);
		}		
		annotationListEditorDialog.dispose();
	}
}
