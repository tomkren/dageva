package cz.tomkren.typewars.phalanx;

import com.google.common.base.Joiner;
import cz.tomkren.helpers.*;
import cz.tomkren.typewars.*;
import cz.tomkren.typewars.basicgen.CreationLog;
import cz.tomkren.typewars.eva.FitVal;
import cz.tomkren.typewars.reusable.QuerySolver;

import java.util.*;

/** Created by tom on 6.7.2015. */

public class SkeletonCar {

    private Island initialIsland;
    private CreationLog creationLog;
    private final Deque<SkeletonLeaf> leafs;

    private final TreeSet<PolyTree> population;

    private SubtreePos actualPos;

    private double bestFitVal;

    public TreeSet<PolyTree> getPopulation() {return population;}
    /*public void setIsland(Island island) {this.initialIsland = island;}*/

    public SkeletonCar(Type goalType, Island initialIsland) {

        this.initialIsland = initialIsland;
        creationLog = CreationLog.START;
        leafs = new ArrayDeque<>();

        population = new TreeSet<>(QuerySolver.compareTrees);

        actualPos = SubtreePos.root(null);

        leafs.offer(new SkeletonLeaf(goalType, initialIsland.getLib()));

        tryStep();
    }

    private SkeletonCar(CreationLog creationLog, Deque<SkeletonLeaf> leafs, TreeSet<PolyTree> population, SubtreePos actualPos, Island initialIsland) {
        this.creationLog = creationLog;
        this.leafs = leafs;
        this.initialIsland = initialIsland;
        this.population = population;
        this.actualPos =actualPos;

        tryStep();
    }

    private void tryStep() {

        if (leafs.isEmpty()) {
            return;
        }

        int numPossibleSymbols = leafs.getFirst().numPossibleSymbols();

        if (numPossibleSymbols == 0) {

            throw new TODO();

        } else if (numPossibleSymbols == 1) {

            SkeletonLeaf singletonLeaf = leafs.removeFirst();

            SmartSym sym = singletonLeaf.getFirstSym();

            creationLog = new CreationLog.Step(creationLog, sym.getCodeNode());

            Integer sonIndex = singletonLeaf.getSonIndex();
            actualPos = sonIndex == null ? actualPos : SubtreePos.classicStep(actualPos, singletonLeaf.getSonIndex(), null);

            List<Type> argTypes = sym.getArgTypes();

            int i = 0;
            for (List<SmartSym> possibleSons: sym.getApplicableSons()) {
                leafs.offer(new SkeletonLeaf(argTypes.get(i),possibleSons, i));
                i++;
            }

            //Log.it(" >>> "+ toString());

            tryStep();

        }
    }

    public boolean isSplittable() {
        return !(leafs.isEmpty() || (leafs.size() == 1 && leafs.getFirst().numPossibleSymbols() == 1));
    }

    public AA<SkeletonCar> split(Random rand) {

        if (leafs.isEmpty()) {
            return null;
        }

        if (leafs.getFirst().numPossibleSymbols() == 0) {
            throw new Error("nemělo by nastat protože by mel odchytit asi tryStep() nebo něco..");
        }

        if (leafs.size() == 1 && leafs.getFirst().numPossibleSymbols() == 1) {
            return null;
        }

        Deque<SkeletonLeaf> leafs1 = new ArrayDeque<>(leafs);
        Deque<SkeletonLeaf> leafs2 = new ArrayDeque<>(leafs);

        leafs1.removeFirst();
        leafs2.removeFirst();

        AA<SkeletonLeaf> twoLeafs = leafs.getFirst().split(rand);

        leafs1.addFirst(twoLeafs._1());
        leafs2.addFirst(twoLeafs._2());

        AA<TreeSet<PolyTree>> twoPopulations = splitPopulation(population, actualPos, twoLeafs._1());

        SkeletonCar car1 = new SkeletonCar(creationLog, leafs1, twoPopulations._1(), actualPos, initialIsland); //, sub, nextVarId, treeSize);
        SkeletonCar car2 = new SkeletonCar(creationLog, leafs2, twoPopulations._2(), actualPos, initialIsland); //, sub, nextVarId, treeSize);

        return new AA<>(car1,car2);
    }




    public static AA<TreeSet<PolyTree>> splitPopulation(Set<PolyTree> population, SubtreePos splitPos, SkeletonLeaf leaf1) {

        TreeSet<PolyTree> population1 = new TreeSet<>();
        TreeSet<PolyTree> population2 = new TreeSet<>();

        for (PolyTree tree : population) {

            PolyTree subtree = tree.getSubtree(splitPos);

            (leaf1.contains(subtree.getName()) ? population1 : population2).add(tree);
        }

        return new AA<>(population1, population2);
    }




    public SkeletonTree toTree() {
        return toTree(0, creationLog.toRowList(), new ArrayList<>(leafs));
    }

    @Override
    public String toString() {
        return toString(0, creationLog.toRowList(), new ArrayList<>(leafs)); // + " -vs- "+ toTree().toString();
    }

    protected void computeBestFitVal() {
        double best = -Integer.MAX_VALUE;
        for (PolyTree tree :  population) {
            double fitVal = tree.getFitVal().getVal();
            if (fitVal > best) {
                best = fitVal;
            }
        }
        bestFitVal = best;
    }

    public double getBestFitVal() {
        return bestFitVal;
    }

    public String showPopulation(boolean sortByFitVal) {
        StringBuilder sb = new StringBuilder();

        for (PolyTree tree :  (sortByFitVal ? F.sort(population,t->-t.getFitVal().getVal()) : population) ) {
            String shortStr = tree.toStringWithoutParams();
            FitVal fitVal = tree.getFitVal();
            sb.append("    ").append(fitVal == null ? "" : "["+fitVal.getVal()+"]\t").append(shortStr).append("   ");
            sb.append(F.fillStr(150 - shortStr.length(), ".")).append("   ").append(tree).append("\n");
        }

        return sb.toString();
    }

    private SkeletonTree toTree(int i, List<CreationLog.Row> rows, List<SkeletonLeaf> leafsList) {
        if (rows.size() <= i) {
            return leafsList.get(i-rows.size()).toTree();
        } else {
            CreationLog.Row row = rows.get(i);
            return new SkeletonTree(row.getNode().getName(), F.map(row.getSuccIs(), j -> toTree(j, rows, leafsList)));
        }
    }

    private String toString(int i, List<CreationLog.Row> rows, List<SkeletonLeaf> leafsList) {

        if (rows.size() <= i) {
            return leafsList.get(i-rows.size()).toString();
        }

        CreationLog.Row row = rows.get(i);
        ProtoNode node = row.getNode();
        List<String> sons = F.map(row.getSuccIs(), j -> toString(j, rows, leafsList));

        String name = node.getName();

        return sons.isEmpty() ? name : "("+name+" "+ Joiner.on(" ").join(sons) +")" ;
    }

    public static void main(String[] args) {
        Checker ch = new Checker(-7770084078076581017L);
        Random rand = ch.getRandom();

        SmartLib lib =  SmartLib.DATA_SCIENTIST_WITH_PARAMS_01;
        Type goal = Types.parse("D => LD");

        Log.it(lib);

        QuerySolver qs = new QuerySolver(lib, ch.getRandom());

        Archipelago archipelago = new Archipelago(goal,0, 2, 15, qs);

        Island island = new Island(15, archipelago);

        SkeletonCar ic = island.getSkeletons().get(0);

        ch.itln(ic, "{SVC,logR,gaussianNB,DT,dia0,dia}");


        AA<SkeletonCar> ics1 = ic.split(rand);

        ch.it(ics1._1(),   "{SVC,logR,gaussianNB,DT,dia0}");
        ch.itln(ics1._2(), "(dia {PCA,kBest} {split} {vote})");

        AA<SkeletonCar> ics2 = ics1._2().split(rand);

        ch.it(ics2._1(), "(dia kBest (split {kMeans,copy} {nil,cons}) vote)");
        ch.itln(ics2._2(), "(dia PCA (split {kMeans,copy} {nil,cons}) vote)");

        AA<SkeletonCar> ics3 = ics2._2().split(rand);

        ch.it(ics3._1(),"(dia PCA (split kMeans {nil,cons}) vote)");
        ch.itln(ics3._2(), "(dia PCA (split copy {nil,cons}) vote)");

        AA<SkeletonCar> ics4 = ics3._2().split(rand);

        ch.it(ics4._1(), "(dia PCA (split copy (cons {nil,PCA,kBest,kMeans,copy,SVC,logR,gaussianNB,DT,vote,dia0,split,cons,dia} {nil,cons})) vote)");
        ch.itln(ics4._2(), "(dia PCA (split copy nil) vote)");

        AA<SkeletonCar> ics5 = ics4._1().split(rand);

        ch.it(ics5._1(), "(dia PCA (split copy (cons {PCA,kBest,copy,logR,gaussianNB,DT,vote,split,cons,dia} {nil,cons})) vote)");
        ch.itln(ics5._2(), "(dia PCA (split copy (cons {nil,kMeans,SVC,dia0} {nil,cons})) vote)");

        AA<SkeletonCar> ics6 = ics5._2().split(rand);

        ch.it(ics6._1(), "(dia PCA (split copy (cons {nil,kMeans,SVC} {nil,cons})) vote)");
        ch.itln(ics6._2(), "(dia PCA (split copy (cons (dia0 {split} {vote}) {nil,cons})) vote)");


        //-------

        //ch.it( island.generateOneWithParams(1)  );
        //ch.it(island.generateOneWithParams(1));
        //ch.it( island.generateOneWithParams(1)  );
        //ch.it(island.generateOneWithParams(1));
        //ch.it( island.generateOneWithParams(1)  );
        //ch.itln(island.generateOneWithParams(1));

        //ch.it( island.generateOneWithParams(9)  );
        //ch.it( island.generateOneWithParams(9)  );
        //ch.it( island.generateOneWithParams(9)  );
        //ch.it( island.generateOneWithParams(9)  );
        //ch.it( island.generateOneWithParams(9)  );
        //ch.it(island.generateOneWithParams(9));

        // 1  2  3  4  5  6  7  8   9  10   11   12   13
        //[4, 0, 0, 0, 0, 0, 0, 0, 32, 64, 128, 256, 512]

        ch.results();
    }

}
