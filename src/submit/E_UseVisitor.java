package submit;

import submit.expr.*;
import joeq.Compiler.Quad.*;
import joeq.Main.Helper;

public class E_UseVisitor extends QuadVisitor.EmptyVisitor {
	private ExprSet[] e_use;
	private ControlFlowGraph cfg;
	private int cfgSize;
	
	public E_UseVisitor(ControlFlowGraph cfg, int cfgSize) {
		this.cfgSize = cfgSize;
		this.cfg = cfg;
		this.e_use = null;
	}
	
	public ExprSet[] getE_use() {
		if (this.e_use == null) {
			this.e_use = new ExprSet[this.cfgSize];
			
			for (int i = 0; i < this.e_use.length; ++i) {
				this.e_use[i] = new ExprSet();
			}
			
			Helper.runPass(this.cfg, this);
		}
		
		return this.e_use;
	}
	
	@Override
	public void visitBinary(Quad q) {
		this.e_use[q.getID()] = new ExprSet(new BinaryExpr(q, Operation.Gen)); 
	}
}
