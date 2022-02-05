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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.results;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.lims.IDTMsSummary;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.AbstractIDTrackerLimsPanel;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.IndeterminateProgressDialog;
import edu.umich.med.mrc2.datoolbox.gui.utils.LongUpdateTask;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DockableIDTrackerResultsOverviewPanel extends AbstractIDTrackerLimsPanel
	implements ListSelectionListener, PersistentLayout {

	private static final Icon componentIcon = GuiUtils.getIcon("msms", 16);
	private static final Icon refreshDataIcon = GuiUtils.getIcon("refreshDbData", 24);

	private static final File layoutConfigFile = 
			new File(MRC2ToolBoxCore.configDir + "IDTrackerResultsOverviewPanel.layout");
	private IDTrackerResultsOverviewToolbar toolbar;

	private CControl control;
	private CGrid grid;

	private DockableMSOneSummaryPanel msOneSummaryPanel;
	private DockableMSMSSummaryPanel msmsSummaryPanel;
	private IndeterminateProgressDialog idp;

	public DockableIDTrackerResultsOverviewPanel(IDTrackerLimsManagerPanel idTrackerLimsManager) {

		super(idTrackerLimsManager, "DockableIDTrackerResultsOverviewPanel", 
				componentIcon, "Result summaries", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
//		toolbar = new IDTrackerResultsOverviewToolbar(this);
//		add(toolbar, BorderLayout.NORTH);

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);

		msOneSummaryPanel = new DockableMSOneSummaryPanel();
		msmsSummaryPanel = new DockableMSMSSummaryPanel();

		grid.add(0, 0, 100, 100, msOneSummaryPanel, msmsSummaryPanel);

		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);

		//control.getController().setFocusedDockable(designEditor.intern(), true);
		loadLayout(layoutConfigFile);
		initActions();
	}

	@Override
	protected void initActions() {

		super.initActions();
		
		menuActions.add(GuiUtils.setupButtonAction(
				MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName(),
				MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName(), 
				refreshDataIcon, this));
		
		//	menuActions.addSeparator();
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(!isConnected())
			return;
		
		String command = e.getActionCommand();

		if(command.equals(MainActionCommands.REFRESH_LIMS_DATA_COMMAND.getName()))
			refreshSummaryData();
	}

	private void refreshSummaryData() {

		RefreshDataTask task = new RefreshDataTask();
		idp = new IndeterminateProgressDialog("Uptating table data ...", this.getContentPane(), task);
		idp.setLocationRelativeTo(this.getContentPane());
		idp.setVisible(true);
	}

	class RefreshDataTask extends LongUpdateTask {
		/*
		 * Main task. Executed in background thread.
		 */

		public RefreshDataTask() {
			super();
		}

		@Override
		public Void doInBackground() {

			Collection<IDTMsSummary>dataSummaries = null;
			try {
				dataSummaries = IDTUtils.getIDTMsOneSummary();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(dataSummaries != null)
				msOneSummaryPanel.setTableModelFromSummaryCollection(dataSummaries);

			Collection<IDTMsSummary>msmsDataSummaries = null;
			try {
				msmsDataSummaries = IDTUtils.getIDTMsTwoSummary();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(msmsDataSummaries != null)
				msmsSummaryPanel.setTableModelFromSummaryCollection(msmsDataSummaries);

			return null;
		}

	    @Override
	    public void done() {
	        try {
	            if (!isCancelled()) get();
	        } catch (ExecutionException e) {
	            // Exception occurred, deal with it
	            System.out.println("Exception: " + e.getCause());
	        } catch (InterruptedException e) {
	            // Shouldn't happen, we're invoked when computation is finished
	            throw new AssertionError(e);
	        }
	        super.done();
	    }
	}

	@Override
	public void loadLayout(File layoutFile) {

		if(control != null) {

			if(layoutFile.exists()) {
				try {
					control.readXML(layoutFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					control.writeXML(layoutFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void saveLayout(File layoutFile) {

		if(control != null) {

			for(int i=0; i<control.getCDockableCount(); i++) {

				CDockable uiObject = control.getCDockable(i);
				if(uiObject instanceof PersistentLayout)
					((PersistentLayout)uiObject).saveLayout(((PersistentLayout)uiObject).getLayoutFile());
			}
			try {
				control.writeXML(layoutFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public File getLayoutFile() {
		return layoutConfigFile;
	}

	public synchronized void clearPanel() {

		msOneSummaryPanel.clearPanel();
		msmsSummaryPanel.clearPanel();
	}

	@Override
	public void loadPreferences(Preferences preferences) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loadPreferences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void savePreferences() {
		// TODO Auto-generated method stub
		
	}
}
