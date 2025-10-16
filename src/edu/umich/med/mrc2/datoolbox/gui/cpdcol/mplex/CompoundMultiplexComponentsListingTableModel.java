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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.mplex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundCollectionComponent;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixture;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixtureComponent;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;

public class CompoundMultiplexComponentsListingTableModel extends BasicTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3707538261469467980L;

	public static final String COLLECTION_ID_COLUMN = "CollectionID";
	public static final String MIXTURE_ID_COLUMN = "MixtureID";
	public static final String IS_MS_READY_COLUMN = "MSReady";
	public static final String ID_COLUMN = "ComponentID";
	public static final String NAME_COLUMN = "Name";
	public static final String ACCESSION_COLUMN = "Accession";
	public static final String CAS_COLUMN = "CAS";	
	public static final String SOLVENT_COLUMN = "Solvent";
	public static final String CONCENTRATION_COLUMN = "Conc., " + '\u03BC' + "M";
	public static final String XLOGP_COLUMN = "XLogP";
	public static final String ALIQUOTE_VOLUME_COLUMN = "Subaliquot volume";
	public static final String FORMULA_COLUMN = "Formula";	
	public static final String SMILES_FORMULA_COLUMN = "SMILES Formula";
	public static final String CHARGE_COLUMN = "CHARGE";
	public static final String FORMULAS_DELTA_MASS_COLUMN = '\u0394' + " mass(?)";
	public static final String CONFLICT_COLUMN = "Conflict";
	
	public CompoundMultiplexComponentsListingTableModel() {
		super();
		columnArray = new ColumnContext[] {
			new ColumnContext(ID_COLUMN, "Compound collection ID", String.class, false),
			new ColumnContext(MIXTURE_ID_COLUMN, "Multiplex mixture ID", CompoundMultiplexMixture.class, false),
			new ColumnContext(IS_MS_READY_COLUMN, "Is compound in MS-ready form?", Boolean.class, false),
			new ColumnContext(NAME_COLUMN, "Compound name", CompoundMultiplexMixtureComponent.class, false),
			new ColumnContext(ACCESSION_COLUMN, "Primary database accession and web link to the source database", CompoundIdentity.class, false),
			new ColumnContext(CAS_COLUMN, "CAS registry number", String.class, false),			
			new ColumnContext(SOLVENT_COLUMN, "Solvent used", MobilePhase.class, false),
			new ColumnContext(CONCENTRATION_COLUMN, "Concentration, " + '\u03BC' + "M", Double.class, false),
			new ColumnContext(XLOGP_COLUMN, "octanol/water partition coefficient", Double.class, false),
			new ColumnContext(ALIQUOTE_VOLUME_COLUMN, ALIQUOTE_VOLUME_COLUMN, Double.class, false),			
			new ColumnContext(FORMULA_COLUMN, "Formula provided by manufacturer", String.class, false),	
			new ColumnContext(SMILES_FORMULA_COLUMN, "Formula calculated from SMILES string", String.class, false),	
			new ColumnContext(CHARGE_COLUMN, CHARGE_COLUMN, Integer.class, false),			
			new ColumnContext(FORMULAS_DELTA_MASS_COLUMN, 
					"Mass difference between the formula provided by manufacturer and calculated from SMILES", Double.class, false),
			new ColumnContext(CONFLICT_COLUMN, "Is formula conflict present?", Boolean.class, false),
		};		
	}	

	public void setTableModelFromCompoundMultiplexMixtureComponents(
			Collection<CompoundMultiplexMixtureComponent>components) {

		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();

		for(CompoundMultiplexMixtureComponent component : components){

			CompoundCollectionComponent ccComp = component.getCCComponent();
			CompoundIdentity cid = ccComp.getCid();
			Object[] obj = {
					ccComp.getCollectionId(),
					null,
					ccComp.isMsReady(),
					component,
					cid,
					ccComp.getCas(),
					component.getSolvent(),
					component.getConcentrationMkm(),
					component.getXlogp(),
					component.getAliquoteVolume(),
					ccComp.getPrimary_formula(),
					ccComp.getFormula_from_primary_smiles(),
					ccComp.getCharge_from_primary_smiles(),
					ccComp.getPrimary_formula_mass_conflict(),
					ccComp.hasConflict(),
				};
			rowData.add(obj);
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}

	public void setTableModelFromCompoundMultiplexMixtures(
			Collection<CompoundMultiplexMixture> mixtures) {
		
		setRowCount(0);
		List<Object[]>rowData = new ArrayList<Object[]>();
		
		for(CompoundMultiplexMixture mixture : mixtures) {
			
			for(CompoundMultiplexMixtureComponent component : mixture.getComponents()){

				CompoundCollectionComponent ccComp = component.getCCComponent();
				CompoundIdentity cid = ccComp.getCid();
				Object[] obj = {
						ccComp.getId(),
						mixture,
						ccComp.isMsReady(),
						component,
						cid,
						ccComp.getCas(),
						component.getSolvent(),
						component.getConcentrationMkm(),
						component.getXlogp(),
						component.getAliquoteVolume(),
						ccComp.getPrimary_formula(),
						ccComp.getFormula_from_primary_smiles(),
						ccComp.getCharge_from_primary_smiles(),
						ccComp.getPrimary_formula_mass_conflict(),
						ccComp.hasConflict(),
					};
				rowData.add(obj);
			}
		}
		if(!rowData.isEmpty())
			addRows(rowData);
	}

	public void updateComponentData(CompoundMultiplexMixtureComponent component) {

		int col = getColumnIndex(NAME_COLUMN);
		for(int i=0; i<getRowCount(); i++) {
			
			if(getValueAt(i, col).equals(component)) {
				
				CompoundCollectionComponent ccComp = component.getCCComponent();
				CompoundIdentity cid = ccComp.getCid();
				
				setValueAt(ccComp.getId(), i, getColumnIndex(ID_COLUMN));
//				setValueAt(cid, i, getColumnIndex(MIXTURE_ID_COLUMN));
				setValueAt(ccComp.isMsReady(), i, getColumnIndex(IS_MS_READY_COLUMN));
				setValueAt(component, i, getColumnIndex(NAME_COLUMN));
				setValueAt(cid, i, getColumnIndex(ACCESSION_COLUMN));
				setValueAt(ccComp.getCas(), i, getColumnIndex(CAS_COLUMN));				
				setValueAt(component.getSolvent(), i, getColumnIndex(SOLVENT_COLUMN));
				setValueAt(component.getConcentrationMkm(), i, getColumnIndex(CONCENTRATION_COLUMN));
				setValueAt(component.getXlogp(), i, getColumnIndex(XLOGP_COLUMN));
				setValueAt(component.getAliquoteVolume(), i, getColumnIndex(ALIQUOTE_VOLUME_COLUMN));
				setValueAt(ccComp.getPrimary_formula(), i, getColumnIndex(FORMULA_COLUMN));
				setValueAt(ccComp.getFormula_from_primary_smiles(), i, getColumnIndex(SMILES_FORMULA_COLUMN));
				setValueAt(ccComp.getCharge_from_primary_smiles(), i, getColumnIndex(CHARGE_COLUMN));
				setValueAt(ccComp.getPrimary_formula_mass_conflict(), i, getColumnIndex(FORMULAS_DELTA_MASS_COLUMN));
				setValueAt(ccComp.hasConflict(), i, getColumnIndex(CONFLICT_COLUMN));
				return;
			}
		}
	}
}



































