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

package edu.umich.med.mrc2.datoolbox.gui.tables.ms;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.TandemMassSpectrum;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.RawDataUtils;
import umich.ms.datatypes.scan.IScan;

public class MsMsTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 5088486364694169431L;
	private TandemMassSpectrum currentMsms;
	private IScan currentScan;

	public MsMsTable() {

		super();
		model = new MsMsTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MsMsTableModel>((MsMsTableModel)model);
		setRowSorter(rowSorter);
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		columnModel.getColumnById(MsMsTableModel.MZ_COLUMN)
			.setCellRenderer(mzRenderer);
		columnModel.getColumnById(MsMsTableModel.INTENSITY_COLUMN)
			.setCellRenderer(new FormattedDecimalRenderer(
					MRC2ToolBoxConfiguration.getSpectrumIntensityFormat()));

		addTablePopupMenu(new MsMsTablePopupMenu(this));
		
		thf = new TableFilterHeader(this, AutoChoices.DISABLED);
		finalizeLayout();
	}
	
	@Override
	public void clearTable() {

		currentMsms = null;
		currentScan = null;
		super.clearTable();
	}

	public void setTableModelFromTandemMs(TandemMassSpectrum msms) {

		thf.setTable(null);
		((MsMsTableModel)model).setTableModelFromTandemMs(msms);
		thf.setTable(this);
		adjustColumns();	
		currentMsms = msms;
		currentScan = null;
	}

	public void setTableModelFromScan(IScan scan) {
		thf.setTable(null);
		((MsMsTableModel)model).setTableModelFromScan(scan);
		thf.setTable(this);
		adjustColumns();
		currentMsms = null;
		currentScan = scan;
	}
	
	public void setTableModelFromDataPoints(Collection<MsPoint> points, MsPoint parent) {
		thf.setTable(null);
		((MsMsTableModel)model).setTableModelFromDataPoints(points, parent);
		thf.setTable(this);
		adjustColumns();
		currentMsms = null;
		currentScan = null;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();
		
		if (command.equals(MainActionCommands.COPY_SPECTRUM_AS_TSV_COMMAND.getName()))
			copyMassListAsTSV(false);

		if (command.equals(MainActionCommands.COPY_NORMALIZED_SPECTRUM_AS_TSV_COMMAND.getName()))
			copyMassListAsTSV(true);
		
		if (command.equals(MainActionCommands.COPY_FEATURE_WITH_METADATA_COMMAND.getName()))
			copyFeatureWithMetadata();
			
		if (command.equals(MainActionCommands.COPY_SCAN_WITH_METADATA_COMMAND.getName()))
			copyScanWithMetadata();
		
		if (command.equals(MainActionCommands.COPY_SELECTED_MASSES_AS_CSV_COMMAND.getName()))
			copySelectedMassesAsCSV();
			
		super.actionPerformed(e);
	}
	
	public void copySelectedMassesAsCSV() {
		
		ArrayList<String> massList = new ArrayList<String>();
		int[] selectedRows = getSelectedRows();
		if(selectedRows.length == 0)
			return;
		
		int massColumn = model.getColumnIndex(MsOneTableModel.MZ_COLUMN);
		for(int i : selectedRows) {
			double mz = (double) model.getValueAt(convertRowIndexToModel(i), massColumn);
			massList.add(MRC2ToolBoxConfiguration.getMzFormat().format(mz));
		}
		StringSelection stringSelection = new StringSelection(StringUtils.join(massList, ","));
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}

	private void copyFeatureWithMetadata() {
		// TODO Auto-generated method stub
		if(currentMsms == null)
			return;
		else {
			MessageDialog.showInfoMsg("Feature under development.", this);
			return;
		}	
	}

	private void copyScanWithMetadata() {

		if(currentScan == null)
			return;
		else {			
			String scanWithMetadata = 
					RawDataUtils.getScanWithMetadata(currentScan);			
			StringSelection stringSelection = 
					new StringSelection(scanWithMetadata);
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(stringSelection, null);
		}
	}
	
	private void copyMassListAsTSV(boolean normalize) {
		
		int massColumn = model.getColumnIndex(MsMsTableModel.MZ_COLUMN);
		int intensityColumn = model.getColumnIndex(MsMsTableModel.INTENSITY_COLUMN);
		Collection<MsPoint>spectrum = new ArrayList<MsPoint>();
		MsPoint[] pattern = new MsPoint[0];
		for(int i=0; i<model.getRowCount(); i++) {
			double mz = (double)model.getValueAt(i, massColumn);
			double intensity = (double)model.getValueAt(i, intensityColumn);
			spectrum.add(new MsPoint(mz, intensity));
		}
		if(normalize)
			pattern = MsUtils.normalizeAndSortMsPattern(spectrum);
		else
			pattern = spectrum.stream().
					sorted(MsUtils.mzSorter).
					toArray(size -> new MsPoint[size]);		
		
		ArrayList<String> massIntensityList = new ArrayList<String>();
		if(normalize) {
			for(int i=0; i<pattern.length; i++) {
				String line = 					
						MsUtils.spectrumMzExportFormat.format(pattern[i].getMz()) + "\t" + 
						MsUtils.mspIntensityFormat.format(pattern[i].getIntensity());
				massIntensityList.add(line);
			}
		}
		else {
			for(int i=0; i<pattern.length; i++) {
				String line = 					
						MsUtils.spectrumMzExportFormat.format(pattern[i].getMz()) + "\t" + 
						MsUtils.spectrumIntensityFormat.format(pattern[i].getIntensity());
				massIntensityList.add(line);
			}		
		}	
		StringSelection stringSelection = new StringSelection(StringUtils.join(massIntensityList, "\n"));
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}
}
