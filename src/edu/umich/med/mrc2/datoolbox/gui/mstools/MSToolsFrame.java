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

package edu.umich.med.mrc2.datoolbox.gui.mstools;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import edu.umich.med.mrc2.datoolbox.gui.main.PersistentLayout;
import edu.umich.med.mrc2.datoolbox.gui.mstools.mfgen.DockableFormulaGenerator;
import edu.umich.med.mrc2.datoolbox.gui.mstools.mfgen.FormulaGeneratorPanel;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;
import edu.umich.med.mrc2.datoolbox.main.MRC2ToolBoxCore;

public class MSToolsFrame extends JFrame implements ActionListener, PersistentLayout {

	/**
	 *
	 */
	private static final long serialVersionUID = 8229460374416893894L;

	private static final Icon frameIcon = GuiUtils.getIcon("toolbox", 32);

	private DockableIsotopicPatternCalculator isotopicPatternCalculatorWrapper;
	private DockableFormulaGenerator formulaGeneratorWrapper;
	private DockableMiscCalculationsPanel calcPanel;

	private CControl control;
	private CGrid grid;
	private static final File layoutConfigFile = new File(MRC2ToolBoxCore.configDir + "MSToolsFrame.layout");

	public MSToolsFrame() {

		super("MS tools");
		setIconImage(((ImageIcon) frameIcon).getImage());

		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setMinimumSize(new Dimension(800, 600));
		setPreferredSize(new Dimension(800, 600));

		control = new CControl(this);
		control.getController().setTheme(new EclipseTheme());
		this.add( control.getContentArea() );
		grid = new CGrid(control);

		isotopicPatternCalculatorWrapper = 
				new DockableIsotopicPatternCalculator(new IsotopicPatternCalculator(this));
		formulaGeneratorWrapper = 
				new DockableFormulaGenerator(new FormulaGeneratorPanel(this));
		calcPanel = new DockableMiscCalculationsPanel();

		grid.add(0, 0, 1, 1,
				isotopicPatternCalculatorWrapper,
				formulaGeneratorWrapper,
				calcPanel);

		control.getContentArea().deploy(grid);		
		loadLayout(layoutConfigFile);
		grid.select(0, 0, 1, 1, isotopicPatternCalculatorWrapper);
		
		pack();
	}

	@Override
	public void setVisible(boolean b) {

		if(!b)
			saveLayout(layoutConfigFile);
		
		super.setVisible(b);
	}

	@Override
	public void dispose() {

		saveLayout(layoutConfigFile);		
		super.dispose();
	}
	
	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();

		//if (command.equals(CALCULATE_ISOTOPE_DISTRIBUTION))

	}

	@Override
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

	@Override
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



































