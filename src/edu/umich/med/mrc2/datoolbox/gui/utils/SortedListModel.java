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
import java.util.Comparator;
import java.util.TreeSet;

import javax.swing.DefaultListModel;

public class SortedListModel<E> extends DefaultListModel<E> {

	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("rawtypes")
	private Comparator comp;

	@SuppressWarnings("rawtypes")
	public SortedListModel(Comparator comp) {
		super();
		this.comp = comp;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortedListModel(E[] items, Comparator comp) {
		this(comp);
		Arrays.sort(items, comp);
		addAll(Arrays.asList(items));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortedListModel(Collection<? extends Comparable> items) {

		TreeSet itemSet = new TreeSet();
		itemSet.addAll(items);
		this.addAll(itemSet);		
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addElement(E element) {

        TreeSet<E> items = new TreeSet<E>();
        if(comp != null)
        	items = new TreeSet<E>(comp);
        
		int size = getSize();
		for (int i = 0; i < size; i++)
			items.add(getElementAt(i));
		
		items.add(element);

		addAll(items);
	}

    public void addAll(Collection<? extends E> c) {

        if (c.isEmpty())
            return;

        TreeSet<E> items = new TreeSet<E>();
        if(comp != null)
        	items = new TreeSet<E>(comp);
        
		int size = getSize();
		for (int i = 0; i < size; i++)
			items.add(getElementAt(i));

        items.addAll(c);
		removeAllElements();
		for(E item : items)
			super.addElement(item);
    }

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void insertElementAt(E element, int index) {
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

	@SuppressWarnings("unchecked")
	public Collection<? extends E>getElementCollection(){

        TreeSet<E> items = new TreeSet<E>();
        if(comp != null)
        	items = new TreeSet<E>(comp);
        
		int size = getSize();
		for (int i = 0; i < size; i++)
			items.add(getElementAt(i));

		return items;
	}
}
