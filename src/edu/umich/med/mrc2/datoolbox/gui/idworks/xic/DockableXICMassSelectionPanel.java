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

package edu.umich.med.mrc2.datoolbox.gui.idworks.xic;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JScrollPane;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.MsPoint;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableXICMassSelectionPanel extends DefaultSingleCDockable {

	private MassSelectionTable massSelectionTable;
	private static final Icon tableIcon = GuiUtils.getIcon("table", 16);

	public DockableXICMassSelectionPanel(ActionListener listener) {

		super("DockableXICMassSelectionPanel", tableIcon, "Select masses for extraction", null, Permissions.MIN_MAX_STACK);
		setCloseable(false);
		
		setLayout(new BorderLayout(0, 0));
		massSelectionTable = new MassSelectionTable();
		JScrollPane scroll = new JScrollPane(massSelectionTable);
		add(scroll, BorderLayout.CENTER);
		
		JButton addMassesButton = new JButton(MainActionCommands.ADD_MASSES_TO_EXTRACT_XIC_COMMAND.getName());
		addMassesButton.setActionCommand(MainActionCommands.ADD_MASSES_TO_EXTRACT_XIC_COMMAND.getName());
		addMassesButton.addActionListener(listener);
		add(addMassesButton, BorderLayout.SOUTH);
	}	
	
	public void setTableModelFromDataPoints(Collection<MsPoint>points) {
		massSelectionTable.setTableModelFromDataPoints(points);
	}
	
	public Collection<Double>getSelectedMasses(){
		return massSelectionTable.getSelectedMasses();
	}
	
	public void selectMass(double mass) {		
		massSelectionTable.selectMass(mass);
	}
}
