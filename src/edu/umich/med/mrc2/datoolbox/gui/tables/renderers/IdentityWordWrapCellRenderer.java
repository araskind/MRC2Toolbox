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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.data.MsFeatureIdentity;
import edu.umich.med.mrc2.datoolbox.data.enums.CompoundIdentityField;

public class IdentityWordWrapCellRenderer extends JTextArea implements TableCellRenderer {

    /**
	 *
	 */
	private static final long serialVersionUID = 7135984416347268526L;
	
	private CompoundIdentityField idField;

	public IdentityWordWrapCellRenderer(CompoundIdentityField idField) {
		super();
		this.idField = idField;
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    	Component rendererComponent = table.prepareRenderer(new DefaultTableCellRenderer(), row, column);
		setForeground(rendererComponent.getForeground());
		setBackground(rendererComponent.getBackground());
		setFont(rendererComponent.getFont());
		setMargin(new Insets(5,10,5,10));

		if(value == null) {
			setText("");
			return this;
		}
		CompoundIdentity id = null;
		boolean isQc = false;
		if(value instanceof CompoundIdentity)
			id = (CompoundIdentity)value;
		
		if(value instanceof MsFeatureIdentity) {
			id = ((MsFeatureIdentity)value).getCompoundIdentity();			
			isQc = ((MsFeatureIdentity)value).isQcStandard();
		}		
		if(id == null) {
			setText("");
			return this;
		}
		String text = "";
		if (idField.equals(CompoundIdentityField.NAME))
			text = id.getName();

		if (idField.equals(CompoundIdentityField.COMMON_NAME))
			text = id.getCommonName();

		if (idField.equals(CompoundIdentityField.SYS_NAME))
			text = id.getSysName();

		if (idField.equals(CompoundIdentityField.CLASS_NAME))
			text = id.getClassName();

		if (idField.equals(CompoundIdentityField.FORMULA))
			text = id.getFormula();

		if (idField.equals(CompoundIdentityField.SMILES))
			text = id.getSmiles();

		if(isQc) {
			setFont(new Font("Default", Font.BOLD, getFont().getSize()));
			setForeground(Color.RED);
		}
		setText(text);
	
        setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
        if (table.getRowHeight(row) != getPreferredSize().height)
            table.setRowHeight(row, getPreferredSize().height);

        return this;
    }
}

