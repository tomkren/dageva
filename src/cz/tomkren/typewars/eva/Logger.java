package cz.tomkren.typewars.eva;

import java.util.List;

public interface Logger<Indiv extends Probable> {
    void logPop(int run, int generation, EvaledPop<Indiv> pop);

    void logErrorIndivs(int generation, List<Object> errorIndiv);

    default void logRun(int run) {}

}