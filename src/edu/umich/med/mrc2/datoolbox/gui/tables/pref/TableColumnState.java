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

package edu.umich.med.mrc2.datoolbox.gui.tables.pref;

import javax.swing.SortOrder;

import org.jdom2.Element;

public class TableColumnState {

	private String columnName;
	private boolean isVisible;
	private SortOrder sortOrder;
	private int sorterPosition;
	
	public TableColumnState(String columnName) {
		super();
		this.columnName = columnName;
		isVisible = true;
		sortOrder = SortOrder.UNSORTED;
		sorterPosition = -1;
	}

	public TableColumnState(
			String columnName, 
			boolean isVisible, 
			SortOrder sortOrder) {
		super();
		this.columnName = columnName;
		this.isVisible = isVisible;
		this.sortOrder = sortOrder;
		sorterPosition = -1;
	}

	public TableColumnState(
			String columnName, 
			boolean isVisible, 
			SortOrder sortOrder, 
			int sorterPosition) {
		super();
		this.columnName = columnName;
		this.isVisible = isVisible;
		this.sortOrder = sortOrder;
		this.sorterPosition = sorterPosition;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public boolean isVisible() {
		return isVisible;
	}

	public int getSorterPosition() {
		return sorterPosition;
	}

	public void setSorterPosition(int sorterPosition) {
		this.sorterPosition = sorterPosition;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public SortOrder getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(SortOrder sortOrder) {
		this.sortOrder = sortOrder;
	}
	
	public Element getXmlElement() {
	
		Element tcsElement = 
				new Element(TableLayoutFields.TableColumnState.name());		
		tcsElement.setAttribute(
				TableLayoutFields.ColumnName.name(), columnName);
		tcsElement.setAttribute(
				TableLayoutFields.IsVisible.name(), Boolean.toString(isVisible));
		tcsElement.setAttribute(
				TableLayoutFields.SortOrder.name(), sortOrder.name());
		tcsElement.setAttribute(
				TableLayoutFields.SorterPosition.name(), Integer.toString(sorterPosition));

		return tcsElement;
	}
	
	public TableColumnState(Element tcsElement) {
		super();
		columnName = 
				tcsElement.getAttributeValue(TableLayoutFields.ColumnName.name());
		isVisible = Boolean.valueOf(
				tcsElement.getAttributeValue(TableLayoutFields.IsVisible.name()));
		sortOrder = SortOrder.valueOf(
				tcsElement.getAttributeValue(TableLayoutFields.SortOrder.name()));
		sorterPosition = Integer.valueOf(
				tcsElement.getAttributeValue(TableLayoutFields.SorterPosition.name()));
	}
}





