package org.monarchinitiative.boom.io;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.monarchinitiative.boom.model.CliqueSolution;
import org.monarchinitiative.boom.model.LabelUtil;
import org.monarchinitiative.boom.model.ProbabilisticEdge;
import org.monarchinitiative.boom.model.ProbabilisticGraph;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.Node;

/**
 * Write a clique as a dot file
 * 
 * @author cjm
 *
 */
public class CliqueSolutionDotWriter {

	private CliqueSolution cs;
	private OWLOntology ontology;
	private ProbabilisticGraph probabilisticGraph;


	public CliqueSolutionDotWriter(CliqueSolution cs, OWLOntology ontology) {
		super();
		this.cs = cs;
		this.ontology = ontology;
	}



	public CliqueSolutionDotWriter(CliqueSolution cs, OWLOntology ontology,
			ProbabilisticGraph probabilisticGraph) {
		super();
		this.cs = cs;
		this.ontology = ontology;
		this.probabilisticGraph = probabilisticGraph;
	}



	public String render() {
		return "digraph cliquegraph {\n" + renderNodesInCliques() + 
				renderGivenLogicalEdges() + 
				renderPriorEdges() + 
				renderFinalAxioms() + 
				"}";
	}

	public String renderToFile(String dirname) throws IOException {
		String base = dirname + getId(cs.cliqueId);
		String fn = base + ".dot";
		String png = base + ".png";

		File f = new File(fn);
		FileUtils.writeStringToFile(f, render());
		// TODO - make path configurable
		Runtime.getRuntime().exec("/opt/local/bin/dot -Grankdir=BT -T png " + " -o " + png + " " + fn);
		return png;
	}

	private String renderNodesInCliques() {
		return cs.nodes.stream().map( (Node<OWLClass> n) -> renderNode(n) ).collect(Collectors.joining("\n"));
	}

	private String renderNode(Node<OWLClass> n) {
		return "subgraph cluster_"+getId(n.getRepresentativeElement()) +" {" + 
				n.getEntities().stream().map( (OWLClass c) -> renderClass(c)).collect(Collectors.joining("\n")) +
				"}\n";

	}


	private String renderClass(OWLClass c) {
		return getId(c) + " [ label=\"" + getLabel(c) + "\" ];";
	}

	private String renderFinalAxioms() {
		// render axioms blue
		return cs.axioms.stream().map( (OWLAxiom a) -> renderEdge(a, "blue")).collect(Collectors.joining("\n"));
	}
	private String renderGivenLogicalEdges() {
		// note that the greedy optimization procedure will have switched som
		// probabilistic edges to logical edges in advance; these will not have an entry in
		// the axiom probability index. TODO: improve datamodel so this is not necessary
		return cs.subGraph.getLogicalEdges().stream().
				filter( (OWLAxiom a) -> probabilisticGraph.getAxiomPriorProbability(a) == null).
				map( (OWLAxiom a) -> renderEdge(a, "black")).collect(Collectors.joining("\n"));
	}

	private String renderPriorEdges() {
		return cs.subGraph.getProbabilisticEdges().stream().map( (ProbabilisticEdge e) -> renderEdge(e)).collect(Collectors.joining("\n"));
	}

	private String renderEdge(ProbabilisticEdge e) {
		// probabilistic edges are dotted
		return renderEdge(
				getId(getId(e.getSourceClass())), 
				getId(getId(e.getTargetClass())), 
				"none",
				"dotted",
				"blue",
				1,
				//e.getProbabilityTable().toString(),
				"",
				""); // TODO
	}

	private String renderEdge(OWLAxiom ax, String color) {
		String elabel;
		Double pr = probabilisticGraph.getAxiomPriorProbability(ax);
		int penwidth;
		if (pr == null) {
			elabel = "";
			penwidth = 1;
		}
		else {
			elabel = pr.toString();
			penwidth = (int) (1 + pr*10);
			if (cs.subGraph.getLogicalEdges().contains(ax)) {
				// the probabilistic edge was turned into a logical edge
				// by the greedy optimization algorithm
				elabel = elabel + "*";
			}
		}
		if (ax instanceof OWLSubClassOfAxiom) {
			OWLSubClassOfAxiom sca = (OWLSubClassOfAxiom)ax;
			return renderEdge(
					getId((OWLClass) sca.getSubClass()), 
					getId((OWLClass) sca.getSuperClass()),
					"normal",
					"solid",
					color,
					penwidth,
					elabel,
					"");
		}
		else if (ax instanceof OWLEquivalentClassesAxiom) {
			OWLEquivalentClassesAxiom eca = (OWLEquivalentClassesAxiom)ax;
			List<OWLClassExpression> xs = eca.getClassExpressionsAsList();
			return renderEdge(
					getId((OWLClass) xs.get(0)), 
					getId((OWLClass) xs.get(1)),
					"ediamond",
					"solid",
					"red",
					penwidth,
					elabel,
					", arrowtail=ediamond, dir=both");
		}
		return null;
	}

	private String renderEdge(String s, String t, String arrowhead, 
			String style, String color, Integer penwidth, String elabel, String extra) {

		return s + " -> " + t +" [ arrowhead = " + arrowhead +", penwidth="+penwidth+
				", color="+color+", label=\""+elabel+"\""+
				", style="+style+extra+"]\n";
	}


	private String getId(OWLClass c) {
		return c.getIRI().getFragment();
	}

	private String getId(String c) {
		return IRI.create(c).getFragment();
	}


	private String getLabel(OWLClass c) {
		String label = LabelUtil.getLabel(c, ontology);
		label = label.replaceAll(" ", "\n");
		label = label.replaceAll("\"", "'");
		return c.getIRI().getFragment() + " " + label;
	}

}