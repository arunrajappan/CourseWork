package edu.ai.diagnosis;

import java.util.ArrayList;
import java.util.HashSet;

public class Node {
	
	ArrayList<Boolean> curState = null;
	HashSet<String> curDiseases = null;
	double dGn=0.0, dHn = 0.0, dFn = 0.0;
	int iNumberOfStatesOn = 0;
	String sQryCondition = "";
	
	public Node(ArrayList<Boolean> pState, HashSet<String> pDiseases, double pGn, double pHn, int pNumberOfStatesOn, String pQryCondition){
		this.curState = pState;
		this.curDiseases = pDiseases;
		this.dGn = pGn;
		this.dHn = pHn;
		this.iNumberOfStatesOn = pNumberOfStatesOn;
		sQryCondition = pQryCondition;
		
	}
	
	public ArrayList<Boolean> getState(){
		return this.curState;
	}
	public String getQryCondition(){
		return sQryCondition;
	}
	public HashSet<String> getDiseases(){
		return this.curDiseases;
	}
	public double getGn(){
		this.dGn = iNumberOfStatesOn+1;
		return this.dGn;
	}
	public double getHn(){
		if(curDiseases.size() != 0){
			this.dHn = 1.0-(1.0/curDiseases.size());
		}else{
			this.dHn = Math.pow(2.0,20.0);
		}
		return this.dHn;
	}
	public double getFn(){
		
		getGn();
		getHn();
		this.dFn = this.dGn + this.dHn;
		return this.dFn;
	}

}
