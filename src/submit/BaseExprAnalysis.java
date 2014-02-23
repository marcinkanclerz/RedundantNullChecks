package submit;

import java.util.*;


import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.Operand.*;
import joeq.Main.Helper;
import flow.Flow;
import flow.ConstantProp.ConstantPropTable;
import submit.expr.*;

/**
 * Base class for all analyses in PRE providing common functionality.
 */
public class BaseExprAnalysis {
	protected ExprSet[] in, out;
	protected ExprSet entry, exit;
	
	/**
	 * Returns size of the CFG.
	 */
	protected static int getCfgSize(ControlFlowGraph cfg) {
        QuadIterator qit = new QuadIterator(cfg);
        int max = 0;
        
        while (qit.hasNext()) {
            int id = qit.next().getID();
            if (id > max) 
                max = id;
        }
        
        return max + 1;
	}
	
	/**
	 * Generic implementation of Flow.Analysis for ExprSet. 
	 */
    public ExprSet getEntry() { 
    	ExprSet result = newTempVar();
        result.copy(entry); 
        return result;
    }
    
    /**
	 * Generic implementation of Flow.Analysis for ExprSet. 
	 */
    public ExprSet getExit() { 
        ExprSet result = newTempVar();
        result.copy(exit); 
        return result;
    }
    
    /**
	 * Generic implementation of Flow.Analysis for ExprSet. 
	 */
    public ExprSet getIn(Quad q) { 
        ExprSet result = newTempVar();
        result.copy(in[q.getID()]); 
        return result;
    }
    
    /**
	 * Generic implementation of Flow.Analysis for ExprSet. 
	 */
    public ExprSet getOut(Quad q) { 
        ExprSet result = this.newTempVar();
        result.copy(out[q.getID()]); 
        return result;
    }
    
    /**
	 * Generic implementation of Flow.Analysis for ExprSet. 
	 */
    public void setIn(Quad q, Flow.DataflowObject value) { 
        in[q.getID()].copy(value); 
    }
    
    /**
	 * Generic implementation of Flow.Analysis for ExprSet. 
	 */
    public void setOut(Quad q, Flow.DataflowObject value) { 
        out[q.getID()].copy(value); 
    }
    
    /**
	 * Generic implementation of Flow.Analysis for ExprSet. 
	 */
    public void setEntry(Flow.DataflowObject value) { 
        entry.copy(value); 
    }
    
    /**
	 * Generic implementation of Flow.Analysis for ExprSet. 
	 */
    public void setExit(Flow.DataflowObject value) { 
        exit.copy(value); 
    }

    /**
	 * Generic implementation of Flow.Analysis for ExprSet. 
	 */
    public ExprSet newTempVar() { 
    	return new ExprSet(); 
	}
}
