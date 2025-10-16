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

package edu.umich.med.mrc2.datoolbox.gui.idtlims.prep;

import java.awt.BorderLayout;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSProtocol;
import edu.umich.med.mrc2.datoolbox.gui.idtlims.sop.ProtocolTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableSopTable extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("sop", 16);
	private ProtocolTable sopTable;

	public DockableSopTable() {

		super("DockableSopTable", componentIcon, "SOP protocols", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		setLayout(new BorderLayout(0, 0));
		sopTable = new ProtocolTable();
		add(new JScrollPane(sopTable), BorderLayout.CENTER);
	}

	public void setTableModelFromProtocols(Collection<LIMSProtocol>protocols) {
		sopTable.setTableModelFromProtocols(protocols);
	}

	public LIMSProtocol getSelectedProtocol() {
		return sopTable.getSelectedProtocol();
	}

	public Collection<LIMSProtocol> getSelectedProtocols() {
		return sopTable.getSelectedProtocols();
	}


	public Collection<LIMSProtocol> getAllProtocols(){
		return sopTable.getAllProtocols();
	}

	public synchronized void clearPanel() {
		sopTable.clearTable();
	}
}
