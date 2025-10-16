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

package edu.umich.med.mrc2.datoolbox.gui.library.feditor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.MsDataPointComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.annotation.DockableObjectAnnotationPanel;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.spectrum.DockableSpectumPlot;
import edu.umich.med.mrc2.datoolbox.gui.utils.GlassPane;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DockableMsMsDataEditorPanel extends DefaultSingleCDockable implements ActionListener, ListSelectionListener, PersistentLayout {

	private MsMsToolbar toolBar;
	private DockableMsMsInfoPanel msMsInfoPanel;
	private DockableSpectumPlot msPlot;
	private DockableMsMsTable msmsTable;
	private DockableObjectAnnotationPanel featureAnnotationPanel;

	private LibraryMsFeature activeFeature;
	private TandemMassSpectrum activeMsMs;
	private TandemMassSpectrum filteredMsMs;
	private MsMsFilterDialog msMsFilterDialog;
	private MsMsChooserFilterDialog msMsChooserFilterDialog;
	private CControl control;
	private CGrid grid;
	private Component defaultGlassPane;

	private static final Icon componentIcon = GuiUtils.getIcon("msms", 16);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "MsMsPanel.layout");

	public DockableMsMsDataEditorPanel() {

		super("DockableMsMsDataEditorPanel", componentIcon, "MS/MS data editor", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));

		toolBar = new MsMsToolbar(this);
		add(toolBar, BorderLayout.NORTH);

		msMsInfoPanel = new DockableMsMsInfoPanel(this);
		msPlot = new DockableSpectumPlot(
				"DockableMsMsDataEditorPanelDockableSpectumPlot", "MSMS spectrum plot");
		msmsTable = new DockableMsMsTable(
				"DockableMsMsPanelDockableMsMsTable", "MSMS spectrum table");
		featureAnnotationPanel = new DockableObjectAnnotationPanel(
				"MsMsPanelAnnotations", "MSMS annotations", 80);

		control = new CControl( MRC2ToolBoxCore.getMainWindow() );
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);;
		grid = new CGrid( control );
		grid.add(0, 0, 25, 100, msMsInfoPanel);
		grid.add( 25, 0, 75, 100, msPlot, msmsTable, featureAnnotationPanel );
		control.getContentArea().deploy( grid );
		grid.select(25, 0, 75, 100, msPlot);

		add(control.getContentArea(), BorderLayout.CENTER);

		loadLayout(layoutConfigFile);
	}


	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		if (activeFeature != null) {

			if (command.equals(MainActionCommands.SHOW_MSMS_IMPORT_DIALOG_COMMAND.getName())) {

				if(msMsChooserFilterDialog == null)
					msMsChooserFilterDialog = new MsMsChooserFilterDialog(this);

				msMsChooserFilterDialog.setActiveFeature(activeFeature);
				msMsChooserFilterDialog.setLocationRelativeTo(this.getContentPane());
				msMsChooserFilterDialog.setVisible(true);
			}

			if (command.equals(MainActionCommands.SHOW_MSMS_DATA_FILTER_COMMAND.getName()))
				showMsMsFilter();

			if (command.equals(MainActionCommands.FILTER_MSMS_DATA_COMMAND.getName()))
				filterMsMs();

			if (command.equals(MainActionCommands.RESET_MSMS_FILTER_COMMAND.getName()))
				resetMsMsFilter();

			if (command.equals(MainActionCommands.ACCEPT_MSMS_COMMAND.getName()))
				acceptMsMs();

			if (command.equals(MainActionCommands.DELETE_MSMS_COMMAND.getName()))
				deleteMsMs();
		}
	}

	private void showMsMsFilter() {

		if (activeMsMs != null) {

			if(msMsFilterDialog == null)
				msMsFilterDialog = new MsMsFilterDialog(this);

			msMsFilterDialog.setLocationRelativeTo(this.getContentPane());
			msMsFilterDialog.setVisible(true);
		}
	}

	private void deleteMsMs() {

		if (activeMsMs != null) {

			String yesNoQuestion = "Do you really want to delete current MSMS (" + activeMsMs.toString() +")?";

			if(MessageDialog.showChoiceWithWarningMsg(yesNoQuestion, this.getContentPane()) == JOptionPane.YES_OPTION) {

				activeFeature.getSpectrum().getTandemSpectra().remove(activeMsMs);
				loadFeatureData(activeFeature);
			}
		}
	}

	private void acceptMsMs() {

		if (activeMsMs != null) {

			if(filteredMsMs != null) {

				activeFeature.getSpectrum().getTandemSpectra().remove(activeMsMs);
				activeMsMs = new TandemMassSpectrum(filteredMsMs);
				filteredMsMs = null;
			}
			activeFeature.getSpectrum().addTandemMs(activeMsMs);
			loadFeatureData(activeFeature);
		}
	}

	public synchronized void clearPanel() {

		msMsInfoPanel.clearPanel();
		msmsTable.clearTable();
		msPlot.removeAllDataSets();
		activeFeature = null;
		activeMsMs = null;
		featureAnnotationPanel.clearPanel();
	}

	public void loadFeatureData(MsFeature activeFeature2) {

		clearPanel();
		activeFeature = (LibraryMsFeature) activeFeature2;
		msMsInfoPanel.loadFeatureData(activeFeature2);

		if(activeFeature.getSpectrum() != null) {

			if (!activeFeature.getSpectrum().getTandemSpectra().isEmpty()) {

				activeMsMs = activeFeature.getSpectrum().getTandemSpectra().iterator().next();
				loadMsMsData(activeMsMs);
			}
		}
	}

	public void loadMsMsData(TandemMassSpectrum msms) {

		if(msms != null) {

			blockPanel();
			activeMsMs = msms;
			msMsInfoPanel.showMsMsParameters(msms);

			msPlot.showTandemMs(msms);
			msmsTable.setTableModelFromTandemMs(msms);
			featureAnnotationPanel.loadFeatureData(msms);

			unblockPanel();
		}
	}

//	private void importMsMsFromMsp() {
//
//		File msmsInput = selectInputFile(MainActionCommands.IMPORT_MSMS_FROM_MSP_COMMAND.getName());
//
//		if (msmsInput != null && activeFeature != null) {
//
//			try {
//				activeMsMs = MsImportUtils.parseMSPspectrum(msmsInput, activeFeature);
//				loadMsMsData(activeMsMs);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//	}

	private void filterMsMs() {

		if(msMsFilterDialog == null)
			msMsFilterDialog = new MsMsFilterDialog(this);

		filteredMsMs = new TandemMassSpectrum(activeMsMs);
		Collection<MsPoint> filteredPoints = filteredMsMs.getSpectrum();

		if(msMsFilterDialog.removeMassesAboveParent()) {

			double maxMass = filteredMsMs.getParent().getMz() + 1.0;
			filteredPoints = filteredPoints.stream().filter(dp -> dp.getMz() < maxMass).collect(Collectors.toSet());
		}
		double intensityCutoff = msMsFilterDialog.getMinimalIntensityCutoff();
		if(intensityCutoff > 0)
			filteredPoints = filteredPoints.stream().filter(dp -> dp.getIntensity() > intensityCutoff).collect(Collectors.toSet());

		int maxFragments = msMsFilterDialog.getMaxFragmentsCount();
		if(maxFragments > 0) {

			filteredPoints =
					filteredPoints.stream().sorted(new MsDataPointComparator(SortProperty.Intensity, SortDirection.DESC)).
					limit(maxFragments).collect(Collectors.toSet());

			filteredPoints.add(filteredMsMs.getActualParentIon());
		}
		filteredMsMs.setSpectrum(filteredPoints);
		loadMsMsData(filteredMsMs);
		msMsFilterDialog.savePreferences();
		msMsFilterDialog.setVisible(false);
	}

	private void resetMsMsFilter() {

		filteredMsMs = null;

		if(activeMsMs != null)
			loadMsMsData(activeMsMs);
	}

	@Override
	public void valueChanged(ListSelectionEvent event) {

		if (!event.getValueIsAdjusting()) {

			activeMsMs = msMsInfoPanel.getSelectedMsMs();
			loadMsMsData(activeMsMs);
		}
	}

	@Override
	public void loadLayout(File layoutFile) {

		if(layoutConfigFile.exists()) {
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

	@Override
	public void saveLayout(File layoutFile) {

		try {
			control.writeXML(layoutFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File getLayoutFile() {
		return layoutConfigFile;
	}

	public void blockPanel() {
		defaultGlassPane = ((JPanel)this.getContentPane()).getRootPane().getGlassPane();
		((JPanel)this.getContentPane()).getRootPane().setGlassPane(new GlassPane());
	}

	public void unblockPanel() {
		((JPanel)this.getContentPane()).getRootPane().setGlassPane(defaultGlassPane);
	}
}























