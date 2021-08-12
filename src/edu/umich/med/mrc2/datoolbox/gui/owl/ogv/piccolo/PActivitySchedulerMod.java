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

package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.piccolo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import edu.umd.cs.piccolo.PRoot;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PActivityScheduler;
import edu.umd.cs.piccolo.util.PUtil;

public class PActivitySchedulerMod extends PActivityScheduler {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5881710729416990553L;
    private transient Timer activityTimerMod = null;
    private final PRoot rootMod;
    private final List activitiesMod;    
    private boolean activitiesChangedMod;
    private boolean animatingMod;
    private final ArrayList processingActivitiesMod;
    
	public PActivitySchedulerMod(PRoot rootNode) {
		super(rootNode);
		rootMod = rootNode;
        activitiesMod = new ArrayList();
        processingActivitiesMod = new ArrayList();
	}


    /**
     * Returns the node from which all activities will be attached.
     * 
     * @return this scheduler's associated root node
     */
    public PRoot getRoot() {
        return rootMod;
    }

    /**
     * Adds the given activity to the scheduler if not already found.
     * 
     * @param activity activity to be scheduled
     */
    public void addActivity(final PActivity activity) {
        addActivity(activity, false);
    }

    /**
     * Add this activity to the scheduler. Sometimes it's useful to make sure
     * that an activity is run after all other activities have been run. To do
     * this set processLast to true when adding the activity.
     * 
     * @param activity activity to be scheduled
     * @param processLast whether or not this activity should be performed after
     *            all other scheduled activities
     */
    public void addActivity(final PActivity activity, final boolean processLast) {
        if (activitiesMod.contains(activity)) {
            return;
        }

        activitiesChangedMod = true;

        if (processLast) {
            activitiesMod.add(0, activity);
        }
        else {
            activitiesMod.add(activity);
        }

        activity.setActivityScheduler(this);

        if (!getActivityTimer().isRunning()) {
            startActivityTimer();
        }
    }

    /**
     * Removes the given activity from the scheduled activities. Does nothing if
     * it's not found.
     * 
     * @param activity the activity to be removed
     */
    public void removeActivity(final PActivity activity) {
        if (!activitiesMod.contains(activity)) {
            return;
        }

        activitiesChangedMod = true;
        activitiesMod.remove(activity);

        if (activitiesMod.size() == 0) {
            stopActivityTimer();
        }
    }

    /**
     * Removes all activities from the list of scheduled activities.
     */
    public void removeAllActivities() {
        activitiesChangedMod = true;
        activitiesMod.clear();
        stopActivityTimer();
    }

    /**
     * Returns a reference to the current activities list. Handle with care.
     * 
     * @return reference to the current activities list.
     */
    public List getActivitiesReference() {
        return activitiesMod;
    }

    /**
     * Process all scheduled activities for the given time. Each activity is
     * given one "step", equivalent to one frame of animation.
     * 
     * @param currentTime the current unix time in milliseconds.
     */
    public void processActivities(final long currentTime) {
        final int size = activitiesMod.size();
        if (size > 0) {
            processingActivitiesMod.addAll(activitiesMod);
            for (int i = size - 1; i >= 0; i--) {
                final PActivity each = (PActivity) processingActivitiesMod.get(i);
                if(each != null)
                	each.processStep(currentTime);
            }
            processingActivitiesMod.clear();
        }
    }

    /**
     * Return true if any of the scheduled activities are animations.
     * 
     * @return true if any of the scheduled activities are animations.
     */
    public boolean getAnimating() {
        if (activitiesChangedMod) {
            animatingMod = false;
            for (int i = 0; i < activitiesMod.size(); i++) {
                final PActivity each = (PActivity) activitiesMod.get(i);
                animatingMod |= false;
            }
            activitiesChangedMod = false;
        }
        return animatingMod;
    }

    /**
     * Starts the current activity timer. Multiple calls to this method are
     * ignored.
     */
    protected void startActivityTimer() {
        getActivityTimer().start();
    }

    /**
     * Stops the current activity timer.
     */
    protected void stopActivityTimer() {
        getActivityTimer().stop();
    }

    /**
     * Returns the activity timer. Creating it if necessary.
     * 
     * @return a Timer instance.
     */
    protected Timer getActivityTimer() {
        if (activityTimerMod == null) {
            activityTimerMod = rootMod.createTimer(PUtil.ACTIVITY_SCHEDULER_FRAME_DELAY, new ActionListener() {
                public void actionPerformed(final ActionEvent e) {
                    rootMod.processInputs();
                }
            });
        }
        return activityTimerMod;
    }
}
