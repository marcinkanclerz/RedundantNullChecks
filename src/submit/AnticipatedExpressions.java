package submit;

import java.util.*;
import java.util.logging.ConsoleHandler;

import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.Operand.*;
import joeq.Main.Helper;
import flow.Flow;
import submit.expr.*;

public class AnticipatedExpressions extends BaseExprAnalysis implements Flow.Analysis {
	private AnticipatedExpressionsTransferFunction transferFunction;
	
	/**
	 * Perform initializations.
	 */
	public void preprocess(ControlFlowGraph cfg) {
		// Get size of the CFG.
		int cfgSize = BaseExprAnalysis.getCfgSize(cfg);

        // Allocate and initialize the in and out arrays.
        this.in = new ExprSet[cfgSize];
        this.out = new ExprSet[cfgSize];
        
        for (int i = 0; i < cfgSize; ++i) {
            this.in[i] = new ExprSet();
            this.out[i] = new ExprSet(); 
            this.in[i].setToTop();
        }
        
        // Create universal set of expressions, according to definition of ExprSet.
        ExprSet.setupLattice(cfg, true /* meet is intersection */, true /* top is universal */);
        
        // Initialize the entry and exit points.
        this.entry = new ExprSet();
        this.exit = new ExprSet();
        
        // Initialize transfer function.
        this.transferFunction = new AnticipatedExpressionsTransferFunction();
        this.transferFunction.val = new ExprSet();
	}
	
	public void processQuad(Quad q) {
        this.transferFunction.val.copy(out[q.getID()]);
        Helper.runPass(q, this.transferFunction);
        this.transferFunction.visitQuad(q);
        this.in[q.getID()].copy(this.transferFunction.val);
	}
	
	public void postprocess(ControlFlowGraph cfg) { 
		System.out.println(cfg.getMethod().toString());

		System.out.println("Universal set:");
		for(Iterator<Expr> it = ExprSet.getUniveralSet().iterator(); it.hasNext();) {
			Expr expr = it.next();
			System.out.println(((BinaryExpr)expr).toString());
		}
		
		System.out.println("Anticipated out:");
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
