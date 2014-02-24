package submit;

import java.util.Iterator;

import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.Quad;
import joeq.Main.Helper;
import submit.expr.*;
import flow.Flow;

public class UsedExpressions extends BaseExprAnalysis implements Flow.Analysis {
	private UsedExpressionsTransferFunction transferFunction;
	private ExprSet[] earliest;
	private ExprSet[] postponableIn;
	private ExprSet[] latest;
	
	public UsedExpressions(ExprSet[] earliest, ExprSet[] postponableIn) {
		this.earliest = earliest;
		this.postponableIn = postponableIn;
	}
	
	/**
	 * Perform initializations.
	 */
	public void preprocess(ControlFlowGraph cfg) {
		// Get size of the CFG.
		int cfgSize = BaseExprAnalysis.getCfgSize(cfg);
		
        // Create universal set of expressions, according to definition of ExprSet.
        ExprSet.setupLattice(cfg, false /* meet is NOT intersection, it is sum */, false /* top is NOT universal, it is empty set */);

        // Allocate and initialize the in and out arrays.
        this.in = new ExprSet[cfgSize];
        this.out = new ExprSet[cfgSize];
        
        for (int i = 0; i < cfgSize; ++i) {
            this.in[i] = new ExprSet();
            this.out[i] = new ExprSet(); 
            this.in[i].setToTop();
        }
        
        // Initialize the entry and exit points.
        this.entry = new ExprSet();
        this.exit = new ExprSet();
        
        LatestVisitor latestVisitor = new LatestVisitor(cfg, this.earliest, this.postponableIn);
        this.latest = latestVisitor.getLatest();
        
        // Initialize transfer function.
        this.transferFunction = new UsedExpressionsTransferFunction(this.latest);
        this.transferFunction.val = new ExprSet();
	}
	
	public void processQuad(Quad q) {
        this.transferFunction.val.copy(out[q.getID()]);
        Helper.runPass(q, this.transferFunction);
        this.in[q.getID()].copy(this.transferFunction.val);
	}
	
	public ExprSet[] getUsedOut() {
		return this.out;
	}
	
	public ExprSet[] getLatest() {
		return this.latest;
	}
	
	
	/**
	 * TODO Do nothing in once sent to submission.
	 */
	public void postprocess(ControlFlowGraph cfg) { 
		System.out.println(cfg.getMethod().toString());

		System.out.println("Universal set:");
		for(Iterator<Expr> it = ExprSet.getUniveralSet().iterator(); it.hasNext();) {
			Expr expr = it.next();
			System.out.println(((BinaryExpr)expr).toString());
		}
		
		System.out.println("Used out:");
		int cfgSize = BaseExprAnalysis.getCfgSize(cfg);
		for (int i = 0; i < cfgSize; ++i) {
			System.out.println(i + ": " + this.in[i].toString());
		}
	}
	
	/**
	 * Anticipated Expressions is BACKWARDS.
	 */
	public boolean isForward() {
		return false;
	}
}
