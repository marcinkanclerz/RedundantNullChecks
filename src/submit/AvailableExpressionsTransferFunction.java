package submit;

import submit.expr.*;
import joeq.Compiler.Quad.*;

public class AvailableExpressionsTransferFunction extends QuadVisitor.EmptyVisitor {
	public ExprSet val;
	
	private ExprSet[] anticipatedIn;
	
	public AvailableExpressionsTransferFunction(ExprSet[] anticipatedIn) {
		this.anticipatedIn = anticipatedIn;
	}
	
	@Override
	public void visitBinary(Quad q) {
		ExprSet anticipatedIn = this.anticipatedIn[q.getID()];
		Expr e_kill = new BinaryExpr(q, Operation.Kill);
		
		// anticipated[B].in is defined @p649, e_kill is defined @p611.
		this.transferFunction(anticipatedIn, new ExprSet(e_kill));
	}
	
	private void transferFunction(ExprSet anticipatedIn, ExprSet e_kill) {
		// Operation defined @p649.
		// (anticipated[B].in SUM x) - e_kill[B]
		this.val.sumWith(anticipatedIn);
		this.val.remove(e_kill, false);
	}
}
