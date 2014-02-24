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
            }
        }
    }
}
