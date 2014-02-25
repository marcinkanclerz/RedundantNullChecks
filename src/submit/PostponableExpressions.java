package submit;

import java.util.Iterator;

import joeq.Compiler.Quad.*;
import joeq.Main.Helper;
import submit.expr.*;
import flow.Flow;

public class PostponableExpressions extends BaseExprAnalysis implements Flow.Analysis {
	private PostponableExpressionsTransferFunction transferFunction;
	private ExprSetMap earliestMap, postponableInMap;
	
	private ExprSet[] earliest;
	
	public PostponableExpressions(ExprSetMap earliestMap, ExprSetMap postponableInMap) {
		this.earliestMap = earliestMap;
		this.postponableInMap = postponableInMap;
	}
	
	/**
	 * Perform initializations.
	 */
	public void preprocess(ControlFlowGraph cfg) {
		// Get size of the CFG.
		int cfgSize = BaseExprAnalysis.getCfgSize(cfg);
		
		// Cope with multiple methods.
		this.earliest = this.earliestMap.getEntry(cfg.getMethod().toString());
		
        // Create universal set of expressions, according to definition of ExprSet.
        ExprSet.setupLattice(cfg, true /* meet is intersection */, true /* top is universal */);

        // Allocate and initialize the in and out arrays.
        this.in = new ExprSet[cfgSize];
        this.out = new ExprSet[cfgSize];
        
        for (int i = 0; i < cfgSize; ++i) {
            this.in[i] = new ExprSet();
            this.out[i] = new ExprSet(); 
            this.out[i].setToTop();
        }
        
        // Initialize the entry and exit points.
        this.entry = new ExprSet();
        this.exit = new ExprSet();
        
        // Initialize transfer function.
        this.transferFunction = new PostponableExpressionsTransferFunction(this.earliest);
        this.transferFunction.val = new ExprSet();
	}
	
	public void processQuad(Quad q) {
		this.transferFunction.val.copy(this.in[q.getID()]);
        Helper.runPass(q, this.transferFunction);
        this.out[q.getID()].copy(this.transferFunction.val);
	}
	
	// TODO In submit this should be empty.
	public void postprocess(ControlFlowGraph cfg) {
		// Cope with multiple methods.
		this.postponableInMap.saveEntry(cfg.getMethod().toString(), this.in);
		
//		System.out.println(cfg.getMethod().toString());
//
//		System.out.println("Universal set:");
//		for(Iterator<Expr> it = ExprSet.getUniveralSet().iterator(); it.hasNext();) {
//			Expr expr = it.next();
//			System.out.println(((BinaryExpr)expr).toString());
//		}
//		
//		System.out.println("Anticipated out:");
//		int cfgSize = BaseExprAnalysis.getCfgSize(cfg);
//		for (int i = 0; i < cfgSize; ++i) {
//			System.out.println(i + ": " + this.in[i].toString());
//		}
	}
	
	/**
	 * Postponable Expressions is FORWARDS.
	 */
	public boolean isForward() {
		return true;
	}
}
