/**
 * 
 */
package edu.ai.diagnosis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

/**
 * @author Arunkumar Rajappan
 * 
 */
public class MedicalDiagnosisQA {

	// Define Global variables
	ArrayList<String> inSymptoms = null;
	String sPrefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
			+ " PREFIX owl: <http://www.w3.org/2002/07/owl#>"
			+ " PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
			+ " PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
			+ " PREFIX md: <http://www.semanticweb.org/arun/ontologies/2014/11/MedicalDiagnosis#>";
	String sSelectPart = " SELECT ?disease ";
	String sWhereClause = " WHERE { ";
	String sConditions = " ?disease a md:Disease .";
	String sEndBraces = "}";

	ArrayList<String> alPossibleDiseases = null;
	ArrayList<String> alPossibleSymptoms = null;
	HashMap<String, String> hmAllSymptoms = null;
	HashSet<String> allDiseases = null;
	ArrayList<Integer> searchStates = null;
	ArrayList<Node> finalPath = null;
	ArrayList<Node> openNodes = null;

	Model model = null;

	/**
	 * Constructor for MedicalDiagnosisQA
	 * @param pSymptoms - Comma seperated list of Symptoms
	 */
	public MedicalDiagnosisQA(String pSymptoms) {
		inSymptoms = new ArrayList<String>();
		String[] params = pSymptoms.split(",");

		for (String sSymp : params) {
			System.out.println("Input Symptom: " + sSymp);
			inSymptoms.add(sSymp.trim());
		}
		init();
	}

	//Initialize all the variables
	private void init() {
		hmAllSymptoms = new HashMap<String, String>();
		hmAllSymptoms.put("hasabdominalpain", "hasAbdominalPain");
		hmAllSymptoms.put("hasbarkingcough", "hasBarkingCough");
		hmAllSymptoms.put("hasbodypain", "hasBodyPain");
		hmAllSymptoms.put("hasbreathingproblems", "hasBreathingProblems");
		hmAllSymptoms.put("hasconstipation", "hasConstipation");
		hmAllSymptoms.put("hascough", "hasCough");
		hmAllSymptoms.put("hasdiarrhea", "hasDiarrhea");
		hmAllSymptoms.put("hasfatigue", "hasFatigue");
		hmAllSymptoms.put("hasfever", "hasFever");
		hmAllSymptoms.put("hasnausea", "hasNausea");
		hmAllSymptoms.put("hasnosebleeds", "hasNoseBleeds");
		hmAllSymptoms.put("hasproblempassinggas", "hasProblemPassingGas");
		hmAllSymptoms.put("hasrectalpain", "hasRectalPain");
		hmAllSymptoms.put("hasrunnynose", "hasRunnyNose");
		hmAllSymptoms.put("hassorethroat", "hasSoreThroat");
		hmAllSymptoms.put("hasvomitting", "hasVomitting");

		initSparqlObjs();
		searchStates = new ArrayList<Integer>();
		ArrayList<Boolean> curState = new ArrayList<Boolean>();
		openNodes = new ArrayList<Node>();
		finalPath = new ArrayList<Node>();
		
		//initalize all the states
		for(String symp: inSymptoms){
			searchStates.add(0);
			curState.add(false);
		}
		getAllDiseases();//Get all the diseases satisfying atleast one input symptom
		
		Node initNode = new Node((ArrayList<Boolean>)curState.clone(),(HashSet<String>)allDiseases.clone(),0.0,0.0,0,sConditions);
		finalPath.add(initNode); //Add Initial Node
		doSearch(); //Start A* Search

	}

	/**
	 * Function to perform A* search
	 * Note: that the g(n) and h(n) value that I am using for this is:
			->	G(n) – Number of Symptoms that are initialized at a particular state i.e. which have indicator value as “1”
			->	H(n) – 1-(1/Number of diseases satisfying those initialized symptoms). In this way only the disease having all the symptoms would be having h(n) as ZERO. Thus heuristic at our goal node would be zero
			->	States – state is the list of input symptoms with indicator values i.e. they have been initialized or observed or not. i.e. [S1, S2] => [0,1]
			->	Initial State – [0,0,…0]
			->	Goal State – [1,1,…,1]
			->	Cost Function f(n) – G(n) + H(n)
	 **/
	public void doSearch() {
		System.out.println("=================================================================");
		System.out.println("Intial States: "+ finalPath.get(finalPath.size()-1).getState().toString());
		System.out.println("Intial Disease List: "+finalPath.get(finalPath.size()-1).getDiseases().toString());
		System.out.println("=================================================================");
		String sQryConditions = sConditions;
		
		for(int ia =0 ;ia<inSymptoms.size();ia++){
			
			openNodes.clear();
			//Get all the next States
			for(int ib=0;ib<inSymptoms.size();ib++){
				ArrayList<Boolean> curState = (ArrayList<Boolean>) finalPath.get(finalPath.size()-1).getState().clone();
				HashSet<String> nextDiseases = null;
				if(!curState.get(ib)){
					String vQryCondition =  finalPath.get(finalPath.size()-1).getQryCondition() + 
							" ?disease md:" + hmAllSymptoms.get(inSymptoms.get(ib).toLowerCase()) + " ?"+inSymptoms.get(ib) + " . ";
					String sparqlQuery = sPrefixes + sSelectPart + sWhereClause + vQryCondition  + sEndBraces;
					curState.set(ib,true);
					allDiseases.clear();
//					System.out.println();
//					System.out.println(sparqlQuery);
//					System.out.println();
					QueryExecution qe = QueryExecutionFactory.create(sparqlQuery, model);
					ResultSet results = qe.execSelect();
					for ( ; results.hasNext() ; )
				    {
				      QuerySolution soln = results.nextSolution() ;
				      RDFNode x = soln.get("disease") ;       // Get a result variable by name.
				      Resource r = soln.getResource("disease") ; // Get a result variable - must be a resource
				      //Literal l = soln.getLiteral("disease") ;   // Get a result variable - must be a literal
				      //System.out.println(r.getLocalName());
				      allDiseases.add(r.getLocalName()); //System.out.println("::"+l.getString());
				    }
					openNodes.add(new Node((ArrayList<Boolean>)curState.clone(),(HashSet<String>)allDiseases.clone(),0.0,0.0,ia,vQryCondition));
				}
			}
			//Search for the node with minimum F(n) value from set of openNodes
			Node nextNode = null;
			double nextFn = Math.pow(2.0,26.0);
			System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			System.out.println("Values on the current set of OpenNodes");
			for(Node vNode:openNodes){
				double vCurFn = vNode.getFn();
				System.out.println("NodeState: "+vNode.getState()+" || Diseases: "+vNode.getDiseases()+" || g(n): "+vNode.getGn()+" || h(n): "+
				vNode.getHn() + " || f(n)[g(n) + h(n)]: " + vNode.getFn());
				if(vCurFn<nextFn){
					nextFn = vCurFn;
					nextNode = vNode;
				}
			}
			System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			finalPath.add(nextNode);
			
			
			System.out.println("=================================================================");
			System.out.println("Current State: "+finalPath.get(finalPath.size()-1).getState().toString());			
			System.out.println("Current Disease List: "+finalPath.get(finalPath.size()-1).getDiseases().toString());
			System.out.println("=================================================================");
		
		}
		
//		int counter = 0;
//		for(String symp: inSymptoms){
//			sQryConditions = sQryConditions + " ?disease md:" + hmAllSymptoms.get(symp.toLowerCase()) + " ?"+symp + " . ";
//			String sparqlQuery = sPrefixes + sSelectPart + sWhereClause + sQryConditions  + sEndBraces;
//			searchStates.set(counter, 1);
//			allDiseases.clear();
//			//System.out.println("sparqlQuery = "+sparqlQuery);
//			QueryExecution qe = QueryExecutionFactory.create(sparqlQuery, model);
//			ResultSet results = qe.execSelect();
//			for ( ; results.hasNext() ; )
//		    {
//		      QuerySolution soln = results.nextSolution() ;
//		      RDFNode x = soln.get("disease") ;       // Get a result variable by name.
//		      Resource r = soln.getResource("disease") ; // Get a result variable - must be a resource
//		      //Literal l = soln.getLiteral("disease") ;   // Get a result variable - must be a literal
//		      //System.out.println(r.getLocalName());
//		      allDiseases.add(r.getLocalName()); //System.out.println("::"+l.getString());
//		    }
//			counter++;
//			System.out.println("==================================================================================================================");
//			System.out.println("Current State: "+searchStates.toString());			
//			System.out.println("Current Disease List: "+allDiseases.toString());
//			System.out.println("==================================================================================================================");
//		}
		
		System.out.println("=================================================================");
		System.out.println("Final State: "+finalPath.get(finalPath.size()-1).getState().toString());
		System.out.println("Input Symptom List: "+inSymptoms.toString());
		System.out.println("Final Disease List: "+finalPath.get(finalPath.size()-1).getDiseases().toString());
		System.out.println("=================================================================");
	
		

	}

	private void initSparqlObjs() {
		FileManager.get().addLocatorClassLoader(
				MedicalDiagnosisQA.class.getClassLoader());
		model = FileManager.get().loadModel("data/medical-diagnosis.owl", null,
				"RDF/XML");
		Reasoner reasoner = ReasonerRegistry.getOWLReasoner().bindSchema(
				model.getGraph());
	}

	public void getAllSymptoms() {

	}

	//gets all the possible diseases for the given set of sysmptoms
	public void getAllDiseases() {
		allDiseases = new HashSet<String>();
		
		for(String symp:inSymptoms){
			String sparqlQuery = sPrefixes + sSelectPart + sWhereClause + sConditions + " ?disease md:" + hmAllSymptoms.get(symp.toLowerCase()) + " ?"+symp + " . " + sEndBraces;
			//System.out.println("sparqlQuery = "+sparqlQuery);
			QueryExecution qe = QueryExecutionFactory.create(sparqlQuery, model);
			ResultSet results = qe.execSelect();
			for ( ; results.hasNext() ; )
		    {
		      QuerySolution soln = results.nextSolution() ;
		      RDFNode x = soln.get("disease") ;       // Get a result variable by name.
		      Resource r = soln.getResource("disease") ; // Get a result variable - must be a resource
		      //Literal l = soln.getLiteral("disease") ;   // Get a result variable - must be a literal
		      //System.out.println(r.getLocalName());
		      allDiseases.add(r.getLocalName()); //System.out.println("::"+l.getString());
		    }
		}
		//System.out.println(allDiseases.toString());
	}

}
