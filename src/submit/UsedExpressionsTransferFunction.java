package submit;

import joeq.Compiler.Quad.*;
import submit.expr.*;

public class UsedExpressionsTransferFunction extends QuadVisitor.EmptyVisitor {
	public ExprSet val;
	private ExprSet[] latest;
	
	public UsedExpressionsTransferFunction(ExprSet[] latest) {
		this.latest = latest;
	}
	
	@Override
	public void visitBinary(Quad q) {
		Expr e_use = new BinaryExpr(q, Operation.Gen);
		ExprSet latest = this.latest[q.getID()];
		
		// e_use is defined @p611, latest is defined @p649.
		this.transferFunction(new ExprSet(e_use), latest);
	}
	
	private void transferFunction(ExprSet e_use, ExprSet latest) {
		// Operation defined @p649.
		// (e_use[B] SUM x) - latest[B]
		this.val.sumWith(e_use);
		// TODO BUG: Strict? Or not?
		this.val.remove(latest, false);
	}
}
