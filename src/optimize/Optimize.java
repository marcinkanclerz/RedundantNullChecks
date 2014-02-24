package optimize;

import java.util.List;

import submit.*;
import submit.expr.ExprSet;
import joeq.Class.jq_Class;
import joeq.Main.Helper;
import flow.Flow;

public class Optimize {
    /*
     * optimizeFiles is a list of names of class that should be optimized
     * if nullCheckOnly is true, disable all optimizations except "remove redundant NULL_CHECKs."
     */
    public static void optimize(List<String> optimizeFiles, boolean nullCheckOnly) {
        Flow.Solver solver = new MySolver();
        for (int i = 0; i < optimizeFiles.size(); i++) {
            jq_Class classes = (jq_Class)Helper.load(optimizeFiles.get(i));
            // Run your optimization on each classes.
            if(nullCheckOnly){
            	Flow.Analysis analysis = new NullChecks();
                solver.registerAnalysis(analysis);
                Helper.runPass(classes, solver);
            } else {
                AnticipatedExpressions anticipatedExpressions = new AnticipatedExpressions();
                solver.registerAnalysis(anticipatedExpressions);
                Helper.runPass(classes, solver);
                
                AvailableExpressions availableExpressions = new AvailableExpressions(anticipatedExpressions.getInResult());
                solver.registerAnalysis(availableExpressions);
                Helper.runPass(classes, solver);
                
                EarliestVisitor earliestVisitor = new EarliestVisitor(anticipatedExpressions.getInResult(), availableExpressions.getInResult());
                ExprSet[] earliest = earliestVisitor.getEarliest();
                
//                for (int j = 0; j < earliest.length; ++j) {
//                	System.out.println(j + " " + earliest[j].toString());
//                }
                
                PostponableExpressions postponableExpressions = new PostponableExpressions(earliest);
                solver.registerAnalysis(postponableExpressions);
                Helper.runPass(classes, solver);
                
                UsedExpressions usedExpressions = new UsedExpressions(earliest, postponableExpressions.getInResult());
                solver.registerAnalysis(usedExpressions);
                Helper.runPass(classes, solver);
                
                // Latest is computed in usedExpressions.preprocess, cause it depends on CFG and this was the easiest
                // way to thread the CFG through the system into computation of Latest.
                ExprSet[] latest = usedExpressions.getLatest();
                ExprSet[] usedOut = usedExpressions.getUsedOut();
                ExprSet[] e_use = usedExpressions.getE_use();
                
                /**
                 * The code below is just a stub showing how all these sets could be used to compute
                 * the quads where the expression should be actually computed or replaced.
                 * 
                 * I realized some shortcomings of my code for the actual removal of quads but 
                 * it feels fixable :-)
                 */
                
                // TODO BUG e_use is of different size than anything else - why? Maybe because it's below Ballmer's Peak?!
                
                ExprSet[] addTemporaryAtTheBeginning = new ExprSet[latest.length];
                
                System.out.println("Add temporary at the beginning of:");
                for (int j = 0; j < latest.length; ++j) {
                	addTemporaryAtTheBeginning[j] = new ExprSet();
                	// As in 8.b, @p654:
                	// latest[B] INTERSECT usedOut[B]
                	addTemporaryAtTheBeginning[j].copy(latest[j]);
                	addTemporaryAtTheBeginning[j].intersectWith(usedOut[j], true);
                	System.out.println(j + ": " + addTemporaryAtTheBeginning[j].toString());
                }
                
                ExprSet[] replaceExpressionByTemporary = new ExprSet[latest.length];
                
                System.out.println("Replace expression with temporary at:");
                for (int j = 0; j < latest.length; ++j) {
                	replaceExpressionByTemporary[j] = new ExprSet();
                	// As in 8.c, @p654:
                	// e_use[B] INTERSECT (~latest[B] SUM usedOut[B])
                	replaceExpressionByTemporary[j].copy(e_use[j]);
                	ExprSet latestComplement = latest[j].complement(); 
                	latestComplement.sumWith(usedOut[j]);
                	replaceExpressionByTemporary[j].intersectWith(latestComplement, true);
                	System.out.println(j + ": " + replaceExpressionByTemporary[j].toString());
                }
            }
        }
    }
}
