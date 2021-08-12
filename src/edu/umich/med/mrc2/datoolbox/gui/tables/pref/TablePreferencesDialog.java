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

package edu.umich.med.mrc2.datoolbox.gui.tables.pref;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.compare.TableColumnStateComparator;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTableModel;
import edu.umich.med.mrc2.datoolbox.gui.tables.ColumnContext;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.gui.utils.MessageDialog;

public class TablePreferencesDialog extends JDialog implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4162998811528600004L;
	private static final Icon tablePrefsIcon = GuiUtils.getIcon("processingMethod", 32);
	
	private BasicTable parentTable;
	private TablePreferencesTable tablePreferencesTable;
	private SorterTable sorterTable;

	public TablePreferencesDialog(BasicTable table) {
		super();
		this.parentTable = table;
		setTitle("Set column visibility and sorting order");
		setIconImage(((ImageIcon) tablePrefsIcon).getImage());
		setPreferredSize(new Dimension(400, 600));
		setSize(new Dimension(400, 600));
		setModalityType(ModalityType.APPLICATION_MODAL);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JPanel tablePanel = new JPanel();
		getContentPane().add(tablePanel, BorderLayout.CENTER);
				
		GridBagLayout gbl_tablePanel = new GridBagLayout();
		gbl_tablePanel.columnWidths = new int[]{0, 0};
		gbl_tablePanel.rowHeights = new int[]{0, 0, 0};
		gbl_tablePanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_tablePanel.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		tablePanel.setLayout(gbl_tablePanel);
		
		JPanel visPanel = new JPanel(new BorderLayout(0,0));
		visPanel.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10), 
				new TitledBorder(
						new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
							new Color(160, 160, 160)), "Select visible columns", 
						TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 12), new Color(0, 0, 0))));
		tablePreferencesTable = new TablePreferencesTable();
		tablePreferencesTable.addTablePopupMenu(new TablePreferencesPopupMenu(this));
		JScrollPane scrollPane = new JScrollPane(tablePreferencesTable);
		visPanel.add(scrollPane, BorderLayout.CENTER);
	
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 0;
		tablePanel.add(visPanel, gbc_panel_1);
		
		JPanel sortPanel = new JPanel(new BorderLayout(0,0));
		sortPanel.setBorder(new CompoundBorder(new EmptyBorder(10, 10, 10, 10), 
				new TitledBorder(
						new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), 
							new Color(160, 160, 160)), "Set sorting direction and priority", 
						TitledBorder.LEADING, TitledBorder.TOP, new Font("Tahoma", Font.BOLD, 12), new Color(0, 0, 0))));
		sorterTable = new SorterTable();
		sorterTable.addTablePopupMenu(new SorterTablePopupMenu(this));		
		JScrollPane scrollPane2 = new JScrollPane(sorterTable);
		sortPanel.add(scrollPane2, BorderLayout.CENTER);
		
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 1;
		tablePanel.add(sortPanel, gbc_panel_2);
		
		JPanel panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		getContentPane().add(panel, BorderLayout.SOUTH);

		JButton btnCancel = new JButton("Cancel");
		panel.add(btnCancel);
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		btnCancel.addActionListener(al);

		JButton btnSave = new JButton("Apply");
		btnSave.setActionCommand(MainActionCommands.APPLY_TABLE_PREFERENCES_COMMAND.getName());
		btnSave.addActionListener(parentTable);
		panel.add(btnSave);
		JRootPane rootPane = SwingUtilities.getRootPane(btnSave);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		rootPane.setDefaultButton(btnSave);
		
		loadParentTableLayout();
		pack();
	}

	private void loadParentTableLayout() {
		
		BasicTableModel model = (BasicTableModel)parentTable.getModel();
		TableColumnState[]columnSettings = new TableColumnState[model.getColumnArray().length];

		ArrayList<TableColumnState>hiddenColumns = new ArrayList<TableColumnState>();
		ArrayList<TableColumnState>sortedColumns = new ArrayList<TableColumnState>();
		List<? extends SortKey> sortKeys = parentTable.getRowSorter().getSortKeys();
		int addedCount = 0;
		for(ColumnContext cc : model.getColumnArray()) {
			
			int modelIndex = model.getColumnIndex(cc.columnName);
			int viewIndex = parentTable.getColumnIndex(cc.columnName);
			TableColumnState columnState = new TableColumnState(cc.columnName);
			SortKey columnSortKey = sortKeys.stream().
					filter(k -> k.getColumn() == modelIndex).
					findFirst().orElse(null);
			if(columnSortKey != null)
				columnState.setSortOrder(columnSortKey.getSortOrder());
			
			if(!columnState.getSortOrder().equals(SortOrder.UNSORTED))
				sortedColumns.add(columnState);
			
			if(viewIndex == -1) {
				columnState.setVisible(false);
				hiddenColumns.add(columnState);
			}
			else {
				columnSettings[viewIndex] = columnState;	
				addedCount++;
			}
		}
		//	Add hidden columns
		if(hiddenColumns.size() > 0) {
			
			for(int i=0; i<hiddenColumns.size(); i++)
				columnSettings[addedCount + i] = hiddenColumns.get(i);		
		}
		if(sortedColumns.size() > 0) {
			TableColumnState[] sorters = sortedColumns.stream().sorted(new TableColumnStateComparator(SortProperty.ID)).
				toArray(size -> new TableColumnState[size]);
			sorterTable.setTableModelFromColumns(sorters);
		}
		tablePreferencesTable.setTableModelFromColumns(columnSettings);	
	}
	
	public TableColumnState[] getColumnSettings() {
		return tablePreferencesTable.getColumnSettings();
	}
	
	public TableColumnState[] getColumnSorters(){
		return sorterTable.getColumnSettings();
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String command = e.getActionCommand();
		if(command.equals(MainActionCommands.ADD_COLUMN_TO_SORTER_COMMAND.getName()))
			addSelectedColumnToSorter();
		
		if(command.equals(MainActionCommands.REMOVE_COLUMN_FROM_SORTER_COMMAND.getName()))
			sorterTable.removeSelectedSorter();
		
		if(command.equals(MainActionCommands.INCREASE_SORTER_PRIORITY_COMMAND.getName()))
			sorterTable.moveSelectedSorterUp();
		
		if(command.equals(MainActionCommands.DECREASE_SORTER_PRIORITY_COMMAND.getName()))
			sorterTable.moveSelectedSorterDown();		
	}
	
	private void addSelectedColumnToSorter() {
		
		TableColumnState columnState = tablePreferencesTable.getSelectedColumnSettings();
		if(columnState == null)
			return;

		if(!columnState.isVisible()) {
			MessageDialog.showErrorMsg("You can not add hidden column to sorter!", this);
			return;
		}
		sorterTable.addColumnToSorter(columnState);
	}
}



















