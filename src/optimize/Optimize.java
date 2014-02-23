package optimize;

import java.util.List;
import submit.*;
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
                Flow.Analysis anticipatedExpressions = new AnticipatedExpressions();
                solver.registerAnalysis(anticipatedExpressions);
                Helper.runPass(classes, solver);

//                analysis = new SomeOtherAnalysis();
//                solver.registerAnalysis(analysis);
//                Helper.runPass(classes, solver);
            }
        }
    }
}
