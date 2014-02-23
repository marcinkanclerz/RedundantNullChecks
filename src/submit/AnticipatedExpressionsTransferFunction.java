package submit;

import java.util.*;

import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.Operand.*;
import joeq.Main.Helper;
import flow.Flow;
import submit.expr.*;

public class AnticipatedExpressionsTransferFunction extends QuadVisitor.EmptyVisitor {
	public ExprSet val;
	
	@Override
	public void visitBinary(Quad q) {
        Expr e_use = new BinaryExpr(q, Operation.Gen);
        Expr e_kill = new BinaryExpr(q, Operation.Kill);
        
        // e_use and e_kill are defined @p611.
        this.transferFunction(new ExprSet(e_use), new ExprSet(e_kill));
	}
	
	private void transferFunction(ExprSet e_use, ExprSet e_kill) {
		// Operation defined @p649.
		// e_use[B] SUM (x - e_kill[B])
		this.val.remove(e_kill, false);
		this.val.sumWith(e_use);
	}
}
