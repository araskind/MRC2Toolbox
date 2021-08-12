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

package edu.umich.med.mrc2.datoolbox.taskcontrol.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.OverlayLayout;

public class LabeledProgressBar extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5487904194566826323L;
	private JLabel label;
	private JProgressBar progressBar;

	public LabeledProgressBar() {

		setLayout(new OverlayLayout(this));

		label = new JLabel();
		label.setAlignmentX(0.5f);
		label.setFont(label.getFont().deriveFont(11f));
		add(label);

		progressBar = new JProgressBar(0, 100);
		progressBar.setBorderPainted(false);
		add(progressBar);
	}

	public LabeledProgressBar(double value) {
		this();
		setValue(value);
	}

	public void setValue(double value) {
		int percent = (int) (value * 100);
		progressBar.setValue(percent);
		label.setText(percent + "%");
	}

	public void setValue(double value, String text) {
		int percent = (int) (value * 100);
		progressBar.setValue(percent);
		label.setText(text);
	}

}
