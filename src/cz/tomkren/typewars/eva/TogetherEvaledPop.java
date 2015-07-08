package cz.tomkren.typewars.eva;

import cz.tomkren.helpers.F;

import java.util.ArrayList;
import java.util.List;

/** Created by tom on 1. 7. 2015. */

public class TogetherEvaledPop<Indiv extends FitIndiv> implements EvaledPop<Indiv> {

    private Distribution<Indiv> popDist;
    private List<Indiv> terminators;

    public TogetherEvaledPop(List<Indiv> pop, TogetherFitFun tFitness, int gen) {
        tFitness.initGeneration(gen);
        terminators = new ArrayList<>();

        // TODO vyřešit "doRecomputeFitVal". Musí se odfiltrovat předtím, než se pošle do tFitness a pak je tam zas vložit ...

        List<Object> objs = F.map(pop, FitIndiv::computeValue);
        List<FitVal> fitVals = tFitness.getFitVals(objs);

        int popSize = pop.size();
        if (popSize != fitVals.size()) {

            throw new Error("There must be same number of individuals and fitness values! "+popSize +" != "+ fitVals.size());
        }

        for (int i = 0; i < popSize; i++) {

            Indiv ind = pop.get(i);
            FitVal fitVal = fitVals.get(i);
            ind.setFitVal(fitVal);

            if (fitVal.isOK()) {
                terminators.add(ind);
            }
        }

        popDist = new Distribution<>(pop);
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
