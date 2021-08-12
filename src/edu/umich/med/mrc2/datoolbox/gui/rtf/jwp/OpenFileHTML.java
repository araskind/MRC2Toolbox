package edu.umich.med.mrc2.datoolbox.gui.rtf.jwp;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class OpenFileHTML implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent arg0) {
		File helpf = new File(this.getClass().getResource("/docs/jwordprocessor_help/index.html").getPath());
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.open(helpf);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}