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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.prep;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang3.StringUtils;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.IDTExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProtocol;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTDataCash;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.IDTrackerLimsManagerPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DockableSamplePrepManagerPanel extends DefaultSingleCDockable
	implements ActionListener, ListSelectionListener, PersistentLayout {

	private static final Icon componentIcon = GuiUtils.getIcon("editSamplePrep", 16);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "SamplePrepManagerPanel.layout");

	private SamplePrepManagerToolbar toolbar;
	private DockablePrepTable samplePrepTable;
	private DockableActivePrepDisplayPanel activePrepPanel;
	private SamplePrepEditorDialog samplePrepEditorDialog;
	private IDTrackerLimsManagerPanel idTrackerLimsManager;
	private CControl control;
	private CGrid grid;

	public DockableSamplePrepManagerPanel(IDTrackerLimsManagerPanel idTrackerLimsManager) {

		super("DockableSamplePrepManagerPanel", componentIcon, "Sample preparations", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		this.idTrackerLimsManager = idTrackerLimsManager;

		toolbar = new SamplePrepManagerToolbar(this);
		getContentPane().add(toolbar, BorderLayout.NORTH);

		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid(control);

		samplePrepTable = new DockablePrepTable(this);
		samplePrepTable.getTable().addMouseListener(

				new MouseAdapter() {

					public void mouseClicked(MouseEvent e) {

						if (e.getClickCount() == 2) {
							LIMSSamplePreparation prep = samplePrepTable.getSelectedPrep();
							if(prep != null)
								showEditSamplePrepDialog(prep);
						}											
					}
				});
		
		activePrepPanel = new DockableActivePrepDisplayPanel();
		grid.add(0, 0, 100, 100, samplePrepTable, activePrepPanel);

		control.getContentArea().deploy(grid);
		add(control.getContentArea(), BorderLayout.CENTER);
		loadLayout(layoutConfigFile);
	}

	public void loadPrepData() {		
		samplePrepTable.setTableModelFromPreps(IDTDataCash.getSamplePreps());
	}
	
	public LIMSSamplePreparation getSelectedPrep() {
		return samplePrepTable.getSelectedPrep();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals(MainActionCommands.ADD_SAMPLE_PREP_DIALOG_COMMAND.getName()))
			showCreateNewPrepDialog(idTrackerLimsManager.getSelectedExperiment());
		
		if(e.getActionCommand().equals(MainActionCommands.EDIT_SAMPLE_PREP_DIALOG_COMMAND.getName()))		
			showEditSamplePrepDialog(samplePrepTable.getSelectedPrep());

		if(e.getActionCommand().equals(MainActionCommands.ADD_SAMPLE_PREP_COMMAND.getName()))
			saveSamplePrepData();

		if(e.getActionCommand().equals(MainActionCommands.EDIT_SAMPLE_PREP_COMMAND.getName()))
			editExistingSamplePrep();

		if(e.getActionCommand().equals(MainActionCommands.DELETE_SAMPLE_PREP_COMMAND.getName()))
			deleteSamplePrep(samplePrepTable.getSelectedPrep());
	}

	public void showCreateNewPrepDialog(LIMSExperiment selectedExperiment) {

		if(selectedExperiment == null)
			return;
		
		if(selectedExperiment.getExperimentDesign().getSamples().isEmpty()) {
			MessageDialog.showErrorMsg(
					"No samples specified for the selected experiment,\n"
					+ "please add the samples first!", 
					this.getContentPane());
			return;
		}		
		samplePrepEditorDialog = new SamplePrepEditorDialog(selectedExperiment, this);
		samplePrepEditorDialog.setLocationRelativeTo(this.getContentPane());
		samplePrepEditorDialog.setVisible(true);
	}

	public void showEditSamplePrepDialog(LIMSSamplePreparation prep) {

		if(prep == null)
			return;
		
		if(IDTDataCash.getExperimentForSamplePrep(prep) == null) {
			MessageDialog.showErrorMsg(
					"Can not find parent experiment for the selected sample prep!", 
					this.getContentPane());
			return;
		}
		samplePrepEditorDialog = new SamplePrepEditorDialog(prep, this);
		samplePrepEditorDialog.setLocationRelativeTo(this.getContentPane());
		samplePrepEditorDialog.setVisible(true);
	}

	//	TODO
	public void deleteSamplePrep(LIMSSamplePreparation prep2delete) {

		if(prep2delete == null)
			return;

		if(!IDTUtils.isSuperUser(this.getContentPane()))
			return;

		int result = MessageDialog.showChoiceWithWarningMsg(
				"Do you really want to delete sample preparation \"" + prep2delete.getName() + "\"?\n" +
				"All associated data will be purged from the database!",
				this.getContentPane());

		if(result == JOptionPane.YES_OPTION) {

			try {
				IDTUtils.deleteSamplePreparation(prep2delete);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			IDTDataCash.refreshSamplePreps();
			loadPrepData();
			idTrackerLimsManager.reloadProjectTree();
		}
	}

	private void editExistingSamplePrep() {

		LIMSSamplePreparation prep2save = samplePrepEditorDialog.getSamplePrep();
		if(prep2save == null)
			return;

		Collection<String>errors = samplePrepEditorDialog.vaidateSamplePrepData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), samplePrepEditorDialog);
			return;
		}
		String prepName = samplePrepEditorDialog.getPrepName();
		LIMSUser prepUser = samplePrepEditorDialog.getPrepUser();
		Date prepDate = samplePrepEditorDialog.getPrepDate();
		Collection<IDTExperimentalSample>selectedSamples = samplePrepEditorDialog.getSelectedSamples();
		Collection<LIMSProtocol>prepSops = samplePrepEditorDialog.getPrepSops();
		Collection<ObjectAnnotation>prepAnnotations = samplePrepEditorDialog.getPrepAnnotations();
		try {
			IDTUtils.updateSamplePrep(prep2save, prepName, prepUser, prepDate);
			prep2save.setName(prepName);
			prep2save.setCreator(prepUser);
			prep2save.setPrepDate(prepDate);

		} catch (Exception e) {
			e.printStackTrace();
		}
		//	TODO	Update SOP list

		//	TODO	Update document list
		
		loadPrepData();
		idTrackerLimsManager.updateProjetTreeNodeForObject(prep2save);
	}

	private void saveSamplePrepData() {

		Collection<String>errors = samplePrepEditorDialog.vaidateSamplePrepData();
		if(!errors.isEmpty()) {
			MessageDialog.showErrorMsg(StringUtils.join(errors, "\n"), samplePrepEditorDialog);
			return;
		}
		String prepName = samplePrepEditorDialog.getPrepName();
		LIMSUser prepUser = samplePrepEditorDialog.getPrepUser();
		Date prepDate = samplePrepEditorDialog.getPrepDate();
		Collection<IDTExperimentalSample>selectedSamples = samplePrepEditorDialog.getSelectedSamples();
		Collection<LIMSProtocol>prepSops = samplePrepEditorDialog.getPrepSops();
		Collection<ObjectAnnotation>prepAnnotations = samplePrepEditorDialog.getPrepAnnotations();
		LIMSSamplePreparation prep2save =  new LIMSSamplePreparation(null, prepName, prepDate, prepUser);
		for(LIMSProtocol protocol : prepSops)
			prep2save.addProtocol(protocol);

		String newPrepId = null;
		Map<LIMSExperiment, Collection<LIMSSamplePreparation>> espMap = 
				IDTDataCash.getExperimentSamplePrepMap();
		LIMSExperiment experiment = samplePrepEditorDialog.getExperiment();	
		try {
			newPrepId = IDTUtils.addNewSamplePrep(prep2save, selectedSamples, prepSops);
			prep2save.setId(newPrepId);
			IDTDataCash.getSamplePreps().add(prep2save);
			if(!espMap.containsKey(experiment))			
				espMap.put(experiment, new TreeSet<LIMSSamplePreparation>());
				
			espMap.get(experiment).add(prep2save);						
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		loadPrepData();
		idTrackerLimsManager.getProjectTreePanel().addObject(prep2save);	
		idTrackerLimsManager.getProjectTreePanel().expandNodeForObject(experiment);	
		samplePrepEditorDialog.dispose();
	}
	
	public void selectSamplePrep(LIMSSamplePreparation prep) {
		samplePrepTable.selectSamplePrep(prep);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()) {
			LIMSSamplePreparation prep = samplePrepTable.getSelectedPrep();
			activePrepPanel.loadPrepData(prep);
			idTrackerLimsManager.loadExperimentForPrep(prep);
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

		samplePrepTable.clearTable();
		activePrepPanel.clearPanel();
	}
	
	public void clearCurrentPrepData() {
		samplePrepTable.clearSelection();
		activePrepPanel.clearPanel();
	}
}































