package cz.tomkren.typewars.phalanx;

import cz.tomkren.helpers.*;
import cz.tomkren.typewars.PolyTree;
import cz.tomkren.typewars.SmartLib;
import cz.tomkren.typewars.Type;
import cz.tomkren.typewars.Types;
import cz.tomkren.typewars.dag.DataScientistFitness;
import cz.tomkren.typewars.eva.FitIndiv;
import cz.tomkren.typewars.eva.FitVal;
import cz.tomkren.typewars.eva.TogetherFitFun;
import cz.tomkren.typewars.reusable.QuerySolver;

import java.lang.reflect.Array;
import java.util.*;

/** Created by tom on 7.7.2015. */

public class Archipelago {

    private Deque<Island> islands;

    private final Type goalType;
    private final QuerySolver querySolver;
    private final int numTreesForOneIslandPerGeneration;

    private int generation;
    private List<PolyTree> treesToEvaluate;
    private final List<PolyTree> terminators;

    public Archipelago(Type goalType, int numIslands, int numTreesForOneIslandPerGeneration, int maxGenerateTreeSize, QuerySolver querySolver) {
        this.goalType = goalType;
        this.querySolver = querySolver;
        this.numTreesForOneIslandPerGeneration = numTreesForOneIslandPerGeneration;

        islands = new ArrayDeque<>();
        islands.add(new Island(maxGenerateTreeSize, this));

        generation = 0;
        treesToEvaluate = new ArrayList<>();
        terminators = new ArrayList<>();

        // todo ošetřit limitem aby nemohlo běhat nekonečně...
        while (islands.size() < numIslands) {

            Island islandToSplit = islands.removeFirst();
            AA<Island> twoIslands = islandToSplit.split();
            if (twoIslands == null) {
                islands.addLast(islandToSplit);
            } else {
                islands.addLast(twoIslands._1());
                islands.addLast(twoIslands._2());
            }

        }
    }

    public boolean isSomethingWaitingToEvaluate() {
        return !treesToEvaluate.isEmpty();
    }

    public void generateTrees() {
        generateTrees(numTreesForOneIslandPerGeneration);
        Log.itln("Vygenerováno!");
    }

    private void generateTrees(int numToGenerate) {

        int numToGenerateInNextTry = 0;

        for (Island island : islands) {
            for (int i = 0; i < numToGenerate; i++) {
                PolyTree newTree = island.generateOneWithParams();
                if (newTree == null) {
                    Log.it("BUM-PRáSK!");
                    numToGenerateInNextTry ++;
                } else {
                    treesToEvaluate.add(newTree);
                }
            }
        }

        if (numToGenerateInNextTry > 0) {
            generateRestTrees(numToGenerateInNextTry);
        }
    }

    private void generateRestTrees(int num) {

        while (true) {
            for (Island island : islands) {

                PolyTree newTree = island.generateOneWithParams();
                if (newTree == null) {
                    Log.it("BUM-PRáSK! 2");
                } else {
                    treesToEvaluate.add(newTree);
                    num--;
                }

                if (num == 0) {return;}
            }
        }

    }

    public void evaluateTrees(TogetherFitFun fitFun) {

        fitFun.initGeneration(generation);

        List<Object> objs = F.map(treesToEvaluate, FitIndiv::computeValue);
        List<FitVal> fitVals = fitFun.getFitVals(objs);

        int numTrees = treesToEvaluate.size();
        if (numTrees != fitVals.size()) {
            throw new Error("There must be same number of individuals and fitness values! "+numTrees +" != "+ fitVals.size());
        }

        for (int i = 0; i < numTrees; i++) {
            PolyTree tree = treesToEvaluate.get(i);
            FitVal fitVal = fitVals.get(i);
            tree.setFitVal(fitVal);

            if (fitVal.isOK()) {
                terminators.add(tree);
            }
        }

        for (Island island : islands) {
            island.computeBestFitVal();
        }

        treesToEvaluate = new ArrayList<>();

        // ..todo vyřešit distribuci, tzn analog k: popDist = new Distribution<>(pop);

        generation++;
    }

    public void operateIslands() {

        Log.itln("Splitting and merging islands...");

        if (isSomethingWaitingToEvaluate()) {
            throw new Error("Something is waiting to evaluate.");
        }

        // TODO udelat efektivně !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        List<Island> islandList = new ArrayList<>(islands);

        List<Island> splittableIslands = F.filter(islandList, Island::isSplittable);

        if (splittableIslands.size() < 3) {
            Log.it("Less than 3 splittable islands, aborting island operations.");
            return;
        }

        Island island1 = F.removeRandomElement(splittableIslands, getRand());
        Island island2 = F.removeRandomElement(splittableIslands, getRand());
        Island island3 = F.removeRandomElement(splittableIslands, getRand());

        islandList.remove(island1);
        islandList.remove(island2);
        islandList.remove(island3);


        List<Island> sortedResult = F.sort(Arrays.asList(island1, island2, island3), isl -> -isl.getBestFitVal());

        Island winner = sortedResult.get(0);
        Island loser1 = sortedResult.get(1);
        Island loser2 = sortedResult.get(2);

        // TODO asi dát tu '0.8' sem

        AA<Island> twoFromWinner = winner.split();
        Island mergedLosers = Island.merge(loser1, loser2);

        islandList.add(twoFromWinner._1());
        islandList.add(twoFromWinner._2());
        islandList.add(mergedLosers);

        islandList = F.sort(islandList, isl ->- isl.getBestFitVal());

        islands = new ArrayDeque<>(islandList);

    }


    public Deque<Island> getIslands() {return islands;}
    public Type getGoalType() {return goalType;}
    public QuerySolver getQuerySolver() {return querySolver;}

    public Random getRand() {return querySolver.getRand();}


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        int i=0;
        for (Island island : (isSomethingWaitingToEvaluate() ? islands : F.sort(islands,isl->-isl.getBestFitVal()) )  ) {
            sb.append(isSomethingWaitingToEvaluate()?"":"["+island.getBestFitVal()+"] ").append("island #").append(++i).append("\n").append(island).append("\n");
        }

        return sb.toString();
    }

    public void performOneGeneration(TogetherFitFun fitness) {

        Log.itln("-- Generation "+generation+" "+F.fillStr(300,"-"));

        generateTrees();
        Log.it(this);

        evaluateTrees(fitness);
        Log.it(this);

        operateIslands();
        Log.it(this);
    }

    public static void main(String[] args) {

        // TODO na tomhle seedu se to rozzseká při splitu !!!
        Checker ch = new Checker(); //(4081111614699016120L);

        DataScientistFitness fitness = new DataScientistFitness("http://127.0.0.1:8080", "wilt.csv", false); // "winequality-white.csv");

        Type goalType = Types.parse("D => LD");
        SmartLib lib  = SmartLib.DATA_SCIENTIST_WITH_PARAMS_01;

        int numIslands = 8; //4;
        int numTreesForOneIslandPerGeneration = 2;
        int numGenerations = 8;

        int maxGenerateTreeSize = 15;

        Archipelago archipelago = new Archipelago(goalType, numIslands, numTreesForOneIslandPerGeneration, maxGenerateTreeSize, new QuerySolver(lib,ch.getRandom()));
        Log.it(archipelago);

        for (int i = 0; i < numGenerations; i++) {
            archipelago.performOneGeneration(fitness);
        }

        ch.results();
    }

}
