package cz.tomkren.typewars.eva;

import cz.tomkren.helpers.Log;
import cz.tomkren.typewars.PolyTree;

/** Created by pejsek on 6.7.2015. */

public class PolyTreeEvolutionLogger implements Logger<PolyTree> {

    @Override
    public void logPop(int run, int generation, EvaledPop<PolyTree> pop) {
        PolyTree best = pop.getBestIndividual();
        Log.it("gen" + generation + " \t best: [" + best.getProbability() + "] " + best);
        Log.it(pop);

        /*for (PolyTree tree : pop.getIndividuals().getList()) {
            Log.it(  );
        }*/

    }


}
