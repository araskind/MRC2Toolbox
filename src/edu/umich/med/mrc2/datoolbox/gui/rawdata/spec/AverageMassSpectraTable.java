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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.spec;

import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.AverageMassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.compare.AverageMassSpectrumComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.format.AverageMassSpectrumFormat;
import edu.umich.med.mrc2.datoolbox.data.format.RangeFormat;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AverageMassSpectrumRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RangeRenderer;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.Range;

public class AverageMassSpectraTable  extends BasicTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7515659594072618562L;
	
	public AverageMassSpectraTable() {

		super();

		model = new AverageMassSpectraTableModel();
		setModel(model); 	
		rowSorter = new TableRowSorter<AverageMassSpectraTableModel>(
				(AverageMassSpectraTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(model.getColumnIndex(AverageMassSpectraTableModel.DATA_FILE_COLUMN),
				new AverageMassSpectrumComparator(SortProperty.dataFile));

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		columnModel.getColumnById(AverageMassSpectraTableModel.RT_RANGE_COLUMN)
			.setCellRenderer(new RangeRenderer(MRC2ToolBoxConfiguration.getRtFormat()));
		columnModel.getColumnById(AverageMassSpectraTableModel.DATA_FILE_COLUMN)
			.setCellRenderer(new AverageMassSpectrumRenderer());

		columnModel.getColumnById(
				AverageMassSpectraTableModel.RT_RANGE_COLUMN).setMaxWidth(120);
		columnModel.getColumnById(
				AverageMassSpectraTableModel.MS_LEVEL_COLUMN).setMaxWidth(80);
		columnModel.getColumnById(
				AverageMassSpectraTableModel.DATA_FILE_COLUMN).setMinWidth(300);
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(AverageMassSpectrum.class, 
				new AverageMassSpectrumFormat(SortProperty.dataFile));
		thf.getParserModel().setComparator(AverageMassSpectrum.class, 
				new AverageMassSpectrumComparator(SortProperty.dataFile));
		thf.getParserModel().setFormat(Range.class, 
				new RangeFormat(MRC2ToolBoxConfiguration.getRtFormat()));
		
		finalizeLayout();
		
	}

	public void setTableModelFromSpectra(
			Collection<AverageMassSpectrum>spectra) {

		thf.setTable(null);
		((AverageMassSpectraTableModel)model).setTableModelFromSpectra(spectra);
		thf.setTable(this);
		adjustColumns();
	}
	
	public AverageMassSpectrum getSelectedSpectrum() {
		
		int row = getSelectedRow();
		if(row == -1)
			return null;
		
		return (AverageMassSpectrum)model.getValueAt(
					convertRowIndexToModel(row), 
					model.getColumnIndex(AverageMassSpectraTableModel.DATA_FILE_COLUMN));
	}

	public Collection<AverageMassSpectrum> getSelectedSpectra() {
		
		Collection<AverageMassSpectrum>selected = 
				new ArrayList<AverageMassSpectrum>();
		int chromColumn = model.getColumnIndex(AverageMassSpectraTableModel.DATA_FILE_COLUMN);
		for(int i : getSelectedRows()) 
			selected.add((AverageMassSpectrum)model.getValueAt(
							convertRowIndexToModel(i), chromColumn));		
			
		return selected;
	}
	
	public void addSpectrum(AverageMassSpectrum spectrum) {
		thf.setTable(null);
		((AverageMassSpectraTableModel)model).addSpectrum(spectrum);
		thf.setTable(this);
		adjustColumns();
	}
	
	public void removeSpectrum(AverageMassSpectrum spectrum) {
		thf.setTable(null);
		((AverageMassSpectraTableModel)model).removeSpectrum(spectrum);
		thf.setTable(this);
		adjustColumns();
	}
	
	public void addSpectra(Collection<AverageMassSpectrum>spectra) {
		thf.setTable(null);
		((AverageMassSpectraTableModel)model).addSpectra(spectra);
		thf.setTable(this);
		adjustColumns();
	}
	
	public void removeSpectra(Collection<AverageMassSpectrum>spectra) {	
		thf.setTable(null);
		spectra.stream().forEach(c -> ((AverageMassSpectraTableModel)model).removeSpectrum(c));	
		thf.setTable(this);
		adjustColumns();
	}
}






























