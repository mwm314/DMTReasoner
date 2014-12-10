package main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.*;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLDataPropertyNode;
import org.semanticweb.owlapi.reasoner.impl.OWLDataPropertyNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNode;
import org.semanticweb.owlapi.reasoner.impl.OWLObjectPropertyNodeSet;
import org.semanticweb.owlapi.util.Version;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

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
    public DMTReasoner(OWLOntology ontology) {
        this.ontology = ontology;
        axioms = ontology.getAxioms();
        reason();
    }

    /*
     * ONLY FOR TESTING. Get rid of this eventually.
     */
    public DMTReasoner() {
    }

    public void setClassNodeHierarchy(DirectedAcyclicGraph<Node<OWLClass>, DefaultEdge> classNodeHierarchy) {
        this.classNodeHierarchy = classNodeHierarchy;
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
        reason();

    }

    /**
     * Returns the bottom class node from our classNodeHierarchy. This node is
     * the node without any incoming edges
     *
     * @return
     */
    @Override
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

    /**
     * Returns the bottom data property node from our dataPropertyNodeHierarchy
     * This node is the node without any incoming edges
     */
    @Override
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

    /**
     * Returns the bottom data property node from our
     * objectPropertyNodeHierarchy This node is the node without any incoming
     * edges
     */
    @Override
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

    /**
     * Individuals are represented by the individuals node set. We return the
     * NodeSet of all individual Nodes except for the node with the given
     * individual. Same individuals are located in the same node. Returns null
     * if the individual is not anywhere in the NodeSet of individuals
     *
     * @param individual
     * @return
     */
    @Override
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
    /**
     * Returns a NodeSet with one node representing the disjoint classes.
     *
     */
    public NodeSet<OWLClass> getDisjointClasses(OWLClassExpression owlClassExpr) {
        if (!owlClassExpr.isAnonymous()) {

            //Get all the disjoint classes
            OWLClassNodeSet allDisjointClasses = new OWLClassNodeSet();
            for (OWLAxiom a : axioms) {
                if (a.getAxiomType() == AxiomType.DISJOINT_CLASSES) {
                    OWLClassNode node = new OWLClassNode();
                    for (OWLClass c : a.getClassesInSignature()) {
                        node.add(c);
                    }
                    allDisjointClasses.addNode(node);
                }
            }

            for (Node<OWLClass> n : allDisjointClasses) {
                if (n.contains(owlClassExpr.asOWLClass())) {
                    OWLClassNodeSet disjointFromExpr = new OWLClassNodeSet();
                    disjointFromExpr.addNode(n);
                    return disjointFromExpr;
                }
            }

            //No disjoint classes found
            return new OWLClassNodeSet();
        } //TODO Partial implementation
        else {
            throw new DMTDoesNotSupportException("Only grab disjoint classes for a given owl class, not owlclass expression");
        }
    }

    @Override
    /**
     * Returns a NodeSet such that every class in a given node are disjoint from
     * one another
     */
    public NodeSet<OWLDataProperty> getDisjointDataProperties(OWLDataPropertyExpression dataPropExpr) {
        if (!dataPropExpr.isAnonymous()) {

            //Get all the disjoint classes
            OWLDataPropertyNodeSet allDisjointDataProperties = new OWLDataPropertyNodeSet();
            for (OWLAxiom a : axioms) {
                if (a.getAxiomType() == AxiomType.DISJOINT_DATA_PROPERTIES) {
                    OWLDataPropertyNode node = new OWLDataPropertyNode();
                    for (OWLDataProperty c : a.getDataPropertiesInSignature()) {
                        node.add(c);
                    }
                    allDisjointDataProperties.addNode(node);
                }
            }

            for (Node<OWLDataProperty> n : allDisjointDataProperties) {
                if (n.contains(dataPropExpr.asOWLDataProperty())) {
                    OWLDataPropertyNodeSet disjointFromExpr = new OWLDataPropertyNodeSet();
                    disjointFromExpr.addNode(n);
                    return disjointFromExpr;
                }
            }

            //No disjoint DataPropertyes found
            return new OWLDataPropertyNodeSet();
        } //TODO Partial implementation
        else {
            throw new DMTDoesNotSupportException("Only grab disjoint classes for a given owl dataproperty, not owl dataproperty expression");
        }
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(OWLObjectPropertyExpression objectPropExpr) {
        if (!objectPropExpr.isAnonymous()) {

            //Get all the disjoint classes
            OWLObjectPropertyNodeSet allDisjointObjectProperties = new OWLObjectPropertyNodeSet();
            for (OWLAxiom a : axioms) {
                if (a.getAxiomType() == AxiomType.DISJOINT_OBJECT_PROPERTIES) {
                    OWLObjectPropertyNode node = new OWLObjectPropertyNode();
                    for (OWLObjectProperty c : a.getObjectPropertiesInSignature()) {
                        node.add(c);
                    }
                    allDisjointObjectProperties.addNode(node);
                }
            }

            for (Node<OWLObjectPropertyExpression> n : allDisjointObjectProperties) {
                if (n.contains(objectPropExpr.asOWLObjectProperty())) {
                    OWLObjectPropertyNodeSet disjointFromExpr = new OWLObjectPropertyNodeSet();
                    disjointFromExpr.addNode(n);
                    return disjointFromExpr;
                }
            }

            //No disjoint DataPropertyes found
            return new OWLObjectPropertyNodeSet();
        } //TODO Partial implementation
        else {
            throw new DMTDoesNotSupportException("Only grab disjoint classes for a given owl object property, not owl object property expression");
        }
    }

    /**
     * Get the classes from our classNodeHierarchy which are equivalent to the
     * given class expression.
     */
    @Override
    public Node<OWLClass> getEquivalentClasses(OWLClassExpression classExpr) {
        if (classExpr.isAnonymous()) {
            // TODO Tougher to deal with this, need to reason about anonymous class expressions.
            return null;
        } else {
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

    /**
     * Not entirely sure how "fresh entities" are defined. For now, we will
     * disallow them
     */
    @Override
    public FreshEntityPolicy getFreshEntityPolicy() {
        return FreshEntityPolicy.DISALLOW;
    }

    /**
     * This means that if two individuals are marked as being owl:sameAs, we
     * group them into the same node. So, if i,j,k are individuals all of class
     * C, i owl:sameAs j, and we want to return all instances of C, then we will
     * return a node set with two nodes, one node with i and j, and the other
     * node with k.
     */
    @Override
    public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
        return IndividualNodeSetPolicy.BY_SAME_AS;
    }

    @Override
    public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * We do not handle inverse properties, so the DTMReasoner object will
     * always throw an exception when this method is called
     *
     * @param arg0
     * @return
     */
    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(OWLObjectPropertyExpression arg0) {
        throw new DMTDoesNotSupportException("Inverse Properties not supported.");
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(OWLObjectPropertyExpression pe, boolean direct) {
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

    /**
     * Individuals are represented by the individuals node set. We return the
     * Node of individuals that is contains the specified individual. Same
     * individuals are located in the same node. Returns null if the individual
     * is not anywhere in the NodeSet of individuals
     *
     * @param individual
     * @return
     */
    @Override
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

    /**
     * Returns the sub dataProperties of the specified dataProperty.
     *
     * @param dataProperty
     * @param direct If direct is true, then we only grab the direct sub
     * dataProperties (i.e. properties only one edge away in our data prop
     * hierarchy)
     * @return
     */
    @Override
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

                } else {

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
     * @param currentNode The node that we want to get a list of all subnodes
     * for
     * @param instances Helper parameter to keep track of the nodes we have
     * already added
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

                } else {

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
     * @param currentNode The node that we want to get a list of all subnodes
     * for
     * @param instances Helper parameter to keep track of the nodes we have
     * already added
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

    /**
     * Returns the top class node from our classNodeHierarchy. This node is the
     * node without any outgoing edges
     *
     * @return
     */
    @Override
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

    /**
     * Returns the top class node from our dataPropertyNodeHierarchy. This node
     * is the node without any outgoing edges
     *
     * @return
     */
    @Override
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

    /**
     * Returns the top class node from our objectPropertyNodeHierarchy. This
     * node is the node without any outgoing edges
     *
     * @return
     */
    @Override
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

    /**
     * Just return our bottom class node
     *
     * @return
     */
    @Override
    public Node<OWLClass> getUnsatisfiableClasses() {
        return getBottomClassNode();
    }

    @Override
    public void interrupt() {
        // TODO Auto-generated method stub

    }

    /**
     * In order to determine consistency, we check if our DAG contains only a
     * topNode and bottomNode, and if the topNode is a singleton.
     *
     * @return
     */
    @Override
    public boolean isConsistent() {
        // If there is an edge between the top and bottom class nodes, then there are just two nodes
        if (classNodeHierarchy.containsEdge(getTopClassNode(), getBottomClassNode())) {
            if (getTopClassNode().isSingleton()) {
                if (!getBottomClassNode().isSingleton()) {
                    // If all classes are in the bottomClassNode, return true.
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
    /**
     * My attempt at testing whether a class expression is satisfiable for FL-.
     * So we are assuming no unions or negation of any kind. Just dealing with
     * Exists, ForAll, and Intersection.
     */
    //TODO: Spinning my wheels pretty badly here. Sorry :/. Sleeping on it for tonight.
    //Need to find satisfiability wrt our axioms
    public boolean isSatisfiable(OWLClassExpression classExpr) {
        //axioms

        Node<OWLClass> constraintClasses = new OWLClassNode();
        Node<OWLObjectPropertyExpression> constraintProperties = new OWLObjectPropertyNode();
        if (classExpr.isAnonymous()) {
            // Iterate through this as a set of conjuncts
            Iterator<OWLClassExpression> iter = classExpr.asConjunctSet().iterator();
            while (iter.hasNext()) {
                OWLClassExpression expr = iter.next();
                ClassExpressionType type = expr.getClassExpressionType();
                if (type.equals(ClassExpressionType.OBJECT_ALL_VALUES_FROM)) {
                    OWLObjectAllValuesFrom subExpr = (OWLObjectAllValuesFrom) expr;
					// I assume if we have (ForAll)R.C that C is the filler...but this is not really clear from the documentation
                    // addForAllToConstraintSystem(subExpr.getFiller(), constraintSystem);

                    // OWLObjectProperty prop = subExpr.getProperty().asOWLObjectProperty();
                } else if (type.equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)) {

                } else if (type.equals(ClassExpressionType.OWL_CLASS)) {

                } else {
                    throw new DMTDoesNotSupportException("We only support universal restricitons and limited existential quantification");
                }
            }
            return true;
        } else {
            // If it is not anonymous, we know it is a named class. So, it is satisfiable if it is not in the bottom node
            if (!getBottomClassNode().contains(classExpr.asOWLClass())) {
                return true;
            } else {
                return false;
            }
        }

    }

    /**
     * Recursively add all possible classes to our constraint system
     *
     * @param expr
     * @param cs
     */
    //TODO Linked to problem above, sleeping on it
    private void addForallToConstraintSystem(OWLObjectAllValuesFrom expr, OWLClassNode cs) {
        if (expr.isAnonymous()) {
            Iterator<OWLClassExpression> iter = expr.asConjunctSet().iterator();
            while (iter.hasNext()) {

            }
        } else {
            cs.add(expr.asOWLClass());
        }
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
                    } else if (i.isRemoveAxiom()) {
                        removals.add(i.getAxiom());
                    }
                } else {
                    axioms = ontology.getAxioms();
                }
            }
        }
    }

    private void reason() {
        reasonClasses();
        /*reasonProperties();
         reasonDataproperties();
         */
    }

    private void reasonClasses() {
        Set<OWLClass> classes = ontology.getClassesInSignature();
        ArrayList<Set<OWLSubClassOfAxiom>> classDescriptions = new ArrayList<>();
        ArrayList<Boolean> primitives = new ArrayList<>();
        for (OWLClass c : classes) {
            classDescriptions.add(ontology.getSubClassAxiomsForSubClass(c));
            primitives.add(Boolean.TRUE);
        }
        ArrayList<OWLClass> classArray = new ArrayList<>();
        for (OWLClass c : classes) {
            classArray.add(c);
        }
        for (int i = 0; i < classArray.size(); i++) {
            Set<OWLDisjointClassesAxiom> as = ontology.getDisjointClassesAxioms(classArray.get(i));
            for (OWLDisjointClassesAxiom a : as) {
                for (OWLSubClassOfAxiom b : a.asOWLSubClassOfAxioms()) {
                    if (b.getSubClass().equals(classArray.get(i))) {
                        classDescriptions.get(i).add(b);
                    }
                }
            }
        }
        for (int i = 0; i < classArray.size(); i++) {
            Set<OWLEquivalentClassesAxiom> as = ontology.getEquivalentClassesAxioms(classArray.get(i));
            if (!as.isEmpty()) {
                primitives.set(i, Boolean.FALSE);
            }
            for (OWLEquivalentClassesAxiom a : as) {
                for (OWLSubClassOfAxiom b : a.asOWLSubClassOfAxioms()) {
                    if (b.getSubClass().equals(classArray.get(i))) {
                        classDescriptions.get(i).add(b);
                    }
                }
            }
        }
        for (int i = 0; i < classArray.size(); i++) {
            Set<OWLDisjointUnionAxiom> as = ontology.getDisjointUnionAxioms(classArray.get(i));
            if (!as.isEmpty()) {
                primitives.set(i, Boolean.FALSE);
            }
            for (OWLDisjointUnionAxiom a : as) {
                for (OWLSubClassOfAxiom b : a.getOWLEquivalentClassesAxiom().asOWLSubClassOfAxioms()) {
                    if (b.getSubClass().equals(classArray.get(i))) {
                        classDescriptions.get(i).add(b);
                    }
                }
                for (OWLSubClassOfAxiom b : a.getOWLDisjointClassesAxiom().asOWLSubClassOfAxioms()) {
                    if (b.getSubClass().equals(classArray.get(i))) {
                        classDescriptions.get(i).add(b);
                    }
                }
            }
        }
        for (int i = 0; i < classArray.size(); i++) {
            if (classDescriptions.get(i).isEmpty()) {
                classDescriptions.set(i, new HashSet<OWLSubClassOfAxiom>());
                classDescriptions.get(i).add(new OWLSubClassOfAxiomImpl(classArray.get(i), classArray.get(i), new HashSet<OWLAnnotation>()));
            } else {
                if (primitives.get(i)) {
                    classDescriptions.get(i).add(new OWLSubClassOfAxiomImpl(classArray.get(i), new OWLClassImpl(IRI.create(classArray.get(i).getIRI().toString(), "*")), new HashSet<OWLAnnotation>()));
                }
            }
        }
        //Extension
        for (int i = 0; i < classArray.size(); i++) {
            boolean done = false;
            while (!done) {
                ArrayList<OWLSubClassOfAxiom> adds = new ArrayList<>();
                ArrayList<OWLSubClassOfAxiom> subs = new ArrayList<>();
                for (OWLSubClassOfAxiom a : classDescriptions.get(i)) {
                    OWLClassExpression d = a.getSuperClass().getNNF();
                    if (!d.isAnonymous()) {
                        if (classArray.contains(d.asOWLClass()) && !d.asOWLClass().equals(classArray.get(i))) {
                            subs.add(a);
                            for (OWLSubClassOfAxiom ax : classDescriptions.get(classArray.indexOf(d.asOWLClass()))) {
                                adds.add(new OWLSubClassOfAxiomImpl(classArray.get(i), ax.getSuperClass(), new HashSet<OWLAnnotation>()));
                            }
                        }
                    } else {
                        if (d.isClassExpressionLiteral()) {
                            if (classArray.contains(d.getComplementNNF().asOWLClass()) && !d.getComplementNNF().asOWLClass().equals(classArray.get(i))) {
                                subs.add(a);
                                HashSet<OWLClassExpression> de = new HashSet<>();
                                for (OWLSubClassOfAxiom ax : classDescriptions.get(classArray.indexOf(d.getComplementNNF().asOWLClass()))) {
                                    de.add(ax.getSuperClass().getComplementNNF());
                                }
                                OWLObjectUnionOfImpl ce = new OWLObjectUnionOfImpl(de);
                                adds.add(new OWLSubClassOfAxiomImpl(classArray.get(i), ce, new HashSet<OWLAnnotation>()));
                            }
                        }
                        //TODO OTHER OPTIONS (UNION, INTERSECTION, ETC.
                    }
                }
                classDescriptions.get(i).removeAll(subs);
                classDescriptions.get(i).addAll(adds);
                if (adds.equals(subs)) {
                    done = true;
                }
            }
        }
        //Conversion
        ArrayList<ArrayList<OWLClassExpression>> subclassLists = new ArrayList<>();
        for (int i = 0; i < classDescriptions.size(); i++) {
            subclassLists.add(new ArrayList<OWLClassExpression>());
            for (OWLSubClassOfAxiom a : classDescriptions.get(i)) {
                subclassLists.get(i).add(a.getSuperClass());
            }
        }
        //Subsumption
        ArrayList<ArrayList<OWLClass>> subsumptions = new ArrayList<>();
        for (int i = 0; i < classArray.size(); i++) {
            subsumptions.add(new ArrayList<OWLClass>());
        }
        for (int i = 0; i < classArray.size(); i++) {
            for (int j = 0; j < classArray.size(); j++) {
                if (i != j) {
                    boolean subsumed = true;
                    for (OWLClassExpression a : subclassLists.get(j)) {
                        if (!subclassLists.get(i).contains(a)) {
                            subsumed = false;
                            break;
                        }
                    }
                    if (subsumed) {
                        subsumptions.get(i).add(classArray.get(j));
                    }
                }
            }
        }
        buildDAG(subsumptions, classArray, null);
        /*for (int i = 0; i < classArray.size(); i++) {
         System.out.println(classArray.get(i));
         System.out.println(subsumptions.get(i));
         System.out.println(classDescriptions.get(i));
         }*/
        System.out.println(classNodeHierarchy);

    }

    private void reasonProperties() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void reasonDataproperties() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void buildDAG(ArrayList<ArrayList<OWLClass>> subsumptions, ArrayList<OWLClass> classes, OWLClassNode vertex) {
        if (classNodeHierarchy.vertexSet().isEmpty()) {
            classNodeHierarchy.addVertex(OWLClassNode.getTopNode());
        }
        if (!subsumptions.isEmpty()) {
            if (vertex == null) {
                for (int i = 0; i < subsumptions.size(); i++) {
                    if (subsumptions.get(i).isEmpty()) {
                        OWLClassNode v = new OWLClassNode(classes.get(i));
                        classNodeHierarchy.addVertex(v);
                        try {
                            classNodeHierarchy.addDagEdge(v, OWLClassNode.getTopNode());
                        } catch (DirectedAcyclicGraph.CycleFoundException ex) {
                            System.out.println(ex);
                        }
                        ArrayList<ArrayList<OWLClass>> temp = new ArrayList<>();
                        for (int j = 0; j < subsumptions.size(); j++) {
                            temp.add((ArrayList<OWLClass>) subsumptions.get(j).clone());
                        }
                        temp.remove(i);
                        ArrayList<OWLClass> tempClasses = (ArrayList<OWLClass>) classes.clone();
                        tempClasses.remove(i);
                        buildDAG(temp, tempClasses, v);
                    }
                }
            } else {
                for (int i = 0; i < subsumptions.size(); i++) {
                    if (subsumptions.get(i).contains(vertex.getRepresentativeElement())) {
                        OWLClassNode v = new OWLClassNode(classes.get(i));
                        classNodeHierarchy.addVertex(v);
                        try {
                            classNodeHierarchy.addDagEdge(v, vertex);
                        } catch (DirectedAcyclicGraph.CycleFoundException ex) {
                            System.out.println(ex);
                        }
                        ArrayList<ArrayList<OWLClass>> temp = new ArrayList<>();
                        for (int j = 0; j < subsumptions.size(); j++) {
                            temp.add((ArrayList<OWLClass>) subsumptions.get(j).clone());
                        }
                        ArrayList<OWLClass> tempClasses = (ArrayList<OWLClass>) classes.clone();
                        if (temp.get(i).size() == 1) {
                            temp.remove(i);
                            tempClasses.remove(i);
                        } else {
                            temp.get(i).remove(vertex.getRepresentativeElement());
                        }
                        buildDAG(temp, tempClasses, v);
                    }
                }
            }
        }
        classNodeHierarchy.addVertex(OWLClassNode.getBottomNode());
        for (Node<OWLClass> node : classNodeHierarchy.vertexSet()) {
            if (classNodeHierarchy.inDegreeOf(node) == 0 && classNodeHierarchy.outDegreeOf(node) > 0 && !node.equals(OWLClassNode.getBottomNode())) {
                try {
                    classNodeHierarchy.addDagEdge(OWLClassNode.getBottomNode(), node);
                } catch (DirectedAcyclicGraph.CycleFoundException ex) {
                    System.out.println(ex);
                }
            }
        }
    }

}
