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

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.enums.Polarity;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AdductRenderer;
import edu.umich.med.mrc2.datoolbox.main.AdductManager;

public class LossChooserTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -392741375728418413L;
	private LossChooserTableModel model;

	public LossChooserTable() {

		super();
		model = new LossChooserTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<LossChooserTableModel>(model);
		setRowSorter(rowSorter);
		
		getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		setDefaultRenderer(Adduct.class, new AdductRenderer());
		columnModel.getColumnById(AdductChooserTableModel.MASS_CORRECTION_COLUMN).
			setCellRenderer(mzRenderer);

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void loadAdductsForPolarityAndFeature(Polarity polarity, MsFeature feature) {

		thf.setTable(null);
		model.setTableModelFromLossList(AdductManager.getNeutralLosses());		
		if(feature != null) {
			
			MassSpectrum spectrum = feature.getSpectrum();
			if(spectrum != null) {
				
				Adduct primaryAdduct = spectrum.getPrimaryAdduct();
				int col = getColumnIndex(AdductChooserTableModel.CHEM_MOD_COLUMN);
				for(int i=0; i<getRowCount(); i++) {
					
					if(primaryAdduct.equals((Adduct)getValueAt(i, col))){
						setRowSelectionInterval(i, i);
						scrollToSelected();
						break;
					}
				}
			}
		}
		thf.setTable(this);
		tca.adjustColumns();
	}

	public Adduct getSelectedAdduct(){

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (Adduct) model.getValueAt(convertRowIndexToModel(row), 
				model.getColumnIndex(AdductChooserTableModel.CHEM_MOD_COLUMN));
	}
}
