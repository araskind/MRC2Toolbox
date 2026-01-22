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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;

public abstract class BasicDialogWithPreferences extends BasicDialog implements BackedByPreferences {

	private static final long serialVersionUID = 1L;
	
	public BasicDialogWithPreferences(
			String title,
			String iconId,
			Dimension preferredSize,
			ActionListener actionListener) {
		
		super(title, iconId,  preferredSize, actionListener);
	}
	
	@Override
	protected void createCancelListener() {
		
		cancelListener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};		
	}
	
	protected void disposeWithoutSavingPreferences() {
		super.dispose();
	}
	
	@Override
	public void dispose() {
		savePreferences();
		super.dispose();
	}
}
