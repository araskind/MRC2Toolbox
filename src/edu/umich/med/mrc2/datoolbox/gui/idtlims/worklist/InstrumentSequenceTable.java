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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.worklist;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.DataFile;
import edu.umich.med.mrc2.datoolbox.data.ExperimentalSample;
import edu.umich.med.mrc2.datoolbox.data.StockSample;
import edu.umich.med.mrc2.datoolbox.data.Worklist;
import edu.umich.med.mrc2.datoolbox.data.compare.AnalysisMethodComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.ExperimentalSampleComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.LIMSSamplePreparationComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.AnalysisMethodFormat;
import edu.umich.med.mrc2.datoolbox.data.format.ExperimentalSampleFormat;
import edu.umich.med.mrc2.datoolbox.data.format.LIMSSamplePreparationFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.DataAcquisitionMethod;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSExperiment;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSSamplePreparation;
import edu.umich.med.mrc2.datoolbox.database.idt.IDTUtils;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.ExperimentalSampleSelectorEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.StringSelectorEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DateTimeCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ExperimentalSampleRendererExtended;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.StockSampleRenderer;

public class InstrumentSequenceTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5111265849253755884L;
	private InstrumentSequenceTableModel model;

	public InstrumentSequenceTable() {
		super();
		model = new InstrumentSequenceTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<InstrumentSequenceTableModel>(model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(InstrumentSequenceTableModel.SAMPLE_COLUMN),
				new ExperimentalSampleComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(InstrumentSequenceTableModel.SAMPLE_PREP_COLUMN),
				new LIMSSamplePreparationComparator(SortProperty.Name));
		rowSorter.setComparator(model.getColumnIndex(InstrumentSequenceTableModel.ACQ_METHOD_COLUMN),
				new AnalysisMethodComparator(SortProperty.Name));
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		setDefaultRenderer(ExperimentalSample.class, new ExperimentalSampleRendererExtended());
		setDefaultRenderer(StockSample.class, new StockSampleRenderer(SortProperty.ID));
		setDefaultRenderer(Date.class, new DateTimeCellRenderer());

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(ExperimentalSample.class,
				new ExperimentalSampleFormat(SortProperty.ID));
		thf.getParserModel().setComparator(ExperimentalSample.class,
				new ExperimentalSampleComparator(SortProperty.ID));
		thf.getParserModel().setFormat(LIMSSamplePreparation.class,
				new LIMSSamplePreparationFormat(SortProperty.Name));
		thf.getParserModel().setComparator(LIMSSamplePreparation.class,
				new LIMSSamplePreparationComparator(SortProperty.Name));
		thf.getParserModel().setComparator(DataAcquisitionMethod.class,
				new AnalysisMethodComparator(SortProperty.Name));
		thf.getParserModel().setFormat(DataAcquisitionMethod.class,
				new AnalysisMethodFormat(SortProperty.Name));

		finalizeLayout();
	}

	public void populateTableFromWorklistExperimentAndSamplePrep(
			Worklist wkl,
			LIMSExperiment experiment,
			LIMSSamplePreparation activeSamplePrep) {

		thf.setTable(null);
		Collection<? extends ExperimentalSample>samples = new ArrayList<>();
		if(experiment != null && experiment.getExperimentDesign() != null) {
			samples = experiment.getExperimentDesign().getSamples();
		}
		else {
			if(activeSamplePrep != null) {
				try {
					samples = IDTUtils.getSamplesForPrep(activeSamplePrep);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		setDefaultEditor(ExperimentalSample.class,
				new ExperimentalSampleSelectorEditor(samples, this));

		Collection<String>prepItems = new TreeSet<String>();
		if(activeSamplePrep != null && activeSamplePrep.getPrepItemMap() != null)
			prepItems = activeSamplePrep.getPrepItemMap().keySet();
			
		columnModel.getColumnById(InstrumentSequenceTableModel.SAMPLE_PREP_ITEM_COLUMN).
				setCellEditor(new StringSelectorEditor(prepItems, this));

		model.setTableModelFromLimsWorklist(wkl);
		thf.setTable(this);
		tca.adjustColumns();
	}
	
	public void updateColumnEditorsFromSamplesAndPrep(
			Collection<? extends ExperimentalSample>samples, 
			LIMSSamplePreparation activeSamplePrep) {
		
		if(samples == null)
			samples = new ArrayList<ExperimentalSample>();
		
		setDefaultEditor(ExperimentalSample.class,
				new ExperimentalSampleSelectorEditor(samples, this));
		
		Collection<String>prepItems = new TreeSet<String>();
		if(activeSamplePrep != null && activeSamplePrep.getPrepItemMap() != null)
			prepItems = activeSamplePrep.getPrepItemMap().keySet();
			
		columnModel.getColumnById(InstrumentSequenceTableModel.SAMPLE_PREP_ITEM_COLUMN).
				setCellEditor(new StringSelectorEditor(prepItems, this));
		
		revalidate();
		repaint();
	}

	public String getWorklistsAsString() {
		return getTableDataAsString();
	}

	public Collection<ExperimentalSample> getSelectedSamples() {

		Collection<ExperimentalSample> selectedSamples = new ArrayList<ExperimentalSample>();
		if(getSelectedRowCount() == 0)
			return selectedSamples;

		int sampleColumn = model.getColumnIndex(InstrumentSequenceTableModel.SAMPLE_COLUMN);
		for(int i : getSelectedRows())
			selectedSamples.add((ExperimentalSample)model.getValueAt(convertRowIndexToModel(i), sampleColumn));

		return selectedSamples;
	}

	public Collection<DataFile> getSelectedDataFiles() {

		Collection<DataFile> selectedFiles = new ArrayList<DataFile>();
		if(getSelectedRowCount() == 0)
			return selectedFiles;

		int fileColumn = model.getColumnIndex(InstrumentSequenceTableModel.DATA_FILE_COLUMN);
		for(int i : getSelectedRows())
			selectedFiles.add((DataFile)model.getValueAt(convertRowIndexToModel(i), fileColumn));

		return selectedFiles;
	}
	
	public Set<DataAcquisitionMethod>getDataAcquisitionMethods(){
		
		Set<DataAcquisitionMethod>methods = 
				new HashSet<DataAcquisitionMethod>();
		
		int methodColumn = model.getColumnIndex(
				InstrumentSequenceTableModel.ACQ_METHOD_COLUMN);
		for(int i=0; i<model.getRowCount(); i++) {
			DataAcquisitionMethod method = (DataAcquisitionMethod)model.getValueAt(i, methodColumn);
			if(method != null)
				methods.add(method);
		}	
		return methods;
	}
}
























