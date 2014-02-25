package submit;

import java.util.Iterator;

import joeq.Compiler.Quad.*;
import joeq.Main.Helper;
import submit.expr.*;

public class Latest extends QuadVisitor.EmptyVisitor {
	private ExprSet[] earliest;
	private ExprSet[] postponableIn;
	private ExprSet[] latest;
	private ExprSet[] e_use;
	private ExprSetMap latestComplementMap;
	private ControlFlowGraph cfg;
	
	public Latest(ControlFlowGraph cfg, 
			ExprSetMap earliestMap, 
			ExprSetMap postponableInMap, 
			ExprSetMap e_useMap, 
			ExprSetMap latestComplementMap) {
		this.earliest = earliestMap.getEntry(cfg.getMethod().toString());
		this.postponableIn = postponableInMap.getEntry(cfg.getMethod().toString());
		this.e_use = e_useMap.getEntry(cfg.getMethod().toString());
		this.cfg = cfg;
		this.latest = null;
		this.latestComplementMap = latestComplementMap;
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
		ExprSet[] localE_use = new ExprSet[this.earliest.length];
		
		for (int i = 0; i < localE_use.length; ++i) {
			localE_use[i] = new ExprSet();
			localE_use[i].copy(this.e_use[i]);
		}
		
		// This horrendous operation is defined @p649.
		// latest[B] = (earliest[B] SUM postponableIn[B]) INTERSECT (e_use[B] SUM ~(INTERSECT[S=succ(B)](earliest[S] SUM postponableIn[S]))
		for (int i = 0; i < this.latest.length; ++i) {
			// earliest[B] SUM postponableIn[B]
			this.latest[i] = new ExprSet();
			this.latest[i].copy(this.earliest[i]);
			this.latest[i].sumWith(this.postponableIn[i]);
			
			// e_use[B] SUM ~(INTERSECT[S=succ(B)](earliest[S] SUM postponableIn[S])
			localE_use[i].sumWith(complementOfIntersectionOfSuccessors[i]);
			
			// the Big INTERSECT
			// TODO BUG: Is this intersection strict?
			this.latest[i].intersectWith(localE_use[i], true);
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
		
		this.latestComplementMap.saveEntry(this.cfg.getMethod().toString(), complement);
		
		return complement;
	}
}
