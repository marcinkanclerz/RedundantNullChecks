package submit;

import java.util.Iterator;

import submit.expr.*;
import joeq.Compiler.Quad.QuadVisitor;

public class Earliest {
	private ExprSetMap earliestMap, anticipatedInMap, availableInMap;
	
	public Earliest(ExprSetMap anticipatedInMap, ExprSetMap availableInMap, ExprSetMap earliestMap) {
		this.anticipatedInMap = anticipatedInMap;
		this.availableInMap = availableInMap;
		this.earliestMap = earliestMap;
	}
	
	public void doTheMagic() {
		for (Iterator<String> it = this.anticipatedInMap.getKeyIterator(); it.hasNext();) {
			String methodSignature = it.next();
			
			ExprSet[] anticipatedIn = this.anticipatedInMap.getEntry(methodSignature);
			ExprSet[] availableIn = this.availableInMap.getEntry(methodSignature);
			ExprSet[] earliest = new ExprSet[availableIn.length];
			
			for (int i = 0; i < earliest.length; ++i) {
				// Operation defined @p649.
				// earliest[B] = anticipated[B].in - available[B].in
				earliest[i] = new ExprSet();
				earliest[i].copy(anticipatedIn[i]);
				earliest[i].remove(availableIn[i], false);
			}
			
			this.earliestMap.saveEntry(methodSignature, earliest);
		}
	}
}
