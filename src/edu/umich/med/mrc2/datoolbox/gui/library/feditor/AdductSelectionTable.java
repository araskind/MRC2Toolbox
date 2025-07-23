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

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.LibraryMsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.AdductSubset;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AdductRenderer;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class AdductSelectionTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -392741375728418413L;

	public AdductSelectionTable() {

		super();
		model = new AdductSelectionTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<AdductSelectionTableModel>(
				(AdductSelectionTableModel)model);
		setRowSorter(rowSorter);
		
		getTableHeader().setReorderingAllowed(false);
		chmodRenderer = new AdductRenderer();
		setDefaultRenderer(Adduct.class, chmodRenderer);
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void loadFeatureData(
			LibraryMsFeature feature,
			Polarity polarity, 
			AdductSubset adductSubset) {

		Collection<Adduct> adducts = new ArrayList<Adduct>();
		if(adductSubset.equals(AdductSubset.COMPLETE_LIST))
			adducts = AdductManager.getAdductsForPolarity(polarity);
		
		if(adductSubset.equals(AdductSubset.SELECTED_ONLY)  
				&& feature.getSpectrum() != null)
			adducts = feature.getSpectrum().getAdducts();
		
		if(adductSubset.equals(AdductSubset.MOST_COMMON))			
			adducts = AdductManager.getSimplifiedAdductListForPolarity(polarity);
		
		thf.setTable(null);
		((AdductSelectionTableModel)model).setTableModelFromAdductListAndFeature(adducts, feature);
		thf.setTable(this);
		adjustColumns();
	}
	
	public void setTableModelFromAdductListForPolarityAndSubset(
			Polarity polarity, AdductSubset adductSubset) {
		
		Collection<Adduct> adducts = new ArrayList<Adduct>();
		if(adductSubset.equals(AdductSubset.COMPLETE_LIST))
			adducts = AdductManager.getAdductsForPolarity(polarity);
		
		if(adductSubset.equals(AdductSubset.MOST_COMMON))			
			adducts = AdductManager.getSimplifiedAdductListForPolarity(polarity);
		
		setTableModelFromAdductList(adducts);
	}
	
	public void setTableModelFromAdductListForPolarity(Polarity polarity) {
		
		Collection<Adduct> adducts = 
				AdductManager.getAdductsForPolarity(polarity);
		setTableModelFromAdductList(adducts);
	}
	
	public void setTableModelFromAdductList(Collection<Adduct> adducts) {
		
		thf.setTable(null);
		((AdductSelectionTableModel)model).setTableModelFromAdductList(adducts);
		thf.setTable(this);
		adjustColumns();
	}

	public Collection<Adduct>getSelectedAdducts(){

		Collection<Adduct> selected = new ArrayList<Adduct>();
		int activeIdx = model.getColumnIndex(AdductSelectionTableModel.ENABLED_COLUMN);
		int adductIdx = model.getColumnIndex(AdductSelectionTableModel.CHEM_MOD_COLUMN);

		for(int i=0; i<model.getRowCount(); i++) {

			if((boolean) model.getValueAt(i, activeIdx))
				selected.add((Adduct) model.getValueAt(i, adductIdx));
		}
		return selected;
	}
}
