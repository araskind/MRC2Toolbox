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

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.motrpac.MoTrPACReport;
import edu.umich.med.mrc2.datoolbox.gui.mptrack.report.MoTrPACReportTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MoTrPACReportDocumentTypeRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8176749198564853226L;

	public MoTrPACReportDocumentTypeRenderer() {
		super();
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		Component rendererComponent = 
				table.prepareRenderer(new DefaultTableCellRenderer(), row, column);
		setForeground(rendererComponent.getForeground());
		setBackground(rendererComponent.getBackground());
		setFont(rendererComponent.getFont());

		if(value == null){
			setText("");
	    	setIcon(null);
	    	setText(null);
	    	setToolTipText(null);
			return this;
		}
    	if(value instanceof MoTrPACReport) {

    		MoTrPACReport report = (MoTrPACReport)value;
    		if(report.getLinkedDocumentId() != null) {
    			Icon downloadIcon = GuiUtils.getDocumentFormatIcon(
    					report.getLinkedDocumentFormat(), MoTrPACReportTable.iconSize);
    			setIcon(downloadIcon);
    			setText(report.getLinkedDocumentFormat().name());
    			setToolTipText(report.getLinkedDocumentName());
    		}
    	}
		return this;
	}
}
