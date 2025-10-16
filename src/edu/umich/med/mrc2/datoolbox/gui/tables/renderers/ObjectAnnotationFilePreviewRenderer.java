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

package edu.umich.med.mrc2.datoolbox.gui.tables.renderers;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.lims.ObjectAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.annotation.ObjectAnnotationTable;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class ObjectAnnotationFilePreviewRenderer extends DefaultTableCellRenderer implements MouseListener {

	/**
	 *
	 */
	private static final long serialVersionUID = -3896968919119145076L;

	private ObjectAnnotationTable parentTable;
	private ObjectAnnotation annotation;
	protected static final Icon previewIcon = GuiUtils.getIcon("previewDocument", ObjectAnnotationTable.iconSize);

	public ObjectAnnotationFilePreviewRenderer(ObjectAnnotationTable parentTable) {
		super();
		this.parentTable = parentTable;
	}

	@Override
	public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    	Component rendererComponent = table.prepareRenderer(new DefaultTableCellRenderer(), row, column);
    	setForeground(rendererComponent.getForeground());
    	setBackground(rendererComponent.getBackground());
    	setFont(rendererComponent.getFont());
    	
		if(value == null){
	    	setIcon(null);
	    	setText(null);
	    	setToolTipText(null);
	    	annotation = null;
			return this;
		}
    	if(value instanceof ObjectAnnotation) {

    		annotation = (ObjectAnnotation)value;
    		if(annotation.getLinkedDocumentId() != null) {
    			setIcon(previewIcon);
    			setToolTipText(
    					//	"Click to view " + 
    					annotation.getLinkedDocumentName());
    		}
    	}
		return this;
	}

	@Override
	public void mouseClicked(MouseEvent e) {

		if(annotation.getLinkedDocumentId() != null)
			parentTable.previewAnnotationDocument(annotation);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}
}
