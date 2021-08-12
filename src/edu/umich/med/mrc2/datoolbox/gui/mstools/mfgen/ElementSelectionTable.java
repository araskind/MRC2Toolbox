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

package edu.umich.med.mrc2.datoolbox.gui.mstools.mfgen;

import java.io.IOException;

import javax.swing.table.TableRowSorter;

import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.formula.MolecularFormulaRange;
import org.openscience.cdk.formula.rules.ElementRule;
import org.openscience.cdk.interfaces.IIsotope;

import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.SpinnerEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.CdkIsotopeRenderer;

public class ElementSelectionTable extends BasicTable{

	/**
	 *
	 */
	private static final long serialVersionUID = -5112212976307291771L;
	private ElementSelectionTableModel model;
	private CdkIsotopeRenderer isotopeRenderer;
	private SpinnerEditor spinnerEditor;

	public ElementSelectionTable() {

		model = new ElementSelectionTableModel();
		setModel(model);
		getTableHeader().setReorderingAllowed(false);
		rowSorter = new TableRowSorter<ElementSelectionTableModel>(model);
		setRowSorter(rowSorter);
		
		setRowHeight(25);
		isotopeRenderer = new CdkIsotopeRenderer();
		setDefaultRenderer(IIsotope.class, isotopeRenderer);

		spinnerEditor = new SpinnerEditor(0, 1000);
		setDefaultEditor(Integer.class, spinnerEditor);
		finalizeLayout();
	}

	public ElementRule getElementLimits(){

		ElementRule limits = new ElementRule();
		MolecularFormulaRange ranges = new MolecularFormulaRange();
		int enabledColumn = model.getColumnIndex(ElementSelectionTableModel.ENABLED_COLUMN);
		int elementColumn = model.getColumnIndex(ElementSelectionTableModel.ELEMENT_COLUMN);
		int minColumn = model.getColumnIndex(ElementSelectionTableModel.MIN_COLUMN);
		int maxColumn = model.getColumnIndex(ElementSelectionTableModel.MAX_COLUMN);
		
		for(int i=0; i<model.getRowCount(); i++){

			if((boolean) model.getValueAt(i, enabledColumn)){

				IIsotope isotope = (IIsotope) model.getValueAt(i, elementColumn);
				int countMin = (int) model.getValueAt(i, minColumn);
				int countMax = (int) model.getValueAt(i, maxColumn);
				ranges.addIsotope(isotope, countMin, countMax);
			}
		}
		try {
			limits.setParameters(new Object[]{ranges});
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return limits;
	}

	public void setCarbonLimits(int numCarbons, int numCarbonsMax) {

		int elementColumn = model.getColumnIndex(ElementSelectionTableModel.ELEMENT_COLUMN);
		int minColumn = model.getColumnIndex(ElementSelectionTableModel.MIN_COLUMN);
		int maxColumn = model.getColumnIndex(ElementSelectionTableModel.MAX_COLUMN);
		IIsotope carbon = null;
		try {
			carbon = Isotopes.getInstance().getMajorIsotope("C");
		} catch (IOException e) {

			//ErrorDialog ed = new ErrorDialog("Failed to create carbon isotope!", e);
		}
		for(int i=0; i<model.getRowCount(); i++){

			if(model.getValueAt(i, elementColumn).equals(carbon)){

				model.setValueAt(numCarbons, i, minColumn);
				model.setValueAt(numCarbonsMax, i, maxColumn);
			}
		}
	}

	public int[] getCarbonLimits(){
		
		int elementColumn = model.getColumnIndex(ElementSelectionTableModel.ENABLED_COLUMN);
		int minColumn = model.getColumnIndex(ElementSelectionTableModel.MIN_COLUMN);
		int maxColumn = model.getColumnIndex(ElementSelectionTableModel.MAX_COLUMN);
		int[] limits = new int[2];
		IIsotope carbon = null;
		try {
			carbon = Isotopes.getInstance().getMajorIsotope("C");
		} catch (IOException e) {

			//ErrorDialog ed = new ErrorDialog("Failed to create carbon isotope!", e);
		}
		for(int i=0; i<model.getRowCount(); i++){

			if(model.getValueAt(i, elementColumn).equals(carbon)){

				limits[0] = (int) model.getValueAt(i, minColumn);
				limits[1] = (int) model.getValueAt(i, maxColumn);
			}
		}
		return limits;
	}

	public int[] getDefaultCarbonLimits() {

		return new int[]{0,78};
	}

	public void resetElementLimitsToDefault() {

		model.setRowCount(0);
		try {
			model.populateDefaultModel();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}






















