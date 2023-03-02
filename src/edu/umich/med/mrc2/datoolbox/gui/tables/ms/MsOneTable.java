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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MassSpectrum;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.compare.MsDataPointComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AdductRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import edu.umich.med.mrc2.datoolbox.utils.MsUtils;
import edu.umich.med.mrc2.datoolbox.utils.RawDataUtils;
import umich.ms.datatypes.scan.IScan;

public class MsOneTable  extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 883564169882286269L;
	private MsOneTableModel model;
	private MsFeature currentFeature;
	private IScan currentScan;

	public MsOneTable() {

		super();

		model = new MsOneTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MsOneTableModel>(model);
		setRowSorter(rowSorter);
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		chmodRenderer = new AdductRenderer();
		setDefaultRenderer(Adduct.class, chmodRenderer);

		columnModel.getColumnById(MsOneTableModel.MZ_COLUMN)
			.setCellRenderer(mzRenderer);
		columnModel.getColumnById(MsOneTableModel.INTENSITY_COLUMN)
			.setCellRenderer(new FormattedDecimalRenderer(MRC2ToolBoxConfiguration.getSpectrumIntensityFormat()));

		addTablePopupMenu(new MsTablePopupMenu(this));

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromMsFeature(MsFeature feature) {

		thf.setTable(null);
		model.setTableModelFromMsFeature(feature);
		thf.setTable(this);
		currentFeature = feature;
		currentScan = null;
		tca.adjustColumns();
	}
	
	public void setTableModelFromSpectrum(MassSpectrum spectrum) {
		
		thf.setTable(null);
		model.setTableModelFromSpectrum(spectrum);
		thf.setTable(this);
		currentFeature = null;
		currentScan = null;
		tca.adjustColumns();
	}
	
	public void setTableModelFromScan(IScan scan) {
		
		thf.setTable(null);
		model.setTableModelFromScan(scan);
		thf.setTable(this);
		currentFeature = null;
		currentScan = scan;
		tca.adjustColumns();
	}

	@Override
	public void clearTable() {
		currentFeature = null;
		currentScan = null;
		super.clearTable();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		String command = e.getActionCommand();
		
		if (command.equals(MainActionCommands.COPY_SELECTED_MASSES_AS_CSV_COMMAND.getName()))
			copySelectedMassesAsCSV();

		if (command.equals(MainActionCommands.COPY_MASS_LIST_AS_CSV_COMMAND.getName()))
			copyMassListAsCSV(0);

		if (command.equals(MainActionCommands.COPY_SELECTED_ADUCT_MASS_SUBLIST_2_AS_CSV_COMMAND.getName()))
			copyMassListAsCSV(2);

		if (command.equals(MainActionCommands.COPY_SELECTED_ADUCT_MASS_SUBLIST_3_AS_CSV_COMMAND.getName()))
			copyMassListAsCSV(3);
		
		if (command.equals(MainActionCommands.COPY_SPECTRUM_AS_TSV_COMMAND.getName()))
			copyMassListAsTSV(false);

		if (command.equals(MainActionCommands.COPY_NORMALIZED_SPECTRUM_AS_TSV_COMMAND.getName()))
			copyMassListAsTSV(true);
		
		if (command.equals(MainActionCommands.COPY_FEATURE_WITH_METADATA_COMMAND.getName()))
			copyFeatureWithMetadata();
			
		if (command.equals(MainActionCommands.COPY_SCAN_WITH_METADATA_COMMAND.getName()))
			copyScanWithMetadata();
			
		super.actionPerformed(e);
	}

	private void copyFeatureWithMetadata() {
		// TODO Auto-generated method stub
		if(currentFeature == null)
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

	public void copyMassListAsCSV(int massCount) {

		ArrayList<String> massList = new ArrayList<String>();
		int massColumn = model.getColumnIndex(MsOneTableModel.MZ_COLUMN);
		if(massCount == 0) {

			for (int i = 0; i < model.getRowCount(); i++) {

				double mz = (double) model.getValueAt(i, massColumn);
				massList.add(MRC2ToolBoxConfiguration.getMzFormat().format(mz));
			}
		}
		else {
			HashSet<Adduct> visibleAdducts = getVisibleAdducts();
			for(Adduct ad : visibleAdducts) {

				MsPoint[] points = currentFeature.getSpectrum().getMsForAdduct(ad);
				Arrays.sort(points, new MsDataPointComparator(SortProperty.Intensity, SortDirection.DESC));
				for (int i = 0; i < massCount; i++)
					massList.add(MRC2ToolBoxConfiguration.getMzFormat().format(points[i].getMz()));
			}
		}
		StringSelection stringSelection = new StringSelection(StringUtils.join(massList, ","));
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}
	

	private void copyMassListAsTSV(boolean normalize) {
		
		int massColumn = model.getColumnIndex(MsOneTableModel.MZ_COLUMN);
		int intensityColumn = model.getColumnIndex(MsOneTableModel.INTENSITY_COLUMN);
		Collection<MsPoint>spectrum = new ArrayList<MsPoint>();
		MsPoint[] pattern = new MsPoint[0];
		for(int i=0; i<model.getRowCount(); i++) {
			double mz = (double)model.getValueAt(i, massColumn);
			double intensity = (double)model.getValueAt(i, intensityColumn);
			spectrum.add(new MsPoint(mz, intensity));
		}
		if(normalize)
			pattern = MsUtils.normalizeAndSortMsPatternForMsp(spectrum);
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
		StringSelection stringSelection = 
				new StringSelection(StringUtils.join(massIntensityList, "\n"));
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}

	public HashSet<Adduct>getVisibleAdducts(){

		HashSet<Adduct>visAdducts = new HashSet<Adduct>();
		int adductColumn = model.getColumnIndex(MsOneTableModel.ADDUCT_COLUMN);

		for(int i=0; i<getRowCount(); i++) {
			
			Object value = model.getValueAt(convertRowIndexToModel(i), adductColumn);
			if(value != null && Adduct.class.isAssignableFrom(value.getClass()))
				visAdducts.add((Adduct)value);
		}
		return visAdducts;
	}
}






























