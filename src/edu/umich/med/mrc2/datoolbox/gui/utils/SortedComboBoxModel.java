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

package edu.umich.med.mrc2.datoolbox.gui.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;

public class SortedComboBoxModel<T> extends DefaultComboBoxModel<Object> {

	private static final long serialVersionUID = 1L;

	public SortedComboBoxModel() {
		super();
	}

	public SortedComboBoxModel(Object[] items) {
		Arrays.sort(items);
		int size = items.length;
		for (int i = 0; i < size; i++) {
			super.addElement(items[i]);
		}
		if (items.length > 0)
			setSelectedItem(items[0]);
	}

	public SortedComboBoxModel(Collection<?> items) {

		this(items.toArray(new Object[items.size()]));
	}

	public SortedComboBoxModel(Vector items) {


		Collections.sort(items);
		int size = items.size();
		for (int i = 0; i < size; i++) {
			super.addElement(items.elementAt(i));
		}
		setSelectedItem(items.elementAt(0));
	}

	@Override
	public void addElement(Object element) {
		insertElementAt(element, 0);
	}

	@Override
	public void insertElementAt(Object element, int index) {
		int size = getSize();
		// Determine where to insert element to keep model in sorted order
		for (index = 0; index < size; index++) {
			Comparable c = (Comparable) getElementAt(index);
			if (c.compareTo(element) > 0) {
				break;
			}
		}
		super.insertElementAt(element, index);
	}
}
