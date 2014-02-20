package optimize;

import java.util.List;
import joeq.Class.jq_Class;
import joeq.Main.Helper;
import submit.MySolver;
import submit.NullChecks;
import flow.Flow;

public class Optimize {
    /*
     * optimizeFiles is a list of names of class that should be optimized
     * if nullCheckOnly is true, disable all optimizations except "remove redundant NULL_CHECKs."
     */
    public static void optimize(List<String> optimizeFiles, boolean nullCheckOnly) {
        Flow.Solver solver = new MySolver();
        Flow.Analysis analysis;
        for (int i = 0; i < optimizeFiles.size(); i++) {
            jq_Class classes = (jq_Class)Helper.load(optimizeFiles.get(i));
            // Run your optimization on each classes.
            if(nullCheckOnly){
                analysis = new NullChecks();
                solver.registerAnalysis(analysis);
                Helper.runPass(classes, solver);
            }
            else{
              /*
                analysis = new NullChecks();
                solver.registerAnalysis(analysis);
                Helper.runPass(classes, solver);

                analysis = new SomeOtherAnalysis();
                solver.registerAnalysis(analysis);
                Helper.runPass(classes, solver);

                .
                .
                .

              */
            }
        }
    }
}
