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

package edu.umich.med.mrc2.datoolbox.gui.library.manager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collection;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.commons.lang.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCache;
import edu.umich.med.mrc2.datoolbox.database.idt.MSRTLibraryUtils;
import edu.umich.med.mrc2.datoolbox.gui.library.MsLibraryPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PanelList;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.library.DuplicateLibraryTask;

public class LibraryManager extends JDialog implements ActionListener, TaskListener{

	/**
	 *
	 */
	private static final long serialVersionUID = 4092462690776270043L;
	
	private LibraryManagerToolbar toolbar;
	private LibraryListingTable libraryListingTable;
	private LibraryInfoDialog libraryInfoDialog;
	private DuplicateLibraryDialog duplicateLibraryDialog;

	private Collection<CompoundLibrary>libList;
	private CompoundLibrary activeLibrary;

	private static final Icon libraryManagerIcon = GuiUtils.getIcon("libraryManager", 32);

	public LibraryManager(MsLibraryPanel parentPanel, CompoundLibrary activeLibrary) {

		super(MRC2ToolBoxCore.getMainWindow(), "Manage MS libraries", true);
		
		setIconImage(((ImageIcon) libraryManagerIcon).getImage());
		setSize(new Dimension(800, 640));
		setPreferredSize(new Dimension(800, 640));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		this.activeLibrary = activeLibrary;

		toolbar = new LibraryManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		libraryListingTable = new LibraryListingTable();
		libList = new TreeSet<CompoundLibrary>();
		refreshLibraryListing();
		libraryListingTable.addTablePopupMenu(
				new LibraryManagerPopupMenu(this, libraryListingTable));
		
		libraryListingTable.addMouseListener(

		        new MouseAdapter(){

		          public void mouseClicked(MouseEvent e){

		            if (e.getClickCount() == 2) {
		            	parentPanel.openLibraryFromDatabase(getSelectedLibrary());
		            	dispose();
		            }
		          }
	        });
		getContentPane().add(new JScrollPane(libraryListingTable), BorderLayout.CENTER);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JRootPane rootPane = SwingUtilities.getRootPane(toolbar);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		pack();
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.NEW_LIBRARY_DIALOG_COMMAND.getName()))
			showNewLibraryDialog();

		if (command.equals(MainActionCommands.DUPLICATE_LIBRARY_DIALOG_COMMAND.getName()))
			showDuplicateLibraryDialog();
		
		if (command.equals(MainActionCommands.DUPLICATE_LIBRARY_COMMAND.getName()))
			duplicateLibrary();

		if (command.equals(MainActionCommands.EDIT_LIBRARY_DIALOG_COMMAND.getName()))
			showEditLibraryDialog();
		
		if (command.equals(MainActionCommands.CREATE_NEW_LIBRARY_COMMAND.getName()))
			createNewLibrary();
		
		if (command.equals(MainActionCommands.EDIT_MS_LIBRARY_INFO_COMMAND.getName()))
			editLibraryInformation();
		
		if (command.equals(MainActionCommands.DELETE_LIBRARY_COMMAND.getName()))
			deleteSelectedLibrary();
	}
	
	private void createNewLibrary() {
		
		Collection<String>errors = libraryInfoDialog.validateLibraryData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), libraryInfoDialog);
			return;
		}		
		CompoundLibrary newLibrary = 
				new CompoundLibrary(
						libraryInfoDialog.getLibraryName(), 
						libraryInfoDialog.getLibraryDescription());
		newLibrary.setPolarity(libraryInfoDialog.getPolarity());

		String libId = null;
		try {
			libId = MSRTLibraryUtils.createNewLibrary(newLibrary);
		} catch (Exception e1) {
			MessageDialog.showErrorMsg("Library creation failed", libraryInfoDialog);
			e1.printStackTrace();
			return;
		}		
		refreshLibraryListing();		
		for (CompoundLibrary l : libList) {

			if (l.getLibraryId().equals(libId)) {

				MRC2ToolBoxCore.getActiveMsLibraries().add(newLibrary);
				((MsLibraryPanel)MRC2ToolBoxCore.getMainWindow().
						getPanel(PanelList.MS_LIBRARY)).reloadLibraryData(newLibrary);
				break;
			}
		}
		File inputFile = libraryInfoDialog.getInputLibraryFile();
		Collection<Adduct> adductList = libraryInfoDialog.getSelectedAdducts();
		
		if(inputFile != null && inputFile.exists()) {
			((MsLibraryPanel)MRC2ToolBoxCore.getMainWindow().
					getPanel(PanelList.MS_LIBRARY)).importLibraryFromFile(inputFile, adductList);
		}		
		libraryInfoDialog.dispose();
		dispose();
	}
	
	private void editLibraryInformation() {
		
		Collection<String>errors = libraryInfoDialog.validateLibraryData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), libraryInfoDialog);
			return;
		}	
		CompoundLibrary selected = libraryInfoDialog.getLibrary();
		String libraryName = libraryInfoDialog.getLibraryName();
		String libraryDescription = libraryInfoDialog.getLibraryDescription();
		selected.setLibraryName(libraryName);
		selected.setLibraryDescription(libraryDescription);
		try {
			MSRTLibraryUtils.updateLibraryInfo(selected);
		} catch (Exception e) {
			e.printStackTrace();
		}
		refreshLibraryListing();
		libraryInfoDialog.dispose();
		((MsLibraryPanel)MRC2ToolBoxCore.getMainWindow().
				getPanel(PanelList.MS_LIBRARY)).updateLibraryMenuAndLabel();
	}
	
	private void deleteSelectedLibrary() {

		CompoundLibrary selected = getSelectedLibrary();
		if(selected == null)
			return;

		int approve = MessageDialog.showChoiceWithWarningMsg(
				"Do you really want to delete the library \"" + 
						selected.getLibraryName() + "\"?", this);

		if (approve == JOptionPane.YES_OPTION) {

			MRC2ToolBoxCore.getActiveMsLibraries().remove(selected);
			try {
				MSRTLibraryUtils.deleteLibrary(selected);
			} catch (Exception e) {
				e.printStackTrace();
			}
			refreshLibraryListing();
		}
		((MsLibraryPanel)MRC2ToolBoxCore.getMainWindow().
				getPanel(PanelList.MS_LIBRARY)).finalizeLibraryDeletion(selected);
	}
	
	private void showNewLibraryDialog() {

		libraryInfoDialog = new LibraryInfoDialog(this);
		libraryInfoDialog.setLocationRelativeTo(this);
		libraryInfoDialog.initNewLibrary();
		libraryInfoDialog.setVisible(true);
	}

	private void showEditLibraryDialog() {
		
		CompoundLibrary selected = getSelectedLibrary();
		if(selected == null)
			return;

		libraryInfoDialog = new LibraryInfoDialog(this);
		libraryInfoDialog.setLocationRelativeTo(this);
		libraryInfoDialog.loadLibraryData(selected, false);
		libraryInfoDialog.setVisible(true);
	}
	
	private void showDuplicateLibraryDialog() {
		
		CompoundLibrary selected = getSelectedLibrary();
		if(selected == null)
			return;
//
//		duplicateLibraryDialog = new DuplicateLibraryDialog(this, this);
//		duplicateLibraryDialog.loadLibrary(selected);
//		duplicateLibraryDialog.setLocationRelativeTo(this);
//		duplicateLibraryDialog.setVisible(true);
		
		libraryInfoDialog = new LibraryInfoDialog(this);
		libraryInfoDialog.setLocationRelativeTo(this);
		libraryInfoDialog.loadLibraryData(selected, true);
		libraryInfoDialog.setVisible(true);
	}

	private void duplicateLibrary() {
		
		Collection<String>errors = libraryInfoDialog.validateLibraryData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(
					StringUtils.join(errors, "\n"), libraryInfoDialog);
			return;
		}
		CompoundLibrary sourceLibrary = libraryInfoDialog.getLibrary();
		CompoundLibrary destinationLibrary = new CompoundLibrary(
				libraryInfoDialog.getLibraryName(),
				libraryInfoDialog.getLibraryDescription(),
				libraryInfoDialog.getPolarity());
		DuplicateLibraryTask dlt = new DuplicateLibraryTask(
				sourceLibrary, 
				destinationLibrary,
				libraryInfoDialog.preserveSpectraOnCopy(),
				libraryInfoDialog.clearRetention(),
				libraryInfoDialog.clearAnnotations(),
				libraryInfoDialog.getSelectedAdducts());
		dlt.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(dlt);
		libraryInfoDialog.dispose();
	}

	public LibraryInfoDialog getLibraryInfoDialog(){
		return libraryInfoDialog;
	}

	public CompoundLibrary getSelectedLibrary(){
		return libraryListingTable.getSelectedLibrary();
	}

	public synchronized void refreshLibraryListing(){

		IDTDataCache.refreshMsRtLibraryList();		
		libList = IDTDataCache.getMsRtLibraryList();
		libraryListingTable.setTableModelFromLibraryCollection(
				libList, activeLibrary);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);
			if(e.getSource().getClass().equals(DuplicateLibraryTask.class))
				refreshLibraryListing();		
		}
	}
}





























