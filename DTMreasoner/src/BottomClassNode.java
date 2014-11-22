import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
import org.semanticweb.owlapi.reasoner.Node;

/**
 * A class to represent the bottom class node
 * @author Matt
 *
 */
public class BottomClassNode implements Node<OWLClass> {
	
	/**
	 * members represents the owl classes which are a member of the bottom class
	 */
	private Set<OWLClass> members = new HashSet<OWLClass>();
	
	BottomClassNode() {
		OWLNothing bottomConcept = new OWLNothing();
		members.add(bottomConcept);
	}

	@Override
	public Iterator<OWLClass> iterator() {
		return members.iterator();
	}

	@Override
	public boolean contains(OWLClass owlClass) {
		if (this.getEntities().contains(owlClass)) {
			return true;
		}
		else return false;
	}

	@Override
	public Set<OWLClass> getEntities() {
		return members;
	}

	@Override
	public Set<OWLClass> getEntitiesMinus(OWLClass owlclass) {
		Set<OWLClass> membersWithoutClass = new HashSet<OWLClass>();
		membersWithoutClass = members;
		membersWithoutClass.remove(owlclass);
		return membersWithoutClass;
	}

	@Override
	public Set<OWLClass> getEntitiesMinusBottom() {
		// These are all bottom concepts
		return null;
	}

	@Override
	public Set<OWLClass> getEntitiesMinusTop() {
		return members;
	}

	@Override
	public OWLClass getRepresentativeElement() {
		return members.iterator().next();
	}

	@Override
	public int getSize() {
		return members.size();
	}

	@Override
	public boolean isBottomNode() {
		return true;
	}

	@Override
	public boolean isSingleton() {
		if (members.size() == 1)
			return true;
		return false;
	}

	@Override
	public boolean isTopNode() {
		return false;
	}

	
}
