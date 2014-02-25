package submit.expr;

import java.util.*;

public class ExprSetMap {
	private Map<String, ExprSet[]> exprSetMap;
	
	public ExprSetMap() {
		this.exprSetMap = new TreeMap<String, ExprSet[]>();
	}
	
	public Iterator<String> getKeyIterator() {
		return this.exprSetMap.keySet().iterator();
	}
	
	public void saveEntry(String methodSignature, ExprSet[] exprSet) {
		this.exprSetMap.put(methodSignature, exprSet);
	}
	
	public ExprSet[] getEntry(String methodSignature) {
		return this.exprSetMap.get(methodSignature);
	}
}
