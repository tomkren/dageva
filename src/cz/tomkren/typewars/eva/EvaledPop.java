package cz.tomkren.typewars.eva;

import java.util.List;

public interface EvaledPop<Indiv extends Probable> {
    boolean isTerminating();
    Indiv getBestIndividual();
    Distribution<Indiv> getIndividuals();
    List<Indiv> getTerminators();
}
