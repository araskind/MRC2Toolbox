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

package edu.umich.med.mrc2.datoolbox.gui.adducts.chemmod;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import edu.umich.med.mrc2.datoolbox.data.Adduct;
import edu.umich.med.mrc2.datoolbox.gui.adducts.exchange.DockableAdductExchangeManager;
import edu.umich.med.mrc2.datoolbox.gui.structure.DockableMolStructurePanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.ChemicalModificationsManager;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class ChemicalModificationManagerFrame extends JFrame implements ListSelectionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 3992935589064642489L;

	private static final Icon frameIcon = GuiUtils.getIcon("chemModList", 32);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "ChemicalModificationManagerFrame.layout");
	private CControl control;
	private CGrid grid;
	private DockableChemModEditor chemModEditor;
	private DockableAdductExchangeManager adductExchangeManager;
	private DockableMolStructurePanel structurePanel;

	public ChemicalModificationManagerFrame() throws HeadlessException {

		super("Chemical modifications manager");
		setIconImage(((ImageIcon) frameIcon).getImage());

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(800, 600));
		setPreferredSize(new Dimension(800, 600));
		
		if(!ChemicalModificationsManager.isInitialized()) {
			
			try {
				ChemicalModificationsManager.populateDataLists();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		control = new CControl(this);
		control.getController().setTheme(new EclipseTheme());
		this.add( control.getContentArea() );
		grid = new CGrid(control);

		chemModEditor = new DockableChemModEditor();
		chemModEditor.getAdductTable().getSelectionModel().addListSelectionListener(this);
		adductExchangeManager = new DockableAdductExchangeManager();
		structurePanel = new DockableMolStructurePanel(
				"ChemicalModificationManagerFrameDockableMolStructurePanel");

		grid.add(0, 0, 1, 1,
				chemModEditor,
				adductExchangeManager,
				structurePanel);

		control.getController().setFocusedDockable(chemModEditor.intern(), true);
		control.getContentArea().deploy(grid);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JRootPane rootPane = SwingUtilities.getRootPane(control.getContentArea());
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		loadLayout(layoutConfigFile);
		pack();
	}

	@Override
	public void dispose() {

		saveLayout(layoutConfigFile);
		super.dispose();
	}

	public void loadLayout(File layoutFile) {

		if(control != null) {

			if(layoutFile.exists()) {
				try {
					control.readXML(layoutFile);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					control.writeXML(layoutFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void saveLayout(File layoutFile) {

		if(control != null) {
			try {
				control.writeXML(layoutFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		structurePanel.clearPanel();
		Adduct mod = chemModEditor.getAdductTable().getSelectedModification();
		if(mod == null || mod.getSmiles() == null)
			return;
		
		structurePanel.showStructure(mod.getSmiles());
	}
}












