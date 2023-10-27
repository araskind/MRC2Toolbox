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

package edu.umich.med.mrc2.datoolbox.gui.cpdcol.mplex;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.text.DecimalFormat;
import java.util.Collection;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.compare.CompoundIdentityComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.MobilePhaseComparator;
import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixture;
import edu.umich.med.mrc2.datoolbox.data.cpdcoll.CompoundMultiplexMixtureComponent;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;
import edu.umich.med.mrc2.datoolbox.data.format.CompoundIdentityFormat;
import edu.umich.med.mrc2.datoolbox.data.format.MobilePhaseFormat;
import edu.umich.med.mrc2.datoolbox.data.lims.MobilePhase;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.AutoChoices;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.BooleanColorCircleFlagRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.CompoundIdentityDatabaseLinkRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MobilePhaseRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;

public class CompoundMultiplexComponentsListingTable extends BasicTable {

	/**
	 *
	 */
	private static final long serialVersionUID = -3817580957098149548L;

	public CompoundMultiplexComponentsListingTable() {

		super();

		model = new CompoundMultiplexComponentsListingTableModel();
		setModel(model);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		rowSorter = 
				new TableRowSorter<CompoundMultiplexComponentsListingTableModel>(
						(CompoundMultiplexComponentsListingTableModel)model);
		setRowSorter(rowSorter);
		rowSorter.setComparator(
				model.getColumnIndex(CompoundMultiplexComponentsListingTableModel.SOLVENT_COLUMN),
				new MobilePhaseComparator(SortProperty.Name));
		rowSorter.setComparator(
				model.getColumnIndex(CompoundMultiplexComponentsListingTableModel.ACCESSION_COLUMN),
				new CompoundIdentityComparator(SortProperty.ID));
		
		columnModel.getColumnById(CompoundMultiplexComponentsListingTableModel.CONFLICT_COLUMN)
			.setCellRenderer(new RadioButtonRenderer());
		columnModel.getColumnById(CompoundMultiplexComponentsListingTableModel.FORMULAS_DELTA_MASS_COLUMN)
			.setCellRenderer(new FormattedDecimalRenderer(new DecimalFormat("###.######"), true));
		
		columnModel.getColumnById(CompoundMultiplexComponentsListingTableModel.IS_MS_READY_COLUMN)
			.setCellRenderer(new BooleanColorCircleFlagRenderer(15));
		
		
		setDefaultRenderer(MobilePhase.class, 
				new MobilePhaseRenderer(SortProperty.Name));
		msfIdRenderer = new CompoundIdentityDatabaseLinkRenderer();
		setDefaultRenderer(CompoundIdentity.class, msfIdRenderer);
		
		//	Database link adapter
		MouseMotionAdapter mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();

				if(columnModel.isColumnVisible(
						columnModel.getColumnById(CompoundMultiplexComponentsListingTableModel.ACCESSION_COLUMN))) {

					if (columnAtPoint(p) == 
							columnModel.getColumnIndex(CompoundMultiplexComponentsListingTableModel.ACCESSION_COLUMN))
						setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					else
						setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
				else
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		};
		addMouseMotionListener(mma);
		addMouseListener(msfIdRenderer);
		addMouseMotionListener(msfIdRenderer);
		
		thf = new TableFilterHeader(this, AutoChoices.ENABLED);
		thf.getParserModel().setFormat(
				MobilePhase.class, new MobilePhaseFormat(SortProperty.Name));
		thf.getParserModel().setComparator(
				MobilePhase.class, new MobilePhaseComparator(SortProperty.Name));		
		thf.getParserModel().setFormat(
				CompoundIdentity.class, 
				new CompoundIdentityFormat(CompoundIdentityField.DB_ID));
		thf.getParserModel().setComparator(
				CompoundIdentity.class, 
				new CompoundIdentityComparator(SortProperty.ID));
		
		finalizeLayout();
	}

	public void setTableModelFromCompoundMultiplexMixtureComponents(
			Collection<CompoundMultiplexMixtureComponent>components)   {

//		thf.setTable(null);
		((CompoundMultiplexComponentsListingTableModel)model).
				setTableModelFromCompoundMultiplexMixtureComponents(components);
//		thf.setTable(this);
		adjustColumns();
	}
	
	public void setTableModelFromCompoundMultiplexMixtures(
			Collection<CompoundMultiplexMixture>mixtures)   {

//		thf.setTable(null);
		((CompoundMultiplexComponentsListingTableModel)model).
				setTableModelFromCompoundMultiplexMixtures(mixtures);
//		thf.setTable(this);
		adjustColumns();
	}

	public CompoundMultiplexMixtureComponent getSelectedCompoundMultiplexMixtureComponent() {

		int row = getSelectedRow();
		if(row == -1)
			return null;

		return (CompoundMultiplexMixtureComponent) model.getValueAt(convertRowIndexToModel(row), 
				model.getColumnIndex(CompoundMultiplexComponentsListingTableModel.NAME_COLUMN));
	}

	public void updateComponentData(CompoundMultiplexMixtureComponent mmComponent) {
		((CompoundMultiplexComponentsListingTableModel)model).updateComponentData(mmComponent);
	}
}



















