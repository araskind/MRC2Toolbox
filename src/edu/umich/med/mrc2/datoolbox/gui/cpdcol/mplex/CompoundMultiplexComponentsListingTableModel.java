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
	public static final String ID_COLUMN = "ComponentID";
	public static final String NAME_COLUMN = "Name";
	public static final String ACCESSION_COLUMN = "Accession";
	public static final String CAS_COLUMN = "CAS";	
	public static final String SOLVENT_COLUMN = "Solvent";
	public static final String CONCENTRATION_COLUMN = "Concentration, uM";
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
			new ColumnContext(ID_COLUMN, String.class, false),
			new ColumnContext(MIXTURE_ID_COLUMN, CompoundMultiplexMixture.class, false),
			new ColumnContext(NAME_COLUMN, CompoundMultiplexMixtureComponent.class, false),
			new ColumnContext(ACCESSION_COLUMN, CompoundIdentity.class, false),
			new ColumnContext(CAS_COLUMN, String.class, false),			
			new ColumnContext(SOLVENT_COLUMN, MobilePhase.class, false),
			new ColumnContext(CONCENTRATION_COLUMN, Double.class, false),
			new ColumnContext(XLOGP_COLUMN, Double.class, false),
			new ColumnContext(ALIQUOTE_VOLUME_COLUMN, Double.class, false),			
			new ColumnContext(FORMULA_COLUMN, String.class, false),	
			new ColumnContext(SMILES_FORMULA_COLUMN, String.class, false),	
			new ColumnContext(CHARGE_COLUMN, Integer.class, false),			
			new ColumnContext(FORMULAS_DELTA_MASS_COLUMN, Double.class, false),
			new ColumnContext(CONFLICT_COLUMN, Boolean.class, false),
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
						ccComp.getCollectionId(),
						mixture,
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
		addRows(rowData);
	}
}



































