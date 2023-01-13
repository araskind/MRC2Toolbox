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
import java.util.Iterator;
import java.util.Vector;

import javax.swing.SwingUtilities;

import edu.umich.med.mrc2.datoolbox.gui.main.MainWindow;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskControlListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskController;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskPriority;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.gui.TaskProgressPanel;

public class TaskControllerImpl implements TaskController, Runnable {

	ArrayList<TaskControlListener> listeners = new ArrayList<TaskControlListener>();
	TaskControlListener listenerToRemove;

	/**
	 * Update the task progress window every 300 ms
	 */
	private final int TASKCONTROLLER_THREAD_SLEEP = 300;
	private Thread taskControllerThread;
	private TaskQueue taskQueue;
	private TaskProgressPanel taskPanel;
	private int maxRunningThreads;

	/**
	 * This vector contains references to all running threads of NORMAL
	 * priority. Maximum number of concurrent threads is specified in the
	 * preferences dialog.
	 */
	private Vector<WorkerThread> runningThreads;

	public void addTask(Task task) {
		addTasks(new Task[] { task }, TaskPriority.NORMAL);
	}

	public void addTask(Task task, TaskPriority priority) {
		addTasks(new Task[] { task }, priority);
	}

	@Override
	public synchronized void addTaskControlListener(TaskControlListener listener) {

		listeners.add(listener);
	}

	public void addTasks(Task tasks[]) {
		addTasks(tasks, TaskPriority.NORMAL);
	}

	public void addTasks(Task tasks[], TaskPriority priority) {

		// It can sometimes happen during a batch that no tasks are actually
		// executed --> tasks[] array may be empty
		if ((tasks == null) || (tasks.length == 0))
			return;

		for (Task task : tasks) {
			WrappedTask newQueueEntry = new WrappedTask(task, priority);
			taskQueue.addWrappedTask(newQueueEntry);
		}

		// Wake up the task controller thread
		synchronized (this) {
			this.notifyAll();
		}
		// This is a workaround for Automator, which will use built-in progress
		// instead of progress dialog by adding tasks with HIGH priority
		if (priority.equals(TaskPriority.NORMAL)) {

			SwingUtilities.invokeLater(new Runnable() {

				public void run() {
					MainWindow.showProgressDialog();
				}
			});
		}
	}

	/**
	 * @return the runningThreads
	 */
	public Vector<WorkerThread> getRunningThreads() {
		return runningThreads;
	}

	public TaskProgressPanel getTaskPanel() {
		return taskPanel;
	}

	public TaskQueue getTaskQueue() {

		return taskQueue;
	}

	/**
	 * Initialize the task controller
	 */
	public void initModule() {

		taskQueue = new TaskQueue();

		runningThreads = new Vector<WorkerThread>();
		maxRunningThreads = 1;

		// Create a low-priority thread that will manage the queue and start
		// worker threads for tasks
		taskControllerThread = new Thread(this, "Task controller thread");
		taskControllerThread.setPriority(Thread.MIN_PRIORITY);
		taskControllerThread.start();

		// Create the task progress window
		taskPanel = new TaskProgressPanel(this);
	}

	public synchronized void removeTaskControlListener(TaskControlListener listener) {

		// listeners.remove(listener);

		listenerToRemove = listener;

		Runnable swingCode = new Runnable() {

			public void run() {

				listeners.remove(listenerToRemove);
			}
		};
		try {
			if (SwingUtilities.isEventDispatchThread())
				swingCode.run();
			else
				SwingUtilities.invokeAndWait(swingCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Task controller thread main method.
	 *
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		int previousQueueSize = -1;

		while (true) {

			int currentQueueSize = taskQueue.getNumOfWaitingTasks();
			if (currentQueueSize != previousQueueSize) {
				previousQueueSize = currentQueueSize;
				for (TaskControlListener listener : listeners)
					listener.numberOfWaitingTasksChanged(currentQueueSize);
			}

			// If the queue is empty, we can sleep. When new task is added into
			// the queue, we will be awaken by notify()
			synchronized (this) {
				while (taskQueue.isEmpty()) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						// Ignore
					}
				}
			}

			// Check if all tasks in the queue are finished
			if (taskQueue.allTasksFinished()) {

				for (TaskControlListener listener : listeners)
					listener.allTasksFinished(true);

				taskQueue.clear();

				SwingUtilities.invokeLater(new Runnable() {

					public void run() {

						MRC2ToolBoxCore.getMainWindow().hideProgressDialog();
					}
				});
				continue;
			}

			// Remove already finished threads from runningThreads
			Iterator<WorkerThread> threadIterator = runningThreads.iterator();
			while (threadIterator.hasNext()) {
				WorkerThread thread = threadIterator.next();
				if (thread.isFinished())
					threadIterator.remove();
			}

			// Get a snapshot of the queue
			WrappedTask[] queueSnapshot = taskQueue.getQueueSnapshot();

			// Check all tasks in the queue
			for (WrappedTask task : queueSnapshot) {

				// Skip assigned and canceled tasks
				if (task.isAssigned() || (task.getActualTask().getStatus() == TaskStatus.CANCELED))
					continue;

				if (runningThreads.size() < maxRunningThreads) {

					WorkerThread newThread = new WorkerThread(task);
					runningThreads.add(newThread);
					newThread.start();
				}
			}

			// Tell the queue to refresh the Task progress window
			taskQueue.refresh();

			// Sleep for a while until next update
			try {
				Thread.sleep(TASKCONTROLLER_THREAD_SLEEP);
			} catch (InterruptedException e) {
				// Ignore
			}
		}
	}

	public void setMaxRunningThreads(int maxRunningThreads) {
		this.maxRunningThreads = maxRunningThreads;
	}

	/**
	 * @param runningThreads
	 *            the runningThreads to set
	 */
	public void setRunningThreads(Vector<WorkerThread> runningThreads) {
		this.runningThreads = runningThreads;
	}

	public void setTaskPriority(Task task, TaskPriority priority) {

		// Get a snapshot of current task queue
		WrappedTask currentQueue[] = taskQueue.getQueueSnapshot();

		// Find the requested task
		for (WrappedTask wrappedTask : currentQueue) {

			if (wrappedTask.getActualTask() == task) {

				wrappedTask.setPriority(priority);

				// Call refresh to re-sort the queue according to new priority
				// and update the Task progress window
				taskQueue.refresh();
			}
		}
	}

	@Override
	public void cancelAllTasks() {

		for (WrappedTask wrappedTask : getTaskQueue().getQueueSnapshot()) {

			Task task = wrappedTask.getActualTask();

			if ((task.getStatus() == TaskStatus.WAITING)
					|| task.getStatus() == TaskStatus.PROCESSING
					|| task.getStatus() == TaskStatus.ERROR)
				task.cancel();
		}
		getTaskQueue().clear();
		return;
	}
}
