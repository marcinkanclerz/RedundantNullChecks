package submit.expr;

import java.util.*;

import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.Operand.*;
import joeq.Main.Helper;
import flow.Flow;

public class ExprSet implements Flow.DataflowObject {
	private static ExprSet universal;
	private static boolean meetIsIntersection;
	private static boolean topIsUniversal;
	
	private Collection<Expr> exprSet = new LinkedList<Expr>();
	
	public ExprSet() {}
	
	public ExprSet(Expr expr) {
		this.exprSet.add(expr);
	}
	
	/**
	 * For testing purposes.
	 * @param collection List that's just added, no additional logic there.
	 */
	public ExprSet(Collection<Expr> collection) {
		this.exprSet.addAll(collection);
	}
	
	public static void setupLattice(ControlFlowGraph cfg, boolean meetIsIntersection, boolean topIsUniversal) {
		// Find and set universal set.
		UniversalSetVisitor universalSetVisitor = new UniversalSetVisitor(cfg); 
		ExprSet.universal = universalSetVisitor.getUniversalSet();
		
		ExprSet.meetIsIntersection = meetIsIntersection;
		ExprSet.topIsUniversal = topIsUniversal;
	}
	
	public static ExprSet getUniveralSet() {
		ExprSet exprSet = new ExprSet();
		exprSet.copy(ExprSet.universal);
		
		return exprSet;
	}
	
	public Iterator<Expr> iterator() {
		return this.exprSet.iterator();
	}
	
	public void setToTop() {
		this.setToUniversalOrEmpty(ExprSet.topIsUniversal);
	}
	
	public void setToBottom() {
		this.setToUniversalOrEmpty(!ExprSet.topIsUniversal);
	}
	
	/**
	 * Meet operation depends on ExprSet.meetIsIntersection. 
	 * If true, intersection is used, sum otherwise.
	 */
	public void meetWith(Flow.DataflowObject o) {
		ExprSet rhs = (ExprSet)o;
		
		if (ExprSet.meetIsIntersection) {
			// Operation is set intersection.
			this.intersectWith(rhs, true);
		} else {
			// Operation is set sum.
			this.sumWith(rhs);
		}
	}
	
	public void copy(Flow.DataflowObject o) {
		// TODO BUG THere's shitloads of stuff here...
		ExprSet rhs = (ExprSet)o;
		
		LinkedList<Expr> newExprList = new LinkedList<Expr>();
		
		for (Iterator<Expr> it = rhs.iterator(); it.hasNext();) {
			Expr expr = it.next();
			newExprList.add(expr.clone());
		}
		
		this.exprSet = newExprList;
	}
	
	public boolean contains(Expr expr, boolean strict) {
		boolean contains = false;
		
		for (Iterator<Expr> it = this.exprSet.iterator(); it.hasNext();) {
			if (it.next().isCongruent(expr, strict)) {
				contains = true;
				break;
			}
		}
		
		return contains;
	}
	
	public void intersectWith(ExprSet setToIntersect, boolean strict) {
		LinkedList<Expr> newExprSet = new LinkedList<Expr>();
		
		for (Iterator<Expr> it = this.exprSet.iterator(); it.hasNext();) {
			Expr expr = it.next();
			if (setToIntersect.contains(expr, strict)) {
				newExprSet.add(expr.clone());
			}
		}
		
		this.exprSet = newExprSet;
	}
	
	public void remove(ExprSet setToBeRemoved, boolean strict) {
		LinkedList<Expr> newExprSet = new LinkedList<Expr>();
		
		for (Iterator<Expr> it = this.exprSet.iterator(); it.hasNext();) {
			Expr expr = it.next();
			if (!setToBeRemoved.contains(expr, strict)) {
				newExprSet.add(expr.clone());
			}
		}
		
		this.exprSet = newExprSet;
	}
	
	/**
	 * Is there a need for non-strict sum? 
	 */
	public void sumWith(ExprSet setToBeAdded) {
		for (Iterator<Expr> it = setToBeAdded.iterator(); it.hasNext();) {
			Expr expr = it.next();
			if (!this.contains(expr, true)) {
				this.exprSet.add(expr.clone());
			}
		}
	}
	
	@Override 
	public boolean equals(Object o) {
		// TODO Deprimitivy.
		boolean equal = false;
		
		if (o instanceof ExprSet) {
			ExprSet rhs = (ExprSet)o;

			if (this.exprSet.size() == rhs.exprSet.size()) {
				ExprSet lhsCopy = new ExprSet();
				lhsCopy.copy(this);
				
				lhsCopy.intersectWith(rhs, true);
				
				if (lhsCopy.exprSet.size() == this.exprSet.size()) {
					equal = true;
				} else {
					equal = false;
					
					ExprSet lhsCopy2 = new ExprSet();
					lhsCopy2.copy(this);
					
					lhsCopy2.intersectWith(rhs, true);
				}
			}
		}
		
		return equal;
	}
	
	@Override
	public String toString() {
		String s = "{ ";
		for (Iterator<Expr> it = this.exprSet.iterator(); it.hasNext();) {
			s += it.next().toString() + ", "; 
		}
		s += " }";
		return s;
	}
	
	private void setToUniversalOrEmpty(boolean universal) {
		if (universal) {
			this.copy(ExprSet.universal);
		} else {
			this.exprSet = new LinkedList<Expr>();
		}
	}
}
