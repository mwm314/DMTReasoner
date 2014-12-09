package test;
import static org.junit.Assert.*;
import main.DMTReasoner;

import org.jgraph.graph.DefaultEdge;
import org.jgrapht.experimental.dag.DirectedAcyclicGraph;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.model.IRI;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;


public class TestDAG {

	/**
	 *          TOP
	 *     MIDDLE  MIDDLE2
	 *     	   BOTTOM
	 */
	@Test
	public void testGetBottomClassNode() throws Exception{
		System.out.println("TEST");
		DirectedAcyclicGraph<Node<OWLClass>, DefaultEdge> classNodeHierarchy = new DirectedAcyclicGraph<Node<OWLClass>, DefaultEdge>(DefaultEdge.class);
		OWLClassNode bottomNode = new OWLClassNode();
		OWLClassNode middleNode = new OWLClassNode();
		OWLClassNode middleNode2 = new OWLClassNode();
		OWLClassNode topNode = new OWLClassNode();
		IRI owlNothing = IRI.create("owl:Nothing");
		IRI owlMiddle = IRI.create("owl:Middle");
		IRI owlMiddle2 = IRI.create("owl:Middle2");
		IRI owlThing = IRI.create("owl:Thing");
		OWLClassImpl bottomClass = new OWLClassImpl(owlNothing);
		OWLClassImpl middleClass = new OWLClassImpl(owlMiddle);
		OWLClassImpl middle2Class = new OWLClassImpl(owlMiddle2);
		OWLClassImpl topClass = new OWLClassImpl(owlThing);
		bottomNode.add(bottomClass);
		middleNode.add(middleClass);
		topNode.add(topClass);
		middleNode2.add(middle2Class);
		
		classNodeHierarchy.addVertex(bottomNode);
		classNodeHierarchy.addVertex(middleNode);
		classNodeHierarchy.addVertex(middleNode2);
		classNodeHierarchy.addVertex(topNode);
		
		classNodeHierarchy.addDagEdge(bottomNode, middleNode);
		classNodeHierarchy.addDagEdge(bottomNode, middleNode2);
		classNodeHierarchy.addDagEdge(middleNode, topNode);
		classNodeHierarchy.addDagEdge(middleNode2, topNode);
		
		DMTReasoner test = new DMTReasoner();
		test.setClassNodeHierarchy(classNodeHierarchy);
		
		System.out.println("TEST");
		test.getBottomClassNode();
		System.out.println("TEST");
		assertEquals(true, test.getBottomClassNode().contains(bottomClass));
		
		System.out.println("TEST");
		
		
	}

}
