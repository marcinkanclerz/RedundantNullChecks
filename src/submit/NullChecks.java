package submit;

// some useful things to import. add any additional imports you need.
import java.util.*;
import joeq.Compiler.Quad.*;
import joeq.Compiler.Quad.Operand.*;
import joeq.Main.Helper;
import flow.Flow;

/**
 * Skeleton class for implementing a reaching definition analysis
 * using the Flow.Analysis interface.
 */
public class NullChecks implements Flow.Analysis {

    /**
     * Class for the dataflow objects in the ReachingDefs analysis.
     * You are free to change this class or move it to another file.
     */
    public static class VarSet implements Flow.DataflowObject {
        private Set<String> set;  // set that holds set of nullchecked vars at a point
        public static Set<String> universalSet; // set of all vars        
        public VarSet() { set = new TreeSet<String>(); }

        public void setToTop() { set = new TreeSet<String>(universalSet); }
        public void setToBottom() { set = new TreeSet<String>(); }

        public void meetWith(Flow.DataflowObject o) 
        {
            VarSet a = (VarSet)o;
            set.retainAll(a.set);  // retainAll():Intersection. addAll():Union
        }

        public void copy(Flow.DataflowObject o) 
        {
            VarSet a = (VarSet) o;
            set = new TreeSet<String>(a.set);
        }
        public boolean contains(String s) 
        {
            return set.contains(s);
        }
        @Override
        public boolean equals(Object o) 
        {
            if (o instanceof VarSet) 
            {
                VarSet a = (VarSet) o;
                return set.equals(a.set);
            }
            return false;
        }
        @Override
        public int hashCode() {
            return set.hashCode();
        }
        @Override
        public String toString() 
        {
            return set.toString();
        }

        public void addVar(String v) {set.add(v);}
        public void removeVar(String v) {set.remove(v);}
    }

    /**
     * Dataflow objects for the interior and entry/exit points
     * of the CFG. in[ID] and out[ID] store the entry and exit
     * state for the input and output of the quad with identifier ID.
     *
     * You are free to modify these fields, just make sure to
     * preserve the data printed by postprocess(), which relies on these.
     */
    private VarSet[] in, out;
    private VarSet entry, exit;
    private String[] isNullCheckQuad; //is quad NULL_CHECK ? "<nullchecked var>" : "" 

    private boolean printOutput; //true for FindRedundantNullChecks, false for Optimize
    public NullChecks(boolean printout) { printOutput = printout; }

    /**
     * This method initializes the datflow framework.
     *
     * @param cfg  The control flow graph we are going to process.
     */
    public void preprocess(ControlFlowGraph cfg) {
        // this line must come first.
        //System.out.println("Method: "+cfg.getMethod().getName().toString());

        // get the amount of space we need to allocate for the in/out arrays.
        QuadIterator qit = new QuadIterator(cfg);
        int max = 0;
        while (qit.hasNext()) {
            int id = qit.next().getID();
            if (id > max) 
                max = id;
        }
        max += 1;


        // allocate the in and out arrays.
        in = new VarSet[max];
        out = new VarSet[max];
        isNullCheckQuad = new String[max];

        // Create and setup universalSet (Important since meet function is intersection) 
        Set<String> s = new TreeSet<String>();
        VarSet.universalSet = s;
        // Add the arguments of methods to universalSet
        int numargs = cfg.getMethod().getParamTypes().length;
        for (int i = 0; i < numargs; i++) {
            s.add("R"+i);
        }
        // Add the defined and used registers to universalSet
        qit = new QuadIterator(cfg);
        while (qit.hasNext()) {
            Quad q = qit.next();
            for (RegisterOperand def : q.getDefinedRegisters()) {
                s.add(def.getRegister().toString());
            }
            for (RegisterOperand use : q.getUsedRegisters()) {
                s.add(use.getRegister().toString());
            }
        }

        // initialize the entry and exit points.
        entry = new VarSet();
        exit = new VarSet();
        transferfn.val = new VarSet();
        for (int i=0; i<in.length; i++) {
            in[i] = new VarSet();
            out[i] = new VarSet(); out[i].setToTop();
            isNullCheckQuad[i] = "";
        }
        
        //System.out.println("Initialization completed.");
        /************************************************
         * Your remaining initialization code goes here *
         ************************************************/
    }

    /**
     * This method is called after the fixpoint is reached.
     * It must print out the dataflow objects associated with
     * the entry, exit, and all interior points of the CFG.
     * Unless you modify in, out, entry, or exit you shouldn't
     * need to change this method.
     *
     * @param cfg  Unused.
     */
    public void postprocess (ControlFlowGraph cfg) {
       /* 
        //// Useful debug info ////
        System.out.println("Method: "+cfg.getMethod().getName().toString());
        System.out.println("entry: " + entry.toString());
        for (int i=0; i<in.length; i++) {
            if (in[i] != null) {
                System.out.println(i + " in:  " + in[i].toString());
                System.out.println(i + " NullCheckVar: " + isNullCheckQuad[i]);
                System.out.println(i + " out: " + out[i].toString());
            }
        }
        System.out.println("exit: " + exit.toString());
        //// Useful debug info ////
        */
        // List to keep track of redundant quads
        List<Integer> redundantQuads = new ArrayList<Integer>();

        // Print out redundant quads (first part of assignment)
        if(printOutput){System.out.print(cfg.getMethod().getName().toString());}
        for (int i=0; i<in.length; i++) {
            if (in[i] != null) {
                if(in[i].contains(isNullCheckQuad[i])){ 
                    if(printOutput){System.out.print(" "+i);}
                    redundantQuads.add(i);
                }
            }
        }
        if(printOutput){System.out.println("");}

        // Remove redundant quads (second part of assignment)
        if(redundantQuads.size() > 0){
            for (int i = 0; i < redundantQuads.size(); i++) {
                //System.out.println("removing"+redundantQuads.get(i));
                QuadIterator qit = new QuadIterator(cfg);
                while (qit.hasNext()) {
                    int id = qit.next().getID();
                    //System.out.println("   "+id);
                    if(id == redundantQuads.get(i)){
                        qit.remove();
                        break;
                    }
                }
            }
        }

    }

    /**
     * Other methods from the Flow.Analysis interface.
     * See Flow.java for the meaning of these methods.
     * These need to be filled in.
     */
    public boolean isForward () { return true; }
    public Flow.DataflowObject getEntry() { 
        Flow.DataflowObject result = newTempVar();
        result.copy(entry);
        return result;
    }
    public Flow.DataflowObject getExit() { 
        Flow.DataflowObject result = newTempVar();
        result.copy(exit); 
        return result;
    }
    public void setEntry(Flow.DataflowObject value) {
        entry.copy(value);
    }
    public void setExit(Flow.DataflowObject value) {
        exit.copy(value);
    }
    public Flow.DataflowObject getIn(Quad q) { 
        Flow.DataflowObject result = newTempVar();
        result.copy(in[q.getID()]); 
        return result;
    }
    public Flow.DataflowObject getOut(Quad q) { 
        Flow.DataflowObject result = newTempVar();
        result.copy(out[q.getID()]); 
        return result;
    }
    public void setIn(Quad q, Flow.DataflowObject value) {
        in[q.getID()].copy(value);
    }
    public void setOut(Quad q, Flow.DataflowObject value) {
        out[q.getID()].copy(value);
    }
    public Flow.DataflowObject newTempVar() { return new VarSet(); }

    private TransferFunction transferfn = new TransferFunction ();
    public void processQuad(Quad q) {
        transferfn.NullCheckVar = "";
        transferfn.val.copy(in[q.getID()]);
        Helper.runPass(q, transferfn);
        out[q.getID()].copy(transferfn.val);
        isNullCheckQuad[q.getID()] = transferfn.NullCheckVar;
    }

    /* TransferFunction is the QuadVisitor that applies the transfer function */
    public static class TransferFunction extends QuadVisitor.EmptyVisitor
    {
        // val is IN before visitors are called. val becomes OUT after visitors are executed
        VarSet val;
        String NullCheckVar = "";

        /*
         *  Since visitNullCheck (in appropriate) will be called before visitQuad
         *  according to joeq.Compiler.Quad.QuadVisitor doc, transferfn is:
         *  val = (valUchecked)-defined. Note that this is same as (val-defined)Uchecked
         *  since a single variable is exclusively checked/defined per quad. 
         */

        @Override
        public void visitNullCheck(Quad q) {
            for (RegisterOperand use : q.getUsedRegisters()) {
                val.addVar(use.getRegister().toString());
                NullCheckVar = use.getRegister().toString();
            }
        }

        @Override
        public void visitQuad(Quad q) {
            for (RegisterOperand def : q.getDefinedRegisters()) {
                val.removeVar(def.getRegister().toString());
            }
        }
    }

}

