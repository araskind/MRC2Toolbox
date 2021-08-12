package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph.collapse;

import org.apache.log4j.Logger;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph.tooltip.AbstractTooltipFactory;

public class SimpleTooltipFactory extends AbstractTooltipFactory {

	//initialize logger
	protected final static Logger logger = Logger.getLogger(SimpleTooltipFactory.class);
	
	protected static SimpleTooltipFactory factory;
	
	public static synchronized SimpleTooltipFactory getInstance() {
		if (factory == null)
			factory = new SimpleTooltipFactory();
		return factory;
	}

	@Override
	public PNode getTooltip(PNode node) {
		PText text = null;
		if (node.getAttribute("tooltipText") != null)
			text = new PText((String) node.getAttribute("tooltipText"));
		return text;
	}

}
