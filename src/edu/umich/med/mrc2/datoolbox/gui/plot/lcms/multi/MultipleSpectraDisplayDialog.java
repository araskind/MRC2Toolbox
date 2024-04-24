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

package edu.umich.med.mrc2.datoolbox.gui.plot.lcms.multi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import edu.umich.med.mrc2.datoolbox.gui.plot.PlotType;
import edu.umich.med.mrc2.datoolbox.gui.preferences.BackedByPreferences;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class MultipleSpectraDisplayDialog extends JDialog 
			implements BackedByPreferences, ActionListener, ItemListener {

	private static final long serialVersionUID = 1L;
	
	private static final Icon componentIcon = GuiUtils.getIcon("msmsMulti", 32);
	private LCMSMultiPlotToolbar toolbar;
	private LCMSMultiPlotPanel plotPanel;

	public MultipleSpectraDisplayDialog() {
		super();
		setPreferredSize(new Dimension(640, 800));
		setSize(new Dimension(640, 800));
		setTitle("Multiple spactra display");
		setIconImage(((ImageIcon) componentIcon).getImage());
		setModalityType(ModalityType.MODELESS);
		setResizable(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		toolbar = new LCMSMultiPlotToolbar(PlotType.SPECTRUM, this);
		getContentPane().add(toolbar, BorderLayout.NORTH);
			
		plotPanel = new LCMSMultiPlotPanel(PlotType.SPECTRUM);
		getContentPane().add(new JScrollPane(plotPanel), BorderLayout.CENTER);
				
		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		ActionListener al = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				dispose();
			}
		};
		JRootPane rootPane = SwingUtilities.getRootPane(plotPanel);
		rootPane.registerKeyboardAction(al, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	@Override
	public void dispose() {
		
		savePreferences();
		super.dispose();
	}
	
	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadPreferences(Preferences preferences) {
		// TODO Auto-generated method stub

	}

	@Override
	public void loadPreferences() {
		// TODO Auto-generated method stub

	}

	@Override
	public void savePreferences() {
		// TODO Auto-generated method stub

	}

}
