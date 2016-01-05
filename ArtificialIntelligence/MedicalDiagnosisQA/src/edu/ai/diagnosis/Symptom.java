package edu.ai.diagnosis;

public class Symptom {

	String symName = "";

	public Symptom(String pName) {
		symName = pName;
	}

	// function to get the name for the Symptom
	public String getName() {
		return symName;
	}

	// function to get the name for the Symptom
	public void setName(String pName) {
		symName = pName;
	}

}
