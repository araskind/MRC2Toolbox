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

package edu.umich.med.mrc2.datoolbox.gui.cpddatabase.msready;

import java.awt.BorderLayout;

import javax.swing.Icon;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import edu.umich.med.mrc2.datoolbox.data.CompoundIdentity;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class DockableCompoundStructuralDescriptorsPanel extends DefaultSingleCDockable {

	private static final Icon componentIcon = GuiUtils.getIcon("compoundInfo", 16);

	private CompoundStructuralDescriptorsPanel structuralDescriptorsPanel;
	
	public DockableCompoundStructuralDescriptorsPanel(String id, String name, boolean enablePaste) {

		super(id, componentIcon, name, null, Permissions.MIN_MAX_STACK);
		
		setLayout(new BorderLayout(0,0));
		structuralDescriptorsPanel = new CompoundStructuralDescriptorsPanel(enablePaste);
		add(structuralDescriptorsPanel, BorderLayout.CENTER);
	}
	
	public String getFormula() {
		return structuralDescriptorsPanel.getFormula();
	}
	
	public String getInchiKey() {
		return structuralDescriptorsPanel.getInchiKey();
	}
	
	public String getSmiles() {
		return structuralDescriptorsPanel.getSmiles();
	}
	
	public int getCharge() {
		return structuralDescriptorsPanel.getCharge();
	}
	
	public double getMass() {
		return structuralDescriptorsPanel.getMass();
	}
	
	public void setFormula(String formula) {
		structuralDescriptorsPanel.setFormula(formula);
	}
	
	public void setInchiKey(String inchiKey) {
		structuralDescriptorsPanel.setInchiKey(inchiKey);
	}
	
	public void setSmiles(String smiles) {
		structuralDescriptorsPanel.setSmiles(smiles);
	}
	
	public void setCharge(int charge) {
		structuralDescriptorsPanel.setCharge(charge);
	}
	
	public void setMass(double mass) {		
		structuralDescriptorsPanel.setMass(mass);	
	}
	
	public void loadCompoundIdentity(CompoundIdentity cid) {		
		structuralDescriptorsPanel.loadCompoundIdentity(cid);
	}
	
	public void lockEditing(boolean includeSmiles) {		
		structuralDescriptorsPanel.lockEditing(includeSmiles);
	}
	
	public void unlockEditing() {		
		structuralDescriptorsPanel.unlockEditing();
	}

	public synchronized void clearPanel() {
		structuralDescriptorsPanel.clearPanel();		
	}
}
