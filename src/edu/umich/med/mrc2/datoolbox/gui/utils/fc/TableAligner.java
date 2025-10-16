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

package edu.umich.med.mrc2.datoolbox.gui.utils.fc;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * A listener that sets the alignment of cells in a {@link JTable}. Expected usage:
 * <pre>
 * JTable table = new JTable(...);
 * TableAligner aligner = new TableAligner(table, SwingConstants.XXXX);
 * table.getColumnModel().addColumnModelListener(aligner);
 * </pre>
 */
public class TableAligner implements TableColumnModelListener {
    private final JTable table;
    private final int alignment;

    /**
     * Creates a listener that installs alignment decorators on new columns added to {@code table}.
     * Also performs the initial alignment of {@code table}.
     *
     * @param table  a table.
     * @param alignment  one of the following constants:
     *     <ul>
     *         <li>{@link SwingConstants#LEFT}</li>
     *         <li>{@link SwingConstants#CENTER} (the default for image-only labels)</li>
     *         <li>{@link SwingConstants#RIGHT}</li>
     *         <li>{@link SwingConstants#LEADING} (the default for text-only labels)</li>
     *         <li>{@link SwingConstants#TRAILING}</li>
     *     </ul>
     */
    public TableAligner(JTable table, int alignment) {
        this.table = table;
        this.alignment = alignment;
        installInAllColumns(table, alignment);  // Perform initial installation.
    }

    ////////////////////////////////////////////////////// Static //////////////////////////////////////////////////////

    /**
     * Installs alignment decorators in all of the table's columns.
     *
     * @param table  a table.
     * @param alignment  one of the following constants:
     *     <ul>
     *         <li>{@link SwingConstants#LEFT}</li>
     *         <li>{@link SwingConstants#CENTER} (the default for image-only labels)</li>
     *         <li>{@link SwingConstants#RIGHT}</li>
     *         <li>{@link SwingConstants#LEADING} (the default for text-only labels)</li>
     *         <li>{@link SwingConstants#TRAILING}</li>
     *     </ul>
     */
    public static void installInAllColumns(JTable table, int alignment) {
        // We don't want to set up completely new cell renderers: rather, we want to use the existing ones but just
        // change their alignment.
        for (int colViewIndex = 0; colViewIndex < table.getColumnCount(); ++colViewIndex) {
            installInOneColumn(table, colViewIndex, alignment);
        }
    }

    /**
     * Installs alignment decorators in the table column at {@code colViewIndex}.
     *
     * @param table  a table.
     * @param colViewIndex  the index of the column in the table <i>view</i>.
     * @param alignment  one of the following constants:
     *     <ul>
     *         <li>{@link SwingConstants#LEFT}</li>
     *         <li>{@link SwingConstants#CENTER} (the default for image-only labels)</li>
     *         <li>{@link SwingConstants#RIGHT}</li>
     *         <li>{@link SwingConstants#LEADING} (the default for text-only labels)</li>
     *         <li>{@link SwingConstants#TRAILING}</li>
     *     </ul>
     */
    public static void installInOneColumn(JTable table, int colViewIndex, int alignment) {
        TableColumn tableColumn = table.getColumnModel().getColumn(colViewIndex);

        TableCellRenderer headerRenderer = tableColumn.getHeaderRenderer();
        if (headerRenderer == null) {
            headerRenderer = table.getTableHeader().getDefaultRenderer();
        }
        if (!(headerRenderer instanceof  RendererAlignmentDecorator)) {  // Don't install a redundant decorator.
            tableColumn.setHeaderRenderer(new RendererAlignmentDecorator(headerRenderer, alignment));
        }

        TableCellRenderer cellRenderer = tableColumn.getCellRenderer();
        if (cellRenderer == null) {
            cellRenderer = table.getDefaultRenderer(table.getColumnClass(colViewIndex));
        }
        if (!(cellRenderer instanceof RendererAlignmentDecorator)) {  // Don't install a redundant decorator.
            tableColumn.setCellRenderer(new RendererAlignmentDecorator(cellRenderer, alignment));
        }
    }

    // We don't want to set up completely new cell renderers.
    // Rather, we just want to decorate the existing ones with alignment behavior.
    private static class RendererAlignmentDecorator implements TableCellRenderer {
        private final TableCellRenderer delegate;
        private final int alignment;

        private RendererAlignmentDecorator(TableCellRenderer delegate, int alignment) {
            this.delegate = delegate;
            this.alignment = alignment;
        }

        @Override public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cellRendererComp =
                    delegate.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (cellRendererComp instanceof DefaultTableCellRenderer) {
                ((DefaultTableCellRenderer) cellRendererComp).setHorizontalAlignment(alignment);
            }

            return cellRendererComp;
        }
    }

    ///////////////////////////////////////////// TableColumnModelListener /////////////////////////////////////////////

    @Override public void columnAdded(TableColumnModelEvent e) {
        installInOneColumn(table, e.getToIndex(), alignment);  // Only install in added column.
    }

    @Override public void columnRemoved(TableColumnModelEvent e) { }

    @Override public void columnMoved(TableColumnModelEvent e) { }

    @Override public void columnMarginChanged(ChangeEvent e) { }

    @Override public void columnSelectionChanged(ListSelectionEvent e) { }
}