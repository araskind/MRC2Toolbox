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

package edu.umich.med.mrc2.datoolbox.gui.idworks.stan;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import edu.umich.med.mrc2.datoolbox.data.StandardFeatureAnnotation;
import edu.umich.med.mrc2.datoolbox.gui.tables.BasicTable;

public class StandardFeatureAnnotationListRenderer extends JLabel 
			implements ListCellRenderer<StandardFeatureAnnotation> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 6337551369995892458L;

	public static final Color ALTERNATE_ROW_COLOR = BasicTable.ALTERNATE_ROW_COLOR;
	public static final Color WHITE_COLOR = BasicTable.WHITE_COLOR;
	
	public StandardFeatureAnnotationListRenderer() {
		super();
		setOpaque(true);
	}

	@Override
    public Component getListCellRendererComponent(
    		JList<? extends StandardFeatureAnnotation> list, 
    		StandardFeatureAnnotation annotation, 
    		int index,
    		boolean isSelected, 
    		boolean cellHasFocus) {
         
		String s = "<B>" + annotation.getCode() + "</B><BR>" + annotation.getText();
		String html = "<html><body style='width: %1spx'>%1s";
        setText(String.format(html, list.getWidth(), s));
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {			
        	setForeground(list.getForeground()); 
			Color bg = (index % 2 == 0 ? ALTERNATE_ROW_COLOR : WHITE_COLOR);
			setBackground(bg);
			bg = null;
        }         
        return this;
    }
}

