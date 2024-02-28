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

package edu.umich.med.mrc2.datoolbox.gui.tables;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import javax.swing.AbstractButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.compare.TableColumnStateComparator;
import edu.umich.med.mrc2.datoolbox.data.lims.LIMSUser;
import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;
import edu.umich.med.mrc2.datoolbox.gui.preferences.TableLayoutManager;
import edu.umich.med.mrc2.datoolbox.gui.tables.editors.RadioButtonEditor;
import edu.umich.med.mrc2.datoolbox.gui.tables.filters.gui.TableFilterHeader;
import edu.umich.med.mrc2.datoolbox.gui.tables.pref.TableColumnState;
import edu.umich.med.mrc2.datoolbox.gui.tables.pref.TablePreferencesDialog;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.AdductRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.CompoundIdentityDatabaseLinkRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.DateTimeCellRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.FormattedDecimalRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.IntensityRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.LIMSUserRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.MsFeatureRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.PercentValueRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.RadioButtonRenderer;
import edu.umich.med.mrc2.datoolbox.gui.tables.renderers.WordWrapCellRenderer;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;
import edu.umich.med.mrc2.datoolbox.main.config.MRC2ToolBoxConfiguration;

public class BasicTable extends JTable implements ActionListener{

	/**
	 *
	 */
	private static final long serialVersionUID = 7740663380311671468L;

	public static final Color ALTERNATE_ROW_COLOR = Color.decode("#E5FFCC");
	public static final Color WHITE_COLOR = Color.WHITE;

	protected MsFeatureRenderer cfRenderer;
	protected AdductRenderer chmodRenderer;
	protected PercentValueRenderer percentRenderer;
	protected DateTimeCellRenderer dtRenderer;
	protected WordWrapCellRenderer longTextRenderer;
	protected FormattedDecimalRenderer areaRenderer, mzRenderer, rtRenderer, ppmRenderer;
	protected IntensityRenderer intensityRenderer;
	protected CompoundIdentityDatabaseLinkRenderer msfIdRenderer;
	protected RadioButtonRenderer radioRenderer;
	protected RadioButtonEditor radioEditor;
	protected LIMSUserRenderer userRenderer;
	
	protected JPopupMenu tablePopupMenu;
	protected ColumnSelectorPopup columnSelectorPopupMenu;
	protected int popupRow;
	protected int popupCol;

	protected BasicTableModel model;
	protected XTableColumnModel columnModel;
	protected TableColumnAdjuster tca;
	protected Set<Integer>fixedWidthColumns;
	protected Set<String>userColumns;
	protected TableFilterHeader thf;
	protected TablePreferencesDialog tablePreferencesDialog;
	protected TableRowSorter<? extends TableModel> rowSorter;
	protected TableLayoutListener tll;

	/**
	 *
	 */
	public BasicTable() {

		super();
		columnModel  = new XTableColumnModel();
		setColumnModel(columnModel);
		initTable();
	}

	public BasicTable(BasicTableModel tableModel) {

		columnModel  = new XTableColumnModel();
		setColumnModel(columnModel);
		setModel(tableModel);
		initTable();
	}

//	/**
//	 * @param rowData
//	 * @param columnNames
//	 */
//	public BasicTable(Object[][] rowData, Object[] columnNames) {
//
//		columnModel  = new XTableColumnModel();
//		setColumnModel(columnModel);
//		BasicTableModel tableModel = new BasicTableModel(rowData, columnNames);
//		setModel(tableModel);
//		fixedWidthColumns = new ArrayList<Integer>();
//		initTable();
//	}

	public BasicTable(TableModel model) {

		columnModel  = new XTableColumnModel();
		setColumnModel(columnModel);
		setModel(model);
		initTable();
	}
	
	public void adjustColumns() {

		if(fixedWidthColumns.isEmpty()) {
			
			try {
				tca.adjustColumnsExcluding(fixedWidthColumns);
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
		else {
			try {
				tca.adjustColumns();
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
	}
	
	public void clearTable() {
		
		if(getModel().getRowCount() == 0)
			return;

		((BasicTableModel) this.getModel()).setRowCount(0);

		if (tca != null)
			tca.adjustColumns();
	}

	public int getColumnIndex(String columnTitle) {

		int columnCount = this.getColumnCount();

		for (int column = 0; column < columnCount; column++) {

			if (this.getColumnName(column).equalsIgnoreCase(columnTitle))
				return column;
		}
		return -1;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {

		return getRowCount() == 0 ? super.getScrollableTracksViewportWidth()
				: getPreferredSize().width < getParent().getWidth();
	}

	public void initPreferenceBasedRenderers() {

		mzRenderer = new FormattedDecimalRenderer(MRC2ToolBoxConfiguration.getMzFormat(), true);
		rtRenderer = new FormattedDecimalRenderer(MRC2ToolBoxConfiguration.getRtFormat(), true);
		areaRenderer = new FormattedDecimalRenderer(MRC2ToolBoxConfiguration.getIntensityFormat(), true);
		ppmRenderer = new FormattedDecimalRenderer(MRC2ToolBoxConfiguration.getPpmFormat(), true);
	}

	private void initTable() {

		setBackground(WHITE_COLOR);
		setAlignmentY(Component.TOP_ALIGNMENT);
		setAlignmentX(Component.LEFT_ALIGNMENT);
		setAutoCreateRowSorter(true);

		setShowGrid(false);
		setRowMargin(2);
		setIntercellSpacing(new Dimension(2, 2));
		setRowHeight(25);

		setFillsViewportHeight(true);
		// setPreferredScrollableViewportSize(getPreferredSize());

		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		initPreferenceBasedRenderers();		
		
		fixedWidthColumns = new TreeSet<Integer>();
		userColumns = new TreeSet<String>();
	}
	
	public void finalizeLayout() {
		
		tca = new TableColumnAdjuster(this);
		try {
			loadSavedLayout();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//	e.printStackTrace();
		}
		tll = new TableLayoutListener(this);
		adjustColumns();
		addColumnSelectorPopup();
	}

	public boolean isRowSelected(int row) {

		int[] selectedRows = getSelectedRows();

		for (int i = 0; i < selectedRows.length; i++) {

			if (selectedRows[i] == row)
				return true;
		}
		return false;
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {

		Component returnComp = null;
		try {
			returnComp = super.prepareRenderer(renderer, row, column);
		} catch (Exception e) {
			//e.printStackTrace();
		}
		if (returnComp != null) {

			if (isRowSelected(row) && !getColumnSelectionAllowed()) {

				returnComp.setBackground(getSelectionBackground());
			} else if (row == getSelectedRow() && column == getSelectedColumn()) {

				returnComp.setBackground(getSelectionBackground());
			} else {

				Color bg = (row % 2 == 0 ? ALTERNATE_ROW_COLOR : WHITE_COLOR);
				returnComp.setBackground(bg);
				bg = null;
			}
		}
		return returnComp;
	}

	public void showAllColumns() {

		columnModel.setAllColumnsVisible();

		if(columnSelectorPopupMenu != null)
			columnSelectorPopupMenu.enableAllColumnItems();
		
		TableLayoutManager.setTableLayout(this);
	}
	
	public void resetFilter() {

		if(thf != null)
			thf.resetFilter();

		scrollToSelected();
	}

	public void scrollToSelectedOld() {

	  	JViewport viewport = (JViewport) this.getParent();

	  	if(viewport != null) {

		    Rectangle cellRectangle = this.getCellRect(this.getSelectedRow(), 0, true);
		    Rectangle visibleRectangle = viewport.getVisibleRect();

		    SwingUtilities.invokeLater(() -> this.scrollRectToVisible(
		    		new Rectangle(cellRectangle.x, cellRectangle.y, (int) visibleRectangle.getWidth(), (int) visibleRectangle.getHeight())));
	  	}
	}
	
	public void scrollToSelected() {

		JViewport viewport = (JViewport) this.getParent();
		if(viewport != null) {
			
			Rectangle cellRectangle = this.getCellRect(this.getSelectedRow(), this.getSelectedColumn(), true);
			
//			int extentHeight = viewport.getExtentSize().height;
//			int viewHeight = viewport.getViewSize().height;
//			int y = Math.max(0, cellRectangle.y - ((extentHeight - cellRectangle.height) / 2));
//			int yy = Math.min(y, viewHeight - extentHeight);
//			SwingUtilities.invokeLater(() -> viewport.setViewPosition(new Point(0, yy - cellRectangle.height)));
			
			SwingUtilities.invokeLater(() -> this.scrollRectToVisible(cellRectangle));
		}
	}

	public void addTablePopupMenu(JPopupMenu popup) {

		tablePopupMenu = popup;
		setComponentPopupMenu(tablePopupMenu);

		addMouseListener(new MouseAdapter() {
			

			@Override
			public void mousePressed(MouseEvent e) {
				popupRow = rowAtPoint(e.getPoint());
				popupCol = columnAtPoint(e.getPoint());
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				popupRow = rowAtPoint(e.getPoint());
				popupCol = columnAtPoint(e.getPoint());
			}
		});
	}

	public void setTablePopupEnabled(boolean enabled) {

		if(enabled)
			setComponentPopupMenu(tablePopupMenu);
		else
			setComponentPopupMenu(null);
	}

	public void addColumnSelectorPopup() {

		columnSelectorPopupMenu = new ColumnSelectorPopup(this, columnModel);
		getTableHeader().setComponentPopupMenu(columnSelectorPopupMenu);
	}

	public int getPopupRow() {
		return popupRow;
	}
	
	public int getPopupCol() {
		return popupCol;
	}

	@Override
    public void columnMoved(TableColumnModelEvent e) {

		super.columnMoved(e);
		addColumnSelectorPopup();
    }

	public void actionPerformed(ActionEvent event) {
		
		String command = event.getActionCommand();
		if(command.equals(MainActionCommands.SHOW_ALL_TABLE_COLUMNS_COMMAND.getName())) {

			showAllColumns();
			adjustColumns();
			return;
		}
		if(command.equals(MainActionCommands.ADJUST_ALL_TABLE_COLUMNS_COMMAND.getName())) {

			adjustColumns();
			return;
		}
		if(command.equals(MainActionCommands.RESET_COLUMN_FILTERS_COMMAND.getName())) {

			resetFilter();
			adjustColumns();
			return;
		}
		if(command.equals(MainActionCommands.SHOW_TABLE_PREFERENCES_COMMAND.getName())) {
			showTablePreferencesDialog();
			return;
		}
		if(command.equals(MainActionCommands.APPLY_TABLE_PREFERENCES_COMMAND.getName())) {
			applyTableLayoutFromDialog();
			return;
		}
		if(((Component) event.getSource()).getParent().equals(columnSelectorPopupMenu))
			toggleColumnVisibility(event);
	
		if(command.equals(MainActionCommands.COPY_TABLE_DATA_COMMAND.getName()) 
				|| command.equals(MainActionCommands.COPY_VISIBLE_TABLE_ROWS_COMMAND.getName()))
			copyVisibleTableRowsToClipboard();
		
		if(command.equals(MainActionCommands.COPY_TABLE_SELECTED_ROWS_DATA_COMMAND.getName())
				|| command.equals(MainActionCommands.COPY_SELECTED_TABLE_ROWS_COMMAND.getName()))
			copySelectedRowsToClipboard();
		
		if(command.equals(MainActionCommands.COPY_SELECTED_VALUE_COMMAND.getName())) 
			copySelectedValueToClipboard();	
	}

	public String getTableDataAsString() {
		int [] rows = IntStream.range(0, getRowCount()).toArray();
		return getTableDataAsString(rows);
	}
	
	public String getTableDataAsString(int[] rows) {
		
		if(rows.length == 0)
			return "";
		
		StringBuffer tableData = new StringBuffer();
		int numCols = getColumnCount();

		tableData.append(getColumnName(0));

		for(int i=1; i<numCols; i++)
			tableData.append("\t" + getColumnName(i));

		tableData.append("\n");

		for(int i : rows){

			for(int j=0; j<numCols; j++){
				
				tableData.append(getCellStringValue(i,j));

                if(j<numCols-1)
                	tableData.append("\t");
                else
                	tableData.append("\n");
			}
		}
		return tableData.toString();
	}
	
	private String getCellStringValue(int row, int col) {
		
        final TableCellRenderer renderer = getCellRenderer(row, col);
        final Component comp = prepareRenderer(renderer, row, col);
        String txt = null;
        if(comp == null) {
        	return "";
        }
        else {
            if(JLabel.class.isAssignableFrom(comp.getClass())) {  
            	
            	txt = ((JLabel) comp).getText();
            	if(txt == null || txt.isEmpty())
            		txt = ((JLabel) comp).getToolTipText();
            	
            	if(txt == null)
            		txt = "";
            }
            else if (JTextPane.class.isAssignableFrom(comp.getClass()))
            	txt = ((JTextPane) comp).getText();
            
            else if (JTextField.class.isAssignableFrom(comp.getClass()))
            	txt = ((JTextField) comp).getText();
            
            else if (JTextArea.class.isAssignableFrom(comp.getClass()))
            	txt = ((JTextArea) comp).getText();
            
            else if(JCheckBox.class.isAssignableFrom(comp.getClass()))
            	txt = Boolean.toString(((JCheckBox) comp).isSelected());
            
            else if(JRadioButton.class.isAssignableFrom(comp.getClass()))
            	txt = Boolean.toString(((JRadioButton) comp).isSelected());
            
            else {
            	Object o = getValueAt(row, col);
            	if(o != null)
            		txt = o.toString();
            }
        }
        if(txt != null) {
        	try {
        		//	Strip HTML code
				txt = txt.trim().replaceAll("<[^>]*>", ""); 
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        return txt;
	}
	
	public void copyVisibleTableRowsToClipboard() {

		String dataString = getTableDataAsString();		
		if(dataString == null)
			dataString = "";

		StringSelection stringSelection = new StringSelection(dataString);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}
		
	public void copySelectedRowsToClipboard() {
		
		int [] rows = getSelectedRows();
		String dataString = getTableDataAsString(rows);
		if(dataString == null)
			dataString = "";

		StringSelection stringSelection = new StringSelection(dataString);
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}
	
	private void copySelectedValueToClipboard() {
		
		if(popupRow == -1 || popupCol == -1)
			return;

		StringSelection stringSelection = 
				new StringSelection(getCellStringValue(popupRow, popupCol));
		Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
	}
	
	private void showTablePreferencesDialog() {

		tablePreferencesDialog = new TablePreferencesDialog(this);
		tablePreferencesDialog.setLocationRelativeTo(MRC2ToolBoxCore.getMainWindow());
		tablePreferencesDialog.setVisible(true);
	}
	
	protected void applyTableLayoutFromDialog() {

		TableColumnState[] columnSorters = tablePreferencesDialog.getColumnSorters();
		TableRowSorter<? extends TableModel> sorter = (TableRowSorter<? extends TableModel>) getRowSorter();
		List<RowSorter.SortKey> sortKeys = new ArrayList<>();
		for(TableColumnState state : columnSorters) {
			
			if(state.isVisible() && !state.getSortOrder().equals(SortOrder.UNSORTED)) {
				
				int colIndex = ((BasicTableModel)getModel()).getColumnIndex(state.getColumnName());
				sortKeys.add(new RowSorter.SortKey(colIndex, state.getSortOrder()));
			}
		}
		sorter.setSortKeys(sortKeys);
		sorter.sort();

		//	Set visible/hidden columns
		TableColumnState[] newSettings = tablePreferencesDialog.getColumnSettings();
		for(TableColumnState state : newSettings) {
			
			TableColumn column = columnModel.getColumnById(state.getColumnName());
			if(columnModel.isColumnVisible(column) != state.isVisible())
				columnModel.setColumnVisible(column, state.isVisible());
		}
		TableLayoutManager.setTableLayout(this);
		tablePreferencesDialog.dispose();
	}
	
	public void loadSavedLayout() {
		
		TableColumnState[] layout = TableLayoutManager.getTableLayout(this);
		if(layout == null || layout.length == 0)
			return;
		
		if(getModel() instanceof BasicTableModel) {
			
			//	Get sorters TableColumnStateComparator
			TableColumnState[] columnSorters = Arrays.asList(layout).stream().
					filter(t -> !t.getSortOrder().equals(SortOrder.UNSORTED)).
					toArray(size -> new TableColumnState[size]);
			
			if(columnSorters.length > 0)
				Arrays.sort(columnSorters, new TableColumnStateComparator(SortProperty.ID));
			
			TableRowSorter<? extends TableModel> sorter = (TableRowSorter<? extends TableModel>) getRowSorter();
			List<RowSorter.SortKey> sortKeys = new ArrayList<>();
			for(TableColumnState state : columnSorters) {
				
				if(state.isVisible() && !state.getSortOrder().equals(SortOrder.UNSORTED)) {
					
					int colIndex = ((BasicTableModel)getModel()).getColumnIndex(state.getColumnName());
					sortKeys.add(new RowSorter.SortKey(colIndex, state.getSortOrder()));
				}
			}
			sorter.setSortKeys(sortKeys);
			sorter.sort();
			
			//	Set visible/hidden columns
			for(TableColumnState state : layout) {
				
				TableColumn column = null;
				try {
					column = columnModel.getColumnById(state.getColumnName());
				} catch (Exception e) {
					// TODO Auto-generated catch block
				}
				if(column != null && columnModel.isColumnVisible(column) != state.isVisible())
					columnModel.setColumnVisible(column, state.isVisible());
			}
			//	Reorder columns
			for(int i=0; i<layout.length; i++) {
				
				TableColumn column = null;
				try {
					column = columnModel.getColumnById(layout[i].getColumnName());
				} catch (Exception e) {
					// TODO Auto-generated catch block
				}
				if(column != null && columnModel.isColumnVisible(column)) {
					int currentIndex = getColumnIndex(layout[i].getColumnName());
					if(currentIndex != i)
						columnModel.moveColumn(currentIndex, i);
				}		
			}
		}	
	}
	
	public TableColumnState[] getTableLayout() {

		TableColumnState[]columnSettings = new TableColumnState[0];
		if(getModel() instanceof BasicTableModel) {
			
			BasicTableModel model = (BasicTableModel)getModel();
			columnSettings = new TableColumnState[model.getColumnArray().length];
			ArrayList<TableColumnState>hiddenColumns = new ArrayList<TableColumnState>();
			List<? extends SortKey> sortKeys = getRowSorter().getSortKeys();
			int addedCount = 0;
			for(ColumnContext cc : model.getColumnArray()) {
				
				int modelIndex = model.getColumnIndex(cc.columnName);
				int viewIndex = getColumnIndex(cc.columnName);
				TableColumnState columnState = new TableColumnState(cc.columnName);
				for(int i=0; i<sortKeys.size(); i++) {
					
					if(sortKeys.get(i).getColumn() == modelIndex) {
						
						columnState.setSortOrder(sortKeys.get(i).getSortOrder());
						columnState.setSorterPosition(i);
					}
				}
//				SortKey columnSortKey = sortKeys.stream().
//						filter(k -> k.getColumn() == modelIndex).
//						findFirst().orElse(null);
//				if(columnSortKey != null)
//					columnState.setSortOrder(columnSortKey.getSortOrder());
				
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

		}
		return columnSettings;
	}

	private void toggleColumnVisibility(ActionEvent event) {
		
		String columnName = event.getActionCommand();
		getTableHeader().setDraggedColumn(null);
		AbstractButton aButton = (AbstractButton) event.getSource();
		boolean visible = aButton.getModel().isSelected();
		TableColumn column = columnModel.getColumnById(columnName);
		columnModel.setColumnVisible(column, visible);
		TableLayoutManager.setTableLayout(this);
	}
	
	public TableRowSorter<? extends TableModel>getRowSorter(){
		
		if(rowSorter == null)
			return (TableRowSorter<? extends TableModel>) super.getRowSorter();
		else
			return rowSorter;
	}
	
	@Override
    protected JTableHeader createDefaultTableHeader() {
		
        return new JTableHeader(columnModel) {

			private static final long serialVersionUID = 1L;
			public String getToolTipText(MouseEvent e) {
                String tip = null;
                //	TODO
                java.awt.Point p = e.getPoint();
                int index = columnModel.getColumnIndexAtX(p.x);
                if(model != null) {
                	
                    int realIndex = columnModel.getColumn(index).getModelIndex();                   
                    return model.getColumnTooltip(realIndex);
                }
                else {
                	return columnModel.getColumn(index).getHeaderValue().toString();
                }
            }
        };
    }
	
	@Override
	public void setEnabled(boolean b) {
		
		if(thf != null)
			thf.setEnabled(b);
		
		for(int i= 0; i<getModel().getColumnCount(); i++) {
			
			TableCellRenderer renderer = columnModel.getColumn(i).getCellRenderer();
			for(int j=0; j<getModel().getRowCount(); j++) {
				if (renderer != null)
					prepareRenderer(renderer, i, j).setEnabled(b);
			}						
		}			
		super.setEnabled(b);
	}
	
	protected void createInteractiveUserRenderer(Collection<String>userColumnIdentifiers) { 
		
		userRenderer = new LIMSUserRenderer();
		setDefaultRenderer(LIMSUser.class, userRenderer);
		userColumns.clear();
		userColumns.addAll(userColumnIdentifiers);
		MouseMotionAdapter mma = new MouseMotionAdapter() {

			public void mouseMoved(MouseEvent e) {

				Point p = e.getPoint();
				Object columnId = columnModel.getColumn(columnAtPoint(p)).getIdentifier();
				if(userColumns.contains(columnId))
					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				else
					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		};
		addMouseMotionListener(mma);
		addMouseListener(userRenderer);
		addMouseMotionListener(userRenderer);
	}
	
	protected void setExactColumnWidth(String columnIdentifier, int width) {
		
		TableColumn column = columnModel.getColumnById(columnIdentifier);
		if(column != null) {
			column.setPreferredWidth(width);
			column.setMaxWidth(width);
			column.setMinWidth(width);
			//column.setResizable(false);
		}
	}
}

















