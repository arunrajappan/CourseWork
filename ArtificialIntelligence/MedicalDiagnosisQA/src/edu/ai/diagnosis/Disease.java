package edu.ai.diagnosis;

import java.util.ArrayList;


/**
 * Class to store Disease instances
 * @author Arunkumar Rajappan
 *
 */
public class Disease {

	ArrayList<Symptom> alSymptoms = null;
	private double dWeight = 0.0;
	private String sName = "";
	/**
	 * Constructor for Disease class
	 * @param pName - Disease name
	 */
	public Disease(String pName){
		this.sName = pName;
		alSymptoms = new ArrayList<Symptom>();
	}
	
	
	//function to get the list of the symptoms associated with the disease
	public ArrayList<Symptom> getSymptoms(){
		return alSymptoms;
	}

	//function to get the name for the disease
	public String getName(){
		return sName;
	}
	
	//Function to get the weigh of the disease
	public double getWeight(){
		return dWeight;
	}
	//Function to set the Symptom
	public void setSymptoms(Symptom pSymptom){
		alSymptoms.add(pSymptom);
	}

	//function to set the name
	public void setName(String pName){
		this.sName = pName;
	}
	
	//function to set the current weight of the disease for A* search
	public void setWeight(double pWeight){
		this.dWeight = pWeight;
	}
}
