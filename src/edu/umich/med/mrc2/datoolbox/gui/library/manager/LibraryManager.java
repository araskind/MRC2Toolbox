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

package edu.umich.med.mrc2.datoolbox.gui.library.manager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.data.CompoundLibrary;
import edu.umich.med.mrc2.datoolbox.database.idt.RemoteMsLibraryUtils;
import edu.umich.med.mrc2.datoolbox.gui.library.MsLibraryPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
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
	private JScrollPane scrollPane;
	private LibraryInfoDialog libraryInfoDialog;
	private DuplicateLibraryDialog duplicateLibraryDialog;

	private static final Icon libraryManagerIcon = GuiUtils.getIcon("libraryManager", 32);

	public LibraryManager(ActionListener listener) {

		super(MRC2ToolBoxCore.getMainWindow(), "Manage MS libraries", true);
		setIconImage(((ImageIcon) libraryManagerIcon).getImage());

		setSize(new Dimension(400, 220));
		setPreferredSize(new Dimension(600, 300));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));

		toolbar = new LibraryManagerToolbar(listener, this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		libraryListingTable = new LibraryListingTable();
		scrollPane = new JScrollPane(libraryListingTable);
		scrollPane.setViewportView(libraryListingTable);
		scrollPane.setPreferredSize(libraryListingTable.getPreferredScrollableViewportSize());
		//	scrollPane.addComponentListener(new ResizeTableAdjuster());

		getContentPane().add(scrollPane, BorderLayout.CENTER);

		libraryListingTable.addMouseListener(

	        new MouseAdapter(){

	          public void mouseClicked(MouseEvent e){

	            if (e.getClickCount() == 2)
	            	((MsLibraryPanel)listener).openLibrary();
	          }
        });
		libraryInfoDialog = new LibraryInfoDialog(this, listener);
		duplicateLibraryDialog = new DuplicateLibraryDialog(this, this);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		JRootPane rootPane = SwingUtilities.getRootPane(toolbar);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);

		refreshLibraryListing();

		pack();
		setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		setVisible(false);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (command.equals(MainActionCommands.NEW_LIBRARY_DIALOG_COMMAND.getName())){

			libraryInfoDialog.setLocationRelativeTo(this);
			libraryInfoDialog.initNewLibrary();
			libraryInfoDialog.setVisible(true);
		}

		if (command.equals(MainActionCommands.DUPLICATE_LIBRARY_DIALOG_COMMAND.getName())){

			CompoundLibrary selected = getSelectedLibrary();

			if(selected != null){

				duplicateLibraryDialog.loadLibrary(selected);
				duplicateLibraryDialog.setLocationRelativeTo(this);
				duplicateLibraryDialog.setVisible(true);
			}
		}
		if (command.equals(MainActionCommands.DUPLICATE_LIBRARY_COMMAND.getName()))
			duplicateLibrary();

		if (command.equals(MainActionCommands.EDIT_LIBRARY_DIALOG_COMMAND.getName())){

			CompoundLibrary selected = getSelectedLibrary();

			if(selected != null){

				libraryInfoDialog.setLocationRelativeTo(this);
				libraryInfoDialog.loadLibraryData(selected);
				libraryInfoDialog.setVisible(true);
			}
		}
	}

	private void duplicateLibrary() {

		CompoundLibrary source = duplicateLibraryDialog.getLibrary();
		String copyName  = duplicateLibraryDialog.getLibraryName();
		boolean libNameExists = false;
		try {
			libNameExists = RemoteMsLibraryUtils.libraryNameExists(copyName);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(libNameExists) {
			MessageDialog.showErrorMsg("Library named \"" + copyName + " already exists!", this);
			return;
		}
		DuplicateLibraryTask dlt = new DuplicateLibraryTask(
				source, copyName,
				duplicateLibraryDialog.clearRetention(),
				duplicateLibraryDialog.clearSpectra(),
				duplicateLibraryDialog.clearAnnotations());

		dlt.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(dlt);
	}

	public LibraryInfoDialog getLibraryInfoDialog(){

		return libraryInfoDialog;
	}

	public CompoundLibrary getSelectedLibrary(){

		CompoundLibrary selected = null;

		int libCol = libraryListingTable.getColumnIndex(LibraryListingTableModel.LIBRARY_COLUMN);

		if(libraryListingTable.getSelectedRow() > -1)
			selected = (CompoundLibrary) libraryListingTable.getValueAt(libraryListingTable.getSelectedRow(), libCol);

		return selected;
	}

	public void hideLibInfoDialog(){

		libraryInfoDialog.setVisible(false);
	}

	public void refreshLibraryListing(){

		Collection<CompoundLibrary>libList = new TreeSet<CompoundLibrary>();
		try {
			libList = RemoteMsLibraryUtils.getAllLibraries();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		libraryListingTable.setTableModelFromLibraryCollection(libList);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask)e.getSource()).removeTaskListener(this);

			if(e.getSource().getClass().equals(DuplicateLibraryTask.class)) {

				duplicateLibraryDialog.setVisible(false);
				refreshLibraryListing();
			}
		}
	}
}





























