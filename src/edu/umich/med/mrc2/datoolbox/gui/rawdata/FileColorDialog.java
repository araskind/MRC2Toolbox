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

package edu.umich.med.mrc2.datoolbox.gui.rawdata;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.umich.med.mrc2.datoolbox.gui.main.MainActionCommands;

public class FileColorDialog extends JDialog implements ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3950458335043071228L;
	private JColorChooser tcc;
	private JButton selectColorButton;
	private Color newColor;

	public FileColorDialog(ActionListener listener) {

		setTitle("Set file color");
		setSize(new Dimension(450, 350));
		setResizable(false);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		tcc = new JColorChooser();
		getContentPane().add(tcc, BorderLayout.CENTER);
		tcc.getSelectionModel().addChangeListener(this);
		
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(440, 50));
		getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		
		selectColorButton = new JButton(MainActionCommands.SET_FILE_COLOR.getName());
		selectColorButton.setFont(new Font("Tahoma", Font.BOLD, 12));
		selectColorButton.setActionCommand(MainActionCommands.SET_FILE_COLOR.getName());
		selectColorButton.addActionListener(listener);
		panel.add(selectColorButton);
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		newColor = tcc.getColor();
	}

	public Color getNewColor() {
		return newColor;
	}		
}






