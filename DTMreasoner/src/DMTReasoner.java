import java.io.File;
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
import org.semanticweb.owlapi.reasoner.impl.OWLNamedIndividualNodeSet;
import org.semanticweb.owlapi.util.Version;

public class DMTReasoner implements OWLReasoner, OWLOntologyChangeListener {
    //List of class variables
    /**
     * The ontology we are reasoning over
     */
    private OWLOntology ontology;
    
    //DAGS for our class and property hierarchies? See here for why we may need them: http://owlapi.sourceforge.net/javadoc/org/semanticweb/owlapi/reasoner/OWLReasoner.html
    //private DirectedAcyclicGraph<Node<OWLClass>, DefaultEdge> classNodeHierarchy = new DirectedAcyclicGraph<Node<OWLClass>, DefaultEdge>(DefaultEdge.class);
    //private DirectedAcyclicGraph<Node<OWLDataProperty>, DefaultEdge> dataPropertyNodeHierarchy = new DirectedAcyclicGraph<Node<OWLDataProperty>, DefaultEdge>(DefaultEdge.class);
    //private DirectedAcyclicGraph<Node<OWLObjectProperty>, DefaultEdge> objectPropertyNodeHierarchy = new DirectedAcyclicGraph<Node<OWLObjectProperty>, DefaultEdge>(DefaultEdge.class);
    
    //A NodeSet representing the individuals
    private OWLNamedIndividualNodeSet individuals = new OWLNamedIndividualNodeSet();

    //God only knows what this does
    private BufferingMode bufferingMode = BufferingMode.BUFFERING;

    //Axioms added
    private Set<OWLAxiom> additions = new HashSet<OWLAxiom>();

    //Axioms removed
    private Set<OWLAxiom> removals = new HashSet<OWLAxiom>();
    
    //Given axioms from the ontology
    private Set<OWLAxiom> axioms;
    
    
    //These have to be attached to a hierarchy of some kind
    /*private OWLClassNode bottomClassNode = OWLClassNode.getBottomNode();
    private OWLDataPropertyNode bottomDataPropertyNode = OWLDataPropertyNode.getBottomNode();
    private OWLObjectPropertyNode bottomObjectPropertyNode = OWLObjectPropertyNode.getBottomNode();

    private OWLClassNode topClassNode = OWLClassNode.getTopNode();
    private OWLDataPropertyNode topDataPropertyNode = OWLDataPropertyNode.getTopNode();
    private OWLObjectPropertyNode topObjectPropertyNode = OWLObjectPropertyNode.getTopNode();*/

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
        for(OWLAxiom i : removals){
            axioms.remove(i);
        }
        for(OWLAxiom i : additions){
            axioms.add(i);
        }
        

    }

    @Override
    /**
     * Returns the bottom class node from our classNodeSet hierarchy
     * @return
     */
    public Node<OWLClass> getBottomClassNode() {
    	// TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
    	// TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
    	// TODO Auto-generated method stub
        return null;
    }

    @Override
    public BufferingMode getBufferingMode() {
        return bufferingMode;
    }

    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(OWLDataProperty dataProperty,
            boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<OWLLiteral> getDataPropertyValues(OWLNamedIndividual individual,
            OWLDataProperty dataProperty) {
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
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(
            OWLNamedIndividual individual) {
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
    public NodeSet<OWLDataProperty> getDisjointDataProperties(
            OWLDataPropertyExpression dataPropExpr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(
            OWLObjectPropertyExpression objectPropExpr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node<OWLClass> getEquivalentClasses(OWLClassExpression classExpr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(
            OWLDataProperty dataProp) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(
            OWLObjectPropertyExpression objectPropExpr) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FreshEntityPolicy getFreshEntityPolicy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IndividualNodeSetPolicy getIndividualNodeSetPolicy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeSet<OWLNamedIndividual> getInstances(OWLClassExpression arg0,
            boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(
            OWLObjectPropertyExpression arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(
            OWLObjectPropertyExpression arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(
            OWLObjectPropertyExpression arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(
            OWLNamedIndividual arg0, OWLObjectPropertyExpression arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomAdditions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<OWLAxiom> getPendingAxiomRemovals() {
        // TODO Auto-generated method stub
        return null;
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
    public NodeSet<OWLClass> getSubClasses(OWLClassExpression classExpression, boolean directSubclass) {
    	// TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeSet<OWLDataProperty> getSubDataProperties(OWLDataProperty arg0,
            boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(
            OWLObjectPropertyExpression arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeSet<OWLClass> getSuperClasses(OWLClassExpression arg0,
            boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(
            OWLDataProperty arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(
            OWLObjectPropertyExpression arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getTimeOut() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Node<OWLClass> getTopClassNode() {
    	// TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node<OWLDataProperty> getTopDataPropertyNode() {
    	// TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
    	// TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeSet<OWLClass> getTypes(OWLNamedIndividual arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Node<OWLClass> getUnsatisfiableClasses() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void interrupt() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isConsistent() {
        // TODO Auto-generated method stub
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
                    } else if (i.isRemoveAxiom()) {
                        removals.add(i.getAxiom());
                    }
                }
                else{
                    axioms = ontology.getAxioms();
                }
            }
        }
    }
    

}
