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

package edu.umich.med.mrc2.datoolbox.gui.adducts;

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
import edu.umich.med.mrc2.datoolbox.gui.adducts.adduct.DockableAdductEditor;
import edu.umich.med.mrc2.datoolbox.gui.adducts.bindif.DockableBinnerAnnotationsEditor;
import edu.umich.med.mrc2.datoolbox.gui.adducts.exchange.DockableAdductExchangeManager;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.structure.DockableMolStructurePanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class AdductManagerFrame extends JFrame 
		implements ListSelectionListener, PersistentLayout {

	/**
	 *
	 */
	private static final long serialVersionUID = 3992935589064642489L;

	private static final Icon frameIcon = GuiUtils.getIcon("chemModList", 32);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "AdductManagerFrame.layout");
	private CControl control;
	private CGrid grid;
	private DockableAdductEditor adductEditor;
	private DockableAdductExchangeManager adductExchangeManager;
	private DockableMolStructurePanel structurePanel;
	private DockableBinnerAnnotationsEditor binnerAnnotationsEditor;

	public AdductManagerFrame() throws HeadlessException {

		super("MS Adduct/loss manager");
		setIconImage(((ImageIcon) frameIcon).getImage());

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(800, 600));
		setPreferredSize(new Dimension(800, 600));
		
		control = new CControl(this);
		control.getController().setTheme(new EclipseTheme());
		this.add( control.getContentArea() );
		grid = new CGrid(control);

		adductEditor = new DockableAdductEditor();
		adductEditor.getAdductTable().getSelectionModel().addListSelectionListener(this);
		adductExchangeManager = new DockableAdductExchangeManager();
		structurePanel = new DockableMolStructurePanel(
				"AdductManagerFrameDockableMolStructurePanel");
		binnerAnnotationsEditor = new DockableBinnerAnnotationsEditor();

		grid.add(0, 0, 1, 1,
				adductEditor,
				adductExchangeManager,
				binnerAnnotationsEditor,
				structurePanel);

		control.getController().setFocusedDockable(adductEditor.intern(), true);
		control.getContentArea().deploy(grid);

		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				disposeWithoutSavingPreferences();
			}
		};
		JRootPane rootPane = SwingUtilities.getRootPane(control.getContentArea());
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		loadLayout(layoutConfigFile);
		pack();
	}
	
	private void disposeWithoutSavingPreferences() {
		super.dispose();
	}
	
	public void refreshAdductList() {
		adductEditor.refreshAdductList();
	}

	@Override
	public void dispose() {

		saveLayout(layoutConfigFile);
		super.dispose();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		structurePanel.clearPanel();
		Adduct mod = adductEditor.getAdductTable().getSelectedModification();
		if(mod == null || mod.getSmiles() == null)
			return;
		
		structurePanel.showStructure(mod.getSmiles());
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
	public File getLayoutFile() {
		return layoutConfigFile;
	}

}












