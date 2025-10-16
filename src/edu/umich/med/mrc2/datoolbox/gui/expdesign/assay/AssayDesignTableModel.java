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

package edu.umich.med.mrc2.datoolbox.gui.expdesign.assay;

import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.table.DefaultTableModel;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesign;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignFactor;
import edu.umich.med.mrc2.datoolbox.data.ExperimentDesignLevel;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.project.DataAnalysisProject;

public class AssayDesignTableModel extends DefaultTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 3728620451705940293L;
	public static final String ORDER_COLUMN = "##";
	public static final String ENABLED_COLUMN = "Enabled";
	public static final String DATA_FILE_COLUMN = "Data file";
	public static final String SAMPLE_ID_COLUMN = "Sample ID";
	public static final String SAMPLE_NAME_COLUMN = "Sample name";
	public static final String BATCH_COLUMN = "Batch";

	public static final int ENABLED_COLUMN_INDEX = 1;
	public static final int DATA_FILE_COLUMN_INDEX = 2;
	public static final int SAMPLE_COLUMN_INDEX = 3;

	public AssayDesignTableModel() {

		super();

		addColumn(ORDER_COLUMN);
		addColumn(ENABLED_COLUMN);
		addColumn(DATA_FILE_COLUMN);
		addColumn(SAMPLE_ID_COLUMN);
		addColumn(SAMPLE_NAME_COLUMN);
		addColumn(BATCH_COLUMN);
	}

	public void clearModel() {

		setRowCount(0);
		setColumnCount(0);
		addColumn(ORDER_COLUMN);
		addColumn(ENABLED_COLUMN);
		addColumn(DATA_FILE_COLUMN);
		addColumn(SAMPLE_ID_COLUMN);
		addColumn(SAMPLE_NAME_COLUMN);
		addColumn(BATCH_COLUMN);
	}

	@Override
	public Class getColumnClass(int col) {

		if (col == 0) // Counter
			return Integer.class;
		else if (col == 1) // File enabled column
			return Boolean.class;
		else if (col == 2) // File name column
			return DataFile.class;
		else if (col == 3) // Sample ID column
			return ExperimentalSample.class;
		else if (col == 4) // Sample ID column
			return String.class;
		else if (col == 5) // Batch column
			return Integer.class;
		else
			return ExperimentDesignLevel.class; // Level columns
	}

	public Vector getColumnIdentifiers() {
		return columnIdentifiers;
	}

	@Override
	public boolean isCellEditable(int row, int column) {

		if (column == 1 && getValueAt(row, 1) != null) // Sample name column
			return true;
		else if (column == 3) // Sample column
			return true;
		else
			return false;
	}
	
	public int getColumnIndex(String columnName) {

		int index = -1;

		for (int i = 0; i < columnIdentifiers.size(); i++) {

			if (columnName.equals(columnIdentifiers.elementAt(i)))
				index = i;
		}
		return index;
	}

	public void setTableModelFromExperimentDesign(
			DataAnalysisProject currentProject, DataPipeline activeDataPipeline) {

		clearModel();
		if(currentProject == null || activeDataPipeline == null)
			return;

		ExperimentDesign design = currentProject.getExperimentDesign();
		ExperimentDesignFactor[] factors = design.getOrderedFactors();
		for (ExperimentDesignFactor ef : factors)
			addColumn(ef);

		int columnCount = 0;
		int fileCount = 1;

		List<ExperimentalSample> sampleList = design.getSamples().stream().
				filter(s -> !s.getDataFilesForMethod(activeDataPipeline.getAcquisitionMethod()).isEmpty()).
				sorted().collect(Collectors.toList());

		if(sampleList.isEmpty())
			return;

		for (ExperimentalSample es : sampleList) {

			for (DataFile df : es.getDataFilesForMethod(activeDataPipeline.getAcquisitionMethod())) {

				columnCount = 0;
				Object[] newRow = new Object[design.getFactors().size() + 6];
				newRow[columnCount] = fileCount;
				newRow[++columnCount] = df.isEnabled();
				newRow[++columnCount] = df;
				newRow[++columnCount] = es;
				newRow[++columnCount] = es.getName();
				newRow[++columnCount] = df.getBatchNumber();

				for (ExperimentDesignFactor ef : factors)
					newRow[++columnCount] = es.getLevel(ef);

				addRow(newRow);
				fileCount++;
			}
		}
	}
}




