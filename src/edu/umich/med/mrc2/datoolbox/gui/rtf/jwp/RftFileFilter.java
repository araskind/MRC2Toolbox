package edu.umich.med.mrc2.datoolbox.gui.rtf.jwp;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class RftFileFilter extends FileFilter {
	@Override
	public boolean accept(File file) {
		if (file.isDirectory()) {
			return true;
		}
		String fname = file.getName().toLowerCase();
		return fname.endsWith("rtf");
	}

	@Override
	public String getDescription() {
		return "File di testo";
	}
}
