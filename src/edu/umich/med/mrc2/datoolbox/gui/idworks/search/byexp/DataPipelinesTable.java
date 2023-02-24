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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.byexp;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.AnalysisMethodComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.DataPipelineComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.AnalysisMethodFormat;
import edu.umich.med.mrc2.datoolbox.data.format.DataPipelineFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataExtractionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.DataPipeline;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AnalysisMethodRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DataPipelineRenderer;

public class DataPipelinesTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3232039944841480790L;
	private DataPipelinesTableModel model;

	public DataPipelinesTable() {

		super();
		model = new DataPipelinesTableModel();		
		setModel(model);
		rowSorter = new TableRowSorter<DataPipelinesTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(DataPipelinesTableModel.DATA_ACQ_METHOD_COLUMN),
				new AnalysisMethodComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(DataPipelinesTableModel.DATA_PROC_METHOD_COLUMN),
				new AnalysisMethodComparator(SortProperty.Name));
		
		setDefaultRenderer(DataAcquisitionMethod.class, 
				new AnalysisMethodRenderer());
		setDefaultRenderer(DataExtractionMethod.class, 
				new AnalysisMethodRenderer());
		
		columnModel.getColumnById(DataPipelinesTableModel.CODE_COLUMN)
			.setCellRenderer(new DataPipelineRenderer(SortProperty.ID));
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setComparator(DataAcquisitionMethod.class, 
				new AnalysisMethodComparator(SortProperty.Name));
		thf.getParserModel().setFormat(DataAcquisitionMethod.class, 
				new AnalysisMethodFormat(SortProperty.Name));
		thf.getParserModel().setComparator(DataExtractionMethod.class, 
				new AnalysisMethodComparator(SortProperty.Name));
		thf.getParserModel().setFormat(DataExtractionMethod.class, 
				new AnalysisMethodFormat(SortProperty.Name));		
		thf.getParserModel().setComparator(DataPipeline.class, 
				new DataPipelineComparator(SortProperty.ID));
		thf.getParserModel().setFormat(DataPipeline.class, 
				new DataPipelineFormat(SortProperty.ID));
		
		finalizeLayout();
	}

	public void setTableModelFromDataPipelineCollection(Collection<DataPipeline>pipelines) {
		thf.setTable(null);
		model.setTableModelFromDataPipelineCollection(pipelines);
		thf.setTable(this);
		tca.adjustColumns();
		columnModel.getColumnById(DataPipelinesTableModel.POLARITY_COLUMN).setMaxWidth(80);
		columnModel.getColumnById(DataPipelinesTableModel.CODE_COLUMN).setMaxWidth(120);
	}
	
	public Collection<DataPipeline> getSelectedDataPipelines() {
	
		Collection<DataPipeline>selected = new ArrayList<DataPipeline>();
		if(getSelectedRow() == -1)
			return selected;
		
		int dpColumn = model.getColumnIndex(DataPipelinesTableModel.CODE_COLUMN);
		for(int row : getSelectedRows()) {
			
			DataPipeline dp = (DataPipeline)model.getValueAt(convertRowIndexToModel(row), dpColumn);
			selected.add(dp);
		}
		return selected;		
	}
	
	public Collection<DataPipeline> getAllDataPipelines() {
		
		Collection<DataPipeline>pipelines = new ArrayList<DataPipeline>();
		int dpColumn = model.getColumnIndex(DataPipelinesTableModel.CODE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++)
			pipelines.add((DataPipeline)model.getValueAt(i, dpColumn));
		
		return pipelines;
	}

	public void setSelectedDataPipelines(Collection<DataPipeline>selected) {
		
		int dpColumn = model.getColumnIndex(DataPipelinesTableModel.CODE_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			
			if(selected.contains((DataPipeline)model.getValueAt(i, dpColumn)))
				getSelectionModel().addSelectionInterval(i, i);
		}
	}
}




