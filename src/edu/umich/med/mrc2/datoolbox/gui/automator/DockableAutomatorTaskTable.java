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

package edu.umich.med.mrc2.datoolbox.gui.automator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.gui.ComponentCellRenderer;
import edu.umich.med.mrc2.datoolbox.taskcontrol.impl.WrappedTask;

public class DockableAutomatorTaskTable extends DefaultSingleCDockable{

	private static final Icon componentIcon = GuiUtils.getIcon("script", 16);

	private BasicTable taskTable;
	private JScrollPane qeueScrollPane;

	private JPopupMenu popupMenu;

	private JMenu priorityMenu;

	@SuppressWarnings("unused")
	private JMenuItem
		highPriorityMenuItem,
		normalPriorityMenuItem,
		cancelTaskMenuItem,
		cancelAllMenuItem,
		restartMenuItem;

	public DockableAutomatorTaskTable(ActionListener listener) {

		super("DockableAutomatorTaskTable", componentIcon, "Progress monitor", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0,0));

		taskTable = new BasicTable(MRC2ToolBoxCore.getTaskController().getTaskQueue());
		taskTable.setAlignmentY(Component.TOP_ALIGNMENT);
		taskTable.setAlignmentX(Component.LEFT_ALIGNMENT);
		taskTable.setCellSelectionEnabled(false);
		taskTable.setColumnSelectionAllowed(false);
		taskTable.setRowSelectionAllowed(true);
		taskTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		taskTable.setDefaultRenderer(JComponent.class, new ComponentCellRenderer());
		taskTable.getTableHeader().setReorderingAllowed(false);
		taskTable.setAutoCreateRowSorter(true);
		taskTable.getColumnModel().getColumn(0).setPreferredWidth(350);

		qeueScrollPane = new JScrollPane(taskTable);
		qeueScrollPane.setAlignmentY(Component.TOP_ALIGNMENT);
		qeueScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		qeueScrollPane.setViewportView(taskTable);

		popupMenu = new JPopupMenu();

		priorityMenu = new JMenu("Set priority...");

		highPriorityMenuItem = GuiUtils.addMenuItem(priorityMenu,
				MainActionCommands.SET_HIGH_PRIORITY_COMMAND.getName(), listener,
				MainActionCommands.SET_HIGH_PRIORITY_COMMAND.getName());

		normalPriorityMenuItem = GuiUtils.addMenuItem(priorityMenu,
				MainActionCommands.SET_NORMAL_PRIORITY_COMMAND.getName(), listener,
				MainActionCommands.SET_NORMAL_PRIORITY_COMMAND.getName());

		popupMenu.add(priorityMenu);

		cancelTaskMenuItem = GuiUtils.addMenuItem(popupMenu,
				MainActionCommands.CANCEL_SELECTED_TASK_COMMAND.getName(), listener,
				MainActionCommands.CANCEL_SELECTED_TASK_COMMAND.getName());

		cancelAllMenuItem = GuiUtils.addMenuItem(popupMenu,
				MainActionCommands.CANCEL_ALL_TASKS_COMMAND.getName(), listener,
				MainActionCommands.CANCEL_ALL_TASKS_COMMAND.getName());

		restartMenuItem = GuiUtils.addMenuItem(popupMenu,
				MainActionCommands.RESTART_SELECTED_TASK_COMMAND.getName(), listener,
				MainActionCommands.RESTART_SELECTED_TASK_COMMAND.getName());

		taskTable.setComponentPopupMenu(popupMenu);

		add(qeueScrollPane, BorderLayout.CENTER);
	}

	public Task[] getSelectedTasks() {

		WrappedTask currentQueue[] = MRC2ToolBoxCore.getTaskController().getTaskQueue().getQueueSnapshot();
		ArrayList<Task>selected = new ArrayList<Task>();

		int[] selectedRows = taskTable.getSelectedRows();

		for (int i : selectedRows) {

			if ((i < currentQueue.length) && (i >= 0))
				selected.add(currentQueue[taskTable.convertRowIndexToModel(i)].getActualTask());
		}
		return selected.toArray(new Task[selected.size()]);
	}
}




















