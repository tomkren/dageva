package cz.tomkren.typewars.mains;


import cz.tomkren.helpers.AB;
import cz.tomkren.helpers.Checker;
import cz.tomkren.helpers.Log;
import cz.tomkren.typewars.PolyTree;
import cz.tomkren.typewars.SmartLib;
import cz.tomkren.typewars.Type;
import cz.tomkren.typewars.Types;
import cz.tomkren.typewars.dag.DataScientistFitness;
import cz.tomkren.typewars.eva.*;
import cz.tomkren.typewars.reusable.QuerySolver;

import java.util.Arrays;
import java.util.Random;

/** Created by tom on 6.7.2015. */

public class DagEva02 {

    public static void main(String[] args) {

        //Ok už.. 3798799110567534523L .... Probability for dist mus be >= 0, was -4.066709679942271E-4
        //Ok už.. 7293879928187169227L Warning: Score < 0 ... -0.0015471911547373818

        Checker ch = new Checker();
        Random rand = ch.getRandom();


        DataScientistFitness fitness = new DataScientistFitness("http://127.0.0.1:8080", "winequality-white.csv", true);// "wilt.csv");
        SmartLib lib = SmartLib.mkDataScientistLib01FromParamsInfo(fitness.getAllParamsInfo());
        QuerySolver querySolver = new QuerySolver(lib, rand);
        Type goalType = Types.parse("D => LD");

        int numGenerations = 16;  //10;
        int populationSize = 32;  //8;
        boolean saveBest = true;

        int generatingMaxTreeSize  = 15;
        int mutationMaxSubtreeSize = 10;

        IndivGenerator<PolyTree> generator = new RandomParamsPolyTreeGenerator(goalType, generatingMaxTreeSize, querySolver);
        Selection<PolyTree> selection = new Selection.Tournament<>(0.8, rand);
        Distribution<Operator<PolyTree>> operators = new Distribution<>(Arrays.asList(
                new BasicTypedXover(0.3, rand),
                new SameSizeSubtreeMutation(0.3, querySolver, mutationMaxSubtreeSize),
                new OneParamMutation(0.3, ch.getRandom(), Arrays.asList(
                        AB.mk(-2,0.1),
                        AB.mk(-1,0.4),
                        AB.mk( 1,0.4),
                        AB.mk( 2,0.1)
                )),
                new CopyOp<>(0.1)
        ));

        Evolver<PolyTree> evolver = new Evolver.Opts<>(fitness, new EvoOpts(numGenerations,populationSize,saveBest), generator, operators, selection, new PolyTreeEvolutionLogger(), rand).mk();

        Log.it("Generating initial population...");
        evolver.startRun();

        ch.results();
    }



}
