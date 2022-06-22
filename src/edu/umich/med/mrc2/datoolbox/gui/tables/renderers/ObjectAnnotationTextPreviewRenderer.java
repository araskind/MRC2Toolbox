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
import java.awt.Insets;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;

import edu.umich.med.mrc2.datoolbox.data.compare.SortProperty;
import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import rtf.AdvancedRTFDocument;

public class ObjectAnnotationTextPreviewRenderer extends WordWrapCellRenderer {

	/**
	 *
	 */
	private static final long serialVersionUID = -5574529780890867665L;
	
	private int maxLength;
	private SortProperty idField;
	
	public ObjectAnnotationTextPreviewRenderer(SortProperty idField, int maxLength) {
		super();
		this.idField = idField;
		this.maxLength = maxLength;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

    	Component rendererComponent = table.prepareRenderer(new DefaultTableCellRenderer(), row, column);
		setForeground(rendererComponent.getForeground());
		setBackground(rendererComponent.getBackground());
		setFont(rendererComponent.getFont());
		setMargin(new Insets(5,10,5,10));

		if(value == null) {
			setText("");
			return this;
		}
		if (value instanceof ObjectAnnotation) {

			ObjectAnnotation annotation = (ObjectAnnotation)value;

			if(idField.equals(SortProperty.ID))
				setText(annotation.getUniqueId());

			if(idField.equals(SortProperty.Name)) {
				
				if(annotation.getLinkedDocumentId() != null)
					setText(annotation.getLinkedDocumentName());
				else if(annotation.getChemModel() != null)
					setText(annotation.getChemModelNotes());
				else {
					AdvancedRTFDocument rtfDoc = (AdvancedRTFDocument)annotation.getRtfDocument(); 
					if(rtfDoc != null){
						Element paragraph = rtfDoc.getParagraph(0);
						String txt = "";
						try {
							txt = rtfDoc.getText(paragraph.getStartOffset(), paragraph.getEndOffset() - paragraph.getStartOffset());
						} catch (BadLocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(txt.length() > maxLength)
							txt = txt.substring(0, maxLength) + " ...";
						
						setText(txt);
					}
				}
			}
		}
        setSize(table.getColumnModel().getColumn(column).getWidth(), getPreferredSize().height);
        if (table.getRowHeight(row) != getPreferredSize().height)
            table.setRowHeight(row, getPreferredSize().height);

		return this;
	}
}
