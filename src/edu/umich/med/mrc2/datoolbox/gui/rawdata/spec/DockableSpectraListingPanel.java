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

package edu.umich.med.mrc2.datoolbox.gui.rawdata.spec;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionListener;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.AverageMassSpectrum;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableSpectraListingPanel extends DefaultSingleCDockable {

	private AverageMassSpectraTable spectraTable;
	private static final Icon tableIcon = GuiUtils.getIcon("specTable", 16);

	public DockableSpectraListingPanel() {

		super("DockableSpectraListingPanel", tableIcon, "Spectra", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		
		setLayout(new BorderLayout(0, 0));
		spectraTable = new AverageMassSpectraTable();
		JScrollPane scroll = new JScrollPane(spectraTable);
		add(scroll, BorderLayout.CENTER);
	}	
	
	public void addSpectrumSelectionListener(ListSelectionListener lsl) {
		spectraTable.getSelectionModel().addListSelectionListener(lsl);
	}
	
	public void setTableModelFromSpectra(
			Collection<AverageMassSpectrum>spectra) {
		spectraTable.setTableModelFromSpectra(spectra);
	}
	
	public AverageMassSpectrum getSelectedSpectrum() {
		return spectraTable.getSelectedSpectrum();
	}

	public Collection<AverageMassSpectrum> getSelectedSpectra() {				
		return spectraTable.getSelectedSpectra();
	}
	
	public void addSpectrum(AverageMassSpectrum spectrum) {
		spectraTable.addSpectrum(spectrum);
	}
	
	public void removeSpectrum(AverageMassSpectrum spectrum) {
		spectraTable.removeSpectrum(spectrum);
	}
	
	public void addSpectra(Collection<AverageMassSpectrum>spectra) {
		spectraTable.addSpectra(spectra);
	}
	
	public void removeSpectra(Collection<AverageMassSpectrum>spectra) {		
		spectraTable.removeSpectra(spectra);		
	}
	
	public synchronized void clearPanel() {
		spectraTable.clearTable();
	}

	public AverageMassSpectraTable getTable() {
		return spectraTable;
	}
}
