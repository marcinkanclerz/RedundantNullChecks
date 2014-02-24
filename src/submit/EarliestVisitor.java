package submit;

import submit.expr.*;
import joeq.Compiler.Quad.QuadVisitor;

public class EarliestVisitor extends QuadVisitor.EmptyVisitor {
	private ExprSet[] earliest, anticipatedIn, availableIn;
	
	public EarliestVisitor(ExprSet[] anticipatedIn, ExprSet[] availableIn) {
		this.anticipatedIn = anticipatedIn;
		this.availableIn = availableIn;
		this.earliest = null;
	}
	
	public ExprSet[] getEarliest() {
		if (this.earliest == null) {
			this.earliest = new ExprSet[this.anticipatedIn.length];
			
			for (int i = 0; i < this.earliest.length; ++i) {
				// Operation defined @p649.
				// earliest[B] = anticipated[B].in - available[B].in
				this.earliest[i] = new ExprSet();
				this.earliest[i].copy(this.anticipatedIn[i]);
				this.earliest[i].remove(this.availableIn[i], false);
			}
		}
		
		return this.earliest;
	}
}
