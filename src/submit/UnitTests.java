package submit;

import java.util.LinkedList;

import submit.expr.*;

public class UnitTests {
	private static String getOp(int id, int i, String op) {
		return "{id: " + id + ", n: " + i + ", op: " + op + "}";
	}
	
	private static BinaryExpr getBinaryExpr(Operation op, int id, int i) {
		return new BinaryExpr(
				op, 
				UnitTests.getOp(id, i, "op1"), 
				UnitTests.getOp(id, i, "op2"),
				"MUL_I",
				UnitTests.getOp(id, i, "op1"));
	}
	
	private static LinkedList<Expr> getExprCollection(Operation op, int id, int startFrom, int n) {
		LinkedList<Expr> collection = new LinkedList<Expr>();
		
		for (int i = startFrom; i < n; ++i) {
			collection.add(UnitTests.getBinaryExpr(op, id, i));
		}
		
		return collection;
	}
	
	private static void meetWith() {
		// TODO Meet is relative.
	}
	
	/**
	 * Seems fine :-)
	 */
	private static void copy() {
		System.out.println("*** TEST BEGINS ***");
		ExprSet lhs = new ExprSet();
		ExprSet rhs = new ExprSet(UnitTests.getExprCollection(Operation.Gen, 1, 0, 2));
		
		System.out.println(lhs.toString());
		System.out.println("copy");
		System.out.println(rhs.toString());
		
		lhs.copy(rhs);
		
		System.out.println("result:");
		System.out.println(lhs.toString());
		System.out.println("*** TEST ENDS ***");
		System.out.println();
	}
	
	/**
	 * Seems fine :-)
	 */
	private static void contains() {
		System.out.println("*** TEST BEGINS ***");
		ExprSet lhs = new ExprSet(UnitTests.getExprCollection(Operation.Gen, 1, 0, 3));
		Expr e = UnitTests.getBinaryExpr(Operation.Gen, 1, 1);
		
		System.out.println(lhs.toString());
		System.out.println("contains");
		System.out.println(e.toString());
		
		boolean contains = lhs.contains(e, true);
		
		System.out.println("result:");
		System.out.println(contains);
		System.out.println("*** TEST ENDS ***");
		System.out.println();
	}
	
	/**
	 * Seems fine :-)
	 */
	private static void intersectWith() {
		System.out.println("*** TEST BEGINS ***");
		ExprSet lhs = new ExprSet(UnitTests.getExprCollection(Operation.Gen, 2, 1, 3));
		ExprSet rhs = new ExprSet(UnitTests.getExprCollection(Operation.Gen, 2, 0, 2));
		
		System.out.println(lhs.toString());
		System.out.println("intersectWith");
		System.out.println(rhs.toString());
		
		lhs.intersectWith(rhs, true);
		
		System.out.println("result:");
		System.out.println(lhs.toString());
		System.out.println("*** TEST ENDS ***");
		System.out.println();
	}
	
	/**
	 * Seems fine :-)
	 */
	private static void remove() {
		System.out.println("*** TEST BEGINS ***");
		ExprSet lhs = new ExprSet(UnitTests.getExprCollection(Operation.Gen, 1, 0, 3));
		ExprSet rhs = new ExprSet(UnitTests.getExprCollection(Operation.Kill, 1, 0, 2));
		
		System.out.println(lhs.toString());
		System.out.println("remove");
		System.out.println(rhs.toString());
		
		lhs.remove(rhs, false);
		
		System.out.println("result:");
		System.out.println(lhs.toString());
		System.out.println("*** TEST ENDS ***");
		System.out.println();
	}
	
	/**
	 * Seems fine :-)
	 */
	private static void sumWith() {
		System.out.println("*** TEST BEGINS ***");
		ExprSet lhs = new ExprSet(UnitTests.getExprCollection(Operation.Gen, 1, 0, 3));
		ExprSet rhs = new ExprSet(UnitTests.getExprCollection(Operation.Kill, 1, 2, 4));
		
		System.out.println(lhs.toString());
		System.out.println("sumWith");
		System.out.println(rhs.toString());
		
		lhs.sumWith(rhs);
		
		System.out.println("result:");
		System.out.println(lhs.toString());
		System.out.println("*** TEST ENDS ***");
		System.out.println();
	}
	
	private static void equals() {
		System.out.println("*** TEST BEGINS ***");
		ExprSet lhs = new ExprSet(UnitTests.getExprCollection(Operation.Gen, 1, 0, 3));
		ExprSet rhs = new ExprSet(UnitTests.getExprCollection(Operation.Gen, 1, 0, 3));
		
		System.out.println(lhs.toString());
		System.out.println("equals");
		System.out.println(rhs.toString());
		
		boolean equals = lhs.equals(rhs);
		
		System.out.println("result:");
		System.out.println(equals);
		System.out.println("*** TEST ENDS ***");
		System.out.println();
	}
	
	public static void main(String[] args) {
		UnitTests.contains();
		UnitTests.copy();
		UnitTests.intersectWith();
		UnitTests.remove();
		UnitTests.sumWith();
		UnitTests.equals();
	}
}
