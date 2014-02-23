package submit.expr;

import java.util.*;

public interface Expr {
	Expr clone();
	boolean isCongruent(Expr rhs, boolean strict);
}
