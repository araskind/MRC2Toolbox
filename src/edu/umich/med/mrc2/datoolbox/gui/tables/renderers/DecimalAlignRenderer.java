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

package edu.umich.med.mrc2.datoolbox.gui.tables.renderers;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

public class DecimalAlignRenderer implements TableCellRenderer {

	protected static final float POS = 40f;
	protected static final int ALIGN = TabStop.ALIGN_DECIMAL;
	protected static final int LEADER = TabStop.LEAD_NONE;
	protected static final SimpleAttributeSet ATTRIBS = new SimpleAttributeSet();
	protected static final TabStop TAB_STOP = new TabStop(POS, ALIGN, LEADER);
	protected static final TabSet TAB_SET = new TabSet(new TabStop[] { TAB_STOP });
	protected StyledDocument document = new DefaultStyledDocument();
	protected JTextPane pane = new JTextPane(document);

	public DecimalAlignRenderer() {

		StyleConstants.setTabSet(ATTRIBS, TAB_SET);
		pane.setParagraphAttributes(ATTRIBS, false);
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		if (value == null || (double)value == 0.0 || Double.isNaN((double)value)) {
			pane.setText("\t");
		} else {
			pane.setText("\t" + value.toString());
		}
		return pane;
	}

}
