package edu.umich.med.mrc2.datoolbox.gui.rtf.jwp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExitApp implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			System.exit(0);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
