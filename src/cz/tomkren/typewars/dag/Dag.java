package cz.tomkren.typewars.dag;

import com.google.common.base.Joiner;
import cz.tomkren.helpers.Checker;
import cz.tomkren.helpers.F;
import cz.tomkren.kutil2.items.Int2D;
import cz.tomkren.typewars.PolyTree;
import cz.tomkren.typewars.Vertex;

import java.util.*;
import java.util.function.Consumer;

public class Dag {

    private List<Vertex> ins;
    private List<Vertex> outs;

    private int width, height;

    public Dag(String name) {
        ins  = new ArrayList<>();
        outs = new ArrayList<>();
        Vertex v = new Vertex(name);
        ins.add(v);
        outs.add(v);

        width  = 1;
        height = 1;
    }

    public List<Vertex> getIns() {return ins;}
    public List<Vertex> getOuts() {return outs;}




    public void addSource(Dag source) {

        source.serialMove(this);
        height = source.height;
        width  = source.width;

        source.outs.forEach(out -> ins.forEach(out::addSuccessor));
        ins = source.ins;
    }

    public void addTarget(Dag target) {

        serialMove(target);

        target.ins.forEach(in -> outs.forEach(v -> v.addSuccessor(in)));
        outs = target.outs;
    }

    public void parallelMerge(Dag dag2) {

        parallelMove(dag2);

        ins .addAll(dag2.ins);
        outs.addAll(dag2.outs);
    }


    private void parallelMove(Dag dag2) {
        dag2.move(width, 0);
        width += dag2.width;

        double yMove = 0.5 * Math.abs(height - dag2.height);

        if (height < dag2.height) {
            move(0,yMove);
        } else if (height > dag2.height) {
            dag2.move(0,yMove);
        }

        height = Math.max(height, dag2.height);
    }

    private void serialMove(Dag dag2) {
        dag2.move(0, height);
        height += dag2.height;

        double xMove = 0.5 * Math.abs(width - dag2.width);

        if (width < dag2.width) {
            move(xMove, 0);
        } else if (width > dag2.width) {
            dag2.move(xMove,0);
        }

        width = Math.max(width, dag2.width);
    }



    /**Pozor na to, že používá pak přímo outs ze vstupu, čili dag2 už by se neměl dál používat. */
    public void serialMerge(Dag dag2) {

        List<Vertex> ins2 = dag2.ins;

        int n = outs.size();
        if (n != ins2.size()) {

            if (ins2.size() == 1) {

                addTarget(dag2);

                return;
                //throw new Error("Aspon to bylo 1");
            } else {
                throw new MergeException("Serial merge needs outs.size() == dag.ins.size(),"+
                        " but it was outs.size(): "+outs.size()+" dag.ins.size(): "+ins2.size());
            }
        }

        serialMove(dag2);

        for (int i = 0; i < n; i++) {
            outs.get(i).addSuccessor( ins2.get(i) );
        }

        outs = dag2.outs;
    }

    public static Dag serialMerge(Dag d1, Dag d2) {
        d1.serialMerge(d2);
        return d1;
    }

    public static Dag serialMerge(List<Dag> dags) {
        return F.reduce(dags,(d1,d2)->serialMerge(d1,d2));
    }


    public static enum Boxes { DIA, SPLIT, SERI }

    public static Dag fromTree(PolyTree tree) {
        String name = tree.getName();

        // todo : debilní hack, pak udělat normálně systematicky TFGP štajlem až to bude ozkoušený
        if (name.equals(":")) {return fromList(tree);}

        if (tree.isTerminal()) {return new Dag(name);}
        List<Dag> subDags = F.map(tree.getSons(), Dag::fromTree);
        try {
            switch (Boxes.valueOf(name)) {
                case SERI  : return mkSeri(subDags);
                case DIA   : return mkDia(subDags);
                case SPLIT : return mkSplit(subDags);
                default: throw new Error("Unimplemented box '" + name + "'.");
            }
        } catch (IllegalArgumentException e) {
            throw new Error("Unsupported box '"+name+".");
        }
    }

    private static Dag fromList(PolyTree tree) {
        List<PolyTree> sons = tree.getSons();
        checkSize("FROM_LIST", sons, 2);

        PolyTree x  = tree.getSons().get(0);
        PolyTree xs = tree.getSons().get(1);

        Dag xDag = fromTree(x);

        if (!xs.getName().equals("[]")) {
            xDag.parallelMerge( fromList(xs) ); // todo neefektivní kuli tomu jak je implementován parallelMerge
        }
        return xDag;
    }

    private static Dag mkSeri(List<Dag> subDags) {
        checkSize("SERI", subDags, 2);

        Dag dag1 = subDags.get(0);
        Dag dag2 = subDags.get(1);

        return serialMerge(dag1, dag2);
    }

    private static Dag mkDia(List<Dag> subDags) {
        checkSize("DIA", subDags, 3);

        Dag preProcess = subDags.get(0);
        Dag core       = subDags.get(1);
        Dag union      = subDags.get(2);

        Dag ret = serialMerge(preProcess, core);
        ret.addTarget(union);
        return ret;
    }

    private static Dag mkSplit(List<Dag> subDags) {
        checkSize("SPLIT", subDags, 2);

        Dag spliter  = subDags.get(0);
        Dag parallel = subDags.get(1);

        parallel.addSource(spliter);
        return parallel;
    }



    private static <A> void checkSize(String name, List<A> xs, int n) {
        if (xs.size() != n) {
            throw new Error(name+" must have "+n+" arguments.");
        }
    }

    public void move(double dx, double dy) {
        forEachVertex(v -> {
            v.moveX(dx);
            v.moveY(dy);
        });
    }


    public void forEachVertex(Consumer<Vertex> f) {
        Set<Vertex> vSet = new HashSet<>();
        Set<Vertex> processed = new HashSet<>();
        vSet.addAll(ins);
        while (!vSet.isEmpty()) {
            Set<Vertex> vSet_new = new HashSet<>();
            for (Vertex v1 : vSet) {
                f.accept(v1);
                processed.add(v1);
                v1.getSuccessors().forEach(v2 -> {
                    if (!processed.contains(v2)) {
                        vSet_new.add(v2);
                    }
                });
            }
            vSet = vSet_new;
        }
    }

    public String toKutilXML(Int2D pos) {
        StringBuilder sb = new StringBuilder();

        forEachVertex( v -> {
            v.toKutilXML(sb, pos);
            sb.append('\n');
        });

        return sb.toString();
    }

    public String toJson() {
        StringBuilder sb = new StringBuilder();

        forEachVertex( v -> {
            v.toJson(sb);
            sb.append('\n');
        });

        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        List<Vertex> vsList = new ArrayList<>();
        Set<Vertex> vsSet   = new HashSet<>();


        vsList.addAll(ins);

        while (!vsList.isEmpty()) {

            sb.append(Joiner.on(' ').join(vsList)).append('\n');

            sb2.append(Joiner.on(' ').join(F.map(vsList, Vertex::successorsStr))).append('\n');


            List<Vertex> temp = new ArrayList<>();

            for (Vertex v1 : vsList) {
                for (Vertex v2 : v1.getSuccessors()) {
                    if (!vsSet.contains(v2)) {
                        temp.add(v2);
                        vsSet.add(v2);
                    }
                }
            }

            vsList = temp;
            vsSet  = new HashSet<>();

        }

        return sb.toString() +"\n"+ sb2.toString() ; // +"\n"+toKutilXML(new Int2D(0,0));
    }

    public static void main(String[] args) {
        Checker ch = new Checker();


        Dag d1 = new Dag("d1");
        ch.it(d1);

        d1.parallelMerge( new Dag("d2") );
        ch.it(d1);



        ch.results();
    }

    public static class MergeException extends RuntimeException {
        public MergeException(String message) {
            super(message);
        }
    }
}
