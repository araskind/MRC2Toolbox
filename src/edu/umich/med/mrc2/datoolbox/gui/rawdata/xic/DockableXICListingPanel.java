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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.xic;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.ExtractedChromatogram;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableXICListingPanel extends DefaultSingleCDockable {

	private ChromatogramTable chromatogramTable;
	private static final Icon tableIcon = GuiUtils.getIcon("xicTable", 16);

	public DockableXICListingPanel() {

		super("DockableXICListingPanel", tableIcon, "Chromatograms", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		
		setLayout(new BorderLayout(0, 0));
		chromatogramTable = new ChromatogramTable();
		JScrollPane scroll = new JScrollPane(chromatogramTable);
		add(scroll, BorderLayout.CENTER);
	}	
	
	public void addChromatogramSelectionListener(ListSelectionListener lsl) {
		chromatogramTable.getSelectionModel().addListSelectionListener(lsl);
	}
	
	public void setTableModelFromChromatograms(
			Collection<ExtractedChromatogram>chromatograms) {
		chromatogramTable.setTableModelFromChromatograms(chromatograms);
	}

	public Collection<ExtractedChromatogram> getSelectedChromatograms() {				
		return chromatogramTable.getSelectedChromatograms();
	}
	
	public void addChromatogram(ExtractedChromatogram chromatogram) {
		chromatogramTable.addChromatogram(chromatogram);;
	}
	
	public void removeChromatogram(ExtractedChromatogram chromatogram) {
		chromatogramTable.removeChromatogram(chromatogram);
	}
	
	public void addChromatograms(Collection<ExtractedChromatogram>chromatograms) {
		chromatogramTable.addChromatograms(chromatograms);
	}
	
	public void removeChromatograms(Collection<ExtractedChromatogram>chromatograms) {		
		chromatogramTable.removeChromatograms(chromatograms);		
	}
	
	public synchronized void clearPanel() {
		chromatogramTable.clearTable();
	}

	public ChromatogramTable getTable() {
		return chromatogramTable;
	}
}
