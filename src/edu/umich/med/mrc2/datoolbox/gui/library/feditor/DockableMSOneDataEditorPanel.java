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

package edu.umich.med.mrc2.datoolbox.gui.library.feditor;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.theme.ThemeMap;
import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.plot.lcms.spectrum.DockableSpectumPlot;
import edu.umich.med.mrc2.datoolbox.gui.tables.ms.DockableMsTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;

public class DockableMSOneDataEditorPanel extends DefaultSingleCDockable implements TableModelListener, ItemListener, PersistentLayout {

	private DockableAductSelector adductSelector;
	private DockableMsTable libraryMsTable;
	private DockableSpectumPlot msPlot;
	private LibraryMsFeature activeFeature;
	private LibraryMsFeature featureCopy;
	private CControl control;
	private CGrid grid;

	private static final SmilesParser smipar = new SmilesParser(SilentChemObjectBuilder.getInstance());
	private static final Pattern pattern = Pattern.compile("(\\[\\d+)");

	private static final Icon componentIcon = GuiUtils.getIcon("msAnnotation", 16);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "MSOnePanel.layout");

	public DockableMSOneDataEditorPanel() {

		super("DockableMSOneDataEditorPanel", componentIcon, "MS1 data editor", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));

		adductSelector = new DockableAductSelector(this, this);
		libraryMsTable = new DockableMsTable(
				"DockableMSOneDataEditorPanelDockableMsTableMS1", "MS1 spectrum table") ;
		msPlot = new DockableSpectumPlot(
				"DockableMSOneDataEditorPanelDockableSpectumPlot", "MS1 spectrum plot");

		control = new CControl( MRC2ToolBoxCore.getMainWindow() );
		control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);
		grid = new CGrid( control );
		grid.add( 0, 0, 25, 100, adductSelector);
		grid.add( 25, 0, 25, 100, libraryMsTable);
		grid.add( 50, 0, 50, 100, msPlot);
		control.getContentArea().deploy( grid );

		add(control.getContentArea(), BorderLayout.CENTER);
		loadLayout(layoutConfigFile);
	}

	public void loadFeatureData(LibraryMsFeature feature, Polarity polarity) {

		activeFeature = feature;
		featureCopy = new LibraryMsFeature(activeFeature);
		adductSelector.setPolarity(polarity);
		adductSelector.loadFeatureData(activeFeature);
		msPlot.showMsForLibraryFeature(activeFeature);
		libraryMsTable.setTableModelFromMsFeature(activeFeature);
	}

	public synchronized void clearPanel() {

		activeFeature = null;
		featureCopy = null;
		adductSelector.clearPanel();
		msPlot.removeAllDataSets();
		libraryMsTable.clearTable();
	}

	public Collection<Adduct> getActiveAdducts() {
		return adductSelector.getSelectedAdducts();
	}

	@Override
	public void tableChanged(TableModelEvent event) {

		if (event.getType() == TableModelEvent.UPDATE) {

			if (activeFeature.getPrimaryIdentity() == null)
				return;

			featureCopy.setSpectrum(new MassSpectrum());
			Map<Adduct, Collection<MsPoint>> adductMap =
					MsUtils.createIsotopicPatternCollection(
						activeFeature.getPrimaryIdentity().getCompoundIdentity(),
						adductSelector.getSelectedAdducts());

			adductMap.entrySet().stream().
				forEach(e -> featureCopy.getSpectrum().
						addSpectrumForAdduct(e.getKey(), e.getValue()));

			msPlot.showMsForLibraryFeature(featureCopy);
			libraryMsTable.setTableModelFromMsFeature(featureCopy);
		}
	}

	public void setAndLockFeaturePolarity(Polarity polarity) {
		adductSelector.setAndLockFeaturePolarity(polarity);
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED) {

			if (activeFeature != null) {

				adductSelector.loadFeatureData(activeFeature);
				msPlot.showMsForLibraryFeature(activeFeature);
				libraryMsTable.setTableModelFromMsFeature(activeFeature);
			} else {
				clearPanel();
			}
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
}
