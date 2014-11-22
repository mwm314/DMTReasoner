
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeListener;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.util.Version;

public class DMTReasoner implements OWLReasoner, OWLOntologyChangeListener {
    //List of class variables
    /**
     * The ontology we are reasoning over
     */
    private OWLOntology ontology;

    private BufferingMode bufferingMode = BufferingMode.BUFFERING;

    private Set<OWLAxiom> additions = new HashSet<>();

    private Set<OWLAxiom> removals = new HashSet<>();
    
    private Set<OWLAxiom> axioms;
    /**
     * Our bottom class node
     */
    private OWLClassNode bottomClass = OWLClassNode.getBottomNode();

    /**
     * Our top class node
     */
    private OWLClassNode topClass = OWLClassNode.getTopNode();

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
    public Node<OWLClass> getBottomClassNode() {
        // TODO Auto-generated method stub
        return bottomClass;
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
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(
            OWLNamedIndividual individual) {
        // TODO Auto-generated method stub
        return null;
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
    public Node<OWLNamedIndividual> getSameIndividuals(OWLNamedIndividual arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NodeSet<OWLClass> getSubClasses(OWLClassExpression arg0, boolean arg1) {
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
