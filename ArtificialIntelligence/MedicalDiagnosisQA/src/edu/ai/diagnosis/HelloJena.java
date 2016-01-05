package edu.ai.diagnosis;

import java.util.Iterator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.reasoner.ValidityReport.Report;
import com.hp.hpl.jena.util.FileManager;

public class HelloJena {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		FileManager.get().addLocatorClassLoader(
				HelloJena.class.getClassLoader());
		Model model = FileManager.get().loadModel("data/medical-diagnosis.owl",
				null, "RDF/XML");
		Reasoner reasoner = ReasonerRegistry.getOWLReasoner().bindSchema(
				model.getGraph());
		String sparqlQuery = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ " PREFIX owl: <http://www.w3.org/2002/07/owl#>"
				+ " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ " PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ " PREFIX md: <http://www.semanticweb.org/arun/ontologies/2014/11/MedicalDiagnosis#>"
				+ " SELECT ?disease "
				+ " WHERE { "
				+" ?disease a md:Disease ." +
				" ?disease md:hasRunnyNose ?symp ." +
				" ?disease md:hasCough ?symp2"
				+ " }";
		//Query query = QueryFactory.create();
		QueryExecution qe = QueryExecutionFactory.create(sparqlQuery, model);
		ResultSet results = qe.execSelect();
		//ResultSetFormatter.out(System.out,results, query);
		
		 for ( ; results.hasNext() ; )
		    {
		      QuerySolution soln = results.nextSolution() ;
		      RDFNode x = soln.get("disease") ;       // Get a result variable by name.
		      Resource r = soln.getResource("disease") ; // Get a result variable - must be a resource
		      //Literal l = soln.getLiteral("disease") ;   // Get a result variable - must be a literal
		      //System.out.println(x.toString());
		      System.out.println(r.getLocalName()); //System.out.println("::"+l.getString());
		    }
	}

}
