package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph;

import java.awt.Shape;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLObject;

import edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph.LinkDatabase.Link;
import edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph.tooltip.TooltipFactory;

public class DefaultNodeFactory implements NodeFactory {

	//initialize logger
	protected final static Logger logger = Logger.getLogger(DefaultNodeFactory.class);

	private final TypeIconManager iconManager;
	private final TypeColorManager colorManager;
	private final NodeLabelProvider labelProvider;
	private final TooltipFactory tooltipFactory;

	public DefaultNodeFactory(TypeIconManager iconManager, TypeColorManager colorManager, 
			NodeLabelProvider labelProvider, TooltipFactory tooltipFactory) {
		super();
		this.iconManager = iconManager;
		this.colorManager = colorManager;
		this.labelProvider = labelProvider;
		this.tooltipFactory = tooltipFactory;
	}

	@Override
	public OELink createLink(Link link, Shape s) {
		OELink node = new OELink(link, iconManager, colorManager, s);
		node.setTooltipFactory(tooltipFactory);
		return node;
	}

	@Override
	public OENode createNode(OWLObject obj, Shape s) {
		OENode node = new OENode(obj, labelProvider, s);
		return node;
	}

}
