package cz.tomkren.typewars.eva;

import cz.tomkren.helpers.AB;
import cz.tomkren.helpers.Checker;
import cz.tomkren.helpers.F;
import cz.tomkren.helpers.Log;
import cz.tomkren.typewars.*;
import cz.tomkren.typewars.reusable.QuerySolver;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Created by tom on 6.7.2015. */

public class OneParamMutation extends PolyTreeMutation {

    private final Random rand;
    private List<AB<Integer,Double>> shiftsWithProbabilities;


    public OneParamMutation(double operatorProbability, Random rand, List<AB<Integer,Double>> shiftsWithProbabilities) {
        super(operatorProbability);
        this.shiftsWithProbabilities = shiftsWithProbabilities;
        this.rand = rand;
    }

    @Override
    public PolyTree mutate(PolyTree tree) {
        List<SubtreePos> posesWithParams = tree.getAllSubtreePosesWhere(OneParamMutation::hasCodeNodeWithMoreThanZeroParams);
        if (posesWithParams.isEmpty()) {return tree;}

        SubtreePos subtreePos = F.randomElement(posesWithParams, rand);
        PolyTree selectedSubtree = tree.getSubtree(subtreePos);

        PolyTree newSubtree = selectedSubtree.randomlyShiftOneParam(rand, shiftsWithProbabilities);
        return tree.changeSubtree(subtreePos, newSubtree);
    }

    private static boolean hasCodeNodeWithMoreThanZeroParams(PolyTree tree) {
        return tree.getCodeNode() instanceof CodeNodeWithParams && ((CodeNodeWithParams) tree.getCodeNode()).numParams() > 0;
    }





    public static void main(String[] args) {

        Checker ch = new Checker(); // seed : 7268261639444262123L

        SmartLib lib = SmartLib.DATA_SCIENTIST_WITH_PARAMS_01;

        QuerySolver qs = new QuerySolver(lib, ch.getRandom());


        OneParamMutation oneParamMutation = new OneParamMutation(1.0, ch.getRandom(), Arrays.asList(
                AB.mk(-2,0.1),
                AB.mk(-1,0.4),
                AB.mk( 1,0.4),
                AB.mk( 2,0.1)
        ));

        List<PolyTree> trees = qs.uniformGenerateWithRandomizedParams("D => LD", 20, 100);

        for (PolyTree tree : trees) {
            Log.it("-----------------------------------------------------------------");

            Log.it(tree);
            PolyTree mutant = oneParamMutation.mutate(tree);
            //ch.eqStrSilent(tree, mutant); //  předělat vypisování aby todle neplatilo -- hotovo

            ch.it(((TypedDag) tree.computeValue()).toJson());
            ch.it( ((TypedDag)mutant.computeValue()).toJson() );

        }


        ch.results();
    }
}
