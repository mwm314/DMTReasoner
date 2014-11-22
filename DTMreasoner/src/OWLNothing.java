import java.util.Set;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitorEx;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEntityVisitor;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNamedObjectVisitor;
import org.semanticweb.owlapi.model.OWLNamedObjectVisitorEx;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectVisitor;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/**
 * A class to represent OWLNothing
 * @author Matt
 *
 */
public class OWLNothing implements OWLClass {

	@Override
	public void accept(OWLClassExpressionVisitor arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <O> O accept(OWLClassExpressionVisitorEx<O> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLClassExpression> asConjunctSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLClassExpression> asDisjunctSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClass asOWLClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsConjunct(OWLClassExpression arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ClassExpressionType getClassExpressionType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression getComplementNNF() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression getNNF() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLClassExpression getObjectComplementOf() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAnonymous() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isClassExpressionLiteral() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOWLNothing() {
		return true;
	}

	@Override
	public boolean isOWLThing() {
		return false;
	}

	@Override
	public void accept(OWLObjectVisitor arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <O> O accept(OWLObjectVisitorEx<O> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLClassExpression> getNestedClassExpressions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBottomEntity() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTopEntity() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int compareTo(OWLObject arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Set<OWLEntity> getSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsEntityInSignature(OWLEntity arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<OWLAnonymousIndividual> getAnonymousIndividuals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLClass> getClassesInSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLObjectProperty> getObjectPropertiesInSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLDataProperty> getDataPropertiesInSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLNamedIndividual> getIndividualsInSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLDatatype> getDatatypesInSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<OWLAnnotationProperty> getAnnotationPropertiesInSignature() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void accept(OWLEntityVisitor arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <O> O accept(OWLEntityVisitorEx<O> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLAnnotationProperty asOWLAnnotationProperty() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLDataProperty asOWLDataProperty() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLDatatype asOWLDatatype() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLNamedIndividual asOWLNamedIndividual() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OWLObjectProperty asOWLObjectProperty() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EntityType<?> getEntityType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBuiltIn() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOWLAnnotationProperty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOWLClass() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOWLDataProperty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOWLDatatype() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOWLNamedIndividual() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isOWLObjectProperty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isType(EntityType<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String toStringID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void accept(OWLNamedObjectVisitor arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <O> O accept(OWLNamedObjectVisitorEx<O> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IRI getIRI() {
		// TODO Auto-generated method stub
		return null;
	}

}
