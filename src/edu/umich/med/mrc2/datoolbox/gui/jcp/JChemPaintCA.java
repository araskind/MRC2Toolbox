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

package edu.umich.med.mrc2.datoolbox.gui.jcp;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.openscience.cdk.interfaces.IChemModel;
import org.openscience.jchempaint.GT;
import org.openscience.jchempaint.JCPPropertyHandler;
import org.openscience.jchempaint.JChemPaintPanel;
import org.openscience.jchempaint.application.JChemPaint;

public class JChemPaintCA extends JChemPaint {

	public JChemPaintCA() {
		super();
        Properties props = JCPPropertyHandler.getInstance(true).getJCPProperties();
        try {
            UIManager.setLookAndFeel(props.getProperty("LookAndFeelClass"));
        } catch (Throwable e)  {
            String sys = UIManager.getSystemLookAndFeelClassName();
            try {
				UIManager.setLookAndFeel(sys);
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
					| UnsupportedLookAndFeelException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            props.setProperty("LookAndFeelClass", sys);
        }
        // Language
        props.setProperty("General.language", System.getProperty("user.language", "en"));
        showEmptyInstance(false);      
	}

    public static void showEmptyInstance(boolean debug) {
        IChemModel chemModel = emptyModel();
        showInstance(chemModel, GT.get("Untitled") + " "
                + (instancecounter++), debug);
    }

    public static JChemPaintPanel showInstance(IChemModel chemModel, String title, boolean debug) {
    	
        JFrame f = new JFrame(title + " - JChemPaint");
        chemModel.setID(title);
        f.addWindowListener(new AppCloser());
        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        JChemPaintPanel p = new JChemPaintPanel(chemModel, GUI_APPLICATION, debug, null, new ArrayList<String>());
        f.setPreferredSize(new Dimension(800, 494));    //1.618
        f.setJMenuBar(p.getJMenuBar());
        f.add(p);
        f.pack();
        Point point = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getCenterPoint();
        int w2 = (f.getWidth() / 2);
        int h2 = (f.getHeight() / 2);
        f.setLocation(point.x - w2, point.y - h2);
        f.setVisible(true);
		frameList.add(f);
        return p;
    }
    
    /**
     * Class for closing jcp
     *
     *@author shk3
     *@cdk.created November 23, 2008
     */
    public final static class AppCloser extends WindowAdapter {

        /**
         * closing Event. Shows a warning if this window has unsaved data-
         */
    	//	TODO adopt this for closing the dialog and take into account if any changes were made
    	
        public void windowClosing(WindowEvent e) {
            int clear = ((JChemPaintPanel) ((JFrame) e.getSource())
                    .getContentPane().getComponents()[0]).showWarning();
            if (JOptionPane.CANCEL_OPTION != clear) {
            	frameList.remove((JFrame) e.getSource());
                ((JFrame) e.getSource()).setVisible(false);
                ((JFrame) e.getSource()).dispose();
            }
        }
    }
}
