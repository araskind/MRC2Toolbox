package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.piccolo;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;

public interface Morpher {
	public PActivity morph(PNode before, PNode after, long duration);
}
