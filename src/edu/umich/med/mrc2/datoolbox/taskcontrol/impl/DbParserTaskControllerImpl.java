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

import java.util.Iterator;

import javax.swing.SwingUtilities;

import edu.umich.med.mrc2.datoolbox.dbparse.DbParserFrame;
import edu.umich.med.mrc2.datoolbox.taskcontrol.Task;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskControlListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskPriority;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;

public class DbParserTaskControllerImpl extends TaskControllerImpl {

	@Override
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
					DbParserFrame.showProgressDialog();
				}
			});
		}
	}
	
	@Override
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
						DbParserFrame.hideProgressDialog();
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
}
