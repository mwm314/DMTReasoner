import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.change.OntologyAnnotationChangeData;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLDataPropertyNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNode;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNodeSet;
import org.semanticweb.owlapi.util.Version;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

public class DMTReasoner implements OWLReasoner, OWLOntologyChangeListener {
	// List of class variables
	/**
	 * The ontology we are reasoning over
	 */
	private OWLOntology ontology;

	// DAGS for our class and property hierarchies. See here for why we need them:
	// http://owlapi.sourceforge.net/javadoc/org/semanticweb/owlapi/reasoner/OWLReasoner.html
	private DirectedAcyclicGraph<Node<OWLClass>, DefaultEdge> classNodeHierarchy = new DirectedAcyclicGraph<Node<OWLClass>, DefaultEdge>(DefaultEdge.class);
	private DirectedAcyclicGraph<Node<OWLDataProperty>, DefaultEdge> dataPropertyNodeHierarchy = new DirectedAcyclicGraph<Node<OWLDataProperty>, DefaultEdge>(DefaultEdge.class);
	// I'm not sure why, but it seems as if this interface is conducive
	// Node<OWLObjectPropertyExpression>, but I feel like they should be
	// Node<OWLObjectProperty>.
	private DirectedAcyclicGraph<Node<OWLObjectPropertyExpression>, DefaultEdge> objectPropertyNodeHierarchy = new DirectedAcyclicGraph<Node<OWLObjectPropertyExpression>, DefaultEdge>(DefaultEdge.class);

	// A NodeSet representing the individuals
	private OWLNamedIndividualNodeSet individuals = new OWLNamedIndividualNodeSet();

	private BufferingMode bufferingMode = BufferingMode.BUFFERING;

	// Axioms added
	private Set<OWLAxiom> additions = new HashSet<OWLAxiom>();

	// Axioms removed
	private Set<OWLAxiom> removals = new HashSet<OWLAxiom>();

	// Given axioms from the ontology
	private Set<OWLAxiom> axioms;

	// These have to be attached to a hierarchy of some kind
	/*
	 * private OWLClassNode bottomClassNode = OWLClassNode.getBottomNode(); private OWLDataPropertyNode bottomDataPropertyNode = OWLDataPropertyNode.getBottomNode(); private OWLObjectPropertyNode bottomObjectPropertyNode = OWLObjectPropertyNode.getBottomNode();
	 * 
	 * private OWLClassNode topClassNode = OWLClassNode.getTopNode(); private OWLDataPropertyNode topDataPropertyNode = OWLDataPropertyNode.getTopNode(); private OWLObjectPropertyNode topObjectPropertyNode = OWLObjectPropertyNode.getTopNode();
	 */

	/**
	 * Constructor for DMTReasoner
	 */
	DMTReasoner(OWLOntology ontology) {
		this.ontology = ontology;
		axioms = ontology.getAxioms();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void flush() {
		for (OWLAxiom i : removals) {
			axioms.remove(i);
		}
		for (OWLAxiom i : additions) {
			axioms.add(i);
		}

	}

	@Override
	/**
	 * Returns the bottom class node from our classNodeHierarchy.
	 * This node is the node without any incoming edges
	 * @return
	 */
	public Node<OWLClass> getBottomClassNode() {
		Iterator<Node<OWLClass>> iter = classNodeHierarchy.iterator();
		while (iter.hasNext()) {

			Node<OWLClass> currentNode = iter.next();
			Set<DefaultEdge> edgeSet = classNodeHierarchy.incomingEdgesOf(currentNode);

			if (edgeSet.isEmpty()) {
				// The bottom node should not have any incoming edges, so return this node
				return currentNode;
			}

		}
		// We should never get here if our hierarchy is implemented correctly
		return null;
	}

	@Override
	/**
	 * Returns the bottom data property node from our dataPropertyNodeHierarchy
	 * This node is the node without any incoming edges
	 */
	public Node<OWLDataProperty> getBottomDataPropertyNode() {
		Iterator<Node<OWLDataProperty>> iter = dataPropertyNodeHierarchy.iterator();
		while (iter.hasNext()) {

			Node<OWLDataProperty> currentNode = iter.next();
			Set<DefaultEdge> edgeSet = dataPropertyNodeHierarchy.incomingEdgesOf(currentNode);

			if (edgeSet.isEmpty()) {
				// The bottom node should not have any incoming edges, so return this node
				return currentNode;
			}

		}
		// We should never get here if our hierarchy is implemented correctly
		return null;
	}

	@Override
	/**
	 * Returns the bottom data property node from our objectPropertyNodeHierarchy
	 * This node is the node without any incoming edges
	 */
	public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
		// Should this return Node<OWLObjectProperty>?! Confusing...
		Iterator<Node<OWLObjectPropertyExpression>> iter = objectPropertyNodeHierarchy.iterator();
		while (iter.hasNext()) {

			Node<OWLObjectPropertyExpression> currentNode = iter.next();
			Set<DefaultEdge> edgeSet = objectPropertyNodeHierarchy.incomingEdgesOf(currentNode);

			if (edgeSet.isEmpty()) {
				// The bottom node should not have any incoming edges, so return this node
				return currentNode;
			}

		}
		// We should never get here if our hierarchy is implemented correctly
		return null;
	}

	@Override
	public BufferingMode getBufferingMode() {
		return bufferingMode;
	}

	@Override
	public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty dataProperty, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual individual, OWLDataProperty dataProperty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	/**
	 * Individuals are represented by the individuals node set. We return the NodeSet of all individual Nodes
	 * except for the node with the given individual. Same individuals are located in the same node.
	 * Returns null if the individual is not anywhere in the NodeSet of individuals
	 * @param individual
	 * @return
	 */
	public NodeSet<OWLNamedIndividual> getDifferentIndividuals(OWLNamedIndividual individual) {

		Iterator<Node<OWLNamedIndividual>> iter = individuals.iterator();
		OWLNamedIndividualNodeSet instances = new OWLNamedIndividualNodeSet();

		while (iter.hasNext()) {
			Node<OWLNamedIndividual> currentNode = iter.next();
			if (!currentNode.contains(individual)) {
				instances.addNode(currentNode);
			}
		}
		return instances;
	}

	@Override
	public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression owlClassExpr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression dataPropExpr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(OWLObjectPropertyExpression objectPropExpr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	/**
	 * Get the classes from our classNodeHierarchy which are equivalent to the given class expression.
	 */
	public Node<OWLClass> getEquivalentClasses(OWLClassExpression classExpr) {
		if (classExpr.isAnonymous()) {
			// TODO Tougher to deal with this, need to reason about anonymous class expressions.
			return null;
		}
		else {
			// If it is not anonymous, it must be a class we already have
			OWLClass owlclass = classExpr.asOWLClass();
			Iterator<Node<OWLClass>> iter = classNodeHierarchy.iterator();

			while (iter.hasNext()) {
				Node<OWLClass> currentClassNode = iter.next();
				if (currentClassNode.contains(owlclass)) {
					return currentClassNode;
				}
			}

			return null;

		}
	}

	@Override
	public Node<OWLDataProperty> getEquivalentDataProperties(OWLDataProperty dataProp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(OWLObjectPropertyExpression objectPropExpr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	/**
	 * Not entirely sure how "fresh entities" are defined. For now, we will disallow them
	 */
	public FreshEntityPolicy getFreshEntityPolicy() {
		return FreshEntityPolicy.DISALLOW;
	}

	@Override
	/**
	 * This means that if two individuals are marked as being owl:sameAs, we group them into the same node.
	 * So, if i,j,k are individuals all of class C, i owl:sameAs j, and we want to return all instances of C, then we will return a node set
	 * with two nodes, one node with i and j, and the other node with k.
	 */
	public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
		return IndividualNodeSetPolicy.BY_SAME_AS;
	}

	@Override
	public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	/**
	 * We do not handle inverse properties, so the DTMReasoner object will always throw an exception when this method is called
	 * @param arg0
	 * @return
	 */
	public Node<OWLObjectPropertyExpression> getInverseObjectProperties(OWLObjectPropertyExpression arg0) {
		//Throw exception here
		return null;
	}

	@Override
	public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLClass> getObjectPropertyRanges(OWLObjectPropertyExpression arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLNamedIndividual> getObjectPropertyValues(OWLNamedIndividual arg0, OWLObjectPropertyExpression arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLAxiom> getPendingAxiomAdditions() {
		return additions;
	}

	@Override
	public Set<OWLAxiom> getPendingAxiomRemovals() {
		return removals;
	}

	@Override
	public List<OWLOntologyChange> getPendingChanges() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<InferenceType> getPrecomputableInferenceTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getReasonerName() {
		return "DMT (Dan, Matt, Tyler) Reasoner";
	}

	@Override
	public Version getReasonerVersion() {
		return new Version(1, 1, 1, 1);
	}

	@Override
	public OWLOntology getRootOntology() {
		return ontology;
	}

	@Override
	/**
	 * Individuals are represented by the individuals node set. We return the Node of individuals that
	 * is contains the specified individual. Same individuals are located in the same node.
	 * Returns null if the individual is not anywhere in the NodeSet of individuals
	 * @param individual
	 * @return
	 */
	public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual individual) {
		Iterator<Node<OWLNamedIndividual>> iter = individuals.iterator();
		while (iter.hasNext()) {
			Node<OWLNamedIndividual> currentNode = iter.next();
			if (currentNode.contains(individual)) {
				return currentNode;
			}
		}
		return null;
	}

	@Override
	public NodeSet<OWLClass> getSubClasses(OWLClassExpression classExpression, boolean direct) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	/**
	 * Returns the sub dataProperties of the specified dataProperty.
	 * @param direct
	 * If direct is true, then we only grab the direct sub dataProperties (i.e. properties only one edge away in our data prop hierarchy)
	 */
	public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty dataProperty, boolean direct) {

		OWLDataPropertyNodeSet instances = new OWLDataPropertyNodeSet();
		Iterator<Node<OWLDataProperty>> iter = dataPropertyNodeHierarchy.iterator();

		while (iter.hasNext()) {
			Node<OWLDataProperty> currentNode = iter.next();
			if (currentNode.contains(dataProperty)) {

				if (direct) {

					Set<DefaultEdge> incomingEdges = dataPropertyNodeHierarchy.incomingEdgesOf(currentNode);
					Iterator<DefaultEdge> edgeIter = incomingEdges.iterator();

					while (edgeIter.hasNext()) {
						DefaultEdge currentEdge = edgeIter.next();
						Node<OWLDataProperty> dataPropertyNode = dataPropertyNodeHierarchy.getEdgeSource(currentEdge);
						instances.addNode(dataPropertyNode);
					}
					return instances;

				}
				else {

					return getSubDataPropsRecursively(currentNode, instances);

				}
			}
		}
		// This means the specified data property was not in our data property hierarchy
		return null;
	}

	/**
	 * Method recursively rolls through the dataPropertyNodeHierarchy
	 * 
	 * @param currentNode
	 *            The node that we want to get a list of all subnodes for
	 * @param instances
	 *            Helper parameter to keep track of the nodes we have already added
	 * @return
	 */
	private OWLDataPropertyNodeSet getSubDataPropsRecursively(Node<OWLDataProperty> currentNode, OWLDataPropertyNodeSet instances) {

		if (!instances.containsEntity(currentNode.getRepresentativeElement())) {
			instances.addNode(currentNode);
		}

		Iterator<Node<OWLDataProperty>> iter = dataPropertyNodeHierarchy.iterator();

		// This could probably be a bit more efficient, but *should* work
		while (iter.hasNext()) {

			Set<DefaultEdge> incomingEdges = dataPropertyNodeHierarchy.incomingEdgesOf(currentNode);
			Iterator<DefaultEdge> edgeIter = incomingEdges.iterator();

			while (edgeIter.hasNext()) {
				DefaultEdge currentEdge = edgeIter.next();
				Node<OWLDataProperty> dataPropertyNode = dataPropertyNodeHierarchy.getEdgeSource(currentEdge);
				getSubDataPropsRecursively(dataPropertyNode, instances);
			}

		}

		return instances;
	}

	@Override
	public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(OWLObjectPropertyExpression objectPropExpression, boolean direct) {

		OWLObjectPropertyNodeSet instances = new OWLObjectPropertyNodeSet();
		Iterator<Node<OWLObjectPropertyExpression>> iter = objectPropertyNodeHierarchy.iterator();

		while (iter.hasNext()) {
			Node<OWLObjectPropertyExpression> currentNode = iter.next();
			if (currentNode.contains(objectPropExpression)) {

				if (direct) {

					Set<DefaultEdge> incomingEdges = objectPropertyNodeHierarchy.incomingEdgesOf(currentNode);
					Iterator<DefaultEdge> edgeIter = incomingEdges.iterator();

					while (edgeIter.hasNext()) {
						DefaultEdge currentEdge = edgeIter.next();
						Node<OWLObjectPropertyExpression> objectPropertyNode = objectPropertyNodeHierarchy.getEdgeSource(currentEdge);
						instances.addNode(objectPropertyNode);
					}
					return instances;

				}
				else {

					return getSubObjectPropsRecursively(currentNode, instances);

				}
			}
		}
		// This means the specified data property was not in our data property hierarchy
		return null;
	}

	/**
	 * Method recursively rolls through the dataPropertyNodeHierarchy
	 * 
	 * @param currentNode
	 *            The node that we want to get a list of all subnodes for
	 * @param instances
	 *            Helper parameter to keep track of the nodes we have already added
	 * @return
	 */
	private NodeSet<OWLObjectPropertyExpression> getSubObjectPropsRecursively(Node<OWLObjectPropertyExpression> currentNode, OWLObjectPropertyNodeSet instances) {

		if (!instances.containsEntity(currentNode.getRepresentativeElement())) {
			instances.addNode(currentNode);
		}

		Iterator<Node<OWLObjectPropertyExpression>> iter = objectPropertyNodeHierarchy.iterator();

		// This could probably be a bit more efficient, but *should* work
		while (iter.hasNext()) {

			Set<DefaultEdge> incomingEdges = objectPropertyNodeHierarchy.incomingEdgesOf(currentNode);
			Iterator<DefaultEdge> edgeIter = incomingEdges.iterator();

			while (edgeIter.hasNext()) {
				DefaultEdge currentEdge = edgeIter.next();
				Node<OWLObjectPropertyExpression> objectPropertyNode = objectPropertyNodeHierarchy.getEdgeSource(currentEdge);
				getSubObjectPropsRecursively(objectPropertyNode, instances);
			}

		}

		return instances;
	}

	@Override
	public NodeSet<OWLClass> getSuperClasses(OWLClassExpression arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLDataProperty> getSuperDataProperties(OWLDataProperty arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(OWLObjectPropertyExpression arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTimeOut() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	/**
	 * Returns the top class node from our classNodeHierarchy.
	 * This node is the node without any outgoing edges
	 * @return
	 */
	public Node<OWLClass> getTopClassNode() {
		Iterator<Node<OWLClass>> iter = classNodeHierarchy.iterator();
		while (iter.hasNext()) {

			Node<OWLClass> currentNode = iter.next();
			Set<DefaultEdge> edgeSet = classNodeHierarchy.outgoingEdgesOf(currentNode);

			if (edgeSet.isEmpty()) {
				// The bottom node should not have any outgoing edges, so return this node
				return currentNode;
			}

		}
		// We should never get here if our hierarchy is implemented correctly
		return null;
	}

	@Override
	/**
	 * Returns the top class node from our dataPropertyNodeHierarchy.
	 * This node is the node without any outgoing edges
	 * @return
	 */
	public Node<OWLDataProperty> getTopDataPropertyNode() {
		Iterator<Node<OWLDataProperty>> iter = dataPropertyNodeHierarchy.iterator();
		while (iter.hasNext()) {

			Node<OWLDataProperty> currentNode = iter.next();
			Set<DefaultEdge> edgeSet = dataPropertyNodeHierarchy.outgoingEdgesOf(currentNode);

			if (edgeSet.isEmpty()) {
				// The bottom node should not have any outgoing edges, so return this node
				return currentNode;
			}

		}
		// We should never get here if our hierarchy is implemented correctly
		return null;
	}

	@Override
	/**
	 * Returns the top class node from our objectPropertyNodeHierarchy.
	 * This node is the node without any outgoing edges
	 * @return
	 */
	public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
		Iterator<Node<OWLObjectPropertyExpression>> iter = objectPropertyNodeHierarchy.iterator();
		while (iter.hasNext()) {

			Node<OWLObjectPropertyExpression> currentNode = iter.next();
			Set<DefaultEdge> edgeSet = objectPropertyNodeHierarchy.outgoingEdgesOf(currentNode);

			if (edgeSet.isEmpty()) {
				// The bottom node should not have any outgoing edges, so return this node
				return currentNode;
			}

		}
		// We should never get here if our hierarchy is implemented correctly
		return null;
	}

	@Override
	public NodeSet<OWLClass> getTypes(OWLNamedIndividual arg0, boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	/**
	 * Just return our bottom class node
	 * @return
	 */
	public Node<OWLClass> getUnsatisfiableClasses() {
		return getBottomClassNode();
	}

	@Override
	public void interrupt() {
		// TODO Auto-generated method stub

	}

	@Override
	/**
	 * In order to determine consistency, we check if our DAG contains only a topNode and bottomNode, and if the topNode is a singleton.
	 * @return
	 */
	public boolean isConsistent() {
		//If there is an edge between the top and bottom class nodes, then there are just two nodes
		if (classNodeHierarchy.containsEdge(getTopClassNode(), getBottomClassNode())) {
			if (getTopClassNode().isSingleton()) {
				if (!getBottomClassNode().isSingleton()) {
					//If all classes are in the bottomClassNode, return true.
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isEntailed(OWLAxiom arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEntailed(Set<? extends OWLAxiom> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEntailmentCheckingSupported(AxiomType<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isPrecomputed(InferenceType arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSatisfiable(OWLClassExpression arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void precomputeInferences(InferenceType... arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void ontologiesChanged(List<? extends OWLOntologyChange> list) throws OWLException {
		for (OWLOntologyChange i : list) {
			if (i.getOntology().equals(ontology)) {
				if (bufferingMode.equals(BufferingMode.BUFFERING)) {
					if (i.isAddAxiom()) {
						additions.add(i.getAxiom());
					}
					else if (i.isRemoveAxiom()) {
						removals.add(i.getAxiom());
					}
				}
				else {
					axioms = ontology.getAxioms();
				}
			}
		}
	}

}
