import java.io.File;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

//import com.github.jsonldjava.core.RDFDataset.IRI;

/**
 * Just an example of reading in an ontology
 * @author Matt
 *
 */
public class TestOntologyRead {
	public static final IRI pizza_iri = IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl");
	public static void main(String[] args) throws Exception {
		/*
		 * OWLOntologyManager inputOntologyManager =
		 * OWLManager.createOWLOntologyManager(); OWLOntologyManager
		 * outputOntologyManager = OWLManager.createOWLOntologyManager();
		 * 
		 * OWLOntology ont =
		 * inputOntologyManager.loadOntologyFromOntologyDocument(new
		 * File("path to ontology"));
		 */
		OWLOntologyManager ontManager = OWLManager.createOWLOntologyManager();
		OWLOntology ont = ontManager.loadOntologyFromOntologyDocument(new File("C:/Users/Matt/Desktop/FinalOntology/testOntology.owl"));
		Set<OWLAxiom> axioms = ont.getAxioms();
		
		Iterator<OWLAxiom> iter = axioms.iterator();
		
		while (iter.hasNext()) {
			OWLAxiom axiom = iter.next();
			System.out.println("STRING: " + axiom.toString());
			System.out.println("Axiom class: " + axiom.getClass());
		}
	}

}
