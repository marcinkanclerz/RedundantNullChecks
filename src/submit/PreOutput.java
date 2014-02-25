package submit;

import java.util.Iterator;

import submit.expr.*;

public class PreOutput {
	private ExprSetMap latestMap, latestComplementMap, usedOutMap, e_useMap;
	public ExprSetMap addTemporaryAtTheBeginningMap, replaceExpressionByTemporaryMap;
	
	public PreOutput(ExprSetMap latestMap,
			ExprSetMap latestComplementMap,
			ExprSetMap usedOutMap,
			ExprSetMap e_useMap) {
		this.latestMap = latestMap;
		this.latestComplementMap = latestComplementMap;
		this.usedOutMap = usedOutMap;
		this.e_useMap = e_useMap;
		
		this.addTemporaryAtTheBeginningMap = new ExprSetMap();
		this.replaceExpressionByTemporaryMap = new ExprSetMap();
	}
	
	public void doTheMagic() {
		for (Iterator<String> it = this.latestMap.getKeyIterator(); it.hasNext();) {
			String methodSignature = it.next();
			
		    // Latest is computed in usedExpressions.preprocess, cause it depends on CFG and this was the easiest
		    // way to thread the CFG through the system into computation of Latest.
		    ExprSet[] latest = this.latestMap.getEntry(methodSignature);
		    ExprSet[] usedOut = this.usedOutMap.getEntry(methodSignature);
		    ExprSet[] e_use = this.e_useMap.getEntry(methodSignature);
		    
		    ExprSet[] addTemporaryAtTheBeginning = new ExprSet[latest.length];
		    
		    for (int j = 0; j < latest.length; ++j) {
		    	addTemporaryAtTheBeginning[j] = new ExprSet();
		    	// As in 8.b, @p654:
		    	// latest[B] INTERSECT usedOut[B]
		    	addTemporaryAtTheBeginning[j].copy(latest[j]);
		    	addTemporaryAtTheBeginning[j].intersectWith(usedOut[j], true);
		    }
		    
		    this.addTemporaryAtTheBeginningMap.saveEntry(methodSignature, addTemporaryAtTheBeginning);
		    
		    ExprSet[] replaceExpressionByTemporary = new ExprSet[latest.length];
		    
		    for (int j = 0; j < latest.length; ++j) {
		    	replaceExpressionByTemporary[j] = new ExprSet();
		    	// As in 8.c, @p654:
		    	// e_use[B] INTERSECT (~latest[B] SUM usedOut[B])
		    	replaceExpressionByTemporary[j].copy(e_use[j]);
		    	ExprSet latestComplement = latest[j].complement(); 
		    	latestComplement.sumWith(usedOut[j]);
		    	replaceExpressionByTemporary[j].intersectWith(latestComplement, true);
		    }
		    
		    this.replaceExpressionByTemporaryMap.saveEntry(methodSignature, replaceExpressionByTemporary);
		}
	}
	
	public void printOutput() {
		for (Iterator<String> it = this.latestMap.getKeyIterator(); it.hasNext();) {
			String methodSignature = it.next();
			
			System.out.println("**************************");
			System.out.println(">" + methodSignature + "<");
			System.out.println("**************************");
			
			System.out.println();
		    System.out.println("Add temporary at the beginning of:");
		    
		    ExprSet[] addTemporaryAtTheBeginning = this.addTemporaryAtTheBeginningMap.getEntry(methodSignature);
		    
		    for (int j = 0; j < addTemporaryAtTheBeginning.length; ++j) {
		    	System.out.println(j + ": " + addTemporaryAtTheBeginning[j].toString());
		    }
		    
		    System.out.println();
		    System.out.println(methodSignature + ": replace expression with temporary at:");
		    
		    ExprSet[] replaceExpressionByTemporary = this.replaceExpressionByTemporaryMap.getEntry(methodSignature);
		    for (int j = 0; j < replaceExpressionByTemporary.length; ++j) {
		    	System.out.println(j + ": " + replaceExpressionByTemporary[j].toString());
		    }
		    
			System.out.println();
		}
	}
}
