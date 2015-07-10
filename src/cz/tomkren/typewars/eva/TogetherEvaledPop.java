package cz.tomkren.typewars.eva;

import cz.tomkren.helpers.F;
import cz.tomkren.helpers.Log;
import cz.tomkren.typewars.TypedDag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/** Created by tom on 1. 7. 2015. */

public class TogetherEvaledPop<Indiv extends FitIndiv> implements EvaledPop<Indiv> {

    private Distribution<Indiv> popDist;
    private List<Indiv> terminators;

    public TogetherEvaledPop(List<Indiv> pop, TogetherFitFun tFitness, int gen, Comparator<Indiv> comparator, Logger<Indiv> logger) {
        tFitness.initGeneration(gen);
        terminators = new ArrayList<>();

        // TODO vyřešit "doRecomputeFitVal". Musí se odfiltrovat předtím, než se pošle do tFitness a pak je tam zas vložit ...

        if (comparator != null) {
            Collections.sort(pop, comparator);
        }
        //Log.list(F.map(pop, in -> ((PolyTree) in).toStringWithoutParams()  ));


        List<Object> objs = F.map(pop, FitIndiv::computeValue);

        List<Object> okObjs = F.filter(objs, o -> !((TypedDag) o).isMalformed()); // TODO OBECNE, TOHLE JEN HACK NA BUG !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!


        boolean errorOccurred = false;
        if (okObjs.size() != objs.size()) { // caught ERROR individual !!!
            errorOccurred = true;

            List<Object> koObjs = F.filter(objs, o -> ((TypedDag) o).isMalformed());

            // LOG THEM ALL !!!!

            logger.logErrorIndivs(gen, koObjs);

            System.err.println("ERROR individual(s) !!!");

            Log.itln("\n\n\n\n ERROR individual(s) !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n\n\n\n");
            Log.list(F.map(koObjs, o -> ((TypedDag) o).toJson()));
            Log.itln("\n\n\n\n");

        }

        List<FitVal> fitVals = tFitness.getFitVals(okObjs);

        if (okObjs.size() != fitVals.size()) {
            throw new Error("There must be same number of individuals and fitness values! "+okObjs.size() +" != "+ fitVals.size());
        }

        List<Indiv> newPop_errorCase = new ArrayList<>();

        int popSize = pop.size();

        if (errorOccurred) {

            int j = 0;
            for (int i = 0; i < popSize; i++) {


                Indiv ind = pop.get(i);

                if ( ! ((TypedDag)objs.get(i)).isMalformed() && j < fitVals.size() ) {

                    newPop_errorCase.add(ind);

                    FitVal fitVal = fitVals.get(j);
                    ind.setFitVal(fitVal);

                    j++;
                }
            }

        } else {

            for (int i = 0; i < popSize; i++) {

                Indiv ind = pop.get(i);
                FitVal fitVal = fitVals.get(i);
                ind.setFitVal(fitVal);

                if (fitVal.isOK()) {
                    terminators.add(ind);
                }
            }
        }

        popDist = new Distribution<>( errorOccurred ? newPop_errorCase : pop );
    }

    @Override
    public boolean isTerminating() {
        return !terminators.isEmpty();
    }

    @Override
    public Indiv getBestIndividual() {
        return popDist.getBest();
    }

    @Override
    public Distribution<Indiv> getIndividuals() {
        return popDist;
    }

    @Override
    public List<Indiv> getTerminators() {
        return terminators;
    }

    @Override
    public String toString() {
        return popDist.toString();
    }
}
