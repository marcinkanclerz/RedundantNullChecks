package submit.expr;

import java.util.*;

import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.Operand.*;

/**
 * TODO:
 *  * Is this called on more than a+b expressions?
 */
public class BinaryExpr implements Expr {
	private static Set<String> commutativeOperators = new TreeSet<String>(Arrays.asList("ADD_I", "MUL_I"));
	
	private Operation operation;
	
	public String op1;
	public String op2;
	public String operator;
	public String dst;
	
	/**
	 * Testing purposes.
	 */
	public BinaryExpr(Operation operation, String op1, String op2, String operator, String dst) {
		this.operation = operation;
		this.op1 = op1;
		this.op2 = op2;
		this.operator = operator;
		this.dst = dst;
	}
	
	public BinaryExpr(Quad q, Operation operation) {
		this.operation = operation;
		
		this.op1 = BinaryExpr.GetOperandValue(Operator.Binary.getSrc1(q));
		this.op2 = BinaryExpr.GetOperandValue(Operator.Binary.getSrc2(q));
        this.operator = q.getOperator().toString();
        this.dst = Operator.Binary.getDest(q).getRegister().toString();
	}
	
	private static String GetOperandValue(Operand op) {
		String operandValue = null;
		
		if  (op instanceof RegisterOperand) {
			RegisterOperand reg = (RegisterOperand)op;
			operandValue = reg.getRegister().toString();
		} else if (op instanceof IConstOperand) {
			IConstOperand val = (IConstOperand)op;
			operandValue = Integer.toString(val.getValue());
		} else {
			System.out.println("Null operand.");
		}
		
		return operandValue;
	}
	
	private List<String> getList() {
		
		List<String> list = new LinkedList<String>();
		
		switch (this.operation) {
		case Gen:
			list.add(this.op1);
			list.add(this.op2);
			break;
		case Kill:
			list.add(this.dst);
			break;
		}
		
		return list;
	}
	
	public BinaryExpr clone() {
		BinaryExpr binaryExpr = new BinaryExpr(
			this.operation,
			this.op1,
			this.op2,
			this.operator,
			this.dst);
		
		return binaryExpr;
	}
	
	@Override
	public String toString() {
		String s = "";
		switch (this.operation) {
		case Gen:
			s = "Gen: {" + this.op1 + ", " + this.op2 + ", " + this.operator + "}";
			break;
		case Kill:
			s = "Kill: {" + this.dst + "}";
			break;
		}
		// this.dst + " = " + this.op1 + " " + this.operator + " " + this.op2;
		return s;
	}
	
	public boolean isCongruent(Expr rhsExpr, boolean strict) {
		boolean isCongruent = false;
		
		if (rhsExpr instanceof BinaryExpr) {
			List<String> lhsList = this.getList();
			List<String> rhsList = ((BinaryExpr)rhsExpr).getList();
			
			if (strict) {
				if (BinaryExpr.commutativeOperators.contains(this.operator)) {
					isCongruent = lhsList.containsAll(rhsList) && rhsList.containsAll(lhsList) && this.operator.equals(((BinaryExpr)rhsExpr).operator);
				} else {
					if (lhsList.size() == rhsList.size()) {
						boolean equal = true;
						for (int i = 0; i < lhsList.size(); ++i) {
							if (!lhsList.get(i).equals(rhsList.get(i))) {
								equal = false;
								break;
							}
						}
						isCongruent = equal && this.operator.equals(((BinaryExpr)rhsExpr).operator);
					}
				}
			} else {
				// Operator doesn't matter.
				for (Iterator<String> it = rhsList.iterator(); it.hasNext();) {
					String s = it.next();
					if (lhsList.contains(s)) {
						isCongruent = true;
						break;
					}
				}
			}
		}
		
		return isCongruent;
	}
}
