package submit;

import java.util.*;

import joeq.Compiler.Quad.*;
import joeq.Main.Helper;
import flow.Flow;
import submit.expr.*;

public class AvailableExpressions extends BaseExprAnalysis implements Flow.Analysis {
	private AvailableExpressionsTransferFunction transferFunction;
	private ExprSetMap anticipatedInMap;
	private ExprSetMap availableInMap;
	
	private ExprSet[] anticipatedIn;
	
	public AvailableExpressions(ExprSetMap anticipatedInMap, ExprSetMap availableInMap) {
		this.anticipatedInMap = anticipatedInMap;
		this.availableInMap = availableInMap;
	}
	
	/**
	 * Perform initializations.
	 */
	public void preprocess(ControlFlowGraph cfg) {
		// Get size of the CFG.
		int cfgSize = BaseExprAnalysis.getCfgSize(cfg);
		
		// Cope with multiple methods.
		this.anticipatedIn = this.anticipatedInMap.getEntry(cfg.getMethod().toString());
		
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
        this.transferFunction = new AvailableExpressionsTransferFunction(this.anticipatedIn);
        this.transferFunction.val = new ExprSet();
	}
	
	public void processQuad(Quad q) {
		this.transferFunction.val.copy(this.in[q.getID()]);
        Helper.runPass(q, this.transferFunction);
        this.out[q.getID()].copy(this.transferFunction.val);
	}
	
	public void postprocess(ControlFlowGraph cfg) {
		// Cope with multiple methods.
		this.availableInMap.saveEntry(cfg.getMethod().toString(), this.in);
	}
	
	/**
	 * Available Expressions is FORWARDS.
	 */
	public boolean isForward() {
		return true;
	}
}
