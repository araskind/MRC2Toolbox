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
import java.util.HashSet;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.StringUtils;

import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.data.MsFeature;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.data.compare.MsDataPointComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortDirection;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.coderazzi.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.ChemicalModificationRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;
import umich.ms.datatypes.scan.IScan;

public class MsOneTable  extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = 883564169882286269L;
	private MsOneTableModel model;
	private MsFeature currentFeature;

	public MsOneTable() {

		super();

		model = new MsOneTableModel();
		setModel(model);
		rowSorter = new TableRowSorter<MsOneTableModel>(model);
		setRowSorter(rowSorter);
		
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		chmodRenderer = new ChemicalModificationRenderer();
		setDefaultRenderer(Adduct.class, chmodRenderer);

		columnModel.getColumnById(MsOneTableModel.MZ_COLUMN)
			.setCellRenderer(mzRenderer);
		columnModel.getColumnById(MsOneTableModel.INTENSITY_COLUMN)
			.setCellRenderer(new FormattedDecimalRenderer(MRC2ToolBoxConfiguration.getSpectrumIntensityFormat()));

		addTablePopupMenu(new MsTablePopupMenu(this));

		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		finalizeLayout();
	}

	public void setTableModelFromSpectrum(MsFeature feature) {

		thf.setTable(null);
		model.setTableModelFromSpectrum(feature);
		thf.setTable(this);
		currentFeature = feature;
		tca.adjustColumns();
	}
	
	public void setTableModelFromScan(IScan scan) {
		
		thf.setTable(null);
		model.setTableModelFromScan(scan);
		thf.setTable(this);
		currentFeature = null;
		tca.adjustColumns();
	}	

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals(MainActionCommands.COPY_MASS_LIST_AS_CSV_COMMAND.getName()))
			copyMassListAsCSV(0);

		if (e.getActionCommand().equals(MainActionCommands.COPY_SELECTED_ADUCT_MASS_SUBLIST_2_AS_CSV_COMMAND.getName()))
			copyMassListAsCSV(2);

		if (e.getActionCommand().equals(MainActionCommands.COPY_SELECTED_ADUCT_MASS_SUBLIST_3_AS_CSV_COMMAND.getName()))
			copyMassListAsCSV(3);

		super.actionPerformed(e);
	}

	@Override
	public synchronized void clearTable() {

		super.clearTable();
		currentFeature = null;
	}

	public void copyMassListAsCSV(int massCount) {

		ArrayList<String> massList = new ArrayList<String>();
		int massColumn = model.getColumnIndex(MsOneTableModel.MZ_COLUMN);
		if(massCount == 0) {

			for (int i = 0; i < model.getRowCount(); i++) {

				double mz = (double) getValueAt(i, massColumn);
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

	public HashSet<Adduct>getVisibleAdducts(){

		HashSet<Adduct>visAdducts = new HashSet<Adduct>();
		int adductColumn = model.getColumnIndex(MsOneTableModel.ADDUCT_COLUMN);

		for(int i=0; i<getRowCount(); i++)
			visAdducts.add((Adduct) model.getValueAt(convertRowIndexToModel(i), adductColumn));

		return visAdducts;
	}
}






























