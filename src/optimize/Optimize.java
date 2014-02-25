package optimize;

import java.util.List;

import submit.*;
import submit.expr.*;
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
            	Flow.Analysis analysis = new NullChecks(false);
                solver.registerAnalysis(analysis);
                Helper.runPass(classes, solver);
            } else {
            	ExprSetMap anticipatedInMap = new ExprSetMap();
                AnticipatedExpressions anticipatedExpressions = new AnticipatedExpressions(anticipatedInMap);
                solver.registerAnalysis(anticipatedExpressions);
                Helper.runPass(classes, solver);
                
                ExprSetMap availableInMap = new ExprSetMap();
                AvailableExpressions availableExpressions = new AvailableExpressions(anticipatedInMap, availableInMap);
                solver.registerAnalysis(availableExpressions);
                Helper.runPass(classes, solver);
                
                ExprSetMap earliestMap = new ExprSetMap();
                Earliest earliestComputation = new Earliest(anticipatedInMap, availableInMap, earliestMap);
                earliestComputation.doTheMagic();
                
                ExprSetMap postponableInMap = new ExprSetMap();
                PostponableExpressions postponableExpressions = new PostponableExpressions(earliestMap, postponableInMap);
                solver.registerAnalysis(postponableExpressions);
                Helper.runPass(classes, solver);
                
                ExprSetMap latestMap = new ExprSetMap();
                ExprSetMap latestComplementMap = new ExprSetMap();
                ExprSetMap usedOutMap = new ExprSetMap();
                ExprSetMap e_useMap = new ExprSetMap();
                UsedExpressions usedExpressions = new UsedExpressions(earliestMap, 
                		postponableInMap, 
                		latestMap, 
                		latestComplementMap, 
                		usedOutMap,
                		e_useMap);
                solver.registerAnalysis(usedExpressions);
                Helper.runPass(classes, solver);
                
                PreOutput preOutput = new PreOutput(latestMap, latestComplementMap, usedOutMap, e_useMap);
                preOutput.doTheMagic();

                preOutput.printOutput();
            }
        }
    }
}

