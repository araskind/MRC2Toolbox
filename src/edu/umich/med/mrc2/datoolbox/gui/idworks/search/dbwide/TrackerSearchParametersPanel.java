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

package edu.umich.med.mrc2.datoolbox.gui.idworks.search.dbwide;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.umich.med.mrc2.datoolbox.data.enums.ParameterSetStatus;
import edu.umich.med.mrc2.datoolbox.gui.communication.FormChangeEvent;
import edu.umich.med.mrc2.datoolbox.gui.communication.FormChangeListener;

public abstract class TrackerSearchParametersPanel extends JPanel 
	implements ActionListener, ListSelectionListener, ItemListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7230952891179346216L;
	
	protected Set<FormChangeListener> changeListeners;
	protected FormDocumentListener fdl;
		
	public TrackerSearchParametersPanel() {
		super();
		changeListeners = ConcurrentHashMap.newKeySet();
		fdl = new FormDocumentListener();
	}

	public void addFormChangeListener(FormChangeListener listener) {
		changeListeners.add(listener);
	}
	
	public void removeFormChangeListener(FormChangeListener listener) {
		changeListeners.remove(listener);
	}

	public void fireFormChangeEvent(ParameterSetStatus newStatus) {

		FormChangeEvent event = new FormChangeEvent(this, newStatus);
		changeListeners.stream().forEach(l -> ((FormChangeListener) l).
				formDataChanged(event));
	}
	
	public abstract Collection<String>validateInput();
	
	public abstract void resetPanel(Preferences preferences);
	
	public abstract boolean hasSpecifiedConstraints();
	
	class FormDocumentListener implements DocumentListener {

		public void changedUpdate(DocumentEvent e) {
			fireFormChangeEvent(ParameterSetStatus.CHANGED);
		}

		public void removeUpdate(DocumentEvent e) {
			fireFormChangeEvent(ParameterSetStatus.CHANGED);
		}

		public void insertUpdate(DocumentEvent e) {
			fireFormChangeEvent(ParameterSetStatus.CHANGED);
		}
	}
	
	@Override
	public void valueChanged(ListSelectionEvent e) {

		if(!e.getValueIsAdjusting()) 
			fireFormChangeEvent(ParameterSetStatus.CHANGED);
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getStateChange() == ItemEvent.SELECTED 
				|| e.getStateChange() == ItemEvent.DESELECTED) {
			fireFormChangeEvent(ParameterSetStatus.CHANGED);
		}
	}
}
