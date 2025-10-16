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

package edu.umich.med.mrc2.datoolbox.gui.idworks.manid;

import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AdductRenderer;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class AdductChooserTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -392741375728418413L;

	public AdductChooserTable() {

		super();
		model = new AdductChooserTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<AdductChooserTableModel>(
				(AdductChooserTableModel)model);
		setRowSorter(rowSorter);
		
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setDefaultRenderer(Adduct.class, new AdductRenderer());
		columnModel.getColumnById(AdductChooserTableModel.MASS_CORRECTION_COLUMN).
			setCellRenderer(mzRenderer);

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void loadAdductsForFeature(MsFeature feature) {

		thf.setTable(null);
		clearTable();
		if(feature == null)
			return;
		
		Collection<Adduct> adducts = 
				AdductManager.getAdductsForPolarity(feature.getPolarity());
		//	adducts.addAll(AdductManager.getNeutralLosses());
		((AdductChooserTableModel)model).setTableModelFromAdductList(adducts);	
		Adduct primaryAdduct = null;		
		if(feature.getPrimaryIdentity() != null)
			primaryAdduct = feature.getPrimaryIdentity().getPrimaryAdduct();
			
		if(primaryAdduct == null) {
			
			MassSpectrum spectrum = feature.getSpectrum();
			if(spectrum != null) 	
				primaryAdduct = spectrum.getPrimaryAdduct();
			else
				primaryAdduct = AdductManager.getDefaultAdductForCharge(feature.getCharge());
		}
		int col = getColumnIndex(AdductChooserTableModel.CHEM_MOD_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			if(primaryAdduct.equals((Adduct)getValueAt(i, col))){
				setRowSelectionInterval(i, i);
				scrollToSelected();
				break;
			}
		}
		thf.setTable(this);
		tca.adjustColumns();
	}
	
	public void selectAdduct(Adduct adduct) {
		
		int col = getColumnIndex(AdductChooserTableModel.CHEM_MOD_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			if(adduct.equals((Adduct)getValueAt(i, col))){
				setRowSelectionInterval(i, i);
				scrollToSelected();
				break;
			}
		}
	}

	public Adduct getSelectedAdduct(){

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (Adduct) model.getValueAt(convertRowIndexToModel(row), 
				model.getColumnIndex(AdductChooserTableModel.CHEM_MOD_COLUMN));
	}

	
}






