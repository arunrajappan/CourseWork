package edu.ai.diagnosis;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if(args.length != 1){
			System.err.println("USAGE: medicaldiagnosis.jar <COMMA_SEPERATED_SYMPTOMS_LIST>");
			System.exit(0);
		}
		
		String param = "hasCough,hasRunnyNose,hasSoreThroat";
		param = args[0];
		MedicalDiagnosisQA mdqa = new MedicalDiagnosisQA(param);
	}

}
