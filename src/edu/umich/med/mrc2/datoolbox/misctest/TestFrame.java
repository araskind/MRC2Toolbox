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

package edu.umich.med.mrc2.datoolbox.misctest;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CGrid;
import edu.umich.med.mrc2.datoolbox.gui.utils.GuiUtils;

public class TestFrame extends JFrame implements ActionListener, WindowListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4052979852344669138L;
	private static final Icon componentIcon = GuiUtils.getIcon("annotations", 16);

	private CControl control;
	private CGrid grid;
	
	public static void main(String[] args) {

		TestFrame testFrame = new TestFrame();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		testFrame.setLocation(
				dim.width/2-testFrame.getSize().width/2, 
				dim.height/2-testFrame.getSize().height/2);
		testFrame.setVisible(true);
	}

	public TestFrame() {
		initGui();
		createContents();
		setPreferredSize(new Dimension(300, 200));
		pack();
	}

	private void initGui(){

		try {
			// Set System L&F
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			// handle exception
		} catch (ClassNotFoundException e) {
			// handle exception
		} catch (InstantiationException e) {
			// handle exception
		} catch (IllegalAccessException e) {
			// handle exception
		}
		addWindowListener(this);
	}
	
	private void createContents() {
		
		Icon loader = GuiUtils.getLoaderIcon("orange_circles", 64);
		JLabel label = new JLabel("Waiting ...", loader, SwingConstants.LEFT);	
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
        panel.add(label);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(panel);
		pack();
		
//		control = new CControl(this);
//		control.getController().setTheme(new EclipseTheme());
//		this.add( control.getContentArea() );
//		grid = new CGrid(control);		
//		IChemModel chemModel = JChemPaint.emptyModel();
//		//JChemPaintPanel jcPanel = JChemPaint.showInstance(chemModel, "New molecule", false);
//		List<String>ignoreItems = Arrays.asList("new", "close", "exit");
//		JChemPaintPanel jcPanel = new JChemPaintPanel(chemModel, JChemPaint.GUI_APPLICATION, false, null, ignoreItems);
////		jcPanel.setShowMenuBar(false);
////		jcPanel.customizeView();
//		DefaultSingleCDockable panel = 
//				new DefaultSingleCDockable(
//						"Test panel", 
//						componentIcon, 
//						"Test panel", 
//						jcPanel, 
//						Permissions.MIN_MAX_STACK);
//		panel.setCloseable(false);
//		grid.add(0, 0, 1, 1,
//				panel);
//		control.getContentArea().deploy(grid);
	}

	@Override
	public void actionPerformed(ActionEvent event) {

		String command = event.getActionCommand();
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent e) {

		this.dispose();
		System.gc();
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}
}























