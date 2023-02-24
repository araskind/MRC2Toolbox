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

package edu.umich.med.mrc2.datoolbox.gui.io.matcher;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.SampleDataResultObject;
import edu.umich.med.mrc2.datoolbox.data.compare.ExperimentalSampleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SampleDataResultObjectComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.ExperimentalSampleFormat;
import edu.umich.med.mrc2.datoolbox.data.format.SampleDataResultObjectFormat;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.ExperimentalSampleSelectorEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRendererExtended;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.SampleDataResultObjectRenderer;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class DataFileSampleMatchTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3111543056268144390L;
	private DataFileSampleMatchTableModel model;

	public DataFileSampleMatchTable() {

		super();

		getTableHeader().setReorderingAllowed(false);
		model = new DataFileSampleMatchTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<DataFileSampleMatchTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(DataFileSampleMatchTableModel.SDR_OBJECT_COLUMN),
				new SampleDataResultObjectComparator(SortProperty.resultFile));
		rowSorter.setComparator(model.getColumnIndex(DataFileSampleMatchTableModel.SAMPLE_COLUMN),
				new ExperimentalSampleComparator(SortProperty.ID));
		
		setDefaultRenderer(ExperimentalSample.class, new ExperimentalSampleRendererExtended());
		//	setDefaultRenderer(DataFile.class, new DataFileCellRenderer());
		setDefaultRenderer(SampleDataResultObject.class, 
				new SampleDataResultObjectRenderer(SortProperty.resultFile));

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(ExperimentalSample.class, 
				new ExperimentalSampleFormat(SortProperty.ID));
		thf.getParserModel().setComparator(ExperimentalSample.class, 
				new ExperimentalSampleComparator(SortProperty.ID));		
		thf.getParserModel().setFormat(SampleDataResultObject.class, 
				new SampleDataResultObjectFormat(SortProperty.resultFile));
		thf.getParserModel().setComparator(SampleDataResultObject.class, 
				new SampleDataResultObjectComparator(SortProperty.resultFile));
		
		finalizeLayout();
		columnModel.getColumnById(DataFileSampleMatchTableModel.ENABLED_COLUMN).setMaxWidth(50);
	}
	
	public void setModelFromSampleDataResultObjects(Set<SampleDataResultObject>objects) {
		
		thf.setTable(null);
		model.setModelFromSampleDataResultObjects(objects);
		TreeSet<ExperimentalSample> samples = 
				MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getSamples();
		
		setDefaultEditor(ExperimentalSample.class,
				new ExperimentalSampleSelectorEditor(samples, this));

		thf.setTable(this);
		tca.adjustColumnsExcluding(Collections.singleton(
				getColumnIndex(DataFileSampleMatchTableModel.ENABLED_COLUMN)));
	}
	
	public Set<SampleDataResultObject>getSampleDataResultObject(boolean enabledOnly){
		
		Set<SampleDataResultObject>objects = 
				new TreeSet<SampleDataResultObject>(new SampleDataResultObjectComparator(SortProperty.resultFile));
		
		int bCol = model.getColumnIndex(DataFileSampleMatchTableModel.ENABLED_COLUMN);
		int col = model.getColumnIndex(DataFileSampleMatchTableModel.SDR_OBJECT_COLUMN);
		int sCol = model.getColumnIndex(DataFileSampleMatchTableModel.SAMPLE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			
			if(enabledOnly && !(Boolean)model.getValueAt(i, bCol))
				continue;
			
			SampleDataResultObject o = (SampleDataResultObject)model.getValueAt(i, col);
			ExperimentalSample s = (ExperimentalSample)model.getValueAt(i, sCol);
			o.setSample(s);
			objects.add(o);
		}		
		return objects;
	}
		
	public Set<SampleDataResultObject> getSelectedSampleDataResultObjects(){
		
		Set<SampleDataResultObject>objects = 
				new TreeSet<SampleDataResultObject>(new SampleDataResultObjectComparator(SortProperty.resultFile));
		
		int bCol = model.getColumnIndex(DataFileSampleMatchTableModel.ENABLED_COLUMN);
		int col = model.getColumnIndex(DataFileSampleMatchTableModel.SDR_OBJECT_COLUMN);
		int sCol = model.getColumnIndex(DataFileSampleMatchTableModel.SAMPLE_COLUMN);
		for(int r : getSelectedRows()) {
			
			int i = convertRowIndexToModel(r);
			SampleDataResultObject o = (SampleDataResultObject)model.getValueAt(i, col);
			ExperimentalSample s = (ExperimentalSample)model.getValueAt(i, sCol);
			o.setSample(s);
			objects.add(o);
		}		
		return objects;
	}
	
	public void refreshSampleEditor() {
		
		setDefaultEditor(ExperimentalSample.class,
				new ExperimentalSampleSelectorEditor(
						MRC2ToolBoxCore.getActiveMetabolomicsExperiment().getExperimentDesign().getSamples(), this));

		tca.adjustColumnsExcluding(Collections.singleton(
				getColumnIndex(DataFileSampleMatchTableModel.ENABLED_COLUMN)));
	}
}
















