package edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph.tooltip;


import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import edu.umd.cs.piccolo.PNode;
import edu.umich.med.mrc2.datoolbox.gui.owl.graph.OWLGraphWrapper;
import edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph.LinkDatabase;
import edu.umich.med.mrc2.datoolbox.gui.owl.ogv.graph.OELink;
import edu.umich.med.mrc2.datoolbox.gui.owl.ogv.piccolo.ViewRenderedStyleText;

public class LinkTooltipFactory extends AbstractTooltipFactory {

	private final OWLGraphWrapper graph;

	public LinkTooltipFactory(OWLGraphWrapper graph) {
		this.graph = graph;
	}

	ViewRenderedStyleText text = new ViewRenderedStyleText();

	@Override
	public PNode getTooltip(PNode node) {
		if (node instanceof OELink) {
			LinkDatabase.Link link = ((OELink) node).getLink();
			if (link != null) {
				StringBuilder html = new StringBuilder();
				html.append("<html>\n<body>\n");
				html.append("<table>");
				OWLObject source = link.getSource();
				html.append("<tr><td><b>Source</b></td><td>");
				html.append(graph.getLabelOrDisplayId(source));
				html.append("</td>");
				
				OWLObject target = link.getTarget();
				html.append("<tr><td><b>Target</b></td><td>");
				html.append(graph.getLabelOrDisplayId(target));
				html.append("</td>");
				
				OWLObjectProperty property = link.getProperty();
				html.append("<tr><td><b>Type</b></td><td>");
				if (property == null) {
					html.append("subClassOf");
				}
				else {
					html.append(graph.getLabelOrDisplayId(property));
				}
				html.append("</td>");
				
				html.append("</table></body></html>");
//				text.setWidth(canvas.getWidth() * .6);
				text.setText(html.toString(), true);
				return text;
			}
		}
		return null;
	}

}
