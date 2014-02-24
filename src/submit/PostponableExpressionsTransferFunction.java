package submit;

import submit.expr.*;
import joeq.Compiler.Quad.*;

public class PostponableExpressionsTransferFunction extends QuadVisitor.EmptyVisitor {
	public ExprSet val;
	
	private ExprSet[] earliest;
	
	public PostponableExpressionsTransferFunction(ExprSet[] earliest) {
		this.earliest = earliest;
	}
	
	@Override
	public void visitBinary(Quad q) {
		ExprSet earliest = this.earliest[q.getID()];
		Expr e_use = new BinaryExpr(q, Operation.Gen);
		
		// earliest[B] is defined @p649, e_use is defined @p611.
		this.transferFunction(earliest, new ExprSet(e_use));
	}
	
	private void transferFunction(ExprSet earliest, ExprSet e_use) {
		// Operation defined @p649.
		// (earliest[B] SUM x) - e_use[B]
		this.val.sumWith(earliest);
		this.val.remove(e_use, false);
	}
}
