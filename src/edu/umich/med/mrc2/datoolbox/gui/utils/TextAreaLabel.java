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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public class TextAreaLabel extends JPanel{

	/**
	 *
	 */
	private static final long serialVersionUID = 7167030972774481826L;
	private JTextArea textArea;

	public TextAreaLabel() {

		super(new BorderLayout(0,0));
		setBorder(null);
		textArea = new JTextArea();
	    textArea.setWrapStyleWord(true);
	    textArea.setLineWrap(true);
	    textArea.setOpaque(false);
	    textArea.setEditable(false);
	    textArea.setFocusable(false);
	    textArea.setBackground(UIManager.getColor("Label.background"));
	    textArea.setFont(UIManager.getFont("Label.font"));
	    textArea.setBorder(null);

		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setBorder(null);
		add(scrollPane, BorderLayout.CENTER);
	}

	public void setText(String text) {
		textArea.setText(text);
	}

	public String getText() {
		return textArea.getText();
	}

	@Override
	public void setFont(Font newFont) {

		if(textArea != null)
			textArea.setFont(newFont);
	}

	public void setEditable(boolean b) {

		if(textArea != null) {

			textArea.setEditable(b);
			textArea.setFocusable(b);
			textArea.setOpaque(b);

			if(b)
				textArea.setBackground(Color.WHITE);
			else
				textArea.setBackground(UIManager.getColor("Label.background"));
		}
	}
 }
