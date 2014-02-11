package submit;

import java.util.*;
import joeq.Compiler.Quad.*;
import flow.Flow;

/**
 * Skeleton class for implementing the Flow.Solver interface.
 */
public class MySolver implements Flow.Solver {

    protected Flow.Analysis analysis;

    /**
     * Sets the analysis.  When visitCFG is called, it will
     * perform this analysis on a given CFG.
     *
     * @param analyzer The analysis to run
     */
    public void registerAnalysis(Flow.Analysis analyzer) {
        this.analysis = analyzer;
    }

    /**
     * Runs the solver over a given control flow graph.  Prior
     * to calling this, an analysis must be registered using
     * registerAnalysis
     *
     * @param cfg The control flow graph to analyze.
     */
    public void visitCFG(ControlFlowGraph cfg) {

        // this needs to come first.
        analysis.preprocess(cfg);

        if (analysis.isForward()) {
            QuadIterator qit = new QuadIterator(cfg, true);

            boolean changesToAnyOut = true;
            // While changes to any out occur, iterate over basic blocks.
            while (changesToAnyOut) {
                qit = new QuadIterator(cfg, true);
                changesToAnyOut = false;

                // For each basic block B different than Entry compute out[B] and in[B].
                // We don't need to check if it's Entry/Exit quad, CFG does that for us.                
                while (qit.hasNext()) {
                    Quad q = qit.next();

                    Flow.DataflowObject previousOut = analysis.newTempVar();
                    previousOut.copy(analysis.getOut(q));

                    // in[B] = meet over predecessors P of B of out[P]
                    Flow.DataflowObject meetResult = meetOperation(qit.predecessors(), true); 
                    analysis.setIn(q, meetResult);

                    // processQuad also performs the computation:
                    // out[B] = f_b(in[B])
                    analysis.processQuad(q);

                    changesToAnyOut |= (!previousOut.equals(analysis.getOut(q)));
                }
            }

            // Compute Exit value by looking at outs of quads that lead to exit
            Set<Quad> exitQuads = new HashSet<Quad>();
            qit = new QuadIterator(cfg, true);
            // Make a list of quads that go to exit
            while (qit.hasNext()) {
                Quad q = qit.next();
                if(isBoundaryQuad(qit.successors())) exitQuads.add(q);
            }
            // in[EXIT] = meet over predecessors P of EXIT of out[P]
            analysis.setExit(meetOperation(exitQuads.iterator(), true)); 

        } else {
            QuadIterator qit = new QuadIterator(cfg, false);

            boolean changesToAnyIn = true;
            // While changes to any out occur, iterate over basic blocks.
            while (changesToAnyIn) {
                qit = new QuadIterator(cfg, false);
                changesToAnyIn = false;

                // For each basic block B different than Entry compute out[B] and in[B].
                // We don't need to check if it's Entry/Exit quad, CFG does that for us.
                while (qit.hasPrevious()) {
                    Quad q = qit.previous();

                    Flow.DataflowObject previousIn = analysis.newTempVar();
                    previousIn.copy(analysis.getIn(q));

                    // out[B] = meet over successors P of B of in[P]
                    Flow.DataflowObject meetResult = meetOperation(qit.successors(), false); 
                    analysis.setOut(q, meetResult);

                    // processQuad also performs the computation:
                    // in[B] = f_b(out[B])
                    analysis.processQuad(q);

                    changesToAnyIn |= (!previousIn.equals(analysis.getIn(q)));
                }
            }

            // Compute Entry value by looking at ins of quads that lead to entry
            Set<Quad> entryQuads = new HashSet<Quad>();
            qit = new QuadIterator(cfg, false);
            // Make a list of quads that go to exit
            while (qit.hasPrevious()) {
                Quad q = qit.previous();
                if(isBoundaryQuad(qit.predecessors())) entryQuads.add(q);
            }
            // out[ENTRY] = meet over successors P of ENTRY of in[P]
            analysis.setEntry(meetOperation(entryQuads.iterator(), false)); 
            
        }
        
        // this needs to come last.
        analysis.postprocess(cfg);
    }

    // Iterator qit can include null value corresponding to Entry/Exit accordingly.
    private Flow.DataflowObject meetOperation(Iterator<Quad> qit, boolean out) {
        Flow.DataflowObject meetResult = analysis.newTempVar();
        Quad q;

        if (qit.hasNext()) {
            q = qit.next();

            // Initialize meetResult with any quad's in/out value.
            if (out) {
                if (q == null) {
                    meetResult.copy(analysis.getEntry());
                } else {
                  meetResult.copy(analysis.getOut(q));
                } 
            } else {
                if (q == null) {
                    meetResult.copy(analysis.getExit());
                } else {
                   meetResult.copy(analysis.getIn(q));
                }
            }

            // Rely on meet's properties.
            while (qit.hasNext()) {
                q = qit.next();
                
                if (q == null) continue;

                if (out) {
                    meetResult.meetWith(analysis.getOut(q));
                } else {
                    meetResult.meetWith(analysis.getIn(q));
                }
            }
        }

        return meetResult;
    }

    // Can be used to check if block is fromEntry or toExit
    // fromEntry : pass predecessors to qit
    // toExit: pass successors to qit
    private boolean isBoundaryQuad(Iterator<Quad> qit) {
        Quad q;
        boolean isBoundary = false;
        
        while (qit.hasNext()) {
            q = qit.next();
            if (q == null) {
            	isBoundary = true;
            	break;
            }
        }
        
        return isBoundary;
    }
}
