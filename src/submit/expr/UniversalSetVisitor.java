package submit.expr;

import java.util.Set;
import java.util.TreeSet;

import joeq.Compiler.Quad.ControlFlowGraph;
import joeq.Compiler.Quad.Quad;
import joeq.Compiler.Quad.QuadIterator;
import joeq.Compiler.Quad.QuadVisitor;
import joeq.Compiler.Quad.Operand.RegisterOperand;
import joeq.Main.Helper;

public class UniversalSetVisitor extends QuadVisitor.EmptyVisitor {
	private ExprSet universal;
	private ControlFlowGraph cfg;
	
	public UniversalSetVisitor(ControlFlowGraph cfg) {
		this.cfg = cfg;
		this.universal = null;
	}
	
	// TODO Remove.
	/*public void magic() {
        // Create and setup universalSet (Important since meet function is intersection) 
        Set<String> s = new TreeSet<String>();
        VarSet.universalSet = s;
        // Add the arguments of methods to universalSet
        int numargs = cfg.getMethod().getParamTypes().length;
        for (int i = 0; i < numargs; i++) {
            s.add("R"+i);
        }
        // Add the defined and used registers to universalSet
        qit = new QuadIterator(cfg);
        while (qit.hasNext()) {
            Quad q = qit.next();
            for (RegisterOperand def : q.getDefinedRegisters()) {
                s.add(def.getRegister().toString());
            }
            for (RegisterOperand use : q.getUsedRegisters()) {
                s.add(use.getRegister().toString());
            }
        }
	}*/
	
	public ExprSet getUniversalSet() {
		if (this.universal == null) {
			this.universal = new ExprSet();
			Helper.runPass(this.cfg, this);
		}
		
		return this.universal;
	}
	
	@Override 
	public void visitBinary(Quad q) {
		Expr expr = new BinaryExpr(q, Operation.Gen);
		this.universal.sumWith(new ExprSet(expr));
	}
}
