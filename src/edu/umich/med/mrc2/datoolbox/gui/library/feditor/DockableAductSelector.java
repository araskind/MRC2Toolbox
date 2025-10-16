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
import java.awt.event.ItemListener;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelListener;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.AdductSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableAductSelector extends DefaultSingleCDockable{

	private AdductSelectionTable adductsTable;
	private AdductSelectorControlPanel adductSelectorControlPanel;

	private static final Icon componentIcon = GuiUtils.getIcon("chemModList", 16);

	public DockableAductSelector(
			TableModelListener adductSelectonListener, 
			ItemListener polarityListener) {

		super("DockableAductSelector", componentIcon, "Adduct selector", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);

		setLayout(new BorderLayout(0, 0));
		adductSelectorControlPanel = 
				new AdductSelectorControlPanel(polarityListener);
		adductSelectorControlPanel.setPolarity(Polarity.Neutral);
		add(adductSelectorControlPanel, BorderLayout.NORTH);

		adductsTable = new AdductSelectionTable();
		adductsTable.getModel().addTableModelListener(adductSelectonListener);
		add(new JScrollPane(adductsTable), BorderLayout.CENTER);
	}

	public void setPolarity(Polarity polarity) {
		adductSelectorControlPanel.setPolarity(polarity);
	}

	public void loadFeatureData(LibraryMsFeature activeFeature) {
		
		adductsTable.loadFeatureData(
				activeFeature, 
				adductSelectorControlPanel.getPolarity(),
				adductSelectorControlPanel.getAdductSubset());
	}

	public synchronized void clearPanel() {
		adductsTable.clearTable();
	}

	public Collection<Adduct> getSelectedAdducts() {
		return adductsTable.getSelectedAdducts();
	}

	public Polarity getPolarity() {
		return adductSelectorControlPanel.getPolarity();
	}
	
	public void setAndLockFeaturePolarity(Polarity polarity) {
		adductSelectorControlPanel.setAndLockFeaturePolarity(polarity);
	}
	
	public void setAdductSubset(AdductSubset subset) {
		adductSelectorControlPanel.setAdductSubset(subset);
	}

	public AdductSubset getAdductSubset() {
		return adductSelectorControlPanel.getAdductSubset();
	}
}














