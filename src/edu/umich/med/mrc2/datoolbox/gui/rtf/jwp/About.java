package edu.umich.med.mrc2.datoolbox.gui.rtf.jwp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

public class About implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent arg0) {
		JOptionPane.showMessageDialog(null,
				"<html><body><p>This project was created with the support of the library JOrtho Spell Check</p>"
				+ " <p>produced by <a href='http://inetsoftware.net' target='_blank'>i-net software</a> "
				+ "- http://www.inetsoftware.net/ </p><p>License GPL - https://www.inetsoftware.de/other-products/jortho</p><hr/><p>"
				+ "JWordProcessor by <a href='http://ulmdesign.mediamaster.eu' target='_blank'>Ulmdesign</a> software developer - "
				+ "Visit my App Store</p> <p> on https://play.google.com/store/apps/developer?id=UlmDesign</p></body></html>");
	}
}