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

package edu.umich.med.mrc2.datoolbox.taskcontrol.impl;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.table.AbstractTableModel;

import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskPriority;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.gui.LabeledProgressBar;

public class TaskQueue extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 948023765995647706L;

	private static final int DEFAULT_CAPACITY = 64;

	private static final String columns[] = { "Item", "Priority", "Status", "% done" };

	/**
	 * This array stores the actual tasks
	 */
	private WrappedTask[] queue;

	/**
	 * Current size of the queue
	 */
	private int size;

	private Hashtable<Integer, LabeledProgressBar> progressBars;

	TaskQueue() {

		size = 0;
		queue = new WrappedTask[DEFAULT_CAPACITY];
		progressBars = new Hashtable<Integer, LabeledProgressBar>();
	}

	synchronized void addWrappedTask(WrappedTask task) {

		if(queue.length == 0)
			queue = new WrappedTask[DEFAULT_CAPACITY];
		
		// If the queue is full, make a bigger queue
		if (size == queue.length) {

			WrappedTask[] newQueue = new WrappedTask[queue.length * 2];
			System.arraycopy(queue, 0, newQueue, 0, size);
			queue = newQueue;
		}

		queue[size] = task;
		size++;

		// Call fireTableDataChanged because we have a new row and order of rows
		// may have changed
		fireTableDataChanged();

	}

	public synchronized boolean allTasksFinished() {
		for (int i = 0; i < size; i++) {
			TaskStatus status = queue[i].getActualTask().getStatus();
			if ((status == TaskStatus.PROCESSING) || (status == TaskStatus.WAITING))
				return false;
		}
		return true;
	}

	public synchronized void clear() {
		size = 0;
		queue = new WrappedTask[DEFAULT_CAPACITY];
		fireTableDataChanged();
	}

	public synchronized void clearFinished() {

		ArrayList<WrappedTask> newQueue = new ArrayList<WrappedTask>();

		for (WrappedTask wt : queue) {

			if (wt != null) {

				if (!wt.getActualTask().getStatus().equals(TaskStatus.FINISHED)
						&& !wt.getActualTask().getStatus().equals(TaskStatus.PROCESSING))
					newQueue.add(wt);
			}
		}
		queue = newQueue.toArray(new WrappedTask[newQueue.size()]);
		size = queue.length;
		fireTableDataChanged();
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnClass(int)
	 */
	public Class<?> getColumnClass(int column) {
		switch (column) {
		case 0:
			return String.class;
		case 1:
			return TaskPriority.class;
		case 2:
			return TaskStatus.class;
		case 3:
			return LabeledProgressBar.class;
		}
		return null;

	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		return columns.length;
	}

	public String getColumnName(int column) {
		return columns[column];
	}

	public synchronized int getNumOfCancelledTasks() {

		int numOfCancelledTasks = 0;

		for (int i = 0; i < size; i++) {

			TaskStatus status = queue[i].getActualTask().getStatus();

			if (status == TaskStatus.CANCELED)
				numOfCancelledTasks++;
		}
		return numOfCancelledTasks;
	}

	/* TableModel implementation */

	synchronized int getNumOfWaitingTasks() {
		int numOfWaitingTasks = 0;
		for (int i = 0; i < size; i++) {
			
			if(queue[i] != null) {
				
				TaskStatus status = queue[i].getActualTask().getStatus();
				if ((status == TaskStatus.PROCESSING) || (status == TaskStatus.WAITING))
					numOfWaitingTasks++;
			}
		}
		return numOfWaitingTasks;
	}

	public synchronized WrappedTask[] getQueueSnapshot() {
		WrappedTask[] snapshot = new WrappedTask[size];
		System.arraycopy(queue, 0, snapshot, 0, size);
		return snapshot;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public synchronized int getRowCount() {
		return size;
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public synchronized Object getValueAt(int row, int column) {

		if (row < size) {

			WrappedTask wrappedTask = queue[row];
			Task actualTask = wrappedTask.getActualTask();

			switch (column) {
			case 0:
				return actualTask.getTaskDescription();
			case 1:
				return wrappedTask.getPriority();
			case 2:
				return actualTask.getStatus();
			case 3:
				double finishedPercentage = actualTask.getFinishedPercentage();
				LabeledProgressBar progressBar = progressBars.get(row);
				if (progressBar == null) {
					progressBar = new LabeledProgressBar(finishedPercentage);
					progressBars.put(row, progressBar);
				} else {
					progressBar.setValue(finishedPercentage);
				}
				return progressBar;
			}
		}
		return null;
	}

	public synchronized boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Refresh the queue (reorder the tasks according to priority) and send a
	 * signal to Tasks in progress window to redraw updated data, such as task
	 * status and finished percentages.
	 */
	public synchronized void refresh() {

		// We must not call fireTableDataChanged, because that would clear the
		// selection in the task window
		fireTableRowsUpdated(0, size - 1);

	}

	public synchronized void removeTask(Task task) {

		if (task == null)
			return;

		ArrayList<WrappedTask> newQueue = new ArrayList<WrappedTask>();

		for (WrappedTask wt : queue) {

			if (wt != null) {

				if (!wt.getActualTask().equals(task))
					newQueue.add(wt);
			}
		}
//		WrappedTask[] nQueue = new WrappedTask[queue.length];
//
//		for (int i = 0; i < newQueue.size(); i++)
//			nQueue[i] = newQueue.get(i);

//		queue = nQueue;
		
		queue = newQueue.toArray(new WrappedTask[newQueue.size()]);
		size = queue.length;
		fireTableDataChanged();
	}
}
