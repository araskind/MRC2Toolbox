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

package edu.umich.med.mrc2.datoolbox.gui.rgen;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.rgen.mcr.DockableMetabCombinerScriptGenerator;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class RscriptGeneratorWindow extends JFrame 
	implements ActionListener, BackedByPreferences, PersistentLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Preferences preferences;
	
	private static final Icon frameIcon = GuiUtils.getIcon("rScript", 32);
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "RscriptGeneratorWindow.layout");
	private CControl control;
	private CGrid grid;
	
	private DockableMetabCombinerScriptGenerator metabCombinerScriptGenerator;
	
	public RscriptGeneratorWindow() throws HeadlessException {

		super("R-script generator");
		setIconImage(((ImageIcon) frameIcon).getImage());

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setMinimumSize(new Dimension(1000, 800));
		setPreferredSize(new Dimension(1000, 800));
		
		control = new CControl(this);
		control.getController().setTheme(new EclipseTheme());
		this.add( control.getContentArea() );
		grid = new CGrid(control);
		
		metabCombinerScriptGenerator = new DockableMetabCombinerScriptGenerator();
		grid.add(0, 0, 1, 1, metabCombinerScriptGenerator);
		
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
	
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
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

	@Override
	public void loadPreferences(Preferences preferences) {
		
		this.preferences = preferences;
//		String baseDirPath = preferences.get(BASE_DIRECTORY, 
//						MRC2ToolBoxConfiguration.getDefaultExperimentsDirectory());
//		try {
//			baseDirectory = Paths.get(baseDirPath).toFile();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	@Override
	public void loadPreferences() {
		loadPreferences(Preferences.userNodeForPackage(this.getClass()));		
	}

	@Override
	public void savePreferences() {

		preferences = Preferences.userNodeForPackage(this.getClass());
//		preferences.put(BASE_DIR, baseDirectory.getAbsolutePath());
	}
}
