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

package edu.umich.med.mrc2.datoolbox.gui.worklist.manifest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.table.DefaultTableModel;

import org.apache.commons.lang3.math.NumberUtils;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.WorklistItem;
import edu.umich.med.mrc2.datoolbox.data.enums.AgilentSampleInfoFields;
import edu.umich.med.mrc2.datoolbox.data.enums.MoTrPACRawDataManifestFields;

public class ManifestTableModel extends DefaultTableModel {

	/**
	 *
	 */
	private static final long serialVersionUID = 7936373115803794369L;
	private Worklist activeWorklist;
	private ArrayList<String> columnNames;
	private HashMap<Integer, Class> columnClassMap;

	public ManifestTableModel() {

		super();

		columnNames = new ArrayList<String>();
		columnClassMap = new HashMap<Integer, Class>();
		columnNames.add("##");
		columnClassMap.put(0, Integer.class);
	}

	public void clearModel() {

		setRowCount(0);
		setColumnCount(0);
	}

	private void findPopulatedColumns() {

		ArrayList<String> allColumnNames = new ArrayList<String>();
		for(MoTrPACRawDataManifestFields v : MoTrPACRawDataManifestFields.values())
			allColumnNames.add(v.getName());
		
		ArrayList<String> obligatoryColumnNames = new ArrayList<String>();
		obligatoryColumnNames.addAll(allColumnNames);
		
		List<String>worklistFields = activeWorklist.getTimeSortedWorklistItems().stream().
			flatMap(i -> i.getProperties().keySet().stream()).			
			distinct().
			filter(i -> !allColumnNames.contains(i)).
			sorted().collect(Collectors.toList());
		allColumnNames.addAll(worklistFields);

		LinkedHashMap<String, Integer> valueCount = new LinkedHashMap<String, Integer>();

		for (String field : allColumnNames) {

			valueCount.put(field, 0);

			for (WorklistItem item : activeWorklist.getTimeSortedWorklistItems()) {

				if(item.getProperty(field) == null || item.getProperty(field).isEmpty())
					continue;
				else{
					Integer current = valueCount.get(field) + 1;
					valueCount.replace(field, current);
				}
			}
		}
		columnNames = new ArrayList<String>();
		columnClassMap = new HashMap<Integer, Class>();

		int counter = 0;

		for (Entry<String, Integer> entry : valueCount.entrySet()) {

			if (entry.getValue() > 0 || obligatoryColumnNames.contains(entry.getKey())) {

				if (entry.getKey().equals(AgilentSampleInfoFields.ACQUISITION_TIME.getName())
						|| entry.getKey().equals(AgilentSampleInfoFields.ACQTIME.getName()))
					columnClassMap.put(counter, Date.class);
				else if (entry.getKey().equals(AgilentSampleInfoFields.DATA_FILE.getName()))
					columnClassMap.put(counter, DataFile.class);
				else if (entry.getKey().equals(AgilentSampleInfoFields.INJ_VOL.getName()))
					columnClassMap.put(counter, Double.class);
				else if (entry.getKey().equals(MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_ORDER.getName()))
					columnClassMap.put(counter, Integer.class);
				else
					columnClassMap.put(counter, String.class);

				columnNames.add(entry.getKey());
				addColumn(entry.getKey());
				counter++;
			}
		}
	}

	@Override
	public Class getColumnClass(int col) {
		return columnClassMap.get(col);
	}

	public Vector getColumnIdentifiers() {
		return columnIdentifiers;
	}

	@Override
	public boolean isCellEditable(int row, int column) {

		return false;
	}

	public void setTableModelFromWorklist(Worklist worklist) {

		activeWorklist = worklist;
		clearModel();
		findPopulatedColumns();

		for (WorklistItem item : activeWorklist.getTimeSortedWorklistItems()) {

			Object[] newRow = new Object[columnNames.size()];
			for (int i = 0; i < columnNames.size(); i++) {

				if (columnNames.get(i).equals(AgilentSampleInfoFields.ACQUISITION_TIME.getName())
						|| columnNames.get(i).equals(AgilentSampleInfoFields.ACQTIME.getName()))
					newRow[i] = item.getTimeStamp();
				else if (columnNames.get(i).equals(AgilentSampleInfoFields.DATA_FILE.getName()))
					newRow[i] = item.getDataFile();				
				else if (columnNames.get(i).equals(AgilentSampleInfoFields.INJ_VOL.getName())) {
					
					String p = item.getProperty(AgilentSampleInfoFields.INJ_VOL.getName());
					 if(NumberUtils.isCreatable(p))
						 newRow[i] = Double.parseDouble(p);
					 else
						 newRow[i] = 0.0d; // newRow[i] = item.getProperty(AgilentSampleInfoFields.INJ_VOL.getName());
				}else if (columnNames.get(i).equals(MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_ORDER.getName())) {
					String p = item.getProperty(MoTrPACRawDataManifestFields.MOTRPAC_SAMPLE_ORDER.getName());
					 if(NumberUtils.isCreatable(p))
						 newRow[i] = Integer.parseInt(p);
					 else
						 newRow[i] = null;
				}
				else
					newRow[i] = item.getProperty(columnNames.get(i));
			}
			addRow(newRow);
		}
		fireTableStructureChanged();
	}

}
