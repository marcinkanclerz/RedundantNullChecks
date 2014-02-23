package submit.expr;

/**
 * SingletonExpr is used in gen-kill expressions mainly in kill sets.
 * Lets say we have a set X = {a+b, b+c}, then Y = {SingletonExpr(b)}, then X \ Y = empty.
 * To express that, SingletonExpr compareTo called with e.g. BinaryExpr should return
 * 0 if any operator in BinaryExpr is equal to SingletonExpr's representative.  
 */
public class SingletonExpr implements Expr {
	private String representative;
	
	public SingletonExpr(String representative) {
		this.representative = representative;
	}
	
	public boolean isCongruent(Expr rhs, boolean strict) {
		// TODO Implement.
		
		if (rhs instanceof BinaryExpr) {
			BinaryExpr binaryExpr = (BinaryExpr)rhs;
			binaryExpr.op1.toString();
		}
		return true;
	}
	
	public SingletonExpr clone() {
		// TODO Implement.
		return null;
	}
}

