package submit;

import java.util.Iterator;

import joeq.Compiler.Quad.*;
import joeq.Main.Helper;
import submit.expr.*;

public class LatestVisitor extends QuadVisitor.EmptyVisitor {
	private ExprSet[] earliest;
	private ExprSet[] postponableIn;
	private ExprSet[] latest;
	private ExprSet[] e_use;
	private ControlFlowGraph cfg;
	
	/**
	 * This visitor kicks itself off in computeLatest -> computeUse. 
	 */
	public LatestVisitor(ControlFlowGraph cfg, ExprSet[] earliest, ExprSet[] postponableIn) {
		this.earliest = earliest;
		this.postponableIn = postponableIn;
		this.latest = null;
		this.cfg = cfg;
	}
	
	@Override
	public void visitBinary(Quad q) {
		this.e_use[q.getID()] = new ExprSet(new BinaryExpr(q, Operation.Gen)); 
	}
	
	public ExprSet[] getLatest() {
		if (this.latest == null) {
			this.computeLatest();
		}
		
		return this.latest;
	}
	
	private void computeLatest() {
		this.latest = new ExprSet[this.earliest.length];
		
		ExprSet[] intersectionOfSuccessors = this.computeIntersectionOfSuccessors();
		ExprSet[] complementOfIntersectionOfSuccessors = this.computeComplementOfIntersectionOfSuccessors(intersectionOfSuccessors);
		
		ExprSet[] e_use = this.computeUse();

		// This horrendous operation is defined @p649.
		// latest[B] = (earliest[B] SUM postponableIn[B]) INTERSECT (e_use[B] SUM ~(INTERSECT[S=succ(B)](earliest[S] SUM postponableIn[S]))
		for (int i = 0; i < this.latest.length; ++i) {
			// earliest[B] SUM postponableIn[B]
			this.latest[i] = new ExprSet();
			this.latest[i].copy(this.earliest[i]);
			this.latest[i].sumWith(this.postponableIn[i]);
			
			// e_use[B] SUM ~(INTERSECT[S=succ(B)](earliest[S] SUM postponableIn[S])
			e_use[i].sumWith(complementOfIntersectionOfSuccessors[i]);
			
			// the Big INTERSECT
			// TODO BUG: Is this intersection strict?
			this.latest[i].intersectWith(e_use[i], true);
		}
	}
	
	private ExprSet[] computeIntersectionOfSuccessors() {
		ExprSet[] intersectionOfSuccessors = new ExprSet[this.earliest.length];
		
		for (int i = 0; i < intersectionOfSuccessors.length; ++i) {
			// TODO Is this needed at all? Maybe because of nulls?
			intersectionOfSuccessors[i] = new ExprSet();
		}
		
		QuadIterator qit = new QuadIterator(cfg, true /* Forward */);
		while (qit.hasNext()) {
			Quad q = qit.next();
			
			Iterator<Quad> successors = qit.successors();
			ExprSet intersection = null;
			while (successors.hasNext()) {
				Quad s = successors.next();
				// earliest[S] SUM postponableIn[S]
				ExprSet earliestSumWithPostponableIn = new ExprSet();
				// TODO BUG This could be null if it's connected with EXIT node. Is this correct way of coping with this?
				if (s == null) {
					earliestSumWithPostponableIn = ExprSet.getUniveralSet();
				} else {
					earliestSumWithPostponableIn.copy(this.earliest[s.getID()]);
					earliestSumWithPostponableIn.sumWith(this.postponableIn[s.getID()]);
				}
				
				if (intersection == null) {
					intersection = new ExprSet();
					intersection.copy(earliestSumWithPostponableIn);
				}
				
				// TODO BUG: Strict or not strict?
				intersection.intersectWith(earliestSumWithPostponableIn, true);
			}
			
			intersectionOfSuccessors[q.getID()] = intersection;
		}
		
		return intersectionOfSuccessors;
	}
	
	private ExprSet[] computeComplementOfIntersectionOfSuccessors(ExprSet[] intersectionOfSuccessors) {
		// TODO BUG Is complement strict?
		ExprSet[] complement = new ExprSet[intersectionOfSuccessors.length];
		
		for (int i = 0; i < complement.length; ++i) {
			complement[i] = intersectionOfSuccessors[i].complement();
		}
		
		return complement;
	}
	
	// TODO BUG: This is bug prone once we will be trying to cope with more than BinaryExpressions only.
	private ExprSet[] computeUse() {
		this.e_use = new ExprSet[this.earliest.length];
		for (int i = 0; i < this.e_use.length; ++i) {
			this.e_use[i] = new ExprSet();
		}
		
		Helper.runPass(this.cfg, this);
		
		return this.e_use;
	}
}
