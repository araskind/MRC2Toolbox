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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.editor;

import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.enums.StandardFactors;
import edu.umich.med.mrc2.datoolbox.main.ReferenceSamplesManager;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class ExperimentDesignTableModel extends DefaultTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 7744317016355708140L;

	public static final String ROWNUM_COLUMN = "##";
	public static final String ENABLED_COLUMN = "Enabled";
	public static final String SAMPLE_ID_COLUMN = "Sample ID";
	public static final String SAMPLE_NAME_COLUMN = "Sample name";

	/**
	 *
	 */
	public ExperimentDesignTableModel() {
		super();
		addColumn(ROWNUM_COLUMN);
		addColumn(ENABLED_COLUMN);
		addColumn(SAMPLE_ID_COLUMN);
		addColumn(SAMPLE_NAME_COLUMN);
	}

	public void clearModel() {

		setRowCount(0);
		for (int i = columnIdentifiers.size() - 1; i > 3; i--)
			columnIdentifiers.removeElementAt(i);
	}

	@Override
	public Class getColumnClass(int col) {

		if (col == 0) // row num column
			return Integer.class;
		else if (col == 1) // Sample enabled column
			return Boolean.class;
		else if (col == 2) // Sample ID
			return ExperimentalSample.class;
		else if(col == 3)
			return String.class;
		else
			return ExperimentDesignLevel.class; // Level columns
	}

	public int getColumnIdex(String columnName) {

		int index = -1;

		for (int i = 0; i < columnIdentifiers.size(); i++) {

			if (columnName.equalsIgnoreCase(columnIdentifiers.get(i).toString()))
				index = i;
		}
		return index;
	}

	@Override
	public boolean isCellEditable(int row, int column) {

		if (column == 0 || column == 2) // Row number and sample ID columns are locked
			return false;
		else if(isReferenceSampleRow(row) && column > 0)	//	Lock reference sample columns for editing
			return false;
		else if(isSampleTypeColum(column))	//	Lock sample type column for editing - define through sample ID
			return false;
		else
			return true;
	}

	private boolean isSampleTypeColum(int column) {
		return getColumnIdex(StandardFactors.SAMPLE_CONTROL_TYPE.getName()) == column;
	}

	private boolean isReferenceSampleRow(int row) {

		ExperimentalSample es = (ExperimentalSample) getValueAt(row, getColumnIdex(SAMPLE_ID_COLUMN));
		return ReferenceSamplesManager.isReferenceSample(es);
	}

	public void loadDesignFromDesignObject(ExperimentDesign experimentDesign) {

		TableModelListener[] listeners = getTableModelListeners();

		for(TableModelListener l : listeners)
			this.removeTableModelListener(l);

		clearModel();

		for (ExperimentDesignFactor ef : experimentDesign.getOrderedFactors())
			addColumn(ef);

		int columnCount;
		int rowCount = 0;
		
		for (ExperimentalSample es : experimentDesign.getSamples()) {

			columnCount = 0;
			rowCount++;
			Object[] newRow = new Object[experimentDesign.getFactors().size() + 4];
			newRow[columnCount] = rowCount;
			newRow[++columnCount] = es.isEnabled();
			newRow[++columnCount] = es;
			newRow[++columnCount] = es.getName();

			for (ExperimentDesignFactor ef : experimentDesign.getOrderedFactors())
				newRow[++columnCount] = es.getLevel(ef);

			addRow(newRow);
		}
		for(TableModelListener l : listeners)
			this.addTableModelListener(l);

		fireTableStructureChanged();
	}

	public void loadDesignFromProject(DataAnalysisProject cefAnalyzerProject) {
		loadDesignFromDesignObject(cefAnalyzerProject.getExperimentDesign());
	}
	
	public int getColumnIndex(String columnName) {

		int index = -1;
		for (int i = 0; i < columnIdentifiers.size(); i++) {

			if (columnName.equals(columnIdentifiers.elementAt(i)))
				index = i;
		}
		return index;
	}
}
























