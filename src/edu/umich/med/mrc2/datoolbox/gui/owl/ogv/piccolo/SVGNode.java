package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.piccolo;

import java.io.IOException;

import edu.umich.med.mrc2.datoolbox.gui.owl.ogv.gui.SVGIcon;

public class SVGNode extends IconNode {

	// generated
	private static final long serialVersionUID = 3224240826005055637L;

	public SVGNode(String uri) throws IOException {
		super(new SVGIcon(uri));
	}

}
