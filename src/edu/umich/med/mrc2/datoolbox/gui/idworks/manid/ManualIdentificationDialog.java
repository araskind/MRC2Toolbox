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

package edu.umich.med.mrc2.datoolbox.gui.idworks.manid;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdSource;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentificationConfidence;
import edu.umich.med.mrc2.datoolbox.data.enums.MassErrorType;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.DockableDatabaseCompoundTable;
import edu.umich.med.mrc2.datoolbox.gui.cpddatabase.cpdinfo.DockableSynonymsTable;
import edu.umich.med.mrc2.datoolbox.gui.main.DockableMRC2ToolboxPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.structure.DockableMolStructurePanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.taskcontrol.AbstractTask;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskEvent;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskListener;
import edu.umich.med.mrc2.datoolbox.taskcontrol.TaskStatus;
import edu.umich.med.mrc2.datoolbox.taskcontrol.tasks.cpdb.SearchCompoundDatabaseTask;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class ManualIdentificationDialog extends JDialog  
	implements ActionListener, BackedByPreferences, PersistentLayout, ListSelectionListener, TaskListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6745869001916986372L;

	private static final Icon dialogIcon = GuiUtils.getIcon("manualIdentification", 32);
	
	private Preferences preferences;
	private static final String MASS_ACCURACY_VALUE = "MASS_ACCURACY_VALUE";
	private static final String MASS_ACCURACY_TYPE = "MASS_ACCURACY_TYPE";
	private static final String ADDUCT_MASS_ACCURACY_VALUE = "ADDUCT_MASS_ACCURACY_VALUE";
	private static final String ADDUCT_MASS_ACCURACY_TYPE = "ADDUCT_MASS_ACCURACY_TYPE";
	private static final String WINDOW_WIDTH = "WINDOW_WIDTH";
	private static final String WINDOW_HEIGTH = "WINDOW_HEIGTH";
	private static final String WINDOW_POSITION_X = "WINDOW_POSITION_X";
	private static final String WINDOW_POSITION_Y = "WINDOW_POSITION_Y";
	
	private ActionListener listener;
	private CControl control;
	private CGrid grid;
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "ManualIdentificationDialog.layout");
	
	private DockableDatabaseCompoundTable compoundTable;
	private DockableMolStructurePanel molStructurePanel;
	private DockableSynonymsTable synonymsTable;
	private DockableExtendedCompoundDatabaseSearchPanel compoundDatabaseSearchPanel;
	private JButton btnSave;
	private MsFeature activeFeature;
	
//	private DockableMzAdductDbSearchPanel mzAdductDbSearchPanel;
//	private DbSearchType searchType;
//	private enum DbSearchType {		
//		FREE_FORM,
//		MZ_ADDUCT,
//		;
//	}

	public ManualIdentificationDialog(ActionListener listener) {

		super();
		setTitle("Add manual identification to selected feature");
		setIconImage(((ImageIcon) dialogIcon).getImage());
		setModalityType(ModalityType.APPLICATION_MODAL);
		//setPreferredSize(new Dimension(1000, 800));
		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		this.listener = listener;
//		searchType = null;
		
		control = new CControl(MRC2ToolBoxCore.getMainWindow());
		control.getController().setTheme(new EclipseTheme());
		this.add( control.getContentArea() );
		grid = new CGrid(control);
		
		compoundTable = new DockableDatabaseCompoundTable();
		compoundTable.getTable().getSelectionModel().addListSelectionListener(this);
		molStructurePanel = new DockableMolStructurePanel(
				"ManualIdentificationDialogDockableMolStructurePanel");
		synonymsTable = new DockableSynonymsTable(this, false);
		compoundDatabaseSearchPanel = new DockableExtendedCompoundDatabaseSearchPanel(this);
//		mzAdductDbSearchPanel = new DockableMzAdductDbSearchPanel(this);

		grid.add(0, 0, 1, 1,
				compoundDatabaseSearchPanel,
//				mzAdductDbSearchPanel,
				compoundTable,
				synonymsTable,
				molStructurePanel);

		control.getContentArea().deploy(grid);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setVisible(false);
			}
		};
		btnCancel.addActionListener(al);

		btnSave = new JButton("Assign selected identification");
		btnSave.setActionCommand(MainActionCommands.SET_MANUAL_FEATURE_ID_COMMAND.getName());
		btnSave.addActionListener(listener);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		loadLayout(layoutConfigFile);
		loadPreferences();
		pack();
		loadPreferences();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.SEARCH_DATABASE_COMMAND.getName()))
			searchDatabase();
		
		if(command.equals(MainActionCommands.SEARCH_DATABASE_BY_MZ_ADDUCT_COMMAND.getName()))
			searchDatabaseByMzAdduct();
	}
	
	private void searchDatabase() {

//		searchType = DbSearchType.FREE_FORM; 
		String cpdName = compoundDatabaseSearchPanel.getCompoundName();
		String molFormula = compoundDatabaseSearchPanel.getFormula();
		String cpdId = compoundDatabaseSearchPanel.getId();
		String inchi = compoundDatabaseSearchPanel.getInChi();
		Range massRange = compoundDatabaseSearchPanel.getMassRange();

		// Check if any search parameters provided
		if (cpdName.isEmpty() && molFormula.isEmpty() && cpdId.isEmpty() && inchi.isEmpty() && massRange == null) {
			MessageDialog.showErrorMsg("No search parameters specified!", this.getContentPane());
			return;
		}
		if (!molFormula.isEmpty()) {
			try {
				IMolecularFormula mf = MolecularFormulaManipulator.getMolecularFormula(molFormula,
						DefaultChemObjectBuilder.getInstance());
			} catch (Exception e) {
				MessageDialog.showErrorMsg("Formula not valid!", this.getContentPane());
				return;
			}
		}
		clearResults();
		SearchCompoundDatabaseTask sdbTask = new SearchCompoundDatabaseTask(
				cpdName,
				molFormula,
				cpdId,
				inchi,
				massRange,
				compoundDatabaseSearchPanel.getNameScope(),
				compoundDatabaseSearchPanel.getNameMatchFidelity());
		sdbTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(sdbTask);
	}
	
	private void searchDatabaseByMzAdduct() {

//		searchType = DbSearchType.MZ_ADDUCT;
		Range massRange = compoundDatabaseSearchPanel.getMassRange();
		if(massRange == null)
			return;

		clearResults();
		SearchCompoundDatabaseTask sdbTask = new SearchCompoundDatabaseTask(
				"",
				"",
				"",
				"",
				massRange,
				null,
				null);
		sdbTask.addTaskListener(this);
		MRC2ToolBoxCore.getTaskController().addTask(sdbTask);
	}
	
	public void clearResults() {

		compoundTable.clearTable();
		molStructurePanel.clearPanel();
		synonymsTable.clearTable();
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));
	}

	@Override
	public void loadPreferences(Preferences prefs) {

		preferences = prefs;

		//	DB search panel
		MassErrorType dbErrorType = MassErrorType.getTypeByName(
				preferences.get(MASS_ACCURACY_TYPE, MassErrorType.ppm.name()));
		compoundDatabaseSearchPanel.setMassError(
				preferences.getDouble(MASS_ACCURACY_VALUE, MRC2ToolBoxConfiguration.getMassAccuracy()), 
				dbErrorType);
		
		//	Adduct search panel
		MassErrorType adductErrorType = MassErrorType.getTypeByName(
				preferences.get(ADDUCT_MASS_ACCURACY_TYPE, MassErrorType.ppm.name()));
		compoundDatabaseSearchPanel.setMassError(
				preferences.getDouble(ADDUCT_MASS_ACCURACY_VALUE, MRC2ToolBoxConfiguration.getMassAccuracy()), 
				adductErrorType);
				
		//	Set window size
		setSize(new Dimension(
				preferences.getInt(WINDOW_WIDTH, 1000), 
				preferences.getInt(WINDOW_HEIGTH, 800)));

		//	Set window location
		int y = preferences.getInt(WINDOW_POSITION_Y, -1);
		int x = preferences.getInt(WINDOW_POSITION_X, -1);
		if(x >= 0 && y >= 0)
			setLocation(y, x);
		else
			setLocationRelativeTo(((DockableMRC2ToolboxPanel)listener).getContentPane());
 	}
	
	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
		
		preferences.putDouble(MASS_ACCURACY_VALUE, compoundDatabaseSearchPanel.getMassErrorValue());
		preferences.put(MASS_ACCURACY_TYPE, compoundDatabaseSearchPanel.getMassErrorType().name());
		preferences.putDouble(ADDUCT_MASS_ACCURACY_VALUE, compoundDatabaseSearchPanel.getMassErrorValue());
		preferences.put(ADDUCT_MASS_ACCURACY_TYPE, compoundDatabaseSearchPanel.getMassErrorType().name());
		
		Dimension size = getSize();
		preferences.putInt(WINDOW_WIDTH, (int) size.getWidth());
		preferences.putInt(WINDOW_HEIGTH, (int) size.getHeight());
		
		Point location = getLocation();
		preferences.putInt(WINDOW_POSITION_Y, (int) location.getY());
		preferences.putInt(WINDOW_POSITION_X, (int) location.getX());
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

	@Override
	public void setVisible(boolean b) {

		if(!b) {
			saveLayout(layoutConfigFile);
			savePreferences();
		}
		super.setVisible(b);
	}
	
	@Override
	public void dispose() {

		saveLayout(layoutConfigFile);
		savePreferences();
		super.dispose();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()) {

			if (compoundTable.getSelectedCompound() != null)
				loadCompoundData(compoundTable.getSelectedCompound());
			else
				clearDataPanels();
		}		
	}
	
	private void clearDataPanels() {

		synonymsTable.clearTable();
		molStructurePanel.clearPanel();
	}

	private void loadCompoundData(CompoundIdentity cpd) {

		molStructurePanel.showStructure(cpd.getSmiles());
		synonymsTable.loadCompoundData(cpd);
	}

	/**
	 * @return the activeFeature
	 */
	public MsFeature getActiveFeature() {
		return activeFeature;
	}

	/**
	 * @param activeFeature the activeFeature to set
	 */
	public void setActiveFeature(MsFeature activeFeature) {
		
		clearResults();
		this.activeFeature = activeFeature;
		compoundDatabaseSearchPanel.setActiveFeature(activeFeature);
	}

	@Override
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			((AbstractTask) e.getSource()).removeTaskListener(this);

			if (e.getSource().getClass().equals(SearchCompoundDatabaseTask.class))
				showSearchResults((SearchCompoundDatabaseTask) e.getSource());
		}
	}
	
	private void showSearchResults(SearchCompoundDatabaseTask searchTask) {

		clearResults();
		if (searchTask.getCompoundList().isEmpty())
			return;

		compoundTable.setTableModelFromCompoundCollection(searchTask.getCompoundList());
		if(compoundTable.getTable().getRowCount() > 0)
			compoundTable.getTable().setRowSelectionInterval(0, 0);
	}
	
	public CompoundIdentity getSelectedCompoundIdentity() {
		return compoundTable.getSelectedCompound();
	}
	
	public MsFeatureIdentity getSelectedFeatureIdentity() {
		
		if(compoundTable.getSelectedCompound() == null)
			return null;
		
		MsFeatureIdentity manualId = 
				new MsFeatureIdentity(
						compoundTable.getSelectedCompound(), 
						CompoundIdentificationConfidence.ACCURATE_MASS);
		manualId.setIdSource(CompoundIdSource.MANUAL);
		manualId.setPrimaryAdduct(compoundDatabaseSearchPanel.getSelectedAdduct());				
		return manualId;
	}
}
















