package edu.umich.med.mrc2.datoolbox.gui.owl.graph;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataAllValuesFrom;
import org.semanticweb.owlapi.model.OWLDataExactCardinality;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectExactCardinality;
import org.semanticweb.owlapi.model.OWLObjectHasSelf;
import org.semanticweb.owlapi.model.OWLObjectHasValue;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectOneOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

/**
 * Tools to handle cardinality constraints in OWL.
 */
public class CardinalityContraintsTools {

	/**
	 * Remove the cardinality constraints, by removing the axioms and replacing
	 * them with the weaker axioms without the constraints.
	 * 
	 * @param ontology
	 */
	public static void removeCardinalityConstraints(OWLOntology ontology) {
		CardinalityRemover remover = new CardinalityRemover(ontology);
		for(OWLAxiom axiom : ontology.getAxioms()) {
			axiom.accept(remover);
		}
	}
	
	/**
	 * Find the axioms with cardinality constraints. Creates also the weaker
	 * axioms as potential replacement.
	 * 
	 * @param ontology
	 * @return reporter with the axiom potential changes
	 */
	public static CardinalityReporter findCardinalityConstraints(OWLOntology ontology) {
		CardinalityReporter reporter = new CardinalityReporter(ontology);
		for(OWLAxiom axiom : ontology.getAxioms()) {
			axiom.accept(reporter);
		}
		return reporter;
	}
	
	public static class CardinalityReporter extends CardinalityHandler {
		
		private final Set<OWLAxiom> removed = new HashSet<OWLAxiom>();
		private final Set<OWLAxiom> added = new HashSet<OWLAxiom>();

		CardinalityReporter(OWLOntology ontology) {
			super(ontology);
		}

		@Override
		protected void remove(OWLOntology ontology, OWLAxiom axiom) {
			removed.add(axiom);
		}

		@Override
		protected void add(OWLOntology ontology, OWLAxiom axiom) {
			added.add(axiom);
		}

		/**
		 * @return the removed
		 */
		public final Set<OWLAxiom> getRemoved() {
			return Collections.unmodifiableSet(removed);
		}

		/**
		 * @return the added
		 */
		public final Set<OWLAxiom> getAdded() {
			return Collections.unmodifiableSet(added);
		}
		
	}
	
	static class CardinalityRemover extends CardinalityHandler {
		
		private final OWLOntologyManager manager;

		public CardinalityRemover(OWLOntology ontology) {
			super(ontology);
			manager = ontology.getOWLOntologyManager();
		}

		@Override
		protected void remove(OWLOntology ontology, OWLAxiom axiom) {
			manager.removeAxiom(ontology, axiom);
		}

		@Override
		protected void add(OWLOntology ontology, OWLAxiom axiom) {
			manager.addAxiom(ontology, axiom);
		}
		
	}
	
	/**
	 * Visitor pattern. Abstract class to find axioms containing cardinality
	 * constraints. Currently only done for {@link OWLSubClassOfAxiom} and
	 * {@link OWLEquivalentClassesAxiom}.
	 */
	abstract static class CardinalityHandler implements OWLAxiomVisitor {
		
		private final OWLDataFactory factory;
		private final OWLOntology ontology;
		
		private final CardinalityExpressionHandler handler;
		
		public CardinalityHandler(OWLOntology ontology) {
			this.ontology = ontology;
			OWLOntologyManager manager = ontology.getOWLOntologyManager();
			factory = manager.getOWLDataFactory();
			handler = new CardinalityExpressionHandler(factory);
		}
		
		protected abstract void remove(OWLOntology ontology, OWLAxiom axiom);
		
		protected abstract void add(OWLOntology ontology, OWLAxiom axiom);

		@Override
		public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		}

		@Override
		public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
		}

		@Override
		public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
		}

		@Override
		public void visit(OWLSubClassOfAxiom axiom) {
			OWLClassExpression subClass = axiom.getSubClass();
			OWLClassExpression superClass = axiom.getSuperClass();
			HandlerResult modifiedSubClass = subClass.accept(handler);
			HandlerResult modifiedSuperClass = superClass.accept(handler);
			if (modifiedSubClass != null || modifiedSuperClass != null) {
				if (modifiedSubClass != null) {
					if (modifiedSubClass.remove) {
						remove(ontology, axiom);
						return;
					}
					subClass = modifiedSubClass.modified;
				}
				if (modifiedSuperClass != null) {
					if (modifiedSuperClass.remove) {
						remove(ontology, axiom);
						return;
					}
					superClass = modifiedSuperClass.modified;
				}
				remove(ontology, axiom);
				OWLSubClassOfAxiom newAxiom = factory.getOWLSubClassOfAxiom(subClass, superClass, axiom.getAnnotations());
				add(ontology, newAxiom);
			}
		}

		@Override
		public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		}

		@Override
		public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		}

		@Override
		public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		}

		@Override
		public void visit(OWLDisjointClassesAxiom axiom) {
		}

		@Override
		public void visit(OWLDataPropertyDomainAxiom axiom) {
		}

		@Override
		public void visit(OWLObjectPropertyDomainAxiom axiom) {
		}

		@Override
		public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		}

		@Override
		public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		}

		@Override
		public void visit(OWLDifferentIndividualsAxiom axiom) {
		}

		@Override
		public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		}

		@Override
		public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		}

		@Override
		public void visit(OWLObjectPropertyRangeAxiom axiom) {
		}

		@Override
		public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		}

		@Override
		public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		}

		@Override
		public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		}

		@Override
		public void visit(OWLDisjointUnionAxiom axiom) {
		}

		@Override
		public void visit(OWLDeclarationAxiom axiom) {
		}

		@Override
		public void visit(OWLAnnotationAssertionAxiom axiom) {
		}

		@Override
		public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		}

		@Override
		public void visit(OWLDataPropertyRangeAxiom axiom) {
		}

		@Override
		public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		}

		@Override
		public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		}

		@Override
		public void visit(OWLClassAssertionAxiom axiom) {
		}

		@Override
		public void visit(OWLEquivalentClassesAxiom axiom) {
			Set<OWLClassExpression> newExpressions = new HashSet<OWLClassExpression>();
			boolean changed = false;
			for (OWLClassExpression ce : axiom.getClassExpressions()) {
				HandlerResult result = ce.accept(handler);
				if (result != null) {
					if (result.remove) {
						// skip handling and immediately remove and return
						remove(ontology, axiom);
						return;
					}
					changed = true;
					newExpressions.add(result.modified);
				}
				else {
					newExpressions.add(ce);
				}
			}
			if (changed) {
				remove(ontology, axiom);
				OWLEquivalentClassesAxiom newAxiom = factory.getOWLEquivalentClassesAxiom(newExpressions, axiom.getAnnotations());
				add(ontology, newAxiom);
			}
		}

		@Override
		public void visit(OWLDataPropertyAssertionAxiom axiom) {
		}

		@Override
		public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		}

		@Override
		public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		}

		@Override
		public void visit(OWLSubDataPropertyOfAxiom axiom) {
		}

		@Override
		public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		}

		@Override
		public void visit(OWLSameIndividualAxiom axiom) {
		}

		@Override
		public void visit(OWLSubPropertyChainOfAxiom axiom) {
		}

		@Override
		public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		}

		@Override
		public void visit(OWLHasKeyAxiom axiom) {
		}

		@Override
		public void visit(OWLDatatypeDefinitionAxiom axiom) {
		}

		@Override
		public void visit(SWRLRule rule) {
		}
	}
	
	/**
	 * Used my {@link CardinalityExpressionHandler} to communicate the required
	 * changes for an {@link OWLClassExpression}.<br>
	 * A null value indicates no change, if remove is set to true, remove the
	 * element. Otherwise replace the current one with the modified value.
	 */
	static class HandlerResult {
		
		OWLClassExpression modified;
		boolean remove;
		
		static HandlerResult modified(OWLClassExpression modified) {
			final HandlerResult result = new HandlerResult();
			result.modified = modified;
			return result;
		}
		
		static HandlerResult remove() {
			final HandlerResult result = new HandlerResult();
			result.remove = true;
			return result;
		}
	}
	
	/**
	 * Visitor pattern. Recursive check of {@link OWLClassExpression}s for cardinality constraints.
	 * Replace with inner {@link OWLClassExpression}.<br>
	 * Note: max cardinality = 0 and exact cardinality = 0 are special cases. 
	 *  
	 * @see HandlerResult for return value.
	 */
	static class CardinalityExpressionHandler implements OWLClassExpressionVisitorEx<HandlerResult> {
		
		private final OWLDataFactory factory;

		public CardinalityExpressionHandler(OWLDataFactory factory) {
			this.factory = factory;
		}
		
		@Override
		public HandlerResult visit(OWLClass ce) {
			return null;
		}

		@Override
		public HandlerResult visit(OWLObjectIntersectionOf intersectionOf) {
			Set<OWLClassExpression> newOperands = new HashSet<OWLClassExpression>();
			boolean changed = false;
			for (OWLClassExpression ce : intersectionOf.getOperands()) {
				HandlerResult handlerResult = ce.accept(this);
				if (handlerResult != null) {
					if (handlerResult.remove) {
						return HandlerResult.remove();
					}
					changed = true;
					newOperands.add(handlerResult.modified);
				}
				else {
					newOperands.add(ce);
				}
			}
			if (changed) {
				if (newOperands.size() == 1) {
					return HandlerResult.modified(newOperands.iterator().next());
				}
				return HandlerResult.modified(factory.getOWLObjectIntersectionOf(newOperands));
				
			}
			return null;
		}

		@Override
		public HandlerResult visit(OWLObjectUnionOf unionOf) {
			Set<OWLClassExpression> newOperands = new HashSet<OWLClassExpression>();
			boolean changed = false;
			for (OWLClassExpression ce : unionOf.getOperands()) {
				HandlerResult handlerResult = ce.accept(this);
				if (handlerResult != null) {
					if (handlerResult.remove) {
						return HandlerResult.remove();
					}
					changed = true;
					newOperands.add(handlerResult.modified);
				}
				else {
					newOperands.add(ce);
				}
			}
			if (changed) {
				if (newOperands.size() == 1) {
					return HandlerResult.modified(newOperands.iterator().next());
				}
				return HandlerResult.modified(factory.getOWLObjectUnionOf(newOperands));
			}
			return null;
		}

		@Override
		public HandlerResult visit(OWLObjectComplementOf ce) {
			return ce.getOperand().accept(this);
		}

		@Override
		public HandlerResult visit(OWLObjectSomeValuesFrom ce) {
			return ce.getFiller().accept(this);
		}

		@Override
		public HandlerResult visit(OWLObjectAllValuesFrom ce) {
			return ce.getFiller().accept(this);
		}

		@Override
		public HandlerResult visit(OWLObjectHasValue ce) {
			return null;
		}

		@Override
		public HandlerResult visit(OWLObjectMinCardinality ce) {
			final OWLClassExpression filler = ce.getFiller();
			final HandlerResult recursive = filler.accept(this);
			OWLObjectSomeValuesFrom newCE;
			if (recursive == null) {
				newCE = factory.getOWLObjectSomeValuesFrom(ce.getProperty(), filler);
			}
			else if (recursive.remove) {
				return HandlerResult.remove();
			}
			else {
				newCE = factory.getOWLObjectSomeValuesFrom(ce.getProperty(), recursive.modified);
			}
			return HandlerResult.modified(newCE);
		}

		@Override
		public HandlerResult visit(OWLObjectExactCardinality ce) {
			if (ce.getCardinality() == 0) {
				// remove the ce if the max cardinality is zero
				return HandlerResult.remove();
			}
			final OWLClassExpression filler = ce.getFiller();
			final HandlerResult recursive = filler.accept(this);
			OWLObjectSomeValuesFrom newCE;
			if (recursive == null) {
				newCE = factory.getOWLObjectSomeValuesFrom(ce.getProperty(), filler);
			}
			else if (recursive.remove) {
				return HandlerResult.remove();
			}
			else {
				newCE = factory.getOWLObjectSomeValuesFrom(ce.getProperty(), recursive.modified);
			}
			return HandlerResult.modified(newCE);
		}

		@Override
		public HandlerResult visit(OWLObjectMaxCardinality ce) {
			if (ce.getCardinality() == 0) {
				// remove the ce if the max cardinality is zero
				return HandlerResult.remove();
			}
			final OWLClassExpression filler = ce.getFiller();
			final HandlerResult recursive = filler.accept(this);
			OWLObjectSomeValuesFrom newCE;
			if (recursive == null) {
				newCE = factory.getOWLObjectSomeValuesFrom(ce.getProperty(), filler);
			}
			else if (recursive.remove) {
				return HandlerResult.remove();
			}
			else {
				newCE = factory.getOWLObjectSomeValuesFrom(ce.getProperty(), recursive.modified);
			}
			return HandlerResult.modified(newCE);
		}

		@Override
		public HandlerResult visit(OWLObjectHasSelf ce) {
			return null;
		}

		@Override
		public HandlerResult visit(OWLObjectOneOf ce) {
			return null;
		}

		@Override
		public HandlerResult visit(OWLDataSomeValuesFrom ce) {
			return null;
		}

		@Override
		public HandlerResult visit(OWLDataAllValuesFrom ce) {
			return null;
		}

		@Override
		public HandlerResult visit(OWLDataHasValue ce) {
			return null;
		}

		@Override
		public HandlerResult visit(OWLDataMinCardinality ce) {
			return null;
		}

		@Override
		public HandlerResult visit(OWLDataExactCardinality ce) {
			return null;
		}

		@Override
		public HandlerResult visit(OWLDataMaxCardinality ce) {
			return null;
		}
		
	}
}
